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
package gc.base.sql;
import gc.base.task.TaskContext;
import gc.base.task.TaskStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * An SQL query executor.
 */
public class SqlQuery {

	/** Default constructor. */
	public SqlQuery() {}
	
	/**
	 * Applies a list of bindings to a prepared statement.
	 * @param st the statement
	 * @param bindings the bindings
	 * @throws SQLException if an exception occurs
	 */
	protected void applyQueryBindings(PreparedStatement st, List<Object> bindings) 
			throws SQLException {
		if ((bindings != null) && (bindings.size() > 0)) {
			int nIdx = 0;
			for (Object binding: bindings) {
				nIdx++;
				st.setObject(nIdx,binding);
			}
		}
	}
	
	/**
	 * Ensures that the table name for a query has been set.
	 * @param context the task context
	 * @param info the query information
	 */
	protected void ensureTableName(TaskContext context, SqlQInfo info) {
		String table = info.getTableName();
		if (table == null) {
			String suffix = info.getTableSuffix();
			if (suffix != null) {
				// TODO use configuration
				info.setTableName("GPT_"+suffix);
			}
		}
	}
	
	/**
	 * Prepares and executes a database query.
	 * @param context the task context
	 * @param con the database connection
	 * @param info the query information
	 * @param handler the row handler
	 * @throws Exception if an exception occurs
	 */
	public void query(TaskContext context, Connection con, 
			SqlQInfo info, SqlRowHandler handler) throws Exception {
		boolean closeCon = (con == null);
		PreparedStatement st = null;
		try {
			TaskStats stats = context.getStats();
			String tn = context.getTaskName()+".SqlQuery";
			long rowNum = 0;
			ensureTableName(context,info);
  	  String sql = makeSql(info);
  	  
  	  if (con == null) {
  	  	/*
	  	  long tc1 = System.currentTimeMillis();
	  	  SqlConnectionBroker conBroker = new SqlConnectionBroker();
	  	  con = conBroker.getConnection(context);
	  	  long tc2 = System.currentTimeMillis();
	  	  stats.incrementTime(tn+".getConnection",tc2-tc1);
	  	  */
  	  	throw new Exception("Null SQL connection.");
  	  }
  	  
  	  long t1 = System.currentTimeMillis();
  	  st = con.prepareStatement(sql);
  	  List<Object> bindings = info.getQueryBindings();
  	  if ((bindings != null) && (bindings.size() > 0)) {
  	  	applyQueryBindings(st,bindings);
  	  }
      if (info.getMaxRows() > 0) st.setMaxRows(info.getMaxRows());
      if (info.getFetchSize() > 0) st.setFetchSize(info.getFetchSize());
      ResultSet rs = st.executeQuery();
      long t2 = System.currentTimeMillis();
      stats.incrementTime(tn+".executeQuery",t2-t1);
      while (rs.next()) {
      	if (Thread.interrupted()) {
      		throw new InterruptedException("Interrupted while iterating ResultSet.");
      	}
      	rowNum++;
      	handler.handleSqlRow(context,con,rs,rowNum);
      }
      long t3 = System.currentTimeMillis();
      stats.incrementTime(tn+".iterate",t3-t2);
		} finally {
      try {if (st != null) st.close();} 
      catch (Exception ef) {ef.printStackTrace();}
      try {if (closeCon && (con != null)) con.close();} 
      catch (Exception ef) {ef.printStackTrace();}
		}
	}
	
	/**
	 * Builds the SQL query string.
	 * @param info the query information
	 * @return the query string
	 */
	protected String makeSql(SqlQInfo info) {
		StringBuilder sbSql = new StringBuilder();
		String table = info.getTableName();
		String[] fields = info.getFields();
		String where = info.getWhere();
		String orderBy = info.getOrderBy();
		
		StringBuilder sbFields = new StringBuilder();
		if ((fields != null) && (fields.length > 0)) {
			for (String f: fields) {
				if (sbFields.length() > 0) sbFields.append(",");
				sbFields.append(f);
			}
		}
		if (sbFields.length() == 0) sbFields.append("*");
		
	  sbSql.append(" SELECT ").append(sbFields.toString());
	  sbSql.append(" FROM ").append(table);
	  if ((where != null) && (where.length() > 0)) {
	  	if (!where.toLowerCase().startsWith("where")) {
	  	  sbSql.append(" WHERE ").append(where);
	  	}
	  }
	  if ((orderBy != null) && (orderBy.length() > 0)) {
	  	if (!orderBy.toLowerCase().startsWith("order by")) {
	  	  sbSql.append(" ORDER BY ").append(orderBy);
	  	}
	  }
	  return sbSql.toString();
	}
}
