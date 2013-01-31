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
package com.esri.gpt.catalog.schema;
import com.esri.gpt.catalog.discovery.PropertyMeanings;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import java.io.IOException;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Provides a factory for instantiating schema component objects.
 */
public class SchemaFactory {

  // class variables =============================================================
    
  // instance variables ==========================================================
    
  // constructors ================================================================
  
  /** Default constructor. */
  public SchemaFactory() {}
  
  // properties ==================================================================
  
  // methods =====================================================================
  
  /**
   * Loads all property meanings from the gpt/metadata/property-meanings.xml configuration file.
   * @throws ParserConfigurationException if configuration exception occurs
   * @throws SAXException if an exception during xml parsing
   * @throws IOException if an i/o exception occurs
   * @throws ParserConfigurationException if error parsing configuration 
   */
  public PropertyMeanings loadPropertyMeanings() 
    throws ParserConfigurationException, SAXException, IOException {
    Document dom = DomUtil.makeDomFromResourcePath("gpt/metadata/property-meanings.xml",false);    
    PropertyMeanings meanings = new PropertyMeanings();
    try {
      meanings.load(dom);
    } catch (XPathExpressionException e) {
      throw new IOException(
          "An XPath expression failed while loading property-meanings.xml: "+e.getMessage());
    }
    return meanings;
  }
  
  /**
   * Loads the schemas contained within a file.
   * @param cgfContext the configuration conects
   * @param schemas the schema collection to populate
   * @param relativePath the relative path to the schema file
   * @throws ParserConfigurationException if configuration exception occurs
   * @throws SAXException if an exception during xml parsing
   * @throws IOException if an i/o exception occurs
   */
  private void loadSchemaFile(CfgContext cfgContext, Schemas schemas, String relativePath)
    throws ParserConfigurationException, SAXException, IOException {
        
    Document dom = DomUtil.makeDomFromResourcePath(relativePath,false);
    Node[] schemaNodes = null;
    Node ndContainer = DomUtil.findFirst(dom,"schemas");
    if (ndContainer != null) {
      schemaNodes = DomUtil.findChildren(ndContainer,"schema");
    } else {
      schemaNodes = DomUtil.findChildren(dom,"schema");
    }
    
    if (schemaNodes != null) {
      for (Node ndSchema: schemaNodes) {
        NamedNodeMap nnmSchema = ndSchema.getAttributes();
        String sFileName = Val.chkStr(DomUtil.getAttributeValue(nnmSchema,"fileName"));
        String sKey = Val.chkStr(DomUtil.getAttributeValue(nnmSchema,"sKey"));
        if (sFileName.length() > 0) {
       
          try {
            loadSchemaFile(cfgContext,schemas,sFileName);
          } catch (Exception e) {
            String sMsg = "Error loading metadata schema: "+sFileName;
            LogUtil.getLogger().log(Level.SEVERE,sMsg,e);
          }
        } else {
  
          try {
            Schema schema = cfgContext.getFactory().newSchema(schemas,cfgContext,ndSchema);
            schemas.add(schema);
          } catch (Exception e) {
            String sMsg = "Error loading metadata schema: "+relativePath+" , @key="+sKey;
            LogUtil.getLogger().log(Level.SEVERE,sMsg,e);
          }
          
        }
      }
    }
  }
    
  /**
   * Loads all schemas from the gpt/metadata/schema.xml configuration file.
   * @throws ParserConfigurationException if configuration exception occurs
   * @throws SAXException if an exception during xml parsing
   * @throws IOException if an i/o exception occurs
   */
  public Schemas loadSchemas() 
    throws ParserConfigurationException, SAXException, IOException {
    CfgContext cfgContext = new CfgContext(this);
    PropertyMeanings meanings = loadPropertyMeanings();
    Schemas schemas = new Schemas();
    schemas.setPropertyMeanings(meanings);
    loadSchemaFile(cfgContext,schemas,"gpt/metadata/schemas.xml");    
    return schemas;
  }
  
  /**
   * Instantiates and configures a new Code component.
   * <br/>The node argument can be null, if so then return a null value.
   * @param context the configuration context
   * @param node the configuration node associated with the component
   * @return the new Code component (null if the node was invalid)
   */
  public Code newCode(CfgContext context, Node node) {
    if (node != null) {    
      Code component = new Code();
      component.configure(context,node,node.getAttributes());
      return component;
    }
    return null;
  }
  
  /**
   * Instantiates and configures a new Content component.
   * <br/>The node argument can be null, if so then return a null value.
   * @param context the configuration context
   * @param node the configuration node associated with the component
   * @return the new Content component (null if the node was invalid)
   */
  public Content newContent(CfgContext context, Node node) {
    if (node != null) {
      Content component = new Content();
      component.configure(context,node,node.getAttributes());
      return component;
    }
    return null;
  }
  
  /**
   * Instantiates and configures a new Label component.
   * <br/>The node argument can be null, if so then return a null value.
   * @param context the configuration context
   * @param node the configuration node associated with the component
   * @return the new Label component (null if the node was invalid)
   */
  public Input newInput(CfgContext context, Node node) {
    if (node != null) {
      Input component = null;
      String sType = DomUtil.getAttributeValue(node.getAttributes(),"type");
      
      // determine the input component type
      if (sType.equalsIgnoreCase(Input.INPUTTYPE_TEXT)) {
        component = new InputText();
      } else if (sType.equalsIgnoreCase(Input.INPUTTYPE_DELIMITEDTEXTAREA)) {
        component = new InputDelimitedTextArea();
      } else if (sType.equalsIgnoreCase(Input.INPUTTYPE_MAP)) {
        component = new InputMap();
      } else if (sType.equalsIgnoreCase(Input.INPUTTYPE_SELECTMANYCHECKBOX)) {
        component = new InputSelectManyCheckbox();
      } else if (sType.equalsIgnoreCase(Input.INPUTTYPE_SELECTONEMEMU)) {
        component = new InputSelectOneMenu();
      } else if (sType.equalsIgnoreCase(Input.INPUTTYPE_SELECTWITHOTHER)) {
        component = new InputSelectWithOther();
      } else if (sType.equalsIgnoreCase(Input.INPUTTYPE_TEXTAREA)) {
        component = new InputTextArea();
      } else if (sType.equalsIgnoreCase(Input.INPUTTYPE_TEXTARRAY)) {
        component = new InputTextArray();
      } else {  
      }
      
      // configure and return the component
      if (component != null) {
        component.configure(context,node,node.getAttributes());
        //System.err.println(component);
        return component;
      }
    }
    return null;
  }
  
  /**
   * Instantiates and configures a new Interrogation component.
   * <br/>The node argument can be null, if so then return a null value.
   * @param context the configuration context
   * @param node the configuration node associated with the component
   * @return the new Interrogation component (null if the node was invalid)
   */
  public Interrogation newInterrogation(CfgContext context, Node node) {
    if (node != null) {
      Interrogation component = new Interrogation();
      component.configure(context,node,node.getAttributes());
      return component;
    }
    return null;
  }
  
  /**
   * Instantiates and configures a new Label component.
   * <br/>The node argument can be null, if so then return a null value.
   * @param context the configuration context
   * @param node the configuration node associated with the component
   * @return the new Label component (null if the node was invalid)
   */
  public Label newLabel(CfgContext context, Node node) {
    if (node != null) {
      Label component = new Label();
      component.configure(context,node,node.getAttributes());
      return component;
    }
    return null;
  }
  
  /**
   * Instantiates and configures a new Namespace component.
   * <br/>The node argument can be null, if so then return a null value.
   * @param context the configuration context
   * @param node the configuration node associated with the component
   * @return the new Namespace component (null if the node was invalid)
   */
  public Namespace newNamespace(CfgContext context, Node node) {
    if (node != null) {
      Namespace component = new Namespace();
      component.configure(context,node,node.getAttributes());
      return component;
    }
    return null;
  }
  
  /**
   * Instantiates and configures a new Parameter component.
   * <br/>The node argument can be null, if so then return a null value.
   * @param context the configuration context
   * @param node the configuration node associated with the component
   * @return the new Parameter component (null if the node was invalid)
   */
  public Parameter newParameter(CfgContext context, Node node) {
    if (node != null) {
      Parameter component = new Parameter();
      component.configure(context,node,node.getAttributes());
      return component;
    }
    return null;
  }
    
  /**
   * Instantiates and configures a new Schema component.
   * <br/>The node argument can be null, if so then return a null value.
   * @param schemas the parent schemas collection
   * @param context the configuration context
   * @param node the configuration node associated with the component
   * @return the new Schema component (null if the node was invalid)
   */
  private Schema newSchema(Schemas schemas, CfgContext context, Node node) {
    if (node != null) {
      Schema component = new Schema();
      component.setPropertyMeanings(schemas.getPropertyMeanings());
      component.configure(context,node,node.getAttributes());
      return component;
    }
    return null;
  }
  
  /**
   * Instantiates and configures a new Section component.
   * <br/>The node argument can be null, if so then return a null value.
   * @param context the configuration context
   * @param node the configuration node associated with the component
   * @return the new Section component (null if the node was invalid)
   */
  public Section newSection(CfgContext context, Node node) {
    if (node != null) {
      Section component = new Section();
      component.configure(context,node,node.getAttributes());
      return component;
    }
    return null;
  }
  
  /**
   * Instantiates and configures a new Validation component.
   * <br/>The node argument can be null, if so then return a null value.
   * @param context the configuration context
   * @param node the configuration node associated with the component
   * @return the new Validation component (null if the node was invalid)
   */
  public Validation newValidation(CfgContext context, Node node) {
    if (node != null) {
      Validation component = new Validation();
      component.configure(context,node,node.getAttributes());
      return component;
    }
    return null;
  }

}

