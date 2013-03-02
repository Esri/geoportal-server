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
package com.esri.gpt.framework.security.identity.open;
import com.esri.gpt.framework.collection.CaseInsensitiveMap;
import com.esri.gpt.framework.util.Val;

import java.util.Collection;

/**
 * A collection of configured Openid or oAuth providers.
 */
public class OpenProviders {

  /** instance variables ====================================================== */
  private CaseInsensitiveMap<OpenProvider> members = new CaseInsensitiveMap<OpenProvider>(false);
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public OpenProviders() {}
  
  /** properties ============================================================== */
  
  /** methods ================================================================= */
  
  /**
   * Adds a member to the collection.
   * @param member the member to add
   */
  public void add(OpenProvider member) {
    if ((member != null) && (member.getName() != null)) {
      String key = Val.chkStr(member.getName());
      if (key.length() > 0) {
        this.members.put(key.toLowerCase(),member);
      }
    }
  }
  
  /**
   * Gets the provider associated with a name.
   * @param name the open provider name
   * @return the associated concept (can be null)
   */
  public OpenProvider get(String name) {
    return this.members.get(name);
  }
  
  /**
   * Returns the size of the collection.
   * @return the size
   */
  public int size() {
    return this.members.size();
  }
  
  /**
   * Returns the collection of values.
   * @return the values
   */
  public Collection<OpenProvider> values() {
    return this.members.values();
  }
}
