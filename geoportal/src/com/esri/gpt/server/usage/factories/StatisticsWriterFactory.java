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

package com.esri.gpt.server.usage.factories;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import com.esri.gpt.control.rest.writer.ResponseWriter;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.usage.api.StatisticsRequestContext;
import com.esri.gpt.server.usage.api.StatisticsTypes;
import com.esri.gpt.server.usage.common.JSONWriter;

/**
 * This class is used to instantiate statistics writer to write response.
 * 
 * @author prat5814
 * 
 */
public class StatisticsWriterFactory {

	/**
	 * Creates appropriate writer object and sets it to statistics request
	 * context class
	 * 
	 * @param statReqContext
	 *            the statistics request context
	 * @throws Exception
	 *             if exception occurs
	 */
	public void makeStatisticsWriter(StatisticsRequestContext statReqContext)
			throws Exception {
		ResponseWriter rsWriter;
		statReqContext.setResponseString(new StringBuilder());
		HttpServletResponse response = statReqContext.getResponse();
		PrintWriter writer = response.getWriter();
		StringAttributeMap attrMap = statReqContext.getStatQueryParams();
		String outputFormat = Val.chkStr(attrMap.getValue("f"));
		if (outputFormat.equalsIgnoreCase("json")
				&& statReqContext.getRequestType().equalsIgnoreCase(
						StatisticsTypes.HARVESTER.toString())) {
			response.setContentType("text/plain; charset=UTF-8");
			rsWriter = new JSONWriter(writer,
					statReqContext.getResponseString());
		} else if (outputFormat.equalsIgnoreCase("html")) {
			throw new Exception("Output format not supported.");
		} else if (outputFormat.equalsIgnoreCase("xml")) {
			throw new Exception("Output format not supported.");
		} else {
			rsWriter = new JSONWriter(writer,
					statReqContext.getResponseString());
		}
		statReqContext.setWriter(rsWriter);
	}
}
