/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.catalog.harvest.clients;

import com.esri.gpt.catalog.harvest.clients.exceptions.HRConnectionException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidProtocolException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidResponseException;
import com.esri.gpt.framework.util.Val;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * ArcGis client.
 * Note, that since 10.0 clients found in this package are no loger in use except
 * to perform 'ping' functionality. Therefore, this class implements only neccessary
 * functionality and can not be considered as a full implementation of the client.
 */
public class HRArcGisClient extends HRProtectedHttpClient {

/** hostUrl */
private String hostUrl;

/**
 * Creates instance of the client.
 * @param hostUrl host URL
 */
public HRArcGisClient(String hostUrl) {
  this.hostUrl = Val.chkStr(hostUrl);
}

@Override
protected String getHostUrl() {
  return hostUrl;
}

@Override
public void ping() throws HRInvalidProtocolException, HRConnectionException, UnsupportedOperationException {
  validateProtocol();
  ping(createPingString());
}

@Override
public boolean isPingSupported() {
  return true;
}

/**
 * Checks and verifies connection to the remote repository.
 * @param pingString ping string
 * @throws HRInvalidProtocolException if provided connection
 * definition is incomplete
 * @throws HRConnectionException if connection to the remote repository can not
 * be established at this moment
 */
private void ping(String pingString)
  throws HRInvalidProtocolException, HRConnectionException {

  InputStream input = null;

  try {

    input = openConnection(pingString);
    analyzePingResponse(input);

  } finally {
    if (input != null) {
      try {
        input.close();
      } catch (IOException ignore) {
      }
    }
  }
}

/**
 * Analyzes response from the ping request.
 * @param response response from the server
 * @throws HRInvalidResponseException if response is invalid
 */
private void analyzePingResponse(InputStream response)
  throws HRInvalidResponseException {
  try {
    BufferedReader reader = new BufferedReader(new InputStreamReader(response));
    reader.readLine();
    reader.close();
  } catch (IOException ex) {
    throw new HRInvalidResponseException("Invalid response.", ex);
  }
}

/**
 * Gets connection string
 * @return connection string
 */
private String createPingString() {

  // remove trailing / or \
  String url = getHostUrl().replaceAll("[/\\\\]\\p{Blank}*$", "");

  if (isFtp()) {
    if (getUserName().length() > 0 && getUserPassword().length() > 0) {
      url = FTP_URL_PFX +
        getUserName() +
        ":" +
        getUserPassword() +
        "@" +
        url.substring(FTP_URL_PFX.length());
    }
    url += "/";
  }

  return url;
}

}
