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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolWaf;
import com.esri.gpt.catalog.harvest.repository.HrRecord;

/**
 * WAF protocol validator factory.
 */
class WAFValidatorFactory implements IValidatorFactory {

  @Override
  public Class getProtocolClass() {
    return HarvestProtocolWaf.class;
  }

  @Override
  public IValidator create(HrRecord record) {
    if (record!=null && getProtocolClass().isInstance(record.getProtocol())) {
      String url = record.getHostUrl();
      HarvestProtocolWaf protocol = (HarvestProtocolWaf) record.getProtocol();
      return new WAFValidator(url, protocol);
    }
    return null;
  }
}
