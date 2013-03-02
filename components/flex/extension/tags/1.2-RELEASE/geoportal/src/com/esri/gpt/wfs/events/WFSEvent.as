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
package com.esri.gpt.wfs.events
{
	import flash.events.Event;

	/**
	 * Represents WFS events.
	 * @see WFSLayer
	 * @see WFSTransactionalLayer
	 */
	public class WFSEvent extends Event
	{
		/** [static] Defines the value of the type property of the event object for an loadingStarted event. */
		public static const LOADING_STARTED :String = "loadingStarted";
		
		/** [static] Defines the value of the type property of the event object for an loadingComplete event. */
		public static const LOADING_COMPLETE :String = "loadingComplete";
		
		/** [static] Defines the value of the type property of the event object for an loadingFailed event. */
		public static const LOADING_FAILED :String = "loadingFailed";
		
		/**
		 * Creates a new WFSEvent.
		 * @param type The WFSEvent type string.
		 */
		public function WFSEvent( type :String )
		{
			super( type );
		}
		
	}
}