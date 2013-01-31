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
public static final int DEFAULT_PORT_NO = 80;
// instance variables ==========================================================
  /** Port number. */
  private int _portNo = DEFAULT_PORT_NO;
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
    _portNo = portNo >= 0 && portNo < 65536 ? portNo : DEFAULT_PORT_NO;
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
   * @deprecated 
   */
  @Override
  @Deprecated
  public final ProtocolType getType() {
    return ProtocolType.ArcIms;
  }

  @Override
  public String getKind() {
    return "ArcIms";
  }

  /**
   * Gets all the attributes.
   * @return attributes as attribute map
   */
  @Override
  public StringAttributeMap extractAttributeMap() {
    StringAttributeMap properties = new StringAttributeMap();

    properties.set("username", encryptString(getUserName()));
    properties.set("password", encryptString(getUserPassword()));
    properties.set("service", getServiceName());
    properties.set("port", Integer.toString(getPortNo()));
    properties.set("rootFolder", getRootFolder());

    return properties;
  }

  /**
   * Gets all the attributes.
   * @return attributes as attribute map
   */
  @Override
  public StringAttributeMap getAttributeMap() {
    StringAttributeMap properties = new StringAttributeMap();

    properties.set("arcims.username", getUserName());
    properties.set("arcims.password", getUserPassword());
    properties.set("service", getServiceName());
    properties.set("port", Integer.toString(getPortNo()));
    properties.set("rootFolder", getRootFolder());

    return properties;
  }

  /**
   * Sets all the attributes.
   * @param attributeMap attributes as attribute map
   */
  @Override
  public void applyAttributeMap(StringAttributeMap attributeMap) {
    setUserName(decryptString(chckAttr(attributeMap.get("username"))));
    setUserPassword(decryptString(chckAttr(attributeMap.get("password"))));
    setServiceName(chckAttr(attributeMap.get("service")));
    setPortNo(Val.chkInt(chckAttr(attributeMap.get("port")), DEFAULT_PORT_NO));
    setRootFolder(chckAttr(attributeMap.get("rootFolder")));
  }

  /**
   * Sets all the attributes.
   * @param attributeMap attributes as attribute map
   */
  @Override
  public void setAttributeMap(StringAttributeMap attributeMap) {
    setUserName(chckAttr(attributeMap.get("arcims.username")));
    setUserPassword(chckAttr(attributeMap.get("arcims.password")));
    setServiceName(chckAttr(attributeMap.get("service")));
    setPortNo(Val.chkInt(chckAttr(attributeMap.get("port")), DEFAULT_PORT_NO));
    setRootFolder(chckAttr(attributeMap.get("rootFolder")));
  }

  @Override
  public QueryBuilder newQueryBuilder(IterationContext context, String url) {
    return new ArcImsQueryBuilder(context, this, url);
  }
}
