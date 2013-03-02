package com.esri.gpt.server.csw.provider.local;
import com.esri.gpt.control.webharvest.engine.Harvester;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.XmlIoUtil;
import com.esri.gpt.junit.cfg.CfgAction;
import com.esri.gpt.junit.cfg.CfgActionHandler;
import com.esri.gpt.junit.cfg.CfgAssertion;
import com.esri.gpt.junit.cfg.CfgAssertionHelper;
import com.esri.gpt.junit.cfg.CfgConfigFileProcessor;
import com.esri.gpt.junit.facade.HttpServletRequestFacade;
import com.esri.gpt.server.csw.provider.components.CswNamespaces;
import com.esri.gpt.server.csw.provider.components.OperationContext;
import com.esri.gpt.server.csw.provider.components.OwsException;
import com.esri.gpt.server.csw.provider.components.RequestHandler;
import com.esri.gpt.server.csw.provider.components.RequestOptions;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.junit.Test;
import org.w3c.dom.NamedNodeMap;

/**
 * Tests the local CSW provider factory.
 * <p/>
 * TODO: need documentation on  the data folder and config XMLs
 * TODO idDelete* doesn't work
 */
public class ProviderFactoryTest implements CfgActionHandler {
  
  /** An administrator. */
  private Publisher admin = null;
  
  /** Verbose output. */
  private boolean verbose = false;
  
  /** An XPath configured with CSW namespaces. */
  private XPath xpath = null;
  
  /**
   * Executes the test.
   * @throws Exception if a processing exception occurs
   */
  @Test
  public void executeTest() throws Exception {
   URL url = this.getClass().getResource("data/Config.xml");
   
   //url = this.getClass().getResource("data/Config_GetRecordById.xml");
   //url = this.getClass().getResource("data/Config_GetRecords.xml");
   //url = this.getClass().getResource("data/Config_GetCapabilities.xml");
   //url = this.getClass().getResource("data/Config_GetRecords4.xml");
   //url = this.getClass().getResource("data/Config_GetRecords5.xml");
   //url = this.getClass().getResource("data/Config_GetRecords6.xml");
   //url = this.getClass().getResource("data/Config_GetRecords7.xml");
   //url = this.getClass().getResource("data/Config_GetRecords8.xml");
   //url = this.getClass().getResource("data/Config_Dev.xml");
   
   this.verbose = true;
   
   ApplicationContext appCtx = ApplicationContext.getInstance();

   // create harvester engine
   MessageBroker messageBroker = new MessageBroker();
   messageBroker.setBundleBaseName(MessageBroker.DEFAULT_BUNDLE_BASE_NAME);
   Harvester harvester = new Harvester(messageBroker, appCtx.getConfiguration().getHarvesterConfiguration());
   appCtx.setHarvestingEngine(harvester);
   
   CfgConfigFileProcessor processor = new CfgConfigFileProcessor();
   processor.processConfigFile(new File(url.getPath()),this,true);
  }
  
  /**
   * Determines if an action is testable.
   * @param action the active test action
   * @return true if the action is testable
   */
  public boolean isActionTestable(CfgAction action) {
    return action.nodeName.equalsIgnoreCase("url") || 
           action.nodeName.equalsIgnoreCase("xml") || 
           action.nodeName.equalsIgnoreCase("file");
  }
  
  /**
   * Executes the test of a configured action.
   * @param action the active test action
   * @throws Exception if a processing exception occurs
   */
  public void testAction(CfgAction action) throws Exception {
    RequestContext rc = null;
    RequestHandler handler = null;
    boolean succeeded = false;
    try {

      // make the HTTP servlet request
      String actionName = action.nodeName;
      String queryString = null;
      if (action.nodeName.equalsIgnoreCase("url")) {
        queryString = Val.chkStr(action.nodeText);
      }
      HttpServletRequestFacade httpRequest = new HttpServletRequestFacade(queryString);
      
      // set up an administrative publisher
      if (this.admin == null) {
        RequestContext rc2 = null;
        try {
          rc2 = RequestContext.extract(null);
          this.admin = Publisher.makeSystemAdministrator(rc2);
        } finally {
          if (rc2 != null) rc2.onExecutionPhaseCompleted();
        }
      }
      httpRequest.getSession().setAttribute("com.esri.gpt.user",admin);
      
      // make the CSW request handler
      rc = RequestContext.extract(httpRequest);
      handler = ProviderFactory.newHandler(rc);
      RequestOptions rOptions = handler.getOperationContext().getRequestOptions();
      rOptions.getTransactionOptions().setAutoApprove(true);
      
      // execute the testable action
      if (action.nodeName.equalsIgnoreCase("url")) {
        handler.handleGet(httpRequest);
      } else if (actionName.equalsIgnoreCase("xml")) {
        handler.handleXML(Val.chkStr(action.nodeText));
      } else if (actionName.equalsIgnoreCase("file")) {
        handler.handleXML(XmlIoUtil.readXml(action.dataFile.getCanonicalPath()));
      } else {
        throw new Exception(action.makeMessagePrefix()+" The action is invalid: "+actionName);
      }
      if (this.verbose) {
        System.err.println(handler.getOperationContext().getOperationResponse().getResponseXml());
      }
   
      // assert that the action should have passed
      succeeded = true;
      action.onSucceeded();
              
    // OWS exceptions
    } catch (OwsException ows) {
      if (this.verbose) {
        System.err.println(ows.getReport());
      }
      action.onFailed(ows,false);
      
      // assert the owsLocator and owsCode if set
      NamedNodeMap nnmItemAttributes = action.item.node.getAttributes();
      String owsCode = Val.chkStr(DomUtil.getAttributeValue(nnmItemAttributes,"owsCode"));
      String owsLocator = Val.chkStr(DomUtil.getAttributeValue(nnmItemAttributes,"owsLocator"));
      if (owsLocator.startsWith("@")) {
        owsLocator = Val.chkStr(owsLocator.substring(1));
      }
      if (owsLocator.length() > 0) {
        String msg = action.makeMessagePrefix()+" The OwsException locator is incorrect.";
        org.junit.Assert.assertEquals(msg,owsLocator,ows.getLocator());
      }
      if (owsCode.length() > 0) {
        String msg = action.makeMessagePrefix()+" The OwsException code is incorrect.";
        org.junit.Assert.assertEquals(msg,owsCode,ows.getCode());
      }
      
    // other exceptions
    } catch (Exception e) {
      action.onFailed(e,true);
    } finally {
      if (rc != null) rc.onExecutionPhaseCompleted();
    }
    
    // handle remaining assertions    
    CfgAssertion[] assertions = action.item.assertions;
    if (succeeded && (assertions != null) && (assertions.length > 0)) {
      CfgAssertionHelper helper = new CfgAssertionHelper();
      OperationContext ctx = handler.getOperationContext();
      String resultXml = ctx.getOperationResponse().getResponseXml();
      if (this.xpath == null) {
        CswNamespaces ns = new CswNamespaces();
        this.xpath = XPathFactory.newInstance().newXPath();
        this.xpath.setNamespaceContext(ns.makeNamespaceContext());
      }
  
      HashMap<String,String> actuals = new HashMap<String,String>();
      actuals.put("serviceProperties.serviceName",
          ctx.getServiceProperties().getServiceName());
      actuals.put("serviceProperties.serviceVersion",
          ctx.getServiceProperties().getServiceVersion());
      actuals.put("operationResponse.outputFormat",
          ctx.getOperationResponse().getOutputFormat());
      actuals.put("transactionOptions.deletionIDs.size",
          ""+ctx.getRequestOptions().getTransactionOptions().getDeletionIDs().size());
      
      helper.processAssertions(action,actuals,resultXml,this.xpath);
    }
    
  }
    
}
