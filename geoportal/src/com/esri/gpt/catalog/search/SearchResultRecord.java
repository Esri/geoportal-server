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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.esri.gpt.control.georss.IFeedRecord;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.request.Record;
import com.esri.gpt.framework.util.Val;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * The Class SearchResultRecord. Contains attributes describing a metadata
 * record . Not thread safe.
 */
public class SearchResultRecord extends Record {

  /** class variables ========================================================= */
  
  /** The maximum length of the abstract * */
  public final static int ABSTRACT_MAXLENGTH = 255;
  
  /**
   * Prefix where content type is appended to to come up with the readable content type title
   */
  public final static String CONTENTTYPE_PROPERTY_PREFIX = "catalog.mdParam.contentType.content.";
  
  /** instance variables ====================================================== */
  private String        _abstract = "";
  private String        _contentType = "";
  private Envelope      _envelope = new Envelope();
  private boolean       _external = false;
  private String        _externalId = "";
  private String        _fileIdentifier = "";
  private Date          _modifiedDate = null;
  private ResourceLinks _resourceLinks = new ResourceLinks();
  private String        _resourceUrl = "";
  private String        _service = "";
  private String        _serviceType = "";
  private String        _title = "";
  private String        _uuid = "";
  private boolean       _supportsViewMetadata = true;
  private boolean       _defaultGeometry = false; 
  private final Map<String,Object> _objectMap = new HashMap<String,Object>();
 
public SearchResultRecord() {
    
  }
  
  public SearchResultRecord(IFeedRecord record) {
    _abstract = record.getAbstract();
    _contentType = record.getContentType();
    _envelope = record.getEnvelope();
//    _external = false;
//    _externalId = "";
//    _fileIdentifier = "";
    _modifiedDate = record.getModfiedDate();
    _resourceLinks = record.getResourceLinks();
    _resourceUrl = record.getResourceUrl();
//    _service = "";
    _serviceType = record.getServiceType();
    _title = record.getTitle();
    _uuid = record.getUuid();
//    _supportsViewMetadata = true;
//    _defaultGeometry = false; 
    _objectMap.putAll(getResourceLinksAsMap(record.getResourceLinks()));
  }
  
  private Map<String,String> getResourceLinksAsMap(ResourceLinks rLinks) {
    return rLinks!=null && rLinks.getUrlsByTag()!=null? rLinks.getUrlsByTag(): new HashMap<String, String>();
  }
  /** properties ============================================================== */
  
  /**
   * Gets the abstract.
   * @return the abstract (trimmed, never null)
   */
  public String getAbstract() {
    return Val.chkStr(_abstract);
  }
  /**
   * Sets the abstract.
   * @param abs the abstract
   */
  public void setAbstract(String abs) {
    _abstract = Val.chkStr(abs);
 // Strip out xml/html characters
    _abstract = _abstract.replaceAll("\\<.*?\\>", "");
    if (this._abstract.length() > ABSTRACT_MAXLENGTH) {
      this._abstract = Val.chkStr((this._abstract.substring(0, ABSTRACT_MAXLENGTH)+"..."));
    }
    
  }
  
  /**
   * Gets the content type.
   * @return the content type (trimmed, never null)
   */
  public String getContentType() {
    return _contentType;
  }
  /**
   * Sets the content type.
   * @param contentType the new content type
   */
  public void setContentType(String contentType) {
    _contentType = Val.chkStr(contentType);
  }
  
  /** 
   * Gets the content type link associated with the record.
   * <br/>Convienence method for:
   * <br/>SearchResultRecord.getResourceLinks().getIcon();
   * @return content type link
   */
  public ResourceLink getContentTypeLink() {
    return this.getResourceLinks().getIcon();
  }
  
  /**
   * Gets the bounding envelope.
   * @return the bounding envelope
   */
  public Envelope getEnvelope() {
    return _envelope;
  }
  /**
   * Sets the bounding envelope.
   * @param envelope the bounding envelope
   */
  public void setEnvelope(Envelope envelope) {
    _envelope = envelope;
    if (_envelope == null) {
      _envelope = new Envelope();
    }
  }
  
  /**
   * Determines if the record is from an external repository.
   * @return true if external
   */
  public boolean isExternal() {
    return _external;
  }
  /**
   * Sets the flag indicating if the record is from an external repository.
   * @param external true if external
   */
  public void setExternal(boolean external) {
    this._external = external;
  }
  
  /**
   * Gets the external repository id.
   * @return the external id (trimmed, never-null)
   */
  public String getExternalId() {
    return _externalId;
  }
  /**
   * Sets the external repository id.
   * @param externalId the new external id
   */
  public void setExternalId(String externalId) {
    this._externalId = Val.chkStr(externalId);
  }
  
  /**
   * Gets the file identifier. 
   * <br/>The file identifier is typically associated
   * with ISO-19139 documents.
   * @return the file identifier (trimmed, never null)
   */
  public String getFileIdentifier() {
    return _fileIdentifier;
  }
  /**
   * Sets the file identifier. 
   * <br/>The file identifier is typically associated with ISO-19139 documents.
   * @param id the file identifier
   */
  public void setFileIdentifier(String id) {
    _fileIdentifier = Val.chkStr(id);
  }
  
  /**
   * Gets the modified date.
   * @return the modfied date (possibly null)
   */
  public Date getModfiedDate() {
    return this._modifiedDate;
  }
  /**
   * Sets the modified date.
   * @param date the new modified date
   */
  public void setModifiedDate(Date date) {
    this._modifiedDate = date;
  }
  
  /**
   * Gets the free form object map associated with this request.
   * <br/>This map can be used in a manner similar to the attributes
   * of a ServletRequest.
   * @return the free form object map
   */
  public Map<String,Object> getObjectMap() {
    return _objectMap;
  }
  
  /**
   * Gets the resource links as map.
   *
   ** <br/Example:<br/>
   * Rendered="#{record.resourceLinksAsMap['metadata']}".  For the map
   * string, look at ResourceLink static strings with prefix TAG
   * 
   * @return the resource links as map
   */
  public Map<String, String> getResourceLinksAsMap() {
	  Map<String, String> m = new HashMap<String, String>();
	  ResourceLinks rLinks = this.getResourceLinks();
	  if(rLinks == null) {
		  return m;
	  }
	  if(rLinks.getUrlsByTag() != null) {
		  m = this.getResourceLinks().getUrlsByTag();
	  }
	  return m;
  }
  
  /**
   * Gets the resource links as map.
   * 
   * Short cut for #{record.resourceLinksAsMap['ResourceLink.TAG_DETAILS']}
   * in searchResults.jsp
   *
   * @return the resource links as map
   */
  public String getViewMetadataUrl() {
	  String url = "";
	  Map<String, String> m = new HashMap<String, String>();
	  ResourceLinks rLinks = this.getResourceLinks();
	  if(rLinks == null) {
		  return "";
	  }
	  if(rLinks.getUrlsByTag() != null) {
		  m = this.getResourceLinks().getUrlsByTag();
		  url = m.get(ResourceLink.TAG_DETAILS);
	  }
	  return Val.chkStr(url);
  }
  
  /**
   * Gets the resource links.
   * @return the links
   */
  public ResourceLinks getResourceLinks() {
    return _resourceLinks;
  }
  /**
   * Sets the resource links.
   * @param links the resource links
   */
  protected void setResourceLinks(ResourceLinks links) {
    this._resourceLinks = links;
  }
  
  /**
   * Gets primary resource URL.
   * <br/>The primary resource URL is associated with the resource that the
   * metadata record describes.
   * @return the URL
   */
  public String getResourceUrl() {
    return this._resourceUrl;
  }
  /**
   * Sets primary resource URL.
   * <br/>The primary resource URL is associated with the resource that the
   * metadata record describes.
   * @param url the URL 
   */
  public void setResourceUrl(String url) {
    this._resourceUrl = Val.chkStr(url);
  }
  
  /**
   * Gets the service name.
   * <br/>This is rarely used, mostly applicable to ArcIMS service names.
   * @return the service name (never null, trimmed)
   */
  public String getService() {
    return _service;
  }
  /**
   * Sets the service.
   * <br/>This is rarely used, mostly applicable to ArcIMS service names.
   * @param service the new service name
   */
  public void setService(String service) {
    this._service =  Val.chkStr(service);
  }
  
  /**
   * Gets the service type. 
   * Currently service type values in ags, wms, wcs,wfs, aims, kml, csw, sos,
   * ArcGIS:nmf, ArcGIS:lyr,ArcGIS:mxd
   * ags - ArcGIS Server Service
   * aims - ArcIMS Image Service
   * wms - Web Map Service
   * wcs - Web Coverage Service
   * wfs - Web Feature Service
   * kml - Keyhole Markup Language
   * csw - Catalogue Web Service
   * sos - Sensor Observation Service
   * ArcGIS:nmf - ArcGIS Explorer Document
   * ArcGIS:lyr - ArcMap Layer File
   * ArcGIS:mxd - ArcMap Map Document 
   * @return the service type (never null, trimmed)
   */
  public String getServiceType() {
    return _serviceType;
  }
  /**
   * Sets the service type.
   * @param serviceType the new service type
   */
  public void setServiceType(String serviceType) {
    _serviceType = Val.chkStr(serviceType);
  }
  
  /** 
   * Gets the thumbnail link associated with the record.
   * <br/>Convienence method for:
   * <br/>SearchResultRecord.getResourceLinks().getThumbnail();
   * @return thumbnail link
   */
  public ResourceLink getThumbnailLink() {
    return this.getResourceLinks().getThumbnail();
  }  
  
  /** 
   * Gets the thumbnail link associated with the record.
   * <br/>Convienence method for:
   * <br/>SearchResultRecord.getResourceLinks().getThumbnail();
   * @return thumbnail link
   */
  public String getThumbnailUrl() {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    Object request = facesContext.getExternalContext();
    String baseContextPath = Val.chkStr(request instanceof HttpServletRequest? RequestContext.resolveBaseContextPath((HttpServletRequest)request): "");
    String baseScheme = baseContextPath.indexOf(":")>=0? baseContextPath.substring(0, baseContextPath.indexOf(":")): "";
    String facesScheme = facesContext.getExternalContext().getRequestScheme();
    String scheme = !baseScheme.isEmpty()? baseScheme: facesScheme;
    String thumbnailUrl = this.getResourceLinks().getThumbnail().getUrl();
    String thumbnailScheme = thumbnailUrl.indexOf(":")>=0? thumbnailUrl.substring(0, thumbnailUrl.indexOf(":")): "";
    if (scheme.toLowerCase().endsWith("http") && thumbnailScheme.toLowerCase().equals("https")) {
      scheme = "https";
    }
    return Val.stripHttpProtocol(thumbnailUrl, !scheme.isEmpty()? scheme+":": "");
  }
  
  /**
   * Gets the title.
   * @return the title (trimmed, never null)
   */
  public String getTitle() {
    return _title;
  }
  /**
   * Sets the title.
   * @param title the title
   */
  public void setTitle(String title) {
    _title = Val.chkStr(title);
  }
  
  /**
   * Gets the UUID.
   * @return the UUID (trimmed, never null)
   */
  public String getUuid() {
    return _uuid;
  }
  /**
   * Sets the UUID.
   * @param uuid the UUID
   */
  public void setUuid(String uuid) {
    _uuid = Val.chkStr(uuid);
  }
  
  //** below methods added in GPT 10
  /**
   * Checks if is supports view metadata.
   * 
   * @return true, if is supports view metadata
   */
  public boolean isSupportsViewMetadata() {
    return _supportsViewMetadata;
  }
  
  /**
   * Sets the supports view metadata.
   * 
   * @param supportsViewMetadata the new supports view metadata
   */
  public void setSupportsViewMetadata(boolean supportsViewMetadata) {
    this._supportsViewMetadata = supportsViewMetadata;
  }
  
  /**
   * Checks if is default geometry.
   * 
   * @return true, if is default geometry
   */
  public boolean isDefaultGeometry() {
    return _defaultGeometry;
  }
  
  /**
   * Sets the default geometry.
   * 
   * @param defaultGeometry the new default geometry
   */
  public void setDefaultGeometry(boolean defaultGeometry) {
    this._defaultGeometry = defaultGeometry;
  }
 
}
