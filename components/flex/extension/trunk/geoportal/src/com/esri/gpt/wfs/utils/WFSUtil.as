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
	import com.esri.ags.layers.Layer;
//	import com.esri.gpt.wfs.layers.WFSTLayer;
	// import com.esri.gpt.wfs.layers.WFSTLazyLockLayer;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.http.mxml.HTTPService;
	
	public class WFSUtil
	{
		/** Constructor. */
		public function WFSUtil()
		{
		}
		
		/**
		 * Determines if the ArrayCollection contains a String object matching the search String.
		 * @param searchString The String to match against.
		 * @param collection The ArrayCollection to check.
		 * @return True is the string is found, false otherwise.
		 */
		public static function containsString( searchString :String, collection :ArrayCollection ) :Boolean
		{
			for each( var string :String in collection )
			{
				if( searchString == string )
				{
					return true;
				}
			}
			return false;
		}
		
		/**
		 * Retrieves the index of the search string if it is contained in the collection.
		 * @param searchString The String to match against.
		 * @param collection The ArrayCollection to check.
		 * @return If no match is found then -1 is returned, otherwise the index of the search match is returned.
		 */
		public static function getStringIndex( searchString :String, collection :ArrayCollection ) :int
		{
			for each( var string :String in collection )
			{
				if( searchString == string )
				{
					return collection.getItemIndex( string );
				}
			}
			return -1;
		}
		
		/**
		 * Retrieves the label for a layer. For WFSLayer or WFSTransactionalLayers it looks for the label property,
		 * and if it is set returns it instead of the name attribute.
		 * @param The layer to get the label for.
		 */
		public static function getLayerLabel( layer :Layer ) :String
		{
			/*if( layer is WFSTLazyLockLayer )
			{
				var transLayer :WFSTLazyLockLayer = layer as WFSTLazyLockLayer;
				var label :String = transLayer.label;
				if( label == null || label == "" )
				{
					label = transLayer.name;
				}
				return label;
			}
			else if( layer is WFSTLayer )
			{
				var stransLayer :WFSTLayer = layer as WFSTLayer;
				label = stransLayer.label;
				if( label == null || label == "" )
				{
					label = stransLayer.name;
				}
				return label;
			}*/
			return layer.name;
		}
		
		/**
		 * Create a new HTTPService.
		 * @param url The URL to open the HTTP Service connection on.
		 * @return The HTTPService object.
		 */
		public static function createService( url :String ) :HTTPService
		{
			var service :HTTPService = new HTTPService();
			service.url = url;
			service.method = "POST";
			service.contentType = "application/xml";
    		service.resultFormat = "e4x";
    		service.requestTimeout = 60;
			service.useProxy = false;
			return service;
		}
	}
}