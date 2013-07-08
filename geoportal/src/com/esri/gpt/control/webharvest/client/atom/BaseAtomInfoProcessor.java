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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolAtom;
import com.esri.gpt.framework.util.Val;

abstract class BaseAtomInfoProcessor implements IAtomInfoProcessor {

	private BaseAtomInfo atomInfo;
	private String atomInfoClassName = "";
		
	public String getAtomInfoClassName() {
		return atomInfoClassName;
	}

	public void setAtomInfoClassName(String atomInfoClassName) {
		this.atomInfoClassName = atomInfoClassName;
	}

	public BaseAtomInfo getAtomInfo() {
		return atomInfo;
	}

	/**
	 * Initializes base atom info object with protocol and url
	 * @param protocol protocol
	 * @param url URL
	 * @return the info object
	 */
	@Override
	public BaseAtomInfo initializeAtomInfo(HarvestProtocolAtom protocol,String url) {
		 String[] parts = Val.chkStr(url).split("atomInfoClassName=");
			if(atomInfoClassName.length() == 0){
				if (parts != null && parts.length >= 2) {
					atomInfoClassName = Val.chkStr(parts[1]);
					int idx = atomInfoClassName.indexOf("&");
					if (idx == -1) {
						atomInfoClassName = atomInfoClassName.substring(0);
					} else {
						atomInfoClassName = atomInfoClassName.substring(0, idx);
					}
				}
				if (atomInfoClassName.length() == 0) {
					atomInfoClassName = "com.esri.gpt.control.webharvest.client.atom.BaseAtomInfo";
				}
			}
			Class<?> clsAdapter = null;
			try {
				clsAdapter = Class.forName(atomInfoClassName);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Object atomInfoObj = null;
			try {
				atomInfoObj = clsAdapter.newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (atomInfoObj instanceof BaseAtomInfo) {
				this.atomInfo = (BaseAtomInfo) atomInfoObj;
				this.atomInfo.initialize(url, protocol.getUserName(), protocol.getUserPassword());							
			}
		return this.atomInfo;
	}
	
	/**
	 * Initializes hot count collector
	 */
	public void initializeHitCountCollector(){		
	  String hitCountCollectorClassName = Val.chkStr(this.atomInfo.getHitCountCollectorClassName());
	  if(hitCountCollectorClassName.length() == 0 ){			
		  String[] parts = this.atomInfo.getUrl().split("hitCountCollectorClassName=");
		  if(parts != null && parts.length >= 2){			  
			  hitCountCollectorClassName = Val.chkStr(parts[1]);
			  int idx = hitCountCollectorClassName.indexOf("&");
			  if(idx == -1){
				  hitCountCollectorClassName = hitCountCollectorClassName.substring(0);
			  }else{
				  hitCountCollectorClassName = hitCountCollectorClassName.substring(0, idx);
			  }
		  }			  
	  }
	  if(hitCountCollectorClassName.length() == 0){
		  hitCountCollectorClassName= "com.esri.gpt.control.webharvest.client.atom.OpenSearchHitCountCollector";
	  }
	  Class<?> clsAdapter;
	  try {
		clsAdapter = Class.forName(hitCountCollectorClassName);	 
	    Object objHitCountCollector = clsAdapter.newInstance();
	    if (objHitCountCollector instanceof IHitCountCollector) {
	    	this.atomInfo.setHitCountCollector((IHitCountCollector) objHitCountCollector);
	    }
	  } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
	}

}
