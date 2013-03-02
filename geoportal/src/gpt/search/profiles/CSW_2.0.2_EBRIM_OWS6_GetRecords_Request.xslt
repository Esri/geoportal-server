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
		<csw:GetRecords 
			outputFormat="application/xml"
			outputSchema="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"
			version="2.0.2"
			service="CSW-EBRIM"
			resultType="results"
			xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"
			xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" 
			xmlns:ogc="http://www.opengis.net/ogc" >
			<xsl:attribute name="startPosition"><xsl:value-of select="/GetRecords/StartPosition"/></xsl:attribute>
			<xsl:attribute name="maxRecords"><xsl:value-of select="/GetRecords/MaxRecords"/></xsl:attribute>
			<csw:Query typeNames="Service">
				<csw:ElementSetName typeNames="Service">full</csw:ElementSetName>
				<csw:Constraint version="1.1.0">
				  <ogc:Filter>
						<xsl:choose>
							<xsl:when test="count(/GetRecords/KeyWord) + count(/GetRecords/FromDate) > 1">
								<ogc:And>
									<!-- Key Word search -->
									<xsl:apply-templates select="/GetRecords/KeyWord"/>
									<!-- LiveDataOrMaps search -->
									<!-- xsl:apply-templates select="/GetRecords/LiveDataMap"/ -->
									<!-- Envelope search, e.g. ogc:BBOX -->
									<!-- xsl:apply-templates select="/GetRecords/Envelope"/ -->
								</ogc:And>
							</xsl:when>
							<xsl:otherwise>
								<!-- only one criterion is given, do not include enclosing ogc:And elements -->
								
								<!-- Key Word search -->
								<xsl:apply-templates select="/GetRecords/KeyWord"/>
								<!-- LiveDataOrMaps search -->
								<!-- xsl:apply-templates select="/GetRecords/LiveDataMap"/ -->
								<!-- Envelope search, e.g. ogc:BBOX -->
								<!-- xsl:apply-templates select="/GetRecords/Envelope"/ -->
							</xsl:otherwise>
						</xsl:choose>
				  </ogc:Filter>
				</csw:Constraint>
			</csw:Query>
		</csw:GetRecords>
	</xsl:template>
	<!-- key word search -->
	<xsl:template match="/GetRecords/KeyWord" xmlns:ogc="http://www.opengis.net/ogc">
		<xsl:if test="normalize-space(.)!=''">
			<ogc:PropertyIsLike singleChar="?" wildCard="*" escapeChar="~">
				<ogc:PropertyName>Service/Description/LocalizedString/@value</ogc:PropertyName>
				<ogc:Literal>
					<xsl:value-of select="."/>
				</ogc:Literal>
			</ogc:PropertyIsLike>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
