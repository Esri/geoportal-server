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
package com.esri.gpt.control.webharvest.client.oai;

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolOai;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.common.CommonCapabilities;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.query.Capabilities;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import java.util.logging.Logger;

/**
 * OAI query builder.
 */
public class OaiQueryBuilder implements QueryBuilder {

/** logger */
private static final Logger LOGGER = Logger.getLogger(OaiQuery.class.getCanonicalName());
/** capabilities */
private static final Capabilities capabilities = new OaiCommonCapabilities();
/** iteration context */
private IterationContext context;
/** service info */
private OaiInfo info;

/**
 * Creates instance of the builder.
 * @param context iteration context
 * @param protocol harvest protocol
 * @param url url
 */
public OaiQueryBuilder(IterationContext context, HarvestProtocolOai protocol, String url) {
  if (context == null)
    throw new IllegalArgumentException("No context provided.");
  this.context = context;
  this.info = new OaiInfo(url, protocol.getPrefix(), protocol.getSet(), protocol.getUserName(), protocol.getUserPassword());
}

public Capabilities getCapabilities() {
  return capabilities;
}

public Query newQuery(Criteria crt) {
  OaiProxy proxy = new OaiProxy(info);
  Query q = new OaiQuery(context, info, proxy, crt);
  LOGGER.finer("Query created: " + q);
  return q;
}

public Native getNativeResource() {
  return null;
}

/**
 * OAI capabilities.
 */
private static class OaiCommonCapabilities extends CommonCapabilities {

@Override
public boolean canQueryFromDate() {
  return true;
}

@Override
public boolean canQueryToDate() {
  return true;
}

@Override
public boolean canQueryMaxRecords() {
  return true;
}
}
}
