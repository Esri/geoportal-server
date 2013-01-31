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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;

import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;

/**
 * Handles requests to a remote indexing service.
 *
 */
public class RemoteIndexer {

  /** constructors ============================================================ */
  
  /** Default constructor. */
  public RemoteIndexer() {}
    
  /** methods ================================================================= */
  
  /**
   * Sends an indexing request to a remote service.
   * @param context the request context
   * @param action the action(publish|delete)
   * @param uuid the document uuid
   */
  public void send(RequestContext context, String action, String uuid) {
    String[] uuids = {uuid};
    this.send(context,action,uuids);
  }
  
  /**
   * Sends an indexing request to a remote service.
   * @param context the request context
   * @param action the action(publish|delete)
   * @param uuids the set of document uuids
   */
  public void send(RequestContext context, String action, String[] uuids) {
    if ((uuids != null) && (uuids.length > 0)) {
      StringAttributeMap params = context.getCatalogConfiguration().getParameters();
      String param = Val.chkStr(params.getValue("lucene.useRemoteWriter"));
      boolean useRemoteWriter = param.equalsIgnoreCase("true");
      String remoteWriterUrl = Val.chkStr(params.getValue("lucene.remoteWriterUrl"));
      if (useRemoteWriter && (remoteWriterUrl.length() > 0)) {
        RemoteIndexJob job = new RemoteIndexJob(action,uuids,remoteWriterUrl);
        Thread thread = new Thread(job,"RemoteIndexJob");
        thread.setDaemon(true);
        thread.start();
      } else {
        String s = "Inconsistent configuration parameters,"+
          " lucene.useRemoteWriter lucene.remoteWriterUrl";
        throw new IllegalArgumentException(s);
      }
    }
  }
  
  /**
   * Executes the remote indexing request
   */
  private class RemoteIndexJob implements Runnable { 
    private String   action;
    private String   remoteWriterUrl;
    private String[] uuids;
    
    // construct
    protected RemoteIndexJob(String action, String[] uuids, String remoteWriterUrl) {
      this.action = action;
      this.remoteWriterUrl = remoteWriterUrl;
      this.uuids = uuids;
    }
    
    // run
    public void run() {
      
      String err = "Error executing RemoteIndexJob: "+this.remoteWriterUrl;
      StringBuilder sb = new StringBuilder();
      for (String sUuid: this.uuids) {
        if (sb.length() > 0) sb.append(",");
        sb.append(sUuid);
      }
      try {
        String av = URLEncoder.encode(Val.chkStr(action),"UTF-8");
        String iv = URLEncoder.encode(sb.toString(),"UTF-8");
        String sUrl = this.remoteWriterUrl+"?action="+av+"&ids="+iv;
        URL url = new URL(sUrl);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        int code = con.getResponseCode();
        if ((code < 200) || (code >= 300)) {
          String s = err+ " responseCode="+code;
          LogUtil.getLogger().severe(s);
        }
        
      } catch (Exception e) {
        LogUtil.getLogger().log(Level.SEVERE,err,e);
      }      
    }
  }
  
}
