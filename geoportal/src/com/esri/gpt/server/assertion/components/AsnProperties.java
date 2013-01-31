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
import java.util.ArrayList;

/**
 * A list of properties.
 */
@SuppressWarnings("serial")
public class AsnProperties extends ArrayList<AsnProperty> {
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnProperties() {}
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnProperties(AsnProperties objectToDuplicate) {
    if (objectToDuplicate != null) {
      for (AsnProperty property: objectToDuplicate) {
        this.add(property.duplicate());
      }
    }
  }
  
  /** methods ================================================================= */
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnProperties(this);
   * @return the duplicated object
   */
  public AsnProperties duplicate() {
    return new AsnProperties(this);
  }
  
}
