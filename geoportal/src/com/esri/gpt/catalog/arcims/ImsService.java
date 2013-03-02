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

/**
 * Stores a reference to an ArcIMS service.
 */
public class ImsService {

// class variables =============================================================

// instance variables ==========================================================
private String  _fullServiceUrl = "";
private String  _serverUrl = "";
private String  _serviceName = "";
private int     _timeoutMillisecs = 0;

// constructors ================================================================

/** Default constructor. */
public ImsService() {}

// properties ==================================================================

/**
 * Gets the full URL for accessing the service.
 * @return the url
 */
public String getFullServiceUrl() {
  return _fullServiceUrl;
}
/**
 * Sets the full URL for accessing the service.
 */
private void setFullServiceUrl() {
  String sUrl = getServerUrl();
  if (sUrl.length() > 0) {
    String sEsrimap = "servlet/com.esri.esrimap.Esrimap";
    if ((sUrl.indexOf(sEsrimap) == -1) && 
        (sUrl.indexOf("?") == -1) &&
        (sUrl.indexOf("&") == -1)) {
      if (!sUrl.endsWith("/")) {
        sUrl += "/";
      }
      sUrl += sEsrimap+"?ServiceName="+getServiceName();
    }
  }
  _fullServiceUrl = sUrl;
}

/**
 * Determines if the service requires socket based communication.
 * @return true if the service requires socket based communication
 */
public boolean getIsSocketBased() {
  return (getServerUrl().length() == 0);
}

/**
 * Gets the server url.
 * @return the server url
 */
public String getServerUrl() {
  return _serverUrl;
}
/**
 * Sets the server url.
 * @param url the server url
 */
public void setServerUrl(String url) {
  _serverUrl = Val.chkStr(url);
  setFullServiceUrl();
}

/**
 * Gets the service name.
 * @return the service name
 */
public String getServiceName() {
  return _serviceName;
}

/**
 * Sets the service name.
 * @param name the service name.
 */
public void setServiceName(String name) {
  _serviceName = Val.chkStr(name);
  setFullServiceUrl();
}

/**
 * Gets the HTTP connection timeout in milli-seconds.
 * <br/>0 indicates no timeout.
 * @return the connection timeout
 */
public int getTimeoutMillisecs() {
  return _timeoutMillisecs;
}
/**
 * Sets the HTTP connection timeout in milli-seconds.
 * <br/>0 indicates no timeout.
 * @param millisecs connection timeout
 */
public void setTimeoutMillisecs(int millisecs) {
  _timeoutMillisecs = millisecs;
  if (_timeoutMillisecs < 0) {
    _timeoutMillisecs= 0;
  }
}

// methods =====================================================================

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append(" serviceName=\"").append(getServiceName()).append("\"\n");
  sb.append(" isSocketBased=\"").append(getIsSocketBased()).append("\"\n");
  if (!getIsSocketBased()) {
    sb.append(" serverUrl=\"").append(getServerUrl()).append("\"\n");
    sb.append(" fullServiceUrl=\"").append(getFullServiceUrl()).append("\"\n");
  }
  sb.append(" timeoutMillisecs=\"").append(getTimeoutMillisecs()).append("\"\n");
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}

}

