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

package com.esri.gpt.framework.util;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.esri.gpt.framework.collection.StringAttributeMap;

/**
 * HTTP request utility class.
 */
public class HttpRequestUtil {
	
	// class variables =============================================================
	  
	// instance variables ==========================================================
	
	// constructors ================================================================

	/** Default constructor. */
	
	// properties ==================================================================
	
	// method ==================================================================

	/**
	 * Responsible for collecting all http query parameter and transform it to a string attibute map.
	 * 
	 * @param request
	 * @return StringAttributeMap
	 */
	public static StringAttributeMap collectQuery(final HttpServletRequest request) {
		StringAttributeMap params = new StringAttributeMap();
		for (Enumeration<String> paramnames = request.getParameterNames(); paramnames.hasMoreElements();) {
			String name = (String) paramnames.nextElement();
			String val = Val.chkStr(request.getParameter(name));
			params.set(name,val);
		}
		return params;
	}

	/**
	 * Responsible for collecting all http headers and transform it to a string array (odd==keys, even=values).
	 * 
	 * @param request
	 * @return StringAttributeMap
	 */
	public static StringAttributeMap collectHeader(final HttpServletRequest request) {
		StringAttributeMap headers = new StringAttributeMap();
		for (Enumeration<String> headernames = request.getHeaderNames(); headernames.hasMoreElements();) {
			String name = (String) headernames.nextElement();
			String val = Val.chkStr(request.getHeader(name));
			headers.set(name,val);
		}
		return headers;
	}
}
