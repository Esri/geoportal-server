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
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Provider for the content of an HTTP request body.
 */
public abstract class ContentProvider extends ContentBase { 
    
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public ContentProvider() {}
    
  /** properties  ============================================================= */
    
  /**
   * Gets the content length (in bytes) to be provided in the HTTP request header.
   * @return the request content length (in bytes, use -1 if unknown);
   */
  public abstract long getContentLength();
  
  /**
   * Gets the content type to be provided in the HTTP request header.
   * <br/>Include a charset if applicable, e.g. text/xml; charset=UTF-8
   * @return the request content type
   */
  public abstract String getContentType();
  
  /**
   * Tests if the HTTP request content can be written to the output stream more than once. 
   * @return true if the content can written more than once (typically via a cache)
   */
  public abstract boolean isRepeatable();
  
  /** methods ================================================================= */
  
  /**
   * Converts a String to a UTF-8 encoded byte array.
   * <br/>if nuul is passwd, null will be returned.
   * @param content the String to convert
   */
  public static byte[] asBytes(String content) {
    if (content == null) {
      return null;
    } else {
      try {
        return content.getBytes("UTF-8");
      } catch (UnsupportedEncodingException e) {
        // this will never happen
        throw new RuntimeException("Cannot convert String to UTF-8 encoded byte array.",e);
      }
    }
  }
  /**
   * Ensures that an HTTP Contype-Type string contains a supplied encoding (i.e. charset reference).
   * @param contentType the HTTP Contype-Type string  to check
   * @param charset the charset to enforce
   * @return the resultant HTTP Content-Type string
   */
  public static String ensureContentTypeEncoding(String contentType, String charset) {
    if ((contentType != null) && (contentType.length() > 0)) {
      if ((charset != null) && (charset.length() > 0)) {
        if ((contentType.toLowerCase().indexOf("charset=") == -1) &&
            (charset.toLowerCase().indexOf("charset=") == -1)) {
          contentType += "; charset="+charset;
        }
      }
    }
    return contentType;
  }
      
  /**
   * Provides an opportunity to prepare content prior to writing the HTTP request body.
   * <br/>Content can be cached if required o this step.
   * <br/>The default behavior does nothing.
   * @param request the HTTP request that is executing
   * @throws IOException if an exception occurs
   */
  public void prepareForWrite(HttpClientRequest request) throws IOException {}

  /**
   * Writes the content of the HTTP request body to an output stream.
   * @param request the HTTP request that is executing
   * @param destination the output stream to which data will be written
   * @throws IOException if an exception occurs
   */
  public abstract void writeRequest(HttpClientRequest request, OutputStream destination) 
    throws IOException;

}
