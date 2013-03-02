/*See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
Esri Inc. licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/ 
package com.esri.gpt.wms
{

	import com.esri.ags.layers.supportClasses.LayerInfo;
	import com.esri.ags.layers.supportClasses.WMSLayerInfo;
	
	import mx.collections.ArrayCollection;
	import mx.logging.*;
	
	//--------------------------------------
	//  Other metadata
	//--------------------------------------
	
	[Bindable]
	
	/**
	 * Contains information about each layer in a WMS service.
	 */
	public class WMSLayerInfo extends LayerInfo
	{
	 
	    /***************************************************************************
	    * Class variables
	    * **************************************************************************/
	    
	    /** Class logger **/
	    private static const LOG:ILogger = Log.getLogger("WMSLayerInfo");
	    
	    
		//--------------------------------------------------------------------------
		//
		//  Variables
		//
		//--------------------------------------------------------------------------
		/**
		 *  Indicates whether the layer is visible
		 * 
		 * */
		public var visible:Boolean;
		/**
		 * Layer title as defined in the map service. This will actually be the name
		 * in WMS.  The menu shows the name, yet the name in wms is a key parameter.
		 * So will switch the name and title & use the title instead as the 
		 */
		public var title:String;
		
		/**
		 * Spatial reference
		 * 
		 * */
		public var spatialRef:String;	
		/**
		 * If the layer is part of a group layer, it will include the parent group layer.
		 * Otherwise, the value is null.
		 */
		public var parentLayer:com.esri.gpt.wms.WMSLayerInfo;
		   
		[ArrayElementType("com.esri.gpt.wms.WMSLayerInfo")]
		/**
		 * If the layer is a parent layer, it will have one or more sublayers included in an array.
		 * Otherwise, the value is null.
		 */
		public var subLayers:Array;
		
		
		public var queryable:String;
        
        public var arrCrs:ArrayCollection = new ArrayCollection();
        
        public var bBox:ArrayCollection = new ArrayCollection();
        
        public var legendUrl:String;
	
	}

}
