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
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a table of contents tree.
 */
public class TocTree {
  
  /** instance variables ====================================================== */
  private List<TocItem> children = new ArrayList<TocItem>();
  private String        idProperty;
  private String        labelProperty;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public TocTree() {}
    
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
   * Gets the ID property.
   * @return the ID property
   */
  public String getIDProperty() {
    return this.idProperty;
  }
  
  /**
   * Sets the ID property.
   * @param attribute the ID property
   */
  public void setIDProperty(String attribute) {
    this.idProperty = attribute;
  }
  
  /**
   * Gets the label property.
   * @return the label property
   */
  public String getLabelProperty() {
    return this.labelProperty;
  }
  
  /**
   * Sets the label property.
   * @param attribute the label property
   */
  public void setLabelProperty(String attribute) {
    this.labelProperty = attribute;
  }
  
  /** methods ================================================================= */
  
  /**
   * Returns a JSON representation of the property.
   * @return the JSON string
   * @throws IOException if an exception occurs
   */
  public String asJson() throws IOException {
    PrintWriter pw = null;
    try {
      StringWriter sw = new StringWriter();
      pw = new PrintWriter(sw);
      this.toJson(pw,0,true);
      pw.flush();
      return sw.toString();
    } finally {
      try {if (pw != null) pw.close();} catch (Exception ef) {}
    }
  }
  
  /**
   * Returns an XML representation of the property.
   * @return the XML string
   * @throws IOException if an exception occurs
   */
  public String asXml() throws IOException {
    PrintWriter pw = null;
    try {
      StringWriter sw = new StringWriter();
      pw = new PrintWriter(sw);
      pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      this.toXml(pw,0);
      pw.flush();
      return sw.toString();
    } finally {
      try {if (pw != null) pw.close();} catch (Exception ef) {}
    }
  }
  
  /**
   * Builds a tree.
   * @param context the operation context
   * @param relativePath path the associated XML document
   * @return the tree
   */
  public static TocTree build(TocContext context, String relativePath) throws Exception {
        
    // create the DOM determine the root node
    Node root = null;
    Document dom = DomUtil.makeDomFromResourcePath(relativePath,false);
    NodeList nl = dom.getChildNodes(); 
    for (int i=0; i<nl.getLength(); i++) {
      Node ndChild = nl.item(i);
      if (ndChild.getNodeType() == Node.ELEMENT_NODE) { 
        String nodeName = Val.chkStr(ndChild.getNodeName());
        if (nodeName.equalsIgnoreCase("tree")) {
          root = ndChild;
        }
        break;
      }
    }
    
    // process the tree node
    if (root != null) {
      TocTree tree = null;
      Node ndClassName = root.getAttributes().getNamedItem("className");
      if (ndClassName == null) {
        tree = new TocTree();
      } else {
        tree = TocTree.makeTreeInstance(context,ndClassName.getNodeValue());
      }
      tree.processTreeNode(context,root);
      return tree;
    } else {
      String msg = "Unable to locate <tree> root node within: "+relativePath;
      throw new ConfigurationException(msg);
    }
    
  }
  
  /**
   * Generates the response string.
   * @param context the operation context
   * @return the response string
   * @throws Exception if an exception occurs
   */
  public String generateResponse(TocContext context) throws Exception {
    String fmt = Val.chkStr(context.getOutputFormat());
    boolean isJson = fmt.equalsIgnoreCase("") ||
                     fmt.equalsIgnoreCase("json") || 
                     fmt.equalsIgnoreCase("pjson")  ||
                     fmt.equalsIgnoreCase("application/json");
    if (isJson) {
      return this.asJson();
    } else {
      return this.asXml();
    } 
  }
  
  /**
   * Determines whether or not this property has children.
   * @return <code>true</code> if this property has children
   */
  public boolean hasChildren() {
    return (this.getChildren() != null) && (this.getChildren().size() > 0);
  }
  
  /**
   * Makes a TocTree instance based upon a class name.
   * @param context the operation context
   * @param className the fully qualified class name
   * @return the instance
   * @throws ClassNotFoundException if the class was not found
   * @throws InstantiationException if the class could not be instantiated
   * @throws IllegalAccessException if the class could not be accessed
   */
  public static TocTree makeTreeInstance(TocContext context, String className) 
    throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    className = Val.chkStr(className);
    if (className.length() == 0) {
      return new TocTree();
    } else {
      Class<?> cls = Class.forName(className);
      Object obj = cls.newInstance();
      if (obj instanceof TocTree) {
        return (TocTree)obj;
      } else {
        String msg = "The configured tree.className is invalid: "+className;
        throw new ConfigurationException(msg);
      }
    }
  }
  
  /**
   * Processes a TOC tree node.
   * @param context the operation context
   * @param node the tree node
   * @throws Exception if an exception occurs
   */
  public void processTreeNode(TocContext context, Node node) throws Exception {
    NodeList nlChildren = node.getChildNodes();
    for (int i=0; i<nlChildren.getLength(); i++) {
      Node ndChild = nlChildren.item(i);
      if (ndChild.getNodeType() == Node.ELEMENT_NODE) {
        String nodeName = Val.chkStr(ndChild.getNodeName());
        
        if (nodeName.equalsIgnoreCase("identifier")) {
          this.setIDProperty(ndChild.getTextContent());
          
        } else if (nodeName.equalsIgnoreCase("label")) {
          this.setLabelProperty(ndChild.getTextContent());
          
        } else if (nodeName.equalsIgnoreCase("item")) {
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
    
    String sIdProp = Val.chkStr(this.getIDProperty());
    String sLabelProp = Val.chkStr(this.getLabelProperty());
    
    writer.println(pfx+"{");
    
    if (sIdProp.length() > 0) {
      line = pfx2+"\"identifier\": \""+Val.escapeStrForJson(sIdProp)+"\"";
      if (hc || (sLabelProp.length() > 0)) line +=",";
      writer.println(line);
    }
    
    if (sLabelProp.length() > 0) {
      line = pfx2+"\"label\": \""+Val.escapeStrForJson(sLabelProp)+"\"";
      if (hc) line +=",";
      writer.println(line);
    }
        
    if (hc) {
      line = pfx2+"\"items\": [";
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
    
    String sIdProp = Val.chkStr(this.getIDProperty());
    String sLabelProp = Val.chkStr(this.getLabelProperty());

    writer.println(pfx+"<tree>");
    if (sIdProp.length() > 0) {
      writer.println(pfx2+"<identifier>"+Val.escapeXml(sIdProp)+"</identifier>");
    }
    if (sLabelProp.length() > 0) {
      writer.println(pfx2+"<label>"+Val.escapeXml(sLabelProp)+"</label>");
    }

    if (this.hasChildren()) {
      for (TocItem child: this.getChildren()) {
        child.toXml(writer,(depth+1));
      }
    }
    writer.println(pfx+"</tree>");
  }

}
