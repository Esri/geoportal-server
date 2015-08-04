/*
 * Copyright 2015 pete5162.
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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolGpt2Agp;
import com.esri.gpt.control.webharvest.protocol.Protocol;
import com.esri.gpt.control.webharvest.protocol.ProtocolFactory;

/**
 *
 * @author pete5162
 */
public class Gpt2AgpProtocolFactory implements ProtocolFactory {

  @Override
  public String getName() {
    return HarvestProtocolGpt2Agp.NAME;
  }

  @Override
  public Protocol newProtocol() {
    return new HarvestProtocolGpt2Agp();
  }
}
