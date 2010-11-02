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
package com.esri.gpt.catalog.harvest.protocols;

import com.esri.gpt.catalog.harvest.clients.HRARCIMSClient;
import com.esri.gpt.catalog.harvest.clients.HRClient;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.client.arcims.ArcImsQueryBuilder;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.framework.util.Val;

/**
 * ArcIMS protocol.
 */
public class HarvestProtocolArcIms extends AbstractHTTPHarvestProtocol {

// class variables =============================================================
// instance variables ==========================================================
/** Port number. */
private int _portNo = HRARCIMSClient.DEFAULT_PORT_NO;
/** Port number as string. */
private String _portNoAsString = Integer.toString(_portNo);
/** Service name. */
private String _serviceName = "";
/** Root folder. */
private String _rootFolder = "";

// constructors ================================================================
// properties ==================================================================
/**
 * Gets port number.
 * @return port number
 */
public int getPortNo() {
  return _portNo;
}

/**
 * Sets port number.
 * @param portNo port number
 */
public void setPortNo(int portNo) {
  _portNo = portNo >= 0 && portNo < 65536 ? portNo : HRARCIMSClient.DEFAULT_PORT_NO;
  _portNoAsString = Integer.toString(_portNo);
}

/**
 * Gets port number as string.
 * @return port number as string
 */
public String getPortNoAsString() {
  return _portNoAsString;
}

/**
 * Sets port number as string.
 * @param portNoAsString port number as string
 */
public void setPortNoAsString(String portNoAsString) {
  _portNoAsString = Val.chkStr(portNoAsString);
  try {
    int portNo = Integer.parseInt(_portNoAsString);
    if (portNo >= 0 && portNo < 65536) {
      _portNo = portNo;
    }
  } catch (NumberFormatException ex) {
  }
}

/**
 * Gets service name.
 * @return service name
 */
public String getServiceName() {
  return _serviceName;
}

/**
 * Sets servie name.
 * @param serviceName service name
 */
public void setServiceName(String serviceName) {
  _serviceName = Val.chkStr(serviceName);
}

/**
 * Gets root folder.
 * @return root folder
 */
public String getRootFolder() {
  return _rootFolder;
}

/**
 * Sets root folder.
 * @param rootFolder root folder
 */
public void setRootFolder(String rootFolder) {
  _rootFolder = Val.chkStr(rootFolder);
}

// methods =====================================================================
/**
 * Gets protocol type.
 * @return protocol type
 */
public final ProtocolType getType() {
  return ProtocolType.ArcIms;
}

/**
 * Gets all the attributes.
 * @return attributes as attribute map
 */
@Override
protected StringAttributeMap extractAttributeMap() {
  StringAttributeMap properties = super.extractAttributeMap();

  properties.set("service", _serviceName);
  properties.set("port", Integer.toString(_portNo));
  properties.set("rootFolder", _rootFolder);

  return properties;
}

/**
 * Gets all the attributes.
 * @return attributes as attribute map
 */
@Override
public StringAttributeMap getAttributeMap() {
  StringAttributeMap properties = super.getAttributeMap();

  properties.set("service", _serviceName);
  properties.set("port", Integer.toString(_portNo));
  properties.set("rootFolder", _rootFolder);

  return properties;
}

/**
 * Sets all the attributes.
 * @param attributeMap attributes as attribute map
 */
@Override
protected void applyAttributeMap(StringAttributeMap attributeMap) {
  super.applyAttributeMap(attributeMap);
  setServiceName(chckAttr(attributeMap.get("service")));
  setPortNo(Val.chkInt(chckAttr(attributeMap.get("port")), HRARCIMSClient.DEFAULT_PORT_NO));
  setRootFolder(chckAttr(attributeMap.get("rootFolder")));
}

/**
 * Sets all the attributes.
 * @param attributeMap attributes as attribute map
 */
@Override
public void setAttributeMap(StringAttributeMap attributeMap) {
  super.setAttributeMap(attributeMap);
  setServiceName(chckAttr(attributeMap.get("service")));
  setPortNo(Val.chkInt(chckAttr(attributeMap.get("port")), HRARCIMSClient.DEFAULT_PORT_NO));
  setRootFolder(chckAttr(attributeMap.get("rootFolder")));
}

/**
 * Gets harvest client.
 * @return harvest client
 */
@Override
public HRClient getClient(String hostUrl) {
  return new HRARCIMSClient(hostUrl, _portNo, _serviceName, getUserName(),
      getUserPassword(), _rootFolder);
}

public QueryBuilder newQueryBuilder(IterationContext context, String url) {
  return new ArcImsQueryBuilder(context, this, url);
}
}
