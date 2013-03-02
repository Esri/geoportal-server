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
package com.esri.gpt.wfs.layers
{
	import com.esri.ags.FeatureSet;
	import com.esri.ags.Graphic;
	import com.esri.ags.events.ExtentEvent;
	import com.esri.ags.events.LayerEvent;
	import com.esri.ags.geometry.MapPoint;
	import com.esri.ags.geometry.Polygon;
	import com.esri.ags.geometry.Polyline;
	import com.esri.ags.layers.GraphicsLayer;
	import com.esri.ags.layers.Layer;
	import com.esri.gpt.utils.UrlUtils;
	import com.esri.gpt.wfs.WFSCapabilities;
	import com.esri.gpt.wfs.events.WFSEvent;
	import com.esri.gpt.wfs.tasks.WFSQuery;
	import com.esri.gpt.wfs.tasks.WFSQueryTask;
	import com.esri.gpt.wfs.utils.WFSUtil;
	
	import mx.collections.ArrayCollection;
	import mx.logging.ILogger;
	import mx.logging.Log;
	import mx.rpc.AsyncResponder;
	import mx.rpc.AsyncToken;
	import mx.rpc.Fault;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.mxml.HTTPService;
	import mx.utils.ObjectUtil;

	/**
	 * Dispatched when a query task successfully completes.
	 * @eventType com.esri.wfs.events.WFSQueryEvent.EXECUTE_COMPLETE
	 */
	[Event(name="executeComplete", type="com.esri.gpt.wfs.events.WFSQueryEvent")]
	
	/**
	 * Dispatched when loading begins.
	 * @eventType com.esri.wfs.events.WFSLoadingEvent.LOADING_STARTED
	 */
	[Event(name="loadingStarted", type="com.esri.gpt.wfs.events.WFSEvent")]
	
	/**
	 * Dispatched when the loading is complete.
	 * @eventType com.esri.wfs.events.WFSLoadingEvent.LOADING_COMPLETE
	 */
	[Event(name="loadingComplete", type="com.esri.gpt.wfs.events.WFSEvent")]
	
	/**
	 * Dispatched if the loading fails.
	 * @eventType com.esri.wfs.events.WFSLoadingEvent.LOADING_FAILED
	 */
	[Event(name="loadingFailed", type="com.esri.gpt.wfs.events.WFSEvent")]

	[Bindable]
	/**
	 * Executes queries against a WFS endpoint and provides transactional commits to that same endpoint.
	 */
	public class WFSLayer extends GraphicsLayer
	{
		private static const SERVICE :String = "WFS";
		private static const VERSION :String = "1.1.0";
		private static const OUTPUT_FORMAT :String = "text/xml; subtype=gml/3.1.1";
		private static const EXPIRY_TIME :String = "5";
		private static const RELEASE_ACTION :String = "ALL";
		private static const ESRI :Namespace = new Namespace( "http://www.esri.com" );
		private static const WFS :Namespace = new Namespace( "http://www.opengis.net/wfs" );
		private static const GML :Namespace = new Namespace( "http://www.opengis.net/gml" );
		private static const OGC :Namespace = new Namespace( "http://www.opengis.net/ogc" );
		private static const OWS :Namespace = new Namespace( "http://www.opengis.net/ows" );
		private static const XS :Namespace = new Namespace( "http://www.w3.org/2001/XMLSchema" );
		private static const XLINK :Namespace = new Namespace( "http://www.w3.org/1999/xlink" );
       /** Class logger **/
    private static const LOG:ILogger = Log.getLogger("WFS");
		/** Alternate label for display purposes. */
		public var label :String = null;
		
		// WFS Query
		private var m_wfsQuery :WFSQuery = new WFSQuery();
		private var m_wfsQueryTask :WFSQueryTask = new WFSQueryTask();
		
		// Feature details
		private var m_clearGraphics :Boolean = false;
		private var m_featureLimit :int = 0;
		private var m_featureIDs :ArrayCollection = new ArrayCollection();
		private var m_requestTimeout :int = 0;
    private var m_proxyUrl:String;
    private var m_urlChanged:Boolean = true;
    private var m_wfsCapabilities:WFSCapabilities;
		/**
		 * Constructs a new WFSLayer object.
		 * @param url the URL of the WFS endpoint.
		 */
		public function WFSLayer( url :String = null )
		{
			super();
			m_wfsQuery.returnGeometry = true;
			m_wfsQueryTask.url = url;
		}
		
		/** @private */
		override protected function addMapListeners() :void
		{
			super.addMapListeners();
			if( map != null )
			{
				map.addEventListener( ExtentEvent.EXTENT_CHANGE, handleExtentChange );
			}
		}
		
		/** @private */
		override protected function removeMapListeners() :void
		{
			super.removeMapListeners();
			if( map != null )
			{
				map.removeEventListener( ExtentEvent.EXTENT_CHANGE, handleExtentChange );
			}
		}
		
		/**
		 * The maximum number of features retrieved in any WFS query.
		 * If set to 0, all features will be retrieved for every request.
		 * Defaults to 0.
		 */
		public function get maxFeatures() :int
		{
			return m_wfsQuery.maxFeatures;
		}
		
		/** @private */
		public function set maxFeatures( value :int ) :void
		{
			if( m_wfsQuery.maxFeatures != value )
			{
				m_wfsQuery.maxFeatures = value;
			}
		}
    
    /** Gets the request timeout **/
    public function get requestTimeout():int {
      return this.m_requestTimeout;
    }
    
    /** Sets the request timeout **/
    public function set requestTimeout(requestTimeout:int):void {
      this.m_requestTimeout = requestTimeout;
    }
		
		/** The SRS that is used by the WFS feature type. Defaults to "EPSG:4326". */
		public function get featureSRS() :String
		{
			return m_wfsQuery.srsName;
		}
		
		/** @private */
		public function set featureSRS( value :String ) :void
		{
			if( m_wfsQuery.srsName != value )
			{
				m_wfsQuery.srsName = value;
			}
		}
		
		/** The name of the WFS feature type. */
		public function get featureName() :String
		{
			return m_wfsQuery.featureName;
		}
		
		/** @private */
		public function set featureName( value :String ) :void
		{
			if( m_wfsQuery.featureName != value )
			{
				m_wfsQuery.featureName = value;
				resetLayerState();
			}
		}
		
		/** The namespace of the WFS feature type. */
		public function get featureNamespace() :String
		{
			return m_wfsQuery.featureNamespace;
		}
		
		/** @private */
		public function set featureNamespace( value :String ) :void
		{
			if( m_wfsQuery.featureNamespace != value )
			{
				m_wfsQuery.featureNamespace = value;
			}
		}

		/**
		 * Attribute fields to exclude from the FeatureSet.
		 * All fields are included if this is null or empty.
		 * @see outFields
		 */
		public function get excludeFields() :ArrayCollection
		{
			return m_wfsQuery.excludeFields;
		}
		
		/** @private */
		public function set excludeFields( value :ArrayCollection ) :void
		{
			if( m_wfsQuery.excludeFields != value )
			{
				m_wfsQuery.excludeFields = value;
			}
		}
		
		/**
		 * Attribute fields to include in the FeatureSet.
		 * All fields are included if this is null or empty.
		 * @see excludeFields
		 */
		public function get outFields() :ArrayCollection
		{
			return m_wfsQuery.outFields;
		}
		
		/** @private */
		public function set outFields( value :ArrayCollection ) :void
		{
			if( m_wfsQuery.outFields != value )
			{
				m_wfsQuery.outFields = value;
			}
		}
		
		/**
		 * If true, parses and outputs coordinates in "Y X" format.
		 * Otherwise, parses and outputs coordinates in "X Y" format.
		 * Defaults to true.
		 */
		public function get swapCoordinates() :Boolean
		{
			return m_wfsQuery.swapCoordinates;
		}
		
		/** @private */
		public function set swapCoordinates( value :Boolean ) :void
		{
			if( m_wfsQuery.swapCoordinates != value )
			{
				m_wfsQuery.swapCoordinates = value;
			}
		}
		
		/** The feature property name which will contain the feature geometry. Defaults to "SHAPE". */
		public function get shapeField() :String
		{
			return m_wfsQuery.shapeField;
		}
		
		/** @private */
		public function set shapeField( value :String ) :void
		{
			if( m_wfsQuery.shapeField != value )
			{
				m_wfsQuery.shapeField = value;
			}
		}
		
		/**
		 * The default symbol function for this layer. The function should have the following signature:
		 * public function mySymbolFunction( graphic : Graphic ) : Symbol
		 * This property can be used as the source for data binding.
		 *
		override public var symbolFunction :Function = function( graphic :Graphic ) :Symbol
		{
			if( graphic != null && graphic.geometry != null )
			{
				if( graphic.geometry is MapPoint )
				{
					return new SimpleMarkerSymbol( "circle", 13, 0xfdd0a2, 0.75, new SimpleLineSymbol( "solid", 0xfd8d3c, 1, 2 ) );
				}
				if( graphic.geometry is Polygon )
				{
					return new SimpleFillSymbol( "solid", 0xfdd0a2, 0.75, new SimpleLineSymbol( "solid", 0xfd8d3c, 1, 2 ) );
				}
				if( graphic.geometry is Polyline )
				{
					return new SimpleLineSymbol( "solid", 0xfd8d3c, 1, 3 );
				}
			}
			return null;
		}*/
		
		/**
	     * URL to the ArcGIS Server WFS endpoint. For more information on constructing a URL, see Using ArcGIS Services Directory.
	     */
		public function get url() :String
		{
		    return m_wfsQueryTask.url;
		}

		/** @private */
		public function set url( value :String ) :void
		{
      m_urlChanged = true;
      invalidateProperties();
      
			if( m_wfsQueryTask.url != value )
			{
		    	m_wfsQueryTask.url = value;
		    	resetLayerState();
		 	}
		}
		
		/** The URL to proxy the request through. */
		public function get proxyURL() :String
		{
      return this.m_proxyUrl;
			//return m_wfsQueryTask.proxyURL;
		}
		
		/** @private */
		public function set proxyURL( value :String ) :void
		{
      this.m_proxyUrl = value;
      
			if( m_wfsQueryTask.proxyURL != value )
			{
				m_wfsQueryTask.proxyURL = value;
			}
		}
		
		/**
		 * The maximum number of features stored in memory or displayed on
		 * the map at any given time. If this number of features is reached, the
		 * store of features will be cleared. A value of 0 (default) indicates that
		 * the feature store will never be cleared, all unique features encountered
		 * will be stored and displayed on the map.
		 */
		public function get featureLimit() :int
		{
			return m_featureLimit;
		}
		
		/** @private */
		public function set featureLimit( value :int ) :void
		{
			if( m_featureLimit != value )
			{
				m_featureLimit = value;
			}
		}
		
		/**
		 * Returns the geometry type of the features currently active in the layer.
		 * @see Geometry.type
		 */
		public function get geometryType() :String
		{
			if( numGraphics > 0 )
			{
				var graphic :Graphic = getChildAt( 0 ) as Graphic;
				if( graphic.geometry )
				{
					return graphic.geometry.type;
				}
			}
			return null;
		}
		
		/** Performs the query against the WFS endpoint. */ 
		public function executeQuery() :void
		{
			if( map && hasRequiredParameters() )
			{
				m_wfsQuery.extent = map.extent;				
				
				if( m_featureLimit > 0 && numChildren > m_featureLimit )
				{
					m_clearGraphics = true;
				}
				
				// event: loading started
				dispatchEvent( new WFSEvent( WFSEvent.LOADING_STARTED ) );
				
				m_wfsQueryTask.execute( m_wfsQuery, new AsyncResponder(
					function onSuccess( featureSet :FeatureSet, token :Object = null ) :void
					{
						if( m_clearGraphics )
						{
							clear();
							m_clearGraphics = false;
						}
						
						for each( var graphic :Graphic in featureSet.features )
						{
							var featureID :String = graphic.attributes.GML_FEATURE_ID;
							if( !WFSUtil.containsString( featureID, m_featureIDs ) )
							{
								add( graphic );
								m_featureIDs.addItem( featureID );
							}
						}
						
						// event: loading complete
						dispatchEvent( new WFSEvent( WFSEvent.LOADING_COMPLETE ) );
					},
					function onFault( info :Object, token :Object = null ) :void
					{
						// event: loading failed
						dispatchEvent( new WFSEvent( WFSEvent.LOADING_FAILED ) );
						handleStringError( "Unable to load features." );
					}
				) );
			}
		}
		
		/**
		 * @private
		 * Handles a simple error.
		 * @param error the error message as a String.
		 */
		private function handleStringError( error :String ) :void
		{
			var stringFault :Fault = new Fault( null, error );
	        dispatchEvent( new FaultEvent( FaultEvent.FAULT, false, true, stringFault ) );
		}
		
		/** Resets the layer state in the event. */
		private function resetLayerState() :void
		{
			clear();
		}
		
		/** @private */
		private function handleExtentChange( event :ExtentEvent ) :void
		{
			executeQuery();
		}
		
		/** Determines if the required parameters are set. Returns true if they are, false otherwise. */
		private function hasRequiredParameters() :Boolean
		{
			if( m_wfsQuery.featureName &&
				m_wfsQuery.srsName &&
				m_wfsQueryTask.url )
			{
				return true;
			}
			return false;
		}
    
    override protected function commitProperties():void 
    {
      super.commitProperties();
      this.invalidateLayer();
      if (m_urlChanged)
      {
        m_urlChanged = false;
        
        removeAllChildren();
        loadServiceInfo();
      }
    }
    
    private function loadServiceInfo():void 
    {
      var capsURL:String = url;
      if (!capsURL)
      {
        LOG.info("Capabilities url is null");
        return;
      }
      LOG.debug("Recieved Capabilities URL" + capsURL);
      
      // Ask for the highest WMS version. The server should send back a 
      // downgraded wmsCapabilities version if it doesn't support this one.
      //capsURL += getURLPrefix(capsURL);		
      if(capsURL.match(new RegExp("\\?")) == null ||
        capsURL.match(new RegExp("\\?")).length < 1) {
        capsURL = capsURL + "?";
      }
      //capsURL = capsURL.replace(/&?service=wms/i, "");
      //capsURL = capsURL.replace(/&?request=GetCapabilities/i, "");
      //capsURL = capsURL.replace(/&?version=(\d|\.)*/i, "");
      
      var wfsurl:UrlUtils = new UrlUtils(capsURL);
      /*if (wmsurl.valid){
      capsURL = wmsurl.rootURL() + "?" ;
      }*/
      var value:String = wfsurl.getParameterValue("service");
      capsURL += (value != null)? "": "&service=WFS";
      value = wfsurl.getParameterValue("version");
      capsURL += (value != null)? "": "&version=1.1.0";
      value = wfsurl.getParameterValue("request");
      capsURL += (value != null)? "": "&request=GetCapabilities";
      
      //capsURL = capsURL.replace(/&request=GetMap/i, "");
      //capsURL = capsURL.replace(/&request=GetFeatureInfo/i, "");
      //if(capsURL.indexOf("?") < capsURL.length - 1) {
      //  capsURL += "&";
      //}
      //capsURL += "service=WMS&version=1.3.0&request=GetCapabilities";
      LOG.debug("constructed capabilites url" + capsURL);
      if (proxyURL)
      {
        if(proxyURL.match(new RegExp("\\?")) == null ||
          proxyURL.match(new RegExp("\\?")).length < 1) {
          capsURL = proxyURL + "?" + escape(capsURL);
        } else {
          capsURL = proxyURL + escape(capsURL);
        }
        //capsURL = proxyURL + capsURL;
      }
      
      if (Log.isDebug())
      {
        LOG.debug("{0}::WFS GetCapabilities URL: {1}", id, capsURL)
      }
      
      var httpService:HTTPService = new HTTPService();
      httpService.resultFormat = "text";
      httpService.requestTimeout = this.requestTimeout;
      httpService.url = capsURL;
      
      //_visibleLayers = null;
      //var layer:Layer = this;
      
      LOG.debug("Get WFS Capabilities URL" + capsURL);
      var token:AsyncToken = httpService.send();
      token.serviceUrl = capsURL;
      var layer:Layer = this;
      token.addResponder(new AsyncResponder(
        function onResult( event:ResultEvent, token:Object = null ):void
        {
          // Make sure the result is for the current URL
          if (token == url)
          {
            try{
              m_wfsCapabilities = new  WFSCapabilities(event.result as String);
              /*if (_wfsCapabilities != null && _wfsCapabilities.layerInfo != null)
              {
                setDefaultVisibleLayers();
                
                invalidateLayer();
                var flattenedLayers:ArrayCollection = new ArrayCollection();
                if(wmsCapabilities.layerInfo == null) {
                  dispatchEvent(new LayerEvent(LayerEvent.LOAD_ERROR, layer, 
                    new Fault("WMS", "No Layer information found" + url, 
                      "No Layer information found" + url)));
                  return;
                }
                flattenedLayers.addItem(wmsCapabilities.layerInfo);
                var index:Number = 0;
                for each (var layerInfo:WMSLayerInfo in flattenedLayers) {
                  if(layerInfo != null) {
                    //layerInfo.defaultVisibility = index < 
                    //this.numLayersShownInitially - 1;
                    layerInfo.id = index;
                    
                    index++;
                  }
                  if(layerInfo == null || layerInfo.subLayers == null || 
                    layerInfo.subLayers.length < 1) {
                    continue;
                  }
                  for each ( var childLayerInfo:WMSLayerInfo in layerInfo.subLayers) {
                    flattenedLayers.addItem(childLayerInfo);
                  }
                  
                }
                //_visibleLayers = flattenedLayers;
                setLoaded(true);
              } else {
                var error:String = WMSError.readError(event.result as XML);
                if(error == null || error == "") {
                  error = "No Layers Found in WFS url"
                }
                dispatchEvent(new LayerEvent(LayerEvent.LOAD_ERROR, layer, 
                  new Fault("WMS", error + url, error + url)));
                if (Log.isError()) 
                {
                  logger.error("{0}::Invalid WFS capabilities: {1}", id, ObjectUtil.toString(event));
                }
              }*/
            } catch(error:Error) {
              dispatchEvent(new LayerEvent(LayerEvent.LOAD_ERROR, layer, 
                new Fault("WFS", event.message
                  + ": Error while connecting to url" + capsURL, 
                  event.message
                  + ": Error while connecting to url" + capsURL)));
            }	 
          } 
        },
        function onFault( event:FaultEvent, token:Object = null ):void
        {
          dispatchEvent(new LayerEvent(LayerEvent.LOAD_ERROR, layer, 
            new Fault("WFS", event.message
              + ": Error while connecting to url" + capsURL,
              event.message
              + ": Error while connecting to url" + capsURL)));
          if (Log.isError())
          {
            LOG.error("{0}::{1}", id, ObjectUtil.toString(event));
          }
        },
        url
      ));
    }

	}
}