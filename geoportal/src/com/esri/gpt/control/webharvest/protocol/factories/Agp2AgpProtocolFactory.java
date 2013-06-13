/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.control.webharvest.protocol.factories;

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolAgp2Agp;
import com.esri.gpt.control.webharvest.protocol.Protocol;
import com.esri.gpt.control.webharvest.protocol.ProtocolFactory;

/**
 * Agp2Agp protocol factory
 */
public class Agp2AgpProtocolFactory implements ProtocolFactory {

  @Override
  public String getName() {
    return HarvestProtocolAgp2Agp.NAME;
  }

  @Override
  public Protocol newProtocol() {
    return new HarvestProtocolAgp2Agp();
  }
}
