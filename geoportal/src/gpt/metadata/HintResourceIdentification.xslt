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
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:param name="operation"/>
	<xsl:param name="input"/>
	<xsl:template match="/">
		<xsl:call-template name="main"/>
	</xsl:template>
	<xsl:template name="main">
		<output>
			<xsl:choose>
				<xsl:when test="normalize-space($operation) = 'guessServiceTypeFromUrl'">
					<xsl:call-template name="guessServiceTypeFromUrl"/>
				</xsl:when>
				<xsl:when test="normalize-space($operation) = 'guessArcIMSContentTypeFromUrl'">
					<xsl:call-template name="guessArcIMSContentTypeFromUrl"/>
				</xsl:when>
				<xsl:when test="normalize-space($operation) = 'guessArcIMSContentTypeFromResourceType'">
					<xsl:call-template name="guessArcIMSContentTypeFromResourceType"/>
				</xsl:when>
				<xsl:when test="normalize-space($operation) = 'getAgsMapServerSoapUrl'">
					<xsl:call-template name="getAgsMapServerSoapUrl"/>
				</xsl:when>
				<xsl:when test="normalize-space($operation) = 'getAgsRestServerRootUrl'">
					<xsl:call-template name="getAgsRestServerRootUrl"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'Invalid operation'"/>
				</xsl:otherwise>
			</xsl:choose>
		</output>
	</xsl:template>
	<xsl:template name="guessServiceTypeFromUrl">
		<xsl:choose>
			<xsl:when test="contains(normalize-space($input),'service=wms') or contains(normalize-space($input),'wmsserver')
									 or  contains(normalize-space($input),'com.esri.wms.esrimap') ">
				<xsl:value-of select="'wms'"/>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'service=wfs') or contains(normalize-space($input),'wfsserver')">
				<xsl:value-of select="'wfs'"/>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'service=wcs') or contains(normalize-space($input),'wcsserver')">
				<xsl:value-of select="'wcs'"/>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'com.esri.esrimap.esrimap')">
				<xsl:value-of select="'aims'"/>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'arcgis/rest') or contains(normalize-space($input),'arcgis/services')">
				<xsl:value-of select="'ags'"/>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'service=csw')">
				<xsl:value-of select="'csw'"/>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'.nmf')">
				<xsl:variable name="endsWithResult">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$input"/>
						<xsl:with-param name="substr" select="'.nmf'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="$endsWithResult = 'true'">
					<xsl:value-of select="'ArcGIS:nmf'"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'.lyr')">
				<xsl:variable name="endsWithResult">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$input"/>
						<xsl:with-param name="substr" select="'.lyr'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="$endsWithResult = 'true'">
					<xsl:value-of select="'ArcGIS:lyr'"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'.mxd')">
				<xsl:variable name="endsWithResult">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$input"/>
						<xsl:with-param name="substr" select="'.mxd'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="$endsWithResult = 'true'">
					<xsl:value-of select="'ArcGIS:mxd'"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'.kml')">
				<xsl:variable name="endsWithResult">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$input"/>
						<xsl:with-param name="substr" select="'.kml'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="$endsWithResult = 'true'">
					<xsl:value-of select="'kml'"/>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="''"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="ends-with">
		<xsl:param name="value"/>
		<xsl:param name="substr"/>
		<xsl:choose>
			<xsl:when test="substring(normalize-space($value), (string-length(normalize-space($value)) - string-length($substr)) + 1) = $substr">
				<xsl:value-of select="'true'"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'false'"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="index-of">
		<xsl:param name="value"/>
		<xsl:param name="substr"/>
		<xsl:choose>
			<xsl:when test="contains($value, $substr)">
				<xsl:value-of select="string-length(substring-before($value, $substr))+1"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'-1'"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="guessArcIMSContentTypeFromUrl">
		<xsl:choose>
			<xsl:when test="contains(normalize-space($input),'service=wms') or contains(normalize-space($input),'wmsserver')
									 or  contains(normalize-space($input),'com.esri.wms.esrimap') ">
				<xsl:value-of select="'liveData'"/>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'service=wfs') or contains(normalize-space($input),'wfsserver')">
				<xsl:value-of select="'liveData'"/>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'service=wcs') or contains(normalize-space($input),'wcsserver')">
				<xsl:value-of select="'liveData'"/>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'service=csw') or contains(normalize-space($input),'cswserver')">
				<xsl:value-of select="''"/>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'com.esri.esrimap.esrimap')">
				<xsl:value-of select="'liveData'"/>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'nmf')">
				<xsl:variable name="endsWithNmf">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$input"/>
						<xsl:with-param name="substr" select="'.nmf'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="indexOfNmf">
					<xsl:call-template name="index-of">
						<xsl:with-param name="value" select="$input"/>
						<xsl:with-param name="substr" select="'f=nmf'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="$endsWithNmf = 'true' or number(normalize-space($indexOfNmf)) > 0">
					<xsl:value-of select="'liveData'"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'lyr')">
				<xsl:variable name="endsWithLyr">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$input"/>
						<xsl:with-param name="substr" select="'.lyr'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="indexOfLyr">
					<xsl:call-template name="index-of">
						<xsl:with-param name="value" select="$input"/>
						<xsl:with-param name="substr" select="'f=lyr'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="$endsWithLyr = 'true' or number(normalize-space($indexOfLyr)) > 0">
					<xsl:value-of select="'liveData'"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'.mxd')">
				<xsl:variable name="endsWithResult">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$input"/>
						<xsl:with-param name="substr" select="'.mxd'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="$endsWithResult = 'true'">
					<xsl:value-of select="'liveData'"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'kml') or contains(normalize-space($input),'kmz')">
				<xsl:variable name="endsWithKml">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$input"/>
						<xsl:with-param name="substr" select="'.kml'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="endsWithKmz">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$input"/>
						<xsl:with-param name="substr" select="'.kmz'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="indexOfKml">
					<xsl:call-template name="index-of">
						<xsl:with-param name="value" select="$input"/>
						<xsl:with-param name="substr" select="'f=kml'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="$endsWithKml = 'true' or $endsWithKmz = 'true'  or number(normalize-space($indexOfKml)) > 0">
					<xsl:value-of select="'geographicActivities'"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'.xml')">
				<xsl:variable name="endsWithResult">
					<xsl:call-template name="ends-with">
						<xsl:with-param name="value" select="$input"/>
						<xsl:with-param name="substr" select="'.xml'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="$endsWithResult = 'true'">
					<xsl:if test="contains(normalize-space($input),'rss') or contains(normalize-space($input),'georss')">
						<xsl:value-of select="'geographicActivities'"/>
					</xsl:if>
				</xsl:if>
			</xsl:when>
			<xsl:when test="contains(normalize-space($input),'arcgis/rest') or contains(normalize-space($input),'arcgis/services')">
				<xsl:choose>
					<xsl:when test="contains(normalize-space($input),'/mapserver')">
						<xsl:value-of select="'liveData'"/>
					</xsl:when>
					<xsl:when test="contains(normalize-space($input),'/globeserver')">
						<xsl:value-of select="'liveData'"/>
					</xsl:when>
					<xsl:when test="contains(normalize-space($input),'/gpserver')">
						<xsl:value-of select="'geographicService'"/>
					</xsl:when>
					<xsl:when test="contains(normalize-space($input),'/geocodeserver')">
						<xsl:value-of select="'geographicService'"/>
					</xsl:when>
					<xsl:when test="contains(normalize-space($input),'/geometryserver')">
						<xsl:value-of select="'geographicService'"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="'geographicService'"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="''"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="guessArcIMSContentTypeFromResourceType">
		<xsl:call-template name="ArcIMS_Codes"/>
	</xsl:template>
	<xsl:template name="ArcIMS_Codes">
		<!-- ArcIMS codes-->
		<xsl:choose>
			<xsl:when test="normalize-space($input) = 'livedata'">
				<xsl:value-of select="'liveData'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'downloadabledata'">
				<xsl:value-of select="'downloadableData'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'offlinedata'">
				<xsl:value-of select="'offlineData'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'staticmapimage'">
				<xsl:value-of select="'staticMapImage'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'document'">
				<xsl:value-of select="'document'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'application'">
				<xsl:value-of select="'application'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'geographicservice'">
				<xsl:value-of select="'geographicService'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'clearinghouse'">
				<xsl:value-of select="'clearinghouse'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'mapfiles'">
				<xsl:value-of select="'mapFiles'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'geographicactivities'">
				<xsl:value-of select="'geographicActivities'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'unknown'">
				<xsl:value-of select="'unknown'"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="ISO_CI_OnLineFunctionCodes"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="ISO_CI_OnLineFunctionCodes">
		<xsl:choose>
			<xsl:when test="normalize-space($input) = 'download'">
				<xsl:value-of select="'downloadableData'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'order'">
				<xsl:value-of select="'downloadableData'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'offlineaccess'">
				<xsl:value-of select="'offlineData'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'search'">
				<xsl:value-of select="'application'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'information'">
				<xsl:value-of select="'application'"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="FGDC_Resource_Codes"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="FGDC_Resource_Codes">
		<!-- FGDC_Codes-->
		<xsl:choose>
			<xsl:when test="normalize-space($input) = 'live data and maps'">
				<xsl:value-of select="'liveData'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'downloadable data'">
				<xsl:value-of select="'downloadableData'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'offline data'">
				<xsl:value-of select="'offlineData'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'static map images'">
				<xsl:value-of select="'staticMapImage'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'other documents'">
				<xsl:value-of select="'document'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'applications'">
				<xsl:value-of select="'application'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'geographic services'">
				<xsl:value-of select="'geographicService'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'clearinghouses'">
				<xsl:value-of select="'clearinghouse'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'map files'">
				<xsl:value-of select="'mapFiles'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'geographic activities'">
				<xsl:value-of select="'geographicActivities'"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="FGDC_Variations_Codes"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="FGDC_Variations_Codes">
		<!-- FGDC_Variations_Codes-->
		<xsl:choose>
			<xsl:when test="normalize-space($input) = 'live data'">
				<xsl:value-of select="'liveData'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'documents'">
				<xsl:value-of select="'document'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'static map image'">
				<xsl:value-of select="'staticMapImage'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'other document'">
				<xsl:value-of select="'document'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'geographic service'">
				<xsl:value-of select="'geographicService'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'map file'">
				<xsl:value-of select="'mapFiles'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = 'geographic activities'">
				<xsl:value-of select="'geographicActivities'"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="Esri_Iso_Codes"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="Esri_Iso_Codes">
		<xsl:choose>
			<xsl:when test="normalize-space($input) = '001'">
				<xsl:value-of select="'liveData'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = '002'">
				<xsl:value-of select="'downloadableData'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = '003'">
				<xsl:value-of select="'offlineData'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = '004'">
				<xsl:value-of select="'staticMapImage'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = '005'">
				<xsl:value-of select="'document'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = '006'">
				<xsl:value-of select="'application'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = '007'">
				<xsl:value-of select="'geographicService'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = '008'">
				<xsl:value-of select="'clearinghouse'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = '009'">
				<xsl:value-of select="'mapFiles'"/>
			</xsl:when>
			<xsl:when test="normalize-space($input) = '010'">
				<xsl:value-of select="'geographicActivities'"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="getAgsMapServerSoapUrl">
		<xsl:variable name="indexOfAGSServices">
			<xsl:call-template name="index-of">
				<xsl:with-param name="value" select="$input"/>
				<xsl:with-param name="substr" select="'arcgis/services'"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:if test="number(normalize-space($indexOfAGSServices)) > 0">
			<xsl:variable name="endsWithMapServer">
				<xsl:call-template name="ends-with">
					<xsl:with-param name="value" select="$input"/>
					<xsl:with-param name="substr" select="concat('','/mapserver')"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="endsWithMapServerWsdl">
				<xsl:call-template name="ends-with">
					<xsl:with-param name="value" select="$input"/>
					<xsl:with-param name="substr" select="'mapserver?wsdl'"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:if test="normalize-space($endsWithMapServer) = 'true' or normalize-space($endsWithMapServerWsdl) = 'true'">
				<xsl:variable name="mapServerIndex">
					<xsl:call-template name="index-of">
						<xsl:with-param name="value" select="$input"/>
						<xsl:with-param name="substr" select="concat('','/mapserver')"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:value-of select="substring(normalize-space($input),0,number(normalize-space($mapServerIndex)) + string-length('/mapserver'))"/>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	<xsl:template name="getAgsRestServerRootUrl">
		<xsl:if test="contains(normalize-space($input),'/mapserver') and contains(normalize-space($input),'rest/services')">
			<xsl:variable name="mapServerIndex">
				<xsl:call-template name="index-of">
					<xsl:with-param name="value" select="$input"/>
					<xsl:with-param name="substr" select="concat('','/mapserver')"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="restServicesIndex">
				<xsl:call-template name="index-of">
					<xsl:with-param name="value" select="$input"/>
					<xsl:with-param name="substr" select="'rest/services'"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:if test="number(normalize-space($mapServerIndex)) > number(normalize-space($restServicesIndex))">
				<xsl:value-of select="substring(normalize-space($input),0,number(normalize-space($mapServerIndex)) + string-length('/mapserver'))"/>
			</xsl:if>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
