/*
 * Copyright 2015 Esri, Inc..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.cart;

import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Esri, Inc.
 */
public final class TryHandlerBuilder {
  
  private static final ITryHandler DEFAULT = new ITryHandler() {
    @Override
    public TryResponse tryKeys(HttpServletRequest request, HttpServletResponse response, RequestContext context, Cart cart, List<String> keys) {
      TryResponse tryResponse = new TryResponse();
      if (keys!=null) {
        for (String key: keys) {
          tryResponse.add(key, true);
        }
      }
      return tryResponse;
    }
  };
  
  public static ITryHandler newHandlerInstance() {
    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();
    StringAttributeMap parameters = appCfg.getCatalogConfiguration().getParameters();
    String tryHandlerClass = Val.chkStr(parameters.getValue("catalog.cart.tryhandler.class"));
    
    if (tryHandlerClass.isEmpty()) {
      return DEFAULT;
    } else {
      try {
        Class tryHandlerClassInstance = Class.forName(tryHandlerClass);
        Object tryHandlerInstance = tryHandlerClassInstance.newInstance();
        return (ITryHandler)tryHandlerInstance;
      } catch (Exception ex) {
        return DEFAULT;
      }
    }
  }
}
