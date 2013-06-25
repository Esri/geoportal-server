<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gml32="http://www.opengis.net/gml/3.2"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gmi="http://www.isotc211.org/2005/gmi"
                exclude-result-prefixes="gmd gmi gco gml gml32 srv xlink">
  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
  <xsl:strip-space elements="*"/>
  <xsl:include href="iso19115-base-toSolr.xslt"/>

  <xsl:template match="/gmd:MD_Metadata | /gmi:MI_Metadata">
  <doc>
    <xsl:call-template name="writeBaseInfo"/>
  </doc>    
  </xsl:template>

</xsl:stylesheet>
