/*
 * Copyright 2013 Esri.
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
package com.esri.gpt.control.webharvest.client.dcat;

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.Result;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DCAT query.
 */
class DCATQuery implements Query {

  /** logger */
  private static final Logger LOGGER = Logger.getLogger(DCATQuery.class.getCanonicalName());
  /** iteration context */
  private IterationContext context;
  /** service info */
  private DCATInfo info;
  /** proxy */
  private DCATProxy proxy;
  /** query criteria */
  private Criteria criteria;

  public DCATQuery(IterationContext context, DCATInfo info, DCATProxy proxy, Criteria criteria) {
    if (context == null) {
      throw new IllegalArgumentException("No context provided.");
    }
    if (info == null) {
      throw new IllegalArgumentException("No info provided.");
    }
    if (proxy == null)
      throw new IllegalArgumentException("No proxy provided.");
    this.context = context;
    this.info = info;
    this.proxy = proxy;
    this.criteria = criteria;
  }

  @Override
  public Result execute() {
    LOGGER.log(Level.FINER, "Executing query: {0}", this);
    LOGGER.log(Level.FINER, "Completed query execution: {0}", this);
    return null;
  }

  @Override
  public String toString() {
    return "{protocol: " + info + ", criteria:" + criteria + "}";
  }
  
}
