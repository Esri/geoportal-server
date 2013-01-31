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
package com.esri.gpt.catalog.schema.indexable;
import com.esri.gpt.catalog.schema.CfgContext;
import com.esri.gpt.catalog.schema.NamespaceContextImpl;
import com.esri.gpt.catalog.schema.Namespaces;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A collection of indexable properties associated with a metadata schema.
 */
public class Indexables {
  
  /** class variables ========================================================= */
  
  /** Logger */
  private static Logger LOGGER = Logger.getLogger(Indexables.class.getName());
  
  /** instance variables ====================================================== */ 
  private IndexableContext        indexableContext;
  private Namespaces              namespaces;
  private List<IndexableProperty> properties;
  private List<Indexables>        siblings;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public Indexables() {}
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public Indexables(Indexables objectToDuplicate) {
    if (objectToDuplicate == null) {
    } else {
      if (objectToDuplicate.getNamespaces() != null) {
        this.setNamespaces(new Namespaces(objectToDuplicate.getNamespaces()));
      }
      if (objectToDuplicate.getProperties() != null) {
        if (this.getProperties() == null) {
          this.setProperties(new ArrayList<IndexableProperty>());
        }
        for (IndexableProperty property: objectToDuplicate.getProperties()) {
          this.getProperties().add(new IndexableProperty(property));
        }
      }
      if (objectToDuplicate.getSiblings() != null) {
        if (this.getSiblings() == null) {
          this.setSiblings(new ArrayList<Indexables>());
        }
        for (Indexables sibling: objectToDuplicate.getSiblings()) {
          this.getSiblings().add(new Indexables(sibling));
        }
      }
    } 
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the indexable context.
   * @return the indexable context
   */
  public IndexableContext getIndexableContext() {
    return this.indexableContext;
  }
  /**
   * Sets the indexable context.
   * @param context the indexable context
   */
  public void setIndexableContext(IndexableContext context) {
    this.indexableContext = context;
  }
  
  /**
   * Gets the namespaces for XPath evaluation.
   * @return the namespaces
   */
  public Namespaces getNamespaces() {
    return this.namespaces;
  }
  /**
   * Sets the namespaces for XPath evaluation.
   * @param namespaces the namespaces
   */
  public void setNamespaces(Namespaces namespaces) {
    this.namespaces = namespaces;
  }
  
  /**
   * Gets the child properties.
   * @return the child properties
   */
  public List<IndexableProperty> getProperties() {
    return this.properties;
  }
  /**
   * Sets the child properties.
   * @param properties the child properties
   */
  public void setProperties(List<IndexableProperty> properties) {
    this.properties = properties;
  }
  
  /**
   * Gets the list of associated siblings.
   * @return the siblings
   */
  public List<Indexables> getSiblings() {
    return this.siblings;
  }
  /**
   * Sets the list of associated siblings.
   * @param siblings the siblings
   */
  public void setSiblings(List<Indexables> siblings) {
    this.siblings = siblings;
  }
  
  /** methods ================================================================= */
  
  /**
   * Adds a sibling.
   * @param sibling the sibling to add
   */
  public void addSibling(Indexables sibling) {
    if (sibling != null) {
      if (this.getSiblings() == null) {
        this.setSiblings(new ArrayList<Indexables>());
      }
      this.getSiblings().add(sibling);
    }
  }
  
  /**
   * Configures the object based upon a node loaded from a 
   * schema configuration XML.
   * <p/>
   * The following attributes are configured:
   * <br/>fileName
   * <p/>
   * The following child nodes are configured:
   * <br/>namespace property
   * @param context the configuration context
   * @param node the configuration node
   * @param attributes the attributes of the configuration node
   */
  public void configure(CfgContext context, Node node, NamedNodeMap attributes) {

    // loop through the child nodes
    NodeList nl = node.getChildNodes();
    for (int i=0;i<nl.getLength();i++) {
      Node nd = nl.item(i);
      if (nd.getNodeType() == Node.ELEMENT_NODE) {
        String nodeName = Val.chkStr(nd.getNodeName());
        
        // configure namespaces
        if (nodeName.equalsIgnoreCase("namespace")) {
          if (this.getNamespaces() == null) {
            this.setNamespaces(new Namespaces());
          }
          this.getNamespaces().add(context.getFactory().newNamespace(context,nd));
          
        // configure properties  
        } else if (nodeName.equalsIgnoreCase("property")) {
          if (this.getProperties() == null) {
            this.setProperties(new ArrayList<IndexableProperty>());
          }
          IndexableProperty property = new IndexableProperty();
          property.configure(context,nd,nd.getAttributes());
          this.getProperties().add(property);
        }
      }
    }
    
    String fileName = Val.chkStr(DomUtil.getAttributeValue(attributes,"fileName"));
    if (fileName.length() > 0) {
      String err = "Unable to load indexables: "+fileName;
      try {
        Document dom = DomUtil.makeDomFromResourcePath(fileName,false);
        NodeList nli = dom.getElementsByTagName("indexables");
        if ((nli == null) || (nli.getLength() == 0)) {
          LOGGER.log(Level.CONFIG,"No <indexables> elements were found in: "+fileName);
        } else {
          for (int i=0;i<nli.getLength();i++) {
            Node nd = nli.item(i);
            Indexables idxables = new Indexables();
            idxables.configure(context,nd,nd.getAttributes());
            if (idxables.hasPropertiesOrSiblings()) {
              this.addSibling(idxables);
            }
          }
        }
      } catch (ParserConfigurationException e) {
        LOGGER.log(Level.SEVERE,err,e);
      } catch (SAXException e) {
        LOGGER.log(Level.SEVERE,err,e);
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE,err,e);
      }
    }
    
  }
  
  /**
   * Evaluates indexable properties based upon the supplied metadata document.
   * @param schema the schema being evaluated
   * @param dom the metadata document
   * @param context the active indexable context
   * @param xpath an XPath object configured with an appropriate 
   *        Namespace context for the schema
   * @throws XPathExpressionException if an evaluation expression fails 
   */
  public void evaluate(Schema schema, 
                       IndexableContext context, 
                       Document dom, 
                       XPath xpath)
    throws XPathExpressionException {
    
    // determine the local XPath
    XPath localXPath = xpath; 
    if (this.getNamespaces() != null) {
      NamespaceContextImpl ns = new NamespaceContextImpl(this.getNamespaces());
      localXPath = XPathFactory.newInstance().newXPath();
      localXPath.setNamespaceContext(ns);
    }
 
    // evaluate and resolve properties
    if (this.getProperties() != null) {
      for (IndexableProperty property: this.getProperties()) {
        property.evaluate(schema,context,dom,null,localXPath);
      }
      for (IndexableProperty property: this.getProperties()) {
        property.resolve(schema,context,null);
      }
    }
    
    // evaluate siblings
    if (this.getSiblings() != null) {
      for (Indexables sibling: this.getSiblings()) {
        sibling.evaluate(schema,context,dom,xpath);
      }
    }
    
  }
  
  /**
   * Determines if properties of siblings have been configured for this set.
   * @return <code>true</code> if properties of siblings have been configured
   */
  public boolean hasPropertiesOrSiblings() {
    if ((this.getProperties() != null) && (this.getProperties().size() > 0)) {
      return true;
    } else if ((this.getSiblings() != null) && (this.getSiblings().size() > 0)) {
      return true;
    } else {
      return false;
    }
  }
  
}
