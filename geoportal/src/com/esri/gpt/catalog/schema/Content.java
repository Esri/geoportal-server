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
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.DateProxy;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Content component associated with a metadata schema.
 * <p/>
 * The component is configured from a node with a schema configuration
 * XML document.
 * <p/>
 * Example:<br/> 
 * &lt;content select="/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString"/&gt;
 */
public class Content extends Component {

  // class variables =============================================================
  
  /** Logger */
  private static Logger LOGGER = Logger.getLogger(Content.class.getName());
    
  /** GCO List node type = "gcoList" */
  public static final String NODETYPE_GCOLIST = "gcoList";
    
  /** 
   * ISO code list value = "isoCodeListValue" 
   * <p/>
   * This node type is represented by a duplication of an ISO code in
   * both a codeListValue attribute and the text node of the 
   * attributes's parent element.
   */
  public static final String NODETYPE_ISOCODELISTVALUE = "isoCodeListValue";
    
  /** List node type = "list" */
  public static final String NODETYPE_LIST = "list";
    
  /** Single node type = "single" (this is the default) */
  public static final String NODETYPE_SINGLE = "single";
  
  /** Single node type = "single" (this is the default) */
  public static final String NODETYPE_PAIRRIGHTVALUE = "pairRightValue";
  
  /** Single node type = "single" (this is the default) */
  public static final String NODETYPE_PAIRLEFTVALUE = "pairLeftValue";
   
  
  // instance variables ==========================================================
  private Codes         _codes;
  private String        _delete = "";
  private boolean       _deleteIfEmpty = false;
  private boolean       _deleteParentIfEmpty = false;
  private ContentValues _multipleValues;
  private String        _nilReasonPath = "";
  private String        _nodeType = Content.NODETYPE_SINGLE;
  private String        _select = "";
  private ContentValue  _singleValue;
  private String        _update = "";
  private boolean       _useSelectForUpdate = false;
    
  // constructors ================================================================
  
  /** Default constructor. */
  public Content() {
    this(null);
  }
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public Content(Content objectToDuplicate) {
    super(objectToDuplicate);
    if (objectToDuplicate == null) {
      setCodes(new Codes());
      setSingleValue(new ContentValue());
      setMultipleValues(new ContentValues());
    } else {
      setCodes(new Codes(objectToDuplicate.getCodes()));
      setNodeType(objectToDuplicate.getNodeType());
      setSingleValue(objectToDuplicate.getSingleValue().duplicate());
      setMultipleValues(new ContentValues(objectToDuplicate.getMultipleValues()));
      setSelect(objectToDuplicate.getSelect());
      setUpdate(objectToDuplicate.getUpdate());
      setDelete(objectToDuplicate.getDelete());
      setNilReasonPath(objectToDuplicate.getNilReasonPath());
      setUseSelectForUpdate(objectToDuplicate.getUseSelectForUpdate());
      setDeleteIfEmpty(objectToDuplicate.getDeleteIfEmpty());
      setDeleteParentIfEmpty(objectToDuplicate.getDeleteParentIfEmpty());
    }
  }
  
  // properties ==================================================================
  
  /**
   * Gets the codes for this parameter.
   * @return the value codes
   */
  public Codes getCodes() {
    return _codes;
  }
  /**
   * Sets the codes for this parameter.
   * @param codes the value codes
   */
  protected void setCodes(Codes codes) {
    _codes = codes;
    if (_codes == null) _codes = new Codes();
  }
  
  /**
   * Gets the XPath expression used to select nodes for deletion when an updated value is empty.
   * @return the delete expression
   */
  public String getDelete() {
    return _delete;
  }
  /**
   * Sets the XPath expression used to select nodes for deletion when an updated value is empty.
   * @param expression the delete expression
   */
  public void setDelete(String expression) {
    _delete = Val.chkStr(expression);
  }
  
  /**
   * Indicates if the node associated with the update expression should be 
   * deleted if the updated value is empty.
   * @return true if the node should be deleted when the updated value is empty
   */
  public boolean getDeleteIfEmpty() {
    return _deleteIfEmpty;
  }
  /**
   * Indicates if the node(s) associated with the update expression should be 
   * deleted if the updated value is empty.
   * @param deleteIfEmpty true if the node should be deleted when the updated value is empty
   */
  public void setDeleteIfEmpty(boolean deleteIfEmpty) {
    _deleteIfEmpty = deleteIfEmpty;
  }
  
  /**
   * Indicates if the parent node associated with the update expression should be 
   * deleted if the updated value is empty.
   * @return true if the parent node should be deleted when the updated value is empty
   */
  public boolean getDeleteParentIfEmpty() {
    return _deleteParentIfEmpty;
  }
  /**
   * Indicates if the node(s) associated with the update expression should be deleted if the
   * updated value is empty.
   * @param deleteIfEmpty true if the node should be deleted when the updated value is empty
   */
  public void setDeleteParentIfEmpty(boolean deleteIfEmpty) {
    _deleteParentIfEmpty = deleteIfEmpty;
  }
  
  /**
   * Gets the multiple value list.
   * @return the multiple value list
   */
  public ContentValues getMultipleValues() {
    return _multipleValues;
  }
  /**
   * Sets the multiple value list
   * @param values the multiple value list
   */
  protected void setMultipleValues(ContentValues values) {
    _multipleValues = values;
    if (_multipleValues == null) _multipleValues = new ContentValues();
  }
  
  /**
   * Gets the XPath expression used to a ISO nil-reason attribute associated with this element.
   * @return the nil-reason expression
   */
  public String getNilReasonPath() {
    return _nilReasonPath;
  }
  /**
   * Sets the XPath expression used to a ISO nil-reason attribute associated with this element.
   * @param expression the nil-reason expression
   */
  public void setNilReasonPath(String expression) {
    _nilReasonPath = Val.chkStr(expression);
  }
  
  /**
   * Gets the node type.
   * @return the node type
   */
  public String getNodeType() {
    return _nodeType;
  }
  /**
   * Sets the node type.
   * @param type the node type
   */
  public void setNodeType(String type) {
    type = Val.chkStr(type);
    if (type.equals("")) {
      _nodeType = Content.NODETYPE_SINGLE;
    } else if (type.equalsIgnoreCase(Content.NODETYPE_GCOLIST)) {
      _nodeType = Content.NODETYPE_GCOLIST;
    } else if (type.equalsIgnoreCase(Content.NODETYPE_ISOCODELISTVALUE)) {
      _nodeType = Content.NODETYPE_ISOCODELISTVALUE;
    } else if (type.equalsIgnoreCase(Content.NODETYPE_LIST)) {
      _nodeType = Content.NODETYPE_LIST;
    } else if (type.equalsIgnoreCase(Content.NODETYPE_SINGLE)) {
      _nodeType = Content.NODETYPE_SINGLE;
    } else if (type.equalsIgnoreCase(Content.NODETYPE_PAIRLEFTVALUE)) {
      _nodeType = Content.NODETYPE_PAIRLEFTVALUE;
    } else if (type.equalsIgnoreCase(Content.NODETYPE_PAIRRIGHTVALUE)) {
      _nodeType = Content.NODETYPE_PAIRRIGHTVALUE;
    }else {
      _nodeType = Content.NODETYPE_SINGLE;
    }
  }
  
  /**
   * Gets the XPath selection expression.
   * @return the selection expression
   */
  public String getSelect() {
    return _select;
  }
  /**
   * Sets the XPath selection expression.
   * @param expression the selection expression
   */
  public void setSelect(String expression) {
    _select = Val.chkStr(expression);
  }
  
  /**
   * Gets the single value.
   * @return the single value
   */
  public ContentValue getSingleValue() {
    return _singleValue;
  }
  
  /**
   * Sets the single value.
   * @param value the single value
   */
  protected void setSingleValue(ContentValue value) {
    _singleValue = value;
    if (_singleValue == null) _singleValue = new ContentValue();
  }
  
  /**
   * Gets the XPath expression used to select nodes for update.
   * @return the update expression
   */
  public String getUpdate() {
    return _update;
  }
  /**
   * Sets the XPath expression used to select nodes for update.
   * @param expression the update expression
   */
  public void setUpdate(String expression) {
    _update = Val.chkStr(expression);
  }
  
  /**
   * Gets the status indicating if the selection expression should
   * be used for locating nodes during the update process.
   * @return true if the selection expression should be used for update
   */
  public boolean getUseSelectForUpdate() {
    return _useSelectForUpdate;
  }
  /**
   * Sets the status indicating if the selection expression should
   * be used for locating nodes during the update process.
   * @param useSelectForUpdate true if the selection expression 
   *        should be used for update
   */
  public void setUseSelectForUpdate(boolean useSelectForUpdate) {
    _useSelectForUpdate = useSelectForUpdate;
  }
  
  // methods =====================================================================
  
  /**
   * Clears single and multiple values.
   */
  public void clearAllValues() {
    getSingleValue().clear();
    getMultipleValues().clear();
  }
  
  /**
   * Configures the object based upon a node loaded from a 
   * schema configuration XML.
   * <br/>The super.configure method should be invoked prior to any
   * sub-class configuration.
   * <p/>
   * The following attributes are configured:
   * <br/>nodeType useSelectForUpdate deleteIfEmpty deleteParentIfEmpty select update delete nilReason 
   * <p/>
   * The following child nodes are configured:
   * <br/>codes
   * @param context the configuration context
   * @param node the configuration node
   * @param attributes the attributes of the configuration node
   */
  @Override
  public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
    super.configure(context,node,attributes);
    
    // configure attributes
    setNodeType(DomUtil.getAttributeValue(attributes,"nodeType"));
    setSelect(DomUtil.getAttributeValue(attributes,"select"));
    setUpdate(DomUtil.getAttributeValue(attributes,"update"));
    setDelete(DomUtil.getAttributeValue(attributes,"delete"));
    setNilReasonPath(DomUtil.getAttributeValue(attributes,"nilReason"));
    setUseSelectForUpdate(Val.chkBool(
        DomUtil.getAttributeValue(attributes,"useSelectForUpdate"),false));
    setDeleteIfEmpty(Val.chkBool(
        DomUtil.getAttributeValue(attributes,"deleteIfEmpty"),false));
    setDeleteParentIfEmpty(Val.chkBool(
        DomUtil.getAttributeValue(attributes,"deleteParentIfEmpty"),false));
    
    // configure codes
    Node ndCodes = DomUtil.findFirst(node,"codes");
    if (ndCodes != null) {
      Node[] aryCodes = DomUtil.findChildren(ndCodes,"code");
      for (Node ndCode: aryCodes) {
        getCodes().add(context.getFactory().newCode(context,ndCode));
      }
    }
    
    // check to see if the selection expression should be used during
    // the update process
    if (getUseSelectForUpdate()) {
      setUpdate(getSelect());
    }
  }
  
  /**
   * Deletes nodes from a document based upon an XPath expression.
   * @param dom the metadata document template for the schema
   * @param xpath an XPath object configured with an appropriate 
   *        Namespace context for the schema
   * @param expression the expression to use to delete the nodes
   * @throws XPathExpressionException if an expression fails 
   * @throws SchemaException if the update fails
   */
  public static void deleteNodes(Document dom, XPath xpath, String expression) 
    throws XPathExpressionException {
    expression = Val.chkStr(expression);
    if (expression.length() > 0) {
      NodeList nl = (NodeList)xpath.evaluate(expression,dom,XPathConstants.NODESET);
      for (int i=0;i<nl.getLength();i++) {
        Node node = nl.item(i);
        if (node.getParentNode() != null) {
          node.getParentNode().removeChild(node);
        }
      }
    }
  }
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new Content(this);
   */
  public Content duplicate() {
    return new Content(this);
  }
  
  /**
   * Appends property information for the component to a StringBuffer.
   * <br/>The method is intended to support "FINEST" logging.
   * <br/>super.echo should be invoked prior appending any local information.
   * @param sb the StringBuffer to use when appending information
   */
  @Override
  public void echo(StringBuffer sb) {
    super.echo(sb);
    sb.append(" nodeType=\"").append(getNodeType()).append("\"");
    sb.append(" useSelectForUpdate=\"").append(getUseSelectForUpdate()).append("\"");
    sb.append(" deleteIfEmpty=\"").append(getDeleteIfEmpty()).append("\"");
    sb.append(" deleteParentIfEmpty=\"").append(getDeleteParentIfEmpty()).append("\"");
    sb.append("\n select=\"").append(getSelect()).append("\"");
    sb.append("\n update=\"").append(getUpdate()).append("\"");
    sb.append("\n delete=\"").append(getDelete()).append("\"");
    if (this.getNilReasonPath().length() > 0) {
      sb.append("\n nilReason=\"").append(this.getNilReasonPath()).append("\"");
    }
    if ((getCodes().size() > 0)) {
      sb.append("\n").append(getCodes());
    }
    if (isSingleValue()) {
      sb.append("\n").append(getSingleValue());
    } else {
      sb.append("\n").append(getMultipleValues());
    }
  }
  
  /**
   * Evaluates the XPath select expression associated with a parameter.
   * @param dom the metadata document
   * @param xpath an XPath object configured with an appropriate 
   *        Namespace context for the schema
   * @param parameter the associated parameter
   * @throws XPathExpressionException if an evaluation expression fails 
   */
  public void evaluate(Document dom, XPath xpath, Parameter parameter) 
    throws XPathExpressionException {
    clearAllValues();
    String sSelect = getSelect();
    LOGGER.finer("Evaluating xpath: "+sSelect);
    if (sSelect.length() > 0) {
      
      // evaluate single or multiple node values
      if (isSingleValue()) {        
        String sValue = xpath.evaluate(sSelect,dom);           
        getSingleValue().setValue(sValue);      
      } else {
        ContentValues values = getMultipleValues();
        NodeList nl = (NodeList)xpath.evaluate(sSelect,dom,XPathConstants.NODESET);
        for (int i=0;i<nl.getLength();i++) {
          String sValue = nl.item(i).getTextContent();        
          values.add(new ContentValue(sValue));
        }
      }
    }
    
    // check for an ISO nil-reason
    parameter.getValidation().setNilReasonValue("");
    String sNil = this.getNilReasonPath();
    if (sNil.length() > 0) {
      LOGGER.finer("Evaluating nilReason xpath: "+sNil);
      parameter.getValidation().setNilReasonValue(xpath.evaluate(sNil,dom));
    }
  }
  
  /**
   * Formats a value associated with a parameter.
   * <p/>
   * Currently, only Date type values are formatted.
   * @param parameter the associated parameter
   * @param value the value to format
   */
  public String formatValue(Parameter parameter, String value) {
    value = Val.chkStr(value);
    if (value.length() > 0) {
      if (parameter.getValidation().getValueType().equals(Validation.VALUETYPE_DATE)) {
        DateProxy dp = new DateProxy();
        dp.setDate(value);
        value = (dp.getDate());
      }
    }
    return value;
  }
  
  /**
   * Determines if the node type is of single value.
   * @return true if the node type is a single value type
   */
  public boolean isSingleValue() {
    return !getNodeType().equals(Content.NODETYPE_GCOLIST) && 
           !getNodeType().equals(Content.NODETYPE_LIST);
  }
  
  /**
   * Determines if the node value(s) is/are empty.
   * @return true if the node node value(s) is/are empty
   */
  public boolean isValueEmpty() {
    boolean bIsEmpty = true;
    if (isSingleValue()) {
      bIsEmpty = (getSingleValue().getValue().length() == 0);
    } else {
      for (ContentValue value: getMultipleValues()) {
        if (value.getValue().length() > 0) {
          bIsEmpty = false;
          break;
        }
      }
    }
    return bIsEmpty;
  }
  
  /**
   * Makes the display value for a parameter.
   * <p/>
   * The output component is suitable for display on the 
   * metadata details page.
   * @param messageBroker the message broker
   * @param parameter the associated parameter
   * @return the UI input component
   */
  public String makeDisplayValue(MessageBroker messageBroker, Parameter parameter) {
    Codes codes = getCodes();
    if (isSingleValue()) {
      String sValue = formatValue(parameter,getSingleValue().getValue());
      sValue = codes.lookupDisplayValue(messageBroker,sValue);
      return sValue;
    } else {
      StringBuffer sb = new StringBuffer();
      String sDelimiter = ",";
      for (ContentValue value: getMultipleValues()) {
        String sValue = Val.chkStr(value.getValue());
        sValue = codes.lookupDisplayValue(messageBroker,sValue);
        if (sValue.length() > 0) {        
          if (sb.length() > 0) {
            sb.append(sDelimiter).append(" ");
          }
          sb.append(sValue);
        }
      }
      return sb.toString();
    }
  }
  
  /**
   * Generates a String array of value(s) associated with the parameter.
   * @return the String array of values
   */
  public String[] toValueArray() {
    ArrayList<String> alValues = new ArrayList<String>();
    if (isSingleValue()) {
      alValues.add(getSingleValue().getValue());
    } else {
      for (ContentValue value: getMultipleValues()) {
        alValues.add(value.getValue());
      }
    }
    return alValues.toArray(new String[0]);
  }
  
  /**
   * Updates the metadata document based upon entered parameter value(s).
   * <p/>
   * @param dom the metadata document template for the schema
   * @param xpath an XPath object configured with an appropriate 
   *        Namespace context for the schema
   * @param parameter the associated parameter
   * @throws XPathExpressionException if an expression fails 
   * @throws SchemaException if the update fails
   */
  public void update(Document dom, XPath xpath, Parameter parameter) 
    throws XPathExpressionException, SchemaException {
    
    // get the update expression, throw an exception if empty
    String sUpdate = getUpdate();
    LOGGER.finer("Updating xpath: "+sUpdate);
    if (sUpdate.length() == 0) {
      
    } else if (isSingleValue()) {
        
      // get the value to set, search for the node to update
      String sValue = getSingleValue().getValue();   
      Node node = (Node)xpath.evaluate(sUpdate,dom,XPathConstants.NODE);
      
      // throw an exception if the node to update was not located
      // within the template
      if (node == null) {
        
      } else {
        
        // update the node
        if (node.getNodeType() == Node.ELEMENT_NODE) {       
         
          if (getNodeType().equals(Content.NODETYPE_PAIRLEFTVALUE)) { 
            String sTextContent = Val.chkStr(node.getTextContent());
            int spaceIndx = sTextContent.indexOf(' '); 
            if((spaceIndx == -1) && (sTextContent.length()>0)){
              sValue = sValue + " " + sTextContent;
            }
          } else if(getNodeType().equals(Content.NODETYPE_PAIRRIGHTVALUE)){
            String sTextContent = Val.chkStr(node.getTextContent());
            int spaceIndx = sTextContent.indexOf(' '); 
            if((spaceIndx == -1) && (sTextContent.length()>0)){
              sValue = sTextContent + " " + sValue;
            }
          }
          
          node.setTextContent(sValue);  
          
        } else if (node.getNodeType() == Node.TEXT_NODE) {
          node.setNodeValue(sValue);
        } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
          node.setNodeValue(sValue);
          
          // for an ISO code list value
          //   attempt to set the text node of the parent element
          //   to the code value
          if (getNodeType().equals(Content.NODETYPE_ISOCODELISTVALUE)) {
            if (node instanceof Attr) {
              Attr attr = (Attr)node;
              if (attr.getOwnerElement() != null) {
                attr.getOwnerElement().setTextContent(sValue);
              }
            }            
          }
        }
        
        // delete if required
        if (isValueEmpty() && getDeleteParentIfEmpty()) {
          if (node instanceof Attr) {
            Node ndToDelete = ((Attr)node).getOwnerElement();
            ndToDelete.getParentNode().removeChild(ndToDelete);
          } else {
            node.getParentNode().getParentNode().removeChild(node.getParentNode());
          }
        } else if (isValueEmpty() && getDeleteIfEmpty()) {
          if (node instanceof Attr) {
            ((Attr)node).getOwnerElement().removeAttributeNode((Attr)node);
          } else {
            node.getParentNode().removeChild(node);
          }
        }
        
      }    
      
    } else {
      Node node = (Node)xpath.evaluate(sUpdate,dom,XPathConstants.NODE);
  
      // throw an exception if the node to update was not located
      // within the template
      if (node == null) {
        
      } else {
        
        Node ndToClone = node;
        if (node instanceof Attr) {
          Attr attr = (Attr)node;
          ndToClone = attr.getOwnerElement();
        } else if (getNodeType().equals(Content.NODETYPE_GCOLIST)) {
          ndToClone = node.getParentNode();
        }
        Node ndInsertBefore = ndToClone.getNextSibling();
        Node ndParent = ndToClone.getParentNode();
        ArrayList<String> alValues = new ArrayList<String>();
        int nCount = 0;
        for (ContentValue value: getMultipleValues()) {
          if (value.getValue().length() > 0) {
            nCount++;
            alValues.add(value.getValue());
            if (nCount > 1) {
              Node ndCloned = ndToClone.cloneNode(true);
              ndParent.insertBefore(ndCloned,ndInsertBefore);
            }
          }
        }
        NodeList nl = (NodeList)xpath.evaluate(sUpdate,dom,XPathConstants.NODESET);
        for (int i=0;i<nl.getLength();i++) {
          String sValue = "";
          if (i < alValues.size()) {
            sValue = alValues.get(i);
          }
          Node ndActive = nl.item(i);
          ndActive.setTextContent(sValue);
        }
        
        // delete if required
        if (isValueEmpty() && getDeleteParentIfEmpty()) {
          node.getParentNode().getParentNode().removeChild(node.getParentNode());
        } else if (isValueEmpty() && getDeleteIfEmpty()) {
          node.getParentNode().removeChild(node);
        }
        
      }
      
    }
    
    // delete if required
    if (isValueEmpty() && (getDelete().length() > 0)) {
      deleteNodes(dom,xpath,getDelete());
    }
    
  }

}
