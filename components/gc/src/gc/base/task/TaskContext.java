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
import gc.base.util.UuidUtil;

/**
 * Provides a context for a task.
 */
public class TaskContext {

	
	private String              taskID = UuidUtil.makeUuid(true);
  private String              taskName; 
  private TaskStats           taskStats;
  
  /**
   * Construct with a supplied task name.
   * @param taskName the task name
   */
	public TaskContext(String taskName) {
		this.taskName = taskName;
		taskStats = new TaskStats(this.taskName);
	}
  
	/*
	public GcTaskContext(Configuration conf, String taskName) {
		this.conf = conf;
		this.taskName = taskName;
		taskStats = new GcTaskStats(this);
	}
	*/
	
	/*
	public Configuration getConfiguration() {
		return conf;
	}

	public void setConfiguration(Configuration conf) {
		this.conf = conf;
	}
	*/
	
	public String getTaskID() {
		return taskID;
	}
	public void setTaskID(String taskID) {
		this.taskID = taskID;
	}
	
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	
	public TaskStats getStats() {
		return taskStats;
	}
	public void setStats(TaskStats taskStats) {
		this.taskStats = taskStats;
	}
	
}
