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

/**
 * Handler for the content of an HTTP response body.
 */
public abstract class ContentHandler extends ContentBase {
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public ContentHandler() {}
  
  /** methods ================================================================= */
  
  /**
   * Called before response is being read.
   * @param request HTTP client request
   * @return <code>true</code> to continue reading response
   */
  public boolean onBeforeReadResponse(HttpClientRequest request) {
    return true;
  }
  
  /**
   * Handle the content associated with an HTTP response body.
   * @param request the HTTP request that was executed
   * @param responseStream the stream associated with the HTTP response body
   * @throws IOException if an exception occurs
   */
  public abstract void readResponse(HttpClientRequest request, InputStream responseStream) 
    throws IOException;
  
}
