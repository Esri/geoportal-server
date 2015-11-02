<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dct="http://purl.org/dc/terms/" xmlns:ows="http://www.opengis.net/ows" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gmi="http://www.isotc211.org/2005/gmi" exclude-result-prefixes="csw dct">
  <xsl:output indent="yes" method="xml" omit-xml-declaration="no"/>
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="/csw:GetRecordByIdResponse/ows:ExceptionReport">
        <exception>
          <exceptionText>
            <xsl:for-each select="/ows:ExceptionReport/ows:Exception">
              <xsl:value-of select="ows:ExceptionText"/>
            </xsl:for-each>
          </exceptionText>
        </exception>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="/csw:GetRecordByIdResponse/gmd:MD_Metadata"/>
        <xsl:apply-templates select="/csw:GetRecordByIdResponse/gmi:MI_Metadata"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template> 
  <xsl:template match="/csw:GetRecordByIdResponse/gmd:MD_Metadata">
    <xsl:copy-of select="/csw:GetRecordByIdResponse/gmd:MD_Metadata"/>
  </xsl:template>
  <xsl:template match="/csw:GetRecordByIdResponse/gmi:MI_Metadata">
    <xsl:copy-of select="/csw:GetRecordByIdResponse/gmi:MI_Metadata"/>
  </xsl:template>
</xsl:stylesheet>
