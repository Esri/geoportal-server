<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="no" encoding="UTF-8" omit-xml-declaration="yes"/>
  <xsl:template match="/">
    <xsl:element name="csw:GetRecords" use-attribute-sets="GetRecordsAttributes" xmlns:csw="http://www.opengis.net/cat/csw">
      <csw:Query typeNames="csw:Record">
        <csw:ElementSetName>brief</csw:ElementSetName>
        <csw:Constraint version="1.0.0">
          <Filter xmlns="http://www.opengis.net/ogc">
            <PropertyIsGreaterThan>
              <PropertyName>Modified</PropertyName>
              <Literal><xsl:apply-templates select="/GetRecords/FromDate"/></Literal>
            </PropertyIsGreaterThan>
          </Filter>
        </csw:Constraint>
      </csw:Query>
    </xsl:element>
  </xsl:template>

  <xsl:attribute-set name="GetRecordsAttributes">
    <xsl:attribute name="version">2.0.0</xsl:attribute>
    <xsl:attribute name="service">CSW</xsl:attribute>
    <xsl:attribute name="resultType">RESULTS</xsl:attribute>
    <xsl:attribute name="startPosition"><xsl:value-of select="/GetRecords/StartPosition"/></xsl:attribute>
    <xsl:attribute name="maxRecords"><xsl:value-of select="/GetRecords/MaxRecords"/></xsl:attribute>
    <xsl:attribute name="outputSchema">csw:Record</xsl:attribute>
  </xsl:attribute-set>

  <!-- FromDate: if empty, use 1900-01-01 as default -->
  <xsl:template match="/GetRecords/FromDate">
    <xsl:choose>
      <xsl:when test="normalize-space(.)=''">1900-01-01</xsl:when>
      <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
