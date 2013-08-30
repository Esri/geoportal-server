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
package com.esri.gpt.catalog.arcgis.metadata;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.util.Val;

import com.esri.arcgisws.Envelope;
import com.esri.arcgisws.EnvelopeN;
import com.esri.arcgisws.GeographicCoordinateSystem;
import com.esri.arcgisws.SpatialReference;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Stores basic properties about a service.
 */
public class ServiceInfo {
  
  /** class variables ========================================================= */
  
  /** Logger */
  private static final Logger LOGGER = Logger.getLogger(ServiceInfo.class.getName());
  
  /** instance variables ====================================================== */
  private String    capabilities;
  private String    creator;
  private String    description;
  private Envelope  envelope;
  private StringSet keywords = new StringSet();
  private String    name;
  private String    parentType;
  private RDFPairs  rdfPairs = new RDFPairs();
  private String    resourceUrl;
  private String    restUrl;
  private String    soapUrl;
  private String    thumbnailUrl;
  private String    type;
  private ServiceInfo parentInfo;
  private List<LayerInfo> layersInfo = new ArrayList<LayerInfo>();
  private String    copyright = "";
  private String    text = "";
  
  /** constructors ============================================================ */

  /** Default constructor. */
  public ServiceInfo() {}

  /** properties ============================================================== */
  /**
   * Gets text info.
   * @return text info
   */
  public String getText() {
    return text;
  }

  /**
   * Sets text info.
   * @param text text info
   */
  public void setText(String text) {
    this.text = Val.chkStr(text);
  }

  /**
   * Gets layers info.
   * @return list of layers info
   */
  public List<LayerInfo> getLayersInfo() {
    return layersInfo;
  }
  
  /**
   * Sets layers info.
   * @param layersInfo list of layers info
   */
  public void setLayersInfo(List<LayerInfo> layersInfo) {
    this.layersInfo = layersInfo!=null? layersInfo: new ArrayList<LayerInfo>();
  }

  /**
   * Gets copyright info.
   * @return copyright info
   */
  public String getCopyright() {
    return copyright;
  }

  /**
   * Sets copyright info.
   * @param copyright copyright info
   */
  public void setCopyright(String copyright) {
    this.copyright = Val.chkStr(copyright);
  }

  
  /**
   * Gets parent info.
   * @return parent info
   */
  public ServiceInfo getParentInfo() {
    return parentInfo;
  }

  /**
   * Sets parent info.
   * @param parentInfo parent info 
   */
  public void setParentInfo(ServiceInfo parentInfo) {
    this.parentInfo = parentInfo;
  }
  
  /**
   * Gets the service creator.
   * @return the service creator
   */
  public String getCreator() {
    return this.creator;
  }
  /**
   * Sets the service creator.
   * @param creator the service creator
   */
  public void setCreator(String creator) {
    this.creator = creator;
  }
  
  /**
   * Gets the capabilities string associated with the service.
   * @return the capabilities
   */
  public String getCapabilities() {
    return this.capabilities;
  }
  /**
   * Sets the capabilities string associated with the service.
   * @param capabilities the capabilities
   */
  public void setCapabilities(String capabilities) {
    this.capabilities = capabilities;
  }
  
  /**
   * Gets the service description.
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }
  /**
   * Sets the service description.
   * @param description the description
   */
  public void setDescription(String description) {
    this.description = description;
  }
  
  /**
   * Gets the service envelope.
   * @return the envelope
   */
  public Envelope getEnvelope() {
    return this.envelope;
  }
  
  /**
   * Sets the service envelope.
   * @param envelope the envelope
   */
  public void setEnvelope(Envelope envelope) {
    this.envelope = envelope;
  }
  
  /**
   * Gets the keywords associated with the service.
   * @return the keywords
   */
  public StringSet getKeywords() {
    return this.keywords;
  }
  
  /**
   * Gets the service name.
   * @return the name
   */
  public String getName() {
    return this.name;
  }
  /**
   * Sets the service name.
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * Gets the parent service type.
   * @return the parent type
   */
  public String getParentType() {
    return this.parentType;
  }
  /**
   * Sets the parent service type.
   * @param type the parent type
   */
  public void setParentType(String type) {
    this.parentType = type;
  }
  
  /**
   * Gets the catch-all map of RDF pairs associated with the service.
   * @return the catch-all map of RDF pairs
   */
  private RDFPairs getRDFPairs() {
    return this.rdfPairs;
  }
  
  /**
   * Gets the resource URL for the service.
   * @return the resource URL
   */
  public String getResourceUrl() {
    return this.resourceUrl;
  }
  /**
   * Sets the resource URL for the service.
   * @param url the resource URL
   */
  public void setResourceUrl(String url) {
    this.resourceUrl = url;
  }
  
  /**
   * Gets the REST URL for the service.
   * @return the REST URL
   */
  public String getRestUrl() {
    return this.restUrl;
  }
  /**
   * Sets the REST URL for the service.
   * @param url the REST URL
   */
  public void setRestUrl(String url) {
    this.restUrl = url;
  }
  
  /**
   * Gets the SOAP URL for the service.
   * @return the SOAP URL
   */
  public String getSoapUrl() {
    return this.soapUrl;
  }
  /**
   * Sets the SOAP URL for the service.
   * @param url the SOAP URL
   */
  public void setSoapUrl(String url) {
    this.soapUrl = url;
  }
  
  /**
   * Gets the thumbnail URL for the service.
   * @return the thumbnail URL
   */
  public String getThumbnailUrl() {
    return this.thumbnailUrl;
  }
  /**
   * Sets the thumbnail URL for the service.
   * @param url the thumbnail URL
   */
  public void setThumbnailUrl(String url) {
    this.thumbnailUrl = url;
  }
  
  /**
   * Gets the service type.
   * @return the type
   */
  public String getType() {
    return this.type;
  }
  /**
   * Sets the service type.
   * @param type the type
   */
  public void setType(String type) {
    this.type = type;
  }
  
  /** methods ================================================================= */
  
  /**
   * Adds one or more keywords to the keyword set.
   * @param words the delimited list of words to add
   * @param delimiter thedelimited (can be null)
   */
  public void addKeywords(String words, String delimiter) {
    words = Val.chkStr(words);
    if (words.length() > 0) {
      if ((delimiter != null) && (delimiter.length() > 0)) {
        String[] tokens = words.split(delimiter);
        for (String token: tokens) {
          String word = Val.chkStr(token);
          if (word.length() > 0) {
            getKeywords().add(word);
          }
        }
      } else {
        getKeywords().add(words);
      }
    }
  }
  
  /**
   * Adds a predicate/value pair to the catch-all map of RDF pairs associated with the service.
   * @param predicate the predicate URI
   * @param value the literal value
   */
  public void addRDFPair(String predicate, String value) {
    this.getRDFPairs().addValue(predicate,value);
  }
  
  /**
   * Returns the Dublin Core metadata for the service.
   * @param processor the ArcGIS Server service processor
   * @return the Dublin Core metadata
   * @throws Exception if an exception occurs
   */
  public String asDublinCore(AGSProcessor processor) throws Exception {
    return asDublinCore(
        processor.getContext().getRequestContext().getApplicationConfiguration(),
        processor.getContext().getHttpClient());
  }

  /**
   * Returns the Dublin Core metadata for the service.
   * @param cfg application configuration
   * @param http HTTP client request
   * @return the Dublin Core metadata
   * @throws Exception if an exception occurs
   */
  public String asDublinCore(ApplicationConfiguration cfg, HttpClientRequest http) throws Exception {

    String url = this.getResourceUrl();

    String tmp;
    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    sb.append("\r<rdf:RDF");
    sb.append(" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");
    sb.append(" xmlns:dc=\"http://purl.org/dc/elements/1.1/\"");
    sb.append(" xmlns:dct=\"http://purl.org/dc/terms/\"");
    sb.append(" xmlns:dcmiBox=\"http://dublincore.org/documents/2000/07/11/dcmi-box/\"");
    sb.append(" xmlns:ows=\"http://www.opengis.net/ows\"");
    sb.append(">");
    sb.append("\r<rdf:Description");
    if (url.length() > 0) {
      sb.append(" rdf:about=\"").append(Val.escapeXml(url)).append("\"");
    }
    sb.append(">");

    // identifier
    if (url.length() > 0) {
      sb.append("\r<dc:identifier>").append(Val.escapeXml(url)).append("</dc:identifier>");
    }

    // title, description, creator
    tmp = Val.chkStr(this.getName());
    if (tmp.length() > 0) {
      sb.append("\r<dc:title>").append(Val.escapeXml(tmp)).append("</dc:title>");
    }
    tmp = Val.chkStr(this.getDescription());
    if (tmp.length() > 0) {
      sb.append("\r<dc:description>").append(Val.escapeXml(tmp)).append("</dc:description>");
    }
    tmp = Val.chkStr(this.getCreator());
    if (tmp.length() > 0) {
      sb.append("\r<dc:creator>").append(Val.escapeXml(tmp)).append("</dc:creator>");
    }

    // dc:format (mime-type)
    // dc:type ??
    // dc:date ??
    // dct:alternative alternative name for the resource

    // resource url
    if (url.length() > 0) {
      //scheme = "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server";
      String scheme = "urn:x-esri:specification:ServiceType:ArcGIS";
      tmp = Val.chkStr(this.getParentType());
      if (tmp.length() > 0) scheme += ":"+tmp;
      tmp = Val.chkStr(this.getType());
      if (tmp.length() > 0) scheme += ":"+tmp;
      sb.append("\r<dct:references");
      sb.append(" scheme=\"").append(Val.escapeXml(scheme)).append("\">");
      sb.append(Val.escapeXml(url)).append("</dct:references>");
    }

    // thumbnail url
    tmp = Val.chkStr(this.getThumbnailUrl());
    if (tmp.length() > 0) {
      String scheme = "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Thumbnail";
      sb.append("\r<dct:references");
      sb.append(" scheme=\"").append(Val.escapeXml(scheme)).append("\">");
      sb.append(Val.escapeXml(tmp)).append("</dct:references>");
    }

    // keywords
    for (String keyword: this.getKeywords()) {
      sb.append("\r<dc:subject>").append(Val.escapeXml(keyword)).append("</dc:subject>");
    }

    // envelope
    double[] env = this.validateEnvelope(cfg, http,this.getEnvelope());
    if (env != null) {
      String lower = env[0]+" "+env[1];
      String upper = env[2]+" "+env[3];
      sb.append("\r<ows:WGS84BoundingBox>");
      sb.append("\r<ows:LowerCorner>").append(Val.escapeXml(lower)).append("</ows:LowerCorner>");
      sb.append("\r<ows:UpperCorner>").append(Val.escapeXml(upper)).append("</ows:UpperCorner>");
      sb.append("\r</ows:WGS84BoundingBox>");
    }

    // RDF pairs
    if (this.getRDFPairs().size() > 0) {
      sb.append("\r<dct:abstract>");
      for (Map.Entry<String,RDFPair> entry: this.getRDFPairs().entrySet()) {
        RDFPair rdfPair = entry.getValue();
        for (String rdfValue: rdfPair.getValues()) {
          sb.append("\r<rdf:value");
          sb.append(" rdf:resource=\"").append(rdfPair.getPredicate()).append("\">");
          sb.append(Val.escapeXml(rdfValue));
          sb.append("\r</rdf:value>");
        }
      }
      sb.append("\r</dct:abstract>");
    }
    sb.append("\r</rdf:Description>");
    sb.append("\r</rdf:RDF>");

    // TODO : logging here?
    //System.err.println(sb.toString());

    return sb.toString();

  }
  
  /**
   * Returns a string representation of the object.
   * @return the string
   */
  @Override
  public String toString() {    
    StringBuilder sb = new StringBuilder();
    sb.append(this.getName());
    sb.append("\n  name=").append(this.getName());
    sb.append("\n  type=").append(this.getType());
    sb.append("\n  parentType=").append(this.getParentType());
    sb.append("\n  resourceUrl=").append(this.getResourceUrl());
    sb.append("\n  restUrl=").append(this.getRestUrl());
    sb.append("\n  soapUrl=").append(this.getSoapUrl());
    sb.append("\n  thumbnailUrl=").append(this.getThumbnailUrl());
    sb.append("\n  capabilities=").append(this.getCapabilities());
    sb.append("\n  creator=").append(this.getCreator());
    sb.append("\n  description=").append(this.getDescription());
    sb.append("\n  keywords=").append(this.getKeywords());
    if (this.getEnvelope() != null) {
      if (this.getEnvelope() instanceof EnvelopeN) {
        EnvelopeN envn = (EnvelopeN)this.getEnvelope();
        sb.append("\n  envelope=");
        sb.append(envn.getXMin()).append(", ").append(envn.getYMin()).append(", ");
        sb.append(envn.getXMax()).append(", ").append(envn.getYMax());
        if (envn.getSpatialReference() != null) {
          sb.append(" wkid=").append(envn.getSpatialReference().getWKID());
        }
      }
    }
    return sb.toString();
  }

  private double[] validateEnvelope(AGSProcessor processor, Envelope env) throws Exception {
    return validateEnvelope(
        processor.getContext().getRequestContext().getApplicationConfiguration(),
        processor.getContext().getHttpClient(), env);
  }

  private double[] validateEnvelope(ApplicationConfiguration cfg, HttpClientRequest http, Envelope env) throws Exception {

    // initialize envelope properties
    if ((env == null) || !(env instanceof EnvelopeN)) {
      return null;
    }
    EnvelopeN envn = (EnvelopeN)env;
    SpatialReference spref = envn.getSpatialReference();
    if ((spref == null) || (spref.getWKID() == null)) {
      return null;
    }
    int wkid = spref.getWKID().intValue();
    double xmin = envn.getXMin();
    double ymin = envn.getYMin();
    double xmax = envn.getXMax();
    double ymax = envn.getYMax();

    // project if required
    if (wkid == 4326) {
      return new double[]{xmin,ymin,xmax,ymax};
    //} else if (spref instanceof GeographicCoordinateSystem) {
    //  return new double[]{xmin,ymin,xmax,ymax};
    } else {

      // determine the rest url to the geometry service
      String geomRestUrl = Val.chkStr(cfg.getInteractiveMap().getGeometryServiceUrl());
      if (geomRestUrl.length() == 0) {
        LOGGER.warning("A geometryServiceUrl has not been configured, envelope projection is unavailable.");
        return null;
      }

      // build the projection service url
      StringBuilder sb = new StringBuilder();
      StringBuilder sbg = new StringBuilder();
      sb.append(geomRestUrl).append("/project");
      sb.append("?f=json").append("&inSR=").append(wkid).append("&outSR=4326");
      sbg.append("{\"geometryType\":\"esriGeometryEnvelope\",\"geometries\":[{");
      sbg.append("\"xmin\":").append(xmin);
      sbg.append(",\"ymin\":").append(ymin);
      sbg.append(",\"xmax\":").append(xmax);
      sbg.append(",\"ymax\":").append(ymax);
      sbg.append("}]}");
      sb.append("&geometries=").append(URLEncoder.encode(sbg.toString(),"UTF-8"));
      String projectionUrl = sb.toString();

      // execute the projection, parse the JSON response
      http.setUrl(projectionUrl);
      try {
        String response = http.readResponseAsCharacters();
        JSONObject jso = new JSONObject(response);
        if (jso != null) {
          JSONArray jsoGeometries = jso.getJSONArray("geometries");
          if ((jsoGeometries != null) && (jsoGeometries.length() == 1)) {
            JSONObject jsoEnv = jsoGeometries.getJSONObject(0);
            if (jsoEnv != null) {
              double jsoXmin = jsoEnv.getDouble("xmin");
              double jsoYmin = jsoEnv.getDouble("ymin");
              double jsoXmax = jsoEnv.getDouble("xmax");
              double jsoYmax = jsoEnv.getDouble("ymax");
              return new double[]{jsoXmin,jsoYmin,jsoXmax,jsoYmax};
            }
          }
        }

      } catch (IOException e) {
        String msg = "Error projecting envelope, url="+projectionUrl;
        LOGGER.warning(msg+"\n "+e.toString());
      } catch (JSONException e) {
        String msg = "Error projecting envelope, problem parsing JSON response, url="+projectionUrl;
        LOGGER.warning(msg+"\n "+e.toString());
      }

    }

    // if we get this far and the spatial reference is geographic, then return the envelope
    if (spref instanceof GeographicCoordinateSystem) {
      return new double[]{xmin,ymin,xmax,ymax};
    }

    return null;
  }
  
  public static final class LayerInfo {
    private String name;
    private String title;
    
    public LayerInfo(String name, String title) {
      this.name = name;
      this.title = title;
    }
    
    public String getName() {
      return name;
    }
    
    public String getTitle() {
      return title;
    }
  }
}
