/*
 * See the NOTICE file distributed with
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

import com.esri.gpt.catalog.harvest.clients.HRAgpClient;
import com.esri.gpt.catalog.harvest.clients.HRClient;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.client.agportal.AgpQueryBuilder;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.framework.util.Val;

/**
 * ArcGIS Portal protocol.
 * NOTE! This is EXPERIMENTAL feature. It might be removed at any time in the future.
 */
public class HarvestProtocolAgp extends AbstractHTTPHarvestProtocol {

// class variables =============================================================

// instance variables ==========================================================
  
// constructors ================================================================

// properties ==================================================================

/**
 * Gets protocol type.
 * @return protocol type
 */
public final ProtocolType getType() {
  return ProtocolType.AGP;
}

// methods =====================================================================

/**
 * Gets harvest client.
 * @return harvest client
 */
public HRClient getClient(final String hostUrl) {
  return new HRAgpClient(normalizeUrl(hostUrl));
}

public QueryBuilder newQueryBuilder(IterationContext context, String url) {
  return new AgpQueryBuilder(context, this, url);
}

private String normalizeUrl(String url) {
  return Val.chkStr(url).replaceAll("/+$", "") + "/search?q=*:*&f=pjson&num=10&start=1";
}
}
