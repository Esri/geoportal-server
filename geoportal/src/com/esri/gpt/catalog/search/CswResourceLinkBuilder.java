/*
 * Copyright 2014 Esri, Inc..
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
package com.esri.gpt.catalog.search;

import com.esri.gpt.catalog.context.CatalogConfiguration;
import static com.esri.gpt.catalog.search.ResourceLinkBuilder.RESOURCE_TYPE;
import com.esri.gpt.control.georss.CswContext;
import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.search.SearchXslRecord;
import com.esri.gpt.framework.util.Val;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;

/**
 * Resource link builder for external CSW endpoints.
 */
public class CswResourceLinkBuilder extends ResourceLinkBuilder {

  private final CswContext cswContext;
  private static final String MAP_VIEWER_URL_PATTERN = "http://www.arcgis.com/home/webmap/viewer.html?url=${url}";
  private String mapViewerUrlPattern = MAP_VIEWER_URL_PATTERN;

  public static ResourceLinkBuilder newBuilder(RequestContext context, CswContext cswContext,
    HttpServletRequest servletRequest, MessageBroker messageBroker) {

    // initialize
    if (context == null) {
      context = RequestContext.extract(servletRequest);
    }

    if (messageBroker == null) {
      messageBroker = new MessageBroker();
      messageBroker.setBundleBaseName("gpt.resources.gpt");
    }

    CatalogConfiguration catCfg = context.getCatalogConfiguration();
    String cswResourceLinkBuilderClassName = Val.chkStr(catCfg.getParameters().getValue("cswResourceLinkBuilder"));
    String cswMapViewerUrlPattern = Val.chkStr(catCfg.getParameters().getValue("cswMapViewerUrlPattern"), MAP_VIEWER_URL_PATTERN);
    
    ResourceLinkBuilder linkBuilder = null;
    if (cswResourceLinkBuilderClassName.length() == 0) {
      linkBuilder = new CswResourceLinkBuilder(cswContext, cswMapViewerUrlPattern);
    } else {
      try {
        Class<?> cls = Class.forName(cswResourceLinkBuilderClassName);
        Constructor<?> constructor = cls.getConstructor(CswContext.class,String.class);
        linkBuilder = (ResourceLinkBuilder) constructor.newInstance(cswContext,cswMapViewerUrlPattern);
      } catch (Exception ex) {
        linkBuilder = new CswResourceLinkBuilder(cswContext, cswMapViewerUrlPattern);
      }
    }
    
    linkBuilder.initialize(servletRequest, context, messageBroker);

    return linkBuilder;
  }

  private CswResourceLinkBuilder(CswContext cswContext, String mapViewerUrlPattern) {
    this.cswContext = cswContext;
    this.mapViewerUrlPattern = mapViewerUrlPattern;
  }

  /**
   * Builds the bind-able resource links associated with a resultant search
   * record.
   *
   * @param xRecord the underlying CSW record
   * @param record the search result record
   */
  public void build(SearchXslRecord xRecord, SearchResultRecord record) {

    // determine the primary resource URL
    this.determineResourceUrl(xRecord, record);

//    // build the content type and thumbnail links
//    this.buildContentTypeLink(xRecord, record);
    this.buildThumbnailLink(xRecord, record);
//
//    // build remaining links
//    this.buildOpenLink(xRecord, record);
//    this.buildPreviewLink(xRecord, record);
//    this.buildAGSLinks(xRecord, record);
    this.buildMetadataLink(xRecord, record);
//    this.buildAddToMapLink(xRecord, record);
    this.buildResourceLink(xRecord, record);
//    this.buildWebsiteLink(xRecord, record);
//    this.buildDetailsLink(xRecord, record);
//    this.buildCustomLinks(xRecord, record);
  }

  @Override
  protected void buildAddToMapLink(SearchXslRecord xRecord, SearchResultRecord record) {
    String[] allowedServices = {"ags", "wms", "kml"};

    if (!xRecord.getLinks().readShowLink(ResourceLink.TAG_ADDTOMAP)) {
      return;
    }

    String resourceUrl = Val.chkStr(record.getResourceUrl());
    String serviceType = Val.chkStr(record.getServiceType()).toLowerCase();

    Arrays.sort(allowedServices);
    if (Arrays.binarySearch(allowedServices, serviceType) < 0) {
      return;
    }

    if (resourceUrl.indexOf("q=") >= 0 && resourceUrl.indexOf("user=") >= 0 && resourceUrl.indexOf("max=") >= 0 && resourceUrl.indexOf("dest=") >= 0 && resourceUrl.indexOf("destuser=") >= 0) {
      // look like this is AGP-2-AGP registration; don'e generate preview link
      return;
    }

    // return if we cannot preview
    String tmp = resourceUrl.toLowerCase();
    if ((resourceUrl.length() == 0)) {
      return;
    } else if (tmp.indexOf("?getxml=") != -1) {
      return;
    }

    // build the link
    String url = mapViewerUrlPattern.replaceAll("\\$\\{url\\}",encodeUrlParam(resourceUrl));
    String resourceKey = "catalog.rest.preview";
    ResourceLink link = this.makeLink(url, ResourceLink.TAG_PREVIEW, resourceKey);
    if (serviceType.length() > 0) {
      link.getParameters().add(new StringAttribute(RESOURCE_TYPE, serviceType));
    }
    if (record.isExternal()) {
      link.setForExtenalRecord(true);
    }
    record.getResourceLinks().add(link);
  }

  protected void buildResourceLink(SearchXslRecord xRecord, SearchResultRecord record) {
    String[] allowedServices = {"ags", "wms", "kml"};

    if (!xRecord.getLinks().readShowLink(ResourceLink.TAG_RESOURCE)) {
      return;
    }

    String resourceUrl = Val.chkStr(record.getResourceUrl());
    String serviceType = Val.chkStr(record.getServiceType()).toLowerCase();

    Arrays.sort(allowedServices);
    if (Arrays.binarySearch(allowedServices, serviceType) < 0) {
      return;
    }

    if (resourceUrl.indexOf("q=") >= 0 && resourceUrl.indexOf("user=") >= 0 && resourceUrl.indexOf("max=") >= 0 && resourceUrl.indexOf("dest=") >= 0 && resourceUrl.indexOf("destuser=") >= 0) {
      // look like this is AGP-2-AGP registration; don'e generate preview link
      return;
    }

    // return if we cannot preview
    String tmp = resourceUrl.toLowerCase();
    if ((resourceUrl.length() == 0)) {
      return;
    } else if (tmp.indexOf("?getxml=") != -1) {
      return;
    }

    // build the link
    String resourceKey = "catalog.rest.resource";
    ResourceLink link = this.makeLink(encodeUrlParam(resourceUrl), ResourceLink.TAG_RESOURCE, resourceKey);
    if (serviceType.length() > 0) {
      link.getParameters().add(new StringAttribute(RESOURCE_TYPE, serviceType));
    }
    if (record.isExternal()) {
      link.setForExtenalRecord(true);
    }
    record.getResourceLinks().add(link);
  }
  
  @Override
  protected void buildMetadataLink(SearchXslRecord xRecord, SearchResultRecord record) {
    if (!xRecord.getLinks().readShowLink(ResourceLink.TAG_METADATA)) {
      return;
    }
    String uuid = Val.chkStr(record.getUuid());
    String url = "";
    if (uuid.length() > 0) {
      // if external we need to use a different route.  Because
      // of authentication issues.  Internal is automatically authenticated
      // while if we used rest getrecord for external, the workflow to
      // get the user to input username and password may be more complicated since
      // we'd have to broker the authentication
      url = this.getBaseContextPath() + this.externalMetadataPath + "?uuid="
        + encodeUrlParam(uuid) + "&cswUrl="
        + encodeUrlParam(this.cswContext.getCswUrl())
        + "&cswProfileId="
        + encodeUrlParam(this.cswContext.getCswProfileId());
    }
    if (url.length() > 0) {
      String resourceKey = "catalog.rest.viewFullMetadata";
      ResourceLink link = this.makeLink(url, ResourceLink.TAG_METADATA,
        resourceKey);
      record.getResourceLinks().add(link);
    }
  }

}
