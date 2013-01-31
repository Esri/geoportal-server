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
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.io.Writer;

/**
 * Writes a JSON response based upon a JDBC ResultSet.
 */
public class JsonResultSetWriter extends ResultSetWriter {
  
  /** instance variables ====================================================== */
  private boolean firstCell = false;
  private boolean firstRow = false;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with an underlying response writer.
   * @param underlyingWriter the underlying writer
   */
  public JsonResultSetWriter(Writer underlyingWriter) {
    super(underlyingWriter);
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the response content type.
   * @return the response content type
   */
  @Override
  public String getContentType() {
    return "text/plain; charset=UTF-8";
  }
  
  /** methods ================================================================= */
    
  /**
   * Ends a row.
   * @param depth the indent depth
   * @throws IOException if an I/O exception occurs
   */
  @Override
  public void endRow(int depth) throws IOException {
    this.write(this.getNewline()+this.makeTabs(depth)+"}");
  }
  
  /**
   * Ends a collection of rows.
   * @param depth the indent depth
   * @throws IOException if an I/O exception occurs
   */
  @Override
  public void endRows(int depth) throws IOException {
    this.write(this.getNewline()+this.makeTabs(depth)+"]");
  }
  
  /**
   * Starts a row.
   * @param depth the indent depth
   * @throws IOException if an I/O exception occurs
   */
  @Override
  public void startRow(int depth) throws IOException {
    if (!this.firstRow) this.write(",");
    else this.firstRow = false;
    this.firstCell = true;
    this.write(this.getNewline()+this.makeTabs(depth)+"{");
  }
  
  /**
   * Starts a collection of rows.
   * @param depth the indent depth
   * @throws IOException if an I/O exception occurs
   */
  @Override
  public void startRows(int depth) throws IOException {
    this.firstRow = true;
    if (this.wasWrittenTo()) this.write(this.getNewline());
    this.write(this.makeTabs(depth)+"\"rows\": [");
  }
  
  /**
   * Writes a cell property value.
   * @param name the property name
   * @param value the property value
   * @param depth the indent depth
   * @throws IOException if an I/O exception occurs
   */
  @Override
  public void writeCell(String name, Object value, int depth) throws IOException {
    if (!this.firstCell) this.write(",");
    else this.firstCell = false;
    this.write(this.getNewline()+this.makeTabs(depth)+"\""+name+"\": ");
    if (value == null) {
      this.write("null");
    } else if (value instanceof Number) {
      this.write(""+value);
    } else if (value instanceof String) {
      this.write("\""+Val.escapeStrForJson((String)value)+"\"");
    } else {
      this.write("\""+Val.escapeStrForJson(value.toString())+"\"");
    }
  }

}
