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
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;

import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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
      
  private String osddLocation;
  
  /** methods ================================================================= */
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    
    osddLocation = config.getInitParameter("osddLocation");
  }

  /**
   * Executes a request.
   * @param request the servlet request
   * @param response the servlet response
   * @param context the request context
   * @throws Exception if an exception occurs
   */
  protected void execute(HttpServletRequest request, HttpServletResponse response, RequestContext context) throws Exception {
    // process the request
    LOGGER.finer("Returning openSearchDescription XML ....");
    OpenSearchDescriptionProvider provider = new OpenSearchDescriptionProvider(osddLocation);
    String xml = provider.readXml(request,context);
    String contentType = "application/opensearchdescription+xml; charset=UTF-8";
    LOGGER.finer("openSearchDescription.xml:\n"+xml);
    writeCharacterResponse(response,xml,"UTF-8",contentType);
  }

}
