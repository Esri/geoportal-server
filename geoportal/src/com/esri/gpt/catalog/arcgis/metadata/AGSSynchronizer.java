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
package com.esri.gpt.catalog.arcgis.metadata;
import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.catalog.lucene.LuceneIndexAdapter;
import com.esri.gpt.catalog.publication.ProcessingContext;
import com.esri.gpt.catalog.publication.PublicationRecord;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.scheduler.IScheduledTask;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * Background thread to synchronize ArcGIS Server content with the metadata catalog.
 */
public class AGSSynchronizer implements Runnable, IScheduledTask {

  /** class variables ========================================================= */
  
  /** Logger */
  private static Logger LOGGER = Logger.getLogger(AGSSynchronizer.class.getName());
  
  /** instance variables ====================================================== */
  private StringAttributeMap parameters;
  
  /** constructors  =========================================================== */

  /** Default constructor. */
  public AGSSynchronizer() {}

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
   * Run the synchronization process.
   */
  public void run() {
    LOGGER.info("AGSSynchronizer run started...");
    RequestContext rContext = null;
    Lock backgroundLock = null;
    long tStartMillis = System.currentTimeMillis();
    try {
      
      // initialize
      String restUrl = "";
      String soapUrl = "";
      PublicationRecord template = new PublicationRecord(); 
      if (this.parameters != null) {
        restUrl = Val.chkStr(this.parameters.getValue("restUrl"));
        soapUrl = Val.chkStr(this.parameters.getValue("soapUrl"));
        template.setAutoApprove(
            Val.chkStr(this.parameters.getValue("autoApprove")).equalsIgnoreCase("true"));
        template.setUpdateOnlyIfXmlHasChanged(
            Val.chkStr(this.parameters.getValue("updateOnlyIfXmlHasChanged")).equalsIgnoreCase("true"));
      }
      if (restUrl.length() == 0) {
        LOGGER.log(Level.SEVERE,"AGSSynchronizer run aborted: the restUrl parameter was empty.");
        return;
      }
      if (soapUrl.length() == 0) {
        LOGGER.log(Level.SEVERE,"AGSSynchronizer run aborted: the soapUrl parameter was empty.");
        return;
      }
      
      // obtain the background thread lock, 
      // sleep for 10 minutes if busy then try again
      rContext = RequestContext.extract(null);
      LuceneIndexAdapter adapter = new LuceneIndexAdapter(rContext);
      adapter.touch(); // ensures that a proper directory structure exists
      try {
        backgroundLock = adapter.obtainBackgroundLock();
      } catch (LockObtainFailedException lofe) {
        if (Thread.currentThread().isInterrupted()) return;
        try {
          Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
          throw new IOException(e.toString());
        }
        if (Thread.currentThread().isInterrupted()) return;
        backgroundLock = adapter.obtainBackgroundLock();
      }
      if (Thread.currentThread().isInterrupted()) return;
      
      // process services on the ArcGIS server
      StringBuilder sbSummary = new StringBuilder();
      Publisher publisher = Publisher.makeSystemAdministrator(rContext);
      HttpClientRequest httpClient = HttpClientRequest.newRequest();
      
      ProcessingContext pContext = new ProcessingContext(rContext,publisher,httpClient,template,false);
      AGSProcessor ags = new AGSProcessor(pContext);
      ags.getTarget().setRestUrl(restUrl);
      ags.getTarget().setSoapUrl(soapUrl);
      ags.getTarget().setTargetUrl(restUrl);
      ags.getTarget().setTargetType(AGSTarget.TargetType.ROOT);
      if (!Thread.currentThread().isInterrupted()) {
        ags.process();
      }
      sbSummary.append("\n numCreated=").append(pContext.getNumberCreated());
      sbSummary.append(", numReplaced=").append(pContext.getNumberReplaced());
      sbSummary.append(", numUnchanged=").append(pContext.getNumberUnchanged());
      sbSummary.append(", numDeleted=").append(pContext.getNumberDeleted());
      sbSummary.append(", numFailed=").append(pContext.getNumberFailed());
            
      // log a summary message
      double dSec = (System.currentTimeMillis() - tStartMillis) / 1000.0;
      StringBuilder msg = new StringBuilder();
      msg.append("AGSSynchronizer run completed."); 
      msg.append("\n restUrl=").append(restUrl);
      msg.append("\n soapUrl=").append(soapUrl);
      msg.append(sbSummary.toString());
      msg.append("\n wasInterrupted=").append(Thread.currentThread().isInterrupted());
      msg.append(", runtime: ");
      msg.append(Math.round(dSec / 60.0 * 100.0) / 100.0).append(" minutes");
      if (dSec <= 600) {
        msg.append(", ").append(Math.round(dSec * 100.0) / 100.0).append(" seconds");
      }
      LOGGER.info(msg.toString());
      
    } catch (ImsServiceException e) {
      LOGGER.log(Level.SEVERE,"Deletion error.",e);
    } catch (CatalogIndexException e) {
      LOGGER.log(Level.SEVERE,"Catalog index error.",e);
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE,"Database error.",e);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE,"Unknown error.",e);
      
    } finally {
      if (backgroundLock != null) {
        try {
          backgroundLock.release();
        } catch (Throwable t) {
          LOGGER.log(Level.WARNING,"Error releasing lock.",t);
        }
      }
      if (rContext != null) {
        rContext.onExecutionPhaseCompleted();
      }
      if (Thread.currentThread().isInterrupted()) {
        LOGGER.info("AGSSynchronizer run was interrupted."); 
      }
    }
  }

}

