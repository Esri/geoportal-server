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
package com.esri.gpt.control.search.browse;

import java.util.LinkedHashMap;

/**
 * Provides collection of toc configuration.
 */
@SuppressWarnings("serial")
public class TocCollection extends LinkedHashMap<String,String>{

  // class variables =============================================================
  // instance variables ==========================================================
    
  // constructors ================================================================
  
  // properties ==================================================================
  
  // methods =====================================================================
  	  
  /**
   * Returns the string representation of the object.
   * @return the string
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer(getClass().getName());
    if (size() == 0) {
      sb.append(" ()");
    } else {
      sb.append(" (\n");
      for (String member: values()) {
        sb.append(member).append("\n");
      }
      sb.append(") ===== end ").append(getClass().getName());
    }
    return sb.toString();
  }
   
}
