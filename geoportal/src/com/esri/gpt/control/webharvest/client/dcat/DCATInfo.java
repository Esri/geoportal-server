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

import com.esri.gpt.framework.util.Val;

/**
 * DCAT info.
 */
class DCATInfo {

  private String url;

  /**
   * Creates instance of the info.
   * @param url service URL
   */
  public DCATInfo(String url) {
    this.url = Val.chkStr(url);
  }

  /**
   * Gets service URL.
   * @return service URL
   */
  public String getUrl() {
    return url;
  }

  @Override
  public String toString() {
    return "{type: dcat, url: \"" + getUrl() + "\"}";
  }
}
