package com.esri.gpt.junit.cfg;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.XmlIoUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Processes test configuration files.
 */
public class CfgConfigFileProcessor {
  
  /** The list of configuration files that were processed. */
  public List<CfgConfigFile> configFilesProcessed = new ArrayList<CfgConfigFile>();
  
  /**
   * Reads and processes a test configuration file.
   * @param file the configuration file
   * @param actionHandler the action handler
   * @param summarize summarize results on completion
   * @throws Exception if a processing exception occurs
   */
  public void processConfigFile(File file, CfgActionHandler actionHandler, boolean summarize) 
    throws Exception {
          
    // load the configuration file
    CfgConfigFile configFile = new CfgConfigFile();
    configFile.file = file;
    configFile.actionHandler = actionHandler;
    this.configFilesProcessed.add(configFile);
    
    String msgPfx = configFile.file.getName();
    String xml = XmlIoUtil.readXml(configFile.file.getCanonicalPath());
    Document dom = DomUtil.makeDomFromString(xml,false);  
    
    // loop through other configuration file nodes within the document
    NodeList nlConfigFiles = dom.getElementsByTagName("configFile");
    for (int i=0; i<nlConfigFiles.getLength(); i++) {
      Node ndConfigFile = nlConfigFiles.item(i);
      String nodeText = Val.chkStr(ndConfigFile.getTextContent());
      String path = nodeText;
      
      // TODO: absolute vs relative paths??
      path = configFile.file.getParentFile().getAbsolutePath()+"/"+nodeText;
      this.processConfigFile(new File(path),actionHandler,false); 
    }
    
    // loop through the configured items
    NodeList nlItems = dom.getElementsByTagName("item");
    for (int i=0; i<nlItems.getLength(); i++) {
      Node ndItem = nlItems.item(i);
      
      // initialize the configured item
      NamedNodeMap itemAttributes = ndItem.getAttributes();
      CfgItem item = new CfgItem();
      item.configFile = configFile;
      item.actionHandler = configFile.actionHandler;
      item.node = ndItem;
      String shouldPass = Val.chkStr(DomUtil.getAttributeValue(itemAttributes,"shouldPass"));
      if (shouldPass.equalsIgnoreCase("true")) {
        item.shouldPass = true;
      } else if (shouldPass.equalsIgnoreCase("false")) {
        item.shouldPass = false;
      } else {
        throw new Exception(msgPfx+" item="+(i+1)+", shouldPass is a required attribute.");
      }
      
      // load assertion nodes
      ArrayList<CfgAssertion> alAssertions = new ArrayList<CfgAssertion>();
      NodeList nlAssertions = ndItem.getChildNodes();
      for (int j=0; j<nlAssertions.getLength(); j++) {
        Node ndAssertion = nlAssertions.item(j);
        if (ndAssertion.getNodeType() == Node.ELEMENT_NODE) { 
          String nodeName = Val.chkStr(ndAssertion.getNodeName());
          if (nodeName.equalsIgnoreCase("assertion")) {
            CfgAssertion assertion = new CfgAssertion();
            assertion.node = ndAssertion;
            alAssertions.add(assertion);
          }
        }
      }
      if (alAssertions.size() > 0) {
        item.assertions = alAssertions.toArray(new CfgAssertion[0]);
      }
      
      // loop through the actions
      NodeList nlActions = ndItem.getChildNodes();
      for (int j=0; j<nlActions.getLength(); j++) {
        Node ndAction = nlActions.item(j);
        short nodeType = ndAction.getNodeType();
        String nodeName = Val.chkStr(ndAction.getNodeName());
        if ((nodeType == Node.ELEMENT_NODE) && !nodeName.equalsIgnoreCase("assertion")) { 
          CfgAction action = new CfgAction();
          action.item = item;
          action.node = ndAction;
          action.nodeName = nodeName;
          action.nodeText = ndAction.getTextContent();
          if (item.actionHandler.isActionTestable(action)) {
            if (nodeName.equalsIgnoreCase("file")) {
              String nodeText = Val.chkStr(ndAction.getTextContent());
              String path = nodeText;
              
              // TODO: absolute vs relative paths??
              path = configFile.file.getParentFile().getAbsolutePath()+"/"+nodeText;
              this.processDataFile(new File(path),item,action); 
            } else {
              action.onStart();
              item.actionHandler.testAction(action);
              action.onComplete();
            }
          } else {
            throw new Exception(msgPfx+" item="+(i+1)+", This action is not testable: "+nodeName);
          }
        }
      }
      
    }   
    
    // summarize
    if (summarize) {
      for (CfgConfigFile cfgFile: this.configFilesProcessed) {
        String msg = cfgFile.file.getName();
        msg += ", itemsTested="+cfgFile.itemsTested;
        msg += ", actionsTested="+cfgFile.actionsTested;
        msg += ", assertionsTested="+cfgFile.assertionsTested;
        msg += ", dataFilesTested="+cfgFile.dataFilesTested;
        System.err.println(msg);
      }
    }
    
  } 
  
  /**
   * Processes a standalone data file or folder.
   * @param file the file or folder to process
   * @param item the configuration item
   * @param action the active test action
   * @throws Exception if a processing exception occurs
   */
  public void processDataFile(File file, CfgItem item, CfgAction action) throws Exception {
    NamedNodeMap nnm = action.node.getAttributes();
    if (file.isDirectory()) {
      boolean recursive = Val.chkBool(DomUtil.getAttributeValue(nnm,"recursive"),true);
      File[] files = file.listFiles();
      for (File subFile: files) {
        if (subFile.isDirectory()) {
          if (recursive) {
            this.processDataFile(subFile,item,action);
          }
        } else if (subFile.isFile()) {
          this.processDataFile(subFile,item,action);
        }
      }
    } else if (file.isFile()) {
      String suffix = Val.chkStr(DomUtil.getAttributeValue(nnm,"suffix"));
      if (suffix.length() == 0) {
        suffix = ".xml";
      }
      String lcName = file.getName().toLowerCase();
      if (lcName.endsWith(suffix)) {
        action.dataFile = file;
        action.onStart();
        item.actionHandler.testAction(action);
        action.onComplete();
        action.dataFile = null;
      }
    }
  }

}
