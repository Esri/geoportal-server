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
import com.esri.ags.layers.supportClasses.KMLFeatureInfo;
import com.esri.ags.layers.supportClasses.KMLFolder;

/**
 * A TOC item representing a NetworkLink within a KML Layer.
 *
 * @private
 */
public class TocKmlNetworkLinkItem extends TocItem
{
    public function TocKmlNetworkLinkItem(parentItem:TocItem, networkLink:KMLLayer, layer:KMLLayer)
    {
        super(parentItem);

        _networkLink = networkLink;
        _layer = layer;
        label = networkLink.name;

        setVisible(networkLink.visible, false);
    }

    //--------------------------------------------------------------------------
    //  Property:  folder
    //--------------------------------------------------------------------------

    private var _networkLink:KMLLayer;

    /**
     * The KML Folder that represents this TOC item.
     */
    public function get networkLink():KMLLayer
    {
        return _networkLink;
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
            networkLink.visible = value;
            if (value)
            {
                if (parent is TocKmlFolderItem)
                {
                    networkLink.visible = isNetworkLinkVisibileBasedOnParentFolder();
                }
            }
        }
    }

    private function isNetworkLinkVisibileBasedOnParentFolder():Boolean
    {
        var result:Boolean;

        // find the immediate parent folder
        var parentFolder:KMLFolder = TocKmlFolderItem(parent).folder;
        result = parentFolder.visible;
        if (parentFolder.visible)
        {
            var parents:Array = getParentFolders(parentFolder);
            if (parents.length > 0)
            {
                for (var p:int = 0; p < parents.length; )
                {
                    if (!KMLFolder(parents[p]).visible)
                    {
                        result = false;
                        break;
                    }
                    else
                    {
                        p++;
                    }
                }
            }
        }
        return result;
    }

    private function getParentFolders(folder:KMLFolder, arr:Array = null):Array
    {
        if (!arr)
        {
            arr = [];
        }

        // Returns the parent folders ids of the given folder
        var parentId:Number = folder.parentFolderId;

        if (parentId != -1)
        {
            var kmlFeatureInfo:KMLFeatureInfo = new KMLFeatureInfo;
            kmlFeatureInfo.type = KMLFeatureInfo.FOLDER;
            kmlFeatureInfo.id = parentId;

            var parentFolder:KMLFolder = layer.getFeature(kmlFeatureInfo) as KMLFolder;

            arr.push(parentFolder);
            return getParentFolders(parentFolder, arr);
        }
        return arr;
    }
}

}
