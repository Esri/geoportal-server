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
package com.esri.gpt.control.search.browse;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents an item within a table of contents.
 */
public class TocItem {
  
  /** instance variables ====================================================== */
  private List<TocItem> children = new ArrayList<TocItem>();
  private boolean       isValid = true;
  private String        id;
  private String        name;
  private String        query;
  private String        resourceKey;
  private String        type;  
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public TocItem() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the children.
   * @return the children
   */
  public List<TocItem> getChildren() {
    return this.children;
  }
  
  /**
   * Sets the children.
   * @param children the children
   */
  public void setChildren(List<TocItem> children) {
    this.children = children;
  }
  
  /**
   * Gets the id.
   * @return the id
   */  
  public String getId() {
	return id;
  }

  /**
   * Sets the id.
   * @param id the id
   */
  public void setId(String id) {
	this.id = id;
  }

/**
   * Gets the name.
   * @return the name
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * Sets the name.
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * Gets the query string (rest).
   * @return the name
   */
  public String getQuery() {
    return this.query;
  }
  
  /**
   * Sets the query string (rest).
   * @param query query string
   */
  public void setQuery(String query) {
    this.query = query;
  }
  
  /**
   * Gets the UI property bundle resource key.
   * @return the resource key
   */
  public String getResourceKey() {
    return this.resourceKey;
  }
  
  /**
   * Sets the UI property bundle resource key.
   * @param resourceKey the resource key
   */
  public void setResourceKey(String resourceKey) {
    this.resourceKey = resourceKey;
  }
  
  /**
   * Gets the type (for TOC image display).
   * @return the type
   */
  public String getType() {
    return this.type;
  }
  
  /**
   * Sets the type (for TOC image display).
   * @param type the type
   */
  public void setType(String type) {
    this.type = type;
  }
  
  /** methods ================================================================= */
  
  /**
   * Determines whether or not this property has children.
   * @return <code>true</code> if this property has children
   */
  public boolean hasChildren() {
    return (this.getChildren() != null) && (this.getChildren().size() > 0);
  }
  
  /**
   * Invalidates the item.
   */
  public void invalidate() {
    this.isValid = false;
  }
  
  /**
   * Gets the flag indicating whether or not the item is valid.
   * @return <code>true</code> if the item is valid
   */
  public boolean isValid() {
    return this.isValid;
  }
  
  /**
   * Looks up the item name based upon the UI property bundle resource key.
   * @param msgBroker the message broker
   */
  public void lookupName(MessageBroker msgBroker) {
    String rKey = Val.chkStr(this.getResourceKey());
    if (rKey.length() > 0) {
      String rVal = Val.chkStr(msgBroker.retrieveMessage(rKey));
      if (rVal.length() > 0) {
        boolean hadName = (Val.chkStr(this.getName()).length() > 0);
        if (!hadName || !rVal.startsWith("??")) {
          this.setName(rVal);
        } 
      }
    }
  }
  
  /**
   * Makes a TocItem instance based upon a class name.
   * @param context the operation context
   * @param className the fully qualified class name
   * @return the instance
   * @throws ClassNotFoundException if the class was not found
   * @throws InstantiationException if the class could not be instantiated
   * @throws IllegalAccessException if the class could not be accessed
   */
  public static TocItem makeItemInstance(TocContext context, String className) 
    throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    className = Val.chkStr(className);
    if (className.length() == 0) {
      return new TocItem();
    } else {
      Class<?> cls = Class.forName(className);
      Object obj = cls.newInstance();
      if (obj instanceof TocItem) {
        return (TocItem)obj;
      } else {
        String msg = "The configured item.className is invalid: "+className;
        throw new ConfigurationException(msg);
      }
    }
  }
  
  /**
   * Processes a TOC item node.
   * @param context the operation context
   * @param node the item node
   * @throws Exception if an exception occurs
   */
  public void processItemNode(TocContext context, Node node) throws Exception {

    // process the name and type nodes
    boolean hadName = false;
    NodeList nlChildren = node.getChildNodes();
    for (int i=0; i<nlChildren.getLength(); i++) {
      Node ndChild = nlChildren.item(i);
      if (ndChild.getNodeType() == Node.ELEMENT_NODE) {
        String nodeName = Val.chkStr(ndChild.getNodeName());
        if (nodeName.equalsIgnoreCase("name")) {
          this.setName(ndChild.getTextContent());
          Node ndRes = ndChild.getAttributes().getNamedItem("resourceKey");
          if (ndRes != null) {
            this.setResourceKey(ndRes.getNodeValue());
            this.lookupName(context.getMessageBroker());
          }
          hadName = (Val.chkStr(this.getName()).length() > 0);
        } else if (nodeName.equalsIgnoreCase("type")) {
          this.setType(ndChild.getTextContent());
        } else if (nodeName.equalsIgnoreCase("id")) {
            this.setId(ndChild.getTextContent());
        }
      }
    }
    if (!hadName) this.invalidate();
    if (!this.isValid()) return;
    
    // process the query node
    for (int i=0; i<nlChildren.getLength(); i++) {
      Node ndChild = nlChildren.item(i);
      if (ndChild.getNodeType() == Node.ELEMENT_NODE) {
        String nodeName = Val.chkStr(ndChild.getNodeName());
        if (nodeName.equalsIgnoreCase("query")) {
          TocQueryProvider qProvider = null;
          Node ndClassName = ndChild.getAttributes().getNamedItem("className");
          if (ndClassName == null) {
            qProvider = new TocQueryProvider();
          } else {
            qProvider = TocQueryProvider.makeProviderInstance(context,ndClassName.getNodeValue());
          }
          qProvider.processQueryNode(context,this,ndChild);
        }
      }
    }
    if (!this.isValid()) return;
    
    // process the child items
    for (int i=0; i<nlChildren.getLength(); i++) {
      Node ndChild = nlChildren.item(i);
      if (ndChild.getNodeType() == Node.ELEMENT_NODE) {
        String nodeName = Val.chkStr(ndChild.getNodeName());
        if (nodeName.equalsIgnoreCase("item")) {
          TocItem subItem = null;
          Node ndClassName = ndChild.getAttributes().getNamedItem("className");
          if (ndClassName == null) {
            subItem = new TocItem();
          } else {
            subItem = TocItem.makeItemInstance(context,ndClassName.getNodeValue());
          }
          subItem.processItemNode(context,ndChild);
          if (subItem.isValid()) {
            this.getChildren().add(subItem);
          }
        }
      }
    }
    
  }
  
  /**
   * Writes a JSON representation of the property.
   * @param writer the writer
   * @param depth the depth of the parent
   * @throws IOException if an exception occurs
   */
  public void toJson(PrintWriter writer, int depth, boolean isLast) throws IOException {
    String pfx = "";
    for (int i=0;i<2*depth;i++) pfx += " ";
    String pfx2 = pfx+"  ";
    String line;
    boolean hc = this.hasChildren();
    
    String sId = Val.chkStr(this.getId());
    String sName = Val.chkStr(this.getName());
    String sType = Val.chkStr(this.getType());
    String sQuery = Val.chkStr(this.getQuery());
    
    writer.println(pfx+"{");
    
    if (sId.length() > 0) {
        line = pfx2+"\"id\": \""+Val.escapeStrForJson(sId)+"\"";
        if (hc || (sName.length() > 0) || (sType.length() > 0) || (sQuery.length() > 0)) line +=",";
        writer.println(line);
    }
    
    if (sName.length() > 0) {
      line = pfx2+"\"name\": \""+Val.escapeStrForJson(sName)+"\"";
      if (hc || (sType.length() > 0) || (sQuery.length() > 0)) line +=",";
      writer.println(line);
    }
    
    if (sType.length() > 0) {
      line = pfx2+"\"type\": \""+Val.escapeStrForJson(sType)+"\"";
      if (hc || (sQuery.length() > 0)) line +=",";
      writer.println(line);
    }
    
    if (sQuery.length() > 0) {
      line = pfx2+"\"query\": \""+Val.escapeStrForJson(sQuery)+"\"";
      if (hc) line +=",";
      writer.println(line);
    }
    
    if (hc) {
      line = pfx2+"\"children\": [";
      writer.println(line);
      for (int i=0; i<this.getChildren().size(); i++) {
        TocItem child = this.getChildren().get(i);
        boolean bLast = (i >= (this.getChildren().size() - 1));
        child.toJson(writer,(depth+2),bLast);
      }
      writer.println(pfx2+"]");
    }
    
    line = pfx+"}";
    if (!isLast) line +=",";
    writer.println(line);
    
  }
  
  /**
   * Writes an XML representation of the property.
   * @param writer the writer
   * @param depth the depth of the parent
   * @throws IOException if an exception occurs
   */
  public void toXml(PrintWriter writer, int depth) throws IOException {
    String pfx = "";
    for (int i=0;i<2*depth;i++) pfx += " ";
    String pfx2 = pfx+"  ";
    
    String sId = Val.chkStr(this.getId());
    String sName = Val.chkStr(this.getName());
    String sType = Val.chkStr(this.getType());
    String sQuery = Val.chkStr(this.getQuery());
    
    writer.println(pfx+"<item>");
    if (sId.length() > 0) {
        writer.println(pfx2+"<id>"+Val.escapeXml(sId)+"</id>");
      }
    if (sName.length() > 0) {
      writer.println(pfx2+"<name>"+Val.escapeXml(sName)+"</name>");
    }
    if (sType.length() > 0) {
      writer.println(pfx2+"<type>"+Val.escapeXml(sType)+"</type>");
    }
    if (sQuery.length() > 0) {
      writer.println(pfx2+"<query>"+Val.escapeXml(sQuery)+"</query>");
    }

    if (this.hasChildren()) {
      for (TocItem child: this.getChildren()) {
        child.toXml(writer,(depth+1));
      }
    }
    writer.println(pfx+"</item>");
  }

}
