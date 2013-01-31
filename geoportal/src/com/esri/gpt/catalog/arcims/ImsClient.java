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
package com.esri.gpt.catalog.arcims;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;

/**
 * Super class for an ArcIMS client.
 */
public abstract class ImsClient {

// class variables =============================================================

// instance variables ==========================================================
private String _axlRequest = "";
private String _axlResponse = "";
private int    _responseCode = 0;
private String _responseCodeText = "";
private int    _timeoutMillisecs = 0;

// constructors ================================================================

/** Default constructor. */
public ImsClient() {}

// properties ==================================================================

/**
 * Gets the ArcXML request.
 * @return the ArcXML request
 */
public String getAxlRequest() {
  return _axlRequest;
}

/**
 * Sets the ArcXML request.
 * @param axl the ArcXML request
 */
public void setAxlRequest(String axl) {
  _axlRequest = Val.chkStr(axl);
}

/**
 * Gets the ArcXML response.
 * @return the ArcXML response
 */
public String getAxlResponse() {
  return _axlResponse;
}

/**
 * Sets the ArcXML response.
 * @param axl the ArcXML response
 */
protected void setAxlResponse(String axl) {
  _axlResponse = Val.chkStr(axl);
}

/**
 * Gets the HTTP response code.
 * <ul>
 * <li>1xx: Informational</li>
 * <li>2xx: Success</li>
 * <li>3xx: Redirection</li>
 * <li>4xx: Client Error</li>
 * <li>5xx: Server Error</li> 
 * </ul>
 * @return the HTTP response code
 */
public int getResponseCode() {
  return _responseCode;
}

/**
 * Sets the HTTP response code.
 * <ul>
 * <li>1xx: Informational</li>
 * <li>2xx: Success</li>
 * <li>3xx: Redirection</li>
 * <li>4xx: Client Error</li>
 * <li>5xx: Server Error</li> 
 * </ul>
 * @param responseCode the HTTP response code
 */
protected void setResponseCode(int responseCode) {
  _responseCode = responseCode;
}

/**
 * Gets the HTTP response code text.
 * @return the HTTP response code text
 */
public String getResponseCodeText() {
  return _responseCodeText;
}

/**
 * Sets the HTTP response code text.
 * @param responseCodeText the HTTP response code text
 */
protected void setResponseCodeText(String responseCodeText) {
  _responseCodeText = responseCodeText;
}

/**
 * Gets the timeout in milli-seconds.
 * <br/>0 indicates no timeout.
 * @return the timeout
 */
public int getTimeoutMillisecs() {
  return _timeoutMillisecs;
}

/**
 * Sets the timeout in milli-seconds.
 * <br/>0 indicates no timeout.
 * @param millisecs the timeout
 */
public void setTimeoutMillisecs(int millisecs) {
  _timeoutMillisecs = millisecs;
  if (_timeoutMillisecs < 0) _timeoutMillisecs = 0;
}

// methods =====================================================================

/**
 * Sends the request.
 * @throws IOException if a communication exception occurs
 */
public abstract void sendRequest() throws IOException;

}
