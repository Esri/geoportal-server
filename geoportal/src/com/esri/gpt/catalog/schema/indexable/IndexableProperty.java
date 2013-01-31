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
import com.esri.gpt.catalog.discovery.Discoverable;
import com.esri.gpt.catalog.discovery.PropertyMeaning;
import com.esri.gpt.catalog.discovery.PropertyValueType;
import com.esri.gpt.catalog.schema.CfgContext;
import com.esri.gpt.catalog.schema.EsriTags;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.catalog.search.ResourceIdentifier;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An indexable property associated with a metadata schema.
 */
public class IndexableProperty {
  
  /** instance variables ====================================================== */
  private List<IndexableProperty> children;
  private List<String>            evaluatedValues;
  private String                  meaningName;
  private String                  xpathExpression;
  private String                  xpathType;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public IndexableProperty() {}
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public IndexableProperty(IndexableProperty objectToDuplicate) {
    if (objectToDuplicate == null) {
    } else {
      this.setMeaningName(objectToDuplicate.getMeaningName());
      this.setXPathExpression(objectToDuplicate.getXPathExpression());
      this.setXPathType(objectToDuplicate.getXPathType());
      if (objectToDuplicate.getChildren() != null) {
        if (this.getChildren() == null) {
          this.setChildren(new ArrayList<IndexableProperty>());
        }
        for (IndexableProperty child: objectToDuplicate.getChildren()) {
          this.getChildren().add(new IndexableProperty(child));
        }
      }
    } 
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the child properties.
   * @return the child properties
   */
  public List<IndexableProperty> getChildren() {
    return this.children;
  }
  
  /**
   * Sets the child properties.
   * @param children the child properties
   */
  public void setChildren(List<IndexableProperty> children) {
    this.children = children;
  }
  
  /**
   * Gets the first evaluated value.
   * @return the first evaluated value (can be null)
   */
  public String getEvaluatedValue() {
    if (this.getEvaluatedValues() != null) {
      for (String value: this.getEvaluatedValues()) {
        value = Val.chkStr(value);
        if (value.length() > 0) {
          return value;
        }
      }
    }
    return null;
  }
  
  /**
   * Gets the evaluated values.
   * @return the evaluated values
   */
  public List<String> getEvaluatedValues() {
    return this.evaluatedValues;
  }
  /**
   * Sets the evaluated values.
   * @param values the evaluated values
   */
  public void setEvaluatedValues(List<String> values) {
    this.evaluatedValues = values;
  }
  
  /**
   * Gets the meaning name.
   * @return the meaning name
   */
  public String getMeaningName() {
    return this.meaningName;
  }
  /**
   * Sets the meaning name.
   * @param name the meaning name
   */
  public void setMeaningName(String name) {
    this.meaningName = name;
  }
  
  /**
   * Gets the XPath expression.
   * @return the XPath expression
   */
  public String getXPathExpression() {
    return this.xpathExpression;
  }
  /**
   * Sets the XPath expression.
   * @param expression the XPath expression
   */
  public void setXPathExpression(String expression) {
    this.xpathExpression = expression;
  }  
  
  /**
   * Gets the XPath expression result type.
   * <br/>BOOLEAN NODE NODESET NUMBER STRING
   * @return the XPath expression result type
   */
  public String getXPathType() {
    return this.xpathType;
  }
  /**
   * Sets the XPath expression result type.
   * <br/>BOOLEAN NODE NODESET NUMBER STRING
   * @param xpathType the expression result type
   */
  public void setXPathType(String xpathType) {
    this.xpathType = xpathType;
  }  
  
  /** methods ================================================================= */

  /**
   * Adds an evaluated value to the collection.
   * @param context the active indexable context
   * @param meaning the associated property meaning
   * @param value the evaluated value
   */
  protected void addValue(IndexableContext context, PropertyMeaning meaning, String value) {
    value = Val.chkStr(value);
    if (value.length() > 0) {
      if (this.getEvaluatedValues() == null) {
        this.setEvaluatedValues(new ArrayList<String>());
      }
      this.getEvaluatedValues().add(value);
    }
  }
  
  /**
   * Configures the object based upon a node loaded from a 
   * schema configuration XML.
   * <p/>
   * The following attributes are configured:
   * <br/>meaning xpathType xpath
   * <p/>
   * The following child nodes are configured:
   * <br/>property
   * @param context the configuration context
   * @param node the configuration node
   * @param attributes the attributes of the configuration node
   */
  public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
    String mName = DomUtil.getAttributeValue(attributes,"meaning");
    if (Val.chkStr(mName).equalsIgnoreCase("anytext")) mName = "body";
    if (Val.chkStr(mName).equalsIgnoreCase("resource.type")) mName = "contentType";
    this.setMeaningName(mName);
    this.setXPathExpression(Val.chkStr(DomUtil.getAttributeValue(attributes,"xpath")));
    this.setXPathType(Val.chkStr(DomUtil.getAttributeValue(attributes,"xpathType")));
    
    // loop through the children
    NodeList nl = node.getChildNodes();
    for (int i=0;i<nl.getLength();i++) {
      Node nd = nl.item(i);
      if (nd.getNodeType() == Node.ELEMENT_NODE) {
        String nodeName = Val.chkStr(nd.getNodeName());
        if (nodeName.equalsIgnoreCase("property")) {
          if (this.getChildren() == null) {
            this.setChildren(new ArrayList<IndexableProperty>());
          }
          IndexableProperty child = new IndexableProperty();
          child.configure(context,nd,nd.getAttributes());
          this.getChildren().add(child);
        }
      }
    }
    
  }
  
  /**
   * Evaluates the property based upon the supplied metadata document.
   * @param schema the schema being evaluated
   * @param context the active indexable context
   * @param dom the metadata document
   * @param parent the metadata document node the is actively being processed (can be null)
   * @param xpath an XPath object configured with an appropriate 
   *        Namespace context for the schema
   * @throws XPathExpressionException if an evaluation expression fails 
   */
  public void evaluate(Schema schema, 
                       IndexableContext context, 
                       Document dom, 
                       Node parent,
                       XPath xpath)
    throws XPathExpressionException {
    
    // initialize
    String mName = Val.chkStr(this.getMeaningName());
    String xpExpr = Val.chkStr(this.getXPathExpression());
    String xpType = Val.chkStr(this.getXPathType());
    boolean hasChildren = (this.getChildren() != null) && (this.getChildren().size() > 0);
    
    // determine the meaning
    PropertyMeaning meaning = null;
    if (mName.length() > 0) {
      meaning = context.getPropertyMeanings().get(mName);
      if (meaning == null) {
        Discoverable discoverable = context.getPropertyMeanings().getAllAliased().get(mName);
        if (discoverable != null) {
          meaning = discoverable.getMeaning();
        } else {
          // TODO warn if the meaning is null
        }
      }
    }
    
    // loop through children if no XPath expression was supplied
    if (xpExpr.length() == 0) {
      if (hasChildren) {
        for (IndexableProperty child: this.getChildren()) {
          child.evaluate(schema,context,dom,null,xpath);
        }
      }
      
    } else {
      
      // determine the starting context for the expression
      Object startItem = dom;
      if (parent != null) {
        if (!xpExpr.startsWith("/")) {
          startItem = parent;
        }
      }
      
      // determine the return type, BOOLEAN NODE NODESET NUMBER STRING
      QName returnType = null;
      if (xpType.equalsIgnoreCase("BOOLEAN")) {
        returnType = XPathConstants.BOOLEAN;
      } else if (xpType.equalsIgnoreCase("NODE")) {
        returnType = XPathConstants.NODE;
      } else if (xpType.equalsIgnoreCase("NODESET") || 
                 xpType.equalsIgnoreCase("NODELIST") ||
                 xpType.equalsIgnoreCase("LIST")) {
        returnType = XPathConstants.NODESET;
      } else if (xpType.equalsIgnoreCase("NUMBER")) {
        returnType = XPathConstants.NUMBER;
      } else if (xpType.equalsIgnoreCase("STRING")) {
        returnType = XPathConstants.STRING;
      } else {
        if (hasChildren) {
          returnType = XPathConstants.NODESET;
        } else {
          if (xpExpr.toLowerCase().endsWith("text()")) {
            returnType = XPathConstants.STRING;
          } else {
            returnType = XPathConstants.NODESET;
          }
        }
      }
      
      // evaluate the expression, process the result
      Object result = xpath.evaluate(xpExpr,startItem,returnType);
      if (result != null) {
        
        if (returnType.equals(XPathConstants.STRING)) {
          String value = (String)result;
          this.addValue(context,meaning,value);
        } else if (returnType.equals(XPathConstants.NUMBER)) {
          String value = ""+((Number)result);
          this.addValue(context,meaning,value);
        } else if (returnType.equals(XPathConstants.BOOLEAN)) {
          String value = ""+((Boolean)result);
          this.addValue(context,meaning,value);
          
        } else if (returnType.equals(XPathConstants.NODE)) {
          Node nd = (Node)result;
          String value = this.getTextContent(nd,true);
          this.addValue(context,meaning,value);
          
          if (hasChildren) {
            for (IndexableProperty child: this.getChildren()) {
              child.evaluate(schema,context,dom,nd,xpath);
            }
          }
          
        } else if (returnType.equals(XPathConstants.NODESET)) {
          NodeList nl = (NodeList)result;
          for (int i=0;i<nl.getLength();i++) {
            Node nd = nl.item(i);
            String value = this.getTextContent(nd,true);
            this.addValue(context,meaning,value);
            
            if (hasChildren) {
              for (IndexableProperty child: this.getChildren()) {
                child.evaluate(schema,context,dom,nd,xpath);
              }
            }  
          }
          
        } 
      }    
    } 
  } 
  
  /**
   * Gets the text associated with a node. 
   * @param node the subject node
   * @param first if true, get the first text node associated with an element node
   * @return the text (can be null)
   */
  protected String getTextContent(Node node, boolean first) {
    String text = null;
    if (node.getNodeType() == Node.ATTRIBUTE_NODE ) {
      text = node.getNodeValue();
    } else if (node.getNodeType() == Node.TEXT_NODE) {
      text = node.getNodeValue();
    } else if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
      text = node.getNodeValue();
    } else if (node.getNodeType() == Node.ELEMENT_NODE) {
      if (first) {
        NodeList nl = node.getChildNodes();
        for (int i=0;i<nl.getLength();i++) {  
          Node nd = nl.item(i);
          if (nd.getNodeType() == Node.TEXT_NODE) {
            text = nd.getNodeValue();
          } else {
            break;
          }
        }
      } else {
        text = node.getTextContent();
      }
    }
    if (text != null) {
      text = Val.chkStr(text);
      if (text.length() == 0) {
        text = null;
      }
    }
    return text;
  }
  
  /**
   * Resolves an evaluated property.
   * @param schema the schema being evaluated
   * @param context the active indexable context
   * @param parent the parent property
   */
  public void resolve(Schema schema, IndexableContext context, IndexableProperty parent) {
    
    // initialize
    String mName = Val.chkStr(this.getMeaningName());
    boolean hasChildren = (this.getChildren() != null) && (this.getChildren().size() > 0);
    List<String> values = this.getEvaluatedValues();
    
    // determine the meaning
    PropertyMeaning meaning = null;
    if (mName.length() > 0) {
      meaning = context.getPropertyMeanings().get(mName);
      if (meaning == null) {
        Discoverable discoverable = context.getPropertyMeanings().getAllAliased().get(mName);
        if (discoverable != null) {
          meaning = discoverable.getMeaning();
        } else {
          // TODO warn if the meaning is null
        }
      }
    }
    
    // handle geometries
    if ((meaning != null) && (meaning.getValueType() != null)) {
      if (meaning.getValueType().equals(PropertyValueType.GEOMETRY)) {
        if (hasChildren) {
          hasChildren = false;
          Envelope envelope = new Envelope();
          for (IndexableProperty child: this.getChildren()) {
            String mn = Val.chkStr(child.getMeaningName());
            String ev = Val.chkStr(child.getEvaluatedValue());
            if (ev.length() > 0) {
              if (mn.equalsIgnoreCase("envelope.west")) {
                envelope.setMinX(ev);
              } else if (mn.equalsIgnoreCase("envelope.south")) {
                envelope.setMinY(ev);
              } else if (mn.equalsIgnoreCase("envelope.east")) { 
                envelope.setMaxX(ev);
              } else if (mn.equalsIgnoreCase("envelope.north")) { 
                envelope.setMaxY(ev);
              } else if (mn.equalsIgnoreCase("envelope.lowerCorner")) {
                String[] pt = ev.split(" ");
                if (pt.length == 2) {
                  envelope.setMinX(pt[0]);
                  envelope.setMinY(pt[1]);
                }
              } else if (mn.equalsIgnoreCase("envelope.upperCorner")) {
                String[] pt = ev.split(" ");
                if (pt.length == 2) {
                  envelope.setMaxX(pt[0]);
                  envelope.setMaxY(pt[1]);
                }
              }
            }
          }
          if (!envelope.isEmpty()) {
            context.addStoreableValue(meaning,envelope);
          }
        }
        return;
      }
    }
    
    // ArcIMS content type
    if ((meaning != null) && (values != null) && (values.size() > 0)) {
      if (Val.chkStr(meaning.getName()).equalsIgnoreCase("contentType")) {
        ArrayList<String> al = new ArrayList<String>();
        boolean changed = false;
        ResourceIdentifier ri = context.ensureResourceIdentifier();
        for (String value: values) {
          String s = Val.chkStr(ri.guessArcIMSContentTypeFromResourceType(value));
          if ((s != null) && (s.length() > 0)) {
            al.add(s);
            changed = true;
          } else {
            // TODO: allow non-enumerated values?
            al.add(value);
          }
        }
        if (changed) {
          this.setEvaluatedValues(al);
          values = this.getEvaluatedValues();
        }
      }
    }
    
    // data themes (ISO MD_TopicCategoryCode)
    if ((meaning != null) && (values != null) && (values.size() > 0)) {
      if (Val.chkStr(meaning.getName()).equalsIgnoreCase("dataTheme")) {
        ArrayList<String> al = new ArrayList<String>();
        boolean changed = false;
        for (String value: values) {
          String s = EsriTags.DATA_THEME_CODES.get(value);
          if ((s != null) && (s.length() > 0)) {
            al.add(s);
            changed = true;
          } else {
            // TODO: allow non-enumerated values?
            al.add(value);
          }
        }
        if (changed) {
          this.setEvaluatedValues(al);
          values = this.getEvaluatedValues();
        }
      }
    }
     
    // process the evaluated values
    if (this.getEvaluatedValues() != null) {
      boolean isSingleValued = false;
      if (isSingleValued) {
        String singleValue = Val.chkStr(this.getEvaluatedValue());
        if (singleValue.length() == 0) {
          this.setEvaluatedValues(null);
        } else {
          this.getEvaluatedValues().clear();
          this.addValue(context,meaning,singleValue);
        }
        
      } else {
        context.addStorableValues(meaning,this.getEvaluatedValues().toArray(new String[0]));
      }
    }
    
    // process the children
    if (hasChildren) {
      for (IndexableProperty child: this.getChildren()) {
        child.resolve(schema,context,this);
      }
    } 
    
  }
  
}
