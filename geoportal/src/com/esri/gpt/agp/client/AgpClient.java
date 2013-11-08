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
package com.esri.gpt.agp.client;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esri.gpt.framework.http.ContentHandler;
import com.esri.gpt.framework.http.ContentProvider;
import com.esri.gpt.framework.http.HttpClientException;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.StringHandler;
import com.esri.gpt.framework.http.StringProvider;
import com.esri.gpt.framework.util.Val;
import org.apache.commons.httpclient.Header;

import org.apache.commons.httpclient.HttpClient;
import org.json.JSONObject;

/**
 * An ArcGIS Portal client.
 */
public class AgpClient {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static final Logger LOGGER = Logger.getLogger(AgpClient.class.getName());
  
  /** instance variables ====================================================== */
  private HttpClient batchHttpClient;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpClient() {
    super();
    this.batchHttpClient = new HttpClient();
  }
    
  /** methods ================================================================= */
  
  /**
   * Closes any open resources.
   */
  public void close() {
    if ((this.batchHttpClient != null) && 
        (this.batchHttpClient.getHttpConnectionManager() != null)) {
      this.batchHttpClient.getHttpConnectionManager().closeIdleConnections(0);
    }
    this.batchHttpClient = null;
  }
  
  /**
   * Executes a request expecting a JSON response.
   * @param url the URL
   * @param requestHeader optional request header properties
   * @param contentProvider the request body content provider
   * @return the JSON response object
   * @throws Exception is an exception occurs
   */
  public JSONObject executeJsonRequest(String url, 
                                       AgpProperties requestHeader, 
                                       ContentProvider contentProvider) 
    throws Exception {
    LOGGER.finest("Sending URL: "+url);
    StringHandler handler = new StringHandler();
    this.executeRequest(url,requestHeader,contentProvider,handler);
    String sResponse = handler.getContent();
    LOGGER.finest("Response for URL: "+url+"\n"+sResponse);
    
    JSONObject jsoResponse = null;
    try {
      if (sResponse == null) {
        LOGGER.finest("Response for URL: "+url+"\nnull response");
      } else if (sResponse.length() == 0) {
        LOGGER.finest("Response for URL: "+url+"\nempty response");
      } else {
        jsoResponse = new JSONObject(sResponse); 
        if (jsoResponse.has("error") && (!jsoResponse.isNull("error"))) {
          AgpError agpError = new AgpError();
          agpError.parse(jsoResponse);
          throw new AgpException(agpError);
        } 
      }
    } catch (AgpException e) {
      LOGGER.log(Level.FINEST,"Request failed.",e);
      throw e;
    } catch (Throwable t) {
      LOGGER.log(Level.FINEST,"Request failed.",t);
      AgpError agpError = new AgpError();
      agpError.setMessage(t.toString());
      throw new AgpException(agpError);
    }
    return jsoResponse;
  }
  
  /**
   * Executes a request expecting a JSON response.
   * @param url the URL
   * @param requestHeader optional request header properties
   * @param content the request body content
   * @param contentType the request body content type
   * @return the JSON response object
   * @throws Exception is an exception occurs
   */
  public JSONObject executeJsonRequest(String url, 
                                       AgpProperties requestHeader, 
                                       StringBuilder content, 
                                       String contentType) 
    throws Exception {
    ContentProvider provider = null;
    if ((content != null) && (content.length() > 0)) {
      provider = new StringProvider(content.toString(),contentType);
    } 
    return this.executeJsonRequest(url,requestHeader,provider);
  }
  
  /**
   * Executes a request.
   * @param url the URL
   * @param requestHeader optional request header properties
   * @param contentProvider the request body content provider
   * @param contentHandler the response body content handler
   * @throws Exception is an exception occurs
   */
  public void executeRequest(String url, 
                             AgpProperties requestHeader, 
                             ContentProvider contentProvider, 
                             ContentHandler contentHandler) 
    throws Exception {
    HttpClientRequest http = new HttpClientRequest();
    http.setRetries(-1);
    http.setBatchHttpClient(this.batchHttpClient);
    http.setUrl(url);
    if (requestHeader != null) {
      for (AgpProperty prop: requestHeader.values()) {
        http.setRequestHeader(prop.getName(),prop.getValue());
      }
    }
    http.setContentProvider(contentProvider);
    http.setContentHandler(contentHandler);
    
    try {
      http.execute();
    } catch (HttpClientException ex) {
      boolean doThrow = true;
      if (ex.getHttpStatusCode()==302) {
        // This part of the code deals with redirect issue which accurs when
        // harvesting from arcgis.com
        Header hdrLocation = http.getResponseInfo().getResponseHeader("Location");
        if (hdrLocation!=null) {
          String location = Val.chkStr(hdrLocation.getValue());
          if (url.endsWith("/data") && location.contains("ago-item-storage")) {
            location = location.replaceAll("^https:", "http:");
            doThrow = false;
            HttpClientRequest http2 = new HttpClientRequest();
            http2.setUrl(location);
            http2.setRetries(-1);
            http2.setContentHandler(contentHandler);
            http2.execute();
          }
        }
      }
      if (doThrow) {
        throw ex;
      }
    }
  }
  
}