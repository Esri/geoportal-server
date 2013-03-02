<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:ows="http://www.opengis.net/ows"
                exclude-result-prefixes="csw dct">
  <xsl:output method="text" indent="yes" encoding="UTF-8" omit-xml-declaration="yes"/>
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
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="/agolResponse/itemUrl">
    <xsl:value-of select="."/>
     <xsl:text>&#x2714;</xsl:text>
     <xsl:value-of select="@scheme"/>
     <xsl:text>&#x2715;</xsl:text>
  </xsl:template>
</xsl:stylesheet>
