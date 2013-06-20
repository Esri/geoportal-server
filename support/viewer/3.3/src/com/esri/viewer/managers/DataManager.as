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
package com.esri.viewer.managers
{

import com.esri.viewer.utils.Hashtable;

import flash.events.EventDispatcher;

import mx.collections.ArrayCollection;
import com.esri.viewer.AppEvent;
import com.esri.viewer.ViewerContainer;

/**
 * Data manager will be storing the session data to support data reuse such as widget chain.
 */
public class DataManager extends EventDispatcher
{
    private var dataTable:Hashtable;

    public function DataManager()
    {
        super();

        dataTable = new Hashtable();
        AppEvent.addListener(AppEvent.CONFIG_LOADED, configLoadedHandler);

        // This is an example to set up the listener to get the type of data the Data
        // Manager is interested in.
        AppEvent.addListener(AppEvent.DATA_FETCH_ALL, fetchAllData);
        AppEvent.addListener(AppEvent.DATA_PUBLISH, addData);
        AppEvent.addListener(AppEvent.DATA_FETCH, fetchData);
    }

    private function configLoadedHandler(event:AppEvent):void
    {
    }

    private function fetchAllData(event:AppEvent):void
    {
        AppEvent.dispatch(AppEvent.DATA_SENT, dataTable);
    }

    private function fetchData(event:AppEvent):void
    {
        var key:String = event.data.key as String;
        var data:Object =
            {
                key: key,
                collection: dataTable.find(key)
            };
        AppEvent.dispatch(AppEvent.DATA_SENT, data);
    }

    private function addData(event:AppEvent):void
    {
        var key:String = event.data.key;
        if (key)
        {
            var dataCollection:Object = event.data.collection;
            if (dataTable.containsKey(key))
            {
                dataTable.remove(key);
            }
            dataTable.add(key, dataCollection);

            var data:Object =
                {
                    key: key,
                    data: dataTable
                };
            AppEvent.dispatch(AppEvent.DATA_NEW_PUBLISHED, data);
        }
    }
}

}
