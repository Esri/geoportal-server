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

import com.esri.gpt.catalog.harvest.clients.HRClient;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRConnectionException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidProtocolException;
import com.esri.gpt.control.webharvest.protocol.Protocol;
import com.esri.gpt.control.webharvest.protocol.ProtocolSerializer;
import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;

/**
 * Generic harvesting protocol.
 * @see com.esri.gpt.catalog.harvest.repository.HrRecord
 */
public abstract class HarvestProtocol implements Protocol, Serializable {

// class variables =============================================================
// instance variables ==========================================================
private long flags;
// constructors ================================================================
// properties ==================================================================

/**
 * Checks if <i>ping</i> operation is supported.
 * @return <code>true</code> if <i>ping</i> operation is supported
 * @see HRClient#ping
 */
public boolean getPingSupported() {
  try {
    return getClient(null).isPingSupported();
  } catch (HRInvalidProtocolException ex) {
    return false;
  }
}

// methods =====================================================================

/**
 * Gets protocol type.
 * @return protocol type
 */
public abstract ProtocolType getType();

@Override
public String getKind() {
  return getType().name();
}

/**
 * Gets client associated with particular protocol.
 * @param hostUrl host URL
 * @return instance of the harvest repository client
 * @throws HRInvalidProtocolException if unable to create client for 
 * the protocol
 */
public abstract HRClient getClient(String hostUrl)
    throws HRInvalidProtocolException;

/**
 * Checks connection to the specific server.
 * @param url server URL 
 * @throws HRInvalidProtocolException when protocol attributes are invalid
 * @throws HRConnectionException if connecting remote repository failed
 */
public final void checkConnection(String url)
    throws HRInvalidProtocolException, HRConnectionException {
  getClient(url).ping();
}

/**
 * Pings resource.
 * @param url resource URL
 * @throws IllegalArgumentException if invalid protocol definition
 * @throws IOException if error connection resource
 */
public final void ping(String url) throws Exception {
  try {
    checkConnection(url);
  } catch (HRInvalidProtocolException ex) {
    throw new Exception("Invalid protocol definition", ex);
  } catch (HRConnectionException ex) {
    throw new Exception("Protocol connection exception", ex);
  }
}

/**
 * Creates xml string representation of the protocol.
 * @return xml string representation of the protocol
 */
public String toXmlString() {
  return ProtocolSerializer.toXmlString(this);
}

/**
 * Gets string representation of the protocol.
 * @return string representation of the protocol
 */
@Override
public String toString() {
  StringBuilder sb = new StringBuilder();

  sb.append(getType().toString());
  for (String key : extractAttributeMap().keySet()) {
    StringAttribute value = extractAttributeMap().get(key);
    sb.append(" ").append(key).append(":").append(value.getValue());
  }

  return sb.toString();
}

/**
 * Gets all the attributes.
 * @return attributes as attribute map
 */
protected abstract StringAttributeMap extractAttributeMap();

/**
 * Sets all the attributes.
 * @param attributeMap attributes as attribute map
 */
protected abstract void applyAttributeMap(StringAttributeMap attributeMap);

/**
 * Checks attribute.
 * @param attribute attributes
 * @return attribute value
 */
protected String chckAttr(StringAttribute attribute) {
  return attribute != null ? attribute.getValue() : "";
}

@Override
public long getFlags() {
  return flags;
}

@Override
public void setFlags(long flags) {
  this.flags = flags;
}

// custom types ================================================================
/**
 * Protocol type.
 */
public enum ProtocolType {

/** No protocol defined. */
None("None"),
/** ESRI Metadata Services */
ArcIms("ESRI MS"),
/** OAI */
OAI("OAI"),
/** WAF */
WAF("WAF"),
/** CSW */
CSW("CSW"),
/** Resource */
RES("RES");
/** protocol id */
private String _id;

/**
 * Creates instance of the enum element.
 * @param id protocol id
 */
ProtocolType(String id) {
  _id = id;
}

/**
 * Gets id of the protocol.
 * @return id of the protocol
 */
public String getId() {
  return _id;
}

/**
 * Checks type.
 * @param name type name
 * @return type or <code>Type.None</code> if type unrecognized
 */
public static ProtocolType checkValueOf(String name) {
  name = Val.chkStr(name);
  for (ProtocolType t : values()) {
    if (t.name().equalsIgnoreCase(name) || t.getId().equalsIgnoreCase(name)) {
      return t;
    }
  }
  LogUtil.getLogger().log(Level.SEVERE, "Error parsing ProtocolType value: {0}", name);
  return None;
}
}
}
