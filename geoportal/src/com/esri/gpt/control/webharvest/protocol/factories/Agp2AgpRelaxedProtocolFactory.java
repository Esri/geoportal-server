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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolAgp2Agp;
import com.esri.gpt.control.webharvest.protocol.Protocol;

/**
 * Relaxed Agp2Agp protocol factory.
 * Relaxed protocol factory creates protocol handler which allow to continue 
 * harvesting between two AGP servers even an exception occurred on a single
 * item.
 * <p/>
 * Regular protocol factory creates handler which stops harvesting after a single
 * error.
 */
public class Agp2AgpRelaxedProtocolFactory extends Agp2AgpProtocolFactory {

  @Override
  public Protocol newProtocol() {
    return new HarvestProtocolAgp2Agp(false);
  }
  
}
