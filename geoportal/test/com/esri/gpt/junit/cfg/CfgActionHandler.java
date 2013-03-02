package com.esri.gpt.junit.cfg;

/**
 * Interface for testing a configured action.
 */
public interface CfgActionHandler {
  
  /**
   * Determines if an action is testable.
   * @param action the active test action
   * @return true if the action is testable
   */
  public boolean isActionTestable(CfgAction action);
  
  /**
   * Executes the test of a configured action.
   * @param action the active test action
   * @throws Exception if a processing exception occurs
   */
  public void testAction(CfgAction action) throws Exception;

}
