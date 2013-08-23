/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.framework.dcat.adaptors;

import com.esri.gpt.framework.dcat.dcat.DcatDistribution;
import com.esri.gpt.framework.dcat.raw.RawDcatAttributes;


/**
 * DCAT distribution adaptor.
 */
class DcatDistributionAdaptor extends DcatAdaptor implements DcatDistribution {

  public DcatDistributionAdaptor(RawDcatAttributes attrs) {
    super(attrs);
  }

  @Override
  public String getAccessURL() {
    return getString("accessURL");
  }

  @Override
  public String getFormat() {
    return getString("format");
  }
  
  @Override
  public String toString() {
    return attrs.toString();
  }
}
