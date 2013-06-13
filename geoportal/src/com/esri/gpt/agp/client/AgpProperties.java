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
package com.esri.gpt.agp.client;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * A map of item properties.
 */
public class AgpProperties {
    
  /** instance variables ====================================================== */
  private LinkedHashMap<String,AgpProperty> properties = 
          new LinkedHashMap<String,AgpProperty>();
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpProperties() {}
  
  /** methods ================================================================= */
  
  /**
   * Adds a property.
   * @param property the property
   */
  public void add(AgpProperty property) {
    this.properties.put(property.getName(),property);
  }
  
  /**
   * Gets a property by name.
   * @param name the property name
   * @return the property
   */
  public AgpProperty get(String name) {
    return this.properties.get(name);
  }
  
  /**
   * Gets a property by value name.
   * @param name the property name
   * @return the property value
   */
  public String getValue(String name) {
    AgpProperty property = this.get(name);
    if (property != null) {
      return property.getValue();
    }
    return null;
  }
  
  /**
   * The collection size.
   * @return the collection size
   */
  public int size() {
    return this.properties.size();
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(this.getClass().getCanonicalName());
    sb.append(" (").append(this.size()).append(") {");
    for (AgpProperty prop: this.values()) {
      sb.append("\n").append(prop.getName()).append("=").append(prop.getValue());
    }
    sb.append("\n}");
    return sb.toString();
  }
  
  /**
   * The collection members. 
   * @return the collection members
   */
  public Collection<AgpProperty> values() {
    return this.properties.values();
  }
  
}