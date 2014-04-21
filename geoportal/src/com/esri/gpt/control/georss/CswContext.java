/*
 * Copyright 2014 Esri, Inc..
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

package com.esri.gpt.control.georss;

import com.esri.gpt.framework.util.Val;

/**
 * CSW context.
 */
public class CswContext {
  private final String cswUrl;
  private final String cswProfileId;

  public static CswContext create(String cswUrl, String cswProfileId) {
    cswUrl = Val.chkStr(cswUrl);
    cswProfileId = Val.chkStr(cswProfileId);
    
    if (!cswUrl.isEmpty() && !cswProfileId.isEmpty()) {
      return new CswContext(cswUrl, cswProfileId);
    }
    
    return null;
  }
  
  private CswContext(String cswUrl, String cswProfileId) {
    this.cswUrl = Val.chkStr(cswUrl);
    this.cswProfileId = Val.chkStr(cswProfileId);
  }

  public String getCswUrl() {
    return cswUrl;
  }

  public String getCswProfileId() {
    return cswProfileId;
  }
  
}
