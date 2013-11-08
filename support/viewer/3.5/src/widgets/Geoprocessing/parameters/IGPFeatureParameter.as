///////////////////////////////////////////////////////////////////////////
// Copyright (c) 2011 Esri. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
///////////////////////////////////////////////////////////////////////////
package widgets.Geoprocessing.parameters
{

import com.esri.ags.SpatialReference;
import com.esri.ags.layers.FeatureLayer;
import com.esri.ags.portal.supportClasses.PopUpInfo;
import com.esri.ags.renderers.IRenderer;
import com.esri.ags.symbols.Symbol;

import mx.core.ClassFactory;

public interface IGPFeatureParameter extends IGPParameter
{
    function get geometryType():String;
    function get mode():String;
    function get layerNames():Array;
    function get popUpInfo():PopUpInfo;
    function get layer():FeatureLayer;
    function get renderer():IRenderer;
    function get defaultSymbol():Symbol;
    function get layerName():String;
    function get popUpRenderer():ClassFactory;
    function get spatialReference():SpatialReference;

    function set geometryType(value:String):void;
    function set mode(value:String):void;
    function set layerNames(value:Array):void;
    function set popUpInfo(value:PopUpInfo):void;
    function set renderer(value:IRenderer):void;
    function set layerName(value:String):void;
    function set spatialReference(value:SpatialReference):void;
}

}
