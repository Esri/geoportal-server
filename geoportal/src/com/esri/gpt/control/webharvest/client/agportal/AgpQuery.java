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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocol;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.common.CommonResult;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.Result;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ArcGIS Portal query.
 * NOTE! This is EXPERIMENTAL feature. It might be removed at any time in the future.
 */
public class AgpQuery implements Query {
  /** logger */
private static final Logger LOGGER = Logger.getLogger(AgpQuery.class.getCanonicalName());
/** iteration context */
private IterationContext context;
/** service info */
private AgpInfo info;
/** query criteria */
private Criteria criteria;


/**
 * Creates instance of the query.
 * @param context iteration context
 * @param info service info
 * @param criteria query criteria
 */
public AgpQuery(IterationContext context, AgpInfo info, Criteria criteria) {
  if (context==null) throw new IllegalArgumentException("No context provided.");
  if (info==null) throw new IllegalArgumentException("No info provided.");
  this.context = context;
  this.info = info;
  this.criteria = criteria;
}

public Result execute()  {
  LOGGER.log(Level.FINER, "Executing query: {0}", this);
  Result r = new CommonResult(new AgpFolders(context, info, criteria));
  LOGGER.log(Level.FINER, "Completed query execution: {0}", this);
  return r;
}

@Override
public String toString() {
  return "{protocol: " + HarvestProtocol.ProtocolType.AGP + ", criteria:" + criteria + "}";
}

}
