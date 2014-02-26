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
import com.esri.ags.geometry.Extent;
import com.esri.ags.geometry.Polygon;
import com.esri.ags.layers.GraphicsLayer;
import com.esri.ags.utils.GraphicUtil;

import flash.utils.Dictionary;

import widgets.Geoprocessing.parameters.BaseParamParser;
import widgets.Geoprocessing.parameters.FeatureLayerParameter;
import widgets.Geoprocessing.parameters.GPParameterTypes;
import widgets.Geoprocessing.parameters.IGPFeatureParameter;
import widgets.Geoprocessing.parameters.IGPParameter;
import widgets.Geoprocessing.parameters.InputParamParser;
import widgets.Geoprocessing.parameters.OutputParamParser;

public class GPParamHandler
{
    public function GPParamHandler(paramsXML:XML)
    {
        var inputParamParser:BaseParamParser = new InputParamParser();
        var outputParamParser:BaseParamParser = new OutputParamParser();
        _inputParams = inputParamParser.parseParameters(paramsXML.inputparams.param);
        _outputParams = outputParamParser.parseParameters(paramsXML.outputparams.param);
        _totalOutputFeatures = getTotalFeatures(_outputParams);
        _totalInputFeatures = getTotalFeatures(_inputParams);

        if (_totalInputFeatures)
        {
            _needsToReprojectInputParams = hasInputSpatialReferences();
        }
    }

    private function getTotalFeatures(params:Array):int
    {
        var total:int = 0;
        var featureParam:IGPFeatureParameter;

        for each (var param:IGPParameter in params)
        {
            if (param.type == GPParameterTypes.FEATURE_RECORD_SET_LAYER)
            {
                featureParam = (param as IGPFeatureParameter);
                total++;
            }
        }

        return total;
    }

    public function getInputParamGeometryMap():Dictionary
    {
        var inputGeometryMap:Dictionary = new Dictionary();
        var inputFeatureSet:FeatureSet;

        for each (var inputParam:IGPParameter in _inputParams)
        {
            if (inputParam.type == GPParameterTypes.FEATURE_RECORD_SET_LAYER)
            {
                inputFeatureSet = (inputParam as IGPFeatureParameter).getRequestObjectValue() as FeatureSet;
                inputGeometryMap[inputParam.name] = GraphicUtil.getGeometries(inputFeatureSet.features);
            }
        }

        return inputGeometryMap;
    }

    private var _needsToReprojectInputParams:Boolean;

    public function get needsToReprojectInputParams():Boolean
    {
        return _needsToReprojectInputParams;
    }

    private function hasInputSpatialReferences():Boolean
    {
        var atLeastOneSpatialReferenceDefined:Boolean = false;

        for each (var inputParam:IGPParameter in _inputParams)
        {
            if (inputParam.type == GPParameterTypes.FEATURE_RECORD_SET_LAYER)
            {
                if ((inputParam as IGPFeatureParameter).spatialReference)
                {
                    atLeastOneSpatialReferenceDefined = true;
                    break;
                }
            }
        }

        return atLeastOneSpatialReferenceDefined;
    }

    private var _totalInputFeatures:int;

    public function get totalInputFeatures():int
    {
        return _totalInputFeatures;
    }

    private var _totalOutputFeatures:int;

    public function get totalOutputFeatures():int
    {
        return _totalOutputFeatures;
    }

    private var _inputParams:Array;

    public function get inputParams():Array
    {
        return _inputParams;
    }

    private var _outputParams:Array;

    public function get outputParams():Array
    {
        return _outputParams;
    }

    public function getVisibleInputParams():Array
    {
        return getVisibleParams(_inputParams);
    }

    private function getVisibleParams(params:Array):Array
    {
        var visibleParams:Array = [];

        for each (var param:IGPParameter in params)
        {
            if (param.visible)
            {
                visibleParams.push(param);
            }
        }

        return visibleParams;
    }

    private function getIncompleteRequiredFields(params:Array):Array
    {
        var incompleteRequiredFields:Array = [];

        for each (var param:IGPParameter in params)
        {
            if (param.required && !param.hasValidValue())
            {
                incompleteRequiredFields.push(param);
            }
        }

        return incompleteRequiredFields;
    }

    public function getIncompleteRequiredInputParams():Array
    {
        return getIncompleteRequiredFields(_inputParams);
    }

    public function createInputRequestObject():Object
    {
        return createRequestObject(_inputParams);
    }

    private function createRequestObject(params:Array):Object
    {
        var requestObject:Object = {};

        for each (var param:IGPParameter in params)
        {
            requestObject[param.name] = param.getRequestObjectValue();
        }

        return requestObject;
    }

    public function findInputParamByName(name:String):IGPParameter
    {
        return findParamByName(name, _inputParams);
    }

    public function findOutputParamByName(name:String):IGPParameter
    {
        return findParamByName(name, _outputParams);
    }

    private function findParamByName(name:String, params:Array):IGPParameter
    {
        var foundParam:IGPParameter;

        for each (var param:IGPParameter in params)
        {
            if (param.name == name)
            {
                foundParam = param;
                break;
            }
        }

        return foundParam;
    }

    public function hasVisibleOutput():Boolean
    {
        var hasVisibleOutputParam:Boolean = false;

        if (outputParams)
        {
            for each (var param:IGPParameter in outputParams)
            {
                if (isVisibleOutputType(param))
                {
                    hasVisibleOutputParam = true;
                    break;
                }
            }
        }

        return hasVisibleOutputParam;
    }

    private function isVisibleOutputType(param:IGPParameter):Boolean
    {
        var isVisibleOutput:Boolean

        switch (param.type)
        {
            case GPParameterTypes.STRING:
            case GPParameterTypes.BOOLEAN:
            case GPParameterTypes.LONG:
            case GPParameterTypes.DOUBLE:
            case GPParameterTypes.DATE:
            case GPParameterTypes.LINEAR_UNIT:
            case GPParameterTypes.RASTER_DATA_LAYER:
            case GPParameterTypes.RECORD_SET:
            {
                isVisibleOutput = true;
                break;
            }
            default:
            {
                isVisibleOutput = false;
            }
        }

        return isVisibleOutput;
    }

    public function showFeatureLayers():void
    {
        toggleFeatureParameterLayerVisibility(true);
    }

    public function hideFeatureLayers():void
    {
        toggleFeatureParameterLayerVisibility(false);
    }

    public function getInputFeatureParams():Array
    {
        return getFeatureParams(_inputParams);
    }

    public function getOutputFeatureParams():Array
    {
        return getFeatureParams(_outputParams);
    }

    private function getFeatureParams(params:Array):Array
    {
        var featureParams:Array = [];

        for each (var param:IGPParameter in params)
        {
            if (param.type == GPParameterTypes.FEATURE_RECORD_SET_LAYER)
            {
                featureParams.push(param);
            }
        }

        return featureParams;
    }

    public function getOutputFeaturesExtent():Extent
    {
        var featureParam:IGPFeatureParameter;
        var features:Array;
        var mergedFeatures:Array = [];

        for each (var param:IGPParameter in _outputParams)
        {
            if (param.type == GPParameterTypes.FEATURE_RECORD_SET_LAYER)
            {
                featureParam = (param as IGPFeatureParameter);
                features = featureParam.layer.featureCollection.featureSet.features;
                mergedFeatures = mergedFeatures.concat(features);
            }
        }

        return GraphicUtil.getGraphicsExtent(mergedFeatures);
    }

    public function graphicsLayerBelongsToFeatureParam(graphicsLayer:GraphicsLayer):Boolean
    {
        var graphicsLayerBelongsToFeatureParam:Boolean = false;
        var featureParam:IGPFeatureParameter;

        for each (var param:IGPParameter in _outputParams)
        {
            if (param.type == GPParameterTypes.FEATURE_RECORD_SET_LAYER)
            {
                featureParam = (param as IGPFeatureParameter);
                if (featureParam.layer === graphicsLayer)
                {
                    graphicsLayerBelongsToFeatureParam = true;
                    break;
                }
            }
        }

        return graphicsLayerBelongsToFeatureParam;
    }

    private function toggleFeatureParameterLayerVisibility(visible:Boolean):void
    {
        for each (var inputParam:IGPParameter in _inputParams)
        {
            if (inputParam.type == GPParameterTypes.FEATURE_RECORD_SET_LAYER)
            {
                (inputParam as IGPFeatureParameter).layer.visible = visible;
            }
        }

        for each (var outputParam:IGPParameter in _outputParams)
        {
            if (outputParam.type == GPParameterTypes.FEATURE_RECORD_SET_LAYER)
            {
                (outputParam as IGPFeatureParameter).layer.visible = visible;
            }
        }
    }

    public function clearOutputFeatures():void
    {
        clearFeatures(_outputParams);
    }

    private function clearFeatures(params:Array):void
    {
        for each (var param:IGPParameter in params)
        {
            if (param.type == GPParameterTypes.FEATURE_RECORD_SET_LAYER)
            {
                FeatureLayerCollectionHandler.clear((param as IGPFeatureParameter).layer);
            }
        }
    }

    public function clearInputFeatures():void
    {
        clearFeatures(_inputParams);
    }

    public function hasMapExtentInputParams():Boolean
    {
        var hasParamWithMapExtentMode:Boolean = false;

        var featureParam:IGPFeatureParameter;
        for each (var param:IGPParameter in _inputParams)
        {
            featureParam = param as IGPFeatureParameter;
            if (featureParam && featureParam.mode == FeatureLayerParameter.MAP_EXTENT_SOURCE)
            {
                hasParamWithMapExtentMode = true;
                break;
            }
        }

        return hasParamWithMapExtentMode;
    }

    public function updateMapExtentInputParams(extent:Extent):void
    {
        const extentPolygon:Polygon = extent.toPolygon();

        var featureParam:IGPFeatureParameter;
        for each (var param:IGPParameter in _inputParams)
        {
            featureParam = param as IGPFeatureParameter;
            if (featureParam && featureParam.mode == FeatureLayerParameter.MAP_EXTENT_SOURCE)
            {
                FeatureLayerCollectionHandler.clear(featureParam.layer);
                FeatureLayerCollectionHandler.add(new Graphic(extentPolygon), featureParam.layer);
            }
        }
    }

    public function setServiceInfo(serviceInfo:Object):void
    {
        var allParams:Array = _inputParams.concat(_outputParams);

        for each (var param:IGPParameter in allParams)
        {
            param.serviceInfo = serviceInfo;
        }
    }

    public function hasUploadCompatibleParams():Boolean
    {
        var hasUploadCompatibleInputParam:Boolean = false;

        for each (var param:IGPParameter in inputParams)
        {
            if (param.type == GPParameterTypes.DATA_FILE
                || param.type == GPParameterTypes.RASTER_DATA_LAYER)
            {
                hasUploadCompatibleInputParam = true;
                break;
            }
        }

        return hasUploadCompatibleInputParam;
    }

    public function setTaskInfo(taskInfo:Object):void
    {
        if (!taskInfo || !taskInfo.parameters)
        {
            return;
        }

        var allParams:Array = _inputParams.concat(_outputParams);

        for each (var param:IGPParameter in allParams)
        {
            for each (var paramInfo:Object in taskInfo.parameters)
            {
                if (param.name == paramInfo.name)
                {
                    param.paramInfo = paramInfo;
                    break;
                }
            }
        }
    }
}
}
