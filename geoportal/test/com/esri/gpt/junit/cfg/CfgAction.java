package com.esri.gpt.junit.cfg;
import java.io.File;
import org.w3c.dom.Node;

/**
 * Represents a configured test action.
 */
public class CfgAction {
  
  /** The number of assertions tested. */
  public int assertionsTested = 0;
  
  /** The active data file to test (for <file> actions). */
  public File dataFile;
    
  /** The parent test item. */
  public CfgItem item;
  
  /** The action node. */
  public Node node;
  
  /** The action node name. */
  public String nodeName;
  
  /** The action node text content. */
  public String nodeText;
  
  /** Indicates if an onFailed() or onSucceded() method was triggered. */
  private boolean wasMarkedFailedOrSucceded = false;
  
  /**
   * Increments the number of assertions tested.
   */
  public void incrementAssertionsTested() {
    this.assertionsTested = this.assertionsTested + 1;
    this.item.configFile.assertionsTested = this.item.configFile.assertionsTested + 1;
  }
  
  /**
   * Makes a message prefix for the action being tested.
   * @return the message prefix
   */
  public String makeMessagePrefix() {
    int nItem = this.item.configFile.itemsTested;
    int nAction = this.item.actionsTested;
    String msg = this.item.configFile.file.getName()+" item="+nItem+" action="+nAction;
    return msg;
  }
  
  /**
   * Flags the completion of the action.
   */
  public void onComplete() {
    String pfx = this.makeMessagePrefix();
    if (!this.wasMarkedFailedOrSucceded) {
      String msg = pfx+" An action.onSucceeded or action.onFailed method was not triggered.";
      org.junit.Assert.assertTrue(msg,this.item.shouldPass);
    }
    if ((this.item.assertions != null) && (this.item.assertions.length > 0)) {
      String msg = pfx+" The action.assertionsTested count has a mis-match.";
      org.junit.Assert.assertEquals(msg,this.item.assertions.length,this.assertionsTested);
    }
  }
  
  /**
   * Handles an exception condition on a tested action.
   * @param e the exception that was thrown
   * @param fatal this is a fatal unexpected exception
   * @throws Exception if the action should have passed, or if the condition is fatal
   */
  public void onFailed(Exception e, boolean fatal) throws Exception {
    this.wasMarkedFailedOrSucceded = true;
    String pfx = this.makeMessagePrefix();
    if (this.item.shouldPass) {
      String msg = pfx+" The action should have passed: "+e.toString();
      throw new Exception(msg,e);
    } else if (fatal) {
      
      // TODO: throw this?
      String msg = pfx+" The action failed: "+e.toString();
      throw new Exception(msg,e);
    }
  }
  
  /**
   * Flags the start of the action.
   */
  public void onStart() {
    this.assertionsTested = 0;
    this.wasMarkedFailedOrSucceded = false;
    this.item.incrementActionsTested(this);
  }
  
  /**
   * Asserts that a successfully tested action should have passed.
   */
  public void onSucceeded() {
    this.wasMarkedFailedOrSucceded = true;
    String pfx = this.makeMessagePrefix();
    String msg = pfx+" The action should have failed.";
    org.junit.Assert.assertTrue(msg,this.item.shouldPass);
  }
  
}