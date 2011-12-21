/*
 * Copyright 2011 Esri.
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
package com.esri.gpt.control.webharvest.engine;

import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import org.w3c.dom.Node;

/**
 * Local data processor factory.
 */
public class LocalDataProcessorFactory implements DataProcessorFactory {
  private String name = "Local";
  
  @Override
  public String getName() {
    return name;
  }
  
  @Override
  public void setName(String name) {
    this.name = Val.chkStr(name);
  }

  @Override
  public void init(Node ndConfig) {
    // do nothing
  }

  @Override
  public DataProcessor newProcessor(MessageBroker messageBroker, String baseContextPath, Harvester.Listener listener) {
    return new LocalDataProcessor(getName(), messageBroker, baseContextPath, listener);
  }
  
}
