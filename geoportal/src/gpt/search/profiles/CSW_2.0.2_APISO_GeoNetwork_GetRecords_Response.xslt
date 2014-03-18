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
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
	xmlns:ows="http://www.opengis.net/ows" xmlns:dct="http://purl.org/dc/terms/"
	xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="no" />
	<xsl:template match="/">
		<xsl:choose>
			<xsl:when test="/ows:ExceptionReport">
				<exception>
					<exceptionText>
						<xsl:for-each select="/ows:ExceptionReport/ows:Exception">
							<xsl:value-of select="ows:ExceptionText" />
						</xsl:for-each>
					</exceptionText>
				</exception>
			</xsl:when>
			<xsl:otherwise>
			  <!--  Test -->
				<Records>
					<xsl:attribute name="maxRecords">
            <xsl:value-of
						  select="/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsMatched" />
          </xsl:attribute>
					<xsl:for-each
						select="/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata">
						<Record>
							<ID>
								<xsl:value-of select="gmd:fileIdentifier/gco:CharacterString" />
							</ID>
							<Title>
								<xsl:value-of
									select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString|gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString" />
							</Title>
							<Abstract>
								<xsl:value-of
									select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString|gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:abstract/gco:CharacterString" />
							</Abstract>
							<LowerCorner>
								<xsl:value-of
									select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal|./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal" />
								<xsl:value-of select="' '" />
								<xsl:value-of
									select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal|./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal" />
							</LowerCorner>
							<UpperCorner>
								<xsl:value-of
									select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal|./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal" />
								<xsl:value-of select="' '" />
								<xsl:value-of
									select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal|./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal" />
							</UpperCorner>

							<xsl:for-each
								select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:protocol[contains(gco:CharacterString,'OGC:WMS')]]/gmd:linkage/gmd:URL">
								<References>
									<xsl:value-of select="." />
									<xsl:text>&#x2714;</xsl:text>
									urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server
									<xsl:text>&#x2715;</xsl:text>
								</References>
								<Types>
									liveData
									<xsl:text>&#x2714;</xsl:text>
									urn:x-esri:specification:ServiceType:ArcIMS:Metadata:ContentType
									<xsl:text>&#x2715;</xsl:text>
								</Types>
								<Type>liveData</Type>
							</xsl:for-each>
							<xsl:for-each
								select="./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata[srv:operationName/gco:CharacterString='GetCapabilities']/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL">
								<References>
									<xsl:value-of select="." />
									<xsl:text>&#x2714;</xsl:text>
									urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server
									<xsl:text>&#x2715;</xsl:text>
								</References>
								<Types>
									liveData
									<xsl:text>&#x2714;</xsl:text>
									urn:x-esri:specification:ServiceType:ArcIMS:Metadata:ContentType
									<xsl:text>&#x2715;</xsl:text>
								</Types>
								<Type>liveData</Type>
							</xsl:for-each>

							<!--
								<xsl:if
								test="count(./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:protocol/gco:CharacterString='OGC:WMS']/gmd:linkage/gmd:URL)
								+
								count(./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata[srv:operationName/gco:CharacterString='GetCapabilities'])>0">
								<References> <xsl:choose> <xsl:when
								test="count(./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata[srv:operationName/gco:CharacterString='GetCapabilities'])>0">
								<xsl:variable name="sUrl"
								select="./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata[srv:operationName/gco:CharacterString='GetCapabilities']/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
								<xsl:variable name="sUrlUpper"
								select="translate($sUrl,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
								<xsl:value-of select="$sUrl"/> <xsl:if
								test="not(contains($sUrlUpper,'?'))"> <xsl:text>?</xsl:text>
								</xsl:if> <xsl:if
								test="not(contains($sUrlUpper,'=GETCAPABILITIES'))">
								<xsl:text>&amp;request=GetCapabilities</xsl:text> </xsl:if>
								<xsl:if test="not(contains($sUrlUpper,'=WMS'))">
								<xsl:text>&amp;service=WMS</xsl:text> </xsl:if> <xsl:if
								test="not(contains($sUrlUpper,'VERSION='))">
								<xsl:text>&amp;version=1.3.0</xsl:text> </xsl:if> </xsl:when>
								<xsl:otherwise> <xsl:value-of
								select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:protocol/gco:CharacterString='OGC:WMS']/gmd:linkage/gmd:URL"/>
								</xsl:otherwise> </xsl:choose>
								<xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server<xsl:text>&#x2715;</xsl:text>
								</References>
								<Types>liveData<xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:ContentType<xsl:text>&#x2715;</xsl:text>
								</Types> <Type>liveData</Type> </xsl:if>
							-->
						</Record>
					</xsl:for-each>

				</Records>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>
</xsl:stylesheet>
