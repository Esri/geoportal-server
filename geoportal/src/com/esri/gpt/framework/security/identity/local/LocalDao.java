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
package com.esri.gpt.framework.security.identity.local;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.sql.BaseDao;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Super-class for a database access objects associated with local
 * identity tables.
 */
public class LocalDao extends BaseDao {
  
// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
protected LocalDao() {
  super();
}

/**
 * Constructs with an associated request context.
 * @param requestContext the request context
 */
public LocalDao(RequestContext requestContext) {
  super(requestContext);
}

// properties ==================================================================

/**
 * Gets the name of the user table.
 * @return the table name
 */
protected String getUserTableName() {
  return getRequestContext().getCatalogConfiguration().getUserTableName();
}

// methods =====================================================================

/**
 * Determines if a username already exists.
 * @param con the JDBC connection
 * @param username the username to check
 * @return true if a user by this name already exists
 * @throws SQLException if a database exception occurs
 */
private boolean doesUsernameExist(Connection con, String username)
  throws SQLException {
  boolean bExists = false;
  PreparedStatement st = null;
  try {
    StringBuffer sbSql = new StringBuffer();
    sbSql.append("SELECT USERNAME FROM ").append(getUserTableName());
    if (getIsDbCaseSensitive(this.getRequestContext())) {
      sbSql.append(" WHERE UPPER(USERNAME)=?");
    } else {
      sbSql.append(" WHERE USERNAME=?");
    }
    logExpression(sbSql.toString());
    st = con.prepareStatement(sbSql.toString());
    st.setString(1,username.toUpperCase());
    ResultSet rs = st.executeQuery();
    if (rs.next()) {
      bExists = true;
    }
  } finally {
    closeStatement(st);
  }
  return bExists;
}

/**
 * Ensures a local reference to a remote user.
 * @param user the user (if user id less than zero, a record will be created)
 * @throws IdentityException if an integrity violation occurs
 * @throws SQLException if a database exception occurs
 */
public void ensureReferenceToRemoteUser(User user)
  throws IdentityException, SQLException {
  
  // establish the connection
  ManagedConnection mc = returnConnection();
  Connection con = mc.getJdbcConnection();
  
  // if the local id has not been set,
  //   query for an existing reference,
  //   if none was found
  //     insert a new reference
  int nUserId = user.getLocalID();
  if (nUserId < 0) {
    nUserId = readUserByDN(con,user);
    if (nUserId < 0) {
      String sDn = user.getDistinguishedName();
      String sUsername = user.getName();
      if ((sUsername.length() == 0) ||
          (sUsername.length() > 64) ||
           doesUsernameExist(con,sUsername)) {
        String sMsg = "A valid userid was not auto-generated for remote user: "+sDn;
        if (sUsername.length() == 0) {
          sMsg += " The identity store username is empty.";
        } else if (sUsername.length() > 64) {
          sMsg += " The username is greater than 64 characters.";
        } else {
          sMsg += " This username already exists with a different DN.";
        }
        throw new IdentityException(sMsg);
      }
      insertUser(con,sDn,sUsername);
      nUserId = readUserByDN(con,user);
      if (nUserId < 0) {
        String sMsg = "A valid userid was not auto-generated for remote user: "+sDn;
        throw new IdentityException(sMsg);
      }
    }
  }
}

/**
 * Inserts a new user.
 * @param con the JDBC connection
 * @param dn the distinguished name
 * @param username the username
 * @param password the password
 * @throws SQLException if a database exception occurs
 */
private void insertUser(Connection con, String dn, String username)
  throws SQLException {
  PreparedStatement st = null;
  try {
    String sSql = "INSERT INTO "+getUserTableName()+"(DN,USERNAME) VALUES(?,?)";
    logExpression(sSql);
    st = con.prepareStatement(sSql);
    st.setString(1,dn);
    st.setString(2,username);
    st.execute();
  } finally {
    closeStatement(st);
  }
}

/**
 * Reads the distinguished name associated with a user id.
 * @param userId the subject user id
 * @return the associated distinguished name (empty if none)
 * @throws SQLException if a database exception occurs
 */
public String readDN(int userId) throws SQLException {
  return readValueByUserId("DN",userId);
}

/**
 * Reads the local reference information for a user based upon
 * the user's distinguished name.
 * @param con the JDBC connection
 * @param user user with distinguished name to check
 * @return the local ID reference, -1 if none
 * @throws IdentityException if an integrity violation occurs
 * @throws SQLException if a database exception occurs
 */
private int readUserByDN(Connection con, User user)
  throws IdentityException, SQLException {
  int nUserId = -1;
  String sUsername = "";
  PreparedStatement st = null;
  try {
    
    // ensure that a distinguished name was supplied
    if (user.getDistinguishedName().length() == 0) {
      throw new IdentityException("Empty DN");
    }
    
    // query for the distinguished name reference within the local users table 
    String sSql = null;
    if (getIsDbCaseSensitive(this.getRequestContext())) {
      sSql = "SELECT USERID,USERNAME FROM "+getUserTableName()+" WHERE UPPER(DN)=?";
    } else {
      sSql = "SELECT USERID,USERNAME FROM "+getUserTableName()+" WHERE DN=?";
    }

    logExpression(sSql);
    st = con.prepareStatement(sSql);
    st.setString(1,user.getDistinguishedName().toUpperCase());
    ResultSet rs = st.executeQuery();
    int nCount = 0;
    while (rs.next()) {
      nUserId = rs.getInt(1);
      sUsername = Val.chkStr(rs.getString(2));
      
      // throw an exception if multiple users with the same 
      // distinguished name exist
      nCount++;
      if (nCount > 1) {
        String sMsg = "Integrity violation within local user table: "+
                      "multiple references to same DN";
        throw new IdentityException(sMsg);
      }
    }
  } finally {
    closeStatement(st);
  }
  if (nUserId >= 0) {
    user.setLocalID(nUserId);
    user.setName(sUsername);
  }
  return nUserId;
}

/**
 * Reads the username associated with a user id.
 * @param userId the subject user id
 * @return the associated username (empty if none)
 * @throws SQLException if a database exception occurs
 */
public String readUsername(int userId) throws SQLException {
  return readValueByUserId("USERNAME",userId);
}

/**
 * Reads a field value associated with a user id.
 * @param field the field name
 * @param userId the subject user id
 * @return the associated value (empty if none)
 * @throws SQLException if a database exception occurs
 */
private String readValueByUserId(String field, int userId)
  throws SQLException {
  String sValue = "";
  ManagedConnection mc = returnConnection();
  Connection con = mc.getJdbcConnection();
  PreparedStatement st = null;
  try {    
    String sSql = "SELECT "+field+" FROM "+getUserTableName()+" WHERE USERID=?";
    logExpression(sSql);
    st = con.prepareStatement(sSql);
    st.setInt(1,userId);
    ResultSet rs = st.executeQuery();
    if (rs.next()) {
      sValue = Val.chkStr(rs.getString(1));
    }
  } finally {
    closeStatement(st);
  }
  return sValue;
}

}
