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
package com.esri.gpt.control.webharvest.engine;
import com.esri.gpt.catalog.lucene.LuceneIndexSynchronizer;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.scheduler.IScheduledTask;
import com.esri.gpt.framework.util.TimePeriod;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Performs a scheduled pause of the harvesting engine.
 * <p>
 * Intended to be used when multiple remote nodes are synchronizing
 * a local Lucene index at a scheduled time.
 * </p>
 */
public class ScheduledPause implements Runnable, IScheduledTask {
  private final static long DEFAULT_CONNECTION_TIMEOUT     = 30 * 60 * 1000; // 30 minutes
  private final static long DEFAULT_RESPONSE_TIMEOUT       = 30 * 60 * 1000; // 30 minutes
  private final static long DEFAULT_INITIAL_SLEEP_TIME     = 30 * 60 * 1000; // 30 minutes
  private final static long DEFAULT_CONSECUTIVE_SLEEP_TIME = 15 * 60 * 1000; // 15 minutes

  /** class variables ========================================================= */
  private static Logger LOGGER = Logger.getLogger(ScheduledPause.class.getName());

  /** instance variables ====================================================== */
  private StringAttributeMap parameters = null;
  private boolean            wasInterrupted = false;
  
  /** constructors  =========================================================== */

  /** Default constructor. */
  public ScheduledPause() {}

  /** properties  ============================================================= */
  
  /**
   * Sets the configuration paramaters for the task.
   * @param parameters the configuration paramaters
   */
  public void setParameters(StringAttributeMap parameters) {
    this.parameters = parameters;
  }

  /** methods ================================================================= */
  
  /**
   * Checks to see if the thread was interrupted.
   * @return true if the thread was interrupted
   */
  private boolean checkInterrupted() {
    if (!this.wasInterrupted) {
      if (Thread.interrupted()) {
        this.wasInterrupted = true;
      }
    }
    return this.wasInterrupted;
  }
  
  /**
   * Check to see if remote nodes with local Lucene indexes are
   * actively running the Lucene index synchronizer.
   * @param taskParams the scheduled task parameters
   */
  private RemoteIndexerInfo checkRemoteIndexers(StringAttributeMap taskParams) {
    long tStartMillis = System.currentTimeMillis();
    String sUrls = Val.chkStr(taskParams.getValue("remoteIndexingUrls"));
    LOGGER.info("Checking remoteIndexingUrls: "+sUrls);
    String[] aUrls = Val.tokenize(sUrls,",");
    
    RemoteIndexerInfo info = new RemoteIndexerInfo();
    info.numUrls = aUrls.length;
    
    String connectionTimeout = taskParams.getValue("connectionTimeout");
    String responseTimeout = taskParams.getValue("responseTimeout");
    
    try {
      for (String sUrl: aUrls) {
        if ("self".equalsIgnoreCase(sUrl)) {
          boolean isRunning = LuceneIndexSynchronizer.RUNNING;
          if (isRunning) {
            info.numActive++;
          } else {
            info.numInactive++;
          }
        } else {
          sUrl += "?action=isSynchronizerRunning";
          HttpClientRequest request = new HttpClientRequest();
          request.setConnectionTimeMs((int)parsePeriod(connectionTimeout, DEFAULT_CONNECTION_TIMEOUT).getValue());
          request.setResponseTimeOutMs((int)parsePeriod(responseTimeout, DEFAULT_RESPONSE_TIMEOUT).getValue());
          request.setUrl(sUrl);
          try {
            LOGGER.info("Checking: "+sUrl);
            String response = Val.chkStr(request.readResponseAsCharacters());
            LOGGER.info("Response from: "+sUrl+" ="+response);
            if (response.equalsIgnoreCase("true")) {
              info.numActive++;
            } else if (response.equalsIgnoreCase("false")) {
              info.numInactive++;
            } else {
              info.numUnexpected++;
            }
          } catch (SocketTimeoutException ex) {
            info.numTimedOut++;
            LOGGER.info("Timeout on: "+sUrl);
          } catch (IOException e) {
            info.numFailed++;
            LOGGER.log(Level.SEVERE,"Error with: "+sUrl,e);
          }
        }
      }
    } finally {
      double dSec = (System.currentTimeMillis() - tStartMillis) / 1000.0;
      StringBuilder msg = new StringBuilder();
      msg.append(" Check remote indexers:");
      msg.append(" urls=").append(aUrls.length);
      msg.append(", active=").append(info.numActive);
      msg.append(", inactive=").append(info.numInactive);
      msg.append(", unexpected=").append(info.numUnexpected);
      msg.append(", timedout=").append(info.numTimedOut);
      msg.append(", failed=").append(info.numFailed);
      msg.append(", time=").append(dSec).append("seconds");
      LOGGER.info(msg.toString());
    }
    
    return info;
  }
  
  /**
   * Run the process.
   */
  @Override
  public void run() {
    LOGGER.info("Harvester scheduled pause run started...");
    RequestContext context = null;
    
    String initialSleepTime = parameters.getValue("initialSleepTime");
    String consecutiveSleepTime = parameters.getValue("consecutiveSleepTime");
    long tInitialTime = parsePeriod(initialSleepTime, DEFAULT_INITIAL_SLEEP_TIME).getValue();
    long tConsecutiveTime = parsePeriod(consecutiveSleepTime, DEFAULT_CONSECUTIVE_SLEEP_TIME).getValue();
    
    // suspend harvesting engine
    getHarvestingEngine().safeSuspend();

    
    try {
      // initial sleep
      Thread.sleep(tInitialTime);
      
      do {
        RemoteIndexerInfo info = checkRemoteIndexers(this.parameters);
        if (checkInterrupted()) return;
        if (info.numActive==0) break;
        
        // consecutive sleep
        Thread.sleep(tConsecutiveTime);
      } while (true);
      
      
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE,"Error during scheduled pause.",t);
    } finally {
      
      // we are done waiting, resume harvesting
      if (!checkInterrupted()) {
        LOGGER.info("Harvester scheduled pause completed, resuming harvester...");
        getHarvestingEngine().safeResume();
      }
      
      if (context != null) {
        context.onExecutionPhaseCompleted();
      }
      if (this.wasInterrupted) {
        LOGGER.info("Harvester scheduled pause was interrupted."); 
      }
    }
  }
  
  /**
   * Safely parses time period giving default value if time period can not be parsed.
   * @param periodDef period definition to parse
   * @param defaultValue default value if period definition cannot be parsed
   * @return time period
   */
  private TimePeriod parsePeriod(String periodDef, long defaultValue) {
    try {
      return TimePeriod.parseValue(periodDef);
    } catch (IllegalArgumentException ex) {
      return new TimePeriod(defaultValue);
    }
  }
  
  private Harvester getHarvestingEngine() {
    return ApplicationContext.getInstance().getHarvestingEngine();
  }
  
  /** Stores information collected checking remote indexers. */
  class RemoteIndexerInfo {
    int numUrls = 0;
    int numActive = 0;
    int numInactive = 0; 
    int numUnexpected = 0; 
    int numFailed = 0;
    int numTimedOut = 0;
    
    boolean isOk() {

      boolean bOk = ((numActive + numInactive + numTimedOut) == numUrls);
      if (!bOk) {
        bOk = ((numActive + numInactive + numTimedOut + numUnexpected) == numUrls);
      }
      
      return bOk;
    }
  }
}
