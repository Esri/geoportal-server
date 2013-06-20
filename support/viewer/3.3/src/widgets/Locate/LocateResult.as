///////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010-2011 Esri. All Rights Reserved.
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
package widgets.Locate
{

import com.esri.ags.geometry.MapPoint;
import com.esri.ags.symbols.Symbol;

import flash.events.EventDispatcher;

[Bindable]

public class LocateResult extends EventDispatcher
{
    public var title:String;

    public var symbol:Symbol;

    public var content:String;

    public var point:MapPoint;

    public var link:String;

    public var selected:Boolean;
}

}
