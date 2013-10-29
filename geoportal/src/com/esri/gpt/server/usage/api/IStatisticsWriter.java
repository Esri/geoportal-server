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
package com.esri.gpt.server.usage.api;

import java.sql.ResultSet;

/**
 * Statistics writer.
 */
public interface IStatisticsWriter {

  /**
   * Writes an element.
   *
   * @param name element name
   * @param value element value
   * @throws Exception if writing element fails
   */
  public void writeElement(String name, String value) throws Exception;

  /**
   * Writes an element.
   *
   * @param name element name
   * @param value element value
   * @param hasMore <code>true</code> to indicate there are more elements
   * intended to be written
   * @param isNumber <code>true</code> to indicate that element is a number
   * @throws Exception if writing element fails
   */
  public void writeElement(String name, String value, boolean hasMore, boolean isNumber) throws Exception;

  /**
   * Writes result set.
   *
   * @param tableName table name
   * @param rset result set instance
   * @param columnTags column tags
   * @throws Exception if writing element fails
   */
  public void writeResultSet(String tableName, ResultSet rset, String[] columnTags) throws Exception;
}
