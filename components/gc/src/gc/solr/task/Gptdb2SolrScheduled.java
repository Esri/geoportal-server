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

import gc.base.task.SingleTaskExecutor;
import gc.base.task.TaskContext;
import gc.base.xmltypes.XmlTypes;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Gptdb2SolrScheduled {
	
  private final ScheduledExecutorService scheduler =
      Executors.newScheduledThreadPool(1);
  
  public void execute() {
  	
    final Runnable worker = new Runnable() {
      public void run() {
      	try {
	    		List<Gptdb2SolrInstance> instances = Gptdb2SolrInstance.createInstancesFromConfig();
	    		for (Gptdb2SolrInstance instance: instances) {
	    			XmlTypes xmlTypes = XmlTypes.createFromConfig();
	    			TaskContext context = new TaskContext("Gptdb2SolrTask");
	    			context.getStats().setFeedbackMillis(60000);
	    			Gptdb2SolrTask task = new Gptdb2SolrTask(context,xmlTypes,instance);
	    			SingleTaskExecutor.executeTask(task);
	    		}
      	} catch (Exception e) {
    		  e.printStackTrace();
      	}
      }
    };
    
    long initialDelay = 15;
    long delay = 60 * 60 * 24;
    final ScheduledFuture<?> handle = scheduler.scheduleWithFixedDelay(
    		worker,initialDelay,delay,TimeUnit.SECONDS);
  }
  
}
