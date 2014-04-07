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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolDCAT;
import com.esri.gpt.framework.http.CredentialProvider;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * DCAT validator.
 */
class DCATValidator extends GenericValidator {
  private HarvestProtocolDCAT protocol;

  /**
   * Creates instance of the validator.
   *
   * @param url host URL
   * @param protocol protocol
   */
  public DCATValidator(String url, HarvestProtocolDCAT protocol) {
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
    
    if (protocol.getFormat().isEmpty()) {
      mb.addErrorMessage("catalog.harvest.manage.edit.err.emptyFormat");
      _valid = false;
    } else {
      try {
        Pattern.compile(protocol.getFormat(), Pattern.CASE_INSENSITIVE);
      } catch (PatternSyntaxException ex) {
        mb.addErrorMessage("catalog.harvest.manage.edit.err.invalidFormat");
        _valid = false;
      }
    }

    return _valid;
  }

  @Override
  public boolean checkConnection(IMessageCollector mb) {
    url = url.replaceAll("\\{start\\}", "1").replaceAll("\\{page\\}", "1").replaceAll("\\{max\\}", "10");
    return super.checkConnection(mb);
  }

  @Override
  protected CredentialProvider getCredentialProvider() {
    String userName = protocol.getUserName();
    String userPassword = protocol.getUserPassword();
    if (!userName.isEmpty() && !userPassword.isEmpty()) {
      return new CredentialProvider(userName, userPassword);
    }
    return null;
  }
}
