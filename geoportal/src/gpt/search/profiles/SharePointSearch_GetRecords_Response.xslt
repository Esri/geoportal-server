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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dct="http://purl.org/dc/terms/" 
xmlns:ows="http://www.opengis.net/ows" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:srrt="http://schemas.microsoft.com/WebParts/v3/srchrss/runtime" xmlns:media="http://search.yahoo.com/mrss/" xmlns:search="http://schemas.microsoft.com/SharePoint/Search/RSS"
exclude-result-prefixes="csw dc dct ows">
  <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no"/>
  <xsl:variable 
    name="link" 
    select="/rss/channel/link"/>
  <xsl:variable 
    name="linkStartRecord" 
    select="concat(substring-after(translate($link, 'start1=', 'START1='), 'START1='), '&amp;')"/>    
  <xsl:variable
    name="startRecord"
    select="number(normalize-space(substring-before($linkStartRecord, '&amp;')))"/> 
  <xsl:variable 
    name="numOfRecords"
    select="count(/rss/channel/item)"/>    
    
  <xsl:template match="/">  
        <Records>
          
          <xsl:attribute name="maxRecords">
            <xsl:choose>
	            <xsl:when test="string($startRecord) != 'NaN'">
	              <xsl:value-of select="$numOfRecords + $startRecord"></xsl:value-of>
	            </xsl:when>
	            <xsl:otherwise>
	              <xsl:value-of select="$numOfRecords"></xsl:value-of>
	            </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          <s>
          <xsl:value-of select="$link"/>
          <xsl:text> ... </xsl:text>
          <xsl:value-of select="$linkStartRecord"/>
          <xsl:text> ... </xsl:text>
          <xsl:value-of select="$numOfRecords"/>
          </s>
          <xsl:for-each select="/rss/channel/item">
            <Record>
              <ID>
                <xsl:value-of select="normalize-space(./link/text())"/>
              </ID>
              <Title>
                <xsl:choose>
                  <xsl:when test=" ./title/text() != 'null' ">
                    <xsl:value-of select="normalize-space(./title/text())"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="normalize-space(./search:hithighlightedsummary/text())"/>
                  </xsl:otherwise>
                </xsl:choose>
              </Title>
              <Abstract>
                <xsl:value-of select="normalize-space(./description/text())"/>
              </Abstract>
              <Type>
                <!-- convert remote type to GPT type -->
                Video
              </Type>
              <!-- [[-110.05,44.13],[-110,44.98]] -->
              <ModifiedDate>
                <xsl:value-of select="./pubDate"/>
              </ModifiedDate>
			<!--	<xsl:if test="count(./enclosure[@type='application/vnd.openxmlformats-officedocument.wordprocessingml.document']/@url)>0">
					<References>
						<xsl:value-of select="./enclosure[@type='application/vnd.openxmlformats-officedocument.wordprocessingml.document']/@url"/><xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server<xsl:text>&#x2715;</xsl:text>
					</References>
				</xsl:if>-->
		 			<xsl:if test="count(./link/text())>0">
					<References>
						<xsl:value-of select="./link/text()"/><xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server<xsl:text>&#x2715;</xsl:text>
					</References>
				</xsl:if>

              <Types>
                  <xsl:value-of select="normalize-space(./search:hithighlightedsummary/text())"/>
                <!-- convert remote type to GPT type again -->
               
                <xsl:text>document&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:ContentType<xsl:text>&#x2715;</xsl:text>
              </Types>
            </Record>
          </xsl:for-each>
        </Records>    
  </xsl:template>
</xsl:stylesheet>
