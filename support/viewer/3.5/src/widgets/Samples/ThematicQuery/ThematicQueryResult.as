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
package widgets.Samples.ThematicQuery
{

import com.esri.ags.geometry.Geometry;
import com.esri.ags.geometry.MapPoint;

import flash.events.EventDispatcher;

public class ThematicQueryResult extends EventDispatcher
{
    public var title:String;

    public var icon:String;

    public var content:String;

    public var attributes:Object;

    public var listFields:Array;

    public var columnChartFields:Array;

    public var point:MapPoint;
}

}
