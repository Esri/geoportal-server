<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
  <xsl:strip-space elements="*"/>
  <xsl:include href="fgdc-base-toSolr.xslt"/>

  <xsl:template match="/metadata">
  <doc>
    <xsl:call-template name="writeBaseInfo"/>
  </doc>    
  </xsl:template>

</xsl:stylesheet>
