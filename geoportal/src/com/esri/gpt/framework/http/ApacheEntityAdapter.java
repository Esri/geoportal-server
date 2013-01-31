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
import org.apache.commons.httpclient.methods.RequestEntity;

/**
 * Adapts the provider for the content of the HTTP request body to the Apache 
 * RequestEntity model.
 */
class ApacheEntityAdapter implements RequestEntity {
  
  /** instance variables ====================================================== */ 
  private ContentProvider   provider;
  private HttpClientRequest request;
  
  /** constructors ============================================================ */
  
  /**
   * Construct with an associated HTTP client request and request content provider.
   * @param request the HTTP request that is executing
   * @param provider the provider for the content of the HTTP request body
   */
  public ApacheEntityAdapter(HttpClientRequest request, ContentProvider provider) {
    this.request = request;
    this.provider = provider;
  }
    
  /** properties  ============================================================= */
  
  /**
   * Gets the content length (in bytes) to be provided in the HTTP request header.
   * @return the request content length (in bytes, use -1 if unknown);
   */
  public long getContentLength() {
    return this.provider.getContentLength();
  }

  /**
   * Gets the content type to be provided in the HTTP request header.
   * <br/>Include a charset if applicable, e.g. text/xml; charset=UTF-8
   * @return the request content type
   */
  public String getContentType() {
    return this.provider.getContentType();
  }

  /**
   * Tests if the HTTP request content can be written to the output stream
   * more than once. 
   * <br/>This is applicable when the content has been stored in memory 
   * <br/> (i.e. as a String, byte array ...)
   * @return true if the content can be written more than once
   */
  public boolean isRepeatable() {
    return this.provider.isRepeatable();
  }

  /**
   * Writes the content of the HTTP request body to an output stream.
   * @param request the HTTP request that is executing
   * @param destination the output stream to which data will be written
   * @throws IOException if an exception occurs
   */
  public void writeRequest(OutputStream destination) throws IOException {
    this.provider.writeRequest(this.request,destination);
  }

}
