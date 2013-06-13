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

import org.json.JSONObject;

import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.util.Val;

/**
 * This class is used to collect hit count for Portal for ArcGIS 
 * harvest request.
 *
 */
public class AGPHitCountCollector implements IHitCountCollector {

	/* (non-Javadoc)
	 * @see com.esri.gpt.control.webharvest.client.atom.IHitCountCollector#collectHitCount(java.lang.Object)
	 */
	/**
	 * Makes http request to json endpoint to read the hit count
	 * for the rss query url.
	 * @param source the url
	 * @return hit count
	 */
	@Override
	public int collectHitCount(Object source) throws Exception {
		String url = Val.chkStr((String) source);
		if(url.length() > 0){
			url= url;
			if(url.toLowerCase().contains("f=rss")){
				String[] parts = url.split("f=");
				  if(parts != null && parts.length >= 2){			  
					  String fParam = Val.chkStr(parts[1]);
					  int idx = fParam.indexOf("&");
					  fParam = fParam.substring(0, idx);
					  String oldFParam = "f="+fParam;
					  String jsonFParam = "f=json";
					  url = url.replace(oldFParam, jsonFParam);
				  }		
			}
			HttpClientRequest cr = new HttpClientRequest();	
		    cr.setUrl(url);
		    String response = Val.chkStr(cr.readResponseAsCharacters());
		    if(response.length() > 0){
		    	JSONObject jso = new JSONObject(response);
		        String total = jso.has("total") ? jso.getString("total") : "-1";
		        return Integer.parseInt(total); 
		    }
		}
		return -1;
	}

}
