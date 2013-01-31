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
package com.esri.gpt.sdisuite;
import com.esri.gpt.catalog.search.ResourceLink;
import com.esri.gpt.catalog.search.ResourceLinkBuilder;
import com.esri.gpt.catalog.search.SearchResultRecord;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.search.SearchXslRecord;
import com.esri.gpt.framework.util.Val;

import javax.servlet.http.HttpServletRequest;

/**
 * Resource link builder supporting sde.suite integration.
 */
public class IntegrationLinkBuilder extends ResourceLinkBuilder {
  
  /** instance variables ====================================================== */
  private boolean useLinkCallbackServlet = false;
  
  /** constructors ============================================================ */

  /** Default constructor. */
  public IntegrationLinkBuilder() {}
  
  /** methods ================================================================= */
  
  @Override
  public void build(SearchXslRecord xRecord, SearchResultRecord record) {
    super.build(xRecord,record);
    
    // re-point resource links to the callback servlet
    if (this.useLinkCallbackServlet) {
      String callback = this.getBaseContextPath()+"/link";
      for (ResourceLink link: record.getResourceLinks()) {
        String url = link.getUrl();
        if ((url != null) && (url.length() > 0)) {
          if (!url.toLowerCase().startsWith("javascript:")) {
            String newUrl = callback+"?act="+this.encodeUrlParam(link.getTag());
            newUrl += "&fwd="+this.encodeUrlParam(link.getUrl());
            link.setUrl(newUrl);
          }
        }
      }
    }
    
  }

  @Override
  protected void determineResourceUrl(SearchXslRecord xRecord, SearchResultRecord record) {
    super.determineResourceUrl(xRecord,record);
  }
  
  @Override
  public void initialize(HttpServletRequest request, 
                         RequestContext context,
                         MessageBroker messageBroker) {
    super.initialize(request,context,messageBroker);
    
    // determine if the callback servlet should be active
    IntegrationContextFactory icf = new IntegrationContextFactory();
    if (icf.isIntegrationEnabled()) {
      this.useLinkCallbackServlet = Val.chkBool(icf.getConfiguration().getValue("sdisuite.useLinkCallbackServlet"),false);
    }
  }

}
