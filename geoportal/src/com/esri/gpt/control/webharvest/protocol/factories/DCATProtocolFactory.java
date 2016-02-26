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
package com.esri.gpt.control.webharvest.protocol.factories;

import com.esri.gpt.catalog.harvest.protocols.HarvestEnvironment;
import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolDCAT;
import com.esri.gpt.control.webharvest.protocol.Protocol;
import com.esri.gpt.control.webharvest.protocol.ProtocolFactoryExt;

/**
 * DCAT protocol factory.
 */
public class DCATProtocolFactory implements ProtocolFactoryExt {

  @Override
  public String getName() {
    return "DCAT";
  }

  @Override
  public Protocol newProtocol() {
    return new HarvestProtocolDCAT();
  }

  @Override
  public Protocol newProtocol(HarvestEnvironment hEnv) {
    return new HarvestProtocolDCAT(hEnv);
  }
}
