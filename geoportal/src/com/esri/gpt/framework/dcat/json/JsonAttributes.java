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
package com.esri.gpt.framework.dcat.json;

import java.util.HashMap;
import java.util.Map;

/**
 * Raw DCAT attributes.
 */
public class JsonAttributes extends HashMap<String,JsonAttribute> {
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String,JsonAttribute> attr: this.entrySet()) {
      if (sb.length()>0) {
        sb.append(",");
      }
      sb.append("\"").append(attr.getKey()).append("\":").append(attr.getValue());
    }
    return sb.toString();
  }
}
