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
   xmlns:gmd="http://www.isotc211.org/2005/gmd"
   xmlns:gco="http://www.isotc211.org/2005/gco"
   xmlns:srv="http://www.isotc211.org/2005/srv"> 
  <xsl:output indent="yes" method="xml" omit-xml-declaration="no"/>
  
  <xsl:template match="/gmd:MD_Metadata">
		<xsl:copy>
			<!-- <xsl:apply-templates/> -->
			<xsl:copy-of select="/gmd:MD_Metadata/gmd:fileIdentifier"/>
			<xsl:copy-of select="/gmd:MD_Metadata/gmd:hierarchyLevel"/>
			<xsl:copy-of select="/gmd:MD_Metadata/gmd:contact"/>
			<xsl:copy-of select="/gmd:MD_Metadata/gmd:dateStamp"/>
			<xsl:apply-templates select="gmd:identificationInfo"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo">
		<xsl:copy>
				<xsl:if test="count(/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title)=1">
					<xsl:apply-templates select="gmd:MD_DataIdentification"/>
				</xsl:if> 				
				<xsl:if test="count(/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title)=1">
					<xsl:apply-templates select="srv:SV_ServiceIdentification"/>
				</xsl:if>
		</xsl:copy>	
	</xsl:template >

	<!-- dataset case -->
	<xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification">
		<xsl:copy>
			<xsl:apply-templates select="gmd:citation"/>
			<xsl:copy-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract"/>
			<xsl:copy-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview"/>
			<xsl:copy-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:purpose"/>
			<xsl:copy-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:language"/>
			<xsl:apply-templates select="gmd:extent"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation">
		<xsl:copy>
			<xsl:copy-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent">
		<xsl:copy>
			<xsl:apply-templates select="gmd:EX_Extent"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent">
		<xsl:copy>
			<xsl:apply-templates select="gmd:geographicElement"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement">
		<xsl:copy>
			<xsl:copy-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox"/>
		</xsl:copy>
	</xsl:template>

	<!-- service case -->	
	<xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification">
		<xsl:copy>
			<xsl:apply-templates select="gmd:citation"/>
			<xsl:copy-of select="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:graphicOverview"/>
			<xsl:copy-of select="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType"/>							
			<xsl:apply-templates select="srv:extent"/>
			<xsl:copy-of select="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceTypeVersion"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation">
		<xsl:copy>
			<xsl:copy-of select="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent">
		<xsl:copy>
			<xsl:apply-templates select="gmd:EX_Extent"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent">
		<xsl:copy>
			<xsl:apply-templates select="gmd:geographicElement"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement">
		<xsl:copy>
			<xsl:copy-of select="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox"/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>

