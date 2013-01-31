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
import java.io.StringWriter;

/**
 * Writes the content of an HTTP response body to a String.
 */
public class StringHandler extends CharacterHandler {
    
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public StringHandler() {
    super(new StringWriter());
  }
  
  /** properties  ============================================================= */
  
  /**
   * Gets the content associated with the HTTP response.
   * @return the content
   */
  public String getContent() {
    return ((StringWriter)this.getWriter()).getBuffer().toString();
  }

  /**
   * Gets the content associated with the HTTP response suitable for logging.
   * @return the loggable string (null if not applicable)
   */
  @Override
  public String getLoggableContent() {
    return this.getContent();
  }
  
}
