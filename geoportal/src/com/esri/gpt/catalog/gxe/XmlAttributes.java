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
package com.esri.gpt.catalog.gxe;
import com.esri.gpt.framework.collection.CaseInsensitiveMap;
import java.util.Collection;

/**
 * An XML attribute collection.
 */
public class XmlAttributes {
  
  /** instance variables ====================================================== */
  private CaseInsensitiveMap<XmlAttribute> members = new CaseInsensitiveMap<XmlAttribute>(false);

  /** constructors ============================================================ */
  
  /** Default constructor */
  public XmlAttributes() {}
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   * @param parent the parent
   */
  public XmlAttributes(XmlAttributes objectToDuplicate, XmlElement parent) {
    if (objectToDuplicate != null) {
      Collection<XmlAttribute> values = objectToDuplicate.values();
      if (values != null) {
        for (XmlAttribute member: values) {
          this.add(member.duplicate(parent));
        }
      }
    }
  }
    
  /** methods ================================================================= */
  
  /**
   * Adds a member to the collection.
   * @param member the member to add
   */
  public void add(XmlAttribute member) {
    if ((member != null) && (member.getNodeInfo() != null)) {
      String key = member.getNodeInfo().getNamespaceURI();
      key += ":::"+member.getNodeInfo().getLocalName();
      this.members.put(key.toLowerCase(),member);
    }
  }
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new XmlAttributes(this,parent);
   * @param parent the parent
   * @return the duplicated object
   */
  public XmlAttributes duplicate(XmlElement parent) {
    return new XmlAttributes(this,parent);
  }
  
  /**
   * Finds an attribute within the collection 
   * @param namespaceURI the namespace
   * @param nodeName the local node name
   * @return the attribute
   */
  public XmlAttribute find(String namespaceURI, String nodeName) {
    String key = namespaceURI+":::"+nodeName;
    return this.members.get(key);
  }
    
  /**
   * Returns the size of the collection.
   * @return the size
   */
  public int size() {
    return this.members.size();
  }
    
  /**
   * Returns the collection of values.
   * @return the values
   */
  public Collection<XmlAttribute> values() {
    return this.members.values();
  }
  
}
