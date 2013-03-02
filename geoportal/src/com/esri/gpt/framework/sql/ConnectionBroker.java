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
import java.sql.SQLException;

import javax.naming.NamingException;

import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.util.Val;

/**
 * Primary interface for accessing JDBC connection resources.
 * <p>
 * This class is intended for use by a single request execution thread.
 */
public final class ConnectionBroker {

// class variables =============================================================

// instance variables ==========================================================
private DatabaseReferences _dbReferences = null;
private final ManagedConnections _connections  = new ManagedConnections();

// constructors ================================================================

/** Default constructor. */
public ConnectionBroker() {
  this(ApplicationContext.getInstance().getConfiguration().getDatabaseReferences());
}

/**
 * Construct based upon a configured collection of DatabaseReferences.
 * @param databaseReferences the DatabaseReferences associated with the configuration
 */
public ConnectionBroker(DatabaseReferences databaseReferences) {
  if (databaseReferences == null) {
    throw new IllegalArgumentException("databaseReferences cannot be null");
  } else {
    _dbReferences = databaseReferences;
  }
}

// properties ==================================================================

// methods =====================================================================

/**
 * Closes all ManagedConnections.
 * <br/>The underlying JDBC connections are closed.
 */
public void closeAll() {
  _connections.closeAll();
}

/**
 * Closes a ManagedConnection.
 * <br/>The underlying JDBC connection is closed.
 * @param managedConnection the ManagedConnection to close
 */
public void closeConnection(ManagedConnection managedConnection) {
  _connections.closeConnection(managedConnection);
}

/**
 * Returns the ManagedConnection associated with a supplied connection tag.
 * <p>
 * If a ManagedConnection has already been established for the tag,
 * it will be returned.
 * Otherwise an new ManagedConnection will be established based upon the
 * underlying DatabaseReference associated with the supplied tag.
 * @param referenceTag the tag associated with the database reference (can be null for default connection)
 * @return the associated ManagedConnection
 * @throws SQLException if an SQL exception occurs while establishing the connection
 */
public ManagedConnection returnConnection(String referenceTag)
  throws SQLException {
  ManagedConnection mc = null;
  referenceTag = Val.chkStr(referenceTag);
  DatabaseReference reference = _dbReferences.findByTag(referenceTag);
  if (reference != null) {
    try {
      mc = _connections.returnConnection(reference);
    } catch (ClassNotFoundException e) {
      String sMsg = "Unable to open connection for driver: "+
                    reference.getDirectDriverClassName()+
                    " (connectionTag="+referenceTag+")";
      throw new ConfigurationException(sMsg,e);
    } catch (NamingException e) {
      String sMsg = "Unable to open connection for jndiName: "+
                    reference.getJndiName()+
                    " (connectionTag="+referenceTag+")";
      throw new ConfigurationException(sMsg,e);
    }
  } else {
    String sMsg = "A databaseReference has not been configured for connectionTag: "+
                  referenceTag;
    throw new ConfigurationException(sMsg);
  }
  return mc;
}

}
