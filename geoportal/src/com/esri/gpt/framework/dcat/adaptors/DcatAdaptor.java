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

import com.esri.gpt.framework.dcat.json.JsonAttribute;
import com.esri.gpt.framework.dcat.json.JsonAttributes;

/**
 * DCAT base adaptor.
 */
public abstract class DcatAdaptor {
  protected JsonAttributes attrs;

  /**
   * Creates instance of the adaptor.
   * @param attrs attributes
   */
  public DcatAdaptor(JsonAttributes attrs) {
    this.attrs = attrs;
  }
  
  /**
   * Gets attribute.
   * @param name attribute name
   * @return attribute value
   */
  protected String getString(String name) {
    JsonAttribute attr = attrs.get(name);
    return attr!=null? attr.getString(): "";
  }
}
