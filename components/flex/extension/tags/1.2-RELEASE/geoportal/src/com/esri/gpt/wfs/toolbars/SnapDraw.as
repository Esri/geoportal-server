/* Copyright 2008 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 * See use restrictions at http://resources.esri.com/help/9.3/usagerestrictions.htm.
 */
package com.esri.gpt.wfs.toolbars
{
	import com.esri.ags.Graphic;
	import com.esri.ags.Map;
	import com.esri.ags.events.DrawEvent;
	import com.esri.ags.geometry.Extent;
	import com.esri.ags.geometry.GeomUtils;
	import com.esri.ags.geometry.MapPoint;
	import com.esri.ags.geometry.Multipoint;
	import com.esri.ags.geometry.Polygon;
	import com.esri.ags.geometry.Polyline;
	import com.esri.ags.layers.GraphicsLayer;
	import com.esri.ags.symbol.SimpleFillSymbol;
	import com.esri.ags.symbol.SimpleLineSymbol;
	import com.esri.ags.symbol.SimpleMarkerSymbol;
	import com.esri.ags.toolbars.BaseToolbar;
	
	import flash.display.Graphics;
	import flash.events.MouseEvent;
	import flash.utils.getTimer;

	/**
	 * The drawEnd event is dispatched when the drawing is complete.
	 * @eventType com.esri.ags.events.DrawEvent.DRAW_END
	 */
	[Event(name="drawEnd", type="com.esri.ags.events.DrawEvent")]
	
	/**
	 * The drawStart event is dispatched when the drawing starts.
	 * @eventType com.esri.ags.events.DrawEvent.DRAW_START
	 */
	[Event(name="drawStart", type="com.esri.ags.events.DrawEvent")]
	
	/**
	 * This toolbar is based entirely off the built-in Draw toolbar provided by the ArcGIS Flex API. It is
	 * meant as a prototype to test out a drawing filter function.
	 * Toolbar that supports functionality to draw different geometries:
	 * points (MAPPOINT or MULTIPOINT), lines (LINE, POLYLINE, or FREEHAND_POLYLINE),
	 * polygons (FREEHAND_POLYGON or POLYGON), or rectangles (EXTENT).
	 *
	 * @example If you specify a graphicsLayer, the features will automatically be added to that graphicsLayer:
	 * <listing version="3.0">
	 * &lt;esri:Draw id="myDrawToolbar" map="{myMap}" graphicsLayer="{myGraphicsLayer}"
	 *     markerSymbol="{sms}" lineSymbol="{sls}" fillSymbol="{sfs}" /&gt;</listing>
	 *
	 * @example If you don't explicitly specify a graphicsLayer, you will need to add the graphic yourself (if you want it to show after you've drawn it):
	 * <listing version="3.0">
	 * &lt;esri:Draw id="myDrawToolbar" map="{myMap}"
	 *     markerSymbol="{sms}" lineSymbol="{sls}" fillSymbol="{sfs}" /&gt;</listing>
	 */
	public class SnapDraw extends BaseToolbar
	{
		/**
	     * Draws a point.
	     */
	    public static const MAPPOINT:String = "mappoint";
	    /**
	     * Draws a multipoint.
	     */
	    public static const MULTIPOINT:String = "multipoint";
	    /**
	     * Draws a polyline.
	     */
	    public static const POLYLINE:String = "polyline";
	    /**
	     * Draws a freehand polyline.
	     */
	    public static const FREEHAND_POLYLINE:String = "freehandpolyline";
	    /**
	     * Draws a polygon.
	     */
	    public static const POLYGON:String = "polygon";
	    /**
	     * Draws a line.
	     */
	    public static const LINE:String = "line";
	    /**
	     * Draws a freehand polygon.
	     */
	    public static const FREEHAND_POLYGON:String = "freehandpolygon";
	    /**
	     * Draws an extent.
	     */
	    public static const EXTENT:String = "extent";
	    
	    /**
	     * Creates a new SnapDraw object.
	     * @param map Map the toolbar is associated with.
	     */
		public function SnapDraw( map :Map = null )
		{
			super( map );
		}
		
	    //--------------------------------------------------------------------------
	    //
	    //  Variables
	    //
	    //--------------------------------------------------------------------------
		
	    private var m_map :Map;
	    private var m_active :Boolean = false;
	    /** Symbol to be used when drawing a polygon or extent. */
	    private var m_fillSymbol :SimpleFillSymbol;
	    /** Symbol to be used when drawing a polyline. */
	    private var m_lineSymbol :SimpleLineSymbol;
	    /** Symbol to be used when drawing a point or multipoint. */
	    private var m_markerSymbol :SimpleMarkerSymbol;
	    /** Graphics Layer to be used with the map. */
	    private var m_graphicsLayer :GraphicsLayer;
	    /**
	     * When set to false, the geometry is modified to be topologically correct.
	     * When set to true, the input geometry is not modified.
	     */
	    private var m_respectDrawingVertexOrder :Boolean;
		/**
		 * Filter function that is called when a point is added, but before it is drawn.
		 * Method signature: myDrawFilterFunction(point:MapPoint):MapPoint
		 */
		private var m_drawFilterFunction :Function;
	
	    private var m_geometryType :String = null;
	    private var polyline :Polyline = new Polyline();
	    private var polygon :Polygon = new Polygon();
	    private var multipoint :Multipoint = new Multipoint();
	    private var lastCount :Number = 0;
	    private var graphic :Graphic = new Graphic();
	    private var gLayer :GraphicsLayer = new GraphicsLayer();
	    private var extent :Extent = new Extent();
	    private var startPt :MapPoint;
	    
	    //--------------------------------------------------------------------------
	    //
	    //  Properties
	    //
	    //--------------------------------------------------------------------------
	
	    [Bindable]
	    /** The symbol for drawing polygons. */
	    public function get fillSymbol() :SimpleFillSymbol
	    {
	        return m_fillSymbol;
	    }
	    /** @private */
	    public function set fillSymbol( value :SimpleFillSymbol ) :void
	    {
	        if( value != m_fillSymbol )
	        {
	            m_fillSymbol = value;
	        }
	    }
	
	    [Bindable]
	    /** The symbol for drawing lines. */
	    public function get lineSymbol() :SimpleLineSymbol
	    {
	        return m_lineSymbol;
	    }
	    /** @private */
	    public function set lineSymbol( value :SimpleLineSymbol ) :void
	    {
	        if( value != m_lineSymbol )
	        {
	            m_lineSymbol = value;
	        }
	    }
	
		[Bindable]
	    /** The symbol for drawing points. */
	    public function get markerSymbol() :SimpleMarkerSymbol
	    {
	        return m_markerSymbol;
	    }
	    /** @private */
	    public function set markerSymbol( value :SimpleMarkerSymbol ) :void
	    {
	        if( value != m_markerSymbol )
	        {
	            m_markerSymbol = value;
	        }
	    }
	
	    /**
	     * When set to false, the geometry is modified to be topologically correct.
	     * When set to true, the input geometry is not modified.
	     * @default false
	     */
	    public function get respectDrawingVertexOrder():Boolean
	    {
	        return m_respectDrawingVertexOrder;
	    }
	    /** @private */
	    public function set respectDrawingVertexOrder(value:Boolean):void
	    {
	        if (value != m_respectDrawingVertexOrder)
	        {
	            m_respectDrawingVertexOrder = value;
	        }
	    }
	    
		[Bindable]
		/**
		 * Filter function that is called when a point is added, but before it is drawn.
		 * Method signature: myDrawFilterFunction(point:MapPoint):MapPoint
		 */
		public function get drawFilterFunction() :Function
		{
			return m_drawFilterFunction;
		}
		/** @private */
		public function set drawFilterFunction( value :Function ) :void
		{
			if( value != m_drawFilterFunction )
			{
				m_drawFilterFunction = value;
			}
		}
	
	    /** The graphics layer to draw the features in. */
	    public function get graphicsLayer() :GraphicsLayer
	    {
	        return m_graphicsLayer;
	    }
	    /** @private */
	    public function set graphicsLayer( value :GraphicsLayer ) :void
	    {
	        if( value != m_graphicsLayer )
	        {
	            m_graphicsLayer = value;
	        }
	    }
	
	    /** Map the toolbar is associated with. */
	    override public function get map() :Map
	    {
	        return super.map;
	    }
	
	    /** @private */
	    override public function set map( map :Map ) :void
	    {
	        resetVars();
	        super.map = map;
	    }
	    
	    //--------------------------------------------------------------------------
	    //
	    //  Methods
	    //
	    //--------------------------------------------------------------------------
	
	    /**
	     * Activates the toolbar for drawing geometries. Activating the toolbar disables map navigation.
	     * @param geometryType The geometry type. One of either POINT, LINE, POLYLINE, or POLYGON.
	     */
	    public function activate( geometryType :String ) :void
	    {
	        if( !map )
	        {
	            return;
	        }
	
	        if( !m_active )
	        {
	            deactivateMapTools( true, false, false, true );
	            m_active = true;
	        }
	
	        map.addEventListener( MouseEvent.MOUSE_DOWN, map_mouseStartDownHandler, false, 0, true );
	        switch( geometryType )
	        {
	            case MAPPOINT:
	                map.addEventListener( MouseEvent.CLICK, map_mouseClickHandler, false, 0, true );
	                break;
	            case MULTIPOINT:
	                map.addEventListener( MouseEvent.CLICK, map_mouseClickHandler, false, 0, true );
	                break;
	            case EXTENT:
	                map.addEventListener( MouseEvent.MOUSE_DOWN, map_mouseDownHandler, false,0,true );
	                break;
	            case POLYLINE:
	                map.addEventListener( MouseEvent.CLICK,map_mouseClickHandler, false, 0 , true );
	                break;
	            case LINE:
	                map.addEventListener( MouseEvent.MOUSE_DOWN,map_mouseDownHandler, false, 0 , true );
	            case FREEHAND_POLYLINE:
	                map.addEventListener( MouseEvent.MOUSE_DOWN, map_mouseDownHandler, false, 0, true );
	                break;
	            case POLYGON:
	                map.addEventListener( MouseEvent.CLICK, map_mouseClickHandler, false, 0, true );
	                break;
	            case FREEHAND_POLYGON:
	                map.addEventListener( MouseEvent.MOUSE_DOWN, map_mouseDownHandler, false, 0, true );
	                break;
	            default:
	                return;
	        }	
	        m_geometryType = geometryType;
	    }
	
	    /** Deactivates the toolbar and reactivates map navigation. */
	    public function deactivate():void
	    {
	        if( m_active )
	        {
	            deactivateListeners();
	
	            m_active = false;
	            m_geometryType = null;
	
	            activateMapTools( true, false, false, true );
	        }
	    }
		
		/** Resets the variables. */
	    private function resetVars():void
	    {
	        m_geometryType = null;
	        m_respectDrawingVertexOrder: false
	    }
	
		/** Deactivates the mouse listerners. */
	    private function deactivateListeners():void
	    {
	        if( !map )
	        {
	            return;
	        }
	
	        map.removeEventListener( MouseEvent.MOUSE_DOWN, map_mouseDownHandler );
	        map.removeEventListener( MouseEvent.MOUSE_MOVE, map_mouseMoveHandler );
	        map.removeEventListener( MouseEvent.MOUSE_UP, map_mouseUpHandler );
	        map.removeEventListener( MouseEvent.CLICK, map_mouseClickHandler );
	        map.removeEventListener( MouseEvent.CLICK, map_clickNextHandler );
	        map.removeEventListener( MouseEvent.DOUBLE_CLICK,map_doubleClickHandler );
	        map.removeEventListener( MouseEvent.MOUSE_DOWN, map_mouseStartDownHandler );
	    }
		
		/** Handles the mouse down start event. */
	    private function map_mouseStartDownHandler( event :MouseEvent ) :void
	    {
	        dispatchEvent( new DrawEvent( DrawEvent.DRAW_START, null ) );
	    }
		
		/** Handles the mouse click event. */
	    private function map_mouseClickHandler( event :MouseEvent ) :void
	    {
	        map.removeEventListener( MouseEvent.MOUSE_DOWN, map_mouseStartDownHandler );
	        
	        // grab the event point
	        var local :MapPoint = map.toMapFromStage( event.stageX, event.stageY );
	        local.spatialReference = map.spatialReference;
	        
	        // filter the event point
			if( m_drawFilterFunction != null )
			{
				local = m_drawFilterFunction( local );
			}
			
	        switch( m_geometryType )
	        {
	            case MAPPOINT:
	                graphic = new Graphic();
	                graphic.geometry = local;
	                graphic.symbol = m_markerSymbol;
	                if( m_graphicsLayer )
	                {
	                    graphicsLayer.add( graphic );
	                }
	                dispatchEvent( new DrawEvent( DrawEvent.DRAW_END, graphic ) );
	                map.addEventListener( MouseEvent.MOUSE_DOWN, map_mouseStartDownHandler );
	                break;
	            case MULTIPOINT:
	                map.removeEventListener( MouseEvent.CLICK, map_mouseClickHandler );
	                map.addEventListener( MouseEvent.CLICK, map_clickNextHandler );
	
	                multipoint = new Multipoint();
	                multipoint.points = [];
	                multipoint.points.push( local );
	
	                graphic = new Graphic();
	                graphic.geometry = multipoint;
	                graphic.symbol = m_markerSymbol;
	                addGraphic( graphic );
	                lastCount = getTimer();
	                break;
	            case POLYLINE:
	                map.removeEventListener( MouseEvent.CLICK, map_mouseClickHandler );
	                map.addEventListener( MouseEvent.MOUSE_MOVE, map_mouseMoveHandler );
	                map.addEventListener( MouseEvent.CLICK, map_clickNextHandler );
	                map.addEventListener( MouseEvent.DOUBLE_CLICK, map_doubleClickHandler );
	
	                polyline = new Polyline();
	                polyline.paths = [];
	                polyline.paths.push( [local, local] );
	
	                graphic = new Graphic();
	                graphic.geometry = polyline;
	                graphic.symbol = m_lineSymbol;
	                addGraphic( graphic );
	                break;
	            case POLYGON:
	                map.removeEventListener( MouseEvent.CLICK, map_mouseClickHandler );
	                map.addEventListener( MouseEvent.MOUSE_MOVE, map_mouseMoveHandler );
	                map.addEventListener( MouseEvent.CLICK, map_clickNextHandler );
	                map.addEventListener( MouseEvent.DOUBLE_CLICK, map_doubleClickHandler );
	                
	                polygon = new Polygon();
	                polygon.rings = [];
	                polygon.rings.push( [local, local] );
	
	                graphic = new Graphic();
	                graphic.geometry = polygon;
	                graphic.symbol = m_fillSymbol;
	                addGraphic( graphic );
	                break;
	         }
	    }
	
		/** Handles the next mouse click event. */
	    private function map_clickNextHandler( event :MouseEvent ) :void
	    {
	        var local :MapPoint = map.toMapFromStage( event.stageX, event.stageY );
	        local.spatialReference = map.spatialReference;
	        switch( m_geometryType )
	        {
	            case MULTIPOINT:
	                var duration : uint = getTimer();
	                multipoint.spatialReference = map.spatialReference;
	                if( duration - lastCount < 300 )
	                {
	                    removeLayerEndDraw();
	                    map.addEventListener( MouseEvent.MOUSE_DOWN, map_mouseStartDownHandler );
	                    map.addEventListener( MouseEvent.CLICK , map_mouseClickHandler );
	                    map.removeEventListener( MouseEvent.CLICK, map_clickNextHandler );
	                }
	                else
	                {
	                    multipoint.points.push( local );
	                    graphic.refresh();
	                }
	                lastCount = duration;
	                break;
	            case POLYLINE:
	                polyline.paths[0].push( local );
	                break;
	             case POLYGON:
	                polygon.rings[0].push( local );
	                break;
	         }
	    }
	
		/** Handles a mouse double-click event. */
	    private function map_doubleClickHandler( event :MouseEvent ) :void
	    {
	        var g :Graphics;
	        switch( m_geometryType )
	        {
	            case POLYLINE:
	                map.removeEventListener( MouseEvent.MOUSE_MOVE, map_mouseMoveHandler );
	                map.removeEventListener( MouseEvent.CLICK, map_clickNextHandler );
	                map.removeEventListener( MouseEvent.DOUBLE_CLICK, map_doubleClickHandler );
	
	                polyline.paths[0].pop();
	                if( polyline.paths[0].length >= 2 )
	                {
	                    removeLayerEndDraw();
	                }
	                else
	                {
	                    removeGraphic( graphic );
	                }
	                map.addEventListener( MouseEvent.MOUSE_DOWN, map_mouseStartDownHandler );
	                map.addEventListener( MouseEvent.CLICK , map_mouseClickHandler );
	                break;
	            case POLYGON:
	                map.removeEventListener( MouseEvent.MOUSE_MOVE, map_mouseMoveHandler );
	                map.removeEventListener( MouseEvent.CLICK, map_clickNextHandler );
	                map.removeEventListener( MouseEvent.DOUBLE_CLICK, map_doubleClickHandler );
	
	                polygon.rings[0].pop();
	                polygon.rings[0].push( polygon.rings[0][0] );
	                if( !GeomUtils.isClockwise( polygon.rings[0] ) && !m_respectDrawingVertexOrder )
	                {
	                    ( polygon.rings[0] as Array ).reverse();
	                }
	                if( polygon.rings[0].length >= 3 )
	                {
	                    removeLayerEndDraw();
	                }
	                else if( polygon.rings[0].length <= 2 )
	                {
	                    removeGraphic( graphic );
	                }
	                map.addEventListener( DrawEvent.DRAW_START, map_mouseStartDownHandler );
	                map.addEventListener( MouseEvent.CLICK , map_mouseClickHandler );
	                break;
	         }
	    }
	
		/** Handles the mouse down event. */
	    private function map_mouseDownHandler( event :MouseEvent ) :void
	    {
	        map.removeEventListener( MouseEvent.MOUSE_DOWN, map_mouseStartDownHandler );
	        var local :MapPoint = map.toMapFromStage( event.stageX, event.stageY );
			if( m_drawFilterFunction != null )
			{
				local = m_drawFilterFunction( local );
			}
			
	        switch( m_geometryType )
	        {
	           case FREEHAND_POLYLINE:
	                map.removeEventListener( MouseEvent.MOUSE_DOWN, map_mouseDownHandler );
	                map.addEventListener( MouseEvent.MOUSE_MOVE, map_mouseMoveHandler );
	                map.addEventListener( MouseEvent.MOUSE_UP, map_mouseUpHandler );
	
	                polyline = new Polyline();
	                polyline.paths = [];
	                polyline.paths.push( [local] );
	
	                graphic = new Graphic();
	                graphic.geometry = polyline;
	                graphic.symbol = m_lineSymbol;
	                addGraphic( graphic );
	                break;
	           case LINE:
	                map.removeEventListener( MouseEvent.MOUSE_DOWN, map_mouseDownHandler );
	                map.addEventListener( MouseEvent.MOUSE_MOVE, map_mouseMoveHandler );
	                map.addEventListener( MouseEvent.MOUSE_UP, map_mouseUpHandler );
	
	                polyline = new Polyline();
	                polyline.paths = [];
	                polyline.paths.push( [local,local] );
	
	                graphic = new Graphic();
	                graphic.geometry = polyline;
	                graphic.symbol = m_lineSymbol;
	                addGraphic( graphic );
	                break;
	            case FREEHAND_POLYGON:
	                map.removeEventListener( MouseEvent.MOUSE_DOWN, map_mouseDownHandler );
	                map.addEventListener( MouseEvent.MOUSE_MOVE, map_mouseMoveHandler );
	                map.addEventListener( MouseEvent.MOUSE_UP, map_mouseUpHandler );
	
	                polygon = new Polygon();
	                polygon.rings = [];
	                polygon.rings.push( [local] );
	
	                graphic = new Graphic();
	                graphic.geometry = polygon;
	                graphic.symbol = m_fillSymbol;
	                addGraphic( graphic );
	                break;
	            case EXTENT:
	                map.removeEventListener( MouseEvent.MOUSE_DOWN, map_mouseDownHandler );
	                map.addEventListener( MouseEvent.MOUSE_MOVE, map_mouseMoveHandler );
	                map.addEventListener( MouseEvent.MOUSE_UP, map_mouseUpHandler );
	
	                startPt = map.toMapFromStage(event.stageX,event.stageY);
					if( m_drawFilterFunction != null )
					{
						startPt = m_drawFilterFunction( startPt );
					}
	
	                extent = new Extent();
	                extent.xmin = startPt.x;
	                extent.ymin = startPt.y;
	                extent.xmax = extent.xmin;
	                extent.ymax = extent.ymin;
	
	                graphic = new Graphic();
	                graphic.geometry = extent;
	                graphic.symbol = m_fillSymbol;
	                addGraphic( graphic );
	                break;
	         }
	    }
	
		/** Handles the mouse move event. */
	    private function map_mouseMoveHandler( event :MouseEvent ) :void
	    {
	        var local :MapPoint = map.toMapFromStage( event.stageX, event.stageY );
			if( m_drawFilterFunction != null )
			{
				local = m_drawFilterFunction( local );
			}
			
	        switch( m_geometryType )
	        {
	           case POLYLINE:
	                polyline.paths[0][polyline.paths[0].length - 1] = local;
	                polyline.spatialReference = map.spatialReference;
	                graphic.refresh();
	                break;
	            case LINE:
	                polyline.paths[0][polyline.paths[0].length - 1] = local;
	                polyline.spatialReference = map.spatialReference;
	                graphic.refresh();
	                break;
	            case FREEHAND_POLYLINE:
	                polyline.paths[0].push( local );
	                polyline.spatialReference = map.spatialReference;
	                graphic.refresh();
	                break;
	             case POLYGON:
	                polygon.rings[0][polygon.rings[0].length - 1] = local;
	                polygon.spatialReference = map.spatialReference;
	                graphic.refresh();
	                break
	            case FREEHAND_POLYGON:
	                polygon.rings[0].push( local );
	                polygon.spatialReference = map.spatialReference;
	                graphic.refresh();
	                break;
	            case EXTENT:
	                extent.xmin = Math.min( startPt.x, local.x );
	                extent.ymin = Math.min( startPt.y, local.y );
	                extent.xmax = extent.xmin + Math.abs( startPt.x - local.x );
	                extent.ymax = extent.ymin + Math.abs( startPt.y - local.y );
	                extent.spatialReference = map.spatialReference;
	                graphic.refresh();
	                break;
	         }
	    }
	
		/** Handles the mouse up event. */
	    private function map_mouseUpHandler( event :MouseEvent ) :void
	    {
	        switch( m_geometryType )
	        {
	            case LINE:
	                map.removeEventListener( MouseEvent.MOUSE_MOVE, map_mouseMoveHandler );
	                map.removeEventListener( MouseEvent.MOUSE_UP, map_mouseUpHandler );
	
	                if( polyline.paths[0][0] != polyline.paths[0][1] )
	                {
	                    removeLayerEndDraw();
	                }
	                else
	                {
	                    removeGraphic( graphic );
	                }
	                map.addEventListener( MouseEvent.MOUSE_DOWN, map_mouseStartDownHandler );
	                map.addEventListener( MouseEvent.MOUSE_DOWN, map_mouseDownHandler );
	                break;
	            case FREEHAND_POLYLINE:
	                map.removeEventListener( MouseEvent.MOUSE_MOVE, map_mouseMoveHandler );
	                map.removeEventListener( MouseEvent.MOUSE_UP, map_mouseUpHandler );
	
	                if( polyline.paths[0].length >= 2 )
	                {
	                    removeLayerEndDraw();
	                }
	                else
	                {
	                    removeGraphic( graphic );
	                }
	                map.addEventListener( MouseEvent.MOUSE_DOWN, map_mouseStartDownHandler );
	                map.addEventListener( MouseEvent.MOUSE_DOWN, map_mouseDownHandler );
	                break;
	            case FREEHAND_POLYGON:
	                map.removeEventListener( MouseEvent.MOUSE_MOVE, map_mouseMoveHandler );
	                map.removeEventListener( MouseEvent.MOUSE_UP, map_mouseUpHandler );
	
	                polygon.rings[0].push( polygon.rings[0][0] );
	                if( !GeomUtils.isClockwise( polygon.rings[0] ) && !m_respectDrawingVertexOrder )
	                {
	                   ( polygon.rings[0] as Array ).reverse();
	                }
	                if( polygon.rings[0].length >= 3 )
	                {
	                    removeLayerEndDraw();
	                }
	                else if( polygon.rings[0].length <= 2 )
	                {
	                    removeGraphic( graphic );
	                }
	                map.addEventListener( MouseEvent.MOUSE_DOWN, map_mouseStartDownHandler );
	                map.addEventListener( MouseEvent.MOUSE_DOWN, map_mouseDownHandler );
	                break;
	            case EXTENT:
	                map.removeEventListener( MouseEvent.MOUSE_MOVE, map_mouseMoveHandler );
	                map.removeEventListener( MouseEvent.MOUSE_UP, map_mouseUpHandler );
	
	                removeLayerEndDraw();
	                
	                map.addEventListener( MouseEvent.MOUSE_DOWN, map_mouseStartDownHandler );
	                map.addEventListener( MouseEvent.MOUSE_DOWN, map_mouseDownHandler );
	                break;
	         }
	    }
	    
	    /** Adds the graphic to the GraphicsLayer. */
	    private function addGraphic( graphic :Graphic ) :void
	    {
	    	if( m_graphicsLayer )
            {
                graphicsLayer.add( graphic );
            }
            else
            {
                gLayer.clear();
                gLayer.add( graphic );
                map.addLayer( gLayer );
            }
	    }
	    
	    /** Removes the graphic from the GraphicsLayer. */
	    private function removeGraphic( graphic :Graphic ) :void
	    {
	    	if( m_graphicsLayer )
            {
                graphicsLayer.remove( graphic );
            }
            else
            {
                gLayer.remove( graphic );
            }
	    }
	    
	    /** Removes the built-in graphics layer from the map and dispatched the draw end event. */
	    private function removeLayerEndDraw() :void
	    {
	    	if( gLayer )
            {
                map.removeLayer( gLayer );
            }
            dispatchEvent( new DrawEvent( DrawEvent.DRAW_END, graphic ) );
	    }
	}
}