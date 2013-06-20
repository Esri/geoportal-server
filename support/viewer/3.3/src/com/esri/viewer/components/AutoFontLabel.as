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
package com.esri.viewer.components
{

import flash.events.Event;
import flash.events.TimerEvent;
import flash.text.TextLineMetrics;
import flash.utils.Timer;

import mx.core.EventPriority;
import mx.core.FlexGlobals;
import mx.core.UITextFormat;
import mx.events.FlexEvent;
import mx.styles.CSSStyleDeclaration;
import mx.styles.IStyleManager2;

import spark.components.Label;

[Event(name="textChanged", type="flash.events.Event")]
public class AutoFontLabel extends Label
{
    public static const TEXT_CHANGED:String = "textChanged";

    private const MIN_FONT_RESIZE_DELAY:int = 50;

    public var minFontSize:Number = 10;

    public var autoShrinking:Boolean = true;

    private var initialFontSize:Number;

    private var fontResizeTimer:Timer = new Timer(MIN_FONT_RESIZE_DELAY, 1);

    override public function set text(value:String):void
    {
        if (super.text != value)
        {
            super.text = value;
            dispatchEvent(new Event(TEXT_CHANGED));
        }
    }

    public function AutoFontLabel()
    {
        addEventListener(FlexEvent.PREINITIALIZE, preinitializeHandler, false, EventPriority.DEFAULT_HANDLER);
        addEventListener(FlexEvent.UPDATE_COMPLETE, updateCompleteHandler, false, EventPriority.DEFAULT_HANDLER);
        fontResizeTimer.addEventListener(TimerEvent.TIMER_COMPLETE, fontResizeTimer_timerCompleteHandler, false, 0, true);
    }

    protected function preinitializeHandler(event:FlexEvent):void
    {
        initFontSize();
        setStyle("lineBreak", "explicit");
    }

    private function initFontSize():void
    {
        var topLevelStyleManager:IStyleManager2 = FlexGlobals.topLevelApplication.styleManager;
        var style:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("." + styleName);

        initialFontSize = style.getStyle("fontSize") as Number;
    }

    protected function updateCompleteHandler(event:FlexEvent):void
    {
        width = maxWidth;

        if (fontResizeTimer.running)
        {
            fontResizeTimer.reset();
        }

        fontResizeTimer.start();
    }

    protected function fontResizeTimer_timerCompleteHandler(event:TimerEvent):void
    {
        resizeLabel();
    }

    private function resizeLabel():void
    {
        constrainTextToWidth();

        if (canFit())
        {
            setStyle("lineBreak", "explicit");
            showTruncationTip = false;
            this.width = Math.min(maxWidth, width + 5);
        }
        else
        {
            setStyle("lineBreak", "toFit");
            maxDisplayedLines = 1;
            showTruncationTip = true;
        }
    }

    private function constrainTextToWidth():void
    {
        if (width <= 0)
        {
            return;
        }

        var topLevelStyleManager:IStyleManager2 = FlexGlobals.topLevelApplication.styleManager;
        var style:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("." + styleName);

        var fontSize:Number = initialFontSize;

        if (autoShrinking)
        {
            while (fontSize > minFontSize
                && getTextWidth(text, fontSize, style) > width)
            {
                fontSize -= 0.5;
            }
        }

        setStyle("fontSize", fontSize);
    }

    public function getRequiredWidthToDisplayFullText(maxWidth:Number):Number
    {
        var desiredWidth:Number;

        if (maxWidth > 0)
        {
            var topLevelStyleManager:IStyleManager2 = FlexGlobals.topLevelApplication.styleManager;
            var style:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("." + styleName);

            var fontSize:Number = minFontSize;

            while (fontSize < initialFontSize
                && getTextWidth(text, fontSize, style) < maxWidth)
            {
                fontSize += 0.5;
            }

            desiredWidth = getTextWidth(text, fontSize, style) + 10;
        }

        return desiredWidth;
    }

    private function getTextWidth(text:String, fontSize:Number, style:CSSStyleDeclaration):Number
    {
        var textFormat:UITextFormat =
            new UITextFormat(
            FlexGlobals.topLevelApplication.systemManager,
            style.getStyle("fontFamily"),
            fontSize,
            null,
            style.getStyle("fontWeight") == "bold",
            style.getStyle("fontStyle") == "italic",
            null,
            null,
            null,
            null,
            style.getStyle("paddingLeft"),
            style.getStyle("paddingRight"),
            style.getStyle("textIndent"));
        textFormat.antiAliasType = flash.text.AntiAliasType.ADVANCED;
        textFormat.gridFitType = flash.text.GridFitType.PIXEL;

        var textMetrics:TextLineMetrics = textFormat.measureText(text);
        return textMetrics.width;
    }

    private function canFit():Boolean
    {
        var topLevelStyleManager:IStyleManager2 = FlexGlobals.topLevelApplication.styleManager;
        var style:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("." + styleName);
        var fontSize:Number = style.getStyle("fontSize") as Number;

        return getTextWidth(text, fontSize, style) <= width;
    }
}
}
