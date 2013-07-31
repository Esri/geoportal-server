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
import com.esri.gpt.control.webharvest.engine.DataProcessor;
import com.esri.gpt.control.webharvest.engine.ExecutionUnit;
import com.esri.gpt.control.webharvest.engine.Executor;
import com.esri.gpt.control.webharvest.engine.IWorker;
import com.esri.gpt.control.webharvest.protocol.Protocol;
import com.esri.gpt.control.webharvest.protocol.ProtocolSerializer;
import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.security.codec.PC1_Encryptor;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Generic harvesting protocol.
 * @see com.esri.gpt.catalog.harvest.repository.HrRecord
 */
public abstract class HarvestProtocol implements Protocol, Serializable {

// class variables =============================================================
  private static final Pattern PATTERN = Pattern.compile("(\\p{Digit}{1,3}\\-)*\\p{Digit}{1,3}");
// instance variables ==========================================================
  private long flags;
  /** destinations */
  private List<String> destinations;
  /** ad-hoc */
  private String addHoc = "";

  // constructors ================================================================
  public HarvestProtocol() {
  }
  
// properties ==================================================================
  
  /**
   * Sets destinations.
   * @param destinations destinations
   */
  public void setDestinations(List<String> destinations) {
    this.destinations = destinations;
  }
  
  /**
   * Gets destinations.
   * @return destinations
   */
  public List<String> getDestinations() {
    return destinations;
  }

  /**
   * Checks if <i>ping</i> operation is supported.
   * @return <code>true</code> if <i>ping</i> operation is supported
   * @see HRClient#ping
   */
  public boolean getPingSupported() {
    return true;
  }

// methods =====================================================================
  /**
   * Gets protocol type.
   * @return protocol type
   * @deprecated 
   */
  @Deprecated
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
   * @deprecated
   */
  @Deprecated
  public HRClient getClient(String hostUrl)
      throws HRInvalidProtocolException {
    return null;
  }

  /**
   * Checks connection to the specific server.
   * @param url server URL 
   * @throws HRInvalidProtocolException when protocol attributes are invalid
   * @throws HRConnectionException if connecting remote repository failed
   * @deprecated 
   */
  @Deprecated
  public final void checkConnection(String url)
      throws HRInvalidProtocolException, HRConnectionException {
    HRClient client = getClient(url);
    if (client != null) {
      client.ping();
    }
  }

  /**
   * Pings resource.
   * @param url resource URL
   * @throws IllegalArgumentException if invalid protocol definition
   * @throws IOException if error connection resource
   * @deprecated
   */
  @Deprecated
  public void ping(String url) throws Exception {
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

    sb.append(getKind());
    for (String key : extractAttributeMap().keySet()) {
      StringAttribute value = extractAttributeMap().get(key);
      sb.append(" ").append(key).append(":").append(value.getValue());
    }

    return sb.toString();
  }

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

  @Override
  public StringAttributeMap getAttributeMap() {
    return new StringAttributeMap();
  }

  @Override
  public void setAttributeMap(StringAttributeMap attributeMap) {
  }

  @Override
  public StringAttributeMap extractAttributeMap() {
    // default implementationis to do exactly what getAttributeMap() does
    return getAttributeMap();
  }

  @Override
  public void applyAttributeMap(StringAttributeMap attributeMap) {
    // default implementation is to do exatly what setAttributeMap() does
    setAttributeMap(attributeMap);
  }

  @Override
  public String getAdHoc() {
    return addHoc;
  }

  @Override
  public void setAdHoc(String adHoc) {
    this.addHoc = Val.chkStr(adHoc);
  }

  @Override
  public Executor newExecutor(DataProcessor dataProcessor, ExecutionUnit unit, IWorker worker) {
    return new ExecutorImpl(dataProcessor, unit, worker);
  }

  /**
   * Decrypts string.
   * @param s string to decrypt
   * @return decrypted string
   */
  protected String decryptString(String s) {
    s = Val.chkStr(s);
    if (PATTERN.matcher(s).matches()) {
      String sEncKey = getEncKey();
      if (sEncKey.length() > 0 && s.length() > 0) {
        try {
          s = PC1_Encryptor.decrypt(sEncKey, s);
        } catch (IllegalArgumentException ex) {
        }
      }
    }
    return s;
  }

  /**
   * Encrypts string.
   * @param s string to encrypt
   * @return encrypted string
   */
  protected String encryptString(String s) {
    s = Val.chkStr(s);
    if (!s.isEmpty()) {
      String sEncKey = getEncKey();
      if (sEncKey.length() > 0) {
        s = PC1_Encryptor.encrypt(sEncKey, s);
      }
    }
    return s;
  }

  /**
   * Gets encryption key.
   * @return encryption key
   */
  private String getEncKey() {
    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();
    return appCfg.getIdentityConfiguration().getEncKey();
  }

// custom types ================================================================
  /**
   * Protocol type.
   * @deprecated 
   */
  @Deprecated
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
    RES("RES"),
    /* ArcGIS Portal */
    /* NOTE! This is EXPERIMENTAL feature. It might be removed at any time in the future. */
    AGP("AGP"),
    /** Atom */
    ATOM("ATOM");
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
  
  /**
   * Executor implementation.
   */
  private static class ExecutorImpl extends Executor {
    private IWorker worker;

    public ExecutorImpl(DataProcessor dataProcessor, ExecutionUnit unit, IWorker worker) {
      super(dataProcessor, unit);
      this.worker = worker;
    }

    @Override
    protected boolean isActive() {
      return worker.isActive();
    }

    @Override
    protected boolean isShutdown() {
      return worker.isShutdown();
    }

    @Override
    protected boolean isSuspended() {
      return worker.isSuspended();
    }
  }
}
