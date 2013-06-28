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
package com.esri.viewer.components.toc.tocClasses
{

import com.esri.ags.layers.KMLLayer;
import com.esri.ags.layers.supportClasses.KMLFolder;

/**
 * A TOC item representing folder of a KML Layer.
 *
 * @private
 */
public class TocKmlFolderItem extends TocItem
{
    public function TocKmlFolderItem(parentItem:TocItem, folder:KMLFolder, layer:KMLLayer)
    {
        super(parentItem);

        _folder = folder;
        _layer = layer;
        label = folder.name;

        setVisible(folder.visible, false);
    }

    //--------------------------------------------------------------------------
    //  Property:  folder
    //--------------------------------------------------------------------------

    private var _folder:KMLFolder;

    /**
     * The KML Folder that represents this TOC item.
     */
    public function get folder():KMLFolder
    {
        return _folder;
    }

    //--------------------------------------------------------------------------
    //  Property:  layer
    //--------------------------------------------------------------------------

    private var _layer:KMLLayer;

    /**
     * The KML layer associated with this TOC item.
     */
    public function get layer():KMLLayer
    {
        return _layer;
    }

    //--------------------------------------------------------------------------
    //
    //  Methods
    //
    //--------------------------------------------------------------------------

    /**
     * @private
     */
    override internal function setVisible(value:Boolean, layerRefresh:Boolean = true):void
    {
        // Set the visible state of this item, but defer the folder refresh on the layer
        super.setVisible(value, false);

        if (layerRefresh)
        {
            if (layer.visible)
            {
                layer.setFolderVisibility(folder, value); // refresh the folder in the layer
            }
            else
            {
                layer.setFolderVisibility(folder, false);
            }
        }
    }
}

}
