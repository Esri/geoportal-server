/*
 * Copyright 2013 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.georss.dcatcache;

import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.control.georss.DcatJsonFeedWriter;
import com.esri.gpt.control.georss.DcatJsonSearchEngine;
import com.esri.gpt.control.georss.FeedLinkBuilder;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.provider.local.CoreQueryables;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * DCAT cache update request.
 */
public class DcatCacheUpdateRequest {
  private static final Logger LOGGER = Logger.getLogger(DcatCacheUpdateRequest.class.getCanonicalName());
  private volatile static Thread runningThread;
  
  /**
   * Executes update request.
   * @throws IOException if request fails
   */
  public void execute() throws IOException {
    if (runningThread==null) {
      Runnable runnable = new DcatCacheUpdateRunnable();
      Thread thread = new Thread(runnable, "DcatCacheUpdateRequest");
      thread.setDaemon(true);
      thread.start();
    } else {
      LOGGER.info("DCAT cache update process already running.");
    }
  }
  
  /**
   * Called when update is complete.
   */
  protected void onCompleted() {}
  
  /**
   * Called when exception is being thrown during update.
   * @param ex exception
   */
  protected void onException(Exception ex){}
  
  /**
   * Runnable for update process.
   */
  private class DcatCacheUpdateRunnable implements Runnable {

    @Override
    public void run() {
      runningThread = Thread.currentThread();
      try {
        process();
        onCompleted();
      } catch (Exception ex) {
        onException(ex);
      } finally {
        runningThread = null;
      }
    }
    
  }
  
  /**
   * Processes request.
   * @throws IOException if error processing request
   */
  private void process() throws IOException {
    LOGGER.info("Starting DCAT cache update process...");
    RestQuery query = new RestQuery();
    query.setResponseFormat("dcat");
    RequestContext context = RequestContext.extract(null);
    MessageBroker msgBroker = new MessageBroker();
    msgBroker.setBundleBaseName(MessageBroker.DEFAULT_BUNDLE_BASE_NAME);
    
    String baseContextPath = Val.chkStr(RequestContext.resolveBaseContextPath(null));
    if (baseContextPath.isEmpty()) {
      StringAttributeMap params = ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration().getParameters();
      baseContextPath = Val.chkStr(params.getValue("reverseProxy.baseContextPath"));;
    }
    
    FeedLinkBuilder linkBuilder = new FeedLinkBuilder(context, baseContextPath, msgBroker);
    
    DcatCache cache = DcatCache.getInstance();
    OutputStream cacheStream = null;
    PrintWriter writer = null;
    
    try {
      cacheStream = cache.createOutputCacheStream();
      writer = new PrintWriter(new OutputStreamWriter(cacheStream, "UTF-8"));
      
      DcatJsonFeedWriter feedWriter = new DcatJsonFeedWriter(context, writer, query);
      feedWriter.setMessageBroker(msgBroker);
      
      query.setReturnables(new CoreQueryables(context).getFull());
      
      DcatJsonSearchEngine.DcatRecordsAdapter discoveredRecordsAdapter = new DcatJsonSearchEngine.DcatRecordsAdapter(msgBroker, linkBuilder, context, query);
      
      feedWriter.write(discoveredRecordsAdapter);
    } finally {
      LOGGER.info("DCAT cache update process completed.");
      if (writer!=null) {
          writer.close();
      }
    }
  }
  
}
