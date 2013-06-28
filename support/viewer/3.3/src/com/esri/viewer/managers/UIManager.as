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

import com.esri.viewer.AppEvent;
import com.esri.viewer.ConfigData;
import com.esri.viewer.utils.LocalizationUtil;

import flash.events.EventDispatcher;

import mx.styles.CSSStyleDeclaration;
import mx.styles.IStyleManager2;
import mx.styles.StyleManager;

/**
 * UIManager applies the style colors, fonts etc as specific in the main configuration file.
 */
public class UIManager extends EventDispatcher
{
    private var configData:ConfigData;

    public function UIManager()
    {
        AppEvent.addListener(AppEvent.SET_TEXT_COLOR, setTextColorHandler);
        AppEvent.addListener(AppEvent.SET_BACKGROUND_COLOR, setBackgroundColorHandler);
        AppEvent.addListener(AppEvent.SET_ROLLOVER_COLOR, setRolloverColorHandler);
        AppEvent.addListener(AppEvent.SET_SELECTION_COLOR, setSelectionColorHandler);
        AppEvent.addListener(AppEvent.SET_TITLE_COLOR, setTitleColorHandler);
        AppEvent.addListener(AppEvent.SET_APPLICATION_BACKGROUND_COLOR, setApplicationBackgroundColorHandler);
        AppEvent.addListener(AppEvent.SET_FONT_NAME, setFontNameHandler);
        AppEvent.addListener(AppEvent.SET_APP_TITLE_FONT_NAME, setAppTitleFontNameHandler);
        AppEvent.addListener(AppEvent.SET_SUB_TITLE_FONT_NAME, setSubTitleFontNameHandler);
        AppEvent.addListener(AppEvent.SET_ALPHA, setAlphaHandler);
        AppEvent.addListener(AppEvent.SET_PREDEFINED_STYLES, setPredefinedStyles);

        AppEvent.addListener(AppEvent.CONFIG_LOADED, configLoadedHandler);
    }

    private function setPredefinedStyles(event:AppEvent):void
    {
        configData.styleColors[0] = event.data.textColor;
        configData.styleColors[1] = event.data.backgroundColor;
        configData.styleColors[2] = event.data.rolloverColor;
        configData.styleColors[3] = event.data.selectionColor;
        configData.styleColors[4] = event.data.titleColor;
        configData.styleColors[5] = event.data.applicationBackgroundColor;
        configData.styleAlpha = event.data.alpha;
        setViewerStyle();
    }

    private function setSubTitleFontNameHandler(event:AppEvent):void
    {
        configData.subTitleFont.name = event.data;
        setViewerStyle();
    }

    private function setAppTitleFontNameHandler(event:AppEvent):void
    {
        configData.titleFont.name = event.data;
        setViewerStyle();
    }

    private function setFontNameHandler(event:AppEvent):void
    {
        configData.font.name = event.data;
        setViewerStyle();
    }

    private function setTextColorHandler(event:AppEvent):void
    {
        configData.styleColors[0] = event.data;
        setViewerStyle();
    }

    private function setBackgroundColorHandler(event:AppEvent):void
    {
        configData.styleColors[1] = event.data;
        setViewerStyle();
    }

    private function setRolloverColorHandler(event:AppEvent):void
    {
        configData.styleColors[2] = event.data;
        setViewerStyle();
    }

    private function setSelectionColorHandler(event:AppEvent):void
    {
        configData.styleColors[3] = event.data;
        setViewerStyle();
    }

    private function setTitleColorHandler(event:AppEvent):void
    {
        configData.styleColors[4] = event.data;
        setViewerStyle();
    }

    private function setApplicationBackgroundColorHandler(event:AppEvent):void
    {
        configData.styleColors[5] = event.data;
        setViewerStyle();
    }

    private function setAlphaHandler(event:AppEvent):void
    {
        configData.styleAlpha = event.data as Number;
        setViewerStyle();
    }

    private function configLoadedHandler(event:AppEvent):void
    {
        configData = event.data as ConfigData;
        setViewerStyle();

//        if (configData.styleSWF)
//        {
//            var topLevelStyleManager:IStyleManager2 = StyleManager.getStyleManager(null);
//            var loadStyleDispatcher:IEventDispatcher = topLevelStyleManager.loadStyleDeclarations(configData.styleSWF, false, false, null, SecurityDomain.currentDomain);
//            loadStyleDispatcher.addEventListener(StyleEvent.COMPLETE, completeHandler);
//            loadStyleDispatcher.addEventListener(StyleEvent.ERROR, errorHandler);
//        }
//        else
//        {
//            setViewerStyle();
//        }
//
//        function completeHandler(event:StyleEvent):void
//        {
//            loadStyleDispatcher.removeEventListener(StyleEvent.COMPLETE, completeHandler);
//            loadStyleDispatcher.removeEventListener(StyleEvent.ERROR, errorHandler);
//            setViewerStyle();
//        }
//        function errorHandler(event:StyleEvent):void
//        {
//            completeHandler(event);
//            AppEvent.dispatch(AppEvent.APP_ERROR, event.errorText);
//        }
    }

    private function setViewerStyle():void
    {
        var topLevelStyleManager:IStyleManager2 = StyleManager.getStyleManager(null);
        var numberOfStyleColors:uint = configData.styleColors.length;

        var textColor:uint;
        var backgroundColor:uint;
        var rolloverColor:uint;
        var selectionColor:uint;
        var titleColor:uint;
        var applicationBackgroundColor:uint;
        var styleAlpha:Number;

        var cssStyleDeclarationGlobal:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("global")

        var defaultFontSize:int = cssStyleDeclarationGlobal.getStyle("fontSize");
        var defaultTextColor:uint = cssStyleDeclarationGlobal.getStyle("fontColor");
        var defaultLayoutDirection:String = cssStyleDeclarationGlobal.getStyle("layoutDirection");
        var localeDefaultFontName:String = LocalizationUtil.getDefaultString("textFont1");
        var fallbackFontName:String = "_sans";

        var mainFontFamily:String = buildFontFamilyString(configData.font.name, localeDefaultFontName, fallbackFontName);
        var titleFontFamily:String = configData.titleFont.name ? buildFontFamilyString(configData.titleFont.name, localeDefaultFontName, fallbackFontName) : mainFontFamily;
        var subTitleFontFamily:String = configData.subTitleFont.name ? buildFontFamilyString(configData.subTitleFont.name, localeDefaultFontName, fallbackFontName) : mainFontFamily;

        var fontSize:int = (configData.font.size != 0) ? configData.font.size : defaultFontSize;
        var titleFontSize:int = (configData.titleFont.size != 0) ? configData.titleFont.size : 20;
        var subTitleFontSize:int = (configData.subTitleFont.size != 0) ? configData.subTitleFont.size : 12;
        var layoutDirection:String = configData.layoutDirection && (configData.layoutDirection == "ltr" || configData.layoutDirection == "rtl") ? configData.layoutDirection : defaultLayoutDirection;

        // for RTL
        cssStyleDeclarationGlobal.setStyle("layoutDirection", layoutDirection);
        cssStyleDeclarationGlobal.setStyle("direction", layoutDirection);

        styleAlpha = configData.styleAlpha;
        if (numberOfStyleColors > 4)
        {
            textColor = configData.styleColors[0];
            backgroundColor = configData.styleColors[1];
            rolloverColor = configData.styleColors[2];
            selectionColor = configData.styleColors[3];
            titleColor = configData.styleColors[4];
            applicationBackgroundColor = (configData.styleColors[5] != null) ? configData.styleColors[5] : 0xFFFFFF;
        }

        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationGlobal.setStyle("chromeColor", backgroundColor);
            cssStyleDeclarationGlobal.setStyle("color", textColor);
            cssStyleDeclarationGlobal.setStyle("contentBackgroundColor", backgroundColor);
            cssStyleDeclarationGlobal.setStyle("symbolColor", textColor);
            cssStyleDeclarationGlobal.setStyle("rollOverColor", rolloverColor);
            cssStyleDeclarationGlobal.setStyle("selectionColor", selectionColor);
            cssStyleDeclarationGlobal.setStyle("focusColor", titleColor);
            cssStyleDeclarationGlobal.setStyle("accentColor", textColor);
            cssStyleDeclarationGlobal.setStyle("textSelectedColor", textColor);
            cssStyleDeclarationGlobal.setStyle("textRollOverColor", textColor);
        }
        cssStyleDeclarationGlobal.setStyle("contentBackgroundAlpha", styleAlpha);
        cssStyleDeclarationGlobal.setStyle("fontSize", fontSize);
        cssStyleDeclarationGlobal.setStyle("fontFamily", mainFontFamily);

        var cssStyleDeclarationModule:CSSStyleDeclaration = new CSSStyleDeclaration();
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationModule.setStyle("chromeColor", backgroundColor);
            cssStyleDeclarationModule.setStyle("color", textColor);
            cssStyleDeclarationModule.setStyle("contentBackgroundColor", backgroundColor);
            cssStyleDeclarationModule.setStyle("symbolColor", textColor);
            cssStyleDeclarationModule.setStyle("rollOverColor", rolloverColor);
            cssStyleDeclarationModule.setStyle("selectionColor", selectionColor);
            cssStyleDeclarationModule.setStyle("focusColor", titleColor);
            cssStyleDeclarationModule.setStyle("accentColor", textColor);
            cssStyleDeclarationModule.setStyle("textSelectedColor", textColor);
            cssStyleDeclarationModule.setStyle("textRollOverColor", textColor);
        }
        cssStyleDeclarationModule.setStyle("contentBackgroundAlpha", styleAlpha);
        cssStyleDeclarationModule.setStyle("fontSize", fontSize);
        cssStyleDeclarationModule.setStyle("fontFamily", mainFontFamily);
        topLevelStyleManager.setStyleDeclaration("mx.modules.Module", cssStyleDeclarationModule, false);

        //Style Application
        var cssStyleDeclarationApplication:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("spark.components.Application");
        if (cssStyleDeclarationApplication)
        {
            if (numberOfStyleColors > 4)
            {
                cssStyleDeclarationApplication.setStyle("backgroundColor", applicationBackgroundColor);
            }
            cssStyleDeclarationApplication.setStyle("backgroundAlpha", styleAlpha);
        }

        var cssStyleDeclarationApplicationWindowed:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("spark.components.WindowedApplication");
        if (cssStyleDeclarationApplicationWindowed)
        {
            if (numberOfStyleColors > 4)
            {
                cssStyleDeclarationApplicationWindowed.setStyle("backgroundColor", applicationBackgroundColor);
            }
            cssStyleDeclarationApplicationWindowed.setStyle("backgroundAlpha", styleAlpha);
        }

        //Style WidgetTemplate
        var cssStyleDeclarationWT:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("com.esri.viewer.WidgetTemplate");
        // When pointing to graphical theme replace WidgetTemplateSkin with GraphicalWidgetTemplateSkin
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationWT.setStyle("borderColor", textColor);
        }

        /*Style For InfoWindow
        When pointing to graphical theme, you will need to explicitly set properties for infoWindow.
        e.g. cssStyleDeclarationInfoContainer.setStyle("backgroundColor",0xFFFFFF);
        */
        var cssStyleDeclarationInfoContainer:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("com.esri.ags.components.supportClasses.InfoWindow");
        /*
        For custom skin for infoWindow, borderSkin needs to be set to null as shouwn below
        infoOffsetX and infoOffsetY can be used to change the location where infowindow will be shown.
        infoPlacement would need to be set to none in this case for preventing infowindow placement from auto adjusting.
        */
        /*
        cssStyleDeclarationInfoContainer.setStyle("borderSkin",null);
        cssStyleDeclarationInfoContainer.setStyle("infoOffsetX",-45);
        */
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationInfoContainer.setStyle("backgroundColor", backgroundColor);
            cssStyleDeclarationInfoContainer.setStyle("borderColor", textColor);
        }
        else
        {
            cssStyleDeclarationInfoContainer.setStyle("borderColor", defaultTextColor);
        }
        cssStyleDeclarationInfoContainer.setStyle("borderThickness", 1);
        cssStyleDeclarationInfoContainer.setStyle("backgroundAlpha", styleAlpha);

        var cssStyleDeclarationInfoWindowLabel:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("com.esri.ags.components.supportClasses.InfoWindowLabel");
        cssStyleDeclarationInfoWindowLabel.setStyle("fontSize", fontSize);
        cssStyleDeclarationInfoWindowLabel.setStyle("fontFamily", mainFontFamily);

        var cssStyleDeclarationInfoSymbolWindow:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("com.esri.ags.components.supportClasses.InfoSymbolWindow");
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationInfoSymbolWindow.setStyle("backgroundColor", backgroundColor);
            cssStyleDeclarationInfoSymbolWindow.setStyle("borderColor", textColor);
        }
        else
        {
            cssStyleDeclarationInfoSymbolWindow.setStyle("borderColor", defaultTextColor);
        }
        cssStyleDeclarationInfoSymbolWindow.setStyle("borderThickness", 1);
        cssStyleDeclarationInfoSymbolWindow.setStyle("backgroundAlpha", styleAlpha);

        if (numberOfStyleColors > 4)
        {
            var cssStyleDeclarationPopUpRendererLink:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("com.esri.ags.portal.PopUpRenderer");
            cssStyleDeclarationPopUpRendererLink.setStyle("linkActiveColor", titleColor);
            cssStyleDeclarationPopUpRendererLink.setStyle("linkNormalColor", textColor);
            cssStyleDeclarationPopUpRendererLink.setStyle("linkHoverColor", titleColor);
        }

        var cssStyleDeclarationContentNavigator:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("com.esri.ags.components.ContentNavigator");
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationContentNavigator.setStyle("headerBackgroundColor", backgroundColor);
            cssStyleDeclarationContentNavigator.setStyle("headerColor", textColor);
        }
        cssStyleDeclarationContentNavigator.setStyle("headerBackgroundAlpha", styleAlpha);

        //Style Banner title and WidgetTitle
        var cssStyleDeclarationWidgetTitle:CSSStyleDeclaration = new CSSStyleDeclaration(".WidgetTitle");
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationWidgetTitle.setStyle("color", titleColor);
        }
        cssStyleDeclarationWidgetTitle.setStyle("fontSize", fontSize);
        cssStyleDeclarationWidgetTitle.setStyle("fontFamily", mainFontFamily);
        cssStyleDeclarationWidgetTitle.setStyle("fontWeight", "bold");

        var cssStyleDeclarationBannerTitle:CSSStyleDeclaration = new CSSStyleDeclaration(".BannerTitle");
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationBannerTitle.setStyle("color", titleColor);
        }
        if (titleFontSize > 0)
        {
            cssStyleDeclarationBannerTitle.setStyle("fontSize", titleFontSize);
        }
        cssStyleDeclarationBannerTitle.setStyle("fontFamily", titleFontFamily);
        cssStyleDeclarationBannerTitle.setStyle("paddingBottom", 2);
        cssStyleDeclarationBannerTitle.setStyle("paddingTop", 4);

        var cssStyleDeclarationBannerSubtitle:CSSStyleDeclaration = new CSSStyleDeclaration(".BannerSubtitle");
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationBannerSubtitle.setStyle("color", titleColor);
        }
        if (subTitleFontSize > 0)
        {
            cssStyleDeclarationBannerSubtitle.setStyle("fontSize", subTitleFontSize);
        }
        cssStyleDeclarationBannerSubtitle.setStyle("fontFamily", subTitleFontFamily);

        // Style s|Panel
        var cssStyleDeclarationSPanel:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("spark.components.Panel");
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationSPanel.setStyle("backgroundColor", backgroundColor);
        }
        cssStyleDeclarationSPanel.setStyle("backgroundAlpha", styleAlpha);

        //Style mx|Panel
        var cssStyleDeclarationMxPanel:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("mx.containers.Panel");
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationMxPanel.setStyle("backgroundColor", backgroundColor);
        }
        cssStyleDeclarationMxPanel.setStyle("backgroundAlpha", styleAlpha);

        //Style TabNavigator
        var cssStyleDeclarationNavigator:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("mx.containers.TabNavigator");
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationNavigator.setStyle("backgroundColor", backgroundColor);
        }
        cssStyleDeclarationNavigator.setStyle("backgroundAlpha", styleAlpha);

        // Style mx|Alert
        var cssStyleDeclaration:CSSStyleDeclaration = new CSSStyleDeclaration();
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclaration.setStyle("color", textColor);
            cssStyleDeclaration.setStyle("backgroundColor", backgroundColor);
        }
        topLevelStyleManager.setStyleDeclaration("mx.controls.Alert", cssStyleDeclaration, false);

        //Style Tooltip
        var cssStyleDeclarationTooltip:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("mx.controls.ToolTip");
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationTooltip.setStyle("color", textColor);
            cssStyleDeclarationTooltip.setStyle("backgroundColor", backgroundColor);
        }
        cssStyleDeclarationTooltip.setStyle("fontSize", fontSize);
        cssStyleDeclarationTooltip.setStyle("fontFamily", mainFontFamily);

        //Style TitleWindow
        if (numberOfStyleColors > 4)
        {
            var cssStyleDeclarationTitleWindow:CSSStyleDeclaration = new CSSStyleDeclaration();
            cssStyleDeclarationTitleWindow.setStyle("color", textColor);
            cssStyleDeclarationTitleWindow.setStyle("backgroundColor", backgroundColor);
            topLevelStyleManager.setStyleDeclaration("mx.containers.TitleWindow", cssStyleDeclarationTitleWindow, false);
        }
        //Style DataGrid
        var cssStyleDeclarationDataGrid:CSSStyleDeclaration = new CSSStyleDeclaration();
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationDataGrid.setStyle("alternatingItemColors", [ backgroundColor, backgroundColor ]);
            cssStyleDeclarationDataGrid.setStyle("contentBackgroundColor", backgroundColor);
        }
        cssStyleDeclarationDataGrid.setStyle("backgroundAlpha", styleAlpha);
        topLevelStyleManager.setStyleDeclaration("mx.controls.DataGrid", cssStyleDeclarationDataGrid, false);

        //Style RichEditableText
        if (numberOfStyleColors > 4)
        {
            var cssStyleDeclarationRET:CSSStyleDeclaration = new CSSStyleDeclaration();
            cssStyleDeclarationRET.setStyle("focusedTextSelectionColor", rolloverColor);
            cssStyleDeclarationRET.setStyle("unfocusedTextSelectionColor", rolloverColor);
            topLevelStyleManager.setStyleDeclaration("spark.components.RichEditableText", cssStyleDeclarationRET, false);

            var cssStyleDeclarationTI:CSSStyleDeclaration = new CSSStyleDeclaration();
            cssStyleDeclarationTI.setStyle("chromeColor", textColor);
            topLevelStyleManager.setStyleDeclaration("spark.components.TextInput", cssStyleDeclarationTI, false);
            topLevelStyleManager.setStyleDeclaration("spark.components.TextArea", cssStyleDeclarationTI, false);
            topLevelStyleManager.setStyleDeclaration("mx.controls.TextInput", cssStyleDeclarationTI, false);
            topLevelStyleManager.setStyleDeclaration("mx.controls.TextArea", cssStyleDeclarationTI, false);
            topLevelStyleManager.setStyleDeclaration("mx.controls.VSlider", cssStyleDeclarationTI, false);
            topLevelStyleManager.setStyleDeclaration("spark.components.VSlider", cssStyleDeclarationTI, false);
            topLevelStyleManager.setStyleDeclaration("mx.controls.HSlider", cssStyleDeclarationTI, false);
            topLevelStyleManager.setStyleDeclaration("spark.components.HSlider", cssStyleDeclarationTI, false);
        }

        if (numberOfStyleColors > 4)
        {
            var cssStyleDeclarationChartDataTips:CSSStyleDeclaration = new CSSStyleDeclaration();
            cssStyleDeclarationChartDataTips.setStyle("backgroundColor", backgroundColor);
            topLevelStyleManager.setStyleDeclaration("mx.charts.chartClasses.DataTip", cssStyleDeclarationChartDataTips, false);
        }

        var cssStyleDeclarationModal:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("global")
        cssStyleDeclarationModal.setStyle("modalTransparencyColor", 0x777777);
        cssStyleDeclarationModal.setStyle("modalTransparencyBlur", 1);
        cssStyleDeclarationModal.setStyle("modalTransparency", 0.5);
        cssStyleDeclarationModal.setStyle("modalTransparencyDuration", 300); //messes up tween!
        topLevelStyleManager.setStyleDeclaration("global", cssStyleDeclarationModal, true);
    }

    private function buildFontFamilyString(... fontNames):String
    {
        return fontNames.filter(isValidValue).join(", ");
    }

    private function isValidValue(item:String, index:int, array:Array):Boolean
    {
        return Boolean(item);
    }
}

}
