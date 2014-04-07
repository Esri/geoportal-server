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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="text" indent="no" encoding="UTF-8" omit-xml-declaration="yes"/>
	<!--  -->
	<xsl:template match="/">
	  <xsl:text>?o=search</xsl:text>
	  <xsl:variable name="keyword" select="normalize-space(/GetRecords/KeyWord)"/>
	  <xsl:variable name="liveDataTmp" select="normalize-space(/GetRecords/LiveDataMap)"/>
	  <xsl:variable name="bLiveData" 
	    select="translate(normalize-space($liveDataTmp),'true', 'TRUE')"/>
	  <xsl:choose>
	     <xsl:when test="$keyword = '' and $bLiveData != 'TRUE'">
	       <!-- avoiding empty keyword which causes an error. -->
         <xsl:text>&amp;q=thereShouldBeNoDataThatMatchesThisDefaultString</xsl:text>
	     </xsl:when>
	     <xsl:when test="$keyword != '' and $bLiveData = 'TRUE'">
         <xsl:text>&amp;q=</xsl:text><xsl:value-of select="$keyword"/>
         <xsl:text>%20typekeywords:service</xsl:text>
       </xsl:when>
       <xsl:when test="$bLiveData = 'TRUE'">
         <xsl:text>&amp;q=typekeywords:service</xsl:text>
       </xsl:when>
        <xsl:otherwise>
	        <!-- Search Text should be escaped already -->
         <xsl:text>&amp;q=</xsl:text><xsl:value-of select="$keyword"/>
 	     </xsl:otherwise>
	  </xsl:choose>
	  
      <xsl:if test="/GetRecords/RecordsFullyWithinEnvelope/text() = 'true' or /GetRecords/RecordsIntersectWithEnvelope/text() = 'true' ">
	       <xsl:text>&amp;bbox=</xsl:text>
	       <xsl:value-of select="/GetRecords/Envelope/MinX"/>,<xsl:value-of select="/GetRecords/Envelope/MinY"/>,<xsl:value-of select="/GetRecords/Envelope/MaxX"/>,<xsl:value-of select="/GetRecords/Envelope/MaxY"/>
       </xsl:if>
	  
		<xsl:text>&amp;start=</xsl:text>
		<xsl:value-of select="/GetRecords/StartPosition"/>
		<xsl:text>&amp;num=</xsl:text>
		<xsl:value-of select="/GetRecords/MaxRecords"/>
	</xsl:template>
</xsl:stylesheet>
