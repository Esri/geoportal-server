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
package com.esri.gpt.catalog.lucene;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.scheduler.IScheduledTask;
import com.esri.gpt.server.assertion.AsnConfig;
import com.esri.gpt.server.assertion.AsnFactory;
import com.esri.gpt.server.assertion.index.AsnIndexAdapter;
import com.esri.gpt.server.assertion.index.AsnIndexReference;
import com.esri.gpt.server.assertion.index.AsnIndexReferences;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * Background thread to optimize the Lucene index.
 */
public class LuceneIndexOptimizer implements Runnable, IScheduledTask {

  /** class variables ========================================================= */
	private static Logger LOGGER = Logger.getLogger(LuceneIndexOptimizer.class.getName());

	/** instance variables ====================================================== */
	private StringAttributeMap parameters = null;
	private boolean            wasInterrupted = false;
	
	/** constructors  =========================================================== */

	/** Default constructor. */
	public LuceneIndexOptimizer() {}

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
	 * Run the optimization process.
	 */
	public void run() {
	  LOGGER.info("Optimization run started...");
		RequestContext context = null;
		IndexWriter writer = null;
		Lock backgroundLock = null;
		long tStartMillis = System.currentTimeMillis();
		try {
		  
		  // initialize
			context = RequestContext.extract(null);
			LuceneIndexAdapter adapter = new LuceneIndexAdapter(context);
	    adapter.touch(); // ensures that a proper directory structure exists
	    if (this.checkInterrupted()) return;
	    
			// obtain the background thread lock, 
			// sleep for 10 minutes if busy then try again
			try {
			  backgroundLock = adapter.obtainBackgroundLock();
			} catch (LockObtainFailedException lofe) {
			  if (this.checkInterrupted()) return;
	      try {
	        Thread.sleep(10 * 1000);
	      } catch (InterruptedException e) {
	        throw new IOException(e.toString());
	      }
	      if (this.checkInterrupted()) return;
	      backgroundLock = adapter.obtainBackgroundLock();
			}
      
			// optimize the index
      writer = adapter.newWriter();
      if (this.checkInterrupted()) return;
      writer.optimize();
      adapter.closeWriter(writer);
      writer = null;
      
      // log the summary message
      double dSec = (System.currentTimeMillis() - tStartMillis) / 1000.0;
      StringBuffer msg = new StringBuffer();
      msg.append("Optimization run completed."); 
      msg.append(", runtime: ");
      msg.append(Math.round(dSec / 60.0 * 100.0) / 100.0).append(" minutes");
      if (dSec <= 600) {
        msg.append(", ").append(Math.round(dSec * 100.0) / 100.0).append(" seconds");
      }
      LOGGER.info(msg.toString());
      
    } catch (LockObtainFailedException e) {
      LOGGER.log(Level.INFO,"Optimization run aborted, reason: "+e.getMessage());
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE,"Error optimizing index.",t);
		} finally {
			if (writer != null) {
  			try {
  			  writer.close();
  		  } catch (Throwable t) {
  		    LOGGER.log(Level.SEVERE,"Error closing IndexWriter.",t);
  		  } 
			}
			if (backgroundLock != null) {
        try {
          backgroundLock.release();
        } catch (Throwable t) {
          LOGGER.log(Level.WARNING,"Error releasing lock.",t);
        }
			}
	    if (context != null) {
	      context.onExecutionPhaseCompleted();
	    }
      if (this.wasInterrupted) {
        LOGGER.info("LuceneIndexOptimizer run was interrupted."); 
      }
		}
	
	  // optimize the assertion indexes
	  AsnFactory asnFactory = AsnFactory.newFactory(null);
	  AsnConfig asnConfig = asnFactory.getConfiguration();
	  if (asnConfig.getAreAssertionsEnabled()) {
	    AsnIndexReferences asnIndexRefs = asnConfig.getIndexReferences();
	    if (asnIndexRefs != null) {
	      for (AsnIndexReference asnIndexRef: asnIndexRefs.values()) {
	        if ((asnIndexRef != null) && asnIndexRef.getEnabled()) {
            String asnLoc = asnIndexRef.getIndexLocation();
            LOGGER.fine("Optimizing assertion index: "+asnLoc);
	          try {
	            long asnStartMillis = System.currentTimeMillis();
              AsnIndexAdapter asnIndexAdapter = asnIndexRef.makeIndexAdapter(null);
              asnIndexAdapter.optimize();
              
              double asnSec = (System.currentTimeMillis() - asnStartMillis) / 1000.0;
              StringBuffer msg = new StringBuffer();
              msg.append("Optimization of assertion index complete: "+asnLoc); 
              msg.append(", runtime: ");
              msg.append(Math.round(asnSec / 60.0 * 100.0) / 100.0).append(" minutes");
              if (asnSec <= 600) {
                msg.append(", ").append(Math.round(asnSec * 100.0) / 100.0).append(" seconds");
              }
              LOGGER.fine(msg.toString());
              
            } catch (Exception e) {
              LOGGER.log(Level.SEVERE,"Error optimizing assertion index: "+asnLoc,e);
            }
	        }
	      }
	    }
	  }
	  
	}

}
