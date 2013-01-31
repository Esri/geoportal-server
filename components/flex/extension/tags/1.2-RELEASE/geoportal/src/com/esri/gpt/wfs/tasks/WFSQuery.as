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
	import com.esri.ags.geometry.Extent;
	
	import mx.collections.ArrayCollection;

	/**
	 * Input to WFSQueryTask. Not all query properties are required to execute a WFSQueryTask.
	 * The query definition requires the following properties: featureName, featureNamespace, srsName.
	 * Optional properties include outFields, excludeFields, extent, maxFeatures, returnGeometry, shapeField, and swapCoordinates.
	 * If returnGeometry is set to true, make sure that shapeField is set to the name of the field containing the geometry.
	 * @see WFSQueryTask
	 */
	public class WFSQuery
	{
		private var m_srsName :String = "EPSG:4326";
		private var m_featureNamespace :String = "http://www.esri.com";
		private var m_featureName :String;
		private var m_shapeField :String = "SHAPE";
		private var m_outFields :ArrayCollection;
		private var m_excludeFields :ArrayCollection;
		private var m_extent :Extent;
		private var m_swapCoordinates :Boolean = true;
		private var m_returnGeometry :Boolean = true;
		private var m_maxFeatures :int = 0;
		
		/** Creates a new WFSQuery. */
		public function WFSQuery()
		{
		}
		
		[Bindable]
		/**
		 * The SRS name used by the feature. Defaults to "EPSG:4326".
		 * This should match the SRS name returned by the WFS endpoint in the response to a GetFeature request.
		 */
		public function get srsName() :String
		{
			return m_srsName;
		}
		
		/** @private */
		public function set srsName( srsName :String ) :void
		{
			if( m_srsName != srsName )
			{
				m_srsName = srsName;
			}
		}
		
		[Bindable]
		/**
		 * The namespace that the feature resides in. Defaults to "http://www.esri.com".
		 * This should match the namespace of the features returned by the WFS endpoint in the response to a GetFeature request.
		 */ 
		public function get featureNamespace() :String
		{
			return m_featureNamespace;
		}
		
		/** @private */
		public function set featureNamespace( featureNamespace :String ) :void
		{
			m_featureNamespace = featureNamespace;
		}
		
		[Bindable]
		/** The name of the feature. Should match the typeName you wish to query against through the WFS endpoint. */
		public function get featureName() :String
		{
			return m_featureName;
		}
		
		/** @private */
		public function set featureName( featureName :String ) :void
		{
			m_featureName = featureName;
		}
		
		[Bindable]
		/** The feature property name which will contain the feature geometry. Defaults to "SHAPE". */
		public function get shapeField() :String
		{
			return m_shapeField;
		}
		
		/** @private */
		public function set shapeField( shapeField :String ) :void
		{
			if( m_shapeField != shapeField )
			{
				m_shapeField = shapeField;
			}
		}
		
		[Bindable]
		/** If true, each feature in the FeatureSet includes the geometry. */
		public function get returnGeometry() :Boolean
		{
			return m_returnGeometry;
		}
		
		/** @private */
		public function set returnGeometry( returnGeometry :Boolean ) :void
		{
			m_returnGeometry = returnGeometry;
		}
		
		[Bindable]
		/**
		 * If true, parses and outputs coordinates in "Y X" format. Otherwise, parses and outputs coordinates in "X Y" format.
		 * Defaults to true.
		 */
		public function get swapCoordinates() :Boolean
		{
			return m_swapCoordinates;
		}
		
		/** @private */
		public function set swapCoordinates( swapCoordinates :Boolean ) :void
		{
			if( m_swapCoordinates != swapCoordinates )
			{
				m_swapCoordinates = swapCoordinates;
			}
		}
		
		[Bindable]
		public function get extent() :Extent
		{
			return m_extent;
		}
		
		public function set extent( extent :Extent ) :void
		{
			if( m_extent != extent )
			{
				m_extent = extent;
			}
		}
		
		[Bindable]
		/**
		 * Attribute fields to include in the FeatureSet.
		 * All fields are included if this is null or empty.
		 * @see excludeFields
		 */
		public function get outFields() :ArrayCollection
		{
			return m_outFields;
		}
		
		/** @private */
		public function set outFields( outFields :ArrayCollection ) :void
		{
			if( m_outFields != outFields )
			{
				m_outFields = outFields;
			}
		}
		
		[Bindable]
		/**
		 * Attribute fields to exclude from the FeatureSet.
		 * All fields are included if this is null or empty.
		 * @see outFields
		 */
		public function get excludeFields() :ArrayCollection
		{
			return m_excludeFields;
		}
		
		/** @private */
		public function set excludeFields( excludeFields :ArrayCollection ) :void
		{
			if( m_excludeFields != excludeFields )
			{
				m_excludeFields = excludeFields;
			}
		}
		
		[Bindable]
		/** Limits the number of features returned in the FeatureSet if greater than 0. Defaults to 0. */ 
		public function get maxFeatures() :int
		{
			return m_maxFeatures;
		}
		
		/** @private */
		public function set maxFeatures( maxFeatures :int ) :void
		{
			if( m_maxFeatures != maxFeatures )
			{
				m_maxFeatures = maxFeatures;
			}
		}
	}
}