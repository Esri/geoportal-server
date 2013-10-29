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

import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.StringHandler;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic validator.
 */
abstract class GenericValidator implements IValidator {
  protected String url;
  private Map<String,IConnectionChecker> checkers = new HashMap<String,IConnectionChecker>();
  
  {
    checkers.put("this", this);
  }

  /**
   * Creates instance of the validator.
   * @param url service URL
   */
  public GenericValidator(String url) {
    this.url = url;
  }

  @Override
  public boolean checkConnection(IMessageCollector mb) {
    try {
      HttpClientRequest httpRequest = new HttpClientRequest();
      httpRequest.setUrl(url);
      httpRequest.setCredentialProvider(getCredentialProvider());
      httpRequest.setContentHandler(new StringHandler());
      httpRequest.execute();
      return true;
    } catch (IOException ex) {
      mb.addErrorMessage("catalog.harvest.manage.test.err.HarvestConnectionException");
    }
    return false;
  }

  @Override
  public Map<String, IConnectionChecker> listConnectionCheckers() {
    return checkers;
  }
  
  protected CredentialProvider getCredentialProvider() {
    return null;
  }
  
}
