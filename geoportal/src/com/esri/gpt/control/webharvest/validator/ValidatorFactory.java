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

import com.esri.gpt.catalog.harvest.repository.HrRecord;
import java.util.HashMap;

/**
 * Validator factory.
 */
public class ValidatorFactory {
  private static final HashMap<Class,IValidatorFactory> factories = new HashMap<Class, IValidatorFactory>();
  private static ValidatorFactory instance = new ValidatorFactory();
  
  static {
    register(new ArcImsValidatorFactory());
    register(new OAIValidatorFactory());
    register(new ArcGISValidatorFactory());
    register(new Agp2AgpValidatorFactory());
    register(new Ags2AgpValidatorFactory());
  }
  
  /**
   * Gets singleton instance.
   */
  public static ValidatorFactory getInstance() {
    return instance;
  }
  
  /**
   * Registers validator factory.
   * @param factory validator factory
   */
  public static void register(IValidatorFactory factory) {
    factories.put(factory.getProtocolClass(), factory);
  }
  
  /**
   * Gets validator.
   * @param record repository record
   * @return validator or <code>null</code> if validator not available
   */
  public IValidator getValidator(HrRecord record) {
    IValidatorFactory validatorFactory = factories.get(record.getProtocol().getClass());
    return validatorFactory!=null? validatorFactory.create(record): null;
  }
}
