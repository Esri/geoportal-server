<?xml version="1.0" encoding="UTF-8"?>
<!--
 See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 Esri Inc. licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
<!-- Copyright (c)2006, Environmental Systems Research Institute, Inc. All rights reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink">
  <xsl:output method="html"/>
  <xsl:strip-space elements="*"/>
  <xsl:template match="/">
    <xsl:variable name="fgdc" select="/metadata/idinfo/citation/citeinfo/title"/>
    <xsl:variable name="iso" select="/metadata/dataIdInfo/idCitation/resTitle"/>
    <xsl:variable name="anzlic" select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString | 
    /MD_Metadata/identificationInfo/MD_DataIdentification/citation/CI_Citation/title/CharacterString"/>
    <html>
      <head>
        <title>
          <xsl:choose>
            <xsl:when test="($fgdc !=&apos;&apos;)">
              <xsl:value-of select="$fgdc"/>
              <xsl:if test="($fgdc != $iso) and ($iso != &apos;&apos;)">
                <xsl:text>&#32;(FGDC)&#32;/&#32;</xsl:text>
                <xsl:value-of select="$iso"/>
                <xsl:text>(ISO)</xsl:text>
              </xsl:if>
            </xsl:when>
            <xsl:when test="($iso != &apos;&apos;)">
              <xsl:value-of select="$iso"/>
            </xsl:when>
            <xsl:when test="($anzlic != &apos;&apos;)">
              <xsl:value-of select="$anzlic"/><xsl:text> (ISO 19139)</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>Metadata Document</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </title>
        <link href="xsl/metadata.css" rel="stylesheet" type="text/css"/>
      </head>
      <body class="bodyText">
        <h1 class="toolbarTitle">
          <xsl:choose>
            <xsl:when test="($fgdc !=&apos;&apos;)">
              <xsl:value-of select="$fgdc"/>
              <xsl:if test="($fgdc != $iso) and ($iso != &apos;&apos;)">
                <xsl:text>&#32;(FGDC)&#32;/&#32;</xsl:text>
                <xsl:value-of select="$iso"/>
                <xsl:text>&#32;(ISO)</xsl:text>
              </xsl:if>
            </xsl:when>
            <xsl:when test="($iso != &apos;&apos;)">
              <xsl:value-of select="$iso"/>
            </xsl:when>
             <xsl:when test="($anzlic != &apos;&apos;)">
              <xsl:value-of select="$anzlic"/><xsl:text> (ISO 19139)</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>Metadata Document</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </h1>
        <hr/>
        <xsl:if test="/metadata/idinfo">
          <xsl:if test="/metadata/dataIdInfo">
            <h3 class="headTitle">
              <xsl:text>FGDC Metadata</xsl:text>
            </h3>
          </xsl:if>
          <dl>
            <xsl:apply-templates select="/metadata/idinfo"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/dataqual"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/spdoinfo"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/spref"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/eainfo"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/distinfo"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/metainfo"/>
          </dl>
          <xsl:if test="/metadata/dataIdInfo">
            <hr/>
            <h3 class="headTitle">
              <xsl:text>ISO Metadata</xsl:text>
            </h3>
          </xsl:if>
        </xsl:if>
        <xsl:if test="/metadata/dataIdInfo">
          <dl>
            <xsl:apply-templates select="/metadata/mdFileID"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/mdLang"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/mdChar"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/mdParentID"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/mdHrLv"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/mdHrLvName"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/mdContact"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/mdDateSt"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/mdStanName"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/mdStanVer"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/distInfo"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/dataIdInfo"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/appSchInfo"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/porCatInfo"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/mdMaint"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/mdConst"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/dqInfo"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/spatRepInfo"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/refSysInfo"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/contInfo"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/metadata/mdExtInfo"/>
          </dl>
        </xsl:if>
       <!--       -->
       <xsl:if test="/gmd:MD_Metadata/gmd:identificationInfo | /MD_Metadata/identificationInfo">
          <dl>
            <xsl:apply-templates select="/gmd:MD_Metadata/gmd:fileIdentifier | /MD_Metadata/fileIdentifier"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/gmd:MD_Metadata/gmd:language | /MD_Metadata/language"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/gmd:MD_Metadata/gmd:characterSet | /MD_Metadata/characterSet"/>
          </dl>
          <!--dl>
            <xsl:apply-templates select="/metadata/mdParentID"/>
          </dl-->
          <dl>
            <xsl:apply-templates select="/gmd:MD_Metadata/gmd:hierarchyLevel | /MD_Metadata/hierarchyLevel"/>
          </dl>
          <!--dl>
            <xsl:apply-templates select="/metadata/mdHrLvName"/>
          </dl-->
          <dl>
            <xsl:apply-templates select="/gmd:MD_Metadata/gmd:contact | /MD_Metadata/contact"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/gmd:MD_Metadata/gmd:dateStamp | /MD_Metadata/dateStamp"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/gmd:MD_Metadata/gmd:metadataStandardName | /MD_Metadata/metadataStandardName"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/gmd:MD_Metadata/gmd:metadataStandardVersion | /MD_Metadata/metadataStandardVersion"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution | /MD_Metadata/distributionInfo/MD_Distribution"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification | /MD_Metadata/identificationInfo/MD_DataIdentification"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality | /MD_Metadata/dataQualityInfo/DQ_DataQuality"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/gmd:MD_Metadata/gmd:spatialRepresentationInfo/gmd:MD_Georeferenceable | /MD_Metadata/spatialRepresentationInfo/MD_Georeferenceable"/>
          </dl>
          <dl>
            <xsl:apply-templates select="/gmd:MD_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem | /MD_Metadata/referenceSystemInfo/MD_ReferenceSystem"/>
          </dl>
        </xsl:if>

       <!--      -->
        <!--xsl:if test="/metadata/Esri">
          <hr/>
          <h3 class="headTitle">
            <xsl:text>ESRI Metadata</xsl:text>
          </h3>
          <dl>
            <xsl:apply-templates select="/metadata/Esri"/>
          </dl>
        </xsl:if>
         <xsl:if test="/MD_Metadata/Esri">
          <hr/>
          <h3 class="headTitle">
            <xsl:text>ESRI Metadata</xsl:text>
          </h3>
          <dl>
            <xsl:apply-templates select="/MD_Metadata/Esri"/>
          </dl>
        </xsl:if-->
        <!-- xsl:if test="/metadata/gos">
          <hr/>
          <h3 class="headTitle">
            <xsl:text>GOS Metadata</xsl:text>
          </h3>
          <dl>
            <xsl:apply-templates select="/metadata/gos"/>
          </dl>
        </xsl:if -->
        <xsl:for-each select="/metadata/*">
          <xsl:variable select="position()" name="number"/>
          <xsl:if test="local-name() != &apos;Binary&apos; and local-name() != &apos;Esri&apos; and local-name() != &apos;gos&apos; and local-name() != &apos;idinfo&apos; and local-name() != &apos;dataqual&apos; and local-name() != &apos;spdoinfo&apos; and local-name() != &apos;spref&apos; and local-name() != &apos;eainfo&apos; and local-name() != &apos;distinfo&apos; and local-name() != &apos;metainfo&apos; and local-name() != &apos;mdFileID&apos; and local-name() != &apos;mdLang&apos; and local-name() != &apos;mdChar&apos; and local-name() != &apos;mdParentID&apos; and local-name() != &apos;mdHrLv&apos; and local-name() != &apos;mdHrLvName&apos; and local-name() != &apos;mdContact&apos; and local-name() != &apos;mdDateSt&apos; and local-name() != &apos;mdStanName&apos; and local-name() != &apos;mdStanVer&apos; and local-name() != &apos;distInfo&apos; and local-name() != &apos;dataIdInfo&apos; and local-name() != &apos;appSchInfo&apos; and local-name() != &apos;porCatInfo&apos; and local-name() != &apos;mdMaint&apos; and local-name() != &apos;mdConst&apos; and local-name() != &apos;dqInfo&apos; and local-name() != &apos;spatRepInfo&apos; and local-name() != &apos;refSysInfo&apos; and local-name() != &apos;contInfo&apos; and local-name() != &apos;mdExtInfo&apos;">
            <xsl:apply-templates select="." mode="other">
              <xsl:with-param select="$number - 1" name="number"/>
            </xsl:apply-templates>
          </xsl:if>
        </xsl:for-each>
      </body>
    </html>
  </xsl:template>


  <xsl:template match="*">
    <xsl:choose>
      <xsl:when test="@value">
        <xsl:call-template name="get_text">
          <xsl:with-param name="el">
            <xsl:value-of select="local-name()"/>
          </xsl:with-param>
        </xsl:call-template>
        <xsl:value-of select="@value"/>
      </xsl:when>
      <xsl:when test="./*[@value]">
        <dt>
          <xsl:call-template name="get_text">
            <xsl:with-param name="el">
              <xsl:value-of select="local-name()"/>
            </xsl:with-param>
          </xsl:call-template>
        </dt>
        <xsl:apply-templates select="*"/>
      </xsl:when>
      <xsl:when test="*">
        <dt>
          <xsl:call-template name="get_text">
            <xsl:with-param name="el">
              <xsl:value-of select="local-name()"/>
            </xsl:with-param>
          </xsl:call-template>
        </dt>
        <dl>
          <xsl:apply-templates select="*"/>
        </dl>
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="text() != &apos;&apos;">
          <dt>
            <xsl:call-template name="get_text">
              <xsl:with-param name="el">
                <xsl:value-of select="local-name()"/>
              </xsl:with-param>
            </xsl:call-template>
            <xsl:apply-templates select="text()"/>
          </dt>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template mode="other" match="*">
    <xsl:param name="number"/>
    <xsl:if test="$number = 0">
      <hr/>
      <h3 class="headTitle">
        <xsl:text>Other markup</xsl:text>
      </h3>
    </xsl:if>
    <xsl:choose>
      <xsl:when test="*">
        <dl>
          <dt>
            <em>
              <xsl:value-of select="local-name()"/>
              <xsl:text>:</xsl:text>
            </em>
          </dt>
          <xsl:apply-templates mode="other">
            <xsl:with-param select="$number+1" name="number"/>
          </xsl:apply-templates>
        </dl>
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="text() != &apos;&apos;">
          <dt>
            <em>
              <xsl:value-of select="local-name()"/>
              <xsl:text>:</xsl:text>
            </em>
            <xsl:value-of select="text()"/>
          </dt>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

 <xsl:template name="get_text">
     <xsl:param name="el"/>
    <em>
     <xsl:choose>
       <xsl:when test="$el = &quot;equScale&quot;">Dataset's scale:&#32;</xsl:when>
       <xsl:when test="$el = &quot;digform&quot;">Digital Form:&#32;</xsl:when>
       <xsl:when test="$el = &quot;begin&quot;">Beginning date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;refSysName&quot;">Reference system name identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Binary&quot;">Binary Enclosures:&#32;</xsl:when>
       <xsl:when test="$el = &quot;methtype&quot;">Methodology Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;minalti&quot;">Minimum Altitude:&#32;</xsl:when>
       <xsl:when test="$el = &quot;bgFileDesc&quot;">File description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attribSet&quot;">Attributes:&#32;</xsl:when>
       <xsl:when test="$el = &quot;VectSpatRep&quot;">Spatial Representation - Vector:&#32;</xsl:when>
       <xsl:when test="$el = &quot;measId&quot;">Registered standard procedure:&#32;</xsl:when>
       <xsl:when test="$el = &quot;othergrd&quot;">Other Grid System's Definition:&#32;</xsl:when>
       <xsl:when test="$el = &quot;endtime&quot;">Ending Time:&#32;</xsl:when>
       <xsl:when test="$el = &quot;usrDefFreq&quot;">Time period between updates:&#32;</xsl:when>
       <xsl:when test="$el = &quot;scope&quot;">Scope:&#32;</xsl:when>
       <xsl:when test="$el = &quot;TM_Period&quot;">Range of Dates/Times:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Result&quot;">General test results:&#32;</xsl:when>
       <xsl:when test="$el = &quot;extEleRat&quot;">Why the element was created:&#32;</xsl:when>
       <xsl:when test="$el = &quot;idStatus&quot;">Status:&#32;</xsl:when>
       <xsl:when test="$el = &quot;sfequat&quot;">Scale Factor at Equator:&#32;</xsl:when>
       <xsl:when test="$el = &quot;evalMethType&quot;">Type of test:&#32;</xsl:when>
       <xsl:when test="$el = &quot;otfcpkey&quot;">Origin Primary Key:&#32;</xsl:when>
       <xsl:when test="$el = &quot;oncomp&quot;">Online Computer and Operating System:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vpfinfo&quot;">VPF Point and Vector Object Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;altres&quot;">Altitude Resolution:&#32;</xsl:when>
       <xsl:when test="$el = &quot;elevmin&quot;">Elevation Minimum:&#32;</xsl:when>
       <xsl:when test="$el = &quot;years&quot;">Years:&#32;</xsl:when>
       <xsl:when test="$el = &quot;imagCond&quot;">Imaging condition:&#32;</xsl:when>
       <xsl:when test="$el = &quot;asCstLang&quot;">Formal language used in schema:&#32;</xsl:when>
       <xsl:when test="$el = &quot;colcount&quot;">Column Count:&#32;</xsl:when>
       <xsl:when test="$el = &quot;reccap&quot;">Recording Capacity:&#32;</xsl:when>
       <xsl:when test="$el = &quot;feast&quot;">False Easting:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mdFileID&quot;">Metadata identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;srcinfo&quot;">Source Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;bearres&quot;">Bearing Resolution:&#32;</xsl:when>
       <xsl:when test="$el = &quot;resEd&quot;">Edition:&#32;</xsl:when>
       <xsl:when test="$el = &quot;relcard&quot;">Relationship Cardinality:&#32;</xsl:when>
       <xsl:when test="$el = &quot;citation&quot;">Citation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;plance&quot;">Planar Coordinate Encoding Method:&#32;</xsl:when>
       <xsl:when test="$el = &quot;camCalInAv&quot;">Camera calibration is available:&#32;</xsl:when>
       <xsl:when test="$el = &quot;linrefer&quot;">Linear Referencing:&#32;</xsl:when>
       <xsl:when test="$el = &quot;metrd&quot;">Metadata Review Date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;longCntMer&quot;">Longitude of central meridian:&#32;</xsl:when>
       <xsl:when test="$el = &quot;incWithDS&quot;">Catalogue accompanies the dataset:&#32;</xsl:when>
       <xsl:when test="$el = &quot;proccont&quot;">Process Contact:&#32;</xsl:when>
       <xsl:when test="$el = &quot;planci&quot;">Planar Coordinate Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;westBL&quot;">West longitude:&#32;</xsl:when>
       <xsl:when test="$el = &quot;FetCatDesc&quot;">Content Information - Feature Catalogue Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rdommax&quot;">Range Domain Maximum:&#32;</xsl:when>
       <xsl:when test="$el = &quot;toolcite&quot;">Tool Citation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;srcprod&quot;">Source Produced Citation Abbreviation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;state&quot;">State or Province:&#32;</xsl:when>
       <xsl:when test="$el = &quot;langdata&quot;">Language of Dataset:&#32;</xsl:when>
       <xsl:when test="$el = &quot;altenc&quot;">Altitude Encoding Method:&#32;</xsl:when>
       <xsl:when test="$el = &quot;medVol&quot;">Number of media items:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geolage&quot;">Geologic Age:&#32;</xsl:when>
       <xsl:when test="$el = &quot;relinfo&quot;">Relationship Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;behrmann&quot;">Behrmann:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQTempValid&quot;">Data quality report - Temporal validity:&#32;</xsl:when>
       <xsl:when test="$el = &quot;address&quot;">Address:&#32;</xsl:when>
       <xsl:when test="$el = &quot;identAuth&quot;">Reference that defines the value:&#32;</xsl:when>
       <xsl:when test="$el = &quot;reposit&quot;">Repository:&#32;</xsl:when>
       <xsl:when test="$el = &quot;RematchLocator&quot;">Geocoding Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;procdate&quot;">Process Date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;obqllong&quot;">Oblique Line Longitude:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stepProc&quot;">Process contact:&#32;</xsl:when>
       <xsl:when test="$el = &quot;quotech&quot;">Quote Character:&#32;</xsl:when>
       <xsl:when test="$el = &quot;quanValUnit&quot;">Value units:&#32;</xsl:when>
       <xsl:when test="$el = &quot;planAvDtTm&quot;">Date of availability:&#32;</xsl:when>
       <xsl:when test="$el = &quot;timeinfo&quot;">Time Period Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;themekey&quot;">Theme Keyword:&#32;</xsl:when>
       <xsl:when test="$el = &quot;featSet&quot;">Features:&#32;</xsl:when>
       <xsl:when test="$el = &quot;robinson&quot;">Robinson:&#32;</xsl:when>
       <xsl:when test="$el = &quot;metsc&quot;">Metadata Security Classification:&#32;</xsl:when>
       <xsl:when test="$el = &quot;hgtProsPt&quot;">Height of prospective point above surface:&#32;</xsl:when>
       <xsl:when test="$el = &quot;metsi&quot;">Metadata Security Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;tpCat&quot;">Themes or categories of the resource:&#32;</xsl:when>
       <xsl:when test="$el = &quot;logic&quot;">Logical Consistency Report:&#32;</xsl:when>
       <xsl:when test="$el = &quot;refSysInfo&quot;">Reference System Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ServiceFCName&quot;">Service Feature Class Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;descript&quot;">Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;city&quot;">City:&#32;</xsl:when>
       <xsl:when test="$el = &quot;valUnit&quot;">Wavelength units:&#32;</xsl:when>
       <xsl:when test="$el = &quot;time&quot;">Time of Day:&#32;</xsl:when>
       <xsl:when test="$el = &quot;purpose&quot;">Purpose:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stepRat&quot;">Rationale:&#32;</xsl:when>
       <xsl:when test="$el = &quot;utmzone&quot;">UTM Zone Number:&#32;</xsl:when>
       <xsl:when test="$el = &quot;exSpat&quot;">Spatial extent:&#32;</xsl:when>
       <xsl:when test="$el = &quot;delPoint&quot;">Delivery point:&#32;</xsl:when>
       <xsl:when test="$el = &quot;plandu&quot;">Planar Distance Units:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vdgrin&quot;">Van der Grinten:&#32;</xsl:when>
       <xsl:when test="$el = &quot;upszone&quot;">UPS Zone Identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQThemClassCor&quot;">Data quality report - Thematic classification correctness:&#32;</xsl:when>
       <xsl:when test="$el = &quot;accessConsts&quot;">Access constraints:&#32;</xsl:when>
       <xsl:when test="$el = &quot;begdate&quot;">Beginning Date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ruleeid&quot;">Edge Feature Class:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rasttype&quot;">Raster Object Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mettc&quot;">Metadata Time Convention:&#32;</xsl:when>
       <xsl:when test="$el = &quot;SecConsts&quot;">Security constraints:&#32;</xsl:when>
       <xsl:when test="$el = &quot;SyncTime&quot;">Synchronization Time:&#32;</xsl:when>
       <xsl:when test="$el = &quot;otfcfkey&quot;">Origin Foreign Key:&#32;</xsl:when>
       <xsl:when test="$el = &quot;methdesc&quot;">Methodology Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;relflab&quot;">Relationship Forward Label:&#32;</xsl:when>
       <xsl:when test="$el = &quot;natvform&quot;">Native Dataset Format:&#32;</xsl:when>
       <xsl:when test="$el = &quot;procsv&quot;">Process Software and Version:&#32;</xsl:when>
       <xsl:when test="$el = &quot;numstop&quot;">Number StopBits:&#32;</xsl:when>
       <xsl:when test="$el = &quot;detailed&quot;">Detailed Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;idPurp&quot;">Purpose:&#32;</xsl:when>
       <xsl:when test="$el = &quot;seriesName&quot;">Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attrva&quot;">Attribute Value Accuracy:&#32;</xsl:when>
       <xsl:when test="$el = &quot;formatName&quot;">Format name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stdorder&quot;">Standard Order Process:&#32;</xsl:when>
       <xsl:when test="$el = &quot;topLvl&quot;">Level of topology for this dataset:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ruleteid&quot;">To Edge Feature Class:&#32;</xsl:when>
       <xsl:when test="$el = &quot;uom&quot;">Units of Measurement Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;SpatTempEx&quot;">Spatial and temporal extent:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mrgtype&quot;">Merge Rule:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attwidth&quot;">Attribute Width:&#32;</xsl:when>
       <xsl:when test="$el = &quot;placekey&quot;">Place Keyword:&#32;</xsl:when>
       <xsl:when test="$el = &quot;minutes&quot;">Minutes:&#32;</xsl:when>
       <xsl:when test="$el = &quot;evalMethDesc&quot;">Evaluation method:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cassini&quot;">Cassini:&#32;</xsl:when>
       <xsl:when test="$el = &quot;BoundPoly&quot;">Bounding polygon:&#32;</xsl:when>
       <xsl:when test="$el = &quot;authent&quot;">Authentication:&#32;</xsl:when>
       <xsl:when test="$el = &quot;RangeDim&quot;">Range of cell values:&#32;</xsl:when>
       <xsl:when test="$el = &quot;idAbs&quot;">Abstract:&#32;</xsl:when>
       <xsl:when test="$el = &quot;westbc&quot;">West Bounding Coordinate:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mapproj&quot;">Map Projection:&#32;</xsl:when>
       <xsl:when test="$el = &quot;sdtstype&quot;">SDTS Point and Vector Object Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vertMaxVal&quot;">Maximum value:&#32;</xsl:when>
       <xsl:when test="$el = &quot;specUsage&quot;">Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;spcszone&quot;">SPCS Zone Identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rulejmnc&quot;">Junction Minimum Cardinality:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rastifor&quot;">Image Format:&#32;</xsl:when>
       <xsl:when test="$el = &quot;metuc&quot;">Metadata Use Constraints:&#32;</xsl:when>
       <xsl:when test="$el = &quot;resMaint&quot;">Resource maintenance:&#32;</xsl:when>
       <xsl:when test="$el = &quot;gridsys&quot;">Grid Coordinate System:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attscale&quot;">Attribute Scale:&#32;</xsl:when>
       <xsl:when test="$el = &quot;sechandl&quot;">Security Handling Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dataLang&quot;">Dataset language:&#32;</xsl:when>
       <xsl:when test="$el = &quot;windir&quot;">Wind Direction:&#32;</xsl:when>
       <xsl:when test="$el = &quot;maxVal&quot;">Longest wavelength:&#32;</xsl:when>
       <xsl:when test="$el = &quot;distorFormat&quot;">Available format:&#32;</xsl:when>
       <xsl:when test="$el = &quot;aziPtLong&quot;">Azimuth measure point longitude:&#32;</xsl:when>
       <xsl:when test="$el = &quot;pubinfo&quot;">Publication Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;barpres&quot;">Barometric Pressure:&#32;</xsl:when>
       <xsl:when test="$el = &quot;paraCit&quot;">Georeferencing parameters citation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;bottombc&quot;">Bottom Bounding Coordinate:&#32;</xsl:when>
       <xsl:when test="$el = &quot;contInfo&quot;">Content Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ups&quot;">Universal Polar Stereographic:&#32;</xsl:when>
       <xsl:when test="$el = &quot;issn&quot;">ISSN:&#32;</xsl:when>
       <xsl:when test="$el = &quot;GridSpatRep&quot;">Spatial Representation - Grid:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attrdef&quot;">Attribute Definition:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cntAddress&quot;">Address:&#32;</xsl:when>
       <xsl:when test="$el = &quot;eframes&quot;">Data Frames:&#32;</xsl:when>
       <xsl:when test="$el = &quot;custom&quot;">Custom Order Process:&#32;</xsl:when>
       <xsl:when test="$el = &quot;polygon&quot;">Polygon:&#32;</xsl:when>
       <xsl:when test="$el = &quot;onlinopt&quot;">Online Option:&#32;</xsl:when>
       <xsl:when test="$el = &quot;lowtime&quot;">Time of Low Tide:&#32;</xsl:when>
       <xsl:when test="$el = &quot;langmeta&quot;">Language of Metadata:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Server&quot;">Server:&#32;</xsl:when>
       <xsl:when test="$el = &quot;quanValType&quot;">Values required for conformance:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Units&quot;">of measure length:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attrmres&quot;">Attribute Measurement Resolution:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mdLang&quot;">Metadata language:&#32;</xsl:when>
       <xsl:when test="$el = &quot;netwrole&quot;">Network Role:&#32;</xsl:when>
       <xsl:when test="$el = &quot;horizdn&quot;">Horizontal Datum Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;LocalName&quot;">Local name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQRelIntPosAcc&quot;">Data quality report - Relative internal positional accuracy:&#32;</xsl:when>
       <xsl:when test="$el = &quot;posacc&quot;">Positional Accuracy:&#32;</xsl:when>
       <xsl:when test="$el = &quot;landsat&quot;">Landsat Number:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geoDesc&quot;">Description of the resource's location:&#32;</xsl:when>
       <xsl:when test="$el = &quot;denFlatRat&quot;">Denominator of flattening ratio:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ruleemnc&quot;">Edge Minimum Cardinality:&#32;</xsl:when>
       <xsl:when test="$el = &quot;offset&quot;">Offset of values from zero:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Consts&quot;">Constraints:&#32;</xsl:when>
       <xsl:when test="$el = &quot;formatDist&quot;">Distributor:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dimDescrp&quot;">Minimum and maximum values:&#32;</xsl:when>
       <xsl:when test="$el = &quot;event&quot;">Environmental Event:&#32;</xsl:when>
       <xsl:when test="$el = &quot;eckert1&quot;">Eckert I:&#32;</xsl:when>
       <xsl:when test="$el = &quot;eckert2&quot;">Eckert II:&#32;</xsl:when>
       <xsl:when test="$el = &quot;eckert3&quot;">Eckert III:&#32;</xsl:when>
       <xsl:when test="$el = &quot;onlink&quot;">Online Linkage:&#32;</xsl:when>
       <xsl:when test="$el = &quot;illElevAng&quot;">Illumination elevation angle:&#32;</xsl:when>
       <xsl:when test="$el = &quot;eckert4&quot;">Eckert IV:&#32;</xsl:when>
       <xsl:when test="$el = &quot;eckert5&quot;">Eckert V:&#32;</xsl:when>
       <xsl:when test="$el = &quot;status&quot;">Status:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cntPhone&quot;">Phone:&#32;</xsl:when>
       <xsl:when test="$el = &quot;eckert6&quot;">Eckert VI:&#32;</xsl:when>
       <xsl:when test="$el = &quot;catLang&quot;">Feature catalogue's language:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rastcmap&quot;">Image Colormap:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Integer&quot;">Integer:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dtfcname&quot;">Destination Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ellipsoid&quot;">Ellipsoid identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rdommean&quot;">Range Domain Mean:&#32;</xsl:when>
       <xsl:when test="$el = &quot;svlong&quot;">Straight-Vertical Longitude from Pole:&#32;</xsl:when>
       <xsl:when test="$el = &quot;grngpoin&quot;">G-Ring Point:&#32;</xsl:when>
       <xsl:when test="$el = &quot;endgeol&quot;">Ending Geologic Age:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQCompOm&quot;">Data quality report - Completeness omission:&#32;</xsl:when>
       <xsl:when test="$el = &quot;semiMajAx&quot;">Semi-major axis:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attributeType&quot;">Attribute type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attrdefs&quot;">Attribute Definition Source:&#32;</xsl:when>
       <xsl:when test="$el = &quot;filedec&quot;">File Decompression Technique:&#32;</xsl:when>
       <xsl:when test="$el = &quot;azimptl&quot;">Azimuth Measure Point Longitude:&#32;</xsl:when>
       <xsl:when test="$el = &quot;adminArea&quot;">Administrative area:&#32;</xsl:when>
       <xsl:when test="$el = &quot;theme&quot;">Theme:&#32;</xsl:when>
       <xsl:when test="$el = &quot;equicon&quot;">Equidistant Conic:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attrlabl&quot;">Attribute Label:&#32;</xsl:when>
       <xsl:when test="$el = &quot;missingv&quot;">Missing Value Code:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Esri&quot;">Esri:&#32;</xsl:when>
       <xsl:when test="$el = &quot;toneGrad&quot;">Number of discrete values:&#32;</xsl:when>
       <xsl:when test="$el = &quot;scpExt&quot;">Scope extent:&#32;</xsl:when>
       <xsl:when test="$el = &quot;coordinates&quot;">Coordinates:&#32;</xsl:when>
       <xsl:when test="$el = &quot;assndesc&quot;">Description of Association:&#32;</xsl:when>
       <xsl:when test="$el = &quot;TM_CalDate&quot;">Calendar date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;medFormat&quot;">How the medium is written:&#32;</xsl:when>
       <xsl:when test="$el = &quot;azimequi&quot;">Azimuthal Equidistant:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dfwidth&quot;">Data Field Width:&#32;</xsl:when>
       <xsl:when test="$el = &quot;eastBL&quot;">East longitude:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stereo&quot;">Stereographic:&#32;</xsl:when>
       <xsl:when test="$el = &quot;tidtype&quot;">Type of Tide:&#32;</xsl:when>
       <xsl:when test="$el = &quot;sdeconn&quot;">SDE Connection Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;asSchLang&quot;">Schema language used:&#32;</xsl:when>
       <xsl:when test="$el = &quot;relcomp&quot;">Composite Relationship:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rastxu&quot;">Cell Size X Units:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rastorig&quot;">Raster Origin:&#32;</xsl:when>
       <xsl:when test="$el = &quot;offmedia&quot;">Offline Media:&#32;</xsl:when>
       <xsl:when test="$el = &quot;taxonsys&quot;">Taxonomic System:&#32;</xsl:when>
       <xsl:when test="$el = &quot;medDensity&quot;">Recording density:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ider&quot;">Identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;gringlon&quot;">G-Ring Longitude:&#32;</xsl:when>
       <xsl:when test="$el = &quot;quanValue&quot;">Result value:&#32;</xsl:when>
       <xsl:when test="$el = &quot;typesrc&quot;">Type of Source Media:&#32;</xsl:when>
       <xsl:when test="$el = &quot;role&quot;">Contact's role:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Georect&quot;">Spatial Representation - Georectified Grid:&#32;</xsl:when>
       <xsl:when test="$el = &quot;formatSpec&quot;">Format specification:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stfield&quot;">Subtype Attribute:&#32;</xsl:when>
       <xsl:when test="$el = &quot;spcs&quot;">State Plane Coordinate System:&#32;</xsl:when>
       <xsl:when test="$el = &quot;UomVelocity&quot;">Units of measure, velocity:&#32;</xsl:when>
       <xsl:when test="$el = &quot;recdel&quot;">Record Delimiter:&#32;</xsl:when>
       <xsl:when test="$el = &quot;topbc&quot;">Top Bounding Coordinate:&#32;</xsl:when>
       <xsl:when test="$el = &quot;formatAmdNum&quot;">Format amendment number:&#32;</xsl:when>
       <xsl:when test="$el = &quot;asSwDevFiFt&quot;">Software development file format:&#32;</xsl:when>
       <xsl:when test="$el = &quot;recden&quot;">Recording Density:&#32;</xsl:when>
       <xsl:when test="$el = &quot;latProjCnt&quot;">Latitude of projection center:&#32;</xsl:when>
       <xsl:when test="$el = &quot;seqID&quot;">Band identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;compat&quot;">Compatibility Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;tempEle&quot;">Temporal Component:&#32;</xsl:when>
       <xsl:when test="$el = &quot;refDate&quot;">Date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;keyTyp&quot;">Keyword Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;extEleRule&quot;">Relationship to existing elements:&#32;</xsl:when>
       <xsl:when test="$el = &quot;distFormat&quot;">Distribution format:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rastband&quot;">Number of Bands:&#32;</xsl:when>
       <xsl:when test="$el = &quot;specimen&quot;">Specimen:&#32;</xsl:when>
       <xsl:when test="$el = &quot;deschead&quot;">Description of Header Content:&#32;</xsl:when>
       <xsl:when test="$el = &quot;usrDetLim&quot;">How the resource must not be used:&#32;</xsl:when>
       <xsl:when test="$el = &quot;offLineMed&quot;">Medium of distribution:&#32;</xsl:when>
       <xsl:when test="$el = &quot;codesetd&quot;">Codeset Domain:&#32;</xsl:when>
       <xsl:when test="$el = &quot;citId&quot;">Unique resource identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rastyu&quot;">Cell Size Y Units:&#32;</xsl:when>
       <xsl:when test="$el = &quot;sdtsterm&quot;">SDTS Terms Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQThemAcc&quot;">Data quality report - Thematic accuracy:&#32;</xsl:when>
       <xsl:when test="$el = &quot;CovDesc&quot;">Content Information - Coverage Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dqReport&quot;">Report:&#32;</xsl:when>
       <xsl:when test="$el = &quot;temporal&quot;">Temporal:&#32;</xsl:when>
       <xsl:when test="$el = &quot;codesetn&quot;">Codeset Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;compCode&quot;">Catalogue complies with ISO 19110:&#32;</xsl:when>
       <xsl:when test="$el = &quot;utm&quot;">Universal Transverse Mercator:&#32;</xsl:when>
       <xsl:when test="$el = &quot;RefSystem&quot;">Reference system identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ruletype&quot;">Rule Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ServiceType&quot;">Service Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;codesets&quot;">Codeset Source:&#32;</xsl:when>
       <xsl:when test="$el = &quot;metextns&quot;">Metadata Extensions:&#32;</xsl:when>
       <xsl:when test="$el = &quot;usrCntInfo&quot;">Party using the resource:&#32;</xsl:when>
       <xsl:when test="$el = &quot;toolacc&quot;">Tool Access Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;procstep&quot;">Process Step:&#32;</xsl:when>
       <xsl:when test="$el = &quot;transize&quot;">Transfer Size:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stratum&quot;">Stratum:&#32;</xsl:when>
       <xsl:when test="$el = &quot;heightpt&quot;">Height of Perspective Point Above Surface:&#32;</xsl:when>
       <xsl:when test="$el = &quot;radCalDatAv&quot;">Radiometric calibration is available:&#32;</xsl:when>
       <xsl:when test="$el = &quot;enabfld&quot;">Enabled Attribute:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geoId&quot;">Geographic identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dssize&quot;">Dataset Size:&#32;</xsl:when>
       <xsl:when test="$el = &quot;metainfo&quot;">Metadata Reference Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rdom&quot;">Range Domain:&#32;</xsl:when>
       <xsl:when test="$el = &quot;resConst&quot;">Resource constraints:&#32;</xsl:when>
       <xsl:when test="$el = &quot;metprof&quot;">Profile Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;axisUnits&quot;">Axis units:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dialtel&quot;">Dialup Telephone:&#32;</xsl:when>
       <xsl:when test="$el = &quot;userNote&quot;">Legal constraints:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attribIntSet&quot;">Attribute instances:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Service&quot;">Service:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rdommin&quot;">Range Domain Minimum:&#32;</xsl:when>
       <xsl:when test="$el = &quot;SyncOnce&quot;">Synchronize Once:&#32;</xsl:when>
       <xsl:when test="$el = &quot;roletype&quot;">Ancillary Role:&#32;</xsl:when>
       <xsl:when test="$el = &quot;otherCitDet&quot;">Other citation details:&#32;</xsl:when>
       <xsl:when test="$el = &quot;medDenUnits&quot;">Density units of measure:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cntemail&quot;">Contact Electronic Mail Address:&#32;</xsl:when>
       <xsl:when test="$el = &quot;resAltTitle&quot;">Alternate titles:&#32;</xsl:when>
       <xsl:when test="$el = &quot;distorCont&quot;">Contact information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;srccitea&quot;">Source Citation Abbreviation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dtfcpkey&quot;">Destination Primary Key:&#32;</xsl:when>
       <xsl:when test="$el = &quot;qhorizpa&quot;">Quantitative Horizontal Positional Accuracy Assessment:&#32;</xsl:when>
       <xsl:when test="$el = &quot;domfldtp&quot;">Attribute Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;eastbc&quot;">East Bounding Coordinate:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geograph&quot;">Geographic:&#32;</xsl:when>
       <xsl:when test="$el = &quot;relnodir&quot;">Notification Direction:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vouchers&quot;">Vouchers:&#32;</xsl:when>
       <xsl:when test="$el = &quot;tideref&quot;">Tide Table Reference:&#32;</xsl:when>
       <xsl:when test="$el = &quot;techpreq&quot;">Technical Prerequisites:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cntfax&quot;">Contact Facsimile Telephone:&#32;</xsl:when>
       <xsl:when test="$el = &quot;calDate&quot;">Calendar date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;identCode&quot;">Identity code:&#32;</xsl:when>
       <xsl:when test="$el = &quot;domtype&quot;">Domain Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;addrtype&quot;">Address Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geolcit&quot;">Geologic Citation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mercator&quot;">Mercator:&#32;</xsl:when>
       <xsl:when test="$el = &quot;obLineLong&quot;">Oblique line longitude:&#32;</xsl:when>
       <xsl:when test="$el = &quot;featCatSup&quot;">Feature catalogue supplement:&#32;</xsl:when>
       <xsl:when test="$el = &quot;extOnRes&quot;">Extension online resource:&#32;</xsl:when>
       <xsl:when test="$el = &quot;longpc&quot;">Longitude of Projection Center:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attracce&quot;">Attribute Accuracy Explanation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;taxoncl&quot;">Taxonomic Classification:&#32;</xsl:when>
       <xsl:when test="$el = &quot;methcite&quot;">Methodology Citation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;tranParaAv&quot;">Transformation parameters are available:&#32;</xsl:when>
       <xsl:when test="$el = &quot;bgFileName&quot;">File name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;version&quot;">Version Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;relblab&quot;">Relationship Backward Label:&#32;</xsl:when>
       <xsl:when test="$el = &quot;useConsts&quot;">Use constraints:&#32;</xsl:when>
       <xsl:when test="$el = &quot;othercit&quot;">Other Citation Details:&#32;</xsl:when>
       <xsl:when test="$el = &quot;typeProps&quot;">Attributes of the service:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Sync&quot;">Synchronize Automatically:&#32;</xsl:when>
       <xsl:when test="$el = &quot;semiaxis&quot;">Semi-major Axis:&#32;</xsl:when>
       <xsl:when test="$el = &quot;northBL&quot;">North latitude:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dataScale&quot;">Spatial resolution:&#32;</xsl:when>
       <xsl:when test="$el = &quot;server&quot;">Server Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vertacc&quot;">Vertical Positional Accuracy:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attraccr&quot;">Attribute Accuracy Report:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Data&quot;">Data:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vpflevel&quot;">VPF Topology Level:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ptvctinf&quot;">Point and Vector Object Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;srcRefSys&quot;">Source reference system:&#32;</xsl:when>
       <xsl:when test="$el = &quot;taxoncom&quot;">Taxonomic Completeness:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attraccv&quot;">Attribute Accuracy Value:&#32;</xsl:when>
       <xsl:when test="$el = &quot;orieParaAv&quot;">Orientation parameters are available:&#32;</xsl:when>
       <xsl:when test="$el = &quot;distres&quot;">Distance Resolution:&#32;</xsl:when>
       <xsl:when test="$el = &quot;keywords&quot;">Keywords:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cntinst&quot;">Contact Instructions:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cntaddr&quot;">Contact Address:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rpOrgName&quot;">Organization's name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;bearunit&quot;">Bearing Units:&#32;</xsl:when>
       <xsl:when test="$el = &quot;efeacnt&quot;">ESRI Feature Count:&#32;</xsl:when>
       <xsl:when test="$el = &quot;scpLvlDesc&quot;">Scope Description Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;srcCitatn&quot;">Source citation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rfDenom&quot;">Scale denominator:&#32;</xsl:when>
       <xsl:when test="$el = &quot;begdatea&quot;">Beginning Date of Attribute Values:&#32;</xsl:when>
       <xsl:when test="$el = &quot;onLineSrc&quot;">Online source:&#32;</xsl:when>
       <xsl:when test="$el = &quot;srcStep&quot;">Process step:&#32;</xsl:when>
       <xsl:when test="$el = &quot;efeatyp&quot;">ESRI Feature Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Scale&quot;">Fraction is derived from scale:&#32;</xsl:when>
       <xsl:when test="$el = &quot;turnarnd&quot;">Turnaround:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ellParas&quot;">Ellipsoid parameters:&#32;</xsl:when>
       <xsl:when test="$el = &quot;horizpae&quot;">Horizontal Positional Accuracy Explanation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ordInstr&quot;">Instructions:&#32;</xsl:when>
       <xsl:when test="$el = &quot;catFetTyps&quot;">Feature types in the dataset:&#32;</xsl:when>
       <xsl:when test="$el = &quot;appSchInfo&quot;">Application schema Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;orFunct&quot;">Function performed:&#32;</xsl:when>
       <xsl:when test="$el = &quot;prcStep&quot;">Process step:&#32;</xsl:when>
       <xsl:when test="$el = &quot;MdIdent&quot;">Identifier Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stratkey&quot;">Stratum Keyword:&#32;</xsl:when>
       <xsl:when test="$el = &quot;falNorthng&quot;">False northing:&#32;</xsl:when>
       <xsl:when test="$el = &quot;progress&quot;">Progress:&#32;</xsl:when>
       <xsl:when test="$el = &quot;computer&quot;">Computer Contact Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;browse&quot;">Browse Graphic:&#32;</xsl:when>
       <xsl:when test="$el = &quot;maxalti&quot;">Maximum Altitude:&#32;</xsl:when>
       <xsl:when test="$el = &quot;horizpar&quot;">Horizontal Positional Accuracy Report:&#32;</xsl:when>
       <xsl:when test="$el = &quot;native&quot;">Native Data Set Environment:&#32;</xsl:when>
       <xsl:when test="$el = &quot;citeinfo&quot;">Citation Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;horizpav&quot;">Horizontal Positional Accuracy Value:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geoBox&quot;">Resource's bounding rectangle:&#32;</xsl:when>
       <xsl:when test="$el = &quot;edomvds&quot;">Enumerated Domain Value Definition Source:&#32;</xsl:when>
       <xsl:when test="$el = &quot;pubdate&quot;">Publication Date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;featTypeList&quot;">Feature type list:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rastplyr&quot;">Pyramid Layers:&#32;</xsl:when>
       <xsl:when test="$el = &quot;srccontr&quot;">Source Contribution:&#32;</xsl:when>
       <xsl:when test="$el = &quot;marweat&quot;">Marine Weather Condition:&#32;</xsl:when>
       <xsl:when test="$el = &quot;distributor&quot;">Distributor:&#32;</xsl:when>
       <xsl:when test="$el = &quot;contentTyp&quot;">Type of information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ftname&quot;">File or Table Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dtfcfkey&quot;">Destination Foreign Key:&#32;</xsl:when>
       <xsl:when test="$el = &quot;eadetcit&quot;">Entity and Attribute Detail Citation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;transSize&quot;">Transfer size:&#32;</xsl:when>
       <xsl:when test="$el = &quot;sclFacPrOr&quot;">Scale factor at projection origin:&#32;</xsl:when>
       <xsl:when test="$el = &quot;toolcomp&quot;">Tool Computer and Operating System:&#32;</xsl:when>
       <xsl:when test="$el = &quot;asciistr&quot;">ASCII File Structure:&#32;</xsl:when>
       <xsl:when test="$el = &quot;depthsys&quot;">Depth System Definition:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rpPosName&quot;">Contact's position:&#32;</xsl:when>
       <xsl:when test="$el = &quot;enddate&quot;">Ending Date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;taxongen&quot;">General Taxonomic Coverage:&#32;</xsl:when>
       <xsl:when test="$el = &quot;projcsn&quot;">Projected Coordinate System Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;supplinf&quot;">Supplemental Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;othConsts&quot;">Other constraints:&#32;</xsl:when>
       <xsl:when test="$el = &quot;spdom&quot;">Spatial Domain:&#32;</xsl:when>
       <xsl:when test="$el = &quot;enttyp&quot;">Entity Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;distorTran&quot;">Transfer options:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cntper&quot;">Contact Person:&#32;</xsl:when>
       <xsl:when test="$el = &quot;TM_DateAndTime&quot;">Calendar date and time:&#32;</xsl:when>
       <xsl:when test="$el = &quot;procdesc&quot;">Process Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;UomVolume&quot;">Units of measure, volume:&#32;</xsl:when>
       <xsl:when test="$el = &quot;atprecis&quot;">Attribute Precision:&#32;</xsl:when>
       <xsl:when test="$el = &quot;spatObj&quot;">Spatial object:&#32;</xsl:when>
       <xsl:when test="$el = &quot;class&quot;">Classification:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attrdomv&quot;">Attribute Domain Values:&#32;</xsl:when>
       <xsl:when test="$el = &quot;leftbc&quot;">Left Bounding Coordinate:&#32;</xsl:when>
       <xsl:when test="$el = &quot;enttypds&quot;">Entity Type Definition Source:&#32;</xsl:when>
       <xsl:when test="$el = &quot;conSpec&quot;">Description of conformance requirements:&#32;</xsl:when>
       <xsl:when test="$el = &quot;wavhite&quot;">Wave Height:&#32;</xsl:when>
       <xsl:when test="$el = &quot;domdesc&quot;">Domain Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;hightime&quot;">Time of High Tide:&#32;</xsl:when>
       <xsl:when test="$el = &quot;spref&quot;">Spatial Reference Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;sfprjorg&quot;">Scale Factor at Projection Origin:&#32;</xsl:when>
       <xsl:when test="$el = &quot;distInfo&quot;">Distribution Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;common&quot;">Applicable Common Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;gring&quot;">G-Ring:&#32;</xsl:when>
       <xsl:when test="$el = &quot;longres&quot;">Longitude Resolution:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rulefeid&quot;">From Edge Feature Class:&#32;</xsl:when>
       <xsl:when test="$el = &quot;CreaTime&quot;">Creation Time:&#32;</xsl:when>
       <xsl:when test="$el = &quot;falEastng&quot;">False easting:&#32;</xsl:when>
       <xsl:when test="$el = &quot;distorOrdPrc&quot;">Ordering process:&#32;</xsl:when>
       <xsl:when test="$el = &quot;nettype&quot;">Network Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attrmfrq&quot;">Attribute Measurement Frequency:&#32;</xsl:when>
       <xsl:when test="$el = &quot;northbc&quot;">North Bounding Coordinate:&#32;</xsl:when>
       <xsl:when test="$el = &quot;arcsys&quot;">ARC Coordinate System:&#32;</xsl:when>
       <xsl:when test="$el = &quot;local&quot;">Local:&#32;</xsl:when>
       <xsl:when test="$el = &quot;enttypc&quot;">Entity Type Count:&#32;</xsl:when>
       <xsl:when test="$el = &quot;elemcls&quot;">Network Element:&#32;</xsl:when>
       <xsl:when test="$el = &quot;toolcont&quot;">Tool Contact:&#32;</xsl:when>
       <xsl:when test="$el = &quot;offoptn&quot;">Offline Option:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attrunit&quot;">Attribute Units of Measure:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cloudCovPer&quot;">Percent cloud cover:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ModTime&quot;">Modification Time:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dqInfo&quot;">Data Quality Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;obqllat&quot;">Oblique Line Latitude:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQConcConsis&quot;">Data quality report - Conceptual consistency:&#32;</xsl:when>
       <xsl:when test="$el = &quot;orieParaDs&quot;">Orientation parameter description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ruleest&quot;">Edge Subtype:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rolefld&quot;">Ancillary Role Attribute:&#32;</xsl:when>
       <xsl:when test="$el = &quot;caldate&quot;">Calendar Date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;measDesc&quot;">Measure produced by the test:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mapprojn&quot;">Map Projection Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mdHrLvName&quot;">Scope name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mdDateSt&quot;">Last update:&#32;</xsl:when>
       <xsl:when test="$el = &quot;issue&quot;">Issue Identification:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mapprojp&quot;">Map Projection Parameters:&#32;</xsl:when>
       <xsl:when test="$el = &quot;value&quot;">Precision:&#32;</xsl:when>
       <xsl:when test="$el = &quot;taxonomy&quot;">Taxonomy:&#32;</xsl:when>
       <xsl:when test="$el = &quot;fileDecmTech&quot;">File decompression technique:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ruletest&quot;">To Edge Subtype:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stflddd&quot;">Attribute Defined Domain:&#32;</xsl:when>
       <xsl:when test="$el = &quot;tidrang&quot;">Range of Tide:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQCompComm&quot;">Data quality report - Completeness commission:&#32;</xsl:when>
       <xsl:when test="$el = &quot;spatRpType&quot;">Spatial representation type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;distliab&quot;">Distribution Liability:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rulejmxc&quot;">Junction Maximum Cardinality:&#32;</xsl:when>
       <xsl:when test="$el = &quot;distrib&quot;">Distributor:&#32;</xsl:when>
       <xsl:when test="$el = &quot;artPage&quot;">Pages:&#32;</xsl:when>
       <xsl:when test="$el = &quot;horizsys&quot;">Horizontal Coordinate System Definition:&#32;</xsl:when>
       <xsl:when test="$el = &quot;windsp&quot;">Wind speed:&#32;</xsl:when>
       <xsl:when test="$el = &quot;metac&quot;">Metadata Access Constraints:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rngdates&quot;">Range of Dates/Times:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dsgpoly&quot;">Data Set G-Polygon:&#32;</xsl:when>
       <xsl:when test="$el = &quot;denflat&quot;">Denominator of Flattening Ratio:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geodetic&quot;">Geodetic Model:&#32;</xsl:when>
       <xsl:when test="$el = &quot;hours&quot;">Hours:&#32;</xsl:when>
       <xsl:when test="$el = &quot;srcScale&quot;">Resolution of the source data:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stflddv&quot;">Subtype Default Value:&#32;</xsl:when>
       <xsl:when test="$el = &quot;tempkt&quot;">Temporal Keyword Thesaurus:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cntorgp&quot;">Contact Organization Primary:&#32;</xsl:when>
       <xsl:when test="$el = &quot;TM_GeometricPrimitive&quot;">Temporal extent:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Georef&quot;">Spatial Representation - Georeferenceable Grid:&#32;</xsl:when>
       <xsl:when test="$el = &quot;edomvd&quot;">Enumerated Domain Value Definition:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rowcount&quot;">Row Count:&#32;</xsl:when>
       <xsl:when test="$el = &quot;srcExt&quot;">Extent of the source data:&#32;</xsl:when>
       <xsl:when test="$el = &quot;srcDesc&quot;">Level of the source data:&#32;</xsl:when>
       <xsl:when test="$el = &quot;porCatInfo&quot;">Portrayal Catalogue Reference:&#32;</xsl:when>
       <xsl:when test="$el = &quot;junctid&quot;">Available Junctions Feature Class:&#32;</xsl:when>
       <xsl:when test="$el = &quot;depthdn&quot;">Depth Datum Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQDomConsis&quot;">Data quality report - Domain consistency:&#32;</xsl:when>
       <xsl:when test="$el = &quot;gallster&quot;">Gall Stereographic:&#32;</xsl:when>
       <xsl:when test="$el = &quot;SyncDate&quot;">Synchronization Date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;maintNote&quot;">Other maintenance requirements:&#32;</xsl:when>
       <xsl:when test="$el = &quot;trianInd&quot;">Triangulation has been performed:&#32;</xsl:when>
       <xsl:when test="$el = &quot;asSwDevFile&quot;">Schema software development file:&#32;</xsl:when>
       <xsl:when test="$el = &quot;depthdu&quot;">Depth Distance Units:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cordsysn&quot;">Coordinate System Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;obqmerc&quot;">Oblique Mercator:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ordres&quot;">Ordinate Resolution:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ruleemxc&quot;">Edge Maximum Cardinality:&#32;</xsl:when>
       <xsl:when test="$el = &quot;chkPtDesc&quot;">Check point description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;coordrep&quot;">Coordinate Representation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stcode&quot;">Subtype Code:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vpftype&quot;">VPF Point and Vector Object Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mollweid&quot;">Mollweide:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mdExtInfo&quot;">Metadata extension information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;resRefDate&quot;">Reference date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dataExt&quot;">Other extent information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;database&quot;">Database Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Thumbnail&quot;">Thumbnail:&#32;</xsl:when>
       <xsl:when test="$el = &quot;pkResp&quot;">Peak response wavelength:&#32;</xsl:when>
       <xsl:when test="$el = &quot;end&quot;">Ending date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;accconst&quot;">Access Constraints:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ctrlPtAv&quot;">Control points are available:&#32;</xsl:when>
       <xsl:when test="$el = &quot;formname&quot;">Format Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;datum&quot;">Datum identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;resEdDate&quot;">Edition date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dateNext&quot;">Date of next update:&#32;</xsl:when>
       <xsl:when test="$el = &quot;descKeys&quot;">Keyword Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;localp&quot;">Local Planar:&#32;</xsl:when>
       <xsl:when test="$el = &quot;metscs&quot;">Metadata Security Classification System:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rpIndName&quot;">Individual's name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;metadata&quot;">Metadata Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;depthem&quot;">Depth Encoding Method:&#32;</xsl:when>
       <xsl:when test="$el = &quot;indspref&quot;">Indirect Spatial Reference:&#32;</xsl:when>
       <xsl:when test="$el = &quot;origin&quot;">Originator:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attrtype&quot;">Attribute Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;loximuth&quot;">Loximuthal:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ruledjid&quot;">Default Junction Feature Class:&#32;</xsl:when>
       <xsl:when test="$el = &quot;latProjOri&quot;">Latitude of projection origin:&#32;</xsl:when>
       <xsl:when test="$el = &quot;centerPt&quot;">Center point:&#32;</xsl:when>
       <xsl:when test="$el = &quot;scaleDist&quot;">Ground sample distance:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ImgDesc&quot;">Content Information - Image Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;descgeog&quot;">Description of Geographic extent:&#32;</xsl:when>
       <xsl:when test="$el = &quot;sername&quot;">Series Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;obLineLat&quot;">Oblique line latitude:&#32;</xsl:when>
       <xsl:when test="$el = &quot;TempExtent&quot;">Temporal extent:&#32;</xsl:when>
       <xsl:when test="$el = &quot;localdes&quot;">Local Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;numdata&quot;">Number DataBits:&#32;</xsl:when>
       <xsl:when test="$el = &quot;winkel1&quot;">Winkel I:&#32;</xsl:when>
       <xsl:when test="$el = &quot;horizpa&quot;">Horizontal Positional Accuracy:&#32;</xsl:when>
       <xsl:when test="$el = &quot;distinfo&quot;">Distribution Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;qattracc&quot;">Quantitative Attribute Accuracy Assessment:&#32;</xsl:when>
       <xsl:when test="$el = &quot;enddatea&quot;">Ending Date of Attribute Values:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mdattim&quot;">Multiple Dates/Times:&#32;</xsl:when>
       <xsl:when test="$el = &quot;longProjCnt&quot;">Longitude of projection center:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geoEle&quot;">Geographic extent:&#32;</xsl:when>
       <xsl:when test="$el = &quot;localpd&quot;">Local Planar Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;fnorth&quot;">False Northing:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vertUoM&quot;">Units of measure, length:&#32;</xsl:when>
       <xsl:when test="$el = &quot;latprjc&quot;">Latitude of Projection Center:&#32;</xsl:when>
       <xsl:when test="$el = &quot;modsak&quot;">Modified Stereographic for Alaska:&#32;</xsl:when>
       <xsl:when test="$el = &quot;update&quot;">Maintenance and Update Frequency:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stname&quot;">Subtype Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;PublishedDocID&quot;">Published Document ID:&#32;</xsl:when>
       <xsl:when test="$el = &quot;distbrep&quot;">Distance and Bearing Representation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;UomArea&quot;">Units of measure, area:&#32;</xsl:when>
       <xsl:when test="$el = &quot;distTranOps&quot;">Transfer options:&#32;</xsl:when>
       <xsl:when test="$el = &quot;relattr&quot;">Attributed Relationship:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQComplete&quot;">Data quality report - Completeness:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attalias&quot;">Attribute Alias:&#32;</xsl:when>
       <xsl:when test="$el = &quot;latprjo&quot;">Latitude of Projection Origin:&#32;</xsl:when>
       <xsl:when test="$el = &quot;orDesc&quot;">Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;framect&quot;">Data Frame Count:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ptInPixel&quot;">Point in pixel:&#32;</xsl:when>
       <xsl:when test="$el = &quot;asName&quot;">Application schema name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;medNote&quot;">Limitations for using the medium:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rulejid&quot;">Junction Feature Class:&#32;</xsl:when>
       <xsl:when test="$el = &quot;tidtime&quot;">Time of Tide:&#32;</xsl:when>
       <xsl:when test="$el = &quot;overview&quot;">Overview Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;datasetSeries&quot;">Series:&#32;</xsl:when>
       <xsl:when test="$el = &quot;metc&quot;">Metadata Contact:&#32;</xsl:when>
       <xsl:when test="$el = &quot;isbn&quot;">ISBN:&#32;</xsl:when>
       <xsl:when test="$el = &quot;lowbps&quot;">Lowest BPS:&#32;</xsl:when>
       <xsl:when test="$el = &quot;metd&quot;">Metadata Date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vrtcount&quot;">Vertical Count:&#32;</xsl:when>
       <xsl:when test="$el = &quot;framenam&quot;">Data Frame Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;numheadl&quot;">Number Header Lines:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQLogConsis&quot;">Data quality report - Logical consistency:&#32;</xsl:when>
       <xsl:when test="$el = &quot;protocol&quot;">Connection protocol:&#32;</xsl:when>
       <xsl:when test="$el = &quot;datafiel&quot;">Data Field:&#32;</xsl:when>
       <xsl:when test="$el = &quot;extEleName&quot;">Element name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;axDimProps&quot;">Axis dimensions properties:&#32;</xsl:when>
       <xsl:when test="$el = &quot;TM_Instant&quot;">Single Date/Time:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mdStanVer&quot;">Version of the metadata standard:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rastnodt&quot;">Background Nodata Value:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dialfile&quot;">Dialup File Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;envirDesc&quot;">Processing environment:&#32;</xsl:when>
       <xsl:when test="$el = &quot;datumID&quot;">Vertical datum:&#32;</xsl:when>
       <xsl:when test="$el = &quot;timeIndicator&quot;">Time indicator:&#32;</xsl:when>
       <xsl:when test="$el = &quot;UomTime&quot;">Units of measure, time:&#32;</xsl:when>
       <xsl:when test="$el = &quot;idCredit&quot;">Credits:&#32;</xsl:when>
       <xsl:when test="$el = &quot;splttype&quot;">Split Rule:&#32;</xsl:when>
       <xsl:when test="$el = &quot;formverd&quot;">Format Version Date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;lineage&quot;">Lineage:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rightbc&quot;">Right Bounding Coordinate:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rastinfo&quot;">Raster Object Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;exTemp&quot;">Temporal extent:&#32;</xsl:when>
       <xsl:when test="$el = &quot;taxonkt&quot;">Taxonomic Keyword Thesaurus:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vertacce&quot;">Vertical Positional Accuracy Explanation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;southBL&quot;">South latitude:&#32;</xsl:when>
       <xsl:when test="$el = &quot;transDimDesc&quot;">Transformation dimension description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mdParentID&quot;">Parent identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;formvern&quot;">Format Version Number:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ptcontac&quot;">Point of Contact:&#32;</xsl:when>
       <xsl:when test="$el = &quot;digtopt&quot;">Digital Transfer Option:&#32;</xsl:when>
       <xsl:when test="$el = &quot;serinfo&quot;">Series Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;sclFacCnt&quot;">Scale factor at center line:&#32;</xsl:when>
       <xsl:when test="$el = &quot;toolnet&quot;">Tool Network Resource Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vertaccr&quot;">Vertical Positional Accuracy Report:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQQuanAttAcc&quot;">Data quality report - Quantitative attribute accuracy:&#32;</xsl:when>
       <xsl:when test="$el = &quot;subtype&quot;">Subtype Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;bounding&quot;">Bounding Coordinates:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQTopConsis&quot;">Data quality report - Topological consistency:&#32;</xsl:when>
       <xsl:when test="$el = &quot;altunits&quot;">Altitude Units:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vertaccv&quot;">Vertical Positional Accuracy Value:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geoform&quot;">Geospatial Data Presentation Form:&#32;</xsl:when>
       <xsl:when test="$el = &quot;extEleMxOc&quot;">Maximum occurrence:&#32;</xsl:when>
       <xsl:when test="$el = &quot;gnomonic&quot;">Gnomonic:&#32;</xsl:when>
       <xsl:when test="$el = &quot;abstract&quot;">Abstract:&#32;</xsl:when>
       <xsl:when test="$el = &quot;srcscale&quot;">Source Scale Denominator:&#32;</xsl:when>
       <xsl:when test="$el = &quot;esritopo&quot;">ESRI Topology:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cornerPts&quot;">Corner points:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geolun&quot;">Geologic Age Uncertainty:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQPosAcc&quot;">Data quality report - Positional accuracy:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geometObjs&quot;">Geometric objects:&#32;</xsl:when>
       <xsl:when test="$el = &quot;keywtax&quot;">Keywords/Taxon:&#32;</xsl:when>
       <xsl:when test="$el = &quot;recdenu&quot;">Recording Density Units:&#32;</xsl:when>
       <xsl:when test="$el = &quot;lboundng&quot;">Local Bounding Coordinates:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stepDesc&quot;">Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;sngdate&quot;">Single Date/Time:&#32;</xsl:when>
       <xsl:when test="$el = &quot;instance&quot;">Instance Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;statement&quot;">Lineage statement:&#32;</xsl:when>
       <xsl:when test="$el = &quot;depthres&quot;">Depth Resolution:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geolexpl&quot;">Geologic Age Explanation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;reldesc&quot;">Description of Relationship:&#32;</xsl:when>
       <xsl:when test="$el = &quot;filmDistInAv&quot;">Film distortion information is available:&#32;</xsl:when>
       <xsl:when test="$el = &quot;themekt&quot;">Theme Keyword Thesaurus:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cntOnlineRes&quot;">Online source:&#32;</xsl:when>
       <xsl:when test="$el = &quot;TypeName&quot;">Type name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stanParal&quot;">Standard parallel:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dfwidthd&quot;">Data Field Width Delimiter:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ServiceFCType&quot;">Service Feature Class Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;GM_Polygon&quot;">Polygon coordinate system:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cntvoice&quot;">Contact Voice Telephone:&#32;</xsl:when>
       <xsl:when test="$el = &quot;miller&quot;">Miller Cylindrical:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vpfterm&quot;">VPF Terms Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rastbpp&quot;">Bits Per Pixel:&#32;</xsl:when>
       <xsl:when test="$el = &quot;country&quot;">Country:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rulecat&quot;">Rule Category:&#32;</xsl:when>
       <xsl:when test="$el = &quot;toolinst&quot;">Tool Access Instructions:&#32;</xsl:when>
       <xsl:when test="$el = &quot;transDimMap&quot;">Tranformation dimension mapping:&#32;</xsl:when>
       <xsl:when test="$el = &quot;extEleSrc&quot;">Extension source:&#32;</xsl:when>
       <xsl:when test="$el = &quot;absres&quot;">Abscissa Resolution:&#32;</xsl:when>
       <xsl:when test="$el = &quot;projection&quot;">Projection identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;days&quot;">Days:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dafieldnm&quot;">Data Field Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;publish&quot;">Publisher:&#32;</xsl:when>
       <xsl:when test="$el = &quot;formspec&quot;">Format Specification:&#32;</xsl:when>
       <xsl:when test="$el = &quot;datasetSet&quot;">Dataset:&#32;</xsl:when>
       <xsl:when test="$el = &quot;proctime&quot;">Process Time:&#32;</xsl:when>
       <xsl:when test="$el = &quot;designator&quot;">Time period designator:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQNonQuanAttAcc&quot;">Data quality report - Non quantitative attribute accuracy:&#32;</xsl:when>
       <xsl:when test="$el = &quot;availabl&quot;">Available Time Period:&#32;</xsl:when>
       <xsl:when test="$el = &quot;pathnum&quot;">Path Number:&#32;</xsl:when>
       <xsl:when test="$el = &quot;procite&quot;">Process Step Citation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dataChar&quot;">Dataset character set:&#32;</xsl:when>
       <xsl:when test="$el = &quot;resFees&quot;">Terms and fees:&#32;</xsl:when>
       <xsl:when test="$el = &quot;extEleInfo&quot;">Extended element information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;edomv&quot;">Enumerated Domain Value:&#32;</xsl:when>
       <xsl:when test="$el = &quot;exTypeCode&quot;">Extent contains the resource:&#32;</xsl:when>
       <xsl:when test="$el = &quot;metshd&quot;">Metadata Security Handling Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;PublishStatus&quot;">Published Status:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mdMaint&quot;">Maintenance:&#32;</xsl:when>
       <xsl:when test="$el = &quot;winkel2&quot;">Winkel II:&#32;</xsl:when>
       <xsl:when test="$el = &quot;crossref&quot;">Cross Reference:&#32;</xsl:when>
       <xsl:when test="$el = &quot;atoutwid&quot;">Attribute Output Width:&#32;</xsl:when>
       <xsl:when test="$el = &quot;browsed&quot;">Browse Graphic File Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;MetaID&quot;">Metadata ID:&#32;</xsl:when>
       <xsl:when test="$el = &quot;idSpecUse&quot;">How the resource is used:&#32;</xsl:when>
       <xsl:when test="$el = &quot;complete&quot;">Completeness Report:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Dimen&quot;">Dimension:&#32;</xsl:when>
       <xsl:when test="$el = &quot;browsen&quot;">Browse Graphic File Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geogcsn&quot;">Geographic Coordinate System Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;measDateTm&quot;">Date of the test:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mdChar&quot;">Metadata character set:&#32;</xsl:when>
       <xsl:when test="$el = &quot;spaceobq&quot;">Space Oblique Mercator (Landsat):&#32;</xsl:when>
       <xsl:when test="$el = &quot;southbc&quot;">South Bounding Coordinate:&#32;</xsl:when>
       <xsl:when test="$el = &quot;arczone&quot;">ARC System Zone Identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;begtime&quot;">Beginning Time:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geoObjCnt&quot;">Object count:&#32;</xsl:when>
       <xsl:when test="$el = &quot;browset&quot;">Browse Graphic File Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;keyword&quot;">Keywords:&#32;</xsl:when>
       <xsl:when test="$el = &quot;falENUnits&quot;">False easting northing units:&#32;</xsl:when>
       <xsl:when test="$el = &quot;metstdn&quot;">Metadata Standard Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dsFormat&quot;">Resource format:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rpCntInfo&quot;">Contact information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;obqlpt&quot;">Oblique Line Point:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vertdef&quot;">Vertical Coordinate System Definition:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geoObjTyp&quot;">Object type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Enclosure&quot;">Enclosure:&#32;</xsl:when>
       <xsl:when test="$el = &quot;azimangl&quot;">Azimuthal Angle:&#32;</xsl:when>
       <xsl:when test="$el = &quot;taxonpro&quot;">Taxonomic Procedures:&#32;</xsl:when>
       <xsl:when test="$el = &quot;placekt&quot;">Place Keyword Thesaurus:&#32;</xsl:when>
       <xsl:when test="$el = &quot;metstdv&quot;">Metadata Standard Version:&#32;</xsl:when>
       <xsl:when test="$el = &quot;aziAngle&quot;">Azimuth angle:&#32;</xsl:when>
       <xsl:when test="$el = &quot;faxNum&quot;">Fax:&#32;</xsl:when>
       <xsl:when test="$el = &quot;errStat&quot;">Statistical method used to determine values:&#32;</xsl:when>
       <xsl:when test="$el = &quot;orthogr&quot;">Orthographic:&#32;</xsl:when>
       <xsl:when test="$el = &quot;featdesc&quot;">Feature Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;highbps&quot;">Highest BPS:&#32;</xsl:when>
       <xsl:when test="$el = &quot;asAscii&quot;">Schema ASCII file:&#32;</xsl:when>
       <xsl:when test="$el = &quot;thesaName&quot;">Thesaurus name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cntInstr&quot;">Contact instructions:&#32;</xsl:when>
       <xsl:when test="$el = &quot;recfmt&quot;">Recording Format:&#32;</xsl:when>
       <xsl:when test="$el = &quot;UomAngle&quot;">Units of measure, angle:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cntHours&quot;">Hours of service:&#32;</xsl:when>
       <xsl:when test="$el = &quot;tempkey&quot;">Temporal Keyword:&#32;</xsl:when>
       <xsl:when test="$el = &quot;obLnPtPars&quot;">Oblique line point parameter:&#32;</xsl:when>
       <xsl:when test="$el = &quot;current&quot;">Currentness Reference:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stepSrc&quot;">Source data:&#32;</xsl:when>
       <xsl:when test="$el = &quot;methkt&quot;">Methodology Keyword Thesaurus:&#32;</xsl:when>
       <xsl:when test="$el = &quot;asGraFile&quot;">Schema graphics file:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dataqual&quot;">Data Quality Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;spdoinfo&quot;">Spatial Data Organization Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;exDesc&quot;">Extent description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;citRespParty&quot;">Party responsible for the resource:&#32;</xsl:when>
       <xsl:when test="$el = &quot;esriterm&quot;">ESRI Terms Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;illAziAng&quot;">Illumination azimuth angle:&#32;</xsl:when>
       <xsl:when test="$el = &quot;enttypd&quot;">Entity Type Definition:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cntorg&quot;">Contact Organization:&#32;</xsl:when>
       <xsl:when test="$el = &quot;tool&quot;">Analytical Tool:&#32;</xsl:when>
       <xsl:when test="$el = &quot;classSys&quot;">Classification system:&#32;</xsl:when>
       <xsl:when test="$el = &quot;useconst&quot;">Use Constraints:&#32;</xsl:when>
       <xsl:when test="$el = &quot;tidinfo&quot;">Tidal Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stepDateTm&quot;">When the process occurred:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cmpGenQuan&quot;">Number of lossy compression cycles:&#32;</xsl:when>
       <xsl:when test="$el = &quot;enttypl&quot;">Entity Type Label:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cntpos&quot;">Contact Position:&#32;</xsl:when>
       <xsl:when test="$el = &quot;domowner&quot;">Domain Owner:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ordTurn&quot;">Turnaround time:&#32;</xsl:when>
       <xsl:when test="$el = &quot;accinstr&quot;">Access Instructions:&#32;</xsl:when>
       <xsl:when test="$el = &quot;bonne&quot;">Bonne:&#32;</xsl:when>
       <xsl:when test="$el = &quot;UomScale&quot;">Units of measure, scale:&#32;</xsl:when>
       <xsl:when test="$el = &quot;enttypt&quot;">Entity Type Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;albers&quot;">Albers Conical Equal Area:&#32;</xsl:when>
       <xsl:when test="$el = &quot;latres&quot;">Latitude Resolution:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cnttdd&quot;">Contact TDD/TTY Telephone:&#32;</xsl:when>
       <xsl:when test="$el = &quot;metfrd&quot;">Metadata Future Review Date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stdparll&quot;">Standard Parallel:&#32;</xsl:when>
       <xsl:when test="$el = &quot;gridsysn&quot;">Grid Coordinate System Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;classcit&quot;">Classification System Citation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attr&quot;">Attribute:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vertMinVal&quot;">Minimum value:&#32;</xsl:when>
       <xsl:when test="$el = &quot;linkage&quot;">Online location (URL):&#32;</xsl:when>
       <xsl:when test="$el = &quot;domname&quot;">Domain Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;UomLength&quot;">Units of measure, length:&#32;</xsl:when>
       <xsl:when test="$el = &quot;formatVer&quot;">Format version:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dataSource&quot;">Source data:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ScopedName&quot;">Scope:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attrvae&quot;">Attribute Value Accuracy Explanation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;TM_ClockTime&quot;">Clock time:&#32;</xsl:when>
       <xsl:when test="$el = &quot;nondig&quot;">Non-digital Form:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attrvai&quot;">Attribute Value Accuracy Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;localpgi&quot;">Local Planar Georeference Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;fees&quot;">Fees:&#32;</xsl:when>
       <xsl:when test="$el = &quot;polarst&quot;">Polar Stereographic:&#32;</xsl:when>
       <xsl:when test="$el = &quot;srcused&quot;">Source Used Citation Abbreviation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;elevmax&quot;">Elevation Maximum:&#32;</xsl:when>
       <xsl:when test="$el = &quot;minVal&quot;">Shortest wavelength:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Band&quot;">Band information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;polycon&quot;">Polyconic:&#32;</xsl:when>
       <xsl:when test="$el = &quot;methkey&quot;">Methodology Keyword:&#32;</xsl:when>
       <xsl:when test="$el = &quot;measResult&quot;">General test results:&#32;</xsl:when>
       <xsl:when test="$el = &quot;sclFac&quot;">Scale factor applied to values:&#32;</xsl:when>
       <xsl:when test="$el = &quot;maintFreq&quot;">Update frequency:&#32;</xsl:when>
       <xsl:when test="$el = &quot;imagQuCode&quot;">Image quality code:&#32;</xsl:when>
       <xsl:when test="$el = &quot;extEleDef&quot;">Definition:&#32;</xsl:when>
       <xsl:when test="$el = &quot;featIntSet&quot;">Feature instances:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rulefest&quot;">From Edge Subtype:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dimName&quot;">Dimension name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;gringlat&quot;">G-Ring Latitude:&#32;</xsl:when>
       <xsl:when test="$el = &quot;measName&quot;">Name of the test:&#32;</xsl:when>
       <xsl:when test="$el = &quot;methodid&quot;">Methodolgy Identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ptvctcnt&quot;">Point and Vector Object Count:&#32;</xsl:when>
       <xsl:when test="$el = &quot;chkPtAv&quot;">Check points are available:&#32;</xsl:when>
       <xsl:when test="$el = &quot;altsys&quot;">Altitude System Definition:&#32;</xsl:when>
       <xsl:when test="$el = &quot;unitsODist&quot;">Units of distribution (e.g., tiles):&#32;</xsl:when>
       <xsl:when test="$el = &quot;RS_Identifier&quot;">Identifier Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cntperp&quot;">Contact Person Primary:&#32;</xsl:when>
       <xsl:when test="$el = &quot;direct&quot;">Direct Spatial Reference Method:&#32;</xsl:when>
       <xsl:when test="$el = &quot;lworkcit&quot;">Larger Work Citation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rastcomp&quot;">Compression Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;collTitle&quot;">Collection title:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geolscal&quot;">Geologic Time Scale:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attracc&quot;">Attribute Value Accuracy Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;handDesc&quot;">Additional restrictions:&#32;</xsl:when>
       <xsl:when test="$el = &quot;extShortName&quot;">Short name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;bearrefd&quot;">Bearing Reference Direction:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dqScope&quot;">Scope of quality information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stfldnam&quot;">Subtype Attribute Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;edom&quot;">Enumerated Domain:&#32;</xsl:when>
       <xsl:when test="$el = &quot;taxonkey&quot;">Taxonomic Keyword:&#32;</xsl:when>
       <xsl:when test="$el = &quot;usageDate&quot;">Date and time of use:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dsgpolyo&quot;">Data Set G-Polygon Outer G-Ring:&#32;</xsl:when>
       <xsl:when test="$el = &quot;bearrefm&quot;">Bearing Reference Meridian:&#32;</xsl:when>
       <xsl:when test="$el = &quot;lensDistInAv&quot;">Lens distortion information is available:&#32;</xsl:when>
       <xsl:when test="$el = &quot;sfctrmer&quot;">Scale Factor at Central Meridian:&#32;</xsl:when>
       <xsl:when test="$el = &quot;tooldesc&quot;">Analytical Tool Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;months&quot;">Months:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dsgpolyx&quot;">Data Set G-Polygon Exclusion G-Ring:&#32;</xsl:when>
       <xsl:when test="$el = &quot;efeageom&quot;">ESRI Feature Geometry:&#32;</xsl:when>
       <xsl:when test="$el = &quot;idinfo&quot;">Identification Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;upScpDesc&quot;">Scope Description Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;browseem&quot;">Browse Graphic Embedded:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dataIdInfo&quot;">Identification Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;LegConsts&quot;">Legal constraints:&#32;</xsl:when>
       <xsl:when test="$el = &quot;formcont&quot;">Format Information Content:&#32;</xsl:when>
       <xsl:when test="$el = &quot;compress&quot;">Compression Support:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cloud&quot;">Cloud Cover:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rastityp&quot;">Image Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;lamberta&quot;">Lambert Azimuthal Equal Area:&#32;</xsl:when>
       <xsl:when test="$el = &quot;aName&quot;">Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;lambertc&quot;">Lambert Conformal Conic:&#32;</xsl:when>
       <xsl:when test="$el = &quot;scpLvl&quot;">Level of the data:&#32;</xsl:when>
       <xsl:when test="$el = &quot;CreaDate&quot;">Creation Date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;title&quot;">Title:&#32;</xsl:when>
       <xsl:when test="$el = &quot;clkTime&quot;">Clock time:&#32;</xsl:when>
       <xsl:when test="$el = &quot;Descript&quot;">Description of enclosure:&#32;</xsl:when>
       <xsl:when test="$el = &quot;classsys&quot;">Classification System/Authority:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mdConst&quot;">Metadata constraints:&#32;</xsl:when>
       <xsl:when test="$el = &quot;localgeo&quot;">Local Georeference Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;altdatum&quot;">Altitude Datum Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;taxonrn&quot;">Taxon Rank Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ModDate&quot;">Modification Date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;sinusoid&quot;">Sinusoidal:&#32;</xsl:when>
       <xsl:when test="$el = &quot;MdCoRefSys&quot;">Polygon coordinate system:&#32;</xsl:when>
       <xsl:when test="$el = &quot;sfctrlin&quot;">Scale Factor at Center Line:&#32;</xsl:when>
       <xsl:when test="$el = &quot;networka&quot;">Network Address:&#32;</xsl:when>
       <xsl:when test="$el = &quot;taxonrv&quot;">Taxon Rank Value:&#32;</xsl:when>
       <xsl:when test="$el = &quot;tidedat&quot;">Tidal Datum:&#32;</xsl:when>
       <xsl:when test="$el = &quot;zone&quot;">Zone number:&#32;</xsl:when>
       <xsl:when test="$el = &quot;junctst&quot;">Available Junctions Subtype:&#32;</xsl:when>
       <xsl:when test="$el = &quot;method&quot;">Methodology:&#32;</xsl:when>
       <xsl:when test="$el = &quot;extDomCode&quot;">Codelist value:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ordering&quot;">Ordering Instructions:&#32;</xsl:when>
       <xsl:when test="$el = &quot;GeoBndBox&quot;">Geographic extent:&#32;</xsl:when>
       <xsl:when test="$el = &quot;idCitation&quot;">Citation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;secsys&quot;">Security Classification System:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geolest&quot;">Geologic Age Estimate:&#32;</xsl:when>
       <xsl:when test="$el = &quot;srccite&quot;">Source Citation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;gvnsp&quot;">General Vertical Near-sided Perspective:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dataLineage&quot;">Lineage:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQFormConsis&quot;">Data quality report - Formal consistency:&#32;</xsl:when>
       <xsl:when test="$el = &quot;digtinfo&quot;">Digital Transfer Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;eaover&quot;">Entity and Attribute Overview:&#32;</xsl:when>
       <xsl:when test="$el = &quot;qvertpa&quot;">Quantitative Vertical Positional Accuracy Assessment:&#32;</xsl:when>
       <xsl:when test="$el = &quot;useLimit&quot;">Limitations of use:&#32;</xsl:when>
       <xsl:when test="$el = &quot;spatRepInfo&quot;">Spatial Representation Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;secinfo&quot;">Security Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;obqlazim&quot;">Oblique Line Azimuth:&#32;</xsl:when>
       <xsl:when test="$el = &quot;networkr&quot;">Network Resource Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;evalProc&quot;">Description of evaluation procedure:&#32;</xsl:when>
       <xsl:when test="$el = &quot;citIdType&quot;">Type of identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rastdtyp&quot;">Raster Display Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;extEleOb&quot;">Obligation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;udom&quot;">Unrepresentable Domain:&#32;</xsl:when>
       <xsl:when test="$el = &quot;resdesc&quot;">Resource Description:&#32;</xsl:when>
       <xsl:when test="$el = &quot;postCode&quot;">Postal code:&#32;</xsl:when>
       <xsl:when test="$el = &quot;georefPars&quot;">Georeferencing parameters:&#32;</xsl:when>
       <xsl:when test="$el = &quot;resTitle&quot;">Title:&#32;</xsl:when>
       <xsl:when test="$el = &quot;projParas&quot;">Projection parameters:&#32;</xsl:when>
       <xsl:when test="$el = &quot;srctime&quot;">Source Time Period of Content:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cellGeo&quot;">Cell geometry:&#32;</xsl:when>
       <xsl:when test="$el = &quot;img&quot;">Enclosure type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;extEleCond&quot;">Condition:&#32;</xsl:when>
       <xsl:when test="$el = &quot;conPass&quot;">Test passed:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ruledjst&quot;">Default Junction Subtype:&#32;</xsl:when>
       <xsl:when test="$el = &quot;extEleDomVal&quot;">Domain:&#32;</xsl:when>
       <xsl:when test="$el = &quot;quartic&quot;">Quartic Authalic:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQGridDataPosAcc&quot;">Data quality report - Gridded data positional accuracy:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ellips&quot;">Ellipsoid Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;extEleParEnt&quot;">Parent element:&#32;</xsl:when>
       <xsl:when test="$el = &quot;catCitation&quot;">Feature catalogue citation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;datacred&quot;">Data Set Credit:&#32;</xsl:when>
       <xsl:when test="$el = &quot;otherprj&quot;">Other Projection’s Definition:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mdContact&quot;">Metadata contact:&#32;</xsl:when>
       <xsl:when test="$el = &quot;conExpl&quot;">Meaning of the result:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rastxsz&quot;">Cell Size X Direction:&#32;</xsl:when>
       <xsl:when test="$el = &quot;portCatCit&quot;">Portrayal catalogue citation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;atnumdec&quot;">Attribute Number of Decimals:&#32;</xsl:when>
       <xsl:when test="$el = &quot;beggeol&quot;">Beginning Geologic Age:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ContInfo&quot;">Content Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;prcTypCde&quot;">Processing level code:&#32;</xsl:when>
       <xsl:when test="$el = &quot;parity&quot;">Parity:&#32;</xsl:when>
       <xsl:when test="$el = &quot;uomName&quot;">Units:&#32;</xsl:when>
       <xsl:when test="$el = &quot;classmod&quot;">Classification System Modifications:&#32;</xsl:when>
       <xsl:when test="$el = &quot;orName&quot;">Name of resource:&#32;</xsl:when>
       <xsl:when test="$el = &quot;attDesc&quot;">Attribute described by cell values:&#32;</xsl:when>
       <xsl:when test="$el = &quot;conversionToISOstandardUnit&quot;">Conversion to metric:&#32;</xsl:when>
       <xsl:when test="$el = &quot;bgFileType&quot;">File type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;secclass&quot;">Security Classification:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dialinst&quot;">Dialup Instructions:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rulejunc&quot;">Available Junctions:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQAccTimeMeas&quot;">Data quality report - Accuracy of a time measurement:&#32;</xsl:when>
       <xsl:when test="$el = &quot;longcm&quot;">Longitude of Central Meridian:&#32;</xsl:when>
       <xsl:when test="$el = &quot;refDateType&quot;">Type of date:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rdomstdv&quot;">Range Domain Standard Deviation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;atindex&quot;">Attribute Indexed:&#32;</xsl:when>
       <xsl:when test="$el = &quot;otfcname&quot;">Origin Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;voiceNum&quot;">Voice:&#32;</xsl:when>
       <xsl:when test="$el = &quot;sclFacEqu&quot;">Scale factor at equator:&#32;</xsl:when>
       <xsl:when test="$el = &quot;ConResult&quot;">Conformance test results:&#32;</xsl:when>
       <xsl:when test="$el = &quot;cntinfo&quot;">Contact Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;casesens&quot;">Case Sensitive:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mdStanName&quot;">Name of the metadata standard used:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQTempConsis&quot;">Data quality report - Temporal consistency:&#32;</xsl:when>
       <xsl:when test="$el = &quot;MemberName&quot;">Member name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;graphOver&quot;">Graphic overview:&#32;</xsl:when>
       <xsl:when test="$el = &quot;calTime&quot;">Time:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rulehelp&quot;">Rule Help:&#32;</xsl:when>
       <xsl:when test="$el = &quot;edition&quot;">Edition:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dimResol&quot;">Resolution:&#32;</xsl:when>
       <xsl:when test="$el = &quot;geogunit&quot;">Geographic Coordinate Units:&#32;</xsl:when>
       <xsl:when test="$el = &quot;timeperd&quot;">Time Period of Content:&#32;</xsl:when>
       <xsl:when test="$el = &quot;mdHrLv&quot;">Scope of the data described by the metadata:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQAbsExtPosAcc&quot;">Data quality report - Absolute external positional accuracy:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rulejst&quot;">Junction Subtype:&#32;</xsl:when>
       <xsl:when test="$el = &quot;suppInfo&quot;">Supplemental information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;eleDataType&quot;">Data type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;maintScp&quot;">Scope of the updates:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vertEle&quot;">Vertical extent:&#32;</xsl:when>
       <xsl:when test="$el = &quot;netinfo&quot;">Geometric Network Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;dimSize&quot;">Dimension size:&#32;</xsl:when>
       <xsl:when test="$el = &quot;seconds&quot;">Seconds:&#32;</xsl:when>
       <xsl:when test="$el = &quot;presForm&quot;">Presentation format:&#32;</xsl:when>
       <xsl:when test="$el = &quot;QuanResult&quot;">Quality test results:&#32;</xsl:when>
       <xsl:when test="$el = &quot;spindex&quot;">Spatial Index:&#32;</xsl:when>
       <xsl:when test="$el = &quot;idref&quot;">Identification Reference:&#32;</xsl:when>
       <xsl:when test="$el = &quot;tidesup&quot;">Supplemental Tidal Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;spatSchName&quot;">Spatial schema name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;postal&quot;">Postal Code:&#32;</xsl:when>
       <xsl:when test="$el = &quot;GeoDesc&quot;">Geographic extent:&#32;</xsl:when>
       <xsl:when test="$el = &quot;equirect&quot;">Equirectangular:&#32;</xsl:when>
       <xsl:when test="$el = &quot;quanVal&quot;">Result value:&#32;</xsl:when>
       <xsl:when test="$el = &quot;srccurr&quot;">Source Currentness Reference:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stratkt&quot;">Stratum Keyword Thesaurus:&#32;</xsl:when>
       <xsl:when test="$el = &quot;connrule&quot;">Connectivity Rule:&#32;</xsl:when>
       <xsl:when test="$el = &quot;idPoC&quot;">Point of contact:&#32;</xsl:when>
       <xsl:when test="$el = &quot;DQTempAcc&quot;">Data quality report - Temporal accuracy:&#32;</xsl:when>
       <xsl:when test="$el = &quot;eainfo&quot;">Entity and Attribute Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;other&quot;">Other:&#32;</xsl:when>
       <xsl:when test="$el = &quot;orienta&quot;">Orientation:&#32;</xsl:when>
       <xsl:when test="$el = &quot;obLnAziPars&quot;">Oblique line azimuth parameter:&#32;</xsl:when>
       <xsl:when test="$el = &quot;pubplace&quot;">Publication Place:&#32;</xsl:when>
       <xsl:when test="$el = &quot;stVrLongPl&quot;">Straight vertical longitude from pole:&#32;</xsl:when>
       <xsl:when test="$el = &quot;vertDatum&quot;">vertical datum:&#32;</xsl:when>
       <xsl:when test="$el = &quot;eMailAdd&quot;">e-mail address:&#32;</xsl:when>
       <xsl:when test="$el = &quot;numDims&quot;">Number of dimensions:&#32;</xsl:when>
       <xsl:when test="$el = &quot;appProfile&quot;">Application profile:&#32;</xsl:when>
       <xsl:when test="$el = &quot;planar&quot;">Planar:&#32;</xsl:when>
       <xsl:when test="$el = &quot;covDim&quot;">Range dimension Information:&#32;</xsl:when>
       <xsl:when test="$el = &quot;serType&quot;">Type of service:&#32;</xsl:when>
       <xsl:when test="$el = &quot;transmer&quot;">Transverse Mercator:&#32;</xsl:when>
       <xsl:when test="$el = &quot;user&quot;">User Name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;issId&quot;">Issue:&#32;</xsl:when>
       <xsl:when test="$el = &quot;rastysz&quot;">Cell Size Y Direction:&#32;</xsl:when>
       <xsl:when test="$el = &quot;pubtime&quot;">Publication Time:&#32;</xsl:when>
       <xsl:when test="$el = &quot;bitsPerVal&quot;">Number of bits per value:&#32;</xsl:when>
       <xsl:when test="$el = &quot;medName&quot;">Medium name:&#32;</xsl:when>
       <xsl:when test="$el = &quot;place&quot;">Place:&#32;</xsl:when>
       <xsl:when test="$el = &quot;refSysID&quot;">Reference system identifier:&#32;</xsl:when>
       <xsl:when test="$el = &quot;tmPosition&quot;">Time:&#32;</xsl:when>

       <!-- GOS Specific Tags
       <xsl:when test="$el = &quot;gos&quot;">GOS:&#32;</xsl:when>
       <xsl:when test="$el = &quot;certification&quot;">Rating:&#32;</xsl:when>
       <xsl:when test="$el = &quot;activity&quot;">Activity:&#32;</xsl:when>
       <xsl:when test="$el = &quot;fundingtype&quot;">Funding Type:&#32;</xsl:when>
       <xsl:when test="$el = &quot;projectedcost&quot;">Projected Cost:&#32;</xsl:when>
       <xsl:when test="$el = &quot;budgetedcost&quot;">Budgeted Cost:&#32;</xsl:when>
       <xsl:when test="$el = &quot;partneredcost&quot;">Partnered Cost:&#32;</xsl:when>
       <xsl:when test="$el = &quot;fee&quot;">Fee:&#32;</xsl:when> -->


       <!-- All Other Tags -->
       <xsl:otherwise>(<xsl:value-of select="$el"/><xsl:text>):</xsl:text></xsl:otherwise>
     </xsl:choose>
     </em>
  </xsl:template>

  <!-- filtered out -->
  <xsl:template match="PubSourceCd | ContentDevTypeCd">
  </xsl:template>


  <xsl:template match="text()">
    <xsl:value-of select="."/>
  </xsl:template>
  <xsl:template match="onlink">
    <xsl:if test="text() != &apos;&apos;">
      <dt>
        <xsl:call-template name="get_text">
          <xsl:with-param name="el">
            <xsl:value-of select="local-name()"/>
          </xsl:with-param>
        </xsl:call-template>
        <xsl:element name="a">
          <xsl:attribute name="href">
            <xsl:value-of select="."/>
          </xsl:attribute>
          <xsl:value-of select="."/>
        </xsl:element>
      </dt>
    </xsl:if>
  </xsl:template>
   <xsl:template match="orDesc">
        <dt>
        <xsl:call-template name="get_text">
          <xsl:with-param name="el">
            <xsl:value-of select="local-name()"/>
          </xsl:with-param>
        </xsl:call-template>
        <xsl:choose>
          <xsl:when test="text()=&apos;001&apos;">
            <xsl:text>Live Data and Maps</xsl:text>
          </xsl:when>
          <xsl:when test="text()=&apos;002&apos;">
            <xsl:text>Downloadable Data</xsl:text>
          </xsl:when>
          <xsl:when test="text()=&apos;003&apos;">
  		  <xsl:text>Offline Data</xsl:text>
          </xsl:when>
          <xsl:when test="text()=&apos;004&apos;">
  		  <xsl:text>Static Map Images</xsl:text>
          </xsl:when>
          <xsl:when test="text()=&apos;005&apos;">
  		  <xsl:text>Other Documents</xsl:text>
          </xsl:when>
          <xsl:when test="text()=&apos;006&apos;">
  		  <xsl:text>Applications</xsl:text>
          </xsl:when>
          <xsl:when test="text()=&apos;007&apos;">
  		  <xsl:text>Geographic Services</xsl:text>
          </xsl:when>
          <xsl:when test="text()=&apos;008&apos;">
  		  <xsl:text>Clearinghouses</xsl:text>
          </xsl:when>
          <xsl:when test="text()=&apos;009&apos;">
  		  <xsl:text>Map Files</xsl:text>
          </xsl:when>
          <xsl:when test="text()=&apos;010&apos;">
  		  <xsl:text>Geographic Activities</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="."/>
          </xsl:otherwise>
        </xsl:choose>
      </dt>
  </xsl:template>

  <xsl:template match="linkage">
    <xsl:if test="text() != &apos;&apos;">
      <dt>
        <xsl:call-template name="get_text">
          <xsl:with-param name="el">
            <xsl:value-of select="local-name()"/>
          </xsl:with-param>
        </xsl:call-template>
        <xsl:element name="a">
          <xsl:attribute name="href">
            <xsl:value-of select="."/>
          </xsl:attribute>
          <xsl:value-of select="."/>
        </xsl:element>
      </dt>
    </xsl:if>
  </xsl:template>
  <xsl:template match="tranParaAv">
    <dt>
      <xsl:call-template name="get_text">
        <xsl:with-param name="el">
          <xsl:value-of select="local-name()"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="text()=&apos;1&apos;">
          <xsl:text>yes</xsl:text>
        </xsl:when>
        <xsl:when test="text()=&apos;0&apos;">
          <xsl:text>no</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </dt>
  </xsl:template>
  <xsl:template match="chkPtAv">
    <dt>
      <xsl:call-template name="get_text">
        <xsl:with-param name="el">
          <xsl:value-of select="local-name()"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="text()=&apos;1&apos;">
          <xsl:text>yes</xsl:text>
        </xsl:when>
        <xsl:when test="text()=&apos;0&apos;">
          <xsl:text>no</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </dt>
  </xsl:template>
  <xsl:template match="ctrlPtAv">
    <dt>
      <xsl:call-template name="get_text">
        <xsl:with-param name="el">
          <xsl:value-of select="local-name()"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="text()=&apos;1&apos;">
          <xsl:text>yes</xsl:text>
        </xsl:when>
        <xsl:when test="text()=&apos;0&apos;">
          <xsl:text>no</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </dt>
  </xsl:template>
  <xsl:template match="orieParaAv">
    <dt>
      <xsl:call-template name="get_text">
        <xsl:with-param name="el">
          <xsl:value-of select="local-name()"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="text()=&apos;1&apos;">
          <xsl:text>yes</xsl:text>
        </xsl:when>
        <xsl:when test="text()=&apos;0&apos;">
          <xsl:text>no</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </dt>
  </xsl:template>
  <xsl:template match="incWithDS">
    <dt>
      <xsl:call-template name="get_text">
        <xsl:with-param name="el">
          <xsl:value-of select="local-name()"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="text()=&apos;1&apos;">
          <xsl:text>yes</xsl:text>
        </xsl:when>
        <xsl:when test="text()=&apos;0&apos;">
          <xsl:text>no</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </dt>
  </xsl:template>
  <xsl:template match="compCode">
    <dt>
      <xsl:call-template name="get_text">
        <xsl:with-param name="el">
          <xsl:value-of select="local-name()"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="text()=&apos;1&apos;">
          <xsl:text>yes</xsl:text>
        </xsl:when>
        <xsl:when test="text()=&apos;0&apos;">
          <xsl:text>no</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </dt>
  </xsl:template>
  <xsl:template match="trianInd">
    <dt>
      <xsl:call-template name="get_text">
        <xsl:with-param name="el">
          <xsl:value-of select="local-name()"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="text()=&apos;1&apos;">
          <xsl:text>yes</xsl:text>
        </xsl:when>
        <xsl:when test="text()=&apos;0&apos;">
          <xsl:text>no</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </dt>
  </xsl:template>
  <xsl:template match="radCalDatAv">
    <dt>
      <xsl:call-template name="get_text">
        <xsl:with-param name="el">
          <xsl:value-of select="local-name()"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="text()=&apos;1&apos;">
          <xsl:text>yes</xsl:text>
        </xsl:when>
        <xsl:when test="text()=&apos;0&apos;">
          <xsl:text>no</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </dt>
  </xsl:template>
  <xsl:template match="camCalInAv">
    <dt>
      <xsl:call-template name="get_text">
        <xsl:with-param name="el">
          <xsl:value-of select="local-name()"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="text()=&apos;1&apos;">
          <xsl:text>yes</xsl:text>
        </xsl:when>
        <xsl:when test="text()=&apos;0&apos;">
          <xsl:text>no</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </dt>
  </xsl:template>
  <xsl:template match="filmDistInAv">
    <dt>
      <xsl:call-template name="get_text">
        <xsl:with-param name="el">
          <xsl:value-of select="local-name()"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="text()=&apos;1&apos;">
          <xsl:text>yes</xsl:text>
        </xsl:when>
        <xsl:when test="text()=&apos;0&apos;">
          <xsl:text>no</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </dt>
  </xsl:template>
  <xsl:template match="lensDistInAv">
    <dt>
      <xsl:call-template name="get_text">
        <xsl:with-param name="el">
          <xsl:value-of select="local-name()"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="text()=&apos;1&apos;">
          <xsl:text>yes</xsl:text>
        </xsl:when>
        <xsl:when test="text()=&apos;0&apos;">
          <xsl:text>no</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </dt>
  </xsl:template>
  <xsl:template match="conPass">
    <dt>
      <xsl:call-template name="get_text">
        <xsl:with-param name="el">
          <xsl:value-of select="local-name()"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="text()=&apos;1&apos;">
          <xsl:text>yes</xsl:text>
        </xsl:when>
        <xsl:when test="text()=&apos;0&apos;">
          <xsl:text>no</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </dt>
  </xsl:template>
  <xsl:template match="exTypeCode">
    <dt>
      <xsl:call-template name="get_text">
        <xsl:with-param name="el">
          <xsl:value-of select="local-name()"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="text()=&apos;1&apos;">
          <xsl:text>yes</xsl:text>
        </xsl:when>
        <xsl:when test="text()=&apos;0&apos;">
          <xsl:text>no</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </dt>
  </xsl:template>
  <xsl:template match="languageCode">
    <xsl:choose>
      <xsl:when test="@value = &apos;fo&apos;">
        <xsl:text>Faroese</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ur&apos;">
        <xsl:text>Urdu</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;na&apos;">
        <xsl:text>Nauru</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;fr&apos;">
        <xsl:text>French</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ne&apos;">
        <xsl:text>Nepali</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;uz&apos;">
        <xsl:text>Uzbek</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;fy&apos;">
        <xsl:text>Frisian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;nl&apos;">
        <xsl:text>Dutch</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;no&apos;">
        <xsl:text>Norwegian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ga&apos;">
        <xsl:text>Irish</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;gd&apos;">
        <xsl:text>Scots Gaelic</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;vi&apos;">
        <xsl:text>Vietnamese</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;gl&apos;">
        <xsl:text>Galician</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;vo&apos;">
        <xsl:text>Volapuk</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;gn&apos;">
        <xsl:text>Guarani</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;oc&apos;">
        <xsl:text>Occitan</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;gu&apos;">
        <xsl:text>Gujarati</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;om&apos;">
        <xsl:text>(Afan) Oromo</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;or&apos;">
        <xsl:text>Oriya</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ha&apos;">
        <xsl:text>Hausa</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;hi&apos;">
        <xsl:text>Hindi</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;wo&apos;">
        <xsl:text>Wolof</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;pa&apos;">
        <xsl:text>Punjabi</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;hr&apos;">
        <xsl:text>Croatian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;aa&apos;">
        <xsl:text>Afar</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ab&apos;">
        <xsl:text>Abkhazian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;hu&apos;">
        <xsl:text>Hungarian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;af&apos;">
        <xsl:text>Afrikaans</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;hy&apos;">
        <xsl:text>Armenian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;pl&apos;">
        <xsl:text>Polish</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;am&apos;">
        <xsl:text>Amharic</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ia&apos;">
        <xsl:text>Interlingua</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ps&apos;">
        <xsl:text>Pashto, Pushto</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ar&apos;">
        <xsl:text>Arabic</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;pt&apos;">
        <xsl:text>Portugese</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;as&apos;">
        <xsl:text>Assamese</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ie&apos;">
        <xsl:text>Interlingue</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;xh&apos;">
        <xsl:text>Xhosa</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ay&apos;">
        <xsl:text>Aymara</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;az&apos;">
        <xsl:text>Azerbaijani</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ik&apos;">
        <xsl:text>Inupiak</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;in&apos;">
        <xsl:text>Indonesian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ba&apos;">
        <xsl:text>Bashkir</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;is&apos;">
        <xsl:text>Icelandic</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;it&apos;">
        <xsl:text>Italian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;be&apos;">
        <xsl:text>Byelorussian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;iw&apos;">
        <xsl:text>Hebrew</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;bg&apos;">
        <xsl:text>Bulgarian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;bh&apos;">
        <xsl:text>Bihari</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;bi&apos;">
        <xsl:text>Bislama</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;bn&apos;">
        <xsl:text>Bengali, Bangla</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;bo&apos;">
        <xsl:text>Tibetan</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ja&apos;">
        <xsl:text>Japanese</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;br&apos;">
        <xsl:text>Breton</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;qu&apos;">
        <xsl:text>Quechua</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ji&apos;">
        <xsl:text>Yiddish</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;yo&apos;">
        <xsl:text>Yoruba</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ca&apos;">
        <xsl:text>Catalan</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;jw&apos;">
        <xsl:text>Javanese</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;rm&apos;">
        <xsl:text>Rhaeto-Romance</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;rn&apos;">
        <xsl:text>Kirundi</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ro&apos;">
        <xsl:text>Romanian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;co&apos;">
        <xsl:text>Corsican</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ka&apos;">
        <xsl:text>Georgian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;cs&apos;">
        <xsl:text>Czech</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ru&apos;">
        <xsl:text>Russian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;rw&apos;">
        <xsl:text>Kinyarwanda</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;zh&apos;">
        <xsl:text>Chinese</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;cy&apos;">
        <xsl:text>Welsh</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;kk&apos;">
        <xsl:text>Kazakh</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;kl&apos;">
        <xsl:text>Greenlandic</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;km&apos;">
        <xsl:text>Cambodian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;kn&apos;">
        <xsl:text>Kannada</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ko&apos;">
        <xsl:text>Korean</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;sa&apos;">
        <xsl:text>Sanskrit</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;da&apos;">
        <xsl:text>Danish</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ks&apos;">
        <xsl:text>Kashmiri</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;zu&apos;">
        <xsl:text>Zulu</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;sd&apos;">
        <xsl:text>Sindhi</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ku&apos;">
        <xsl:text>Kurdish</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;de&apos;">
        <xsl:text>German</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;sg&apos;">
        <xsl:text>Sangho</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;sh&apos;">
        <xsl:text>Serbo-Croatian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;si&apos;">
        <xsl:text>Singhalese</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ky&apos;">
        <xsl:text>Kirghiz</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;sk&apos;">
        <xsl:text>Slovak</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;sl&apos;">
        <xsl:text>Slovenian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;sm&apos;">
        <xsl:text>Samoan</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;sn&apos;">
        <xsl:text>Shona</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;so&apos;">
        <xsl:text>Somali</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;sq&apos;">
        <xsl:text>Albanian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;la&apos;">
        <xsl:text>Latin</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;sr&apos;">
        <xsl:text>Serbian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ss&apos;">
        <xsl:text>Siswati</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;st&apos;">
        <xsl:text>Sesotho</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;su&apos;">
        <xsl:text>Sundanese</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;sv&apos;">
        <xsl:text>Swedish</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;sw&apos;">
        <xsl:text>Swahili</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;dz&apos;">
        <xsl:text>Bhutani</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ln&apos;">
        <xsl:text>Lingala</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;lo&apos;">
        <xsl:text>Laothian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ta&apos;">
        <xsl:text>Tamil</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;lt&apos;">
        <xsl:text>Lithuanian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;te&apos;">
        <xsl:text>Telugu</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;lv&apos;">
        <xsl:text>Latvian, Lettish</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;tg&apos;">
        <xsl:text>Tajik</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;th&apos;">
        <xsl:text>Thai</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ti&apos;">
        <xsl:text>Tigrinya</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;tk&apos;">
        <xsl:text>Turkmen</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;tl&apos;">
        <xsl:text>Tagalog</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;el&apos;">
        <xsl:text>Greek</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;tn&apos;">
        <xsl:text>Setswana</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;en&apos;">
        <xsl:text>English</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;to&apos;">
        <xsl:text>Tonga</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;eo&apos;">
        <xsl:text>Esperanto</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;tr&apos;">
        <xsl:text>Turkish</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ts&apos;">
        <xsl:text>Tsonga</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;es&apos;">
        <xsl:text>Spanish</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;tt&apos;">
        <xsl:text>Tatar</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;et&apos;">
        <xsl:text>Estonian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;eu&apos;">
        <xsl:text>Basque</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;tw&apos;">
        <xsl:text>Twi</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;mg&apos;">
        <xsl:text>Malagasy</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;mi&apos;">
        <xsl:text>Maori</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;mk&apos;">
        <xsl:text>Macedonian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ml&apos;">
        <xsl:text>Malayalam</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;mn&apos;">
        <xsl:text>Mongolian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;mo&apos;">
        <xsl:text>Moldavian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;mr&apos;">
        <xsl:text>Marathi</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;fa&apos;">
        <xsl:text>Persian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;ms&apos;">
        <xsl:text>Malay</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;mt&apos;">
        <xsl:text>Maltese</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;my&apos;">
        <xsl:text>Burmese</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;fi&apos;">
        <xsl:text>Finnish</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;fj&apos;">
        <xsl:text>Fiji</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;uk&apos;">
        <xsl:text>Ukrainian</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="CharSetCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;010&apos;">
        <xsl:text>8859part5 - Cyrillic</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>ucs4 - 32 bit Universal Character Set</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;011&apos;">
        <xsl:text>8859part6 - Arabic</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>utf7 - 7 bit UCS Transfer Format</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;020&apos;">
        <xsl:text>eucJP - Japanese for UNIX</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;012&apos;">
        <xsl:text>8859part7 - Greek</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>utf8 - 8 bit UCS Transfer Format</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;021&apos;">
        <xsl:text>U.S. ASCII</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;013&apos;">
        <xsl:text>8859part8 - Hebrew</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>utf16 - 16 bit UCS Transfer Format</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;022&apos;">
        <xsl:text>ebcdic - IBM mainframe</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;014&apos;">
        <xsl:text>8859part9 - Latin-5, Turkish</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;006&apos;">
        <xsl:text>8859part1 - Latin-1, Western European</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;023&apos;">
        <xsl:text>eucKR - Korean</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;015&apos;">
        <xsl:text>8859part11 - Thai</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;007&apos;">
        <xsl:text>8859part2 - Latin-2, Central European</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;024&apos;">
        <xsl:text>big5 - Taiwanese</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;016&apos;">
        <xsl:text>8859part14 - Latin-8</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;008&apos;">
        <xsl:text>8859part3 - Latin-3, South European</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;017&apos;">
        <xsl:text>8859part15 - Latin-9</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;009&apos;">
        <xsl:text>8859part4 - Latin-4, North European</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;018&apos;">
        <xsl:text>jis - Japanese for electronic transmission</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;019&apos;">
        <xsl:text>shiftJIS - Japanese for MS-DOS</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="ScopeCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>attribute</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;010&apos;">
        <xsl:text>feature type</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>attribute type</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;011&apos;">
        <xsl:text>property type</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>collection hardware</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;012&apos;">
        <xsl:text>field session</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>collection session</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;013&apos;">
        <xsl:text>software</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>dataset</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;014&apos;">
        <xsl:text>service</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;006&apos;">
        <xsl:text>series</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;015&apos;">
        <xsl:text>model</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;007&apos;">
        <xsl:text>non-geographic dataset</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;008&apos;">
        <xsl:text>dimension group</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;009&apos;">
        <xsl:text>feature</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="MaintFreqCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>continual</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;010&apos;">
        <xsl:text>irregular</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>daily</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;011&apos;">
        <xsl:text>not planned</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>weekly</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>fortnightly</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>monthly</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;998&apos;">
        <xsl:text>unknown</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;006&apos;">
        <xsl:text>quarterly</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;007&apos;">
        <xsl:text>biannually</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;008&apos;">
        <xsl:text>annually</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;009&apos;">
        <xsl:text>as needed</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="ClasscationCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>secret</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>top secret</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>unclassified</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>restricted</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>confidential</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="RestrictCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>copyright</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>patent</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>patent pending</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>trademark</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>license</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;006&apos;">
        <xsl:text>intellectual property rights</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;007&apos;">
        <xsl:text>restricted</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;008&apos;">
        <xsl:text>other restrictions</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="TopicCatCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>farming</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;010&apos;">
        <xsl:text>imagery base maps, and earth cover</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>biota</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;011&apos;">
        <xsl:text>intelligence, and military</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>boundaries</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;012&apos;">
        <xsl:text>inland waters</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>climatology, meteorology, and atmosphere</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;013&apos;">
        <xsl:text>location</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>economy</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;014&apos;">
        <xsl:text>oceans</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;006&apos;">
        <xsl:text>elevation</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;015&apos;">
        <xsl:text>planning, and cadastre</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;007&apos;">
        <xsl:text>environment</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;016&apos;">
        <xsl:text>society</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;008&apos;">
        <xsl:text>geo-scientific information</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;017&apos;">
        <xsl:text>structure</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;009&apos;">
        <xsl:text>health</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;018&apos;">
        <xsl:text>transportation</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;019&apos;">
        <xsl:text>utilities, and communication</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="ProgCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>on-going</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>planned</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;006&apos;">
        <xsl:text>required</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;007&apos;">
        <xsl:text>under development</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>completed</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>historical archive</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>obsolete</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="SpatRepTypCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>tin</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>stereo model</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;006&apos;">
        <xsl:text>video</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>vector</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>grid</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>text table</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="KeyTypCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>Temporal keywords</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>Theme keywords</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>Discipline keywords</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>Place keywords</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>Stratum keywords</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="EvalMethTypeCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>direct internal</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>direct external</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>indirect</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="MedNameCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>CD-ROM</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;010&apos;">
        <xsl:text>3580 cartridge</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>DVD</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;011&apos;">
        <xsl:text>4mm cartridge tape</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>DVD-ROM</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;012&apos;">
        <xsl:text>8mm cartridge tape</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>3.5 inch floppy disk</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;013&apos;">
        <xsl:text>0.25 inch cartridge tape</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>5.25 inch floppy disk</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;014&apos;">
        <xsl:text>digital linear tape</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;006&apos;">
        <xsl:text>7 track tape</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;015&apos;">
        <xsl:text>online link</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;007&apos;">
        <xsl:text>9 track tape</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;016&apos;">
        <xsl:text>satellite link</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;008&apos;">
        <xsl:text>3480 cartridge</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;017&apos;">
        <xsl:text>telephone link</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;009&apos;">
        <xsl:text>3490 cartridge</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;018&apos;">
        <xsl:text>hardcopy</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="MedFormCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>iso9660 (CD-ROM)</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>iso9660 Rock Ridge</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;006&apos;">
        <xsl:text>iso9660 Apple HFS</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>cpio</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>tar</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>high sierra file system</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="ObCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>mandatory</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>optional</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>conditional</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="DatatypeCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>class</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;010&apos;">
        <xsl:text>union class</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>codelist</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;011&apos;">
        <xsl:text>meta class</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>enumeration</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;012&apos;">
        <xsl:text>type class</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>codelist element</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;013&apos;">
        <xsl:text>character string</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>abstract class</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;014&apos;">
        <xsl:text>integer</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;006&apos;">
        <xsl:text>aggregate class</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;015&apos;">
        <xsl:text>association</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;007&apos;">
        <xsl:text>specified class</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;008&apos;">
        <xsl:text>datatype class</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;009&apos;">
        <xsl:text>interface class</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="PresFormCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>digital document</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;010&apos;">
        <xsl:text>hardcopy profile</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>hardcopy document</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;011&apos;">
        <xsl:text>digital table</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>digital image</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;012&apos;">
        <xsl:text>hardcopy table</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>hardcopy image</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;013&apos;">
        <xsl:text>digital video</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>digital map</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;014&apos;">
        <xsl:text>hardcopy video</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;006&apos;">
        <xsl:text>hardcopy map</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;007&apos;">
        <xsl:text>digital model</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;008&apos;">
        <xsl:text>hardcopy model</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;009&apos;">
        <xsl:text>digital profile</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="DateTypCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>creation</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>publication</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>revision</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="RoleCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>resource provider</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;010&apos;">
        <xsl:text>publisher</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>custodian</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>owner</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>user</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>distributor</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;006&apos;">
        <xsl:text>originator</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;007&apos;">
        <xsl:text>point of contact</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;008&apos;">
        <xsl:text>principal investigator</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;009&apos;">
        <xsl:text>processor</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="OnFunctCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>order</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>search</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>download</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>information</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>offline access</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="ContentTypCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>image</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>thematic classification</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>physical measurement</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="ImgCondCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>blurred image</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;010&apos;">
        <xsl:text>snow</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>cloud</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;011&apos;">
        <xsl:text>terrain masking</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>degrading obliquity</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>fog</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>heavy smoke or dust</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;006&apos;">
        <xsl:text>night</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;007&apos;">
        <xsl:text>rain</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;008&apos;">
        <xsl:text>semi-darkness</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;009&apos;">
        <xsl:text>shadow</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="CellGeoCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>point</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>area</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="DimNameTypCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>row (y-axis)</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>column (x-axis)</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>vertical (z-axis)</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>track (along direction of motion)</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>cross track (perpendicular to direction of motion)</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;006&apos;">
        <xsl:text>scal line of sensor</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;007&apos;">
        <xsl:text>sample (element along scan line)</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;008&apos;">
        <xsl:text>time duration</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="GeoObjTypCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>point</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>solid</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;006&apos;">
        <xsl:text>surface</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>complexes</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>composites</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>curve</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="PixOrientCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>upper right</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>upper left</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>center</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>lower left</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>lower right</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="TopoLevCd">
    <xsl:choose>
      <xsl:when test="@value = &apos;001&apos;">
        <xsl:text>geometry only</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;002&apos;">
        <xsl:text>topology 1D</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;003&apos;">
        <xsl:text>planar graph</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;004&apos;">
        <xsl:text>full planar graph</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;005&apos;">
        <xsl:text>surface graph</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;006&apos;">
        <xsl:text>full surface graph</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;007&apos;">
        <xsl:text>topology 3D</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;008&apos;">
        <xsl:text>full topology 3D</xsl:text>
      </xsl:when>
      <xsl:when test="@value = &apos;009&apos;">
        <xsl:text>abstract</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/metadata/Binary" />

  <!-- GOS Specific Domains -->

  <!-- Content Developer Types -->
  <xsl:template match="ContentDevType">
        <dt><em>Content Developer Type:</em></dt>

		<xsl:choose>
		<xsl:when test="text() = 1">
			<xsl:text>Central</xsl:text>
		</xsl:when>
		<xsl:when test="text() = 2">
			<xsl:text>State</xsl:text>
		</xsl:when>
		<xsl:when test="text() = 3">
			<xsl:text>District</xsl:text>
		</xsl:when>
		<xsl:when test="text() = 4">
			<xsl:text>Taluk</xsl:text>
		</xsl:when>
		<xsl:when test="text() = 5">
			<xsl:text>Panchayat</xsl:text>
		</xsl:when>
		<xsl:when test="text() = 6">
			<xsl:text>Municipality</xsl:text>
		</xsl:when>
		<xsl:when test="text() = 7">
			<xsl:text>Corporation</xsl:text>
		</xsl:when>
		<xsl:when test="text() = 8">
			<xsl:text>Organization</xsl:text>
		</xsl:when>
		<xsl:when test="text() = 9">
			<xsl:text>University</xsl:text>
		</xsl:when>
		<xsl:when test="text() = 10">
			<xsl:text>Commercial</xsl:text>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="text()"/>
		</xsl:otherwise>
		</xsl:choose>
  </xsl:template>

  <!-- Rating Levels -->
  <xsl:template match="certification">
        <dt><em>Rating:</em></dt>

		<xsl:choose>
		<xsl:when test="text() = 1">
			<xsl:text>Primary</xsl:text>
		</xsl:when>
		<xsl:when test="text() = 2">
			<xsl:text>Secondary</xsl:text>
		</xsl:when>
		<xsl:when test="text() = 3">
			<xsl:text>Tertiary</xsl:text>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="text()"/>
		</xsl:otherwise>
		</xsl:choose>
  </xsl:template>

  <!-- Publication Sources-->
  <xsl:template match="PubSourceCd">
        <dt><em>Input Source:</em></dt>

		<xsl:choose>
		<xsl:when test="text() = 1">
			<xsl:text>Geography Network</xsl:text>
		</xsl:when>
		<xsl:when test="text() = 2">
			<xsl:text>Geocommunicator</xsl:text>
		</xsl:when>
		<xsl:when test="text() = 3">
			<xsl:text>ArcCatalog</xsl:text>
		</xsl:when>
		<xsl:when test="text() = 4">
			<xsl:text>GeoData Online Form</xsl:text>
		</xsl:when>
		<xsl:when test="text() = 5">
			<xsl:text>GeoData XML Upload</xsl:text>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="text()"/>
		</xsl:otherwise>
		</xsl:choose>
  </xsl:template>


  <!-- Projected Cost-->
  <xsl:template match="projectedcost">
        <dt><em>Projected Cost:</em></dt>
        <xsl:if test="contains(text(),'$') = false()">$ </xsl:if>
        <xsl:value-of select="text()" />
  </xsl:template>

  <!-- Budgeted Cost-->
  <xsl:template match="budgetedcost">
        <dt><em>Projected Cost:</em></dt>
        <xsl:if test="contains(text(),'$') = false()">$ </xsl:if>
        <xsl:value-of select="text()" />
  </xsl:template>

  <!-- Partnered Cost-->
  <xsl:template match="partneredcost">
        <dt><em>Projected Cost:</em></dt>
        <xsl:if test="contains(text(),'$') = false()">$ </xsl:if>
        <xsl:value-of select="text()" />
  </xsl:template>

  <!-- Fee -->
  <xsl:template match="fee">
        <dt><em>Fee:</em></dt>
        <xsl:if test="contains(text(),'$') = false()">$ </xsl:if>
        <xsl:value-of select="text()" />
  </xsl:template>
</xsl:stylesheet>
