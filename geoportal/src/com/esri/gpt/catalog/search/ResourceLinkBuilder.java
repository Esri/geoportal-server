/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.catalog.search;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.catalog.search.SearchEngineCSW.Scheme;
import com.esri.gpt.control.georss.RestQueryServlet;
import com.esri.gpt.control.georss.SearchResultRecordAdapter;
import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.search.DcList;
import com.esri.gpt.framework.search.SearchXslRecord;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds the collection of bind-able links associated with a document or resource. 
 */
public class ResourceLinkBuilder {

/**
 * Service types.
 */
public static enum ServiceType {AGS, AIMS, WMS, WCS, WFS}

public static final String        RESOURCE_TYPE        = "resourceType";

private static final Logger LOG = Logger.getLogger(
    ResourceLinkBuilder.class.getCanonicalName());

/** instance variables ====================================================== */
private String                    baseContextPath      = "";
private final String              externalMetadataPath = "/catalog/search/dsFullMetadata.page";
private final String              imagePath            = "/catalog/images";
private String                    mapViewerUrl         = "";
private final String              metadataPath         = "/rest/document";
private final String              previewPath          = "/catalog/livedata/preview.page";
private final String              resourceDetailsPath  = "/catalog/search/resource/details.page";
private final String              resourcePreviewPath          = "/catalog/search/resource/livedata-preview.page";

private final Map<String, String> labels               = new HashMap<String, String>();
private MessageBroker             messageBroker;
private RequestContext            requestContext;
private ResourceIdentifier        resourceIdentifier;
       
/** constructors ============================================================ */

/** Default constructor. */
public ResourceLinkBuilder() {
}

/** properties ============================================================== */

/**
 * Gets the base context path associated with the HTTP servlet request.
 * <br/>(<i>http://&lt;host:port&gt;/&lt;Context&gt;</i>)
 * @return the base context path
 */
protected String getBaseContextPath() {
	
	String path = this.getRelativePath();
	if(path == null) {
		path = this.baseContextPath;
	}
  return path;
}

/**
 * Gets the relative path.
 * 
 * @return the relative path
 */
private String getRelativePath() {

	try {
		@SuppressWarnings("unchecked")
		Map<String, String> extraArgsMap = (Map<String, String>) this
		    .getRequestContext().getObjectMap()
		    .get(RestQueryServlet.EXTRA_REST_ARGS_MAP);
		if (extraArgsMap == null) {
			return null;
		}
		Object obj = extraArgsMap
		    .get(RestQueryServlet.PARAM_KEY_SHOW_RELATIVE_URLS);
		if (obj == null) {
			return null;
		}
		boolean showRel = Val.chkBool(obj.toString(), false);
		if (showRel == false) {
			return null;
		}
		if (Val.chkStr(this.baseContextPath).equals("")) {
			return null;
		}
		String tmpContextPath = this.baseContextPath.replaceAll("/$", "");
		tmpContextPath = tmpContextPath.replaceAll(".*/", "/");
		return tmpContextPath;
	} catch (Throwable e) {
		LOG.log(Level.FINER, "", e);
	}
	return null;
}

/**
 * Sets the base context path associated with the HTTP servlet request.
 * <br/>(<i>http://&lt;host:port&gt;/&lt;Context&gt;</i>)
 * @param path the base context path
 */
protected void setBaseContextPath(String path) {
  this.baseContextPath = Val.chkStr(path);
}

/**
 * Gets the map viewer url.
 * @return the map viewer url
 */
protected String getMapViewerUrl() {
  return this.mapViewerUrl;
}

/**
 * Gets the resource bundle message broker.
 * @return the message broker
 */
protected MessageBroker getMessageBroker() {
  return messageBroker;
}

/**
 * Sets the resource bundle message broker.
 * @param messageBroker the message broker
 */
protected void setMessageBroker(MessageBroker messageBroker) {
  this.messageBroker = messageBroker;
}

/**
 * Gets the associated request context.
 * @return the request context
 */
protected RequestContext getRequestContext() {
  return this.requestContext;
}

/**
 * Gets the resource identifier associated with the request.
 * @return the resource identifier
 */
protected ResourceIdentifier getResourceIdentifier() {
  return this.resourceIdentifier;
}

/**
 * Sets the resource identifier associated with the request.
 * @param resourceIdentifier the resource identifier
 */
protected void setResourceIdentifier(ResourceIdentifier resourceIdentifier) {
  this.resourceIdentifier = resourceIdentifier;
}

/** methods ================================================================= */

/**
 * Builds the bind-able resource links associated with a resultant search record.
 * @param xRecord the underlying CSW record
 * @param record the search result record 
 */
public void build(SearchXslRecord xRecord, SearchResultRecord record) {
  
  // determine the primary resource URL
  this.determineResourceUrl(xRecord, record);

  // build the content type and thumbnail links
  this.buildContentTypeLink(xRecord, record);
  this.buildThumbnailLink(xRecord, record);

  // build remaining links
  this.buildOpenLink(xRecord, record);
  this.buildPreviewLink(xRecord, record);
  this.buildAGSLinks(xRecord, record);
  this.buildAddToMapLink(xRecord, record);
  this.buildWebsiteLink(xRecord, record);
  this.buildDetailsLink(xRecord, record);
  this.buildMetadataLink(xRecord, record);
  this.buildCustomLinks(xRecord, record);
}

/**
 * Builds the link associated with adding a service to the map viewer.
 * @param xRecord the underlying CSW record
 * @param record the search result record
 */
protected void buildAddToMapLink(SearchXslRecord xRecord, SearchResultRecord record) {
  if(!xRecord.getLinks().readShowLink(ResourceLink.TAG_ADDTOMAP)) {
    return;
  }
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
      boolean canHandle = (serviceType.equalsIgnoreCase(ServiceType.AGS.name())
          || serviceType.equalsIgnoreCase(ServiceType.AIMS.name())
          || serviceType.equalsIgnoreCase(ServiceType.WMS.name())
          || serviceType.equalsIgnoreCase(ServiceType.WFS.name()) || serviceType
          .equalsIgnoreCase(ServiceType.WCS.name()));
      if (!canHandle)
        return;
    }

  } else {
    iMapViewer = MapViewerFactory.createMapViewer(resourceUrl, serviceType, 
        new SearchResultRecordAdapter(record), this.getRequestContext());
    if (iMapViewer == null) {
      return;
    }
    String addToMapUrl = iMapViewer.readAddToMapUrl();
    if(addToMapUrl == null || "".equals(addToMapUrl)) {
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
      resourceUrl = Val.chkStr(this.getResourceIdentifier()
          .guessAgsMapServerSoapUrl(resourceUrl));
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
    RequestContext rContext = this.getRequestContext();
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

  }

}

/**
 * Builds a set of links associated with a rest based ArcGIS map, image or globe 
 * service.
 * @param xRecord the underlying CSW record
 * @param record the search result record 
 */
protected void buildAGSLinks(SearchXslRecord xRecord, SearchResultRecord record) {
  String resourceUrl = Val.chkStr(record.getResourceUrl());
  String serviceType = Val.chkStr(record.getServiceType()).toLowerCase();
  String restUrl = Val.chkStr(this.getResourceIdentifier()
      .guessAgsServiceRestUrl(resourceUrl));
  String url;
  String resourceKey;
  ResourceLink link;

  if ((restUrl.length() > 0) && serviceType.equals("ags")) {

    // kml
    if (xRecord.getLinks().readShowLink(ResourceLink.TAG_AGSKML)
            && (restUrl.toLowerCase().endsWith("/mapserver") || restUrl.toLowerCase().endsWith("/imageserver"))) {
      url = restUrl + "/kml/mapImage.kmz";
      if (restUrl.toLowerCase().endsWith("/imageserver")) {
        url = restUrl + "/kml/image.kmz";
      }
      resourceKey = "catalog.rest.addToGlobeKml";
      link = this.makeLink(url, ResourceLink.TAG_AGSKML, resourceKey);
      record.getResourceLinks().add(link);
    }

    // nmf
    if (xRecord.getLinks().readShowLink(ResourceLink.TAG_AGSNMF)
            && (restUrl.toLowerCase().endsWith("/mapserver") || restUrl.toLowerCase().endsWith("/imageserver"))) {
      url = restUrl + "?f=nmf";
      resourceKey = "catalog.rest.addToGlobeNmf";
      link = this.makeLink(url, ResourceLink.TAG_AGSNMF, resourceKey);
      record.getResourceLinks().add(link);
    }

    // lyr
    if (xRecord.getLinks().readShowLink(ResourceLink.TAG_AGSLYR)
            && (restUrl.toLowerCase().endsWith("/mapserver") || restUrl.toLowerCase().endsWith("/imageserver") || restUrl.toLowerCase().endsWith("/globeserver"))) {
      url = restUrl + "?f=lyr";
      resourceKey = "catalog.rest.addToArcMap";
      link = this.makeLink(url, ResourceLink.TAG_AGSLYR, resourceKey);
      record.getResourceLinks().add(link);
    }
  }
}


/**
 * Checks if is request jsf and relative.
 *
 * @return true, if is request jsf and relative
 */
private boolean isRequestJsfAndRelative() {
	try {
		@SuppressWarnings("unchecked")
    Map<String, String> extraMap =
			(Map<String, String>)this.getRequestContext().getObjectMap().get(
				RestQueryServlet.EXTRA_REST_ARGS_MAP);
		boolean isJsfReq = 
			Val.chkBool(extraMap.get(
					RestQueryServlet.PARAM_KEY_IS_JSFREQUEST).toString(), false);
		boolean isShowRelUrls = 
			Val.chkBool(extraMap.get(
					RestQueryServlet.PARAM_KEY_SHOW_RELATIVE_URLS).toString(), false);
		return isShowRelUrls && isJsfReq;
	} catch (Throwable e) {
		LOG.log(Level.FINER, "This error is no big deal", e);
	}
	return false;
}
/**
 * Builds the link associated with the content type icon.
 * @param xRecord the underlying CSW record
 * @param record the search result record
 */
protected void buildContentTypeLink(SearchXslRecord xRecord,
    SearchResultRecord record) {
  String contentType = "";
  List<String> schemeVals = xRecord.getTypes().get(
      Scheme.CONTENTTYPE_FGDC.getUrn());
  if (schemeVals.size() < 1) {
    schemeVals = xRecord.getTypes().get(Scheme.CONTENTTYPE_ISO.getUrn());
  }
  if (schemeVals.size() < 1) {
    schemeVals = xRecord.getTypes().get(null);
  }
  if (schemeVals.size() > 0) {
    contentType = Val.chkStr(schemeVals.get(0));
  }

  if (contentType.length() > 0) {
    contentType = this.getResourceIdentifier()
        .guessArcIMSContentTypeFromResourceType(contentType);
  }
  if (contentType.length() == 0) {
    String resourceUrl = record.getResourceUrl();
    contentType = this.getResourceIdentifier().guessArcIMSContentTypeFromUrl(
        resourceUrl);
  }
  if (contentType.length() == 0)
    contentType = "unknown";

  record.setContentType(contentType);
  if (contentType.length() > 0) {
  	String tmpBaseContextPath = this.getBaseContextPath();
  	if(isRequestJsfAndRelative() == true) {
  		// caution! contextpath is put automatically by jsf when we put relative
  		// url
  		tmpBaseContextPath = "";
  	}
    String url = tmpBaseContextPath + this.imagePath + "/ContentType_"
        + contentType + ".png";
    String resourceKey = "catalog.search.filterContentTypes." + contentType;
    ResourceLink link = this.makeLink(url, ResourceLink.TAG_CONTENTTYPE,
        resourceKey);
    record.getResourceLinks().setIcon(link);
  }
}

/**
 * Builds the link associated with the metadata details page.
 * <br/>Records from an extenal repositories do not have a details link.
 * @param xRecord the underlying CSW record
 * @param record the search result record
 */
protected void buildDetailsLink(SearchXslRecord xRecord, SearchResultRecord record) {
  if(!xRecord.getLinks().readShowLink(ResourceLink.TAG_DETAILS)) {
    return;
  }
  String uuid = Val.chkStr(record.getUuid());
  String url = "";
  String resourceUrl = "";
  if (!record.isExternal() && (uuid.length() > 0)) {
  /*  url = this.getBaseContextPath() + this.detailsPath + "?uuid="
        + encodeUrlParam(uuid);
 */
    resourceUrl = this.getBaseContextPath() + this.resourceDetailsPath + "?uuid="
    + encodeUrlParam(uuid);     
  }
  if (resourceUrl.length() > 0) {
    String resourceKey = "catalog.rest.viewDetails";
   /* ResourceLink link = this.makeLink(url, ResourceLink.TAG_DETAILS,
        resourceKey);*/
   
    ResourceLink resourcePageLink = this.makeLink(resourceUrl, ResourceLink.TAG_DETAILS,
            resourceKey);
    
    // record.getResourceLinks().add(link);
    record.getResourceLinks().add(resourcePageLink);
  }
}

/**
 * Builds the link associated with full metadata retrieval.
 * <br/>Records from an extenal repositories do not have a details link.
 * @param xRecord the underlying CSW record
 * @param record the search result record
 */
protected void buildMetadataLink(SearchXslRecord xRecord, SearchResultRecord record) {
  if(!xRecord.getLinks().readShowLink(ResourceLink.TAG_METADATA)) {
    return;
  }
  String uuid = Val.chkStr(record.getUuid());
  String url = "";
  if (uuid.length() > 0) {
    if (record.isExternal()) {

      // if external we need to use a different route.  Because
      // of authentication issues.  Internal is automatically authenticated
      // while if we used rest getrecord for external, the workflow to
      // get the user to input username and password may be more complicated since
      // we'd have to broker the authentication
      

      url = this.getBaseContextPath() + this.externalMetadataPath + "?uuid="
          + encodeUrlParam(uuid) + "&rid="
          + encodeUrlParam(record.getExternalId());
    } else {
      url = this.getBaseContextPath() + this.metadataPath + "?id="
          + encodeUrlParam(uuid);
    }
  }
  if (url.length() > 0) {
    String resourceKey = "catalog.rest.viewFullMetadata";
    ResourceLink link = this.makeLink(url, ResourceLink.TAG_METADATA,
        resourceKey);
    record.getResourceLinks().add(link);
  }
}

/**
 * Builds a generic Open link for the resource URL associated with the record.
 * @param xRecord the underlying CSW record
 * @param record the search result record 
 */
protected void buildOpenLink(SearchXslRecord xRecord, SearchResultRecord record) {
  if(!xRecord.getLinks().readShowLink(ResourceLink.TAG_OPEN)) {
    return;
  }
  String resourceUrl = Val.chkStr(record.getResourceUrl());
  String serviceType = Val.chkStr(record.getServiceType()).toLowerCase();

  // return if we cannot open
  if ((resourceUrl.length() == 0) || serviceType.equals("aims")) {
    return;
  }

  // possibly we should reset AGS rest services to their home URL??
  /*
  if (serviceType.equals("ags")) {
    String agsRest = Val.chkStr(this.getResourceIdentifier().guessAgsServiceRestUrl(resourceUrl));
    if (agsRest.length() > 0) {
      resourceUrl = agsRest;
    } 
  }
   */

  String resourceKey = "catalog.rest.open";
  ResourceLink link = this.makeLink(resourceUrl, ResourceLink.TAG_OPEN,
      resourceKey);
  if (serviceType.length() > 0) {
    link.getParameters().add(new StringAttribute(RESOURCE_TYPE, serviceType));
  }
  record.getResourceLinks().add(link);
  
  //this.makeAddToMapFromFactory(resourceUrl, null, resourceKey, record);
  
  
}

/**
 * Builds the link associated with the resource preview page.
 * @param xRecord the underlying CSW record
 * @param record the search result record
 */
protected void buildPreviewLink(SearchXslRecord xRecord, SearchResultRecord record) {
  if(!xRecord.getLinks().readShowLink(ResourceLink.TAG_PREVIEW)) {
    return;
  }
  String id = Val.chkStr(record.getUuid());
  String rid = Val.chkStr(record.getExternalId());
  String resourceUrl = Val.chkStr(record.getResourceUrl());
  String serviceType = Val.chkStr(record.getServiceType()).toLowerCase();
  //String previewPath = Val.chkStr(this.previewPath);
  
  if (resourceUrl.indexOf("q=")>=0 && resourceUrl.indexOf("user=")>=0 && resourceUrl.indexOf("max=")>=0 && resourceUrl.indexOf("dest=")>=0 && resourceUrl.indexOf("destuser=")>=0) {
    // look like this is AGP-2-AGP registration; don'e generate preview link
    return;
  }

  // return if we cannot preview
  String tmp = resourceUrl.toLowerCase();
  if ((resourceUrl.length() == 0) || (previewPath.length() == 0)) {
    return;
  } else if (tmp.indexOf("?getxml=") != -1) {
    return;
  }
  
  String sFilter = Val.chkStr(ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration().getParameters().getValue("resourceLinkBuilder.preview.filter"));
  if (sFilter.length()>0) {
    try {
      Pattern pattern = Pattern.compile(sFilter, Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(resourceUrl);
      if (matcher.matches()) {
        return;
      }
    } catch (Exception ex) {
      
    }
  }

  // possibly we should reset AGS rest services to their home URL??
  /*
  if (serviceType.equals("ags")) {
    String agsRest = Val.chkStr(this.getResourceIdentifier().guessAgsServiceRestUrl(resourceUrl));
    if (agsRest.length() > 0) {
      resourceUrl = agsRest;
    } 
  }
   */

  // build the link TODO: metadata url not same as rest info url
  String url ="";
  if(record.isExternal()){
	  url = this.getBaseContextPath() + previewPath;
  }else{
	  url = this.getBaseContextPath() + resourcePreviewPath;
  }
  if (id.length() > 0) {
	  url += "?uuid=" +encodeUrlParam(id)+"&url=" + encodeUrlParam(resourceUrl);
  }else{
	  url += "?url=" + encodeUrlParam(resourceUrl);
  }
  if (serviceType.length() > 0) {
    url += "&" + RESOURCE_TYPE + "=" + serviceType;
  }
  boolean showInfo = xRecord.getLinks().readShowLink(
      ResourceLink.TAG_PREVIEW_PARAM_INFO, true);
  if (id.length() > 0 && showInfo == true ) {
    String infoUrl = this.getBaseContextPath() + this.metadataPath
        + "?f=html&showRelativeUrl=true&id=" + encodeUrlParam(id);
    if (record.isExternal() && rid.length() > 0) {
      infoUrl += "&rid=" + encodeUrlParam(rid);
    }
    url += "&info=" + encodeUrlParam(infoUrl);
  }
  String resourceKey = "catalog.rest.preview";
  ResourceLink link = this.makeLink(url, ResourceLink.TAG_PREVIEW, resourceKey);
  if (serviceType.length() > 0) {
    link.getParameters().add(new StringAttribute(RESOURCE_TYPE, serviceType));
  }
  if(record.isExternal()){
	  link.setForExtenalRecord(true);
  }
  record.getResourceLinks().add(link);
}

private boolean showThumbnail() {
	@SuppressWarnings("unchecked")
  Map<String, String> extraArgsMap = (Map<String, String>)
	this.getRequestContext().getObjectMap().get(
			RestQueryServlet.EXTRA_REST_ARGS_MAP);
	if(extraArgsMap == null) {
		return true;
	}
	return Val.chkBool(extraArgsMap.get(RestQueryServlet.PARAM_KEY_SHOW_THUMBNAIL), 
			true);

}
/**
 * Builds the link associated with the thumbnail.
 * @param xRecord the underlying CSW record
 * @param record the search result record
 */
protected void buildThumbnailLink(SearchXslRecord xRecord, SearchResultRecord record) {
	if(this.showThumbnail() == false) {
		return;
	}
  String url = "";
  DcList references = xRecord.getReferences();
  List<String> schemeVals = references.get(Scheme.THUMBNAIL_FGDC.getUrn());
  if (schemeVals.size() < 1) {
    schemeVals = references.get(Scheme.THUMBNAIL_ISO.getUrn());
  }
  if (schemeVals.size() > 0) {
    url = schemeVals.get(0);
  } else {
    for (DcList.Value reference : references) {
      if (reference != null) {
        String tmp = reference.getValue().toLowerCase();
        if (tmp.endsWith(".gif") || tmp.endsWith(".jpg")
            || tmp.endsWith(".jpeg") || tmp.endsWith(".png")
            || tmp.contains("/thumbnail?uuid")) {
          url = reference.getValue();
          break;
        }
      }
    }
  }
  url = Val.chkStr(url);

  url = this.checkUrl(url);
  if (url.length() > 0) {
    String resourceKey = "catalog.rest.thumbNail";
    ResourceLink link = this.makeLink(url, ResourceLink.TAG_THUMBNAIL,
        resourceKey);
    record.getResourceLinks().setThumbnail(link);
  }
}

/**
 * Builds the link associated with an organizational website.
 * @param xRecord the underlying CSW record
 * @param record the search result record
 */
protected void buildWebsiteLink(SearchXslRecord xRecord, SearchResultRecord record) {
  if(!xRecord.getLinks().readShowLink(ResourceLink.TAG_WEBSITE)) {
    return;
  }
  String url = "";
  DcList references = xRecord.getReferences();
  List<String> schemeVals = references.get(Scheme.ONLINK_FGDC.getUrn());
  if (schemeVals.size() < 1) {
    schemeVals = references.get(Scheme.ONLINK_ISO.getUrn());
  }
  if (schemeVals.size() > 0) {
    url = Val.chkStr(schemeVals.get(0));
  }

  String resourceKey = "catalog.rest.webSite";
  url = this.checkUrl(url);
  if (url.length() > 0) {
    if (url.startsWith("www."))
      url = "http://" + url;
    
    ResourceLink link = this.makeLink(url, ResourceLink.TAG_WEBSITE,
        resourceKey);
    record.getResourceLinks().add(link);
  }
  this.makeAddToMapFromFactory(url, null, resourceKey, record);
}

/**
 * Builds the custom links.
 * 
 * @param xRecord the x record
 * @param record the record
 */
protected void buildCustomLinks(SearchXslRecord xRecord, 
    SearchResultRecord record) {
  if(!xRecord.getLinks().readShowLink(ResourceLink.TAG_CUSTOM)) {
    return;
  }
  Map<String, List<String>> links = xRecord.getLinks().getCustomLinks();
  Iterator<String> iter = links.keySet().iterator();
  while(iter.hasNext()) {
    String label = iter.next();
    List<String> list = links.get(label);
    Iterator<String> iter2 = list.iterator();
    while(iter2.hasNext()) {
      String url = iter2.next();
      url = this.checkUrl(url);
      if (url.length() > 0) {
        ResourceLink link = this.makeLink(url, ResourceLink.TAG_CUSTOM, label);
        record.getResourceLinks().add(link);
        makeAddToMapFromFactory(url, null, label, record);
      }
    }
  }
}

/**
 * Checks a url the url to check
 * <br/>Only urls beginning with www. http:// https:// ftp:// ftps:// will be returned.
 * @param urlToCheck the urlToCheck
 * @return the checked url (zero length if invalid)
 */
protected String checkUrl(String urlToCheck) {
  urlToCheck = Val.chkStr(urlToCheck);
  String url = urlToCheck.toLowerCase();
  if (url.startsWith("www.")) {
    return "http://"+urlToCheck;
  } else {
    if (url.startsWith("http://") || url.startsWith("https://") ||
        url.startsWith("ftp://") || url.startsWith("ftps://")) {
      return urlToCheck;
    } 
  }
  return "";
}


/**
 * Determines the primary resource URL associated with the resultant record.
 * <br/>The primary resource URL is associated with the resource that the
 * metadata record describes.
 * @param xRecord the underlying CSW record
 * @param record the search result record
 */
protected void determineResourceUrl(SearchXslRecord xRecord,
    SearchResultRecord record) {

  // initialize
  String resourceUrl = "";
  String serviceType = "";
  String serviceName = "";
  DcList references = xRecord.getReferences();

  // determine the service url, name and type
  List<String> schemeVals = references.get(Scheme.SERVER.getUrn());
  if (schemeVals.size() > 0) {
    resourceUrl = Val.chkStr(schemeVals.get(0));
  }

  schemeVals = references.get(Scheme.SERVICE.getUrn());
  if (schemeVals.size() > 0) {
    serviceName = Val.chkStr((schemeVals.get(0)));
  }

  schemeVals = references.get(Scheme.SERVICE_TYPE.getUrn());
  if (schemeVals.size() > 0) {
    serviceType = Val.chkStr((schemeVals.get(0)));
  }
  if ((resourceUrl.length() > 0) && (serviceType.length() == 0)) {
    serviceType = this.getResourceIdentifier().guessServiceTypeFromUrl(
        resourceUrl);
  }

  // handle the case where an ArcIMS service has been specified with 
  // server/service/serviceType parameters
  if ((resourceUrl.length() > 0)
      && (serviceType.equalsIgnoreCase("image")
          || serviceType.equalsIgnoreCase("feature") || serviceType
          .equalsIgnoreCase("metadata"))) {

    if ((serviceName.length() > 0)) {
      String esrimap = "servlet/com.esri.esrimap.Esrimap";
      if (resourceUrl.indexOf(esrimap) == -1) {
        if (resourceUrl.indexOf("?") == -1) {
          if (!resourceUrl.endsWith("/"))
            resourceUrl += "/";
          resourceUrl = resourceUrl + esrimap + "?ServiceName=" + serviceName;
        }
      } else {
        if (resourceUrl.indexOf("?") == -1) {
          resourceUrl = resourceUrl + "?ServiceName=" + serviceName;
        } else if (resourceUrl.indexOf("ServiceName=") == -1) {
          resourceUrl = resourceUrl + "&ServiceName=" + serviceName;
        }
      }
    }

    if (serviceType.equals("image")) {
      serviceType = "aims";
    }
  }

  // if the resource url has not been directly specified through a "scheme" attribute, 
  // then attempt to pick the best fit for the collection of references
  if (resourceUrl.length() == 0) {
    
    for (DcList.Value reference : references) {
      if (reference != null) {
        String url = Val.chkStr(reference.getValue());
        String type = this.getResourceIdentifier().guessServiceTypeFromUrl(url);
        if (type.length() > 0) {
          resourceUrl = url;
          serviceType = type;
          break;
        }
      }
    }

    /*
            if (!(server.endsWith(".gif") || server.endsWith(".jpg")
          || server.endsWith(".jpeg") || server.endsWith(".png") || server
          .contains("/thumbnail?uuid") || server.contains("?getxml="))) {
    
        if(!links.findUrlByTag(RESOURCE_LINK_KEY.gpt_openResource.toString()).equals(server)
            && !links.findUrlByTag(RESOURCE_LINK_KEY.gpt_openResource.toString()).equals("http://"+server)){
          links.add(buildResourceLink(buildOpenResourceUrl(server,serviceType),
              RESOURCE_LINK_KEY.gpt_openResource.toString(), "catalog.rest.open",
              true));
        }
     */
  }

  // update the record
  resourceUrl = this.checkUrl(resourceUrl);
  if (resourceUrl.length() > 0) {
    record.setResourceUrl(resourceUrl);
    record.setService(serviceName);
    record.setServiceType(serviceType);
  }

}

/**
 * Encodes a URL parameter value.
 * @param value the URL parameter value to encode
 * @return the encoded parameter value
 */
protected String encodeUrlParam(String value) {
  value = Val.chkStr(value);
  try {
    return URLEncoder.encode(value, "UTF-8");
  } catch (UnsupportedEncodingException ex) {
    LogUtil.getLogger().severe("Unsupported encoding: UTF-8");
    return value;
  }
}

/**
 * Initializes the resource link builder.
 * @param context the active request context
 * @param request the active HTTP servlet request (can be null)
 * @param messageBroker the active message broker
 */
public void initialize(HttpServletRequest request, RequestContext context,
    MessageBroker messageBroker) {
  this.setMessageBroker(messageBroker);
  this.setResourceIdentifier(ResourceIdentifier.newIdentifier(context));
  this.mapViewerUrl = context.getCatalogConfiguration().getSearchConfig()
      .getMapViewerUrl();
  this.setBaseContextPath(RequestContext.resolveBaseContextPath(request));
  this.requestContext = context;
}

/**
 * Makes the label associated with a resource bundle key.
 * @param resourceKey the resource bundle key
 * @return the associated key
 */
protected String makeLabel(String resourceKey) {
  String label = this.labels.get(resourceKey);
  if (label == null) {
    label = this.getMessageBroker().retrieveMessage(resourceKey);
    this.labels.put(resourceKey, label);
  }
  label = Val.chkStr(label);
  if (label.length() > 0) {
    return label;
  } else {
    return resourceKey;
  }
}

/**
 * Makes a link.
 * @param url the URL associated with the resource
 * @param tag the tag idenitfying a type of link
 * @param resourceKey the resource bundle key associated with the label
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
 * Instantiates a new resource link builder.
 * <p/>
 * By default, a new instance of 
 * com.esri.gpt.catalog.search.ResourceLinkBuilder is returned.
 * <p/>
 * This can be overridden by the configuration parameter:
 * /gptConfig/catalog/parameter@key="resourceLinkBuilder"
 * @param context the active request context
 * @param servletRequest the active HTTP servlet request
 * @param messageBroker the message broker
 * @return the resource link builder
 */
public static ResourceLinkBuilder newBuilder(RequestContext context,
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

  // look for a configured class name for the resource link builder
  String className = Val.chkStr(catCfg.getParameters().getValue(
      "resourceLinkBuilder"));
  if (className.length() == 0) {
    className = com.esri.gpt.catalog.search.ResourceLinkBuilder.class.getName();
  }

  // instantiate the builder
  try {
    Class<?> cls = Class.forName(className);
    Object obj = cls.newInstance();
    if (obj instanceof ResourceLinkBuilder) {
      ResourceLinkBuilder linkBuilder = (ResourceLinkBuilder) obj;
      linkBuilder.initialize(servletRequest, context, messageBroker);
      return linkBuilder;
    } else {
      String sMsg = "The configured resourceLinkBuilder parameter is invalid: "
          + className;
      throw new ConfigurationException(sMsg);
    }
  } catch (ConfigurationException t) {
    throw t;
  } catch (Throwable t) {
    String sMsg = "Error instantiating resource link builder: " + className;
    throw new ConfigurationException(sMsg, t);
  }
}

/**
 * Make add to map from factory.
 * 
 * @param resourceUrl the resource url
 * @param serviceType the service type (could be null)
 * @param prefixResource the prefix resource (Used for the label)
 * @param record the record
 */
private void makeAddToMapFromFactory(String resourceUrl, 
    String serviceType, String prefixResource, SearchResultRecord record) {
  IMapViewer iMapViewer = MapViewerFactory.createMapViewer(resourceUrl, serviceType, 
      new SearchResultRecordAdapter(record), this.getRequestContext());
  if (iMapViewer == null) {
    return;
  }
  String addToMapUrl = iMapViewer.readAddToMapUrl();
  if(addToMapUrl == null || "".equals(addToMapUrl)) {
    return;
  }
  String resourceKey = "catalog.rest.addToMap";
  ResourceLink link = this.makeLink(addToMapUrl, ResourceLink.TAG_ADDTOMAP,
      resourceKey);
  String label =  this.getMessageBroker().retrieveMessage(prefixResource) + " - " +
    this.getMessageBroker().retrieveMessage(resourceKey);
  link.setLabel(label);
  link.setTarget(iMapViewer.readTarget());
  record.getResourceLinks().add(link);
}

}
