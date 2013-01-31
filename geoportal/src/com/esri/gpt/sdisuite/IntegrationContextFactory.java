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
package com.esri.gpt.sdisuite;
import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.ResourcePath;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A factory for configuring and instantiating the sdi.suite integration context.
 */
public class IntegrationContextFactory {
 
  /** class variables ========================================================= */
  
  /** The configuration. */
  private static StringAttributeMap CONFIG;
  
  /** The configuration file location. */
  public static final String CONFIG_FILE = "gpt/config/gpt-tc.xml";
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(IntegrationContextFactory.class.getName());
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public IntegrationContextFactory() {}
  
  /** main ==================================================================== */
  
  /**
   * Main unit test method.
   * @param args startup arguments
   */
  public static void main(String[] args) {
    RequestContext rc = null;
    try {
      
      IntegrationContextFactory self = new IntegrationContextFactory();
      System.out.println("isIntegrationEnabled="+self.isIntegrationEnabled());
      IntegrationContext ictx = self.newIntegrationContext();
      if (ictx == null) {
        System.out.println("newIntegrationContext=null");
      } else {
        System.out.println("sdisuite.securityManagerUrl="+self.getSecurityManagerUrl());
      }
      
    } catch (Throwable t) {
      t.printStackTrace(System.err);
    } finally {
      if (rc != null) rc.onExecutionPhaseCompleted();
    }
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the configuration.
   * @return the configuration (can be null)
   */
  public StringAttributeMap getConfiguration() {
    StringAttributeMap config = IntegrationContextFactory.CONFIG;
    if (config != null) {
      return config;
    } else {
      configure();
      return IntegrationContextFactory.CONFIG;
    }
  }
  
  /**
   * Gets the URL for viewing a user's license list..
   * @return the URL (can be null if not integrated)
   */
  public String getLicensesUrl() {
    if (this.isIntegrationEnabled()) {
      return Val.chkStr(this.getConfiguration().getValue("sdisuite.licensesUrl"));
    }
    return "";
  }
  
  /**
   * Gets the URL to the security manager user interface.
   * @return the URL (can be null if not integrated)
   */
  public String getSecurityManagerUrl() {
    if (this.isIntegrationEnabled()) {
      return Val.chkStr(this.getConfiguration().getValue("sdisuite.securityManagerUrl"));
    }
    return "";
  }
  
  /**
   * Gets the URL to the service monitor user interface.
   * @return the URL (can be null if not integrated)
   */
  public String getServiceMonitorUrl() {
    if (this.isIntegrationEnabled()) {
      return Val.chkStr(this.getConfiguration().getValue("sdisuite.serviceMonitorUrl"));
    }
    return "";
  }
  
  /**
   * Gets the URL to the smart editor user interface.
   * @return the URL (can be null if not integrated)
   */
  public String getSmartEditorUrl() {
    if (this.isIntegrationEnabled()) {
      return Val.chkStr(this.getConfiguration().getValue("sdisuite.smartEditorUrl"));
    }
    return "";
  }
  
  /**
   * Gets the URL to the smart editor user interface (for opening an existing document).
   * @return the URL (can be null if not integrated)
   */
  public String getSmartEditorStartWithUrl() {
    if (this.isIntegrationEnabled()) {
      return Val.chkStr(this.getConfiguration().getValue("sdisuite.smartEditorStartWithUrl"));
    }
    return "";
  }
  
  /** methods ================================================================= */
  
  /**
   * Builds the configuration if necessary. 
   */
  private synchronized void configure() {
    StringAttributeMap config = IntegrationContextFactory.CONFIG;
    if (config == null) {
      config = new StringAttributeMap();
      
      String configFile = IntegrationContextFactory.CONFIG_FILE;
      URL configUrl = null;
      ResourcePath rp = new ResourcePath();
      try {
        configUrl = rp.makeUrl(configFile);
      } catch (IOException e) {
        LOGGER.finer(configFile+" was not loaded.");
      }
      if (configUrl != null) {
        
        String err = "Exception while loading: "+configFile;
        try {
          LOGGER.config("Loading configuration file: "+configFile);
          Document dom = DomUtil.makeDomFromResourcePath(configFile,false);
          XPath xpath = XPathFactory.newInstance().newXPath();
          NodeList nl = (NodeList)xpath.evaluate("//parameter",dom,XPathConstants.NODESET);
          for (int i=0; i< nl.getLength(); i++) {
            Node nd = nl.item(i);
            String key = Val.chkStr(xpath.evaluate("@key",nd));
            String value = Val.chkStr(xpath.evaluate("@value",nd));
            if (key.length() > 0) {
              config.add(new StringAttribute(key,value));
            }
          }          
        } catch (ParserConfigurationException e) {
          LOGGER.log(Level.SEVERE,err,e);
        } catch (SAXException e) {
          LOGGER.log(Level.SEVERE,err,e);
        } catch (IOException e) {
          LOGGER.log(Level.SEVERE,err,e);
        } catch (XPathExpressionException e) {
          LOGGER.log(Level.SEVERE,err,e);
        }
        
      }

      IntegrationContextFactory.CONFIG = config;
    } 
  }
  
  /**
   * Determines if the integrating is enabled.
   * @return <code>true</code> if enabled
   */
  public boolean isIntegrationEnabled() {
    StringAttributeMap config = this.getConfiguration();
    if (config != null) {
      String val = Val.chkStr(config.getValue("sdisuite.enabled"));
      return (val.equalsIgnoreCase("true"));
    }
    return false;
  }
  
  /**
   * Instantiates a new integration context.
   * <p/>
   * This instantiated class is based upon the configuration parameter:
   * <br/>gpt/config/gpt-tc.xml ... //parameter/@key="sdisuite.integrationContextClass"
   * <p/>
   * The method will return null if:
   * <br/>gpt/config/gpt-tc.xml does not exist
   * <br/>//parameter/@key="sdisuite.enabled" is not set to true
   * <br/>//parameter/@key="sdisuite.integrationContextClass" is empty
   * @return the integration context (can be null if not integrated)
   * @throws ClassNotFoundException if the class was not found
   * @throws InstantiationException if the class could not be instantiated
   * @throws IllegalAccessException if the class could not be accessed
   */
  public IntegrationContext newIntegrationContext() 
    throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    
    StringAttributeMap config = this.getConfiguration();
    if (config == null) {
      return null;
    } else {
      String val = Val.chkStr(config.getValue("sdisuite.enabled"));
      if (!val.equalsIgnoreCase("true")) {
        return null;
      }
    }
    
    String key = "sdisuite.integrationContextClass";
    String className =Val.chkStr(config.getValue(key));
    if (className.length() == 0) {
      String msg = "The configured "+key+" was empty";
      throw new ConfigurationException(msg);
    } else {
      Class<?> cls = Class.forName(className);
      Object obj = cls.newInstance();
      if (obj instanceof IntegrationContext) {
        IntegrationContext ictx = (IntegrationContext)obj;
        ictx.setConfig(config);
        return ictx;
      } else {
        String msg = "The configured "+key+" is invalid: "+ className;
        throw new ConfigurationException(msg);
      }
    }
    
  }

}
