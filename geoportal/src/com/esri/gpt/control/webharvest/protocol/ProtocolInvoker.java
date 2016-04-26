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
package com.esri.gpt.control.webharvest.protocol;

import com.esri.gpt.framework.robots.BotsMode;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRConnectionException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidProtocolException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Invokes various methods on protocol through Reflection.
 */
public class ProtocolInvoker {
  private static final int DEEP_HARVEST_FLAG = 0;
  private static final int XML_GENERATION_FLAG = 1;
  private static final int AUTO_APPROVE_FLAG = 2;
  private static final int LOCK_TITLE_FLAG = 3;
  private static final int ROBOTS_MODE = 4;
  private static final int RETRY_AS_DRAFT_FLAG = 6;

  /**
   * Invokes ping on the protocol.
   * @param protocol protocol
   * @param url resource URL
   * @throws IllegalArgumentException if invalid protocol definition
   * @throws IOException if error connection resource
   * @deprecated 
   */
  @Deprecated
  public static void ping(Protocol protocol, String url) throws Exception {
    try {
      Method mt = protocol.getClass().getMethod("ping", String.class);
      mt.invoke(protocol, url);
    } catch (InvocationTargetException ex) {
      if (ex.getTargetException()!=null) {
        if (ex.getTargetException().getCause() instanceof HRInvalidProtocolException) {
          throw (Exception)ex.getTargetException().getCause();
        }
        if (ex.getTargetException().getCause() instanceof HRConnectionException) {
          throw (Exception)ex.getTargetException().getCause();
        }
      }
      throw new IllegalArgumentException("Protocol connection exception", ex);
    } catch (Exception ex) {
    }
  }

  /**
   * Checks if 'deep harvest' is enabled.
   * @param protocol protocol
   * @return <code>true</code> if 'deep harvest' is enabled
   */
  public static boolean getUpdateContent(Protocol protocol) {
    return getFlag(protocol.getFlags(), DEEP_HARVEST_FLAG);
  }

  /**
   * Enables/disables 'deep harvest'
   * @param protocol protocol
   * @param enabled <code>true</code> to enable 'deep harvest'
   */
  public static void setUpdateContent(Protocol protocol, boolean enabled) {
    protocol.setFlags(setFlag(protocol.getFlags(), DEEP_HARVEST_FLAG, enabled));
  }

  /**
   * Checks if 'xml generation' is enabled.
   * @param protocol protocol
   * @return <code>true</code> if 'xml generation' is enabled
   */
  public static boolean getUpdateDefinition(Protocol protocol) {
    return getFlag(protocol.getFlags(), XML_GENERATION_FLAG);
  }

  /**
   * Enables/disables 'xml generation'
   * @param protocol protocol
   * @param enabled <code>true</code> to enable 'xml generation'
   */
  public static void setUpdateDefinition(Protocol protocol, boolean enabled) {
    protocol.setFlags(setFlag(protocol.getFlags(), XML_GENERATION_FLAG, enabled));
  }

  /**
   * Checks if 'auto-approve' is enabled.
   * @param protocol protocol
   * @return <code>true</code> if 'auto-approve' is enabled
   */
  public static boolean getAutoApprove(Protocol protocol) {
    return getFlag(protocol.getFlags(), AUTO_APPROVE_FLAG);
  }

  /**
   * Enables/disables 'auto-approve'
   * @param protocol protocol
   * @param autoApprove <code>true</code> to enable 'auto-approve'
   */
  public static void setAutoApprove(Protocol protocol, boolean autoApprove) {
    protocol.setFlags(setFlag(protocol.getFlags(), AUTO_APPROVE_FLAG, autoApprove));
  }

  /**
   * Checks if 'lock-title' is enabled.
   * If a flag is set, it means a title is locked and synchronizer is not allowed
   * to update it, although all the rest of information is allowed to be updated.
   * @param protocol protocol
   * @return <code>true</code> if 'lock-title' is enabled
   */
  public static boolean getLockTitle(Protocol protocol) {
    return getFlag(protocol.getFlags(), LOCK_TITLE_FLAG);
  }

  /**
   * Enables/disables 'lock-title'.
   * If a flag is set, it means a title is locked and synchronizer is not allowed
   * to update it, although all the rest of information is allowed to be updated.
   * @param protocol protocol
   * @param lockTitle <code>true</code> to enable 'lock-title'
   */
  public static void setLockTitle(Protocol protocol, boolean lockTitle) {
    protocol.setFlags(setFlag(protocol.getFlags(), LOCK_TITLE_FLAG, lockTitle));
  }
  
  /**
   * Checks if another attempt to publish as "draft" should be made if first attempt failed validation.
   * first attempt failed.
   * @param protocol protocol
   * @return <code>true</code> if another attempt should be made
   */
  public static boolean getRetryAsDraft(Protocol protocol) {
    return getFlag(protocol.getFlags(), RETRY_AS_DRAFT_FLAG);
  }
  
  /**
   * Allows to make another attempt to publish as a draft if first attempt failed validation.
   * @param protocol protocol
   * @param retryAsDraft <code>true</code> if another attempt should be made
   */
  public static void setRetryAsDraft(Protocol protocol, boolean retryAsDraft) {
    protocol.setFlags(setFlag(protocol.getFlags(), RETRY_AS_DRAFT_FLAG, retryAsDraft));
  }
  
  /**
   * Gets protocol destinations.
   * @param protocol protocol
   * @return list of protocol destinations or <code>null</code>
   */
  public static List<String> getDestinations(Protocol protocol) {
    try {
      Method mt = protocol.getClass().getMethod("getDestinations");
      return (List<String>)mt.invoke(protocol);
    } catch (Exception ex) {
      Logger.getLogger(ProtocolInvoker.class.getCanonicalName()).log(Level.WARNING,"Error getting protocol destinations", ex);
      return null;
    }
  }
  
  /**
   * Sets protocol destinations.
   * @param protocol protocol
   * @param destinations list of protocol destinations or <code>null</code>
   */
  public static void setDestinations(Protocol protocol, List<String> destinations) {
    try {
      Method mt = protocol.getClass().getMethod("setDestinations", List.class);
      mt.invoke(protocol, destinations);
    } catch (Exception ex) {
      Logger.getLogger(ProtocolInvoker.class.getCanonicalName()).log(Level.WARNING,"Error setting protocol destinations", ex);
    }
  }

  /**
   * Sets robots.txt mode.
   * @param protocol protocol
   * @param mode mode
   */
  public static void setRobotsTxtMode(Protocol protocol, BotsMode mode) {
    mode = mode!=null? mode: BotsMode.getDefault();
    
    boolean lb = ((mode.ordinal() & 0x01) == 0x01);
    boolean hb = ((mode.ordinal()>>1 & 0x01) == 0x01);
    
    
    protocol.setFlags(setFlag(protocol.getFlags(),ROBOTS_MODE, lb));
    protocol.setFlags(setFlag(protocol.getFlags(),ROBOTS_MODE+1, hb));
  }
  
  /**
   * Gets robots.txt mode.
   * @param protocol protocol
   * @return mode
   */
  public static BotsMode getRobotsTxtMode(Protocol protocol) {
    boolean lb = getFlag(protocol.getFlags(), ROBOTS_MODE);
    boolean hb = getFlag(protocol.getFlags(), ROBOTS_MODE+1);
    
    int mode = 0;
    mode = (mode<<1) | (hb? 0x01: 0x00);
    mode = (mode<<1) | (lb? 0x01: 0x00);
    
    if (mode < BotsMode.values().length) {
      return BotsMode.values()[mode];
    }
    
    return BotsMode.getDefault();
  }
  
  /**
   * Gets state of the specific flag.
   * @param bits bits representing flag set
   * @param flag number of the flag
   * @return state of the flag
   */
  private static boolean getFlag(long bits, int flag) {
    return ((bits>>flag) & 0x01) == 1;
  }

  /**
   * Sets state of the flag.
   * @param bits bits representing flag set
   * @param flag number of the flag
   * @param state state of the flag
   * @return result flag
   */
  private static long setFlag(long bits, int flag, boolean state) {
    return state? bits | (0x01 << flag): bits & ~(0x01 << flag);
  }
}
