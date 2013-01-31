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

	public String[] getRestUriParts() {
		return restUriParts;
	}

	public void setRestUriParts(String[] restUriParts) {
		this.restUriParts = restUriParts;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public ResponseWriter getWriter() {
		return writer;
	}

	public void setWriter(ResponseWriter writer) {
		this.writer = writer;
	}

	public StringAttributeMap getStatHeaderParams() {
		return statHeaderParams;
	}

	public void setStatHeaderParams(StringAttributeMap statHeaderParams) {
		this.statHeaderParams = statHeaderParams;
	}

	public StringAttributeMap getStatQueryParams() {
		return statQueryParams;
	}

	public void setStatQueryParams(StringAttributeMap statQueryParams) {
		this.statQueryParams = statQueryParams;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public RequestContext getRequestContext() {
		return requestContext;
	}

	public void setRequestContext(RequestContext context) {
		this.requestContext = context;
	}

	public StringBuilder getResponseString() {
		return responseString;
	}

	public void setResponseString(StringBuilder responseString) {
		this.responseString = responseString;
	}

}
