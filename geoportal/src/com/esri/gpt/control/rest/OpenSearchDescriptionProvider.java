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
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.mail.MailConfiguration;
import com.esri.gpt.framework.util.ResourcePath;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.XmlIoUtil;
import javax.servlet.http.HttpServletRequest;

/**
 * Open Search Description provider.
 */
public class OpenSearchDescriptionProvider {
  
  /** The location of the OpenSearch description XML file */
  private final String osddLocation;

  public OpenSearchDescriptionProvider(String osddLocation) {
    this.osddLocation = osddLocation;
  }
  
  
  /**
   * Reads the OpenSearch description XML.
   * @param request the servlet request
   * @param context the request context
   * @return the description XML string
   * @throws Exception if an exception occurs
   */
  public String readXml(HttpServletRequest request, RequestContext context) 
    throws Exception {
    
    // initialize values for substitution
    MailConfiguration mailCfg = context.getMailConfiguration();
    MessageBroker msgBroker = new MessageBroker();
    msgBroker.setBundleBaseName("gpt.resources.gpt");
    
    String basePath = RequestContext.resolveBaseContextPath(request);
    String restPath = basePath+"/rest/find/document";
    String imagePath = basePath+"/catalog/images";
    String shortName = Val.escapeXml(msgBroker.retrieveMessage("catalog.openSearch.shortName"));
    String description = Val.escapeXml(msgBroker.retrieveMessage("catalog.openSearch.description"));
    String contact = Val.escapeXml(mailCfg.getOutgoingFromAddress());
        
    // read the XML, substitute values
    ResourcePath rp = new ResourcePath();
    String xml = XmlIoUtil.readXml(rp.makeUrl(osddLocation).toExternalForm());
    xml = xml.replaceAll("\\{openSearch.basePath\\}",basePath);
    xml = xml.replaceAll("\\{openSearch.restPath\\}",restPath);
    xml = xml.replaceAll("\\{openSearch.imagePath\\}",imagePath);
    xml = xml.replaceAll("\\{openSearch.shortName\\}",shortName);
    xml = xml.replaceAll("\\{openSearch.description\\}",description);
    xml = xml.replaceAll("\\{openSearch.contact\\}",contact);
    
    String clientId = Val.chkStr(request.getParameter("clientId"));
    if (!clientId.isEmpty()) {
      clientId="&amp;clientId="+clientId;
    }
    xml = xml.replaceAll("\\{clientId\\}",clientId);
    
    return xml;
  }
}
