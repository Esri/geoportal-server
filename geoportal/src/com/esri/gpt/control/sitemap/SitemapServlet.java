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
package com.esri.gpt.control.sitemap;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Endpoint for handling sitemap related requests.
 */
public class SitemapServlet extends BaseServlet {
  
  /**
   * Processes the HTTP request.
   * @param request the HTTP request
   * @param response the HTTP response
   * @param context the request context
   * @throws Exception if an exception occurs
   */
  @Override
  protected void execute(HttpServletRequest request, 
                         HttpServletResponse response, 
                         RequestContext context)
    throws Exception {
    SitemapHandler handler = new SitemapHandler();
    handler.handle(request,response,context);
  }

}
