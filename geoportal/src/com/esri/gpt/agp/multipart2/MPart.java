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
package com.esri.gpt.agp.multipart2;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * A part of an HTTP multi-part request.
 * <p/>
 * Based upon Apache HttpClient 3.1:
 * org.apache.commons.httpclient.methods.multipart
 */
public class MPart {
  
  /** instance variables ====================================================== */
  private String boundary;
  private String charset;
  private String contentType;
  private byte[] data;
  private String fileName;
  private String name;
  private String transferEncoding;

  /** constructors ============================================================ */
  
  /** Default constructor. */
  public MPart() {}
  
  /**
   * Constructor.
   * @param name the part name
   * @param data the data 
   */
  public MPart(String name, String data) {
    this(name,data,"text/plain");
  }
  
  /**
   * Constructor.
   * @param name the part name
   * @param data the data 
   * @param contentType the content type
   */
  public MPart(String name, String data, String contentType) {
    this.setName(name);
    try {
      this.setData(data.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      // shouldn't happen
      e.printStackTrace();
    }
    this.setFileName(fileName);
    if (contentType == null) contentType = "text/plain";
    this.setContentType(contentType);
    this.setCharset("UTF-8");
    //this.setTransferEncoding("8bit");
    //this.setTransferEncoding("binary");
  }
  
  /**
   * Constructor.
   * @param name the part name
   * @param data the data 
   * @param fileName the file name
   * @param contentType the content type
   * @param charset the character set
   */
  public MPart(String name, 
               byte[] data,
               String fileName,
               String contentType, 
               String charset) {
    this.setName(name);
    this.setData(data);
    this.setFileName(fileName);
    if (contentType == null) contentType = "application/octet-stream";
    this.setContentType(contentType);
    this.setCharset(charset);
    this.setTransferEncoding("binary");
  }
    
  /** properties ============================================================== */
  
  /**
   * Gets the boundary string.
   * @return the boundary
   */
  public String getBoundary() {
    return this.boundary;
  }
  /**
   * Sets the boundary string.
   * @param boundary the boundary
   */
  public void setBoundary(String boundary) {
    this.boundary = boundary;
  }
  
  /**
   * Gets the character set.
   * @return the character set
   */
  public String getCharset() {
    return this.charset;
  }
  /**
   * Sets the character set.
   * @param charset the character set
   */
  public void setCharset(String charset) {
    this.charset = charset;
  }
  
  /**
   * Gets the content type.
   * @return the content type
   */
  public String getContentType() {
    return this.contentType;
  }
  /**
   * Sets the content type.
   * @param contentType the content type
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
  
  /**
   * Gets the data.
   * @return the data
   */
  public byte[] getData() {
    return this.data;
  }
  /**
   * Sets the data.
   * @param data the data
   */
  public void setData(byte[] data) {
    this.data = data;
  }
  
  /**
   * Gets the file name.
   * @return the file name
   */
  public String getFileName() {
    return this.fileName;
  }
  /**
   * Sets the file name.
   * @param fileName the file name
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * Gets the part name.
   * @return the part name
   */
  public String getName() {
    return this.name;
  }
  /**
   * Sets the part name.
   * @param name the part name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the transfer encoding.
   * @return the transfer encoding
   */
  public String getTransferEncoding() {
    return this.transferEncoding;
  }
  /**
   * Sets the transfer encoding.
   * @param transferEncoding the transfer encoding
   */
  public void setTransferEncoding(String transferEncoding) {
    this.transferEncoding = transferEncoding;
  }
  
  /** methods ================================================================= */
  
  /**
   * Determine the length of the part data in bytes (-1 if unknown).
   * <br/>This should be overridden if the data is not a string or byte array.
   * @return the data length
   * @throws IOException if an exception occurs
   */
  protected long dataLength() throws IOException {
    if (this.data == null) {
      return 0;
    } else {
      return this.data.length;
    }
  }
  
  /**
   * Gets the US-ASCII bytes for a value
   * @param value the value
   * @return the bytes
   * @throws UnsupportedEncodingException if the encoding is unsupported
   */
  protected byte[] getAsciiBytes(String value) throws UnsupportedEncodingException {
    return value.getBytes("US-ASCII");
  }
  
  /**
   * True if the part can be written to the output stream more than once. 
   * @return true if repeatable
   */
  public boolean isRepeatable() {
    return true;
  }
  
  /**
   * Determine the length of the part in bytes (-1 if unknown).
   * @return the length of the part
   * @throws IOException if an exception occurs
   */
  public long partLength() throws IOException {
    if (dataLength() < 0) {
      return -1;
    }
    ByteArrayOutputStream overhead = new ByteArrayOutputStream();
    sendStart(overhead);
    sendDispositionHeader(overhead);
    sendContentTypeHeader(overhead);
    sendTransferEncodingHeader(overhead);
    sendEndOfHeader(overhead);
    sendEnd(overhead);
    //System.err.println(new String(overhead.toByteArray()));
    return overhead.size() + dataLength();
  }
  
  /**
   * Write the part to the stream.
   * @param out the output stream
   * @throws IOException if an exception occurs
   */
  public void send(OutputStream out) throws IOException {
    sendStart(out);
    sendDispositionHeader(out);
    sendContentTypeHeader(out);
    sendTransferEncodingHeader(out);
    sendEndOfHeader(out);
    sendData(out);
    sendEnd(out);
  }
  
  /**
   * Write the Content-Type to the stream.
   * @param out the output stream
   * @throws IOException if an exception occurs
   */
  protected void sendContentTypeHeader(OutputStream out) throws IOException {
    String sContentType = this.getContentType();
    String sCharset = this.getCharset();
    if (sContentType != null) {
      out.write(this.getAsciiBytes("\r\n"));;
      out.write(this.getAsciiBytes("Content-Type: "));
      out.write(this.getAsciiBytes(sContentType));
      if (sCharset != null) {
        out.write(this.getAsciiBytes("; charset="));
        out.write(this.getAsciiBytes(sCharset));
      }
    }
  }
  
  /**
   * Write the part data to the stream.
   * @param out the output stream
   * @throws IOException if an exception occurs
   */
  protected void sendData(OutputStream out) throws IOException {
    if (this.dataLength() > 0) {
      out.write(this.getData());
      //System.err.println(new String(this.getData(),"UTF-8"));
    }
  }
  
  /**
   * Write the Content-Disposition to the stream.
   * @param out the output stream
   * @throws IOException if an exception occurs
   */
  protected void sendDispositionHeader(OutputStream out) throws IOException {
    out.write(this.getAsciiBytes("Content-Disposition: form-data; name="));
    out.write(this.getAsciiBytes("\""));
    out.write(this.getAsciiBytes(this.getName()));
    out.write(this.getAsciiBytes("\""));
    String sFileName = this.getFileName();
    if (sFileName != null) {
      out.write(this.getAsciiBytes("; filename="));
      out.write(this.getAsciiBytes("\""));
      out.write(this.getAsciiBytes(sFileName));
      out.write(this.getAsciiBytes("\""));
    }
  }
  
  /**
   * Write the bytes that end a part to the stream.
   * @param out the output stream
   * @throws IOException if an exception occurs
   */
  protected void sendEnd(OutputStream out) throws IOException {
    out.write(this.getAsciiBytes("\r\n"));
  }
  
  /**
   * Write the bytes that end a part header to the stream.
   * @param out the output stream
   * @throws IOException if an exception occurs
   */
  protected void sendEndOfHeader(OutputStream out) throws IOException {
    out.write(this.getAsciiBytes("\r\n"));
    out.write(this.getAsciiBytes("\r\n"));
  }
  
  /**
   * Write the bytes that start a part to the stream.
   * @param out the output stream
   * @throws IOException if an exception occurs
   */
  protected void sendStart(OutputStream out) throws IOException {
    out.write(this.getAsciiBytes("--"));
    out.write(this.getAsciiBytes(this.getBoundary()));
    out.write(this.getAsciiBytes("\r\n"));
  }
  
  /**
   * Write the content Content-Transfer-Encoding to the stream.
   * @param out the output stream
   * @throws IOException if an exception occurs
   */
  protected void sendTransferEncodingHeader(OutputStream out) throws IOException {
    String sTransferEncoding = this.getTransferEncoding();
    if (sTransferEncoding != null) {
      out.write(this.getAsciiBytes("\r\n"));
      out.write(this.getAsciiBytes("Content-Transfer-Encoding: "));
      out.write(this.getAsciiBytes(sTransferEncoding));
    }
  }
  
  /**
   * Stream data from an input to an output.
   * @param source the input stream
   * @param destination the output stream
   * @throws IOException if an exception occurs
   */
  protected long streamData(InputStream source, OutputStream destination) throws IOException {    
    BufferedInputStream bis = null;
    BufferedOutputStream bos = null;
    try {
      byte buffer[] = new byte[4096];
      int nRead = 0;
      int nMax = buffer.length;
      long nTotal = 0;
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
  
}
