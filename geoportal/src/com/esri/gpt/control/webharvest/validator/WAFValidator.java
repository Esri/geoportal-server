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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolWaf;
import com.esri.gpt.framework.http.CredentialProvider;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import org.apache.commons.net.ftp.FTPClient;

/**
 * WAF protocol validator.
 */
class WAFValidator extends GenericValidator {
  private HarvestProtocolWaf protocol;

  /**
   * Creates instance of the validator.
   *
   * @param url host URL
   * @param protocol protocol
   */
  public WAFValidator(String url, HarvestProtocolWaf protocol) {
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

    return _valid;
  }

  @Override
  public boolean checkConnection(IMessageCollector mb) {
    if (url.toLowerCase().startsWith("ftp://")) {
      FTPClient client = null;
      try {
        URL u = new URL(url);
        String host = u.getHost();
        int port = u.getPort() >= 0 ? u.getPort() : 21;
        client = new FTPClient();
        client.connect(host, port);
        CredentialProvider cp = getCredentialProvider();
        if (cp == null) {
          client.login("anonymous", "anonymous");
        } else {
          client.login(cp.getUsername(), cp.getPassword());
        }
        return true;
      } catch (MalformedURLException ex) {
        mb.addErrorMessage("catalog.harvest.manage.test.err.HarvestInvalidUrl");
        return false;
      } catch (IOException ex) {
        mb.addErrorMessage("catalog.harvest.manage.test.err.HarvestConnectionException");
        return false;
      } finally {
        if (client!=null) {
          try {
            client.disconnect();
          } catch (IOException ex) {}
        }
      }
    } else {
      return super.checkConnection(mb);
    }
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
