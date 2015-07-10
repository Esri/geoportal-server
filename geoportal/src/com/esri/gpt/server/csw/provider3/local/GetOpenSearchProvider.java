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
package com.esri.gpt.server.csw.provider3.local;

import com.esri.gpt.control.rest.OpenSearchDescriptionProvider;
import com.esri.gpt.server.csw.components.IOperationProvider;
import com.esri.gpt.server.csw.components.OperationContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import org.w3c.dom.Node;

/**
 * Open Search Provider.
 */
public class GetOpenSearchProvider implements IOperationProvider {
    private final String osddLocation;

    public GetOpenSearchProvider(String osddLocation) {
      this.osddLocation = osddLocation;
    }

    protected void execute(OperationContext context, HttpServletRequest request) throws Exception {
        OpenSearchDescriptionProvider osProvider = new OpenSearchDescriptionProvider(osddLocation);
        String xml = osProvider.readXml(request, context.getRequestContext());
        context.getOperationResponse().setResponseXml(xml);  
    }
    
    @Override
    public void handleGet(OperationContext context, HttpServletRequest request) throws Exception {
        execute(context, request);
    }

    @Override
    public void handleXML(OperationContext context, Node root, XPath xpath) throws Exception {
        HttpServletRequest request = (HttpServletRequest) context.getRequestContext().getServletRequest();
        execute(context, request);
    }
    
}
