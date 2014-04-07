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
package com.esri.gpt.control.webharvest.client.dcat;

import com.esri.gpt.control.webharvest.common.CommonInfo;
import com.esri.gpt.framework.util.Val;

/**
 * DCAT info.
 */
class DCATInfo extends CommonInfo {

  private String url;
  private String format;

  /**
   * Creates instance of the info.
   * <p>
   * Format property is a value of the 'format' attribute within 'Distribution'
   * array in the DCAT JSON response.
   * </p>
   * @param url service URL
   * @param format format
   */
  public DCATInfo(String url, String format) {
    this.url = Val.chkStr(url);
    this.format = Val.chkStr(format);
  }

  /**
   * Gets service URL.
   * @return service URL
   */
  public String getUrl() {
    return url;
  }
  
  /**
   * Gets format.
   * @return format
   */
  public String getFormat() {
    return format;
  }

  @Override
  public String toString() {
    return "{type: dcat, url: \"" + getUrl() + "\", format: \"" +getFormat()+ "\"}";
  }
}
