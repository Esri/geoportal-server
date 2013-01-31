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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles the loading of a Geoportal XML editor definition.
 */
public class GxeLoader {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(GxeLoader.class.getName());
     
  /** constructors ============================================================ */
  
  /** Default constructor */
  public GxeLoader() {}
      
  /** methods ================================================================= */
  
  /**
   * Checks the I18N (internationalization and localization) attributes for an XmlElement.
   * <br/>Any element that has a g:targetName attribute with no g:label attribute will be processed.
   * <br/>A g:label attribute will be added to the element, value = "$i18nBase."+localNameForTarget
   * <br/>E.g. <g:element g:targetName="dc:title" results in g:label="$i18nBase.title"
   * @param context the processing context
   * @param file the file that is currently being processed
   * @param domNode the active DOM node
   * @param gxeElement the active XmlElement
   * @throws Exception if an exception occurs
   */
  protected void checkI18NAttributes(GxeContext context, 
                                     GxeFile file, 
                                     Node domNode, 
                                     XmlElement gxeElement) throws Exception {
    XmlAttributes attributes = gxeElement.getAttributes();
    XmlAttribute attrLabel = attributes.find(GxeContext.URI_GXE,"label");
    if (attrLabel == null) {
      XmlAttribute attrTargetName = attributes.find(GxeContext.URI_GXE,"targetName");
      if (attrTargetName != null) {
        String targetName = Val.chkStr(attrTargetName.getNodeInfo().getNodeValue());
        int nIdx = targetName.indexOf(":");
        if (nIdx != -1) targetName = Val.chkStr(targetName.substring(nIdx+1));
        if (targetName.length() > 0) {
          XmlAttribute attribute = new XmlAttribute();
          attribute.setParent(gxeElement);
          attribute.getNodeInfo().setNamespaceURI(GxeContext.URI_GXE);
          attribute.getNodeInfo().setLocalName("label");
          attribute.getNodeInfo().setNodeValue("$i18nBase."+targetName);
          attributes.add(attribute);
        }
      }
    }
  }
  
  /**
   * Gets the text associated with a node. 
   * @param domNode the subject node
   * @param first if true, get the first text node associated with an element node
   * @return the text (can be null)
   */
  protected String getTextContent(Node domNode, boolean first) {
    String text = null;
    if (domNode.getNodeType() == Node.ATTRIBUTE_NODE ) {
      text = domNode.getNodeValue();
      if (text != null) text = Val.chkStr(text);
    } else if (domNode.getNodeType() == Node.TEXT_NODE) {
      text = domNode.getNodeValue();
    } else if (domNode.getNodeType() == Node.CDATA_SECTION_NODE) {
      text = domNode.getNodeValue();
      
    } else if (domNode.getNodeType() == Node.ELEMENT_NODE) {
      if (first) {
        NodeList nl = domNode.getChildNodes();
        for (int i=0;i<nl.getLength();i++) {  
          Node nd = nl.item(i);
          if (nd.getNodeType() == Node.TEXT_NODE) {
            text = nd.getNodeValue();
          } else if (nd.getNodeType() == Node.CDATA_SECTION_NODE) {
            text = nd.getNodeValue();
          } else {
            break;
          }
        }
      } else {
        text = domNode.getTextContent();
      }
      if (text != null) {
        text = Val.chkStr(text);
        if (text.length() == 0) {
          text = null;
        }
      }
    }
    return text;
  }
  
  /**
   * Loads a Geoportal XML editor definition.
   * <br/>The file location within the definition must be set prior invoking this method.
   * @param context the processing context
   * @param definition the definition to be loaded
   * @throws Exception if an exception occurs
   */
  public void loadDefinition(GxeContext context, GxeDefinition definition) throws Exception {
    context.setActiveDefinition(definition);
    GxeFile file = new GxeFile();
    file.setLocation(definition.getFileLocation());
    file.setIsRoot(true);
    Document dom = file.loadDom();
    Node domRoot = file.findRoot(dom);
    XmlElement gxeRoot = new XmlElement();
    definition.setRootElement(gxeRoot);
    this.loadXmlElement(context,file,domRoot,gxeRoot);
  }
  
  /**
   * Loads the XmlAttributes associated with an XmlElement.
   * @param context the processing context
   * @param file the file that is currently being processed
   * @param domNode the active DOM node
   * @param gxeElement the active XmlElement
   * @throws Exception if an exception occurs
   */
  protected void loadXmlAttributes(GxeContext context, 
                                   GxeFile file, 
                                   Node domNode, 
                                   XmlElement gxeElement) throws Exception { 
    NamedNodeMap nnm = domNode.getAttributes();
    for (int i=0; i<nnm.getLength(); i++) {
      Node nd = nnm.item(i);
      String nn = Val.chkStr(nd.getNodeName());
      if (!nn.equals("xmlns") && (!nn.startsWith("xmlns:"))) {
        XmlAttribute attribute = new XmlAttribute();
        attribute.setParent(gxeElement);
        this.loadXmlNodeInfo(context,file,nd,attribute);
        gxeElement.ensureAttributes().add(attribute);
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest("Loading: "+file.getLocation()+" "+domNode.getNodeName()+"@"+nn+"="+
              Val.chkStr(attribute.getNodeInfo().getNodeValue()));
        }
      }
    }
  }
  
  /**
   * Loads the child XmlElements associated with an XmlElement.
   * @param context the processing context
   * @param file the file that is currently being processed
   * @param domNode the active DOM node
   * @param gxeElement the active XmlElement
   * @throws Exception if an exception occurs
   */
  protected void loadXmlChildren(GxeContext context, 
                                 GxeFile file, 
                                 Node domNode, 
                                 XmlElement gxeElement) throws Exception {
    NodeList nl = domNode.getChildNodes(); 
    for (int i=0; i<nl.getLength(); i++) {
      Node nd = nl.item(i);
      if (nd.getNodeType() == Node.ELEMENT_NODE) { 
        String ns = Val.chkStr(nd.getNamespaceURI());
        String ln = Val.chkStr(nd.getLocalName());
                  
        if (ns.equals(GxeContext.URI_GXE) && ln.equals("container")) {
          boolean bLoadChildren = true;
          Node attr = nd.getAttributes().getNamedItemNS(GxeContext.URI_GXE,"rendered");
          if (attr != null) {
            String s = Val.chkStr(attr.getNodeValue());
            
            if (s.equals("$editor.isOriginalMode")) bLoadChildren = false;
            else if (s.equals("$editor.isExpertMode")) bLoadChildren = false;
            else if (s.equals("$editor.isAdvancedMode")) bLoadChildren = false;
            else if (s.equals("$editor.isSimplifiedMode")) bLoadChildren = true;
            
            /*
            if (s.equals("$editor.isOriginalMode")) bLoadChildren = true;
            else if (s.equals("$editor.isExpertMode")) bLoadChildren = false;
            else if (s.equals("$editor.isAdvancedMode")) bLoadChildren = true;
            else if (s.equals("$editor.isSimplifiedMode")) bLoadChildren = false;
            */
          }
          if (bLoadChildren) {
            this.loadXmlChildren(context,file,nd,gxeElement);
          }
          
        } else if (ns.equals(GxeContext.URI_GXE) && ln.equals("import")) {
          String refLocation = null;
          Node ndSrc = nd.getAttributes().getNamedItemNS(GxeContext.URI_GXE,"src");
          //if (ndSrc == null) ndSrc = nd.getAttributes().getNamedItemNS(GxeContext.URI_GXE,"schemaLocation");
          if (ndSrc != null) refLocation = Val.chkStr(ndSrc.getNodeValue());
          if ((refLocation != null) && (refLocation.length() > 0)) {
            
            // load the import element
            XmlElement importElement = new XmlElement();
            this.loadXmlAttributes(context,file,nd,importElement); 
            this.loadXmlChildren(context,file,nd,importElement);
            
            // load the referenced document
            XmlElement refElement = new XmlElement();
            GxeFile refFile = new GxeFile(file,refLocation);
            Document refDom = refFile.loadDom();
            Node refRoot = refFile.findRoot(refDom);
            this.loadXmlElement(context,refFile,refRoot,refElement);

            String ns2 = Val.chkStr(refRoot.getNamespaceURI());
            String ln2 = Val.chkStr(refRoot.getLocalName());
            boolean bImportChildrenOnly = false;
            if (ns2.equals(GxeContext.URI_GXE) && ln2.equals("attributeGroup")) {
              bImportChildrenOnly = true;
            } else if (ns2.equals(GxeContext.URI_GXE) && ln2.equals("group")) {
              bImportChildrenOnly = true;
            } else if (ns2.equals(GxeContext.URI_GXE) && ln2.equals("container")) {
              bImportChildrenOnly = true;
            } 
            
            if (bImportChildrenOnly) {
              refElement.importChildren(importElement.getChildren());
              gxeElement.appendChildren(refElement.getChildren());
            } else {
              refElement.setParent(gxeElement);
              refElement.importAttributes(importElement.getAttributes(),false);
              refElement.importChildren(importElement.getChildren());
              gxeElement.ensureChildren().add(refElement);
            }
                        
          } else {
            String msg = "A <g:import> element has an empty g:src attribute: "+file.getLocation();
            LOGGER.config(msg);
          }          
          
        } else {
          XmlElement child = new XmlElement();
          child.setParent(gxeElement);
          this.loadXmlElement(context,file,nd,child);
          gxeElement.ensureChildren().add(child);
        }
      }
    }
  }
  
  /**
   * Loads an XmlElement.
   * @param context the processing context
   * @param file the file that is currently being processed
   * @param domNode the active DOM node
   * @param gxeElement the active XmlElement
   * @throws Exception if an exception occurs
   */
  protected void loadXmlElement(GxeContext context, 
                                GxeFile file, 
                                Node domNode, 
                                XmlElement gxeElement) throws Exception {
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("Loading: "+file.getLocation()+" "+domNode.getNodeName());
    }
    
    this.loadXmlNodeInfo(context,file,domNode,gxeElement);
    this.loadXmlAttributes(context,file,domNode,gxeElement);
    
    String refLocation = null;
    XmlAttribute attrExtends = gxeElement.getAttributes().find(GxeContext.URI_GXE,"extends");
    if (attrExtends != null) refLocation = Val.chkStr(attrExtends.getNodeInfo().getNodeValue());
    if ((refLocation != null) && (refLocation.length() > 0)) {
      
      // load the referenced document
      XmlElement refElement = new XmlElement();
      GxeFile refFile = new GxeFile(file,refLocation);
      Document refDom = refFile.loadDom();
      Node refRoot = refFile.findRoot(refDom);
      this.loadXmlElement(context,refFile,refRoot,refElement);
      
      // extend the local element using the reference element
      gxeElement.importAttributes(refElement.getAttributes(),true);
      gxeElement.setChildren(refElement.getChildren());
      gxeElement.getChildren().resetParent(gxeElement);
      
      // import the children of the local element
      XmlElement tmpElement = new XmlElement();
      this.loadXmlChildren(context,file,domNode,tmpElement);
      gxeElement.importChildren(tmpElement.getChildren());
      
    } else {
      this.loadXmlChildren(context,file,domNode,gxeElement);  
    }
    
    this.checkI18NAttributes(context,file,domNode,gxeElement);
    this.onXmlElementLoaded(context,file,domNode,gxeElement);
  }
  
  /**
   * Loads the XmlNodeInfo associated with an XmlNode.
   * @param context the processing context
   * @param file the file that is currently being processed
   * @param domNode the active DOM node
   * @param gxeNode the active XmlNode (XmlElement or XmlAttribute)
   * @throws Exception if an exception occurs
   */
  protected void loadXmlNodeInfo(GxeContext context, 
                                 GxeFile file, 
                                 Node domNode, 
                                 XmlNode gxeNode) throws Exception {
    XmlNodeInfo nodeInfo = gxeNode.getNodeInfo();
    nodeInfo.setLocalName(domNode.getLocalName());
    nodeInfo.setNamespacePrefix(domNode.getPrefix());
    nodeInfo.setNamespaceURI(domNode.getNamespaceURI());
    
    String text = this.getTextContent(domNode,true);
    nodeInfo.setNodeValue(text);
  }
  
  /**
   * Fired when an XmlElement has been loaded.
   * @param context the processing context
   * @param file the file that is currently being processed
   * @param domNode the active DOM node
   * @param gxeElement the active XmlElement
   * @throws Exception if an exception occurs
   */
  public void onXmlElementLoaded(GxeContext context, 
                                 GxeFile file, 
                                 Node domNode, 
                                 XmlElement gxeElement) throws Exception {
  }
    
}
