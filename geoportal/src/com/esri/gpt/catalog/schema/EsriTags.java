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
package com.esri.gpt.catalog.schema;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.esri.gpt.catalog.discovery.IStoreable;
import com.esri.gpt.catalog.schema.indexable.Indexables;
import com.esri.gpt.catalog.search.ResourceIdentifier;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Maintains information associated with ESRI tags within a metadata document.
 */
public class EsriTags {

  // class variables =============================================================
  public static Map<String,String> DATA_THEME_CODES;
  
  // instance variables ==========================================================
  private String _aimsContentType = "";
  private String _contentDevType = "";
  private String _primaryOnlink = "";
  private String _publishedDocId = "";
  private String _pubSource = "";
  private String _resourceType = "";
  private String _resourceUrl = "";
  private String _server = "";
  private String _service = "";
  private String _serviceParams = "";
  private String _serviceType = "";
  private String _thumbnailBinary = null;
  
  // static initialization =======================================================
  static {
    
    HashMap<String,String> map = new HashMap<String,String>();
    map.put("001","farming");
    map.put("002","biota");
    map.put("003","boundaries");
    map.put("004","climatologyMeteorologyAtmosphere");
    map.put("005","economy");
    map.put("006","elevation");
    map.put("007","environment");
    map.put("008","geoscientificInformation");
    map.put("009","health");
    map.put("010","imageryBaseMapsEarthCover");
    map.put("011","intelligenceMilitary");
    map.put("012","inlandWaters");
    map.put("013","location");
    map.put("014","oceans");
    map.put("015","planningCadastre");
    map.put("016","society");
    map.put("017","structure");
    map.put("018","transportation");
    map.put("019","utilitiesCommunication");
    DATA_THEME_CODES = map;
    
  }
  
  // constructors ================================================================
    
  /** Default constructor. */
  public EsriTags() {}
  
  // properties ==================================================================
  
  /**
   * Gets the ArcIMS content type.
   * @return the ArcIMS content type
   */
  public String getArcIMSContentType() {
    return _aimsContentType;
  }
  /**
   * Sets the ArcIMS content type.
   * @param type the ArcIMS content type
   */
  public void setArcIMSContentType(String type) {
    _aimsContentType = Val.chkStr(type);
  }
  
  /**
   * Gets the content developer type.
   * @return the content developer type
   */
  public String getContentDevType() {
    return _contentDevType;
  }
  /**
   * Sets the content developer type.
   * @param type the content developer type
   */
  public void setContentDevType(String type) {
    _contentDevType = Val.chkStr(type);
  }
  
  /**
   * Gets the primary online linkage url.
   * @return the primary online linkage url
   */
  public String getPrimaryOnlink() {
    return _primaryOnlink;
  }
  /**
   * Sets the primary online linkage url.
   * @param url the primary online linkage url
   */
  public void setPrimaryOnlink(String url) {
    _primaryOnlink = Val.chkStr(url);
  }
  
  /**
   * Gets the published UUID for the document.
   * @return the published UUID
   */
  public String getPublishedDocId() {
    return _publishedDocId;
  }
  /**
   * Sets the published UUID for the document.
   * @param uuid the published UUID
   */
  public void setPublishedDocId(String uuid) {
    _publishedDocId = UuidUtil.addCurlies(uuid);
  }
  
  /**
   * Gets the publication source.
   * @return the publication source
   */
  public String getPubSource() {
    return _pubSource;
  }
  /**
   * Sets the publication source.
   * @param source the publication source
   */
  public void setPubSource(String source) {
    _pubSource = Val.chkStr(source);
  }
  
  /**
   * Gets the resource type.
   * @return the resource type
   */
  public String getResourceType() {
    return _resourceType;
  }
  /**
   * Sets the resource type.
   * @param type the resource type
   */
  public void setResourceType(String type) {
    _resourceType = Val.chkStr(type);
  }
  
  /**
   * Gets the resource URL.
   * @return the resource URL
   */
  private String getResourceUrl() {
    return _resourceUrl;
  }
  /**
   * Sets the resource URL.
   * @param url the resource URL
   */
  private void setResourceUrl(String url) {
    _resourceUrl = Val.chkStr(url);
  }
  
  /**
   * Gets the map server.
   * @return the map server
   */
  public String getServer() {
    return _server;
  }
  /**
   * Sets the map server.
   * @param server the map server
   */
  public void setServer(String server) {
    _server = Val.chkStr(server);
  }
  
  /**
   * Gets the map service.
   * @return the map service
   */
  public String getService() {
    return _service;
  }
  /**
   * Sets the map service.
   * @param service the map service
   */
  public void setService(String service) {
    _service = Val.chkStr(service);
  }
  
  /**
   * Gets the map service query parameters.
   * @return the map service query parameters
   */
  public String getServiceParams() {
    return _serviceParams;
  }
  /**
   * Sets the map service query parameters.
   * @param parameters the map service query parameters
   */
  public void setServiceParams(String parameters) {
    _serviceParams = Val.chkStr(parameters);
  }
  
  /**
   * Gets the map service type.
   * @return the map service type
   */
  public String getServiceType() {
    return _serviceType;
  }
  /**
   * Sets the map service type.
   * @param type the map service type
   */
  public void setServiceType(String type) {
    _serviceType = Val.chkStr(type);
  }
  
  /**
   * Gets the base64 encoded string for the thumbnail image.
   * @return the base64 encoded string for the thumbnail image
   */
  public String getThumbnailBinary() {
    return _thumbnailBinary;
  }
  /**
   * Sets the base64 encoded string for the thumbnail image.
   * @param base64 the base64 encoded string for the thumbnail image
   */
  public void setThumbnailBinary(String base64) {
    _thumbnailBinary = base64;
  }
  
  // methods =====================================================================
  
  
  /**
   * Applies meaning associated with evaluated ESRI tags
   * @param schema the schema to which evaluated meanings will be applied
   * @param esriTags the evaluated ESRI tags
   */
  private void apply(Schema schema) {
    
    ResourceIdentifier ri = ResourceIdentifier.newIdentifier(null);
    
    // published doc id
    String sEsriDocID = schema.getMeaning().getEsriDocID();
    if ((sEsriDocID == null) || (sEsriDocID.length() == 0)) {
      sEsriDocID = this.getPublishedDocId();
      if ((sEsriDocID != null) && (sEsriDocID.length() > 0)) {
        schema.getMeaning().setEsriDocID(sEsriDocID);
      }
    }
    
    // thumbnail binary
    String base64Thumbnail = schema.getMeaning().getThumbnailBinary();
    if ((base64Thumbnail == null) || (base64Thumbnail.length() == 0)) {
      base64Thumbnail = this.getThumbnailBinary();
      if ((base64Thumbnail != null) && (base64Thumbnail.length() > 0)) {
        schema.getMeaning().setThumbnailBinary(base64Thumbnail);
      }
    }
    
    // website URL
    String sWebsiteUrl = schema.getMeaning().getWebsiteUrl();
    if ((sWebsiteUrl == null) || (sWebsiteUrl.length() == 0)) {
      sWebsiteUrl = this.getPrimaryOnlink();
      if ((sWebsiteUrl != null) && (sWebsiteUrl.length() > 0)) {
        IStoreable storeable = schema.getMeaning().getStoreables().get(Meaning.MEANINGTYPE_WEBSITE_URL);
        if (storeable != null) {
          storeable.setValue(sWebsiteUrl);
        }
      }
    }
   
    // resource URL
    String sResourceUrl = schema.getMeaning().getResourceUrl();
    //if ((sResourceUrl == null) || (sResourceUrl.length() == 0)) {
      sResourceUrl = Val.chkStr(this.makeResourceUrl(ri));
      if (sResourceUrl.length() > 0) {
        IStoreable storeable = schema.getMeaning().getStoreables().get(Meaning.MEANINGTYPE_RESOURCE_URL);
        if (storeable != null) {
          storeable.setValue(sResourceUrl);
          try {
            for (Section section : schema.getSections().values()) {
              for (Parameter param : section.getParameters().values()) {
                if (param.getMeaningType().equalsIgnoreCase(Meaning.MEANINGTYPE_RESOURCE_URL)) {
                  if ((param.getContent() != null) && (param.getContent().getSingleValue() != null)) {
                    //String sCurrent = param.getContent().getSingleValue().getValue();
                    //if ((sCurrent == null) || (sCurrent.length() == 0)) {
                      param.getContent().getSingleValue().setValue(sResourceUrl);
                    //}
                  }
                }
              }
            }
          } catch (Exception e) {}
        }
      }
    //}
    
    // content type
    String sContentType = schema.getMeaning().getArcIMSContentType();
    if ((sContentType == null) || (sContentType.length() == 0)) {
      sContentType = this.getArcIMSContentType();
      if ((sContentType != null) && (sContentType.length() > 0)) {
        schema.getMeaning().setArcIMSContentType(sContentType);
      }
    }
      
  }
  
  /**
   * Evaluates ESRI tags within a document.
   * @param schema the schema being evaluated
   * @param dom the XML document associated with the schema
   */
  public void evaluate(Schema schema, Document dom) {
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      Node ndEsri = (Node)xpath.evaluate("/metadata/Esri",dom,XPathConstants.NODE);
      if (ndEsri != null) {
        this.setPublishedDocId(xpath.evaluate("PublishedDocID",ndEsri));
        this.setServer(xpath.evaluate("Server",ndEsri));
        this.setService(xpath.evaluate("Service",ndEsri));
        this.setServiceType(xpath.evaluate("ServiceType",ndEsri));
        this.setServiceParams(xpath.evaluate("ServiceParam",ndEsri));
        this.setPrimaryOnlink(xpath.evaluate("primaryOnlink",ndEsri));
        this.setContentDevType(xpath.evaluate("ContentDevType",ndEsri));
        this.setResourceType(xpath.evaluate("resourceType",ndEsri));
        this.setPubSource(xpath.evaluate("PubSourceCd",ndEsri));
      }
      String base64Thumbnail = xpath.evaluate("/metadata/Binary/Thumbnail/Data",dom);
      if ((base64Thumbnail != null) && (base64Thumbnail.length() > 0)) {
        this.setThumbnailBinary(base64Thumbnail);
      }
    } catch (XPathExpressionException e) {
      // ignore, never thrown
    }
  }
  
  /**
   * Makes a resource URL from a set of ESRI tags.
   */
  public String makeResourceUrl(ResourceIdentifier resourceIdentifier) {
    
    // initialize
    String sResourceUrl = "";
    String sServer = this.getServer();
    String sService = this.getService();
    String sServiceType = this.getServiceType();
    String sServiceParams = this.getServiceParams();
    if (sServer.length() > 0) {
      
      // ArcIMS image/feature/metadata services
      if (sServiceType.equalsIgnoreCase("image") || 
          sServiceType.equalsIgnoreCase("feature") ||
          sServiceType.equalsIgnoreCase("metadata")) {
        String sEsrimap = "servlet/com.esri.esrimap.Esrimap";
        if (sServer.indexOf(sEsrimap) == -1) { 
          if ((sServer.indexOf("?") == -1) && (sServer.indexOf("&") == -1)) {
            if (!sServer.endsWith("/")) sServer += "/";
            if (sService.length() > 0) sResourceUrl = sServer+sEsrimap+"?ServiceName="+sService;
          }
        } else {
          if ((sServer.indexOf("?") == -1) && (sServer.indexOf("&") == -1)) {
            if (!sServer.endsWith("/")) sServer += "/";
            if (sService.length() > 0) sResourceUrl = sServer+"?ServiceName="+sService;
          } else if (sServer.indexOf("ServiceName=") == -1) {
            if (sServer.indexOf("?") == -1) {
              if (!sServer.endsWith("/")) sServer += "/";
              sResourceUrl = sServer+"?ServiceName="+sService;
            } else {
              sResourceUrl = sServer+"&ServiceName="+sService;
            }
          }
        }
        if (sResourceUrl.length() > 0) {
          if (sServiceType.equalsIgnoreCase("image")) {
            this.setArcIMSContentType("liveData");
          } else if (sServiceType.equalsIgnoreCase("feature")) {
            //if (meaning != null) meaning.setArcIMSContentType("liveData");
            sResourceUrl = "";
          } else if (sServiceType.equalsIgnoreCase("metadata")) {
            //if (meaning != null) meaning.setArcIMSContentType("liveData");
            sResourceUrl = "";
          }
        }
      
      // OGC services
      } else {
        if ((sServer.indexOf("?") == -1) && (sServer.indexOf("&") == -1)) {
          if (sServiceParams.length() > 0) {
            if (!sServiceParams.startsWith("?")) sServer += "?";
            sServer += sServiceParams;
          }
        }
        String sContentType = resourceIdentifier.guessArcIMSContentTypeFromUrl(sServer);
        if ((sContentType.length() > 0) && !sContentType.equalsIgnoreCase("unknown")) {
          this.setArcIMSContentType(sContentType);
          sResourceUrl = sServer;
        }
      } 
         
    }
    
    return sResourceUrl;
  }

}