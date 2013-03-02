package com.esri.gpt.junit.cfg;
import org.w3c.dom.Node;

/**
 * Represents an item within a test configuration file.
 */
public class CfgItem {
  
  /** The action handler. */
  public CfgActionHandler actionHandler;
  
  /** The number of actions tested. */
  public int actionsTested = 0;
  
  /** The array or assertion. */
  public CfgAssertion[] assertions;
  
  /** The parent configuration file. */
  public CfgConfigFile configFile;
  
  /** The item node. */
  public Node node;
  
  /** True if the test should pass (item@shouldPass). */
  public boolean shouldPass = true;
    
  /**
   * Increments the number of actions tested.
   * @param action the active test action
   */
  public void incrementActionsTested(CfgAction action) {
    boolean firstAction = (this.actionsTested == 0);
    this.actionsTested = this.actionsTested + 1;
    this.configFile.actionsTested = this.configFile.actionsTested + 1;
    if (firstAction) {
      this.configFile.itemsTested = this.configFile.itemsTested + 1;
    }
    if (action.nodeName.equalsIgnoreCase("file")) {
      this.configFile.dataFilesTested = this.configFile.dataFilesTested + 1;
    }
  }
  
}