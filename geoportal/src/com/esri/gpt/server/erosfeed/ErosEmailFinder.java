/*
 * Copyright 2012 Esri.
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
package com.esri.gpt.server.erosfeed;

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.credentials.CredentialsDeniedException;
import com.esri.gpt.framework.security.credentials.UsernameCredential;
import com.esri.gpt.framework.security.identity.IdentityAdapter;
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.sql.ManagedConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 * Finds email of the record owner.
 * Uses identity adapter (typically: LDAP) to find email of the user who owns 
 * the record.
 */
public class ErosEmailFinder {
  private static final Logger LOGGER = Logger.getLogger(ErosEmailFinder.class.getCanonicalName());
  private HashMap<String, String> emailCache = new HashMap<String, String>();
  private RequestContext requestContext;
  private IdentityAdapter idAdapter;
  
  /**
   * Creates instance of the finder.
   * @param requestContext request context
   * @param idAdapter identity adapter
   */
  public ErosEmailFinder(RequestContext requestContext, IdentityAdapter idAdapter) {
    this.requestContext = requestContext;
    this.idAdapter = idAdapter;
  }

  /**
   * Finds email of the owner of the record.
   * @param recordUuid record UUID
   * @return email address of the owner or empty string if user has no email
   */
  public String findEmail(String recordUuid) {
    try {
      String owner = findRecordOwner(recordUuid);
      return !owner.isEmpty()? getUserEmail(owner): "";
    } catch (Exception ex) {
      LOGGER.log(Level.WARNING, "Error finding record owner email.", ex);
      return "";
    }
  }

  private String findRecordOwner(String recordUuid) throws SQLException {
    ManagedConnection managedConnection = requestContext.getConnectionBroker().returnConnection("");
    Connection connection = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    
    try {
      connection = managedConnection.getJdbcConnection();
      
      st = connection.prepareStatement("select OWNER from GPT_RESOURCE where DOCUUID=?");
      st.setString(1, recordUuid);
      
      rs = st.executeQuery();
      if (rs.next()) {
        int ownerId = rs.getInt(1);
        
        close(rs);
        close(st);
        
        st = connection.prepareStatement("select USERNAME from GPT_USER where USERID=?");
        st.setInt(1, ownerId);
        
        rs = st.executeQuery();
        
        if (rs.next()) {
          return rs.getString(1);
        }
      }
      
    } finally {
      close(rs);
      close(st);
      requestContext.getConnectionBroker().closeConnection(managedConnection);
    }
    return "";
  }
  
  private void close(ResultSet rs) {
    if (rs!=null) {
      try {
        rs.close();
      } catch (SQLException ex) {};
    }
  }
  
  private void close(Statement st) {
    if (st!=null) {
      try {
        st.close();
      } catch (SQLException ex) {};
    }
  }
  
  private String getUserEmail(String userName) throws SQLException, IdentityException, CredentialsDeniedException, NamingException {
    if (emailCache.containsKey(userName)) {
      return emailCache.get(userName);
    } else {
      UsernameCredential unCredential = new UsernameCredential(userName);
      User user = new User();
      user.setCredentials(unCredential);
      idAdapter.authenticate(user);
      idAdapter.readUserProfile(user);

      String emailAddress = user.getProfile().getEmailAddress();

      emailCache.put(userName, emailAddress);

      return emailAddress;
    }
  }
  
}
