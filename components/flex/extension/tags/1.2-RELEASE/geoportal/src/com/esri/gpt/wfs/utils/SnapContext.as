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
	import com.esri.ags.Graphic;
	import com.esri.ags.Map;
	import com.esri.ags.geometry.MapPoint;
	
	import flash.display.Shape;
	
	import mx.collections.ArrayCollection;
	
	/**
	 * Contains the context used in snapping calculations.
	 */
	public class SnapContext
	{
		/** Constructor. */
		public function SnapContext()
		{
		}
		
		/** A reference to the map, which the snapShape will be drawn on. */
		public var map :Map = null;
		
		/** The graphicProvider reference from the WFSTransactionLayer. */
		public var graphicProvider :ArrayCollection = null;
		
		/** The currently selected feature in the WFSTransactionLayer. */
		public var selectedGraphic :Graphic = null;
		
		/** The point which the snap calculations are finding the closest match to. */
		public var origin :MapPoint = null;
		
		/** The shape defined by the origin and the snapTolerance which is used to find candidate features to snap to. */
		public var snapShape :Shape = null;
		
		/** The snapping mode. Defaults to "none".*/
		public var snapMode :String = "none";
		
		/** The snapping tolerance, in pixels. Defaults to 20 pixels. */
		public var snapTolerance :int = 20;
		
		/** The snap shape color. Defaults to 0x333333. */
		public var snapColor :Number = 0x333333;
		
		/** The snap shape alpha. Defaults to 0.5. */
		public var snapAlpha :Number = 0.5;
	}
}