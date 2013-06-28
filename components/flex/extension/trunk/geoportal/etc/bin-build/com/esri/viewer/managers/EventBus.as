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
package com.esri.viewer.managers
{

import flash.events.Event;
import flash.events.EventDispatcher;

/**
 * The EventBus allows centrallized communication among modules without
 * point-to-point messaging. It uses the singleton design pattern
 * to make sure one event bus is available globally. The bus itself
 * is only available to the container. Modules use the container's
 * static method to communicate with the event bus.
 */
public class EventBus extends EventDispatcher
{
    /** Application event buss instance */
    private static var _eventBus:EventBus;

    /** Lock to enforce singleton */
    private static var lock:Boolean = false;

    /**
     * Normally the EventBus is not instantiated via the <b>new</b> method directly.
     * The constructor helps enforce only one EvenBus availiable for the application
     * (singeton) so that it asures the communication only via a sigle event bus.
     */
    public function EventBus()
    {
        if (!lock)
        {
            throw new Error("ContainerEventDispatcher can only be defined once!");
        }
    }

    /**
     * The factory method is used to create a instance of the EventBus. It returns
     * the only instanace of EventBus and makes sure no another instance is created.
     */
    public static function getInstance():EventBus
    {
        if (_eventBus == null)
        {
            lock = true;
            _eventBus = new EventBus();
            lock = false;
        }
        return _eventBus;
    }


    /**
     * Basic dispatch function, dispatches simple named events. In the case
     * that the event is only significant by the event token (type string),
     * this new dispatch method simplify the code.
     */
    public function dispatch(type:String):Boolean
    {
        return dispatchEvent(new Event(type));
    }
}

}
