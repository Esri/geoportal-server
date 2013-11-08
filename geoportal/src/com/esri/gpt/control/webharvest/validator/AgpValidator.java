/*
 * Copyright 2013 Esri.
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
package com.esri.gpt.control.webharvest.validator;

import com.esri.gpt.agp.sync.AgpDestination;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.util.Val;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic AGP protocol validator.
 */
abstract class AgpValidator implements IValidator {
  protected static final String HOST_NAME_REGEX = "(^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])([/].+)?$)|(^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])([/].+)?$)";
  protected boolean arcgisDotComAllowed;
  protected boolean crossAllowed;
  private Map<String,IConnectionChecker> checkers = new HashMap<String,IConnectionChecker>();
  
  {
    checkers.put("this",this);
    checkers.put("destination", new IConnectionChecker() {
      @Override
      public boolean checkConnection(IMessageCollector mb) {
        return checkDestinationConnection(mb);
      }
    });
  }

  public AgpValidator() {

    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();

    String sArcgisDotComAllowed = appCfg.getCatalogConfiguration().getParameters().getValue("webharvester.agp2agp.arcgisDotCom.allowed");
    this.arcgisDotComAllowed = Val.chkBool(sArcgisDotComAllowed, false);


    String sCrossAllowed = appCfg.getCatalogConfiguration().getParameters().getValue("webharvester.agp2agp.sameDomain.allowed");
    this.crossAllowed = Val.chkBool(sCrossAllowed, false);
  }

  @Override
  public Map<String, IConnectionChecker> listConnectionCheckers() {
    return checkers;
  }
  
  /**
   * Checks connection to the destination.
   * @param mb message collector
   * @return <code>true</code> if connection could be verified
   */
  public abstract boolean checkDestinationConnection(IMessageCollector mb);

  protected boolean getArcgisDotComAllowed() {
    return arcgisDotComAllowed;
  }

  protected boolean getCrossAllowed() {
    return crossAllowed;
  }

  protected void setArcgisDotComAllowed(boolean arcgisDotComAllowed) {
    this.arcgisDotComAllowed = arcgisDotComAllowed;
  }

  protected void setCrossAllowed(boolean crossAllowed) {
    this.crossAllowed = crossAllowed;
  }
  
  /**
   * Checks connection to the destination.
   * @param mb message collector
   * @param destination destination
   * @return <code>true</code> if connection could be verified
   */
  protected boolean checkConnection(IMessageCollector mb, AgpDestination destination) {
    try {
        boolean stop = false;
        if (destination.getConnection().getHost().isEmpty()) {
          mb.addErrorMessage("catalog.harvest.manage.test.err.agp2agp.dst.nohost");
          stop = true;
        }

        if (destination.getConnection().getTokenCriteria().getCredentials().getUsername().isEmpty() || destination.getConnection().getTokenCriteria().getCredentials().getPassword().isEmpty()) {
          mb.addErrorMessage("catalog.harvest.manage.test.err.agp2agp.dst.nocredentials");
          stop = true;
        }

        if (!stop) {
          destination.getConnection().generateToken();
          return true;
        }
    } catch (Exception ex) {
      String message = Val.chkStr(ex.getMessage());
      if (message.isEmpty()) {
        mb.addErrorMessage("catalog.harvest.manage.test.err.HarvestConnectionException");
      } else {
        mb.addErrorMessage("catalog.harvest.manage.test.err.agp2agp.connect", new Object[]{message});
      }
    }
    return false;
  }
  
}
