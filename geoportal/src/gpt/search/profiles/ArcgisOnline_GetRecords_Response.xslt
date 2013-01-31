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
					<xsl:attribute name="maxRecords"><xsl:value-of select="/gptJsonXml/total"/></xsl:attribute>
					<xsl:for-each select="/gptJsonXml/results">
						<Record>
							<ID>
								<xsl:value-of select="normalize-space(./id/text())"/>
							</ID>
							<Title>
								<xsl:choose>
									<xsl:when test=" ./title/text() != 'null' ">
										<xsl:value-of select="normalize-space(./title/text())"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="normalize-space(./name/text())"/>
									</xsl:otherwise>
								</xsl:choose>
							</Title>
							<Abstract>
								<xsl:value-of select="normalize-space(./description/text())"/>
							</Abstract>
							<Type>
								<!-- convert remote type to GPT type -->
								<xsl:choose>
									<xsl:when test="./type/text()='MapDocument'">mapFiles</xsl:when>
									<xsl:when test="./type/text()='Map Service'">liveData</xsl:when>
									<xsl:when test="./itemType/text()='file'">downloadableData</xsl:when>
									<xsl:otherwise>document</xsl:otherwise>
								</xsl:choose>
							</Type>
							<agolType>
								<xsl:value-of select="@type"/>
							</agolType>
							<!-- 
								<extent>
									<array>-74.5786</array>
									<array>17.2833</array>
								</extent>
								<extent>
									<array>-70.1511</array>
									<array>20.1727</array>
								</extent>
							-->
							<xsl:choose>
								<xsl:when test="count(extent) &lt; 2">
								
								</xsl:when>
								<xsl:otherwise>
									<xsl:variable name="x1" select="extent[1]/array[1]"/>
									<xsl:variable name="x2" select="extent[2]/array[1]"/>
									<xsl:variable name="y1" select="extent[1]/array[2]"/>
									<xsl:variable name="y2" select="extent[2]/array[2]"/>
									<xsl:choose>
										<xsl:when test="$x1 &lt; $x2">
											<MinX>
												<xsl:value-of select="$x1"/>
											</MinX>
											<MaxX>
												<xsl:value-of select="$x2"/>
											</MaxX>
										</xsl:when>
										<xsl:otherwise>
											<MinX>
												<xsl:value-of select="$x2"/>
											</MinX>
											<MaxX>
												<xsl:value-of select="$x1"/>
											</MaxX>
										</xsl:otherwise>
									</xsl:choose>
									<xsl:choose>
										<xsl:when test="$y1 &lt; $y2">
											<MinY>
												<xsl:value-of select="$y1"/>
											</MinY>
											<MaxY>
												<xsl:value-of select="$y2"/>
											</MaxY>
										</xsl:when>
										<xsl:otherwise>
											<MinY>
												<xsl:value-of select="$y2"/>
											</MinY>
											<MaxY>
												<xsl:value-of select="$y1"/>
											</MaxY>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:otherwise>
							</xsl:choose>


<!--
							<xsl:choose>
								<xsl:when test="count(/extent) &lt; 2">
									<MinX>-180</MinX>
									<MinY>-90</MinY>
									<MaxX>180</MaxX>
									<MaxY>90</MaxY>
								</xsl:when>
								<xsl:otherwise>
									<MinX>
										<xsl:value-of select="substring-after(substring-before(./extent/text(),','),'[[')"/>
									</MinX>
									<MinY>
										<xsl:value-of select="substring-before(substring-after(./extent/text(),','),']')"/>
									</MinY>
									<MaxX>
										<xsl:value-of select="substring-before(substring-after(substring-after(substring-after(./extent/text(),'['),'['),'['),',')"/>
									</MaxX>
									<MaxY>
										<xsl:value-of select="substring-before(substring-after(substring-after(substring-after(substring-after(./extent/text(),'['),'['),'['),','),']]')"/>
									</MaxY>
								</xsl:otherwise>
							</xsl:choose>
-->
							<ModifiedDate>
								<xsl:value-of select="./dct:modified"/>
							</ModifiedDate>
							<References>
								<xsl:choose>
									<xsl:when test="./itemType/text()='file'">
										<xsl:text>/content/items/</xsl:text><xsl:value-of select="normalize-space(./id/text())"/><xsl:text>/data</xsl:text>
										<xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server<xsl:text>&#x2715;</xsl:text>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="normalize-space(./item/text())"/>
										<xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server<xsl:text>&#x2715;</xsl:text>
									</xsl:otherwise>
								</xsl:choose>
							</References>
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
                 <Link label="catalog.search.searchResult.metadataSite">http://www.arcgisonline.com/home/item.html?id=<xsl:value-of select="normalize-space(./id/text())"/></Link>                        
                 <Link label="catalog.search.searchResult.openResource">
                 <xsl:choose>
                   <xsl:when test="./type/text()='Map Service'">
                      http://www.arcgis.com/home/webmap/viewer.html?services=<xsl:value-of select="normalize-space(./id/text())"/>
                    </xsl:when>
                   <xsl:otherwise>
                      http://www.arcgis.com/sharing/content/items/<xsl:value-of select="normalize-space(./id/text())"/>/data
                   </xsl:otherwise>
                 </xsl:choose>
                 </Link>                      
                 <Link gptLinkTag="metadata" show="false"/>
                 <Link gptLinkTag="addToMap" show="true"/>
                 <Link gptLinkTag="zoomTo" show="true"/>  
                 <Link gptLinkTag="open" show="false"/>
                 <Link gptLinkTag="previewInfo" show="false"/>
                 <xsl:choose>
                   <xsl:when test="./type/text()='Map Service'">
                     <Link gptLinkTag="preview" show="true"/>       
                   </xsl:when>  
                     <xsl:when test="./type/text()='file'">
                     <Link gptLinkTag="preview" show="true"/>       
                   </xsl:when>  
                    <xsl:otherwise>
                         <Link gptLinkTag="preview" show="false"/>      
                    </xsl:otherwise>
                 </xsl:choose>     
              </Links>
						</Record>
					</xsl:for-each>
				</Records>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
