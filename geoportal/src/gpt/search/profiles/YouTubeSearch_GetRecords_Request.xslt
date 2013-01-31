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
	  <xsl:text>?v=2&amp;safeSearch=moderate</xsl:text>
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
	  
		<xsl:text>&amp;start-index=</xsl:text>
		<xsl:value-of select="/GetRecords/StartPosition"/>
		<xsl:text>&amp;max-results=</xsl:text>
		<xsl:value-of select="/GetRecords/MaxRecords"/>
		<xsl:if test="count(/GetRecords/Envelope)>0">
			<xsl:variable name="minx" select="normalize-space(/GetRecords/Envelope/MinX)"/>
			<xsl:variable name="miny" select="normalize-space(/GetRecords/Envelope/MinY)"/>
			<xsl:variable name="maxx" select="normalize-space(/GetRecords/Envelope/MaxX)"/>
			<xsl:variable name="maxy" select="normalize-space(/GetRecords/Envelope/MaxY)"/>
			<xsl:variable name="diffX">
				<xsl:call-template name="getDiff">
					<xsl:with-param name="min" select="$minx"/>
					<xsl:with-param name="max" select="$maxx"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="diffY">
				<xsl:call-template name="getDiff">
					<xsl:with-param name="min" select="$miny"/>
					<xsl:with-param name="max" select="$maxy"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="diff">
				<xsl:call-template name="getDiff">
					<xsl:with-param name="min"  select="$diffX"/>
					<xsl:with-param name="max" select="$diffY"/>
				</xsl:call-template>
			</xsl:variable>		
			<xsl:text>&amp;location=</xsl:text><xsl:value-of select="concat(number(number($diffX) div 2),',',number(number($diffY) div 2))"/>
			<xsl:choose>
				<xsl:when test="$diff &lt; 1000">
					<xsl:text>&amp;location-radius=</xsl:text><xsl:value-of select="concat($diff,'km')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>&amp;location-radius=</xsl:text><xsl:value-of select="'1000km'"/>
				</xsl:otherwise>
			</xsl:choose> 		
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="getDiff">
		<xsl:param name="min"/>
		<xsl:param name="max"/>
		<xsl:choose>
			<xsl:when test="number($min) &gt; number($max)">
				<xsl:value-of select="number(number($min) - number($max))"></xsl:value-of>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="number(number($max) - number($min))"></xsl:value-of>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
