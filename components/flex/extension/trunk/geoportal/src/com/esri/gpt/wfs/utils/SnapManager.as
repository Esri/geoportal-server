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
package com.esri.gpt.wfs.utils
{
	import com.esri.ags.geometry.MapPoint;
	import com.esri.ags.layers.GraphicsLayer;
	
	import flash.display.Shape;
	
	import mx.collections.ArrayCollection;
	
	/**
	 * This component provides snapping support over any GraphicsLayer.
	 * @see WFSTransactionalLayer
	 */
	public class SnapManager
	{
		private var m_snappingEnabled :Boolean = false;
		
		[Bindable]
		public var layerProvider :ArrayCollection;
		
		[Bindable]
		public var defaultSnapTolerance :int = 20;
		
		/** Constructor. */
		public function SnapManager()
		{
		}
				
		/**
		 * Determines if the layer with the specified id has snapping enabled.
		 * @param id The layer id.
		 * @return True if snapping is enabled, false otherwise.
		 */
		public function snappingEnabled() :Boolean
		{
			return m_snappingEnabled;
		}
		
		/** Recalculates whether snapping is enabled for any of the layers being managed. */
		public function resetSnapping() :void
		{
			m_snappingEnabled = false;
			for each( var layerContext :LayerSnapContext in layerProvider )
			{
				m_snappingEnabled = m_snappingEnabled || layerContext.vertex || layerContext.edge || layerContext.end;
			}
		}
		
//		/** The filter function to be hooked into the SnapDraw layer. */
//		public function drawFilter( point :MapPoint ) :MapPoint
//		{
//			if( snappingEnabled() )
//			{
//				var snapContext :SnapContext = new SnapContext();
//				snapContext.origin = point;
//				snapContext.selectedGraphic = null;
//				return findSnapPoint( snapContext );
//			}
//			return point;
//		}
//		
//		/**
//		 * @private
//		 * Sets the defaults of the snap context if they are not already provided.
//		 */
//		private function setSnapContextDefaults( snapContext :SnapContext, layerContext :LayerSnapContext ) :void
//		{
//			if( !snapContext.map )
//			{
//				snapContext.map = GraphicsLayer( layerContext.layer ).map;
//			}
//			if( !snapContext.snapTolerance )
//			{
//				snapContext.snapTolerance = defaultSnapTolerance;
//			}
//			if( !snapContext.snapAlpha )
//			{
//				snapContext.snapAlpha = 0.5
//			}
//			if( !snapContext.snapColor )
//			{
//				snapContext.snapColor = 0x333333;
//			}
//			if( !snapContext.snapShape )
//			{
//				var snapToShape :Shape = new Shape();
//				snapToShape.name = "defaultSnapShape";
//				snapToShape.x = snapContext.origin.x;
//				snapToShape.y = snapContext.origin.y;
//				snapToShape.graphics.clear();
//				snapToShape.graphics.beginFill( snapContext.snapColor, snapContext.snapAlpha );
//				snapToShape.graphics.drawCircle( 0, 0, snapContext.snapTolerance );
//				snapToShape.graphics.endFill();
//				snapContext.snapShape = snapToShape;
//			}
//		} 

		/**
		 * Determines if there is a snap point in any layer meeting the constraints of the overall snapping contraints.
		 * This method relies on each element of the layer provider to be an Object which contains the following fields:
		 * vertex: true or false values only.
		 * edge: true or false values only.
		 * end: true or false values only.
		 * layer: instance of a GraphicsLayer.
		 * @param context The snapping context from the layer which is asking for the snap point.
		 * @return If a snap point is found then it is returned, otherwise returns the input point.
		 * @see GraphicsLayer
		 */
		public function findSnapPoint( context :SnapContext ) :MapPoint
		{
			var closestPoints :ArrayCollection = new ArrayCollection();
			var closePoint :MapPoint;
			var snapMode :String;
			for each( var layerContext :LayerSnapContext in layerProvider )
			{
				snapMode = determineSnapMode( layerContext );
				if( SnapUtil.snappingEnabled( snapMode ) )
				{
//					setSnapContextDefaults( context, layerContext );
					context.snapMode = snapMode;
					context.graphicProvider = GraphicsLayer( layerContext.layer ).graphicProvider as ArrayCollection;
					closePoint = SnapUtil.findSnapPoint( context );
					if( closePoint != null )
					{
						closestPoints.addItem( closePoint );
					}
				}
			}
			
			if( closestPoints.length > 0 )
			{
				closePoint = null;
				for each( var point :MapPoint in closestPoints )
				{
					if( !( point === context.origin ) )
					{
						if( closePoint == null )
						{
							closePoint = point;
						}
						else
						{
							closePoint = SnapUtil.findCloserPoint( context.origin, closePoint, point );
						}
					}
				}
				if( closePoint == null )
				{
					return context.origin;
				}
				return closePoint;
			}
			return context.origin;
		}
		
		/**
		 * @private
		 * Determine the snap mode to use based on the context. The snapping modes are heirarchical,
		 * so we always choose the outermost mode, even if multiple modes are selected. The heirarchy follows:
		 * ---------------
		 * |        Edge |
		 * | ----------- |
		 * | |  Vertex | |
		 * | | ------- | |
		 * | | | End | | |
		 * | | ------- | |
		 * | ----------- |
		 * ---------------
		 * Thus, edge mode always takes precedence if it is selected, then vertex, then end. If none
		 * are selected then the chosen mode is none.
		 * @param context The layer context which contains the mode selections.
		 * @return The snap mode.
		 */
		private function determineSnapMode( context :LayerSnapContext ) :String
		{
			var mode :String = SnapMode.NONE;
			if( context.end )
			{
				mode = SnapMode.END;
			}
			if( context.vertex )
			{
				mode = SnapMode.VERTEX;
			}
			if( context.edge )
			{
				mode = SnapMode.EDGE;
			}
			return mode;
		}
	}
}