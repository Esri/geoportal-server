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
package com.esri.gpt.catalog.harvest.history;

import com.esri.gpt.framework.request.Records;
import com.esri.gpt.framework.util.Val;

/**
 * List of history events.
 */
public class HeRecords extends Records<HeRecord> {

/**
 * Looks for the record by uuid.
 * @param uuid UUID of the record to look for
 * @return found record or <code>null</code> if record not found
 */  
public HeRecord findByUuid(String uuid) {
  uuid = Val.chkStr(uuid);
  for (HeRecord event : this) {
    if (event.getUuid().equalsIgnoreCase(uuid)) {
      return event;
    }
  }
  return null;
}

}
