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
package gc.base.task;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Executes a single task.
 */
public class SingleTaskExecutor {

	/**
	 * Executes the task and waits for completion.
	 * @param task the task
	 * @return true if the task was successful
	 */
	public static boolean executeTask(Task task) {
		boolean successful = false;
		TaskContext context = task.getContext();		
		TaskStats stats = context.getStats();
		FeedbackThread fbThread = new FeedbackThread(context);
		ExecutorService fbExecutor = Executors.newFixedThreadPool(1);
		fbExecutor.execute(fbThread);
		ExecutorService executor = Executors.newFixedThreadPool(1);
		try {
			Future<TaskResult> future = executor.submit(task);
			future.get(Long.MAX_VALUE,TimeUnit.DAYS);
			successful = true;
		} catch (Throwable tex) {
			tex.printStackTrace();
		} finally {
			executor.shutdownNow();
  	  fbExecutor.shutdownNow();
  	  stats.writeCompletionFeedback(successful);
		}
		return successful;
	}
	
}
