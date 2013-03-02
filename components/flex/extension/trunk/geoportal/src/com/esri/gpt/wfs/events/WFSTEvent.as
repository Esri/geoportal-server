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
	 * Represents transaction events.
	 * @see WFSTransactionalLayer
	 */
	public class WFSTEvent extends Event
	{
		/** [static] Defines the value of the type property of the event object for an featureInserted event. */
		public static const FEATURE_INSERTED :String = "featureInserted";
		
		/** [static] Defines the value of the type property of the event object for an featureUpdated event. */
		public static const FEATURE_UPDATED :String = "featureUpdated";
		
		/** [static] Defines the value of the type property of the event object for an featureDeleted event. */
		public static const FEATURE_DELETED :String = "featureDeleted";
		
		/** [static] Defines the value of the type property of the event object for an featureSelected event. */
		public static const FEATURE_SELECTED :String = "featureSelected";
		
		/** [static] Defines the value of the type property of the event object for an featureDeselected event. */
		public static const FEATURE_DESELECTED :String = "featureDeselected";
		
		/** [static] Defines the value of the type property of the event object for an editCanceled event. */
		public static const EDIT_CANCELED :String = "editCanceled";
		
		/** [static] Defines the value of the type property of the event object for an editBegun event. */
		public static const EDIT_BEGUN :String = "editBegun";
		
		/** [static] Defines the value of the type property of the event object for an editSaved event. */
		public static const EDIT_SAVED :String = "editSaved";
		
		/** [static] Defines the value of the type property of the event object for an lockRequested event. */
		public static const LOCK_REQUESTED :String = "lockRequested";
		
		/** [static] Defines the value of the type property of the event object for an lockAcquired event. */
		public static const LOCK_ACQUIRED :String = "lockAcquired";
		
		/** [static] Defines the value of the type property of the event object for an lockFailed event. */
		public static const LOCK_FAILED :String = "lockFailed";
		
		/** [static] Defines the value of the type property of the event object for an transactionStarted event. */
		public static const TRANSACTION_STARTED :String = "transactionStarted";
		
		/** [static] Defines the value of the type property of the event object for an transactionCommitted event. */
		public static const TRANSACTION_COMMITTED :String = "transactionCommitted";
		
		/** [static] Defines the value of the type property of the event object for an transactionCanceled event. */
		public static const TRANSACTION_CANCELED :String = "transactionCanceled";
		
		/** [static] Defines the value of the type property of the event object for an transactionFailed event. */
		public static const TRANSACTION_FAILED :String = "transactionFailed";
		
		/**
		 * Creates a new WFSTEvent.
		 * @param type The WFSTransactionEvent type string.
		 */
		public function WFSTEvent( type:String )
		{
			super( type );
		}
	}
}