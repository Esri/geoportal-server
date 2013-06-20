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
package com.esri.viewer.utils
{

import flash.events.Event;
import flash.events.EventDispatcher;

import mx.utils.ObjectUtil;

public class ItemHistory extends EventDispatcher
{
    private const DEFAULT_MAX_ITEMS_TO_STORE:int = 50;

    private var maxItemsToStore:int = DEFAULT_MAX_ITEMS_TO_STORE;

    public function ItemHistory(maxItemsToStore:int = 50)
    {
        this.maxItemsToStore = maxItemsToStore;
    }

    private var _items:Array = [];

    [Bindable("itemsChanged")]
    public function get items():Array
    {
        return _items.concat();
    }

    public function importHistory(value:Array):void
    {
        if (value)
        {
            _items = value;
            dispatchEvent(new Event("itemsChanged"));
        }
    }

    public function exportHistory():Array
    {
        return _items.concat();
    }

    public function addItem(item:*):void
    {
        if (isItemUnique(item))
        {
            addItemAndEnforceMaxLength(item);
        }
        else
        {
            promoteItemToRecentlyUsed(item);
        }
    }

    private function isItemUnique(item:*):Boolean
    {
        for each (var existingItem:* in _items)
        {
            if (equalsFunction(item, existingItem))
            {
                return false;
            }
        }

        return true;
    }

    public var equalsFunction:Function = defaultEqualsFunction;

    private function defaultEqualsFunction(item1:*, item2:*):Boolean
    {
        return ObjectUtil.compare(item1, item2) == 0;
    }

    private function addItemAndEnforceMaxLength(item:*):void
    {
        _items.unshift(item);
        if (_items.length > maxItemsToStore)
        {
            _items.pop();
        }
        dispatchEvent(new Event("itemsChanged"));
    }

    private function promoteItemToRecentlyUsed(item:*):void
    {
        var matchingItem:* = getMatchingItem(item);
        var itemIndex:int = _items.indexOf(matchingItem);
        _items.splice(itemIndex, 1);
        _items.unshift(matchingItem);
        dispatchEvent(new Event("itemsChanged"));
    }

    private function getMatchingItem(item:*):*
    {
        var matchingItem:*;

        for each (var existingItem:* in _items)
        {
            if (equalsFunction(item, existingItem))
            {
                matchingItem = existingItem;
                break;
            }
        }

        return matchingItem;
    }

    public function mostRecentItem():*
    {
        return _items ? _items[0] : null;
    }
}
}
