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
package com.esri.viewer
{

import com.esri.ags.Map;
import com.esri.ags.symbols.Symbol;

import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.events.SecurityErrorEvent;

import mx.collections.ArrayCollection;
import mx.events.FlexEvent;
import mx.modules.Module;
import mx.resources.ResourceManager;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;
import mx.rpc.http.HTTPService;
import mx.utils.StringUtil;

[Event(name="widgetConfigLoaded", type="flash.events.Event")]
[Event(name="onChainRequest", type="com.esri.viewer.AppEvent")]

/**
 * BaseWidget is the foundation of all widgets. All widgets need to be derived from this BaseWidget class.
 *
 * <p><b>NOTE</b>: Once a new widget class is created by extending this BaseWidget class,
 * the developer is responsible for adding the new widget class to the Flash Builder project properties's module table.
 * This allows the new widget to be compiled into a separate SWF file.</p>
 */
public class BaseWidget extends Module implements IBaseWidget
{
    //--------------------------------------------------------------------------
    //
    //  Constants
    //
    //--------------------------------------------------------------------------

    private static const VIEWER_STRINGS:String = "ViewerStrings";

    /**
     * Indicates the widget config is loaded.
     */
    private static const WIDGET_CONFIG_LOADED:String = "widgetConfigLoaded";

    //--------------------------------------------------------------------------
    //
    //  Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * BaseWidget constructor.
     */
    public function BaseWidget()
    {
        super();
        this.autoLayout = true;

        addEventListener(FlexEvent.CREATION_COMPLETE, initWidgetTemplate);
        ViewerContainer.addEventListener(AppEvent.WIDGET_CHANGE_STATE, onChangeState);
    }

    //--------------------------------------------------------------------------
    //
    //  Variables
    //
    //--------------------------------------------------------------------------

    /**
     * The data structure that holds the configuration information parsed by the ConfigManager from config.xml.
     * A widget can access top level configuration information through this property.
     * The WidgetManager will set it when the widget is initialized.
     *
     * @see configData
     * @see ConfigManager
     */
    private var _configData:ConfigData;

    /**
     * Current active map that the container shows.
     * The WidgetManager will set its value when a widget is initialized.
     */
    private var _map:Map;

    /**
     * The default widget icon.
     */
    private var _widgetIcon:String = "assets/images/i_widget.png";

    private var _widgetTitle:String = "";

    [Bindable]
    private var _widgetId:Number;
    private var _widgetConfig:String;
    private var _widgetPreload:String;
    private var _widgetState:String;
    private var _widgetTemplate:IWidgetTemplate;

    private var _isDraggable:Boolean = true;
    private var _isResizeable:Boolean = true;
    private var _waitForCreationComplete:Boolean = true;

    //--------------------------------------------------------------------------
    //
    //  Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  configXML
    //----------------------------------

    private var _configXML:XML;

    /**
     * The XML type of configuration data.
     * @see configData
     */
    public function get configXML():XML
    {
        return _configXML;
    }

    /**
     * @private
     */
    public function set configXML(value:XML):void
    {
        _configXML = value;
    }

    //----------------------------------
    //  config
    //----------------------------------

    public function get config():String
    {
        return _widgetConfig;
    }

    /**
     * Set configuration file URL. A widget can have its own configuration file.
     * The URL of the widget configuration file is specified in the main config.xml.
     * The WidgetManager will pass the URL for the configuration file to the actual widget.
     *
     * @param value the configuration file URL.
     */
    public function set config(value:String):void
    {
        _widgetConfig = value;
    }

    //----------------------------------
    //  config data
    //----------------------------------

    public function get configData():ConfigData
    {
        return _configData;
    }

    /**
     * Pass in application level configuration data parsed from config.xml.
     *
     * @param value the configuration data structure object.
     * @see ConfigData
     */
    public function set configData(value:ConfigData):void
    {
        _configData = value;
    }

    //----------------------------------
    //  proxy url
    //----------------------------------

    public function get proxyUrl():String
    {
        return _configData ? _configData.proxyUrl : null;
    }

    //----------------------------------
    //  Map
    //----------------------------------

    [Bindable]
    /**
     * Set a map object reference. Used by WidgetManager to pass in the current map.
     *
     * @param value the map reference object.
     */
    public function get map():Map
    {
        return _map;
    }

    public function set map(value:Map):void
    {
        _map = value;
    }

    //----------------------------------
    //  isDraggable
    //----------------------------------

    public function get isDraggable():Boolean
    {
        return _isDraggable;
    }

    public function set isDraggable(value:Boolean):void
    {
        _isDraggable = value;
        setWidgetTemplateControl();
    }

    //----------------------------------
    //  isResizable
    //----------------------------------

    public function get isResizeable():Boolean
    {
        return _isResizeable;
    }

    public function set isResizeable(value:Boolean):void
    {
        _isResizeable = value;
        setWidgetTemplateControl();
    }

    //----------------------------------
    //  widget id
    //----------------------------------

    /**
     * Get the widget ID. A widget ID is an internally generated number.
     */
    public function get widgetId():Number
    {
        return _widgetId;
    }

    /**
     * Set the widget ID. A widget ID is an internally generated number.
     *
     * @param value the Number id.
     */
    public function set widgetId(value:Number):void
    {
        _widgetId = value;
    }

    //----------------------------------
    //  widget title
    //----------------------------------    

    public function get widgetTitle():String
    {
        return _widgetTitle;
    }

    /**
     * Set the widget title. A widget title can be configured in the config.xml.
     *
     * @param value the title text.
     */
    public function set widgetTitle(value:String):void
    {
        _widgetTitle = value;
    }

    //----------------------------------
    //  widget icon
    //----------------------------------

    public function get widgetIcon():String
    {
        return _widgetIcon;
    }

    /**
     * Set widget icon.
     * A widget icon is usually a JPG or PNG file in 40x40 size and set in the config.xml.
     *
     * @param value the icon URL.
     */
    public function set widgetIcon(value:String):void
    {
        _widgetIcon = value;
    }

    //--------------------------------------------------------------------------
    //
    //  Methods
    //
    //--------------------------------------------------------------------------    

    private function initWidgetTemplate(event:Event):void
    {
        var children:Array = this.getChildren();
        for each (var child:Object in children)
        {
            if (child is IWidgetTemplate)
            {
                _widgetTemplate = child as IWidgetTemplate;

                _widgetTemplate.baseWidget = this;

                if (_widgetState)
                {
                    _widgetTemplate.widgetState = _widgetState;
                }

                if (_widgetPreload == WidgetStates.WIDGET_MINIMIZED)
                {
                    _widgetTemplate.widgetState = WidgetStates.WIDGET_MINIMIZED;
                }
            }
        }
        if (_waitForCreationComplete) // by default wait for creationComplete before loading the config
        {
            configLoad();
        }
    }

    private function setWidgetTemplateControl():void
    {
        var children:Array = this.getChildren();
        for each (var child:Object in children)
        {
            if (child is IWidgetTemplate)
            {
                _widgetTemplate = child as IWidgetTemplate;
                _widgetTemplate.resizable = isResizeable;
                _widgetTemplate.draggable = isDraggable;
            }
        }
    }

    private function onChangeState(event:AppEvent):void
    {
        var data:Object = event.data;
        var reqId:Number = data.id as Number;
        var reqState:String = data.state as String;

        if (reqId == this.widgetId)
        {
            this.setState(reqState);
        }
    }

    /**
     * Set configuration file URL(infoconfig) for the info widget on operational layer.
     *
     * @param value the configuration file URL.
     * @param waitForCreationComplete specifies whether to wait for creationComplete before loading the config.
     */
    public function setInfoConfig(value:String, waitForCreationComplete:Boolean = true):void
    {
        this.config = value;

        _waitForCreationComplete = waitForCreationComplete;
        if (!_waitForCreationComplete)
        {
            configLoad(); //false for info widget, do not wait for creation complete to load the config
        }
    }

    /**
     * Set the widget preload.
     * @param value the preload string defined in BaseWidget.
     */
    public function setPreload(value:String):void
    {
        _widgetPreload = value;
    }

    /**
     * Set the widget state.
     * @param value the state string defined in BaseWidget.
     */
    public function setState(value:String):void
    {
        _widgetState = value;
        if (_widgetTemplate)
        {
            _widgetTemplate.widgetState = value;
        }
        notifyStateChanged(value);
    }

    private function notifyStateChanged(widgetState:String):void
    {
        var data:Object =
            {
                id: this._widgetId,
                state: widgetState
            };
        ViewerContainer.dispatchEvent(new AppEvent(AppEvent.WIDGET_STATE_CHANGED, data));
    }

    /**
     * Add information from a widget to the DataManager so that it can be shared between widgets.
     *
     * @param key the widget name
     * @param arrayCollection the list of object in infoData structure.
     */
    public function addSharedData(key:String, arrayCollection:ArrayCollection):void
    {
        var data:Object =
            {
                key: key,
                collection: arrayCollection
            };
        ViewerContainer.dispatchEvent(new AppEvent(AppEvent.DATA_PUBLISH, data));
    }

    /**
     * Fetch shared data from DataManager.
     */
    public function fetchSharedData():void
    {
        ViewerContainer.dispatchEvent(new AppEvent(AppEvent.DATA_FETCH_ALL));
    }

    /**
     * Show information window (infoWindow) based on infoData from widget.
     */
    public function showInfoWindow(infoData:Object):void
    {
        ViewerContainer.dispatchEvent(new AppEvent(AppEvent.SHOW_INFOWINDOW, infoData));
    }

    /**
     * Set map action from widget.
     */
    public function setMapAction(action:String, status:String, symbol:Symbol, callback:Function):void
    {
        var data:Object =
            {
                tool: action,
                status: status,
                symbol: symbol,
                handler: callback
            };
        ViewerContainer.dispatchEvent(new AppEvent(AppEvent.SET_MAP_ACTION, data));
    }

    /**
     * Set map navigation mode, such a pan, zoomin, etc.
     * <p>The navigation methods supported are:</p>
     * <listing>
     * pan          (Navigation.PAN)
     * zoomin       (Navigation.ZOOM_IN)
     * zoomout      (Navigation.ZOOM_OUT)
     * zoomfull     (ViewerContainer.NAVIGATION_ZOOM_FULL)
     * zoomprevious (ViewerContainer.NAVIGATION_ZOOM_PREVIOUS)
     * zoomnext     (ViewerContainer.NAVIGATION_ZOOM_NEXT)
     * </listing>
     */
    public function setMapNavigation(navMethod:String, status:String):void
    {
        var data:Object =
            {
                tool: navMethod,
                status: status
            };
        ViewerContainer.dispatchEvent(new AppEvent(AppEvent.SET_MAP_NAVIGATION, data));
    }

    /**
     * This will display an error message window.
     */
    public function showError(errorMessage:String):void
    {
        ViewerContainer.dispatchEvent(new AppEvent(AppEvent.APP_ERROR, errorMessage));
    }


    //config load
    private function configLoad():void
    {
        if (_widgetConfig)
        {
            var configService:HTTPService = new HTTPService();
            configService.url = _widgetConfig;
            configService.resultFormat = "e4x";
            configService.addEventListener(ResultEvent.RESULT, configResult);
            configService.addEventListener(FaultEvent.FAULT, configFault);
            configService.send();
        }
    }

    //config fault
    private function configFault(event:mx.rpc.events.FaultEvent):void
    {
        // happens if for example a widget file is missing or have crossdomain problem

        var sInfo:String = "";

        // Missing file
        if (event.fault.rootCause is IOErrorEvent)
        {
            var ioe:IOErrorEvent = event.fault.rootCause as IOErrorEvent;
            if (ioe.text.indexOf("2032: Stream Error. URL:") > -1)
            {
                sInfo += StringUtil.substitute(getDefaultString('missingConfigFile'), ioe.text.substring(32)) + "\n\n";
            }
            else
            {
                // some other IOError
                sInfo += event.fault.rootCause + "\n\n";
            }
        }

        // config file with crossdomain issue
        if (event.fault.rootCause is SecurityErrorEvent)
        {
            var sec:SecurityErrorEvent = event.fault.rootCause as SecurityErrorEvent;
            if (sec.text.indexOf("Error #2048: ") > -1)
            {
                sInfo += StringUtil.substitute(getDefaultString('configFileCrossDomain'), "\n", sec.text) + "\n\n";
            }
            else
            {
                // some other Security error
                sInfo += event.fault.rootCause + "\n\n";
            }
        }

        if (event.statusCode) // e.g. 404 - Not Found - http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
        {
            sInfo += StringUtil.substitute(getDefaultString('httpResponseStatus'), event.statusCode) + "\n\n";
        }

        // sInfo += "Event Target: " + event.target + "\n\n";
        // sInfo += "Event Type: " + event.type + "\n\n";
        sInfo += StringUtil.substitute(getDefaultString('faultCode'), event.fault.faultCode) + "\n\n";
        sInfo += StringUtil.substitute(getDefaultString('faultInfo'), event.fault.faultString) + "\n\n";
        sInfo += StringUtil.substitute(getDefaultString('faultDetail'), event.fault.faultDetail);

        showError(sInfo);
    }

    //config result
    private function configResult(event:ResultEvent):void
    {
        try
        {
            configXML = event.result as XML;
            dispatchEvent(new Event(WIDGET_CONFIG_LOADED));
        }
        catch (error:Error)
        {
            showError(StringUtil.substitute(getDefaultString("parseConfigError"), error.message));
        }
    }

    public function setXYPosition(x:Number, y:Number):void
    {
        this.setLayoutBoundsPosition(x, y);
    }

    public function setRelativePosition(left:String, right:String, top:String, bottom:String):void
    {
        if (left)
        {
            this.left = Number(left);
        }
        if (right)
        {
            this.right = Number(right);
        }
        if (top)
        {
            this.top = Number(top);
        }
        if (bottom)
        {
            this.bottom = Number(bottom);
        }
    }

    public function run():void
    {
        //TODO: need a better way of doing it
        //This is to disable a widget's move and resize when it's loaded as a controller
        isDraggable = true;
    }

    public function getDefaultString(resourceName:String):String
    {
        return resourceManager.getString(VIEWER_STRINGS, resourceName);
    }
}

}
