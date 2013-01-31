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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Byte array provider for the content of an HTTP request body.
 */
public class ByteArrayProvider extends ContentProvider {
  
  /** instance variables ====================================================== */
  private byte[] content;
  private String contentType;
  
  /** constructors ============================================================ */
   
  /**
   * Constructs with an array of bytes and a content type.
   * @param content the byte array providing the content for the HTTP request body 
   * @param contentType the content type of the HTTP request body,
   *        use null if unknown, include a charset if applicable,
   *        e.g. text/xml; charset=UTF-8
   */
  public ByteArrayProvider(byte[] content, String contentType) {
    super();
    this.content = content;
    this.contentType = contentType;
  }
    
  /** properties  ============================================================= */   
  
  /**
   * Gets the content length (in bytes) to be provided in the HTTP request header.
   * @return the request content length (in bytes, use -1 if unknown);
   */
  @Override
  public long getContentLength() {
    if (this.content == null) {
      return 0;
    } else {
      return this.content.length;
    }
  }
  
  /**
   * Gets the content type to be provided in the HTTP request header.
   * <br/>Include a charset if applicable, e.g. text/xml; charset=UTF-8
   * @return the request content type
   */
  @Override
  public String getContentType() {
    return this.contentType;
  }
  
  /**
   * Tests if the HTTP request content can be written to the output stream
   * more than once. 
   * @return always true
   */
  @Override
  public boolean isRepeatable() {
    return true;
  }
  
  /** methods ================================================================= */
  
  /**
   * Writes the content of the HTTP request body to an output stream.
   * @param request the HTTP request that is executing
   * @param destination the output stream to which data will be written
   * @throws IOException if an exception occurs
   */
  @Override
  public void writeRequest(HttpClientRequest request, OutputStream destination) throws IOException {
    if (this.content != null) {
      this.executeIO(new ByteArrayInputStream(this.content),destination);
    }
  }
  
}
