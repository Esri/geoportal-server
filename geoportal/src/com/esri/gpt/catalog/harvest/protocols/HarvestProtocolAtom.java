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
package com.esri.gpt.catalog.harvest.protocols;

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.client.atom.AtomQueryBuilder;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.framework.util.Val;

/**
 * Atom protocol.
 */
@SuppressWarnings("serial")
public class HarvestProtocolAtom extends AbstractHTTPHarvestProtocol {

// class variables =============================================================
	/** Default profile */
 private static String DEFAULT_TYPE = "com.esri.gpt.control.webharvest.client.atom.OpenSearchAtomInfoProcessor";
  
// instance variables ==========================================================
  /** ATOM Type. */
 private String _atomType = DEFAULT_TYPE;

// constructors ================================================================
// properties ==================================================================
 
  /**
   * Gets protocol type.
   * @return protocol type
   * @deprecated
   */
  @Override
  @Deprecated
  public final ProtocolType getType() {
    return ProtocolType.ATOM;
  }

  @Override
  public String getKind() {
    return "ATOM";
  }

// methods =====================================================================
  /**
   * Gets type of atom.
   * @return type of atom.
   */
  public String getAtomType() {
		return _atomType;
	}

  /**
   * Sets type of atom
   * @param atomType type of atom
   */
	public void setAtomType(String atomType) {
		this._atomType = atomType;
	}
    
  /**
   * Gets all the attributes.
   * @return attributes as attribute map
   */
  @Override
  public StringAttributeMap getAttributeMap() {
    StringAttributeMap properties = new StringAttributeMap();
    properties.set("atomInfoProcessorClassName", getAtomType());
    return properties;
  }

	/**
   * Sets all the attributes.
   * @param attributeMap attributes as attribute map
   */
  @Override
  public void setAttributeMap(StringAttributeMap attributeMap) {
  	setAtomType(chckAttr(attributeMap.get("atomInfoProcessorClassName")));
  }

  @Override
  public QueryBuilder newQueryBuilder(IterationContext context, String url) {
    return new AtomQueryBuilder(context, this, url);
  }
}
