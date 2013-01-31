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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csw="http://www.opengis.net/cat/csw" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:srv="http://www.isotc211.org/2005/srv" exclude-result-prefixes="">
	<xsl:output method="xml" indent="no" encoding="UTF-8"/>
	
	<!--<xsl:template match="/">
		<xsl:copy-of select="."/>
	</xsl:template>
</xsl:stylesheet>-->

	<xsl:template match="/">
		<metadata>
			<Esri>
				<Server>
					<xsl:value-of select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations[1]/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
				</Server>
				<Service>
				<xsl:value-of select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations[1]/srv:SV_OperationMetadata/srv:DCP/srv:DCPList/@codeListValue"/>
				</Service>
				<ServiceType>
						<xsl:value-of select=" //gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName/text()"/>
				</ServiceType>
				<ServiceParam>request=<xsl:value-of select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations[1]/srv:SV_OperationMetadata/srv:operationName/gco:CharacterString/text()"/>
				</ServiceParam>
				<issecured>
					<xsl:value-of select="//Esri/issecured"/>
				</issecured>
				<resourceType>001</resourceType>
			</Esri>
		</metadata>
	</xsl:template>
	
</xsl:stylesheet>
