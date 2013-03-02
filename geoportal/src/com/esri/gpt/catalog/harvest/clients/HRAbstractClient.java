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

/**
 * Abstract harvest repository client.
 * Provides basic implementation of the harvest repository client.
 */
public abstract class HRAbstractClient implements HRClient {

// class variables =============================================================
/** connection timeout. */
private static final int TIMEOUT = 5000;

// instance variables ==========================================================

// constructors ================================================================

// properties ==================================================================
/**
 * Gets connection timeout.
 * @return timeout in milliseconds
 */
protected int getTimeout() {
  return TIMEOUT;
}

/**
 * Gets host url.
 * @return host url
 */
protected abstract String getHostUrl();

// methods =====================================================================

/**
 * Checks if <i>ping</i> operation is supported.
 * @return <code>true</code> if <i>ping</i> operation is supported
 * @see HRClient#ping
 */  
@Override
public boolean isPingSupported() {
  return true;
}

/**
 * Returns a string representation of the object.
 * @return string representation of the object
 */
@Override
public String toString() {
  return "URL:" + getHostUrl();
}

/**
 * Validates protocol definition.
 * @throws HRInvalidProtocolException if invalid protocol definition
 */
protected void validateProtocol()
  throws HRInvalidProtocolException {
  // check if host url has a value
  if (getHostUrl().length() == 0) {
    throw new HRInvalidProtocolException(
      HRInvalidProtocolException.ProtocolElement.url,
      "Empty host URL.");
  }
}
}
