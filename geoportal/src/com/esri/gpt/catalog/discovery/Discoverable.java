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

/** 
 * Represents a property that can be discovered through query. 
 */
public class Discoverable extends DiscoveryComponent {
  
  /** instance variables ====================================================== */
  private PropertyMeaning meaning;
  private IStoreable storeable;
  
  /** constructors ============================================================ */
  
  /** 
   * Constructs with supplied client name. 
   * @param clientName the name recognized by the client
   */
  public Discoverable(String clientName) {
    super();
  }
  
  /** 
   * Constructs a discoverable based upon another. 
   * @param objectToBaseOn the discoverable that serves as the base
   */
  public Discoverable(Discoverable objectToBaseOn) {
    super();
    if (objectToBaseOn == null) {
      throw new IllegalArgumentException("Discoverable objectToBaseOn can't be null.");
    }
    setStoreable(objectToBaseOn.getStorable());
    setMeaning(objectToBaseOn.getMeaning());
  }
  
  /** properties ============================================================== */
    
  /** 
   * Gets the associated component within the underlying data store.
   * @return the underlying data store component
   */
  public IStoreable getStorable() {
    return storeable;
  }
  
  /** 
   * Sets the associated component within the underlying data store.
   * @param component the underlying data store component
   */
  public void setStoreable(IStoreable component) {
    this.storeable = component;
  }
  
  /** 
   * Gets the meaning behind the property.
   * @return the underlying meaning
   */
  public PropertyMeaning getMeaning() {
    return meaning;
  }
  
  /** 
   * Sets the meaning behind the property.
   * @param meaning the underlying meaning
   */
  public void setMeaning(PropertyMeaning meaning) {
    this.meaning = meaning;
  }
     
  /** methods ================================================================= */
  
  /**
   * Create a sortable property base upon this discoverable.
   * @return the new sortable.
   */
  public Sortable asSortable() {
    return new Sortable(this);
  }
  
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   */
  @Override
  public void echo(StringBuffer sb) {
    echo(sb,0);
  }
  
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   * @param depth the depth of the parent clause
   */
  public void echo(StringBuffer sb, int depth) {
    StringBuffer sbDepth = new StringBuffer();
    for (int i=0;i<2*depth;i++) sbDepth.append(" ");
    
    sb.append(sbDepth).append(getClass().getSimpleName()).append(":");
    sb.append("\n ").append(sbDepth);
    
    if (getStorable() == null) {
      sb.append(" storeable component is null");
    } else {
      sb.append(" storeable name=\"").append(getStorable().getName()).append("\"");
    }  
    
    if (getMeaning() == null) {
      sb.append("\n ").append(sbDepth);
      sb.append(" meaning is null");
    } else {
      getMeaning().echo(sb.append("\n"),depth+1);
    }
  }
   
}