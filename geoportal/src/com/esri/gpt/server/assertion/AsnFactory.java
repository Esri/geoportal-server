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
package com.esri.gpt.server.assertion;
import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.assertion.components.AsnConstants;
import com.esri.gpt.server.assertion.components.AsnContext;
import com.esri.gpt.server.assertion.components.AsnOperation;
import com.esri.gpt.server.assertion.components.AsnSubject;
import com.esri.gpt.server.assertion.exception.AsnInvalidOperationException;
import com.esri.gpt.server.assertion.handler.AsnOperationHandler;
import com.esri.gpt.server.assertion.handler.AsnRequestHandler;
import com.esri.gpt.server.assertion.index.AsnSystemPart;

import javax.servlet.http.HttpServletRequest;

/**
 * Instantiates components associated with the execution of assertion operations. 
 */
public class AsnFactory {
  
  /** class variables ========================================================= */
  
  /** The configuration. */
  private static AsnConfig CONFIG;
    
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnFactory() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the configuration.
   * @return the configuration
   */
  public AsnConfig getConfiguration() {
    AsnConfig config = AsnFactory.CONFIG;
    if (config != null) {
      return config;
    } else {
      configure();
      return AsnFactory.CONFIG;
    }
  }
  
  /** methods ================================================================= */
  
  /**
   * Builds the configuration if necessary. 
   */
  private synchronized void configure() {
    AsnConfig config = AsnFactory.CONFIG;
    if (config == null) {
      config = new AsnConfig();
      config.configure();
      AsnFactory.CONFIG = config;
    } 
  }
  
  /**
   * Makes an operation handler suitable for the active assertion request.
   * @param context the assertion operation context
   * @return the operation handler
   * @throws Exception if an exception occurs
   */
  public AsnOperationHandler makeOperationHandler(AsnContext context) throws Exception { 
        
    // determine the operation, make the operation handler
    AsnConfig config = this.getConfiguration();
    AsnOperation operation = config.getOperations().makeOperation(context);
    context.setOperation(operation);
    AsnOperationHandler opHandler = operation.makeHandler(context);
    if (operation.getIndexReference() != null) {
      if (!operation.getIndexReference().getEnabled()) {
        String msg = "This index is disabled: "+operation.getIndexReference().getName();
        throw new AsnInvalidOperationException(msg);
      }
      opHandler.setIndexAdapter(operation.getIndexReference().makeIndexAdapter(context));
    }
    
    // establish the system part
    if (operation.getSystemPart() == null) {
      operation.setSystemPart(new AsnSystemPart());
    }
    AsnSubject subject = operation.getSubject();
    String subjectPfx = Val.chkStr(subject.getURNPrefix());
    if (subjectPfx.equals(AsnConstants.SUBJECT_PREFIX_RESOURCEID)) {
      operation.getSystemPart().setResourceId(subject.getValuePart());
    }
    
    // establish the user part
    context.getAuthorizer().establishUser(context);
      
    return opHandler;
  }
  
  /**
   * Makes an assertion request handler.
   * @param request the HTTP servlet request
   * @param requestContext the active request context
   * @return the request handler
   */
  public AsnRequestHandler makeRequestHandler(HttpServletRequest request,
                                              RequestContext requestContext) {
    
    // make the operation context
    AsnContext context = new AsnContext();
    context.setAssertionFactory(this);
    context.setRequestContext(requestContext);
    
    // make and return the request handler
    AsnRequestHandler handler = new AsnRequestHandler();
    handler.setAssertionContext(context);
    return handler;
  }
  
  /**
   * Instantiates a new assertion factory.
   * <p/>
   * By default, a new instance of com.esri.gpt.server.assertion.AsnFactory is returned.
   * <p/>
   * This can be overridden by the configuration parameter:
   * /gptConfig/catalog/parameter@key="assertion.AsnFactory"
   * @param requestContext the active request context
   * @return the factory
   */
  public static AsnFactory newFactory(RequestContext requestContext) {
    CatalogConfiguration catCfg = null;
    if (requestContext != null) {
      catCfg = requestContext.getCatalogConfiguration();
    } else {
      catCfg = ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration();
    }
    
    String key = "assertion.AsnFactory";
    String className = Val.chkStr(catCfg.getParameters().getValue(key));
    if (className.length() == 0) {
      return new AsnFactory();
    } else {

      try {
        Class<?> cls = Class.forName(className);
        Object obj = cls.newInstance();
        if (obj instanceof AsnFactory) {
          return (AsnFactory)obj;
        } else {
          String sMsg = "The configured "+key+" parameter is invalid: "+ className;
          throw new ConfigurationException(sMsg);
        }
      } catch (ConfigurationException t) {
        throw t;
      } catch (Throwable t) {
        String sMsg = "Error instantiating assertion factory: " + className;
        throw new ConfigurationException(sMsg, t);
      }
    }
  }

}
