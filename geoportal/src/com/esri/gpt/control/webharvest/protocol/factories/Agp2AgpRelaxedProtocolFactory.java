/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.control.webharvest.protocol.factories;

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolAgp2Agp;
import com.esri.gpt.control.webharvest.protocol.Protocol;

/**
 *
 * @author Esri
 */
public class Agp2AgpRelaxedProtocolFactory extends Agp2AgpProtocolFactory {

  @Override
  public Protocol newProtocol() {
    return new HarvestProtocolAgp2Agp(false);
  }
  
}
