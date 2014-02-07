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

<!--

Extracts statisticts from the report into textual form:

harvested=<number>
validated=<number>
published=<number>
deleted=<number>

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="text"/>

<xsl:template match="/">
<xsl:text>harvested=</xsl:text><xsl:call-template name="harvested"/>
<xsl:text>
</xsl:text>
<xsl:text>validated=</xsl:text><xsl:call-template name="validated"/>
<xsl:text>
</xsl:text>
<xsl:text>published=</xsl:text><xsl:call-template name="published"/>
<xsl:text>
</xsl:text>
<xsl:text>deleted=</xsl:text><xsl:call-template name="deleted"/>
</xsl:template>

<xsl:template name="harvested">
	<xsl:choose>
		<xsl:when test="count(metadata/report/docsHarvested)>0">
			<xsl:value-of select="metadata/report/docsHarvested[1]"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:text>0</xsl:text>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="published">
	<xsl:choose>
		<xsl:when test="count(metadata/report/docsPublished)>0">
			<xsl:value-of select="metadata/report/docsPublished[1]"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:text>0</xsl:text>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="validated">
	<xsl:choose>
		<xsl:when test="count(metadata/report/docsPassedValidation)>0">
			<xsl:value-of select="metadata/report/docsPassedValidation[1]"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="published"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="deleted">
	<xsl:choose>
		<xsl:when test="count(metadata/report/docsDeleted)>0">
			<xsl:value-of select="metadata/report/docsDeleted[1]"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:text>0</xsl:text>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

</xsl:stylesheet>
