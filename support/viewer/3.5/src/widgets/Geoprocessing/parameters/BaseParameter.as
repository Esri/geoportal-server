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

public class BaseParameter implements IGPParameter
{
    //--------------------------------------------------------------------------
    //
    //  Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  serviceInfo
    //----------------------------------

    private var _serviceInfo:Object;

    public function get serviceInfo():Object
    {
        return _serviceInfo ||= {};
    }

    public function set serviceInfo(value:Object):void
    {
        _serviceInfo = value;
    }

    //----------------------------------
    //  paramInfo
    //----------------------------------

    private var _paramInfo:Object;

    public function get paramInfo():Object
    {
        return _paramInfo ||= {};
    }

    public function set paramInfo(value:Object):void
    {
        _paramInfo = value;
    }

    //----------------------------------
    //  label
    //----------------------------------

    private var _label:String;

    public function get label():String
    {
        return _label;
    }

    public function set label(value:String):void
    {
        _label = value;
    }

    //----------------------------------
    //  name
    //----------------------------------

    private var _name:String;

    public function get name():String
    {
        return _name;
    }

    public function set name(value:String):void
    {
        _name = value;
    }

    //----------------------------------
    //  type
    //----------------------------------

    private var _type:String;

    public function get type():String
    {
        return _type;
    }

    //----------------------------------
    //  direction
    //----------------------------------

    private var _direction:String;

    public function get direction():String
    {
        return _direction;
    }

    public function set direction(value:String):void
    {
        _direction = value;
    }

    //----------------------------------
    //  defaultValue
    //----------------------------------

    private var _defaultValue:Object;

    public function get defaultValue():Object
    {
        return _defaultValue;
    }

    public function set defaultValue(value:Object):void
    {
        _defaultValue = value;
    }

    //----------------------------------
    //  choiceList
    //----------------------------------

    private var _choiceList:Array;

    public function get choiceList():Array
    {
        return _choiceList;
    }

    public function set choiceList(value:Array):void
    {
        _choiceList = value;
    }

    //----------------------------------
    //  tooltip
    //----------------------------------

    private var _toolTip:String;

    public function get toolTip():String
    {
        return _toolTip;
    }

    public function set toolTip(value:String):void
    {
        _toolTip = value;
    }

    //----------------------------------
    //  visible
    //----------------------------------

    private var _visible:Boolean = true;

    public function get visible():Boolean
    {
        return _visible;
    }

    public function set visible(value:Boolean):void
    {
        _visible = value;
    }

    //----------------------------------
    //  required
    //----------------------------------

    private var _required:Boolean;

    public function get required():Boolean
    {
        return _required;
    }

    public function set required(value:Boolean):void
    {
        _required = value;
    }

    //--------------------------------------------------------------------------
    //
    //  Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Extending classes should override this method if value cannot be casted from String.
     */
    public function defaultValueFromString(text:String):void
    {
        defaultValue = text;
    }

    public function getRequestObjectValue():Object
    {
        return defaultValue;
    }

    public function hasValidValue():Boolean
    {
        return defaultValue;
    }
}

}
