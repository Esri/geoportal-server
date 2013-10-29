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

/**
 * This class is used to create open search based atom info
 * processor.
 *
 */
public class OpenSearchAtomInfoProcessor extends BaseAtomInfoProcessor {

	@Override
	public void preInitialize() {
	//	setAtomInfoClassName(atomInfoClassName);		
	}
	
	/**
	 * Sets post create options.
	 * @param atomInfo base atom info
	 */
	@Override
	public void postCreate(BaseAtomInfo atomInfo) {
		atomInfo.setHitCountCollectorClassName("com.esri.gpt.control.webharvest.client.atom.OpenSearchHitCountCollector");
		initializeHitCountCollector();
		try {
			atomInfo.setTotalResults(atomInfo.getHitCountCollector().collectHitCount(atomInfo.getUrl()));			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	// add more logic if needed
	}
}
