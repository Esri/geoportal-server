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
package com.esri.gpt.control.webharvest.protocol;

import java.lang.reflect.Method;
import org.w3c.dom.Node;

/**
 * Protocol initializer.
 */
public class ProtocolInitializer {

  /**
   * Initializes factory.
   * @param factory protocol factory
   * @param node node
   */
  public static void init(ProtocolFactory factory, Node node) {
    try {
      Method mt = factory.getClass().getMethod("init", Node.class);
      mt.invoke(factory, node);
    } catch (Exception ex) {

    }
  }
}
