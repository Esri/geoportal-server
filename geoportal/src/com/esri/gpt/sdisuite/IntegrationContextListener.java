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
package com.esri.gpt.sdisuite;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Servlet context listener associated with sde.suite component integration.
 */
public class IntegrationContextListener implements ServletContextListener {

  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(IntegrationContextListener.class.getName());
  
  /**
   * Invoked when the servlet context is initialized.
   * @param sce the servlet context event.
   */
  public void contextInitialized(final ServletContextEvent sce) {
    LOGGER.log(Level.FINE,"Handling contextInitialized");
    IntegrationContextFactory icf = new IntegrationContextFactory();
    if (icf.isIntegrationEnabled()) {
      try {
        LOGGER.log(Level.FINE,"Handling contextInitialized, IntegrationContextFactory.isIntegrationEnabled=true");
        icf.newIntegrationContext();
      } catch (ClassNotFoundException e) {
        LOGGER.log(Level.CONFIG,"Error during contextInitialized.",e);
      } catch (InstantiationException e) {
        LOGGER.log(Level.CONFIG,"Error during contextInitialized.",e);
      } catch (IllegalAccessException e) {
        LOGGER.log(Level.CONFIG,"Error during contextInitialized.",e);
      }
    }
  }

  /**
   * Invoked when the servlet context is destroyed.
   * @param sce the servlet context event.
   */
  public void contextDestroyed(final ServletContextEvent sce) {
    LOGGER.log(Level.FINE,"Handling contextDestroyed");
    IntegrationContextFactory icf = new IntegrationContextFactory();
    if (icf.isIntegrationEnabled()) {
      try {
        LOGGER.log(Level.FINE,"Handling contextDestroyed, IntegrationContextFactory.isIntegrationEnabled=true");
        IntegrationContext ic = icf.newIntegrationContext();
        ic.shutdown();
      } catch (ClassNotFoundException e) {
        LOGGER.log(Level.CONFIG,"Error during contextDestroyed.",e);
      } catch (InstantiationException e) {
        LOGGER.log(Level.CONFIG,"Error during contextDestroyed.",e);
      } catch (IllegalAccessException e) {
        LOGGER.log(Level.CONFIG,"Error during contextDestroyed.",e);
      } catch (Exception e) {
        LOGGER.log(Level.CONFIG,"Error during contextDestroyed.",e);
      }
    }
  }
}
