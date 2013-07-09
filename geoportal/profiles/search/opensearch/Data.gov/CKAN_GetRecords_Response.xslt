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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dct="http://purl.org/dc/terms/" xmlns:ows="http://www.opengis.net/ows" xmlns:dc="http://purl.org/dc/elements/1.1/" exclude-result-prefixes="csw dc dct ows">
	<xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no"/>
	<xsl:template match="/">
		<xsl:variable name="lowercase">abcdefghijklmnopqrstuvwxyz</xsl:variable>
		<xsl:variable name="uppercase">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>	
		<xsl:choose>
			<xsl:when test="/gptJsonXml/error">
				<exception>
					<exceptionText>
						<xsl:for-each select="/gptJsonXml/error">
							<xsl:text> [ </xsl:text>
							<xsl:value-of select="./message"/>
							<xsl:text> : </xsl:text>
							<xsl:value-of select="./details"/>
							<xsl:text> : </xsl:text>
							<xsl:value-of select="./code"/>
							<xsl:text> ] </xsl:text>
						</xsl:for-each>
					</exceptionText>
				</exception>
			</xsl:when>
			<xsl:otherwise>
				<Records>
					<xsl:attribute name="maxRecords"><xsl:value-of select="count(/gptJsonXml/result/results)"/></xsl:attribute>
					<xsl:for-each select="/gptJsonXml/result/results">
						<Record>
							<ID>
								<xsl:value-of select="normalize-space(id)"/>
							</ID>
							<Title><xsl:value-of select="normalize-space(title)"/></Title>
							<Abstract>
								<xsl:value-of select="normalize-space(notes)"/>
							</Abstract>
							
							<MinX><xsl:value-of select="extras[key='bbox-west-long']/value"/></MinX>
							<MinY><xsl:value-of select="extras[key='bbox-south-lat']/value"/></MinY>
							<MaxX><xsl:value-of select="extras[key='bbox-east-long']/value"/></MaxX>
							<MaxY><xsl:value-of select="extras[key='bbox-north-lat']/value"/></MaxY>
							
							<!-- convert remote type to GPT type -->


<!--
							<ModifiedDate>
								<xsl:value-of select="./dct:modified"/>
							</ModifiedDate>
-->

							<Types>
								<!-- convert remote type to GPT type again -->
								<xsl:choose>
									<xsl:when test="./type/text()='MapDocument'">mapFiles</xsl:when>
									<xsl:when test="./type/text()='Map Service'">liveData</xsl:when>
									<xsl:when test="./itemType/text()='file'">downloadableData</xsl:when>
									<xsl:otherwise>document</xsl:otherwise>
								</xsl:choose>
								<xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:ContentType<xsl:text>&#x2715;</xsl:text>
							</Types>
							<Links>  
								<Link label="catalog.search.searchResult.metadataSite">https://catalog.data.gov/dataset/<xsl:value-of select="id"/></Link>
								<xsl:for-each select="resources">
									<xsl:choose>
										<xsl:when test="substring(translate(url,$uppercase,$lowercase),string-length(url)-2, 3)='zip'">
											<Link label="catalog.rest.link.zip"><xsl:value-of select="url"/></Link>
										</xsl:when>
										<xsl:when test="substring(translate(url,$uppercase,$lowercase),string-length(url)-2, 3)='pdf'">
											<Link label="catalog.rest.link.pdf"><xsl:value-of select="url"/></Link>
										</xsl:when>
										<xsl:otherwise>
											<Link label="catalog.rest.link.html"><xsl:value-of select="url"/></Link>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:for-each>
								<Link gptLinkTag="metadata" show="false"/>
								<Link gptLinkTag="addToMap" show="false"/>
								<Link gptLinkTag="zoomTo" show="false"/>  
								<Link gptLinkTag="open" show="true"><xsl:value-of select="resources[1]/url"/></Link>
								<Link gptLinkTag="previewInfo" show="false"/>
							</Links>
						</Record>
					</xsl:for-each>
				</Records>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
