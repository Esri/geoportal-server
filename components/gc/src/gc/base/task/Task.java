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
import java.util.concurrent.Callable;

/**
 * Represents a callable task.
 */
public abstract class Task implements Callable<TaskResult> {
	
	/** Instance variables. */
	private TaskContext context;
	private TaskResult  result;
	
	/** Default constructor. */
	public Task() {}
	
	/**
	 * Constructs with a supplied context.
	 * @param context the task context
	 */
	public Task(TaskContext context) {
		setContext(context);
		setResult(new TaskResult());
	}
	
	/**
	 * Gets the context.
	 * @return the task context
	 */
	public TaskContext getContext() {
		return context;
	}
	/**
	 * Sets the context.
	 * @param context the task context
	 */
	public void setContext(TaskContext context) {
		this.context = context;
	}
	
	/**
	 * Gets the result.
	 * @return the result
	 */
	public TaskResult getResult() {
		return result;
	}
	/**
	 * Sets the result.
	 * @param result the result
	 */
	public void setResult(TaskResult result) {
		this.result = result;
	}
	
	/**
	 * Runs the task.
	 * @return the result
	 * @throws Exception if an exception occurs
	 */
	@Override
	public TaskResult call() throws Exception {
		// TODO: flow
		try {
			executeTask();
		} catch (Exception e) {
			//e.printStackTrace();
			throw e;
		} finally {
			// summarize
		}
		return getResult();
	}
	
	/**
	 * Executes the task.
	 * @throws Exception if an exception occurs
	 */
	protected abstract void executeTask() throws Exception;
	
}
