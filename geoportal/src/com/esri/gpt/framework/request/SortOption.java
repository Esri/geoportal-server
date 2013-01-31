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
package com.esri.gpt.framework.request;
import com.esri.gpt.framework.util.Val;
import java.io.Serializable;

/**
 * Defines the option for sorting results.
 */
public class SortOption implements Serializable {

// class variables =============================================================
  
// instance variables ==========================================================
private String        _columnKey = "";
private SortDirection _direction = SortDirection.defaultValue();
private SortOptionStyleMap _styleMap;
  
// constructors ================================================================

/** Default constructor. */
public SortOption() {
  _styleMap = new SortOptionStyleMap(this);
}
  
// properties ==================================================================

/**
 * Gets the column key for the sort.
 * @return the column key
 */
public String getColumnKey() {
  return _columnKey;
}
/**
 * Sets the column key for the sort.
 * @param key the column key
 */
public void setColumnKey(String key) {
  _columnKey = Val.chkStr(key);
}

/**
 * Sets the column key for the sort with toggling options.
 * @param key the column key
 * @param toggleDirection if true toggle the direction if the
 *        new column key matches the current column key
 * @param defaultToggleDirection the direction to use when 
 *        toggling if the new column key does not match the
 *        current column key
 *        
 */
public void setColumnKey(String key, 
                         boolean toggleDirection,
                         String defaultToggleDirection) {
  key = Val.chkStr(key);
  if (!toggleDirection) {
    _columnKey = key;
  } else {
    if (key.equalsIgnoreCase(_columnKey)) {
      if (getDirection().equals(SortDirection.asc)) {
        setDirection(SortDirection.desc);
      } else {
        setDirection(SortDirection.asc);
      }
    } else {
      setDirection(SortDirection.checkValue(defaultToggleDirection));
    }
    _columnKey = key;
  }
}

/**
 * Gets the sort direction.
 * @return the sort direction.
 */
public SortDirection getDirection() {
  return _direction;
}
/**
 * Sets the sort direction.
 * @param direction the sort direction
 */
public void setDirection(SortDirection direction) {
  _direction = direction;
}
/**
 * Sets the sort direction.
 * @param direction the sort direction
 */
public void setDirection(String direction) {
  _direction = SortDirection.checkValue(direction);
}

/**
 * Gets the style map.
 * @return the style map
 */
public SortOptionStyleMap getStyleMap() {
  return _styleMap;
}

// methods =====================================================================

/**
 * Gets the style class for a column key.
 * <br/>"ascending" is returned if the sort direction is ascending and
 * the supplied column key matches the active column key
 * <br/>"descending" is returned if the sort direction is descending and
 * the supplied column key matches the active column key
 * <br/>"" is returned if the the supplied column key does not match the
 * active column key 
 * @param columnKey the subject column key
 * @return the style class for the column key 
 */
public String getStyleClass(String columnKey) {
  if ((getColumnKey().length() > 0) && 
       getColumnKey().equalsIgnoreCase(columnKey)) {
    if (getDirection().equals(SortDirection.asc)) {
      return "ascending";
    } else {
      return "descending";
    }
  }
  return "";
}

// enums =======================================================================
/**
 * An enumeration describing a the sort direction.
 */
public enum SortDirection {
  
  /** Ascending order (default value). */
  asc,
  /** Descending order. */
  desc;

  /**
   * Checks the value of a String to determine the corresponding enum.
   * @param direction the string to check
   * @return the corresponding enum (default is OrderByDirection.asc)
   */
  public static SortDirection checkValue(String direction) {
    try {
      return SortDirection.valueOf(Val.chkStr(direction));
    } catch (IllegalArgumentException ex) {
      return SortDirection.defaultValue();
    }
  }
  
  /**
   * Returns the default value for the enum.
   * @return OrderByDirection.asc
   */
  public static SortDirection defaultValue() {
    return SortDirection.asc;
  }
}

}
