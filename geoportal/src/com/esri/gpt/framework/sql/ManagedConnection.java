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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.naming.NamingException;

/**
 * Acts as a wrapper for a JDBC connection.
 * <p>
 * This class is intended for use by a single execution thread.
 */
public final class ManagedConnection {

// class variables =============================================================

// instance variables ==========================================================
private DatabaseReference  _databaseReference  = null;
private Connection         _jdbcConnection     = null;
private TransactionCounter _transactionCounter = new TransactionCounter();

/**
 * Construct based upon a supplied DatabaseReference.
 * <p>
 * During construction, a JDBC connection is established based upon
 * the associated with a DatabaseReference.
 * @param databaseReference the associated DatabaseReference
 * @throws ClassNotFoundException if the database driver class was not found
 * @throws NamingException if a JNDI naming exception occurs
 * @throws SQLException if an SQL exception occurs while establishing the connection
 */
protected ManagedConnection(DatabaseReference databaseReference)
  throws ClassNotFoundException, NamingException, SQLException {
  if (databaseReference == null) {
    throw new IllegalArgumentException("databaseReference cannot be null");
  } else {
    setDatabaseReference(databaseReference);
    setJdbcConnection(getDatabaseReference().openConnection());
  }
}

// properties ==================================================================

/**
 * Gets the underlying JDBC connection.
 * @return the underlying JDBC connection
 */
public Connection getJdbcConnection() {
  return _jdbcConnection;
}
/**
 * Sets the underlying JDBC connection.
 * @param con the underlying JDBC connection
 */
private void setJdbcConnection(Connection con) {
  _jdbcConnection = con;
}

/**
 * Gets the DatabaseReference for this connection.
 * @return the associated DatabaseReference
 */
protected DatabaseReference getDatabaseReference() {
  return _databaseReference;
}
/**
 * Sets the DatabaseReference for this connection.
 * @param databaseReference the associated DatabaseReference
 */
private void setDatabaseReference(DatabaseReference databaseReference) {
  _databaseReference = databaseReference;
}

/**
 * Gets the transaction counter.
 * @return the transaction counter
 */
private TransactionCounter getTransactionCounter() {
  return _transactionCounter;
}

// methods =====================================================================

/**
 * Closes the underlying JDBC connection.
 * <br/>The underlying TransactionCounter is reset.
 */
protected void close() {
  Connection con = getJdbcConnection();
  getTransactionCounter().reset(con);
  getDatabaseReference().closeConnection(con);
  setJdbcConnection(null);
}

/**
 * Handles the commit for the transaction.
 * <br/>If this ManagedConnection was not prepared for transactions,
 * no commit is executed.
 * <br/>If the underlying transaction count is zero, no commit is executed.
 * @throws SQLException if an exception occurs
 */
public void commit() throws SQLException {
  getTransactionCounter().commit(getJdbcConnection());
}

/**
 * Finalize on garbage collection.
 * @throws Throwable
 */
@Override
protected void finalize() throws Throwable {
  super.finalize();
  close();
}

/**
 * Increments the underlying TransactionCounter by one.
 */
public void incrementTransactionCount() {
  getTransactionCounter().increment();
}

/**
 * Compare the underlying JDBC connection with another.
 * @param connection the connection to compare
 * @return true if the underlying JDBC connection == the passed connection
 */
protected boolean isJdbcConnection(Connection connection) {
  return ((connection != null) && (connection == getJdbcConnection()));
}

/**
 * Prepares the underlying TransactionCounter prior to any edits.
 * <br/>Auto commit is set to false for the underlying JDBC connection.
 * <br/>This method must be executed prior to any commit or rollback.
 * @throws SQLException if an exception occurs
 */
public void prepareForTransactions() throws SQLException {
  getTransactionCounter().prepare(getJdbcConnection());
}

/**
 * Handles the rollback for the transaction.
 * <br/>A single rolback is executed for each incremented transaction count.
 * <br/>If this ManagedConnection was not prepared for transactions,
 * no rollback is executed.
 */
public void rollback() {
  getTransactionCounter().rollback(getJdbcConnection());
}

/**
 * Gets clob mutator.
 * @return clob mutator
 * @throws java.sql.SQLException if error getting clob mutator
 */
public IClobMutator getClobMutator() throws SQLException {
  DatabaseMetaData dmt = this.getJdbcConnection().getMetaData();
  String database = dmt.getDatabaseProductName().toLowerCase();

  if (database.contains("postgresql")) {
    return new TextClobMutator();
  }

  if (database.contains("oracle")) {
    return new StdClobMutator();
  }

  if (database.contains("microsoft")) {
    return new TextClobMutator();
  }

  if (database.contains("db2")) {
    return new StdClobMutator();
  }

  if (database.contains("mysql")) {
    return new StdClobMutator();
  }
  
  return new StdClobMutator();
}
}
