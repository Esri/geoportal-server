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
<xsl:stylesheet version="1.0" xmlns:csw="http://www.opengis.net/cat/csw" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="no" encoding="UTF-8" omit-xml-declaration="yes"/>
	<xsl:template match="/">
	 <xsl:element name="csw:GetRecords" use-attribute-sets="GetRecordsAttributes"  xmlns:csw='http://www.opengis.net/cat/csw' xmlns:ogc='http://www.opengis.net/ogc' xmlns:gml='http://www.opengis.net/gml'>
		<csw:Query typeNames="ExtrinsicObject">
			<csw:ElementName>/ExtrinsicObject</csw:ElementName>
			<csw:Constraint version="1.0.0">
				<ogc:Filter>
					<ogc:And>
						<ogc:PropertyIsEqualTo>
							<ogc:PropertyName>/ExtrinsicObject/Name/LocalizedString/@value</ogc:PropertyName>
							<ogc:Literal>
								<xsl:value-of select="/GetRecords/KeyWord"/>
							</ogc:Literal>
						</ogc:PropertyIsEqualTo>
						<ogc:PropertyIsEqualTo>
							<ogc:PropertyName>/ExtrinsicObject/@objectType</ogc:PropertyName>
							<ogc:Literal>ISO19139</ogc:Literal>
						</ogc:PropertyIsEqualTo>
					</ogc:And>
				</ogc:Filter>
			</csw:Constraint>
		</csw:Query>
	</xsl:element>
	</xsl:template>
<xsl:attribute-set name="GetRecordsAttributes">
<xsl:attribute name="version">2.0.0</xsl:attribute>
<xsl:attribute name="maxRecords"><xsl:value-of select="/GetRecords/MaxRecords"/></xsl:attribute>
</xsl:attribute-set>
</xsl:stylesheet>
