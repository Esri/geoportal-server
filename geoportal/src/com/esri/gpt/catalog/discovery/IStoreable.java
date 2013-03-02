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
 * Represents the underlying data store component associated with a 
 * discoverable property.
 */
public interface IStoreable {
  
  /** 
   * Gets the name of the underlying data store component. 
   * @return the underlying name
   */
  public String getName();
  
  /** 
   * Gets the underlying values to store.
   * <p/>
   * The values array wild be null if the field was not populated
   * within the associated document.
   * <p/> 
   * There can be multiple values associated with a field, keywords 
   * for instance.
   * @return the data values to store
   */
  public Object[] getValues();
  
  /** 
   * Sets the value collection to a single object value.
   * @param value the object value to set
   */
  public void setValue(Object value);
  
  /** 
   * Sets the underlying values to store.
   * <p/>
   * The values array wild be null if the field was not populated
   * within the associated document.
   * <p/> 
   * There can be multiple values associated with a field, keywords 
   * for instance.
   * @param values the data values to store
   */
  public void setValues(Object[] values);
  
}
