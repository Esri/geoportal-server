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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolAgs2Agp;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.StringHandler;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AGS to AGP protocol validator.
 */
class Ags2AgpValidator extends AgpValidator {

  private String url;
  private HarvestProtocolAgs2Agp protocol;
  
  /**
   * Creates instance of the validator.
   *
   * @param url host URL
   * @param protocol protocol
   */
  public Ags2AgpValidator(String url, HarvestProtocolAgs2Agp protocol) {
    this.url = url;
    this.protocol = protocol;
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
    }
    if (_valid) {
      if (protocol.getAttributeMap().getValue("ags-src-restUrl").length() == 0) {
        mb.addErrorMessage("catalog.harvest.manage.edit.err.hostUrlReq");
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("ags-src-soapUrl").length() == 0) {
        mb.addErrorMessage("catalog.harvest.manage.edit.err.soapUrl");
        _valid = false;
      }
      if (!protocol.getDestinationHost().matches(HOST_NAME_REGEX)) {
        mb.addErrorMessage("catalog.harvest.manage.edit.dest.h.err");
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("ags-dest-o").isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.dest.o.err");
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("ags-dest-u").isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.dest.u.err");
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("ags-dest-p").isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.dest.p.err");
        _valid = false;
      }
      if (protocol.getAttributeMap().getValue("ags-dest-f").isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.edit.dest.f.err");
        _valid = false;
      }
    }
    return _valid;
  }

  @Override
  public boolean checkConnection(IMessageCollector mb) {
    try {
      HttpClientRequest httpRequest = new HttpClientRequest();
      httpRequest.setUrl(protocol.getSourceHost());
      httpRequest.setContentHandler(new StringHandler());
      httpRequest.execute();
      return true;
    } catch (IOException ex) {
      mb.addErrorMessage("catalog.harvest.manage.test.err.HarvestConnectionException");
    }
    return false;
  }

  @Override
  public boolean checkDestinationConnection(IMessageCollector mb) {
    return checkConnection(mb, protocol.getDestination());
  }
}
