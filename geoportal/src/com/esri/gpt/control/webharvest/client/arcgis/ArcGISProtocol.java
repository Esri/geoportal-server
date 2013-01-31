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
package com.esri.gpt.control.webharvest.client.arcgis;

import com.esri.gpt.catalog.harvest.protocols.AbstractHTTPHarvestProtocol;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.framework.util.Val;
import java.util.regex.Pattern;

/**
 * ArcGIS server protocol.
 */
public class ArcGISProtocol extends AbstractHTTPHarvestProtocol {

  /** name of the protocol */
  public static final String NAME = "ARCGIS";
  /** secondary URL tag name */
  public static final String SOAP_URL = "secondaryUrl";

  /** flags to carry over */
  private long flags;
  /** SOAP url */
  private String soapUrl = "";

  @Override
  public String getKind() {
    return NAME;
  }

  @Override
  public long getFlags() {
    return flags;
  }

  @Override
  public void setFlags(long flags) {
    this.flags = flags;
  }

  @Override
  public StringAttributeMap getAttributeMap() {
    StringAttributeMap attributes = new StringAttributeMap();
    attributes.add(new StringAttribute(SOAP_URL, soapUrl));
    return attributes;
  }

  @Override
  public void setAttributeMap(StringAttributeMap attributeMap) {
    soapUrl = Val.chkStr(attributeMap.getValue(SOAP_URL));
  }

  @Override
  public QueryBuilder newQueryBuilder(IterationContext context, String url) {
    return new ArcGISQueryBuilder(context, this, url, soapUrl);
  }

  /**
   * Gets secondary URL.
   * @return secondary URL
   */
  public String getSoapUrl() {
    return soapUrl;
  }

  /**
   * Sets secondary URL.
   * @param value secondary URL
   */
  public void setSoapUrl(String value) {
    this.soapUrl = Val.chkStr(value);
  }

  @Override
  public ProtocolType getType() {
    return null;
  }
}
