////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2010 ESRI
//
// All rights reserved under the copyright laws of the United States.
// You may freely redistribute and use this software, with or
// without modification, provided you include the original copyright
// and use restrictions.  See use restrictions in the file:
// <install location>/License.txt
//
////////////////////////////////////////////////////////////////////////////////
package com.esri.viewer.components.toc.tocClasses
{

import com.esri.ags.layers.TiledMapServiceLayer;
import com.esri.viewer.components.toc.controls.CheckBoxIndeterminate;

import flash.events.MouseEvent;

import mx.controls.treeClasses.TreeItemRenderer;
import mx.controls.treeClasses.TreeListData;

/**
 * A custom tree item renderer for a map Table of Contents.
 */
public class TocItemRenderer extends TreeItemRenderer
{
    // Renderer UI components
    private var _checkbox:CheckBoxIndeterminate;

    // UI component spacing
    private static const PRE_CHECKBOX_GAP:Number = 5;

    private static const POST_CHECKBOX_GAP:Number = 4;

    /**
     * @private
     */
    override protected function createChildren():void
    {
        super.createChildren();

        // Create a checkbox child component for toggling layer visibility.
        if (!_checkbox)
        {
            _checkbox = new CheckBoxIndeterminate();
            _checkbox.addEventListener(MouseEvent.CLICK, onCheckBoxClick);
            _checkbox.addEventListener(MouseEvent.DOUBLE_CLICK, onCheckBoxDoubleClick);
            _checkbox.addEventListener(MouseEvent.MOUSE_DOWN, onCheckBoxMouseDown);
            _checkbox.addEventListener(MouseEvent.MOUSE_UP, onCheckBoxMouseUp);
            addChild(_checkbox);
        }
    }

    /**
     * @private
     */
    override protected function commitProperties():void
    {
        super.commitProperties();

        if (data is TocItem)
        {
            var item:TocItem = TocItem(data);

            // Set the checkbox state
            _checkbox.indeterminate = item.indeterminate;
            // The indeterminate state has visual priority over the selected state
            _checkbox.selected = item.visible && !item.indeterminate;

            // Hide the checkbox for child items of tiled map services
            var checkboxVisible:Boolean = true;
            if (isTiledLayerChild(item))
            {
                checkboxVisible = false;
            }
            _checkbox.visible = checkboxVisible;

            // Apply a bold label style to root nodes
            if (item.isTopLevel())
            {
                setStyle("fontWeight", "bold");
            }
            else
            {
                setStyle("fontWeight", "normal");
            }
        }
    }

    /**
     * @private
     */
    override protected function measure():void
    {
        super.measure();

        // Add space for the checkbox and gaps
        if (isNaN(explicitWidth) && !isNaN(measuredWidth))
        {
            var w:Number = measuredWidth;
            w += _checkbox.measuredWidth;
            w += PRE_CHECKBOX_GAP + POST_CHECKBOX_GAP;
            measuredWidth = w;
        }
    }

    /**
     * @private
     */
    override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
    {
        super.updateDisplayList(unscaledWidth, unscaledHeight);

        var startx:Number = data ? TreeListData(listData).indent : 0;
        if (icon)
        {
            startx = icon.x;
        }
        else if (disclosureIcon)
        {
            startx = disclosureIcon.x + disclosureIcon.width;
        }
        startx += PRE_CHECKBOX_GAP;

        // Position the checkbox between the disclosure icon and the item icon
        _checkbox.x = startx;
        _checkbox.setActualSize(_checkbox.measuredWidth, _checkbox.measuredHeight);
        _checkbox.y = (unscaledHeight - _checkbox.height) / 2;
        startx = _checkbox.x + _checkbox.width + POST_CHECKBOX_GAP;

        if (icon)
        {
            icon.x = startx;
            startx = icon.x + icon.width;
        }

        label.x = startx;
        label.setActualSize(unscaledWidth - startx, measuredHeight);
    }

    /**
     * Whether the specified TOC item is a child of a tiled map service layer.
     */
    private function isTiledLayerChild(item:TocItem):Boolean
    {
        while (item)
        {
            item = item.parent;
            if (item is TocMapLayerItem)
            {
                if (TocMapLayerItem(item).layer is TiledMapServiceLayer)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Updates the visible property of the underlying TOC item.
     */
    private function onCheckBoxClick(event:MouseEvent):void
    {
        event.stopPropagation();

        if (data is TocItem)
        {
            var item:TocItem = TocItem(data);
            item.visible = _checkbox.selected;
        }
    }

    private function onCheckBoxDoubleClick(event:MouseEvent):void
    {
        event.stopPropagation();
    }

    private function onCheckBoxMouseDown(event:MouseEvent):void
    {
        event.stopPropagation();
    }

    private function onCheckBoxMouseUp(event:MouseEvent):void
    {
        event.stopPropagation();
    }
}

}
