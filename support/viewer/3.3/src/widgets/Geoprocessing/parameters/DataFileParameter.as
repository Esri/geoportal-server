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

import com.esri.ags.tasks.supportClasses.DataFile;

public class DataFileParameter extends BaseParameter
{
    //--------------------------------------------------------------------------
    //
    //  Constants
    //
    //--------------------------------------------------------------------------

    private static const URL_DELIMITER:String = "url:";
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

    private var _defaultValue:DataFile;

    override public function get defaultValue():Object
    {
        return _defaultValue;
    }

    override public function set defaultValue(value:Object):void
    {
        _defaultValue = new DataFile(value.url, value.itemID);
    }

    //----------------------------------
    //  type
    //----------------------------------

    override public function get type():String
    {
        return GPParameterTypes.DATA_FILE;
    }

    //--------------------------------------------------------------------------
    //
    //  Overridden methods
    //
    //--------------------------------------------------------------------------

    override public function defaultValueFromString(description:String):void
    {
        var dataFile:DataFile = new DataFile();

        if (description.indexOf(URL_DELIMITER) == 0)
        {
            dataFile.url = description.substr(URL_DELIMITER.length);
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
            var validURLIndex:int = _defaultValue.url.search(VALID_URL_REGEX);
            return validURLIndex == 0;
        }
        else
        {
            return false;
        }
    }
}

}
