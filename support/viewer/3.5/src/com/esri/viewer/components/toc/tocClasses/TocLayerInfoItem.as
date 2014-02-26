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
package com.esri.viewer.components.toc.tocClasses
{

import com.esri.ags.layers.supportClasses.LayerInfo;

/**
 * A TOC item representing a member layer of an ArcGIS or ArcIMS map service.
 * This includes group layers that contain other member layers.
 *
 * @private
 */
public class TocLayerInfoItem extends TocItem
{
    public function TocLayerInfoItem(parentItem:TocItem, layerInfo:LayerInfo, isVisible:Boolean, isInScaleRange:Boolean)
    {
        super(parentItem);

        _layerInfo = layerInfo;
        label = layerInfo.name;

        setVisible(isVisible, false);
        setIsInScaleRange(isInScaleRange, false);
    }

    //--------------------------------------------------------------------------
    //  Property:  layerInfo
    //--------------------------------------------------------------------------

    private var _layerInfo:LayerInfo;

    /**
     * The map layer info that backs this TOC item.
     */
    public function get layerInfo():LayerInfo
    {
        return _layerInfo;
    }
}

}
