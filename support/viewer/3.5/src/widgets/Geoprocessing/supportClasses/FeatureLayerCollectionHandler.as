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
package widgets.Geoprocessing.supportClasses
{

import com.esri.ags.FeatureSet;
import com.esri.ags.Graphic;
import com.esri.ags.events.GraphicEvent;
import com.esri.ags.events.GraphicsLayerEvent;
import com.esri.ags.layers.FeatureLayer;
import com.esri.ags.layers.supportClasses.FeatureCollection;

public final class FeatureLayerCollectionHandler
{
    public static function add(graphic:Graphic, featureLayer:FeatureLayer):void
    {
        var featureSet:FeatureSet = featureLayer.featureCollection.featureSet;
        featureSet.features.push(graphic);
        refreshCollection(featureLayer);
        featureLayer.dispatchEvent(new GraphicEvent(GraphicEvent.GRAPHIC_ADD, graphic));
    }

    private static function refreshCollection(featureLayer:FeatureLayer):void
    {
        var featureCollection:FeatureCollection = featureLayer.featureCollection;
        featureLayer.featureCollection = null;
        featureLayer.featureCollection = featureCollection;
    }

    public static function remove(graphic:Graphic, featureLayer:FeatureLayer):void
    {
        var featureSet:FeatureSet = featureLayer.featureCollection.featureSet;
        var graphicIndex:int = featureSet.features.indexOf(graphic);
        if (graphicIndex > -1)
        {
            featureSet.features.splice(graphicIndex, 1);
            refreshCollection(featureLayer);
            featureLayer.dispatchEvent(new GraphicEvent(GraphicEvent.GRAPHIC_REMOVE, graphic));
        }
    }

    public static function clear(featureLayer:FeatureLayer):void
    {
        featureLayer.featureCollection.featureSet.features = [];
        refreshCollection(featureLayer);
        featureLayer.dispatchEvent(new GraphicsLayerEvent(GraphicsLayerEvent.GRAPHICS_CLEAR));
    }

    public static function updateFeatures(features:Array, featureLayer:FeatureLayer):void
    {
        featureLayer.featureCollection.featureSet.features = features;
        refreshCollection(featureLayer);
    }

    public static function hasFeatures(featureLayer:FeatureLayer):Boolean
    {
        return featureLayer.featureCollection.featureSet.features
            && featureLayer.featureCollection.featureSet.features.length > 0;
    }
}

}
