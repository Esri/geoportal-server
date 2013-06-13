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
package com.esri.gpt.control.webharvest.client.atom;

import com.esri.gpt.framework.util.Val;

/**
 * Collects hit count from resource url.
 *
 */
public class URLHitCountCollector implements IHitCountCollector {

	/* (non-Javadoc)
	 * @see com.esri.gpt.control.webharvest.client.atom.IHitCountCollector#collectHitCount(java.lang.Object)
	 */
	/**
	 * Finds the hit count from url.
	 * @param source the url
	 * @return hit count
	 */
	@Override
	public int collectHitCount(Object source) throws Exception {
		String url = Val.chkStr((String) source);
		if(url.length() > 0){
			  String totalResults = "-1";
				String[] parts = url.split("totalResults=");
			  if(parts != null && parts.length >= 2){			  
			  	totalResults = Val.chkStr(parts[1]);
				  int idx = totalResults.indexOf("&");
				  if(idx == -1){
				  	totalResults = totalResults.substring(0);
				  }else{
				  	totalResults = totalResults.substring(0, idx);
				  }
			  }			  
	      return Integer.parseInt(totalResults); 
		}
		return -1;
	}

}
