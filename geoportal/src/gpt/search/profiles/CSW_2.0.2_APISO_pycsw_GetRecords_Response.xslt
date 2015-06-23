<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" 
xmlns:dct="http://purl.org/dc/terms/" 
xmlns:ows="http://www.opengis.net/ows" 
xmlns:dc="http://purl.org/dc/elements/1.1/" 
xmlns:gmd="http://www.isotc211.org/2005/gmd" 
xmlns:gco="http://www.isotc211.org/2005/gco" 
xmlns:srv="http://www.isotc211.org/2005/srv"  
xmlns:gmi="http://www.isotc211.org/2005/gmi" >
	<xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no"/>
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
					<xsl:for-each select="/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata | /csw:GetRecordsResponse/csw:SearchResults/gmi:MI_Metadata">
						<Record>
							<ID>
                <xsl:choose>
                  <xsl:when test="string-length(./gmd:fileIdentifier/gco:CharacterString)>0"><xsl:value-of select="./gmd:fileIdentifier/gco:CharacterString"/></xsl:when>
                  <xsl:otherwise><xsl:value-of select="position()" /></xsl:otherwise>
                </xsl:choose>								
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
<!--
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
-->
							<Types>
								<xsl:value-of select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:function/gmd:CI_OnLineFunctionCode"/>
								<xsl:text>&#x2714;</xsl:text>
								<xsl:text>http://www.isotc211.org/2005/gmd/MD_Metadata/hierarchyLevelName</xsl:text>
								<xsl:text>&#x2715;</xsl:text>
							</Types>
							<Links>
								<xsl:for-each select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL">
									<xsl:choose>

										<!-- ends with csv -->
										<xsl:when test="substring(translate(.,$uppercase,$lowercase),string-length(.)-2, 3)='csv'">
											<Link label="catalog.rest.link.csv"><xsl:value-of select="."/></Link>
										</xsl:when>

										<!-- ends with DOC or DOCX -->
										<xsl:when test="substring(translate(.,$uppercase,$lowercase),string-length(.)-2, 3)='doc' or substring(translate(.,$uppercase,$lowercase),string-length(.)-2, 4)='docx'">
											<Link label="catalog.rest.link.doc"><xsl:value-of select="."/></Link>
										</xsl:when>

										<!-- ends with gml -->
										<xsl:when test="substring(translate(.,$uppercase,$lowercase),string-length(.)-2, 3)='gml'">
											<Link label="catalog.rest.link.gml"><xsl:value-of select="."/></Link>
										</xsl:when>

										<!-- ends with JSON -->
										<xsl:when test="substring(translate(.,$uppercase,$lowercase),string-length(.)-2, 4)='json'">
											<Link label="catalog.rest.link.json"><xsl:value-of select="."/></Link>
										</xsl:when>

										<!-- ends with GeoJSON -->
										<xsl:when test="substring(translate(.,$uppercase,$lowercase),string-length(.)-2, 7)='geojson'">
											<Link label="catalog.rest.link.geojson"><xsl:value-of select="."/></Link>
										</xsl:when>

										<!-- ends with KML -->
										<xsl:when test="substring(translate(.,$uppercase,$lowercase),string-length(.)-2, 3)='kml'">
											<Link label="catalog.rest.link.kml"><xsl:value-of select="."/></Link>
										</xsl:when>
										
										<!-- ends with PDF -->
										<xsl:when test="substring(translate(.,$uppercase,$lowercase),string-length(.)-2, 3)='pdf'">
											<Link label="catalog.rest.link.pdf"><xsl:value-of select="."/></Link>
										</xsl:when>

										<!-- ends with PPT or PPTX -->
										<xsl:when test="substring(translate(.,$uppercase,$lowercase),string-length(.)-2, 3)='ppt' or substring(translate(.,$uppercase,$lowercase),string-length(.)-2, 4)='pptx'">
											<Link label="catalog.rest.link.ppt"><xsl:value-of select="."/></Link>
										</xsl:when>

										<!-- ends with SHP (probably a zipped shapefile) -->
										<xsl:when test="substring(translate(.,$uppercase,$lowercase),string-length(.)-2, 3)='shp'">
											<Link label="catalog.rest.link.shp"><xsl:value-of select="."/></Link>
										</xsl:when>

										<!-- ends with XLS or XLSX -->
										<xsl:when test="substring(translate(.,$uppercase,$lowercase),string-length(.)-2, 3)='xls' or substring(translate(.,$uppercase,$lowercase),string-length(.)-2, 4)='xlsx'">
											<Link label="catalog.rest.link.xls"><xsl:value-of select="."/></Link>
										</xsl:when>

										<!-- ends with XML -->
										<xsl:when test="substring(translate(.,$uppercase,$lowercase),string-length(.)-2, 3)='xml' or substring(translate(.,$uppercase,$lowercase),string-length(.)-2, 4)='xml'">
											<Link label="catalog.rest.link.xml"><xsl:value-of select="."/></Link>
										</xsl:when>

                    <!-- ends with ZIP -->
										<xsl:when test="substring(translate(.,$uppercase,$lowercase),string-length(.)-2, 3)='zip'">
											<Link label="catalog.rest.link.zip"><xsl:value-of select="."/></Link>
										</xsl:when>
									
										<!-- is likely OGC service -->
										<xsl:when test="contains(translate(.,$uppercase,$lowercase),'request=getcapabilities')">
                      <xsl:choose>

                        <!-- is SOS service -->
                        <xsl:when test="contains(translate(.,$uppercase,$lowercase),'service=sos')">
                          <Link label="catalog.rest.link.sos">
                            <xsl:value-of select="."/>
                          </Link>
                        </xsl:when>

                        <!-- is wcs service -->
                        <xsl:when test="contains(translate(.,$uppercase,$lowercase),'service=wcs')">
                          <Link label="catalog.rest.link.wcs">
                            <xsl:value-of select="."/>
                          </Link>
                        </xsl:when>

                        <!-- is wfs service -->
                        <xsl:when test="contains(translate(.,$uppercase,$lowercase),'service=wfs')">
                          <Link label="catalog.rest.link.wfs">
                            <xsl:value-of select="."/>
                          </Link>
                        </xsl:when>
                      
                        <!-- is wms service -->
                        <xsl:when test="contains(translate(.,$uppercase,$lowercase),'service=wms')">
                          <Link label="catalog.rest.link.wms">
                            <xsl:value-of select="."/>
                          </Link>
                        </xsl:when>
                        
                        <!-- is something else -->
                        <xsl:otherwise>
                          <Link label="catalog.rest.link.html">
                            <xsl:value-of select="."/>
                          </Link>
                        </xsl:otherwise>
                      </xsl:choose>
										</xsl:when>
										
										<!-- contains getmap and is thus essentially a thumbnail -->
										<xsl:when test="contains(translate(.,$uppercase,$lowercase),'request=getmap')">
											<Link label="catalog.rest.link.getmap">
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
																				
										<!-- contains getcoverage and is thus essentially a (raster) file -->
										<xsl:when test="contains(translate(.,$uppercase,$lowercase),'request=getcoverage')">
											<Link label="catalog.rest.link.getcoverage">
												<xsl:value-of select="."/>
											</Link>
										</xsl:when>
										
										<!-- type indicates this is a generic link -->
										<xsl:when test="contains(translate(../../gmd:protocol//text(),$uppercase,$lowercase),'www:link-1.0-http--link')">
											<Link label="catalog.rest.link.html">
												<xsl:value-of select="."/>
											</Link>
										</xsl:when>
										
										<!-- nothing else, so include as generic link -->
										<xsl:otherwise>
											<Link label="catalog.rest.link.html">
												<xsl:value-of select="."/>
											</Link>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:for-each>
								
								<Link gptLinkTag="metadata" show="true"/>
								<Link gptLinkTag="addToMap" show="true"/>
								<Link gptLinkTag="zoomTo" show="true"/>  
								<Link gptLinkTag="open" show="false" />
								<Link gptLinkTag="previewInfo" show="false"/>
							</Links>
						</Record>
					</xsl:for-each>
				</Records>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
