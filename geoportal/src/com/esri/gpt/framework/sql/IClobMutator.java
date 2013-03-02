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

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides mechanizm to access CLOB data.
 * Depending on database it might be a different way to access data stored
 * within CLOB column.
 * @see ManagedConnection#getClobMutator()
 */
public interface IClobMutator {

/**
 * Gets value of the CLOB.
 * @param rs result set
 * @param fieldIndex index of the CLOB field
 * @return value of the CLOB as string
 * @throws java.sql.SQLException if reading CLOB failed
 */
String get(ResultSet rs, int fieldIndex) throws SQLException;

/**
 * Gets value of the CLOB.
 * @param rs result set
 * @param fieldName name of the CLOB field
 * @return value of the CLOB as string
 * @throws java.sql.SQLException if reading CLOB failed
 */
String get(ResultSet rs, String fieldName) throws SQLException;

/**
 * Sets value of the CLOB.
 * @param st prepared statement
 * @param paramIndex index of the CLOB parameter
 * @param value value to set
 * @throws java.sql.SQLException if writing CLOB failed
 */
void set(PreparedStatement st, int paramIndex, String value) throws SQLException;

/**
 * Gets value as a stream
 * @param rs result set
 * @param fieldIndex field index
 * @return input stream to read value
 * @throws SQLException if getting stream failed
 */
InputStream getStream(ResultSet rs, int fieldIndex) throws SQLException;

/**
 * Sets value as a stream
 * @param st prepared statement
 * @param fieldIndex field index
 * @param value value as a stream
 * @param length length of the stream
 * @throws SQLException if setting stream failed
 */
void setStream(PreparedStatement st, int fieldIndex, InputStream value, long length) throws SQLException;

}
