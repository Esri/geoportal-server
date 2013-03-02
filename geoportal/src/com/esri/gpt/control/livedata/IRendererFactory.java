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
package com.esri.gpt.control.livedata;

import com.esri.gpt.control.livedata.selector.IRegistry;
import com.esri.gpt.control.livedata.selector.ISetter;
import com.esri.gpt.framework.http.CredentialProvider;

/**
 * Renderer factory. Provides mechanizm to create instance of the renderer
 * corresponding to the specific URL.
 */
public interface IRendererFactory {
  /**
   * Registers URL.
   * @param reg registry
   * @param setter renderer setter
   * @param url URL to register
   */
  void register(IRegistry reg, ISetter setter, String url);
  
  /**
   * Checks if factory represents definitive protocol.
   * Definitive protocol is the one when responds to the initial request it can 
   * be considered as the only choice.
   * @return <code>true</code> if protocol is definitive;
   */
  boolean isDefinitive();
}
