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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Base class for providers of HTTP request content and 
 * handlers of HTTP response content.
 */
public class IOUtility {
  
  /** instance variables ====================================================== */
  private int initialBufferLength = 4096;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public IOUtility() {}
  
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
  
  /**
   * Executes stream to stream I/O.
   * @param source the input stream from which data will be read
   * @param destination the output stream to which data will be written
   * @throws IOException if an exception occurs
   */
  public void executeIO(InputStream source, OutputStream destination) 
    throws IOException {    
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
    } finally {
      // TODO: watch for any negative effects
      try {if (bis != null) bis.close();} catch (Exception ef) {}
      try {if (bos != null) bos.flush();} catch (Exception ef) {}
    }
  }
  

  
  /**
   * Executes character to character I/O.
   * @param source the character reader from which data will be read
   * @param destination the character writer the to which data will be written
   * @throws IOException if an exception occurs
   */
  public void executeIO(Reader source, Writer destination) 
    throws IOException {    
    BufferedReader br = null;
    BufferedWriter bw = null;
    try {
      char buffer[] = new char[this.getInitialBufferlength()];
      int nRead = 0;
      int nMaxToRead = buffer.length;
      int nTotalRead = 0;
      br = new BufferedReader(source);
      bw = new BufferedWriter(destination);
      while ((nRead = br.read(buffer,0,nMaxToRead)) >= 0) {
        bw.write(buffer,0,nRead);
        nTotalRead += nRead;
      }
    } finally {
      // TODO: watch for any negative effects
      try {if (br != null) br.close();} catch (Exception ef) {}
      try {if (bw != null) bw.close();} catch (Exception ef) {}
    }
  }
  
  /**
   * Executes stream to character I/O.
   * @param source the character reader from which data will be read
   * @param destination the character writer the to which data will be written
   * @throws IOException if an exception occurs
   */
  public void executeIO(InputStream source, String charset, Writer destination) 
    throws IOException {    
    InputStreamReader isr = null;
    try {
      isr = new InputStreamReader(source,charset);
      this.executeIO(isr,destination);
    } finally {
      // TODO: watch for any negative effects
      try {if (isr != null) isr.close();} catch (Exception ef) {}
    }
  }
  
  /**
   * Executes character to stream I/O.
   * @param source the character reader from which data will be read
   * @param destination the output stream to which data will be written
   * @throws IOException if an exception occurs
   */
  public void executeIO(Reader source, String charset, OutputStream destination) 
    throws IOException {    
    OutputStreamWriter osr = null;
    try {
      osr = new OutputStreamWriter(destination,charset);
      this.executeIO(source,osr);
    } finally {
      // TODO: watch for any negative effects
      try {if (osr != null) osr.close();} catch (Exception ef) {}
    }
  }
   
  
  /**
   * Reads the bytes of an input stream into an array.
   * @param source the input stream from which data will be read
   * @return the bytes read
   * @throws IOException if an exception occurs
   */
  private byte[] readBytes(InputStream source) throws IOException {
    ByteArrayOutputStream bos = null;
    try {
      bos = new ByteArrayOutputStream();
      this.executeIO(source,bos);
      return bos.toByteArray();
    } finally {
      try {if (bos != null) bos.close();} catch (Exception ef) {}
    }
  }
  
  /**
   * Reads the bytes of an input stream into a string.
   * @param source the input stream from which data will be read
   * @param charset the character set encoding
   * @return the string
   * @throws IOException if an exception occurs
   */
  private String readCharacters(InputStream source, String charset) 
    throws IOException {
    StringWriter sw = null;
    try {
      sw = new StringWriter();
      this.executeIO(source,charset,sw);
      return sw.getBuffer().toString();
    } finally {
      try {if (sw != null) sw.close();} catch (Exception ef) {}
    }
  }
  
  /**
   * Reads the content a reader.
   * @param reader the reader
   * @return the string
   * @throws IOException if an exception occurs
   */
  private String readCharacters(Reader source) 
    throws IOException {
    StringWriter sw = null;
    try {
      sw = new StringWriter();
      this.executeIO(source,sw);
      return sw.getBuffer().toString();
    } finally {
      try {if (sw != null) sw.close();} catch (Exception ef) {}
    }
  }
  
  /**
   * Writes a byte array to an output stream.
   * @param source the array of bytes to write
   * @param destination the output stream to which data will be written
   * @throws IOException if an exception occurs
   */
  private void writeBytes(byte[] source, OutputStream destination) throws IOException {    
    ByteArrayInputStream bis = null;
    try {
      bis = new ByteArrayInputStream(source);
      this.executeIO(bis,destination);
    } finally {
      try {if (bis != null) bis.close();} catch (Exception ef) {}
    }
  }
  
  /**
   * Writes a string to an output stream.
   * @param source the string to write
   * @param charset the character set encoding
   * @param destination the output stream to which data will be written
   * @throws IOException if an exception occurs
   */
  private void writeCharacters(String source, String charset, OutputStream destination) 
    throws IOException {
    StringReader sr = null;
    try {
      sr = new StringReader(source);
      this.executeIO(sr,charset,destination);
    } finally {
      try {if (sr != null) sr.close();} catch (Exception ef) {}
    }
  }
  
  
}
