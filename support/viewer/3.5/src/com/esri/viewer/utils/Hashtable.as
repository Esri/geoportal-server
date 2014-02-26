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

import flash.utils.Dictionary;

public class Hashtable
{
    private var keys:Dictionary;
    private var dups:Dictionary;
    private var initSize:int;
    private var maxSize:int;
    private var hashSize:int;
    private var item:Object = { key: null, obj: null, prev: null, next: null };
    private var headItem:Object = { key: null, obj: null, prev: null, next: null };
    private var tailItem:Object = { key: null, obj: null, prev: null, next: null };

    /**
     * Initializes a new Hashtable instance.
     *
     * @param size The initial size of the Hashtable.
     */
    public function Hashtable(size:int = 500)
    {
        initSize = maxSize = Math.max(10, size);

        keys = new Dictionary(true);
        dups = new Dictionary(true);
        hashSize = 0;

        var node:Object = { key: null, obj: null, prev: null, next: null };
        headItem = tailItem = node;

        var k:int = initSize + 1;
        for (var i:int = 0; i < k; i++)
        {
            node.next = { key: null, obj: null, prev: null, next: null };
            node = node.next;
        }
        tailItem = node;
    }

    /**
     * Add a key/data couple into the table.
     *
     * @param key The key.
     * @param obj The data associated with the key.
     */
    public function add(key:*, obj:*):Boolean
    {
        if (key == null)
        {
            return false;
        }
        if (obj == null)
        {
            return false;
        }
        if (keys[key])
        {
            return false;
        }

        if (hashSize++ == maxSize)
        {
            var k:int = (maxSize += initSize) + 1;
            for (var i:int = 0; i < k; i++)
            {
                tailItem.next = { key: null, obj: null, prev: null, next: null };
                tailItem = tailItem.next;
            }
        }

        var pair:Object = { key: null, obj: null, prev: null, next: null };
        headItem = headItem.next;
        pair.key = key;
        pair.obj = obj;

        pair.next = pair;
        if (pair)
        {
            pair.prev = pair;
        }
        pair = pair;

        keys[key] = pair;
        dups[obj] ? dups[obj]++ : dups[obj] = 1;

        return true;
    }

    /**
     * Finds the value that is associated with the given key.
     *
     * @param  key The key mapping a value.
     * @return The data associated with the key or null if no matching
     *         entry was found.
     */
    public function find(key:*):*
    {
        var pair:Object = keys[key];
        if (pair)
        {
            return pair.obj;
        }
        return null;
    }

    /**
     * Removes a value based on a given key.
     *
     * @param  key The entry's key.
     * @return The data associated with the key or null if no matching
     *         entry was found.
     */
    public function remove(key:*):*
    {
        var pair:Object = keys[key];
        if (pair)
        {
            var obj:* = pair.obj;

            delete keys[key];

            if (pair.prev)
            {
                pair.prev.next = pair.next;
            }
            if (pair.next)
            {
                pair.next.prev = pair.prev;
            }
            if (pair == pair)
            {
                pair = pair.next;
            }

            pair.prev = null;
            pair.next = null;
            tailItem.next = pair;
            tailItem = pair;

            if (--dups[obj] <= 0)
            {
                delete dups[obj];
            }

            if (--hashSize <= (maxSize - initSize))
            {
                var k:int = (maxSize -= initSize) + 1;
                for (var i:int = 0; i < k; i++)
                {
                    headItem = headItem.next;
                }
            }

            return obj;
        }
        return null;
    }

    /**
     * Checks if a mapping exists for the given key.
     *
     * @return True if key exists, otherwise false.
     */
    public function containsKey(key:*):Boolean
    {
        return keys[key] != undefined;
    }

    /**
     * Writes all keys into an array.
     *
     * @return An array containing all keys.
     */
    public function getKeySet():Array
    {
        var a:Array = new Array(hashSize), i:int;
        for each (var p:Object in keys)
        {
            a[i++] = p.key;
        }
        return a;
    }

    /**
     * @inheritDoc
     */
    public function contains(obj:*):Boolean
    {
        return dups[obj] > 0;
    }

    /**
     * @inheritDoc
     */
    public function clear():void
    {
        keys = new Dictionary(true);
        dups = new Dictionary(true);

        var t:Object;
        var n:Object = item;
        while (n)
        {
            t = n.next;

            n.next = n.prev = null;
            n.key = null;
            n.obj = null;
            tailItem.next = n;
            tailItem = tailItem.next;

            n = t;
        }

        item = null;
        hashSize = 0;
    }


    /**
     * @inheritDoc
     */
    public function get size():int
    {
        return hashSize;
    }

    /**
     * @inheritDoc
     */
    public function isEmpty():Boolean
    {
        return hashSize == 0;
    }

    /**
     * @inheritDoc
     */
    public function toArray():Array
    {
        var a:Array = new Array(hashSize), i:int;
        for each (var p:Object in keys)
        {
            a[i++] = p.obj;
        }
        return a;
    }

    /**
     * Prints out a string representing the current object.
     *
     * @return A string representing the current object.
     */
    public function toString():String
    {
        return "[Hashtable, size=" + size + "]";
    }
}

}
