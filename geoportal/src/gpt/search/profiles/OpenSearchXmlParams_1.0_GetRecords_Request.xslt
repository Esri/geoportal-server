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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:gpt="http://www.esri.com/software/arcgis/geoportal">
  <xsl:output method="xml" indent="no" encoding="UTF-8" omit-xml-declaration="yes"/>
  <!-- #############################################################################################################
@author TM
@purpose Converts GPT Search Filters to XML parameters that can then be used by a program to replace the parameters.

Allowed parameters
{searchTerms}
{startPage}
{count}  (How many results per page)
{startIndex} (Starting result number)
{geo:box}  (west, south, east, north)
{gpt:boxWest}
{gpt:boxSouth}
{gpt:boxEast}
{gpt:boxNorth}
{time:start}
{time:end}
{gpt:contentType}
{gpt:dataCategory}
################################################################################################################## -->
  <!--
Globals
-->
  <xsl:param name="searchQueryUrl"/>
  <xsl:param name="searchQueryDoHitsOnly"/>
  <xsl:param name="gpt:contentType"/>
  
  <xsl:variable name="searchQueryUrlExample" select="'http://wxy?searchText={searchTerms}&amp;startpage={startPage}&amp;bbox={geo:box}&amp;dateStart={time:start}&amp;dateEnd={time:end}&amp;contentType={gpt:contentType}&amp;dCatagory={gpt:dataCategory}'"/>
  <xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyz'"/>
  <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>
  <xsl:variable name="searchQueryUrlL" select="translate($searchQueryUrl, $uppercase, $smallcase)"/>
  <!--  **********************************************************************************************************************************************************
Template default 

****************************************************************************************************************************************************************-->
  <xsl:template match="/">
    <params>
      <!-- Time Based Search -->
      <xsl:call-template name="filterTemporal"/>
      <!-- Key Word search -->
      <xsl:call-template name="filterKeyword"/>
      <!-- Content type search -->
      <xsl:call-template name="filterContentTypes"/>
      <!--  type search -->
      <xsl:call-template name="filterThemeTypes"/>
      <!-- Envelope search, e.g. ogc:BBOX -->
      <xsl:call-template name="filterSpatialBBox"/>
      <!-- Pagination-->
      <xsl:call-template name="filterPagination"/>
    </params>
  </xsl:template>
  <!-- ##########################################################################################################################
Filter: ContentType 
 -->
  <xsl:template name="filterContentTypes">
    <xsl:variable name="rootContentType" select="//filter[@class='com.esri.gpt.catalog.search.SearchFilterContentTypes']"/>
    <xsl:variable name="contentType" select="normalize-space($rootContentType/param[@name='selectedContentType']/@value)"/>
    <xsl:if test="contains($searchQueryUrlL, '{gpt:contenttype}' )">
      <parameter>
        <xsl:attribute name="key">{gpt:contenttype}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$contentType"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <xsl:if test="contains($searchQueryUrlL, '{gpt:contenttype?}' )">
      <parameter>
        <xsl:attribute name="key">{gpt:contenttype?}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$contentType"/></xsl:attribute>
      </parameter>
    </xsl:if>
  </xsl:template>
  <!-- ###########################################################################################################################
Entry point for theme types
-->
  <xsl:template name="filterThemeTypes">
    <xsl:variable name="rootThemeTypes" select="//filter[@class='com.esri.gpt.catalog.search.SearchFilterThemeTypes']"/>
    <xsl:variable name="themeTypes" select="$rootThemeTypes/param[@name='theme']/@value"/>
    <xsl:if test="contains($searchQueryUrlL, '{gpt:datacategory}' )">
      <parameter>
        <xsl:attribute name="key">{gpt:datacategory}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="translate($themeTypes,'|', ',')"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <xsl:if test="contains($searchQueryUrlL, '{gpt:datacategory?}' )">
      <parameter>
        <xsl:attribute name="key">{gpt:datacategory?}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="translate($themeTypes,'|', ',')"/></xsl:attribute>
      </parameter>
    </xsl:if>
  </xsl:template>
  <!-- *******************************************************************************************************************************************************
Template  filterKeyword
@param word  Word to be searched for
 ************************************************************************************************************************************************************* -->
  <xsl:template name="filterKeyword">
    <xsl:variable name="rootFilterKeyword" select="//filter[@class='com.esri.gpt.catalog.search.SearchFilterKeyword']"/>
    <xsl:variable name="keyword" select="normalize-space($rootFilterKeyword/param[@name='SearchText']/@value)"/>
    <xsl:variable name="keywordOperation" select="normalize-space($rootFilterKeyword/param[@name='SearchTextOption']/@value)"/>
    <xsl:choose>
      <xsl:when test="contains($searchQueryUrlL, '{searchterms}' )">
        <parameter>
          <xsl:attribute name="key">{searchterms}</xsl:attribute>
          <xsl:attribute name="value"><xsl:value-of select="$keyword"/></xsl:attribute>
        </parameter>
      </xsl:when>
      <xsl:when test="contains($searchQueryUrlL, '{searchterms?}' )">
        <parameter>
          <xsl:attribute name="key">{searchterms?}</xsl:attribute>
          <xsl:attribute name="value"><xsl:value-of select="$keyword"/></xsl:attribute>
        </parameter>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <!-- envelope search -->
  <!-- ***********************************************************************************************************************************************************************************
Template filterSpatialBBox 
Outputs the bounding box
@gptSpatialValues root of spatial nodes
************************************************************************************************************************************************************************************** -->
  <xsl:template name="filterSpatialBBox">
    <xsl:variable name="gptSpatialValues" select="//filter[@class='com.esri.gpt.catalog.search.SearchFilterSpatial']"/>
    <xsl:choose>
      <xsl:when test="contains($searchQueryUrlL, '{geo:box}' )">
        <parameter>
          <xsl:attribute name="key">{geo:box}</xsl:attribute>
          <xsl:attribute name="value">
          <xsl:if test="string-length($gptSpatialValues/param[@name='minX']/@value) &gt; 0">
            <xsl:value-of select="$gptSpatialValues/param[@name='minX']/@value"/>,<xsl:value-of select="$gptSpatialValues/param[@name='minY']/@value"/>,<xsl:value-of select="$gptSpatialValues/param[@name='maxX']/@value"/>,<xsl:value-of select="$gptSpatialValues/param[@name='maxY']/@value"/>
          </xsl:if>
          </xsl:attribute>
        </parameter>
      </xsl:when>
      <xsl:when test="contains($searchQueryUrlL, '{geo:box?}' )">
        <parameter>
          <xsl:attribute name="key">{geo:box?}</xsl:attribute>
          <xsl:attribute name="value">          
          <xsl:if test="string-length($gptSpatialValues/param[@name='minX']/@value) &gt; 0">
            <xsl:value-of select="$gptSpatialValues/param[@name='minX']/@value"/>,<xsl:value-of select="$gptSpatialValues/param[@name='minY']/@value"/>,<xsl:value-of select="$gptSpatialValues/param[@name='maxX']/@value"/>,<xsl:value-of select="$gptSpatialValues/param[@name='maxY']/@value"/>
          </xsl:if>
          </xsl:attribute>
        </parameter>
      </xsl:when>
    </xsl:choose>
    <!-- West -->
    <xsl:if test="contains($searchQueryUrlL, '{gpt:boxWest}' )">
      <parameter>
        <xsl:attribute name="key">{gpt:boxWest}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$gptSpatialValues/param[@name='minX']/@value"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <xsl:if test="contains($searchQueryUrlL, '{gpt:boxWest?}' )">
      <parameter>
        <xsl:attribute name="key">{gpt:boxWest?}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$gptSpatialValues/param[@name='minX']/@value"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <!-- South -->
    <xsl:if test="contains($searchQueryUrlL, '{gpt:boxSouth}' )">
      <parameter>
        <xsl:attribute name="key">{gpt:boxSouth}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$gptSpatialValues/param[@name='minY']/@value"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <xsl:if test="contains($searchQueryUrlL, '{gpt:boxSouth?}' )">
      <parameter>
        <xsl:attribute name="key">{gpt:boxSouth?}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$gptSpatialValues/param[@name='minY']/@value"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <!-- East -->
    <xsl:if test="contains($searchQueryUrlL, '{gpt:boxEast}' )">
      <parameter>
        <xsl:attribute name="key">{gpt:boxEast}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$gptSpatialValues/param[@name='maxX']/@value"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <xsl:if test="contains($searchQueryUrlL, '{gpt:boxEast?}' )">
      <parameter>
        <xsl:attribute name="key">{gpt:boxEast?}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$gptSpatialValues/param[@name='maxX']/@value"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <!-- North -->
    <xsl:if test="contains($searchQueryUrlL, '{gpt:boxNorth}' )">
      <parameter>
        <xsl:attribute name="key">{gpt:boxNorth}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$gptSpatialValues/param[@name='maxY']/@value"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <xsl:if test="contains($searchQueryUrlL, '{gpt:boxNorth?}' )">
      <parameter>
        <xsl:attribute name="key">{gpt:boxNorth?}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$gptSpatialValues/param[@name='maxY']/@value"/></xsl:attribute>
      </parameter>
    </xsl:if>
  </xsl:template>
  <!-- ##########################################################################################################################
Filter: Temporal Filter templates
 -->
  <!-- ***********************************************************************************************************************************************************************************
Template filterTemporal
Outputs CSW time parameters
************************************************************************************************************************************************************************************** -->
  <xsl:template name="filterTemporal">
    <xsl:variable name="rootTemporal" select="//filter[@class='com.esri.gpt.catalog.search.SearchFilterTemporal']"/>
    <xsl:variable name="modifiedFrom" select="normalize-space($rootTemporal/param[@name='modifiedDateFrom']/@value)"/>
    <xsl:variable name="modifiedTo" select="normalize-space($rootTemporal/param[@name='modifiedDateTo']/@value)"/>
    <xsl:variable name="temporalOption" select="normalize-space($rootTemporal/param[@name='selectedModifiedTime']/@value)"/>
    <xsl:if test="contains($searchQueryUrlL, '{time:start}' )">
      <parameter>
        <xsl:attribute name="key">{time:start}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$modifiedFrom"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <xsl:if test="contains($searchQueryUrlL, '{time:start?}' )">
      <parameter>
        <xsl:attribute name="key">{time:start?}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$modifiedFrom"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <xsl:if test="contains($searchQueryUrlL, '{time:end}' )">
      <parameter>
        <xsl:attribute name="key">{time:end}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$modifiedTo"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <xsl:if test="contains($searchQueryUrlL, '{time:end?}' )">
      <parameter>
        <xsl:attribute name="key">{time:end?}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$modifiedTo"/></xsl:attribute>
      </parameter>
    </xsl:if>
  </xsl:template>
  <!-- ##########################################################################################################################
Filter: Sort Filter templates
 -->
  <!-- ***********************************************************************************************************************************************************************************
Template filterTemporal
Outputs CSW sorting parameters
************************************************************************************************************************************************************************************** -->
  <xsl:template name="filterSort" xmlns:ogc="http://www.opengis.net/ogc">
    <xsl:variable name="root" select="//filter[@class='com.esri.gpt.catalog.search.SearchFilterSort']"/>
    <xsl:variable name="sortValue" select="normalize-space($root/param[@name='selectedSort']/@value)"/>
    <xsl:if test="string-length($sortValue) > 0">
      <xsl:if test="$sortValue != 'relevance'">
        <ogc:SortBy>
          <ogc:SortProperty>
            <ogc:PropertyName>
              <xsl:choose>
                <xsl:when test="$sortValue = 'areaAscending' or $sortValue = 'areaDescending' ">
                  <xsl:text>envelope</xsl:text>
                </xsl:when>
                <xsl:when test="$sortValue = 'dateAscending' or $sortValue = 'dateDescending' ">
                  <xsl:text>Modified</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$sortValue"/>
                </xsl:otherwise>
              </xsl:choose>
            </ogc:PropertyName>
            <xsl:choose>
              <xsl:when test="$sortValue = 'areaAscending' or $sortValue = 'dateAscending'">
                <ogc:SortOrder>ASC</ogc:SortOrder>
              </xsl:when>
              <xsl:when test="$sortValue = 'areaDescending' or $sortValue = 'dateDescending'">
                <ogc:SortOrder>DESC</ogc:SortOrder>
              </xsl:when>
            </xsl:choose>
          </ogc:SortProperty>
        </ogc:SortBy>
      </xsl:if>
    </xsl:if>
  </xsl:template>
  <!-- ##########################################################################################################################
Filter: Pagination template
************************************************************************************************************************************************************************************** -->
  <xsl:template name="filterPagination">
    <xsl:variable name="startPosition" select="//filter[@class='com.esri.gpt.catalog.search.SearchFilterPagination']/param[@name='startPosition']/@value"/>
    <xsl:variable name="currentPage" select="//filter[@class='com.esri.gpt.catalog.search.SearchFilterPagination']/param[@name='currentPage']/@value"/>
    <xsl:variable name="count" select="//filter[@class='com.esri.gpt.catalog.search.SearchFilterPagination']/param[@name='recordsPerPage']/@value"/>
    <xsl:if test="contains($searchQueryUrlL, '{startpage}' )">
      <parameter>
        <xsl:attribute name="key">{startpage}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$currentPage"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <xsl:if test="contains($searchQueryUrlL, '{startpage?}' )">
      <parameter>
        <xsl:attribute name="key">{startpage?}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$currentPage"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <xsl:if test="contains($searchQueryUrlL, '{startindex}' )">
      <parameter>
        <xsl:attribute name="key">{startIndex}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$startPosition"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <xsl:if test="contains($searchQueryUrlL, '{startindex?}' )">
      <parameter>
        <xsl:attribute name="key">{startIndex?}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$startPosition"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <xsl:if test="contains($searchQueryUrlL, '{count}' )">
      <parameter>
        <xsl:attribute name="key">{count}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$count"/></xsl:attribute>
      </parameter>
    </xsl:if>
    <xsl:if test="contains($searchQueryUrlL, '{count?}' )">
      <parameter>
        <xsl:attribute name="key">{count?}</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="$count"/></xsl:attribute>
      </parameter>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
