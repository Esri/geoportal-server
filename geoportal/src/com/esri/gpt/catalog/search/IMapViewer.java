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
import com.esri.gpt.framework.context.RequestContext;


/**
 * The Interface IMapViewer. Inherit this class to put a new map viewer.
 * Class will be manipulated by MapViewerFactory.
 */
public interface IMapViewer {


// properties ==================================================================
/**
 * Sets the map viewer configs. Will be set by factory.
 * 
 * @param mapViewerConfigs the new map viewer configs
 */
public void setMapViewerConfigs(MapViewerConfigs mapViewerConfigs);

/**
 * Sets the resource uri. Will be set by factory.
 * 
 * @param resourceUri the resource uri
 * @param hintServiceType the hint service type (could be null, could be
 * one of ResourceLinkbuilder.)
 */
public void setResourceUri(String resourceUri, String hintServiceType);


/**
 * Sets the search result record.  Will be set by factory.
 * 
 * @param searchResultRecord the new search result record
 */
public void setSearchResultRecord(IFeedRecord searchResultRecord);

/**
 * Sets the request context.
 * 
 * @param context the new request context
 */
public void setRequestContext(RequestContext context);


// methods =====================================================================


/**
 * Can handle resource.  Parses the resoureuri or uses the hintServiceType to
 * wee if url can be supported
 * 
 * @return the boolean
 */
public boolean canHandleResource();

/**
 * Read add to map url.  Generates the add to map url.
 * 
 * @return the string (could be null or empty)
 */
public String readAddToMapUrl();


/**
 * Reads the link target for html e.g. _blank.
 * 
 * @return the string
 */
public String readTarget();

/**
 * Returns string that opens default mapviewer (possibly null).  Currently GUI
 * assumes this will be jscript and puts the value in onclick.
 * 
 * @return the string
 */
public String readOpenDefaultMapViewerUrl();
}
