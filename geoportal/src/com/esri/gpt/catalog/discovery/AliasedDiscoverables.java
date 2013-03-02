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
package com.esri.gpt.catalog.discovery;
import com.esri.gpt.framework.collection.CaseInsensitiveMap;
import java.util.Map;

/**
 * A map of discoverable properties keyed on alias names.
 */
public class AliasedDiscoverables extends CaseInsensitiveMap<Discoverable>{
   
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AliasedDiscoverables() {
    super(false);
  }
  
  /** methods ================================================================= */
    
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   */
  public void echo(StringBuffer sb) {
    if (size() == 0) {
      sb.append(" (No discoverables.)");
    } else {
      for (Map.Entry<String,Discoverable> entry: this.entrySet()) {
        sb.append("\n ").append(entry.getKey()).append("\n");
        ((Discoverable)entry.getValue()).echo(sb,1);
      }
    }    
  }
  
  /**
   * Returns the string representation of the object.
   * @return the string
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer(getClass().getName()).append(" (");
    echo(sb);
    sb.append("\n) ===== end ").append(getClass().getName());
    return sb.toString();
  }

}
