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
package com.esri.gpt.control.search.browse;

import com.esri.gpt.framework.jsf.FacesMap;

/**
 * Provides a Map interface for setting the rendered attribute of a
 * Faces component based upon a supplied browse toc key.
 * <p>Example:<br/>
 * rendered="#{PageContext.tocsByKey['browseCatalog']}"
 */
@SuppressWarnings("serial")
public class TocsByKey extends FacesMap<Boolean> {

	private TocCollection _tocCollection = null; 
 
	/**
	 * Constructs based upon a supplied tocs.
	 * @param tocs the tocs collections configured 
	 */
	public TocsByKey(TocCollection tocs) {
		_tocCollection = tocs;
	  if (_tocCollection == null) {
	  	_tocCollection = new TocCollection();
	  }
	}
    /**
     * Implements the "get" method for a Map to return toc configuration status
     * a tag.
     * <br/>The supplied tag should be a string.
     * @param key the key associated with the toc
     * @return true is toc is configured
     */
    @Override
    public Boolean get(Object key) {
    	 if ((key != null) && (key instanceof String)) {    		 
    		 return new Boolean(this._tocCollection.containsKey(key));
    	 }
    	 return Boolean.FALSE;
    }
 }
  
