/*
 * Copyright 2012 Esri.
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
package com.esri.gpt.control.webharvest.engine;

import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.util.Val;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Harvest policy
 */
public abstract class HarvestPolicy {
  protected static final Logger LOG = Logger.getLogger(HarvestPolicy.class.getCanonicalName());
  public static final String POLICY_CLASS_PARAM_NAME = "webharvester.policy.class";
  
  private static HarvestPolicy instance;
  
  public static HarvestPolicy getInstance() {
    if (instance==null) {
      String className = Val.chkStr(getParameters().getValue(POLICY_CLASS_PARAM_NAME));
      String strForceFrequency = Val.chkStr(getParameters().getValue(SimpleHarvestPolicy.SIMPLE_POLICY_FORCE_FREQUENCY));
      if (!className.isEmpty()) {
        LOG.info("[HARVEST POLICY] Attempting to create policy: "+className);
        try {
          Class harvestPolicyClass = Class.forName(className);
          instance = (HarvestPolicy)harvestPolicyClass.newInstance();
        } catch (Exception ex) {
          LOG.log(Level.SEVERE, "[HARVEST POLICY] Failed creating policy: "+className, ex);
          instance = newPolicy(strForceFrequency);
        }
      } else {
        instance = newPolicy(strForceFrequency);
      }
    }
    return instance;
  }
  
  private static HarvestPolicy newPolicy(String strForceFrequency) {
    if (!strForceFrequency.isEmpty()) {
      LOG.info("[HARVEST POLICY] Creating policy: "+SimpleHarvestPolicy.class.getCanonicalName());
      return new SimpleHarvestPolicy();
    } else {
      LOG.info("[HARVEST POLICY] Creating policy: "+DefaultHarvestPolicy.class.getCanonicalName());
      return new DefaultHarvestPolicy();
    }
  }
  
  protected static StringAttributeMap getParameters() {
    return ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration().getParameters();
  }
  
  
  public abstract boolean getForceFullHarvest(HrRecord record);
}
