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
package com.esri.gpt.control.filter;
import com.esri.gpt.framework.util.LogUtil;
import java.io.IOException;
import java.util.logging.Level;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

/**
 * Filter to handle multipart requests.
 * <p>
 * If the request is multipart, a MultipartWrapper is
 * instantiated to wrap the HTTP servlet request.
 */
public class MultipartFilter implements Filter {

// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public MultipartFilter() {}

// properties ==================================================================

// methods =====================================================================

/**
 * Destroy event for the filter.
 */
public void destroy() {}

/**
 * Executes the filter.
 * @param request the servlet request
 * @param response the servlet response
 * @param chain the filter chain
 * @throws IOException if an io exception occurs
 * @throws ServletException is a servlet exception occurs
 */
public void doFilter(ServletRequest request,
                     ServletResponse response,
                     FilterChain chain)
  throws IOException, ServletException {
  ServletRequest chainRequest = request;
  try {
    if (MultipartWrapper.isMultipartContent(request)) {
      HttpServletRequest httpReq = (HttpServletRequest)request;
      MultipartWrapper wrapper = new MultipartWrapper(httpReq);
      chainRequest = wrapper;
    }
  } catch (Throwable t) {
    if (request != null) {
      request.setAttribute("MultipartFilterException",t);
    }
    LogUtil.getLogger().log(Level.SEVERE,"MultipartFilterException",t);
  } 
  chain.doFilter(chainRequest,response);
}

/**
 * Initialization event for the filter
 * @param filterConfig filter configuration
 * @throws ServletException if an exception occurs
 */
public void init(FilterConfig filterConfig) throws ServletException {}

}



