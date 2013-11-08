///////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010-2012 Esri. All Rights Reserved.
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

import com.esri.viewer.skins.HorizontalPanelBoxButtonSkin;
import com.esri.viewer.skins.VerticalPanelBoxButtonSkin;

import flash.display.GradientType;
import flash.display.SpreadMethod;
import flash.events.MouseEvent;
import flash.geom.*;
import flash.utils.Dictionary;

import mx.containers.DividedBox;
import mx.containers.dividedBoxClasses.*;
import mx.core.mx_internal;
import mx.events.ResizeEvent;

import spark.components.ToggleButton;

use namespace mx_internal;

[Event(name="buttonClick", type="com.esri.viewer.utils.PanelBoxButtonClickEvent")]
/**
 * Custom divided box that contains panel(s).
 */
public class PanelBox extends DividedBox
{
    private var _button:ToggleButton;
    private var _isOverButton:Boolean;
    private var _boxDivider:Array = new Array();

    private var _dividerFillColors:Array;
    private var _dividerBorderColor:uint;
    private var alphas:Array = [ 1, 1 ];
    private var ratios:Array = [ 0, 255 ];

    private var expandPanelTooltip:String = LocalizationUtil.getDefaultString("panelExpandTooltip");
    private var collapsePanelTooltip:String = LocalizationUtil.getDefaultString("panelCollapseTooltip");

    private var _buttonIndexToSelection:Dictionary = new Dictionary();

    public function PanelBox():void
    {
        super();
    }

    private var _showButton:Boolean = true;

    public function get showButton():Boolean
    {
        return _showButton;
    }

    public function set showButton(value:Boolean):void
    {
        _showButton = value
    }

    private var _hasRightPanelContainer:Boolean = false;

    public function get hasRightPanelContainer():Boolean
    {
        return _hasRightPanelContainer;
    }

    public function set hasRightPanelContainer(value:Boolean):void
    {
        _hasRightPanelContainer = value
    }

    /**
     * Don't allow dragging if over a button
     */
    override mx_internal function startDividerDrag(divider:BoxDivider, trigger:MouseEvent):void
    {
        //ignore if we are over a button
        if (_showButton && _isOverButton)
        {
            return;
        }
        super.mx_internal::startDividerDrag(divider, trigger);
    }

    /**
     * Don't show splitter cursor when over a button
     */
    override mx_internal function changeCursor(divider:BoxDivider):void
    {
        //ignore if we are over a button
        if (_showButton && _isOverButton)
        {
            return;
        }
        super.mx_internal::changeCursor(divider);
    }

    override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
    {
        super.updateDisplayList(unscaledWidth, unscaledHeight);

        if (_boxDivider.length == 0)
        {
            for (var i:int = 0; i < numDividers; i++)
            {
                var divbar:BoxDivider = getDividerAt(i);
                divbar.addEventListener("resize", handleResize);
                _boxDivider.push(divbar);

                if (_showButton)
                {
                    _button = new ToggleButton();
                    _button.buttonMode = true;
                    _button.name = "PanelBoxButton" + i;
                    _button.id = String(i);

                    //no divider skin just the button
                    divbar.setStyle("dividerSkin", null);

                    if (direction == "vertical")
                    {
                        _button.setStyle("skinClass", VerticalPanelBoxButtonSkin);
                        _button.selected = _buttonIndexToSelection[i];
                        _button.toolTip = _button.selected ? expandPanelTooltip : collapsePanelTooltip;

                        _button.width = 40;
                        _button.height = getStyle("dividerThickness") + 1;
                        _button.x = (unscaledWidth / 2) - (_button.width / 2);
                    }
                    else
                    {
                        _button.setStyle("skinClass", HorizontalPanelBoxButtonSkin);
                        _button.selected = _buttonIndexToSelection[i];
                        _button.toolTip = _button.selected ? expandPanelTooltip : collapsePanelTooltip;
                        if (numDividers == 1)
                        {
                            _button.toolTip = hasRightPanelContainer ? _button.selected ? collapsePanelTooltip : expandPanelTooltip :
                                _button.selected ? expandPanelTooltip : collapsePanelTooltip;
                        }
                        else
                        {
                            _button.toolTip = i == 1 ? _button.selected ? collapsePanelTooltip : expandPanelTooltip :
                                _button.selected ? expandPanelTooltip : collapsePanelTooltip;
                        }

                        _button.height = 40;
                        _button.width = getStyle("dividerThickness") + 1;
                        _button.y = (unscaledHeight / 2) - (_button.height / 2);
                    }

                    _button.addEventListener(MouseEvent.CLICK, handleClick);
                    _button.addEventListener(MouseEvent.MOUSE_OVER, handleOver);
                    _button.addEventListener(MouseEvent.MOUSE_OUT, handleOut);

                    divbar.addChild(_button);
                }
            }
        }

        drawGradientFill();
    }

    public function isButtonSelected(buttonIndex:int = 0):Boolean
    {
        var result:Boolean = numDividers == 1 ? hasRightPanelContainer ? true : false : buttonIndex == 1 ? true : false;

        var divbar:BoxDivider = getDividerAt(buttonIndex);
        var toggleButton:ToggleButton = ToggleButton(divbar.getChildByName("PanelBoxButton" + buttonIndex));
        if (toggleButton)
        {
            result = toggleButton.selected;
        }

        return result;
    }

    public function changeButtonSelection(buttonIndex:int = 0, isSelected:Boolean = false):void
    {
        if (!_buttonIndexToSelection[buttonIndex])
        {
            _buttonIndexToSelection[buttonIndex] = isSelected;
        }

        var divbar:BoxDivider = getDividerAt(buttonIndex);
        var toggleButton:ToggleButton = ToggleButton(divbar.getChildByName("PanelBoxButton" + buttonIndex));

        if (toggleButton)
        {
            toggleButton.selected = !toggleButton.selected;
            if (numDividers == 1)
            {
                toggleButton.toolTip = hasRightPanelContainer ? toggleButton.selected ? collapsePanelTooltip : expandPanelTooltip :
                    toggleButton.selected ? expandPanelTooltip : collapsePanelTooltip;
            }
            else
            {
                toggleButton.toolTip = buttonIndex == 1 ? toggleButton.selected ? collapsePanelTooltip : expandPanelTooltip :
                    toggleButton.selected ? expandPanelTooltip : collapsePanelTooltip;
            }
        }
    }

    private function handleResize(event:ResizeEvent):void
    {
        if (!_showButton)
        {
            return;
        }

        if (event.currentTarget.width != event.oldWidth || event.currentTarget.height != event.oldHeight)
        {
            for (var i:int = 0; i < numDividers; i++)
            {
                var divbar:BoxDivider = getDividerAt(i);
                var toggleButton:ToggleButton = ToggleButton(divbar.getChildByName("PanelBoxButton" + i));
                if (toggleButton)
                {
                    if (direction == "vertical")
                    {
                        toggleButton.x = (unscaledWidth / 2) - (toggleButton.width / 2);
                    }
                    else
                    {
                        toggleButton.y = (unscaledHeight / 2) - (toggleButton.height / 2);
                    }
                }
            }
        }
    }

    private function handleClick(event:MouseEvent):void
    {
        event.currentTarget.selected = !event.currentTarget.selected; // prevent the state to be changed on click

        var buttonIndex:int = int(event.currentTarget.id);
        dispatchEvent(new PanelBoxButtonClickEvent("buttonClick", buttonIndex));
    }

    private function handleOut(event:MouseEvent):void
    {
        _isOverButton = false;
    }

    private function handleOver(event:MouseEvent):void
    {
        _isOverButton = true;
    }

    /*override public function styleChanged(styleProp:String):void
    {

        super.styleChanged(styleProp);

        // Check to see if style changed.
        if (styleProp == "barFillColors" || styleProp == "barBorderColor")
        {
            _barBorderColor = 0;
            _barFillColors = null;
            invalidateDisplayList();
            return;
        }
    }*/

    private function drawGradientFill():void
    {
        graphics.clear();

        for (var i:int = 0; i < _boxDivider.length; i++)
        {
            if (!_dividerFillColors)
            {
                _dividerFillColors = getStyle("_dividerFillColors");
                if (!_dividerFillColors)
                {
                    _dividerFillColors = [ 0xFAE38F, 0xEE9819 ]; // if no style default to orange
                }
            }
            if (!_dividerBorderColor)
            {
                _dividerBorderColor = getStyle("_dividerBorderColor");
                if (!_dividerBorderColor)
                {
                    _dividerBorderColor = 0xEE9819; // if no style default to orange
                }
            }
            graphics.lineStyle(1, _dividerBorderColor);

            var divwidth:Number = _boxDivider[i].getStyle("dividerThickness");
            if (divwidth == 0)
            {
                divwidth = 10;
            }

            var matr:Matrix = new Matrix();
            if (direction == "vertical")
            {
                matr.createGradientBox(_boxDivider[i].width, divwidth, Math.PI / 2, _boxDivider[i].x, _boxDivider[i].y);
                graphics.beginGradientFill(GradientType.LINEAR, _dividerFillColors, alphas, ratios, matr, SpreadMethod.PAD);
                graphics.drawRect(_boxDivider[i].x, _boxDivider[i].y, _boxDivider[i].width, divwidth);
            }
            else
            {
                matr.createGradientBox(divwidth, _boxDivider[i].height, 0, _boxDivider[i].x, _boxDivider[i].x + 10);
                graphics.beginGradientFill(GradientType.LINEAR, _dividerFillColors, alphas, ratios, matr, SpreadMethod.PAD);
                graphics.drawRect(_boxDivider[i].x, _boxDivider[i].y, divwidth, _boxDivider[i].height);
            }
        }
    }
}

}
