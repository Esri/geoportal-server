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
package com.esri.gpt.catalog.search;

import java.util.Map;
import java.util.TreeMap;

import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletRequest;

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.util.Val;

/**
 * The Class MapViewerConfigs.
 */
public class MapViewerConfigs {

// instance variables ==========================================================
/** The class name. */
private String className;

/** The url. */
private String url;

/** The attributes. */
private Map<String, String> parameters;

// properties ==================================================================
/**
 * Gets the class name.
 * 
 * @return the class name (trimmed, never null)
 */
public String getClassName() {
  return Val.chkStr(className);
}

/**
 * Sets the class name.
 * 
 * @param className the new class name
 */
public void setClassName(String className) {
  this.className = className;
}

/**
 * Gets the url.
 * 
 * @return the url (trimmed, never null)
 */
public String getUrl() {
	url = Val.chkStr(url);
	if(url != null && url.contains("{contextPath}") == true) {
		  
		  FacesContextBroker broker = new FacesContextBroker();
		  if(broker != null){
			  ExternalContext ec = broker.getExternalContext();
			  if(ec != null){
				  url = url.replace("{contextPath}",
						  ec.getRequestContextPath());
			  }
		  }
		  
	}	
  return url;
}

/**
 * Sets the url.
 * 
 * @param url the new url
 */
public void setUrl(String url) {
  
  this.url = url;
}

/**
 * Gets the attributes.
 * 
 * @return the attributes (never null)
 */
public Map<String, String> getParameters() {
  if(this.parameters == null) {
    this.parameters = new TreeMap<String, String>(
        String.CASE_INSENSITIVE_ORDER);
  }

  return this.parameters;
}

/**
 * Sets the attributes.
 * 
 * @param parameters the parameters
 */
public void setParameters(Map<String, String> parameters) {
  this.parameters = parameters;
}

// methods =====================================================================
@Override
public String toString() {
  Map<String,String> params = this.getParameters();
  String representation =  
    "[url= " + this.getUrl() + ", className = " + this.getClassName() + "]";
  if(params == null) {
    return representation;
  }
  
  
  return representation;
}

/**
 * Adds the parameter.
 * 
 * @param key the key
 * @param value the value
 */
public void addParameter(String key, String value) {
  this.getParameters().put(key, value);
}

}
