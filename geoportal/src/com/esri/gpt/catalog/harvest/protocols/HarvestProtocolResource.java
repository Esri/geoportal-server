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

import com.esri.gpt.catalog.harvest.clients.HRArcGisClient;
import com.esri.gpt.catalog.harvest.clients.HRClient;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidProtocolException;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.client.res.ResourceQueryBuilder;
import com.esri.gpt.framework.resource.query.QueryBuilder;

/**
 * RES harvest protocol.
 */
public class HarvestProtocolResource extends AbstractHTTPHarvestProtocol {

@Override
public ProtocolType getType() {
  return ProtocolType.RES;
}

@Override
public HRClient getClient(String hostUrl) throws HRInvalidProtocolException {
  return new HRArcGisClient(hostUrl);
}

public QueryBuilder newQueryBuilder(IterationContext context, String url) {
  return new ResourceQueryBuilder(context, this, url);
}

}
