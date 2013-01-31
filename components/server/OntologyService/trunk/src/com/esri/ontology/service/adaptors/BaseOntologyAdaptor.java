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
package com.esri.ontology.service.adaptors;

import com.esri.ontology.service.control.Format;
import com.esri.ontology.service.control.QueryCriteria;
import com.esri.ontology.service.control.Selection;
import com.esri.ontology.service.util.Val;
import java.util.Properties;

/**
 * Base ontology adaptor.
 */
public abstract class BaseOntologyAdaptor implements IOntologyAdaptor {

  /** query criteria */
  protected QueryCriteria queryCriteria = new QueryCriteria();
  /** selection */
  protected Selection selection = new Selection();
  /** format */
  protected Format format = new Format();

  {
    queryCriteria.setLevel(1);
    queryCriteria.setSeeAlsoWeight(2);
    queryCriteria.setSubClassWeight(2);
    queryCriteria.setThreshold(0);

    selection.setCategories(true);
    selection.setNeighbors(true);

    format.setLucene(true);
    format.setOwl(false);
  }
  
  /**
   * Initializes adaptor.
   * <p/>
   * Initialization parameters:
   * <ul>
   * <li>seealso - weight of SeeAlso bond [optional; default: 2.0]</li>
   * <li>subclassof - weight of SubClassOf bond [optional; default: 2.0]</li>
   * <li>level - tree traverse depth level [optional; default: 1]</li>
   * <li>threshold - threshold [optional; default: 0.0]</li>
   * <li>selection - neighbors selection [optional; choices: <i>categories</i>, <i>neighbors</i>, <i>all</i>; default: <i>all</i>]</li>
   * <li>f - output format [optional; choices: <i>lucene</i>, <i>owl</i>, <i>all</i>; default: <i>lucene</i>]</li>
   * </ul>
   * @param properties properties
   */
  public void init(Properties properties) {
    String sSeeAlso = properties.getProperty("seealso", "2.0");
    String sSubClassOf = properties.getProperty("subclassof", "2.0");
    String sLevel = properties.getProperty("level", "1");
    String sThreshold = properties.getProperty("threshold", "0.0");

    String sSelection = properties.getProperty("selection", "all");

    String sFormat = properties.getProperty("f", "lucene");

    queryCriteria.setSeeAlsoWeight(Val.chkFloat(sSeeAlso, 2.0f));
    queryCriteria.setSubClassWeight(Val.chkFloat(sSubClassOf, 2.0f));
    queryCriteria.setLevel(Val.chkInt(sLevel, 1));
    queryCriteria.setThreshold(Val.chkFloat(sThreshold, 0.0f));

    selection = Selection.parse(sSelection);

    format = Format.parse(sFormat);
  }
}
