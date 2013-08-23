/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.framework.dcat.adaptors;

import com.esri.gpt.framework.dcat.dcat.DcatDistribution;
import com.esri.gpt.framework.dcat.dcat.DcatDistributionList;
import com.esri.gpt.framework.dcat.raw.RawDcatArray;
import com.esri.gpt.framework.dcat.raw.RawDcatAttributes;
import java.util.AbstractList;

/**
 * DCAT distribution list adaptor.
 */
class DcatDistributionListAdaptor extends AbstractList<DcatDistribution> implements DcatDistributionList  {
  private RawDcatArray<RawDcatAttributes> attrsList;

  public DcatDistributionListAdaptor(RawDcatArray<RawDcatAttributes> attrsList) {
    this.attrsList = attrsList;
  }

  @Override
  public DcatDistribution get(int index) {
    return new DcatDistributionAdaptor(attrsList.get(index));
  }

  @Override
  public int size() {
    return attrsList.size();
  }
  
  @Override
  public String toString() {
    return attrsList.toString();
  }
}
