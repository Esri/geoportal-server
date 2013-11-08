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
package com.esri.viewer
{

import com.esri.viewer.components.FocusableImage;
import com.esri.viewer.components.TitlebarButton;
import com.esri.viewer.utils.LocalizationUtil;

import flash.events.Event;
import flash.events.MouseEvent;

import mx.controls.Image;
import mx.core.FlexGlobals;
import mx.core.UIComponent;
import mx.events.FlexEvent;
import mx.managers.CursorManager;
import mx.managers.DragManager;

import spark.components.Application;
import spark.components.Group;
import spark.components.SkinnableContainer;

[Event(name="open", type="flash.events.Event")]
[Event(name="minimized", type="flash.events.Event")]
[Event(name="closed", type="flash.events.Event")]
[Event(name="startDrag", type="flash.events.Event")]
[Event(name="stopDrag", type="flash.events.Event")]

[SkinState("open")]
[SkinState("minimized")]
[SkinState("closed")]

public class WidgetTemplate extends SkinnableContainer implements IWidgetTemplate
{
    [SkinPart(required="false")]
    public var widgetFrame:Group;

    [SkinPart(required="false")]
    public var header:Group;

    [SkinPart(required="false")]
    public var headerToolGroup:Group;

    [SkinPart(required="false")]
    public var icon:FocusableImage;

    [SkinPart(required="false")]
    public var closeButton:FocusableImage;

    [SkinPart(required="false")]
    public var minimizeButton:FocusableImage;

    [SkinPart(required="false")]
    public var resizeButton:Image;

    [Bindable]
    public var enableCloseButton:Boolean = true;

    [Bindable]
    public var enableMinimizeButton:Boolean = true;

    [Bindable]
    public var enableDraging:Boolean = true;

    [Bindable]
    public var widgetWidth:Number;

    [Bindable]
    public var widgetHeight:Number;

    [Embed(source="/assets/images/w_resizecursor.png")]
    public var resizeCursor:Class;

    [Embed(source="/assets/images/w_resizecursor_rtl.png")]
    public var resizeCursor_rtl:Class;

    [Bindable]
    public var enableIcon:Boolean = true;

    private static const WIDGET_OPENED:String = "open";

    private static const WIDGET_MINIMIZED:String = "minimized";

    private static const WIDGET_CLOSED:String = "closed";

    private static const WIDGET_START_DRAG:String = "startDrag";

    private static const WIDGET_STOP_DRAG:String = "stopDrag";

    private static const DEFAULT_MIN_WIDTH:Number = 150;

    private static const DEFAULT_MIN_HEIGHT:Number = 75;

    private var _widgetId:Number;

    private var _widgetState:String;

    private var _cursorID:int = 0;

    private var _widgetTitle:String = "";

    private var _widgetIcon:String = "assets/images/i_widget.png";

    [Bindable]
    private var _draggable:Boolean = true;

    private var _resizable:Boolean = true;

    private var _baseWidget:IBaseWidget;

    private var _isPartOfPanel:Boolean;

    [Bindable(event="isPartOfPanelChanged")]
    public function get isPartOfPanel():Boolean
    {
        return _isPartOfPanel;
    }

    public function set baseWidget(value:IBaseWidget):void
    {
        _baseWidget = value;
        _isPartOfPanel = value.isPartOfPanel;
        dispatchEvent(new Event("isPartOfPanelChanged"));

        if (_isPartOfPanel)
        {
            this.enableIcon = this.enableCloseButton = this.enableMinimizeButton = this.enableDraging = false;
        }
        this.resizable = value.isResizeable;
        this.draggable = value.isDraggable;
        this.widgetId = value.widgetId;
        this.widgetTitle = value.widgetTitle;
        this.widgetIcon = value.widgetIcon;
        if (value.initialWidth != 0)
        {
            if (this.minWidth == 0 || isNaN(this.minWidth) || value.initialWidth > this.minWidth)
            {
                this.width = value.initialWidth;
                this.widgetWidth = value.initialWidth;
            }
        }
        if (value.initialHeight != 0)
        {
            if (this.minHeight == 0 || isNaN(this.minHeight) || value.initialHeight > this.minHeight)
            {
                this.height = value.initialHeight;
                this.widgetHeight = value.initialHeight;
            }
        }
    }

    public function get baseWidget():IBaseWidget
    {
        return _baseWidget;
    }

    public function set resizable(value:Boolean):void
    {
        _resizable = value;
        resizeButton.visible = _resizable;
    }

    [Bindable]
    public function get resizable():Boolean
    {
        return _resizable;
    }

    public function set draggable(value:Boolean):void
    {
        if (enableDraging)
        {
            _draggable = value;
        }
        else
        {
            _draggable = false;
        }
    }

    public function get widgetId():Number
    {
        return _widgetId;
    }

    public function set widgetId(value:Number):void
    {
        _widgetId = value;
    }

    [Bindable]
    public function get widgetTitle():String
    {
        return _widgetTitle;
    }

    public function set widgetTitle(value:String):void
    {
        _widgetTitle = value;
    }

    [Bindable]
    public function get widgetIcon():String
    {
        return _widgetIcon;
    }

    public function set widgetIcon(value:String):void
    {
        _widgetIcon = value;
    }

    public function set widgetState(value:String):void
    {
        this.widgetFrame.toolTip = "";
        this.icon.toolTip = "";
        _widgetState = value;
        if (_widgetState == WIDGET_MINIMIZED)
        {
            this.widgetFrame.toolTip = this.widgetTitle;
            this.icon.toolTip = this.widgetTitle;
        }
        invalidateSkinState();

        dispatchEvent(new Event(value));
    }

    public function get widgetState():String
    {
        return _widgetState;
    }

    private var _selectedTitlebarButtonIndex:int = -1;

    private var _selectedTitlebarButtonIndexChanged:Boolean = false;

    public function get selectedTitlebarButtonIndex():int
    {
        return _selectedTitlebarButtonIndex;
    }

    public function set selectedTitlebarButtonIndex(value:int):void
    {
        if (_selectedTitlebarButtonIndex != value)
        {
            _selectedTitlebarButtonIndex = value;
            _selectedTitlebarButtonIndexChanged = true;
            invalidateProperties();
        }
    }

    public function WidgetTemplate()
    {
        super();

        this.width = 300;
        this.height = 300;

        this.hasFocusableChildren = true;

        this.addEventListener(FlexEvent.CREATION_COMPLETE, creationCompleteHandler);
    }

    private function creationCompleteHandler(event:Event):void
    {
        widgetWidth = width;
        widgetHeight = height;

        this.closeButton.toolTip = LocalizationUtil.getDefaultString("close");
        this.minimizeButton.toolTip = LocalizationUtil.getDefaultString("minimize");
    }

    protected override function partAdded(partName:String, instance:Object):void
    {
        super.partAdded(partName, instance);
        if (instance == icon)
        {
            icon.addEventListener(MouseEvent.CLICK, icon_clickHandler);
        }
        if (instance == widgetFrame)
        {
            widgetFrame.addEventListener(MouseEvent.MOUSE_DOWN, mouse_downHandler);
            widgetFrame.addEventListener(MouseEvent.MOUSE_UP, mouse_upHandler);

            widgetFrame.stage.addEventListener(MouseEvent.MOUSE_UP, mouse_upHandler);
            widgetFrame.stage.addEventListener(Event.MOUSE_LEAVE, stageout_Handler);
        }
        if (instance == header)
        {
            header.addEventListener(MouseEvent.MOUSE_DOWN, mouse_downHandler);
            header.addEventListener(MouseEvent.MOUSE_UP, mouse_upHandler);
        }
        if (instance == closeButton)
        {
            closeButton.addEventListener(MouseEvent.CLICK, close_clickHandler);
        }
        if (instance == minimizeButton)
        {
            minimizeButton.addEventListener(MouseEvent.CLICK, minimize_clickHandler);
        }
        if (instance == resizeButton)
        {
            resizeButton.addEventListener(MouseEvent.MOUSE_OVER, resize_overHandler);
            resizeButton.addEventListener(MouseEvent.MOUSE_OUT, resize_outHandler);
            resizeButton.addEventListener(MouseEvent.MOUSE_DOWN, resize_downHandler);
        }
    }

    override protected function getCurrentSkinState():String
    {
        return _widgetState;
    }

    override protected function commitProperties():void
    {
        super.commitProperties();

        if (_selectedTitlebarButtonIndexChanged)
        {
            _selectedTitlebarButtonIndexChanged = false;
            for (var i:int = 0, n:int = headerToolGroup.numElements; i < n; i++)
            {
                var btn:TitlebarButton = TitlebarButton(headerToolGroup.getElementAt(i));
                if (i == _selectedTitlebarButtonIndex)
                {
                    btn.selected = true;
                }
                else
                {
                    btn.selected = false;
                }
            }
        }
    }

    public function mouse_downHandler(event:MouseEvent):void
    {
        if (_draggable && enableDraging)
        {
            header.addEventListener(MouseEvent.MOUSE_MOVE, mouse_moveHandler);
            widgetFrame.addEventListener(MouseEvent.MOUSE_MOVE, mouse_moveHandler);
        }
    }

    private var widgetMoveStarted:Boolean = false;
    private var xResizeStart:Number;
    private var yResizeStart:Number;

    private function mouse_moveHandler(event:MouseEvent):void
    {
        if (!widgetMoveStarted)
        {
            widgetMoveStarted = true;

            //TODO: not for V2.0
            //AppEvent.dispatch(AppEvent.CHANGE_LAYOUT, LAYOUT_BASIC));
            if (_widgetState != WIDGET_MINIMIZED)
            {
                this.alpha = 0.7;
            }
            var widget:UIComponent = parent as UIComponent;

            if (!DragManager.isDragging)
            {
                widget.startDrag();
                dispatchEvent(new Event(WIDGET_START_DRAG));
            }

            if (_resizable)
            {
                AppEvent.dispatch(AppEvent.WIDGET_FOCUS, widgetId);
            }
        }
    }

    private function mouse_upHandler(event:MouseEvent):void
    {
        header.removeEventListener(MouseEvent.MOUSE_MOVE, mouse_moveHandler);
        widgetFrame.removeEventListener(MouseEvent.MOUSE_MOVE, mouse_moveHandler);

        if (_widgetState != WIDGET_MINIMIZED)
        {
            this.alpha = 1;
        }
        var widget:UIComponent = parent as UIComponent;

        widget.stopDrag();
        dispatchEvent(new Event(WIDGET_STOP_DRAG));

        var appHeight:Number = FlexGlobals.topLevelApplication.height;
        var appWidth:Number = FlexGlobals.topLevelApplication.width;

        if (widget.y < 0)
        {
            widget.y = 0;
        }
        if (widget.y > (appHeight - 40))
        {
            widget.y = appHeight - 40;
        }
        if (widget.x < 0)
        {
            widget.x = 20;
        }

        if (widget.x > (appWidth - 40))
        {
            widget.x = appWidth - 40;
        }

        // clear constraints since x and y have been set
        widget.left = widget.right = widget.top = widget.bottom = undefined;

        widgetMoveStarted = false;
    }

    private function stageout_Handler(event:Event):void
    {
        if (widgetMoveStarted)
        {
            mouse_upHandler(null);
        }
    }

    protected function icon_clickHandler(event:MouseEvent):void
    {
        if (_baseWidget)
        {
            _baseWidget.setState(WIDGET_OPENED);
        }

        this.widgetFrame.toolTip = "";
        this.icon.toolTip = "";
    }

    protected function close_clickHandler(event:MouseEvent):void
    {
        if (_baseWidget)
        {
            _baseWidget.setState(WIDGET_CLOSED);
        }
    }

    protected function minimize_clickHandler(event:MouseEvent):void
    {
        if (_baseWidget)
        {
            _baseWidget.setState(WIDGET_MINIMIZED);
        }

        this.widgetFrame.toolTip = this.widgetTitle;
        this.icon.toolTip = this.widgetTitle;
    }

    private function resize_overHandler(event:MouseEvent):void
    {
        if (isRtl())
        {
            _cursorID = CursorManager.setCursor(resizeCursor_rtl, 2, -10, -10);
        }
        else
        {
            _cursorID = CursorManager.setCursor(resizeCursor, 2, -10, -10);
        }
    }

    private function resize_outHandler(event:MouseEvent):void
    {
        CursorManager.removeCursor(_cursorID);
    }

    private function resize_downHandler(event:MouseEvent):void
    {
        if (_resizable)
        {
            xResizeStart = event.stageX
            yResizeStart = event.stageY;

            /*TODO: for now, it can't be resized when is not basic layout*/
            stage.addEventListener(MouseEvent.MOUSE_MOVE, resize_moveHandler);
            stage.addEventListener(MouseEvent.MOUSE_UP, resize_upHandler);
        }
    }

    public function isRtl():Boolean
    {
        var result:Boolean = false;
        try
        {
            result = (FlexGlobals.topLevelApplication as Application).layoutDirection == "rtl";
        }
        catch (error:Error)
        {
            result = false;
        }
        return result;
    }

    private function resize_moveHandler(event:MouseEvent):void
    {
        // clear constraints
        var widget:UIComponent = parent as UIComponent;
        widget.left = widget.right = widget.top = widget.bottom = undefined;

        var xResizeEnd:Number = event.stageX;
        var yResizeEnd:Number = event.stageY;

        var isWithinVerticalBoundaries:Boolean = (stage.mouseY < stage.height - 20);
        var isWithinHorizontalBoundaries:Boolean = isRtl() ?
            stage.mouseX > 20 : stage.mouseX < stage.width - 20;

        if (isWithinHorizontalBoundaries && isWithinVerticalBoundaries)
        {
            // if there is minWidth and minHeight specified on the container, use them while resizing
            const minimumResizeWidth:Number = minWidth ? minWidth : DEFAULT_MIN_WIDTH;
            const minimumResizeHeight:Number = minHeight ? minHeight : DEFAULT_MIN_HEIGHT;

            var deltaX:Number = xResizeEnd - xResizeStart;
            var deltaY:Number = yResizeEnd - yResizeStart;

            var calculatedWidth:Number = isRtl() ? widgetWidth - deltaX : widgetWidth + deltaX;
            var calculatedHeight:Number = widgetHeight + deltaY;

            if (calculatedWidth > minimumResizeWidth)
            {
                width = calculatedWidth;
            }
            if (calculatedHeight > minimumResizeHeight)
            {
                height = calculatedHeight;
            }
        }
    }

    private function resize_upHandler(event:MouseEvent):void
    {
        widgetWidth = width;
        widgetHeight = height;

        stage.removeEventListener(MouseEvent.MOUSE_MOVE, resize_moveHandler);
        stage.removeEventListener(MouseEvent.MOUSE_UP, resize_upHandler);

        var p:UIComponent = parent as UIComponent;
        p.stopDrag();
    }

    /**
     * Adds a new titlebar button - which usually represent a "state" or "view" within the widget.
     * For example, the Search widget has three titlebar buttons: one for "Select features" (interactively),
     * one for "Select by attribute", and one for "Results".
     * Each button corresponds to a function
     *
     * @param btnIcon URL to icon to use for this button.
     * @param btnTip Tooltip for this button.
     * @param btnFunction The function to call when this button is clicked.
     * @param selectable Boolean.
     */
    public function addTitlebarButton(btnIcon:String, btnTip:String, btnFunction:Function, selectable:Boolean = true):void
    {
        var btn:TitlebarButton = new TitlebarButton();
        btn.callback = btnFunction;
        btn.selectable = selectable;
        btn.source = btnIcon;
        btn.toolTip = btnTip;

        if (selectable)
        {
            btn.addEventListener(MouseEvent.CLICK, titlebarButton_clickHandler);
            if (headerToolGroup.numElements == 0)
            {
                selectedTitlebarButtonIndex = 0; // automatically select the first button added
            }
        }

        headerToolGroup.addElement(btn);
    }

    private function titlebarButton_clickHandler(event:MouseEvent):void
    {
        selectedTitlebarButtonIndex = headerToolGroup.getElementIndex(TitlebarButton(event.currentTarget));
    }
}

}
