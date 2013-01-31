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
 * Response format.
 */
public class Format implements Parametralizable, Serializable {

  /** to provide lucene format response */
  private boolean lucene;
  /** to provide OWL response format */
  private boolean owl;

  /**
   * Gets flag indicating <i>lucene</i> response.
   * @return <code>true</code> to get <i>lucene</i> response
   */
  public boolean isLucene() {
    return lucene;
  }

  /**
   * Sets flag indicating <i>lucene</i> response.
   * @param lucene <code>true</code> to get <i>lucene</i> response
   */
  public void setLucene(boolean lucene) {
    this.lucene = lucene;
  }

  /**
   * Gets flag indicating <i>OWL</i> response.
   * @return <code>true</code> to get <i>OWL</i> response
   */
  public boolean isOwl() {
    return owl;
  }

  /**
   * Sets flag indicating <i>OWL</i> response.
   * @param owl <code>true</code> to get <i>OWL</i> response
   */
  public void setOwl(boolean owl) {
    this.owl = owl;
  }

  /**
   * Gets flag if all hs been selected.
   * @return <code>true</code> if all has been selected
   */
  public boolean isAll() {
    return isLucene() && isOwl();
  }

  @Override
  public String toString() {
    return "{lucene: "+lucene+"; owl: "+owl+"}";
  }

  public String toParameter() {
    if (isAll()) {
      return "f=all";
    } else if (isLucene()) {
      return "f=lucene";
    } else if (isOwl()) {
      return "f=owl";
    } else {
      return "";
    }
  }

  /**
   * Parses format given as a string.
   * Format given a a string can have none, one or multiple keywords separated
   * by coma (,). Eligible keywords are: <i>lucene</i>, <i>owl</i>, or
   * <i>all</i> to indicate both. It is safe to pass just <code>null</code> - no
   * any flags will be set withing created selection object.
   * @param format response format given as a string
   * @return response format
   */
  public static Format parse(String format) {
    format = Val.chkStr(format).toLowerCase();

    Format formatObj = new Format();

    for (String fmt : format.split(", ")) {
      if (fmt.equals("lucene")) {
        formatObj.setLucene(true);
      }

      if (fmt.equals("owl")) {
        formatObj.setOwl(true);
      }

      if (fmt.equals("all")) {
        formatObj.setLucene(true);
        formatObj.setOwl(true);
      }
    }

    return formatObj;
  }

}
