/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.framework.dcat.raw;

/**
 * Raw DCAT record.
 */
public class RawDcatRecord extends RawDcatAttributes {
  private RawDcatArray<RawDcatAttributes> distribution = new RawDcatArray<RawDcatAttributes>();

  public RawDcatArray<RawDcatAttributes> getDistribution() {
    return distribution;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(super.toString());
    if (!distribution.isEmpty()) {
      if (sb.length()>0) {
        sb.append(",");
      }
      sb.append("\"distribution\":").append(distribution.toString());
    }
    return "{"+sb.toString()+"}";
  }
}
