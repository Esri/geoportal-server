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
	/** Defines the snap modes for snapping enabled layers. */
	public class SnapMode
	{		
		/** [static] Defines the value of the no snapping mode. */
		public static const NONE :String = "none";
		
		/** [static] Defines the value of the vertex snapping mode. */
		public static const VERTEX :String = "vertex";
		
		/** [static] Defines the value of the edge snapping mode. */
		public static const EDGE :String = "edge";
		
		/** [static] Defines the value of the end snapping mode. */
		public static const END :String = "end";
		
		/** Constructor. Disable instantiation. */
		public function SnapMode( enforcer :Enforcer )
		{
		}
	}
}

class Enforcer
{
}