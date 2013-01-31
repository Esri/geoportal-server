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

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Creates a zip file of XML documents associated with a set of keys.
 */
public class ZipXmls extends KeysetProcessor {
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public ZipXmls() {}
  
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
    if (keys.length > 0) {
      String sZipFile = "xmls.zip";
      response.setContentType("application/zip");  
      response.addHeader("Content-Disposition","inline; filename="+sZipFile);
      ServletOutputStream out = response.getOutputStream(); 
      ZipOutputStream zip = new ZipOutputStream(out); 
      try {     
        for (String sKey: keys) {
          String sFile = sKey+".xml";
          String sXml = this.readXml(request,context,sKey);
          byte[] bytes = sXml.getBytes("UTF-8");
          zip.putNextEntry(new ZipEntry(sFile)); 
          zip.write(bytes);
          zip.flush();
        }
      } finally {
        zip.close();
        out.flush();
        out.close();
      }
    }
  }

}
