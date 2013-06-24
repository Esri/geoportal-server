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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Task statistics.
 */
public class TaskStats implements FeedbackWriter {

	/** instance variables */
	private Map<String,Long>    counts = Collections.synchronizedMap(new HashMap<String,Long>());
	private Map<String,String>  strings = Collections.synchronizedMap(new HashMap<String,String>());
	private Map<String,Long>    times = Collections.synchronizedMap(new HashMap<String,Long>());
	
	private long                feedbackMillis = 120000;
	private long                startTime;
	private String              taskName;
	
	public TaskStats(String taskName) {
		this.taskName = taskName;
		this.startTime = System.currentTimeMillis();
	}
	
	
	/**
	 * Gets the interval that should be used for logging message feedback.
	 * @return the feedback milliseconds
	 */
  public long getFeedbackMillis() {
		return feedbackMillis;
	}
	/**
   * Sets the interval that should be used for logging message feedback.
   * @param feedbackMillis the feedback milliseconds
   */
	public void setFeedbackMillis(long feedbackMillis) {
		this.feedbackMillis = feedbackMillis;
	}
	
	/**
	 * Gets the start time.
	 * @return the start time
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
   * Sets the start time.
   * @param startTime the start time
   */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
		
	/**
	 * Gets a count.
	 * @param tag the tag
	 */
	public long getCount(String tag) {
		return counts.get(tag);
	}
	
	/**
	 * Gets a string.
	 * @param tag the tag
	 */
	public String getString(String tag) {
		return strings.get(tag);
	}
	
	/**
	 * Gets a time.
	 * @param tag the tag
	 */
	public long getTime(String tag) {
		return times.get(tag);
	}
	
	/**
	 * Increments a count by 1.
	 * @param tag the tag
	 */
	public void incrementCount(String tag) {
		incrementCount(tag,1);
	}

	/**
	 * Increments a count.
	 * @param tag the tag
	 * @param num the increment
	 */
	public void incrementCount(String tag, long num) {
		synchronized(counts) {
			Long cur = counts.get(tag);
			if (cur == null) {
				counts.put(tag,num);
			} else {
				counts.put(tag,num+cur);
			}
		}
	}
	
	/**
	 * Increments a time.
	 * @param tag the tag
	 * @param millis the millisecond increment
	 */
	public void incrementTime(String tag, long millis) {
		synchronized(times) {
			Long cur = times.get(tag);
			if (cur == null) {
				times.put(tag,millis);
			} else {
				times.put(tag,millis+cur);
			}
		}
	}
	
	public void setCount(String tag, long num) {
		synchronized(counts) {
			counts.put(tag,num);
		}
	}
	
	public void setString(String tag, String value) {
		synchronized(strings) {
			strings.put(tag,value);
		}
	}
	
	public void setTime(String tag, long millis) {
		synchronized(times) {
			times.put(tag,millis);
		}
	}
	
	/**
	 * Increments a time based upon the starting time of this task.
	 * @param tag the tag
	 */
	//public void incrementTimeFrom(String tag) {
	//	incrementTimeFrom(tag,startTime);
	//}
	
	/**
	 * Increments a time based upon a starting time.
	 * @param tag the tag
	 * @param startTime the start time
	 */
	//public void incrementTimeFrom(String tag, long startTime) {
	//	incrementTime(tag,(System.currentTimeMillis() - startTime));
	//}

	/**
	 * Logs feedback on task completion.
	 * @param successful true if the task was successful
	 */
	@Override
	public void writeCompletionFeedback(boolean successful) {
		writeStats(true);
		System.err.println("  ...........................");
	}
	
	/**
	 * Logs interval feedback while the task is executing.
	 */
	@Override
	public void writeIntervalFeedback() {
		writeStats(false);
	}
	
	public void writeStats(boolean isComplete) {
		String nl = "\r\n";
		String pfx = nl+"  ";
		long tNow = System.currentTimeMillis();
		double dSec = (tNow - startTime) / 1000.0;
		double dSecR = Math.round(dSec * 100.0) / 100.0;
		double dMinR = Math.round(dSec / 60.0 * 100.0) / 100.0;
		StringBuilder sb = new StringBuilder();
		if (!isComplete) sb.append("Progress: ");
		sb.append(taskName);
		
		synchronized(strings) {
			for (Map.Entry<String,String> entry: strings.entrySet()) {
				String s = entry.getKey()+": "+entry.getValue();
				sb.append(pfx).append(s);
		  }
		}
		synchronized(counts) {
			for (Map.Entry<String,Long> entry: counts.entrySet()) {
				String s = entry.getKey()+": "+entry.getValue();
				sb.append(pfx).append(s);
		  }
		}
		synchronized(times) {
			for (Map.Entry<String,Long> entry: times.entrySet()) {
				String k = entry.getKey();
				long t = entry.getValue();
				double ds = t / 1000.0;
				double dsr = Math.round(ds * 100.0) / 100.0;
				double dmr = Math.round(dsr / 60.0 * 100.0) / 100.0;
				sb.append(pfx).append(k).append(": ").append(dmr).append(" minutes");
		    if (ds <= 600) {
		    	sb.append(", ").append(dsr).append(" seconds");
		    }
		  }
		}
		sb.append(pfx).append("Time: ").append(dMinR).append(" minutes");
    if (dSec <= 600) {
    	sb.append(", ").append(dSecR).append(" seconds");
    }
    System.err.println(sb.toString());
	}
	
}
