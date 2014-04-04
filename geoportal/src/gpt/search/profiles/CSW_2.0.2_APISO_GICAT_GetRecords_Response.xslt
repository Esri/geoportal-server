<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dct="http://purl.org/dc/terms/" xmlns:ows="http://www.opengis.net/ows" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:srv="http://www.isotc211.org/2005/srv" exclude-result-prefixes="csw dc dct ows">
	<xsl:output method="xml" indent="no" encoding="UTF-8" omit-xml-declaration="yes"/>
	<xsl:template match="/">
		<xsl:variable name="lowercase">abcdefghijklmnopqrstuvwxyz</xsl:variable>
		<xsl:variable name="uppercase">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
		<xsl:choose>
			<xsl:when test="/ows:ExceptionReport">
				<exception>
					<exceptionText>
						<xsl:for-each select="/ows:ExceptionReport/ows:Exception">
							<xsl:value-of select="ows:ExceptionText"/>
						</xsl:for-each>
					</exceptionText>
				</exception>
			</xsl:when>
			<xsl:otherwise>
				<Records>
					<xsl:attribute name="maxRecords"><xsl:value-of select="/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsMatched"/></xsl:attribute>
					<xsl:for-each select="/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata | /csw:GetRecordByIdResponse/gmd:MD_Metadata">
						<Record>
							<ID>
								<xsl:value-of select="./gmd:fileIdentifier/gco:CharacterString"/>
							</ID>
							<Title>
								<xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString | ./gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
							</Title>
							<Abstract>
								<xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString | ./gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:abstract/gco:CharacterString"/>
							</Abstract>
							<Type>
								<xsl:value-of select="./gmd:hierarchyLevel/gmd:MD_ScopeCode/text()[count(./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue)=0] | ./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue[count(./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue)>0]"/>
							</Type>
							<LowerCorner>
								<xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal | ./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal"/>
								<xsl:text> </xsl:text>
								<xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal | ./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal"/>
							</LowerCorner>
							<UpperCorner>
								<xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal | ./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal"/>
								<xsl:text> </xsl:text>
								<xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal | ./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal"/>
							</UpperCorner>
							<MaxX>
								<xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal | ./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal"/>
							</MaxX>
							<MaxY>
								<xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal | ./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal"/>
							</MaxY>
							<MinX>
								<xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal | ./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal"/>
							</MinX>
							<MinY>
								<xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal | ./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal"/>
							</MinY>
							<ModifiedDate>
								<xsl:value-of select="./gmd:dateStamp/gco:Date | ./gmd:dateStamp/gco:DateTime"/>
							</ModifiedDate>
							<References>
								<xsl:value-of select="./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
								<xsl:text>&#x2714;</xsl:text>
								<xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server</xsl:text>
								<xsl:text>&#x2715;</xsl:text>
								<xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
								<xsl:text>&#x2714;</xsl:text>
								<xsl:text>http://www.isotc211.org/2005/gmd/MD_BrowseGraphic/filename</xsl:text>
								<xsl:text>&#x2715;</xsl:text>
							</References>
							<Types>
								<xsl:value-of select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:function/gmd:CI_OnLineFunctionCode"/>
								<xsl:text>&#x2714;</xsl:text>
								<xsl:text>http://www.isotc211.org/2005/gmd/MD_Metadata/hierarchyLevelName</xsl:text>
								<xsl:text>&#x2715;</xsl:text>
							</Types>
							<Links>
								<xsl:for-each select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL">
									<xsl:choose>
										<xsl:when test="contains(translate(.,$uppercase,$lowercase),'request=getmap')">
											<Link label="catalog.rest.link.getmap">
												<xsl:value-of select="."/>
											</Link>
										</xsl:when>
										<xsl:when test="contains(translate(.,$uppercase,$lowercase),'request=getcoverage')">
											<Link label="catalog.rest.link.getcoverage">
												<xsl:value-of select="."/>
											</Link>
										</xsl:when>
										<xsl:when test="contains(translate(../../gmd:protocol//text(),$uppercase,$lowercase),'ogc:wms-1.1.1-http-get-map')">
											<Link label="catalog.rest.link.getmap">
												<xsl:value-of select="."/>
											</Link>
										</xsl:when>
										<xsl:when test="contains(translate(../../gmd:protocol//text(),$uppercase,$lowercase),'urn:ogc:serviceType:WebMapService:1.1.1:http')">
											<Link label="catalog.rest.link.getmap">
												<xsl:value-of select="."/>
											</Link>
										</xsl:when>
										<xsl:when test="contains(translate(../../gmd:protocol//text(),$uppercase,$lowercase),'www:link-1.0-http--link')">
											<Link label="catalog.rest.link.html">
												<xsl:value-of select="."/>
											</Link>
										</xsl:when>
										<xsl:otherwise>
											<Link label="catalog.rest.link.html">
												<xsl:value-of select="."/>
											</Link>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:for-each>
							</Links>
						</Record>
					</xsl:for-each>
				</Records>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
