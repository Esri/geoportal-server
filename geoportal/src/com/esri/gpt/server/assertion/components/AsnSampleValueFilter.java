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

/**
 * Sample value filter.
 * <p>Replaces the lower case string "sample" with "*****"
 */
public class AsnSampleValueFilter extends AsnValueFilter {

  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AsnSampleValueFilter() {}
  
  /** methods ================================================================= */
  
  /**
   * Filters a value.
   * <p>Replaces the lower case string "sample" with "*****"
   * @param value the value to filter
   * @return the filtered value
   * @throws Exception if an exception occurs
   */
  public String filter(String value) throws Exception {
    if (value == null) {
      return null;
    } else {
      return value.replace("sample","******");
    }
  }
  
}
