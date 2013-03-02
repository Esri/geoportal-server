package com.esri.gpt.junit.cfg;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;

import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Helps with assertion processing.
 */
public class CfgAssertionHelper  {
  
  /**
   * Determines if any assertion is XPath based.
   * @param assertions the assertions
   * @return true if any assertion is XPath based
   */
  public boolean hasXPathBased(CfgAssertion[] assertions) {
    if (assertions != null) {
      for (CfgAssertion assertion: assertions) {
        if (this.isXPathBased(assertion)) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Determines is this is an XPath based assertion.
   * @param assertion the assertion
   * @return true if the assertion is XPath based
   */
  public boolean isXPathBased(CfgAssertion assertion) {
    String src = Val.chkStr(DomUtil.getAttributeValue(assertion.node.getAttributes(),"src"));
    return src.startsWith("xpath-");
  }
  
  /**
   * Processes assertions following a successfully tested action.
   * @param action the active test action
   * @param actuals a map of actual values that can be asserted 
   * @param resultXml an action result XML (if assertions are XPath based)
   * @param  xpath an XPath to enable queries (properly configured with name spaces)
   * @throws Exception if a processing exception occurs
   */
  public void processAssertions(CfgAction action, 
                                Map<String,String> actuals, 
                                String resultXml,
                                XPath xpath) 
    throws Exception {
    
    // process assertion nodes
    CfgAssertion[] assertions = action.item.assertions;
    if ((assertions != null) && (assertions.length > 0)) {
      Document dom = null;
      String assertionType;
      String assertionSrc;
      String expectedValue = null;
      Object actualValue = null;
      String msg;
      String msgPfx = action.makeMessagePrefix();
      
      // load the dom if x-path assertions are present
      if (this.hasXPathBased(assertions)) {
        dom = DomUtil.makeDomFromString(resultXml,true);
      }
      
      // loop through the assertions
      int assertionCount = 0;
      for (CfgAssertion assertion: assertions) {
        assertionCount++;
        NamedNodeMap nnmAssertions = assertion.node.getAttributes();
        assertionType = Val.chkStr(DomUtil.getAttributeValue(nnmAssertions,"type"));
        assertionSrc = Val.chkStr(DomUtil.getAttributeValue(nnmAssertions,"src"));
        expectedValue = Val.chkStr(DomUtil.getAttributeValue(nnmAssertions,"expected"));
        msg = msgPfx+" assertion="+assertionCount+" type="+assertionType+" src="+assertionSrc;
        
        // determine the actual value
        if (assertionSrc.startsWith("xpath-node:")) {
          String expr = assertionSrc.substring(11);
          actualValue = (Node)xpath.evaluate(expr,dom,XPathConstants.NODE);
        } else if (assertionSrc.startsWith("xpath-string:")) {
          String expr = assertionSrc.substring(13);
          actualValue = xpath.evaluate(expr,dom);
        } else if (assertionSrc.startsWith("xpath-number:")) {
          String expr = assertionSrc.substring(13);
          actualValue = ((Number)xpath.evaluate(expr,dom,XPathConstants.NUMBER)).toString();
        } else {
          if ((actuals != null) && actuals.containsKey(assertionSrc)) {
            actualValue = actuals.get(assertionSrc);
          } else {          
            throw new Exception(msg+" The assertion src is invalid.");
          }
        }
        
        // assert
        if (assertionType.equalsIgnoreCase("assertNull")) {
          org.junit.Assert.assertNull(msg,actualValue);
        } else if (assertionType.equalsIgnoreCase("assertNotNull")) {
          org.junit.Assert.assertNotNull(msg,actualValue);
        } else if (assertionType.equalsIgnoreCase("assertEquals")) {
          org.junit.Assert.assertEquals(msg,expectedValue,actualValue);
        } else {
          throw new Exception(msg+" The assertion type is invalid.");
        }
        action.incrementAssertionsTested();
      }
      
    }
  }
    
}
