This file contains mappings being used to transform OGC GetCapabilities response to 
Dublin Core metadata document for Geoportal extension 10.
=====================================================================================

Lookup Namespaces
------------------
	[CSW-WMS-WCS-WFS-WPS-SOS-SPS]	
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
		xmlns:xlink="http://www.w3.org/1999/xlink" 
		xmlns:fo="http://www.w3.org/1999/XSL/Format" 
		xmlns:ows="http://www.opengis.net/ows" 
		xmlns:ows11="http://www.opengis.net/ows/1.1" 
		xmlns:gml="http://www.opengis.net/gml" 
		xmlns:csw="http://www.opengis.net/cat/csw" 
		xmlns:csw202="http://www.opengis.net/cat/csw/2.0.2" 
		xmlns:wcs="http://www.opengis.net/wcs" 
		xmlns:wcs11="http://www.opengis.net/wcs/1.1" 
		xmlns:wcs111="http://www.opengis.net/wcs/1.1.1" 
		xmlns:wfs="http://www.opengis.net/wfs" 
		xmlns:wms="http://www.opengis.net/wms" 
		xmlns:wps100="http://www.opengis.net/wps/1.0.0" 
		xmlns:sos10="http://www.opengis.net/sos/1.0" 
		xmlns:sps="http://www.opengis.net/sps" 		
		xmlns:tml="http://www.opengis.net/tml" 
		xmlns:sml="http://www.opengis.net/sensorML/1.0.1" 
		xmlns:myorg="http://www.myorg.org/features" 
		xmlns:swe="http://www.opengis.net/swe/1.0.1" 
		xmlns:om="http://www.opengis.net/om/1.0" 		
		xmlns:xal="urn:oasis:names:tc:ciq:xsdschema:xAL:2.0"
		xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
		xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0" 
		xmlns:dc="http://purl.org/dc/elements/1.1/" 
		xmlns:dct="http://purl.org/dc/terms/" 
		xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/"

	[KML]
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
		xmlns:xlink="http://www.w3.org/1999/xlink" 
		xmlns:fo="http://www.w3.org/1999/XSL/Format" 
		xmlns:ows="http://www.opengis.net/ows" 
		xmlns:kml22="http://www.opengis.net/kml/2.2" 
		xmlns:tml="http://www.opengis.net/tml" 
		xmlns:sml="http://www.opengis.net/sensorML/1.0.1" 
		xmlns:atom="http://www.w3.org/2005/Atom" 
		xmlns:xal="urn:oasis:names:tc:ciq:xsdschema:xAL:2.0" 
		xmlns:gml="http://www.opengis.net/gml" 
		
	[GeoRSS]
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
		xmlns:xlink="http://www.w3.org/1999/xlink" 
		xmlns:fo="http://www.w3.org/1999/XSL/Format" 
		xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#" 
		xmlns:eq="http://earthquake.usgs.gov/rss/1.0/" 
		xmlns:ows="http://www.opengis.net/ows" 
		xmlns:georss="http://www.georss.org/georss" 
		xmlns:atom="http://www.w3.org/2005/Atom" 
		xmlns:gml="http://www.opengis.net/gml"
-----------  
Element Set
-----------

Term Name: title
----------------   
Number of occurrences : 1
Xpath Lookups:
	[CSW-WMS-WCS-WFS-WPS-SOS-SPS]
			//ows:ServiceIdentification/ows:Title | 		
			//ows11:ServiceIdentification/ows11:Title | 
			/WMT_MS_Capabilities/Service/Title |
			//wms:Service/wms:Title  | 
			/wms:WMT_MS_Capabilities/wms:Service/wms:Title | 							
			//wcs:Service/wcs:name | 
			//wcs11:Service/wcs11:name | 
			//wcs111:Service/wcs111:name
	
	[KML]
			//kml22:Document/kml22:name
			
	[GeoRSS]
			/rss/channel/title | 
			//atom:feed//atom:title
			
Term Name: description
----------------   
Number of occurrences : 1
Xpath Lookups:
	[CSW-WMS-WCS-WFS-WPS-SOS-SPS]
			//Service/Abstract | 								 
			//ows:ServiceIdentification/ows:Abstract | 
			//ows11:ServiceIdentification/ows11:Abstract |
			//Service/Abstract | 
			//wms:Service/wms:Abstract | 
			//wcs:Service/wcs:description | 													
			//wcs11:Service/wcs11:description | 
			//wcs111:Service/wcs111:description 
		
	[KML]
			//kml22:Document/kml22:description
			
	[GeoRSS]
			/rss/channel/description | 
			//atom:feed//atom:subtitle
					
Term Name: date
----------------   
Number of occurrences : 1*
Xpath Lookups:
	[CSW-WMS-WCS-WFS-WPS-SOS-SPS]
			//gml:relatedTime
	
	[KML]
			//kml22:TimeStampType//kml22:when | 
			//gml:relatedTime
	
	[GeoRSS]
			/rss/channel/pubDate | 
			//atom:feed//atom:updated
			
Term Name: format
----------------   
Number of occurrences : 1*
Xpath Lookups:
	[CSW-WMS-WCS-WFS-WPS-SOS-SPS]
			//ows:OperationsMetadata//ows:Operation[@name='DescribeRecord']//ows:Parameter[@name='outputFormat']//ows:Value |
			//ows:OperationsMetadata//ows:Operation[@name='DescribeSensor']//ows:Parameter[@name='outputFormat']/ows:AllowedValues/ows:Value | 
			//wms:Capability//wms:Request//wms:GetCapabilities//wms:Format |  
			/wfs:WFS_Capabilities/ows:OperationsMetadata/ows:Operation[@name='GetCapabilities']/ows:Parameter[@name='AcceptFormats']/ows:Value
		
	[KML]
			//atom:link/@atomMediaType/text()
			
	[GeoRSS]
			/rss/channel//atom:link/@type | 
			//atom:feed//atom:entry//atom:link/@type | 
			//atom:feed//atom:link/@type
							
Term Name: language
----------------   
Number of occurrences : 1*
Xpath Lookups:
	[CSW-WMS-WCS-WFS-WPS-SOS-SPS]
			//ows:Language
	
	[KML]
			//ows:Language/@atomLanguageTag/text() | 
			//atom:link/@atomLanguageTag/text()
			
	[GeoRSS]
			/Language
			
Term Name: contributor
----------------   
Number of occurrences : 1*
Xpath Lookups:
	[CSW-WMS-WCS-WFS-WPS-SOS-SPS]
			//ows:ServiceProvider/ows:ProviderName | 
			//ows11:ServiceProvider/ows11:ProviderName | 	
			/WMT_MS_Capabilities/Service/ContactInformation/ContactPersonPrimary/ContactPerson	|					 							 
			//wcs:Service/wcs:responsibleParty/wcs:individualName |
			//wcs11:Service/wcs11:responsibleParty/wcs11:individualName | 
			//wcs111:Service/wcs111:responsibleParty/wcs111:individualName | 
			/wps100:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:IndividualName |
			/wps100:Capabilities/ows11:ServiceProvider/ows11:ServiceContact/ows11:IndividualName					
	
	[KML]
	
	[GeoRSS]
			/rss/channel/managingEditor
			
Term Name: identifier
----------------   
Number of occurrences : 1
Xpath Lookups:
			None
Xslt Parameter: 
			sourceUrl [GetCapabilities request Url]

Term Name: type
----------------   
Number of occurrences : 1*
Xpath Lookups:
	[CSW-WMS-WCS-WFS-WPS-SOS-SPS]
			//ows:ServiceIdentification/ows:ServiceType | 
			//ows11:ServiceIdentification/ows11:ServiceType	
Xslt Parameter:
			serviceType [Service parameter value from query string of GetCapabilities request]

Term Name: creator
----------------   
Number of occurrences : 1*
Xpath Lookups:
	[CSW-WMS-WCS-WFS-WPS-SOS-SPS]
			//ows:ServiceProvider/ows:ServiceContact/ows:IndividualName | 
			//ows11:ServiceProvider/ows11:ServiceContact/ows11:IndividualName |
			//wms:Service//wms:ContactInformation/wms:ContactPersonPrimary/wms:ContactPerson    
		
	[KML]
			//atom:author//atom:name
			
	[GeoRSS]
			/rss/channel/dc:publisher | 
			//atom:feed//atom:author//atom:name | 
			/rss/channel/generator
			
Term Name: subject
----------------   
Number of occurrences : 1*
Xpath Lookups:
	[CSW-WMS-WCS-WFS-WPS-SOS-SPS]
			//ows:ServiceIdentification/ows:Keywords/ows:Keyword |
			//ows11:ServiceIdentification/ows11:Keywords/ows11:Keyword |
			/WMT_MS_Capabilities/Service/KeywordList/Keyword	|
			//wms:Service/wms:KeywordList/wms:Keyword | 
			//wcs:Service/wcs:keywords/wcs:keyword | 
			//wcs11:Service/wcs11:keywords/wcs11:keyword | 
			//wcs111:Service/wcs111:keywords/wcs111:keyword 
		
	[KML]
			//kml22:key
			
	[GeoRSS]
	
			
Term Name: references
----------------   
Number of occurrences : 1*
Xpath Lookups:
	[CSW-WMS-WCS-WFS-WPS-SOS-SPS]
			//Service/OnlineResource/@xlink:href |
			//ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get/@xlink:href |
			//ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Post/@xlink:href |
			//ows11:Operation[@name='GetCapabilities']/ows11:DCP/ows11:HTTP/ows11:Get/@xlink:href |
			//ows11:Operation[@name='GetCapabilities']/ows11:DCP/ows11:HTTP/ows11:Post/@xlink:href |
			//wms:GetCapabilities/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href | 	
			//wms:GetCapabilities/wms:DCPType/wms:HTTP/wms:Post/wms:OnlineResource/@xlink:href | 					           
			//wcs:Capability/wcs:Request/wcs:GetCapabilities/wcs:DCPType/wcs:HTTP/wcs:Get/wcs:OnlineResource/@xlink:href | 
			 //wcs:Capability/wcs:Request/wcs:GetCapabilities/wcs:DCPType/wcs:HTTP/wcs:Post/wcs:OnlineResource/@xlink:href | 																
			//wcs11:Capability/wcs11:Request/wcs11:GetCapabilities/wcs11:DCPType/wcs11:HTTP/wcs11:Get/wcs11:OnlineResource/@xlink:href | 
			//wcs11:Capability/wcs11:Request/wcs11:GetCapabilities/wcs11:DCPType/wcs11:HTTP/wcs11:Post/wcs11:OnlineResource/@xlink:href |
			//wcs111:Capability/wcs111:Request/wcs111:GetCapabilities/wcs111:DCPType/wcs111:HTTP/wcs111:Get/wcs111:OnlineResource/@xlink:href |
			//wcs111:Capability/wcs111:Request/wcs111:GetCapabilities/wcs111:DCPType/wcs111:HTTP/wcs111:Post/wcs111:OnlineResource/@xlink:href
	
	[KML]
			//kml22:Link//kml22:href
			
	[GeoRSS]
	
			

Xslt Parameter: 
			sourceUrl [GetCapabilities request Url]

Term Name: WGS84BoundingBox
----------------   
Number of occurrences : 1
Xpath Lookups:
	[CSW-WMS-WCS-WFS-WPS-SOS-SPS]
		[WMS Service]:
				  //wms:LatLonBoundingBox |
				  /WMT_MS_Capabilities/Capability/Layer/Layer/LatLonBoundingBox |
				  /WMT_MS_Capabilities/Capability/Layer/LatLonBoundingBox |				  
				  //wms:Capability/wms:Layer/wms:BoundingBox[@CRS='EPSG:4326']
				  				@[minx,miny,maxx,may]
				  
				  //wms:EX_GeographicBoundingBox//wms:westBoundLongitude
				  //wms:EX_GeographicBoundingBox//wms:eastBoundLongitude
				  //wms:EX_GeographicBoundingBox//wms:northBoundLongitude
				  //wms:EX_GeographicBoundingBox//wms:southBoundLongitude
				  
	    [WCS / WFS / SPS / SOS]:	  
				  //ows:LowerCorner | 
				  //gml:LowerCorner | 
				  //gml:pos[1] | 
				  //gml:coord[1] | 
				  //gml:lowerCorner
				  
				  //ows:UpperCorner | 
				  //gml:UpperCorner | 
				  //gml:pos[2] | 
				  //gml:coord[2] | 
				  //gml:upperCorner
	
	[KML]
				  //kml22:coordinates
				  
	[GeoRSS]
				  //georss:point | 
				  //georss:line | 
				  //georss:polygon | 
				  //georss:box | 
				  //gml:Point  | 
				  //gml:posList | 
				  //gml:LinearRing |
				  //gml:Envelope |
				  //geo:lat |
				  //geo:long |
				  //gml:LowerCorner | 
				  //gml:UpperCorner
------------------