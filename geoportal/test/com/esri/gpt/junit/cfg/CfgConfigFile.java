package com.esri.gpt.junit.cfg;
import java.io.File;

/**
 * Represents a test configuration file.
 */
public class CfgConfigFile {
  
  /** The action handler. */
  public CfgActionHandler actionHandler;
  
  /** The number of actions tested. */
  public int actionsTested = 0;
  
  /** The number of assertions tested. */
  public int assertionsTested = 0;
  
  /** The number of standalone data files tested. */
  public int dataFilesTested = 0;
  
  /** The configuration file. */
  public File file;
  
  /** The number of items tested. */
  public int itemsTested = 0;
  
}