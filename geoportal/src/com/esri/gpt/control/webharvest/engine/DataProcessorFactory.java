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
import org.w3c.dom.Node;

/**
 * Data processor factory.
 */
public interface DataProcessorFactory {
  /**
   * Gets name of the data processor to be created.
   * @return name
   */
  String getName();
  /**
   * Sets name of the data processor to be created.
   * @param name name
   */
  void setName(String name);
  /**
   * Initializes factory.
   * @param ndConfig DOM node with data processor factory configuration.
   */
  void init(Node ndConfig);
  /**
   * Creates new processor.
   * @param messageBroker message broker
   * @param baseContextPath base context path
   * @param listener listener
   * @return data processor instance
   */
  DataProcessor newProcessor(MessageBroker messageBroker, String baseContextPath, Harvester.Listener listener);
}
