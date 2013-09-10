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
package com.esri.gpt.catalog.harvest.protocols;

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.client.dcat.DCATQueryBuilder;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.framework.util.Val;

/**
 * Harvest protocol DCAT.
 */
public class HarvestProtocolDCAT extends AbstractHTTPHarvestProtocol {
  private static final String FORMAT_PATTERN_KEY = "webharvest.dcat.formatPattern";
  public static final String FORMAT_PATTERN_DEFAULT_VALUE;
  private String  format = FORMAT_PATTERN_DEFAULT_VALUE;
  
  static {
    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();
    StringAttributeMap parameters = appCfg.getCatalogConfiguration().getParameters();
    FORMAT_PATTERN_DEFAULT_VALUE = Val.chkStr(parameters.getValue(FORMAT_PATTERN_KEY),"xml");
  }

  @Override
  public ProtocolType getType() {
    return null;
  }

  @Override
  public String getKind() {
    return "DCAT";
  }

  @Override
  public QueryBuilder newQueryBuilder(IterationContext context, String url) {
    return new DCATQueryBuilder(context, this, url);
  }
    
  /**
   * Gets all the attributes.
   * @return attributes as attribute map
   */
  @Override
  public StringAttributeMap getAttributeMap() {
    StringAttributeMap properties = new StringAttributeMap();
    properties.set("dcatFormat", Val.chkStr(getFormat(),FORMAT_PATTERN_DEFAULT_VALUE));
    return properties;
  }

	/**
   * Sets all the attributes.
   * @param attributeMap attributes as attribute map
   */
  @Override
  public void setAttributeMap(StringAttributeMap attributeMap) {
  	setFormat(Val.chkStr(chckAttr(attributeMap.get("dcatFormat")),FORMAT_PATTERN_DEFAULT_VALUE));
  }
  
  /**
   * Gets format.
   * <p>
   * Format property is a value of the 'format' attribute within 'Distribution'
   * array in the DCAT JSON response.
   * </p>
   * @return format
   */
  public String getFormat() {
    return format;
  }
  
  /**
   * Set format.
   * <p>
   * Format property is a value of the 'format' attribute within 'Distribution'
   * array in the DCAT JSON response.
   * </p>
   * @param format format
   */
  public void setFormat(String format) {
    this.format = Val.chkStr(format);
  }
}
