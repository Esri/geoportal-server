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
import com.esri.gpt.framework.util.LogUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Manages the transaction count for a multi-edit transaction.
 */
public final class TransactionCounter {

// class variables =============================================================

// instance variables ==========================================================
private int     _transactionCount = 0;
private boolean _wasAutoCommit    = false;
private boolean _wasPrepared      = false;

// constructors ================================================================

/** Default constructor. */
protected TransactionCounter() {}

// properties ==================================================================

// methods =====================================================================

/**
 * Handles the commit for the transaction.
 * <br/>If this TranasctionCount was not prepared, no commit is executed.
 * <br/>If the counter is zero, no commit is executed.
 * @param con the JDBC connection
 * @throws SQLException if an exception occurs
 */
protected void commit(Connection con) throws SQLException {
  try {
    if (_wasPrepared && (_transactionCount > 0) && (con != null)) {
      con.commit();
    }
  } finally {
    _transactionCount = 0;
  }
}

/**
 * Increments the transaction count by one.
 */
protected void increment() {
  _transactionCount++;
}

/**
 * Prepares the transaction count prior to any edits.
 * <br/>Auto commit is set to false for the connection.
 * <br/>This method must be executed prior to any commit or rollback.
 * @param con the JDBC connection
 * @throws SQLException if an exception occurs
 */
protected void prepare(Connection con) throws SQLException {
  _transactionCount = 0;
  _wasPrepared = true;
  _wasAutoCommit = con.getAutoCommit();
  if (_wasAutoCommit) {
    con.setAutoCommit(false);
  }
}

/**
 * Resets the transaction counter.
 * <p>
 * Auto commit is set to it's previous state for the connection.
 * @param con the JDBC connection
 */
protected void reset(Connection con) {
  try {
    if (_wasPrepared && (con != null) && (_wasAutoCommit != con.getAutoCommit())) {
      con.setAutoCommit(_wasAutoCommit);
    }
  } catch (Exception e) {
    LogUtil.getLogger().log(Level.WARNING,"setAutoCommit failed.",e);
  } finally {
    _transactionCount = 0;
    _wasAutoCommit = false;
    _wasPrepared = false;
  }
}

/**
 * Handles the rollback for the transaction.
 * <br/>A single rolback is executed for each incremented transaction count.
 * <br/>If this TranasctionCount was not prepared, no rollback is executed.
 * @param con the JDBC connection
 */
protected void rollback(Connection con) {
  try {
    if (_wasPrepared && (con != null)) {
      for (int i=0;i<_transactionCount;i++) {
        try {
          con.rollback();
        } catch (Exception e) {
          LogUtil.getLogger().log(Level.WARNING,"Rollback failed.",e);
        }
      }
    }
  } finally {
    _transactionCount = 0;
  }
}

}
