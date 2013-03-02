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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:ows="http://www.opengis.net/ows" xmlns:os="http://a9.com/-/spec/opensearch/1.1/">
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
        <dc:title>
          <xsl:value-of select="/os:OpenSearchDescription/os:ShortName"/>
        </dc:title>
				<xsl:if test="/os:OpenSearchDescription/os:Description">
					<dc:description>
						<xsl:value-of select="/os:OpenSearchDescription/os:Description"/>
					</dc:description>
				</xsl:if>
				<dc:format>
					<xsl:value-of select="'application/opensearchdescription+xml'"/>
				</dc:format>
				<dc:identifier>
					<xsl:value-of select="$sourceUrl"/>
				</dc:identifier>
				<xsl:if test="/os:OpenSearchDescription/os:Language">
					<dc:language>
						<xsl:value-of select="/os:OpenSearchDescription/os:Language"/>
					</dc:language>
				</xsl:if>
                <dc:type>
                    <xsl:value-of select="'OpenSearch'"/>
                </dc:type>
                <dc:subject>OpenSearch</dc:subject>
				<dct:references>
					<xsl:value-of select="$sourceUrl"/>
				</dct:references>
			</rdf:Description>
		</rdf:RDF>
	</xsl:template>
</xsl:stylesheet>
