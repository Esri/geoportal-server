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
package com.esri.gpt.control.webharvest.client.agportal;

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolAgp;
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
 * ArcGIS Portal query builder.
 * NOTE! This is EXPERIMENTAL feature. It might be removed at any time in the future.
 */
public class AgpQueryBuilder implements QueryBuilder {

  /** logger */
  private static final Logger LOGGER = Logger.getLogger(AgpQuery.class.getCanonicalName());
  /** capabilities */
  private static final Capabilities capabilities = new AgpCommonCapabilities();
  /** iteration context */
  private IterationContext context;
  /** service info */
  private AgpInfo info;

  /**
   * Creates instance of the builder.
   * @param context iteration context
   * @param protocol harvest protocol
   * @param url url
   */
  public AgpQueryBuilder(IterationContext context, HarvestProtocolAgp protocol, String url) {
    if (context == null) {
      throw new IllegalArgumentException("No context provided.");
    }
    this.context = context;
    this.info = new AgpInfo(url, protocol.getUserName(), protocol.getUserPassword());
  }

  public Capabilities getCapabilities() {
    return capabilities;
  }

  public Query newQuery(Criteria crt) {
    Query q = new AgpQuery(context, info, crt);
    LOGGER.log(Level.FINER, "Query created: {0}", q);
    return q;
  }

  public Native getNativeResource() {
    return null;
  }

  /**
   * ArcGIS Portal capabilities.
   */
  private static class AgpCommonCapabilities extends CommonCapabilities {

    @Override
    public boolean canQueryMaxRecords() {
      return true;
    }
  }
}
