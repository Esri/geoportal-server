<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
  <xsl:strip-space elements="*"/>

  <xsl:template name="writeBaseInfo">
    <xsl:call-template name="writeGeneralInfo"/>
    <xsl:call-template name="writeSpatialInfo"/>
    <xsl:call-template name="writeTemporalInfo"/>
	<xsl:call-template name="writeServiceInfo"/>
    
  </xsl:template>
  
  <xsl:template name="writeGeometry">
    <xsl:param name="fieldName"/>
    <xsl:for-each select="/metadata/idinfo/spdom/bounding">
      <field>
        <xsl:attribute name="name">
          <xsl:value-of select="$fieldName"/>
        </xsl:attribute>
        <xsl:attribute name="gc-instruction">
          <xsl:value-of select="'checkGeoEnvelope'"/>
        </xsl:attribute>
        <xsl:value-of select="normalize-space(westbc)"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="normalize-space(southbc)"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="normalize-space(eastbc)"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="normalize-space(northbc)"/>
      </field>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="writeGeneralInfo">
    <!--  id.fileid_s ??-->
    <field name="title"> <!-- _txt -->
      <xsl:value-of select="/metadata/idinfo/citation/citeinfo/title"/>
    </field>
    <field name="description"><!-- _t -->
      <xsl:value-of select="/metadata/idinfo/descript/abstract"/>
    </field>
    <xsl:for-each select="/metadata/idinfo/citation/citeinfo/onlink | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr">
      <field name="links"><!-- _ss -->
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <field name="url.thumbnail_s">
      <xsl:value-of select="/metadata/idinfo/browse/browsen"/>
    </field>
    <field name="keywords">
      <xsl:for-each select="/metadata/idinfo/keywords/theme/themekey | /metadata/idinfo/keywords/place/placekey">
        <xsl:value-of select="current()"/>
        <xsl:text> </xsl:text>
      </xsl:for-each>
    </field>
    <xsl:for-each select="/metadata/idinfo/keywords/theme/themekey | /metadata/idinfo/keywords/place/placekey">
      <field name="keywords_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>    
     <xsl:for-each select="//cntinfo/cntorgp/cntorg ">
      <field name="contact.organizations_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
     <xsl:for-each select="//cntinfo/cntorgp/cntper">
      <field name="contact.people_ss">
        <xsl:value-of select="current()"/>
      </field>
    </xsl:for-each>
    <xsl:for-each select="/metadata/distinfo/resdesc">
      <field name="contentType_ss">
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
    <field name="apiso.TempExtent_begin_dts" gc-instruction="checkFgdcDate">
      <xsl:value-of select="/metadata/idinfo/timeperd/timeinfo/rngdates/begdate | /metadata/idinfo/timeperd/timeinfo/sngdate/caldate"/>
    </field>
    <field name="apiso.TempExtent_end_dts" gc-instruction="checkFgdcDate.end">
      <xsl:value-of select="/metadata/idinfo/timeperd/timeinfo/rngdates/enddate | /metadata/idinfo/timeperd/timeinfo/sngdate/caldate"/>
    </field>

    <field name="apiso.TempExtent_begin_tdt" gc-instruction="checkFgdcDate">
      <xsl:value-of select="/metadata/idinfo/timeperd/timeinfo/rngdates/begdate | /metadata/idinfo/timeperd/timeinfo/sngdate/caldate"/>
    </field>
    <field name="apiso.TempExtent_end_tdt" gc-instruction="checkFgdcDate.end">
      <xsl:value-of select="/metadata/idinfo/timeperd/timeinfo/rngdates/enddate | /metadata/idinfo/timeperd/timeinfo/sngdate/caldate"/>
    </field>
  </xsl:template>
  <xsl:template name="writeServiceInfo">	
<xsl:for-each select="/metadata/idinfo/citation/citeinfo/onlink[contains(translate(.,'MAPSERV', 'mapserv'),'mapserver')] | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[contains(translate(.,'MAPSERV', 'mapserv'),'mapserver')]">
		<field name="dataAccessType_ss">ArcGIS MapServer</field>
			<field name="url.mapserver_ss">
				<xsl:value-of select="/metadata/idinfo/citation/citeinfo/onlink[contains(translate(.,'MAPSERV', 'mapserv'),'mapserver')] | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[contains(translate(.,'MAPSERV', 'mapserv'),'mapserver')]"/>
			</field>
		</xsl:for-each>

<xsl:for-each select="/metadata/idinfo/citation/citeinfo/onlink[contains(translate(.,'WMS', 'wms'),'wms')] | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[contains(translate(.,'WMS', 'wms'),'wms')]">
		<field name="dataAccessType_ss">WMS</field>
			<field name="url.wms_ss">
				<xsl:value-of select="/metadata/idinfo/citation/citeinfo/onlink[contains(translate(.,'WMS', 'wms'),'wms')] | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[contains(translate(.,'WMS', 'wms'),'wms')]"/>
			</field>
		</xsl:for-each>

<xsl:for-each select="/metadata/idinfo/citation/citeinfo/onlink[contains(translate(.,'WFS', 'wfs'),'wfs')] | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[contains(translate(.,'WFS', 'wfs'),'wfs')]">
		<field name="dataAccessType_ss">WFS</field>
			<field name="url.wfs_ss">
				<xsl:value-of select="/metadata/idinfo/citation/citeinfo/onlink[contains(translate(.,'WFS', 'wfs'),'wfs')] | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[contains(translate(.,'WFS', 'wfs'),'wfs')]"/>
			</field>
		</xsl:for-each>
		
<xsl:for-each select="/metadata/idinfo/citation/citeinfo/onlink[contains(translate(.,'KML', 'kml'),'kml')] | /metadata/idinfo/citation/citeinfo/onlink[contains(translate(.,'KMZ', 'kmz'),'kmz')] | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[contains(translate(.,'KML', 'kml'),'kml')] | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[contains(translate(.,'KMZ', 'kmz'),'kmz')]">
		<field name="dataAccessType_ss">KML</field>
			<field name="url.kml_ss">
				<xsl:value-of select="/metadata/idinfo/citation/citeinfo/onlink[contains(translate(.,'KML', 'kml'),'kml')] | /metadata/idinfo/citation/citeinfo/onlink[contains(translate(.,'KMZ', 'kmz'),'kmz')] | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[contains(translate(.,'KML', 'kml'),'kml')] | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[contains(translate(.,'KMZ', 'kmz'),'kmz')]"/>
			</field>
		</xsl:for-each>

<xsl:for-each select="/metadata/idinfo/citation/citeinfo/onlink[contains(translate(.,'JSON', 'json'),'json')] | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[contains(translate(.,'JSON', 'json'),'json')]">
		<field name="dataAccessType_ss">JSON</field>
			<field name="url.json_ss">
				<xsl:value-of select="/metadata/idinfo/citation/citeinfo/onlink[contains(translate(.,'JSON', 'json'),'json')] | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[contains(translate(.,'JSON', 'json'),'json')]"/>
			</field>
		</xsl:for-each>
	
		<xsl:for-each select="/metadata/idinfo/citation/citeinfo/onlink[contains(translate(.,'SO', 'so'),'sos')] | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[contains(translate(.,'SO', 'so'),'sos')]">
		<field name="dataAccessType_ss">SOS</field>
			<field name="url.sos_ss">
				<xsl:value-of select="/metadata/idinfo/citation/citeinfo/onlink[contains(translate(.,'SO', 'so'),'sos')] | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[contains(translate(.,'SO', 'so'),'sos')]"/>
			</field>
		</xsl:for-each>
	<xsl:for-each select="/metadata/idinfo/citation/citeinfo/onlink[contains(translate(.,'THREDS', 'threds'),'thredds')] | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[contains(translate(.,'THREDS', 'threds'),'thredds')]">
		<field name="dataAccessType_ss">THREDDS</field>
			<field name="url.thredds_ss">
				<xsl:value-of select="/metadata/idinfo/citation/citeinfo/onlink[contains(translate(.,'THREDS', 'threds'),'thredds')] | /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[contains(translate(.,'THREDS', 'threds'),'thredds')]"/>
			</field>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
