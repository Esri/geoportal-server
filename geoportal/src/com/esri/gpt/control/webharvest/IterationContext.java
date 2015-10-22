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
package com.esri.gpt.control.webharvest;

import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.robots.Bots;

/**
 * Harvest context.
 */
public interface IterationContext {
  /**
   * Provides a way to capture exception which cannot be re-thrown. Both methods
   * {@link java.util.Iterator#hasNext()} and {@link java.util.Iterator#next()}
   * don't allow to throw any checked exception even if an implementation actually
   * should throw it.
   * @param ex
   */
  void onIterationException(Exception ex);
  
  /**
   * Factory method to create instance of {@link HttpClientRequest}
   * @return instance of HttpClientRequest
   */
  HttpClientRequest newHttpClientRequest();
  
  /**
   * Asserts access to the resource referenced by the url
   * @param url url of the resource
   * @throws AccessException if access denied
   */
  void assertAccess(String url) throws AccessException;
  
  /**
   * Gets RobotsTxt if available.
   * @return instance of robots txt or <code>null</code> if robots txt unavailable.
   */
  Bots getRobotsTxt();
}
