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
package com.esri.gpt.control.rest;
import com.esri.gpt.catalog.lucene.LuceneIndexAdapter;
//import com.esri.gpt.catalog.lucene.stats.GlobalFieldStats;
//import com.esri.gpt.catalog.lucene.stats.GlobalTermStats;
import com.esri.gpt.catalog.lucene.stats.SingleFieldStats;
//import com.esri.gpt.catalog.lucene.stats.SingleTermStats;
import com.esri.gpt.catalog.lucene.stats.SummaryStats;
import com.esri.gpt.catalog.lucene.stats.StatsRequest;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;

import java.io.PrintWriter;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

/**
 * Provides an HTTP REST for statistics associated with indexed documents.
 * <p>
 * Usage: <i>http://host:port/context</i><b>/rest/index/stats/fields</b>
 * <br/>provides a summary listing of fields indexed and a count of documents indexed
 * <p>
 * Usage: <i>http://host:port/context</i><b>/rest/index/stats/fields?field=&maxRecords=&minFrequency=&maxFrequency=&f=</b>
 * <br/><b>field</b> - the target field name (required)
 * <br/><b>maxRecords</b> - the maximum number of records to return (alias=max),
 *   (optional, default=100, max=10000)
 * <br/><b>minFrequency</b> - the mimimum term frequency to consider (optional, default=1)
 * <br/><b>maxFrequency</b> - the maximum term frequency to consider (optional, default=none)
 * <br/><b>f</b> - the response format, html or json (optional, default=html)
 * 
 */
public class IndexStatsServlet extends BaseServlet {
  
  /** class variables ========================================================= */
  
  /** The logger.*/
  private static Logger LOGGER = Logger.getLogger(IndexStatsServlet.class.getName());
      
  /** methods ================================================================= */
 
  /**
   * Processes the HTTP request.
   * @param request the HTTP request.
   * @param response HTTP response.
   * @param context request context
   * @throws Exception if an exception occurs
   */
  @Override
  protected void execute(HttpServletRequest request, HttpServletResponse response, RequestContext context) 
    throws Exception {
    
    String responseFormat = "html";
    if (Val.chkStr(this.getParameterValue(request,"f")).equalsIgnoreCase("json")) {
      responseFormat = "json";
    }
    if (responseFormat.equals("html")) {
      response.setContentType("text/html");
    } else {
      response.setContentType("text/plain");
    }
    
    PrintWriter writer = response.getWriter();
    LuceneIndexAdapter adapter = null;
    IndexSearcher searcher = null;
    try {
      String field = this.getParameterValue(request,"field");
      String term = this.getParameterValue(request,"term");
      String sortBy = this.getParameterValue(request,"sortBy");
      String pathInfo = "";
      if (request.getPathInfo() != null) {
        pathInfo = Val.chkStr(request.getPathInfo().toLowerCase());
        if (pathInfo.startsWith("/")) pathInfo = pathInfo.substring(1);
        if (pathInfo.endsWith("/")) pathInfo = pathInfo.substring(0,(pathInfo.length() - 1));
        //System.err.println(pathInfo);
      }
      
      adapter = new LuceneIndexAdapter(context);   
      searcher = adapter.newSearcher();
      IndexReader reader = searcher.getIndexReader();
      StatsRequest statsRequest = new StatsRequest(context);
      statsRequest.setResponseWriter(writer);
      statsRequest.prepare(reader);
      statsRequest.setSortBy(sortBy);
      statsRequest.setResponseFormat(responseFormat);
      
      if (field.length() > 0) {
        int maxRecs = Val.chkInt(this.getParameterValue(request,"max"),-2);
        if (maxRecs == -2) {
          maxRecs = Val.chkInt(this.getParameterValue(request,"maxrecords"),-1);
        }
        int minFreq = Val.chkInt(this.getParameterValue(request,"minFrequency"),-1);
        int maxFreq = Val.chkInt(this.getParameterValue(request,"maxFrequency"),-1);
        SingleFieldStats stats = new SingleFieldStats(field,maxRecs,minFreq,maxFreq);
        stats.collectStats(statsRequest,reader);
//      } else if (term.length() > 0) {
//        SingleTermStats stats = new SingleTermStats(term);
//        stats.collectStats(statsRequest,reader);   
//      } else if (pathInfo.equals("terms")){
//        GlobalTermStats stats = new GlobalTermStats();
//        stats.collectStats(statsRequest,reader);  
      } else {
        //GlobalFieldStats stats = new GlobalFieldStats();
        SummaryStats stats = new SummaryStats();
        stats.collectStats(statsRequest,reader);  
      }
    
   // } catch (Exception ex) {

    //  response.setContentType("text/plain;charset=UTF-8");
     // writer.println("Error getting metadata: " + ex.getMessage());
     // LogUtil.getLogger().log(Level.SEVERE, "Error getting metadata", ex);

    } finally {
      try {
        writer.flush();
      } catch (Exception ef) {};
      try {
        if ((adapter != null) && (searcher != null)) {
          adapter.closeSearcher(searcher);
        }
      } catch (Exception ef) {};
    }
    
  }

}
