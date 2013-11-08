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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolAgs2Agp;
import com.esri.gpt.catalog.harvest.repository.HrRecord;

/**
 * AGS to AGP protocol validator factory.
 */
class Ags2AgpValidatorFactory implements IValidatorFactory {

  @Override
  public Class getProtocolClass() {
    return HarvestProtocolAgs2Agp.class;
  }

  @Override
  public IValidator create(HrRecord record) {
    if (record!=null && getProtocolClass().isInstance(record.getProtocol())) {
      String url = record.getHostUrl();
      HarvestProtocolAgs2Agp protocol = (HarvestProtocolAgs2Agp) record.getProtocol();
      return new Ags2AgpValidator(url, protocol);
    }
    return null;
  }
}
