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

import flash.events.Event;
import flash.events.EventDispatcher;

/**
 * The EventBus allows centralized communication among modules without
 * point-to-point messaging. It uses the singleton design pattern
 * to make sure one event bus is available globally. The bus itself
 * is only available to the container. Modules use the container's
 * static method to communicate with the event bus.
 */
public class EventBus extends EventDispatcher
{
    /** Application event bus instance */
    public static const instance:EventBus = new EventBus();

    /**
     * Normally the EventBus is not instantiated via the <b>new</b> method directly.
     * The constructor helps enforce only one EventBus available for the application
     * (singleton) so that it assures the communication only via a single event bus.
     */
    public function EventBus()
    {
    }

    /**
     * The factory method is used to create a instance of the EventBus. It returns
     * the only instance of EventBus and makes sure no another instance is created.
     */
    [Deprecated(replacement="instance")]
    public static function getInstance():EventBus
    {
        return instance;
    }

    /**
     * Basic dispatch function, dispatches simple named events. In the case
     * that the event is only significant by the event token (type string),
     * this new dispatch method simplify the code.
     */
    [Deprecated(replacement="AppEvent.dispatch")]
    public function dispatch(type:String):Boolean
    {
        return dispatchEvent(new Event(type));
    }
}

}
