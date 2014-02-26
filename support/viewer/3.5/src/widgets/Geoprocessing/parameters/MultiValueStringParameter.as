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

public class MultiValueStringParameter extends BaseParameter
{
    //--------------------------------------------------------------------------
    //
    //  Overridden properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  defaultValue
    //----------------------------------

    private var _defaultValue:Array;

    override public function get defaultValue():Object
    {
        return _defaultValue;
    }

    override public function set defaultValue(value:Object):void
    {
        _defaultValue = value as Array;
    }

    //----------------------------------
    //  type
    //----------------------------------

    override public function get type():String
    {
        return GPParameterTypes.MULTI_VALUE_STRING;
    }

    //--------------------------------------------------------------------------
    //
    //  Overridden methods
    //
    //--------------------------------------------------------------------------

    override public function defaultValueFromString(description:String):void
    {
        if (description)
        {
            defaultValue = description.split(',');
        }
    }

    public override function hasValidValue():Boolean
    {
        var atLeastOneItemSelected:Boolean = false;
        for each (var multiValueItem:MultiValueItem in choiceList)
        {
            if (multiValueItem.selected)
            {
                atLeastOneItemSelected = true;
                break;
            }
        }
        return atLeastOneItemSelected;
    }

    public override function getRequestObjectValue():Object
    {
        var selectedMultiValues:Array = [];
        for each (var multiValueItem:MultiValueItem in choiceList)
        {
            if (multiValueItem.selected)
            {
                selectedMultiValues.push(multiValueItem.item);
            }
        }
        return selectedMultiValues;
    }
}

}
