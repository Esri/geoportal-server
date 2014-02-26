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

import flash.events.Event;
import flash.events.EventDispatcher;

import mx.collections.ArrayCollection;
import mx.events.PropertyChangeEvent;
import mx.utils.ObjectUtil;

/**
 * The base TOC item.
 *
 * @private
 */
public class TocItem extends EventDispatcher
{
    //--------------------------------------------------------------------------
    //
    //  Constructor
    //
    //--------------------------------------------------------------------------

    public function TocItem(parentItem:TocItem = null)
    {
        _parent = parentItem;
    }

    //--------------------------------------------------------------------------
    //
    //  Properties
    //
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //  parent
    //--------------------------------------------------------------------------

    private var _parent:TocItem;

    /**
     * The parent TOC item of this item.
     */
    public function get parent():TocItem
    {
        return _parent;
    }

    //--------------------------------------------------------------------------
    //  children
    //--------------------------------------------------------------------------

    [Bindable]
    /**
     * The child items of this TOC item.
     */
    public var children:ArrayCollection; // of TocItem

    /**
     * Adds a child TOC item to this item.
     */
    internal function addChild(item:TocItem):void
    {
        if (!children)
        {
            children = new ArrayCollection();
        }
        children.addItem(item);
        getTopMostTocItem().dispatchEvent(new Event(Event.CHANGE));
    }

    //--------------------------------------------------------------------------
    //  label
    //--------------------------------------------------------------------------

    internal static const DEFAULT_LABEL:String = "(?)";

    private var _label:String = DEFAULT_LABEL;

    [Bindable("propertyChange")]
    /**
     * The text label for the item renderer.
     */
    public function get label():String
    {
        return _label;
    }

    /**
     * @private
     */
    public function set label(value:String):void
    {
        var oldValue:Object = _label;
        _label = (value ? value : DEFAULT_LABEL);

        // Dispatch a property change event to notify the item renderer
        dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "label", oldValue, _label));
    }

    //--------------------------------------------------------------------------
    //  visible
    //--------------------------------------------------------------------------

    internal static const DEFAULT_VISIBLE:Boolean = true;

    private var _visible:Boolean = DEFAULT_VISIBLE;

    [Bindable("propertyChange")]
    /**
     * Whether the map layer referred to by this TOC item is visible or not.
     */
    public function get visible():Boolean
    {
        return _visible;
    }

    /**
     * @private
     */
    public function set visible(value:Boolean):void
    {
        setVisible(value, true);
    }

    internal static const DEFAULT_IS_IN_SCALE_RANGE:Boolean = true;

    private var _isInScaleRange:Boolean = DEFAULT_IS_IN_SCALE_RANGE;

    [Bindable("propertyChange")]
    /**
     * Whether the map layer referred to by this TOC item is enabled or not.
     */
    public function get isInScaleRange():Boolean
    {
        return _isInScaleRange;
    }

    /**
     * @private
     */
    public function set isInScaleRange(value:Boolean):void
    {
        setIsInScaleRange(value, true);
    }

    /**
     * Allows subclasses to change the visible state without causing a layer refresh.
     */
    internal function setVisible(value:Boolean, layerRefresh:Boolean = true):void
    {
        if (value != _visible)
        {
            var oldValue:Object = _visible;
            _visible = value;

            updateIndeterminateState();
            if (layerRefresh)
            {
                refreshLayer();
            }

            // Dispatch a property change event to notify the item renderer
            dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "visible", oldValue, value));
        }
    }

    /**
     * Allows subclasses to change the isInScaleRange state without causing a layer refresh.
     */
    internal function setIsInScaleRange(value:Boolean, layerRefresh:Boolean = true):void
    {
        if (value != _isInScaleRange)
        {
            var oldValue:Object = _isInScaleRange;
            _isInScaleRange = value;

            if (layerRefresh)
            {
                refreshLayer();
            }

            // Dispatch a property change event to notify the item renderer
            dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "isInScaleRange", oldValue, value));
        }
    }

    private function setVisibleDirect(value:Boolean):void
    {
        if (value != _visible)
        {
            var oldValue:Object = _visible;
            _visible = value;

            // Dispatch a property change event to notify the item renderer
            dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "visible", oldValue, value));
        }
    }

    //--------------------------------------------------------------------------
    //  indeterminate
    //--------------------------------------------------------------------------

    internal static const DEFAULT_INDETERMINATE:Boolean = false;

    private var _indeterminate:Boolean = DEFAULT_INDETERMINATE;

    [Bindable("propertyChange")]
    /**
     * Whether the visibility of this TOC item is in a mixed state,
     * based on child item visibility or other criteria.
     */
    public function get indeterminate():Boolean
    {
        return _indeterminate;
    }

    /**
     * @private
     */
    public function set indeterminate(value:Boolean):void
    {
        if (value != _indeterminate)
        {
            var oldValue:Object = _indeterminate;
            _indeterminate = value;

            // Dispatch a property change event to notify the item renderer
            dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "indeterminate", oldValue, value));
        }
    }

    //--------------------------------------------------------------------------
    //
    //  Overridden Methods
    //
    //--------------------------------------------------------------------------

    override public function toString():String
    {
        return ObjectUtil.toString(this);
    }

    //--------------------------------------------------------------------------
    //
    //  Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Whether this TOC item is at the root level.
     */
    public function isTopLevel():Boolean
    {
        return _parent == null;
    }

    /**
     * Whether this TOC item contains any child items.
     */
    public function isGroupLayer():Boolean
    {
        return children && children.length > 0
            && !(children.getItemAt(0) is TocLegendItem);
    }

    /**
     * Gets top-most TOC item.
     */
    protected function getTopMostTocItem():TocItem
    {
        var topMostTocItem:TocItem;
        var currentTocItem:TocItem = this;
        while (currentTocItem != null)
        {
            topMostTocItem = currentTocItem;
            currentTocItem = currentTocItem.parent;
        }
        return topMostTocItem;
    }

    /**
     * Updates the indeterminate visibility state of this TOC item.
     */
    internal function updateIndeterminateState(calledFromChild:Boolean = false):void
    {
        // Recurse up the tree
        if (parent)
        {
            parent.updateIndeterminateState(true);
        }
    }

    /**
     * Invalidates any map layer that is associated with this TOC item.
     */
    internal function refreshLayer():void
    {
        // Recurse up the tree
        if (parent)
        {
            parent.refreshLayer();
        }
    }
}

}
