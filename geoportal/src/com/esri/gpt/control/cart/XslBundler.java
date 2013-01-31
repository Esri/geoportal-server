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
package com.esri.gpt.control.cart;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.XsltTemplate;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Bundles the XML documents associated with a set of keys into a single response
 * by applying an XSLT.
 */
public class XslBundler extends KeysetProcessor {
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public XslBundler() {}
  
  /** methods ================================================================= */
    
  /**
   * Processes the HTTP request.
   * @param request the HTTP request
   * @param response HTTP response
   * @param context request context
   * @throws Exception if an exception occurs
   */
  @Override
  public void execute(HttpServletRequest request,
                      HttpServletResponse response, 
                      RequestContext context) 
    throws Exception {
    String[] keys = this.readKeys(request,context,true);
    String sXsltPath = Val.chkStr(request.getParameter("xslt"));
    String sMimeType = Val.chkStr(request.getParameter("mimeType"));
    String sContentDisposition = Val.chkStr(request.getParameter("contentDisposition"));
    if ((keys.length > 0) && (sXsltPath.length() > 0)) {
      XsltTemplate template = this.getCompiledTemplate(sXsltPath);
      ServletOutputStream out = response.getOutputStream(); 
            
      if (sMimeType.length() == 0) {
        sMimeType = "text/plain";
      }
      response.setContentType(sMimeType+";charset=UTF-8"); 
      if (sContentDisposition.length() > 0) {
        response.addHeader("Content-Disposition",sContentDisposition);
      }
      
      try {   
        for (String sKey: keys) {
          String sXml = this.readXml(request,context,sKey);
          String sResult = Val.chkStr(template.transform(sXml));
          if (sResult.length() > 0) {
            byte[] bytes = sResult.getBytes("UTF-8");
            out.write(bytes);
            out.flush();
          }
        }
      } finally {
        out.flush();
        out.close();
      }
    }
  }

}
