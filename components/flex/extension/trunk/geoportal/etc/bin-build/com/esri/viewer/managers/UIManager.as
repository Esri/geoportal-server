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

import com.esri.viewer.AppEvent;
import com.esri.viewer.ConfigData;
import com.esri.viewer.ViewerContainer;

import flash.events.EventDispatcher;

import mx.core.FlexGlobals;
import mx.styles.CSSStyleDeclaration;
import mx.styles.IStyleManager2;

public class UIManager extends EventDispatcher
{
    private var configData:ConfigData;

    public function UIManager()
    {
        super();
        ViewerContainer.addEventListener(AppEvent.CONFIG_LOADED, config);
    }

    private function config(event:AppEvent):void
    {
        configData = event.data as ConfigData;
        setViewerStyle();
    }

    private function setViewerStyle():void
    {
        var topLevelStyleManager:IStyleManager2 = FlexGlobals.topLevelApplication.styleManager;
        var numberOfStyleColors:uint = configData.styleColors.length;

        var textColor:uint;
        var backgroundColor:uint;
        var rolloverColor:uint;
        var selectionColor:uint;
        var titleColor:uint;
        var applicationBackgroundColor:uint;
        var styleAlpha:Number;

        var cssStyleDeclarationGlobal:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("global")
        var defaultFontName:String = cssStyleDeclarationGlobal.getStyle("fontFamily");
        var defaultFontSize:int = cssStyleDeclarationGlobal.getStyle("fontSize");
        var defaultTextColor:uint = cssStyleDeclarationGlobal.getStyle("fontColor");
        var titleFontName:String = (configData.titleFont.name != "") ? configData.titleFont.name : defaultFontName;
        var titleFontSize:int = (configData.titleFont.size != 0) ? configData.titleFont.size : 20;
        var fontName:String = (configData.font.name != "") ? configData.font.name : defaultFontName;
        var fontSize:int = (configData.font.size != 0) ? configData.font.size : defaultFontSize;


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
        cssStyleDeclarationGlobal.setStyle("fontFamily", fontName);
        topLevelStyleManager.setStyleDeclaration("global", cssStyleDeclarationGlobal, false);

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
        cssStyleDeclarationModule.setStyle("fontFamily", fontName);
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
            topLevelStyleManager.setStyleDeclaration("spark.components.Application", cssStyleDeclarationApplication, false);
        }

        var cssStyleDeclarationApplicationWindowed:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("spark.components.WindowedApplication");
        if (cssStyleDeclarationApplicationWindowed)
        {
            if (numberOfStyleColors > 4)
            {
                cssStyleDeclarationApplicationWindowed.setStyle("backgroundColor", applicationBackgroundColor);
            }
            cssStyleDeclarationApplicationWindowed.setStyle("backgroundAlpha", styleAlpha);
            topLevelStyleManager.setStyleDeclaration("spark.components.WindowedApplication", cssStyleDeclarationApplicationWindowed, false);
        }

        //Style WidgetTemplate
        var cssStyleDeclarationWT:CSSStyleDeclaration = new CSSStyleDeclaration("com.esri.viewer.WidgetTemplate");
        // When pointing to graphical theme replace WidgetTemplateSkin with GraphicalWidgetTemplateSkin 
        cssStyleDeclarationWT.setStyle("backgroundAlpha", styleAlpha);
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationWT.setStyle("borderColor", textColor);
        }
        topLevelStyleManager.setStyleDeclaration("com.esri.viewer.WidgetTemplate", cssStyleDeclarationWT, false);

        /*Style For InfoWindow
        When pointing to graphical theme, you will need to explicitly set properties for infoWindow.
        e.g. cssStyleDeclarationInfoContainer.setStyle("backgroundColor",0xFFFFFF);
        */
        var cssStyleDeclarationInfoContainer:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("com.esri.ags.components.supportClasses.InfoContainer");
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
        topLevelStyleManager.setStyleDeclaration("com.esri.ags.components.supportClasses.InfoContainer", cssStyleDeclarationInfoContainer, false);

        var cssStyleDeclarationInfoWindowLabel:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("com.esri.ags.components.supportClasses.InfoWindowLabel");
        cssStyleDeclarationInfoWindowLabel.setStyle("fontSize", fontSize);
        cssStyleDeclarationInfoWindowLabel.setStyle("fontFamily", fontName);
        topLevelStyleManager.setStyleDeclaration("com.esri.ags.components.supportClasses.InfoWindowLabel", cssStyleDeclarationInfoWindowLabel, false);

        //Style Banner title and WidgetTitle
        var cssStyleDeclarationWidgetTitle:CSSStyleDeclaration = new CSSStyleDeclaration(".WidgetTitle");
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationWidgetTitle.setStyle("color", titleColor);
        }
        cssStyleDeclarationWidgetTitle.setStyle("fontSize", fontSize);
        cssStyleDeclarationWidgetTitle.setStyle("fontFamily", fontName);

        topLevelStyleManager.setStyleDeclaration(".WidgetTitle", cssStyleDeclarationWidgetTitle, false);

        var cssStyleDeclarationBannerTitle:CSSStyleDeclaration = new CSSStyleDeclaration(".BannerTitle");
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationBannerTitle.setStyle("color", titleColor);
        }
        if (titleFontSize > 0)
        {
            cssStyleDeclarationBannerTitle.setStyle("fontSize", titleFontSize);
        }
        cssStyleDeclarationBannerTitle.setStyle("fontFamily", titleFontName);

        topLevelStyleManager.setStyleDeclaration(".BannerTitle", cssStyleDeclarationBannerTitle, false);

        var cssStyleDeclarationBannerSubtitle:CSSStyleDeclaration = new CSSStyleDeclaration(".BannerSubtitle");
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationBannerSubtitle.setStyle("color", titleColor);
        }
        topLevelStyleManager.setStyleDeclaration(".BannerSubtitle", cssStyleDeclarationBannerSubtitle, false);

        // Style s|Panel
        var cssStyleDeclarationSPanel:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("spark.components.Panel");
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationSPanel.setStyle("backgroundColor", backgroundColor);
        }
        cssStyleDeclarationSPanel.setStyle("backgroundAlpha", styleAlpha);
        topLevelStyleManager.setStyleDeclaration("spark.components.Panel", cssStyleDeclarationSPanel, false);

        //Style mx|Panel
        var cssStyleDeclarationMxPanel:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("mx.containers.Panel");
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationMxPanel.setStyle("backgroundColor", backgroundColor);
        }
        cssStyleDeclarationMxPanel.setStyle("backgroundAlpha", styleAlpha);
        topLevelStyleManager.setStyleDeclaration("mx.containers.Panel", cssStyleDeclarationMxPanel, false);

        //Style TabNavigator
        var cssStyleDeclarationNavigator:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("mx.containers.TabNavigator");
        if (numberOfStyleColors > 4)
        {
            cssStyleDeclarationNavigator.setStyle("backgroundColor", backgroundColor);
        }
        cssStyleDeclarationNavigator.setStyle("backgroundAlpha", styleAlpha);
        topLevelStyleManager.setStyleDeclaration("mx.containers.TabNavigator", cssStyleDeclarationNavigator, false);

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
        cssStyleDeclarationTooltip.setStyle("fontFamily", fontName);
        topLevelStyleManager.setStyleDeclaration("mx.controls.ToolTip", cssStyleDeclarationTooltip, false);

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

        var cssStyleDeclarationModal:CSSStyleDeclaration = topLevelStyleManager.getStyleDeclaration("global")
        cssStyleDeclarationModal.setStyle("modalTransparencyColor", 0x777777);
        cssStyleDeclarationModal.setStyle("modalTransparencyBlur", 1);
        cssStyleDeclarationModal.setStyle("modalTransparency", 0.5);
        cssStyleDeclarationModal.setStyle("modalTransparencyDuration", 1000); //messes up tween!
        topLevelStyleManager.setStyleDeclaration("global", cssStyleDeclarationModal, true);

    }

}
}
