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

import com.esri.ags.renderers.IRenderer;
import com.esri.viewer.utils.RendererParser;

public class BaseParamParser
{
    private const SIMPLE_RENDERER_TYPE:String = "simple";
    private const CLASS_BREAKS_RENDERER_TYPE:String = "classbreaks";
    private const UNIQUE_VALUE_RENDERER_TYPE:String = "uniquevalue";

    private const SIMPLE_MARKER_SYMBOL_TYPE:String = "simplemarker";
    private const PICTURE_MARKER_SYMBOL_TYPE:String = "picturemarker";
    private const SIMPLE_FILL_SYMBOL_TYPE:String = "simplefill";
    private const SIMPLE_LINE_SYMBOL_TYPE:String = "simpleline";

    private const SIMPLE_RENDERER_TAG_NAME:String = "simplerenderer";
    private const CLASS_BREAKS_RENDERER_TAG_NAME:String = "classbreaksrenderer";
    private const UNIQUE_VALUE_RENDERER_TAG_NAME:String = "uniquevaluerenderer";

    private const SIMPLE_LINE_SYMBOL_TAG_NAME:String = "simplelinesymbol";
    private const SIMPLE_FILL_SYMBOL_TAG_NAME:String = "simplefillsymbol";
    private const PICTURE_MARKER_SYMBOL_TAG_NAME:String = "picturemarkersymbol";
    private const SIMPLE_MARKER_SYMBOL_TAG_NAME:String = "simplemarkersymbol";

    protected var rendererParser:RendererParser;

    public function BaseParamParser()
    {
        rendererParser = new RendererParser();
    }

    public function parseParameters(paramsXML:XMLList):Array
    {
        throw new Error("Abstract class - must be implemented by subclasses.");
    }

    protected function parseRenderer(paramXML:XML, geometryType:String):IRenderer
    {
        const hasGPRendererXML:Boolean = paramXML.renderer[0];

        if (hasGPRendererXML)
        {
            updateGPRendererXML(paramXML.renderer[0], geometryType);
        }
        else
        {
            ensureUpdatedRendererAndSymbolXMLExist(paramXML, geometryType);
        }

        return rendererParser.parseRenderer(paramXML);
    }

    /* helper function that converts to new renderer parser XML */
    private function updateGPRendererXML(rendererXML:XML, geometryType:String):void
    {
        ensureRendererTypeExists(rendererXML);
        ensureSymbolTypesExist(rendererXML, geometryType);
        updateRendererTagName(rendererXML);
        updateRendererAndInfosXML(rendererXML);
        updateSymbolTags(rendererXML);
    }

    private function ensureRendererTypeExists(rendererXML:XML):void
    {
        if (!rendererXML.@type)
        {
            rendererXML.@type = SIMPLE_RENDERER_TYPE;
        }
    }

    private function ensureSymbolTypesExist(rendererXML:XML, geometryType:String):void
    {
        var symbolTypeFallback:String = defaultSymbolTypeFromGeometryType(geometryType);

        var defaultSymbolXMLList:XMLList = rendererXML.defaultsymbol;
        if (defaultSymbolXMLList.length() > 0)
        {
            setMissingSymbolTypes(defaultSymbolXMLList, symbolTypeFallback);
        }
        else
        {
            rendererXML.appendChild(updatedDefaultSymbolXMLFromGeometryType(geometryType));
        }

        setMissingSymbolTypes(rendererXML..symbol, symbolTypeFallback);
    }

    private function defaultSymbolTypeFromGeometryType(geometryType:String):String
    {
        var defaultSymbol:String;

        if (geometryType == FeatureLayerParameter.POINT)
        {
            defaultSymbol = SIMPLE_MARKER_SYMBOL_TYPE;
        }
        else if (geometryType == FeatureLayerParameter.POLYGON)
        {
            defaultSymbol = SIMPLE_FILL_SYMBOL_TYPE;
        }
        else if (geometryType == FeatureLayerParameter.POLYLINE)
        {
            defaultSymbol = SIMPLE_LINE_SYMBOL_TYPE;
        }

        return defaultSymbol;
    }

    private function setMissingSymbolTypes(symbolXMLList:XMLList, symbolTypeFallback:String):void
    {
        for each (var symbol:XML in symbolXMLList)
        {
            if (!symbol.@type[0])
            {
                symbol.@type = symbolTypeFallback;
            }
        }
    }

    private function updateRendererTagName(rendererXML:XML):void
    {
        rendererXML.setName(toRendererTagName(rendererXML.@type));
        delete rendererXML.@type;
    }

    private function toRendererTagName(rendererType:String):String
    {
        var rendererTagName:String;

        if (rendererType == CLASS_BREAKS_RENDERER_TYPE)
        {
            rendererTagName = CLASS_BREAKS_RENDERER_TAG_NAME;
        }
        else if (rendererType == UNIQUE_VALUE_RENDERER_TYPE)
        {
            rendererTagName = UNIQUE_VALUE_RENDERER_TAG_NAME;
        }
        else //default is simple renderer
        {
            rendererTagName = SIMPLE_RENDERER_TAG_NAME;
        }

        return rendererTagName;
    }

    private function updateRendererAndInfosXML(rendererXML:XML):void
    {
        if (rendererXML.name() == SIMPLE_RENDERER_TAG_NAME)
        {
            return;
        }

        if (rendererXML.@attribute[0])
        {
            rendererXML.@field = rendererXML.@attribute;
            delete rendererXML.@attribute;
        }

        if (rendererXML.name() == CLASS_BREAKS_RENDERER_TAG_NAME)
        {
            updateClassBreakInfos(rendererXML);
        }
        else if (rendererXML.name() == UNIQUE_VALUE_RENDERER_TAG_NAME)
        {
            updateUniqueValueInfos(rendererXML);
        }
    }

    private function updateClassBreakInfos(rendererXML:XML):void
    {
        var symbolXMLList:XMLList = rendererXML.infos.symbol;
        for each (var symbolXML:XML in symbolXMLList)
        {
            var classBreakInfoXML:XML = <classbreakinfo/>;
            classBreakInfoXML.@min = symbolXML.@min;
            classBreakInfoXML.@max = symbolXML.@max;
            classBreakInfoXML.appendChild(symbolXML);
            rendererXML.appendChild(classBreakInfoXML);
        }
        delete rendererXML.infos;
    }

    private function updateUniqueValueInfos(rendererXML:XML):void
    {
        var symbolXMLList:XMLList = rendererXML.infos.symbol;
        for each (var symbolXML:XML in symbolXMLList)
        {
            var uniqueValueInfoXML:XML = <uniquevalueinfo/>;
            uniqueValueInfoXML.@value = symbolXML.@value;
            uniqueValueInfoXML.appendChild(symbolXML);
            rendererXML.appendChild(uniqueValueInfoXML);
        }
        delete rendererXML.infos;
    }

    private function updateSymbolTags(rendererXML:XML):void
    {
        var symbolNodes:XMLList = rendererXML..defaultsymbol + rendererXML..symbol;

        for each (var symbolNode:XML in symbolNodes)
        {
            symbolNode.setName(toSymbolTagName(symbolNode.@type));
            delete symbolNode.@type;

            if (symbolNode.name() == SIMPLE_LINE_SYMBOL_TAG_NAME)
            {
                if (symbolNode.outline[0])
                {
                    if (symbolNode.outline.@color[0])
                    {
                        symbolNode.@color = symbolNode.outline.@color;
                    }

                    if (symbolNode.outline.@width[0])
                    {
                        symbolNode.@width = symbolNode.outline.@width[0];
                    }

                    delete symbolNode.outline;
                }
            }
        }
    }

    private function toSymbolTagName(symbolType:String):String
    {
        var symbolTagName:String;

        if (symbolType == SIMPLE_LINE_SYMBOL_TYPE)
        {
            symbolTagName = SIMPLE_LINE_SYMBOL_TAG_NAME;
        }
        else if (symbolType == SIMPLE_FILL_SYMBOL_TYPE)
        {
            symbolTagName = SIMPLE_FILL_SYMBOL_TAG_NAME;
        }
        else if (symbolType == PICTURE_MARKER_SYMBOL_TYPE)
        {
            symbolTagName = PICTURE_MARKER_SYMBOL_TAG_NAME;
        }
        else //default is simple marker
        {
            symbolTagName = SIMPLE_MARKER_SYMBOL_TAG_NAME;
        }

        return symbolTagName;
    }

    private function ensureUpdatedRendererAndSymbolXMLExist(paramXML:XML, geometryType:String):void
    {
        const updatedRendererXML:XML = (paramXML.simplerenderer[0] || paramXML.classbreaksrenderer[0] || paramXML.uniquevaluerenderer[0]);
        if (updatedRendererXML)
        {
            const hasSymbolDefined:Boolean = updatedRendererXML.simplemarkersymbol[0] || updatedRendererXML.picturemarkersymbol[0] || updatedRendererXML.simplelinesymbol[0] || updatedRendererXML.simplefillsymbol[0];
            if (!hasSymbolDefined)
            {
                appendUpdatedDefaultSymbolXMLFromGeometry(updatedRendererXML, geometryType);
            }
        }
        else
        {
            appendUpdatedDefaultRendererXML(paramXML, geometryType);
        }
    }

    private function appendUpdatedDefaultSymbolXMLFromGeometry(rendererXML:XML, geometryType:String):void
    {
        rendererXML.appendChild(updatedDefaultSymbolXMLFromGeometryType(geometryType));
    }

    private function updatedDefaultSymbolXMLFromGeometryType(geometryType:String):XML
    {
        var defaultSymbolXML:XML = <symbol/>;

        if (geometryType == FeatureLayerParameter.POINT)
        {
            defaultSymbolXML.setName(SIMPLE_MARKER_SYMBOL_TAG_NAME);
        }
        else if (geometryType == FeatureLayerParameter.POLYGON)
        {
            defaultSymbolXML.setName(SIMPLE_FILL_SYMBOL_TAG_NAME);
        }
        else if (geometryType == FeatureLayerParameter.POLYLINE)
        {
            defaultSymbolXML.setName(SIMPLE_LINE_SYMBOL_TAG_NAME);
        }

        return defaultSymbolXML;
    }

    private function appendUpdatedDefaultRendererXML(paramXML:XML, geometryType:String):void
    {
        var simpleRendererXML:XML = <simplerenderer/>;
        appendUpdatedDefaultSymbolXMLFromGeometry(simpleRendererXML, geometryType);
        paramXML.appendChild(simpleRendererXML);
    }
}
}
