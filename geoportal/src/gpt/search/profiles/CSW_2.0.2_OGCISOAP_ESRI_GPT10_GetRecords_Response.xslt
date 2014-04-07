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
<xsl:stylesheet version="1.0"  
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  
    xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" 
    xmlns:dct="http://purl.org/dc/terms/" 
    xmlns:ows="http://www.opengis.net/ows" 
    xmlns:dc="http://purl.org/dc/elements/1.1/" 
    xmlns:gmd="http://www.isotc211.org/2005/gmd" 
    xmlns:gco="http://www.isotc211.org/2005/gco" 
    xmlns:srv="http://www.isotc211.org/2005/srv"
    xmlns:gmi="http://www.isotc211.org/2005/gmi" 
    exclude-result-prefixes="csw dc dct ows gmi">
  <xsl:output method="xml" indent="no"  encoding="UTF-8" omit-xml-declaration="yes" />
  <xsl:template match="/">
 <xsl:choose>
  <xsl:when test="/ows:ExceptionReport">
        <exception>
          <exceptionText>
            <xsl:for-each select="/ows:ExceptionReport/ows:Exception">
              <xsl:value-of select="ows:ExceptionText"/>
            </xsl:for-each>
          </exceptionText>
        </exception>
   </xsl:when>
   <xsl:otherwise>
    <Records>
      <xsl:attribute name="maxRecords">
        <xsl:value-of select="/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsMatched"/>
      </xsl:attribute>
      <xsl:for-each select="/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata | /csw:GetRecordByIdResponse/gmd:MD_Metadata | /csw:GetRecordsResponse/csw:SearchResults/gmi:MI_Metadata">
        <Record>
          <ID>
          <xsl:value-of select="./gmd:fileIdentifier/gco:CharacterString"/>
          </ID>
          
          <Title>
            <xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString | ./gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
          </Title>
          <Abstract>
            <xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString | ./gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:abstract/gco:CharacterString"/>
          </Abstract>
          <Type>
            <xsl:value-of select="./gmd:hierarchyLevel/gmd:MD_ScopeCode/text()[count(./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue)=0] | ./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue[count(./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue)>0]"/>
          </Type>

          <LowerCorner>
                <xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal | ./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal"/>
	<xsl:text> </xsl:text>
               <xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal | ./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal"/>
           </LowerCorner>

          <UpperCorner>
              <xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal | ./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal"/>
               	<xsl:text> </xsl:text>
              <xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal | ./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal"/>
          </UpperCorner>

          <MaxX>
                <xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal | ./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal"/>
              </MaxX>
              <MaxY>
                <xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal | ./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal"/>
              </MaxY>
              <MinX>
                <xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal | ./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal"/>
              </MinX>
              <MinY>
                <xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal | ./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal"/>
              </MinY>
              <ModifiedDate>
                <xsl:value-of select="./gmd:dateStamp/gco:Date | ./gmd:dateStamp/gco:DateTime"/>
              </ModifiedDate>
  
              <References>
					<xsl:value-of select="./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
					
                  <xsl:text>&#x2714;</xsl:text>
                  <xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server</xsl:text>
                  <xsl:text>&#x2715;</xsl:text>
 			<xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
                  <xsl:text>&#x2714;</xsl:text>
                  <xsl:text>http://www.isotc211.org/2005/gmd/MD_BrowseGraphic/filename</xsl:text>
                  <xsl:text>&#x2715;</xsl:text>             
              </References>
              
              <Types>
           					<xsl:value-of select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:function/gmd:CI_OnLineFunctionCode"/>
                  <xsl:text>&#x2714;</xsl:text>
                  <xsl:text>http://www.isotc211.org/2005/gmd/MD_Metadata/hierarchyLevelName</xsl:text>
                  <xsl:text>&#x2715;</xsl:text>   
              </Types>

        </Record>
      </xsl:for-each>

    </Records>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>
</xsl:stylesheet>
