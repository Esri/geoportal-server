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
package com.esri.gpt.catalog.publication;
import com.esri.gpt.catalog.arcgis.metadata.AGSProcessor;
import com.esri.gpt.catalog.arcgis.metadata.AGSProcessorConfig;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.util.Val;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A factory for instantiating suitable resource processors.
 */
public class ProcessorFactory {
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public ProcessorFactory() {}

  /**
   * Interrogates a resource URL to determine an appropriate resource processor.
   * @param context the resource processing context
   * @param resourceUrl the resource URL to be interrogated
   * @return a processor capable of handling the endpoint (can be null)
   * @throws IOException if a communication exception occurs
   */
  public ResourceProcessor interrogate(ProcessingContext context, String resourceUrl)
    throws IOException {
    return interrogate(context, resourceUrl, null);
  }

  /**
   * Interrogates a resource URL to determine an appropriate resource processor.
   * @param context the resource processing context
   * @param resourceUrl the resource URL to be interrogated
   * @param credentials credentials
   * @return a processor capable of handling the endpoint (can be null)
   * @throws IOException if a communication exception occurs
   */
  public ResourceProcessor interrogate(ProcessingContext context, String resourceUrl, UsernamePasswordCredentials credentials)
    throws IOException {
    
    // it's xml or ags json or ags soap
    
    // determine the base URL (sans query string)
    resourceUrl = Val.chkStr(resourceUrl);
    String baseUrl = resourceUrl;
    if (baseUrl.indexOf("?") != -1) {
      baseUrl = baseUrl.substring(0,baseUrl.indexOf("?"));
    }
    if (baseUrl.endsWith("/")) {
      baseUrl = baseUrl.substring(0,baseUrl.length() - 1);
    }
    
    // determine the protocol
    boolean isHttpProtocol = false;
    boolean isFtpProtocol = false;
    boolean isFileProtocol = false;
    boolean isFile = false;
    boolean isDirectory = false;
    boolean wasMalformed = false;
    URL url = null;
    File file = null;
    try {
      url = new URL(resourceUrl);
      String protocol = Val.chkStr(url.getProtocol()).toLowerCase();
      isHttpProtocol = protocol.equals("http") || protocol.equals("https");
      isFtpProtocol = protocol.equals("ftp") || protocol.equals("ftps");
      isFileProtocol = protocol.equals("file");
    } catch (MalformedURLException e) {
      wasMalformed = true;
    }
    
    // check to see if this is a reference to a server side file or directory
    // TODO: should we allow this??
    if (isFileProtocol || wasMalformed) {
      String filePath = resourceUrl;
      if (isFileProtocol) {
        filePath = url.getPath();
      }
      try {
        file = new File(filePath);
        if (file.exists()) {
          isFile = file.isFile();
          isDirectory = file.isDirectory();
        }
      } catch (SecurityException se) {
        // read only
      }
    }
    
    if (isDirectory) {
      // no applicable processor
    } else if (isFile) {
      SingleXmlProcessor xp = new SingleXmlProcessor(context,file);
      return xp;
    } else if (isFtpProtocol) {
      // no applicable processor
    } else if (isHttpProtocol) {
      return this.interrogateHttpEndpoint(context,url,credentials);
    } else {
      // no applicable processor
    }
    
    return null;
  }
  
  /**
   * Interrogates an HTTP endpoint.
   * @param context the resource processing context
   * @param url the URL to be interrogated
   * @param credentials credentials
   * @return a processor capable of handling the endpoint (can be null)
   * @throws IOException if a communication exception occurs
   */
  private ResourceProcessor interrogateHttpEndpoint(ProcessingContext context, URL url, UsernamePasswordCredentials credentials)
    throws IOException {
    
    // read the character response from the endpoint
    String fullUrl = url.toExternalForm();
    HttpClientRequest httpClient = context.getHttpClient();
    httpClient.setUrl(fullUrl);
    httpClient.setCredentialProvider(
      credentials==null || credentials.getUsername().length()==0 || credentials.getPassword().length()==0?
        null:
        new CredentialProvider(credentials.getUsername(),credentials.getPassword())
    );
    String response = Val.chkStr(httpClient.readResponseAsCharacters());
    
    if (response.length() > 0) {
      
      // first try ArcGIS Server
      boolean interrofgateAGS = true;
      StringAttributeMap params = context.getRequestContext().getCatalogConfiguration().getParameters();
      if (Val.chkStr(params.getValue("AGSProcessor.interrogation.enabled")).equalsIgnoreCase("false")) {
        interrofgateAGS = false;
      } else if (!AGSProcessorConfig.isAvailable()) {
        interrofgateAGS = false;
      }
      if (interrofgateAGS) {
        AGSProcessor ags = new AGSProcessor(context);
        ags.setCredentials(credentials);
        try {
          if (ags.interrogate(url,response)) {
            return ags;
          }
        } catch (IOException ioe) {
          if (ags.getTarget().getWasRecognized()) {
            throw ioe;
          }
        }
      }

    } 
    
    // default to s single xml processor
    SingleXmlProcessor xp = new SingleXmlProcessor(context,url.toExternalForm(),response);
    return xp;
  }
  
}
