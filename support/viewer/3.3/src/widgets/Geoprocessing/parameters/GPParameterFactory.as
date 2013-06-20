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

public class GPParameterFactory
{
    public static function getGPParamFromType(type:String):IGPParameter
    {
        var gpParam:IGPParameter;

        switch (type)
        {
            case GPParameterTypes.BOOLEAN:
            {
                gpParam = new BooleanParameter();
                break;
            }
            case GPParameterTypes.DATA_FILE:
            {
                gpParam = new DataFileParameter();
                break;
            }
            case GPParameterTypes.DATE:
            {
                gpParam = new DateParameter();
                break;
            }
            case GPParameterTypes.DOUBLE:
            {
                gpParam = new DoubleParameter();
                break;
            }
            case GPParameterTypes.FEATURE_RECORD_SET_LAYER:
            {
                gpParam = new FeatureLayerParameter();
                break;
            }
            case GPParameterTypes.LINEAR_UNIT:
            {
                gpParam = new LinearUnitParameter();
                break;
            }
            case GPParameterTypes.LONG:
            {
                gpParam = new LongParameter();
                break;
            }
            case GPParameterTypes.MULTI_VALUE_STRING:
            {
                gpParam = new MultiValueStringParameter();
                break;
            }
            case GPParameterTypes.RASTER_DATA_LAYER:
            {
                gpParam = new RasterDataLayerParam();
                break;
            }
            case GPParameterTypes.RECORD_SET:
            {
                gpParam = new RecordSetParameter();
                break;
            }
            case GPParameterTypes.STRING:
            {
                gpParam = new StringParameter();
                break;
            }
        }

        return gpParam;
    }
}

}
