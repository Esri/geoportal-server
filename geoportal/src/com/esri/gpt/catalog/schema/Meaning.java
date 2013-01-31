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
import com.esri.gpt.catalog.search.ResourceIdentifier;
import com.esri.gpt.catalog.discovery.IStoreable;
import com.esri.gpt.catalog.discovery.IStoreables;
import com.esri.gpt.catalog.discovery.PropertyMeanings;
import com.esri.gpt.catalog.lucene.Storeables;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Stores values that ascribe meaning to a document.
 * <p/>
 * Most schema parameters are treated in an abstract fashion, their
 * associated meaning is unknown. Several parameters are critical for
 * publication and meaning is required. The meaning for a parameter is
 * specified within the XML defining the schema. Example: <br/>
 * &lt;parameter key="identification.title" meaning="title"&gt;
 */
public class Meaning {

  // class variables =============================================================
  
  /** Logger */
  private static Logger LOGGER = Logger.getLogger(Meaning.class.getName());
    
  /** Document abstract = "abstract" */
  public static final String MEANINGTYPE_ABSTRACT = "abstract";
  
  /** Content type = "content.type" */
  public static final String MEANINGTYPE_CONTENTTYPE = "contentType";
  
  /** Data theme (i.e ISO topic category) = "dataTheme" */
  public static final String MEANINGTYPE_DATATHEME = "dataTheme";
    
  /** Bounding envelope east = "envelope.east" */
  public static final String MEANINGTYPE_ENVELOPE_EAST = "envelope.east";
    
  /** Bounding envelope north = "envelope.north" */
  public static final String MEANINGTYPE_ENVELOPE_NORTH = "envelope.north";
  
  /** Bounding envelope south = "envelope.south" */
  public static final String MEANINGTYPE_ENVELOPE_SOUTH = "envelope.south";
  
  /** Bounding envelope west = "envelope.west" */
  public static final String MEANINGTYPE_ENVELOPE_WEST = "envelope.west";
  
  /** ESRI tag document identifier = "esriDocID" */
  public static final String MEANINGTYPE_ESRIDOCID = "esriDocID";
  
  /** File identifier = "fileIdentifier" */
  public static final String MEANINGTYPE_FILEIDENTIFIER = "fileIdentifier";
  
  /** No meaning = "none" (this is the default) */
  public static final String MEANINGTYPE_NONE = "none";
  
  /** Resource type = "resource.type" */
  public static final String MEANINGTYPE_RESOURCE_TYPE = "resource.type";
  
  /** Resource URL = "resource.url" */
  public static final String MEANINGTYPE_RESOURCE_URL = "resource.url";
  
  /** Thumbnail binary = "thumbnail.binary" */
  public static final String MEANINGTYPE_THUMBNAIL_BINARY = "thumbnail.binary";
  
  /** Thumbnail URL = "thumbnail.url" */
  public static final String MEANINGTYPE_THUMBNAIL_URL = "thumbnail.url";
  
  /** Document title = "title" */
  public static final String MEANINGTYPE_TITLE = "title";

  /** original title */
  public static final String MEANINGTYPE_TITLE_ORG = "title.org";
  
  /** Website URL = "website.url" */
  public static final String MEANINGTYPE_WEBSITE_URL = "website.url";
    
  // instance variables ==========================================================
  private String             _aimsContentType = "";
  private Envelope           _envelope = new Envelope();
  private String             _esriDocID = "";
  private PropertyMeanings   _propertyMeanings;
  private ResourceIdentifier _resourceIdentifier;
  private String             _resourceType = "";
  private IStoreables        _storables = new Storeables();
  private String             _thumbnailBinary = null;
  
  // constructors ================================================================
  
  /**
   * Construct with a configured collection of property meanings.
   * propertyMeanings the configured property meanings
   */
  protected Meaning(PropertyMeanings propertyMeanings) {
    this._propertyMeanings = propertyMeanings;
    _resourceIdentifier = ResourceIdentifier.newIdentifier(null);
    //System.err.println(propertyMeanings);
    _storables = new Storeables(this._propertyMeanings);
    
    IStoreable storeable = _storables.get("geometry");
    if (storeable != null) {
      storeable.setValue(_envelope);
    } 
        
  }
      
  // properties ==================================================================
    
  /**
   * Gets the ArcIMS content type.
   * @return the ArcIMS content type
   */
  public String getArcIMSContentType() {
    return getFirstStoreableValue(Meaning.MEANINGTYPE_CONTENTTYPE);
  }
  /**
   * Sets the ArcIMS content type.
   * @param type the ArcIMS content type
   */
  public void setArcIMSContentType(String type) {
    type = Val.chkStr(type);
    IStoreable storeable = this._storables.get(Meaning.MEANINGTYPE_CONTENTTYPE);
    if (storeable != null) storeable.setValue(type);
  }
  
  /**
   * Gets the bounding envelope.
   * @return the bounding envelope
   */
  public Envelope getEnvelope() {
    return _envelope;
  }
  
  /**
   * Gets the value the ESRI tagged document UUID.
   * <br/>The underlying value is associated with a special ESRI tag used by
   * ArcCatalog to uniquely identify the document.
   * @return the ESRI tagged document UUID
   */
  public String getEsriDocID() {
    return _esriDocID;
  }
  /**
   * Sets the value the ESRI tagged document UUID.
   * <br/>The underlying value is associated with a special ESRI tag used by
   * ArcCatalog to uniquely identify the document.
   * @param id the ESRI tagged document UUID
   */
  public void setEsriDocID(String id) {
    _esriDocID = Val.chkStr(id);
  }
  
  /**
   * Gets the file identifier.
   * <br/>The file identifier is typically associated with ISO-19139 documents.
   * @return the file identifier
   */
  public String getFileIdentifier() {
    return getFirstStoreableValue(Meaning.MEANINGTYPE_FILEIDENTIFIER);
  }
  /**
   * Sets the file identifier.
   * <br/>The file identifier is typically associated with ISO-19139 documents.
   * @param identifier the file identifier
   */
  public void setFileIdentifier(String identifier) {
    IStoreable storeable = this._storables.get(Meaning.MEANINGTYPE_FILEIDENTIFIER);
    if (storeable != null) storeable.setValue(identifier);
  }
  
  /**
   * Gets the configured property meanings.
   * @return the property meanings
   */
  public PropertyMeanings getPropertyMeanings() {
    return this._propertyMeanings;
  }
  
  /**
   * Gets the resource identifier.
   * @return the resource identifier
   */
  private ResourceIdentifier getResourceIdentifier() {
    return _resourceIdentifier;
  }
  
  /**
   * Gets the resource type.
   * @return the resource type
   */
  private String getResourceType() {
    return _resourceType;
  }
  /**
   * Sets the resource type.
   * @param resourceType the resource type
   */
  private void setResourceType(String resourceType) {
    _resourceType = Val.chkStr(resourceType);
  }
  
  /**
   * Gets the resource url.
   * @return the resource url
   */
  public String getResourceUrl() {
    return getFirstStoreableValue(Meaning.MEANINGTYPE_RESOURCE_URL);
  }
  
  /**
   * Gets store-able properties associated with the eveluated schema.
   * @return the store-able properties
   */
  public IStoreables getStoreables() {
    return this._storables;
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
  
  /**
   * Gets the thumbnail url.
   * @return the thumbnail url
   */
  public String getThumbnailUrl() {
    return getFirstStoreableValue(Meaning.MEANINGTYPE_THUMBNAIL_URL);
  }
  /**
   * Sets the thumbnail URL.
   * @param url the thumbnail URL
   */
  public void setThumbnailUrl(String url) {
    IStoreable storeable = this._storables.get(Meaning.MEANINGTYPE_THUMBNAIL_URL);
    if (storeable != null) storeable.setValue(url);
  }
  
  /**
   * Gets the title.
   * @return the title
   */
  public String getTitle() {
    return getFirstStoreableValue(Meaning.MEANINGTYPE_TITLE);    
  }
  
  /**
   * Sets the title.
   * @param title the title
   */
  public void setTitle(String title) {
    title = Val.chkStr(title);
    IStoreable storeable = this._storables.get(Meaning.MEANINGTYPE_TITLE);
    if (storeable != null) storeable.setValue(title);
  }
  
  /**
   * Gets the website url.
   * @return the website url
   */
  public String getWebsiteUrl() {
    return getFirstStoreableValue(Meaning.MEANINGTYPE_WEBSITE_URL);
  }
  
  // methods =====================================================================
  
  /**
   * Applies meaning associated with evaluated ESRI tags
   * @param schema the schema to which evaluated meanings will be applied
   * @param esriTags the evaluated ESRI tags
   */
  protected void applyEsriTags(Schema schema, EsriTags esriTags) {
    
    // published doc id
    if (esriTags != null) {
      String sEsriDocID = this.getEsriDocID();
      if ((sEsriDocID == null) || (sEsriDocID.length() == 0)) {
        sEsriDocID = esriTags.getPublishedDocId();
        if ((sEsriDocID != null) && (sEsriDocID.length() > 0)) {
          this.setEsriDocID(sEsriDocID);
        }
      }
    }
    
    // thumbnail attachment (base64)
    if (esriTags != null) {
      String sBase64Thumbnail = this.getThumbnailBinary();
      if ((sBase64Thumbnail == null) || (sBase64Thumbnail.length() == 0)) {
        sBase64Thumbnail = esriTags.getThumbnailBinary();
        if ((sBase64Thumbnail != null) && (sBase64Thumbnail.length() > 0)) {
          this.setThumbnailBinary(sBase64Thumbnail);
        }
      }
    }
    
    
    // website URL
    String sWebsiteUrl = getWebsiteUrl();
    if ((sWebsiteUrl == null) || (sWebsiteUrl.length() == 0)) {
      if (esriTags.getPrimaryOnlink().length() > 0) {
        IStoreable storeable = this._storables.get(Meaning.MEANINGTYPE_WEBSITE_URL);
        if (storeable != null) {
           storeable.setValue(esriTags.getPrimaryOnlink());
        }
      }
    }
    
    // resource URL
    String sResourceUrl = getResourceUrl();
    //if ((sResourceUrl == null) || (sResourceUrl.length() == 0)) {
      sResourceUrl = Meaning.makeResourceUrl(esriTags,this);
      if (sResourceUrl.length() > 0) {
        IStoreable storeable = this._storables.get(Meaning.MEANINGTYPE_RESOURCE_URL);
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
      
  }
  
  /**
   * Appends property information for the component to a StringBuffer.
   * <br/>The method is intended to support "FINEST" logging.
   * <br/>super.echo should be invoked prior appending any local information.
   * @param sb the StringBuffer to use when appending information
   */
  public void echo(StringBuffer sb) {
    sb.append(" title=").append(getTitle());
    if (getEsriDocID().length() > 0) {
      sb.append("\n esriDocID=").append(getEsriDocID());
    }
    if (getFileIdentifier().length() > 0) {
      sb.append("\n fileIdentifier=").append(getFileIdentifier());
    }
    sb.append("\n envelope:");
    sb.append(" west=").append(getEnvelope().getMinX());
    sb.append(" south=").append(getEnvelope().getMinY());
    sb.append(" east=").append(getEnvelope().getMaxX());
    sb.append(" north=").append(getEnvelope().getMaxY());
    if (getResourceType().length() > 0) {
      sb.append("\n resourceType=").append(getResourceType());
    }
    if (getResourceUrl().length() > 0) {
      sb.append("\n resourceUrl=").append(this.getResourceUrl());
    }
    if (getThumbnailUrl().length() > 0) {
      sb.append("\n thumbnailUrl=").append(this.getThumbnailUrl());
    }
    if (getWebsiteUrl().length() > 0) {
      sb.append("\n websiteUrl=").append(this.getWebsiteUrl());
    }
    if (getArcIMSContentType().length() > 0) {
      sb.append("\n aimsContentType=\"").append(getArcIMSContentType()).append("\"");
    }
    
    //sb.append("\n abstract=\n").append(getAbstract());
    //sb.append("\n thumbnailBinary=\n").append(getThumbnailBinary());
  }
  
  /**
   * Evaluates a parameter's meaning.
   * @param parameter the subject parameter
   */
  public void evaluate(Parameter parameter) {
    String sValue = parameter.getContent().getSingleValue().getValue();
    String sMeaningType = parameter.getMeaningType();
    if (sMeaningType.equals("")) {
      
    // handle the envelope parts
    } else if (sMeaningType.equalsIgnoreCase(Meaning.MEANINGTYPE_ENVELOPE_EAST)) {
      getEnvelope().setMaxX(sValue);
    } else if (sMeaningType.equalsIgnoreCase(Meaning.MEANINGTYPE_ENVELOPE_NORTH)) {
      getEnvelope().setMaxY(sValue);
    } else if (sMeaningType.equalsIgnoreCase(Meaning.MEANINGTYPE_ENVELOPE_SOUTH)) {
      getEnvelope().setMinY(sValue);
    } else if (sMeaningType.equalsIgnoreCase(Meaning.MEANINGTYPE_ENVELOPE_WEST)) {
      getEnvelope().setMinX(sValue);
      
    } else if (sMeaningType.equalsIgnoreCase(Meaning.MEANINGTYPE_ESRIDOCID)) {
      setEsriDocID(sValue);
      
    } else if (sMeaningType.equalsIgnoreCase(Meaning.MEANINGTYPE_RESOURCE_TYPE)) {
      setResourceType(sValue);
      if (getArcIMSContentType().length() == 0) {
        setArcIMSContentType(
            getResourceIdentifier().guessArcIMSContentTypeFromResourceType(getResourceType()));
      }
      
    } else if (sMeaningType.equalsIgnoreCase(Meaning.MEANINGTYPE_RESOURCE_URL)) {
      IStoreable storeable = this._storables.get(Meaning.MEANINGTYPE_RESOURCE_URL);
      if (storeable != null) {
        storeable.setValues(parameter.getContent().toValueArray());
        if (getArcIMSContentType().length() == 0) {
          setArcIMSContentType(
              getResourceIdentifier().guessArcIMSContentTypeFromUrl(getResourceUrl()));
        }
      }
      
    } else if (sMeaningType.equalsIgnoreCase(Meaning.MEANINGTYPE_DATATHEME)) {
      IStoreable storeable = this._storables.get(Meaning.MEANINGTYPE_DATATHEME);
      if (storeable != null) {
        String[] values = parameter.getContent().toValueArray();
        if ((values != null) && (values.length > 0)) {
          Map<String,String> map = makeEsriDataThemeCodes();
          ArrayList<String> al = new ArrayList<String>();
          for (String value: values) {
            String s = map.get(value);
            if (s != null) al.add(s);
            else al.add(value);
          }
          storeable.setValues(al.toArray(new String[0]));
        }
      }      
      
    } else if (sMeaningType.equalsIgnoreCase(Meaning.MEANINGTYPE_THUMBNAIL_BINARY)) {
      _thumbnailBinary = sValue;
            
    // handle the generic case
    } else {
      IStoreable storeable = this._storables.get(sMeaningType);
      if (storeable == null) {
        // TODO log a warning
      } else {
        // TODO need type checking
        String[] values = parameter.getContent().toValueArray();
        if ((values != null) && (values.length > 0)) {
          Object[] existingValues = storeable.getValues();
          if ((existingValues != null) && (existingValues.length > 0)) {
            java.util.ArrayList<Object> alValues = new ArrayList<Object>();
            for (Object value: existingValues) alValues.add(value);
            for (String value: values) alValues.add(value);
            storeable.setValues(alValues.toArray());
          } else {
            storeable.setValues(values);
          }
        }
      }
    }
    
  }
  
  private String getFirstStoreableValue(String name) {
    IStoreable storeable = this._storables.get(name);
    if (storeable != null) {
      Object[] values = storeable.getValues();
      if ((values != null) && (values.length > 0) && (values[0] != null)) {
        return values[0].toString();
      }
    }
    return "";
  }
  
  // ESRI data theme lookup codes
  private Map<String,String> makeEsriDataThemeCodes() {
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
    return map;
  }
  
  /**
   * Makes a resource URL from a set of ESRI tyags.
   * @param esriTags the set of ESRI tags
   * @param meaning meaning
   */
  public static String makeResourceUrl(EsriTags esriTags, Meaning meaning) {
    
    // initialize
    String sResourceUrl = "";
    String sServer = esriTags.getServer();
    String sService = esriTags.getService();
    String sServiceType = esriTags.getServiceType();
    String sServiceParams = esriTags.getServiceParams();
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
            if (meaning != null) meaning.setArcIMSContentType("liveData");
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
        String sContentType = ResourceIdentifier.newIdentifier(null).guessArcIMSContentTypeFromUrl(sServer);
        if ((sContentType.length() > 0) && !sContentType.equalsIgnoreCase("unknown")) {
          if (meaning != null)  meaning.setArcIMSContentType(sContentType);
          sResourceUrl = sServer;
        }
      } 
         
    }
    
    return sResourceUrl;
  }
  
}
