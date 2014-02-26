///////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010-2011 Esri. All Rights Reserved.
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
package com.esri.viewer.components.toc.utils
{

import com.esri.ags.Map;
import com.esri.ags.SpatialReference;
import com.esri.ags.geometry.Extent;
import com.esri.ags.layers.ArcGISDynamicMapServiceLayer;
import com.esri.ags.layers.ArcGISImageServiceLayer;
import com.esri.ags.layers.ArcGISTiledMapServiceLayer;
import com.esri.ags.layers.ArcIMSMapServiceLayer;
import com.esri.ags.layers.FeatureLayer;
import com.esri.ags.layers.GPResultImageLayer;
import com.esri.ags.layers.GraphicsLayer;
import com.esri.ags.layers.Layer;

/**
 * Map utilities.
 *
 * @private
 */
public final class MapUtil
{
    /**
     * Iterates over all map layers and passes each one to the given function.
     *
     * @param func  A function with the signature <code>func( layer :Layer ) :void</code>.
     */
    public static function forEachMapLayer(map:Map, func:Function):void
    {
        if (map)
        {
            for each (var layer:Layer in map.layers)
            {
                func(layer);
            }
        }
    }

    /**
     * Returns a suitable label for the specified map layer.
     * Returns null if no label can be determined.
     */
    public static function labelLayer(layer:Layer):String
    {
        var label:String = null;

        if (layer.id == "")
        {
            // Tiled layers
            if (layer is ArcGISTiledMapServiceLayer)
            {
                label = extractServiceNameFromUrl((layer as ArcGISTiledMapServiceLayer).url);

                    // Dynamic layers
            }
            else if (layer is ArcGISDynamicMapServiceLayer)
            {
                label = extractServiceNameFromUrl((layer as ArcGISDynamicMapServiceLayer).url);
            }
            else if (layer is ArcGISImageServiceLayer)
            {
                label = extractServiceNameFromUrl((layer as ArcGISImageServiceLayer).url);
            }
            else if (layer is ArcIMSMapServiceLayer)
            {
                label = (layer as ArcIMSMapServiceLayer).serviceName;
            }
            else if (layer is FeatureLayer)
            {
                label = extractServiceNameFromUrl((layer as FeatureLayer).url);
            }
            else if (layer is GPResultImageLayer)
            {
                label = "(Task Result) " + extractServiceNameFromUrl((layer as GPResultImageLayer).url);

                    // Graphics layers
            }
            else if (layer is GraphicsLayer)
            {
                label = "Graphics";
            }
        }
        else
        {
            label = layer.name;
        }
        return label;
    }

    // These tokens must be lowercase.
    private static const IGNORE_URL_TOKENS:Array =
        [
        "mapserver",
        "wmsserver",
        "imageserver",
        "globeserver",
        "gpserver",
        "http:",
        "https:"
        ];

    private static function extractServiceNameFromUrl(url:String):String
    {
        /*
           ArcGIS URL formats:
           http://<host>/<instance>/rest/services/<service>/MapServer
           http://<host>/<instance>/rest/services/<service>/ImageServer
           http://<host>/<instance>/rest/services/<service>/GPServer/<task>
           ...
         */

        // Extract the <service> part of the URL
        if (url)
        {
            // Remove any query params
            var queryPos:int = url.indexOf("?");
            if (queryPos != -1)
            {
                url = url.substring(0, queryPos);
            }

            // Search for the service name token
            var urlParts:Array = url.split("/").reverse();
            for each (var s:String in urlParts)
            {
                if (s && s != "" && IGNORE_URL_TOKENS.indexOf(s.toLowerCase()) == -1)
                {
                    return s;
                }
            }
        }
        return null;
    }

    public static function createExtent(textualExtentAttributes:Array):Extent
    {
        if (textualExtentAttributes && textualExtentAttributes.length >= 4)
        {
            var xMin:Number = parseFloat(textualExtentAttributes[0]);
            var yMin:Number = parseFloat(textualExtentAttributes[1]);
            var xMax:Number = parseFloat(textualExtentAttributes[2]);
            var yMax:Number = parseFloat(textualExtentAttributes[3]);

            if (textualExtentAttributes.length == 5)
            {
                var wkid:Number = parseFloat(textualExtentAttributes[4]);
            }
        }

        var canCreateExtent:Boolean =
            !isNaN(xMin) && !isNaN(yMin)
            && !isNaN(xMax) && !isNaN(yMax);

        var extent:Extent;
        if (canCreateExtent)
        {
            extent = new Extent(xMin, yMin, xMax, yMax);
            if (!isNaN(wkid))
            {
                extent.spatialReference = new SpatialReference(wkid);
            }
        }

        return extent;
    }
}

}
