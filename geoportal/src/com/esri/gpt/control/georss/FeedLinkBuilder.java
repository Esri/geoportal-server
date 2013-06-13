/*
 * Copyright 2012 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.georss;

import com.esri.gpt.catalog.search.ResourceLink;
import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Feed link builder.
 */
public class FeedLinkBuilder {

  private final String metadataPath        = "/rest/document";
  private final String resourceDetailsPath = "/catalog/search/resource/details.page";
  private final String resourcePreviewPath = "/catalog/search/resource/livedata-preview.page";
  private final String imagePath           = "/catalog/images";
  
  private String baseContextPath;
  private MessageBroker messageBroker;
  private Map<String, String> labels = new HashMap<String, String>();

  public FeedLinkBuilder(String baseContextPath, MessageBroker messageBroker) {
    this.baseContextPath = baseContextPath;
    this.messageBroker = messageBroker;
  }

  public void build(IFeedRecord record) {
    buildMetadataLink(record);
    buildDetailsLink(record);
    buildOpenLink(record);
    buildPreviewLink(record);
    buildContentTypeLink(record);
    buildThumbnailLink(record);
  }

  protected void buildMetadataLink(IFeedRecord record) {
    String uuid = Val.chkStr(record.getUuid());
    String url = baseContextPath + metadataPath + "?id=" + encodeUrlParam(uuid);
    String resourceKey = "catalog.rest.viewFullMetadata";
    ResourceLink link = makeLink(url, ResourceLink.TAG_METADATA, resourceKey);
    record.getResourceLinks().add(link);
  }

  protected void buildDetailsLink(IFeedRecord record) {
    String uuid = Val.chkStr(record.getUuid());
    String url = baseContextPath + this.resourceDetailsPath + "?uuid=" + encodeUrlParam(uuid);
    String resourceKey = "catalog.rest.viewDetails";
    ResourceLink link = makeLink(url, ResourceLink.TAG_DETAILS, resourceKey);
    record.getResourceLinks().add(link);
  }

  protected void buildPreviewLink(IFeedRecord record) {
    String id = Val.chkStr(record.getUuid());
    String resourceUrl = Val.chkStr(record.getResourceUrl());
    String serviceType = Val.chkStr(record.getServiceType()).toLowerCase();
    String tmp = resourceUrl.toLowerCase();
    if ((resourceUrl.length() == 0)) {
      return;
    } else if (tmp.indexOf("?getxml=") != -1) {
      return;
    }

    String sFilter = Val.chkStr(ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration().getParameters().getValue("resourceLinkBuilder.preview.filter"));
    if (sFilter.length() > 0) {
      try {
        Pattern pattern = Pattern.compile(sFilter, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(resourceUrl);
        if (matcher.matches()) {
          return;
        }
      } catch (Exception ex) {
      }
    }
    String url = baseContextPath + resourcePreviewPath + "?uuid=" + encodeUrlParam(id) + "&url=" + encodeUrlParam(resourceUrl);
    if (serviceType.length() > 0) {
      url += "&resourceType=" + serviceType;
    }
    String infoUrl = baseContextPath + this.metadataPath + "?f=html&showRelativeUrl=true&id=" + encodeUrlParam(id);
    url += "&info=" + encodeUrlParam(infoUrl);
    String resourceKey = "catalog.rest.preview";
    ResourceLink link = this.makeLink(url, ResourceLink.TAG_PREVIEW, resourceKey);
    if (serviceType.length() > 0) {
      link.getParameters().add(new StringAttribute("resourceType", serviceType));
    }
    record.getResourceLinks().add(link);
  }
  
  protected void buildOpenLink(IFeedRecord record) {
    String resourceUrl = Val.chkStr(record.getResourceUrl());
    String serviceType = Val.chkStr(record.getServiceType()).toLowerCase();

    // return if we cannot open
    if ((resourceUrl.length() == 0) || serviceType.equals("aims")) {
      return;
    }

    String resourceKey = "catalog.rest.open";
    ResourceLink link = this.makeLink(resourceUrl, ResourceLink.TAG_OPEN,
        resourceKey);
    if (serviceType.length() > 0) {
      link.getParameters().add(new StringAttribute("resourceType", serviceType));
    }
    record.getResourceLinks().add(link);
  }
  
  protected void buildContentTypeLink(IFeedRecord record) {
    String contentType = record.getContentType().isEmpty()? "unknown": record.getContentType();
    
    String url = baseContextPath + imagePath + "/ContentType_" + contentType + ".png";
    String resourceKey = "catalog.search.filterContentTypes." + contentType;
    ResourceLink link = this.makeLink(url, ResourceLink.TAG_CONTENTTYPE, resourceKey);
    record.getResourceLinks().setIcon(link);
    record.getResourceLinks().add(link);
  }
  
  protected void buildThumbnailLink(IFeedRecord record) {
    IFeedAttribute thumbUrl = record.getData(IFeedRecord.STD_COLLECTION_INDEX).get("thumbnail.url");
    if (thumbUrl!=null && thumbUrl.getValue() instanceof List && !((List)thumbUrl.getValue()).isEmpty()) {
      Object oVal = ((List)thumbUrl.getValue()).get(0);
      if (oVal instanceof IFeedAttribute && ((IFeedAttribute)oVal).getValue() instanceof String) {
        String url = (String)((IFeedAttribute)oVal).getValue();
        String resourceKey = "catalog.rest.thumbNail";
        ResourceLink link = this.makeLink(url, ResourceLink.TAG_THUMBNAIL, resourceKey);
        record.getResourceLinks().setThumbnail(link);
      }
    }
  }

  protected ResourceLink makeLink(String url, String tag, String resourceKey) {
    ResourceLink link = new ResourceLink();
    link.setUrl(url);
    link.setTag(tag);
    link.setLabelResourceKey(resourceKey);
    link.setLabel(Val.chkStr(makeLabel(resourceKey)));
    return link;
  }

  protected String makeLabel(String resourceKey) {
    String label = labels.get(resourceKey);
    if (label == null) {
      label = messageBroker.retrieveMessage(resourceKey);
      labels.put(resourceKey, label);
    }
    label = Val.chkStr(label);
    if (label.length() > 0) {
      return label;
    } else {
      return resourceKey;
    }
  }

  protected String encodeUrlParam(String value) {
    value = Val.chkStr(value);
    try {
      return URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      LogUtil.getLogger().fine("Unsupported encoding: UTF-8");
      return value;
    }
  }
}
