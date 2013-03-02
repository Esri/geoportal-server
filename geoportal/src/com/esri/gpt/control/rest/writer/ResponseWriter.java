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

import javax.servlet.http.HttpServletResponse;

/**
 * Super-class for a rest based response writer.
 */
public abstract class ResponseWriter {
  
  /** class variables ========================================================= */
  
  /** The default newline string = "\r\n" */
  public static final String DEFAULT_NEWLINE = "\r\n";
  
  /** The default tab string = "  " (2 spaces) */
  public static final String DEFAULT_TAB = "  ";
  
  /** instance variables ====================================================== */
  private boolean wasWrittenTo = false;
  private String  newline = ResponseWriter.DEFAULT_NEWLINE;
  private String  tab     = ResponseWriter.DEFAULT_TAB;
  private Writer  underlyingWriter;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with an underlying response writer.
   * @param underlyingWriter the underlying writer
   */
  public ResponseWriter(Writer underlyingWriter) {
    this.underlyingWriter = underlyingWriter;
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the response content type.
   * @return the response content type
   */
  public abstract String getContentType();
  
  /**
   * Gets the newline string.
   * @return the newline string
   */
  public String getNewline() {
    return this.newline;
  }
  /**
   * Sets the newline string.
   * @param newline the newline string
   */
  public void setNewline(String newline) {
    this.newline = newline;
  }
  
  /**
   * Gets the tab string.
   * @return the tab string
   */
  public String getTab() {
    return this.tab;
  }
  /**
   * Sets the tab string.
   * @param tab the tab string
   */
  public void setTab(String tab) {
    this.tab = tab;
  }
  
  /**
   * Gets the underlying writer associated with the response.
   * <br/>The underlying writer is typically the PrintWriter associated 
   * with the HttpServletResponse.
   * @return the underlying writer
   */
  public Writer getUnderlyingWriter() {
    return this.underlyingWriter;
  }
  /**
   * Sets the underlying writer associated with the response.
   * <br/>The underlying writer is typically the PrintWriter associated 
   * with the HttpServletResponse.
   * @param underlyingWriter the underlying writer
   */
  protected void setUnderlyingWriter(Writer underlyingWriter) {
    this.underlyingWriter = underlyingWriter;
  }
  
  /**
   * Flag indicating if the response has been written to.
   * @return <true> if the response was written to
   */
  public boolean wasWrittenTo() {
    return this.wasWrittenTo;
  }
  
  
  /** methods ================================================================= */
  
  /**
   * Begins the response.
   * @param response HTTP response
   * @throws IOException if an I/O exception occurs
   */
  public void begin(HttpServletResponse response) throws IOException {
    response.setContentType(this.getContentType());
  }
  
  /**
   * Closes the underlying writer.
   * @throws IOException if an I/O exception occurs
   */
  public void close() throws IOException {
    this.getUnderlyingWriter().close();
  }
  
  /**
   * Flushes the underlying writer.
   * @throws IOException if an I/O exception occurs
   */
  public void flush() throws IOException {
    this.getUnderlyingWriter().flush();
  }
  
  /**
   * Makes a tab string for a supplied index depth.
   * @param depth the indent depth
   * @return the tab string
   */
  public String makeTabs(int depth) {
    if (depth < 1) {
      return "";
    } else if (depth == 1) {
      return getTab();
    } else {
      StringBuffer tabs = new StringBuffer();
      for (int i=0;i<depth;i++) {
        tabs.append(getTab());
      }
      return tabs.toString();
    }
  }
  
  /**
   * Writes a string to the underlying writer.
   * @param value the string to write
   * @throws IOException if an I/O exception occurs
   */
  public void write(String value) throws IOException {
    this.getUnderlyingWriter().write(value);
  }

}
