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
package com.esri.gpt.server.csw.provider.components;
import com.esri.gpt.framework.collection.CaseInsensitiveMap;
import com.esri.gpt.framework.util.Val;

/**
 * A map of supported parameters.
 * <p>
 * The map is keyed on parameter name.
 */
@SuppressWarnings("serial")
public class SupportedParameters extends CaseInsensitiveMap<SupportedParameter> {
      
  /** constructors ============================================================ */
  
  /** Default constructor */
  public SupportedParameters() {
    super(false);
  }
  
  /** methods ================================================================= */
  
  /**
   * Adds an member to the collection.
   * @param member the member to add
   */
  public void add(SupportedParameter member) {
    if (member != null) {
      super.put(Val.chkStr(member.getName().toLowerCase()),member);
    }
  }
    
}
