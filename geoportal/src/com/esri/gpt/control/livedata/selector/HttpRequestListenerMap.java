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
package com.esri.gpt.control.livedata.selector;

import java.util.Map;
import java.util.TreeMap;

/**
 * HTTP request to listeners map.
 */
class HttpRequestListenerMap extends TreeMap<HttpRequestDefinition, HttpResponseListenerArray> implements IRegistry {

  /**
   * Creates instance of the map.
   */
  public HttpRequestListenerMap() {
    super(HttpRequestDefinition.CASE_INSENCITIVE_ORDER);
  }

  public void register(HttpRequestDefinition httpReqDef, IHttpResponseListener listener) {
    HttpResponseListenerArray arr = this.get(httpReqDef);
    if (arr == null) {
      arr = new HttpResponseListenerArray();
      put(httpReqDef, arr);
    }
    arr.add(listener);
  }

  /**
   * Selects subset of listeners by selector.
   * @param selector selector
   * @return subset of listeners
   */
  public HttpRequestListenerMap select(ISelector selector) {
    HttpRequestListenerMap map = new HttpRequestListenerMap();
    for (Map.Entry<HttpRequestDefinition, HttpResponseListenerArray> e : entrySet()) {
      if (selector.eligible(e.getKey())) {
        map.put(e.getKey(), e.getValue());
      }
    }
    return map;
  }

  @Override
  public String toString() {
    return keySet().toString();
  }

  /**
   * Selector.
   */
  public static interface ISelector {

    /**
     * Checks request definition eligibility.
     * @param httpReqDef request definition
     * @return <code>true</code> if request definition eligible to be selected
     */
    boolean eligible(HttpRequestDefinition httpReqDef);
  }
}
