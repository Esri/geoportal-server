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
package com.esri.gpt.control.webharvest.client.arcims;

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolArcIms;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.common.CommonCapabilities;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.query.Capabilities;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ArcIMS query builder.
 */
public class ArcImsQueryBuilder implements QueryBuilder {

/** logger */
private static final Logger LOGGER = Logger.getLogger(ArcImsQueryBuilder.class.getCanonicalName());
/** capabilities */
private static final Capabilities capabilities = new ArcImsCapabilities();
/** iteration context */
private IterationContext context;
/** service proxy */
private ArcImsProxy proxy;
/** service info */
private ArcImsInfo info;

/**
 * Creates instance of the builder.
 * @param context iteration context
 * @param protocol harvest protocol
 * @param url url
 */
public ArcImsQueryBuilder(IterationContext context, HarvestProtocolArcIms protocol, String url) {
  if (context==null) {
    throw new IllegalArgumentException("No context provided.");
  }
  if (!(protocol instanceof HarvestProtocolArcIms)) {
    throw new IllegalArgumentException("invalid protocol.");
  }
  this.context = context;
  this.info = new ArcImsInfo(url, protocol.getServiceName(), protocol.getRootFolder(), protocol.getUserName(), protocol.getUserPassword());
  this.proxy = new ArcImsProxy(info);
}

@Override
public Capabilities getCapabilities() {
  return capabilities;
}

@Override
public Query newQuery(Criteria crt) {
  Query q = new ArcImsQuery(context, info, proxy, crt);
  LOGGER.log(Level.FINER, "Query created: {0}", q);
  return q;
}

@Override
public Native getNativeResource() {
  return null;
}

/**
 * ArcIMS client capabilities.
 */
private static class ArcImsCapabilities extends CommonCapabilities {

@Override
public boolean canQueryBBox() {
  return true;
}

@Override
public boolean canQueryBBoxOption() {
  return true;
}

@Override
public boolean canQueryFromDate() {
  return true;
}

@Override
public boolean canQueryMaxRecords() {
  return true;
}

@Override
public boolean canQuerySearchText() {
  return true;
}

@Override
public boolean canQueryToDate() {
  return true;
}
}
}
