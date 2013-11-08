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
package com.esri.viewer.utils
{

import com.esri.ags.symbols.PictureMarkerSymbol;
import com.esri.ags.symbols.SimpleFillSymbol;
import com.esri.ags.symbols.SimpleLineSymbol;
import com.esri.ags.symbols.SimpleMarkerSymbol;
import com.esri.ags.symbols.Symbol;

import flash.utils.ByteArray;

import mx.utils.Base64Decoder;

public class SymbolParser
{
    public function parseSymbol(symbolXML:XML):Symbol
    {
        var symbol:Symbol;

        if (symbolXML)
        {
            if (symbolXML.simplemarkersymbol[0])
            {
                symbol = parseSimpleMarkerSymbol(symbolXML.simplemarkersymbol[0]);
            }
            else if (symbolXML.picturemarkersymbol[0])
            {
                symbol = parsePictureMarkerSymbol(symbolXML.picturemarkersymbol[0]);
            }
            else if (symbolXML.simplelinesymbol[0])
            {
                symbol = parseSimpleLineSymbol(symbolXML.simplelinesymbol[0]);
            }
            else if (symbolXML.simplefillsymbol[0])
            {
                symbol = parseSimpleFillSymbol(symbolXML.simplefillsymbol[0]);
            }
        }

        return symbol;
    }

    public function parseSimpleMarkerSymbol(smsXML:XML):SimpleMarkerSymbol
    {
        const simpleMarkerSymbol:SimpleMarkerSymbol = createDefaultPointSymbol();

        const parsedColor:Number = parseInt(smsXML.@color[0]);
        const parsedAlpha:Number = parseFloat(smsXML.@alpha[0]);
        const parsedSize:Number = parseFloat(smsXML.@size[0]);

        if (smsXML.@style[0])
        {
            simpleMarkerSymbol.style = smsXML.@style;
        }
        if (!isNaN(parsedAlpha))
        {
            simpleMarkerSymbol.alpha = parsedAlpha;
        }
        if (!isNaN(parsedColor))
        {
            simpleMarkerSymbol.color = parsedColor;
        }
        if (!isNaN(parsedSize))
        {
            simpleMarkerSymbol.size = parsedSize;
        }

        if (!simpleMarkerSymbol.outline)
        {
            simpleMarkerSymbol.outline = createDefaultOutlineSymbol();
        }

        const outlineSymbol:SimpleLineSymbol = simpleMarkerSymbol.outline;

        const parsedOutlineColor:uint = parseInt(smsXML.outline.@color[0]);
        const parsedOutlineWidth:Number = parseFloat(smsXML.outline.@width[0]);

        if (smsXML.outline.@style[0])
        {
            outlineSymbol.style = smsXML.outline.@style
        }
        if (!isNaN(parsedOutlineColor))
        {
            outlineSymbol.color = parsedOutlineColor;
        }
        if (!isNaN(parsedOutlineWidth))
        {
            outlineSymbol.width = parsedOutlineWidth;
        }

        return simpleMarkerSymbol;
    }

    private var _defaultPointSymbol:SimpleMarkerSymbol;

    public function get defaultPointSymbol():SimpleMarkerSymbol
    {
        return _defaultPointSymbol ||= new SimpleMarkerSymbol();
    }

    public function set defaultPointSymbol(value:SimpleMarkerSymbol):void
    {
        _defaultPointSymbol = value;
    }

    protected function createDefaultPointSymbol():SimpleMarkerSymbol
    {
        return defaultPointSymbol.clone() as SimpleMarkerSymbol;
    }

    private var _defaultOutlineSymbol:SimpleLineSymbol;

    public function get defaultOutlineSymbol():SimpleLineSymbol
    {
        return _defaultOutlineSymbol ||= new SimpleLineSymbol();
    }

    public function set defaultOutlineSymbol(value:SimpleLineSymbol):void
    {
        _defaultOutlineSymbol = value;
    }

    protected function createDefaultOutlineSymbol():SimpleLineSymbol
    {
        return defaultOutlineSymbol.clone() as SimpleLineSymbol;
    }

    public function parsePictureMarkerSymbol(pmsXML:XML):PictureMarkerSymbol
    {
        const url:String = pmsXML.@url[0];
        const source:String = pmsXML.@source[0];

        var sourceData:ByteArray;
        if (source)
        {
            var decoder:Base64Decoder = new Base64Decoder();
            decoder.decode(source);
            sourceData = decoder.toByteArray();
        }

        const pictureSource:Object = sourceData ? sourceData : url;

        const parsedHeight:Number = parseFloat(pmsXML.@height[0]);
        const parsedWidth:Number = parseFloat(pmsXML.@width[0]);
        const parsedXOffset:Number = parseFloat(pmsXML.@xoffset[0]);
        const parsedYOffset:Number = parseFloat(pmsXML.@yoffset[0]);
        const parsedAngle:Number = parseFloat(pmsXML.@angle[0]);

        const height:Number = !isNaN(parsedHeight) ? parsedHeight : 0;
        const width:Number = !isNaN(parsedWidth) ? parsedWidth : 0;
        const xOffset:Number = !isNaN(parsedXOffset) ? parsedXOffset : 0;
        const yOffset:Number = !isNaN(parsedYOffset) ? parsedYOffset : 0;
        const angle:Number = !isNaN(parsedAngle) ? parsedAngle : 0;

        return new PictureMarkerSymbol(pictureSource, width, height, xOffset, yOffset, angle);
    }

    public function parseSimpleLineSymbol(slsXML:XML):SimpleLineSymbol
    {
        const simpleLineSymbol:SimpleLineSymbol = createDefaultPolylineSymbol();

        const parsedAlpha:Number = parseFloat(slsXML.@alpha[0]);
        const parsedOutlineColor:uint = parseInt(slsXML.@color[0]);
        const parsedOutlineWidth:Number = parseFloat(slsXML.@width[0]);

        if (slsXML.@style[0])
        {
            simpleLineSymbol.style = slsXML.@style;
        }
        if (!isNaN(parsedAlpha))
        {
            simpleLineSymbol.alpha = parsedAlpha;
        }
        if (!isNaN(parsedOutlineColor))
        {
            simpleLineSymbol.color = parsedOutlineColor;
        }
        if (!isNaN(parsedOutlineWidth))
        {
            simpleLineSymbol.width = parsedOutlineWidth;
        }

        return simpleLineSymbol;
    }

    private var _defaultPolylineSymbol:SimpleLineSymbol;

    public function get defaultPolylineSymbol():SimpleLineSymbol
    {
        return _defaultPolylineSymbol ||= new SimpleLineSymbol();
    }

    public function set defaultPolylineSymbol(value:SimpleLineSymbol):void
    {
        _defaultPolylineSymbol = value;
    }

    protected function createDefaultPolylineSymbol():SimpleLineSymbol
    {
        return defaultPolylineSymbol.clone() as SimpleLineSymbol;
    }

    public function parseSimpleFillSymbol(sfsXML:XML):SimpleFillSymbol
    {
        const simpleFillSymbol:SimpleFillSymbol = createDefaultPolygonSymbol();

        const parsedColor:Number = parseInt(sfsXML.@color[0]);
        const parsedAlpha:Number = parseFloat(sfsXML.@alpha[0]);

        if (sfsXML.@style[0])
        {
            simpleFillSymbol.style = sfsXML.@style;
        }
        if (!isNaN(parsedAlpha))
        {
            simpleFillSymbol.alpha = parsedAlpha;
        }
        if (!isNaN(parsedColor))
        {
            simpleFillSymbol.color = parsedColor;
        }

        if (!simpleFillSymbol.outline)
        {
            simpleFillSymbol.outline = createDefaultOutlineSymbol();
        }

        const outlineSymbol:SimpleLineSymbol = simpleFillSymbol.outline;

        const parsedOutlineColor:uint = parseInt(sfsXML.outline.@color[0]);
        const parsedOutlineWidth:Number = parseFloat(sfsXML.outline.@width[0]);

        if (sfsXML.outline.@style[0])
        {
            outlineSymbol.style = sfsXML.outline.@style;
        }
        if (!isNaN(parsedOutlineColor))
        {
            outlineSymbol.color = parsedOutlineColor;
        }
        if (!isNaN(parsedOutlineWidth))
        {
            outlineSymbol.width = parsedOutlineWidth;
        }

        return simpleFillSymbol;
    }

    private var _defaultPolygonSymbol:SimpleFillSymbol;

    public function get defaultPolygonSymbol():SimpleFillSymbol
    {
        return _defaultPolygonSymbol ||= new SimpleFillSymbol();
    }

    public function set defaultPolygonSymbol(value:SimpleFillSymbol):void
    {
        _defaultPolygonSymbol = value;
    }

    protected function createDefaultPolygonSymbol():SimpleFillSymbol
    {
        return defaultPolygonSymbol.clone() as SimpleFillSymbol;
    }
}
}
