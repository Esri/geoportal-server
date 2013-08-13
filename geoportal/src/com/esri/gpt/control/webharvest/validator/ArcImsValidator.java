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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolArcIms;

/**
 * ArcIMS protocol validator.
 */
class ArcImsValidator extends GenericValidator {
  private HarvestProtocolArcIms protocol;

  /**
   * Creates instance of the validator.
   * @param url host URL
   * @param protocol protocol
   */
  public ArcImsValidator(String url, HarvestProtocolArcIms protocol) {
    super(url);
    this.protocol = protocol;
  }

  @Override
  public boolean validate(IMessageCollector mb) {
    boolean _valid = true;

    if (url.isEmpty()) {
      mb.addErrorMessage("catalog.harvest.manage.edit.err.hostUrlReq");
      _valid = false;
    }

    if (protocol.getPortNoAsString().length() == 0) {
      mb.addErrorMessage("catalog.harvest.manage.edit.err.portNumberReq");
      _valid = false;
    } else {
      try {
        int portNo = Integer.parseInt(protocol.getPortNoAsString());
        if (!(portNo >= 0 && portNo < 65536)) {
          mb.addErrorMessage("catalog.harvest.manage.edit.err.portNumberInv");
          _valid = false;
        }
      } catch (NumberFormatException ex) {
        mb.addErrorMessage("catalog.harvest.manage.edit.err.portNumberInv");
        _valid = false;
      }
    }
    if (protocol.getServiceName().length() == 0) {
      mb.addErrorMessage("catalog.harvest.manage.edit.err.serviceNameReq");
      _valid = false;
    }

    return _valid;
  }
  
}
