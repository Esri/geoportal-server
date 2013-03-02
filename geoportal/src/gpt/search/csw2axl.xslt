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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:ogc="http://www.opengis.net/ogc" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:ows="http://www.opengis.net/ows" exclude-result-prefixes="csw dc dct gml ogc ows">
  <xsl:param name="metadataUrl"/>
  <xsl:param name="partialMetadataUrl"/>
  <xsl:param name="partialThumbnailUrl"/>
  <xsl:param name="startResult"/>
  <xsl:param name="maxResults"/>
  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
  <xsl:template match="/">
    <ARCXML version="1.1">
      <RESPONSE>
        <xsl:element name="METADATA" use-attribute-sets="MetadataAttributes">
          <xsl:for-each select="/csw:GetRecordsResponse/csw:SearchResults/csw:Record | /csw:GetRecordsResponse/csw:SearchResults/csw:BriefRecord | /csw:GetRecordByIdResponse/csw:Record | /csw:GetRecordsResponse/csw:SearchResults/csw:SummaryRecord">
            <xsl:element name="METADATA_DATASET" use-attribute-sets="MetadatasetAttributes">
              <xsl:element name="ENVELOPE" use-attribute-sets="EnvelopeAttributes"/>
            </xsl:element>
          </xsl:for-each>
        </xsl:element>
      </RESPONSE>
    </ARCXML>
  </xsl:template>
  <xsl:template name="metadataset">
    <xsl:for-each select="/csw:GetRecordsResponse/csw:SearchResults/csw:Record | /csw:GetRecordsResponse/csw:SearchResults/csw:BriefRecord | /csw:GetRecordByIdResponse/csw:Record | /csw:GetRecordsResponse/csw:SearchResults/csw:SummaryRecord">
      <xsl:element name="METADATA_DATASET">
        <xsl:attribute name="docid"><xsl:choose><xsl:when test="string-length(normalize-space(dc:identifier[@scheme='urn:x-esri:specification:ServiceType:ArcIMS:Metadata:DocID']/text())) > 0"><xsl:value-of select="normalize-space(dc:identifier[@scheme='urn:x-esri:specification:ServiceType:ArcIMS:Metadata:DocID'])"/></xsl:when><xsl:otherwise><xsl:value-of select="normalize-space(dc:identifier)"/></xsl:otherwise></xsl:choose></xsl:attribute>
        <xsl:element name="ENVELOPE" use-attribute-sets="EnvelopeAttributes"/>
      </xsl:element>
    </xsl:for-each>
  </xsl:template>
  <!--
  <FULLTEXT word="wms"/>
-->
  <xsl:template name="keyword">
    <xsl:if test="count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/FULLTEXT)>0">
      <ogc:PropertyIsLike wildCard="*" escape="\" singleChar="%" xmlns:ogc="http://www.opengis.net/ogc">
        <ogc:PropertyName>AnyText</ogc:PropertyName>
        <ogc:Literal>
          <xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/FULLTEXT/@word"/>
        </ogc:Literal>
      </ogc:PropertyIsLike>
    </xsl:if>
  </xsl:template>
  <!--
  <ENVELOPE maxx="-120.0" maxy="46.0" minx="-130.0" miny="42.0" spatialoperator="overlaps"/>

  TODO: handle @spatialoperator
-->
  <xsl:template name="envelope">
    <!-- generate BBOX query if minx, miny, maxx, maxy are provided -->
    <xsl:if test="count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/ENVELOPE)>0">
      <ogc:Contains xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
        <ogc:PropertyName>Geometry</ogc:PropertyName>
        <gml:Box srsName="EPSG:4326">
          <gml:coordinates>
            <xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/ENVELOPE/@maxx"/>,<xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/ENVELOPE/@minx"/>,<xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/ENVELOPE/@miny"/>,<xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/ENVELOPE/@maxy"/>
          </gml:coordinates>
        </gml:Box>
      </ogc:Contains>
    </xsl:if>
  </xsl:template>
  <xsl:attribute-set name="EnvelopeAttributes">
    <xsl:attribute name="maxx"><xsl:value-of select="normalize-space(substring-before(ows:WGS84BoundingBox/ows:UpperCorner, ' '))"/></xsl:attribute>
    <xsl:attribute name="maxy"><xsl:value-of select="normalize-space(substring-after(ows:WGS84BoundingBox/ows:UpperCorner, ' '))"/></xsl:attribute>
    <xsl:attribute name="minx"><xsl:value-of select="normalize-space(substring-before(ows:WGS84BoundingBox/ows:LowerCorner, ' '))"/></xsl:attribute>
    <xsl:attribute name="miny"><xsl:value-of select="normalize-space(substring-after(ows:WGS84BoundingBox/ows:LowerCorner, ' '))"/></xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="MetadatasetAttributes">
    <xsl:attribute name="url"><xsl:choose><xsl:when test="string-length(normalize-space($metadataUrl))>0"><xsl:value-of select="normalize-space($metadataUrl)"/></xsl:when><xsl:otherwise><xsl:choose><xsl:when test="string-length(normalize-space(dc:identifier[@scheme='urn:x-esri:specification:ServiceType:ArcIMS:Metadata:DocID']/text())) > 0"><xsl:value-of select="concat(normalize-space($partialMetadataUrl),normalize-space(dc:identifier[@scheme='urn:x-esri:specification:ServiceType:ArcIMS:Metadata:DocID']))"/></xsl:when><xsl:otherwise><xsl:value-of select="concat(normalize-space($partialMetadataUrl), normalize-space(dc:identifier))"/></xsl:otherwise></xsl:choose></xsl:otherwise></xsl:choose></xsl:attribute>
    <xsl:attribute name="thumbnail"><xsl:if test="string-length(normalize-space(dc:identifier[@scheme='urn:x-esri:specification:ServiceType:ArcIMS:Metadata:DocID']/text())) > 0"><xsl:value-of select="concat(normalize-space($partialThumbnailUrl),concat('/thumbnail?uuid=', normalize-space(dc:identifier[@scheme='urn:x-esri:specification:ServiceType:ArcIMS:Metadata:DocID'])))"/></xsl:if></xsl:attribute>
    <xsl:attribute name="docid">
	    <xsl:choose>
		    <xsl:when test="string-length(normalize-space(dc:identifier[@scheme='urn:x-esri:specification:ServiceType:ArcIMS:Metadata:DocID']/text())) > 0">
		      <xsl:value-of select="normalize-space(dc:identifier[@scheme='urn:x-esri:specification:ServiceType:ArcIMS:Metadata:DocID'])"/>
		    </xsl:when>
		    <xsl:otherwise>
		      <xsl:value-of select="normalize-space(dc:identifier)"/>
		    </xsl:otherwise>
	    </xsl:choose>
    </xsl:attribute>
    <xsl:attribute name="index_status"><xsl:value-of select="'indexed'"/></xsl:attribute>
    <xsl:attribute name="name"><xsl:value-of select="dc:title"/></xsl:attribute>
    <!--  <xsl:attribute name="">           
            <xsl:value-of select="dct:abstract"/>
          </xsl:attribute> -->
    <xsl:attribute name="content"><xsl:value-of select="dc:type"/></xsl:attribute>
    <xsl:attribute name="updated"><xsl:value-of select="./dct:modified"/></xsl:attribute>
<!-- <xsl:attribute name="onlink"><xsl:for-each select="./dct:references"><xsl:value-of select="."/><xsl:text>&#x2714;</xsl:text><xsl:value-of select="@scheme"/><xsl:text>&#x2715;</xsl:text></xsl:for-each></xsl:attribute>-->
    <xsl:attribute name="folder"><xsl:value-of select="'false'"/></xsl:attribute>
    <xsl:attribute name="children"><xsl:value-of select="'false'"/></xsl:attribute>
    <xsl:attribute name="siblings"><xsl:value-of select="'false'"/></xsl:attribute>
    <!--  <xsl:attribute name=""> 
              <Types>
                <xsl:for-each select="./dc:type">
                  <xsl:value-of select="."/>
                  <xsl:text>&#x2714;</xsl:text>
                  <xsl:value-of select="@scheme"/>
                  <xsl:text>&#x2715;</xsl:text>
                </xsl:for-each>
              </Types>
              </xsl:attribute>-->
  </xsl:attribute-set>
  <xsl:attribute-set name="MetadataAttributes">
    <xsl:attribute name="numresults">
     <xsl:choose>
    <xsl:when test="string-length($maxResults)>0 and string-length($startResult)>0" >    
      <xsl:choose>
        <xsl:when test="$startResult=0 and number(/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsMatched) > $maxResults">
          <xsl:value-of select="$maxResults"/>
        </xsl:when>
        <xsl:when test="string-length($maxResults)=0">
          <xsl:value-of select="/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsReturned"/>
        </xsl:when>
         <xsl:when test="$startResult>= 0 and number(/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsMatched) > $maxResults">
          <xsl:value-of select="/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsReturned"/>
        </xsl:when>
        <xsl:otherwise>
         <xsl:value-of select="/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsReturned"/>
        </xsl:otherwise>
      </xsl:choose>   
    </xsl:when> 
      <xsl:otherwise>
         <xsl:value-of select="/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsMatched"/>
         </xsl:otherwise>
    </xsl:choose>  
    </xsl:attribute>
    <xsl:attribute name="total">
   <xsl:choose>
    <xsl:when test="string-length($maxResults)>0 and string-length($startResult)>0" >  
          <xsl:choose>
            <xsl:when test="$startResult=0 and number(/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsMatched) > $maxResults">
              <xsl:value-of select="$maxResults"/>
            </xsl:when>
            <xsl:when test="string-length($maxResults)=0">
              <xsl:value-of select="/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsReturned"/>
            </xsl:when>
             <xsl:when test="$startResult>= 0 and number(/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsMatched) > $maxResults">
              <xsl:value-of select="number($startResult) + number(/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsReturned)"/>
            </xsl:when>
            <xsl:otherwise>
             <xsl:value-of select="/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsMatched"/>
            </xsl:otherwise>
        </xsl:choose>  
      </xsl:when> 
      <xsl:otherwise>
         <xsl:value-of select="/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsMatched"/>
     </xsl:otherwise>
    </xsl:choose>   
    </xsl:attribute>
    <xsl:attribute name="startresult">
    <xsl:choose>
     <xsl:when test="string-length($maxResults)>0">
      <xsl:value-of select="$startResult"/>
     </xsl:when>
     <xsl:otherwise>0</xsl:otherwise> 
     </xsl:choose>  
    </xsl:attribute>
  </xsl:attribute-set>
</xsl:stylesheet>
