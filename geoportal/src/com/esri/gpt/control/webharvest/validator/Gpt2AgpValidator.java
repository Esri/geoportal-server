/*
 * Copyright 2015 pete5162.
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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolGpt2Agp;
import static com.esri.gpt.control.webharvest.validator.AgpValidator.HOST_NAME_REGEX;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.util.Val;

/**
 *
 * @author pete5162
 */
class Gpt2AgpValidator extends AgpValidator {
  private String name;
  private HarvestProtocolGpt2Agp protocol;

  /**
   * Creates instance of the validator.
   *
   * @param url host URL
   * @param protocol protocol
   */
  public Gpt2AgpValidator(String name, HarvestProtocolGpt2Agp protocol) {
    this.name = name;
    this.protocol = protocol;

    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();

    String sArcgisDotComAllowed = appCfg.getCatalogConfiguration().getParameters().getValue("webharvester.agp2agp.arcgisDotCom.allowed");
    this.arcgisDotComAllowed = Val.chkBool(sArcgisDotComAllowed, false);


    String sCrossAllowed = appCfg.getCatalogConfiguration().getParameters().getValue("webharvester.agp2agp.sameDomain.allowed");
    this.crossAllowed = Val.chkBool(sCrossAllowed, false);
  }

  @Override
  public boolean validate(IMessageCollector mb) {
    boolean _valid = true;

    if (name.isEmpty()) {
      mb.addErrorMessage("catalog.harvest.manage.edit.err.nameReq");
      _valid = false;
    }
    
    if (!getArcgisDotComAllowed()) {
      if (protocol.getDestinationHost().toLowerCase().endsWith("arcgis.com") || protocol.getDestinationHost().toLowerCase().endsWith("arcgisonline.com")) {
        mb.addErrorMessage("catalog.harvest.manage.test.msg.agp2agp.arcgis.forbiden");
        _valid = false;
      }
    }
    if (_valid) {
      if (!protocol.getDestinationHost().matches(HOST_NAME_REGEX)) {
        mb.addErrorMessage("catalog.harvest.manage.edit.dest.h.err");
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("gpt-dest-o").isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.dest.o.err");
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("gpt-dest-u").isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.dest.u.err");
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("gpt-dest-p").isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.dest.p.err");
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("gpt-dest-f").isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.dest.f.err");
        _valid = false;
      }
    }
    return _valid;
  }

  @Override
  public boolean checkConnection(IMessageCollector mb) {
    return true;
  }

  @Override
  public boolean checkDestinationConnection(IMessageCollector mb) {
    return checkConnection(mb, protocol.getDestination());
  }
}
