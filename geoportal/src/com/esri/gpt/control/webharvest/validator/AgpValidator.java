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
package com.esri.gpt.control.webharvest.validator;

import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.util.Val;

/**
 * Generic AGP protocol validator.
 */
abstract class AgpValidator implements IValidator {
  protected static final String HOST_NAME_REGEX = "(^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])([/].+)?$)|(^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])([/].+)?$)";
  protected boolean arcgisDotComAllowed;
  protected boolean crossAllowed;

  public AgpValidator() {

    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();

    String sArcgisDotComAllowed = appCfg.getCatalogConfiguration().getParameters().getValue("webharvester.agp2agp.arcgisDotCom.allowed");
    this.arcgisDotComAllowed = Val.chkBool(sArcgisDotComAllowed, false);


    String sCrossAllowed = appCfg.getCatalogConfiguration().getParameters().getValue("webharvester.agp2agp.sameDomain.allowed");
    this.crossAllowed = Val.chkBool(sCrossAllowed, false);
  }

  protected boolean getArcgisDotComAllowed() {
    return arcgisDotComAllowed;
  }

  protected boolean getCrossAllowed() {
    return crossAllowed;
  }

  protected void setArcgisDotComAllowed(boolean arcgisDotComAllowed) {
    this.arcgisDotComAllowed = arcgisDotComAllowed;
  }

  protected void setCrossAllowed(boolean crossAllowed) {
    this.crossAllowed = crossAllowed;
  }
  
}
