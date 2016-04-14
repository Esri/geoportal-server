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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#" xmlns:eq="http://earthquake.usgs.gov/rss/1.0/" xmlns:ows="http://www.opengis.net/ows" xmlns:georss="http://www.georss.org/georss" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:gml="http://www.opengis.net/gml" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/">
  <xsl:param name="sourceUrl"/>
  <xsl:param name="serviceType"/>
  <xsl:output omit-xml-declaration="no" method="xml" indent="yes" encoding="UTF-8"/>
  <xsl:template match="/">
    <xsl:call-template name="main"/>
  </xsl:template>
  <xsl:template name="main">
    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/">
      <rdf:Description>
        <xsl:attribute namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#" name="about">
          <xsl:value-of select="$sourceUrl"/>
        </xsl:attribute>
        <xsl:for-each select="/oai_dc:dc/dc:contributor">
          <dc:contributor>
            <xsl:value-of select="."/>
          </dc:contributor>
        </xsl:for-each>
        <xsl:for-each select="/oai_dc:dc/dc:coverage">
          <dc:coverage>
            <xsl:value-of select="."/>
          </dc:coverage>
        </xsl:for-each>
        <xsl:for-each select="/oai_dc:dc/dc:creator">
          <dc:creator>
            <xsl:value-of select="."/>
          </dc:creator>
        </xsl:for-each>
        <xsl:for-each select="/oai_dc:dc/dc:date">
          <dc:date>
            <xsl:value-of select="."/>
          </dc:date>
        </xsl:for-each>
        <xsl:for-each select="/oai_dc:dc/dc:description">
          <dc:description>
            <xsl:value-of select="."/>
          </dc:description>
        </xsl:for-each>
        <xsl:for-each select="/oai_dc:dc/dc:format">
          <dc:format>
            <xsl:value-of select="."/>
          </dc:format>
        </xsl:for-each>
        <xsl:for-each select="/oai_dc:dc/dc:identifier">
          <dc:identifier>
            <xsl:value-of select="."/>
          </dc:identifier>
        </xsl:for-each>
        <xsl:for-each select="/oai_dc:dc/dc:language">
          <dc:language>
            <xsl:value-of select="."/>
          </dc:language>
        </xsl:for-each>
        <xsl:for-each select="/oai_dc:dc/dc:publisher">
          <dc:publisher>
            <xsl:value-of select="."/>
          </dc:publisher>
        </xsl:for-each>
        <xsl:for-each select="/oai_dc:dc/dc:relation">
          <dc:relation>
            <xsl:value-of select="."/>
          </dc:relation>
        </xsl:for-each>
        <xsl:for-each select="/oai_dc:dc/dc:rights">
          <dc:rights>
            <xsl:value-of select="."/>
          </dc:rights>
        </xsl:for-each>
        <xsl:for-each select="/oai_dc:dc/dc:source">
          <dc:source>
            <xsl:value-of select="."/>
          </dc:source>
        </xsl:for-each>
        <xsl:for-each select="/oai_dc:dc/dc:subject">
          <dc:subject>
            <xsl:value-of select="."/>
          </dc:subject>
        </xsl:for-each>
        <xsl:for-each select="/oai_dc:dc/dc:title">
          <dc:title>
            <xsl:value-of select="."/>
          </dc:title>
        </xsl:for-each>
        <xsl:for-each select="/oai_dc:dc/dc:type">
          <dc:type>
            <xsl:value-of select="."/>
          </dc:type>
        </xsl:for-each>
      </rdf:Description>
    </rdf:RDF>
  </xsl:template>
</xsl:stylesheet>
