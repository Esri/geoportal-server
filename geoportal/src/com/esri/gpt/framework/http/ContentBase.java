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
package com.esri.gpt.framework.http;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Base class for providers of HTTP request content and 
 * handlers of HTTP response content.
 */
public abstract class ContentBase {
  
  /** instance variables ====================================================== */
  private int initialBufferLength = 4096;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public ContentBase() {}
  
  /** properties  ============================================================= */
  
  /**
   * Gets the initial buffer length (in bytes) for streamed content.
   * <br/>Default = 4096 bytes
   * @return the initial buffer length (in bytes)
   */
  public int getInitialBufferlength() {
    return this.initialBufferLength;
  }
  /**
   * Sets the initial buffer length (in bytes) for streamed content.
   * <br/>Default = 4096 bytes
   * @param length the initial buffer length (in bytes)
   */
  public void setInitialBufferLength(int length) {
    this.initialBufferLength = length;
  }
  
  /**
   * Gets content associated with the HTTP request/response suitable for logging.
   * <br/>The default implementation returns null.
   * @return the loggable string (null if not applicable)
   */
  public String getLoggableContent() {
    return null;
  }
  
  /** methods ================================================================= */
  
  /**
   * Executes stream to stream I/O.
   * @param source the input stream from which data will be read
   * @param destination the output stream to which data will be written
   * @return the number of bytes transferred
   * @throws IOException if an exception occurs
   */
  public long executeIO(InputStream source, OutputStream destination) throws IOException {    
    BufferedInputStream bis = null;
    BufferedOutputStream bos = null;
    try {
      byte buffer[] = new byte[this.getInitialBufferlength()];
      int nRead = 0;
      int nMax = buffer.length;
      int nTotal = 0;
      bis = new BufferedInputStream(source);
      bos = new BufferedOutputStream(destination);
      while ((nRead = bis.read(buffer,0,nMax)) >= 0) {
        bos.write(buffer,0,nRead);
        nTotal += nRead;
      }
      return nTotal;
    } finally {
      try {if (bos != null) bos.flush();} catch (Exception ef) {}
    }
  }
  
  /**
   * Executes character to character I/O.
   * @param source the character reader from which data will be read
   * @param destination the character writer the to which data will be written
   * @return the number of characters transferred
   * @throws IOException if an exception occurs
   */
  public long executeIO(Reader source, Writer destination) throws IOException {    
    BufferedReader br = null;
    BufferedWriter bw = null;
    try {
      char buffer[] = new char[this.getInitialBufferlength()];
      int nRead = 0;
      int nMax = buffer.length;
      int nTotal = 0;
      br = new BufferedReader(source);
      bw = new BufferedWriter(destination);
      while ((nRead = br.read(buffer,0,nMax)) >= 0) {
        bw.write(buffer,0,nRead);
        nTotal += nRead;
      }
      return nTotal;
    } finally {
      try {if (bw != null) bw.flush();} catch (Exception ef) {}
    }
  }
  
}
