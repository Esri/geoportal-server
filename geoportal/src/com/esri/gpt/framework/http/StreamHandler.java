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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Stream based handler for the content of an HTTP response body.
 * <p/>
 */
public class StreamHandler extends ContentHandler {
    
  /** instance variables ====================================================== */  
  private OutputStream outputStream;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied output stream.
   * @param stream the stream to which the HTTP response will be written
   */
  public StreamHandler(OutputStream stream) {
    super();
    this.setOutputStream(stream);
  }
  
  /** properties  ============================================================= */
  
  /**
   * Gets the stream to which the HTTP response is written.
   * @return the output stream
   */
  public OutputStream getOutputStream() {
    return this.outputStream;
  }
  /**
   * Sets the stream to which the HTTP response is written.
   * @param outputStream the output stream
   */
  private void setOutputStream(OutputStream stream) {
    this.outputStream = stream;
  }
  
  /** methods ================================================================= */
      
  /**
   * Handle the content associated with an HTTP response body.
   * @param request the HTTP request that was executed
   * @param responseStream the stream associated with the HTTP response body
   * @throws IOException if an exception occurs
   */
  @Override
  public void readResponse(HttpClientRequest request, InputStream responseStream) 
    throws IOException {    
    long nBytes = this.executeIO(responseStream,this.getOutputStream());
    request.getResponseInfo().setBytesRead(nBytes);
  }
  
}
