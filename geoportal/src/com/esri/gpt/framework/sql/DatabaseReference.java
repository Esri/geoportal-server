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
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;

import java.util.logging.Level;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Configuration reference for a database.
 * <p>
 * The reference usually represents a JNDI based connection.
 * It can also represent a direct connection (driver/url/user/pwd), but this
 * is usually limited to a non-production environment.
 */
public final class DatabaseReference {

// class variables =============================================================

// instance variables ==========================================================
private String        _directDriver   = "";
private String        _directPassword = "";
private String        _directUrl      = "";
private String        _directUsername = "";
private boolean       _isJndiBased    = true;
private DataSource    _jndiDataSource = null;
private String        _jndiName       = "";
private String        _refName        = "";
private StringSet     _tags           = new StringSet();

// constructors ================================================================

/** Default constructor. */
public DatabaseReference() {}

// properties ==================================================================

/**
 * Gets the driver class name for a direct database reference.
 * @return the database driver class name
 */
public String getDirectDriverClassName() {
  return _directDriver;
}
/**
 * Sets the driver class name for a direct database reference.
 * @param driver the database driver class name
 */
public void setDirectDriverClassName(String driver) {
  _directDriver = Val.chkStr(driver);
}

/**
 * Gets the password for a direct database reference.
 * @return the password
 */
public String getDirectPassword() {
  return _directPassword;
}
/**
 * Sets the password for a direct database reference.
 * @param password the password
 */
public void setDirectPassword(String password) {
  _directPassword = password;
}

/**
 * Gets the URL for a direct database reference.
 * @return the database url
 */
public String getDirectUrl() {
  return _directUrl;
}
/**
 * Sets the URL for a direct database reference.
 * @param url the database url
 */
public void setDirectUrl(String url) {
  _directUrl = Val.chkStr(url);
}

/**
 * Gets the username for a direct database reference.
 * @return the username
 */
public String getDirectUsername() {
  return _directUsername;
}
/**
 * Sets the username for a direct database reference.
 * @param username the username
 */
public void setDirectUsername(String username) {
  _directUsername = Val.chkStr(username);
}

/**
 * Gets the JNDI based reference status.
 * @return <b>true</b> if this reference is JNDI based
 */
public boolean getIsJndiBased() {
  return _isJndiBased;
}
/**
 * Sets the JNDI based reference status.
 * @param isJndiBased <b>true</b> if this reference is JNDI based
 */
public void setIsJndiBased(boolean isJndiBased) {
  _isJndiBased = isJndiBased;
}

/**
 * Gets the JNDI based DataSource for the reference.
 * @return the data source
 * @throws NamingException if a JNDI naming exception occurs
 */
protected DataSource getJndiDataSource()
  throws NamingException {
  if (_jndiDataSource == null) {
    _jndiDataSource = openJndiDataSource(getJndiName());
  }
  return _jndiDataSource;
}

/**
 * Gets the name for a JNDI based database reference.
 * @return the JNDI name
 */
public String getJndiName() {
  return _jndiName;
}
/**
 * Sets the name for a JNDI based database reference.
 * @param jndiName the JNDI name
 */
public void setJndiName(String jndiName) {
  _jndiName = Val.chkStr(jndiName);
}

/**
 * Gets the label for the database reference.
 * @return the reference label
 */
protected String getReferenceLabel() {
  if (getIsJndiBased()) {
    return getReferenceName()+", jndiName: "+getJndiName();
  } else {
    return getReferenceName()+", url: "+getDirectUrl();
  }
}

/**
 * Gets the name for the database reference.
 * @return the name
 */
public String getReferenceName() {
  return _refName;
}
/**
 * Sets the name for the database reference.
 * @param referenceName the reference name
 */
public void setReferenceName(String referenceName) {
  _refName = Val.chkStr(referenceName);
}

/**
 * Gets the tags associated with the reference.
 * @return the associated tags
 */
public StringSet getTags() {
  return _tags;
}

// methods =====================================================================

/**
 * Closes a JDBC connection.
 * <br/>Executes the close() method against the JDBC connection.
 * @param con the connection to close
 */
public void closeConnection(Connection con) {
  if (con != null) {
    try {
      if (!con.isClosed()) {
        LogUtil.getLogger().fine("Closing connection for "+getReferenceLabel());
        con.close();
      }
    } catch (Exception e) {
      String sMsg = "Unable to close connection for "+getReferenceLabel();
      LogUtil.getLogger().log(Level.SEVERE,sMsg,e);
    }
  }
}

/**
 * Makes a JDBC connection from based upon the configuration of the database reference.
 * @return the JDBC connection
 * @throws ClassNotFoundException if the database driver class was not found
 * @throws NamingException if a JNDI naming exception occurs
 * @throws SQLException if an SQL exception occurs while establishing the connection
 */
public Connection openConnection()
  throws ClassNotFoundException, NamingException, SQLException {
  Connection con = null;
  String sMsg;

  // obtain the connection
  sMsg = "Opening connection for "+getReferenceLabel();
  LogUtil.getLogger().finer(sMsg);
  if (getIsJndiBased()) {
    con = openJndiConnection();
  } else {
    con = openDirectConnection();
  }
  if (con != null) {
    con.setAutoCommit(true);
  }

  // log information about the connection obtained
  try {
    //System.err.println(con.getMetaData().getDatabaseMajorVersion());
    //System.err.println(con.getMetaData().getDatabaseMinorVersion());
    //System.err.println(con.getMetaData().supportsGetGeneratedKeys());
    sMsg  = "Connection opened for "+getReferenceLabel();
    sMsg += ", productName: "+con.getMetaData().getDatabaseProductName();
    sMsg += ", driver: "+con.getMetaData().getDriverName();
    sMsg += " "+con.getMetaData().getDriverVersion();
    LogUtil.getLogger().fine(sMsg);
  } catch (Exception e2) {
    LogUtil.getLogger().log(Level.WARNING,"Unable to determine connection metaData",e2);
  }
  return con;
}

/**
 * Opens a direct JDBC connection based upon the driver, url, username and password.
 * @return the JDBC connection
 * @throws ClassNotFoundException if the database driver class was not found
 * @throws SQLException if an SQL exception occurs while establishing the connection
 */
protected Connection openDirectConnection()
  throws ClassNotFoundException, SQLException {
  Class.forName(getDirectDriverClassName());
  return DriverManager.getConnection(
         getDirectUrl(),getDirectUsername(),getDirectPassword());
}

/**
 * Opens a JDBC connection from the JNDI DataSource associated with the JNDI name.
 * @return the JDBC connection
 * @throws NamingException if a JNDI naming exception occurs
 * @throws SQLException if an SQL exception occurs while establishing the connection
 */
protected Connection openJndiConnection()
  throws NamingException, SQLException {
  DataSource dataSource = getJndiDataSource();
  return dataSource.getConnection();
}

/**
 * Opens the DataSource associated with the JNDI name.
 * @param jndiName the associated JNDI name
 * @return the associated data source
 * @throws NamingException if a JNDI naming exception occurs
 */
protected DataSource openJndiDataSource(String jndiName)
  throws NamingException {
  DataSource dataSource = null;
  InitialContext initContext = new InitialContext();
  try {
    dataSource = (DataSource)initContext.lookup(jndiName);
  } catch (NamingException e1) {
    try {
      dataSource = (DataSource)initContext.lookup("java:comp/env/"+jndiName);
    } catch (NamingException e2) {
      Context envContext = (Context)initContext.lookup("java:comp/env");
      dataSource = (DataSource)envContext.lookup(jndiName);
    }
  }
  return dataSource;
}

/**
 * Tests the connection associated with the database reference.
 * <br/>A connection is opened, then immediately closed.
 */
protected void testConnection() {
  LogUtil.getLogger().finer("Testing connection for "+getReferenceLabel());
  Connection con = null;
  try {
    con = openConnection();
  } catch (Throwable t) {
    LogUtil.getLogger().log(Level.SEVERE,"Connection test failed.",t);
  } finally {
    closeConnection(con);
  }
}

/**
 * Returns a string representation of this object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer();
  sb.append(getClass().getName()).append(" (\n");
  sb.append(" name=").append(getReferenceName()).append("\n");
  if (getIsJndiBased()) {
    sb.append(" jndiName=").append(getJndiName()).append("\n");
  } else {
    sb.append(" driver=").append(getDirectDriverClassName()).append("\n");
    sb.append(" url=").append(getDirectUrl()).append("\n");
    sb.append(" username=").append(getDirectUsername()).append("\n");
    sb.append(" password=").append(getDirectPassword()).append("\n");
  }
  sb.append(" referenceTags=").append(getTags()).append("\n");
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}

}

