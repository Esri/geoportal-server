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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolAgp2Agp;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;

/**
 * AGP to AGP protocol validator
 */
class Agp2AgpValidator extends AgpValidator {
  private String url;
  private HarvestProtocolAgp2Agp protocol;

  /**
   * Creates instance of the validator.
   *
   * @param url host URL
   * @param protocol protocol
   */
  public Agp2AgpValidator(String url, HarvestProtocolAgp2Agp protocol) {
    this.url = url;
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

    if (url.isEmpty()) {
      mb.addErrorMessage("catalog.harvest.manage.edit.err.hostUrlReq");
      _valid = false;
    }
    
    if (!getArcgisDotComAllowed()) {
      if (protocol.getDestinationHost().toLowerCase().endsWith("arcgis.com") || protocol.getDestinationHost().toLowerCase().endsWith("arcgisonline.com")) {
        mb.addErrorMessage("catalog.harvest.manage.test.msg.agp2agp.arcgis.forbiden");
        _valid = false;
      }
    } else if (!getCrossAllowed()) {
      String srcHost[] = protocol.getSourceHost().split("[.]");
      String dstHost[] = protocol.getDestinationHost().split("[.]");
      if (srcHost != null && srcHost.length >= 2 && dstHost != null && dstHost.length >= 2) {
        if (srcHost[srcHost.length - 1].equalsIgnoreCase(dstHost[dstHost.length - 1]) && srcHost[srcHost.length - 2].equalsIgnoreCase(dstHost[dstHost.length - 2])) {
          mb.addErrorMessage("catalog.harvest.manage.test.msg.agp2agp.cross.forbiden");
          _valid = false;
        }
      }
    }
    if (_valid) {
      if (!protocol.getSourceHost().matches(HOST_NAME_REGEX)) {
        mb.addErrorMessage("catalog.harvest.manage.edit.src.h.err");
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("src-q").isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.src.q.err");
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("src-m").isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.src.m.err");
        _valid = false;
      } else if (Val.chkLong(protocol.getAttributeMap().getValue("src-m"), 0) <= 0 || Val.chkLong(protocol.getAttributeMap().getValue("src-m"), 0) > HarvestProtocolAgp2Agp.getAgp2AgpMaxItems()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.src.m.err.less", new Object[]{HarvestProtocolAgp2Agp.getAgp2AgpMaxItems()});
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("src-u").isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.src.u.err");
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("src-p").isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.src.p.err");
        _valid = false;
      }
      if (!protocol.getDestinationHost().matches(HOST_NAME_REGEX)) {
        mb.addErrorMessage("catalog.harvest.manage.edit.dest.h.err");
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("dest-o").isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.dest.o.err");
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("dest-u").isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.dest.u.err");
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("dest-p").isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.dest.p.err");
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("dest-f").isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.dest.f.err");
        _valid = false;
      }
    }
    return _valid;
  }
}
