/*
 * Copyright 2016 Esri, Inc..
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

import com.esri.gpt.framework.context.AppEnv;
import static com.esri.gpt.catalog.harvest.protocols.HarvestProtocolAgp2Agp.DEFAULT_MAX_ITEMS_AGP2AGP_KEY;
import static com.esri.gpt.catalog.harvest.protocols.HarvestProtocolDCAT.FORMAT_PATTERN_KEY;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;

/**
 * Default harvest environment.
 */
public class DefaultHarvestEnvironment implements AppEnv {
  public final static String WH_ENC_KEY = "webharvest.enckey";
  public final static String WH_MAX_ITEMS_AGP2AGP =  DEFAULT_MAX_ITEMS_AGP2AGP_KEY;
  public final static String WH_CSW_PROFILE = "webharvest.cswprofile";
  public final static String WH_FORMAT_PATTERN = FORMAT_PATTERN_KEY;
  
  private final ApplicationConfiguration appCfg;
  
  /**
   * Gets instance of the default environment.
   * @return instance
   */
  public static AppEnv getInstance() {
    return new DefaultHarvestEnvironment(ApplicationContext.getInstance().getConfiguration());
  }

  /**
   * Creates instance.
   * @param appCfg application configuration
   */
  public DefaultHarvestEnvironment(ApplicationConfiguration appCfg) {
    this.appCfg = appCfg;
  }

  @Override
  public String getValue(String attributeName) {
    if (WH_ENC_KEY.equals(attributeName)) {
      return appCfg.getIdentityConfiguration().getEncKey();
    }
    if (WH_CSW_PROFILE.equals(attributeName)) {
      return appCfg.getCatalogConfiguration().getSearchConfig().getCswProfile();
    }
    return appCfg.getCatalogConfiguration().getParameters().getValue(attributeName);
  }
  
  
}
