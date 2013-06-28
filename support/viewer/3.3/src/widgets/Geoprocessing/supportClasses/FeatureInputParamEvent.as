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

import widgets.Geoprocessing.parameters.IGPFeatureParameter;

public class FeatureInputParamEvent extends Event
{
    public static const DRAW:String = "drawFeature";
    public static const CLEAR:String = "clearFeature";

    public var featureParam:IGPFeatureParameter;
    public var drawType:String;

    public function FeatureInputParamEvent(type:String, featureParam:IGPFeatureParameter, drawType:String = null, bubbles:Boolean = false, cancelable:Boolean = false)
    {
        this.featureParam = featureParam;
        this.drawType = drawType;
        super(type, bubbles, cancelable);
    }

    override public function clone():Event
    {
        return new FeatureInputParamEvent(type, featureParam, drawType, bubbles, cancelable);
    }
}

}
