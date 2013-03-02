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
package com.esri.gpt.wfs.tasks
{
	import com.esri.ags.FeatureSet;
	import com.esri.ags.Graphic;
	import com.esri.ags.SpatialReference;
	import com.esri.ags.geometry.MapPoint;
	import com.esri.ags.geometry.Polygon;
	import com.esri.ags.geometry.Polyline;
	import com.esri.gpt.wfs.events.WFSQueryEvent;
	
	import flash.events.EventDispatcher;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.AsyncResponder;
	import mx.rpc.AsyncToken;
	import mx.rpc.Fault;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.mxml.HTTPService;

	/**
	 * Dispatched when a task call fails.
	 * @eventType mx.rpc.events.FaultEvent.FAULT
	 */
	[Event(name="fault", type="mx.rpc.events.FaultEvent")]
	
	/**
	 * Dispatched when a query task successfully completes.
	 * @eventType com.esri.wfs.events.WFSQueryEvent.EXECUTE_COMPLETE
	 */
	[Event(name="executeComplete", type="com.esri.gpt.wfs.events.WFSQueryEvent")]
	
	[Bindable]
	
	/**
	 * Executes a query operation on a WFS endpoint exposed by the ArcGIS Server.
	 * @see com.esri.ags.tasks.FeatureSet
	 * @see com.esri.wfs.events.WFSQueryEvent
	 * @see com.esri.wfs.tasks.WFSQuery
	 */
	public class WFSQueryTask extends EventDispatcher
	{
		private static const SERVICE :String = "WFS";
		private static const VERSION :String = "1.1.0";
		private static const OUTPUT_FORMAT :String = "text/xml; subType=gml/3.1.1";
		private static const WFS :Namespace = new Namespace("http://www.opengis.net/wfs");
		private static const GML :Namespace = new Namespace("http://www.opengis.net/gml");
		private static const OGC :Namespace = new Namespace("http://www.opengis.net/ogc");
		private static const OWS :Namespace = new Namespace("http://www.opengis.net/ows");
		private static const XLINK :Namespace = new Namespace("http://www.w3.org/1999/xlink");
		
		private var m_url :String;
		private var m_proxyURL :String;
		
		protected var id :String = "";
		
		/** The last result of the execute function. */
		public var executeLastResult :FeatureSet;
		
		/** The request timeout in seconds. A value less than or equal to zero prevents request timeout. */
		public var requestTimeout :Number = -1;
		
		/** Shows the busy cursor until the task has completed. */
    	public var showBusyCursor : Boolean = false;

		/**
	     * Creates a new WFSQueryTask object used to execute a query on the WFS endpoint identified by the URL.
	     * @param url [optional] URL to the ArcGIS Server WFS endpoint.
	     */
		public function WFSQueryTask( url :String = null )
		{
			super();
        	m_url = url;
		}
		
		/**
	     * @private
	     * Called after the implementing object has been created and all component properties specified on the MXML tag have been initialized.
	     * @param document The MXML document that created this object.
	     * @param id The identifier used by document to refer to this object.
	     */
		public function initialized( document :Object, id :String ) :void
		{
			this.id = id;
		}
		
		/**
	     * URL to the ArcGIS Server WFS endpoint. For more information on constructing a URL, see Using ArcGIS Services Directory.
	     */
		public function get url() :String
		{
		    return m_url;
		}

		/** @private */
		public function set url( value :String ) :void
		{
			if( m_url != value )
			{
		    	m_url = value;
		 	}
		}
		
		/** The URL to proxy the request through. */
		public function get proxyURL() :String
		{
			return m_proxyURL;
		}
		
		/** @private */
		public function set proxyURL( value :String ) :void
		{
			if( m_proxyURL != value )
			{
				m_proxyURL = value;
			}
		}
		
		/**
		 * Executes the WFS query. The final results or fault will be directed to the given responder, if one is set.
		 * @param query The WFSQuery object which controls aspects of the response and parsing of the response.
		 * @param responder The IResponder which will handle the results, or fault, if one is set.
		 */
		public function execute( query :WFSQuery = null, responder :IResponder = null ) :void
		{
			if( query == null )
			{
				return;
			}
			getFeatures( query, responder );
		}
		
		/**
		 * @private
		 * Executes a WFS GetFeature request. The final results or fault will be directed to the given responder, if one is set.
		 * @param query the WFSQuery object which controls aspects of the GetFeature request and parsing of the response.
		 * @param responder the IResponder which will handle the results, or fault, if one is set.
		 */ 
		private function getFeatures( query :WFSQuery, responder :IResponder ) :void
		{
			var getFeatureXML :XML = createGetFeatureRequest( query );
			var service :HTTPService = createService();
			var token :AsyncToken = service.send( getFeatureXML );
			token.addResponder( new AsyncResponder(
				function onSuccess( result :ResultEvent, token :Object = null ) :void
				{
					handleResult( query, result, responder, handleFeatures );
				},
				function onFailure( error :FaultEvent, token :Object = null ) :void
				{
					handleFault( error, responder );
				},
				this
			) );
		}
		
		/**
		 * @private
		 * Creates and returns the FeatureSet based on the given XML.
		 * @param query the WFSQuery object which controls aspects of the parsing of the FeatureSet.
		 * @param xml the XML from which the FeatureSet will be created.
		 * @return the new FeatureSet object.
		 */
		private function toFeatures( query :WFSQuery, xml :XML ) :FeatureSet
		{
			var swapCoordinates :Boolean = query.swapCoordinates;
			var featureSet :FeatureSet = new FeatureSet();
			
			// retrieve the well known id from the srsName ex: urn:x-ogc:def:crs:EPSG:6.9:4326
			var wkids :Array = xml..GML::Envelope.@srsName.split( ":" );
			var wkid :Number = new Number( wkids[wkids.length - 1] );
			featureSet.spatialReference = new SpatialReference( wkid );
			
			// retrieve all features
			var features :ArrayCollection = new ArrayCollection();
			var attributes :ArrayCollection = new ArrayCollection();
			var aliases :Object = new Object();
			for each( var featureMember :XML in xml..GML::featureMember )
			{
				var children :XMLList = featureMember.children();
				for each( var child :XML in children )
				{
					var graphic :Graphic = createGraphic( attributes, aliases, child, query, featureSet.spatialReference );
					features.addItem( graphic );
				}
			}
			
			featureSet.features = features.source;
			//featureSet.attributes = attributes.source;
			featureSet.fieldAliases = aliases;
			return featureSet;
		}
		
		/**
		 * @private
		 * Creates and returns a new Graphic based on the XML feature.
		 * @param attributes an ArrayCollection containing a list of all attributes that will be updated if new attributes are found.
		 * @param aliases an Object that maps all attribute names with their aliases. At this time all aliases are equal to the attribute name.
		 * @param feature the XML feature definition.
		 * @param query the WFSQuery object that controls aspects of the feature parsing.
		 * @param spatialReference the SpatialReference object that will be applied to the geometry, if query.returnGeometry is true.
		 * @return the new Graphic object.
		 */
		private function createGraphic(
			attributes :ArrayCollection,
			aliases :Object,
			feature :XML,
			query :WFSQuery,
			spatialReference :SpatialReference ) :Graphic
		{
			var graphic :Graphic = new Graphic;
			graphic.attributes = new Object();
			
			// save the GML feature id as GML_FEATURE_ID
			graphic.attributes.GML_FEATURE_ID = feature.@GML::id;
			
			// retrieve the feature attributes
			for each( var attribute :XML in feature.children() )
			{
				var attributeName :String = attribute.name().toString();
				var nameElements :Array = attributeName.split( "::" );
				var name :String = nameElements[nameElements.length - 1];
				// create the feature geometry
				if( name.toLowerCase() == query.shapeField.toLowerCase() && query.returnGeometry )
				{
					createGeometry( graphic, attribute.children(), query, spatialReference );
				}
				// add the attribute if appropriate
				else
				{
					if( ( query.excludeFields && !query.excludeFields.contains( name.toLowerCase() ) ) || query.excludeFields == null )
					{
						if( !attributes.contains( name ) )
						{
							attributes.addItem( name );
							aliases[name] = name;
						}
						graphic.attributes[name] = attribute;
					}
				}
			}
			return graphic;
		}

		/**
		 * @private
		 * Creates and sets the Graphic.geometry based on the GML shape.
		 * @param graphic the Graphic which the geometry will be applied to.
		 * @param geometry the XMLList of GML shapes.
		 * @param query the WFSQuery object which controls aspects of the geometry parsing.
		 * @param spatialReference the SpatialReference object to apply to the geometry.
		 */
		private function createGeometry(
			graphic :Graphic,
			geometry :XMLList,
			query :WFSQuery,
			spatialReference :SpatialReference ) :void
		{
			for each( var shape :XML in geometry )
			{
				var shapeType :String = shape.name();
				var typeElements :Array = shapeType.split( "::" ); 
				var type :String = typeElements[typeElements.length - 1];
				if( type == "Point" )
				{
					graphic.geometry = createPoint( shape, query.swapCoordinates, spatialReference );
				}
				else if( type == "Polygon" || type == "Surface" )
				{
					graphic.geometry = createPolygon( shape, query.swapCoordinates, spatialReference );
				}
				else if( type == "LineString" || type == "Curve" )
				{
					graphic.geometry = createLine( shape, query.swapCoordinates, spatialReference );
				}
				else if( type == "MultiSurface" )
				{
					graphic.geometry = createMultiPolygon( shape, query.swapCoordinates, spatialReference );
				}
				else if( type == "MultiCurve" )
				{
					graphic.geometry = createMultiLine( shape, query.swapCoordinates, spatialReference ); 
				}
			}
		}
		
		/**
		 * @private
		 * Creates and returns a Polyline object based on the GML "MultiCurve" shape.
		 * @param shape the XML object which contains the GML shape.
		 * @param swapCoordinates if true, use the "y x" format to define points, otherwise use the "x y" format.
		 * @param spatialReference the SpatialReference object to apply to the Polyline.
		 * @return the Polyline object.
		 */
		private function createMultiLine( shape :XML, swapCoordinates :Boolean, spatialReference :SpatialReference ) :Polyline
		{
			var line :Polyline = new Polyline();
			line.spatialReference = spatialReference;
			for each( var path :String in shape..GML::LineString..GML::posList )
			{
				line.addPath( createPointArray( path, swapCoordinates, spatialReference ) );
			}
			for each( path in shape..GML::Curve..GML::posList )
			{
				line.addPath( createPointArray( path, swapCoordinates, spatialReference ) );
			}
			return line;
		}
		
		/**
		 * @private
		 * Creates and returns a Polyline object based on the GML "Curve" or "LineString" shape.
		 * @param shape the XML object which contains the GML shape.
		 * @param swapCoordinates if true, use the "y x" format to define points, otherwise use the "x y" format.
		 * @param spatialReference the SpatialReference object to apply to the Polyline.
		 * @return the Polyline object.
		 */
		private function createLine( shape :XML, swapCoordinates :Boolean, spatialReference :SpatialReference ) :Polyline
		{
			var line :Polyline = null;
			var lines :XML = shape..GML::posList;
			if( lines )
			{
				line = new Polyline();
				for each( var coordinates :String in lines )
				{
					line.addPath( createPointArray( coordinates, swapCoordinates, spatialReference ) );
				}
			}
			return line;
		}
		
		/**
		 * @private
		 * Creates and returns a Polygon object based on the GML "MutliSurface" shape.
		 * @param shape the XML object which contains the GML shape.
		 * @param swapCoordinates if true, use the "y x" format to define points, otherwise use the "x y" format.
		 * @param spatialReference the SpatialReference object to apply to the Polygon.
		 * @return the Polygon object.
		 */
		private function createMultiPolygon( shape :XML, swapCoordinates :Boolean, spatialReference :SpatialReference ) :Polygon
		{
			// grab both the gml:exterior and gml:interior gml:posList elements and let Flash decide what is a hole using the even/odd algorithm
			// if we have to deal with winding direction in the future, simply grab the gml:exterior and gml:interior gml:posList elements separately,
			// and then make sure to reverse the array returned by createRing() for the gml:interior gml:posList
			var polygon :Polygon = new Polygon();
			polygon.spatialReference = spatialReference;
			for each( var ring :String in shape..GML::Polygon..GML::posList )
			{
				polygon.addRing( createPointArray( ring, swapCoordinates, spatialReference ) );
			}
			for each( ring in shape..GML::Surface..GML::posList )
			{
				polygon.addRing( createPointArray( ring, swapCoordinates, spatialReference ) );
			}
			polygon.extent.spatialReference = spatialReference;
			polygon.extent.center.spatialReference = spatialReference;
			return polygon;
		}
		
		/**
		 * @private
		 * Creates and returns a Polygon object based on the GML "Surface" or "Polygon" shape.
		 * @param shape the XML object which contains the GML shape.
		 * @param swapCoordinates if true, use the "y x" format to define points, otherwise use the "x y" format.
		 * @param spatialReference the SpatialReference object to apply to the Polygon.
		 * @return the Polygon object.
		 */ 
		private function createPolygon( shape :XML, swapCoordinates :Boolean, spatialReference :SpatialReference ) :Polygon
		{
			// grab both the gml:exterior and gml:interior gml:posList elements and let Flash decide what is a hole using the even/odd algorithm
			// if we have to deal with winding direction in the future, simply grab the gml:exterior and gml:interior gml:posList elements separately,
			// and then make sure to reverse the array returned by createRing() for the gml:interior gml:posList
			var polygon :Polygon = null;
			var rings :XML = shape..GML::posList; 
			if( rings )
			{
				polygon = new Polygon();
				for each( var ring :String in rings )
				{
					polygon.addRing( createPointArray( ring, swapCoordinates, spatialReference ) );
				}
			}
			return polygon;
		}
		
		/**
		 * @private
		 * Creates and returns an array of MapPoint objects based on the string of coordinates.
		 * @param coordinates a space-delimited set of point coordinates.
		 * @param swapCoordinates if true, use the "y x" format to define points, otherwise use the "x y" format.
		 * @param spatialReference the SpatialReference object to apply to the MapPoint objects within the Array.
		 * @return the Array of MapPoint objects.
		 */ 
		private function createPointArray( coordinates :String, swapCoordinates :Boolean, spatialReference :SpatialReference ) :Array
		{
			var points :Array = new Array();
			var coords :Array = coordinates.split( " " );
			for( var i :int = 0; i < coords.length; i = i + 2 )
			{
				if( swapCoordinates )
				{
					points.push( new MapPoint( coords[i + 1], coords[i], spatialReference ) );
				}
				else
				{
					points.push( new MapPoint( coords[i], coords[i + 1], spatialReference ) );
				}
			}
			return points;
		} 
		
		/**
		 * @private
		 * Creates and retuns a MapPoint based on the GML "Point" shape.
		 * @param shape the XML object which contains the GML shape.
		 * @param swapCoordinates if true, use the "y x" format to define points, otherwise use the "x y" format.
		 * @param spatialReference the SpatialReference object to apply to the MapPoint.
		 * @return the MapPoint object.
		 */ 
		private function createPoint( shape :XML, swapCoordinates :Boolean, spatialReference :SpatialReference ) :MapPoint
		{
			var point :MapPoint = null;
			var coords :Array = shape.GML::pos.split( " " );
			if( coords != null && coords.length > 1 )
			{
				if( swapCoordinates )
				{
					point = new MapPoint( coords[1], coords[0], spatialReference);
				}
				else
				{
					point = new MapPoint( coords[0], coords[1], spatialReference );
				}
			}
			return point;
		}
		
		/**
		 * @private
		 * Handle a successful WFS FeatureCollection response.
		 * @param query the WFSQuery object which controls aspects of a successful FeatureCollection response.
		 * @param xml the FeatureCollection XML object.
		 * @param responder the IResponder that will handle the result, if one is given.
		 */
		private function handleFeatures( query :WFSQuery, xml :XML, responder :IResponder ) :void
		{
			executeLastResult = toFeatures( query, xml );
			if( responder )
			{
				responder.result( executeLastResult );
			}
			dispatchEvent( new WFSQueryEvent( WFSQueryEvent.EXECUTE_COMPLETE, executeLastResult ) );
		}
		
		/**
		 * @private
		 * Handles the result of a given WFS request.
		 * @param query the WFSQuery object which controls aspects of a successful response.
		 * @param result the ResultEvent.
		 * @param responder the IResponder that will handle the final successful result, if one is given.
		 * @param operation the operation that will handle a successful result response.'
		 */
		private function handleResult( query :WFSQuery, result :ResultEvent, responder :IResponder, operation :Function ) :void
		{
			if( result.result == "" )
			{
				handleStrErr( "Empty Result. Check Input Parameters.", responder );
			}
			else
			{
				try
	            {
	                var xml :XML = result.result as XML;
	                if( xml.name() == OWS + "::ExceptionReport" )
	                {
	                    this.handleErr( xml, responder );
	                }
	                else
	                {
	                    operation.call( this, query, xml, responder );
	                }
	            }
	            catch( resultError :Error )
	            {   
	                var resultFault : Fault = new Fault( null, resultError.message );
	                if( responder )
	                {
	                    responder.fault( new FaultEvent( FaultEvent.FAULT, false, true, resultFault ) );
	                }
	            }
			}
		}
		
		/**
		 * @private
		 * Handles a simple error.
		 * @param error the error message as a String.
		 * @param responder the IResponder that will handle the generated Fault.
		 */
		private function handleStrErr( error :String, responder :IResponder ) :void
		{
			var stringFault :Fault = new Fault( null, error );
	        if( responder )
	        {
	            responder.fault( stringFault );
	        }
	        dispatchEvent( new FaultEvent( FaultEvent.FAULT, false, true, stringFault ) );
		}
		
		/**
		 * @private
		 * Handles a WFS ExceptionReport. Also creates and passes a Fault to the provided responder, if one is given.
		 * @param xml the ExceptionReport XML object.
		 * @param responder the IResponder that will handle the generated Fault.
		 */
		private function handleErr( xml :XML, responder :IResponder ) :void
		{
			var faultCode :String = xml.OWS::Exception.@exceptionCode;
			var faultString :String = xml..OWS::ExceptionText;
			var fault : Fault = new Fault( faultCode, faultString );
			fault.rootCause = xml;
			if( responder )
			{
			    responder.fault( fault );
			}
			dispatchEvent( new FaultEvent( FaultEvent.FAULT, false, true, fault ));
		}

		/**
		 * @private
		 * Handles a fault event. Also passes the Fault to the responder, if one is given.
		 * @param event the FaultEvent.
		 * @param responder the IResponder that will handle the generated Fault.
		 */
		private function handleFault( event :Object, responder :IResponder ) :void
		{
			if( responder )
			{
			    responder.fault( event );
			}
			
			if( event is Fault )
			{
				event = new FaultEvent( FaultEvent.FAULT, false, true, event as Fault );
			}
			if( event is Object )
			{
				event = new FaultEvent( FaultEvent.FAULT, false, true, new Fault( "", event.toString() ) );
			}
			dispatchEvent( event as FaultEvent );
		}
		
		/**
		 * @private
		 * Creates and returns an HTTPService object.
		 * @return the new HTTPService object for sending a WFS request.
		 */
		private function createService() :HTTPService
		{
			var service :HTTPService = new HTTPService();
			if( proxyURL )
			{
				service.url = proxyURL + "?" + url;
			}
			else
			{
				service.url = url;
			}
			service.method = "POST";
			service.contentType = "application/xml";
    		service.resultFormat = "e4x";
    		service.requestTimeout = requestTimeout;
    		service.showBusyCursor = showBusyCursor;
			service.useProxy = false;
			return service;
		}
		
		/**
		 * @private
		 * Creates and returns the XML for a WFS GetFeature request. The method uses the WFSQuery input to control variable aspects of the GetFeature request.
		 * @param query the WFSQuery object that provides input for the variable aspects of the request.
		 * @return the GetFeature request as an XML object.
		 */ 
		private function createGetFeatureRequest( query :WFSQuery ) :XML
		{
			var xml :XML =
				<wfs:GetFeature xmlns:wfs={WFS} xmlns:gml={GML} xmlns:ogc={OGC} xmlns:ows={OWS} xmlns:xlink={XLINK}
					xmlns:ns={query.featureNamespace} version={VERSION} service={SERVICE} outputFormat={OUTPUT_FORMAT}>
					<wfs:Query typeName={"ns:" + query.featureName} srsName={query.srsName}> 
					</wfs:Query>
				</wfs:GetFeature>;
				
			// limit the number of features in the response 
			if( query.maxFeatures > 0 )
			{
				xml.@maxFeatures = query.maxFeatures;
			}
			
			// limit the response to include specific feature properties
			// this is according to the WFS-T 1.1.0 spec, but doesn't alter the ArcGIS output
			if( query.outFields != null )
			{
				for each( var field :String in query.outFields )
				{
					var outField :XML = <ogc:PropertyName xmlns:ogc={OGC} xmlns:ns={query.featureNamespace}>{"ns:" + field}</ogc:PropertyName>;
					xml.WFS::Query.appendChild( outField );
				}
			}
			
			// limit the response to include features within the given extent
			if( query.extent != null )
			{
				var filter :XML =
					<ogc:Filter xmlns:ogc={OGC} xmlns:gml={GML}>
						<ogc:BBOX>
							<ogc:PropertyName>{query.shapeField}</ogc:PropertyName>
							<gml:Envelope xmlns:gml={GML} srsName={query.srsName}>
								<gml:lowerCorner>{getCoordinates( query.extent.xmin, query.extent.ymin, query.swapCoordinates )}</gml:lowerCorner>
								<gml:upperCorner>{getCoordinates( query.extent.xmax, query.extent.ymax, query.swapCoordinates )}</gml:upperCorner>
							</gml:Envelope>
						</ogc:BBOX>
					</ogc:Filter>;
				xml..WFS::Query.appendChild( filter );
			}
			return xml;
		}
		
		/**
		 * @private
		 * Return the coordinates of a point in the format "x y". If swapCoordinates is true, returns the coordinates in "y x" format.
		 * @param x the x coordinate.
		 * @param y the y coordinate.
		 * @return the coordinate string.
		 */
		private function getCoordinates( x :Number, y :Number, swapCoordinates :Boolean ) :String
		{
			return ( swapCoordinates ? y + " " + x : x + " " + y );
		}
	}
}