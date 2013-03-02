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
 * Relationship selection.
 */
public class Selection implements Parametralizable, Serializable {

  /** to process categories */
  private boolean categories;
  /** to process neighbors */
  private boolean neighbors;

  /**
   * Gets flag indicating to traverse categories in the ontology model.
   * @return <code>true</code> to traverse categories
   */
  public boolean isCategories() {
    return categories;
  }

  /**
   * Sets flag indicating to traverse categories in the ontology model.
   * @param categories <code>true</code> to traverse categories
   */
  public void setCategories(boolean categories) {
    this.categories = categories;
  }

  /**
   * Gets flag indicating to traverse neighbors in the ontology model.
   * @return <code>true</code> to traverse neighbors
   */
  public boolean isNeighbors() {
    return neighbors;
  }

  /**
   * Sets flag indicating to traverse neighbors in the ontology model.
   * @param neighbors <code>true</code> to traverse neighbors
   */
  public void setNeighbors(boolean neighbors) {
    this.neighbors = neighbors;
  }

  @Override
  public String toString() {
    return "{categories: "+categories+"; neighbors: "+neighbors+"}";
  }

  public String toParameter() {
    if (isCategories() && isNeighbors()) {
      return "selection=all";
    } else if (isCategories()) {
      return "selection=categories";
    } else if (isNeighbors()) {
      return "selection=neighbors";
    } else {
      return "";
    }
  }

  /**
   * Parses selection given as a string.
   * Selection given a a string can have none, one or multiple keywords separated
   * by coma (,). Eligible keywords are: <i>categories</i>, <i>neighbors</i>, or
   * <i>all</i> to indicate both. It is safe to pass just <code>null</code> - no
   * any flags will be set withing created selection object.
   * @param selection selection given as a string
   * @return selection
   */
  public static Selection parse(String selection) {
    selection = Val.chkStr(selection).toLowerCase();

    Selection selObj = new Selection();

    for (String sel : selection.split(", ")) {
      if (sel.equals("categories")) {
        selObj.setCategories(true);
      }

      if (sel.equals("neighbors")) {
        selObj.setNeighbors(true);
      }

      if (sel.equals("all")) {
        selObj.setCategories(true);
        selObj.setNeighbors(true);
      }
    }

    return selObj;
  }
}
