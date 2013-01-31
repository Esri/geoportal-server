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
package com.esri.gpt.server.csw.provider;
import com.esri.gpt.catalog.arcims.ImsMetadataAdminDao;
import com.esri.gpt.catalog.management.MmdActionCriteria;
import com.esri.gpt.catalog.management.MmdActionRequest;
import com.esri.gpt.catalog.management.MmdCriteria;
import com.esri.gpt.catalog.management.MmdResult;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.provider.components.CswNamespaces;
import com.esri.gpt.server.csw.provider.components.IOperationProvider;
import com.esri.gpt.server.csw.provider.components.IProviderFactory;
import com.esri.gpt.server.csw.provider.components.IResponseGenerator;
import com.esri.gpt.server.csw.provider.components.OperationContext;
import com.esri.gpt.server.csw.provider.components.OwsException;
import com.esri.gpt.server.csw.provider.components.TransactionOptions;
import com.esri.gpt.server.csw.provider.components.TransactionSummary;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides the CSW Transaction/Delete operation.
 */
public class DeleteProvider implements IOperationProvider {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(DeleteProvider.class.getName());
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public DeleteProvider() {
    super();
  }
          
  /** methods ================================================================= */
  
  /**
   * Executes a deletion request.
   * @param context the operation context
   * @param publisher the authenticated publisher
   * @param typeName the CSW collection type
   * @param handle a client supplied name (can be echoed within the response)
   * @param ids the collection of IDs to delete
   * @throws Exception if a processing exception occurs
   */
  protected void executeDelete(OperationContext context, 
                               Publisher publisher,
                               String typeName, 
                               String handle,
                               String[] ids) 
    throws Exception {
    if ((ids == null) || (ids.length == 0)) return;
    
    TransactionOptions tOptions = context.getRequestOptions().getTransactionOptions();
    TransactionSummary tSummary = tOptions.getSummary();
    ImsMetadataAdminDao dao = new ImsMetadataAdminDao(context.getRequestContext());
    MmdActionCriteria actionCriteria = new MmdActionCriteria();
    actionCriteria.setActionKey("delete");
    for (String id: ids) {
      String uuid = Val.chkStr(dao.findUuid(id));
      if (uuid.length() == 0) {
        // TODO: throw an exception here?
      } else {
        actionCriteria.getSelectedRecordIdSet().add(uuid);
      }
    }
    if (actionCriteria.getSelectedRecordIdSet().size() > 0) {
      MmdResult result = new MmdResult();
      MmdCriteria criteria = new MmdCriteria();
      criteria.setActionCriteria(actionCriteria);
      MmdActionRequest actionRequest = new MmdActionRequest(
          context.getRequestContext(),publisher,criteria,result);
      actionRequest.execute();
      int numDeleted = result.getActionResult().getNumberOfRecordsModified();
      tSummary.setTotalDeleted(tSummary.getTotalDeleted() + numDeleted);
      // TODO: need some logging
      //LOGGER.finer(result.getActionResult().getNumberOfRecordsModified()+" document(s) deleted.");
    }
  }
  
  /**
   * Generates the response.
   * @param context the operation context
   * @throws Exception if a processing exception occurs
   */
  public void generateResponse(OperationContext context) throws Exception {
    IProviderFactory factory = context.getProviderFactory();
    IResponseGenerator generator = factory.makeResponseGenerator(context);
    if (generator == null) {
      String msg = "IProviderFactory.makeResponseGenerator: instantiation failed.";
      LOGGER.log(Level.SEVERE,msg);
      throw new OwsException(msg);
    } else {
      generator.generateResponse(context);
    } 
  }
  
  /**
   * Handles a URL based request (HTTP GET).
   * @param context the operation context
   * @param request the HTTP request
   * @throws Exception if a processing exception occurs
   */
  public void handleGet(OperationContext context, HttpServletRequest request) 
    throws Exception {
    
    // initialize
    LOGGER.finer("Handling csw:Transaction/csw:Delete request URL...");
    throw new OwsException("HTTP Get is not supported for this operation."); 
  }
  
  /**
   * Handles an XML based request (normally HTTP POST).
   * @param context the operation context
   * @param root the root node
   * @param xpath an XPath to enable queries (properly configured with name spaces)
   * @throws Exception if a processing exception occurs
   */
  public void handleXML(OperationContext context, Node root, XPath xpath)
    throws Exception {
    
    // initialize
    LOGGER.finer("Handling csw:Transaction/csw:Delete request XML...");
    TransactionOptions tOptions = context.getRequestOptions().getTransactionOptions();
    Publisher publisher = new Publisher(context.getRequestContext());
    
    // find all the Delete nodes
    String locator = "csw:Delete";
    NodeList nlActions = (NodeList)xpath.evaluate(locator,root,XPathConstants.NODESET);
    if (nlActions != null) {
      for (int i=0;i<nlActions.getLength();i++) {
        Node ndAction = nlActions.item(i);
        String typeName = xpath.evaluate("@typeName",ndAction);
        String handle = xpath.evaluate("@handle",ndAction);
        StringSet ids = new StringSet(); 
        tOptions.setDeletionIDs(ids);
       
        // find the constraint node
        locator = "csw:Constraint";
        Node ndConstraint = (Node)xpath.evaluate(locator,ndAction,XPathConstants.NODE);
        if (ndConstraint == null) {
          String msg = "The parameter was missing - "+locator;
          throw new OwsException(OwsException.OWSCODE_MissingParameterValue,locator,msg);
        }
        
        // constraint version
        //String cVersion = xpath.evaluate("@version",ndConstraint);
        
        // csw:CqlText
        locator = "csw:CqlText";
        Node ndCql = (Node)xpath.evaluate(locator,ndConstraint,XPathConstants.NODE);
        if (ndCql != null) {
          //String cql = Val.chkStr(ndCql.getTextContent());      
          String msg = "Delete transactions based upon "+locator+" are not supported.";
          throw new OwsException(OwsException.OWSCODE_OperationNotSupported,locator,msg);
        } else {
        
          // ogc:Filter
          locator = "ogc:Filter";
          Node ndFilter = (Node)xpath.evaluate(locator,ndConstraint,XPathConstants.NODE);
          if (ndFilter == null) {
            String msg = "The parameter was missing - "+locator;
            throw new OwsException(OwsException.OWSCODE_MissingParameterValue,locator,msg);
          } else {
            this.parseDeletionIds(ndFilter,xpath,ids);
          }
        }
          
        // execute the delete
        if (ids.size() > 0) {
          this.executeDelete(context,publisher,typeName,handle,ids.toArray(new String[0]));
        } else {
          String msg = "No Ids were specified for deletion.";
          throw new OwsException(msg);
        }
      }
    }
    
    // generate the response
    this.generateResponse(context);
  }
  
  /**
   * Parses the deletion constraint .
   * @param ndDelete the parent defining the action
   * @param xpath an XPath to enable queries (properly configured with name spaces)
   * @param ids the collection of ID's to populate for deletion
   * @throws OwsException if validation exception occurs
   * @throws XPathExpressionException if an XPath related exception occurs
   */
  /*
  protected void parseDeletionConstraint(Node ndDelete, 
                                         XPath xpath, 
                                         StringSet ids) 
    throws OwsException, XPathExpressionException {
    
    // find the constraint node
    String locator = "csw:Constraint";
    Node ndConstraint = (Node)xpath.evaluate(locator,ndDelete,XPathConstants.NODE);
    if (ndConstraint == null) {
      String msg = "The parameter was missing - "+locator;
      throw new OwsException(OwsException.OWSCODE_MissingParameterValue,locator,msg);
    }
    
    // constraint version
    String cVersion = xpath.evaluate("@version",ndConstraint);
    
    // csw:CqlText
    locator = "csw:CqlText";
    Node ndCql = (Node)xpath.evaluate(locator,ndConstraint,XPathConstants.NODE);
    if (ndCql != null) {
      String cql = Val.chkStr(ndCql.getTextContent());      
      String msg = "Delete transactions based upon "+locator+" are not supported.";
      throw new OwsException(OwsException.OWSCODE_OperationNotSupported,locator,msg);
    } else {
    
      // ogc:Filter
      locator = "ogc:Filter";
      Node ndFilter = (Node)xpath.evaluate(locator,ndConstraint,XPathConstants.NODE);
      if (ndFilter == null) {
        String msg = "The parameter was missing - "+locator;
        throw new OwsException(OwsException.OWSCODE_MissingParameterValue,locator,msg);
      } else {
        this.parseDeletionIds(ndFilter,xpath,ids);
      }
    }
  }
  */
  
  /**
   * Parses a parent node for logical, property comparison and spatial sub-clauses.
   * <br/>Any logical clauses encountered will be recursively parsed.
   * @param parent the parent node from which sub-clauses will read
   * @param xpath an XPath to enable queries (properly configured with name spaces)
   * @param ids the collection of ID's to populate for deletion
   * @throws OwsException if validation fails
   * @throws XPathExpressionException if an XPath related exception occurs
   */
  protected void parseDeletionIds(Node parent, 
                                  XPath xpath, 
                                  StringSet ids) 
    throws OwsException, XPathExpressionException {
    NodeList children = parent.getChildNodes();
    if (children == null) return;
    
    Map<String,String> idAliases = new HashMap<String,String>();
    idAliases.put("id","id");
    idAliases.put("csw:id","id");
    idAliases.put("identifier","id");
    idAliases.put("dc:identifier","id");
    
    String err = "For deletion requests - only ogc:Or, ogc:PropertyIsEqualTo";
    err += " and ogc:PropertyName = Id are supported";
    for (int i=0;i<children.getLength();i++) {
      Node subNode = children.item(i);
      String uri = Val.chkStr(subNode.getNamespaceURI());
      if (uri.length() > 0) {
        String localName = Val.chkStr(subNode.getLocalName());
        LOGGER.finer("Parsing node ("+uri+")"+localName);
        if (uri.equals(CswNamespaces.URI_OGC)) {          
          if (localName.equals("Or")) {
            this.parseDeletionIds(subNode,xpath,ids);
            
          } else if (localName.equals("PropertyIsEqualTo")) {
            
            // check the property name
            String locator = "ogc:PropertyIsEqualTo/ogc:PropertyName";
            Node ndPropName = (Node)xpath.evaluate("ogc:PropertyName",subNode,XPathConstants.NODE);
            if (ndPropName == null) {
              String msg = "The parameter was not found.";
              throw new OwsException(OwsException.OWSCODE_MissingParameterValue,locator,msg);
            } 
            String sPropName = Val.chkStr(ndPropName.getTextContent());
            if (sPropName.length() == 0) {
              String msg = "The parameter value was empty.";
              throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
            }
            
            // ensure an ID property
            boolean isIdProperty = idAliases.containsKey(sPropName.toLowerCase());
            if (!isIdProperty) {
              String msg = "\""+sPropName+"\" is not a supported queryable";
              msg += ", only \"Id\" is supported for this operation.";
              throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
            } 
            
            // check the literal
            locator = "ogc:PropertyIsEqualTo/ogc:Literal";
            Node ndLiteral = (Node)xpath.evaluate("ogc:Literal",subNode,XPathConstants.NODE);
            if (ndLiteral == null) {
              String msg = "The parameter was not found.";
              throw new OwsException(OwsException.OWSCODE_MissingParameterValue,locator,msg);
            }
            String sLiteral = Val.chkStr(ndLiteral.getTextContent());
            if (sLiteral.length() == 0) {
              String msg = "The parameter value was empty.";
              throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
            }
            
            // add the ID
            ids.add(sLiteral);
            
          } else {

            String locator = Val.chkStr(subNode.getNodeName());
            throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,err);
          }
        }
      }
    }
  }
  
}
