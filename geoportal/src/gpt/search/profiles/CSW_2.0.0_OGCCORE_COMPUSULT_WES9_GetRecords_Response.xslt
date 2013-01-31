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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns:csw="http://www.opengis.net/cat/csw" xmlns:dct="http://purl.org/dc/terms/" xmlns:dc="http://purl.org/dc/elements/1.1/" exclude-result-prefixes="csw dc dct">
  <xsl:output method="xml" indent="no"  encoding="UTF-8" omit-xml-declaration="yes" />
  <xsl:template match="/">
    <Records>
      <xsl:for-each select="/csw:GetRecordsResponse/csw:SearchResults/csw:SummaryRecord">
        <Record>
          <ID>
            <xsl:value-of select="dc:identifier"/>
          </ID>
          <Title>
            <xsl:value-of select="dc:title"/>
          </Title>
          <Abstract>
            <xsl:value-of select="dct:abstract"/>
          </Abstract>
          <Type>
            <xsl:value-of select="dc:type"/>
          </Type>
        </Record>
      </xsl:for-each>

    </Records>
  </xsl:template>
</xsl:stylesheet>