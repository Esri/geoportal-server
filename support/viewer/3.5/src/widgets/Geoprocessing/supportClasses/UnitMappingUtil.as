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

import com.esri.ags.Units;

public final class UnitMappingUtil
{
    public static function toPrettyUnits(units:String):String
    {
        var prettyUnits:String;

        switch (units)
        {
            case Units.FEET:
            {
                prettyUnits = "Feet";
                break;
            }
            case Units.MILES:
            {
                prettyUnits = "Miles";
                break;
            }
            case Units.NAUTICAL_MILES:
            {
                prettyUnits = "Nautical Miles";
                break;
            }
            case Units.METERS:
            {
                prettyUnits = "Meters";
                break;
            }
            case Units.KILOMETERS:
            {
                prettyUnits = "Kilometers";
                break;
            }
        }

        return prettyUnits;
    }

    public static function toEsriUnits(units:String):String
    {
        var esriUnits:String;

        switch (units)
        {
            case "Feet":
            {
                esriUnits = Units.FEET;
                break;
            }
            case "Miles":
            {
                esriUnits = Units.MILES;
                break;
            }
            case "Nautical Miles":
            {
                esriUnits = Units.NAUTICAL_MILES;
                break;
            }
            case "Meters":
            {
                esriUnits = Units.METERS;
                break;
            }
            case "Kilometers":
            {
                esriUnits = Units.KILOMETERS;
                break;
            }
        }

        return esriUnits;
    }
}

}
