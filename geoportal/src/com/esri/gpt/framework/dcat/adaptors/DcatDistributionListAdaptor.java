/*
 * Copyright 2013 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.framework.dcat.adaptors;

import com.esri.gpt.framework.dcat.dcat.DcatDistribution;
import com.esri.gpt.framework.dcat.dcat.DcatDistributionList;
import com.esri.gpt.framework.dcat.json.JsonArray;
import com.esri.gpt.framework.dcat.json.JsonAttributes;
import java.util.AbstractList;

/**
 * DCAT distribution list adaptor.
 */
class DcatDistributionListAdaptor extends AbstractList<DcatDistribution> implements DcatDistributionList  {
  private JsonArray<JsonAttributes> attrsList;

  public DcatDistributionListAdaptor(JsonArray<JsonAttributes> attrsList) {
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
