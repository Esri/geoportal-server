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
package com.esri.gpt.framework.sql;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;

/**
 * Super-class for a database access object.
 */
public class BaseDao {
  
// class variables =============================================================

// instance variables ==========================================================
private RequestContext _requestContext;

// constructors ================================================================

/** Default constructor. */
protected BaseDao() {}

/**
 * Constructs with an associated request context.
 * @param requestContext the request context
 */
public BaseDao(RequestContext requestContext) {
  setRequestContext(requestContext);
}

// properties ==================================================================

/**
 * Gets the application configuration.
 * @return the application configuration
 */
public ApplicationConfiguration getApplicationConfiguration() {
  return _requestContext.getApplicationContext().getConfiguration();
}

/**
 * Gets the associated request context.
 * @return the request context
 */
public RequestContext getRequestContext() {
  return _requestContext;
}
/**
 * Sets the associated request context.
 * @param requestContext the request context
 */
public void setRequestContext(RequestContext requestContext) {
  _requestContext = requestContext;
}
  
// methods =====================================================================

/**
 * Appends an expression to an SQL where clause.
 * @param whereClause the where clause
 * @param expression the expression
 */
protected void appendExpression(StringBuffer whereClause, 
                                String expression) {
  if (whereClause.length() > 0) {
    whereClause.append(" AND ");
  }
  whereClause.append("(").append(expression).append(")");
}

/**
 * Appends a value filter to an SQL where clause.
 * <p>
 * This is intended for use within a PreparedStatement. The appended
 * filter will have the following forms:
 * <br/>field = ?
 * <br/>field LIKE ?
 * <p>
 * The value returned should be bound within the PreparedStatement:
 * <br/>preparedStatement.setString(n,value)
 * @param whereClause the where clause
 * @param field the field
 * @param value the field value
 * @param forceLike if true force an SQL LIKE
 * @return the value - possible modified for LIKE (ie. "%"+value+"%")
 */
protected String appendValueFilter(StringBuffer whereClause,
                                   String field,
                                   String value,
                                   boolean forceLike) {
  value = value.replaceAll("\\*","%");
  String sExpression = "";
  if (value.indexOf("%") != -1) {
    sExpression = field+" LIKE ?";
  } else if (forceLike) {
    value = "%"+value+"%";
    sExpression = field+" LIKE ?";
  } else {
    sExpression = field+" = ?";
  }
  appendExpression(whereClause,sExpression);
  return value;
}

/**
 * Closes a statement.
 * @param st the JDBC Statement to close
 */
public static void closeStatement(Statement st) {
  if (st != null) {
    try {
      st.close();
    } catch (Throwable t) {
      LogUtil.getLogger().log(Level.SEVERE,"Error closing statement.",t);
    }
  }
} 

/**
 * Closes result set.
 * @param rs result set to close
 */
public static void closeResultSet(ResultSet rs) {
  if (rs!=null) {
    try {
      rs.close();
    } catch (Throwable t) {
      LogUtil.getLogger().log(Level.SEVERE,"Error closing result set.",t);
    }
  }
}

/**
 * Determine if the database is case sensitive.
 * @param context the active request context
 * @return true if the database is case sensitive
 */
public static boolean getIsDbCaseSensitive(RequestContext context) {
  if (context == null) return true;
  StringAttributeMap params  = context.getCatalogConfiguration().getParameters();
  String s = Val.chkStr(params.getValue("database.isCaseSensitive"));
  return !s.equalsIgnoreCase("false");
}

/**
 * Logs a SQL expression.
 * @param expression the expression to log
 */
protected void logExpression(String expression) {
  if (expression != null) {
    LogUtil.getLogger().finer(expression);
  }
}

/**
 * Returns a managed connection to the default database.
 * @throws SQLException if an exception occurs while establishing 
 *         the connection
 */
protected ManagedConnection returnConnection() 
  throws SQLException {
  return getRequestContext().getConnectionBroker().returnConnection("");
}

}
