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

import org.apache.commons.httpclient.Header;

/**
 * Contains basic information about an HTTP response.
 */
public class ResponseInfo {
  
  /** instance variables ====================================================== */
  private long   bytesRead = -1;
  private long   charactersRead = -1;
  private String contentEncoding;
  private long   contentLength = -1;
  private String contentType;
  private String defaultEncoding = "UTF-8"; // "ISO-8859-1"
  private int    responseCode = -1;
  private String responseMessage;
  private Header [] responseHeaders = new Header[]{};
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public ResponseInfo() {}
  
  /** properties  ============================================================= */
  
  /**
   * Gets the actual number of bytes read from the HTTP response body.
   * @return the number of bytes read (-1 if unknown)
   */
  public long getBytesRead() {
    return this.bytesRead;
  }
  /**
   * Sets the actual number of bytes read from the HTTP response body.
   * @param length the number of bytes read (-1 if unknown)
   */
  public void setBytesRead(long length) {
    this.bytesRead = length;
  }
  
  /**
   * Gets the actual number of characters read from the HTTP response body.
   * @return the number of characters read (-1 if unknown)
   */
  public long getCharactersRead() {
    return this.charactersRead;
  }
  /**
   * Sets the actual number of characters read from the HTTP response body.
   * @param length the number of characters read (-1 if unknown)
   */
  public void setCharactersRead(long length) {
    this.charactersRead = length;
  }
  
  /**
   * Gets the character encoding associated with the HTTP response body.
   * @return the encoding of the response body (if known)
   */
  public String getContentEncoding() {
    return this.contentEncoding;
  }
  /**
   * Sets the character encoding associated with the HTTP response body.
   * @param encoding the encoding of the response body (if known)
   */
  public void setContentEncoding(String encoding) {
    this.contentEncoding = encoding;
  }
  
  /**
   * Gets the declared length associated with the HTTP response body.
   * @return the declared length of the response body (-1 if unknown)
   */
  public long getContentLength() {
    return this.contentLength;
  }
  /**
   * Sets the declared length associated with the HTTP response body.
   * @param length the declared length of the response body (-1 if unknown)
   */
  public void setContentLength(long length) {
    this.contentLength = length;
  }
  
  /**
   * Gets the content type (i.e. MIME type) associated with the HTTP response body.
   * @return the content type of the response body (if known)
   */
  public String getContentType() {
    return this.contentType;
  }
  /**
   * Sets the content type (i.e. MIME type) associated with the HTTP response body.
   * @param contentType the content type of the response body (if known)
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
  
  /**
   * Gets the default character encoding for an HTTP response.
   * <br/>The default is used for character responses that do not specify an encoding.
   * @return the default encoding for a response body
   */
  public String getDefaultEncoding() {
    return this.defaultEncoding;
  }
  /**
   * Sets the default character encoding for an HTTP response.
   * <br/>The default is used for character responses that do not specify an encoding.
   * @param encoding the default encoding for a response body
   */
  public void setDefaultEncoding(String encoding) {
    this.defaultEncoding = encoding;
  }
  
  /**
   * Gets the code associated with the HTTP response.
   * @return the HTTP response code
   */
  public int getResponseCode() {
    return this.responseCode;
  }
  /**
   * Sets the code associated with the HTTP response.
   * @param code the HTTP response code
   */
  public void setResponseCode(int code) {
    this.responseCode = code;
  }
  
  /**
   * Gets the status message (or "reason phrase")  associated with the HTTP response.
   * @return the HTTP response message
   */
  public String getResponseMessage() {
    return this.responseMessage;
  }
  /**
   * Sets the status message (or "reason phrase")  associated with the HTTP response.
   * @param message the HTTP response message
   */
  public void setResponseMessage(String message) {
    this.responseMessage = message;
  }

  /**
   * Gets responseHeaders.
   * @return responseHeaders
   */
  public Header[] getResponseHeaders() {
    return responseHeaders;
  }

  /**
   * Sets responseHeaders.
   * @param headers responseHeaders
   */
  public void setResponseHeaders(Header[] headers) {
    this.responseHeaders = headers!=null? headers: new Header[]{};
  }
  
  /**
   * Gets response header by name.
   * @param name header name
   * @return response header or <code>null</code> if specified header not found
   */
  public Header getResponseHeader(String name) {
    for (Header header : getResponseHeaders()) {
      if (header.getName().equals(name)) {
        return header;
      }
    }
    return null;
  }
  
  /** methods ================================================================= */
   
  /**
   * Resets response information.
   */
  public void reset() {  
    this.setBytesRead(-1);
    this.setCharactersRead(-1);
    this.setContentEncoding(null);
    this.setContentLength(-1);
    this.setContentType(null);
    this.setResponseCode(-1);
    this.setResponseMessage(null);
  }
  
}
