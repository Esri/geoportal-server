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
xmlns:atom="http://www.w3.org/2005/Atom"
xmlns:georss="http://www.georss.org/georss/10"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:dc="http://purl.org/dc/elements/1.1/" 
xmlns:dct="http://purl.org/dc/terms/" 
xmlns:media='http://search.yahoo.com/mrss/' 
xmlns:os='http://a9.com/-/spec/opensearch/1.1/'  
xmlns:content="http://purl.org/rss/1.0/modules/content/"
xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/" >
  <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no"/>
  <xsl:template match="/">
     <Records>
          <xsl:attribute name="maxRecords"><xsl:value-of select="/atom:feed/os:itemsPerPage"/></xsl:attribute>
          <xsl:for-each select="/atom:feed/atom:entry">
            <Record>
              <ID><xsl:value-of select="normalize-space(atom:link[1]/@href)" /></ID>
              <Title><xsl:value-of select="atom:title" /></Title>
              <Abstract>
<!--
						<xsl:variable name="summar">
							<xsl:call-template name="string-replace-all">
								<xsl:with-param name="text" select="description" />
								<xsl:with-param name="replace" select="'&amp;nbsp;'" />
								<xsl:with-param name="by" select="' '" />
							</xsl:call-template>
						</xsl:variable>
						<xsl:value-of select="$myVar" />
-->              
              <xsl:value-of select="normalize-space(atom:summary)" />
              </Abstract>
              <Type>Project</Type>
              <!-- <bbox>-80.57808,-75.44013,39.706043,41.91238</bbox> -->

              <MinX><xsl:value-of select="substring-before(georss:box,' ')"/></MinX>
              <MaxX><xsl:value-of select="substring-before(substring-after(georss:box,' '),' ')"/></MaxX>
              <MinY><xsl:value-of select="substring-before(substring-after(substring-after(georss:box,' '),' '),' ')"/></MinY>
              <MaxY><xsl:value-of select="substring-after(substring-after(substring-after(georss:box,' '),' '),' ')"/></MaxY>

              <ModifiedDate>
                <xsl:value-of select="atom:updated"/>
              </ModifiedDate>
              <References>
				<xsl:value-of select="normalize-space(atom:link[1]/@href)"/><xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server<xsl:text>&#x2715;</xsl:text>
				<xsl:value-of select="normalize-space(atom:link[@title='Search for granules']/@href)"/><xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Dataset:Url<xsl:text>&#x2715;</xsl:text>
			  </References>
              <Links>
			   <Link gptLinkTag="metadata" show="true"><xsl:value-of select="normalize-space(atom:link[1]/@href)"/></Link>
			   <Link gptLinkTag="open" show="true"><xsl:value-of select="normalize-space(atom:link[@title='Search for granules']/@href)"/></Link>
			   <Link gptLinkTag="preview" show="false"/>
			   <Link gptLinkTag="addToMap" show="false"/>
			   <Link gptLinkTag="zoomTo" show="false"/>
			</Links>
            </Record>
          </xsl:for-each>
        </Records>    
  </xsl:template>
  
 <xsl:template name="string-replace-all">
    <xsl:param name="text" />
    <xsl:param name="replace" />
    <xsl:param name="by" />
    <xsl:choose>
      <xsl:when test="contains($text, $replace)">
        <xsl:value-of select="substring-before($text,$replace)" />
        <xsl:value-of select="$by" />
        <xsl:call-template name="string-replace-all">
          <xsl:with-param name="text"
          select="substring-after($text,$replace)" />
          <xsl:with-param name="replace" select="$replace" />
          <xsl:with-param name="by" select="$by" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template></xsl:stylesheet>
