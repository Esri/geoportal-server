package widgets.supportClasses
{

import com.esri.ags.FeatureSet;
import com.esri.ags.layers.supportClasses.Field;

public class FeatureSetUtil
{
    public static function sortFeaturesByFieldName(featureSet:FeatureSet, orderByFields:Array):void
    {
        if (!featureSet || !orderByFields)
        {
            return;
        }

        const sortedFeatures:Array = [];

        const features:Array = featureSet.features;
        const attributes:Array = featureSet.attributes;

        if (attributes && attributes.length > 1
            && features && features.length > 1)
        {
            const fieldNames:Array = [];
            const fieldSortingOptions:Array = [];

            var fieldAndOrderPair:Array;
            for each (var fieldAndOrder:String in orderByFields)
            {
                fieldAndOrderPair = fieldAndOrder.split(" ");

                fieldNames.push(fieldAndOrderPair[0]);
                if (fieldAndOrderPair.length > 1)
                {
                    fieldSortingOptions.push(
                        getSortingOptions(featureSet,
                                          fieldAndOrderPair[0],
                                          toArraySortingOrder(fieldAndOrderPair[1])));
                }
                else
                {
                    fieldSortingOptions.push(
                        getSortingOptions(featureSet,
                                          fieldAndOrderPair[0])); //default if missing
                }
            }

            const sortedAttributeIndices:Array =
                attributes.sortOn(fieldNames, fieldSortingOptions);

            //attributes & features should have matching order
            for each (var sortedAttributeIndex:int in sortedAttributeIndices)
            {
                sortedFeatures.push(features[sortedAttributeIndex]);
            }

            featureSet.features = sortedFeatures;
        }
    }

    private static function getSortingOptions(featureSet:FeatureSet, orderByFieldName:String, sortOrder:uint = 0):uint
    {
        return getFieldSortType(featureSet.fields, orderByFieldName) | Array.RETURNINDEXEDARRAY | sortOrder;
    }

    private static function getFieldSortType(fields:Array, orderByFieldName:String):uint
    {
        var sortField:Field;
        for each (var field:Field in fields)
        {
            if (field.name == orderByFieldName)
            {
                sortField = field;
                break;
            }
        }

        const isFieldNumeric:Boolean =
            sortField
            && (sortField.type == Field.TYPE_DOUBLE || sortField.type == Field.TYPE_INTEGER
            || sortField.type == Field.TYPE_SINGLE || sortField.type == Field.TYPE_SMALL_INTEGER);

        return isFieldNumeric ? Array.NUMERIC : Array.CASEINSENSITIVE;
    }

    private static function toArraySortingOrder(sortOrder:String):uint
    {
        return (sortOrder == "DESC") ? Array.DESCENDING : 0;
    }
}

}
