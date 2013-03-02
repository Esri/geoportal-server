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
package com.esri.gpt.control.rest.writer;
import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Super-class for a rest response writer based upon a JDBC ResultSet.
 */
public abstract class ResultSetWriter extends ResponseWriter {
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with an underlying response writer.
   * @param underlyingWriter the underlying writer
   */
  public ResultSetWriter(Writer underlyingWriter) {
    super(underlyingWriter);
  }
  
  /** methods ================================================================= */
  
  /**
   * Ends a row.
   * @param depth the indent depth
   * @throws IOException if an I/O exception occurs
   */
  public abstract void endRow(int depth) throws IOException;
  
  /**
   * Ends a collection of rows.
   * @param depth the indent depth
   * @throws IOException if an I/O exception occurs
   */
  public abstract void endRows(int depth) throws IOException;
  
  /**
   * Starts a row.
   * @param depth the indent depth
   * @throws IOException if an I/O exception occurs
   */
  public abstract void startRow(int depth) throws IOException;
  
  /**
   * Starts a collection of rows.
   * @param depth the indent depth
   * @throws IOException if an I/O exception occurs
   */
  public abstract void startRows(int depth) throws IOException;
  
  /**
   * Writes a cell property value.
   * @param name the property name
   * @param value the property value
   * @param depth the indent depth
   * @throws IOException if an I/O exception occurs
   */
  public abstract void writeCell(String name, Object value, int depth) throws IOException;
  
  /**
   * Writes a ResultSet to the response.
   * @param rs the ResultSet
   * @param depth the indent depth
   * @param columnTags optional, the tag names per column
   * @throws IOException if an I/O exception occurs
   * @throws SQLException if an SQL exception occurs
   */
  public void writeResultSet(ResultSet rs, int depth, String[] columnTags) 
    throws IOException, SQLException {
    this.startRows(depth);
    ResultSetMetaData md = rs.getMetaData();
    int nColumns = md.getColumnCount();
    while (rs.next()) {
      this.startRow(depth+1);
      for (int i=1;i<=nColumns;i++) {
        String name = md.getColumnName(i);
        if (columnTags != null) {
          name = columnTags[i-1];
        }
        this.writeCell(name,rs.getObject(i),depth+2);
      }
      this.endRow(depth+1); 
    }
    this.endRows(depth);
  }

}
