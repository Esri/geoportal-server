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
import com.esri.gpt.framework.util.Val;
import java.util.Collection;

/**
 * Represents an XML element.
 */
public class XmlElement extends XmlNode {
    
  /** instance variables ====================================================== */
  private XmlAttributes attributes = new XmlAttributes();
  private XmlElements   children = new XmlElements();
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public XmlElement() {
    super();
  }
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   * @param parent the parent
   */
  public XmlElement(XmlElement objectToDuplicate, XmlElement parent) {
    super(objectToDuplicate,parent);
    if (objectToDuplicate != null) {
      if (objectToDuplicate.getAttributes() != null) {
        this.setAttributes(objectToDuplicate.getAttributes().duplicate(this));
      }
      if (objectToDuplicate.getChildren() != null) {
        this.setChildren(objectToDuplicate.getChildren().duplicate(this));
      }
    } 
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the attributes.
   * @return the attributes
   */
  public XmlAttributes getAttributes() {
    return this.attributes;
  }
  /**
   * Sets the attributes.
   * @param attributes the attributes
   */
  public void setAttributes(XmlAttributes attributes) {
    this.attributes = attributes;
  }
  
  /**
   * Gets the children.
   * @return the children
   */
  public XmlElements getChildren() {
    return this.children;
  }
  /**
   * Sets the children.
   * @param children the children
   */
  public void setChildren(XmlElements children) {
    this.children = children;
  }
  
  /** methods ================================================================= */
  
  /**
   * Appends child elements.
   * @param elements the elements to append
   */
  public void appendChildren(XmlElements elements) {
    if (elements == null) return;
    Collection<XmlElement> refValues = elements.values();
    if (refValues == null) return;   
    for (XmlElement refChild: refValues) {
      if ((refChild != null) && (refChild.getNodeInfo() != null)) {
        //this.getChildren().add(refChild.duplicate(this));
        refChild.setParent(this);
        this.getChildren().add(refChild);
      }
    }
  }
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new XmlElement(this,parent);
   * @param parent the parent
   * @return the duplicated object
   */
  public XmlElement duplicate(XmlElement parent) {
    return new XmlElement(this,parent);
  }
  
  /**
   * Ensures that the attribute collection is not null and returns the collection.
   * @return the attributes
   */
  public XmlAttributes ensureAttributes() {
    if (this.getAttributes() == null) {
      this.setAttributes(new XmlAttributes());
    }
    return this.getAttributes();
  }
  
  /**
   * Ensures that the child collection is not null and returns the collection.
   * @return the children
   */
  public XmlElements ensureChildren() {
    if (this.getChildren() == null) {
      this.setChildren(new XmlElements());
    }
    return this.getChildren();
  }
  
  /**
   * Gets a child attribute value
   * @param namespaceURI the namespace URI
   * @param name the attribute name
   * @return the associated value (can be null);
   */
  public String getAttributeValue(String namespaceURI, String name) {
    if (this.getAttributes() != null) {
      XmlAttribute attr = this.getAttributes().find(namespaceURI,name);
      if (attr != null) return attr.getNodeInfo().getNodeValue();
    }
    return null;
  }
   
  /**
   * Imports attributes from a base collection into the local collection.
   * <br/>When preserveLocals=true, the local attributes take precedence
   * (i.e. an attribute that is in both collections will not be overriden locally).
   * @param base the base collection
   * @param preserveLocals true if local attributes should be preserved
   */
  public void importAttributes(XmlAttributes base, boolean preserveLocals) {
    if (base != null) {
      Collection<XmlAttribute> refValues = base.values();
      if (refValues != null) {
        XmlAttributes localAttributes = this.ensureAttributes();
        for (XmlAttribute refAttr: refValues) {
          if ((refAttr != null) && (refAttr.getNodeInfo() != null)) {
            String refNS = refAttr.getNodeInfo().getNamespaceURI();
            String refName = refAttr.getNodeInfo().getLocalName();  
            
            //boolean bSet = true;
            //if (refNS.equals(GxeSerializer.URI_GXE)) {
              //if (refName.equals("src")) bSet = false;
            //}
            
            if (!preserveLocals) {
              //localAttributes.add(refAttr.duplicate(this));
              refAttr.setParent(this);
              localAttributes.add(refAttr);
            } else {
              XmlAttribute localAttr = localAttributes.find(refNS,refName);
              if (localAttr == null) {
                //localAttributes.add(refAttr.duplicate(this));
                refAttr.setParent(this);
                localAttributes.add(refAttr);
              } 
            }
          }
        }
      }
    }
  } 

  /**
   * Imports elements from a base collection into the local collection.
   * @param base the base collection
   */
  public void importChildren(XmlElements base) {
    if (base == null) return;
    Collection<XmlElement> refValues = base.values();
    if (refValues == null) return;   

    for (XmlElement refChild: refValues) {
      if ((refChild != null) && (refChild.getNodeInfo() != null)) {
        XmlElement overridable = this.getChildren().findOverridable(refChild);
        if (overridable != null) {
          //this.getChildren().replace(overridable,refChild.duplicate(this));
          refChild.setParent(this);
          this.getChildren().replace(overridable,refChild);
        } else {
          XmlElement extensible = this.getChildren().findExtensible(refChild);
          if (extensible != null) {
            extensible.importAttributes(refChild.getAttributes(),false);
            extensible.importChildren(refChild.getChildren());
            String sText = Val.chkStr(refChild.getNodeInfo().getNodeValue());
            if (sText.length() > 0) {
              extensible.getNodeInfo().setNodeValue(sText);
            }
          } else {
            //this.getChildren().add(refChild.duplicate(this));
            refChild.setParent(this);
            this.getChildren().add(refChild);
          }
        }
      }
    }
  }
  
}
