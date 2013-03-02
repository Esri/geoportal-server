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
import java.util.HashMap;
import java.util.Iterator;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.NamingException;

/**
 * ManagedConnection collection.
 * <p>
 * The collection map is keyed on the name of the DatabaseReference associated
 * with the connection (one connection per name).
 * <p>
 * This class is intended for use by a single execution thread.
 */
public final class ManagedConnections {

// class variables =============================================================

// instance variables ==========================================================
private HashMap<String,ManagedConnection> _hmConnections;

// constructors ================================================================

/** Default constructor. */
protected ManagedConnections() {
  _hmConnections = new HashMap<String,ManagedConnection>();
}

// properties ==================================================================

// methods =====================================================================

/**
 * Closes all ManagedConnections.
 * <br/>The underlying JDBC connections are closed.
 */
protected void closeAll() {
  Iterator<ManagedConnection> it = _hmConnections.values().iterator();
  while (it.hasNext()) {
    it.next().close();
  }
  _hmConnections.clear();
}

/**
 * Closed a ManagedConnection.
 * <br/>The underlying JDBC connection is closed.
 * @param managedConnection the ManagedConnection to close
 */
protected void closeConnection(ManagedConnection managedConnection) {
  if (managedConnection != null) {
    managedConnection.close();
    _hmConnections.remove(managedConnection.getDatabaseReference().getReferenceName());
  }
}

/**
 * Executes a commit for each ManagedConnection.
 * @throws SQLException if an exception occurs
 */
protected void commitAll() throws SQLException {
  Iterator<ManagedConnection> it = _hmConnections.values().iterator();
  while (it.hasNext()) {
    it.next().commit();
  }
  _hmConnections.clear();
}

/**
 * Finds the ManagedConnection associated with a JDBC connection.
 * @param con the subject JDBC connection
 * @return the associated ManagedConnection (null if none)
 */
private ManagedConnection findByJdbcConnection(Connection con) {
  ManagedConnection mc, mcFound = null;
  if (con != null) {
    Iterator<ManagedConnection> it = _hmConnections.values().iterator();
    while (it.hasNext()) {
      mc = it.next();
      if (mc.isJdbcConnection(con)) {
        mcFound = mc;
        break;
      }
    }
  }
  return mcFound;
}

/**
 * Finds the ManagedConnection associated with a DatabaseReference name.
 * @param referenceName the DatabaseReference name associated with the
 *        ManagedConnection to find
 * @return the associated ManagedConnection (null if none)
 */
protected ManagedConnection findByReferenceName(String referenceName) {
  return _hmConnections.get(referenceName);
}

/**
 * Returns the ManagedConnection associated with a supplied DatabaseReference.
 * <p>
 * If a ManagedConnection has already been established for the reference,
 * it will be returned.
 * Otherwise an new ManagedConnection will be established based upon the
 * supplied DatabaseReference.
 * @param databaseReference the associated DatabaseReference
 * @return the ManagedConnection
 * @throws ClassNotFoundException if the database driver class was not found
 * @throws NamingException if a JNDI naming exception occurs
 * @throws SQLException if an SQL exception occurs while establishing the connection
 */
protected ManagedConnection returnConnection(DatabaseReference databaseReference)
  throws ClassNotFoundException, NamingException, SQLException {
  ManagedConnection mc = findByReferenceName(databaseReference.getReferenceName());
  if (mc == null) {
    mc = new ManagedConnection(databaseReference);
    _hmConnections.put(databaseReference.getReferenceName(),mc);
  }
  return mc;
}

/**
 * Executes a rollback for each ManagedConnection.
 */
protected void rollbackAll() {
  Iterator<ManagedConnection> it = _hmConnections.values().iterator();
  while (it.hasNext()) {
    it.next().rollback();
  }
  _hmConnections.clear();
}

}
