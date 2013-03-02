<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns:csw="http://www.opengis.net/cat/csw" xmlns:dc="http://purl.org/dc/elements/1.1/" exclude-result-prefixes="csw dc">
  <xsl:output method="xml" indent="no" encoding="UTF-8"/>
  <xsl:template match="/">
    <Records>
      <xsl:apply-templates select="/csw:GetRecordsResponse/csw:SearchResults/csw:BriefRecord/dc:identifier"/>
    </Records>
  </xsl:template>

  <xsl:template match="/csw:GetRecordsResponse/csw:SearchResults/csw:BriefRecord/dc:identifier">
    <Record><ID><xsl:value-of select="."/></ID></Record>
  </xsl:template>
</xsl:stylesheet>
