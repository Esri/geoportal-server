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

/**
 * String based provider for the content of an HTTP request body.
 */
public class StringProvider extends ByteArrayProvider {
  
  /** instance variables ====================================================== */
  private String cache;
      
  /** constructors ============================================================ */
    
  /**
   * Constructs with a content String and a content type.
   * @param content the String providing the content for the HTTP request body 
   * @param contentType the content type of the HTTP request body,
   *        using null is not-recommended, only a MIME type is required e.g. text/xml
   */
  public StringProvider(String content, String contentType) {
    super(ContentProvider.asBytes(content),
        ContentProvider.ensureContentTypeEncoding(contentType,"UTF-8"));
    this.cache = content;
  }
  
  /** properties  ============================================================= */ 
  
  /**
   * Gets the content associated with the HTTP request suitable for logging.
   * @return the loggable string (null if not applicable)
   */
  @Override
  public String getLoggableContent() {
    return this.cache;
  }
    
}
