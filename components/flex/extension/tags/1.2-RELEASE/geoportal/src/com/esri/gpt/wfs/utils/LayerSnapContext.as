package com.esri.gpt.wfs.utils
{
	import com.esri.ags.layers.GraphicsLayer;
	
	import flash.events.EventDispatcher;
	
	import mx.events.PropertyChangeEvent;
	
	/** Contains information about each layer for a multi-layer snapping environment. */
	public class LayerSnapContext extends EventDispatcher
	{
		private var m_name :String;
		private var m_layer :GraphicsLayer;
		private var m_end :Boolean = false;
		private var m_edge :Boolean = false;
		private var m_vertex :Boolean = false;
		
		/** Constructor. */
		public function LayerSnapContext()
		{
		}
		
		[Bindable("propertyChange")]
		/** The name property. */
		public function get name() :String
		{
			return m_name;
		}
		
		/** @private */
		public function set name( value :String ) :void
		{
			if( m_name != value )
			{
				var oldValue :String = m_name;
				m_name = value;
				dispatchEvent( PropertyChangeEvent.createUpdateEvent( this, "name", oldValue, m_name ) );
			}
		}
		
		[Bindable("propertyChange")]
		/** The layer property. */
		public function get layer() :GraphicsLayer
		{
			return m_layer;
		}
		
		/** @private */
		public function set layer( value :GraphicsLayer ) :void
		{
			if( m_layer != value )
			{
				var oldValue :GraphicsLayer = m_layer;
				m_layer = value;
				dispatchEvent( PropertyChangeEvent.createUpdateEvent( this, "layer", oldValue, m_layer ) );
			}
		}
		
		[Bindable("propertyChange")]
		/** The end property. */
		public function get end() :Boolean
		{
			return m_end;
		}
		
		/** @private */
		public function set end( value :Boolean ) :void
		{
			if( m_end != value )
			{
				m_end = value;
				dispatchEvent( PropertyChangeEvent.createUpdateEvent( this, "end", !m_end, m_end ) );
			}
		}
		
		[Bindable("propertyChange")]
		/** The edge property. */
		public function get edge() :Boolean
		{
			return m_edge;
		}
		
		/** @private */
		public function set edge( value :Boolean ) :void
		{
			if( m_edge != value )
			{
				m_edge = value;
				dispatchEvent( PropertyChangeEvent.createUpdateEvent( this, "edge", !m_edge, m_edge ) );
			}
		}
		
		[Bindable("propertyChange")]
		/** The vertex property. */
		public function get vertex() :Boolean
		{
			return m_vertex;
		}
		
		/** @private */
		public function set vertex( value :Boolean ) :void
		{
			if( m_vertex != value )
			{
				m_vertex = value;
				dispatchEvent( PropertyChangeEvent.createUpdateEvent( this, "vertex", !m_vertex, m_vertex ) );
			}
		}
		
		/** Writes out the object properties in String form. */
		public override function toString() :String
		{
			return "[LayerSnapContext[name: " + name + ", layer: " + layer + ", end: " + end + ", edge: " + edge + ", vertex: " + vertex + "]";
		}
	}
}