<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gml32="http://www.opengis.net/gml/3.2"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xlink="http://www.w3.org/1999/xlink">
  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
  <xsl:strip-space elements="*"/>

  <xsl:template name="writeBaseInfo">
    <xsl:call-template name="writeGeneralInfo"/>
    <xsl:call-template name="writeIdentificationInfo"/>
    <xsl:call-template name="writeSubject"/>
    <xsl:call-template name="writeDates"/>
    <xsl:call-template name="writeLanguage"/>
    <xsl:call-template name="writeServiceInfo"/>
    <xsl:call-template name="writeConstraints"/>
    <xsl:call-template name="writeInspireInfo"/>
    <xsl:call-template name="writeSpatialInfo"/>
    <xsl:call-template name="writeTemporalInfo"/>
    <xsl:call-template name="writeBandInfo"/>
    <xsl:call-template name="writeGridInfo"/>
  </xsl:template>
  
  <xsl:template name="writeBandInfo">
    <xsl:for-each select="//gmd:MD_Band/gmd:sequenceIdentifier/gco:MemberName/gco:aName/gco:CharacterString">
      <field name="mdband.name_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:MD_Band/gmd:sequenceIdentifier/gco:MemberName/gco:attributeType/gco:TypeName/gco:aName/gco:CharacterString">
      <field name="mdband.attrtype_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:MD_Band/gmd:descriptor/gco:CharacterString">
      <field name="mdband.desc_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:MD_Band/gmd:units/@xlink:href">
      <field name="mdband.units_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="writeConstraints">
    <xsl:choose>
      <xsl:when test="//gmd:resourceConstraints">
        <field name="apiso.HasSecurityConstraints_b">true</field>
      </xsl:when>
      <xsl:otherwise>
        <field name="apiso.HasSecurityConstraints_b">false</field>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:for-each select="//gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString">
      <field name="apiso.AccessConstraints_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString">
      <field name="apiso.OtherConstraints_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:classification/gmd:MD_ClassificationCode/@codeListValue">
      <field name="apiso.Classification_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="writeCRS">
    <xsl:for-each select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier">
      <field name="apiso.CRS.Authority_ss">
        <xsl:value-of select="gmd:authority/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
      </field>
      <field name="apiso.CRS.ID_ss">
        <xsl:value-of select="gmd:code/gco:CharacterString"/>
      </field>
      <field name="apiso.CRS.Version_ss">
        <xsl:value-of select="gmd:version/gco:CharacterString"/>
      </field>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="writeDates">
    <field name="apiso.Modified_tdt" gc-instruction="checkIsoDateTime">
      <xsl:value-of select="gmd:dateStamp/gco:Date | gmd:dateStamp/gco:DateTime"/>
    </field>
    <field name="apiso.CreationDate_tdt" gc-instruction="checkIsoDateTime">
      <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date[../../gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation'] | gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date[../../gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']"/>
    </field>
    <field name="apiso.RevisionDate_tdt" gc-instruction="checkIsoDateTime">
      <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date[../../gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision'] | gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date[../../gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']"/>
    </field>
    <field name="apiso.PublicationDate_tdt" gc-instruction="checkIsoDateTime">
      <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date[../../gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='publication'] | gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date[../../gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='publication']"/>
    </field>
  </xsl:template>
  
  <xsl:template name="writeGeometry">
    <xsl:param name="fieldName"/>
    <xsl:for-each select="//gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
      <field>
        <xsl:attribute name="name">
          <xsl:value-of select="$fieldName"/>
        </xsl:attribute>
        <xsl:attribute name="gc-instruction">
          <xsl:value-of select="'checkGeoEnvelope'"/>
        </xsl:attribute>
        <xsl:value-of select="normalize-space(gmd:westBoundLongitude/gco:Decimal)"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="normalize-space(gmd:southBoundLatitude/gco:Decimal)"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="normalize-space(gmd:eastBoundLongitude/gco:Decimal)"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="normalize-space(gmd:northBoundLatitude/gco:Decimal)"/>
      </field>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="writeGeneralInfo">
    <field name="id.fileid_s">
      <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/>
    </field>
    <field name="title">
      <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString | gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
    </field>
    <field name="description">
      <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString | gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:abstract/gco:CharacterString"/>
    </field>
    <xsl:for-each select="//gmd:CI_OnlineResource/gmd:linkage/gmd:URL">
      <field name="links">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <field name="url.thumbnail_s">
      <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
    </field>
    <field name="keywords">
      <xsl:for-each select="//gmd:MD_TopicCategoryCode | 
                            //gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString">
        <xsl:value-of select="current()"/>
        <xsl:text> </xsl:text>
      </xsl:for-each>
    </field>
    <xsl:for-each select="//gmd:MD_TopicCategoryCode | 
                          //gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString">
      <field name="keywords_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
     <xsl:for-each select="//gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString">
      <field name="contact.organizations_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
     <xsl:for-each select="//gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString">
      <field name="contact.people_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="writeGridInfo">
    <xsl:for-each select="//gmd:MD_GridSpatialRepresentation/gmd:numberOfDimensions/gco:Integer">
      <field name="grid.dimensions.num_is">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:MD_GridSpatialRepresentation/gmd:axisDimensionProperties/gmd:MD_Dimension/gco:Integer/gmd:dimensionName/gmd:MD_DimensionNameTypeCode">
      <field name="grid.dimension.name_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:MD_GridSpatialRepresentation/gmd:axisDimensionProperties/gmd:MD_Dimension/gco:Integer/gmd:dimensionSize/gco:Integer">
      <field name="grid.dimension.size_is">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:MD_GridSpatialRepresentation/gmd:cellGeometry/gmd:MD_CellGeometryCode/@codeListValue">
      <field name="grid.cell.geometry_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="writeIdentificationInfo">
    <field name="apiso.Identifier_s">
      <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/>
    </field>
    <field name="apiso.ParentIdentifier_s">
      <xsl:value-of select="gmd:parentIdentifier/gco:CharacterString"/>
    </field>
    <field name="apiso.ResourceIdentifier_s">
      <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString | gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString"/>
    </field>
    <field name="apiso.Title_t">
      <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString | gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
    </field>
    <xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString | gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString">
      <field name="apiso.AlternateTitle_txt">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <field name="apiso:Abstract_t">
      <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString | gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:abstract/gco:CharacterString"/>
    </field>
    <field name="apiso.OrganizationName_t">
      <xsl:value-of select="gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString"/>
    </field>
    <field name="apiso.BrowseGraphic_s">
      <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
    </field>
  </xsl:template>
  
  <xsl:template name="writeInspireInfo">
    <!--
    apiso:Degree apiso:Lineage apiso:ResponsiblePartyRole
    apiso.inspireSpatialDataThemes 
    apiso:SpecificationTitle apiso:SpecificationDate apiso:SpecificationDateType
    apiso:ConditionApplyingToAccessAndUse (same as OtherConstraints)
    apiso:AccessConstraints apiso:OtherConstraints apiso:Classification 
    -->
    <xsl:for-each select="//gmd:title[gco:CharacterString='GEMET - INSPIRE themes, version 1.0']/../../../gmd:keyword/gco:CharacterString">
      <field name="apiso.InspireSpatialDataThemes_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:pass/gco:Boolean">
      <field name="apiso.Degree_b">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString">
      <field name="apiso.Lineage_txt">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString">
      <!-- TODO: Same as OtherConstraints? -->
      <field name="apiso.ConditionApplyingToAccessAndUse_txt">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <field name="apiso.ResponsiblePartyRole_t">
      <!-- TODO: applies to datasets only? -->
      <xsl:choose>
        <xsl:when test="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue">
          <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode"/>
        </xsl:otherwise>
      </xsl:choose>
    </field>
    <xsl:for-each select="//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString">
      <field name="apiso.SpecificationTitle_txt">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/date/gco:Date">
      <!-- TODO: date type -->
      <field name="apiso.SpecificationDate_dts" gc-instruction="checkIsoDateTime">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue">
      <field name="apiso.SpecificationDateType_txt">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="writeLanguage">
    <field name="apiso.Language_s">
      <xsl:choose>
        <xsl:when test="gmd:language/gmd:LanguageCode/@codeListValue">
          <xsl:value-of select="gmd:language/gmd:LanguageCode/@codeListValue"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="gmd:language/gmd:LanguageCode"/>
        </xsl:otherwise>
      </xsl:choose>
    </field>
    <field name="apiso.ResourceLanguage_s">
      <xsl:choose>
        <xsl:when test="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:language/gmd:LanguageCode/@codeListValue">
          <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:language/gmd:LanguageCode/@codeListValue"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:language/gmd:LanguageCode"/>
        </xsl:otherwise>
      </xsl:choose>
    </field>
  </xsl:template>
  
  <xsl:template name="writeServiceInfo">
    <xsl:for-each select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName">
      <field name="apiso.ServiceType_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    
    <xsl:for-each select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName[contains(translate(.,'MAPSERV', 'mapserv'),'mapserver')]">
			<field name="dataAccessType_ss">ArcGIS MapServer</field>
			<field name="url.mapserver_ss">
				<xsl:value-of select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL[contains(translate(.,'MAPSERV', 'mapserv'),'mapserver')]"/>
			</field>
		</xsl:for-each>
		<xsl:for-each select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName[contains(translate(.,'WMS', 'wms'),'wms')]">
			<field name="dataAccessType_ss">WMS</field>
			<field name="url.wms_ss">
				<xsl:value-of select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL[contains(translate(.,'WMS', 'wms'),'wms')]"/>
			</field>
		</xsl:for-each>
		<xsl:for-each select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName[contains(translate(.,'WFS', 'wfs'),'wfs')]">
			<field name="dataAccessType_ss">WFS</field>
			<field name="url.wfs_ss">
				<xsl:value-of select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL[contains(translate(.,'WFS', 'wfs'),'wfs')]"/>
			</field>
		</xsl:for-each>
		<xsl:for-each select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName[contains(translate(.,'KML', 'kml'),'kml')] | //gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName[contains(translate(.,'KMZ', 'kmz'),'kmz')]">
			<field name="dataAccessType_ss">KML</field>
			<field name="url.kml_ss">
				<xsl:value-of select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL[contains(translate(.,'KML', 'kml'),'kml')] | //gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL[contains(translate(.,'KMZ', 'kmz'),'kmz')]"/>
			</field>
		</xsl:for-each>
		<xsl:for-each select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName[contains(translate(.,'JSON', 'json'),'json')]">
			<field name="dataAccessType_ss">JSON</field>
			<field name="url.json_ss">
				<xsl:value-of select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL[contains(translate(.,'JSON', 'json'),'json')]"/>
			</field>
		</xsl:for-each>
		<xsl:for-each select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName[contains(translate(.,'SO', 'so'),'sos')]">
			<field name="dataAccessType_ss">SOS</field>
			<field name="url.sos_ss">
				<xsl:value-of select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL[contains(translate(.,'SO', 'so'),'sos')]"/>
			</field>
		</xsl:for-each>
		<xsl:for-each select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName[contains(translate(.,'THREDS', 'threds'),'thredds')]">
			<field name="dataAccessType_ss">THREDDS</field>
			<field name="url.thredds_ss">
				<xsl:value-of select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL[contains(translate(.,'THREDS', 'threds'),'thredds')]"/>
			</field>
		</xsl:for-each>
		
    <xsl:for-each select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceTypeVersion/gco:CharacterString">
      <field name="apiso.ServiceTypeVersion_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:operationName/gco:CharacterString">
      <field name="apiso.Operation_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:operatesOn/@uuidref | //gmd:identificationInfo/srv:SV_ServiceIdentification/srv:operatesOn/@xlink:href">
      <field name="apiso.OperatesOn_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:coupledResource/srv:SV_CoupledResource/srv:identifier">
      <field name="apiso.OperatesOnIdentifier_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:coupledResource/srv:SV_CoupledResource/srv:operationName">
      <field name="apiso.OperatesOnName_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:couplingType/srv:SV_CouplingType/@codeListValue">
      <field name="apiso.CouplingType_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="writeSpatialInfo">
    <xsl:call-template name="writeCRS"/>
    <xsl:call-template name="writeGeometry">
      <xsl:with-param name="fieldName">envelope_geo</xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="writeGeometry">
      <xsl:with-param name="fieldName">apiso:BoundingBox_geo</xsl:with-param>
    </xsl:call-template>    
    <xsl:for-each select="//gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code">
      <field name="apiso.GeographicDescriptionCode_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <field name="apiso.Denominator_ti">
      <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer"/>
    </field>
    <field name="apiso.DistanceValue_td">
      <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance"/>
    </field>
    <field name="apiso.DistanceUOM_s">
      <xsl:value-of select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance/@uom"/>
    </field>
  </xsl:template>
  
  <xsl:template name="writeSubject">
    <field name="apiso.Subject_t">
      <xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:topicCategory/gmd:MD_TopicCategoryCode | gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString | gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString">
        <xsl:value-of select="current()"/>
        <xsl:text> </xsl:text>
      </xsl:for-each>
    </field>
    <xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/name/gco:CharacterString">
      <field name="apiso.Format_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <field name="apiso.Type_s">
      <xsl:choose>
        <xsl:when test="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue">
          <xsl:value-of select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="gmd:hierarchyLevel/gmd:MD_ScopeCode"/>
        </xsl:otherwise>
      </xsl:choose>
    </field>
    <xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:topicCategory/gmd:MD_TopicCategoryCode">
      <field name="apiso.TopicCategory_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue">
      <field name="apiso.KeywordType_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="writeTemporalInfo">
    <xsl:for-each select="//gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition">
      <field name="apiso.TempExtent_begin_dts" gc-instruction="checkIsoDateTime">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition">
      <field name="apiso.TempExtent_begin_dts" gc-instruction="checkIsoDateTime">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:begin/gml32:TimeInstant/gml32:timePosition">
      <field name="apiso.TempExtent_begin_dts" gc-instruction="checkIsoDateTime">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:beginPosition">
      <field name="apiso.TempExtent_begin_dts" gc-instruction="checkIsoDateTime">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>

    <xsl:for-each select="//gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition">
      <field name="apiso.TempExtent_end_dts" gc-instruction="checkIsoDateTime.end">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition">
      <field name="apiso.TempExtent_end_dts" gc-instruction="checkIsoDateTime.end">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:end/gml32:TimeInstant/gml32:timePosition">
      <field name="apiso.TempExtent_end_dts" gc-instruction="checkIsoDateTime.end">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="//gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:endPosition">
      <field name="apiso.TempExtent_end_dts" gc-instruction="checkIsoDateTime.end">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>   
    
    <field name="apiso.TempExtent_begin_tdt" gc-instruction="checkIsoDateTime">
      <xsl:value-of select="//gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition |
                            //gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition |
                            //gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:begin/gml32:TimeInstant/gml32:timePosition |
                            //gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:beginPosition"/>
    </field>
    <field name="apiso.TempExtent_end_tdt" gc-instruction="checkIsoDateTime.end">
      <xsl:value-of select="//gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition |
                            //gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition |
                            //gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:end/gml32:TimeInstant/gml32:timePosition |
                            //gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:endPosition"/>
    </field> 
  </xsl:template>

</xsl:stylesheet>
