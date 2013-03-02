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
 * The enumeration of meaning types for a property.
 */
public enum PropertyMeaningType {
  ABSTRACT,
  ANYTEXT,
  CONTENTYPE,
  DATATYPE,
  DATEMODIFIED,
  GEOMETRY,
  FILEIDENTIFIER,
  RESOURCETYPE,
  RESOURCEURL,
  THUMBNAILURL,
  TITLE,
  UNKNOWN,
  UUID,
  WEBSITEURL,
  XML,
  XMLURL;
  
  /**
   * Makes a property meaning type from a supplied string value.
   * <br/>I null or an empty string is supplied, PropertyMeaningType.UNKNOWN is returned.
   * @param value the value
   * @return the property comparison type
   * @throws IllegalArgumentException if the value was invalid
   */
  public static PropertyMeaningType from(String value) throws IllegalArgumentException {
    value = Val.chkStr(value);
    if (value.length() == 0) {
      return PropertyMeaningType.UNKNOWN;
    } else {
      try {
        return PropertyMeaningType.valueOf(value.toUpperCase());
      } catch (IllegalArgumentException ex) {
        throw new IllegalArgumentException("Unrecognized property meaning type: "+value);
      }
    }
  }
  
}