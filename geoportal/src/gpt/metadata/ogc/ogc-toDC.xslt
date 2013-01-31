<?xml version="1.0" encoding="UTF-8"?>
<!--
 See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 Esri Inc. licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:ows="http://www.opengis.net/ows" xmlns:ows11="http://www.opengis.net/ows/1.1" xmlns:gml="http://www.opengis.net/gml" xmlns:csw="http://www.opengis.net/cat/csw" xmlns:csw202="http://www.opengis.net/cat/csw/2.0.2" xmlns:wcs="http://www.opengis.net/wcs" xmlns:wcs11="http://www.opengis.net/wcs/1.1" xmlns:wcs111="http://www.opengis.net/wcs/1.1.1" xmlns:wfs="http://www.opengis.net/wfs" xmlns:wms="http://www.opengis.net/wms" xmlns:wps100="http://www.opengis.net/wps/1.0.0" xmlns:sos10="http://www.opengis.net/sos/1.0" xmlns:sps="http://www.opengis.net/sps" xmlns:tml="http://www.opengis.net/tml" xmlns:sml="http://www.opengis.net/sensorML/1.0.1" xmlns:myorg="http://www.myorg.org/features" xmlns:swe="http://www.opengis.net/swe/1.0.1" xmlns:exslt="http://exslt.org/common">
	<xsl:param name="sourceUrl"/>
	<xsl:param name="serviceType"/>
	<xsl:output omit-xml-declaration="no" method="xml" indent="yes" encoding="UTF-8"/>
	<xsl:template match="/">
		<xsl:call-template name="main"/>
	</xsl:template>
	<xsl:template name="main">
		<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/" xsl:exclude-result-prefixes="ows ows11 wms wps100 swe myorg tml sml sps sos10 wfs wcs wcs11 wcs111 csw csw202 gml">
			<rdf:Description>
			  <xsl:attribute namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#" name="about">
			    <xsl:value-of select="$sourceUrl"/>
        </xsl:attribute>
				<xsl:if test="							
								//ows:ServiceIdentification/ows:Title | 		
								//ows11:ServiceIdentification/ows11:Title | 
								/WMT_MS_Capabilities/Service/Title |
								//wms:Service/wms:Title  |
								//wfs:Service/wfs:Title | 
								/wms:WMT_MS_Capabilities/wms:Service/wms:Title | 							
								//wcs:Service/wcs:name | 
								//wcs11:Service/wcs11:name | 
								//wcs111:Service/wcs111:name |
								//wcs:Service/wcs:name | 
								//wcs11:Service/wcs11:name | 
								//wcs111:Service/wcs111:name |
								//wfs:Service/wfs:name				
						">
					<dc:title>
						<xsl:value-of select="//ows:ServiceIdentification/ows:Title | 		
								//ows11:ServiceIdentification/ows11:Title | 
								/WMT_MS_Capabilities/Service/Title |
								//wms:Service/wms:Title  |
								//wfs:Service/wfs:Title | 
								/wms:WMT_MS_Capabilities/wms:Service/wms:Title | 							
								//wcs:Service/wcs:name | 
								//wcs11:Service/wcs11:name | 
								//wcs111:Service/wcs111:name |
								//wcs:Service/wcs:name | 
								//wcs11:Service/wcs11:name | 
								//wcs111:Service/wcs111:name | 
								//wfs:Service/wfs:name"/>
					</dc:title>
				</xsl:if>
				<xsl:if test="
								//Service/Abstract | 								 
								//ows:ServiceIdentification/ows:Abstract | 
								//ows11:ServiceIdentification/ows11:Abstract |
								//Service/Abstract | 
								//wfs:Service/wfs:Abstract |
								//wms:Service/wms:Abstract | 
								//wcs:Service/wcs:description | 													
								//wcs11:Service/wcs11:description | 
								//wcs111:Service/wcs111:description 
						">
					<dc:description>
						<xsl:value-of select="//Service/Abstract | 								 
								//ows:ServiceIdentification/ows:Abstract | 
								//ows11:ServiceIdentification/ows11:Abstract |
								//Service/Abstract | 
								//wfs:Service/wfs:Abstract |
								//wms:Service/wms:Abstract | 
								//wcs:Service/wcs:description | 													
								//wcs11:Service/wcs11:description | 
								//wcs111:Service/wcs111:description"/>
					</dc:description>
				</xsl:if>
				<xsl:for-each select="//gml:relatedTime">
					<dc:date>
						<xsl:value-of select="text()"/>
					</dc:date>
				</xsl:for-each>
				<xsl:for-each select="								//ows:OperationsMetadata//ows:Operation[@name='DescribeRecord']//ows:Parameter[@name='outputFormat']//ows:Value |					//ows:OperationsMetadata//ows:Operation[@name='DescribeSensor']//ows:Parameter[@name='outputFormat']/ows:AllowedValues/ows:Value | //wms:Capability//wms:Request//wms:GetCapabilities//wms:Format | /wfs:WFS_Capabilities/ows:OperationsMetadata/ows:Operation[@name='GetCapabilities']/ows:Parameter[@name='AcceptFormats']/ows:Value">
					<dc:format>
						<xsl:value-of select="text()"/>
					</dc:format>
				</xsl:for-each>
				<xsl:for-each select="//ows:Language">
					<dc:language>
						<xsl:value-of select="text()"/>
					</dc:language>
				</xsl:for-each>
				<xsl:for-each select="
								//ows:ServiceProvider/ows:ProviderName | 
								//ows11:ServiceProvider/ows11:ProviderName | 	
								/WMT_MS_Capabilities/Service/ContactInformation/ContactPersonPrimary/ContactPerson |					 	       				                      //wcs:Service/wcs:responsibleParty/wcs:individualName |
								//wcs11:Service/wcs11:responsibleParty/wcs11:individualName | 
								//wcs111:Service/wcs111:responsibleParty/wcs111:individualName | 
								/wps100:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:IndividualName |
								/wps100:Capabilities/ows11:ServiceProvider/ows11:ServiceContact/ows11:IndividualName
						">
					<dc:contributor>
						<xsl:value-of select="text()"/>
					</dc:contributor>
				</xsl:for-each>
				<dc:identifier>
					<xsl:value-of select="$sourceUrl"/>
				</dc:identifier>
				<xsl:choose>
					<xsl:when test="string-length(normalize-space($serviceType))>0">
						<dc:type>
							<xsl:value-of select="$serviceType"/>
						</dc:type>
					</xsl:when>
					<xsl:otherwise>
						<xsl:for-each select="							
									//ows:ServiceIdentification/ows:ServiceType | 
									//ows11:ServiceIdentification/ows11:ServiceType">
							<dc:type>
								<xsl:value-of select="text()"/>
							</dc:type>
						</xsl:for-each>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:for-each select="							
					            //ows:ServiceProvider/ows:ServiceContact/ows:IndividualName | 
								//ows11:ServiceProvider/ows11:ServiceContact/ows11:IndividualName |
								//wms:Service//wms:ContactInformation/wms:ContactPersonPrimary/wms:ContactPerson">
					<dc:creator>
						<xsl:value-of select="text()"/>
					</dc:creator>
				</xsl:for-each>
				<xsl:if test="string-length(normalize-space($serviceType))>0">
					<dc:subject>
						<xsl:value-of select="$serviceType"/>
					</dc:subject>
				</xsl:if>
				<xsl:for-each select="			
							//ows:ServiceIdentification/ows:Keywords/ows:Keyword |
							//ows11:ServiceIdentification/ows11:Keywords/ows11:Keyword |
							/WMT_MS_Capabilities/Service/KeywordList/Keyword	|
							//wms:Service/wms:KeywordList/wms:Keyword | 
							//wcs:Service/wcs:keywords/wcs:keyword | 
							//wcs11:Service/wcs11:keywords/wcs11:keyword | 
							//wcs111:Service/wcs111:keywords/wcs111:keyword">
					<dc:subject>
						<xsl:value-of select="text()"/>
					</dc:subject>
				</xsl:for-each>
				<dct:references>
					<xsl:value-of select="$sourceUrl"/>
				</dct:references>
				<xsl:for-each select="
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
//wcs111:Capability/wcs111:Request/wcs111:GetCapabilities/wcs111:DCPType/wcs111:HTTP/wcs111:Post/wcs111:OnlineResource/@xlink:href">
					<dct:references>
						<xsl:value-of select="normalize-space(.)"/>
					</dct:references>
				</xsl:for-each>
				<!-- WMS bounding box -->
				<xsl:choose>
					<xsl:when test="//wms:EX_GeographicBoundingBox">
						<xsl:call-template name="WMS_EX_GeographicBoundingBox"/>				
					</xsl:when>
					<xsl:when test="//wms:LatLonBoundingBox |
									  //LatLonBoundingBox |
									  //LatLonBoundingBox | 	
									  //wms:BoundingBox[@CRS='EPSG:4326']
									  ">
						<xsl:call-template name="WMS_BoundingBox"/>
					</xsl:when>			
				
				<!-- WCS / WFS / SPS bounding box -->
					<xsl:when test=" //ows:LowerCorner | //ows11:LowerCorner | //gml:LowerCorner | //gml:pos[1] | //gml:coord[1] | //gml:lowerCorner | //gml:Envelope[@srsName='EPSG:4326'] ">
						<xsl:call-template name="OWS_WGS84BoundingBox"/>					
					</xsl:when>
				</xsl:choose>			
			</rdf:Description>
		</rdf:RDF>
	</xsl:template>
			
	<!-- OWS Bounding Box -->
	<xsl:template name="OWS_WGS84BoundingBox">
		<ows:WGS84BoundingBox xsl:exclude-result-prefixes="wms wps100 swe myorg tml sml sps sos10 wfs wcs wcs11 wcs111 csw csw202 gml">		
		<xsl:choose>	
		<xsl:when test="/wms:WMS_Capabilities/@version = '1.3.0' or /WMS_Capabilities/@version = '1.3.0' or /wms:WMT_MS_Capabilities/@version = '1.3.0' or /WMT_MS_Capabilities/@version = '1.3.0' " >
			<ows:LowerCorner>
				<xsl:call-template name="getLCMiny"/>
				<xsl:value-of select="' '"/>
				<xsl:call-template name="getLCMinx"/>
			</ows:LowerCorner>
			<ows:UpperCorner>
				<xsl:call-template name="getUCMaxy"/>
				<xsl:value-of select="' '"/>
				<xsl:call-template name="getUCMaxx"/>
			</ows:UpperCorner>
		</xsl:when>		
		<xsl:otherwise>
		  	<ows:LowerCorner>
				<xsl:call-template name="getLCMinx"/>
				<xsl:value-of select="' '"/>
				<xsl:call-template name="getLCMiny"/>
			</ows:LowerCorner>
			<ows:UpperCorner>
				<xsl:call-template name="getUCMaxx"/>
				<xsl:value-of select="' '"/>
				<xsl:call-template name="getUCMaxy"/>
			</ows:UpperCorner>
		</xsl:otherwise>
		</xsl:choose>					
		</ows:WGS84BoundingBox>
	</xsl:template>
	
	<xsl:template name="getLCMinx">
		<xsl:for-each select="//ows:LowerCorner | //ows11:LowerCorner | //gml:LowerCorner | //gml:pos[1] | //gml:coord[1] | //gml:lowerCorner">
			<xsl:sort select="number(normalize-space(substring-before(normalize-space(.),' ')))" data-type="number" order="ascending"/>
			<xsl:if test="position() = 1">		
				<xsl:value-of select="number(normalize-space(substring-before(normalize-space(.),' ')))"/>
			</xsl:if>
		</xsl:for-each>
			
		<!--<xsl:param name="bbox"/>
		<xsl:for-each select="nodeset($bbox)">
			<xsl:sort select="substring-before(./ows:LowerCorner,' ')" data-type="number" order="ascending"/>
			<xsl:if test="position() = 1">		
				<xsl:value-of select="substring-before(./ows:LowerCorner,' ')"/>
			</xsl:if>
			<xsl:sort select="substring-after(./ows:LowerCorner,' ') " data-type="number" order="ascending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="' '"/>
				<xsl:value-of select="substring-after(./ows:LowerCorner ,' ') "/>		
			</xsl:if><xsl:value-of select="','"/>	
				<xsl:sort select="substring-before(./ows:UpperCorner ,' ')" data-type="number" order="descending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="substring-before( ./ows:UpperCorner ,' ')"/>
			</xsl:if> 	
			<xsl:sort select="substring-after( ./ows:UpperCorner ,' ') " data-type="number" order="descending"/>
			<xsl:if test="position() = 1"><xsl:value-of select="' '"/>
				<xsl:value-of select="substring-after( ./ows:UpperCorner ,' ') "/>
			</xsl:if>
		</xsl:for-each>-->
	</xsl:template>
	
	<xsl:template name="getLCMiny">
		<xsl:for-each select="//ows:LowerCorner | //ows11:LowerCorner | //gml:LowerCorner | //gml:pos[1] | //gml:coord[1] | //gml:lowerCorner">
			<xsl:sort select="substring-after(.,' ') " data-type="number" order="ascending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="substring-after(.,' ') "/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="getUCMaxx">
		<xsl:for-each select="//ows:UpperCorner | //ows11:UpperCorner | //gml:UpperCorner | //gml:pos[2] | //gml:coord[2] | //gml:upperCorner">
			<xsl:sort select="substring-before(. ,' ')" data-type="number" order="descending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="substring-before( . ,' ')"/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="getUCMaxy">
		<xsl:for-each select="//ows:UpperCorner | //ows11:UpperCorner | //gml:UpperCorner | //gml:pos[2] | //gml:coord[2] | //gml:upperCorner">
			<xsl:sort select="substring-after( . ,' ') " data-type="number" order="descending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="substring-after( . ,' ') "/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	
<!-- WMS Bounding Box -->
	<xsl:template name="WMS_SummaryBoundingBox">
		<xsl:param name="box"/>
		<ows:WGS84BoundingBox xsl:exclude-result-prefixes="wps100 swe myorg tml sml sps sos10 wfs wcs wcs11 wcs111 csw csw202 gml">
		<xsl:choose>	
		<xsl:when test="/wms:WMS_Capabilities/@version = '1.3.0' or /WMS_Capabilities/@version = '1.3.0' or /wms:WMT_MS_Capabilities/@version = '1.3.0' or /WMT_MS_Capabilities/@version = '1.3.0' " >
				<ows:LowerCorner>
				<xsl:value-of select="$box/@miny"/>
				<xsl:value-of select="' '"/>
				<xsl:value-of select="$box/@minx"/>
			</ows:LowerCorner>
			<ows:UpperCorner>
				<xsl:value-of select="$box/@maxy"/>
				<xsl:value-of select="' '"/>
				<xsl:value-of select="$box/@maxx"/>
			</ows:UpperCorner>
		</xsl:when>		
		<xsl:otherwise>
		  	<ows:LowerCorner>
				<xsl:value-of select="$box/@minx"/>
				<xsl:value-of select="' '"/>
				<xsl:value-of select="$box/@miny"/>
			</ows:LowerCorner>
			<ows:UpperCorner>
				<xsl:value-of select="$box/@maxx"/>
				<xsl:value-of select="' '"/>
				<xsl:value-of select="$box/@maxy"/>
			</ows:UpperCorner>
		</xsl:otherwise>
		</xsl:choose>			
		</ows:WGS84BoundingBox>
	</xsl:template>
	<xsl:template name="WMS_BoundingBox">
		<ows:WGS84BoundingBox xsl:exclude-result-prefixes="wps100 swe myorg tml sml sps sos10 wfs wcs wcs11 wcs111 csw csw202 gml">
		<xsl:choose>	
		<xsl:when test="/wms:WMS_Capabilities/@version = '1.3.0' or /WMS_Capabilities/@version = '1.3.0' or /wms:WMT_MS_Capabilities/@version = '1.3.0' or /WMT_MS_Capabilities/@version = '1.3.0' " >
			<ows:LowerCorner>		
				<xsl:call-template name="getMiny"/>
					<xsl:value-of select="' '"/>
				<xsl:call-template name="getMinx"/>
			</ows:LowerCorner>
			<ows:UpperCorner>
				<xsl:call-template name="getMaxy"/>
					<xsl:value-of select="' '"/>
				<xsl:call-template name="getMaxx"/>
			</ows:UpperCorner>
		</xsl:when>		
		<xsl:otherwise>
		  <ows:LowerCorner>
			 <xsl:call-template name="getMinx"/>
				<xsl:value-of select="' '"/>
					<xsl:call-template name="getMiny"/>
			</ows:LowerCorner>
			<ows:UpperCorner>
					<xsl:call-template name="getMaxx"/>
				<xsl:value-of select="' '"/>
				<xsl:call-template name="getMaxy"/>
			</ows:UpperCorner>
		</xsl:otherwise>
		</xsl:choose>								
		</ows:WGS84BoundingBox>
	</xsl:template>
	<xsl:template name="WMS_EX_GeographicBoundingBox">
		<ows:WGS84BoundingBox xsl:exclude-result-prefixes="wps100 swe myorg tml sml sps sos10 wfs wcs wcs11 wcs111 csw csw202 gml">		
		 <ows:LowerCorner>
				<xsl:call-template name="getWestBound"/>
				<xsl:value-of select="' '"/>
				<xsl:call-template name="getSouthBound"/>
			</ows:LowerCorner>
			<ows:UpperCorner>
				<xsl:call-template name="getEastBound"/>
				<xsl:value-of select="' '"/>
				<xsl:call-template name="getNorthBound"/>
			</ows:UpperCorner>			
		</ows:WGS84BoundingBox>
	</xsl:template>
	<xsl:template name="getMinx">

		<xsl:for-each select="//wms:LatLonBoundingBox |
									  //LatLonBoundingBox |
									  //LatLonBoundingBox | 	
									  //wms:BoundingBox[@CRS='EPSG:4326']">
			<xsl:sort select="./@minx" data-type="number" order="ascending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="./@minx"/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="getMiny">
		<xsl:for-each select="//wms:LatLonBoundingBox |
									  //LatLonBoundingBox |
									  //LatLonBoundingBox | 	
									  //wms:BoundingBox[@CRS='EPSG:4326']">
			<xsl:sort select="./@miny" data-type="number" order="ascending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="./@miny"/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="getMaxy">
		<xsl:for-each select="//wms:LatLonBoundingBox |
									  //LatLonBoundingBox |
									  //LatLonBoundingBox | 	
									  //wms:BoundingBox[@CRS='EPSG:4326']">
			<xsl:sort select="./@maxy" data-type="number" order="descending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="./@maxy"/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="getMaxx">
		<xsl:for-each select="//wms:LatLonBoundingBox |
									  //LatLonBoundingBox |
									  //LatLonBoundingBox | 	
									  //wms:BoundingBox[@CRS='EPSG:4326']">
			<xsl:sort select="./@maxx" data-type="number" order="descending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="./@maxx"/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="getWestBound">
		<xsl:for-each select="//wms:westBoundLongitude">
			<xsl:sort select="." data-type="number" order="ascending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="."/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="getSouthBound">
		<xsl:for-each select="//wms:southBoundLatitude">
			<xsl:sort select="." data-type="number" order="ascending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="."/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="getNorthBound">
		<xsl:for-each select="//wms:northBoundLatitude">
			<xsl:sort select="." data-type="number" order="descending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="."/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="getEastBound">
		<xsl:for-each select="//wms:eastBoundLongitude">
			<xsl:sort select="." data-type="number" order="descending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="."/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
