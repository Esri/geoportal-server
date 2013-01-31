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
	<xsl:output method="xml" indent="no" encoding="UTF-8" omit-xml-declaration="no"/>
	<xsl:template match="/">
		<csw:GetRecords xmlns:ogc="http://www.opengis.net/ogc" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" >
			<xsl:attribute name="version">2.0.2</xsl:attribute>
			<xsl:attribute name="service">CSW</xsl:attribute>
			<xsl:attribute name="resultType">RESULTS</xsl:attribute>
			<xsl:attribute name="startPosition"><xsl:value-of select="/GetRecords/StartPosition"/></xsl:attribute>
			<xsl:attribute name="maxRecords"><xsl:value-of select="/GetRecords/MaxRecords"/></xsl:attribute>
			<xsl:attribute name="outputFormat">application/xml</xsl:attribute>
			<xsl:attribute name="outputSchema">http://www.isotc211.org/2005/gmd</xsl:attribute>
			<csw:Query typeNames="gmd:MD_Metadata">
				<csw:ElementName>/gmd:MD_Metadata</csw:ElementName>
				<csw:Constraint version="1.1.0">
					<ogc:Filter xmlns="http://www.opengis.net/ogc">
						<ogc:And>
							<!-- Key Word search -->
							<xsl:apply-templates select="/GetRecords/KeyWord"/>
							<!-- Envelope search, e.g. ogc:BBOX -->
							<xsl:apply-templates select="/GetRecords/Envelope"/>
						</ogc:And>
					</ogc:Filter>
				</csw:Constraint>
			</csw:Query>
		</csw:GetRecords>
	</xsl:template>
	
	<!-- key word search -->
	<xsl:template match="/GetRecords/KeyWord" xmlns:ogc="http://www.opengis.net/ogc">
		<xsl:if test="normalize-space(.)!=''">
			<ogc:PropertyIsLike wildCard="*" escapeChar="!" singleChar="#">
				<ogc:PropertyName>/MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/title/gco:CharacterString</ogc:PropertyName>
				<ogc:Literal>*<xsl:value-of select="normalize-space(.)"/>*</ogc:Literal>
			</ogc:PropertyIsLike>
		</xsl:if>
	</xsl:template>

	<!-- envelope search. It is based on Filter Specification OGC 04-095 -->
	<xsl:template match="/GetRecords/Envelope" xmlns:ogc="http://www.opengis.net/ogc">
		<!-- generate BBOX query if minx, miny, maxx, maxy are provided -->
		<xsl:if test="./MinX and ./MinY and ./MaxX and ./MaxY">
			<ogc:BBOX xmlns:gml="http://www.opengis.net/gml">
				<ogc:PropertyName>Geometry</ogc:PropertyName>
				<gml:Envelope srsName="http://www.opengis.net/gml/srs/epsg.xml#63266405">
					<gml:lowerCorner><xsl:value-of select="MinX"/>,<xsl:value-of select="MinY"/></gml:lowerCorner>
					<gml:upperCorner><xsl:value-of select="MaxX"/>,<xsl:value-of select="MaxY"/></gml:upperCorner>					
				</gml:Envelope>
			</ogc:BBOX>
		</xsl:if>
	</xsl:template>
	<!--
    <xsl:attribute-set name="GetRecordsAttributes">
		<xsl:attribute name="version">2.0.0</xsl:attribute>
		<xsl:attribute name="service">CSW</xsl:attribute>
		<xsl:attribute name="resultType">RESULTS</xsl:attribute>
		<xsl:attribute name="startPosition"><xsl:value-of select="/GetRecords/StartPosition"/></xsl:attribute>
		<xsl:attribute name="maxRecords"><xsl:value-of select="/GetRecords/MaxRecords"/></xsl:attribute>
		<xsl:attribute name="outputSchema">csw:Record</xsl:attribute>
	</xsl:attribute-set>
    -->
</xsl:stylesheet>
