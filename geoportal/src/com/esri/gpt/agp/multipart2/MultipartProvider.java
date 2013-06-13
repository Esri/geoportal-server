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
import com.esri.gpt.framework.http.ContentProvider;
import com.esri.gpt.framework.http.HttpClientRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Provides content for a multi-part HTTP request (outbound).
 */
public class MultipartProvider extends ContentProvider {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(MultipartProvider.class.getName());
  
  /** instance variables ====================================================== */
  private String      boundary = "387F8C2A-CFAB-443C-863B-B180E79B05F4";
  private List<MPart> parts = new ArrayList<MPart>();  
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public MultipartProvider() {}
  
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
   * Gets the content type.
   * @return the content type
   */
  @Override
  public String getContentType() {
    return "multipart/form-data; boundary="+this.boundary;
  }
    
  /**
   * Gets the parts.
   * @return the parts
   */
  public List<MPart> getParts() {
    return this.parts;
  }
  
  /** methods ================================================================= */

  /**
   * Adds a part.
   * @param part the part
   */
  public void add(MPart part) {
    this.getParts().add(part);
  }
  
  /**
   * Adds a part.
   * @param name the part name
   * @param data the data value
   * @throws IOException if an exception occurs
   */
  public void add(String name, String data) throws IOException {
    this.add(new MPart(name,data));
  }
  
  /**
   * Adds a part.
   * @param name the name
   * @param data the data 
   * @param contentType the content type
   * @throws IOException if an exception occurs
   */
  public void add(String name, String data, String contentType) 
    throws IOException {
    this.add(new MPart(name,data,contentType));
  }
  
  /**
   * Adds a part.
   * @param name the name
   * @param data the data 
   * @param filename the file name
   * @param contentType the content type
   * @param charset the character set
   * @throws IOException if an exception occurs
   */
  public void add(String name, byte[] data, String filename, 
      String contentType, String charset) throws IOException {
    this.add(new MPart(name,data,filename,contentType,charset));
  }
  
  /**
   * Adds a part.
   * @param name the name
   * @param file the file 
   * @param filename the file name
   * @param contentType the content type
   * @param charset the character set
   * @param deleteAfterUpload if true then delete the file after uploading
   * @throws IOException if an exception occurs
   */
  public void add(String name, final File file, String filename, 
      String contentType, String charset, final boolean deleteAfterUpload) 
    throws IOException {
    this.add(new MPart(name,null,filename,contentType,charset) {
      @Override
      protected long dataLength() throws IOException {
        if (file != null) return file.length();
        return 0;
      }
      @Override
      public boolean isRepeatable() {
        return !deleteAfterUpload;
      }
      @Override
      protected void sendData(OutputStream out) throws IOException {
        if (this.dataLength() > 0) {
          InputStream in = null;
          try {
            in = new FileInputStream(file);
            this.streamData(in,out);
          } finally {
            try {if (in != null) in.close();} 
            catch (Exception ef) {ef.printStackTrace(System.err);}
            if (deleteAfterUpload) {
              try {
                boolean deleted = file.delete();
                if (!deleted) {
                  LOGGER.warning("Unable to delete file: "+file.getAbsolutePath());
                }
              } catch (SecurityException ex) {
                LOGGER.warning("Unable to delete file: "+file.getAbsolutePath());
              }
            }
          }
        }
      }
    });
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
   * Gets the request body content length.
   * @return the request content length (in bytes, -1 if unknown);
   */
  @Override
  public long getContentLength() {
    try {
      long nTotal = 0;
      for (MPart part: this.getParts()) {
        part.setBoundary(this.getBoundary());
        long n = part.partLength();
        if (n < 0) {
          return -1;
        }
        nTotal += n;
      }
      nTotal += this.getAsciiBytes("--").length;
      nTotal += this.getAsciiBytes(this.getBoundary()).length;
      nTotal += this.getAsciiBytes("--").length;
      nTotal += this.getAsciiBytes("\r\n").length;
      //System.err.println("mpctl===== "+nTotal);
      return nTotal;
    } catch (Exception e) {
      e.printStackTrace(System.err);
      return 0;
    }
  }
  
  /**
   * True if all the parts can be written to the output stream more than once. 
   * @return true if repeatable
   */
  @Override
  public boolean isRepeatable() {
    for (MPart part: this.getParts()) {
      if (!part.isRepeatable()) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Writes the content of the HTTP request body to an output stream.
   * @param request the HTTP request that is executing
   * @param destination the output stream to which data will be written
   * @throws IOException if an exception occurs
   */
  @Override
  public void writeRequest(HttpClientRequest request, OutputStream destination)
      throws IOException {   
    try {
      for (MPart part: this.getParts()) {
        part.setBoundary(this.getBoundary());
        part.send(destination);
      }
      destination.write(this.getAsciiBytes("--"));
      destination.write(this.getAsciiBytes(this.getBoundary()));
      destination.write(this.getAsciiBytes("--"));
      destination.write(this.getAsciiBytes("\r\n"));
    } catch (IOException ioe) {
      throw ioe;
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

}
