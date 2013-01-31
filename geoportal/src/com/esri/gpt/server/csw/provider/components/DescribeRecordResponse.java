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
package com.esri.gpt.server.csw.provider.components;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.XmlIoUtil;

import java.util.ArrayList;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Generates a CSW DescribeRecordResponse.
 */
public class DescribeRecordResponse implements IResponseGenerator {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(DescribeRecordResponse.class.getName());
    
  /** constructors ============================================================ */
  
  /** Default constructor */
  public DescribeRecordResponse() {}
          
  /** methods ================================================================= */
  
  /**
   * Generates the response.
   * @param context the operation context
   * @throws Exception if a processing exception occurs
   */
  public void generateResponse(OperationContext context) throws Exception {
    
    /// initialize
    LOGGER.finer("Generating csw:DescribeRecordResponse...");
    DescribeRecordOptions drOptions = context.getRequestOptions().getDescribeRecordOptions(); 
      
    // load the resource document
    String loc = drOptions.getDescribeRecordLocation();
    Document dom = DomUtil.makeDomFromResourcePath(loc,true);
    //dom.getDocumentElement().normalize();   
    
    // make an XPath for the CSW name space context
    CswNamespaces ns = new CswNamespaces();
    XPath xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(ns.makeNamespaceContext());
    
    // if specific type names were requested, then remove those that were not requested
    StringSet typeNames = drOptions.getTypeNames();
    if ((typeNames != null) && (typeNames.size() > 0)) {
      String expr = "/csw:DescribeRecordResponse/csw:SchemaComponent/xsd:schema";
      NodeList nlSchemas = (NodeList)xpath.evaluate(expr,dom,XPathConstants.NODESET);
      if ((nlSchemas != null) && (nlSchemas.getLength() > 0)) {
        ArrayList<Node> remove = new ArrayList<Node>();
        ArrayList<Node> removeParent = new ArrayList<Node>();
        for (int i=0; i<nlSchemas.getLength(); i++) {
          Node ndSchema = nlSchemas.item(i);
          String typeName = Val.chkStr(xpath.evaluate("@id",ndSchema));
          if ((typeName.length() > 0) && !typeNames.contains(typeName)) {
            remove.add(ndSchema);
          }
        }
        
        for (Node node: remove) {
          try {
            Node parent = node.getParentNode();
            parent.removeChild(node);
            if ((parent.getChildNodes() == null) || (parent.getChildNodes().getLength() == 0)) {
              removeParent.add(parent);
            } else {
              boolean hasElementNode = false;
              for (int i=0; i<parent.getChildNodes().getLength(); i++) {
                if (parent.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                  hasElementNode = true;
                  break;
                }
              }
              if (!hasElementNode)removeParent.add(parent);
            }
          } catch (Exception e) {
            String msg = "Unable to remove xsd:schema node from DescribeRecordResponse.";
            LOGGER.warning(msg+" Reason="+e.toString());
          }
        }
    
        for (Node node: removeParent) {
          try {
            Node parent = node.getParentNode();
            parent.removeChild(node);
          } catch (Exception e) {
            String msg = "Unable to remove xsd:schema parent node from DescribeRecordResponse.";
            LOGGER.warning(msg+" Reason="+e.toString());
          }
        }
      }
    }
             
    // set the response string
    String xml = XmlIoUtil.domToString(dom);
    context.getOperationResponse().setResponseXml(xml);  
  }
  
}
