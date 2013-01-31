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
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:csw="http://www.opengis.net/cat/csw"
	xmlns:ows="http://www.opengis.net/ows"
	xmlns:dct="http://purl.org/dc/terms/"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/"
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gco="http://www.isotc211.org/2005/gco"
	exclude-result-prefixes="csw dc dct dcmiBox ows">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="no" />
	<xsl:template match="/">
		<xsl:choose>
			<xsl:when test="/ows:ExceptionReport">
				<exception>
					<exceptionText>
						<xsl:for-each
							select="/ows:ExceptionReport/ows:Exception">
							<xsl:value-of select="ows:ExceptionText" />
						</xsl:for-each>
					</exceptionText>
				</exception>
			</xsl:when>
			<xsl:otherwise>
				<Records>
					<xsl:attribute name="maxRecords">
			      <xsl:value-of
							select="/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsMatched" />
     		  </xsl:attribute>
					<xsl:for-each
						select="/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata">
						<Record>
							<ID>
								<xsl:value-of
									select="./gmd:fileIdentifier/gco:CharacterString/text()" />
							</ID>
							<Title>
								<xsl:value-of
									select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString" />
							</Title>
							<Abstract>
								<xsl:value-of
									select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString" />
							</Abstract>
							<Type>liveData</Type>
							<LowerCorner>
								<xsl:value-of
									select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal" />
								<xsl:value-of select="' '" />
								<xsl:value-of
									select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal" />
							</LowerCorner>
							<UpperCorner>
								<xsl:value-of
									select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal" />
								<xsl:value-of select="' '" />
								<xsl:value-of
									select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal" />
							</UpperCorner>
							<References>
								<xsl:for-each
									select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine">
									<xsl:value-of
										select="./gmd:CI_OnlineResource/gmd:linkage/gmd:URL" />
									?service=wms
									<xsl:text>&#x2714;</xsl:text>
									urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server
									<xsl:text>&#x2715;</xsl:text>
								</xsl:for-each>
							</References>
							<Types>
								liveData
								<xsl:text>&#x2714;</xsl:text>
								urn:x-esri:specification:ServiceType:ArcIMS:Metadata:ContentType
								<xsl:text>&#x2715;</xsl:text>
							</Types>

						</Record>
					</xsl:for-each>
				</Records>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>
</xsl:stylesheet>



