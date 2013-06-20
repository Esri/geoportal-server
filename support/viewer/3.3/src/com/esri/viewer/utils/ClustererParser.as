package com.esri.viewer.utils
{

import com.esri.ags.clusterers.ESRIClusterer;
import com.esri.ags.clusterers.WeightedClusterer;
import com.esri.ags.clusterers.supportClasses.FlareSymbol;
import com.esri.ags.clusterers.supportClasses.SimpleClusterSymbol;
import com.esri.ags.symbols.Symbol;

import flash.text.TextFormat;

public class ClustererParser
{
    public function parseClusterer(clusteringXML:XML):ESRIClusterer
    {
        var clusterer:ESRIClusterer;

        if (clusteringXML)
        {
            clusterer = new WeightedClusterer();

            if (clusteringXML.@mingraphiccount[0])
            {
                var minGraphicCount:int = parseInt(clusteringXML.@mingraphiccount[0]);
                if (!isNaN(minGraphicCount))
                {
                    clusterer.minGraphicCount = minGraphicCount;
                }
            }
            if (clusteringXML.@sizeinpixels[0])
            {
                var sizeInPixels:Number = parseFloat(clusteringXML.@sizeinpixels[0]);
                if (!isNaN(sizeInPixels))
                {
                    clusterer.sizeInPixels = sizeInPixels;
                }
            }

            clusterer.symbol = parseClusterSymbol(clusteringXML.clustersymbol[0]);
        }

        return clusterer;
    }

    private function parseClusterSymbol(clusterSymbolXML:XML):Symbol
    {
        var clusterSymbol:Symbol;

        var shouldParseSimpleClusterSymbol:Boolean = clusterSymbolXML && (clusterSymbolXML.@type == "simple");
        if (shouldParseSimpleClusterSymbol)
        {
            clusterSymbol = parseSimpleClusterSymbol(clusterSymbolXML);
        }
        else
        {
            clusterSymbol = parseFlareSymbol(clusterSymbolXML);
        }

        return clusterSymbol;
    }

    private function parseSimpleClusterSymbol(clusterSymbolXML:XML):Symbol
    {
        var simpleClusterSymbol:SimpleClusterSymbol = new SimpleClusterSymbol();

        if (clusterSymbolXML.@alpha[0])
        {
            var alpha:Number = parseFloat(clusterSymbolXML.@alpha[0]);
            if (!isNaN(alpha))
            {
                simpleClusterSymbol.alpha = alpha;
            }
        }
        if (clusterSymbolXML.@color[0])
        {
            var color:uint = parseInt(clusterSymbolXML.@color[0]);
            if (!isNaN(color))
            {
                simpleClusterSymbol.color = color;
            }
        }
        if (clusterSymbolXML.@size[0])
        {
            var size:Number = parseFloat(clusterSymbolXML.@size[0]);
            if (!isNaN(size))
            {
                simpleClusterSymbol.size = size;
            }
        }
        if (clusterSymbolXML.@alphas[0])
        {
            simpleClusterSymbol.alphas = parseAlphas(clusterSymbolXML.@alphas[0]);
        }
        if (clusterSymbolXML.@sizes[0])
        {
            simpleClusterSymbol.sizes = parseSizes(clusterSymbolXML.@sizes[0]);
        }
        if (clusterSymbolXML.@weights[0])
        {
            simpleClusterSymbol.weights = parseWeights(clusterSymbolXML.@weights[0]);
        }
        if (clusterSymbolXML.@colors[0])
        {
            simpleClusterSymbol.colors = parseColors(clusterSymbolXML.@colors[0]);
        }
        var textFormat:TextFormat = parseTextFormat(clusterSymbolXML);
        simpleClusterSymbol.textFormat = textFormat;

        return simpleClusterSymbol;
    }

    private function parseAlphas(delimitedAlphas:String):Array
    {
        var alphas:Array = [];
        var alphasToParse:Array = delimitedAlphas.split(',');
        var alpha:Number;
        for each (var alphaToParse:String in alphasToParse)
        {
            alpha = parseFloat(alphaToParse);
            if (isNaN(alpha))
            {
                alphas.push(1);
            }
            else
            {
                alphas.push(alpha);
            }
        }

        return alphas;
    }

    private function parseSizes(delimitedSizes:String):Array
    {
        var sizes:Array = [];
        var sizesToParse:Array = delimitedSizes.split(',');
        var size:Number;
        for each (var sizeToParse:String in sizesToParse)
        {
            size = parseFloat(sizeToParse);
            if (isNaN(size))
            {
                sizes.push(0);
            }
            else
            {
                sizes.push(size);
            }
        }

        return sizes;
    }

    private function parseWeights(delimitedWeights:String):Array
    {
        var weights:Array = [];
        var weightsToParse:Array = delimitedWeights.split(',');
        var weight:Number;
        for each (var weightToParse:String in weightsToParse)
        {
            weight = parseFloat(weightToParse);
            if (isNaN(weight))
            {
                weights.push(0);
            }
            else
            {
                weights.push(weight);
            }
        }

        return weights;
    }

    private function parseColors(delimitedColors:String):Array
    {
        var colors:Array = [];
        var colorsToParse:Array = delimitedColors.split(',');
        var color:uint;
        for each (var colorToParse:String in colorsToParse)
        {
            color = parseInt(colorToParse);

            if (isNaN(color))
            {
                colors.push(0);
            }
            else
            {
                colors.push(color);
            }
        }
        return colors;
    }

    private function parseTextFormat(clusterSymbolXML:XML):TextFormat
    {
        var textFormat:TextFormat = new TextFormat();

        if (clusterSymbolXML.@textcolor[0])
        {
            var color:uint = parseInt(clusterSymbolXML.@textcolor);
            if (!isNaN(color))
            {
                textFormat.color = color;
            }
        }
        if (clusterSymbolXML.@textsize[0])
        {
            var size:int = parseInt(clusterSymbolXML.@textsize);
            if (!isNaN(size))
            {
                textFormat.size = size;
            }
        }

        return textFormat;
    }

    private function parseFlareSymbol(flareSymbolXML:XML):Symbol
    {
        var flareSymbol:FlareSymbol = new FlareSymbol();

        if (flareSymbolXML)
        {
            if (flareSymbolXML.@alpha[0])
            {
                var alpha:Number = parseFloat(flareSymbolXML.@alpha[0]);
                if (!isNaN(alpha))
                {
                    flareSymbol.backgroundAlpha = alpha;
                }
            }
            if (flareSymbolXML.@color[0])
            {
                var color:uint = parseInt(flareSymbolXML.@color[0]);
                if (!isNaN(color))
                {
                    flareSymbol.backgroundColor = color;
                }
            }
            if (flareSymbolXML.@bordercolor[0])
            {
                var borderColor:uint = parseInt(flareSymbolXML.@bordercolor[0]);
                if (!isNaN(borderColor))
                {
                    flareSymbol.borderColor = borderColor;
                }
            }
            if (flareSymbolXML.@flaremaxcount[0])
            {
                var flareMaxCount:int = parseInt(flareSymbolXML.@flaremaxcount[0]);
                if (!isNaN(flareMaxCount))
                {
                    flareSymbol.flareMaxCount = flareMaxCount;
                }
            }
            if (flareSymbolXML.@size[0])
            {
                var size:Number = parseFloat(flareSymbolXML.@size[0]);
                if (!isNaN(size))
                {
                    flareSymbol.size = size;
                }
            }
            if (flareSymbolXML.@alphas[0])
            {
                flareSymbol.backgroundAlphas = parseAlphas(flareSymbolXML.@alphas[0]);
            }
            if (flareSymbolXML.@sizes[0])
            {
                flareSymbol.sizes = parseSizes(flareSymbolXML.@sizes[0]);
            }
            if (flareSymbolXML.@weights[0])
            {
                flareSymbol.weights = parseWeights(flareSymbolXML.@weights[0]);
            }
            if (flareSymbolXML.@colors[0])
            {
                flareSymbol.backgroundColors = parseColors(flareSymbolXML.@colors[0]);
            }

            flareSymbol.textFormat = parseTextFormat(flareSymbolXML);
        }

        return flareSymbol;
    }
}
}
