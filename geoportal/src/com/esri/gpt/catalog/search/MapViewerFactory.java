/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.catalog.search;

import com.esri.gpt.control.georss.IFeedRecord;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esri.gpt.framework.context.RequestContext;


/**
 * Instantiates mapviewer instances and searches for an instance that
 * supports the url/servicetype.
 */
public class MapViewerFactory {

// class variables =============================================================
/** Class logger **/
private final static Logger LOG = 
  Logger.getLogger(MapViewerFactory.class.getCanonicalName());

// methods =====================================================================
/**
 * Creates a new MapViewer object.
 * 
 * @param resourceUrl the resource url
 * @param hintResourceType the hint
 * @param searchResultRecord the search result record
 * @param requestContext the request context
 * @return the mapviewer instance
 */
public static IMapViewer createMapViewer(String resourceUrl, 
    String hintResourceType, IFeedRecord searchResultRecord, 
    RequestContext requestContext) {
  if(resourceUrl == null || "".equals(resourceUrl.trim())) {
    return null;
  }
  resourceUrl = resourceUrl.trim();
  ArrayList<MapViewerConfigs> arrMapViewerInstances = 
    SearchConfig.getConfiguredInstance().getMapViewerInstances();
  if( arrMapViewerInstances == null) {
    return null;
  }
  IMapViewer iSelectedMapViewer = null;
  for(MapViewerConfigs configs : arrMapViewerInstances) {
    if(configs == null) { 
      continue;
    }
    try {
      Object obj = Class.forName(configs.getClassName()).newInstance();
      if(obj instanceof IMapViewer) {
        IMapViewer iMapViewer = (IMapViewer) obj;
        iMapViewer.setMapViewerConfigs(configs);
        iMapViewer.setResourceUri(resourceUrl, hintResourceType);
        iMapViewer.setSearchResultRecord(searchResultRecord);
        iMapViewer.setRequestContext(requestContext);
        if(!iMapViewer.canHandleResource()) {
          continue;
        }
        String resourceUri = iMapViewer.readAddToMapUrl();
        if(resourceUri != null || !"".equals(resourceUri)) {
          iSelectedMapViewer = iMapViewer;
          LOG.finer("Chosen mapviewer for resourceUrl =" + resourceUrl +" = " + 
              iMapViewer.getClass().getCanonicalName());
          break;
        }
      } else if(obj != null){
        LOG.warning("Object " + obj.getClass().getCanonicalName() + " does not" +
        		"inherit from " + IMapViewer.class.getCanonicalName());
      }
    } catch (ClassNotFoundException e) {
      LOG.log(Level.WARNING, "Configured class for MapViewer Factory " +
      		"not found " + configs.getClassName() , e);
    } catch (InstantiationException e) {
      LOG.log(Level.WARNING, "Configured class for MapViewer Factory " +
          "not cannot be instantiated " + configs.getClassName(), e);
    } catch (IllegalAccessException e) {
      LOG.log(Level.WARNING, "Configured class for MapViewer Factory " +
          "not cannot be instantiated " + configs.getClassName(), e);
    }
    
  }
  
  return iSelectedMapViewer;
  
}

/**
 * Creates a new MapViewer object.
 * 
 * @return the map viewer instance.  Could be null;
 */
public static IMapViewer createDefaultMapViewer() {
  IMapViewer iMapViewer = null;
  ArrayList<MapViewerConfigs> arrMapViewerInstances = 
    SearchConfig.getConfiguredInstance().getMapViewerInstances();
  if ((arrMapViewerInstances == null) || (arrMapViewerInstances.size() < 1)) {
    return null;
  }
  MapViewerConfigs configs = 
    (MapViewerConfigs) arrMapViewerInstances.get(0);
  try {
    Object obj = Class.forName(configs.getClassName()).newInstance();
    if(obj instanceof IMapViewer) {
      iMapViewer =  (IMapViewer) obj;
      
    }
    iMapViewer.setMapViewerConfigs(configs);
  } catch (InstantiationException e) {
    LOG.log(Level.WARNING, "error while creating map viewer instance", e);
  } catch (IllegalAccessException e) {
    LOG.log(Level.WARNING, "error while creating map viewer instance", e);
  } catch (ClassNotFoundException e) {
    LOG.log(Level.WARNING, "error while creating map viewer instance", e);
  }
  
  return iMapViewer;
}

}
