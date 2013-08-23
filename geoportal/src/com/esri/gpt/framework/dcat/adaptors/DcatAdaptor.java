/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.framework.dcat.adaptors;

import com.esri.gpt.framework.dcat.raw.RawDcatAttributes;

/**
 * DCAT base adaptor.
 */
public abstract class DcatAdaptor {
  protected RawDcatAttributes attrs;

  public DcatAdaptor(RawDcatAttributes attrs) {
    this.attrs = attrs;
  }
  
  protected String getString(String name) {
    return attrs.get(name).getString();
  }
}
