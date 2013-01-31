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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no"/>

	<xsl:template match="/">
	
		<SOAP-ENV:Envelope
		   SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"
		   xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
		   xmlns:SOAP-ENC="http://schemas.xmlsoap.org/soap/encoding/"
		   xmlns:xsi="http://www.w3.org/1999/XMLSchema-instance"
		   xmlns:xsd="http://www.w3.org/1999/XMLSchema">
	
		<SOAP-ENV:Body>
	
		<csw:GetRecords xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" 
		xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" 
		xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" 
		xmlns:ows="http://www.opengis.net/ows" 
		xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
		xmlns:dc="http://purl.org/dc/elements/1.1/" 
		xmlns:dct="http://purl.org/dc/terms/" 
		xmlns:gml="http://www.opengis.net/gml" 
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" 
		resultType="results" outputFormat="application/xml" 
		outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="10">
		
			<csw:Query typeNames="gmd:MD_Metadata">
				<csw:ElementSetName>brief</csw:ElementSetName>
					<csw:Constraint version="1.1.0">
					<ogc:Filter>
						<ogc:And>

							<!--<ogc:PropertyIsEqualTo>
									<ogc:PropertyName>iso:type</ogc:PropertyName>
									<ogc:Literal>service</ogc:Literal>
							</ogc:PropertyIsEqualTo>-->
							<ogc:PropertyIsEqualTo>
									<ogc:PropertyName>iso:ServiceType</ogc:PropertyName>
									<ogc:Literal>WMS</ogc:Literal>
							</ogc:PropertyIsEqualTo>

							<!-- Envelope search, e.g. ogc:BBOX -->
							<!--<xsl:apply-templates select="/GetRecords/Envelope"/>-->
							<xsl:apply-templates select="/GetRecords/KeyWord"/>
						</ogc:And>
					</ogc:Filter>
					</csw:Constraint>
			</csw:Query>
		</csw:GetRecords>
		
		</SOAP-ENV:Body>
		
		</SOAP-ENV:Envelope>
	</xsl:template>
	
	<!-- key word search -->
	<xsl:template match="/GetRecords/KeyWord" xmlns:ogc="http://www.opengis.net/ogc">
		<xsl:if test="normalize-space(.)!=''">
			<!--<ogc:Or>
				<ogc:PropertyIsEqualTo>
						<ogc:PropertyName>iso:subject</ogc:PropertyName>
						<ogc:Literal>Topographische Karte</ogc:Literal>
				</ogc:PropertyIsEqualTo>-->
				<ogc:PropertyIsLike wildCard="*" escapeChar="\" singleChar="?">
					<ogc:PropertyName>apiso:AnyText</ogc:PropertyName>
					<ogc:Literal><xsl:value-of select="normalize-space(.)"/></ogc:Literal>
				</ogc:PropertyIsLike>
<!--			</ogc:Or>-->
		</xsl:if>
	</xsl:template>

	<!-- envelope search. It is based on Filter Specification OGC 04-095 -->
	<xsl:template match="/GetRecords/Envelope" xmlns:ogc="http://www.opengis.net/ogc">
		<!-- generate BBOX query if minx, miny, maxx, maxy are provided -->
		<xsl:if test="./MinX and ./MinY and ./MaxX and ./MaxY">
			<ogc:BBOX xmlns:gml="http://www.opengis.net/gml">
				<ogc:PropertyName>iso:BoundingBox</ogc:PropertyName>
				<gml:Envelope>
					<gml:lowerCorner><xsl:value-of select="MinX"/> <xsl:value-of select="MinY"/></gml:lowerCorner>
					<gml:upperCorner><xsl:value-of select="MaxX"/> <xsl:value-of select="MaxY"/></gml:upperCorner>					
				</gml:Envelope>
			</ogc:BBOX>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
