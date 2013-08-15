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

import java.util.Map;

/**
 * Validator.
 */
public interface IValidator extends IConnectionChecker {
  /**
   * Validates protocol validator is associated with,
   * @param mb message collector
   * @return <code>true</code> if protocol passes validation
   */
  boolean validate(IMessageCollector mb);
  /**
   * Lists all connections checkers.
   * <p>
   * There is always non-empty map of connection checkers with at least one checker
   * named: "this" which refers to validator itself.
   * </p>
   * @return collection of named connection checkers
   */
  Map<String,IConnectionChecker> listConnectionCheckers();
}
