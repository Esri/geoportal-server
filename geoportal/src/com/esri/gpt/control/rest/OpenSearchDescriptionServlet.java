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
package com.esri.gpt.control.rest;
import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.ResourcePath;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.XmlIoUtil;

import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Returns the OpenSearch description XML.
 * <p/>
 * The XML returned is based upon the file:
 * <br/>gpt/search/openSearchDescription.xml
 * <p/>
 * In addition, the following substitutions are made:
 * <br/>{openSearch.shortName} - property resource catalog.openSearch.shortName
 * <br/>{openSearch.description} - property resource catalog.openSearch.description
 * <br/>{openSearch.restPath} - rest API path for this web application 
 * (e.g. http://somehost:someport/GPT9/rest/find/document)
 * <br/>{openSearch.imagePath} - image folder path for this web application
 * (e.g. http://somehost:someport/GPT9/catalog/images)
 */
public class OpenSearchDescriptionServlet extends BaseServlet {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(OpenSearchDescriptionServlet.class.getName());
  
  /** The location of the OpenSearch description XML file */
  private static final String XML_LOCATION = "gpt/search/openSearchDescription.xml";
      
  /** methods ================================================================= */
  
  /**
   * Executes a request.
   * @param request the servlet request
   * @param response the servlet response
   * @param context the request context
   * @throws Exception if an exception occurs
   */
  protected void execute(HttpServletRequest request, 
                         HttpServletResponse response,
                         RequestContext context)
    throws Exception {
  
    // process the request
    LOGGER.finer("Returning openSearchDescription XML ....");
    String xml = readXml(request,context);
    String contentType = "application/opensearchdescription+xml; charset=UTF-8";
    LOGGER.finer("openSearchDescription.xml:\n"+xml);
    writeCharacterResponse(response,xml,"UTF-8",contentType);
  }
  
  /**
   * Reads the OpenSearch description XML.
   * @param request the servlet request
   * @param context the request context
   * @return the description XML string
   * @throws Exception if an exception occurs
   */
  private String readXml(HttpServletRequest request, RequestContext context) 
    throws Exception {
    
    // initialize values for substitution
    CatalogConfiguration catCfg = context.getCatalogConfiguration();
    MessageBroker msgBroker = new MessageBroker();
    msgBroker.setBundleBaseName("gpt.resources.gpt");
    
    String basePath = RequestContext.resolveBaseContextPath(request);
    String restPath = basePath+"/rest/find/document";
    String imagePath = basePath+"/catalog/images";
    String shortName = Val.escapeXml(msgBroker.retrieveMessage("catalog.openSearch.shortName"));
    String description = Val.escapeXml(msgBroker.retrieveMessage("catalog.openSearch.description"));
        
    // read the XML, substitute values
    ResourcePath rp = new ResourcePath();
    rp.makeUrl(XML_LOCATION);
    String xml = XmlIoUtil.readXml(rp.makeUrl(XML_LOCATION).toExternalForm());
    xml = xml.replaceAll("\\{openSearch.restPath\\}",restPath);
    xml = xml.replaceAll("\\{openSearch.imagePath\\}",imagePath);
    xml = xml.replaceAll("\\{openSearch.shortName\\}",shortName);
    xml = xml.replaceAll("\\{openSearch.description\\}",description);
    
    return xml;
  }

}
