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
<xsl:stylesheet version="1.0"  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dct="http://purl.org/dc/terms/" xmlns:ows="http://www.opengis.net/ows" xmlns:dc="http://purl.org/dc/elements/1.1/" >
  <xsl:output method="xml" indent="no"  encoding="UTF-8" omit-xml-declaration="yes" />
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
      <xsl:attribute name="maxRecords">
        <xsl:value-of select="/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsMatched"/>
      </xsl:attribute>
      <xsl:for-each select="/csw:GetRecordsResponse/csw:SearchResults/csw:Record | /csw:GetRecordsResponse/csw:SearchResults/csw:BriefRecord | /csw:GetRecordByIdResponse/csw:Record | /csw:GetRecordsResponse/csw:SearchResults/csw:SummaryRecord">
        <Record>
          <ID>
			<xsl:value-of select="normalize-space(dc:identifier)"/>
          </ID>
          <Title>
            <xsl:value-of select="dc:title"/>
          </Title>
          <Abstract>
            <xsl:value-of select="dct:abstract"/>
          </Abstract>
          <Type>
            <xsl:value-of select="dc:type"/>
          </Type>
          <LowerCorner>
          <xsl:value-of select="ows:WGS84BoundingBox/ows:LowerCorner"/>
          </LowerCorner>
          <UpperCorner>
          <xsl:value-of select="ows:WGS84BoundingBox/ows:UpperCorner"/>
          </UpperCorner>
          <MaxX>
                <xsl:value-of select="normalize-space(substring-before(ows:WGS84BoundingBox/ows:UpperCorner, ' '))"/>
              </MaxX>
              <MaxY>
                <xsl:value-of select="normalize-space(substring-after(ows:WGS84BoundingBox/ows:UpperCorner, ' '))"/>
              </MaxY>
              <MinX>
                <xsl:value-of select="normalize-space(substring-before(ows:WGS84BoundingBox/ows:LowerCorner, ' '))"/>
              </MinX>
              <MinY>
                <xsl:value-of select="normalize-space(substring-after(ows:WGS84BoundingBox/ows:LowerCorner, ' '))"/>
              </MinY>
              <ModifiedDate>
                <xsl:value-of select="./dct:modified"/>
              </ModifiedDate>
				<Types>
					<!-- convert remote type to GPT type again -->
					<xsl:choose>
						<xsl:when test="contains(./itemType/text(),'Image')">staticMapImages</xsl:when>
						<xsl:when test="contains(./type/text(),'MapDocument')">mapFiles</xsl:when>
						<xsl:when test="contains(./type/text(),'MapService')">liveData</xsl:when>
						<xsl:when test="contains(./itemType/text(),'file')">downloadableData</xsl:when>
						<xsl:otherwise>document</xsl:otherwise>
					</xsl:choose>
					<xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:ContentType<xsl:text>&#x2715;</xsl:text>
				</Types>
				<Links>  
                 <Link label="catalog.search.searchResult.openResource">https://www.sciencebase.gov/catalog/item/<xsl:value-of select="normalize-space(dc:identifier)"/></Link>                      
                 <Link gptLinkTag="metadata" show="false"/>
                 <Link gptLinkTag="addToMap" show="false"/>
                 <Link gptLinkTag="zoomTo" show="true"/>  
                 <Link gptLinkTag="open" show="true"/>
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
