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

import com.esri.gpt.agp.client.AgpItem;
import com.esri.gpt.agp.sync.AgpDestination;
import com.esri.gpt.agp.sync.AgpItemHelper;
import com.esri.gpt.agp.sync.AgpPartHelper;
import com.esri.gpt.catalog.arcgis.metadata.IServiceInfoProvider;
import com.esri.gpt.catalog.arcgis.metadata.ServiceInfo;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.client.arcgis.ArcGISInfo;
import com.esri.gpt.control.webharvest.client.arcgis.ArcGISQueryBuilder;
import com.esri.gpt.control.webharvest.common.CommonCriteria;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.Result;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copies from ArcGIS Server to Portal for ArcGIS.
 */
public class Ags2AgpCopy {
  private AgpItemHelper  itemHelper = new AgpItemHelper(); 
  private AgpPartHelper  partHelper = new AgpPartHelper(); 
  private static final Logger LOGGER = Logger.getLogger(Ags2AgpCopy.class.getCanonicalName());
  
  private ArcGISInfo source;
  private AgpDestination destination;
  private String numItemsConsidered;
  private int numWithNullId;
  private int numWithNullType;
  private int numUnsyncedExistingAtDestination;
  private int numOriginatedFromSynchronization;
  
  /**
   * Creates instance of the class.
   * @param source source
   * @param destination destination
   */
  public Ags2AgpCopy(ArcGISInfo source, AgpDestination destination) {
    this.source = source;
    this.destination = destination;
  }
  
  /**
   * Executes copy action.
   * @throws Exception if anything fails
   */
  public void copy() throws Exception {
    RequestContext requestContext = RequestContext.extract(null);
    try {

      ArcGISQueryBuilder qb = new ArcGISQueryBuilder(new IterationContext() {
        @Override
        public void onIterationException(Exception ex) {
          LOGGER.log(Level.SEVERE, "Error iterating through AGS resources.", ex);
        }
      }, source);
      
      Query newQuery = qb.newQuery(new CommonCriteria());
      Result result = newQuery.execute();

      this.destination.getConnection().generateToken();
      
      for (Resource r: result.getResources()) {
        if (r instanceof IServiceInfoProvider) {
          IServiceInfoProvider siProvider = (IServiceInfoProvider)r;
          ServiceInfo serviceInfo = siProvider.getServiceInfo();
          LOGGER.log(Level.FINE, serviceInfo.toString());
        }
      }

    } finally {
      requestContext.onExecutionPhaseCompleted();
    }
  }
  
  private AgpItem createAgpItem(ServiceInfo serviceInfo) {
    AgpItem agpItem = new AgpItem();
    // TODO provide implementation
    return agpItem;
  }
  
  private boolean syncItem(AgpItem sourceItem) throws Exception {
    AgpDestination dest = this.destination; 
    String sId = sourceItem.getProperties().getValue("id");
    String sType = sourceItem.getProperties().getValue("type");
    String sTitle = sourceItem.getProperties().getValue("title");
    String sMsg = "Processing item ("+this.numItemsConsidered+")";
    sMsg += ", id:"+sId+", type:"+sType+", title:"+sTitle;
    LOGGER.info(sMsg);

    // check the id and type
    if (sId == null) {
      this.numWithNullId++;
      LOGGER.finer("Ignoring item with null id: "+sTitle);
      return false;
    } else if (sType == null) {
      this.numWithNullType++;
      LOGGER.finer("Ignoring item with null type: "+sId+" "+sTitle);
      return false;
    } else if (sType.equalsIgnoreCase("Code Attachment")) {
      // don't publish Code Attachments now, publish within processRelatedItems
      return false;
    }
    
    boolean bUnsyncedItemExists = this.itemHelper.doesUnsyncedItemExist(sourceItem,dest);
    if (bUnsyncedItemExists) {
      this.numUnsyncedExistingAtDestination++;
      String s = "Ignoring unsynced item existing at destination: ";
      LOGGER.finer(s+sId+" "+sTitle);
      return false;
    }
    
    // don't propagate synced items from portal to portal
    boolean bIsSyncedItem = this.itemHelper.isSyncedItem(sourceItem);
    if (bIsSyncedItem) {
      this.numOriginatedFromSynchronization++;
      String s = "Ignoring, an item that originated from synchronization will not be repropagated: ";
      LOGGER.finer(s+sId+" "+sTitle);
      return false;
    }

       
    // determine if the item requires an update
    // process web maps at the end of the job
    // TODO: there will be problems if the item is no longer visible to this user
    
//    AgpItem destItem = this.itemHelper.makeDestinationItem(src,sourceItem);
//    boolean bRequiresUpdate = this.itemHelper.requiresUpdate(sourceItem,dest,destItem);
//    if (this.forceUpdates || bRequiresUpdate) {
//      if (sType.equalsIgnoreCase("Web Map")) {
//        this.webMaps.add(sId);
//      } else {
//        this.execPublishItem(sourceItem,destItem);
//        this.processMetadata(sourceItem,destItem);
//        this.processRelatedItems(sourceItem,destItem);
//      }
//      return true;
//    }
    
    return true;
  }
}
