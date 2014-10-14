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
package com.esri.gpt.control.georss;

import com.esri.gpt.framework.util.Val;
import static com.esri.gpt.framework.util.Val.chkStr;
import java.util.HashMap;

/**
 * Collection of DCAT schemas
 */
public class DcatSchemas extends HashMap<String, DcatFields> {

  @Override
  public DcatFields put(String keys, DcatFields value) {
    String [] keysArr = keys.split(",");
    for (String key: keysArr) {
      key = chkStr(key);
      if (!key.isEmpty()) {
        super.put(key, value);
      }
    }
    return value;
  }
  
    
    
    public DcatFields getDefaultFields() {
      return get("others");
    }

}
