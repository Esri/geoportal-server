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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:ows="http://www.opengis.net/ows">
	<xsl:param name="sourceUrl"/>
	<xsl:param name="serviceType"/>
	<xsl:output omit-xml-declaration="no" method="xml" indent="yes" encoding="UTF-8"/>
	<xsl:template match="/">
		<xsl:call-template name="main"/>
	</xsl:template>
	<xsl:template name="main">
		<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/">
      <rdf:Description>
        <xsl:attribute namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#" name="about">
          <xsl:value-of select="$sourceUrl"/>
        </xsl:attribute>
				<xsl:choose>
					<xsl:when test="/ags-rest/documentInfo/Title | /ags-rest/name">
						<dc:title>
							<xsl:value-of select="/ags-rest/documentInfo/Title | /ags-rest/name"/>
						</dc:title>
					</xsl:when>
					<xsl:otherwise>
						<dc:title xml:lang="en">
							<xsl:call-template name="makeTitle"/>
						</dc:title>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:if test="/ags-rest/description | /ags-rest/serviceDescription">
					<dc:description>
						<xsl:value-of select="/ags-rest/description | /ags-rest/serviceDescription"/>
					</dc:description>
				</xsl:if>
				<!--	<xsl:for-each select="">
					<dc:date>
						<xsl:value-of select=""/>
					</dc:date>
				</xsl:for-each>
				<xsl:for-each select="">
					<dc:format>
						<xsl:value-of select=""/>
					</dc:format>
				</xsl:for-each>
				<xsl:for-each select="">
					<dc:language>
						<xsl:value-of select=""/>
					</dc:language>
				</xsl:for-each>
				<xsl:for-each select="">
					<dc:contributor>
						<xsl:value-of select=""/>
					</dc:contributor>
				</xsl:for-each>-->
				<dc:identifier>
					<xsl:value-of select="$sourceUrl"/>
				</dc:identifier>
				<xsl:choose>
					<xsl:when test="string-length(normalize-space($serviceType))>0">
						<dc:type>
							<xsl:value-of select="$serviceType"/>
						</dc:type>
					</xsl:when>
					<xsl:when test="string-length(normalize-space($serviceType))=0">
						<dc:type>
							<xsl:call-template name="guessServiceType"/>
						</dc:type>
					</xsl:when>
					<xsl:otherwise>
						<xsl:for-each select="/ags-rest/serviceDescription">
							<dc:type>
								<xsl:value-of select="text()"/>
							</dc:type>
						</xsl:for-each>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:for-each select="/ags-rest/documentInfo/Author">
					<dc:creator>
						<xsl:value-of select="text()"/>
					</dc:creator>
				</xsl:for-each>
				<xsl:if test="string-length(normalize-space($serviceType))>0">
					<dc:subject>
						<xsl:value-of select="$serviceType"/>
					</dc:subject>
				</xsl:if>
				<xsl:for-each select="/ags-rest/documentInfo/Keywords">
					<dc:subject>
						<xsl:value-of select="text()"/>
					</dc:subject>
				</xsl:for-each>
				<xsl:for-each select="/ags-rest/documentInfo/Subject">
					<dc:subject>
						<xsl:value-of select="text()"/>
					</dc:subject>
				</xsl:for-each>
				<dct:references>
					<xsl:variable name="endsWithResult1">
						<xsl:call-template name="ends-with">
							<xsl:with-param name="value" select="$sourceUrl"/>
							<xsl:with-param name="substr" select="'.xml'"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="endsWithResult2">
						<xsl:call-template name="ends-with">
							<xsl:with-param name="value" select="$sourceUrl"/>
							<xsl:with-param name="substr" select="'.XML'"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:if test="normalize-space($endsWithResult1) = 'false' and normalize-space($endsWithResult2) = 'false' ">
						<xsl:value-of select="$sourceUrl"/>
					</xsl:if>
				</dct:references>
				<!--	<xsl:for-each select="">
					<dct:references>
						<xsl:value-of select=""/>
					</dct:references>
				</xsl:for-each>	-->
				<xsl:choose>
					<xsl:when test="normalize-space(/ags-rest/fullExtent/spatialReference/wkid) = 4326">
						<xsl:call-template name="BoundingBox"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="WorldBoundingBox"/>
					</xsl:otherwise>
				</xsl:choose>
			</rdf:Description>
		</rdf:RDF>
	</xsl:template>
	<xsl:template name="makeTitle">
			<xsl:choose>
			<xsl:when test="contains($sourceUrl, 'GeoDataServer')">
					<xsl:value-of select="substring-before(substring-after(normalize-space($sourceUrl),'/rest/services/'),'/GeoDataServer')"/>
			</xsl:when>
			<xsl:when test="contains($sourceUrl, 'MapServer')">
				<xsl:value-of select="substring-before(substring-after(normalize-space($sourceUrl),'/rest/services/'),'/MapServer')"/>
			</xsl:when>
			<xsl:when test="contains($sourceUrl, 'ImageServer')">
				<xsl:value-of select="substring-before(substring-after(normalize-space($sourceUrl),'/rest/services/'),'/ImageServer')"/>
			</xsl:when>
			<xsl:when test="contains($sourceUrl, 'GPServer')">
					<xsl:value-of select="substring-before(substring-after(normalize-space($sourceUrl),'/rest/services/'),'/GPServer')"/>
			</xsl:when>
			<xsl:when test="contains($sourceUrl, 'GlobeServer')">
				<xsl:value-of select="substring-before(substring-after(normalize-space($sourceUrl),'/rest/services/'),'/GlobeServer')"/>
			</xsl:when>
			<xsl:when test="contains($sourceUrl, 'GeocodeServer')">
				<xsl:value-of select="substring-before(substring-after(normalize-space($sourceUrl),'/rest/services/'),'/GeocodeServer')"/>
			</xsl:when>
			<xsl:when test="contains($sourceUrl, 'GeometryServer')">
				<xsl:value-of select="substring-before(substring-after(normalize-space($sourceUrl),'/rest/services/'),'/GeometryServer')"/>
			</xsl:when>
			<xsl:when test="contains($sourceUrl, 'NetworkServer')">
				<xsl:value-of select="substring-before(substring-after(normalize-space($sourceUrl),'/rest/services/'),'/NetworkServer')"/>
			</xsl:when>
			</xsl:choose>	
	</xsl:template>
	<xsl:template name="guessServiceType">
		<xsl:choose>
			<xsl:when test="contains($sourceUrl, 'GeoDataServer')">
				<xsl:variable name="endsWithResult1">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$sourceUrl"/>
						<xsl:with-param name="substr" select="'GeoDataServer?f=json'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="endsWithResult2">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$sourceUrl"/>
						<xsl:with-param name="substr" select="'/GeoDataServer'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="normalize-space($endsWithResult1) = 'true' or normalize-space($endsWithResult2) = 'true' ">
					<xsl:value-of select="'GeoDataServer'"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="contains($sourceUrl, 'MapServer')">
				<xsl:variable name="endsWithResult1">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$sourceUrl"/>
						<xsl:with-param name="substr" select="'MapServer?f=json'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="endsWithResult2">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$sourceUrl"/>
						<xsl:with-param name="substr" select="'/MapServer'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="normalize-space($endsWithResult1) = 'true' or normalize-space($endsWithResult2) = 'true' ">
					<xsl:value-of select="'MapServer'"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="contains($sourceUrl, 'ImageServer')">
				<xsl:variable name="endsWithResult1">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$sourceUrl"/>
						<xsl:with-param name="substr" select="'ImageServer?f=json'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="endsWithResult2">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$sourceUrl"/>
						<xsl:with-param name="substr" select="'/ImageServer'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="normalize-space($endsWithResult1) = 'true' or normalize-space($endsWithResult2) = 'true' ">
					<xsl:value-of select="'ImageServer'"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="contains($sourceUrl, 'GPServer')">
				<xsl:variable name="endsWithResult1">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$sourceUrl"/>
						<xsl:with-param name="substr" select="'GPServer?f=json'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="endsWithResult2">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$sourceUrl"/>
						<xsl:with-param name="substr" select="'/GPServer'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="normalize-space($endsWithResult1) = 'true' or normalize-space($endsWithResult2) = 'true' ">
					<xsl:value-of select="'GPServer'"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="contains($sourceUrl, 'GlobeServer')">
				<xsl:variable name="endsWithResult1">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$sourceUrl"/>
						<xsl:with-param name="substr" select="'GlobeServer?f=json'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="endsWithResult2">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$sourceUrl"/>
						<xsl:with-param name="substr" select="'/GlobeServer'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="normalize-space($endsWithResult1) = 'true' or normalize-space($endsWithResult2) = 'true' ">
					<xsl:value-of select="'GlobeServer'"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="contains($sourceUrl, 'GeocodeServer')">
				<xsl:variable name="endsWithResult1">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$sourceUrl"/>
						<xsl:with-param name="substr" select="'GeocodeServer?f=json'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="endsWithResult2">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$sourceUrl"/>
						<xsl:with-param name="substr" select="'/GeocodeServer'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="normalize-space($endsWithResult1) = 'true' or normalize-space($endsWithResult2) = 'true' ">
					<xsl:value-of select="'GeocodeServer'"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="contains($sourceUrl, 'GeometryServer')">
				<xsl:variable name="endsWithResult1">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$sourceUrl"/>
						<xsl:with-param name="substr" select="'GeometryServer?f=json'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="endsWithResult2">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$sourceUrl"/>
						<xsl:with-param name="substr" select="'/GeometryServer'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="normalize-space($endsWithResult1) = 'true' or normalize-space($endsWithResult2) = 'true' ">
					<xsl:value-of select="'GeometryServer'"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="contains($sourceUrl, 'NetworkServer')">
				<xsl:variable name="endsWithResult1">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$sourceUrl"/>
						<xsl:with-param name="substr" select="'NetworkServer?f=json'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="endsWithResult2">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$sourceUrl"/>
						<xsl:with-param name="substr" select="'/NetworkServer'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="normalize-space($endsWithResult1) = 'true' or normalize-space($endsWithResult2) = 'true' ">
					<xsl:value-of select="'NetworkServer'"/>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
			<xsl:value-of select="'MapServer'"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="ends-with">
		<xsl:param name="value"/>
		<xsl:param name="substr"/>
		<xsl:choose>
			<xsl:when test="substring(normalize-space($value), (string-length(normalize-space($value)) - string-length($substr)) + 1) = $substr">
				<xsl:value-of select="'true'"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'false'"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- Bounding Box -->
	<xsl:template name="BoundingBox">
		<ows:WGS84BoundingBox>
			<ows:LowerCorner>
				<xsl:value-of select="/ags-rest/fullExtent/xmin"/>
				<xsl:value-of select="' '"/>
				<xsl:value-of select="/ags-rest/fullExtent/ymin"/>
			</ows:LowerCorner>
			<ows:UpperCorner>
				<xsl:value-of select="/ags-rest/fullExtent/xmax"/>
				<xsl:value-of select="' '"/>
				<xsl:value-of select="/ags-rest/fullExtent/ymax"/>
			</ows:UpperCorner>
		</ows:WGS84BoundingBox>
	</xsl:template>
	<!-- World Bounding Box -->
	<xsl:template name="WorldBoundingBox">
		<ows:WGS84BoundingBox>
			<ows:LowerCorner>-180<xsl:value-of select="' '"/>-90</ows:LowerCorner>
			<ows:UpperCorner>180<xsl:value-of select="' '"/>90</ows:UpperCorner>
		</ows:WGS84BoundingBox>
	</xsl:template>
	<!--<xsl:template name="getLCMinx">
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
-->
</xsl:stylesheet>
