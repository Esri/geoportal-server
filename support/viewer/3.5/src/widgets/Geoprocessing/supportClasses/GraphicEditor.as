package widgets.Geoprocessing.supportClasses
{

import com.esri.ags.Graphic;
import com.esri.ags.Map;
import com.esri.ags.events.EditEvent;
import com.esri.ags.geometry.Extent;
import com.esri.ags.geometry.Polygon;
import com.esri.ags.geometry.Polyline;
import com.esri.ags.tasks.GeometryServiceSingleton;
import com.esri.ags.tools.EditTool;
import com.esri.ags.utils.GeometryUtil;

import flash.events.EventDispatcher;
import flash.events.MouseEvent;

import mx.rpc.AsyncResponder;
import mx.rpc.Fault;
import mx.rpc.events.FaultEvent;

public class GraphicEditor extends EventDispatcher
{
    private var graphicToEdit:Graphic;
    private var lastActiveEditType:String;
    private var isEditing:Boolean;
    private var map:Map;
    private var editTool:EditTool;

    public function GraphicEditor(map:Map)
    {
        this.map = map;
        editTool = new EditTool(map);
        addEditToolEventListeners();
    }

    public function makeGraphicEditable(graphic:Graphic):void
    {
        graphic.addEventListener(MouseEvent.CLICK, graphic_clickHandler, false, 0, true);
    }

    private function graphic_clickHandler(event:MouseEvent):void
    {
        if (graphicToEdit !== Graphic(event.currentTarget))
        {
            graphicToEdit = Graphic(event.currentTarget);
            lastActiveEditType = "none"; // make sure move and edit vertices is the 1st mode
        }
        if (graphicToEdit.geometry is Polyline || graphicToEdit.geometry is Polygon)
        {
            if (lastActiveEditType == "none")
            {
                isEditing = true;
                lastActiveEditType = "moveEditVertices";
                editTool.activate(EditTool.MOVE | EditTool.EDIT_VERTICES, [ graphicToEdit ]);
            }
            else if (lastActiveEditType == "moveEditVertices")
            {
                isEditing = true;
                lastActiveEditType = "moveRotateScale";
                editTool.activate(EditTool.MOVE | EditTool.SCALE | EditTool.ROTATE, [ graphicToEdit ]);
            }
            else if (lastActiveEditType == "moveRotateScale")
            {
                isEditing = false;
                graphicToEdit = null;
                lastActiveEditType = "none";
                editTool.deactivate();
            }
        }
        else if (graphicToEdit.geometry is Extent)
        {
            if (lastActiveEditType == "none")
            {
                isEditing = true;
                lastActiveEditType = "moveScale";
                editTool.activate(EditTool.MOVE | EditTool.SCALE, [ graphicToEdit ]);
            }
            else
            {
                isEditing = false;
                graphicToEdit = null;
                lastActiveEditType = "none";
                editTool.deactivate();
            }
        }
        else
        {
            if (lastActiveEditType == "none")
            {
                isEditing = true;
                lastActiveEditType = "moveEditVertices";
                editTool.activate(EditTool.EDIT_VERTICES | EditTool.MOVE, [ graphicToEdit ]);
            }
            else
            {
                isEditing = false;
                graphicToEdit = null;
                lastActiveEditType = "none";
                editTool.deactivate();
            }
        }

        removeEditToolEventListeners();
        map.removeEventListener(MouseEvent.MOUSE_DOWN, map_mouseDownHandler);

        if (graphicToEdit)
        {
            addEditToolEventListeners();
            map.addEventListener(MouseEvent.MOUSE_DOWN, map_mouseDownHandler);
        }
    }

    private function removeEditToolEventListeners():void
    {
        editTool.removeEventListener(EditEvent.VERTEX_ADD, editTool_vertexAddDeleteHandler);
        editTool.removeEventListener(EditEvent.VERTEX_DELETE, editTool_vertexAddDeleteHandler);
        editTool.removeEventListener(EditEvent.VERTEX_MOVE_STOP, editTool_vertexMoveStopHandler);

        editTool.removeEventListener(EditEvent.GRAPHICS_MOVE_STOP, editTool_graphicsMoveStopHandler);
        editTool.removeEventListener(EditEvent.GRAPHIC_ROTATE_STOP, editTool_graphicRotateStopHandler);
        editTool.removeEventListener(EditEvent.GRAPHIC_SCALE_STOP, editTool_graphicScaleStopHandler);
    }

    private function editTool_vertexAddDeleteHandler(event:EditEvent):void
    {
        if (map.wrapAround180)
        {
            normalizeGraphicGeometry(event.graphic);
        }
    }

    private function normalizeGraphicGeometry(graphic:Graphic):void
    {
        // normalize
        GeometryUtil.normalizeCentralMeridian([ graphic.geometry ], GeometryServiceSingleton.instance, new AsyncResponder(getNormalizedGeometryFunction, faultFunction));
        function getNormalizedGeometryFunction(item:Object, token:Object = null):void
        {
            var normalizedGeometries:Array = item as Array;
            graphic.geometry = normalizedGeometries[0];
            isEditing = true;
            if (graphic.geometry is Polyline || graphic.geometry is Polygon)
            {
                if (lastActiveEditType == "moveEditVertices")
                {
                    editTool.activate(EditTool.MOVE | EditTool.EDIT_VERTICES, [ graphic ]);
                }
                else if (lastActiveEditType == "moveRotateScale")
                {
                    editTool.activate(EditTool.MOVE | EditTool.SCALE | EditTool.ROTATE, [ graphic ]);
                }
            }
            else if (graphic.geometry is Extent)
            {
                if (lastActiveEditType == "moveScale")
                {
                    editTool.activate(EditTool.MOVE | EditTool.SCALE, [ graphic ]);
                }
            }
            else
            {
                if (lastActiveEditType == "moveEditVertices")
                {
                    editTool.activate(EditTool.EDIT_VERTICES | EditTool.MOVE, [ graphic ]);
                }
            }
        }
        function faultFunction(fault:Fault, token:Object = null):void
        {
            dispatchEvent(new FaultEvent(FaultEvent.FAULT, false, false, fault));
        }
    }

    private function editTool_vertexMoveStopHandler(event:EditEvent):void
    {
        if (map.wrapAround180)
        {
            normalizeGraphicGeometry(event.graphic);
        }
    }

    private function editTool_graphicsMoveStopHandler(event:EditEvent):void
    {
        if (map.wrapAround180)
        {
            normalizeGraphicGeometry(event.graphics[0]);
        }
    }

    private function editTool_graphicRotateStopHandler(event:EditEvent):void
    {
        if (map.wrapAround180)
        {
            normalizeGraphicGeometry(event.graphic);
        }
    }

    private function editTool_graphicScaleStopHandler(event:EditEvent):void
    {
        if (map.wrapAround180)
        {
            normalizeGraphicGeometry(event.graphic);
        }
    }

    private function map_mouseDownHandler(event:MouseEvent):void
    {
        map.removeEventListener(MouseEvent.MOUSE_DOWN, map_mouseDownHandler);
        map.addEventListener(MouseEvent.MOUSE_UP, map_mouseUpHandler);
        map.addEventListener(MouseEvent.MOUSE_MOVE, map_mouseMoveHandler);
    }

    private function map_mouseUpHandler(event:MouseEvent):void
    {
        if (event.target !== graphicToEdit)
        {
            map.removeEventListener(MouseEvent.MOUSE_UP, map_mouseUpHandler);
            map.removeEventListener(MouseEvent.MOUSE_MOVE, map_mouseMoveHandler);

            isEditing = false;
            graphicToEdit = null;
            lastActiveEditType = "none";
            editTool.deactivate();
        }
    }

    private function map_mouseMoveHandler(event:MouseEvent):void
    {
        map.addEventListener(MouseEvent.MOUSE_DOWN, map_mouseDownHandler);
        map.removeEventListener(MouseEvent.MOUSE_UP, map_mouseUpHandler);
        map.removeEventListener(MouseEvent.MOUSE_MOVE, map_mouseMoveHandler);
    }

    private function addEditToolEventListeners():void
    {
        editTool.addEventListener(EditEvent.VERTEX_ADD, editTool_vertexAddDeleteHandler);
        editTool.addEventListener(EditEvent.VERTEX_DELETE, editTool_vertexAddDeleteHandler);
        editTool.addEventListener(EditEvent.VERTEX_MOVE_STOP, editTool_vertexMoveStopHandler);

        editTool.addEventListener(EditEvent.GRAPHICS_MOVE_STOP, editTool_graphicsMoveStopHandler);
        editTool.addEventListener(EditEvent.GRAPHIC_ROTATE_STOP, editTool_graphicRotateStopHandler);
        editTool.addEventListener(EditEvent.GRAPHIC_SCALE_STOP, editTool_graphicScaleStopHandler);
    }
}
}
