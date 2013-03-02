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
import com.esri.gpt.framework.geometry.Envelope;

/** 
 * A discoverable property that contains an Object array of associated stored values
 * the were discovered through query.
 */
public class Returnable extends Discoverable {
  
  /** instance variables ====================================================== */
  private Object[] values;
  
  /** constructors ============================================================ */
  
  /** 
   * Constructs a returnable based upon a discoverable and it's associated values. 
   * @param objectToBaseOn the discoverable target that serves as the base
   * @param values the data store values associated with the discoverable
   */
  protected Returnable(Discoverable objectToBaseOn, Object[] values) {
    super(objectToBaseOn);
    this.values = values;
  }
  
  /** properties ============================================================== */
  
  /** 
   * Gets the underlying data store values associated with the 
   * discoverable target. 
   * <p/>
   * The values array will be null if the field was not populated
   * within the associated document.
   * <p/> 
   * There can be multiple values associated with a field, keywords 
   * for instance.
   * <p/>
   * The Objects in the array will be typed with the property value
   * type returned by the getMeaning().getValueType() method for this class. As an 
   * example if getMeaning().getValueType() returns PropertyValueType.TIMESTAMP, 
   * you can cast an object as: 
   * <br/>(java.sql.Timestamp)getValues()[0]
   * @return the data store field values
   */
  public Object[] getValues() {
    return values;
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
    if (getValues() == null) {
      sb.append("\n  ").append(sbDepth).append("values=null");
    } else {
      for (Object o: getValues()) {
        if (o instanceof Envelope) {     
          ((Envelope)o).echo(sb.append("\n  ").append(sbDepth));
        } else {
          sb.append("\n  ").append(sbDepth).append(o);
        }
      }
    }
  }
     
}