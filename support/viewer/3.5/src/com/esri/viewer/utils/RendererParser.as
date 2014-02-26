package com.esri.viewer.utils
{

import com.esri.ags.renderers.ClassBreaksRenderer;
import com.esri.ags.renderers.IRenderer;
import com.esri.ags.renderers.SimpleRenderer;
import com.esri.ags.renderers.UniqueValueRenderer;
import com.esri.ags.renderers.supportClasses.ClassBreakInfo;
import com.esri.ags.renderers.supportClasses.UniqueValueInfo;

public class RendererParser
{
    private var symbolParser:SymbolParser;

    public function RendererParser(symbolParser:SymbolParser = null)
    {
        this.symbolParser = symbolParser ? symbolParser : new SymbolParser();
    }

    public function parseRenderer(rendererXML:XML):IRenderer
    {
        var renderer:IRenderer;

        if (rendererXML)
        {
            if (rendererXML.simplerenderer[0])
            {
                renderer = new SimpleRenderer(symbolParser.parseSymbol(rendererXML.simplerenderer[0]));
            }
            else if (rendererXML.classbreaksrenderer[0])
            {
                renderer = parseClassBreaksRenderer(rendererXML.classbreaksrenderer[0]);
            }
            else if (rendererXML.uniquevaluerenderer[0])
            {
                renderer = parseUniqueValueRenderer(rendererXML.uniquevaluerenderer[0]);
            }
        }

        return renderer;
    }

    private function parseClassBreaksRenderer(rendererXML:XML):IRenderer
    {
        var cbRenderer:ClassBreaksRenderer = new ClassBreaksRenderer(rendererXML.@field[0],
                                                                     symbolParser.parseSymbol(rendererXML),
                                                                     parseClassBreakInfos(rendererXML.classbreakinfo));
        if (rendererXML.defaultlabel[0])
        {
            cbRenderer.defaultLabel = rendererXML.defaultlabel;
        }

        return cbRenderer;
    }

    private function parseClassBreakInfos(classBreaksInfosXMLList:XMLList):Array
    {
        var classBreakInfos:Array = [];

        for each (var classBreaksInfoXML:XML in classBreaksInfosXMLList)
        {
            const classBreakInfo:ClassBreakInfo = new ClassBreakInfo(symbolParser.parseSymbol(classBreaksInfoXML),
                                                                     classBreaksInfoXML.@min,
                                                                     classBreaksInfoXML.@max);
            classBreakInfo.label = classBreaksInfoXML.@label;
            classBreakInfos.push(classBreakInfo);
        }

        return classBreakInfos;
    }

    private function parseUniqueValueRenderer(rendererXML:XML):IRenderer
    {
        var uvRenderer:UniqueValueRenderer = new UniqueValueRenderer(rendererXML.@field,
                                                                     symbolParser.parseSymbol(rendererXML),
                                                                     parseUniqueValueInfos(rendererXML.uniquevalueinfo));
        if (rendererXML.@field2[0])
        {
            uvRenderer.field2 = rendererXML.@field2;
        }
        if (rendererXML.@field3[0])
        {
            uvRenderer.field3 = rendererXML.@field3;
        }
        if (rendererXML.@fielddelimiter[0])
        {
            uvRenderer.fieldDelimiter = rendererXML.@fielddelimiter;
        }
        if (rendererXML.defaultlabel[0])
        {
            uvRenderer.defaultLabel = rendererXML.defaultlabel;
        }

        return uvRenderer;
    }

    private function parseUniqueValueInfos(uniqueValueInfosXMLList:XMLList):Array
    {
        var uniqueValueInfos:Array = [];

        for each (var uniqueValueInfoXML:XML in uniqueValueInfosXMLList)
        {
            const uniqueValueInfo:UniqueValueInfo = new UniqueValueInfo(symbolParser.parseSymbol(uniqueValueInfoXML),
                                                                        uniqueValueInfoXML.@value);
            uniqueValueInfo.label = uniqueValueInfoXML.@label;
            uniqueValueInfos.push(uniqueValueInfo);
        }

        return uniqueValueInfos;
    }
}
}
