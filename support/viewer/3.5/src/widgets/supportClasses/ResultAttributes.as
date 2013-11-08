package widgets.supportClasses
{

import com.esri.ags.FeatureSet;
import com.esri.ags.Graphic;
import com.esri.ags.layers.FeatureLayer;
import com.esri.ags.layers.supportClasses.CodedValue;
import com.esri.ags.layers.supportClasses.CodedValueDomain;
import com.esri.ags.layers.supportClasses.FeatureType;
import com.esri.ags.layers.supportClasses.Field;
import com.esri.ags.layers.supportClasses.LayerDetails;

import mx.core.FlexGlobals;
import mx.core.LayoutDirection;
import mx.formatters.DateFormatter;

[Bindable]
public class ResultAttributes
{
    public var attributes:Object;
    public var title:String;
    public var content:String;
    public var link:String;
    public var linkAlias:String;

    public static function toResultAttributes(fields:XMLList,
                                              graphic:Graphic = null,
                                              featureSet:FeatureSet = null,
                                              layer:FeatureLayer = null,
                                              layerDetails:LayerDetails = null,
                                              fallbackTitle:String = null,
                                              titleField:String = null,
                                              linkField:String = null,
                                              linkAlias:String = null):ResultAttributes
    {
        var resultAttributes:ResultAttributes = new ResultAttributes;

        var value:String = "";
        var title:String = "";
        var content:String = "";
        var link:String = "";
        var linkAlias:String;

        var fieldsXMLList:XMLList = fields ? fields.field : null;
        if (fields && fields[0].@all[0] == "true")
        {
            if (layerDetails.fields)
            {
                for each (var field:Field in layerDetails.fields)
                {
                    if (field.name in graphic.attributes)
                    {
                        displayFields(field.name, getFieldXML(field.name, fieldsXMLList), field);
                    }
                }
            }
            else
            {
                for (var fieldName:String in graphic.attributes)
                {
                    displayFields(fieldName, getFieldXML(fieldName, fieldsXMLList), null);
                }
            }
        }
        else
        {
            for each (var fieldXML:XML in fieldsXMLList) // display the fields in the same order as specified
            {
                if (fieldXML.@name[0] in graphic.attributes)
                {
                    displayFields(fieldXML.@name[0], fieldXML, getField(layer, fieldXML.@name[0]));
                }
            }
        }

        resultAttributes.attributes = graphic.attributes;
        resultAttributes.title = title ? title : fallbackTitle;
        resultAttributes.content = content.replace(/\n$/, '');
        resultAttributes.link = link ? link : null;
        resultAttributes.linkAlias = linkAlias;

        function displayFields(fieldName:String, fieldXML:XML, field:Field):void
        {
            var fieldNameTextValue:String = graphic.attributes[fieldName];
            value = fieldNameTextValue ? fieldNameTextValue : "";

            if (value)
            {
                var isDateField:Boolean;
                var useUTC:Boolean;
                var dateFormat:String;
                if (fieldXML)
                {
                    useUTC = fieldXML.format.@useutc[0] == "true" || fieldXML.@useutc[0] == "true";
                    dateFormat = fieldXML.format.@dateformat[0] || fieldXML.@dateformat[0];
                    if (dateFormat)
                    {
                        isDateField = true;
                    }
                }
                if (!isDateField && field)
                {
                    isDateField = field.type == Field.TYPE_DATE;
                }
                if (isDateField)
                {
                    var dateMS:Number = Number(value);
                    if (!isNaN(dateMS))
                    {
                        value = msToDate(dateMS, dateFormat, useUTC);
                    }
                }
                else
                {
                    var typeID:String = layerDetails.typeIdField ? graphic.attributes[layerDetails.typeIdField] : null;
                    if (fieldName == layerDetails.typeIdField)
                    {
                        var featureType:FeatureType = getFeatureType(layer, typeID);
                        if (featureType && featureType.name)
                        {
                            value = featureType.name;
                        }
                    }
                    else
                    {
                        var codedValue:CodedValue = getCodedValue(layer, fieldName, value, typeID);
                        if (codedValue)
                        {
                            value = codedValue.name;
                        }
                    }
                }
            }

            var upperCaseFieldName:String = fieldName.toUpperCase();
            if (titleField && upperCaseFieldName == titleField.toUpperCase())
            {
                title = value;
            }
            else if (linkField && upperCaseFieldName == linkField.toUpperCase())
            {
                link = value;
                linkAlias = linkAlias;
            }
            else
            {
                var isShapeMeasurementField:Boolean = (upperCaseFieldName == "SHAPE_LENGTH" || upperCaseFieldName == "SHAPE_AREA");
                var isUserDefinedField:Boolean = fields && fields.field.(@name == fieldName).length() > 0;
                if (!isShapeMeasurementField || isUserDefinedField)
                {
                    var fieldLabel:String;

                    if (fieldXML && fieldXML.@alias[0])
                    {
                        fieldLabel = fieldXML.@alias[0];
                    }
                    else
                    {
                        fieldLabel = featureSet.fieldAliases[fieldName];
                    }

                    if (FlexGlobals.topLevelApplication.layoutDirection == LayoutDirection.RTL)
                    {
                        content += value + " :" + fieldLabel + "\n";
                    }
                    else
                    {
                        content += fieldLabel + ": " + value + "\n";
                    }
                }
            }
        }

        return resultAttributes;
    }

    private static function getFieldXML(fieldName:String, fields:XMLList):XML
    {
        var result:XML;

        for each (var fieldXML:XML in fields)
        {
            if (fieldName == fieldXML.@name[0])
            {
                result = fieldXML;
                break;
            }
        }

        return result;
    }

    private static function getField(layer:FeatureLayer, fieldName:String):Field
    {
        var result:Field;

        if (layer)
        {
            for each (var field:Field in layer.layerDetails.fields)
            {
                if (fieldName == field.name)
                {
                    result = field;
                    break;
                }
            }
        }

        return result;
    }

    private static function getFeatureType(layer:FeatureLayer, typeID:String):FeatureType
    {
        var result:FeatureType;

        if (layer)
        {
            for each (var featureType:FeatureType in layer.layerDetails.types)
            {
                if (typeID == featureType.id)
                {
                    result = featureType;
                    break;
                }
            }
        }

        return result;
    }

    private static function msToDate(ms:Number, dateFormat:String, useUTC:Boolean):String
    {
        var date:Date = new Date(ms);
        if (date.milliseconds == 999) // workaround for REST bug
        {
            date.milliseconds++;
        }
        if (useUTC)
        {
            date.minutes += date.timezoneOffset;
        }

        if (dateFormat)
        {
            var dateFormatter:DateFormatter = new DateFormatter();
            dateFormatter.formatString = dateFormat;
            var result:String = dateFormatter.format(date);
            if (result)
            {
                return result;
            }
            else
            {
                return dateFormatter.error;
            }
        }
        else
        {
            return date.toLocaleString();
        }
    }

    private static function getCodedValue(layer:FeatureLayer, fieldName:String, fieldValue:String, typeID:String):CodedValue
    {
        var result:CodedValue;

        var codedValueDomain:CodedValueDomain;
        if (typeID)
        {
            var featureType:FeatureType = getFeatureType(layer, typeID);
            if (featureType)
            {
                codedValueDomain = featureType.domains[fieldName] as CodedValueDomain;
            }
        }
        else
        {
            var field:Field = getField(layer, fieldName);
            if (field)
            {
                codedValueDomain = field.domain as CodedValueDomain;
            }
        }

        if (codedValueDomain)
        {
            for each (var codedValue:CodedValue in codedValueDomain.codedValues)
            {
                if (fieldValue == codedValue.code)
                {
                    result = codedValue;
                    break;
                }
            }
        }

        return result;
    }
}

}
