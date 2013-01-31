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
	xmlns:ows="http://www.opengis.net/ows"
	xmlns:csw="http://www.opengis.net/cat/csw"
	xmlns:dct="http://purl.org/dc/terms/"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	exclude-result-prefixes="">
	<xsl:output method="xml" indent="no" encoding="UTF-8"
		omit-xml-declaration="yes" />
	<xsl:template match="/">
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
			<xsl:for-each
				select="/csw:GetRecordsResponse/csw:SearchResults/csw:SummaryRecord | //csw:Record">
				<Record>
					<ID>
						<xsl:value-of select="dc:identifier" />
					</ID>
					<Title>
						<xsl:value-of select="dc:title" />
					</Title>
					<Abstract>
						<xsl:value-of select="dct:abstract" />
					</Abstract>
					<Type>
						<xsl:value-of select="dc:type" />
					</Type>
					<LowerCorner>
						<xsl:choose>
							<xsl:when
								test="count(ows:WGS84BoundingBox/ows:LowerCorner)>0">
								<xsl:value-of
									select="substring-before(ows:WGS84BoundingBox/ows:UpperCorner, ' ')" />
								<xsl:text> </xsl:text>
								<xsl:value-of
									select="substring-after(ows:WGS84BoundingBox/ows:LowerCorner, ' ')" />
							</xsl:when>
							<xsl:when test="count(dct:spatial)>0">
								<!-- Get westlimit from dct:spatial Looks like arcims 9.1 flips x coords ! -->

								<xsl:value-of
									select="substring-before(substring-after(dct:spatial, 'eastlimit='),';')" />
								<xsl:text> </xsl:text>

								<!-- Get southlimit from dct:spatial -->
								<xsl:value-of
									select="substring-before(substring-after(dct:spatial, 'southlimit='),';')" />
							</xsl:when>
							<xsl:otherwise>-180 -90</xsl:otherwise>
						</xsl:choose>
					</LowerCorner>
					<UpperCorner>
						<xsl:choose>
							<xsl:when
								test="count(ows:WGS84BoundingBox/ows:UpperCorner)>0">
								<xsl:value-of
									select="substring-before(ows:WGS84BoundingBox/ows:LowerCorner, ' ')" />
								<xsl:text> </xsl:text>
								<xsl:value-of
									select="substring-after(ows:WGS84BoundingBox/ows:UpperCorner, ' ')" />
							</xsl:when>
							<xsl:when test="count(dct:spatial)>0">
								<!-- Get eastlimit from dct:spatial Looks like arcims 9.1 flips x coords ! -->
								<xsl:value-of
									select="substring-before(substring-after(dct:spatial, 'westlimit='),';')" />
								<xsl:text> </xsl:text>

								<!-- Get northlimit from dct:spatial -->
								<xsl:value-of
									select="substring-before(substring-after(dct:spatial, 'northlimit='),';')" />
							</xsl:when>
							<xsl:otherwise>180 90</xsl:otherwise>
						</xsl:choose>
					</UpperCorner>
				</Record>
			</xsl:for-each>
		</Records>
		</xsl:otherwise>
	 </xsl:choose>
	</xsl:template>
</xsl:stylesheet>
