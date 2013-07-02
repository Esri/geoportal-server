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
package gc.solr.proxy;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Filter implementation class HttpRequestFilter
 */
public class SolrHttpRequestFilter implements Filter  {

	/** The logger.*/
  private static final Logger LOGGER = Logger.getLogger(SolrHttpRequestFilter.class.getName());
  private String publicOperations;
  private String securedOperations;
  
    /**
     * Default constructor. 
     */
    public SolrHttpRequestFilter() {
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}
	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String requestUri = chkStr(httpRequest.getRequestURI());
		if(requestUri.length() > 0){			
			String[] parts = requestUri.split("/");
			if(parts != null && parts.length > 0){
				String operation = parts[parts.length - 1];		
				StringBuilder subOperation = new StringBuilder();
				subOperation.append(parts[parts.length - 2]).append("/").append(parts[parts.length - 1]);
				if(operation != null && operation.length() > 0 && subOperation.toString().length() > 0){
					if((this.publicOperations.contains(operation) &&  !this.securedOperations.contains(operation)) || (this.publicOperations.contains(subOperation.toString())
							&& !this.securedOperations.contains(subOperation.toString()))){
						chain.doFilter(request, response);
					}else if(this.securedOperations.contains(operation) || this.securedOperations.contains(subOperation.toString())){						
						httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
					}else{
						httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
					}
				}else{
					httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
			}
		}else{
			httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
		}		
	}

	/**
	 * Gets the logger.
	 * @return the logger
	 */
	protected Logger getLogger() {
	  return LOGGER;
	}
	/**
	 * Check a string value.
	 * @param s the string to check
	 * @return the checked string (trimmed, zero length if the supplied String was null)
	 */
	private String chkStr(String s) {
	  if (s == null) {
	    return "";
	  } else {
	    return s.trim();
	  }
	}
	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		if(fConfig != null){
			this.publicOperations = fConfig.getInitParameter("publicOperations");
			if(this.publicOperations == null){
				this.publicOperations = "select,query,browse,update/extract,analysis,admin/luke,admin/ping,spell,tvrh,terms,elevate";
			}
			this.securedOperations = fConfig.getInitParameter("securedOperations");
			if(this.securedOperations == null){
				this.securedOperations = "update,admin";
			}
		}
		
	  
	}

}
