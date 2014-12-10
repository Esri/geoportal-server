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
package com.esri.gpt.control.search.browse;
import com.esri.gpt.catalog.search.SearchFilterHarvestSites;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.util.Val;

import java.util.logging.Logger;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Serves tables of content supporting browse functionality.
 */
public class TocServlet extends BaseServlet {

  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(TocServlet.class.getName());
  
  /** methods ================================================================= */

  /**
   * Executes a request.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @throws Exception if a processing exception occurs
   */
  @Override
  protected void execute(HttpServletRequest request,
                         HttpServletResponse response, 
                         RequestContext context) 
    throws Exception {
            
    // initialize
    TocContext tocContext = null;
    String mimeType = "application/json";
    String sResponse = "";
      		
    try {		
      tocContext = new TocContext();
      tocContext.setRequestContext(context);
      
      // parse the input parameters
      String key = Val.chkStr(request.getParameter("key"));
      String uuid = Val.chkStr(request.getParameter("uuid"));
      String format = Val.chkStr(request.getParameter("f"));
      if (uuid.length() > 0) {
        tocContext.setSubjectResourceID(uuid);
      }
      if (format.length() > 0) {
        tocContext.setOutputFormat(format);
      }
      if (key.length() > 0) {
        
        // set the message broker
        FacesContextBroker fcb = new FacesContextBroker(request,response);
        tocContext.setMessageBroker(fcb.extractMessageBroker());
        
        RequestContext requestCtx = fcb.extractRequestContext();
        // determine the XML file path
        String relativePath = "";
        TocCollection tocs = requestCtx.getCatalogConfiguration().getConfiguredTocs();
        if(tocs!= null && tocs.containsKey(key)){
        	relativePath = tocs.get(key);
        } else{
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Toc not configured.");
        	LOGGER.finer("Toc not configured for key " + key);
        }
        
        // the search filter harvest sites selected site is set to local
        FacesContext fc = FacesContext.getCurrentInstance();
        if(fc != null){
	        ELContext elCtx = fc.getELContext();
	        if(elCtx != null){
		        ELResolver resolver = elCtx.getELResolver();
		        if(resolver != null){
		        	SearchFilterHarvestSites searchFilterHarvestSites = (SearchFilterHarvestSites) resolver.getValue(elCtx, null, "SearchFilterHarvestSites");
		        	if(searchFilterHarvestSites != null){
		        		searchFilterHarvestSites.setSelectedHarvestSiteId("local");
		        	}
		        }
	        }
        }
        
        // generate the tree
        TocTree tree = TocTree.build(tocContext,relativePath);
        sResponse = tree.generateResponse(tocContext);
        mimeType = tocContext.getMimeType();
      }
      
    } finally {
      try {if (tocContext != null) tocContext.getIndexAdapter().close();} catch (Exception ef) {}
    }
    
    // write the response
    LOGGER.finer("tocResponse:\n"+sResponse);
    if ((sResponse != null) && (sResponse.length() > 0)) {
      writeCharacterResponse(response,sResponse,"UTF-8",mimeType+"; charset=UTF-8");
    }
    
  }
		
}
