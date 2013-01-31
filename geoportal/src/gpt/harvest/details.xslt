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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html"/>
<xsl:output encoding="UTF-8"/>

<!--
These params will be localized during harvest report production phase.
To add or modify localized string two steps has to be done:
1. localized message has to be entered/modified within gpt.properties file
2. mapping between param name and localized message added in ReportViewver.java
-->
<xsl:param name="sourceUri">Source URI</xsl:param>
<xsl:param name="validationStatus">Validation Status</xsl:param>
<xsl:param name="publishStatus">Publish Status</xsl:param>
<xsl:param name="validationError">Validation Error:</xsl:param>
<xsl:param name="publishError">Publish Error:</xsl:param>
<xsl:param name="statusOk">OK</xsl:param>
<xsl:param name="statusFailed">FAILED</xsl:param>

<xsl:template match="/">
  
<TABLE class="grid" cellspacing="0" cellpadding="2">
<TR>
  <TH><xsl:value-of select="$sourceUri"/></TH>
  <TH><xsl:value-of select="$validationStatus"/></TH>
  <TH><xsl:value-of select="$publishStatus"/></TH>
</TR>
  <xsl:for-each select="metadata/publishDetails/record">
    <xsl:call-template name="record">
      <xsl:with-param name="content" select="/" />
    </xsl:call-template>
  </xsl:for-each>
</TABLE>
  
</xsl:template>

<xsl:template name="record">
<TR>
	<xsl:if test="sourceUri != ''">
    <TD class="ok"><xsl:value-of select="sourceUri"/></TD>
	</xsl:if>
	<xsl:if test="docUuid != ''">
    <TD class="ok"><xsl:value-of select="docUuid"/></TD>
	</xsl:if>

  <xsl:choose>
		<xsl:when test="validate/status = 'ok'">
      <TD class="ok" align="center"><xsl:value-of select="$statusOk"/></TD>
    </xsl:when>
		<xsl:otherwise>
      <TD class="err"><STRONG><xsl:value-of select="$statusFailed"/></STRONG></TD>
		</xsl:otherwise>
  </xsl:choose>

  <xsl:choose>
		<xsl:when test="publish/status = 'ok'">
      <TD class="ok" align="center"><xsl:value-of select="$statusOk"/></TD>
    </xsl:when>
		<xsl:otherwise>
      <TD class="err"><STRONG><xsl:value-of select="$statusFailed"/></STRONG></TD>
		</xsl:otherwise>
  </xsl:choose>
</TR>

<xsl:if test="validate/status = 'failed'">
  <xsl:for-each select="validate/error">
    <xsl:call-template name="validateError">
      <xsl:with-param name="content" select="/" />
    </xsl:call-template>
  </xsl:for-each>
</xsl:if>

<xsl:if test="validate/status = 'ok'">
  <xsl:for-each select="publish/error">
    <xsl:call-template name="publishError">
      <xsl:with-param name="content" select="/" />
    </xsl:call-template>
  </xsl:for-each>
</xsl:if>

</xsl:template>

<xsl:template name="validateError">
<TR>
	<TD colspan="3" class="errdesc">
		<STRONG><xsl:value-of select="$validationError"/></STRONG>
    <xsl:text> </xsl:text>
    <xsl:value-of select="."/>
	</TD>
</TR>
</xsl:template>

<xsl:template name="publishError">
<TR>
	<TD colspan="3" class="errdesc">
		<STRONG><xsl:value-of select="$publishError"/></STRONG>
    <xsl:text> </xsl:text>
    <xsl:value-of select="."/>
	</TD>
</TR>
</xsl:template>

</xsl:stylesheet>