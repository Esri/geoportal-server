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
package com.esri.gpt.framework.context;

import static com.esri.gpt.catalog.harvest.protocols.HarvestProtocolAgp2Agp.DEFAULT_MAX_ITEMS_AGP2AGP_KEY;
import static com.esri.gpt.catalog.harvest.protocols.HarvestProtocolDCAT.FORMAT_PATTERN_KEY;
import com.esri.gpt.framework.collection.StringAttributeMap;

/**
 * AppEnv wrapper.
 */
public class AppEnvAppCfgAdaptor implements AppEnv {
  public static final String X_HTTP_CONNECTION_TIMEOUT = "http.connection.timeout";
  public static final String X_HTTP_RESPONSE_TIMEOUT = "http.response.timeout";
  public static final String X_HTTP_ALWAYS_CLOSE = "httpClient.alwaysClose";
  public static final String X_HTTP_ALWAYS_PEEK = "HttpClientRequest.allowPeekForXmlEncoding";
  public final static String X_WEBHARVEST_ENC_KEY = "webharvest.enckey";
  public final static String X_WEBHARVEST_MAX_ITEMS_AGP2AGP =  DEFAULT_MAX_ITEMS_AGP2AGP_KEY;
  public final static String X_WEBHARVEST_CSW_PROFILE = "webharvest.cswprofile";
  public final static String X_WEBHARVEST_FORMAT_PATTERN = FORMAT_PATTERN_KEY;
  public static final String X_BOT_ENABLED_PARAM  = "bot.robotstxt.enabled";
  public static final String X_BOT_OVERRIDE_PARAM = "bot.robotstxt.override";
  public static final String X_BOT_AGENT_PARAM    = "bot.agent";
  
  protected final ApplicationConfiguration appCfg;
  
  public final AppEnv WithExtras = new WithExtras();
  
  public static AppEnv newInstance() {
    return new AppEnvAppCfgAdaptor(ApplicationContext.getInstance().getConfiguration()).WithExtras;
  }

  public AppEnvAppCfgAdaptor(ApplicationConfiguration appCfg) {
    this.appCfg = appCfg;
  }

  @Override
  public String getValue(String attributeName) {
    return getParameters().getValue(attributeName);
  }
  
  protected StringAttributeMap getParameters() {
    return appCfg!=null? appCfg.getCatalogConfiguration().getParameters(): new StringAttributeMap();
  }
  
  private class WithExtras implements AppEnv {

    @Override
    public String getValue(String attributeName) {
      if (X_HTTP_CONNECTION_TIMEOUT.equals(attributeName)) {
        return Integer.toString(appCfg.getCatalogConfiguration().getConnectionTimeOutMs());
      }
      if (X_HTTP_RESPONSE_TIMEOUT.equals(attributeName)) {
        return Integer.toString(appCfg.getCatalogConfiguration().getResponseTimeOutMs());
      }
      if (X_WEBHARVEST_ENC_KEY.equals(attributeName)) {
        return appCfg.getIdentityConfiguration().getEncKey();
      }
      if (X_WEBHARVEST_CSW_PROFILE.equals(attributeName)) {
        return appCfg.getCatalogConfiguration().getSearchConfig().getCswProfile();
      }
      return AppEnvAppCfgAdaptor.this.getValue(attributeName);
    }
    
  }
}
