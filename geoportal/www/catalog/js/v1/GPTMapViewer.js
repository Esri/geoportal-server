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
/*
 * Object that manages the map viewer
 */
function GptMapViewer(mapViewerUrl) {
	
	/** Instantiating mapve viewer url **/
	var mapViewerUrl = GptUtils.valChkStr(mapViewerUrl);
	
	/** Registering functions */
	this.addToMap = addToMap;
	this.openDefaultMap = openDefaultMap;
	
	/*
	 *Adds a live map service to the MVS map
	 *@param arg will be an object with arg.server, arg.serviceType, arg.service,
	 *       arg.uuid .  Arguments can therefore be passed by named parameters 
	 *       (use JSON for ease). if arg.fullUrl passed then this
	 * will be opened straight
	 */
	function addToMap(arg) {
		
		var fullUrl = GptUtils.valChkStr(arg.fullUrl, "");
		if(fullUrl != "") {
			var window = GptUtils.popUp(fullUrl, 
                   GptMapViewer.TITLE, 
                   GptMapViewer.dimensions.WIDTH, 
                   GptMapViewer.dimensions.HEIGHT);
      return;
		}
		var sServer = GptUtils.valChkStr(arg.server, "");
		var sServiceType = GptUtils.valChkStr(arg.serviceType, "");
		var sService = GptUtils.valChkStr(arg.service, "");
		var uuid = GptUtils.valChkStr(arg.uuid, "");
		
		
		if(sServer == "") {
			GptUtils.logl(GptUtils.log.Level.WARN,
			  "No server found. Cannot load to map");
			  return;
		}
	
		if(sServiceType == "") {
			GptUtils.logl(GptUtils.log.Level.WARN,
        "No service type found. Cannot load to map");
        return;
		}
		
		var sMvsServiceType = sServiceType;
    if(sServiceType == GptMapViewer.serviceType.AIMS_FEAT
     || sServiceType == GptMapViewer.serviceType.AIMS_IMAGE ) {
      sMvsServiceType = GptMapViewer.serviceType.AIMS;
    }
    if(sMvsServiceType == GptMapViewer.serviceType.AIMS) {
    	if( sServer.match("/$") == null) {
    		sServer = sServer + "/";
    	}
    	sServer = sServer + sService;
    	
    }
    var mvs_serviceinfo = "resources=map:" + sMvsServiceType + "@" + sServer;
    var mvsUrl = mapViewerUrl + "?" + mvs_serviceinfo;
    GptUtils.popUp(mvsUrl, 
                   GptMapViewer.TITLE, 
                   GptMapViewer.dimensions.WIDTH, 
                   GptMapViewer.dimensions.HEIGHT);
  }

	
	/**
	 * Opens the default map
	 * @return Window pointer
	 */
	function openDefaultMap() {
		
		var mapWin = GptUtils.popUp(mapViewerUrl, 
                   GptMapViewer.TITLE, 
                   GptMapViewer.dimensions.WIDTH, 
                   GptMapViewer.dimensions.HEIGHT);
    
		return mapWin;
	}
	

	
	function testViewMap(index) {
	  
	  var url = mapViewerUrl;
	  var sService;
	  var sType;
	  var nExamples = 5;
	  if((index % nExamples) == 0) {
	  	sService = "http://gisdata.usgs.gov:80/wmsconnector/com.esri.wms.Esrimap/USGS_EDC_Ortho_Landsat7?";
	  	sType = GptMapViewer.serviceType.WMS;
	  	GptUtils.logl(GptUtils.log.Level.INFO, 
	  	     "adding service type = " + sType + ", service = "  + sService);
	  	
	  } else if((index % nExamples) == 1) {
      sService = "http://gpt4:8399/arcgis/services/US_Base_Map/MapServer/WFSServer?service=WFS";
      sType = GptMapViewer.serviceType.WFS;
      GptUtils.logl(GptUtils.log.Level.INFO, 
           "adding service type = " + sType + ", service = "  + sService);
      
    } else if((index % nExamples) == 2) {
      sService = "http://nsidc.org/cgi-bin/atlas_north?service=WCS&version=1.0.0";
      sType = GptMapViewer.serviceType.WCS; 
      GptUtils.logl(GptUtils.log.Level.INFO, 
           "adding service type = " + sType + ", service = "  + sService);
    } else if((index % nExamples) == 3) {
      sService = "http://www.geographynetwork.ca/ORN_FULL";
      sType = GptMapViewer.serviceType.AIMS;
      GptUtils.logl(GptUtils.log.Level.INFO, 
           "adding service type = " + sType + ", service = "  + sService);
    } else if((index % nExamples) == 4) {
      sService = "http://gpt4:8399/arcgis/services/US_Base_Map/MapServer";
      sType = GptMapViewer.serviceType.AGS;
      GptUtils.logl(GptUtils.log.Level.INFO, 
           "adding service type = " + sType + ", service = "  + sService);
    }
    
    addToMap(sService, sType);
		return false;
	}
	
		
}

/** Constants for service type. do not change values. 
 Dependancies on values exist. */
GptMapViewer.serviceType = new Object();
GptMapViewer.serviceType.AIMS         = "aims";
GptMapViewer.serviceType.AIMS_IMAGE   = "image";
GptMapViewer.serviceType.AIMS_FEAT    = "feature";
GptMapViewer.serviceType.WCS          = "wcs";
GptMapViewer.serviceType.AGS          = "ags";
GptMapViewer.serviceType.WFS          = "wfs";
GptMapViewer.serviceType.WMS          = "wms";

/** Dimension of mapviewer window */
GptMapViewer.dimensions = new Object();
GptMapViewer.dimensions.WIDTH= 1000;
GptMapViewer.dimensions.HEIGHT = 600;
/** Title for window of map viewer */
GptMapViewer.TITLE = "GPT_Map_Viewer";
