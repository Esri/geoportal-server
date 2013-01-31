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
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * Represents a record discovered through query.
 */
public class DiscoveredRecord extends DiscoveryComponent {
      
  /** instance variables ====================================================== */
  private LinkedHashMap<String,Returnable> fieldMap;
  private String                           responseXml;
      
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public DiscoveredRecord() {
    super();
    fieldMap = new LinkedHashMap<String,Returnable>();
  }
        
  /** properties ============================================================== */
    
  /**
   * Gets the returnable fields associated with the record.
   * @return the returnable field collection
   */
  public Collection<Returnable> getFields() {
    return fieldMap.values();
  }
  
  /**
   * Gets the response XML.
   * <br/>Applicable for non Dublin Core based responses.
   * @return the response XML (can be null)
   */
  public String getResponseXml() {
    return this.responseXml;
  }
  /**
   * Sets the response XML.
   * <br/>Applicable for non Dublin Core based responses.
   * @param xml the response XML
   */
  public void setResponseXml(String xml) {
    this.responseXml = xml;
  }

  /** methods ================================================================= */
  
  /**
   * Adds a field to the record.
   * @param target the discoverable property associated with the field
   * @param values the data store values associated with the target 
   */
  public void addField(Discoverable target, Object[] values) {
    if (target == null) {
      throw new IllegalArgumentException("The discoverable target can't be null.");
    }
    Returnable field = new Returnable(target,values);
    //String key = target.getClientName();
    String key = target.getMeaning().getName();
    fieldMap.put(key,field);
  }
    
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   */
  @Override
  public void echo(StringBuffer sb) {
    sb.append(getClass().getSimpleName()).append(":");
    sb.append(" fields=").append(fieldMap.size()).append("");
    for (Returnable field: getFields()) {
      field.echo(sb.append("\n"));
    }
  }

}
