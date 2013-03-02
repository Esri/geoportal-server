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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dct="http://purl.org/dc/terms/" xmlns:ows="http://www.opengis.net/ows" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:media="http://search.yahoo.com/mrss/" xmlns:openSearch1="http://a9.com/-/spec/opensearch/1.1/" xmlns:openSearch="http://a9.com/-/spec/opensearchrss/1.0/">
	<xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no"/>
	<xsl:template match="/">
		<Records>
			<xsl:attribute name="maxRecords"><xsl:value-of select="count(/rss/channel/item)"/></xsl:attribute>
			<xsl:for-each select="/rss/channel/item">
				<Record>
					<ID>
						<xsl:value-of select="normalize-space(guid)"/>
					</ID>
					<Title>
						<xsl:value-of select="normalize-space(title)"/>
					</Title>
					<Abstract>
						<xsl:value-of select="normalize-space(description)"/>
					</Abstract>
					<Type>Document</Type>
					<!-- [[-110.05,44.13],[-110,44.98]] -->
					<MinX>-180</MinX>
					<MinY>-90</MinY>
					<MaxX>180</MaxX>
					<MaxY>90</MaxY>
					<ModifiedDate>
						<xsl:value-of select="pubDate"/>
					</ModifiedDate>
					<References>
						<xsl:value-of select="link"/>
						<xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server<xsl:text>&#x2715;</xsl:text>
					</References>
					<Links>
						<xsl:variable name="resourceUrl" select="link"/>
						<Link url="$resourceUrl" label="catalog.search.searchResult.viewFullMetadata"/>
						<Link gptLinkTag="customLink" show="true"/>
						
						<Link gptLinkTag="open" show="true"/>
						<Link gptLinkTag="preview" show="false"/>
						<Link gptLinkTag="metadata" show="false"/>
						<Link gptLinkTag="addToMap" show="false"/>
						<Link gptLinkTag="zoomTo" show="false"/>
					</Links>
				</Record>
			</xsl:for-each>
		</Records>
	</xsl:template>
</xsl:stylesheet>
