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
package gc.solr.task;

import java.util.concurrent.ScheduledFuture;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Gptdb2SolrWebListener implements ServletContextListener {

	private ScheduledFuture<?> gptdb2SolrHandle = null;
	
  public void contextInitialized(ServletContextEvent event) {
		Gptdb2SolrScheduled scheduled = new Gptdb2SolrScheduled();
		gptdb2SolrHandle = scheduled.execute();
  }

  public void contextDestroyed(ServletContextEvent event) {
  	//System.err.println("Gptdb2SolrWebListener.contextDestroyed");
  	if (gptdb2SolrHandle != null) {
  		//System.err.println("111111111111111111111");
  		gptdb2SolrHandle.cancel(true);
  		//System.err.println("222222222222222222222");
  		//System.err.println(gptdb2SolrHandle.isDone());
  	}
  }
}
