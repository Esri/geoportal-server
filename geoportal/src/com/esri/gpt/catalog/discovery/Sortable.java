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
 * Represents a stored property that can be be used to sort the results of a query.
 */
public class Sortable extends Discoverable {
  
  /** instance variables ====================================================== */
  private SortDirection direction = SortDirection.ASC;
  
  /** constructors ============================================================ */
  
  /** 
   * Constructs a sortable based upon a discoverable. 
   * @param objectToBaseOn the discoverable that serves as the base
   */
  public Sortable(Discoverable objectToBaseOn) {
    super(objectToBaseOn);
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the sort direction.
   * @return the sort direction
   */
  public SortDirection getDirection() {
    return direction;
  }
  /**
   * Sets the sort direction.
   * @param direction the sort direction
   */
  public void setDirection(SortDirection direction) {
    this.direction = direction;
  }
  
  /** methods ================================================================= */
  
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   * @param depth the depth of the parent clause
   */
  public void echo(StringBuffer sb, int depth) {
    StringBuffer sbDepth = new StringBuffer();
    for (int i=0;i<2*depth;i++) sbDepth.append(" ");
    
    super.echo(sb,depth);
    sb.append("\n ").append(sbDepth);
    sb.append(" direction=").append(getDirection()).append("");
  }
  
  /** enumerations ============================================================ */
  
  /**
   * The enumeration of sorting directions.
   */
  public enum SortDirection {
    ASC,
    DESC;
    
    /**
     * Makes a result type from a supplied string value.
     * <br/>I null is supplied, ResultType.hits is returned.
     * @param value the value
     * @return the result type
     * @throws IllegalArgumentException if the value was invalid
     */
    public static SortDirection from(String value) throws IllegalArgumentException {
      if (value == null) {
        return SortDirection.ASC;
      } else {
        try {
          return SortDirection.valueOf(Val.chkStr(value).toUpperCase());
        } catch (IllegalArgumentException ex) {
          throw new IllegalArgumentException("Sort direction must be one of: ASC,DESC");
        }
      }
    }
  }
     
}