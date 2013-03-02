package com.esri.gpt.junit.facade;

import java.util.Hashtable;

/**
 * Provides an implementation facade for an HttpSession.
 */
@SuppressWarnings("unchecked")
public class HttpSessionFacade extends HttpSessionImpl {

  public Hashtable attributes = new Hashtable();

  @Override
  public Object getAttribute(String arg0) {
    return this.attributes.get(arg0);
  }

  @Override
  public void setAttribute(String arg0, Object arg1) {
    this.attributes.put(arg0,arg1);
  }
  
}
