<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/" xmlns:ows="http://www.opengis.net/ows">
  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
  <xsl:strip-space elements="*"/>

  <xsl:template name="writeBaseInfo">
    <xsl:call-template name="writeGeneralInfo"/>
    <xsl:call-template name="writeSpatialInfo"/>
    <xsl:call-template name="writeTemporalInfo"/>
  </xsl:template>
  
  <xsl:template name="writeGeometry">
<!--
	minx	<xsl:value-of select="substring-after(substring-before(./extent/text(),','),'[[')"/>
	miny	<xsl:value-of select="substring-before(substring-after(./extent/text(),','),']')"/>
	maxx	<xsl:value-of select="substring-before(substring-after(substring-after(substring-after(./extent/text(),'['),'['),'['),',')"/>
	maxy	<xsl:value-of select="substring-before(substring-after(substring-after(substring-after(substring-after(./extent/text(),'['),'['),'['),','),']]')"/>
--> 
  
    <xsl:param name="fieldName"/>
    <xsl:for-each select="/rdf:RDF/rdf:Description/ows:WGS84BoundingBox">
      <field>
        <xsl:attribute name="name">
          <xsl:value-of select="$fieldName"/>
        </xsl:attribute>
        <xsl:attribute name="gc-instruction">
          <xsl:value-of select="'checkGeoEnvelope'"/>
        </xsl:attribute>
        <xsl:value-of select="normalize-space(substring-before(ows:LowerCorner,' '))"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="normalize-space(substring-after(ows:LowerCorner,' '))"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="normalize-space(substring-before(ows:UpperCorner,' '))"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="normalize-space(substring-after(ows:UpperCorner,' '))"/>
      </field>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="writeGeneralInfo">
    <!--  id.fileid_s ??-->
    <field name="title"> <!-- _txt -->
      <xsl:value-of select="/rdf:RDF/rdf:Description/dc:title"/>
    </field>
    <field name="description"><!-- _t -->
      <xsl:value-of select="/rdf:RDF/rdf:Description/dct:abstract"/>
    </field>
    <xsl:for-each select="/rdf:RDF/rdf:Description/dct:references">
      <field name="links"><!-- _ss -->
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <field name="url.thumbnail_s">
      <xsl:value-of select="/rdf:RDF/rdf:Description/dct:references[@scheme='urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Thumbnail']"/>
    </field>
    <field name="keywords">
      <xsl:for-each select="/rdf:RDF/rdf:Description/dc:subject">
        <xsl:value-of select="current()"/>
        <xsl:text> </xsl:text>
      </xsl:for-each>
    </field>
    <xsl:for-each select="/rdf:RDF/rdf:Description/dc:subject">
      <field name="keywords_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>    
     <xsl:for-each select="/rdf:RDF/rdf:Description/dc:creator">
      <field name="contact.organizations_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
     <xsl:for-each select="/rdf:RDF/rdf:Description/dc:creator">
      <field name="contact.people_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="writeSpatialInfo">
   <xsl:call-template name="writeGeometry">
      <xsl:with-param name="fieldName">envelope_geo</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="writeTemporalInfo">
    <field name="apiso.TempExtent_begin_dts" gc-instruction="checkIsoDateTime">
      <xsl:value-of select="/rdf:RDF/rdf:Description/dc:date"/>
    </field>
    <field name="apiso.TempExtent_end_dts" gc-instruction="checkIsoDateTime">
      <xsl:value-of select="/rdf:RDF/rdf:Description/dc:date"/>
    </field>
  </xsl:template>

</xsl:stylesheet>
