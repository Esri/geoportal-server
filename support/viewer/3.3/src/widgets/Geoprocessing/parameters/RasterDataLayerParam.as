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

import com.esri.ags.tasks.supportClasses.RasterData;

public class RasterDataLayerParam extends BaseParameter
{
    //--------------------------------------------------------------------------
    //
    //  Constants
    //
    //--------------------------------------------------------------------------

    private static const URL_DELIMITER:String = "url:";
    private static const FORMAT_DELIMITER:String = "format:";
    private static const ITEM_ID_DELIMITER:String = "itemID:";

    private static const VALID_URL_REGEX:RegExp = /^(ht|f)tps?:\/\/[^\s\.]+(\.[^\s\.]+)*((\/|\.)[^\s\.]+)+\/?$/;

    //--------------------------------------------------------------------------
    //
    //  Overridden properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  defaultValue
    //----------------------------------

    private var _defaultValue:RasterData;

    override public function get defaultValue():Object
    {
        return _defaultValue;
    }

    override public function set defaultValue(value:Object):void
    {
        _defaultValue = new RasterData(value.url, value.format);
    }

    //----------------------------------
    //  type
    //----------------------------------

    override public function get type():String
    {
        return GPParameterTypes.RASTER_DATA_LAYER;
    }

    //--------------------------------------------------------------------------
    //
    //  Overridden methods
    //
    //--------------------------------------------------------------------------

    public override function defaultValueFromString(description:String):void
    {
        var dataFile:RasterData = new RasterData();

        if (description.indexOf(URL_DELIMITER) == 0
            || description.indexOf(FORMAT_DELIMITER) == 0)
        {
            var tokens:Array = description.split(",");
            for each (var token:String in tokens)
            {
                if (token.indexOf(URL_DELIMITER) == 0)
                {
                    dataFile.url = token.substr(URL_DELIMITER.length);
                }
                else if (token.indexOf(FORMAT_DELIMITER) == 0)
                {
                    dataFile.format = token.substr(FORMAT_DELIMITER.length);
                }
            }
        }
        else if (description.indexOf(ITEM_ID_DELIMITER) == 0)
        {
            dataFile.itemID = description.substr(ITEM_ID_DELIMITER.length);
        }

        _defaultValue = dataFile;
    }

    override public function hasValidValue():Boolean
    {
        if (_defaultValue.itemID)
        {
            return true;
        }
        else if (_defaultValue.url)
        {
            return VALID_URL_REGEX.test(_defaultValue.url);
        }
        else
        {
            return false;
        }
    }
}

}
