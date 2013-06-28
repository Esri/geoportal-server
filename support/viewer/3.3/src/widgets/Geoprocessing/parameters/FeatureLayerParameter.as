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

import com.esri.ags.FeatureSet;
import com.esri.ags.SpatialReference;
import com.esri.ags.layers.FeatureLayer;
import com.esri.ags.layers.supportClasses.FeatureCollection;
import com.esri.ags.layers.supportClasses.LayerDetails;
import com.esri.ags.portal.PopUpRenderer;
import com.esri.ags.portal.supportClasses.PopUpInfo;
import com.esri.ags.renderers.ClassBreaksRenderer;
import com.esri.ags.renderers.IRenderer;
import com.esri.ags.renderers.SimpleRenderer;
import com.esri.ags.renderers.UniqueValueRenderer;
import com.esri.ags.symbols.Symbol;

import mx.core.ClassFactory;

public class FeatureLayerParameter extends BaseParameter implements IGPFeatureParameter
{
    //--------------------------------------------------------------------------
    //
    //  Constants
    //
    //--------------------------------------------------------------------------

    public static const DRAW_SOURCE:String = "drawtool";
    public static const LAYERS_SOURCE:String = "layers";
    public static const MAP_EXTENT_SOURCE:String = "extent";

    public static const POINT:String = "point";
    public static const POLYGON:String = "polygon";
    public static const POLYLINE:String = "polyline";

    public static const SIMPLE_MARKER:String = "simplemarker";
    public static const SIMPLE_FILL:String = "simplefill";
    public static const SIMPLE_LINE:String = "simpleline";
    public static const PICTURE_MARKER:String = "picturemarker";

    public static const SIMPLE_RENDERER:String = "simple";
    public static const CLASS_BREAKS_RENDERER:String = "classbreaks";
    public static const UNIQUE_VALUE_RENDERER:String = "uniquevalue";

    //--------------------------------------------------------------------------
    //
    //  Constructor
    //
    //--------------------------------------------------------------------------

    public function FeatureLayerParameter()
    {
        _layer = new FeatureLayer();
        _layer.featureCollection = new FeatureCollection(new FeatureSet([]), new LayerDetails());
        _layer.outFields = [ "*" ];
    }

    //--------------------------------------------------------------------------
    //
    //  Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  geometryType
    //----------------------------------

    private var _geometryType:String;

    public function get geometryType():String
    {
        return _geometryType;
    }

    public function set geometryType(value:String):void
    {
        _geometryType = value;
    }

    //----------------------------------
    //  mode
    //----------------------------------

    private var _mode:String;

    public function get mode():String
    {
        return _mode;
    }

    public function set mode(value:String):void
    {
        _mode = value;
    }

    //----------------------------------
    //  layerNames
    //----------------------------------

    private var _layerNames:Array;

    public function get layerNames():Array
    {
        return _layerNames;
    }

    public function set layerNames(value:Array):void
    {
        _layerNames = value;
    }

    //----------------------------------
    //  popUpInfo
    //----------------------------------

    private var _popUpInfo:PopUpInfo;

    public function get popUpInfo():PopUpInfo
    {
        return _popUpInfo;
    }

    public function set popUpInfo(value:PopUpInfo):void
    {
        _popUpInfo = value;
    }

    //----------------------------------
    //  layer
    //----------------------------------

    private var _layer:FeatureLayer;

    public function get layer():FeatureLayer
    {
        return _layer;
    }

    //----------------------------------
    //  renderer
    //----------------------------------

    private var _renderer:IRenderer;

    public function get renderer():IRenderer
    {
        return _renderer;
    }

    public function set renderer(value:IRenderer):void
    {
        _renderer = value;
        _layer.renderer = value;
    }

    //----------------------------------
    //  layerName
    //----------------------------------

    private var _layerName:String;

    public function get layerName():String
    {
        return _layerName;
    }

    public function set layerName(value:String):void
    {
        _layerName = value;
        _layer.name = value;
    }

    //----------------------------------
    //  popUpRenderer
    //----------------------------------

    public function get popUpRenderer():ClassFactory
    {
        var popUpRenderer:ClassFactory;

        if (_popUpInfo)
        {
            popUpRenderer = new ClassFactory(PopUpRenderer);
            var popUpInfo:PopUpInfo = _popUpInfo;
            popUpRenderer.properties = { popUpInfo: popUpInfo };
        }

        return popUpRenderer;
    }

    //----------------------------------
    //  spatialReference
    //----------------------------------

    private var _spatialReference:SpatialReference;

    public function get spatialReference():SpatialReference
    {
        return _spatialReference;
    }

    public function set spatialReference(value:SpatialReference):void
    {
        _spatialReference = value;
    }

    //----------------------------------
    //  defaultSymbol
    //----------------------------------

    public function get defaultSymbol():Symbol
    {
        var symbol:Symbol;

        if (_renderer is SimpleRenderer)
        {
            symbol = (_renderer as SimpleRenderer).symbol;
        }
        else if (_renderer is ClassBreaksRenderer)
        {
            symbol = (_renderer as ClassBreaksRenderer).defaultSymbol;
        }
        else if (_renderer is UniqueValueRenderer)
        {
            symbol = (_renderer as UniqueValueRenderer).defaultSymbol;
        }

        return symbol;
    }

    //--------------------------------------------------------------------------
    //
    //  Overridden properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  type
    //----------------------------------

    override public function get type():String
    {
        return GPParameterTypes.FEATURE_RECORD_SET_LAYER;
    }

    //----------------------------------
    //  name
    //----------------------------------

    override public function set name(value:String):void
    {
        super.name = value;
        _layer.id = value;
    }

    //--------------------------------------------------------------------------
    //
    //  Overridden methods
    //
    //--------------------------------------------------------------------------

    override public function set paramInfo(value:Object):void
    {
        if (value)
        {
            super.paramInfo = value;
            if (value.defaultValue)
            {
                var featureSet:FeatureSet = FeatureSet.fromJSON(value.defaultValue);
                layer.featureCollection.layerDefinition.fields = featureSet.fields;
            }
        }
    }

    override public function hasValidValue():Boolean
    {
        return _layer.featureCollection.featureSet.features.length > 0;
    }

    public override function getRequestObjectValue():Object
    {
        return _layer.featureCollection.featureSet;
    }
}

}
