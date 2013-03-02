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
        <Records>
          <xsl:attribute name="maxRecords"><xsl:value-of select="count(/CatalogItems/CatalogItem)"/></xsl:attribute>
          <xsl:for-each select="/CatalogItems/CatalogItem">
            <Record>
              <ID>
                <xsl:value-of select="normalize-space(./guid/text())"/>
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
				<xsl:when test="count(./typeinfo/text())>0">
					<xsl:value-of select="normalize-space(./typeinfo/text())"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="normalize-space(./tags/text())"/></xsl:otherwise>
                </xsl:choose>
                
              </Type>
              <!-- [[-110.05,44.13],[-110,44.98]] -->
              <MinX>-180</MinX>
              <MinY>-90</MinY>
              <MaxX>180</MaxX>
              <MaxY>90</MaxY>
              <ModifiedDate>
                <xsl:value-of select="./datalastmodifiedtime/text()"/>
              </ModifiedDate>
              <References>
              <xsl:value-of select="normalize-space(./catalogpath)"/><xsl:value-of select="' '"/>
              <xsl:value-of select="normalize-space(./accessinformation)"/><xsl:value-of select="' '"/>
              <xsl:value-of select="normalize-space(./thumbnail/@src)"/><xsl:value-of select="' '"/>
              <xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server<xsl:text>&#x2715;</xsl:text>
                <xsl:choose>
                  <xsl:when test="./typeinfo/text()='file'">
                    <xsl:text>/content/items/</xsl:text>
                    <xsl:value-of select="normalize-space(./id/text())"/>
                   </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="normalize-space(./item/text())"/>
                   </xsl:otherwise>
                </xsl:choose>
              </References>
              <Types>       
                  <xsl:value-of select="normalize-space(./typekeywords/text())"/>
                  <xsl:value-of select="normalize-space(./snippet/text())"/>
                <!-- convert remote type to GPT type again -->
                <xsl:choose>
                  <xsl:when test="./type/text()='MapDocument'">mapFiles</xsl:when>
                  <xsl:when test="./type/text()='Map Service'">liveData</xsl:when>
                  <xsl:when test="./typeinfo/text()='file'">downloadableData</xsl:when>
                  <xsl:otherwise>document</xsl:otherwise>
                </xsl:choose>
              </Types>
              <Links>		
    
			   <Link gptLinkTag="preview" show="false"/>
			   <Link gptLinkTag="metadata" show="false"/>
			   <Link gptLinkTag="addToMap" show="false"/>
			   <Link gptLinkTag="zoomTo" show="false"/>
			   <Link gptLinkTag="open" show="false"/>
			 </Links>
            </Record>
          </xsl:for-each>
        </Records>    
  </xsl:template>
</xsl:stylesheet>
