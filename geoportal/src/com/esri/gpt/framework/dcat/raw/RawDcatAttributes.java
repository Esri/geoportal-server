/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.framework.dcat.raw;

import java.util.HashMap;
import java.util.Map;

/**
 * Raw DCAT attributes.
 */
public class RawDcatAttributes extends HashMap<String,RawDcatAttribute> {
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String,RawDcatAttribute> attr: this.entrySet()) {
      if (sb.length()>0) {
        sb.append(",");
      }
      sb.append("\"").append(attr.getKey()).append("\":").append(attr.getValue());
    }
    return sb.toString();
  }
}
