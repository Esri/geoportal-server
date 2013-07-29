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

package com.esri.gpt.server.usage.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.esri.gpt.control.rest.writer.ResponseWriter;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;

/**
 * Statistics request context.
 */
public class StatisticsRequestContext {
	private StringAttributeMap statQueryParams;
	private StringAttributeMap statHeaderParams;
	private HttpServletResponse response;
	private HttpServletRequest request;
	private RequestContext requestContext;
	private ResponseWriter writer;
	private String requestType = "site";
	private String[] restUriParts = null;
	private StringBuilder responseString = new StringBuilder();

    /**
     * Gets REST URI parts.
     * @return REST URI parts
     */
	public String[] getRestUriParts() {
		return restUriParts;
	}

    /**
     * Sets REST URI parts.
     * @param restUriParts REST URI parts
     */
	public void setRestUriParts(String[] restUriParts) {
		this.restUriParts = restUriParts;
	}

    /**
     * Gets request type.
     * @return request type
     */
	public String getRequestType() {
		return requestType;
	}

    /**
     * Sets request type.
     * @param requestType request type 
     */
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

    /**
     * Gets writer.
     * @return writer
     */
	public ResponseWriter getWriter() {
		return writer;
	}

    /**
     * Sets writer.
     * @param writer writer
     */
	public void setWriter(ResponseWriter writer) {
		this.writer = writer;
	}

    /**
     * Gets statistics header parameters.
     * @return statistics header parameters
     */
	public StringAttributeMap getStatHeaderParams() {
		return statHeaderParams;
	}

    /**
     * Sets statistics header parameters.
     * @param statHeaderParams statistics header parameters
     */
	public void setStatHeaderParams(StringAttributeMap statHeaderParams) {
		this.statHeaderParams = statHeaderParams;
	}

    /**
     * Gets statistics query parameters.
     * @return statistics query parameters
     */
	public StringAttributeMap getStatQueryParams() {
		return statQueryParams;
	}

    /**
     * Sets statistics query parameters.
     * @param statQueryParams statistics query parameters
     */
	public void setStatQueryParams(StringAttributeMap statQueryParams) {
		this.statQueryParams = statQueryParams;
	}

    /**
     * Gets HTTP response.
     * @return HTTP response
     */
	public HttpServletResponse getResponse() {
		return response;
	}

    /**
     * Sets HTTP response.
     * @param response HTTP response
     */
	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

    /**
     * Gets HTTP request.
     * @return HTTP request
     */
	public HttpServletRequest getRequest() {
		return request;
	}

    /**
     * Sets HTTP request.
     * @param request HTTP request
     */
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

    /**
     * Gets request context.
     * @return request context
     */
	public RequestContext getRequestContext() {
		return requestContext;
	}

    /**
     * Sets request context.
     * @param context request context
     */
	public void setRequestContext(RequestContext context) {
		this.requestContext = context;
	}

    /**
     * Gets response string.
     * @return response string
     */
	public StringBuilder getResponseString() {
		return responseString;
	}

    /**
     * Sets response string.
     * @param responseString response string 
     */
	public void setResponseString(StringBuilder responseString) {
		this.responseString = responseString;
	}

}
