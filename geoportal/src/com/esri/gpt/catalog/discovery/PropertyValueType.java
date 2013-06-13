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
import com.esri.gpt.framework.util.DateProxy;
import com.esri.gpt.framework.util.Val;

/**
 * The enumeration of value types for a property.
 * <p/>DOUBLE: a double precision number
 * <p/>GEOMETRY: the bounding envelope for a document
 * <p/>LONG: a long integer
 * <p/>STRING: a string
 * <p/>TIMEPERIOD: time period of content
 * <p/>TIMESTAMP: a timestamp
 */
public enum PropertyValueType {
  DOUBLE,
  GEOMETRY,
  LONG,
  STRING,
  TIMEPERIOD,
  TIMESTAMP;
    
  /**
   * Converts a String value into an Object of the associated value type.
   * <p/>
   * If a null value is supplied, a null value will be returned.
   * <p/>
   * The GEOMETRY type is virtual an cannot be evaluated. If a non-null
   * value is supplied for a GEOMETRY type, an IllegalArgumentException
   * will be thrown.
   * @param value the value to evaluate
   * @return the evaluated Object
   * @throws NumberFormatException if the supplied value cannot be converted
   *         (applies to types DOUBLE,INTEGER,LONG)
   * @throws IllegalArgumentException if the supplied value cannot be converted
   *         (applies to types GEOMETRY,TIMESTAMP)       
   */
  public Object evaluate(String value) throws NumberFormatException {
    return evaluate(value,false,false);
  }
  
  /**
   * Converts a String value into an Object of the associated value type.
   * <p/>
   * If a null value is supplied, a null value will be returned.
   * <p/>
   * The GEOMETRY type is virtual an cannot be evaluated. If a non-null
   * value is supplied for a GEOMETRY type, an IllegalArgumentException
   * will be thrown.
   * @param value the value to evaluate
   * @param isLowerBoundary true if the value is the lower boundary of a range
   * @param isUpperBoundary true if the value is the upper boundary of a range
   * @return the evaluated Object
   * @throws NumberFormatException if the supplied value cannot be converted
   *         (applies to types DOUBLE,INTEGER,LONG)
   * @throws IllegalArgumentException if the supplied value cannot be converted
   *         (applies to types GEOMETRY,TIMESTAMP)       
   */  
  public Object evaluate(String value, 
                         boolean isLowerBoundary,
                         boolean isUpperBoundary) 
    throws NumberFormatException, IllegalArgumentException {
    if (value == null) return null;
    switch(this) {
      case DOUBLE:
        if (value.trim().length() == 0) return null;
        return Double.valueOf(value.trim());
      case GEOMETRY:
        throw new IllegalArgumentException(
            "GEOMETRY is a virtual type and cannot be evaluated");
      case LONG:
        if (value.trim().length() == 0) return null;
        return Long.valueOf(value.trim());
      case STRING:
        return value;
      case TIMESTAMP: {
        if (value.trim().length() == 0) return null;
        DateProxy proxy = new DateProxy();
        proxy.setDate(value);
        if (!proxy.getIsValid()) {
          throw new IllegalArgumentException("Invalid Timestamp: "+value+
              ", use for yyyy-mm-dd hh:mm:ss.fff");
        }
        if (isLowerBoundary) {
          return proxy.asFromTimestamp();
        } else if (isUpperBoundary) {
          return proxy.asToTimestamp();
        } else {
          return proxy.asFromTimestamp();
        }
      }
    }
    
    // It's recommended to throw this assertion error to avoid
    // compilation problems
    throw new AssertionError("Unknown PropertyType: " + this);
  }
  
  /**
   * Makes a property value type from a supplied string value.
   * <br/>I null or an empty string is supplied, PropertyValueType.STRING is returned.
   * @param value the value
   * @return the property value type
   * @throws IllegalArgumentException if the value was invalid
   */
  public static PropertyValueType from(String value) throws IllegalArgumentException {
    value = Val.chkStr(value);
    if (value.length() == 0) {
      return PropertyValueType.STRING;
    } else {
      try {
        return PropertyValueType.valueOf(value.toUpperCase());
      } catch (IllegalArgumentException ex) {
        throw new IllegalArgumentException("Unrecognized property value type: "+value);
      }
    }
  }
  
}