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
	import com.esri.ags.SpatialReference;
	import com.esri.ags.geometry.Extent;

	public class WMSExtent extends Extent
	{
		public var _rawSpatial:String;
		
			
		public function WMSExtent(xmin:Number=0, ymin:Number=0, xmax:Number=0, 
		  ymax:Number=0, spatialReference:SpatialReference=null)
		{
			super(xmin, ymin, xmax, ymax, spatialReference);
		}
		
		public function set rawSpatial(rawSpatial:String):void {
			this._rawSpatial = rawSpatial;
			if(rawSpatial != null && rawSpatial.toLowerCase().indexOf("epsg") >= 0) {
				var tmpS:String = rawSpatial.replace(/[^\d]*/g, "");
                var x:Number = parseInt(tmpS);
                if(!isNaN(x)) {
                	if(this.spatialReference == null) {
                		this.spatialReference = new SpatialReference();
                	}
                   this.spatialReference.wkid = x;
                }
            } else if(rawSpatial != null && rawSpatial.toLowerCase().indexOf("crs") >= 0) {
            	var tmpS1:String = rawSpatial.replace(/[^\d]*/g, "");
                var y:Number = parseInt(tmpS1);
                if(!isNaN(y) && y == 84) {
                	if(this.spatialReference == null) {
                        this.spatialReference = new SpatialReference();
                    }
                    this.spatialReference.wkid = 4326;
                }
            } 
		}
		
		public function get rawSpatial():String {
			return this._rawSpatial;
		}
				
	}
}