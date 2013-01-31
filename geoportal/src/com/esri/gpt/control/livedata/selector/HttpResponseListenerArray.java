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

import com.esri.gpt.framework.http.ResponseInfo;
import java.util.ArrayList;
import org.w3c.dom.Document;

/**
 * HTTP response listener array.
 */
class HttpResponseListenerArray extends ArrayList<IHttpResponseListener> {

  /**
   * Invoked when response arrives.
   * @param info response info
   * @param strContent response content as string
   * @param docContent response content as DOM or <code>null</code> if response is not a DOM
   */
  public void onResponse(ResponseInfo info, String strContent, Document docContent) {
    for (IHttpResponseListener l : this) {
      l.onResponse(info, strContent, docContent);
    }
  }
}
