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
package com.esri.gpt.control.rest.repositories;
import com.esri.gpt.control.rest.writer.JsonResultSetWriter;
import com.esri.gpt.control.rest.writer.ResultSetWriter;
import com.esri.gpt.control.rest.writer.XmlResultSetWriter;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.sql.BaseDao;
import com.esri.gpt.framework.sql.HttpExpressionBinder;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.Val;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides a Rest API for repositories registered within the GPT harvesting tables.
 * <p/>
 * This end point does not query remote reposities, it provides information about 
 * what has been registered.
 * <p/>
 * Usage: <i>http://host:port/context</i><b>/rest/repositories?f=</b>
 * <br/><b>f</b> - the response format, json or xml (optional, default=json)
 */
public class RepositoriesServlet extends BaseServlet {
  
  /**
   * Processes the HTTP request.
   * @param request the HTTP request.
   * @param response HTTP response.
   * @param context request context
   * @throws Exception if an exception occurs
   */
  @Override
  protected void execute(HttpServletRequest request, 
                         HttpServletResponse response, 
                         RequestContext context)
    throws Exception {
    
    handleSites(request,response,context);
   
  }
  
  /**
   * Handles a rest based query against registered repository sites. 
   * @param request the HTTP request
   * @param response HTTP response
   * @param context request context
   * @throws Exception if an exception occurs
   */
  private void handleSites(HttpServletRequest request, 
                           HttpServletResponse response, 
                           RequestContext context)
    throws Exception {
        
    
    String protocol = Val.chkStr(request.getParameter("protocol"));
    // initilize the writer based upon the requested format
    ResultSetWriter writer = null;
    String format = Val.chkStr(request.getParameter("f"));
    String callback = Val.chkStr(request.getParameter("callback"));
    PrintWriter responseWriter = response.getWriter();
    if (format.equalsIgnoreCase("xml")) {
      response.setContentType("text/xml; charset=UTF-8");
      writer = new XmlResultSetWriter(responseWriter);
    } else {
      response.setContentType("text/plain; charset=UTF-8");
      writer = new JsonResultSetWriter(responseWriter);
    }
    if (!callback.isEmpty()) {
      responseWriter.print(callback+"({");
      responseWriter.flush();
    }
    writer.begin(response);
    ManagedConnection mCon = null;
    Connection con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try { 
      
      // initialize the query string
      String table = context.getCatalogConfiguration().getResourceTableName();
      StringBuffer sql = new StringBuffer(); 
      
      String[] columnTags = {"id","uuid","protocol","name","url"};
      sql.append("SELECT ID,DOCUUID,PROTOCOL_TYPE,TITLE,HOST_URL FROM "+table);
      
      // build the bound query expression based upon HTTP parameter input
      HttpExpressionBinder binder = new HttpExpressionBinder(request);
      binder.parse("id","ID","=",",",HttpExpressionBinder.PARAMETERTYPE_INTEGER);
      binder.parse("uuid","DOCUUID",",",false,false);
      
      if(protocol.toLowerCase().equals("all")) {
        binder.parse("","PROTOCOL_TYPE",",",true,false);
      } else {
        binder.parse("protocol","PROTOCOL_TYPE",",",true,false);
      }
      binder.parse("name","TITLE",null,true,true);
      binder.parse("url","HOST_URL",null,true,true);
            
      // append the bound where clause,
      // create the prepared statement and apply bindings,
      // exexute the query and write the response
      sql.append(" ").append(binder.getExpression(true));
      if(sql.toString().toLowerCase().contains(" where ")) {
        sql.append(" AND ");
      } else {
        sql.append(" WHERE ");
      }
      sql.append(" ((APPROVALSTATUS = 'approved') ")
      .append(" OR  (APPROVALSTATUS = 'reviewed')) ");
      sql.append(" AND SEARCHABLE = 'true'");
      sql.append(" ORDER BY UPPER(TITLE) ASC");
      
      mCon = context.getConnectionBroker().returnConnection("");
      con = mCon.getJdbcConnection();
      st = con.prepareStatement(sql.toString());
      binder.applyBindings(st,1);
      //rs = new RepositoriesResultSetWrapper(st.executeQuery());
      if(protocol.toLowerCase().equals("all") != true) {
        rs = st.executeQuery();
      } else {
        rs = new RepositoriesResultSetWrapper(st.executeQuery());
      }
      writer.writeResultSet(rs,0,columnTags);  
      
    } finally {
      writer.flush();
      if (!callback.isEmpty()) {
        responseWriter.print("})");
        responseWriter.flush();
      }
      BaseDao.closeResultSet(rs);
      BaseDao.closeStatement(st);
      context.getConnectionBroker().closeConnection(mCon);
      writer.close();
    }
    
   
  }

}
