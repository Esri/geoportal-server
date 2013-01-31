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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:csw="http://www.opengis.net/cat/csw"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                exclude-result-prefixes="csw gco gmd">
  <xsl:output method="text" indent="no" encoding="UTF-8"/>
  <xsl:template match="/">
  <xsl:apply-templates select="//gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource"/>
  </xsl:template>
  
  <xsl:template match="//gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource">
     <xsl:if test="./gmd:description/gco:CharacterString='NetcdfSubset' ">
		<xsl:value-of select="./gmd:linkage/gmd:URL"/>
	 </xsl:if>
	 <xsl:if test="./gmd:description/gco:CharacterString='THREDDSCatalog' ">
		<xsl:value-of select="./gmd:linkage/gmd:URL"/>
	 </xsl:if>
  </xsl:template>
</xsl:stylesheet>


