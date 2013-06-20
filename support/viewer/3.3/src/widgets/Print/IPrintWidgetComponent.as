package widgets.Print
{

import com.esri.viewer.BaseWidget;

import mx.core.IVisualElement;

public interface IPrintWidgetComponent extends IVisualElement
{
    function get configXML():XML;
    function set configXML(value:XML):void;

    function get hostBaseWidget():BaseWidget;
    function set hostBaseWidget(value:BaseWidget):void;
}
}
