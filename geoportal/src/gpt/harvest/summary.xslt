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
<xsl:param name="parameter">Parameter</xsl:param>
<xsl:param name="value">Value</xsl:param>
<xsl:param name="recordsLimitation">Number of listed records limited to</xsl:param>
<xsl:param name="errorsLimitation">Number of reported errors limited to</xsl:param>

<xsl:template match="/">
  
<TABLE class="grid" cellspacing="0" cellpadding="2">
<TR>
  <TH scope="col"><xsl:value-of select="$parameter"/></TH>
  <TH scope="col"><xsl:value-of select="$value"/></TH>
</TR>
<xsl:for-each select="metadata/report/*">
  <xsl:choose>
    <xsl:when test="position() mod 2 = 1">
      <TR class="rowOdd">
        <xsl:call-template name="record"/>
      </TR>
    </xsl:when>
    <xsl:otherwise>
      <TR class="rowEven">
        <xsl:call-template name="record"/>
      </TR>
    </xsl:otherwise>
  </xsl:choose>
</xsl:for-each>
</TABLE>

</xsl:template>

<xsl:template name="record">
  <TD>
    <xsl:choose>
      <xsl:when test="name()='recordsLimitation'">
        <xsl:value-of select="$recordsLimitation"/>
      </xsl:when>
      <xsl:when test="name()='errorsLimitation'">
        <xsl:value-of select="$errorsLimitation"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="name()"/>
      </xsl:otherwise>
    </xsl:choose>
  </TD>
  <TD>
    <xsl:value-of select="."/>
  </TD>
</xsl:template>
</xsl:stylesheet>