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

import com.esri.gpt.catalog.harvest.clients.HRClient;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidProtocolException;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.resource.query.QueryBuilder;

/**
 * None protocol.
 */
public class HarvestProtocolNone extends HarvestProtocol {

// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

// properties ==================================================================
/**
 * Gets protocol type.
 * @return protocol type
 */
public final ProtocolType getType() {
  return ProtocolType.None;
}

// methods =====================================================================
/**
 * Gets all the attributes.
 * @return attributes as attribute map (always empty)
 */
protected StringAttributeMap extractAttributeMap() {
  return new StringAttributeMap();
}

/**
 * Gets all the attributes.
 * @return attributes as attribute map (always empty)
 */
public StringAttributeMap getAttributeMap() {
  return new StringAttributeMap();
}

/**
 * Sets all the attributes.
 * @param attributeMap attributes as attribute map (ignored)
 */
protected void applyAttributeMap(StringAttributeMap attributeMap) {
}

/**
 * Sets all the attributes.
 * @param attributeMap attributes as attribute map (ignored)
 */
public void setAttributeMap(StringAttributeMap attributeMap) {
}

/**
 * Gets harvest client.
 * @return <code>null</code> value
 */
@Override
public HRClient getClient(String hostUrl) 
  throws HRInvalidProtocolException {
  throw new HRInvalidProtocolException(
    HRInvalidProtocolException.ProtocolElement.protocol,
    "Unable to create harvest repository client for the protocol.");
}

public QueryBuilder newQueryBuilder(IterationContext context, String url) {
  return null;
}
}
