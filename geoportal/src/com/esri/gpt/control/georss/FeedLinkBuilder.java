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

import com.esri.gpt.catalog.search.IMapViewer;
import com.esri.gpt.catalog.search.MapViewerFactory;
import com.esri.gpt.catalog.search.ResourceIdentifier;
import com.esri.gpt.catalog.search.ResourceLink;
import com.esri.gpt.catalog.search.ResourceLinkBuilder;
import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Feed link builder.
 */
public class FeedLinkBuilder {
  private static final Logger LOGGER = Logger.getLogger(FeedLinkBuilder.class.getCanonicalName());
  private static final TreeSet<String> knownContentTypes = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
  static {
    knownContentTypes.add("unknown");
    knownContentTypes.add("liveData");
    knownContentTypes.add("downloadableData");
    knownContentTypes.add("offlineData");
    knownContentTypes.add("staticMapImage");
    knownContentTypes.add("document");
    knownContentTypes.add("application");
    knownContentTypes.add("geographicService");
    knownContentTypes.add("clearinghouse");
    knownContentTypes.add("mapFiles");
    knownContentTypes.add("geographicActivities");
  }
  private static final TreeMap<String,String> additionalContentTypes = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
  static {
    additionalContentTypes.put("CSW", "geographicService");
  }
  
  private final String metadataPath = "/rest/document";
  private final String resourceDetailsPath = "/catalog/search/resource/details.page";
  private final String resourcePreviewPath = "/catalog/search/resource/livedata-preview.page";
  private final String imagePath = "/catalog/images";
  private RequestContext context;
  private String baseContextPath;
  private MessageBroker messageBroker;
  private Map<String, String> labels = new HashMap<String, String>();
  private final String mapViewerUrl;
  private ResourceIdentifier resourceIdentifier;

  /**
   * Creates instance of the link builder.
   *
   * @param context request context
   * @param baseContextPath base context path
   * @param messageBroker message broker
   */
  public FeedLinkBuilder(RequestContext context, String baseContextPath, MessageBroker messageBroker) {
    this.context = context;
    this.baseContextPath = baseContextPath;
    this.messageBroker = messageBroker;

    this.mapViewerUrl = context.getCatalogConfiguration().getSearchConfig().getMapViewerUrl();
    this.resourceIdentifier = ResourceIdentifier.newIdentifier(context);
  }

  /**
   * Gets map viewer URL.
   *
   * @return map viewer URL
   */
  protected String getMapViewerUrl() {
    return mapViewerUrl;
  }

  /**
   * Buids links and resources.
   *
   * @param record record
   */
  public void build(IFeedRecord record) {
    // links
    buildOpenLink(record);
    buildMetadataLink(record);
    buildDetailsLink(record);
    buildPreviewLink(record);
    buildAGSLinks(record);
    buildAddToMapLink(record);

    // resource links
    buildContentTypeResource(record);
    buildThumbnailResource(record);
  }

  /**
   * Builds ArcGIS links.
   * @param record record
   */
  protected void buildAGSLinks(IFeedRecord record) {
    String resourceUrl = Val.chkStr(record.getResourceUrl());
    String serviceType = Val.chkStr(record.getServiceType()).toLowerCase();
    String restUrl = Val.chkStr(resourceIdentifier.guessAgsServiceRestUrl(resourceUrl));
    String url;
    String resourceKey;
    ResourceLink link;

    if ((restUrl.length() > 0) && serviceType.equals("ags")) {

      // kml
      if ((restUrl.toLowerCase().endsWith("/mapserver") || restUrl.toLowerCase().endsWith("/imageserver"))) {
        url = restUrl + "/kml/mapImage.kmz";
        if (restUrl.toLowerCase().endsWith("/imageserver")) {
          url = restUrl + "/kml/image.kmz";
        }
        resourceKey = "catalog.rest.addToGlobeKml";
        link = this.makeLink(url, ResourceLink.TAG_AGSKML, resourceKey);
        record.getResourceLinks().add(link);
      }

      // nmf
      if ((restUrl.toLowerCase().endsWith("/mapserver") || restUrl.toLowerCase().endsWith("/imageserver"))) {
        url = restUrl + "?f=nmf";
        resourceKey = "catalog.rest.addToGlobeNmf";
        link = this.makeLink(url, ResourceLink.TAG_AGSNMF, resourceKey);
        record.getResourceLinks().add(link);
      }

      // lyr
      if ((restUrl.toLowerCase().endsWith("/mapserver") || restUrl.toLowerCase().endsWith("/imageserver") || restUrl.toLowerCase().endsWith("/globeserver"))) {
        url = restUrl + "?f=lyr";
        resourceKey = "catalog.rest.addToArcMap";
        link = this.makeLink(url, ResourceLink.TAG_AGSLYR, resourceKey);
        record.getResourceLinks().add(link);
      }
    }
  }

  /**
   * Builds 'Add To Map' link.
   * @param record record
   */
  protected void buildAddToMapLink(IFeedRecord record) {
    String resourceUrl = Val.chkStr(record.getResourceUrl());
    String serviceType = Val.chkStr(record.getServiceType()).toLowerCase();
    String serviceName = Val.chkStr(record.getService());
    String viewerUrl = Val.chkStr(this.getMapViewerUrl());
    IMapViewer iMapViewer = null;

    if ((viewerUrl.length() != 0)) {
      // return if a map viewer link cannot be built
      if (resourceUrl.length() == 0 || (serviceType.length() == 0)) {
        return;
      } else {
        boolean canHandle = (serviceType.equalsIgnoreCase(ResourceLinkBuilder.ServiceType.AGS.name())
                || serviceType.equalsIgnoreCase(ResourceLinkBuilder.ServiceType.AIMS.name())
                || serviceType.equalsIgnoreCase(ResourceLinkBuilder.ServiceType.WMS.name())
                || serviceType.equalsIgnoreCase(ResourceLinkBuilder.ServiceType.WFS.name()) || serviceType
                .equalsIgnoreCase(ResourceLinkBuilder.ServiceType.WCS.name()));
        if (!canHandle) {
          return;
        }
      }

    } else {
      iMapViewer = MapViewerFactory.createMapViewer(resourceUrl, serviceType, record, context);
      if (iMapViewer == null) {
        return;
      }
      String addToMapUrl = iMapViewer.readAddToMapUrl();
      if (addToMapUrl == null || "".equals(addToMapUrl)) {
        return;
      }
      String resourceKey = "catalog.rest.addToMap";
      ResourceLink link = this.makeLink(addToMapUrl, ResourceLink.TAG_ADDTOMAP,
              resourceKey);
      link.setTarget(iMapViewer.readTarget());
      record.getResourceLinks().add(link);
      return;

    }
    // maybe url is already an add to map url?  if so no modigications are required
    if ((resourceUrl.toLowerCase().startsWith("http:") || resourceUrl
            .toLowerCase().startsWith("https:"))
            && resourceUrl.toLowerCase().contains("resources=map")) {
    } else {

      // set up an ArcGIS soap service for the viewer
      if (serviceType.equals("ags")) {
        resourceUrl = Val.chkStr(resourceIdentifier.guessAgsMapServerSoapUrl(resourceUrl));
      }

      // set up an ArcIMS service for the viewer
      int nIdx = resourceUrl.toLowerCase().indexOf(
              "/servlet/com.esri.esrimap.esrimap?servicename=");
      if (nIdx > 0) {
        resourceUrl = resourceUrl.substring(0, nIdx) + "/"
                + resourceUrl.substring(nIdx + 46);
      }

      // set up an OGC service for the viewer
      if (serviceType.equalsIgnoreCase("wms")
              || serviceType.equalsIgnoreCase("wfs")
              || serviceType.equalsIgnoreCase("wcs")) {
        nIdx = resourceUrl.toLowerCase().indexOf("?");
        if (nIdx > 0) {
          resourceUrl = resourceUrl.substring(0, nIdx);
        }
      }
    }

    // TODO: explanation?
    resourceUrl = resourceUrl.replaceAll("/$", "")
            + (serviceName.length() > 0 ? "/" + serviceName : "");
    resourceUrl = Val.chkStr(resourceUrl);

    if (resourceUrl.length() > 0) {
      String url = viewerUrl + "?resources=map:" + serviceType + "@"
              + resourceUrl;
      String resourceKey = "catalog.rest.addToMap";
      ResourceLink link = this.makeLink(url, ResourceLink.TAG_ADDTOMAP,
              resourceKey);
      record.getResourceLinks().add(link);
    }

    String url = "";
    try {
      RequestContext rContext = context;
      ServletRequest sRequest = rContext.getServletRequest();
      if (sRequest instanceof HttpServletRequest) {
        HttpServletRequest hSRequest = (HttpServletRequest) sRequest;
        String path = Val.chkStr(hSRequest.getPathInfo()).toLowerCase();
        if (path.contains("catalog/search/search.page")
                || path.contains("catalog/main/home.page")) {
          url = "javascript:GptUtils.popUp(\'" + url + "\',"
                  + "GptMapViewer.TITLE," + "GptMapViewer.dimensions.WIDTH,"
                  + "GptMapViewer.dimensions.HEIGHT);";
        }
      }

    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Error creating 'Add to Map' link.", e);
    }
  }

  /**
   * Builds 'Metadata' link.
   * @param record record
   */
  protected void buildMetadataLink(IFeedRecord record) {
    String uuid = Val.chkStr(record.getUuid());
    String url = baseContextPath + metadataPath + "?id=" + encodeUrlParam(uuid);
    String resourceKey = "catalog.rest.viewFullMetadata";
    ResourceLink link = makeLink(url, ResourceLink.TAG_METADATA, resourceKey);
    record.getResourceLinks().add(link);
  }

  /**
   * Builds 'Details' link.
   * @param record record
   */
  protected void buildDetailsLink(IFeedRecord record) {
    String uuid = Val.chkStr(record.getUuid());
    String url = baseContextPath + this.resourceDetailsPath + "?uuid=" + encodeUrlParam(uuid);
    String resourceKey = "catalog.rest.viewDetails";
    ResourceLink link = makeLink(url, ResourceLink.TAG_DETAILS, resourceKey);
    record.getResourceLinks().add(link);
  }

  /**
   * Builds 'Preview' link.
   * @param record record
   */
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

  /**
   * Builds 'Open' link.
   * @param record record
   */
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

  /**
   * Builds 'Content Type' resource link.
   * @param record record
   */
  protected void buildContentTypeResource(IFeedRecord record) {
    String contentType = record.getContentType().isEmpty() ? "unknown" : record.getContentType();
    if (!knownContentTypes.contains(contentType)) {
      if (additionalContentTypes.containsKey(contentType)) {
        contentType = additionalContentTypes.get(contentType);
      } else {
        contentType = "unknown";
      }
    }

    String url = baseContextPath + imagePath + "/ContentType_" + contentType + ".png";
    String resourceKey = "catalog.search.filterContentTypes." + contentType;
    ResourceLink link = this.makeLink(url, ResourceLink.TAG_CONTENTTYPE, resourceKey);
    record.getResourceLinks().setIcon(link);
    record.getResourceLinks().add(link);
  }

  /**
   * Builds 'Thumbnail' resource link.
   * @param record record
   */
  protected void buildThumbnailResource(IFeedRecord record) {
    IFeedAttribute thumbUrl = record.getData(IFeedRecord.STD_COLLECTION_INDEX).get("thumbnail.url");
    if (thumbUrl != null && thumbUrl.getValue() instanceof List && !((List) thumbUrl.getValue()).isEmpty()) {
      Object oVal = ((List) thumbUrl.getValue()).get(0);
      if (oVal instanceof IFeedAttribute && ((IFeedAttribute) oVal).getValue() instanceof String) {
        String url = (String) ((IFeedAttribute) oVal).getValue();
        String resourceKey = "catalog.rest.thumbNail";
        ResourceLink link = this.makeLink(url, ResourceLink.TAG_THUMBNAIL, resourceKey);
        record.getResourceLinks().setThumbnail(link);
      }
    }
  }

  /**
   * Makes a resource link.
   * @param url URL
   * @param tag tag
   * @param resourceKey resource key for the label
   * @return link
   */
  protected ResourceLink makeLink(String url, String tag, String resourceKey) {
    ResourceLink link = new ResourceLink();
    link.setUrl(url);
    link.setTag(tag);
    link.setLabelResourceKey(resourceKey);
    link.setLabel(Val.chkStr(makeLabel(resourceKey)));
    return link;
  }

  /**
   * Makes label.
   * @param resourceKey resource key for the label
   * @return label
   */
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

  /**
   * Encodes URL parameter.
   * @param value parameter to encode
   * @return encoded parameter
   */
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
