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
package com.esri.gpt.control.publication;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.util.Val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

/**
 * Reads the XML content associated with a resource. The source for the resource can be an XML file, 
 * an XML HTTP response file, or an ArcGIS JSON response.
 */
public class ResourceReader {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(ResourceReader.class.getName());
  
  /** instance variables ====================================================== */
  private ArrayList<String> agsServiceUrls = new ArrayList<String>();
  private boolean           autoRecurse = true;
  private String            baseUrl = "";
  private String            systemId = "";
  private boolean           wasJson = false;
  
  /** Default constructor. */
  public ResourceReader() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets a list of ArcGIS server service URLs associated with the last 
   * ArcGIS server or folder read.
   * @return the list of urls
   */
  public List<String> getAgsServiceUrls() {
    return this.agsServiceUrls;
  }
  
  /** methods ================================================================= */
  
  /**
   * Reads a JSON response from an ArcGIS server Rest end-point.
   * <br/>The supplied rest URL will be appended with "f=json" if required.
   * @param restUrl the rest URL to the ArcGIS service 
   * @return a JSON object representing the response
   * @throws MalformedURLException if the URL is invalid
   * @throws IOException if the communication fails
   * @throws JSONException if a non-empty response could not be 
   *         loaded as a JSON onject
   */
  private JSONObject readAgsJson(String restUrl) 
    throws MalformedURLException, IOException, JSONException {
    if (!restUrl.toLowerCase().startsWith("http")) return null;
      
    // ensure that we are asking for a JSON response
    if ((restUrl.toLowerCase().indexOf("f=json") == -1) &&
        (restUrl.toLowerCase().indexOf("f=pjson") == -1)) {
      if (restUrl.indexOf("?") == -1) {
        restUrl += "?f=json";
      } else {
        restUrl += "&f=json";
      }
    }
    LOGGER.finer("Attempting to read ArcGIS Server JSON response for:\n"+restUrl);
    
    HttpClientRequest client = HttpClientRequest.newRequest();
    client.setUrl(restUrl);
    String sResponse = Val.chkStr(client.readResponseAsCharacters());
    
    // attempt to create a JSON object from the response
    if (sResponse.length() > 0) {
      JSONObject jso = new JSONObject(sResponse);
      this.wasJson = true;
      return jso;
    }
    return null;
  }
    
  /**
   * Reads a JSON response from an ArcGIS server Rest end-point and converts it to an
   * XML string.
   * @param url the rest URL to the ArcGIS service 
   * @return the rest response as an XML String
   * @throws MalformedURLException if the URL is invalid
   * @throws IOException if the communication fails
   * @throws JSONException if a non-empty response could not be 
   *         loaded as a JSON onject or conversion to XML fails
   * @throws TransformerFactoryConfigurationError configuration related exception
   * @throws TransformerException transformation related exception
   */
  private String readAgsXml(String url) 
    throws MalformedURLException, IOException, JSONException, 
           TransformerFactoryConfigurationError, TransformerException {
    String xml = "";
    JSONObject jso = readAgsJson(url);
    if (jso != null) {
      try {
        JSONObject jsoError = jso.getJSONObject("error");
        if (jsoError != null) {
          throw new IOException(jsoError.toString());
        }
      } catch (JSONException e) {}

      if (autoRecurse) {
        parseAgsTree(jso);
        for (String service: this.agsServiceUrls) System.err.println(service);
      }
      xml = toAgsXml(jso);
      LOGGER.finer("ArcGIS Rest/JSON to XML\n"+xml);
    }
    return xml;
  }
  
  /**
   * Fully reads the characters from an input stream.
   * @param stream the input stream
   * @param charset the encoding of the input stream
   * @return the characters read
   * @throws IOException if an exception occurs
   */
  private String readCharacters(InputStream stream, String charset)
    throws IOException {
    StringBuffer sb = new StringBuffer();
    BufferedReader br = null;
    InputStreamReader ir = null;
    try {
      if ((charset == null) || (charset.trim().length() == 0)) charset = "UTF-8";
      char cbuf[] = new char[2048];
      int n = 0;
      int nLen = cbuf.length;
      ir = new InputStreamReader(stream,charset);
      br = new BufferedReader(ir);
      while ((n = br.read(cbuf,0,nLen)) > 0) sb.append(cbuf,0,n);
    } finally {
      try {if (br != null) br.close();} catch (Exception ef) {}
      try {if (ir != null) ir.close();} catch (Exception ef) {}
    }
    return sb.toString();
  }
  
  /**
   * Reads the XML content behind a file or URL.
   * <br/>If the content returns is ArcGIS/JSON, the JSON object is converted to XML.
   * @param systemId the system id of the source (file path or URL)
   * @return the associated XML
   * @throws Exception if an exception occurs
   */
  public String readXml(String systemId) throws Exception {
    
    // initialize
    String xml = "";
    this.systemId = Val.chkStr(systemId);
    this.baseUrl = "";
    this.agsServiceUrls = new ArrayList<String>();
    boolean isHttp = false;
    try {
      URL url = new URL(systemId);
      String tmp = url.getProtocol();
      isHttp = (tmp != null) && (tmp.equals("http") || tmp.equals("https"));
    } catch (MalformedURLException e) {
      isHttp = false;
    }
    if (isHttp) {
      this.baseUrl = this.systemId;
      int nIdx = this.baseUrl.indexOf("?");
      if (nIdx != -1) this.baseUrl = this.baseUrl.substring(0,nIdx);
      
      // guess which to try first
      boolean tryAgsFirst = false;
      if (this.baseUrl.length() > 0) {
        String lc = this.systemId.toLowerCase();
        if (lc.indexOf("/rest/") != -1) {
          if (lc.indexOf("service=") == -1) tryAgsFirst = true;
        }
      }
      
      // execute
      HttpClientRequest client = HttpClientRequest.newRequest();
      client.setUrl(this.systemId);
      if (tryAgsFirst) {
        try {
          xml = this.readAgsXml(this.systemId);
        } catch (Exception e) {
          if (this.wasJson) {
            throw e;
          } else {
            String result = client.readResponseAsCharacters();
            StringReader reader = new StringReader(result);
            StringWriter writer = new StringWriter();
            transform(new StreamSource(reader),new StreamResult(writer));
            xml = Val.chkStr(writer.toString());
          }
        }
        
      } else {
        try {
          String result = client.readResponseAsCharacters();
          StringReader reader = new StringReader(result);
          StringWriter writer = new StringWriter();
          transform(new StreamSource(reader),new StreamResult(writer));
          xml = Val.chkStr(writer.toString());
        } catch (TransformerException te) {
          try {
            xml = this.readAgsXml(this.systemId);
          } catch (MalformedURLException mue) {
            throw te;
          } catch (JSONException e) {
            throw te;
          }
        }
      }
      
    } else {
      
      // read the contexts of a file path      
      StringWriter writer = new StringWriter();
      transform(new StreamSource(this.systemId),new StreamResult(writer)) ;
      xml = Val.chkStr(writer.toString());
    }
    
    return xml;
  }
  
  /**
   * Recursively parses an ArcGIS service tree.
   * <br/>Located services are appended to the "agsServicePaths" list.
   * @param jsoParent the JSON parent object (the server or a folder)
   */
  private void parseAgsTree(JSONObject jsoParent) {
    
    // parse folders if present
    ArrayList<String> folders = new ArrayList<String>();
    try {
      JSONArray jsoFolders = jsoParent.getJSONArray("folders");
      if (jsoFolders != null) {
        for (int i=0;i<jsoFolders.length();i++) {
          String name = Val.chkStr(jsoFolders.getString(i));
          if (name.length() > 0) {
            folders.add(this.baseUrl+"/"+name);
          }
        }
      }
    } catch (JSONException e) {
      LOGGER.finest("No ArcGIS folders: "+e.toString());
    }
    
    // parse services if present
    try {
      JSONArray jsoServices = jsoParent.getJSONArray("services");
      if (jsoServices != null) {
        for (int i=0;i<jsoServices.length();i++) {
          JSONObject service = jsoServices.getJSONObject(i);
          if (service != null) {
            String name = Val.chkStr(service.getString("name"));
            String type = Val.chkStr(service.getString("type"));
            
            // for some reason, service names within a folder are partial paths
            // e.g baseurl = http://server.arcgisonline.com/ArcGIS/rest/services/Elevation 
            // name = Elevation/ESRI_Elevation_World
            if (name.lastIndexOf("/") != -1) {
              if (this.baseUrl.lastIndexOf("/") != -1) {
                String folder = this.baseUrl.substring(this.baseUrl.lastIndexOf("/")+1);
                if (name.startsWith(folder+"/")) {
                  name = name.substring(name.lastIndexOf("/")+1);
                }
              }
            }
            
            if ((name.length() > 0) && (type.length() > 0)) {
              this.agsServiceUrls.add(this.baseUrl+"/"+name+"/"+type);
            }
          }
        }
      }
    } catch (JSONException e) {
      LOGGER.finest("No ArcGIS services: "+e.toString());
    }
    
    // recurse folders
    for (String folder: folders) {
      try {
        JSONObject jsoFolder = readAgsJson(folder);
        if (jsoFolder != null) {
          parseAgsTree(jsoFolder);
        }
      } catch (MalformedURLException e) {
        LOGGER.finer("Error reading folder: "+folder+", urlerr="+e.toString());
      } catch (IOException e) {
        LOGGER.finer("Error reading folder: "+folder+", ioerr="+e.toString());
      } catch (JSONException e) {
        LOGGER.finer("Error reading folder: "+folder+", jsonerr="+e.toString());
      }
    }
        
  }
  
  /**
   * Create an XML string from an ArcGIS Rest response.
   * <br/>The XML string is wrappoed in a parent tag <ags-rest>.
   * @param jso the JSON object representing the response
   * @return the XML String
   * @throws JSONException if the JDON object cannot be converted to an XML
   * @throws TransformerFactoryConfigurationError configuration related exception
   * @throws TransformerException transformation related exception
   */
  private String toAgsXml(JSONObject jso) 
    throws JSONException, TransformerFactoryConfigurationError, TransformerException {
    String xml = XML.toString(jso,"ags-rest");
    StreamSource source = new StreamSource(new StringReader(xml));
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
    transformer.setOutputProperty(OutputKeys.INDENT,"yes");
    transformer.transform(source,result);
    return Val.chkStr(writer.toString());
  }
  
  /**
   * Executes a transformation.
   * <br/>The output encoding is set to UTF-8
   * <br/>The indent is set to "yes"
   * @param source the transformation source
   * @param result the transformation result
   * @throws TransformerException if an exception occurs
   */
  private void transform(javax.xml.transform.Source source,
                         javax.xml.transform.Result result)
    throws TransformerException {
    Transformer transformer = TransformerFactory.newInstance().newTransformer() ;
    transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
    transformer.setOutputProperty(OutputKeys.INDENT,"yes");
    transformer.transform(source,result);
  }
  
}
