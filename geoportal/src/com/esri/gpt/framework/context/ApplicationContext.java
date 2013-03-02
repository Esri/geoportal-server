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
package com.esri.gpt.framework.context;
import com.esri.gpt.control.webharvest.engine.Harvester;
import com.esri.gpt.framework.jsf.MessageBroker;
import java.util.logging.Level;

import com.esri.gpt.framework.util.LogUtil;

/**
 * Context for an application.
 */
public class ApplicationContext {

// class variables =============================================================

/** Singleton instance. */
private static ApplicationContext SINGLETON = null;

// instance variables ==========================================================
private ApplicationConfiguration _configuration;
private Harvester harvestingEngine;

// constructors ================================================================

/** Default constructor. */
protected ApplicationContext() {
  this.setConfiguration(newApplicationConfiguration());
}

// properties ==================================================================

/**
 * Gets the configuration associated with this application.
 * @return the configuration
 */
public ApplicationConfiguration getConfiguration() {
  return _configuration;
}
/**
 * Sets the configuration associated with this application.
 * @param configuration the configuration
 */
protected void setConfiguration(ApplicationConfiguration configuration) {
  _configuration = configuration;
  if (_configuration == null) {
    _configuration = newApplicationConfiguration();
  }
}

/**
 * Gets the single ApplicationContext instance for this application.
 * @return the context for this application
 */
public static synchronized ApplicationContext getInstance() {
  if (SINGLETON == null) {
    SINGLETON = new ApplicationContext();
    SINGLETON.loadConfiguration();
  }
  return SINGLETON;
}

// methods =====================================================================

/**
 * Loads the configuration for the application.
 */
protected void loadConfiguration() {
  try {
    ApplicationConfiguration config = newApplicationConfiguration();
    ApplicationConfigurationLoader loader = new ApplicationConfigurationLoader();
    loader.load(config);
    setConfiguration(config);
  } catch (Throwable t) {
    StringBuffer sb = new StringBuffer();
    sb.append("An error occured while loading the application configuration.");
    LogUtil.getLogger().log(Level.SEVERE,sb.toString(),t);
  } 
}

/**
 * Gets harvesting engine.
 * @return harvesting engine
 */
public Harvester getHarvestingEngine() {
  return harvestingEngine;
}

/**
 * Sets harvesting engine.
 * @param harvestingEngine harvesting engine
 */
public void setHarvestingEngine(Harvester harvestingEngine) {
  this.harvestingEngine = harvestingEngine;
}

/**
 * Instantiates an new application configuration.
 * @return the application configuration
 */
private ApplicationConfiguration newApplicationConfiguration() {
  return new ApplicationConfiguration();
}
}
