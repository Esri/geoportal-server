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
import java.util.ArrayList;
import java.util.List;

/**
 * An XML element collection.
 */
public class XmlElements {
  
  /** instance variables ====================================================== */
  private List<XmlElement> members = new ArrayList<XmlElement>();
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public XmlElements() {}
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   * @param parent the parent
   */
  public XmlElements(XmlElements objectToDuplicate,XmlElement parent) {
    if (objectToDuplicate != null) {
      List<XmlElement> values = objectToDuplicate.values();
      if (values != null) {
        for (XmlElement member: values) {
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
  public void add(XmlElement member) {
    if (member != null) {
      this.members.add(member);
    }
  }
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new XmlElements(this,parent);
   * @param parent the parent
   * @return the duplicated object
   */
  public XmlElements duplicate(XmlElement parent) {
    return new XmlElements(this,parent);
  }
  
  /**
   * Finds an extensible element within the collection. 
   * @param target the target to match (same element name and namespace;
   * @return the extensible element (null if none)
   */
  public XmlElement findExtensible(XmlElement target) {
    return this.findMatchingElement(target,"extensible","true");
  }
  
  /**
   * Finds a matching child element. 
   * @param target the target to match (same element name and namespace)
   * @param gxdAttributeName a configured attribute name the the matching child must possess
   *   (within the GXD namespace, typically "overridable" or "extensible")
   * @param gxdAttributeValue the attribute value that must match (typically "true")
   * @return the matching element (null if none)
   */
  public XmlElement findMatchingElement(XmlElement target, 
                                        String gxdAttributeName, 
                                        String gxdAttributeValue) {
    String namespaceURI = target.getNodeInfo().getNamespaceURI();
    String nodeName = target.getNodeInfo().getLocalName();
    List<XmlElement> values = this.values();
    String uriGxd = GxeContext.URI_GXE;
    for (XmlElement child: values) {
      boolean bMatch = false;
      if (child.getAttributes() != null) {
        XmlAttribute attribute = child.getAttributes().find(uriGxd,gxdAttributeName);
        if ((attribute != null) && (attribute.getNodeInfo().getNodeValue() != null)) {
          bMatch = attribute.getNodeInfo().getNodeValue().equals(gxdAttributeValue);
        }
      }
      if (bMatch) {
        bMatch = false;
        String ns = child.getNodeInfo().getNamespaceURI();
        String name = child.getNodeInfo().getLocalName();
        if (namespaceURI == null) {
          bMatch = (ns == null);
        } else if (ns != null) {
          bMatch = (ns.equals(namespaceURI));
        }
        if (bMatch && name.equals(nodeName)) return child;
      }
    }
    return null;
  }
  
  /**
   * Finds an overridable element within the collection. 
   * @param target the target to match (same element name and namespace;
   * @return the overridable element (null if none)
   */
  public XmlElement findOverridable(XmlElement target) {
    return this.findMatchingElement(target,"overridable","true");
  }
  
  /**
   * Replaces an element within the collection.
   * @param elementToReplace the element to be replaced
   * @param newElement the new element
   */
  public void replace(XmlElement elementToReplace, XmlElement newElement) {
    List<XmlElement> values = this.members;
    int nIdx = -1;
    for (XmlElement child: values) {
      nIdx++;
      if (child == elementToReplace) {
        values.set(nIdx,newElement);
        return;
      }
    }
  }
  
  /**
   * Resets the parent for all child elements..
   * @param parent the new parent
   */
  public void resetParent(XmlElement parent) {
    List<XmlElement> values = this.members;
    for (XmlElement child: values) {
      child.setParent(parent);
    }
  }
    
  /**
   * Returns the size of the collection.
   * @return the size
   */
  public int size() {
    return this.members.size();
  }
  
  /**
   * Returns the list of values.
   * @return the values
   */
  public List<XmlElement> values() {
    return this.members;
  }
  
}
