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

import com.esri.gpt.catalog.lucene.LuceneIndexAdapter;
import com.esri.gpt.control.webharvest.engine.Harvester;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.scheduler.ThreadScheduler;
import com.esri.gpt.framework.util.LogUtil;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import java.util.logging.Level;

/**
 * Initializes configuration upon web application startup.
 * <p>
 * The servlet needs to be identified in the WEB-INF/web.xml file.
 * <br/>&lt;servlet&gt;
 * <br/>&lt;servlet-name&gt;Application Initialization Servlet&lt;/servlet-name&gt;
 * <br/>  &lt;servlet-class&gtcom.esri.gpt.framework.contextInitializationServlet&lt;servlet-class&gt
 * <br/>  &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 * <br/>&lt;/servlet&gt;
 * </p>
 */
public class InitializationServlet extends HttpServlet {

// class variables =============================================================

// instance variables ==========================================================
/** thread scheduler. */  
private ThreadScheduler _scheduler = new ThreadScheduler();

// constructors ================================================================
/** Default constructor. */
public InitializationServlet() {
  super();
}

// properties ==================================================================

// methods =====================================================================
/**
 * Intitalizes the application configuration.
 * @throws ServletException if an exception occurs
 */
@Override
public void init() throws ServletException {
  super.init();
  try {
    LogUtil.getLogger().info("Initializing ApplicationContext...");
    // initialize applciation context
    ApplicationContext appCtx = ApplicationContext.getInstance();
    
    // inform the Lucene index adapter
    LuceneIndexAdapter.onContextInit(appCtx);

    //// create harvester engine
    // create message broker
    MessageBroker messageBroker = new MessageBroker();
    messageBroker.setBundleBaseName(MessageBroker.DEFAULT_BUNDLE_BASE_NAME);

    // create web harvester
    Harvester harvester = new Harvester(messageBroker, appCtx.getConfiguration().getHarvesterConfiguration());
    appCtx.setHarvestingEngine(harvester);
    
    // start web harvester
    harvester.init();

    // schedule tasks
    schedule();
  } catch (Throwable t) {
    LogUtil.getLogger().log(Level.SEVERE, "Initialization failed.", t);
  }
}

/**
 * Destroys servlet.
 */
@Override
public void destroy() {
  LogUtil.getLogger().info("Destroying ApplicationContext...");
  ApplicationContext appCtx = ApplicationContext.getInstance();
  appCtx.getHarvestingEngine().shutdown();
  shutdown();
  
  // inform the Lucene index adapter
  LuceneIndexAdapter.onContextDestroy(appCtx);
  
  super.destroy();
}

/**
 * Schedules background threads.
 */
private void schedule() {
  _scheduler.schedule(
    ApplicationContext.getInstance().getConfiguration().
    getThreadSchedulerConfiguration());
}

/**
 * Shuts down background threads.
 */
private void shutdown() {
  _scheduler.shutdown();
}

}
