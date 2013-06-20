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
import com.esri.ags.symbols.SimpleFillSymbol;
import com.esri.ags.symbols.SimpleLineSymbol;
import com.esri.ags.symbols.SimpleMarkerSymbol;
import com.esri.viewer.utils.RendererParser;
import com.esri.viewer.utils.SymbolParser;

public class InputParamParser extends BaseParamParser
{
    public function InputParamParser()
    {
        var inputParamSymbolParser:SymbolParser = new SymbolParser();
        inputParamSymbolParser.defaultPointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.STYLE_CIRCLE, 15, 0x3FAFDC, 1, 0, 0, 0);
        inputParamSymbolParser.defaultPolylineSymbol = new SimpleLineSymbol(SimpleLineSymbol.STYLE_SOLID, 0x3FAFDC, 1, 5);
        inputParamSymbolParser.defaultPolygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.STYLE_SOLID, 0x3FAFDC, 1);
        rendererParser = new RendererParser(inputParamSymbolParser);
    }

    override public function parseParameters(paramsXML:XMLList):Array
    {
        var params:Array = [];
        var param:IGPParameter;

        for each (var paramXML:XML in paramsXML)
        {
            param = GPParameterFactory.getGPParamFromType(paramXML.@type);
            param.defaultValueFromString(String(paramXML.@defaultvalue));
            param.label = paramXML.@label;
            param.name = paramXML.@name;
            param.toolTip = paramXML.@tooltip;
            param.visible = (paramXML.@visible == "true");
            param.required = (paramXML.@required == "true");

            if (paramXML.choicelist[0])
            {
                if (param.type == GPParameterTypes.MULTI_VALUE_STRING)
                {
                    param.choiceList = parseMultiValueChoiceList(paramXML.choicelist.choice);
                }
                else
                {
                    param.choiceList = parseChoiceList(paramXML.choicelist.choice);
                }
            }

            var featureParam:IGPFeatureParameter = param as IGPFeatureParameter;
            if (featureParam)
            {
                featureParam.geometryType = paramXML.@geometrytype;
                featureParam.layerName = featureParam.label;

                if (paramXML.@mode[0])
                {
                    featureParam.mode = paramXML.@mode[0];

                    if (paramXML.layers[0])
                    {
                        featureParam.layerNames = parseLayerWhiteList(paramXML.layers.layer);
                    }
                }

                var wkid:Number = parseFloat(paramXML.@wkid[0]);
                var wkt:String = paramXML.@wkt[0];
                if (wkid || wkt)
                {
                    featureParam.spatialReference = new SpatialReference(wkid, wkt)
                }

                featureParam.renderer = parseRenderer(paramXML, featureParam.geometryType);
            }

            params.push(param);
        }

        return params;
    }

    private function parseLayerWhiteList(layersXML:XMLList):Array
    {
        var layerWhiteList:Array = [];

        for each (var layer:XML in layersXML)
        {
            layerWhiteList.push(layer.@name);
        }

        return layerWhiteList;
    }

    private function parseMultiValueChoiceList(choiceListXML:XMLList):Array
    {
        var choiceList:Array = [];
        var choiceValue:String;

        for each (var choice:XML in choiceListXML)
        {
            choiceList.push(new MultiValueItem(choice.@value));
        }

        return choiceList;
    }

    private function parseChoiceList(choiceListXML:XMLList):Array
    {
        var choiceList:Array = [];
        var choiceValue:String;

        for each (var choice:XML in choiceListXML)
        {
            choiceValue = choice.@value;
            choiceList.push(choiceValue);
        }

        return choiceList;
    }
}
}
