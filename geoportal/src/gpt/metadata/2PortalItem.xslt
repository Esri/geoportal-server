<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gml32="http://www.opengis.net/gml/3.2"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmi="http://www.isotc211.org/2005/gmi"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:Val="com.esri.gpt.framework.util.Val">
  <xsl:output method="text" indent="yes" encoding="UTF-8"/>
  <xsl:strip-space elements="*"/>
  
  <xsl:template match="/gmd:MD_Metadata | /gmi:MI_Metadata | /metadata">
    <xsl:text>{</xsl:text>
      <xsl:call-template name="writeInfo"/>
    <xsl:text>&#10;}</xsl:text>   
  </xsl:template>
  
  <xsl:template match="*" /> <!-- catch-all: override built-in templates -->
  
  <xsl:template name="writeInfo">
    
    <xsl:text>&#10;&#x9;"title": "</xsl:text>
    <xsl:call-template name="esc">
      <xsl:with-param name="text" 
        select="idinfo/citation/citeinfo/title | 
                gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString | 
                gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString" />
    </xsl:call-template>
    <xsl:text>",</xsl:text>
    
    <xsl:text>&#10;&#x9;"description": "</xsl:text>
    <xsl:call-template name="esc">
      <xsl:with-param name="text" 
        select="idinfo/descript/abstract | 
                gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString | 
                gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:abstract/gco:CharacterString" />
    </xsl:call-template>
    <xsl:text>",</xsl:text>
    
    <xsl:text>&#10;&#x9;"snippet": "</xsl:text>
    <xsl:call-template name="esc">
      <xsl:with-param name="text" 
        select="idinfo/descript/purpose |
                gmd:identificationInfo/gmd:MD_DataIdentification/gmd:purpose/gco:CharacterString |
                gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:purpose/gco:CharacterString" />
    </xsl:call-template>
    <xsl:text>",</xsl:text>
    
    <xsl:text>&#10;&#x9;"accessInformation": "</xsl:text>
    <xsl:call-template name="esc">
      <xsl:with-param name="text" 
        select="idinfo/datacred |
                gmd:identificationInfo/gmd:MD_DataIdentification/gmd:credit/gco:CharacterString |
                gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:credit/gco:CharacterString" />
    </xsl:call-template>
    <xsl:text>",</xsl:text>
    
    <xsl:text>&#10;&#x9;"licenseInfo": "</xsl:text>
    <xsl:call-template name="esc">
      <xsl:with-param name="text" 
        select="idinfo/useconst |
                gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString |
                gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation/gco:CharacterString |
                gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:useLimitation/gco:CharacterString |
                gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString |
                gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation/gco:CharacterString |
                gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:useLimitation/gco:CharacterString" />
    </xsl:call-template>
    <xsl:text>",</xsl:text>
    
    <xsl:text>&#10;&#x9;"tags": [</xsl:text>
    <xsl:for-each 
        select="idinfo/keywords/theme/themekey |
                idinfo/keywords/place/placekey | 
                idinfo/keywords/stratum/stratkey | 
                idinfo/keywords/temporal/tempkey |
                gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword |
                gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword">
      <xsl:text>&#10;&#x9;&#x9;"</xsl:text>
      <xsl:call-template name="esc">
        <xsl:with-param name="text" select="current()" />
      </xsl:call-template>
      <xsl:text>",</xsl:text>
    </xsl:for-each>
    <xsl:text>&#10;&#x9;],</xsl:text>

    <xsl:text>&#10;&#x9;"_minX": "</xsl:text>
    <xsl:call-template name="esc">
      <xsl:with-param name="text" 
        select="idinfo/spdom/bounding/westbc | 
                gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal | 
                gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal" />
    </xsl:call-template>
    <xsl:text>",</xsl:text>
    
    <xsl:text>&#10;&#x9;"_minY": "</xsl:text>
    <xsl:call-template name="esc">
      <xsl:with-param name="text" 
        select="idinfo/spdom/bounding/southbc | 
                gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal | 
                gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal" />
    </xsl:call-template>
    <xsl:text>",</xsl:text>
    
    <xsl:text>&#10;&#x9;"_maxX": "</xsl:text>
    <xsl:call-template name="esc">
      <xsl:with-param name="text" 
        select="idinfo/spdom/bounding/eastbc | 
                gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal | 
                gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal" />
    </xsl:call-template>
    <xsl:text>",</xsl:text>
    
    <xsl:text>&#10;&#x9;"_maxY": "</xsl:text>
    <xsl:call-template name="esc">
      <xsl:with-param name="text" 
        select="idinfo/spdom/bounding/northbc | 
                gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal | 
                gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal" />
    </xsl:call-template>
    <xsl:text>",</xsl:text>
    
    <xsl:text>&#10;&#x9;"_thumbnailurl": "</xsl:text>
    <xsl:call-template name="esc">
      <xsl:with-param name="text" 
        select="idinfo/browse/browsen" />
    </xsl:call-template>
    <xsl:text>",</xsl:text>
   
    <xsl:text>&#10;&#x9;"_links": [</xsl:text>
    <xsl:for-each 
        select="idinfo/citation/citeinfo/onlink | 
                distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr | 
                gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL | 
                gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL">
      <xsl:text>&#10;&#x9;&#x9;"</xsl:text>
      <xsl:call-template name="esc">
        <xsl:with-param name="text" select="current()" />
      </xsl:call-template>
      <xsl:text>",</xsl:text>
    </xsl:for-each>
    <xsl:text>&#10;&#x9;]</xsl:text>

  </xsl:template>
  
  <xsl:template name="esc">
    <xsl:param name="text" />
    <xsl:value-of select="Val:escapeStrForJson($text)"/>
  </xsl:template>
  
  <!--
  
    <xsl:variable name="title">
      <xsl:call-template name="esc">
        <xsl:with-param name="text" select="idinfo/citation/citeinfo/title | 
                                            gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString | 
                                            gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString" />
      </xsl:call-template>
    </xsl:variable>
    "title":"<xsl:value-of select="$title"/>",
    
    <xsl:text>&#10;&#x9;"extent": </xsl:text>
    <xsl:text>[[</xsl:text>
    <xsl:text>null</xsl:text>
    <xsl:text>,</xsl:text>
    <xsl:text>null</xsl:text>
    <xsl:text>],[</xsl:text>
    <xsl:text>null</xsl:text>
    <xsl:text>,</xsl:text>
    <xsl:text>null</xsl:text>
    <xsl:text>]],</xsl:text>
    
  <xsl:template name="esc">
    <xsl:param name="text" />
    <xsl:value-of select="Val:escapeStrForJson($text)"/>
    <xsl:call-template name="replace-string">
      <xsl:with-param name="text" select="$text" />
      <xsl:with-param name="replace" select="'&#xD;'" />
      <xsl:with-param name="with" select="'\\r'" />
    </xsl:call-template>
    <xsl:call-template name="replace-string">
      <xsl:with-param name="text" select="$text" />
      <xsl:with-param name="replace" select="'&quot;'" />
      <xsl:with-param name="with" select="'\&quot;'" />
    </xsl:call-template>
    <xsl:call-template name="replace-string">
      <xsl:with-param name="text" select="$text" />
      <xsl:with-param name="replace" select="'&#xA;'" />
      <xsl:with-param name="with" select="'\n'" />
    </xsl:call-template>
    <xsl:call-template name="replace-string">
      <xsl:with-param name="text" select="$text" />
      <xsl:with-param name="replace" select="'&#xD;'" />
      <xsl:with-param name="with" select="'\r'" />
    </xsl:call-template>
    <xsl:call-template name="replace-string">
      <xsl:with-param name="text" select="$text" />
      <xsl:with-param name="replace" select="'&#x9;'" />
      <xsl:with-param name="with" select="'\t'" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="replace-string">
    <xsl:param name="text" />
    <xsl:param name="replace" />
    <xsl:param name="with" />
    <xsl:choose>
      <xsl:when test="contains($text,$replace)">
        <xsl:value-of select="substring-before($text,$replace)" />
        <xsl:value-of select="$with" />
        <xsl:call-template name="replace-string">
          <xsl:with-param name="text" select="substring-after($text,$replace)" />
          <xsl:with-param name="replace" select="$replace" />
          <xsl:with-param name="with" select="$with" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  -->
  
</xsl:stylesheet>