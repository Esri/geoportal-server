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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gmi="http://www.isotc211.org/2005/gmi" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:srv="http://www.isotc211.org/2005/srv">
  <xsl:output indent="yes" method="html" omit-xml-declaration="yes"/>
  <xsl:template match="/gmd:MD_Metadata|/gmi:MI_Metadata">
    <style>
			.iso_section_title {font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 12pt; font-weight: bold; color: #333333}
			.iso_body {font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 10pt; line-height: 16pt; color: #333333}
			.iso_body .toolbarTitle {font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 14pt; color: #333333; margin:0px;}
			.iso_body .headTitle {  font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 11pt; color: #333333; font-weight: bold}
			.iso_body dl {margin-left: 20px;}
			.iso_body em {font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 10pt; font-weight: bold; color: #333333}
			.iso_body a:link {color: #B66B36; text-decoration: underline}
			.iso_body a:visited {color: #B66B36; text-decoration: underline}
			.iso_body a:hover {color: #4E6816; text-decoration: underline}
			.iso_body li {font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 10pt; line-height: 14pt; color: #333333}
			hr { background-color: #CCCCCC; border: 0 none; height: 1px; }
		</style>
    <script type="text/javascript">
			var mdeEnvelopeIds = new Array('envelope_west','envelope_south','envelope_east','envelope_north');
		</script>
    <div class="iso_body">
      <xsl:call-template name="Page_Title"/>
      <xsl:call-template name="Metadata_Info"/>
      <xsl:call-template name="Identification_Info"/>
      <xsl:if test="count(/gmd:MD_Metadata/gmd:distributionInfo) +  count(/gmi:MI_Metadata/gmd:distributionInfo)>0">
        <xsl:call-template name="Distribution_Info"/>
      </xsl:if>
      <xsl:if test="count(/gmd:MD_Metadata/gmd:dataQualityInfo) +  count(/gmi:MI_Metadata/gmd:dataQualityInfo)>0">
        <xsl:call-template name="Data_Quality_Info"/>
      </xsl:if>
      <xsl:if test="count(/gmi:MI_Metadata/gmi:acquisitionInformation)>0">
        <xsl:call-template name="Acquisition_Info"/>
      </xsl:if>
    </div>
  </xsl:template>
  <xsl:template name="Page_Title">
    <h1 class="toolbarTitle">
      <xsl:value-of select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:identificationInfo/*[self::gmd:MD_DataIdentification or self::srv:SV_ServiceIdentification]/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
    </h1>
    <hr/>
  </xsl:template>
  <xsl:template name="Metadata_Info">
    <div class="iso_section_title">
      <xsl:call-template name="get_property">
        <xsl:with-param name="key">catalog.iso19139.MD_Metadata.section.metadata</xsl:with-param>
      </xsl:call-template>
    </div>
    <dl>
      <xsl:for-each select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:fileIdentifier">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Metadata.<xsl:value-of select="local-name()"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:fileIdentifier/gco:CharacterString"/>
        </dt>
      </xsl:for-each>
      <xsl:if test="count(/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:language) > 0">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Metadata.<xsl:value-of select="local-name(/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:language)"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:choose>
            <xsl:when test=" string-length(/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:language/gmd:LanguageCode/@codeListValue) > 0">
              <xsl:call-template name="get_property">
                <xsl:with-param name="key">catalog.mdCode.language.iso639_2.<xsl:value-of select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:language/gmd:LanguageCode/@codeListValue"/>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:language/gco:CharacterString"/>
            </xsl:otherwise>
          </xsl:choose>
        </dt>
      </xsl:if>
      <xsl:if test="count(/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:characterSet) > 0">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Metadata.<xsl:value-of select="local-name(/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:characterSet)"/>: </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:characterSet"/>
        </dt>
      </xsl:if>
      <xsl:if test="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:parentIdentifier">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Metadata.<xsl:value-of select="local-name(/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:parentIdentifier)"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:parentIdentifier/gco:CharacterString"/>
        </dt>
      </xsl:if>
      <xsl:if test="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:hierarchyLevel/gmd:MD_ScopeCode">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.gemini.MD_Metadata.hierarchyLevel</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.<xsl:value-of select="local-name(/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:hierarchyLevel/gmd:MD_ScopeCode)"/>.<xsl:value-of select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"/>
            </xsl:with-param>
          </xsl:call-template>
        </dt>
      </xsl:if>
      <xsl:for-each select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:contact">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_ResponsibleParty</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:apply-templates select="gmd:CI_ResponsibleParty"/>
        </dt>
      </xsl:for-each>
      <xsl:if test="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:dateStamp">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Metadata.<xsl:value-of select="local-name(/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:dateStamp)"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:dateStamp/gco:Date"/>
        </dt>
      </xsl:if>
      <xsl:if test="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:metadataStandardName">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Metadata.<xsl:value-of select="local-name(/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:metadataStandardName)"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:metadataStandardName/gco:CharacterString"/>
        </dt>
      </xsl:if>
      <xsl:if test="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:metadataStandardVersion">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Metadata.<xsl:value-of select="local-name(/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:metadataStandardVersion)"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:metadataStandardVersion/gco:CharacterString"/>
        </dt>
      </xsl:if>
    </dl>
  </xsl:template>
  <xsl:template name="Identification_Info">
    <xsl:for-each select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:identificationInfo">
      <xsl:if test="gmd:MD_DataIdentification">
        <div class="iso_section_title">
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.MD_Metadata.MD_DataIdentification</xsl:with-param>
          </xsl:call-template>
        </div>
        <xsl:apply-templates select="gmd:MD_DataIdentification"/>
      </xsl:if>
      <xsl:if test="srv:SV_ServiceIdentification">
        <div class="iso_section_title">
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.MD_Metadata.MD_ServiceIdentification</xsl:with-param>
          </xsl:call-template>
        </div>
        <xsl:apply-templates select="srv:SV_ServiceIdentification"/>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  <xsl:template match="gmd:MD_DataIdentification | srv:SV_ServiceIdentification">
    <dl>
      <xsl:for-each select="gmd:abstract">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.AbstractMD_Identification.<xsl:value-of select="local-name()"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gco:CharacterString"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:purpose">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.AbstractMD_Identification.<xsl:value-of select="local-name()"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gco:CharacterString"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:language">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_DataIdentification.<xsl:value-of select="local-name()"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:choose>
            <xsl:when test=" string-length(gmd:LanguageCode/@codeListValue) > 0">
              <xsl:call-template name="get_property">
                <xsl:with-param name="key">catalog.mdCode.language.iso639_2.<xsl:value-of select="gmd:LanguageCode/@codeListValue"/>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="gco:CharacterString"/>
            </xsl:otherwise>
          </xsl:choose>
        </dt>
      </xsl:for-each>
      <xsl:if test="gmd:graphicOverview">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Metadata.section.identification.graphicOverview</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="gmd:graphicOverview/gmd:MD_BrowseGraphic"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:citation">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_Citation</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="gmd:citation/gmd:CI_Citation"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:pointOfContact">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.AbstractMD_Identification.pointOfContact</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="gmd:pointOfContact/gmd:CI_ResponsibleParty"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode/@codeListValue">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_DataIdentification.spatialRepresentationType</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.MD_SpatialRepresentationTypeCode.<xsl:value-of select="gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode/@codeListValue"/>
            </xsl:with-param>
          </xsl:call-template>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Resolution.equivalentScale</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:text>1:</xsl:text>
          <xsl:value-of select="gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:topicCategory/gmd:MD_TopicCategoryCode">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_DataIdentification.topicCategory</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.MD_TopicCategoryCode.<xsl:value-of select="gmd:topicCategory/gmd:MD_TopicCategoryCode"/>
            </xsl:with-param>
          </xsl:call-template>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:descriptiveKeywords">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.AbstractMD_Identification.descriptiveKeywords</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="gmd:descriptiveKeywords/gmd:MD_Keywords"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Metadata.section.identification.extent.geographicElement</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox"/>
        </dt>
      </xsl:if>
      <xsl:if test="srv:serviceType/gco:LocalName">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.SV_ServiceIdentification.serviceType</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="srv:serviceType/gco:LocalName"/>
        </dt>
      </xsl:if>
      <xsl:for-each select="srv:serviceTypeVersion/gco:CharacterString">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.SV_ServiceIdentification.serviceTypeVersion</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="."/>
        </dt>
      </xsl:for-each>
      <xsl:if test="srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.SV_ServiceIdentification.extent</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox"/>
        </dt>
      </xsl:if>
      <xsl:for-each select="srv:containsOperations/srv:SV_OperationMetadata">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.SV_Operation</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="."/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:resourceConstraints/gmd:MD_Constraints">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Metadata.section.identification.resourceConstraints</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:useLimitation/gco:CharacterString"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:resourceConstraints/gmd:MD_LegalConstraints">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_LegalConstraints</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="."/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:resourceConstraints/gmd:MD_SecurityConstraints">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_SecurityConstraints</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="."/>
        </dt>
      </xsl:for-each>
    </dl>
  </xsl:template>
  
  <!-- CI_ResponsibleParty -->
  <xsl:template match="gmd:citedResponsibleParty/gmd:CI_ResponsibleParty | gmd:pointOfContact/gmd:CI_ResponsibleParty | gmd:CI_ResponsibleParty">
    <dl>
      <xsl:for-each select="gmd:individualName">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_ResponsibleParty.<xsl:value-of select="local-name()"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gco:CharacterString"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:organisationName">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_ResponsibleParty.<xsl:value-of select="local-name()"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gco:CharacterString"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:positionName">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_ResponsibleParty.<xsl:value-of select="local-name()"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gco:CharacterString"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:role">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_ResponsibleParty.<xsl:value-of select="local-name()"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.CI_RoleCode.<xsl:value-of select="gmd:CI_RoleCode/@codeListValue"/>
            </xsl:with-param>
          </xsl:call-template>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:contactInfo">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_ResponsibleParty.contactInfo</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:apply-templates select="gmd:CI_Contact"/>
        </dt>
      </xsl:for-each>
    </dl>
  </xsl:template>
  
  <!-- gmd:CI_Citation -->
  <xsl:template match="*[self::gmd:citation or self::gmd:specification or self::gmi:citation]/gmd:CI_Citation">
    <em>
      <xsl:call-template name="get_property">
        <xsl:with-param name="key">catalog.iso19139.CI_ResponsibleParty.contactInfo</xsl:with-param>
      </xsl:call-template>: </em>
    <dl>
      <xsl:for-each select="gmd:title">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_Citation.<xsl:value-of select="local-name()"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gco:CharacterString"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:alternativeTitle | gmd:alternateTitle">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_Citation.<xsl:value-of select="local-name()"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gco:CharacterString"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:date">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_Citation.<xsl:value-of select="local-name()"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="gmd:CI_Date"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:edition">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_Citation.<xsl:value-of select="local-name()"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gco:CharacterString"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:editionDate">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_Citation.<xsl:value-of select="local-name()"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gco:Date"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:identifier | gmi:identifier">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_Citation.<xsl:value-of select="local-name()"/>
              </xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:apply-templates select="gmd:MD_Identifier/gmd:code/gco:CharacterString"/>
        </dt>
      </xsl:for-each>
      <xsl:if test="gmd:citedResponsibleParty">
        <dt>
          <xsl:apply-templates select="gmd:citedResponsibleParty/gmd:CI_ResponsibleParty"/>
        </dt>
      </xsl:if>
    </dl>
  </xsl:template>
  
  
  <xsl:template match="gmd:CI_Date">
    <dl>
      <dt>
        <em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.CI_Date.date</xsl:with-param>
          </xsl:call-template>: </em>
        <xsl:value-of select="gmd:date/gco:Date"/>
      </dt>
      <dt>
        <em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.CI_Date.dateType</xsl:with-param>
          </xsl:call-template>: </em>
        <xsl:call-template name="get_property">
          <xsl:with-param name="key">catalog.iso19139.CI_DateTypeCode.<xsl:value-of select="gmd:dateType/gmd:CI_DateTypeCode/@codeListValue"/></xsl:with-param>
        </xsl:call-template>
      </dt>
    </dl>
  </xsl:template>
  <xsl:template match="gmd:contactInfo/gmd:CI_Contact | gmd:CI_Contact">
    <dl>
      <xsl:if test="gmd:phone/gmd:CI_Telephone/gmd:voice">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_Telephone.voice</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:phone/gmd:CI_Telephone/gmd:facsimile">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_Telephone.facsimile</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:phone/gmd:CI_Telephone/gmd:facsimile/gco:CharacterString"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:address/gmd:CI_Address/gmd:deliveryPoint">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_Address.deliveryPoint</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:address/gmd:CI_Address/gmd:deliveryPoint"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:address/gmd:CI_Address/gmd:city">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_Address.city</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:address/gmd:CI_Address/gmd:city"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:address/gmd:CI_Address/gmd:administrativeArea">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_Address.administrativeArea</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:address/gmd:CI_Address/gmd:administrativeArea"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:address/gmd:CI_Address/gmd:postalCode">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_Address.postalCode</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:address/gmd:CI_Address/gmd:postalCode"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:address/gmd:CI_Address/gmd:country">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_Address.country</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:address/gmd:CI_Address/gmd:country"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:address/gmd:CI_Address/gmd:electronicMailAddress">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_Address.electronicMailAddress</xsl:with-param>
            </xsl:call-template>: </em>
          <a>
            <xsl:attribute name="href">mailto:<xsl:value-of select="gmd:address/gmd:CI_Address/gmd:electronicMailAddress"/></xsl:attribute>
            <xsl:value-of select="gmd:address/gmd:CI_Address/gmd:electronicMailAddress"/>
          </a>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:onlineResource">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_Contact.onlineResource</xsl:with-param>
            </xsl:call-template>: </em>
          <a>
            <xsl:attribute name="href"><xsl:value-of select="gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/></xsl:attribute>
            <xsl:value-of select="gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
          </a>
        </dt>
      </xsl:if>
    </dl>
  </xsl:template>
  <xsl:template match="gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox | srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
    <dl>
      <dt>
        <em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.EX_GeographicBoundingBox.westBoundLongitude</xsl:with-param>
          </xsl:call-template>: </em>
        <span id="mdDetails:envelope_west">
          <xsl:value-of select="gmd:westBoundLongitude/gco:Decimal"/>
        </span>
      </dt>
      <dt>
        <em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.EX_GeographicBoundingBox.eastBoundLongitude</xsl:with-param>
          </xsl:call-template>: </em>
        <span id="mdDetails:envelope_east">
          <xsl:value-of select="gmd:eastBoundLongitude/gco:Decimal"/>
        </span>
      </dt>
      <dt>
        <em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.EX_GeographicBoundingBox.northBoundLatitude</xsl:with-param>
          </xsl:call-template>: </em>
        <span id="mdDetails:envelope_north">
          <xsl:value-of select="gmd:northBoundLatitude/gco:Decimal"/>
        </span>
      </dt>
      <dt>
        <em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.EX_GeographicBoundingBox.southBoundLatitude</xsl:with-param>
          </xsl:call-template>: </em>
        <span id="mdDetails:envelope_south">
          <xsl:value-of select="gmd:southBoundLatitude/gco:Decimal"/>
        </span>
      </dt>
    </dl>
  </xsl:template>
  <xsl:template match="gmd:descriptiveKeywords/gmd:MD_Keywords">
    <dl>
      <xsl:for-each select="gmd:keyword/gco:CharacterString">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Keywords.keyword</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="."/>
        </dt>
      </xsl:for-each>
      <xsl:if test="gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Keywords.thesaurusName</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
        </dt>
      </xsl:if>
    </dl>
  </xsl:template>
  <xsl:template match="gmd:MD_LegalConstraints | gmd:resourceConstraints/gmd:MD_LegalConstraints">
    <dl>
      <xsl:if test="gmd:accessConstraints">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_LegalConstraints.accessConstraints</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.MD_RestrictionCode.<xsl:value-of select="gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue"/>
            </xsl:with-param>
          </xsl:call-template>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:useConstraints">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_LegalConstraints.useConstraints</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.MD_RestrictionCode.<xsl:value-of select="gmd:useConstraints/gmd:MD_RestrictionCode/@codeListValue"/>
            </xsl:with-param>
          </xsl:call-template>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:otherConstraints">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_LegalConstraints.otherConstraints</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:otherConstraints/gco:CharacterString"/>
        </dt>
      </xsl:if>
    </dl>
  </xsl:template>
  <xsl:template match="gmd:MD_SecurityConstraints">
    <dl>
      <xsl:if test="gmd:classification">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_SecurityConstraints.classification</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:classification/gmd:MD_ClassificationCode/@codeListValue"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:userNote">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_SecurityConstraints.userNote</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:userNote/gco:CharacterString"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:classificationSystem">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_SecurityConstraints.classificationSystem</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:classificationSystem/gco:CharacterString"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:handlingDescription">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_SecurityConstraints.handlingDescription</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:handlingDescription/gco:CharacterString"/>
        </dt>
      </xsl:if>
    </dl>
  </xsl:template>
  <xsl:template name="Distribution_Info">
    <div class="iso_section_title">
      <xsl:call-template name="get_property">
        <xsl:with-param name="key">catalog.iso19139.MD_Distribution</xsl:with-param>
      </xsl:call-template>
    </div>
    <dl>
      <xsl:for-each select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Distribution.distributionFormat</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="."/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Distribution.transferOptions</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="gmd:CI_OnlineResource"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Distribution.distributor</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="."/>
        </dt>
      </xsl:for-each>
    </dl>
  </xsl:template>
  
  <xsl:template name="Data_Quality_Info">
    <div class="iso_section_title">
      <xsl:call-template name="get_property">
        <xsl:with-param name="key">catalog.iso19139.MD_Metadata.dataQualityInfo</xsl:with-param>
      </xsl:call-template>
    </div>
    <dl>
      <xsl:if test="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.DQ_DataQuality.scope</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.MD_ScopeCode.<xsl:value-of select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue"/>
            </xsl:with-param>
          </xsl:call-template>
        </dt>
      </xsl:if>
      <xsl:for-each select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/*/gmd:result">
        <xsl:if test="gmd:DQ_ConformanceResult">
          <dt>
            <em>
              <xsl:call-template name="get_property">
                <xsl:with-param name="key">catalog.iso19139.DQ_ConformanceResult</xsl:with-param>
              </xsl:call-template>: </em>
          </dt>
          <dt>
            <xsl:apply-templates select="gmd:DQ_ConformanceResult"/>
          </dt>
        </xsl:if>
      </xsl:for-each>
      <xsl:for-each select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.DQ_DataQuality.lineage</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString"/>
        </dt>
      </xsl:for-each>
    </dl>
  </xsl:template>
  
  
  <xsl:template match="gmd:DQ_ConformanceResult">
    <dl>
      <xsl:if test="gmd:pass">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.DQ_ConformanceResult.pass.Boolean</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:pass"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:explanation">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.DQ_ConformanceResult.explanation</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:explanation/gco:CharacterString"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:specification">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.DQ_ConformanceResult.specification</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="gmd:specification/gmd:CI_Citation"/>
        </dt>
      </xsl:if>
    </dl>
  </xsl:template>
  
  
  <xsl:template match="/*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor">
    <dl>
      <xsl:for-each select="gmd:MD_Distributor/gmd:distributorFormat">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Distributor.distributorFormat</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="."/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:MD_Distributor/gmd:distributorTransferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_OnlineResource</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="gmd:CI_OnlineResource"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:MD_Distributor/gmd:distributorContact">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Distributor.distributorContact</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="gmd:CI_ResponsibleParty"/>
        </dt>
      </xsl:for-each>
    </dl>
  </xsl:template>
  
  
  <xsl:template match="gmd:CI_OnlineResource | srv:connectPoint/gmd:CI_OnlineResource">
    <dl>
      <xsl:if test="gmd:linkage/gmd:URL">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_OnlineResource.linkage</xsl:with-param>
            </xsl:call-template>: </em>
          <a>
            <xsl:attribute name="href"><xsl:value-of select="gmd:linkage/gmd:URL"/></xsl:attribute>
            <xsl:value-of select="gmd:linkage/gmd:URL"/>
          </a>
        </dt>
      </xsl:if>
      <xsl:for-each select="gmd:protocol">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_OnlineResource.protocol</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:protocol"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:applicationProfile">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_OnlineResource.applicationProfile</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:applicationProfile"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:name">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_OnlineResource.name</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:name"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:description">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_OnlineResource.description</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:description"/>
        </dt>
      </xsl:for-each>
      <xsl:for-each select="gmd:function">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.CI_OnlineResource.function</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.CI_OnLineFunctionCode.<xsl:value-of select="gmd:CI_OnLineFunctionCode/@codeListValue"/>
            </xsl:with-param>
          </xsl:call-template>
        </dt>
      </xsl:for-each>
    </dl>
  </xsl:template>
  
  
  <xsl:template match="*[self::gmd:MD_Metadata or self::gmi:MI_Metadata]/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat">
    <dl>
      <xsl:if test="gmd:MD_Format/gmd:name">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Format.name</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:MD_Format/gmd:name"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:MD_Format/gmd:version">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Format.version</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:MD_Format/gmd:version"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:MD_Format/gmd:amendmentNumber">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Format.amendmentNumber</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:MD_Format/gmd:amendmentNumber"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:MD_Format/gmd:specification">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Format.specification</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:MD_Format/gmd:specification"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:MD_Format/gmd:fileDecompressionTechnique">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Format.fileDecompressionTechnique</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:MD_Format/gmd:fileDecompressionTechnique"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:MD_Format/gmd:formatDistributor">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_Format.formatDistributor</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:apply-templates select="gmd:MD_Format/gmd:formatDistributor"/>
        </dt>
      </xsl:if>
    </dl>
  </xsl:template>
  
  
  <xsl:template match="srv:containsOperations/srv:SV_OperationMetadata">
    <dl>
      <xsl:if test="srv:operationName/gco:CharacterString">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.SV_OperationMetadata.operationName</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="srv:operationName/gco:CharacterString"/>
        </dt>
      </xsl:if>
      <xsl:for-each select="srv:DCP">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.SV_PlatformSpecificServiceSpecification.DCP</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.DCPList.<xsl:value-of select="srv:DCPList/@codeListValue"/>
            </xsl:with-param>
          </xsl:call-template>
        </dt>
      </xsl:for-each>
      <xsl:if test="srv:operationDescription/gco:CharacterString">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.SV_OperationMetadata.operationDescription</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="srv:operationDescription/gco:CharacterString"/>
        </dt>
      </xsl:if>
      <xsl:for-each select="srv:connectPoint/gmd:CI_OnlineResource">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.SV_Operation</xsl:with-param>
            </xsl:call-template>: </em>
        </dt>
        <dt>
          <xsl:apply-templates select="."/>
        </dt>
      </xsl:for-each>
    </dl>
  </xsl:template>
  
  <xsl:template match="gmd:graphicOverview/gmd:MD_BrowseGraphic">
    <dl>
      <xsl:if test="gmd:fileName">
        <dt>
          <img>
            <xsl:attribute name="src"><xsl:value-of select="gmd:fileName/gco:CharacterString"/></xsl:attribute>
            <xsl:attribute name="alt"><xsl:value-of select="gmd:fileName/gco:CharacterString"/></xsl:attribute>
          </img>
        </dt>
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_BrowseGraphic</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:fileName/gco:CharacterString"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:fileType">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_BrowseGraphic.fileType</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:fileType"/>
        </dt>
      </xsl:if>
      <xsl:if test="gmd:fileDescription">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139.MD_BrowseGraphic.fileDescription</xsl:with-param>
            </xsl:call-template>: </em>
          <xsl:value-of select="gmd:fileDescription"/>
        </dt>
      </xsl:if>
    </dl>
  </xsl:template>
  
  
  <!-- ISO19115-2 -->
  <!-- Acquisition Information -->
  <xsl:template name="Acquisition_Info">
    <div class="iso_section_title">
      <xsl:call-template name="get_property">
        <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.section.acquisition</xsl:with-param>
      </xsl:call-template>
    </div>
    <dl>
      <xsl:for-each select="/gmi:MI_Metadata/gmi:acquisitionInformation/gmi:MI_AcquisitionInformation">
        <dt>
          <em>
            <xsl:call-template name="get_property">
              <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition</xsl:with-param>
            </xsl:call-template> (<xsl:value-of select="position()"/>): </em>
        </dt>
        <dl>
        <dt>
          <!-- gmi:acquisitionRequirement -->
          <em><xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.section.acuisition.requirement</xsl:with-param>
          </xsl:call-template></em>
          <dl>            
          <xsl:for-each select="gmi:acquisitionRequirement/gmi:MI_Requirement">
            <dt>
              <em>
                <xsl:call-template name="get_property">
                  <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.section.acuisition.requirement</xsl:with-param>
                </xsl:call-template> (<xsl:value-of select="position()"/>): </em>
              <dl>
                <dt>
                  <xsl:apply-templates select="."/>
                </dt>
              </dl>
            </dt>
          </xsl:for-each>
          </dl>

          <!-- gmi:objective -->
          <em><xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.section.acuisition.objective</xsl:with-param>
          </xsl:call-template></em>
          <dl>            
          <xsl:for-each select="gmi:objective/gmi:MI_Objective">
            <dt>
              <em>
                <xsl:call-template name="get_property">
                  <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.section.acuisition.objective</xsl:with-param>
                </xsl:call-template> (<xsl:value-of select="position()"/>): </em>
              <dl>
                <dt>
                  <xsl:apply-templates select="."/>
                </dt>
              </dl>
            </dt>
          </xsl:for-each>
          </dl>

          <!-- gmi:instrument -->
          <em><xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.section.acuisition.instrument</xsl:with-param>
          </xsl:call-template></em>
          <dl>            
          <xsl:for-each select="gmi:objective/gmi:MI_Objective">
            <dt>
              <em>
                <xsl:call-template name="get_property">
                  <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.section.acuisition.instrument</xsl:with-param>
                </xsl:call-template> (<xsl:value-of select="position()"/>): </em>
              <dl>
                <dt>
                  <xsl:apply-templates select="."/>
                </dt>
              </dl>
            </dt>
          </xsl:for-each>
          </dl>

          <!-- gmi:acquisitionPlan -->
          <em><xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.section.acuisition.plan</xsl:with-param>
          </xsl:call-template></em>
          <dl>            
          <xsl:for-each select="gmi:acquisitionPlan/gmi:MI_Plan">
            <dt>
              <em>
                <xsl:call-template name="get_property">
                  <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.section.acuisition.plan</xsl:with-param>
                </xsl:call-template> (<xsl:value-of select="position()"/>): </em>
              <dl>
                <dt>
                  <xsl:apply-templates select="."/>
                </dt>
              </dl>
            </dt>
          </xsl:for-each>
          </dl>

          <!-- gmi:operation -->
          <em><xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.section.acuisition.operation</xsl:with-param>
          </xsl:call-template></em>
          <dl>            
          <xsl:for-each select="gmi:operation/gmi:MI_Operation">
            <dt>
              <em>
                <xsl:call-template name="get_property">
                  <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.section.acuisition.operation</xsl:with-param>
                </xsl:call-template> (<xsl:value-of select="position()"/>): </em>
              <dl>
                <dt>
                  <xsl:apply-templates select="."/>
                </dt>
              </dl>
            </dt>
          </xsl:for-each>
          </dl>

          <!-- gmi:platform -->
          <em><xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.section.acuisition.platform</xsl:with-param>
          </xsl:call-template></em>
          <dl>            
          <xsl:for-each select="gmi:platform/gmi:MI_Platform">
            <dt>
              <em>
                <xsl:call-template name="get_property">
                  <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.section.acuisition.platform</xsl:with-param>
                </xsl:call-template> (<xsl:value-of select="position()"/>): </em>
              <dl>
                <dt>
                  <xsl:apply-templates select="."/>
                </dt>
              </dl>
            </dt>
          </xsl:for-each>
          </dl>
        </dt>
      </dl>

      </xsl:for-each>
    </dl>
  </xsl:template>
  
 
  <xsl:template match="gmd:RS_Identifier | gmi:identifier/gmd:MD_Identifier">
      <dt>
        <em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139.XTN_Identification.citation.RS_Identifier</xsl:with-param>
          </xsl:call-template>: </em>
        <xsl:apply-templates select="gmd:RS_Identifier/gmd:code/gco:CharacterString"/>

        <dl>
        <xsl:if test="gmd:code/gco:CharacterString">
          <dt>
            <em>
              <xsl:call-template name="get_property">
                <xsl:with-param name="key">catalog.iso19139.RS_Identifier.code</xsl:with-param>
              </xsl:call-template>: </em>
              <xsl:value-of select="gmd:code/gco:CharacterString"/>
          </dt>
        </xsl:if>
        <xsl:if test="gmd:codeSpace/gco:CharacterString">
          <dt>
            <em>
              <xsl:call-template name="get_property">
                <xsl:with-param name="key">catalog.iso19139.RS_Identifier.codeSpace</xsl:with-param>
              </xsl:call-template>: </em>
              <xsl:value-of select="gmd:codeSpace/gco:CharacterString"/>
          </dt>
        </xsl:if>
        </dl>
      </dt>
  </xsl:template>

  <xsl:template match="gmi:requestor">
    <dt>
      <em>
        <xsl:call-template name="get_property">
          <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.requirement.requestor</xsl:with-param>
        </xsl:call-template></em>
        <xsl:apply-templates select="gmd:CI_ResponsibleParty"/>
    </dt>
  </xsl:template>

  <xsl:template match="gmi:recipient">
    <dt>
      <em>
        <xsl:call-template name="get_property">
          <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.requirement.recipient</xsl:with-param>
        </xsl:call-template></em>
        <xsl:apply-templates select="gmd:CI_ResponsibleParty"/>
    </dt>
  </xsl:template>

  <xsl:template match="gmi:priority">
    <xsl:if test="gmi:MI_PriorityCode">
      <dt>
        <em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.requirement.priority</xsl:with-param>
          </xsl:call-template>: </em>
        <xsl:call-template name="get_property">
          <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.requirement.priorityCode.<xsl:value-of select="gmi:MI_PriorityCode/@codeListValue"/>
          </xsl:with-param>
        </xsl:call-template>
      </dt>
    </xsl:if>
  </xsl:template>
  
  
  <xsl:template match="gmi:requestedDate">
    <xsl:if test="gmi:MI_RequestedDate">
      <dt>
        <em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.requirement.requestedDate</xsl:with-param>
          </xsl:call-template>: </em>
          
        <dl>
        <xsl:if test="gmi:MI_RequestedDate/gmi:requestedDateOfCollection/gco:DateTime">
          <dt>
            <em>
              <xsl:call-template name="get_property">
                <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.requirement.requestedDate.requestedDateOfCollection</xsl:with-param>
              </xsl:call-template>: </em>
              <xsl:value-of select="gmi:MI_RequestedDate/gmi:requestedDateOfCollection/gco:DateTime"/>
          </dt>
        </xsl:if>
        <xsl:if test="gmi:MI_RequestedDate/gmi:latestAcceptableDate/gco:DateTime">
          <dt>
            <em>
              <xsl:call-template name="get_property">
                <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.requirement.requestedDate.latestAcceptableDate</xsl:with-param>
              </xsl:call-template>: </em>
              <xsl:value-of select="gmi:MI_RequestedDate/gmi:latestAcceptableDate/gco:DateTime"/>
          </dt>
        </xsl:if>
        </dl>
      </dt>
    </xsl:if>
  </xsl:template>  
  
  
  <xsl:template match="gmi:expiryDate">
      <dt>
        <em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.requirement.expiryDate</xsl:with-param>
          </xsl:call-template>: </em>
          
          <xsl:value-of select="gco:DateTime"/>
      </dt>
  </xsl:template> 
  
  <!--  Objective Type Code -->
  <xsl:template match="gmi:type">
    <xsl:if test="gmi:ObjectiveTypeCode ">
      <dt>
        <em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.objective.type</xsl:with-param>
          </xsl:call-template>: </em>
        <xsl:call-template name="get_property">
          <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.objective<xsl:value-of select="gmi:ObjectiveTypeCode/@codeListValue"/>
          </xsl:with-param>
        </xsl:call-template>
      </dt>
    </xsl:if>
  </xsl:template>
 
  <xsl:template match="gmi:MI_Operation/gmi:description | gmi:MI_Platform/gmi:description">
      <dt>
        <em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acuisition.operation.description</xsl:with-param>
          </xsl:call-template>: </em>
          
          <xsl:value-of select="gco:CharacterString"/>
      </dt>
  </xsl:template>
 
  <xsl:template match="gmi:MI_Instrument/gmi:description">
      <dt>
        <em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acuisition.instrument.description</xsl:with-param>
          </xsl:call-template>: </em>
          
          <xsl:value-of select="gco:CharacterString"/>
      </dt>
  </xsl:template>


  <xsl:template match="gmi:trigger">
    <xsl:if test="gmi:MI_TriggerCode">
      <dt>
        <em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.event.trigger</xsl:with-param>
          </xsl:call-template>: </em>
        <xsl:call-template name="get_property">
          <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.event.triggerCode.<xsl:value-of select="gmi:MI_TriggerCode/@codeListValue"/>
          </xsl:with-param>
        </xsl:call-template>
      </dt>
    </xsl:if>
  </xsl:template>

  <xsl:template match="gmi:context">
    <xsl:if test="gmi:MI_ContextCode">
      <dt>
        <em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.event.context</xsl:with-param>
          </xsl:call-template>: </em>
        <xsl:call-template name="get_property">
          <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.event.contextCode.<xsl:value-of select="gmi:MI_ContextCode/@codeListValue"/>
          </xsl:with-param>
        </xsl:call-template>
      </dt>
    </xsl:if>
  </xsl:template>

  <xsl:template match="gmi:sequence">
    <xsl:if test="gmi:MI_SequenceCode">
      <dt>
        <em>
          <xsl:call-template name="get_property">
            <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.event.sequence</xsl:with-param>
          </xsl:call-template>: </em>
        <xsl:call-template name="get_property">
          <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.event.sequenceCode.<xsl:value-of select="gmi:MI_SequenceCode/@codeListValue"/>
          </xsl:with-param>
        </xsl:call-template>
      </dt>
    </xsl:if>
  </xsl:template>

  <xsl:template match="gmi:function">
    <dt>
      <em>
        <xsl:call-template name="get_property">
          <xsl:with-param name="key">catalog.iso19139-2.MI_Metadata.acquisition.objective.function</xsl:with-param>
        </xsl:call-template>: </em>
        
        <xsl:value-of select="gco:CharacterString"/>
    </dt>
  </xsl:template>

  <xsl:template match="gmi:status">
    <xsl:if test="gmd:MD_ProgressCode">

    <dt>
      <em>
        <xsl:call-template name="get_property">
          <xsl:with-param name="key">catalog.iso19139.MD_ProgressCode</xsl:with-param>
        </xsl:call-template>: </em>
      <xsl:call-template name="get_property">
        <xsl:with-param name="key">catalog.iso19139.MD_ProgressCode.<xsl:value-of select="gmd:MD_ProgressCode/@codeListValue"/>
        </xsl:with-param>
      </xsl:call-template>
    </dt>
    <!-- this is here to get the contact info to properly show up after the status info. not sure why this is needed -->
    <dt></dt>
    </xsl:if>
  </xsl:template>

  
  <!--                     -->
  <!--   Properties        -->
  <!--                     -->
  <xsl:template name="get_property">
    <xsl:param name="key"/>
    <!-- Converted parameters from gpt.properties -->
    <xsl:choose>
      <xsl:when test=' $key = &quot;catalog.iso19139.gco.CodeListValue.codeList&quot; '>i18n.catalog.iso19139.gco.CodeListValue.codeList</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.gco.CodeListValue.codeListValue&quot; '>i18n.catalog.iso19139.gco.CodeListValue.codeListValue</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.gco.CodeListValue.codeSpace&quot; '>i18n.catalog.iso19139.gco.CodeListValue.codeSpace</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.gco.ObjectIdentification.id&quot; '>i18n.catalog.iso19139.gco.ObjectIdentification.id</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.gco.ObjectIdentification.uuid&quot; '>i18n.catalog.iso19139.gco.ObjectIdentification.uuid</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.gco.ObjectReference.uuidref&quot; '>i18n.catalog.iso19139.gco.ObjectReference.uuidref</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.MD_Identification&quot; '>i18n.catalog.iso19139.MD_Metadata.MD_Identification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.MD_DataIdentification&quot; '>i18n.catalog.iso19139.MD_Metadata.MD_DataIdentification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.MD_ServiceIdentification&quot; '>i18n.catalog.iso19139.MD_Metadata.MD_ServiceIdentification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.MD_BrowseGraphic&quot; '>i18n.catalog.iso19139.MD_Metadata.MD_BrowseGraphic</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.Multiplicity&quot; '>i18n.catalog.iso19139.Multiplicity</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.Multiplicity.range&quot; '>i18n.catalog.iso19139.Multiplicity.range</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MultiplicityRange&quot; '>i18n.catalog.iso19139.MultiplicityRange</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MultiplicityRange.lower&quot; '>i18n.catalog.iso19139.MultiplicityRange.lower</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MultiplicityRange.upper&quot; '>i18n.catalog.iso19139.MultiplicityRange.upper</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.Length&quot; '>i18n.catalog.iso19139.Length</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.Length.km&quot; '>i18n.catalog.iso19139.Length.km</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.Length.m&quot; '>i18n.catalog.iso19139.Length.m</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.Length.mi&quot; '>i18n.catalog.iso19139.Length.mi</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.Length.ft&quot; '>i18n.catalog.iso19139.Length.ft</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.Length.uom&quot; '>i18n.catalog.iso19139.Length.uom</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ApplicationSchemaInformation&quot; '>i18n.catalog.iso19139.MD_ApplicationSchemaInformation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ApplicationSchemaInformation.name&quot; '>i18n.catalog.iso19139.MD_ApplicationSchemaInformation.name</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ApplicationSchemaInformation.schemaLanguage&quot; '>i18n.catalog.iso19139.MD_ApplicationSchemaInformation.schemaLanguage</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ApplicationSchemaInformation.constraintLanguage&quot; '>i18n.catalog.iso19139.MD_ApplicationSchemaInformation.constraintLanguage</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ApplicationSchemaInformation.schemaAscii&quot; '>i18n.catalog.iso19139.MD_ApplicationSchemaInformation.schemaAscii</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ApplicationSchemaInformation.graphicsFile&quot; '>i18n.catalog.iso19139.MD_ApplicationSchemaInformation.graphicsFile</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ApplicationSchemaInformation.softwareDevelopmentFile&quot; '>i18n.catalog.iso19139.MD_ApplicationSchemaInformation.softwareDevelopmentFile</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ApplicationSchemaInformation.softwareDevelopmentFileFormat&quot; '>i18n.catalog.iso19139.MD_ApplicationSchemaInformation.softwareDevelopmentFileFormat</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_ResponsibleParty&quot; '>i18n.catalog.iso19139.CI_ResponsibleParty</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_ResponsibleParty.individualName&quot; '>i18n.catalog.iso19139.CI_ResponsibleParty.individualName</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_ResponsibleParty.organisationName&quot; '>i18n.catalog.iso19139.CI_ResponsibleParty.organisationName</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_ResponsibleParty.positionName&quot; '>i18n.catalog.iso19139.CI_ResponsibleParty.positionName</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_ResponsibleParty.contactInfo&quot; '>i18n.catalog.iso19139.CI_ResponsibleParty.contactInfo</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_ResponsibleParty.role&quot; '>i18n.catalog.iso19139.CI_ResponsibleParty.role</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Contact&quot; '>i18n.catalog.iso19139.CI_Contact</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Contact.phone&quot; '>i18n.catalog.iso19139.CI_Contact.phone</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Contact.address&quot; '>i18n.catalog.iso19139.CI_Contact.address</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Contact.onlineResource&quot; '>i18n.catalog.iso19139.CI_Contact.onlineResource</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Contact.hoursOfService&quot; '>i18n.catalog.iso19139.CI_Contact.hoursOfService</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Contact.contactInstructions&quot; '>i18n.catalog.iso19139.CI_Contact.contactInstructions</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Telephone&quot; '>i18n.catalog.iso19139.CI_Telephone</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Telephone.voice&quot; '>i18n.catalog.iso19139.CI_Telephone.voice</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Telephone.facsimile&quot; '>i18n.catalog.iso19139.CI_Telephone.facsimile</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Address&quot; '>i18n.catalog.iso19139.CI_Address</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Address.deliveryPoint&quot; '>i18n.catalog.iso19139.CI_Address.deliveryPoint</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Address.city&quot; '>i18n.catalog.iso19139.CI_Address.city</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Address.administrativeArea&quot; '>i18n.catalog.iso19139.CI_Address.administrativeArea</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Address.postalCode&quot; '>i18n.catalog.iso19139.CI_Address.postalCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Address.country&quot; '>i18n.catalog.iso19139.CI_Address.country</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Address.electronicMailAddress&quot; '>i18n.catalog.iso19139.CI_Address.electronicMailAddress</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_OnlineResource&quot; '>i18n.catalog.iso19139.CI_OnlineResource</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_OnlineResource.linkage&quot; '>i18n.catalog.iso19139.CI_OnlineResource.linkage</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_OnlineResource.protocol&quot; '>i18n.catalog.iso19139.CI_OnlineResource.protocol</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_OnlineResource.applicationProfile&quot; '>i18n.catalog.iso19139.CI_OnlineResource.applicationProfile</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_OnlineResource.name&quot; '>i18n.catalog.iso19139.CI_OnlineResource.name</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_OnlineResource.description&quot; '>i18n.catalog.iso19139.CI_OnlineResource.description</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_OnlineResource.function&quot; '>i18n.catalog.iso19139.CI_OnlineResource.function</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_OnLineFunctionCode&quot; '>i18n.catalog.iso19139.CI_OnLineFunctionCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_OnLineFunctionCode.caption&quot; '>i18n.catalog.iso19139.CI_OnLineFunctionCode.caption</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_OnLineFunctionCode.download&quot; '>i18n.catalog.iso19139.CI_OnLineFunctionCode.download</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_OnLineFunctionCode.information&quot; '>i18n.catalog.iso19139.CI_OnLineFunctionCode.information</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_OnLineFunctionCode.offlineAccess&quot; '>i18n.catalog.iso19139.CI_OnLineFunctionCode.offlineAccess</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_OnLineFunctionCode.order&quot; '>i18n.catalog.iso19139.CI_OnLineFunctionCode.order</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_OnLineFunctionCode.search&quot; '>i18n.catalog.iso19139.CI_OnLineFunctionCode.search</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_RoleCode&quot; '>i18n.catalog.iso19139.CI_RoleCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_RoleCode.caption&quot; '>i18n.catalog.iso19139.CI_RoleCode.caption</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_RoleCode.resourceProvider&quot; '>i18n.catalog.iso19139.CI_RoleCode.resourceProvider</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_RoleCode.custodian&quot; '>i18n.catalog.iso19139.CI_RoleCode.custodian</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_RoleCode.owner&quot; '>i18n.catalog.iso19139.CI_RoleCode.owner</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_RoleCode.user&quot; '>i18n.catalog.iso19139.CI_RoleCode.user</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_RoleCode.distributor&quot; '>i18n.catalog.iso19139.CI_RoleCode.distributor</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_RoleCode.originator&quot; '>i18n.catalog.iso19139.CI_RoleCode.originator</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_RoleCode.pointOfContact&quot; '>i18n.catalog.iso19139.CI_RoleCode.pointOfContact</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_RoleCode.principalInvestigator&quot; '>i18n.catalog.iso19139.CI_RoleCode.principalInvestigator</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_RoleCode.processor&quot; '>i18n.catalog.iso19139.CI_RoleCode.processor</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_RoleCode.publisher&quot; '>i18n.catalog.iso19139.CI_RoleCode.publisher</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_RoleCode.author&quot; '>i18n.catalog.iso19139.CI_RoleCode.author</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Date&quot; '>i18n.catalog.iso19139.CI_Date</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Date.date&quot; '>i18n.catalog.iso19139.CI_Date.date</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Date.dateType&quot; '>i18n.catalog.iso19139.CI_Date.dateType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_DateTypeCode&quot; '>i18n.catalog.iso19139.CI_DateTypeCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_DateTypeCode.creation&quot; '>i18n.catalog.iso19139.CI_DateTypeCode.creation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_DateTypeCode.publication&quot; '>i18n.catalog.iso19139.CI_DateTypeCode.publication</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_DateTypeCode.revision&quot; '>i18n.catalog.iso19139.CI_DateTypeCode.revision</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_PresentationFormCode&quot; '>i18n.catalog.iso19139.CI_PresentationFormCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_PresentationFormCode.documentDigital&quot; '>i18n.catalog.iso19139.CI_PresentationFormCode.documentDigital</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_PresentationFormCode.documentHardcopy&quot; '>i18n.catalog.iso19139.CI_PresentationFormCode.documentHardcopy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_PresentationFormCode.imageDigital&quot; '>i18n.catalog.iso19139.CI_PresentationFormCode.imageDigital</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_PresentationFormCode.imageHardcopy&quot; '>i18n.catalog.iso19139.CI_PresentationFormCode.imageHardcopy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_PresentationFormCode.mapDigital&quot; '>i18n.catalog.iso19139.CI_PresentationFormCode.mapDigital</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_PresentationFormCode.mapHardcopy&quot; '>i18n.catalog.iso19139.CI_PresentationFormCode.mapHardcopy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_PresentationFormCode.modelDigital&quot; '>i18n.catalog.iso19139.CI_PresentationFormCode.modelDigital</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_PresentationFormCode.modelHardcopy&quot; '>i18n.catalog.iso19139.CI_PresentationFormCode.modelHardcopy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_PresentationFormCode.profileDigital&quot; '>i18n.catalog.iso19139.CI_PresentationFormCode.profileDigital</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_PresentationFormCode.profileHardcopy&quot; '>i18n.catalog.iso19139.CI_PresentationFormCode.profileHardcopy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_PresentationFormCode.tableDigital&quot; '>i18n.catalog.iso19139.CI_PresentationFormCode.tableDigital</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_PresentationFormCode.tableHardcopy&quot; '>i18n.catalog.iso19139.CI_PresentationFormCode.tableHardcopy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_PresentationFormCode.videoDigital&quot; '>i18n.catalog.iso19139.CI_PresentationFormCode.videoDigital</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_PresentationFormCode.videoHardcopy&quot; '>i18n.catalog.iso19139.CI_PresentationFormCode.videoHardcopy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Series&quot; '>i18n.catalog.iso19139.CI_Series</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Series.name&quot; '>i18n.catalog.iso19139.CI_Series.name</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Series.issueIdentification&quot; '>i18n.catalog.iso19139.CI_Series.issueIdentification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Series.page&quot; '>i18n.catalog.iso19139.CI_Series.page</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Citation&quot; '>i18n.catalog.iso19139.CI_Citation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Citation.title&quot; '>i18n.catalog.iso19139.CI_Citation.title</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Citation.alternateTitle&quot; '>i18n.catalog.iso19139.CI_Citation.alternateTitle</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Citation.date&quot; '>i18n.catalog.iso19139.CI_Citation.date</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Citation.edition&quot; '>i18n.catalog.iso19139.CI_Citation.edition</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Citation.editionDate&quot; '>i18n.catalog.iso19139.CI_Citation.editionDate</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Citation.identifier&quot; '>i18n.catalog.iso19139.CI_Citation.identifier</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Citation.citedResponsibleParty&quot; '>i18n.catalog.iso19139.CI_Citation.citedResponsibleParty</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Citation.presentationForm&quot; '>i18n.catalog.iso19139.CI_Citation.presentationForm</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Citation.series&quot; '>i18n.catalog.iso19139.CI_Citation.series</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Citation.otherCitationDetails&quot; '>i18n.catalog.iso19139.CI_Citation.otherCitationDetails</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Citation.ISBN&quot; '>i18n.catalog.iso19139.CI_Citation.ISBN</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Citation.ISSN&quot; '>i18n.catalog.iso19139.CI_Citation.ISSN</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Citation.specification.title&quot; '>i18n.catalog.iso19139.CI_Citation.specification.title</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.CI_Citation.specification.date&quot; '>i18n.catalog.iso19139.CI_Citation.specification.date</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Constraints&quot; '>i18n.catalog.iso19139.MD_Constraints</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Constraints.useLimitation&quot; '>i18n.catalog.iso19139.MD_Constraints.useLimitation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_LegalConstraints&quot; '>i18n.catalog.iso19139.MD_LegalConstraints</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_LegalConstraints.accessConstraints&quot; '>i18n.catalog.iso19139.MD_LegalConstraints.accessConstraints</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_LegalConstraints.useConstraints&quot; '>i18n.catalog.iso19139.MD_LegalConstraints.useConstraints</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_LegalConstraints.otherConstraints&quot; '>i18n.catalog.iso19139.MD_LegalConstraints.otherConstraints</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_SecurityConstraints&quot; '>i18n.catalog.iso19139.MD_SecurityConstraints</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_SecurityConstraints.classification&quot; '>i18n.catalog.iso19139.MD_SecurityConstraints.classification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_SecurityConstraints.userNote&quot; '>i18n.catalog.iso19139.MD_SecurityConstraints.userNote</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_SecurityConstraints.classificationSystem&quot; '>i18n.catalog.iso19139.MD_SecurityConstraints.classificationSystem</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_SecurityConstraints.handlingDescription&quot; '>i18n.catalog.iso19139.MD_SecurityConstraints.handlingDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ClassificationCode&quot; '>i18n.catalog.iso19139.MD_ClassificationCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ClassificationCode.unclassified&quot; '>i18n.catalog.iso19139.MD_ClassificationCode.unclassified</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ClassificationCode.restricted&quot; '>i18n.catalog.iso19139.MD_ClassificationCode.restricted</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ClassificationCode.confidential&quot; '>i18n.catalog.iso19139.MD_ClassificationCode.confidential</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ClassificationCode.secret&quot; '>i18n.catalog.iso19139.MD_ClassificationCode.secret</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ClassificationCode.topSecret&quot; '>i18n.catalog.iso19139.MD_ClassificationCode.topSecret</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_RestrictionCode&quot; '>i18n.catalog.iso19139.MD_RestrictionCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_RestrictionCode.copyright&quot; '>i18n.catalog.iso19139.MD_RestrictionCode.copyright</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_RestrictionCode.patent&quot; '>i18n.catalog.iso19139.MD_RestrictionCode.patent</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_RestrictionCode.patentPending&quot; '>i18n.catalog.iso19139.MD_RestrictionCode.patentPending</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_RestrictionCode.trademark&quot; '>i18n.catalog.iso19139.MD_RestrictionCode.trademark</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_RestrictionCode.license&quot; '>i18n.catalog.iso19139.MD_RestrictionCode.license</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_RestrictionCode.intellectualPropertyRights&quot; '>i18n.catalog.iso19139.MD_RestrictionCode.intellectualPropertyRights</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_RestrictionCode.restricted&quot; '>i18n.catalog.iso19139.MD_RestrictionCode.restricted</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_RestrictionCode.otherRestrictions&quot; '>i18n.catalog.iso19139.MD_RestrictionCode.otherRestrictions</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DigitalTransferOptions&quot; '>i18n.catalog.iso19139.MD_DigitalTransferOptions</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DigitalTransferOptions.unitsOfDistribution&quot; '>i18n.catalog.iso19139.MD_DigitalTransferOptions.unitsOfDistribution</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DigitalTransferOptions.transferSize&quot; '>i18n.catalog.iso19139.MD_DigitalTransferOptions.transferSize</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DigitalTransferOptions.onLine&quot; '>i18n.catalog.iso19139.MD_DigitalTransferOptions.onLine</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DigitalTransferOptions.offLine&quot; '>i18n.catalog.iso19139.MD_DigitalTransferOptions.offLine</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Distribution&quot; '>i18n.catalog.iso19139.MD_Distribution</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Distribution.distributionFormat&quot; '>i18n.catalog.iso19139.MD_Distribution.distributionFormat</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Distribution.distributor&quot; '>i18n.catalog.iso19139.MD_Distribution.distributor</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Distribution.transferOptions&quot; '>i18n.catalog.iso19139.MD_Distribution.transferOptions</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Distributor&quot; '>i18n.catalog.iso19139.MD_Distributor</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Distributor.distributorContact&quot; '>i18n.catalog.iso19139.MD_Distributor.distributorContact</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Distributor.distributionOrderProcess&quot; '>i18n.catalog.iso19139.MD_Distributor.distributionOrderProcess</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Distributor.distributorFormat&quot; '>i18n.catalog.iso19139.MD_Distributor.distributorFormat</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Distributor.distributorTransferOptions&quot; '>i18n.catalog.iso19139.MD_Distributor.distributorTransferOptions</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Format&quot; '>i18n.catalog.iso19139.MD_Format</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Format.name&quot; '>i18n.catalog.iso19139.MD_Format.name</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Format.version&quot; '>i18n.catalog.iso19139.MD_Format.version</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Format.amendmentNumber&quot; '>i18n.catalog.iso19139.MD_Format.amendmentNumber</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Format.specification&quot; '>i18n.catalog.iso19139.MD_Format.specification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Format.fileDecompressionTechnique&quot; '>i18n.catalog.iso19139.MD_Format.fileDecompressionTechnique</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Format.formatDistributor&quot; '>i18n.catalog.iso19139.MD_Format.formatDistributor</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Medium&quot; '>i18n.catalog.iso19139.MD_Medium</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Medium.name&quot; '>i18n.catalog.iso19139.MD_Medium.name</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Medium.density&quot; '>i18n.catalog.iso19139.MD_Medium.density</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Medium.densityUnits&quot; '>i18n.catalog.iso19139.MD_Medium.densityUnits</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Medium.volumes&quot; '>i18n.catalog.iso19139.MD_Medium.volumes</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Medium.mediumFormat&quot; '>i18n.catalog.iso19139.MD_Medium.mediumFormat</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Medium.mediumNote&quot; '>i18n.catalog.iso19139.MD_Medium.mediumNote</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_StandardOrderProcess&quot; '>i18n.catalog.iso19139.MD_StandardOrderProcess</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_StandardOrderProcess.fees&quot; '>i18n.catalog.iso19139.MD_StandardOrderProcess.fees</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_StandardOrderProcess.plannedAvailableDateTime&quot; '>i18n.catalog.iso19139.MD_StandardOrderProcess.plannedAvailableDateTime</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_StandardOrderProcess.orderingInstructions&quot; '>i18n.catalog.iso19139.MD_StandardOrderProcess.orderingInstructions</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_StandardOrderProcess.turnaround&quot; '>i18n.catalog.iso19139.MD_StandardOrderProcess.turnaround</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DistributionUnits&quot; '>i18n.catalog.iso19139.MD_DistributionUnits</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumFormatCode&quot; '>i18n.catalog.iso19139.MD_MediumFormatCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumFormatCode.cpio&quot; '>i18n.catalog.iso19139.MD_MediumFormatCode.cpio</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumFormatCode.tar&quot; '>i18n.catalog.iso19139.MD_MediumFormatCode.tar</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumFormatCode.highSierra&quot; '>i18n.catalog.iso19139.MD_MediumFormatCode.highSierra</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumFormatCode.iso9660&quot; '>i18n.catalog.iso19139.MD_MediumFormatCode.iso9660</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumFormatCode.iso9660RockRidge&quot; '>i18n.catalog.iso19139.MD_MediumFormatCode.iso9660RockRidge</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumFormatCode.iso9660AppleHFS&quot; '>i18n.catalog.iso19139.MD_MediumFormatCode.iso9660AppleHFS</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode&quot; '>i18n.catalog.iso19139.MD_MediumNameCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.cdRom&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.cdRom</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.dvd&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.dvd</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.dvdRom&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.dvdRom</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.3halfInchFloppy&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.3halfInchFloppy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.5quarterInchFloppy&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.5quarterInchFloppy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.7trackTape&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.7trackTape</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.9trackType&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.9trackType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.3480Cartridge&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.3480Cartridge</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.3490Cartridge&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.3490Cartridge</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.3580Cartridge&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.3580Cartridge</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.4mmCartridgeTape&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.4mmCartridgeTape</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.8mmCartridgeTape&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.8mmCartridgeTape</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.1quarterInchCartridgeTape&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.1quarterInchCartridgeTape</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.digitalLinearTape&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.digitalLinearTape</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.onLine&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.onLine</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.satellite&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.satellite</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.telephoneLink&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.telephoneLink</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MediumNameCode.hardcopy&quot; '>i18n.catalog.iso19139.MD_MediumNameCode.hardcopy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractEX_GeographicExtent&quot; '>i18n.catalog.iso19139.AbstractEX_GeographicExtent</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractEX_GeographicExtent.extentTypeCode&quot; '>i18n.catalog.iso19139.AbstractEX_GeographicExtent.extentTypeCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_Extent&quot; '>i18n.catalog.iso19139.EX_Extent</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_Extent.description&quot; '>i18n.catalog.iso19139.EX_Extent.description</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_Extent.geographicElement&quot; '>i18n.catalog.iso19139.EX_Extent.geographicElement</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_Extent.temporalElement&quot; '>i18n.catalog.iso19139.EX_Extent.temporalElement</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_Extent.verticalElement&quot; '>i18n.catalog.iso19139.EX_Extent.verticalElement</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_GeographicExtent&quot; '>i18n.catalog.iso19139.EX_GeographicExtent</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_TemporalExtent&quot; '>i18n.catalog.iso19139.EX_TemporalExtent</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_TemporalExtent.extent&quot; '>i18n.catalog.iso19139.EX_TemporalExtent.extent</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_TemporalExtent.beginPosition&quot; '>i18n.catalog.iso19139.EX_TemporalExtent.beginPosition</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_TemporalExtent.endPosition&quot; '>i18n.catalog.iso19139.EX_TemporalExtent.endPosition</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_VerticalExtent&quot; '>i18n.catalog.iso19139.EX_VerticalExtent</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_VerticalExtent.minimumValue&quot; '>i18n.catalog.iso19139.EX_VerticalExtent.minimumValue</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_VerticalExtent.maximumValue&quot; '>i18n.catalog.iso19139.EX_VerticalExtent.maximumValue</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_VerticalExtent.verticalCRS&quot; '>i18n.catalog.iso19139.EX_VerticalExtent.verticalCRS</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_GeographicBoundingBox&quot; '>i18n.catalog.iso19139.EX_GeographicBoundingBox</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_GeographicBoundingBox.westBoundLongitude&quot; '>i18n.catalog.iso19139.EX_GeographicBoundingBox.westBoundLongitude</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_GeographicBoundingBox.eastBoundLongitude&quot; '>i18n.catalog.iso19139.EX_GeographicBoundingBox.eastBoundLongitude</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_GeographicBoundingBox.southBoundLatitude&quot; '>i18n.catalog.iso19139.EX_GeographicBoundingBox.southBoundLatitude</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_GeographicBoundingBox.northBoundLatitude&quot; '>i18n.catalog.iso19139.EX_GeographicBoundingBox.northBoundLatitude</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractMD_Identification&quot; '>i18n.catalog.iso19139.AbstractMD_Identification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractMD_Identification.citation&quot; '>i18n.catalog.iso19139.AbstractMD_Identification.citation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractMD_Identification.abstract&quot; '>i18n.catalog.iso19139.AbstractMD_Identification.abstract</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractMD_Identification.purpose&quot; '>i18n.catalog.iso19139.AbstractMD_Identification.purpose</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractMD_Identification.credit&quot; '>i18n.catalog.iso19139.AbstractMD_Identification.credit</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractMD_Identification.status&quot; '>i18n.catalog.iso19139.AbstractMD_Identification.status</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractMD_Identification.pointOfContact&quot; '>i18n.catalog.iso19139.AbstractMD_Identification.pointOfContact</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractMD_Identification.resourceMaintenance&quot; '>i18n.catalog.iso19139.AbstractMD_Identification.resourceMaintenance</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractMD_Identification.graphicOverview&quot; '>i18n.catalog.iso19139.AbstractMD_Identification.graphicOverview</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractMD_Identification.resourceFormat&quot; '>i18n.catalog.iso19139.AbstractMD_Identification.resourceFormat</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractMD_Identification.descriptiveKeywords&quot; '>i18n.catalog.iso19139.AbstractMD_Identification.descriptiveKeywords</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractMD_Identification.resourceSpecificUsage&quot; '>i18n.catalog.iso19139.AbstractMD_Identification.resourceSpecificUsage</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractMD_Identification.resourceConstraints&quot; '>i18n.catalog.iso19139.AbstractMD_Identification.resourceConstraints</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractMD_Identification.aggregationInfo&quot; '>i18n.catalog.iso19139.AbstractMD_Identification.aggregationInfo</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_Association&quot; '>i18n.catalog.iso19139.DS_Association</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_AggregateInformation&quot; '>i18n.catalog.iso19139.MD_AggregateInformation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_AggregateInformation.aggregateDataSetName&quot; '>i18n.catalog.iso19139.MD_AggregateInformation.aggregateDataSetName</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_AggregateInformation.aggregateDataSetIdentifier&quot; '>i18n.catalog.iso19139.MD_AggregateInformation.aggregateDataSetIdentifier</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_AggregateInformation.associationType&quot; '>i18n.catalog.iso19139.MD_AggregateInformation.associationType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_AggregateInformation.initiativeType&quot; '>i18n.catalog.iso19139.MD_AggregateInformation.initiativeType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_BrowseGraphic&quot; '>i18n.catalog.iso19139.MD_BrowseGraphic</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_BrowseGraphic.fileName&quot; '>i18n.catalog.iso19139.MD_BrowseGraphic.fileName</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_BrowseGraphic.fileDescription&quot; '>i18n.catalog.iso19139.MD_BrowseGraphic.fileDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_BrowseGraphic.fileType&quot; '>i18n.catalog.iso19139.MD_BrowseGraphic.fileType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DataIdentification&quot; '>i18n.catalog.iso19139.MD_DataIdentification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DataIdentification.spatialRepresentationType&quot; '>i18n.catalog.iso19139.MD_DataIdentification.spatialRepresentationType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DataIdentification.spatialResolution&quot; '>i18n.catalog.iso19139.MD_DataIdentification.spatialResolution</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DataIdentification.language&quot; '>i18n.catalog.iso19139.MD_DataIdentification.language</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DataIdentification.characterSet&quot; '>i18n.catalog.iso19139.MD_DataIdentification.characterSet</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DataIdentification.topicCategory&quot; '>i18n.catalog.iso19139.MD_DataIdentification.topicCategory</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DataIdentification.environmentDescription&quot; '>i18n.catalog.iso19139.MD_DataIdentification.environmentDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DataIdentification.extent&quot; '>i18n.catalog.iso19139.MD_DataIdentification.extent</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DataIdentification.supplementalInformation&quot; '>i18n.catalog.iso19139.MD_DataIdentification.supplementalInformation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Keywords&quot; '>i18n.catalog.iso19139.MD_Keywords</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Keywords.keyword&quot; '>i18n.catalog.iso19139.MD_Keywords.keyword</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Keywords.type&quot; '>i18n.catalog.iso19139.MD_Keywords.type</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Keywords.thesaurusName&quot; '>i18n.catalog.iso19139.MD_Keywords.thesaurusName</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Keywords.keyword.delimited&quot; '>i18n.catalog.iso19139.MD_Keywords.keyword.delimited</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_RepresentativeFraction&quot; '>i18n.catalog.iso19139.MD_RepresentativeFraction</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_RepresentativeFraction.denominator&quot; '>i18n.catalog.iso19139.MD_RepresentativeFraction.denominator</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Resolution&quot; '>i18n.catalog.iso19139.MD_Resolution</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Resolution.equivalentScale&quot; '>i18n.catalog.iso19139.MD_Resolution.equivalentScale</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Resolution.distance&quot; '>i18n.catalog.iso19139.MD_Resolution.distance</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Usage&quot; '>i18n.catalog.iso19139.MD_Usage</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Usage.specificUsage&quot; '>i18n.catalog.iso19139.MD_Usage.specificUsage</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Usage.usageDateTime&quot; '>i18n.catalog.iso19139.MD_Usage.usageDateTime</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Usage.userDeterminedLimitations&quot; '>i18n.catalog.iso19139.MD_Usage.userDeterminedLimitations</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Usage.userContactInfo&quot; '>i18n.catalog.iso19139.MD_Usage.userContactInfo</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_AssociationTypeCode&quot; '>i18n.catalog.iso19139.DS_AssociationTypeCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_AssociationTypeCode.crossReference&quot; '>i18n.catalog.iso19139.DS_AssociationTypeCode.crossReference</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_AssociationTypeCode.largerWorkCitation&quot; '>i18n.catalog.iso19139.DS_AssociationTypeCode.largerWorkCitation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_AssociationTypeCode.partOfSeamlessDatabase&quot; '>i18n.catalog.iso19139.DS_AssociationTypeCode.partOfSeamlessDatabase</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_AssociationTypeCode.source&quot; '>i18n.catalog.iso19139.DS_AssociationTypeCode.source</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_AssociationTypeCode.stereoMate&quot; '>i18n.catalog.iso19139.DS_AssociationTypeCode.stereoMate</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_InitiativeTypeCode&quot; '>i18n.catalog.iso19139.DS_InitiativeTypeCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_InitiativeTypeCode.campaign&quot; '>i18n.catalog.iso19139.DS_InitiativeTypeCode.campaign</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_InitiativeTypeCode.collection&quot; '>i18n.catalog.iso19139.DS_InitiativeTypeCode.collection</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_InitiativeTypeCode.exercise&quot; '>i18n.catalog.iso19139.DS_InitiativeTypeCode.exercise</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_InitiativeTypeCode.experiment&quot; '>i18n.catalog.iso19139.DS_InitiativeTypeCode.experiment</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_InitiativeTypeCode.investigation&quot; '>i18n.catalog.iso19139.DS_InitiativeTypeCode.investigation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_InitiativeTypeCode.mission&quot; '>i18n.catalog.iso19139.DS_InitiativeTypeCode.mission</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_InitiativeTypeCode.sensor&quot; '>i18n.catalog.iso19139.DS_InitiativeTypeCode.sensor</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_InitiativeTypeCode.operation&quot; '>i18n.catalog.iso19139.DS_InitiativeTypeCode.operation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_InitiativeTypeCode.platform&quot; '>i18n.catalog.iso19139.DS_InitiativeTypeCode.platform</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_InitiativeTypeCode.process&quot; '>i18n.catalog.iso19139.DS_InitiativeTypeCode.process</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_InitiativeTypeCode.program&quot; '>i18n.catalog.iso19139.DS_InitiativeTypeCode.program</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_InitiativeTypeCode.project&quot; '>i18n.catalog.iso19139.DS_InitiativeTypeCode.project</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_InitiativeTypeCode.study&quot; '>i18n.catalog.iso19139.DS_InitiativeTypeCode.study</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_InitiativeTypeCode.task&quot; '>i18n.catalog.iso19139.DS_InitiativeTypeCode.task</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_InitiativeTypeCode.trial&quot; '>i18n.catalog.iso19139.DS_InitiativeTypeCode.trial</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.ucs2&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.ucs2</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.ucs2&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.ucs2</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.ucs4&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.ucs4</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.utf7&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.utf7</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.utf8&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.utf8</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.utf16&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.utf16</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.8859part1&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.8859part1</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.8859part2&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.8859part2</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.8859part3&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.8859part3</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.8859part4&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.8859part4</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.8859part5&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.8859part5</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.8859part6&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.8859part6</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.8859part7&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.8859part7</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.8859part8&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.8859part8</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.8859part9&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.8859part9</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.8859part10&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.8859part10</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.8859part11&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.8859part11</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.8859part13&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.8859part13</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.8859part14&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.8859part14</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.8859part15&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.8859part15</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.8859part16&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.8859part16</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.jis&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.jis</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.shiftJIS&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.shiftJIS</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.eucJP&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.eucJP</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.usAscii&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.usAscii</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.ebcdic&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.ebcdic</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.eucKR&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.eucKR</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.big5&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.big5</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CharacterSetCode.GB2312&quot; '>i18n.catalog.iso19139.MD_CharacterSetCode.GB2312</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_KeywordTypeCode&quot; '>i18n.catalog.iso19139.MD_KeywordTypeCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_KeywordTypeCode.discipline&quot; '>i18n.catalog.iso19139.MD_KeywordTypeCode.discipline</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_KeywordTypeCode.place&quot; '>i18n.catalog.iso19139.MD_KeywordTypeCode.place</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_KeywordTypeCode.stratum&quot; '>i18n.catalog.iso19139.MD_KeywordTypeCode.stratum</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_KeywordTypeCode.temporal&quot; '>i18n.catalog.iso19139.MD_KeywordTypeCode.temporal</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_KeywordTypeCode.theme&quot; '>i18n.catalog.iso19139.MD_KeywordTypeCode.theme</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ProgressCode&quot; '>i18n.catalog.iso19139.MD_ProgressCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ProgressCode.completed&quot; '>i18n.catalog.iso19139.MD_ProgressCode.completed</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ProgressCode.historicalArchive&quot; '>i18n.catalog.iso19139.MD_ProgressCode.historicalArchive</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ProgressCode.obsolete&quot; '>i18n.catalog.iso19139.MD_ProgressCode.obsolete</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ProgressCode.onGoing&quot; '>i18n.catalog.iso19139.MD_ProgressCode.onGoing</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ProgressCode.planned&quot; '>i18n.catalog.iso19139.MD_ProgressCode.planned</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ProgressCode.required&quot; '>i18n.catalog.iso19139.MD_ProgressCode.required</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ProgressCode.underDevelopment&quot; '>i18n.catalog.iso19139.MD_ProgressCode.underDevelopment</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_SpatialRepresentationTypeCode&quot; '>i18n.catalog.iso19139.MD_SpatialRepresentationTypeCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_SpatialRepresentationTypeCode.vector&quot; '>i18n.catalog.iso19139.MD_SpatialRepresentationTypeCode.vector</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_SpatialRepresentationTypeCode.grid&quot; '>i18n.catalog.iso19139.MD_SpatialRepresentationTypeCode.grid</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_SpatialRepresentationTypeCode.textTable&quot; '>i18n.catalog.iso19139.MD_SpatialRepresentationTypeCode.textTable</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_SpatialRepresentationTypeCode.tin&quot; '>i18n.catalog.iso19139.MD_SpatialRepresentationTypeCode.tin</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_SpatialRepresentationTypeCode.stereoModel&quot; '>i18n.catalog.iso19139.MD_SpatialRepresentationTypeCode.stereoModel</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_SpatialRepresentationTypeCode.video&quot; '>i18n.catalog.iso19139.MD_SpatialRepresentationTypeCode.video</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.boundaries&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.boundaries</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.farming&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.farming</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.climatologyMeteorologyAtmosphere&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.climatologyMeteorologyAtmosphere</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.biota&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.biota</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.economy&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.economy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.planningCadastre&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.planningCadastre</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.society&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.society</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.elevation&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.elevation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.environment&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.environment</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.structure&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.structure</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.geoscientificInformation&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.geoscientificInformation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.health&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.health</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.imageryBaseMapsEarthCover&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.imageryBaseMapsEarthCover</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.inlandWaters&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.inlandWaters</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.location&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.location</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.intelligenceMilitary&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.intelligenceMilitary</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.oceans&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.oceans</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.transportation&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.transportation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopicCategoryCode.utilitiesCommunication&quot; '>i18n.catalog.iso19139.MD_TopicCategoryCode.utilitiesCommunication</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.XTN_Identification&quot; '>i18n.catalog.iso19139.XTN_Identification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.XTN_Identification.citation.title&quot; '>i18n.catalog.iso19139.XTN_Identification.citation.title</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.XTN_Identification.citation.date&quot; '>i18n.catalog.iso19139.XTN_Identification.citation.date</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.XTN_Identification.citation.identifier&quot; '>i18n.catalog.iso19139.XTN_Identification.citation.identifier</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.XTN_Identification.citation.MD_Identifier&quot; '>i18n.catalog.iso19139.XTN_Identification.citation.MD_Identifier</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.XTN_Identification.citation.RS_Identifier&quot; '>i18n.catalog.iso19139.XTN_Identification.citation.RS_Identifier</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.XTN_Identification.abstract&quot; '>i18n.catalog.iso19139.XTN_Identification.abstract</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.XTN_Identification.language&quot; '>i18n.catalog.iso19139.XTN_Identification.language</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.XTN_Identification.topicCategory&quot; '>i18n.catalog.iso19139.XTN_Identification.topicCategory</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.XTN_Identification.spatialRepresentationType&quot; '>i18n.catalog.iso19139.XTN_Identification.spatialRepresentationType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.XTN_Identification.spatialResolution&quot; '>i18n.catalog.iso19139.XTN_Identification.spatialResolution</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LocalisedCharacterString&quot; '>i18n.catalog.iso19139.LocalisedCharacterString</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LocalisedCharacterString.id&quot; '>i18n.catalog.iso19139.LocalisedCharacterString.id</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LocalisedCharacterString.locale&quot; '>i18n.catalog.iso19139.LocalisedCharacterString.locale</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LocalisedCharacterString.textNode&quot; '>i18n.catalog.iso19139.LocalisedCharacterString.textNode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.PT_FreeText&quot; '>i18n.catalog.iso19139.PT_FreeText</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.PT_FreeText.textGroup&quot; '>i18n.catalog.iso19139.PT_FreeText.textGroup</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.PT_Locale&quot; '>i18n.catalog.iso19139.PT_Locale</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.PT_Locale.languageCode&quot; '>i18n.catalog.iso19139.PT_Locale.languageCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.PT_Locale.country&quot; '>i18n.catalog.iso19139.PT_Locale.country</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.PT_Locale.characterEncoding&quot; '>i18n.catalog.iso19139.PT_Locale.characterEncoding</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.PT_LocaleContainer&quot; '>i18n.catalog.iso19139.PT_LocaleContainer</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.PT_LocaleContainer.description&quot; '>i18n.catalog.iso19139.PT_LocaleContainer.description</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.PT_LocaleContainer.locale&quot; '>i18n.catalog.iso19139.PT_LocaleContainer.locale</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.PT_LocaleContainer.date&quot; '>i18n.catalog.iso19139.PT_LocaleContainer.date</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.PT_LocaleContainer.responsibleParty&quot; '>i18n.catalog.iso19139.PT_LocaleContainer.responsibleParty</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.PT_LocaleContainer.localisedString&quot; '>i18n.catalog.iso19139.PT_LocaleContainer.localisedString</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceInformation&quot; '>i18n.catalog.iso19139.MD_MaintenanceInformation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceInformation.maintenanceAndUpdateFrequency&quot; '>i18n.catalog.iso19139.MD_MaintenanceInformation.maintenanceAndUpdateFrequency</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceInformation.dateOfNextUpdate&quot; '>i18n.catalog.iso19139.MD_MaintenanceInformation.dateOfNextUpdate</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceInformation.userDefinedMaintenanceFrequency&quot; '>i18n.catalog.iso19139.MD_MaintenanceInformation.userDefinedMaintenanceFrequency</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceInformation.updateScope&quot; '>i18n.catalog.iso19139.MD_MaintenanceInformation.updateScope</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceInformation.updateScopeDescription&quot; '>i18n.catalog.iso19139.MD_MaintenanceInformation.updateScopeDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceInformation.maintenanceNote&quot; '>i18n.catalog.iso19139.MD_MaintenanceInformation.maintenanceNote</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceInformation.contact&quot; '>i18n.catalog.iso19139.MD_MaintenanceInformation.contact</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeDescription&quot; '>i18n.catalog.iso19139.MD_ScopeDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeDescription.attributes&quot; '>i18n.catalog.iso19139.MD_ScopeDescription.attributes</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeDescription.features&quot; '>i18n.catalog.iso19139.MD_ScopeDescription.features</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeDescription.featureInstances&quot; '>i18n.catalog.iso19139.MD_ScopeDescription.featureInstances</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeDescription.attributeInstances&quot; '>i18n.catalog.iso19139.MD_ScopeDescription.attributeInstances</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeDescription.dataset&quot; '>i18n.catalog.iso19139.MD_ScopeDescription.dataset</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeDescription.other&quot; '>i18n.catalog.iso19139.MD_ScopeDescription.other</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceFrequencyCode&quot; '>i18n.catalog.iso19139.MD_MaintenanceFrequencyCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceFrequencyCode.continual&quot; '>i18n.catalog.iso19139.MD_MaintenanceFrequencyCode.continual</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceFrequencyCode.daily&quot; '>i18n.catalog.iso19139.MD_MaintenanceFrequencyCode.daily</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceFrequencyCode.weekly&quot; '>i18n.catalog.iso19139.MD_MaintenanceFrequencyCode.weekly</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceFrequencyCode.fortnightly&quot; '>i18n.catalog.iso19139.MD_MaintenanceFrequencyCode.fortnightly</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceFrequencyCode.monthly&quot; '>i18n.catalog.iso19139.MD_MaintenanceFrequencyCode.monthly</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceFrequencyCode.quarterly&quot; '>i18n.catalog.iso19139.MD_MaintenanceFrequencyCode.quarterly</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceFrequencyCode.biannually&quot; '>i18n.catalog.iso19139.MD_MaintenanceFrequencyCode.biannually</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceFrequencyCode.annually&quot; '>i18n.catalog.iso19139.MD_MaintenanceFrequencyCode.annually</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceFrequencyCode.asNeeded&quot; '>i18n.catalog.iso19139.MD_MaintenanceFrequencyCode.asNeeded</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceFrequencyCode.irregular&quot; '>i18n.catalog.iso19139.MD_MaintenanceFrequencyCode.irregular</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceFrequencyCode.notPlanned&quot; '>i18n.catalog.iso19139.MD_MaintenanceFrequencyCode.notPlanned</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MaintenanceFrequencyCode.unknown&quot; '>i18n.catalog.iso19139.MD_MaintenanceFrequencyCode.unknown</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode&quot; '>i18n.catalog.iso19139.MD_ScopeCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode.attribute&quot; '>i18n.catalog.iso19139.MD_ScopeCode.attribute</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode.attributeType&quot; '>i18n.catalog.iso19139.MD_ScopeCode.attributeType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode.collectionHardware&quot; '>i18n.catalog.iso19139.MD_ScopeCode.collectionHardware</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode.collectionSession&quot; '>i18n.catalog.iso19139.MD_ScopeCode.collectionSession</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode.dataset&quot; '>i18n.catalog.iso19139.MD_ScopeCode.dataset</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode.series&quot; '>i18n.catalog.iso19139.MD_ScopeCode.series</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode.nonGeographicDataset&quot; '>i18n.catalog.iso19139.MD_ScopeCode.nonGeographicDataset</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode.dimensionGroup&quot; '>i18n.catalog.iso19139.MD_ScopeCode.dimensionGroup</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode.feature&quot; '>i18n.catalog.iso19139.MD_ScopeCode.feature</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode.featureType&quot; '>i18n.catalog.iso19139.MD_ScopeCode.featureType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode.propertyType&quot; '>i18n.catalog.iso19139.MD_ScopeCode.propertyType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode.fieldSession&quot; '>i18n.catalog.iso19139.MD_ScopeCode.fieldSession</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode.software&quot; '>i18n.catalog.iso19139.MD_ScopeCode.software</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode.service&quot; '>i18n.catalog.iso19139.MD_ScopeCode.service</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode.model&quot; '>i18n.catalog.iso19139.MD_ScopeCode.model</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ScopeCode.tile&quot; '>i18n.catalog.iso19139.MD_ScopeCode.tile</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractDS_Aggregate&quot; '>i18n.catalog.iso19139.AbstractDS_Aggregate</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractDS_Aggregate.composedOf&quot; '>i18n.catalog.iso19139.AbstractDS_Aggregate.composedOf</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractDS_Aggregate.seriesMetadata&quot; '>i18n.catalog.iso19139.AbstractDS_Aggregate.seriesMetadata</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractDS_Aggregate.subset&quot; '>i18n.catalog.iso19139.AbstractDS_Aggregate.subset</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractDS_Aggregate.superset&quot; '>i18n.catalog.iso19139.AbstractDS_Aggregate.superset</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_DataSet&quot; '>i18n.catalog.iso19139.DS_DataSet</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_DataSet.has&quot; '>i18n.catalog.iso19139.DS_DataSet.has</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DS_DataSet.partOf&quot; '>i18n.catalog.iso19139.DS_DataSet.partOf</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata&quot; '>i18n.catalog.iso19139.MD_Metadata</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.fileIdentifier&quot; '>i18n.catalog.iso19139.MD_Metadata.fileIdentifier</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.language&quot; '>i18n.catalog.iso19139.MD_Metadata.language</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.characterSet&quot; '>i18n.catalog.iso19139.MD_Metadata.characterSet</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.parentIdentifier&quot; '>i18n.catalog.iso19139.MD_Metadata.parentIdentifier</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.hierarchyLevel&quot; '>i18n.catalog.iso19139.MD_Metadata.hierarchyLevel</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.hierarchyLevelName&quot; '>i18n.catalog.iso19139.MD_Metadata.hierarchyLevelName</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.contact&quot; '>i18n.catalog.iso19139.MD_Metadata.contact</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.contact.help&quot; '>i18n.catalog.iso19139.MD_Metadata.contact.help</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.dateStamp&quot; '>i18n.catalog.iso19139.MD_Metadata.dateStamp</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.metadataStandardName&quot; '>i18n.catalog.iso19139.MD_Metadata.metadataStandardName</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.metadataStandardVersion&quot; '>i18n.catalog.iso19139.MD_Metadata.metadataStandardVersion</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.dataSetURI&quot; '>i18n.catalog.iso19139.MD_Metadata.dataSetURI</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.locale&quot; '>i18n.catalog.iso19139.MD_Metadata.locale</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.spatialRepresentationInfo&quot; '>i18n.catalog.iso19139.MD_Metadata.spatialRepresentationInfo</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.referenceSystemInfo&quot; '>i18n.catalog.iso19139.MD_Metadata.referenceSystemInfo</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.metadataExtensionInfo&quot; '>i18n.catalog.iso19139.MD_Metadata.metadataExtensionInfo</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.identificationInfo&quot; '>i18n.catalog.iso19139.MD_Metadata.identificationInfo</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.contentInfo&quot; '>i18n.catalog.iso19139.MD_Metadata.contentInfo</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.distributionInfo&quot; '>i18n.catalog.iso19139.MD_Metadata.distributionInfo</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.dataQualityInfo&quot; '>i18n.catalog.iso19139.MD_Metadata.dataQualityInfo</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.portrayalCatalogueInfo&quot; '>i18n.catalog.iso19139.MD_Metadata.portrayalCatalogueInfo</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.metadataConstraints&quot; '>i18n.catalog.iso19139.MD_Metadata.metadataConstraints</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.applicationSchemaInfo&quot; '>i18n.catalog.iso19139.MD_Metadata.applicationSchemaInfo</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.metadataMaintenance&quot; '>i18n.catalog.iso19139.MD_Metadata.metadataMaintenance</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.series&quot; '>i18n.catalog.iso19139.MD_Metadata.series</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.describes&quot; '>i18n.catalog.iso19139.MD_Metadata.describes</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.propertyType&quot; '>i18n.catalog.iso19139.MD_Metadata.propertyType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.featureType&quot; '>i18n.catalog.iso19139.MD_Metadata.featureType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.featureAttribute&quot; '>i18n.catalog.iso19139.MD_Metadata.featureAttribute</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.metadata&quot; '>i18n.catalog.iso19139.MD_Metadata.section.metadata</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.metadata.identifier&quot; '>i18n.catalog.iso19139.MD_Metadata.section.metadata.identifier</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.metadata.contact&quot; '>i18n.catalog.iso19139.MD_Metadata.section.metadata.contact</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.metadata.date&quot; '>i18n.catalog.iso19139.MD_Metadata.section.metadata.date</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.metadata.standard&quot; '>i18n.catalog.iso19139.MD_Metadata.section.metadata.standard</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.metadata.reference&quot; '>i18n.catalog.iso19139.MD_Metadata.section.metadata.reference</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.citation&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.citation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.abstract&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.abstract</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.contact&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.contact</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.graphicOverview&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.graphicOverview</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.descriptiveKeywords&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.descriptiveKeywords</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.otherKeywords&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.otherKeywords</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.resourceConstraints&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.resourceConstraints</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.resource&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.resource</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.representation&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.representation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.language&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.language</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.classification&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.classification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.extent&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.extent</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.extent.geographicElement&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.extent.geographicElement</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.extent.temporalElement&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.extent.temporalElement</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.service.serviceType&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.service.serviceType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.service.couplingType&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.service.couplingType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.service.operation&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.service.operation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.identification.service.operatesOn&quot; '>i18n.catalog.iso19139.MD_Metadata.section.identification.service.operatesOn</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.distribution&quot; '>i18n.catalog.iso19139.MD_Metadata.section.distribution</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.quality&quot; '>i18n.catalog.iso19139.MD_Metadata.section.quality</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.quality.scope&quot; '>i18n.catalog.iso19139.MD_Metadata.section.quality.scope</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.quality.conformance&quot; '>i18n.catalog.iso19139.MD_Metadata.section.quality.conformance</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Metadata.section.quality.lineage&quot; '>i18n.catalog.iso19139.MD_Metadata.section.quality.lineage</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ExtendedElementInformation&quot; '>i18n.catalog.iso19139.MD_ExtendedElementInformation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ExtendedElementInformation.name&quot; '>i18n.catalog.iso19139.MD_ExtendedElementInformation.name</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ExtendedElementInformation.shortName&quot; '>i18n.catalog.iso19139.MD_ExtendedElementInformation.shortName</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ExtendedElementInformation.domainCode&quot; '>i18n.catalog.iso19139.MD_ExtendedElementInformation.domainCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ExtendedElementInformation.definition&quot; '>i18n.catalog.iso19139.MD_ExtendedElementInformation.definition</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ExtendedElementInformation.obligation&quot; '>i18n.catalog.iso19139.MD_ExtendedElementInformation.obligation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ExtendedElementInformation.condition&quot; '>i18n.catalog.iso19139.MD_ExtendedElementInformation.condition</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ExtendedElementInformation.dataType&quot; '>i18n.catalog.iso19139.MD_ExtendedElementInformation.dataType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ExtendedElementInformation.maximumOccurrence&quot; '>i18n.catalog.iso19139.MD_ExtendedElementInformation.maximumOccurrence</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ExtendedElementInformation.domainValue&quot; '>i18n.catalog.iso19139.MD_ExtendedElementInformation.domainValue</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ExtendedElementInformation.parentEntity&quot; '>i18n.catalog.iso19139.MD_ExtendedElementInformation.parentEntity</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ExtendedElementInformation.rule&quot; '>i18n.catalog.iso19139.MD_ExtendedElementInformation.rule</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ExtendedElementInformation.rationale&quot; '>i18n.catalog.iso19139.MD_ExtendedElementInformation.rationale</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ExtendedElementInformation.source&quot; '>i18n.catalog.iso19139.MD_ExtendedElementInformation.source</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MetadataExtensionInformation&quot; '>i18n.catalog.iso19139.MD_MetadataExtensionInformation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MetadataExtensionInformation.extensionOnLineResource&quot; '>i18n.catalog.iso19139.MD_MetadataExtensionInformation.extensionOnLineResource</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_MetadataExtensionInformation.extendedElementInformation&quot; '>i18n.catalog.iso19139.MD_MetadataExtensionInformation.extendedElementInformation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DatatypeCode&quot; '>i18n.catalog.iso19139.MD_DatatypeCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DatatypeCode.class&quot; '>i18n.catalog.iso19139.MD_DatatypeCode.class</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DatatypeCode.codelist&quot; '>i18n.catalog.iso19139.MD_DatatypeCode.codelist</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DatatypeCode.enumeration&quot; '>i18n.catalog.iso19139.MD_DatatypeCode.enumeration</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DatatypeCode.codelistElement&quot; '>i18n.catalog.iso19139.MD_DatatypeCode.codelistElement</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DatatypeCode.abstractClass&quot; '>i18n.catalog.iso19139.MD_DatatypeCode.abstractClass</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DatatypeCode.aggregateClass&quot; '>i18n.catalog.iso19139.MD_DatatypeCode.aggregateClass</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DatatypeCode.specifiedClass&quot; '>i18n.catalog.iso19139.MD_DatatypeCode.specifiedClass</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DatatypeCode.datatypeClass&quot; '>i18n.catalog.iso19139.MD_DatatypeCode.datatypeClass</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DatatypeCode.interfaceClass&quot; '>i18n.catalog.iso19139.MD_DatatypeCode.interfaceClass</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DatatypeCode.unionClass&quot; '>i18n.catalog.iso19139.MD_DatatypeCode.unionClass</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DatatypeCode.metaClass&quot; '>i18n.catalog.iso19139.MD_DatatypeCode.metaClass</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DatatypeCode.typeClass&quot; '>i18n.catalog.iso19139.MD_DatatypeCode.typeClass</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DatatypeCode.characterString&quot; '>i18n.catalog.iso19139.MD_DatatypeCode.characterString</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DatatypeCode.integer&quot; '>i18n.catalog.iso19139.MD_DatatypeCode.integer</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DatatypeCode.association&quot; '>i18n.catalog.iso19139.MD_DatatypeCode.association</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ObligationCode&quot; '>i18n.catalog.iso19139.MD_ObligationCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ObligationCode.mandatory&quot; '>i18n.catalog.iso19139.MD_ObligationCode.mandatory</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ObligationCode.optional&quot; '>i18n.catalog.iso19139.MD_ObligationCode.optional</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ObligationCode.conditional&quot; '>i18n.catalog.iso19139.MD_ObligationCode.conditional</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_PortrayalCatalogueReference&quot; '>i18n.catalog.iso19139.MD_PortrayalCatalogueReference</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_PortrayalCatalogueReference.portrayalCatalogueCitation&quot; '>i18n.catalog.iso19139.MD_PortrayalCatalogueReference.portrayalCatalogueCitation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractRS_ReferenceSystem&quot; '>i18n.catalog.iso19139.AbstractRS_ReferenceSystem</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractRS_ReferenceSystem.name&quot; '>i18n.catalog.iso19139.AbstractRS_ReferenceSystem.name</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractRS_ReferenceSystem.domainOfValidity&quot; '>i18n.catalog.iso19139.AbstractRS_ReferenceSystem.domainOfValidity</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Identifier&quot; '>i18n.catalog.iso19139.MD_Identifier</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Identifier.authority&quot; '>i18n.catalog.iso19139.MD_Identifier.authority</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Identifier.code&quot; '>i18n.catalog.iso19139.MD_Identifier.code</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ReferenceSystem&quot; '>i18n.catalog.iso19139.MD_ReferenceSystem</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ReferenceSystem.referenceSystemIdentifier&quot; '>i18n.catalog.iso19139.MD_ReferenceSystem.referenceSystemIdentifier</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.RS_Identifier&quot; '>i18n.catalog.iso19139.RS_Identifier</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.RS_Identifier.authority&quot; '>i18n.catalog.iso19139.RS_Identifier.authority</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.RS_Identifier.code&quot; '>i18n.catalog.iso19139.RS_Identifier.code</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.RS_Identifier.codeSpace&quot; '>i18n.catalog.iso19139.RS_Identifier.codeSpace</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.RS_Identifier.version&quot; '>i18n.catalog.iso19139.RS_Identifier.version</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Dimension&quot; '>i18n.catalog.iso19139.MD_Dimension</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Dimension.dimensionName&quot; '>i18n.catalog.iso19139.MD_Dimension.dimensionName</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Dimension.dimensionSize&quot; '>i18n.catalog.iso19139.MD_Dimension.dimensionSize</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Dimension.resolution&quot; '>i18n.catalog.iso19139.MD_Dimension.resolution</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_GeometricObjects&quot; '>i18n.catalog.iso19139.MD_GeometricObjects</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_GeometricObjects.geometricObjectType&quot; '>i18n.catalog.iso19139.MD_GeometricObjects.geometricObjectType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_GeometricObjects.geometricObjectCount&quot; '>i18n.catalog.iso19139.MD_GeometricObjects.geometricObjectCount</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Georectified&quot; '>i18n.catalog.iso19139.MD_Georectified</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Georectified.checkPointAvailability&quot; '>i18n.catalog.iso19139.MD_Georectified.checkPointAvailability</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Georectified.checkPointDescription&quot; '>i18n.catalog.iso19139.MD_Georectified.checkPointDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Georectified.cornerPoints&quot; '>i18n.catalog.iso19139.MD_Georectified.cornerPoints</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Georectified.centerPoint&quot; '>i18n.catalog.iso19139.MD_Georectified.centerPoint</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Georectified.pointInPixel&quot; '>i18n.catalog.iso19139.MD_Georectified.pointInPixel</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Georectified.transformationDimensionDescription&quot; '>i18n.catalog.iso19139.MD_Georectified.transformationDimensionDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Georectified.transformationDimensionMapping&quot; '>i18n.catalog.iso19139.MD_Georectified.transformationDimensionMapping</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Georeferenceable&quot; '>i18n.catalog.iso19139.MD_Georeferenceable</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Georeferenceable.controlPointAvailability&quot; '>i18n.catalog.iso19139.MD_Georeferenceable.controlPointAvailability</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Georeferenceable.orientationParameterAvailability&quot; '>i18n.catalog.iso19139.MD_Georeferenceable.orientationParameterAvailability</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Georeferenceable.orientationParameterDescription&quot; '>i18n.catalog.iso19139.MD_Georeferenceable.orientationParameterDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Georeferenceable.georeferencedParameters&quot; '>i18n.catalog.iso19139.MD_Georeferenceable.georeferencedParameters</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Georeferenceable.parameterCitation&quot; '>i18n.catalog.iso19139.MD_Georeferenceable.parameterCitation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_GridSpatialRepresentation&quot; '>i18n.catalog.iso19139.MD_GridSpatialRepresentation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_GridSpatialRepresentation.numberOfDimensions&quot; '>i18n.catalog.iso19139.MD_GridSpatialRepresentation.numberOfDimensions</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_GridSpatialRepresentation.axisDimensionProperties&quot; '>i18n.catalog.iso19139.MD_GridSpatialRepresentation.axisDimensionProperties</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_GridSpatialRepresentation.cellGeometry&quot; '>i18n.catalog.iso19139.MD_GridSpatialRepresentation.cellGeometry</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_GridSpatialRepresentation.transformationParameterAvailability&quot; '>i18n.catalog.iso19139.MD_GridSpatialRepresentation.transformationParameterAvailability</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_VectorSpatialRepresentation&quot; '>i18n.catalog.iso19139.MD_VectorSpatialRepresentation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_VectorSpatialRepresentation.topologyLevel&quot; '>i18n.catalog.iso19139.MD_VectorSpatialRepresentation.topologyLevel</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_VectorSpatialRepresentation.geometricObjects&quot; '>i18n.catalog.iso19139.MD_VectorSpatialRepresentation.geometricObjects</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CellGeometryCode&quot; '>i18n.catalog.iso19139.MD_CellGeometryCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CellGeometryCode.point&quot; '>i18n.catalog.iso19139.MD_CellGeometryCode.point</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CellGeometryCode.area&quot; '>i18n.catalog.iso19139.MD_CellGeometryCode.area</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DimensionNameTypeCode&quot; '>i18n.catalog.iso19139.MD_DimensionNameTypeCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DimensionNameTypeCode.row&quot; '>i18n.catalog.iso19139.MD_DimensionNameTypeCode.row</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DimensionNameTypeCode.column&quot; '>i18n.catalog.iso19139.MD_DimensionNameTypeCode.column</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DimensionNameTypeCode.vertical&quot; '>i18n.catalog.iso19139.MD_DimensionNameTypeCode.vertical</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DimensionNameTypeCode.track&quot; '>i18n.catalog.iso19139.MD_DimensionNameTypeCode.track</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DimensionNameTypeCode.crossTrack&quot; '>i18n.catalog.iso19139.MD_DimensionNameTypeCode.crossTrack</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DimensionNameTypeCode.line&quot; '>i18n.catalog.iso19139.MD_DimensionNameTypeCode.line</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DimensionNameTypeCode.sample&quot; '>i18n.catalog.iso19139.MD_DimensionNameTypeCode.sample</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_DimensionNameTypeCode.time&quot; '>i18n.catalog.iso19139.MD_DimensionNameTypeCode.time</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_GeometricObjectTypeCode&quot; '>i18n.catalog.iso19139.MD_GeometricObjectTypeCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_GeometricObjectTypeCode.complex&quot; '>i18n.catalog.iso19139.MD_GeometricObjectTypeCode.complex</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_GeometricObjectTypeCode.composite&quot; '>i18n.catalog.iso19139.MD_GeometricObjectTypeCode.composite</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_GeometricObjectTypeCode.curve&quot; '>i18n.catalog.iso19139.MD_GeometricObjectTypeCode.curve</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_GeometricObjectTypeCode.point&quot; '>i18n.catalog.iso19139.MD_GeometricObjectTypeCode.point</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_GeometricObjectTypeCode.solid&quot; '>i18n.catalog.iso19139.MD_GeometricObjectTypeCode.solid</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_GeometricObjectTypeCode.surface&quot; '>i18n.catalog.iso19139.MD_GeometricObjectTypeCode.surface</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_PixelOrientationCode&quot; '>i18n.catalog.iso19139.MD_PixelOrientationCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_PixelOrientationCode.center&quot; '>i18n.catalog.iso19139.MD_PixelOrientationCode.center</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_PixelOrientationCode.lowerLeft&quot; '>i18n.catalog.iso19139.MD_PixelOrientationCode.lowerLeft</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_PixelOrientationCode.lowerRight&quot; '>i18n.catalog.iso19139.MD_PixelOrientationCode.lowerRight</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_PixelOrientationCode.upperRight&quot; '>i18n.catalog.iso19139.MD_PixelOrientationCode.upperRight</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_PixelOrientationCode.upperLeft&quot; '>i18n.catalog.iso19139.MD_PixelOrientationCode.upperLeft</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopologyLevelCode&quot; '>i18n.catalog.iso19139.MD_TopologyLevelCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopologyLevelCode.geometryOnly&quot; '>i18n.catalog.iso19139.MD_TopologyLevelCode.geometryOnly</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopologyLevelCode.topology1D&quot; '>i18n.catalog.iso19139.MD_TopologyLevelCode.topology1D</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopologyLevelCode.planarGraph&quot; '>i18n.catalog.iso19139.MD_TopologyLevelCode.planarGraph</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopologyLevelCode.fullPlanarGraph&quot; '>i18n.catalog.iso19139.MD_TopologyLevelCode.fullPlanarGraph</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopologyLevelCode.surfaceGraph&quot; '>i18n.catalog.iso19139.MD_TopologyLevelCode.surfaceGraph</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopologyLevelCode.fullSurfaceGraph&quot; '>i18n.catalog.iso19139.MD_TopologyLevelCode.fullSurfaceGraph</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopologyLevelCode.topology3D&quot; '>i18n.catalog.iso19139.MD_TopologyLevelCode.topology3D</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopologyLevelCode.fullTopology3D&quot; '>i18n.catalog.iso19139.MD_TopologyLevelCode.fullTopology3D</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_TopologyLevelCode.abstract&quot; '>i18n.catalog.iso19139.MD_TopologyLevelCode.abstract</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_DataQuality&quot; '>i18n.catalog.iso19139.DQ_DataQuality</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_DataQuality.scope&quot; '>i18n.catalog.iso19139.DQ_DataQuality.scope</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_DataQuality.report&quot; '>i18n.catalog.iso19139.DQ_DataQuality.report</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_DataQuality.lineage&quot; '>i18n.catalog.iso19139.DQ_DataQuality.lineage</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_Scope&quot; '>i18n.catalog.iso19139.DQ_Scope</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_Scope.level&quot; '>i18n.catalog.iso19139.DQ_Scope.level</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_Scope.extent&quot; '>i18n.catalog.iso19139.DQ_Scope.extent</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_Scope.levelDescription&quot; '>i18n.catalog.iso19139.DQ_Scope.levelDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractDQ_Element&quot; '>i18n.catalog.iso19139.AbstractDQ_Element</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractDQ_Element.nameOfMeasure&quot; '>i18n.catalog.iso19139.AbstractDQ_Element.nameOfMeasure</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractDQ_Element.measureIdentification&quot; '>i18n.catalog.iso19139.AbstractDQ_Element.measureIdentification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractDQ_Element.measureDescription&quot; '>i18n.catalog.iso19139.AbstractDQ_Element.measureDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractDQ_Element.evaluationMethodType&quot; '>i18n.catalog.iso19139.AbstractDQ_Element.evaluationMethodType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractDQ_Element.evaluationMethodDescription&quot; '>i18n.catalog.iso19139.AbstractDQ_Element.evaluationMethodDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractDQ_Element.evaluationProcedure&quot; '>i18n.catalog.iso19139.AbstractDQ_Element.evaluationProcedure</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractDQ_Element.dateTime&quot; '>i18n.catalog.iso19139.AbstractDQ_Element.dateTime</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractDQ_Element.result&quot; '>i18n.catalog.iso19139.AbstractDQ_Element.result</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_AbsoluteExternalPositionalAccuracy&quot; '>i18n.catalog.iso19139.DQ_AbsoluteExternalPositionalAccuracy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_GriddedDataPositionalAccuracy&quot; '>i18n.catalog.iso19139.DQ_GriddedDataPositionalAccuracy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_RelativeInternalPositionalAccuracy&quot; '>i18n.catalog.iso19139.DQ_RelativeInternalPositionalAccuracy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_NonQuantitiveAttributeAccuracy&quot; '>i18n.catalog.iso19139.DQ_NonQuantitiveAttributeAccuracy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_QuantitativeAttributeAccuracy&quot; '>i18n.catalog.iso19139.DQ_QuantitativeAttributeAccuracy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_AccuracyOfATimeMeasurement&quot; '>i18n.catalog.iso19139.DQ_AccuracyOfATimeMeasurement</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_CompletenessOmission&quot; '>i18n.catalog.iso19139.DQ_CompletenessOmission</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_CompletenessCommission&quot; '>i18n.catalog.iso19139.DQ_CompletenessCommission</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_ConceptualConsistency&quot; '>i18n.catalog.iso19139.DQ_ConceptualConsistency</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_DomainConsistency&quot; '>i18n.catalog.iso19139.DQ_DomainConsistency</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_FormatConsistency&quot; '>i18n.catalog.iso19139.DQ_FormatConsistency</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_TemporalAccuracy&quot; '>i18n.catalog.iso19139.DQ_TemporalAccuracy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_TemporalConsistency&quot; '>i18n.catalog.iso19139.DQ_TemporalConsistency</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_TemporalValidity&quot; '>i18n.catalog.iso19139.DQ_TemporalValidity</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_ThematicAccuracy&quot; '>i18n.catalog.iso19139.DQ_ThematicAccuracy</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_ThematicClassificationCorrectness&quot; '>i18n.catalog.iso19139.DQ_ThematicClassificationCorrectness</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_TopologicalConsistency&quot; '>i18n.catalog.iso19139.DQ_TopologicalConsistency</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_QuantitativeResult&quot; '>i18n.catalog.iso19139.DQ_QuantitativeResult</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_QuantitativeResult.valueType&quot; '>i18n.catalog.iso19139.DQ_QuantitativeResult.valueType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_QuantitativeResult.errorStatistic&quot; '>i18n.catalog.iso19139.DQ_QuantitativeResult.errorStatistic</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_QuantitativeResult.value&quot; '>i18n.catalog.iso19139.DQ_QuantitativeResult.value</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_QuantitativeResult.valueUnit&quot; '>i18n.catalog.iso19139.DQ_QuantitativeResult.valueUnit</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_ConformanceResult&quot; '>i18n.catalog.iso19139.DQ_ConformanceResult</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_ConformanceResult.specification&quot; '>i18n.catalog.iso19139.DQ_ConformanceResult.specification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_ConformanceResult.explanation&quot; '>i18n.catalog.iso19139.DQ_ConformanceResult.explanation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_ConformanceResult.pass&quot; '>i18n.catalog.iso19139.DQ_ConformanceResult.pass</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_ConformanceResult.pass.Boolean&quot; '>i18n.catalog.iso19139.DQ_ConformanceResult.pass.Boolean</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_ConformanceResult.pass.Boolean.true&quot; '>i18n.catalog.iso19139.DQ_ConformanceResult.pass.Boolean.true</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DQ_ConformanceResult.pass.Boolean.false&quot; '>i18n.catalog.iso19139.DQ_ConformanceResult.pass.Boolean.false</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_Lineage&quot; '>i18n.catalog.iso19139.LI_Lineage</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_Lineage.statement&quot; '>i18n.catalog.iso19139.LI_Lineage.statement</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_Lineage.processStep&quot; '>i18n.catalog.iso19139.LI_Lineage.processStep</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_Lineage.source&quot; '>i18n.catalog.iso19139.LI_Lineage.source</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_ProcessStep&quot; '>i18n.catalog.iso19139.LI_ProcessStep</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_ProcessStep.description&quot; '>i18n.catalog.iso19139.LI_ProcessStep.description</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_ProcessStep.rationale&quot; '>i18n.catalog.iso19139.LI_ProcessStep.rationale</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_ProcessStep.dateTime&quot; '>i18n.catalog.iso19139.LI_ProcessStep.dateTime</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_ProcessStep.processor&quot; '>i18n.catalog.iso19139.LI_ProcessStep.processor</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_ProcessStep.source&quot; '>i18n.catalog.iso19139.LI_ProcessStep.source</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_Source&quot; '>i18n.catalog.iso19139.LI_Source</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_Source.description&quot; '>i18n.catalog.iso19139.LI_Source.description</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_Source.scaleDenominator&quot; '>i18n.catalog.iso19139.LI_Source.scaleDenominator</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_Source.sourceReferenceSystem&quot; '>i18n.catalog.iso19139.LI_Source.sourceReferenceSystem</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_Source.sourceCitation&quot; '>i18n.catalog.iso19139.LI_Source.sourceCitation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_Source.sourceExtent&quot; '>i18n.catalog.iso19139.LI_Source.sourceExtent</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.LI_Source.sourceStep&quot; '>i18n.catalog.iso19139.LI_Source.sourceStep</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.XTN_Scope.level&quot; '>i18n.catalog.iso19139.XTN_Scope.level</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.AbstractTimePrimitive&quot; '>i18n.catalog.iso19139.AbstractTimePrimitive</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.StandardObjectProperties&quot; '>i18n.catalog.iso19139.StandardObjectProperties</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.StandardObjectProperties.metaDataProperty&quot; '>i18n.catalog.iso19139.StandardObjectProperties.metaDataProperty</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.StandardObjectProperties.description&quot; '>i18n.catalog.iso19139.StandardObjectProperties.description</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.StandardObjectProperties.descriptionReference&quot; '>i18n.catalog.iso19139.StandardObjectProperties.descriptionReference</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.StandardObjectProperties.name&quot; '>i18n.catalog.iso19139.StandardObjectProperties.name</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.StandardObjectProperties.identifier&quot; '>i18n.catalog.iso19139.StandardObjectProperties.identifier</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_FeatureCatalogueDescription&quot; '>i18n.catalog.iso19139.MD_FeatureCatalogueDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_FeatureCatalogueDescription.complianceCode&quot; '>i18n.catalog.iso19139.MD_FeatureCatalogueDescription.complianceCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_FeatureCatalogueDescription.language&quot; '>i18n.catalog.iso19139.MD_FeatureCatalogueDescription.language</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_FeatureCatalogueDescription.includedWithDataset&quot; '>i18n.catalog.iso19139.MD_FeatureCatalogueDescription.includedWithDataset</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_FeatureCatalogueDescription.featureTypes&quot; '>i18n.catalog.iso19139.MD_FeatureCatalogueDescription.featureTypes</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_FeatureCatalogueDescription.featureCatalogueCitation&quot; '>i18n.catalog.iso19139.MD_FeatureCatalogueDescription.featureCatalogueCitation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CoverageDescription&quot; '>i18n.catalog.iso19139.MD_CoverageDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CoverageDescription.attributeDescription&quot; '>i18n.catalog.iso19139.MD_CoverageDescription.attributeDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CoverageDescription.contentType&quot; '>i18n.catalog.iso19139.MD_CoverageDescription.contentType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CoverageDescription.dimension&quot; '>i18n.catalog.iso19139.MD_CoverageDescription.dimension</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CoverageContentTypeCode&quot; '>i18n.catalog.iso19139.MD_CoverageContentTypeCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CoverageContentTypeCode.image&quot; '>i18n.catalog.iso19139.MD_CoverageContentTypeCode.image</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CoverageContentTypeCode.thematicClassification&quot; '>i18n.catalog.iso19139.MD_CoverageContentTypeCode.thematicClassification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_CoverageContentTypeCode.physicalMeasurement&quot; '>i18n.catalog.iso19139.MD_CoverageContentTypeCode.physicalMeasurement</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_RangeDimension&quot; '>i18n.catalog.iso19139.MD_RangeDimension</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_RangeDimension.sequenceIdentifier&quot; '>i18n.catalog.iso19139.MD_RangeDimension.sequenceIdentifier</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_RangeDimension.descriptor&quot; '>i18n.catalog.iso19139.MD_RangeDimension.descriptor</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Band&quot; '>i18n.catalog.iso19139.MD_Band</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Band.sequenceIdentifier&quot; '>i18n.catalog.iso19139.MD_Band.sequenceIdentifier</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Band.descriptor&quot; '>i18n.catalog.iso19139.MD_Band.descriptor</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Band.minValue&quot; '>i18n.catalog.iso19139.MD_Band.minValue</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Band.maxValue&quot; '>i18n.catalog.iso19139.MD_Band.maxValue</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Band.units&quot; '>i18n.catalog.iso19139.MD_Band.units</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Band.peakResponse&quot; '>i18n.catalog.iso19139.MD_Band.peakResponse</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Band.bitsPerValue&quot; '>i18n.catalog.iso19139.MD_Band.bitsPerValue</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Band.toneGradation&quot; '>i18n.catalog.iso19139.MD_Band.toneGradation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Band.scaleFactor&quot; '>i18n.catalog.iso19139.MD_Band.scaleFactor</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_Band.offset&quot; '>i18n.catalog.iso19139.MD_Band.offset</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImageDescription&quot; '>i18n.catalog.iso19139.MD_ImageDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImageDescription.attributeDescription&quot; '>i18n.catalog.iso19139.MD_ImageDescription.attributeDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImageDescription.contentType&quot; '>i18n.catalog.iso19139.MD_ImageDescription.contentType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImageDescription.dimension&quot; '>i18n.catalog.iso19139.MD_ImageDescription.dimension</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImageDescription.illuminationElevationAngle&quot; '>i18n.catalog.iso19139.MD_ImageDescription.illuminationElevationAngle</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImageDescription.illuminationAzimuthAngle&quot; '>i18n.catalog.iso19139.MD_ImageDescription.illuminationAzimuthAngle</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImageDescription.imagingCondition&quot; '>i18n.catalog.iso19139.MD_ImageDescription.imagingCondition</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImageDescription.imageQualityCode&quot; '>i18n.catalog.iso19139.MD_ImageDescription.imageQualityCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImageDescription.cloudCoverPercentage&quot; '>i18n.catalog.iso19139.MD_ImageDescription.cloudCoverPercentage</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImageDescription.processingLevelCode&quot; '>i18n.catalog.iso19139.MD_ImageDescription.processingLevelCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImageDescription.compressionGenerationQuantity&quot; '>i18n.catalog.iso19139.MD_ImageDescription.compressionGenerationQuantity</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImageDescription.triangulationIndicator&quot; '>i18n.catalog.iso19139.MD_ImageDescription.triangulationIndicator</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImageDescription.radiometricCalibrationDataAvailability&quot; '>i18n.catalog.iso19139.MD_ImageDescription.radiometricCalibrationDataAvailability</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImageDescription.cameraCalibrationInformationAvailability&quot; '>i18n.catalog.iso19139.MD_ImageDescription.cameraCalibrationInformationAvailability</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImageDescription.filmDistortionInformationAvailability&quot; '>i18n.catalog.iso19139.MD_ImageDescription.filmDistortionInformationAvailability</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImageDescription.lensDistortionInformationAvailability&quot; '>i18n.catalog.iso19139.MD_ImageDescription.lensDistortionInformationAvailability</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImagingConditionCode&quot; '>i18n.catalog.iso19139.MD_ImagingConditionCode</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImagingConditionCode.blurredImage&quot; '>i18n.catalog.iso19139.MD_ImagingConditionCode.blurredImage</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImagingConditionCode.cloud&quot; '>i18n.catalog.iso19139.MD_ImagingConditionCode.cloud</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImagingConditionCode.degradingObliquity&quot; '>i18n.catalog.iso19139.MD_ImagingConditionCode.degradingObliquity</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImagingConditionCode.fog&quot; '>i18n.catalog.iso19139.MD_ImagingConditionCode.fog</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImagingConditionCode.heavySmokeOrDust&quot; '>i18n.catalog.iso19139.MD_ImagingConditionCode.heavySmokeOrDust</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImagingConditionCode.night&quot; '>i18n.catalog.iso19139.MD_ImagingConditionCode.night</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImagingConditionCode.rain&quot; '>i18n.catalog.iso19139.MD_ImagingConditionCode.rain</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImagingConditionCode.semiDarkness&quot; '>i18n.catalog.iso19139.MD_ImagingConditionCode.semiDarkness</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImagingConditionCode.shadow&quot; '>i18n.catalog.iso19139.MD_ImagingConditionCode.shadow</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.MD_ImagingConditionCode.snow&quot; '>i18n.catalog.iso19139.MD_ImagingConditionCode.snow</xsl:when>
      <!-- ISO 19115-2 resource strings -->
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.section.acquisition&quot;'>i18n.catalog.iso19139-2.MI_Metadata.section.acquisition</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.section.acuisition.instrument&quot;'>i18n.catalog.iso19139-2.MI_Metadata.section.acuisition.instrument</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.section.acuisition.operation&quot;'>i18n.catalog.iso19139-2.MI_Metadata.section.acuisition.operation</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.section.acuisition.platform&quot;'>i18n.catalog.iso19139-2.MI_Metadata.section.acuisition.platform</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.section.acuisition.objective&quot;'>i18n.catalog.iso19139-2.MI_Metadata.section.acuisition.objective</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.section.acuisition.requirement&quot;'>i18n.catalog.iso19139-2.MI_Metadata.section.acuisition.requirement</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.section.acuisition.plan&quot;'>i18n.catalog.iso19139-2.MI_Metadata.section.acuisition.plan</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acuisition.instrument.type&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acuisition.instrument.type</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acuisition.instrument.description&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acuisition.instrument.description</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acuisition.instrument.citation&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acuisition.instrument.citation</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acuisition.operation.description&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acuisition.operation.description</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.operation.citation&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.operation.citation</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.operation.operationType&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.operation.operationType</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.operation.operationTypeCode&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.operation.operationTypeCode</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.platform.citation&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.platform.citation</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.platform.description&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.platform.description</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.platform.sponsor&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.platform.sponsor</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.platform.instrument&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.platform.instrument</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.objective.priority&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.objective.priority</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.objective.type&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.objective.type</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.objective.function&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.objective.function</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.objective.extent&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.objective.extent</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.objective.objectiveOccurrence&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.objective.objectiveOccurrence</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acuisition.plan.type&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acuisition.plan.type</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.plan.status&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.plan.status</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.plan.citation&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.plan.citation</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.section.acquisition.citation&quot;'>i18n.catalog.iso19139-2.MI_Metadata.section.acquisition.citation</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.requirement.requestor&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.requirement.requestor</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.requirement.recipient&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.requirement.recipient</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.requirement.priority&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.requirement.priority</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.requirement.requestedDate&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.requirement.requestedDate</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.requirement.requestedDate.requestedDateOfCollection&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.requirement.requestedDate.requestedDateOfCollection</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.requirement.requestedDate.latestAcceptableDate&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.requirement.requestedDate.latestAcceptableDate</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.requirement.expiryDate&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.requirement.expiryDate</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.event&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.event</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.event.trigger&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.event.trigger</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.event.triggerCode.automatic&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.event.triggerCode.automatic</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.event.triggerCode.manual&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.event.triggerCode.manual</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.event.triggerCode.PreProgrammed&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.event.triggerCode.PreProgrammed</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.event.context&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.event.context</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.event.contextCode.acquisition&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.event.contextCode.acquisition</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.event.contextCode.pass&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.event.contextCode.pass</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.event.contextCode.wayPoint&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.event.contextCode.wayPoint</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.event.sequence&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.event.sequence</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.event.sequenceCode.start&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.event.sequenceCode.start</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.event.sequenceCode.end&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.event.sequenceCode.end</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.event.sequenceCode.instantaneous&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.event.sequenceCode.instantaneous</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.event.time&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.event.time</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.operation.operationTypeCode.real&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.operation.operationTypeCode.real</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.operation.operationTypeCode.simulated&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.operation.operationTypeCode.simulated</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.operation.operationTypeCode.synthesized&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.operation.operationTypeCode.synthesized</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.objective.objectiveTypeCode.instantaneousCollection&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.objective.objectiveTypeCode.instantaneousCollection</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.objective.objectiveTypeCode.persistentView&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.objective.objectiveTypeCode.persistentView</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.objective.objectiveTypeCode.survey&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.objective.objectiveTypeCode.survey</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.plan.geometryTypeCode.point&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.plan.geometryTypeCode.point</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.plan.geometryTypeCode.linear&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.plan.geometryTypeCode.linear</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.plan.geometryTypeCode.areal&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.plan.geometryTypeCode.areal</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.plan.geometryTypeCode.strip&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.plan.geometryTypeCode.strip</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.requirement.priorityCode.critical&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.requirement.priorityCode.critical</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.requirement.priorityCode.highImportance&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.requirement.priorityCode.highImportance</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.requirement.priorityCode.mediumImportance&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.requirement.priorityCode.mediumImportance</xsl:when>
      <xsl:when test='$key = &quot;catalog.iso19139-2.MI_Metadata.acquisition.requirement.priorityCode.lowImportance&quot;'>i18n.catalog.iso19139-2.MI_Metadata.acquisition.requirement.priorityCode.lowImportance</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Operation&quot; '>i18n.catalog.iso19139.SV_Operation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Operation.operationName&quot; '>i18n.catalog.iso19139.SV_Operation.operationName</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Operation.dependsOn&quot; '>i18n.catalog.iso19139.SV_Operation.dependsOn</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Operation.parameter&quot; '>i18n.catalog.iso19139.SV_Operation.parameter</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_PortSpecification&quot; '>i18n.catalog.iso19139.SV_PortSpecification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_PortSpecification.binding&quot; '>i18n.catalog.iso19139.SV_PortSpecification.binding</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_PortSpecification.address&quot; '>i18n.catalog.iso19139.SV_PortSpecification.address</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Interface&quot; '>i18n.catalog.iso19139.SV_Interface</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Interface.typeName&quot; '>i18n.catalog.iso19139.SV_Interface.typeName</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Interface.theSV_Port&quot; '>i18n.catalog.iso19139.SV_Interface.theSV_Port</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Interface.operation&quot; '>i18n.catalog.iso19139.SV_Interface.operation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Service&quot; '>i18n.catalog.iso19139.SV_Service</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Service.specification&quot; '>i18n.catalog.iso19139.SV_Service.specification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Service.theSV_Port&quot; '>i18n.catalog.iso19139.SV_Service.theSV_Port</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Port&quot; '>i18n.catalog.iso19139.SV_Port</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Port.theSV_Interface&quot; '>i18n.catalog.iso19139.SV_Port.theSV_Interface</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceType&quot; '>i18n.catalog.iso19139.SV_ServiceType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceSpecification&quot; '>i18n.catalog.iso19139.SV_ServiceSpecification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceSpecification.name&quot; '>i18n.catalog.iso19139.SV_ServiceSpecification.name</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceSpecification.opModel&quot; '>i18n.catalog.iso19139.SV_ServiceSpecification.opModel</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceSpecification.typeSpec&quot; '>i18n.catalog.iso19139.SV_ServiceSpecification.typeSpec</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceSpecification.theSV_Interface&quot; '>i18n.catalog.iso19139.SV_ServiceSpecification.theSV_Interface</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_PlatformNeutralServiceSpecification&quot; '>i18n.catalog.iso19139.SV_PlatformNeutralServiceSpecification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_PlatformNeutralServiceSpecification.name&quot; '>i18n.catalog.iso19139.SV_PlatformNeutralServiceSpecification.name</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_PlatformNeutralServiceSpecification.opModel&quot; '>i18n.catalog.iso19139.SV_PlatformNeutralServiceSpecification.opModel</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_PlatformNeutralServiceSpecification.serviceType&quot; '>i18n.catalog.iso19139.SV_PlatformNeutralServiceSpecification.serviceType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_PlatformNeutralServiceSpecification.implSpec&quot; '>i18n.catalog.iso19139.SV_PlatformNeutralServiceSpecification.implSpec</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_PlatformSpecificServiceSpecification&quot; '>i18n.catalog.iso19139.SV_PlatformSpecificServiceSpecification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_PlatformSpecificServiceSpecification.name&quot; '>i18n.catalog.iso19139.SV_PlatformSpecificServiceSpecification.name</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_PlatformSpecificServiceSpecification.opModel&quot; '>i18n.catalog.iso19139.SV_PlatformSpecificServiceSpecification.opModel</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_PlatformSpecificServiceSpecification.serviceType&quot; '>i18n.catalog.iso19139.SV_PlatformSpecificServiceSpecification.serviceType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_PlatformSpecificServiceSpecification.implSpec&quot; '>i18n.catalog.iso19139.SV_PlatformSpecificServiceSpecification.implSpec</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_PlatformSpecificServiceSpecification.DCP&quot; '>i18n.catalog.iso19139.SV_PlatformSpecificServiceSpecification.DCP</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_PlatformSpecificServiceSpecification.implementation&quot; '>i18n.catalog.iso19139.SV_PlatformSpecificServiceSpecification.implementation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ParameterDirection&quot; '>i18n.catalog.iso19139.SV_ParameterDirection</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Paramater&quot; '>i18n.catalog.iso19139.SV_Paramater</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Paramater.name&quot; '>i18n.catalog.iso19139.SV_Paramater.name</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Paramater.direction&quot; '>i18n.catalog.iso19139.SV_Paramater.direction</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Paramater.description&quot; '>i18n.catalog.iso19139.SV_Paramater.description</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Paramater.optionality&quot; '>i18n.catalog.iso19139.SV_Paramater.optionality</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Paramater.repeatability&quot; '>i18n.catalog.iso19139.SV_Paramater.repeatability</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_Paramater.valueType&quot; '>i18n.catalog.iso19139.SV_Paramater.valueType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DCPList&quot; '>i18n.catalog.iso19139.DCPList</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DCPList.XML&quot; '>i18n.catalog.iso19139.DCPList.XML</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DCPList.CORBA&quot; '>i18n.catalog.iso19139.DCPList.CORBA</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DCPList.JAVA&quot; '>i18n.catalog.iso19139.DCPList.JAVA</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DCPList.COM&quot; '>i18n.catalog.iso19139.DCPList.COM</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DCPList.SQL&quot; '>i18n.catalog.iso19139.DCPList.SQL</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.DCPList.WebServices&quot; '>i18n.catalog.iso19139.DCPList.WebServices</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_OperationMetadata&quot; '>i18n.catalog.iso19139.SV_OperationMetadata</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_OperationMetadata.operationName&quot; '>i18n.catalog.iso19139.SV_OperationMetadata.operationName</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_OperationMetadata.DCP&quot; '>i18n.catalog.iso19139.SV_OperationMetadata.DCP</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_OperationMetadata.operationDescription&quot; '>i18n.catalog.iso19139.SV_OperationMetadata.operationDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_OperationMetadata.invocationName&quot; '>i18n.catalog.iso19139.SV_OperationMetadata.invocationName</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_OperationMetadata.parameters&quot; '>i18n.catalog.iso19139.SV_OperationMetadata.parameters</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_OperationMetadata.connectPoint&quot; '>i18n.catalog.iso19139.SV_OperationMetadata.connectPoint</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_OperationMetadata.dependsOn&quot; '>i18n.catalog.iso19139.SV_OperationMetadata.dependsOn</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_CoupledResource&quot; '>i18n.catalog.iso19139.SV_CoupledResource</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_CoupledResource.operationName&quot; '>i18n.catalog.iso19139.SV_CoupledResource.operationName</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_CoupledResource.identifier&quot; '>i18n.catalog.iso19139.SV_CoupledResource.identifier</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_OperationChainMetadata&quot; '>i18n.catalog.iso19139.SV_OperationChainMetadata</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_OperationChainMetadata.name&quot; '>i18n.catalog.iso19139.SV_OperationChainMetadata.name</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_OperationChainMetadata.description&quot; '>i18n.catalog.iso19139.SV_OperationChainMetadata.description</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_OperationChainMetadata.operation&quot; '>i18n.catalog.iso19139.SV_OperationChainMetadata.operation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_OperationChain&quot; '>i18n.catalog.iso19139.SV_OperationChain</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_OperationChain.name&quot; '>i18n.catalog.iso19139.SV_OperationChain.name</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_OperationChain.description&quot; '>i18n.catalog.iso19139.SV_OperationChain.description</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_OperationChain.operation&quot; '>i18n.catalog.iso19139.SV_OperationChain.operation</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceIdentification&quot; '>i18n.catalog.iso19139.SV_ServiceIdentification</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceIdentification.serviceType&quot; '>i18n.catalog.iso19139.SV_ServiceIdentification.serviceType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceIdentification.serviceTypeVersion&quot; '>i18n.catalog.iso19139.SV_ServiceIdentification.serviceTypeVersion</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceIdentification.accessProperties&quot; '>i18n.catalog.iso19139.SV_ServiceIdentification.accessProperties</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceIdentification.restrictions&quot; '>i18n.catalog.iso19139.SV_ServiceIdentification.restrictions</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceIdentification.keywords&quot; '>i18n.catalog.iso19139.SV_ServiceIdentification.keywords</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceIdentification.extent&quot; '>i18n.catalog.iso19139.SV_ServiceIdentification.extent</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceIdentification.coupledResource&quot; '>i18n.catalog.iso19139.SV_ServiceIdentification.coupledResource</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceIdentification.couplingType&quot; '>i18n.catalog.iso19139.SV_ServiceIdentification.couplingType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceIdentification.containsOperations&quot; '>i18n.catalog.iso19139.SV_ServiceIdentification.containsOperations</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_ServiceIdentification.operatesOn&quot; '>i18n.catalog.iso19139.SV_ServiceIdentification.operatesOn</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_CouplingType&quot; '>i18n.catalog.iso19139.SV_CouplingType</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_CouplingType.loose&quot; '>i18n.catalog.iso19139.SV_CouplingType.loose</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_CouplingType.mixed&quot; '>i18n.catalog.iso19139.SV_CouplingType.mixed</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.SV_CouplingType.tight&quot; '>i18n.catalog.iso19139.SV_CouplingType.tight</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_GeographicDescription&quot; '>i18n.catalog.iso19139.EX_GeographicDescription</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_GeographicDescription.geographicIdentifier&quot; '>i18n.catalog.iso19139.EX_GeographicDescription.geographicIdentifier</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.EX_VerticalExtent.verticalCRS.href&quot; '>i18n.catalog.iso19139.EX_VerticalExtent.verticalCRS.href</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.TM_Primitive.indeterminatePosition&quot; '>i18n.catalog.iso19139.TM_Primitive.indeterminatePosition</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.TM_Primitive.indeterminatePosition.before&quot; '>i18n.catalog.iso19139.TM_Primitive.indeterminatePosition.before</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.TM_Primitive.indeterminatePosition.after&quot; '>i18n.catalog.iso19139.TM_Primitive.indeterminatePosition.after</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.TM_Primitive.indeterminatePosition.now&quot; '>i18n.catalog.iso19139.TM_Primitive.indeterminatePosition.now</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.TM_Primitive.indeterminatePosition.unknown&quot; '>i18n.catalog.iso19139.TM_Primitive.indeterminatePosition.unknown</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.object.uuidref&quot; '>i18n.catalog.iso19139.object.uuidref</xsl:when>
      <xsl:when test=' $key = &quot;catalog.iso19139.object.xlink.href&quot; '>i18n.catalog.iso19139.object.xlink.href</xsl:when>
      <!-- ISO 639-2 language code -->
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.ger&quot; '>i18n.catalog.mdCode.language.iso639_2.ger</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.dut&quot; '>i18n.catalog.mdCode.language.iso639_2.dut</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.eng&quot; '>i18n.catalog.mdCode.language.iso639_2.eng</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.fre&quot; '>i18n.catalog.mdCode.language.iso639_2.fre</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.ita&quot; '>i18n.catalog.mdCode.language.iso639_2.ita</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.kor&quot; '>i18n.catalog.mdCode.language.iso639_2.kor</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.lit&quot; '>i18n.catalog.mdCode.language.iso639_2.lit</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.nor&quot; '>i18n.catalog.mdCode.language.iso639_2.nor</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.pol&quot; '>i18n.catalog.mdCode.language.iso639_2.pol</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.por&quot; '>i18n.catalog.mdCode.language.iso639_2.por</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.rus&quot; '>i18n.catalog.mdCode.language.iso639_2.rus</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.spa&quot; '>i18n.catalog.mdCode.language.iso639_2.spa</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.swe&quot; '>i18n.catalog.mdCode.language.iso639_2.swe</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.tur&quot; '>i18n.catalog.mdCode.language.iso639_2.tur</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.chi&quot; '>i18n.catalog.mdCode.language.iso639_2.chi</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.bul&quot; '>i18n.catalog.mdCode.language.iso639_2.bul</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.cze&quot; '>i18n.catalog.mdCode.language.iso639_2.cze</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.dan&quot; '>i18n.catalog.mdCode.language.iso639_2.dan</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.est&quot; '>i18n.catalog.mdCode.language.iso639_2.est</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.fin&quot; '>i18n.catalog.mdCode.language.iso639_2.fin</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.gre&quot; '>i18n.catalog.mdCode.language.iso639_2.gre</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.hun&quot; '>i18n.catalog.mdCode.language.iso639_2.hun</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.gle&quot; '>i18n.catalog.mdCode.language.iso639_2.gle</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.lav&quot; '>i18n.catalog.mdCode.language.iso639_2.lav</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.mlt&quot; '>i18n.catalog.mdCode.language.iso639_2.mlt</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.rum&quot; '>i18n.catalog.mdCode.language.iso639_2.rum</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.slo&quot; '>i18n.catalog.mdCode.language.iso639_2.slo</xsl:when>
      <xsl:when test=' $key = &quot;catalog.mdCode.language.iso639_2.slv&quot; '>i18n.catalog.mdCode.language.iso639_2.slv</xsl:when>
      <xsl:when test=' $key = &quot;catalog.gemini.MD_Metadata.hierarchyLevel&quot; '>i18n.catalog.gemini.MD_Metadata.hierarchyLevel</xsl:when>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
