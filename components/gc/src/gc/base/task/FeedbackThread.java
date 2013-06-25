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

/**
 * Provides logging feedback during task execution.
 */
public class FeedbackThread extends Thread {

	/** instance variables */
	private long             feedbackMillis = 500;
	private FeedbackWriter   feedbackWriter;
  private volatile boolean stop = false;
	
  /**
   * Construct with a supplied task context.
   * @param context the task context
   */
	public FeedbackThread(TaskContext context) {
		this.feedbackWriter = context.getStats();
		this.feedbackMillis = context.getStats().getFeedbackMillis();
	}
	
  /**
   * Construct with a writer and interval.
   * @param feedbackWriter the writer
   * @param feedbackMillis the interval
   */
	public FeedbackThread(FeedbackWriter feedbackWriter, long feedbackMillis) {
		this.feedbackWriter = feedbackWriter;
		this.feedbackMillis = feedbackMillis;
	}
	
	/**
	 * Run the thread.
	 */
  public void run() {
    while (!stop) {
      try {
      	feedbackWriter.writeIntervalFeedback();
        Thread.sleep(feedbackMillis);
      } catch (InterruptedException e) {
      	stop = true;
      }
    }
  }
  
  /**
   * Flags thread termination.
   */
  public void terminate() {
  	stop = true;
  }

}
