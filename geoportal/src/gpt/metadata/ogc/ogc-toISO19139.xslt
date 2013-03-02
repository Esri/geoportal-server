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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:ows="http://www.opengis.net/ows" xmlns:ows11="http://www.opengis.net/ows/1.1" xmlns:gml="http://www.opengis.net/gml" xmlns:csw="http://www.opengis.net/cat/csw" xmlns:csw202="http://www.opengis.net/cat/csw/2.0.2" xmlns:wcs="http://www.opengis.net/wcs" xmlns:wcs11="http://www.opengis.net/wcs/1.1" xmlns:wcs111="http://www.opengis.net/wcs/1.1.1" xmlns:wfs="http://www.opengis.net/wfs" xmlns:wms="http://www.opengis.net/wms" xmlns:wps100="http://www.opengis.net/wps/1.0.0" xmlns:sos10="http://www.opengis.net/sos/1.0" xmlns:sps="http://www.opengis.net/sps" xmlns:tml="http://www.opengis.net/tml" xmlns:sml="http://www.opengis.net/sensorML/1.0.1" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:myorg="http://www.myorg.org/features" xmlns:swe="http://www.opengis.net/swe/1.0.1" xmlns:exslt="http://exslt.org/common">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:param name="sourceUrl"/>
	<xsl:param name="serviceType"/>
	<xsl:param name="currentDate"/>
		<xsl:template match="/">
		<xsl:call-template name="main"/>
	</xsl:template>

<xsl:template name="main">
<!-- Core gmd based instance document -->
<MD_Metadata xmlns="http://www.isotc211.org/2005/gmd" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  xsl:exclude-result-prefixes="ows ows11 wms wps100 swe myorg tml sml sps sos10 wfs wcs wcs11 wcs111 csw csw202 gml">
	<fileIdentifier>
		<gco:CharacterString><xsl:value-of select="$sourceUrl"/></gco:CharacterString>		
	</fileIdentifier>
	<xsl:for-each select="//ows:Language">	
		<language>
			<gco:CharacterString>
					<xsl:value-of select="text()"/>
			</gco:CharacterString>			
		</language>
	</xsl:for-each>
		<xsl:for-each select="							
					            //ows:ServiceProvider/ows:ServiceContact/ows:IndividualName | 
								//ows11:ServiceProvider/ows11:ServiceContact/ows11:IndividualName |
								//wms:Service//wms:ContactInformation/wms:ContactPersonPrimary/wms:ContactPerson">
	<contact>
			<CI_ResponsibleParty>
			<organisationName>
				<gco:CharacterString>
						<xsl:value-of select="text()"/>
				</gco:CharacterString>
			</organisationName>
			<role>
				<CI_RoleCode codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_RoleCode" codeListValue="custodian">custodian</CI_RoleCode>
			</role>
		</CI_ResponsibleParty>
	</contact>
				</xsl:for-each>
	
	<dateStamp>
		<gco:Date><xsl:value-of select="$currentDate"/></gco:Date>
	</dateStamp>
<metadataStandardName>
		<gco:CharacterString>ISO 19139/19119 Metadata for Web Services</gco:CharacterString>
	</metadataStandardName>
	<metadataStandardVersion>
		<gco:CharacterString>2005</gco:CharacterString>
	</metadataStandardVersion>
	<identificationInfo>
	<srv:SV_ServiceIdentification>
		<citation>
			<CI_Citation>
				<title>
				<xsl:if test="							
								//ows:ServiceIdentification/ows:Title | 		
								//ows11:ServiceIdentification/ows11:Title | 
								/WMT_MS_Capabilities/Service/Title |
								//wms:Service/wms:Title  | 
								/wms:WMT_MS_Capabilities/wms:Service/wms:Title | 							
								//wcs:Service/wcs:name | 
								//wcs11:Service/wcs11:name | 
								//wcs111:Service/wcs111:name |
								//wcs:Service/wcs:name | 
								//wcs11:Service/wcs11:name | 
								//wcs111:Service/wcs111:name				
						">
					<gco:CharacterString>
						<xsl:value-of select="//ows:ServiceIdentification/ows:Title | 		
								//ows11:ServiceIdentification/ows11:Title | 
								/WMT_MS_Capabilities/Service/Title |
								//wms:Service/wms:Title  | 
								/wms:WMT_MS_Capabilities/wms:Service/wms:Title | 							
								//wcs:Service/wcs:name | 
								//wcs11:Service/wcs11:name | 
								//wcs111:Service/wcs111:name |
								//wcs:Service/wcs:name | 
								//wcs11:Service/wcs11:name | 
								//wcs111:Service/wcs111:name"/>
						</gco:CharacterString>
				</xsl:if>
					
				</title>
				<date>
				<xsl:choose>
					<xsl:when test="//gml:relatedTime">
					<CI_Date>
						<date>							
							<gco:Date>
								<xsl:value-of select="//gml:relatedTime"/>
							</gco:Date>								
						</date>
						<dateType>
							<CI_DateTypeCode codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_DateTypeCode" codeListValue="publication"/>
						</dateType>
					</CI_Date>
					</xsl:when>
					<xsl:otherwise>
							<CI_Date>
							<date>							
								<gco:Date>
									<xsl:value-of select="$currentDate"/>
								</gco:Date>								
							</date>
							<dateType>
								<CI_DateTypeCode codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_DateTypeCode" codeListValue="publication"/>
							</dateType>
					</CI_Date>
					</xsl:otherwise>
					</xsl:choose>
				</date>
			</CI_Citation>
		</citation>
		<abstract>
			<xsl:if test="
								//Service/Abstract | 								 
								//ows:ServiceIdentification/ows:Abstract | 
								//ows11:ServiceIdentification/ows11:Abstract |
								//Service/Abstract | 
								//wms:Service/wms:Abstract | 
								//wcs:Service/wcs:description | 													
								//wcs11:Service/wcs11:description | 
								//wcs111:Service/wcs111:description 
						">
					<gco:CharacterString>
						<xsl:value-of select="//Service/Abstract | 								 
								//ows:ServiceIdentification/ows:Abstract | 
								//ows11:ServiceIdentification/ows11:Abstract |
								//Service/Abstract | 
								//wms:Service/wms:Abstract | 
								//wcs:Service/wcs:description | 													
								//wcs11:Service/wcs11:description | 
								//wcs111:Service/wcs111:description"/>
					</gco:CharacterString>
				</xsl:if>
		</abstract>
		<pointOfContact>
			<xsl:for-each select="
								//ows:ServiceProvider/ows:ProviderName | 
								//ows11:ServiceProvider/ows11:ProviderName | 	
								/WMT_MS_Capabilities/Service/ContactInformation/ContactPersonPrimary/ContactPerson |					 	       				                      //wcs:Service/wcs:responsibleParty/wcs:individualName |
								//wcs11:Service/wcs11:responsibleParty/wcs11:individualName | 
								//wcs111:Service/wcs111:responsibleParty/wcs111:individualName | 
								/wps100:Capabilities/ows:ServiceProvider/ows:ServiceContact/ows:IndividualName |
								/wps100:Capabilities/ows11:ServiceProvider/ows11:ServiceContact/ows11:IndividualName
						">
				<CI_ResponsibleParty>
				<organisationName>
					<gco:CharacterString/>
						<xsl:value-of select="text()"/>
					</organisationName>
			<!--	<positionName>
					<gco:CharacterString/>
				</positionName>
				<contactInfo>
					<CI_Contact>
						<phone>
							<CI_Telephone>
								<voice>
									<gco:CharacterString/>
								</voice>
								<facsimile>
									<gco:CharacterString/>
								</facsimile>
							</CI_Telephone>
						</phone>
						<address>
							<CI_Address>
								<deliveryPoint>
									<gco:CharacterString/>
								</deliveryPoint>
								<city>
									<gco:CharacterString/>
								</city>
								<administrativeArea>
									<gco:CharacterString/>
								</administrativeArea>
								<postalCode>
									<gco:CharacterString/>
								</postalCode>
								<country>
									<gco:CharacterString/>
								</country>
								<electronicMailAddress>
									<gco:CharacterString/>
								</electronicMailAddress>
							</CI_Address>
						</address>
						<onlineResource>
							<CI_OnlineResource>
								<linkage>
									<URL/>
								</linkage>
							</CI_OnlineResource>
						</onlineResource>
					</CI_Contact>
				</contactInfo>
				<role>
					<CI_RoleCode codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_RoleCode" codeListValue="pointOfContact"/>
				</role>-->
			</CI_ResponsibleParty>
				</xsl:for-each>	
		</pointOfContact>
	
		<srv:serviceType>
		<xsl:choose>
					<xsl:when test="string-length(normalize-space($serviceType))>0">
						<gco:LocalName>
							<xsl:value-of select="$serviceType"/>
						</gco:LocalName>
					</xsl:when>
					<xsl:otherwise>
						<xsl:for-each select="							
									//ows:ServiceIdentification/ows:ServiceType | 
									//ows11:ServiceIdentification/ows11:ServiceType">
							<gco:LocalName>
								<xsl:value-of select="text()"/>
							</gco:LocalName>
						</xsl:for-each>
					</xsl:otherwise>
				</xsl:choose>			
		</srv:serviceType>
		<srv:extent>
				<EX_Extent>
					<geographicElement>
					
					
				<xsl:choose>
					<xsl:when test="//wms:LatLonBoundingBox |
									  //LatLonBoundingBox |
									  //LatLonBoundingBox | 	
									  //wms:BoundingBox[@CRS='EPSG:4326']
									  ">
						<xsl:call-template name="WMS_BoundingBox"/>
					</xsl:when>			
					<xsl:when test="//wms:EX_GeographicBoundingBox">
						<xsl:call-template name="WMS_EX_GeographicBoundingBox"/>				
					</xsl:when>
				
				
					<xsl:when test=" //ows:LowerCorner | //ows11:LowerCorner | //gml:LowerCorner | //gml:pos[1] | //gml:coord[1] | //gml:lowerCorner | //gml:Envelope[@srsName='EPSG:4326'] ">
						<xsl:call-template name="OWS_WGS84BoundingBox"/>					
					</xsl:when>
				</xsl:choose>		
					</geographicElement>
				</EX_Extent>
		</srv:extent>
		<srv:couplingType>

			<srv:SV_CouplingType codeList="#SV_CouplingType" codeListValue="loose">loose</srv:SV_CouplingType>
		</srv:couplingType>
		<srv:containsOperations>
			<xsl:choose>			
			<xsl:when test="
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
				<srv:SV_OperationMetadata>
				<srv:operationName>
					<gco:CharacterString>GetCapabilities</gco:CharacterString>
				</srv:operationName>
				<srv:DCP>
					<srv:DCPList codeList="#DCPList" codeListValue="WebServices">WebServices</srv:DCPList>
				</srv:DCP>
				<srv:connectPoint>
					<CI_OnlineResource>
						<linkage>
							<URL>
						<xsl:value-of select="//Service/OnlineResource/@xlink:href |
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
//wcs111:Capability/wcs111:Request/wcs111:GetCapabilities/wcs111:DCPType/wcs111:HTTP/wcs111:Post/wcs111:OnlineResource/@xlink:href"/></URL>
						</linkage>
					</CI_OnlineResource>
				</srv:connectPoint>
			</srv:SV_OperationMetadata>
				</xsl:when>		
				<xsl:otherwise>
					<srv:SV_OperationMetadata>
				<srv:operationName>
					<gco:CharacterString>GetCapabilities</gco:CharacterString>
				</srv:operationName>
				<srv:DCP>
					<srv:DCPList codeList="#DCPList" codeListValue="WebServices">WebServices</srv:DCPList>
				</srv:DCP>
				<srv:connectPoint>
					<CI_OnlineResource>
						<linkage>
							<URL><xsl:value-of select="$sourceUrl"/></URL>
						</linkage>
					</CI_OnlineResource>
				</srv:connectPoint>
			</srv:SV_OperationMetadata>
			</xsl:otherwise>
			</xsl:choose>					
		</srv:containsOperations>		
	</srv:SV_ServiceIdentification>	
	</identificationInfo>
	
<xsl:for-each select="								//ows:OperationsMetadata//ows:Operation[@name='DescribeRecord']//ows:Parameter[@name='outputFormat']//ows:Value |					//ows:OperationsMetadata//ows:Operation[@name='DescribeSensor']//ows:Parameter[@name='outputFormat']/ows:AllowedValues/ows:Value | //wms:Capability//wms:Request//wms:GetCapabilities//wms:Format | /wfs:WFS_Capabilities/ows:OperationsMetadata/ows:Operation[@name='GetCapabilities']/ows:Parameter[@name='AcceptFormats']/ows:Value">
	<distributionInfo>
		<MD_Distribution>			
			<distributionFormat>			
				<MD_Format>
					<name>
						<xsl:value-of select="text()"/>
					</name>
				</MD_Format>			
			</distributionFormat>						
		</MD_Distribution>
	</distributionInfo>
</xsl:for-each>
		
</MD_Metadata>	
</xsl:template>

	<!-- OWS Bounding Box -->
	<xsl:template name="OWS_WGS84BoundingBox">
		<EX_GeographicBoundingBox xsl:exclude-result-prefixes="wms wps100 swe myorg tml sml sps sos10 wfs wcs wcs11 wcs111 csw csw202 gml">		
			    <westBoundLongitude>
					<gco:Decimal>
						<xsl:call-template name="getLCMinx"/>
					</gco:Decimal>
				</westBoundLongitude>							
				<southBoundLatitude>
					<gco:Decimal>
						<xsl:call-template name="getLCMiny"/>
					</gco:Decimal>
				</southBoundLatitude>
				<eastBoundLongitude>
						<gco:Decimal>
							<xsl:call-template name="getUCMaxx"/>
						</gco:Decimal>
				</eastBoundLongitude>							
					<northBoundLatitude>
						<gco:Decimal>
							<xsl:call-template name="getUCMaxy"/>
					</gco:Decimal>
				</northBoundLatitude>								
		</EX_GeographicBoundingBox>
	</xsl:template>
	
	<xsl:template name="getLCMinx">
		<xsl:for-each select="//ows:LowerCorner | //ows11:LowerCorner | //gml:LowerCorner | //gml:pos[1] | //gml:coord[1] | //gml:lowerCorner">
			<xsl:sort select="number(normalize-space(substring-before(normalize-space(.),' ')))" data-type="number" order="ascending"/>
			<xsl:if test="position() = 1">		
				<xsl:value-of select="number(normalize-space(substring-before(normalize-space(.),' ')))"/>
			</xsl:if>
		</xsl:for-each>
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
			<EX_GeographicBoundingBox xsl:exclude-result-prefixes="wps100 swe myorg tml sml sps sos10 wfs wcs wcs11 wcs111 csw csw202 gml">		
			    <westBoundLongitude>
					<gco:Decimal>
							<xsl:value-of select="$box/@minx"/>
					</gco:Decimal>
				</westBoundLongitude>							
				<southBoundLatitude>
					<gco:Decimal>
						<xsl:value-of select="$box/@miny"/>
					</gco:Decimal>
				</southBoundLatitude>
				<eastBoundLongitude>
						<gco:Decimal>
							<xsl:value-of select="$box/@maxx"/>
						</gco:Decimal>
				</eastBoundLongitude>							
					<northBoundLatitude>
						<gco:Decimal>
							<xsl:value-of select="$box/@maxy"/>
					</gco:Decimal>
				</northBoundLatitude>								
		</EX_GeographicBoundingBox>
	</xsl:template>
	<xsl:template name="WMS_BoundingBox">
		<EX_GeographicBoundingBox xsl:exclude-result-prefixes="wps100 swe myorg tml sml sps sos10 wfs wcs wcs11 wcs111 csw csw202 gml">
		   <westBoundLongitude>
					<gco:Decimal>
						<xsl:call-template name="getMinx"/>
					</gco:Decimal>
				</westBoundLongitude>							
				<southBoundLatitude>
					<gco:Decimal>
						<xsl:call-template name="getMiny"/>
					</gco:Decimal>
				</southBoundLatitude>
				<eastBoundLongitude>
						<gco:Decimal>
							<xsl:call-template name="getMaxx"/>
						</gco:Decimal>
				</eastBoundLongitude>							
					<northBoundLatitude>
						<gco:Decimal>
							<xsl:call-template name="getMaxy"/>
					</gco:Decimal>
				</northBoundLatitude>			
		</EX_GeographicBoundingBox>
	</xsl:template>
	<xsl:template name="WMS_EX_GeographicBoundingBox">
		<EX_GeographicBoundingBox xsl:exclude-result-prefixes="wps100 swe myorg tml sml sps sos10 wfs wcs wcs11 wcs111 csw csw202 gml">		
			   <westBoundLongitude>
					<gco:Decimal>
						<xsl:call-template name="getWestBound"/>
					</gco:Decimal>
				</westBoundLongitude>							
				<southBoundLatitude>
					<gco:Decimal>
						<xsl:call-template name="getSouthBound"/>
					</gco:Decimal>
				</southBoundLatitude>
				<eastBoundLongitude>
						<gco:Decimal>
							<xsl:call-template name="getEastBound"/>
						</gco:Decimal>
				</eastBoundLongitude>							
					<northBoundLatitude>
						<gco:Decimal>
							<xsl:call-template name="getNorthBound"/>
					</gco:Decimal>
				</northBoundLatitude>			
		</EX_GeographicBoundingBox>
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
