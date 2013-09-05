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

import com.esri.gpt.framework.resource.query.Criteria;
import java.util.logging.Logger;

/**
 * DCAT proxy.
 */
class DCATProxy {

  /** logger */
  private static final Logger LOGGER = Logger.getLogger(DCATProxy.class.getCanonicalName());
  /** service info */
  private DCATInfo info;
  /** criteria */
  private Criteria criteria;

  /**
   * Creates instance of the proxy.
   * @param info service info
   * @param criteria criteria
   */
  public DCATProxy(DCATInfo info, Criteria criteria) {
    if (info == null) {
      throw new IllegalArgumentException("No info provided.");
    }
    this.info = info;
    this.criteria = criteria;
  }
  
}
