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
package com.esri.gpt.agp.ags;

import com.esri.gpt.agp.client.AgpConnection;
import com.esri.gpt.agp.sync.AgpDestination;
import com.esri.gpt.catalog.arcgis.agportal.publication.PublicationRequest;
import com.esri.gpt.catalog.harvest.protocols.HostContextPair;
import com.esri.gpt.control.webharvest.client.arcgis.ArcGISInfo;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.scheduler.IScheduledTask;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copies from ArcGIS Server to Portal for ArcGIS (scheduled task).
 */
public class Ags2AgpCopyTask implements Runnable, IScheduledTask {
  /** class variables ========================================================= */
  private static final Logger LOGGER = Logger.getLogger(Ags2AgpCopyTask.class.getName());
  private StringAttributeMap parameters;

  @Override
  public void setParameters(StringAttributeMap parameters) {
    this.parameters = parameters;
  }

  @Override
  public void run() {
    try {
      ArcGISInfo source = getSource();
      AgpDestination destination = getDestination();

      Ags2AgpCopy copy = new Ags2AgpCopy(source, destination);
      
      copy.copy();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error in Ags2AgpCopyTask.", e);
    } finally {
      LOGGER.info("Ags2AgpCopyTask completed.");
    }
  }
  
  private ArcGISInfo getSource() {
    return new ArcGISInfo(
            parameters.getValue("ags.restUrl"),
            parameters.getValue("ags.soapUrl"),
            parameters.getValue("ags.userName"),
            parameters.getValue("ags.userPassword"));
    
  }
  
  private AgpDestination getDestination() {
    AgpConnection connection = new AgpConnection();
    
    AgpDestination destination = new AgpDestination();
    destination.setConnection(connection);
    
    HostContextPair hcp = HostContextPair.makeHostContextPair(parameters.getValue("agp.host"));
    connection.setHost(hcp.getHost());
    connection.setWebContext(hcp.getContext());
    destination.setDestinationOwner(parameters.getValue("agp.owner"));
    destination.setDestinationOwner(parameters.getValue("agp.folder"));
    
    return destination;
  }
  
}
