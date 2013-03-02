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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.credentials.Credentials;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.Val;
import org.xml.sax.Attributes;

/**
 * Super-class for an ArcIMS request.
 */
public class ImsRequest {

// class variables =============================================================
public  static final String ACTION_STATUS_ERROR    = "ERROR";
public  static final String ACTION_STATUS_NONE     = "NONE";
public  static final String ACTION_STATUS_OK       = "OK";
public  static final String ACTION_STATUS_REPLACED = "REPLACED";
public  static final String ACTION_STATUS_UNKNOWN  = "UNKNOWN";

// instance variables ==========================================================
private String      _actionStatus = ACTION_STATUS_NONE;
private String      _axlRequest = "";
private String      _axlResponse = "";
private Credentials _credentials = null;
private ImsService  _service = null;

// constructors ================================================================

/** Default constructor. */
protected ImsRequest() {}

// properties ==================================================================

/**
 * Gets the action status for the request.
 * @return the action status
 */
public String getActionStatus() {
  return _actionStatus;
}
/**
 * Sets the action status for the request.
 * @param actionStatus the action status
 */
protected void setActionStatus(String actionStatus) {
  actionStatus = Val.chkStr(actionStatus);
  if (actionStatus.equalsIgnoreCase(ACTION_STATUS_NONE)) {
    _actionStatus = ACTION_STATUS_NONE;
  } else if (actionStatus.equalsIgnoreCase(ACTION_STATUS_OK)) {
    _actionStatus = ACTION_STATUS_OK;
  } else if (actionStatus.equalsIgnoreCase(ACTION_STATUS_REPLACED)) {
    _actionStatus = ACTION_STATUS_REPLACED;
  } else if (actionStatus.equalsIgnoreCase(ACTION_STATUS_ERROR)) {
    _actionStatus = ACTION_STATUS_ERROR;
  } else {
    _actionStatus = ACTION_STATUS_UNKNOWN;
  }
}

/**
 * Gets the ArcIMS axl request.
 * @return the axl request
 */
protected String getAxlRequest() {
  return _axlRequest;
}
/**
 * Sets the ArcIMS axl request.
 * @param axlRequest the axl request
 */
protected void setAxlRequest(String axlRequest) {
  _axlRequest = Val.chkStr(axlRequest);
}

/**
 * Gets the ArcIMS axl response.
 * @return the axl response
 */
protected String getAxlResponse() {
  return _axlResponse;
}
/**
 * Sets the ArcIMS axl response.
 * @param axlResponse the axl response
 */
protected void setAxlResponse(String axlResponse) {
  _axlResponse = Val.chkStr(axlResponse);
}

/**
 * Gets the credentials for the user.
 * @return the credentials
 */
public Credentials getCredentials() {
  return _credentials;
}
/**
 * Sets the credentials for the user.
 * @param credentials the credentials
 */
public void setCredentials(Credentials credentials) {
  _credentials = credentials;
}

/**
 * Gets the service to which the request will be sent.
 * @return the service
 */
public ImsService getService() {
  return _service;
}
/**
 * Sets the service to which the request will be sent.
 * @param service the service
 */
public void setService(ImsService service) {
  _service = service;
}

/**
 * Returns true if the action was OK.
 * @return true if the action was OK.
 */
public boolean wasActionOK() {
  return (_actionStatus.equals(ACTION_STATUS_OK) ||
          _actionStatus.equals(ACTION_STATUS_REPLACED));
}

// methods =====================================================================

/**
 * Executes an ArcIMS service request and parses the response.
 * @throws ImsServiceException if an exception occurs
 */
protected void executeRequest() throws ImsServiceException {
  executeRequest(makeClient(null,null));
}

/**
 * Executes an ArcIMS service request and parses the response.
 * @throws ImsServiceException if an exception occurs
 */
protected void executeRequest(ImsClient client) throws ImsServiceException {
  setActionStatus(ACTION_STATUS_NONE);
  setAxlResponse("");
  String sMsg;
  String sErrPfx = "ArcIMS Service Communication Error: ";
    
  // send the request
  try {
    client.setAxlRequest(getAxlRequest());
    client.sendRequest();
    setAxlResponse(client.getAxlResponse());
    int nHttpResponseCode = client.getResponseCode();
    if ((nHttpResponseCode < 200) || (nHttpResponseCode > 299)) {
      throw new IOException("Request failed: HTTP "+nHttpResponseCode);
    }
  } catch (MalformedURLException em) {
    setActionStatus(ACTION_STATUS_ERROR);
    sMsg = sErrPfx+"The PublishServer was configured with a malformed URL";
    throw new ImsServiceException(sMsg,em);
  } catch (UnknownHostException eu) {
    setActionStatus(ACTION_STATUS_ERROR);
    sMsg = sErrPfx+"The PublishServer was configured with an unknown host";
    throw new ImsServiceException(sMsg,eu);
  } catch (Exception e) {
    setActionStatus(ACTION_STATUS_ERROR);
    int nHttpResponseCode = client.getResponseCode();
    if (nHttpResponseCode == 0) {
      sMsg = Val.chkStr(e.getMessage());
      sMsg = sErrPfx+sMsg;
      throw new ImsServiceException(sMsg,e);
    } else {
      sMsg = sErrPfx+"[HTTP "+nHttpResponseCode+"] "+client.getResponseCodeText();
      throw new ImsServiceException(sMsg,e);
    }
  }
  
  // parse the response
  if (getAxlResponse().length() == 0) {
    throw new ImsServiceException(sErrPfx+"Empty response.");
  } else {
    try {
      ImsSaxHandler handler = new ImsSaxHandler();
      handler.parseAxlResponse(this,getAxlResponse());
    } catch (ImsSaxException ep) {
      setActionStatus(ACTION_STATUS_ERROR);
      sMsg = Val.chkStr(ep.getMessage());
  
      if (sMsg.startsWith("[ERR1042]")) {
        sMsg = sErrPfx+"[ERR1042] Insufficient permission.";
      } else if (sMsg.startsWith("[ERR0134]")) {
        sMsg = sErrPfx+"[ERR0134] Service is unavalible.";
      } else if (sMsg.startsWith("[ERR1005]")) {
        sMsg = sErrPfx+"[ERR1005] Request is not valid ArcXML.";
      }
      throw new ImsResponseException(sMsg,ep);
    } catch (Exception e) {
      setActionStatus(ACTION_STATUS_ERROR);
      sMsg = sErrPfx+"Error parsing response.";
      throw new ImsServiceException(sMsg,e);
    }
  }
  
  // set the status to ok if it has not been reset elsewhere
  if (getActionStatus().equals(ACTION_STATUS_NONE)) {
    setActionStatus(ACTION_STATUS_OK);
  }
}

/**
 * Makes a suitable client for the service.
 * @param context the active request context (only required
 *        for socket request against the GPT_Browse_Service or GPT_Publish_Service
 * @param publisher the publisher associated with the request (only required
 *        for a socket request against the GPT_Publish_Service
 * @return the client
 */
protected ImsClient makeClient(RequestContext context, Publisher publisher) {
  ImsClient client = null;
  ImsService service = getService();
  ImsHttpClient httpClient = new ImsHttpClient();
  httpClient.setCredentials(getCredentials());
  httpClient.setUrl(service.getFullServiceUrl());
  httpClient.setTimeoutMillisecs(service.getTimeoutMillisecs());
  client = httpClient;
  return client;
}

/**
 * Triggered when a SAX element is ended during the parsing of an axl response.
 * @param lowerCaseTagName the lower-case tag name of the element
 * @param characters the text node value of the element
 */
protected void onEndSaxElement(String lowerCaseTagName, String characters) {
  // no default implementation
}

/**
 * Triggered when a SAX element is started during the parsing of an axl response.
 * @param lowerCaseTagName the lower-case tag name of the element
 * @param attributes the element attributes
 */
protected void onStartSaxElement(String lowerCaseTagName, Attributes attributes) {
  // no default implementation
}

}

