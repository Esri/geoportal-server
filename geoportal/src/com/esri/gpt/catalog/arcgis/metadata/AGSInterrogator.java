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
import com.esri.gpt.catalog.publication.ProcessingContext;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.util.Val;

import com.esri.arcgisws.ServiceCatalogBindingStub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Interrogates an ArcGIS server target to determine the REST and SOAP endpoints
 * for an ArcGIS server services catalog.
 */
public class AGSInterrogator {
  
  /** class variables ========================================================= */
  
  /** Logger */
  private static Logger LOGGER = Logger.getLogger(AGSInterrogator.class.getName());
  
  /** instance variables ====================================================== */
  private HttpClientRequest httpClient;
  private AGSTarget         target;
    
  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied processing context and ArcGIS server target.
   * @param context the processing context
   * @param target the ArcGIS server target
   */
  public AGSInterrogator(ProcessingContext context, AGSTarget target) {
    this(context.getHttpClient(), target);
  }

  /**
   * Constructs with a supplied processing context and ArcGIS server target.
   * @param httpClient HTTP client
   * @param target the ArcGIS server target
   */
  public AGSInterrogator(HttpClientRequest httpClient, AGSTarget target) {
    this.httpClient = httpClient;
    this.target = target;
  }
  
  /** methods ================================================================= */
  
  /**
   * Determines the SOAP endpoint for ArcGIS Server services catalog based upon the
   * REST endpoint to the services catalog.
   */
  private void determineSoapUrl() throws IOException {
    String restUrl = this.target.getRestUrl();
    if ((restUrl == null) || (restUrl.length() == 0)) {
      return;
    }
    
    // start with a guess, it's usually correct
    boolean guess = true;
    if (guess && ((this.target.getSoapUrl() == null) || (this.target.getSoapUrl().length() == 0))) {
      if (restUrl.toLowerCase().endsWith("rest/services")) {
        String tmp1 = restUrl.substring(0,restUrl.length() - 14);
        String tmp2 = restUrl.substring(restUrl.length() - 9);
        String soapEndpoint = tmp1+tmp2;
        try {
          String validated = this.pingCatalogWsdl(soapEndpoint);
          this.target.setSoapUrl(validated);
          String msg = "ArcGIS services catalog soap url guessed from rest url:";
          msg += "\n restUrl="+restUrl+"\n soapUrl="+this.target.getSoapUrl();
          LOGGER.finer(msg);
          return;
        } catch (IOException ioe) { 
          String msg = "ArcGIS services catalog (soap) not found at guessed endpoint:";
          msg += "\n restUrl="+restUrl+"\n soapUrl="+soapEndpoint;
          LOGGER.finest(msg+"\n"+ioe.toString());
        }
      }
    }
    
    // loop through the services and try to scrape a SOAP endpoint from an HTML page
    if ((this.target.getSoapUrl() == null) || (this.target.getSoapUrl().length() == 0)) {
      String soapEndpoint = Val.chkStr(this.determineSoapUrl(restUrl));
      if (soapEndpoint.length() > 0) {
        this.target.setSoapUrl(soapEndpoint);
        String msg = "ArcGIS services catalog soap url determined from html scrape:";
        msg += "\n restUrl="+restUrl+"\n soapUrl="+this.target.getSoapUrl();
        LOGGER.finer(msg);
        return;
      }
    }
    
    // loop through the services and try to scrape a SOAP endpoint from an HTML page
    if ((this.target.getSoapUrl() == null) || (this.target.getSoapUrl().length() == 0)) {
      String msg = "Unable to determine ArcGIS Server services directory SOAP endpoint";
      msg += " associated with REST endpoint: "+restUrl;
      throw new IOException(msg);
    }
    
  }
  
  /**
   * Loops through JSON/HTML REST responses from an ArcGIS services tree in order to 
   * determine the SOAP endpoint for ArcGIS Server services catalog
   * @param baseUrl the current base URL associated with a REST endpoint of the services tree
   * @return the SOAP endpoint to the ArcGIS Server services catalog (can be null)
   */
  private String determineSoapUrl(String baseUrl) {
    
    // submit the json request
    String restUrl = this.target.getRestUrl();
    JSONObject jsoParent = null;
    try {
      this.httpClient.setUrl(baseUrl+"?f=json");
      String json = this.httpClient.readResponseAsCharacters();
      jsoParent = new JSONObject(json);
    } catch (JSONException e) {
      LOGGER.finest("Invalid JSON response: "+e.toString());
      return null;
    } catch (IOException ioe) {
      LOGGER.finest("Cannot scrape HTML response: "+ioe.toString());
    }
    if (jsoParent == null) return null;
    
    // loop through the services
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
              //if (this.baseUrl.lastIndexOf("/") != -1) {
              //  String folder = this.baseUrl.substring(this.baseUrl.lastIndexOf("/")+1);
              //  if (name.startsWith(folder+"/")) {
               //   name = name.substring(name.lastIndexOf("/")+1);
               // }
              //}
            }
            
            // scrape the service HTML page for a SOAP URL
            if ((name.length() > 0) && (type.length() > 0)) {
              String soapEndpoint = "";
              boolean considerReverseProxy = false;
              try {
                String relative = "/"+name+"/"+type;
                String currentUrl = restUrl+relative+"?f=html";
                this.httpClient.setUrl(currentUrl);
                String html = httpClient.readResponseAsCharacters();
                soapEndpoint = Val.chkStr(this.scapeHtmlForSoapEndpoint(html));
                if (soapEndpoint.length() > 0) {
                  int idx = soapEndpoint.toLowerCase().indexOf(relative.toLowerCase());
                  if (idx != -1) {
                    soapEndpoint = soapEndpoint.substring(0,idx);
                  }
                }
              } catch (IOException ioe) {
                String msg = "Cannot scrape ArcGIS service html response::";
                msg += "\n restUrl="+restUrl+"\n htmlUrl="+this.httpClient.getUrl();
                LOGGER.finer(msg+"\n"+ioe.toString());
              }
              
              // validate the soap endpoint
              try {
                if (soapEndpoint.length() > 0) {
                  considerReverseProxy = true;
                  String validated = this.pingCatalogWsdl(soapEndpoint);
                  return validated;
                }
              } catch (IOException ioe) {
                String msg = "Unable to ping ArcGIS services catalog soap url:";
                msg += "\n restUrl="+restUrl+"\n soapUrl="+soapEndpoint;
                LOGGER.finer(msg+"\n"+ioe.toString());
              }
              
              // try again considering a revese proxy for the soap endpoint
              try {
                if (considerReverseProxy) {
                  soapEndpoint = Val.chkStr(guessReverseProxyUrl(soapEndpoint));
                  if (soapEndpoint.length() > 0) {
                    String validated = this.pingCatalogWsdl(soapEndpoint);
                    return validated;
                  }
                }
              } catch (IOException ioe) {
                String msg = "Unable to ping ArcGIS services catalog soap url (try reverse proxy):";
                msg += "\n restUrl="+restUrl+"\n soapUrl="+soapEndpoint;
                LOGGER.finer(msg+"\n"+ioe.toString());
              }
              
            }
          }
        }
      }
    } catch (JSONException e) {
      LOGGER.finest("No ArcGIS services: "+baseUrl+" - "+e.toString());
    }
    
    // loop through the folders
    try {
      JSONArray jsoFolders = jsoParent.getJSONArray("folders");
      if (jsoFolders != null) {
        for (int i=0;i<jsoFolders.length();i++) {
          String name = Val.chkStr(jsoFolders.getString(i));
          if (name.length() > 0) {
            String currentUrl = restUrl+"/"+name;
            String soapEndpoint = this.determineSoapUrl(currentUrl);
            if ((soapEndpoint != null) && (soapEndpoint.length() > 0)) {
              return soapEndpoint;
            }
          }
        }
      }
    } catch (JSONException e) {
      LOGGER.finest("No ArcGIS folders: "+baseUrl+" - "+e.toString());
    }
    
    return null;
  }
  
  /**
   * Makes a guess at the reverse proxy endpoint assoctated with an internal SOAP endpoint.
   * <br/>Sometimes the ArcGIS server REST API references internal SOAP endpoints that are
   * inaccessible outside the local area network.
   * <br/>this method simple replaces the host:port for a SOAP endpoint with the host:port 
   * of the pre-determined REST endpoint
   * @param soapEndpoint the SOAP endpoint that failed
   * @return the modified endpoint
   */
  private String guessReverseProxyUrl(String soapEndpoint) {
    try {
      URL urlRest = new URL(this.target.getRestUrl());
      URL urlSoap = new URL(soapEndpoint);
      String reversed = urlRest.getProtocol()+"://"+urlRest.getHost();
      if ((urlRest.getPort() != -1) && (urlRest.getPort() != 80)) {
        reversed += ":"+urlRest.getPort();
      }
      if ((urlSoap.getPath() != null) && (urlSoap.getPath().length() > 0)) {
        reversed += urlSoap.getPath();
        return reversed;
      }
      
    } catch (MalformedURLException e) {
      String msg = "Unable to guess ArcGIS services catalog soap url (try reverse proxy):";
      msg += "\n restUrl="+this.target.getRestUrl()+"\n soapUrl="+soapEndpoint;
      LOGGER.finer(msg+"\n"+e.toString());
    }
    return null;
  }
  
  /**
   * Interrogates the character response from a target resource URL attempting to
   * determine the REST and SOAP endpoints for an ArcGIS server services catalog.
   * @param url the target URL associated with the resource being interrogated
   * @param response the character based response previously returned from the target URL
   * @return <code>true</code> if the target was recognized as an ArcGIS server endpoint
   */
  public boolean interrogate(URL url, String response) throws IOException {

    String fullUrl = url.toExternalForm();
    this.target.setTargetUrl(fullUrl);
    String servicesRoot = "";
    boolean likelyRestHtml = 
        response.contains("<meta name=\"keywords\" content=\"ArcGIS Services Directory Root\"") ||
        response.contains("ArcGIS Services Directory Root") ||
        response.contains("<td id=\"breadcrumbs\">");
    
    // determine the services directory root (REST)
    if (likelyRestHtml) {
      String chk = "<td id=\"breadcrumbs\">";
      String tmp;
      int idx = response.indexOf(chk);
      if (idx != -1) {
        tmp = response.substring(idx + chk.length());
        chk = "<a href=\"";
        idx = tmp.indexOf(chk);
        if (idx != -1) {
          tmp = tmp.substring(idx + chk.length());
          chk = "\">";
          //chk = "\">Home</a>";
          idx = tmp.indexOf(chk);
          if (idx != -1) {
            String relative = Val.chkStr(tmp.substring(0,idx));
            if (relative.length() > 0) {
              idx = fullUrl.toLowerCase().indexOf(relative.toLowerCase());
              if (idx != -1) {
                servicesRoot = fullUrl.substring(0,(idx + relative.length()));
              }
            }
          }
        }
      }
    }
    
    // if a services directory root (REST) was found, 
    // flag and attempt to determine the services directory root (SOAP) 
    if (servicesRoot.length() > 0) {
      this.target.setRestUrl(servicesRoot);
      this.target.setWasRecognized(true);
      boolean isRoot = servicesRoot.equals(fullUrl);
      boolean hasFolders = response.contains("<ul id='folderList'>");
      boolean hasServices = response.contains("<ul id='serviceList'>");
      boolean isContainer = isRoot || hasFolders || hasServices || response.contains("<title>Folder:");
      if (isRoot) {
        this.target.setTargetType(AGSTarget.TargetType.ROOT);
      } else if (isContainer) {
        this.target.setTargetType(AGSTarget.TargetType.FOLDER);
      }
      
      // determine the services directory root (SOAP)
      this.determineSoapUrl();
    }
    
    return this.target.getWasRecognized();
  }
  
  /**
   * Attempts to hit the SOAP endpoint for an ArcGIS services catalog through
   * a ServiceCatalogBindingStub.
   * @param baseUrl the SOAP url for the ArcGIS services catalog
   * @return the supplied url is returned if no exception was encountered
   * @throws IOException if an exception occurs
   */
  private String pingCatalogWsdl(String baseUrl) throws IOException {
    ServiceCatalogBindingStub stub = new ServiceCatalogBindingStub(baseUrl);
    stub.getFolders();
    return baseUrl;
  }
  
  /**
   * Scrapes an HTML response string (ArcGIS REST response) to determine a SOAP endpoint
   * to the ArcGIS server.
   * @param html the HTML to scrape
   * @return a located SOAP endpoint (can be null)
   */
  private String scapeHtmlForSoapEndpoint(String html) {
    
    // here is an example of the pattern we are looking for
    // <a href="http://host:poty/arcgis/services/name/MapServer?wsdl">SOAP</a>
    int idx = html.indexOf("?wsdl\"");
    if (idx != -1) {
      String tmp = html.substring(0,idx);
      idx = tmp.lastIndexOf(" href=\"");
      if (idx != -1) {
        tmp = tmp.substring(idx+7);
        try {
          new URL(Val.chkStr(tmp));
          return tmp;
        } catch (MalformedURLException e) {
        }
      }
    }
    return null;
  }
    
}
