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
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.util.Val;

/** 
 * Contains basic information for a Dublin Core element. 
 */
public class DcElement extends DiscoveryComponent {
  
  /** instance variables ====================================================== */
  private StringSet aliases = new StringSet();
  private String elementName = "";
  private String scheme = "";
  
  /** constructors ============================================================ */
  
  /** 
   * Constructs with a supplied element name. 
   *  @param elementName the element name
   */
  public DcElement(String elementName) {
    this.setElementName(elementName);
  }
    
  /** properties ============================================================== */
  
  /** 
   * Gets the aliased names recognized by the client. 
   * @return the client aliases
   */
  public StringSet getAliases() {
    return aliases;
  }
  
  /** 
   * Gets the element name.
   * @return the element name
   */
  public String getElementName() {
    return elementName;
  }
  /** 
   * Sets the element name. 
   * @param elementName the element name
   */
  public void setElementName(String elementName) {
    this.elementName = Val.chkStr(elementName);
  }
  
  /** 
   * Gets the scheme attribute value associated with the element.
   * @return the scheme attribute value 
   */
  public String getScheme() {
    return scheme;
  }
  /** 
   * Sets the scheme attribute value associated with the element.
   * @param scheme the scheme attribute value 
   */
  public void setScheme(String scheme) {
    this.scheme = Val.chkStr(scheme);
  }
     
  /** methods ================================================================= */
  
  
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
    sb.append(" elementName=\"").append(getElementName()).append("\"");
    sb.append(" scheme=\"").append(getScheme()).append("\"");
    sb.append(" aliases=").append(getAliases());
  }
   
}