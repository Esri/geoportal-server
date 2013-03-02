/*
 * Copyright 2011 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.catalog.harvest.protocols;

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.client.thredds.TQueryBuilder;
import com.esri.gpt.framework.resource.query.QueryBuilder;

/**
 * Harvest protocol for THREDDS.
 */
public class HarvestProtocolThredds extends AbstractHTTPHarvestProtocol {

  @Override
  public ProtocolType getType() {
    return null;
  }

  @Override
  public String getKind() {
    return "THREDDS";
  }

  public QueryBuilder newQueryBuilder(IterationContext context, String url) {
    return new TQueryBuilder(context, this, url);
  }
  
}
