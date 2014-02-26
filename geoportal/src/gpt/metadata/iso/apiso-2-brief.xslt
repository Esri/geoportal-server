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
   xmlns:gml="http://www.opengis.net/gml" 
   xmlns:gmi="http://www.isotc211.org/2005/gmi"
   xmlns:gmd="http://www.isotc211.org/2005/gmd"
   xmlns:gco="http://www.isotc211.org/2005/gco"
   xmlns:srv="http://www.isotc211.org/2005/srv"> 
  <xsl:output indent="yes" method="xml" omit-xml-declaration="no"/>
  
  <xsl:template match="/gmi:MI_Metadata">
		<xsl:copy>
			<!-- <xsl:apply-templates/> -->
			<xsl:copy-of select="//gmd:fileIdentifier"/>
			<xsl:copy-of select="gmd:language[1]"/>			
			<xsl:copy-of select="//gmd:characterSet"/>			
			<xsl:copy-of select="//gmd:parentIdentifier"/>			
			<xsl:copy-of select="//gmd:hierarchyLevel"/>
			<xsl:copy-of select="//gmd:dateStamp"/>						
			<xsl:copy-of select="//gmd:metadataStandardName"/>
			<xsl:copy-of select="//gmd:metadataStandardVersion"/>			
			<xsl:copy-of select="//gmd:referenceSystemInfo"/>			
			<xsl:apply-templates select="gmd:identificationInfo[1]"/>
			<xsl:apply-templates select="//gmd:distributionInfo"/>					
			<xsl:apply-templates select="//gmd:dataQualityInfo"/>	
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//gmd:identificationInfo">
		<xsl:copy>
				<xsl:if test="count(//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title)=1">
					<xsl:apply-templates select="gmd:MD_DataIdentification"/>
				</xsl:if> 				

		</xsl:copy>	
	</xsl:template >

	<!-- dataset case -->
	<xsl:template match="//gmd:identificationInfo/gmd:MD_DataIdentification">
		<xsl:copy>
			<xsl:apply-templates select="gmd:citation"/>
			<xsl:copy-of select="//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview"/>
			<xsl:apply-templates select="gmd:extent"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation">
		<xsl:copy>
			<xsl:apply-templates select="gmd:CI_Citation"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation">
		<xsl:copy>
			 <xsl:copy-of select="//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title"/> 
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent">
		<xsl:copy>
			<xsl:apply-templates select="gmd:EX_Extent"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent">
		<xsl:copy>
			<xsl:apply-templates select="gmd:geographicElement"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement">
		<xsl:copy>
			<xsl:copy-of select="//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox"/>
		</xsl:copy>
	</xsl:template>

		<!-- sections common to dataset and service -->
	<!-- distrib info section -->
	<xsl:template match="//gmd:distributionInfo">
		<xsl:copy>
				<xsl:copy-of select="//gmd:distributionInfo/gmd:MD_Distribution"/>
		</xsl:copy>	
	</xsl:template >

	<!-- dataQualityInfo section -->
	<!-- distrib info section -->
	<xsl:template match="//gmd:dataQualityInfo">
		<xsl:copy>
				<xsl:apply-templates select="gmd:DQ_DataQuality"/>
		</xsl:copy>	
	</xsl:template >
	<xsl:template match="//gmd:dataQualityInfo/gmd:DQ_DataQuality">
		<xsl:copy>
				<xsl:copy-of select="//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage"/>
		</xsl:copy>	
	</xsl:template >
	
</xsl:stylesheet>

