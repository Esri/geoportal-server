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
import com.esri.gpt.framework.util.Val;

/**
 * The enumeration of comparison types for a property.
 * <p/>ANYTEXT: Represents a placeholder for term/expression based queries against multiple fields. 
 * <p/>KEYWORD: A storable String that supports non-tokenized, case insensitive, 
 * comparison and sorting.
 * <p/>NONE: A storable String that can be retrieved but not queried.
 * <p/>TERMS: A storable tokenized String that supports term/expression based queries as well as 
 * case insensitive comparison and sorting.
 * <p/>VALUE: A storable tokenized String that supports direct, case sensitive
 * comparison and sorting.
 */
public enum PropertyComparisonType {
  ANYTEXT,
  KEYWORD,
  NONE,
  TERMS,
  VALUE;
  
  /**
   * Makes a property comparison type from a supplied string value.
   * <br/>I null or an empty string is supplied, PropertyComparisonType.NONE is returned.
   * @param value the value
   * @return the property comparison type
   * @throws IllegalArgumentException if the value was invalid
   */
  public static PropertyComparisonType from(String value) throws IllegalArgumentException {
    value = Val.chkStr(value);
    if (value.length() == 0) {
      return PropertyComparisonType.NONE;
    } else {
      try {
        return PropertyComparisonType.valueOf(value.toUpperCase());
      } catch (IllegalArgumentException ex) {
        throw new IllegalArgumentException("Unrecognized property comparison type: "+value);
      }
    }
  }
  
}