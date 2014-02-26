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

import com.esri.ags.layers.supportClasses.WMSLayerInfo;

/**
 * A TOC item representing a member layer of a WMS service. 
 *
 * @private
 */
public class TocWMSLayerInfoItem extends TocItem
{
    public function TocWMSLayerInfoItem(parentItem:TocItem, wmsLayerInfo:WMSLayerInfo)
    {
        super(parentItem);

        _wmsLayerInfo = wmsLayerInfo;
        label = wmsLayerInfo.title;
    }

    //--------------------------------------------------------------------------
    //  Property:  wmsLayerInfo
    //--------------------------------------------------------------------------

    private var _wmsLayerInfo:WMSLayerInfo;

    /**
     * The wms layer info that backs this TOC item.
     */
    public function get wmsLayerInfo():WMSLayerInfo
    {
        return _wmsLayerInfo;
    }
}

}
