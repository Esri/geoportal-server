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
package com.esri.gpt.server.assertion.components;
import com.esri.gpt.framework.util.Val;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A collection of security principal URNs.
 */
public class AsnPrincipals {

  /** instance variables ====================================================== */
  private Map<String,String> map = new LinkedHashMap<String,String>();
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AsnPrincipals() {}
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnPrincipals(AsnPrincipals objectToDuplicate) {
    if (objectToDuplicate != null) {
      Collection<String> values = objectToDuplicate.values();
      if (values != null) {
        for (String value: values) {
          this.add(value);
        }
      }
    } 
  }
  
  /** methods ================================================================= */
  
  /**
   * Adds a principal URN to the collection.
   * @param principalURN the principal URN
   */
  public void add(String principalURN) {
    if (principalURN == null) {
      throw new IllegalArgumentException("The principalURN cannot be null.");
    } else {
      principalURN = Val.chkStr(principalURN);
      if (principalURN.length() == 0) {
        throw new IllegalArgumentException("The principalURN cannot be empty.");
      } else {
        String pfx = AsnConstants.APP_URN_PREFIX;
        if (!pfx.endsWith(":")) pfx += ":";
        if (!principalURN.startsWith(pfx)) {
          String msg = "The principalURN must start with "+pfx;
          throw new IllegalArgumentException(msg);
        } else {
          map.put(principalURN,principalURN);
        }
      }
    }
  }
  
  /**
   * Clears the collection.
   */
  public void clear() {
    this.map.clear();
  }
  
  /**
   * Determines if a principal URN is contained within the collection.
   * @param principalURN the principal URN to check
   */
  public boolean contains(String principalURN) {
    return this.map.containsKey(principalURN);
  }
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnPrincipals(this);
   * @return the duplicated object
   */
  public AsnPrincipals duplicate() {
    return new AsnPrincipals(this);
  }
  
  /**
   * Gets the collection of values.
   * @return the values
   */
  public Collection<String> values() {
    return this.map.values();
  }
    
}
