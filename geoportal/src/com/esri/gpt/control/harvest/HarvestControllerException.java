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
package com.esri.gpt.control.harvest;

import com.esri.gpt.framework.util.Val;

/**
 * Harvest controller exception.
 */
class HarvestControllerException extends Exception {
  private String resKey;

  /**
   * Constructs an instance of <code>HarvestControllerException</code> with 
   * the resource key of the specified detail message.
   * @param resKey resource key.
   */
  public HarvestControllerException(String resKey) {
    this.resKey = Val.chkStr(resKey);
  }

  /**
   * Gets resource key with error message.
   * @return resource key
   */
  public String getResKey() {
    return resKey;
  }
}
