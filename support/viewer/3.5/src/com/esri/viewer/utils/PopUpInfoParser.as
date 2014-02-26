///////////////////////////////////////////////////////////////////////////
// Copyright (c) 2011 Esri. All Rights Reserved.
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

import com.esri.ags.portal.supportClasses.PopUpFieldFormat;
import com.esri.ags.portal.supportClasses.PopUpFieldInfo;
import com.esri.ags.portal.supportClasses.PopUpInfo;
import com.esri.ags.portal.supportClasses.PopUpMediaInfo;

/**
 * Helper class for parsing pop-up configuration XML file.
 */
public final class PopUpInfoParser
{
    public static function parsePopUpInfo(popUpXML:XML):PopUpInfo
    {
        var popUpInfo:PopUpInfo = new PopUpInfo();

        if (popUpXML.title[0])
        {
            popUpInfo.title = popUpXML.title;
        }

        if (popUpXML.description[0])
        {
            popUpInfo.description = popUpXML.description;
        }

        if (popUpXML.fields[0])
        {
            popUpInfo.popUpFieldInfos = parsePopUpFields(popUpXML.fields[0]);
        }

        if (popUpXML.medias[0])
        {
            popUpInfo.popUpMediaInfos = parsePopUpMedias(popUpXML.medias[0]);
        }

        if (popUpXML.showattachments[0])
        {
            popUpInfo.showAttachments = (popUpXML.showattachments == "true");
        }

        if (popUpXML.showrelatedrecords[0])
        {
            popUpInfo.showRelatedRecords = (popUpXML.showrelatedrecords == "true");
        }

        if (popUpXML.showzoomtobutton[0])
        {
            popUpInfo.showZoomToButton = (popUpXML.showzoomtobutton == "true");
        }

        return popUpInfo;
    }

    public static function parsePopUpFields(fieldsXML:XML):Array
    {
        var fields:Array = [];
        var field:PopUpFieldInfo;

        for each (var fieldXML:XML in fieldsXML.field)
        {
            field = new PopUpFieldInfo();
            field.label = fieldXML.@alias[0] || fieldXML.@label[0];
            field.fieldName = fieldXML.@name;
            field.visible = fieldXML.@visible == "true";
            if (fieldXML.format[0])
            {
                field.format = parsePopUpFieldFormat(fieldXML.format[0]);
            }

            fields.push(field);
        }

        return fields;
    }

    public static function parsePopUpFieldFormat(formatXML:XML):PopUpFieldFormat
    {
        var popUpFieldFormat:PopUpFieldFormat = new PopUpFieldFormat();

        if (formatXML.@dateformat[0])
        {
            popUpFieldFormat.dateFormat = formatXML.@dateformat;
        }
        popUpFieldFormat.precision = formatXML.@precision[0] ? formatXML.@precision : -1;
        popUpFieldFormat.useThousandsSeparator = (formatXML.@usethousandsseparator == "true");
        popUpFieldFormat.useUTC = (formatXML.@useutc == "true");

        return popUpFieldFormat;
    }

    public static function parsePopUpMedias(mediasXML:XML):Array
    {
        var medias:Array = [];
        var media:PopUpMediaInfo;

        for each (var mediaXML:XML in mediasXML.media)
        {
            media = new PopUpMediaInfo();

            media.caption = mediaXML.@caption;
            if (mediaXML.@chartfields[0])
            {
                media.chartFields = mediaXML.@chartfields.split(',');
            }
            media.chartNormalizationField = mediaXML.@chartnormalizationfield;
            media.imageLinkURL = mediaXML.@imagelinkurl;
            media.imageSourceURL = mediaXML.@imagesourceurl;
            media.title = mediaXML.@title;
            media.type = mapMediaType(mediaXML.@type);

            medias.push(media);
        }

        return medias;
    }

    private static function mapMediaType(type:String):String
    {
        var mediaType:String;

        switch (type)
        {
            case "image":
            {
                mediaType = PopUpMediaInfo.IMAGE;
                break;
            }
            case "barchart":
            {
                mediaType = PopUpMediaInfo.BAR_CHART;
                break;
            }
            case "columnchart":
            {
                mediaType = PopUpMediaInfo.COLUMN_CHART;
                break;
            }
            case "linechart":
            {
                mediaType = PopUpMediaInfo.LINE_CHART;
                break;
            }
            case "piechart":
            {
                mediaType = PopUpMediaInfo.PIE_CHART;
                break;
            }
        }

        return mediaType;
    }
}
}
