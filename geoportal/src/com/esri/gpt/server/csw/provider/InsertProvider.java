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
import com.esri.gpt.catalog.management.MmdEnums;
import com.esri.gpt.catalog.publication.PublicationRecord;
import com.esri.gpt.catalog.publication.PublicationRequest;
import com.esri.gpt.catalog.schema.UnrecognizedSchemaException;
import com.esri.gpt.catalog.schema.ValidationException;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.XmlIoUtil;
import com.esri.gpt.server.csw.provider.components.IOperationProvider;
import com.esri.gpt.server.csw.provider.components.IProviderFactory;
import com.esri.gpt.server.csw.provider.components.IResponseGenerator;
import com.esri.gpt.server.csw.provider.components.OperationContext;
import com.esri.gpt.server.csw.provider.components.OwsException;
import com.esri.gpt.server.csw.provider.components.TransactionOptions;
import com.esri.gpt.server.csw.provider.components.TransactionSummary;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides the CSW Transaction/Insert operation.
 */
public class InsertProvider implements IOperationProvider {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(InsertProvider.class.getName());
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public InsertProvider() {
    super();
  }
          
  /** methods ================================================================= */
  
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
    LOGGER.finer("Handling csw:Transaction/csw:Insert request URL...");
    throw new OwsException("HTTP Get is not supported for this operation."); 
    
    /*
    // initialize
    LOGGER.finer("Handling ccsw:Transaction/csw:Insert request URL...");
    Publisher publisher = new Publisher(context.getRequestContext());
    ParseHelper pHelper = new ParseHelper();
    ValidationHelper vHelper = new ValidationHelper();
    String locator;
    String[] parsed;
    
    // type name and handle
    String typeName = null;
    String handle = null;
    
    // XML document(s) to insert
    //String[] parameterNames = new String[]{"xml,record,metadata,md_metadata"};
    String[] parameterNames = new String[]{"xml"};
    for (String parameterName: parameterNames) {
      parsed = pHelper.getParameterValues(request,parameterName);
      if (parsed != null) {
        for (String xml: parsed) {
          this.publishDocument(context,publisher,typeName,handle,xml);
        }
      }
    }
    
    // generate the response
    this.generateResponse(context);
    */
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
    LOGGER.finer("Handling csw:Transaction/csw:Insert request XML...");
    Publisher publisher = new Publisher(context.getRequestContext());
    
    // find all the Insert nodes
    String locator = "csw:Insert";
    NodeList nlActions = (NodeList)xpath.evaluate(locator,root,XPathConstants.NODESET);
    if (nlActions != null) {
      for (int i=0;i<nlActions.getLength();i++) {
        Node ndAction = nlActions.item(i);
        String typeName = xpath.evaluate("@typeName",ndAction);
        String handle = xpath.evaluate("@handle",ndAction);
        
        // publish all XML documents contained
        NodeList nlXmls = ndAction.getChildNodes();
        this.processDocumentNodes(context,publisher,typeName,handle,nlXmls);
      }
    }
    
    // generate the response
    this.generateResponse(context);
  }
  
  /**
   * Processes and publishes the XML document child nodes associated with 
   * the parent action.
   * @param context the operation context
   * @param publisher the authenticated publisher
   * @param typeName the CSW collection type
   * @param handle a client supplied name (can be echoed within the response)
   * @param xmlNodes a list of child nodes containing XML's to publish
   * @throws Exception if a processing exception occurs
   */
  public void processDocumentNodes(OperationContext context, 
                                   Publisher publisher,
                                   String typeName, 
                                   String handle,
                                   NodeList xmlNodes) 
    throws Exception {
    int nElementNodes = 0;
    for (int i=0;i<xmlNodes.getLength();i++) {
      Node ndXml = xmlNodes.item(i);
      if (ndXml.getNodeType() == Node.ELEMENT_NODE) {
        nElementNodes++;
        
        StringWriter result = new StringWriter();
        XmlIoUtil.transform(new DOMSource(ndXml),new StreamResult(result),true) ;
        String xmlInsert = result.toString();
        
        //Document domInsert = DomUtil.newDocument();
        //Node ndAdopted = domInsert.adoptNode(ndXml);
        //domInsert.appendChild(ndAdopted);
        //String xmlInsert = XmlIoUtil.domToString(domInsert);
        
        xmlInsert = Val.chkStr(Val.removeBOM(xmlInsert));
        this.publishDocument(context,publisher,typeName,handle,xmlInsert);
      }
    }
    if (nElementNodes == 0) {
      String locator = "csw:Insert/*";
      if (this instanceof UpdateProvider) {
        locator = "csw:Update/*";
      }
      String msg = "No child documents were included for publication.";
      throw new OwsException(OwsException.OWSCODE_MissingParameterValue,locator,msg);
    }
  }
      
  /**
   * Publishes a document to the catalog.
   * @param context the operation context
   * @param publisher the authenticated publisher
   * @param typeName the CSW collection type
   * @param handle a client supplied name (can be echoed within the response)
   * @param xml the document XML to publish
   * @throws Exception if a processing exception occurs
   */
  public void publishDocument(OperationContext context, 
                              Publisher publisher,
                              String typeName, 
                              String handle,
                              String xml) 
    throws Exception {
    
    handle = Val.chkStr(handle);
    xml = Val.chkStr(Val.removeBOM(xml));
    TransactionOptions tOptions = context.getRequestOptions().getTransactionOptions();
    TransactionSummary tSummary = tOptions.getSummary();
    PublicationRequest pubRequest = new PublicationRequest(
        context.getRequestContext(),publisher,xml);
    PublicationRecord pubRecord = pubRequest.getPublicationRecord();
    pubRecord.setPublicationMethod(MmdEnums.PublicationMethod.upload.toString());
    if (tOptions.getAutoApprove()) {
      pubRecord.setAutoApprove(true);
    }
    if ((tOptions.getApprovalStatus() != null) && (tOptions.getApprovalStatus().length() > 0)) {
      pubRecord.setApprovalStatus(MmdEnums.ApprovalStatus.valueOf(tOptions.getApprovalStatus()).toString());
    }
    if ((tOptions.getPublicationMethod() != null) && (tOptions.getPublicationMethod().length() > 0)) {
      pubRecord.setPublicationMethod(MmdEnums.PublicationMethod.valueOf(tOptions.getPublicationMethod()).toString());
    }
    
    // TODO: need a sourceUri
    //this.determineSourceUri(request,context,pubRequest);
    
    try {
      pubRequest.publish();
      if (pubRecord.getWasDocumentUnchanged()) {
      } else if (pubRecord.getWasDocumentReplaced()) {
        tSummary.setTotalUpdated(tSummary.getTotalUpdated() + 1);
      } else {
        tSummary.setTotalInserted(tSummary.getTotalInserted() + 1);
      }
    } catch (ValidationException e) {
      MessageBroker msgBroker = new MessageBroker();
      msgBroker.setBundleBaseName(MessageBroker.DEFAULT_BUNDLE_BASE_NAME);
      ArrayList<String> messages = new ArrayList<String>();
      e.getValidationErrors().buildMessages(msgBroker,messages,true);      
      
      StringBuilder sb = new StringBuilder();
      sb.append("<![CDATA[");
      sb.append("\r\nMetadata Validation Exception");
      if (handle.length() > 0) {
        sb.append("\r\nhandle=").append(handle);
        sb.append(", sk=").append(e.getKey());
      } else {
        sb.append("\r\nsk=").append(e.getKey());
      }
      String id = Val.chkStr(pubRecord.getUuid());
      if (id.length() > 0) {
        sb.append(", id=").append(id);
      }
      for (String msg : messages) {
        sb.append("\r\n").append(msg);
      }
      sb.append("\r\n]]>");
      throw new OwsException(sb.toString());    
      
    } catch (UnrecognizedSchemaException e) {
      
      StringBuilder sb = new StringBuilder();
      sb.append("<![CDATA[");
      sb.append("\r\n").append(e.getMessage());
      if (handle.length() > 0) {
        sb.append("\r\nhandle=").append(handle);
      }
      sb.append("\r\n]]>");
      throw new OwsException(sb.toString());  
    }
    
  }
  
}
