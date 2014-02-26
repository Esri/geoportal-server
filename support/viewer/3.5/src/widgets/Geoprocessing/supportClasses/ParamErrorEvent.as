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

import flash.events.Event;

public class ParamErrorEvent extends Event
{
    public static const ERROR:String = "error";

    public function ParamErrorEvent(type:String, message:String, title:String = null)
    {
        super(type, false, false);
        _message = message;
        _title = title;
    }

    private var _message:String;

    public function get message():String
    {
        return _message;
    }

    private var _title:String;

    public function get title():String
    {
        return _title;
    }

    public override function clone():Event
    {
        return new ParamErrorEvent(type, message, title);
    }
}
}
