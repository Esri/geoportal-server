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
	
	
	import com.esri.ags.FeatureSet;
	
	import flash.events.Event;

	/**
	 * Represents query events.
	 * @see WFSQueryTask
	 */
	public class WFSQueryEvent extends Event
	{
		/** [static] Defines the value of the type property of the event object for an executeComplete event. */
		public static const EXECUTE_COMPLETE :String = "executeComplete";
		
		/** The feature set returned by the WFS query task. */
		public var featureSet :FeatureSet = null;		
		
		/**
		 * Creates a new WFSQueryEvent.
		 * @param type The WFSQueryEvent type string.
		 * @param featureSet The query results.
		 */
		public function WFSQueryEvent( type :String, featureSet :FeatureSet = null )
		{
			super( type );
			this.featureSet = featureSet;
		}		
	}
}