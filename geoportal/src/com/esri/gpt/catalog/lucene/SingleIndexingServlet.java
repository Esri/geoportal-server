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
import com.esri.gpt.catalog.arcims.ImsMetadataAdminDao;
import com.esri.gpt.catalog.context.CatalogIndexAdapter;
import com.esri.gpt.catalog.schema.MetadataDocument;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.catalog.schema.SchemaException;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.metadata.MetadataAcl;
import com.esri.gpt.framework.sql.IClobMutator;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.Val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.index.IndexWriter;

/**
 * Lucene indexing service for a single JVM/IndexWriter combination.
 */
@SuppressWarnings("serial")
public class SingleIndexingServlet extends HttpServlet {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(SingleIndexingServlet.class.getName());
  
  /**
   * Handles a GET request.
   * <p/>
   * The default behavior is the execute the doPost method.
   * @param request the servlet request
   * @param response the servlet response
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    doPost(request,response);
  }
  
  /**
   * Handles a POST request.
   * @param request the servlet request
   * @param response the servlet response
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    RequestContext context = null;
    try {
      
      LOGGER.finer("Query string="+request.getQueryString());
      String sEncoding = request.getCharacterEncoding();
      if ((sEncoding == null) || (sEncoding.trim().length() == 0)) {
        request.setCharacterEncoding("UTF-8");
      }
      context = RequestContext.extract(request);
      String action = Val.chkStr(request.getParameter("action"));
      
      if (action.equalsIgnoreCase("isSynchronizerRunning")) {
        boolean isRunning = LuceneIndexSynchronizer.RUNNING;
        this.writeCharacterResponse(response,
            ""+isRunning,"UTF-8","text/plain; charset=UTF-8");
        return;
      }
      
      context.getObjectMap().put("lucene.useRemoteWriter",false);
      StringAttributeMap params = context.getCatalogConfiguration().getParameters();
      String param = Val.chkStr(params.getValue("lucene.useSingleSearcher"));
      boolean useSingleWriter = param.equalsIgnoreCase("true");
      param = Val.chkStr(params.getValue("lucene.useLocalWriter"));
      boolean bUseLocalWriter = !param.equalsIgnoreCase("false");
      
      param = Val.chkStr(params.getValue("lucene.useRemoteWriter"));
      boolean useRemoteWriter = param.equalsIgnoreCase("true");
      String remoteWriterUrl = Val.chkStr(params.getValue("lucene.remoteWriterUrl"));

      boolean bOk = true;
      if (!useSingleWriter || !bUseLocalWriter) {
        bOk = false;
        String s = "Inconsistent configuration parameters,"+
          " lucene.useSingleWriter lucene.useLocalWriter";
        LOGGER.severe(s);
        response.sendError(500,"Inconsistent configuration parameters on server.");
      } 
      
      if (bOk) {
        String sIds = Val.chkStr(request.getParameter("ids"));
        String[] ids = sIds.split(",");
        
        if (action.equalsIgnoreCase("delete")) {
          this.executeDelete(request,response,context,ids);
        
        } else if (action.equalsIgnoreCase("publish")) {
          this.executePublish(request,response,context,ids);
          
        } else if (action.equalsIgnoreCase("runSynchronizer")) {
          StringAttributeMap syncParams = new StringAttributeMap();
          //syncParams.set("feedbackSeconds","30");
          LuceneIndexSynchronizer lis = new LuceneIndexSynchronizer(syncParams);
          lis.syncronize();
          
        } else if (action.equalsIgnoreCase("touch")) {
          LuceneIndexAdapter adapter = new LuceneIndexAdapter(context);
          adapter.touch();

        } else {
          String s = "Unrecognized action: "+action;
          LOGGER.log(Level.WARNING,s);
        }
      }
      
    } catch (Throwable t) {
      String sErr = "Exception occured while processing servlet request.";
      LOGGER.log(Level.SEVERE,sErr,t);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      
    } finally {
      if (context != null) context.onExecutionPhaseCompleted();
    }
  }
  
  /**
   * Executes an index deletion request.
   * @param request the HTTP request
   * @param response the HTTP response
   * @param context the request context
   * @param ids the document ids to delete
   * @throws Exception if an exception occurs
   */
  protected void executeDelete(HttpServletRequest request, 
      HttpServletResponse response, RequestContext context, String[] ids)
    throws Exception {
    if ((ids == null || ids.length == 0)) return;
    
    LuceneIndexAdapter adapter = new LuceneIndexAdapter(context);
    IndexWriter writer = null;
    try {
      if (adapter.getUsingSingleWriter()) {
        writer = adapter.newWriter();
        adapter.setAutoCommitSingleWriter(false);
      }
      
      adapter.deleteDocuments(ids);
      
    } finally {
      try {
        if ((adapter != null) && (writer != null)) {
          adapter.closeWriter(writer);
        }
      } catch (Exception ef) {
        LOGGER.log(Level.WARNING,"IndexWriter failed to close.",ef);
      } 
    }
     
  }
  
  /**
   * Executes an index publication request.
   * @param request the HTTP request
   * @param response the HTTP response
   * @param context the request context
   * @param ids the document ids to publish
   * @throws Exception if an exception occurs
   */
  protected void executePublish(HttpServletRequest request, 
      HttpServletResponse response, RequestContext context, String[] ids)
    throws Exception {
    if ((ids == null || ids.length == 0)) return;
    LuceneIndexAdapter adapter = new LuceneIndexAdapter(context);
    IndexWriter writer = null;
    PreparedStatement st = null;
    try {
      
      if (adapter.getUsingSingleWriter()) {
        writer = adapter.newWriter();
        adapter.setAutoCommitSingleWriter(false);
      }
      
      ImsMetadataAdminDao dao = new ImsMetadataAdminDao(context);
      String resourceTable =  context.getCatalogConfiguration().getResourceTableName();
      String resourceDataTable = context.getCatalogConfiguration().getResourceDataTableName(); 
      StringSet delUuids = new StringSet();
      MetadataAcl acl = new MetadataAcl(context);
      boolean bCheckAcl = !acl.isPolicyUnrestricted();
      
      StringBuilder sbIds = new StringBuilder();
      for (String sUuid: ids) {
        if (sbIds.length() > 0) sbIds.append(",");
        sbIds.append("'").append(sUuid).append("'");
      }
      
      StringBuilder sb = new StringBuilder("SELECT");
      sb.append(" ").append(resourceTable).append(".DOCUUID");
      sb.append(",").append(resourceTable).append(".APPROVALSTATUS");
      sb.append(",").append(resourceTable).append(".PROTOCOL_TYPE");
      sb.append(",").append(resourceTable).append(".FINDABLE");
      sb.append(",").append(resourceTable).append(".UPDATEDATE");
      sb.append(",").append(resourceTable).append(".ACL");
      sb.append(" FROM ").append(resourceTable);
      sb.append(" WHERE DOCUUID IN (").append(sbIds.toString()).append(")");
      String sql = sb.toString();
      LOGGER.finest(sql);
      
      ManagedConnection mc = context.getConnectionBroker().returnConnection("");
      Connection con = mc.getJdbcConnection();
      IClobMutator mutator = mc.getClobMutator();
      
      st = con.prepareStatement(sql);
      ResultSet rs = st.executeQuery();
      if (Thread.interrupted()) return;
      while (rs.next()) {
        if (Thread.interrupted()) return;
        
        String uuid = rs.getString(1);
        String status = rs.getString(2);
        String protocolType = Val.chkStr(rs.getString(3));
        boolean findable = Val.chkBool(rs.getString(4),false);

        boolean bIndexable = (status != null) && 
          (status.equalsIgnoreCase("approved") || status.equalsIgnoreCase("reviewed"));
        if (bIndexable && protocolType.length()>0 && !findable) {
          bIndexable = false;
        }
        
        if (!bIndexable) {
          delUuids.add(uuid);
        } else {          
          Timestamp tsDbModified = rs.getTimestamp(5);
          String sDbAcl = null;
          if (bCheckAcl) {
            sDbAcl = rs.getString(6);
          }
          
          try {
            String sXml = Val.chkStr(dao.readXml(uuid));
            if (sXml.length() > 0) {
              MetadataDocument mdDoc = new MetadataDocument();
              Schema schema = mdDoc.prepareForView(context,sXml);
              adapter.publishDocument(uuid,tsDbModified,schema,sDbAcl);
            }
          } catch (SchemaException se) {
            
            // don't allow the entire process to fail over one bad xml
            String sMsg = "Error indexing document during the handling of a remote request";
            sMsg += ", uuid="+uuid+"\n"+Val.chkStr(se.getMessage());
            LOGGER.log(Level.WARNING,sMsg,se);
          }
        }
      }
      
      if (Thread.interrupted()) return;
      if (delUuids.size() > 0) {
        adapter.deleteDocuments(ids);
      }
    } finally {
      try {if (st != null) st.close();} catch (Exception ef) {}
      try {
        if ((adapter != null) && (writer != null)) {
          adapter.closeWriter(writer);
        }
      } catch (Exception ef) {
        LOGGER.log(Level.WARNING,"IndexWriter failed to close.",ef);
      }
    }
  }
  
  /**
   * Fully reads the characters from the request input stream.
   * @param request the HTTP servlet request
   * @return the characters read
   * @throws IOException if an exception occurs
   */
  protected String readInputCharacters(HttpServletRequest request)
    throws IOException {
    StringBuffer sb = new StringBuffer();
    InputStream is = null;
    InputStreamReader ir = null;
    BufferedReader br = null;
    try {
      //if (request.getContentLength() > 0) {
        char cbuf[] = new char[2048];
        int n = 0;
        int nLen = cbuf.length;
        String sEncoding = request.getCharacterEncoding();
        if ((sEncoding == null) || (sEncoding.trim().length() == 0)) {
          sEncoding = "UTF-8";
        }
        is = request.getInputStream();
        ir = new InputStreamReader(is,sEncoding);
        br = new BufferedReader(ir);
        while ((n = br.read(cbuf,0,nLen)) > 0) {
          sb.append(cbuf,0,n);
        }
      //}
    } finally {
      try {if (br != null) br.close();} catch (Exception ef) {}
      try {if (ir != null) ir.close();} catch (Exception ef) {}
      try {if (is != null) is.close();} catch (Exception ef) {}
    }
    return sb.toString();
  }
  
  /**
   * Writes characters to the response stream.
   * @param response the servlet response
   * @param content the content to write
   * @param charset the response character encoding 
   * @param contentType the response content type
   * @throws IOException if an IO exception occurs
   */
  private void writeCharacterResponse(HttpServletResponse response, 
                                      String content,
                                      String charset,
                                      String contentType) 
    throws IOException {
    PrintWriter writer = null;
    try {
      if (content.length() > 0) {
        response.setCharacterEncoding(charset);
        response.setContentType(contentType);
        writer = response.getWriter();
        writer.write(content);
        writer.flush();
      }
    } finally {
      try {
        if (writer != null) {
          writer.flush();
          writer.close();
        }
      } catch (Exception ef) {
        LOGGER.log(Level.SEVERE,"Error closing PrintWriter.",ef);
      }
    }
  }

}
