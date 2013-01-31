/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.ontology.service.control;

import com.esri.ontology.service.util.Val;
import java.io.Serializable;

/**
 * Ontology servlet query criteria.
 */
public class QueryCriteria implements Parametralizable, Serializable {

  /** search term */
  private String term = "";
  /** SeeAlso weight */
  private float seeAlsoWeight;
  /** SubClass weight */
  private float subClassWeight;
  /** traverse level */
  private int level;
  /** threshold */
  private float threshold;

  /**
   * Gets search term
   * @return search term
   */
  public String getTerm() {
    return term;
  }

  /**
   * Sets search term.
   * @param term search term
   */
  public void setTerm(String term) {
    this.term = Val.chkStr(term);
  }

  /**
   * Sets weight of SeeAlso attribute.
   * @return weight of SeeAlso attribute
   */
  public float getSeeAlsoWeight() {
    return seeAlsoWeight;
  }

  /**
   * Gets weight of SeeAlso attribute.
   * @param seeAlsoWeight weight of SeeAlso attribute
   */
  public void setSeeAlsoWeight(float seeAlsoWeight) {
    this.seeAlsoWeight = seeAlsoWeight;
  }

  /**
   * Gets weight of SubClass attribute.
   * @return weight of SubClass attribute
   */
  public float getSubClassWeight() {
    return subClassWeight;
  }

  /**
   * Sets weight of SubClass attribute.
   * @param subClassWeight weight of SubClass attribute
   */
  public void setSubClassWeight(float subClassWeight) {
    this.subClassWeight = subClassWeight;
  }

  /**
   * Gets traverse level.
   * @return traverse level
   */
  public int getLevel() {
    return level;
  }

  /**
   * Sets traverse level.
   * @param level traverse level
   */
  public void setLevel(int level) {
    this.level = level;
  }

  /**
   * Gets threshold.
   * @return threshold
   */
  public float getThreshold() {
    return threshold;
  }

  /**
   * Sets threshold.
   * @param threshold threshold
   */
  public void setThreshold(float threshold) {
    this.threshold = threshold;
  }

  @Override
  public String toString() {
    return "{" +
      "term: \""+term+"\"; " +
      "seeAlsoWeight: "+seeAlsoWeight+"; " +
      "subClassWeight: "+subClassWeight+"; " +
      "level: "+level+"; "+
      "threshold: "+threshold+";"+
      "}";
  }

  public String toParameter() {
    StringBuilder sb = new StringBuilder();

    if (getTerm().length()>0) {
      sb.append("term="+getTerm());
    }

    if (getSeeAlsoWeight()!=0) {
      if (sb.length()>0) {
        sb.append("&");
      }
      sb.append("seealso="+getSeeAlsoWeight());
    }

    if (getSubClassWeight()!=0) {
      if (sb.length()>0) {
        sb.append("&");
      }
      sb.append("subclassof="+getSubClassWeight());
    }

    if (getLevel()!=0) {
      if (sb.length()>0) {
        sb.append("&");
      }
      sb.append("level="+getLevel());
    }
    return sb.toString();
  }
}
