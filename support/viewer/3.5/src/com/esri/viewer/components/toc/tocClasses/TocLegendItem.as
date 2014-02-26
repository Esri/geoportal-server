///////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010-2013 Esri. All Rights Reserved.
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
package com.esri.viewer.components.toc.tocClasses
{

import com.esri.ags.layers.supportClasses.LegendItemInfo;

/**
 * A TOC item representing a legend item of an ArcGIS map service.
 *
 * @private
 */
public class TocLegendItem extends TocItem
{
    public function TocLegendItem(parentItem:TocItem, legendItemInfo:LegendItemInfo)
    {
        super(parentItem);

        _legendItemInfo = legendItemInfo;
        label = legendItemInfo.label ? legendItemInfo.label : " ";
    }

    //--------------------------------------------------------------------------
    //  Property:  legendItemInfo
    //--------------------------------------------------------------------------

    private var _legendItemInfo:LegendItemInfo;

    /**
     * The map layer info that backs this TOC item.
     */
    public function get legendItemInfo():LegendItemInfo
    {
        return _legendItemInfo;
    }
}

}
