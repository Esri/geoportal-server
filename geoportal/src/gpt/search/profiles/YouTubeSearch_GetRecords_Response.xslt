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
xmlns:ows="http://www.opengis.net/ows" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:atom='http://www.w3.org/2005/Atom' 
xmlns:media='http://search.yahoo.com/mrss/' 
xmlns:openSearch1='http://a9.com/-/spec/opensearch/1.1/'  xmlns:openSearch="http://a9.com/-/spec/opensearchrss/1.0/"
xmlns:gd='http://schemas.google.com/g/2005' xmlns:yt='http://gdata.youtube.com/schemas/2007' 
gd:etag='W/&quot;D0cGRn8ycCp7ImA9WxBWE00.&quot;'
exclude-result-prefixes="csw dc dct ows">
  <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no"/>
  <xsl:template match="/">
     <Records>
          <xsl:attribute name="maxRecords"><xsl:value-of select="concat(/atom:feed/openSearch:totalResults,/atom:feed/openSearch1:totalResults )"/></xsl:attribute>
          <xsl:for-each select="/atom:feed/atom:entry">
            <Record>
              <ID>
				    <xsl:if test="string-length(substring-after(./atom:id,'videos/'))>0">
					   <xsl:value-of select="normalize-space(substring-after(./atom:id,'videos/'))"/>
					   <xsl:variable name="resourceId" select="normalize-space(substring-after(./atom:id,'videos/'))"/>
					</xsl:if>
					<xsl:if test="string-length(substring-after(./atom:id,':video:'))>0">
					   <xsl:value-of select="normalize-space(substring-after(./atom:id,':video:'))"/>
					   <xsl:variable name="resourceId" select="normalize-space(substring-after(./atom:id,':video:'))"/>				
					</xsl:if>
              </ID>
              <Title>
                <xsl:choose>
                  <xsl:when test=" ./atom:title/text() != 'null' ">
                    <xsl:value-of select="normalize-space(./atom:title/text())"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="normalize-space(./media:group/media:title/text())"/>
                  </xsl:otherwise>
                </xsl:choose>
              </Title>
              <Abstract>
                <xsl:value-of select="normalize-space(./media:group/media:description/text())"/>
              </Abstract>
              <Type>
                <!-- convert remote type to GPT type -->
                Video
              </Type>
              <!-- [[-110.05,44.13],[-110,44.98]] -->
              <MinX>-180</MinX>
              <MinY>-90</MinY>
              <MaxX>180</MaxX>
              <MaxY>90</MaxY>
              <ModifiedDate>
                <xsl:value-of select="./atom:updated"/>
              </ModifiedDate><References>
				<xsl:if test="count(./media:group/media:content[@type='application/x-shockwave-flash']/@url)>0">
					
						<xsl:value-of select="./media:group/media:content[@type='application/x-shockwave-flash']/@url"/><xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server<xsl:text>&#x2715;</xsl:text>
					
				</xsl:if>
					<xsl:if test="count(./media:group/media:thumbnail/@url)>0">
				
						<xsl:value-of select="./media:group/media:thumbnail/@url"/><xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Thumbnail<xsl:text>&#x2715;</xsl:text>
					
				</xsl:if>
                               
				<xsl:if test="count(/atom:feed/atom:logo)>0">
					
						<xsl:value-of select="/atom:feed/atom:logo"/><xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:ContentType<xsl:text>&#x2715;</xsl:text>
					
				</xsl:if>
                                </References>
<!--          <xsl:value-of select="normalize-space(./atom:link/@href)"/>              
              <xsl:value-of select="normalize-space(./media:group/media:thumbnail/@url)"/>-->
              <Types>
                  <xsl:value-of select="normalize-space(./media:group/media:keywords/text())"/>
                <!-- convert remote type to GPT type again -->
               
                <xsl:text>Video</xsl:text>]
              </Types>
              <Links>
			   <Link url="http://www.youtube.com" label="catalog.search.searchSite.youtubesearch"/>
			   <!-- xsl:variable name="resourceUrl" select="concat(concat('http://www.youtube.com/watch?v=',$resourceId),'&amp;feature=youtube_gdata')"/>
			   <Link url="$resourceUrl" label="catalog.search.searchResult.viewFullMetadata"/ -->
			   <Link gptLinkTag="open" show="true"/>
			   <Link gptLinkTag="preview" show="true"/>
			   <Link gptLinkTag="metadata" show="false"/>
			   <Link gptLinkTag="addToMap" show="false"/>
			   <Link gptLinkTag="zoomTo" show="false"/>
			</Links>
            </Record>
          </xsl:for-each>
        </Records>    
  </xsl:template>
</xsl:stylesheet>
