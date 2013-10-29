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

/**
 * Message collector.
 */
public interface IMessageCollector {
  /**
   * Adds error message.
   * @param resourceKey resource key of the message
   */
  void addErrorMessage(String resourceKey);
  /**
   * Adds error message.
   * @param resourceKey resource key of the message
   * @param parameters parameters
   */
  void addErrorMessage(String resourceKey, Object[] parameters);
  /**
   * Adds success message.
   * @param resourceKey resource key of the message
   */
  void addSuccessMessage(String resourceKey);
  /**
   * Adds success message.
   * @param resourceKey resource key of the message
   * @param parameters parameters
   */
  void addSuccessMessage(String resourceKey, Object[] parameters);
}
