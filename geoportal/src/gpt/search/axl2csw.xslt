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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="all"/>
  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
  <xsl:template match="/">
    <xsl:element name="csw:GetRecords" use-attribute-sets="GetRecordsAttributes" xmlns:gml="http://www.opengis.net/gml" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:ogc="http://www.opengis.net/ogc" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2">
      <csw:Query typeNames="csw:Record">
        <xsl:if test="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/@fulloutput = 'false'">
          <csw:ElementSetName>brief</csw:ElementSetName>
        </xsl:if>
        <xsl:if test="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/@fulloutput = 'true'">
          <csw:ElementSetName>full</csw:ElementSetName>
        </xsl:if>
        <csw:Constraint version="1.0.0">
          <ogc:Filter>
            <ogc:And>
              <xsl:choose>
                <xsl:when test="string-length(normalize-space($all))>0">
                  <xsl:call-template name="all"/>
                </xsl:when>
                <xsl:when test="count(/ARCXML/REQUEST/GET_METADATA/GET_METADATA_DOCUMENT)>0">
                  <xsl:call-template name="id"/>
                </xsl:when>
                <xsl:otherwise>
                  <!-- Key Word search -->
                  <xsl:call-template name="keyword"/>
                  <!-- Envelope search, e.g. ogc:BBOX -->
                  <xsl:call-template name="envelope"/>
                  <!-- Date Range search -->
                  <xsl:call-template name="date"/>
                  <!-- xpath search -->
                  <xsl:if test="count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT | /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE)>0">
                    <!-- title -->
                    <xsl:call-template name="xpath">
                      <xsl:with-param name="param1" select="'metadata/idinfo/citation/citeinfo/title'"/>
                      <xsl:with-param name="param2" select="'metadata/dataIdInfo/idCitation/resTitle'"/>
                      <xsl:with-param name="param3" select="'dummy'"/>
                      <xsl:with-param name="param4" select="'dummy'"/>
                      <xsl:with-param name="propName" select="'dc:title'"/>
                    </xsl:call-template>
                    <!-- abstract / purpose -->
                    <xsl:call-template name="xpath">
                      <xsl:with-param name="param1" select="'metadata/idinfo/descript/abstract'"/>
                      <xsl:with-param name="param2" select="'metadata/dataIdInfo/idAbs'"/>
                      <xsl:with-param name="param3" select="'metadata/idinfo/descript/purpose'"/>
                      <xsl:with-param name="param4" select="'metadata/dataIdInfo/idPurp'"/>
                      <xsl:with-param name="propName" select="'dc:description'"/>
                    </xsl:call-template>
                    <!-- originator -->
                    <xsl:call-template name="xpath">
                      <xsl:with-param name="param1" select="'metadata/idinfo/citation/citeinfo/origin'"/>
                      <xsl:with-param name="param2" select="'metadata/dataIdInfo/idCitation/citRespParty/rpOrgName'"/>
                      <xsl:with-param name="param3" select="'metadata/dataIdInfo/idCitation/citRespParty/rpIndName'"/>
                      <xsl:with-param name="param4" select="'dummy'"/>
                      <xsl:with-param name="propName" select="'dc:creator'"/>
                    </xsl:call-template>
                    <!-- publisher -->
                    <xsl:call-template name="xpath">
                      <xsl:with-param name="param1" select="'metadata/idinfo/citation/citeinfo/pubinfo/publish'"/>
                      <xsl:with-param name="param2" select="'metadata/dataIdInfo/idCitation/citRespParty/rpOrgName'"/>
                      <xsl:with-param name="param3" select="'metadata/dataIdInfo/idCitation/citRespParty/rpIndName'"/>
                      <xsl:with-param name="param4" select="'dummy'"/>
                      <xsl:with-param name="propName" select="'dc:publisher'"/>
                    </xsl:call-template>
                    <!-- theme / purpose-->
                    <xsl:call-template name="xpath">
                      <xsl:with-param name="param1" select="'metadata/idinfo/keywords/theme/themekey'"/>
                      <xsl:with-param name="param2" select="'metadata/dataIdInfo/descKeys/keyword'"/>
                      <xsl:with-param name="param3" select="'metadata/idinfo/descript/purpose'"/>
                      <xsl:with-param name="param4" select="'metadata/dataIdInfo/idPurp'"/>
                      <xsl:with-param name="propName" select="'dc:subject,dc:type'"/>
                    </xsl:call-template>
                    <!--  single date -->
                    <!-- <xsl:call-template name="dateXpath1">
                      <xsl:with-param name="param1" select="'metadata/idinfo/timeperd/timeinfo/sngdate/caldate'"/>
                      <xsl:with-param name="param2" select="'metadata/dataqual/lineage/srcinfo/srctime/timeinfo/sngdate/caldate'"/>
                      <xsl:with-param name="param3" select="'metadata/dataIdInfo/dataExt/tempEle/TempExtent/exTemp/TM_GeometricPrimitive/TM_Instant/tmPosition/TM_CalDate/calDate'"/>
                      <xsl:with-param name="param4" select="'metadata/idinfo/citation/citeinfo/pubdate'"/>
                      <xsl:with-param name="param5" select="'metadata/dataIdInfo/idCitation/resRefDate/refDate'"/>
                    </xsl:call-template> -->
                    <!-- start and end date -->
                    <xsl:call-template name="dateXpath2">
                      <xsl:with-param name="param1" select="'metadata/idinfo/timeperd/timeinfo/rngdates/begdate'"/>
                      <xsl:with-param name="param2" select="'metadata/dataIdInfo/dataExt/tempEle/TempExtent/exTemp/TM_GeometricPrimitive/TM_Period/begin'"/>
                      <xsl:with-param name="param3" select="'metadata/idinfo/timeperd/timeinfo/rngdates/enddate'"/>
                      <xsl:with-param name="param4" select="'metadata/dataIdInfo/dataExt/tempEle/TempExtent/exTemp/TM_GeometricPrimitive/TM_Period/end'"/>
                    </xsl:call-template>
                    <!-- other date -->
                    <xsl:call-template name="dateXpath3">
                      <xsl:with-param name="param1" select="'metadata/metainfo/metd'"/>
                      <xsl:with-param name="param2" select="'metadata/mdDateSt'"/>
                      <xsl:with-param name="param3" select="'metadata/dataIdInfo/idCitation/resRefDate/refDate'"/>
                      <xsl:with-param name="param4" select="'metadata/idinfo/citation/citeinfo/pubdate'"/>
                      <xsl:with-param name="param5" select="'metadata/idinfo/timeperd/timeinfo/sngdate/caldate'"/>
                      <xsl:with-param name="param6" select="'metadata/dataqual/lineage/srcinfo/srctime/timeinfo/sngdate/caldate'"/>
                      <xsl:with-param name="param7" select="'metadata/dataIdInfo/dataExt/tempEle/TempExtent/exTemp/TM_GeometricPrimitive/TM_Instant/tmPosition/TM_CalDate/calDate'"/>
                    </xsl:call-template>
                    <!-- progress -->
                    <!-- dataFormat -->
                    <xsl:call-template name="xpath">
                      <xsl:with-param name="param1" select="'metadata/idinfo/citation/citeinfo/geoform'"/>
                      <xsl:with-param name="param2" select="'metadata/dataqual/lineage/srcinfo/srccite/citeinfo/geoform'"/>
                      <xsl:with-param name="param3" select="'dummy'"/>
                      <xsl:with-param name="param4" select="'dummy'"/>
                      <xsl:with-param name="propName" select="'dc:format'"/>
                    </xsl:call-template>
                    <!-- extent -->
                  </xsl:if>
                </xsl:otherwise>
              </xsl:choose>
            </ogc:And>
          </ogc:Filter>
        </csw:Constraint>
      </csw:Query>
    </xsl:element>
  </xsl:template>
  <xsl:template name="extent">
    <xsl:param name="westbc"/>
    <xsl:param name="westBL"/>
    <xsl:param name="eastbc"/>
    <xsl:param name="eastBL"/>
    <xsl:param name="northbc"/>
    <xsl:param name="northBL"/>
    <xsl:param name="southbc"/>
    <xsl:param name="southBL"/>
    <ogc:Contains xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
      <ogc:PropertyName xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">Geometry</ogc:PropertyName>
      <gml:Box srsName="EPSG:4326" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
        <gml:coordinates>
          <xsl:value-of select="$eastbc |$eastBL"/>,<xsl:value-of select="$westbc |$westBL"/>,<xsl:value-of select="$southbc | $southBL"/>,<xsl:value-of select="$northbc | $northBL"/>
        </gml:coordinates>
      </gml:Box>
    </ogc:Contains>
  </xsl:template>
  <xsl:template name="xpath">
    <xsl:param name="param1"/>
    <xsl:param name="param2"/>
    <xsl:param name="param3"/>
    <xsl:param name="param4"/>
    <xsl:param name="propName"/>
    <xsl:choose>
      <xsl:when test="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param1
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param1
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param2
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param2
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param3
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param3
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param4
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param4">
        <ogc:PropertyIsLike wildCard="*" escape="\" singleChar="%" xmlns:ogc="http://www.opengis.net/ogc">
          <ogc:PropertyName>
            <xsl:value-of select="$propName"/>
          </ogc:PropertyName>
          <ogc:Literal>
            <xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@word | /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@word"/>
          </ogc:Literal>
        </ogc:PropertyIsLike>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="dateXpath3">
    <xsl:param name="param1"/>
    <xsl:param name="param2"/>
    <xsl:param name="param3"/>
    <xsl:param name="param4"/>
    <xsl:param name="param5"/>
    <xsl:param name="param6"/>
    <xsl:param name="param7"/>
    <xsl:if test="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param1
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param1
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param2
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param2
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param3
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param3
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param4
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param4
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param5
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param5
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param6
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param6
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param7
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param7">
      <xsl:if test="count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@greaterthan)>0 or count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@greaterthan)>0
          or  count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@greaterthanorequalto)>0 or count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@equalto)>0">
        <ogc:PropertyIsGreaterThanOrEqualTo xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
          <ogc:PropertyName>Modified</ogc:PropertyName>
          <ogc:Literal>
            <xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@greaterthan | /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@greaterthan
          | /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@greaterthanorequalto | /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@equalto"/>
          </ogc:Literal>
        </ogc:PropertyIsGreaterThanOrEqualTo>
      </xsl:if>
      <xsl:if test="count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@lessthan)>0 or count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@lessthan)>0
          or count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@lessthanorequalto)>0 or count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@equalto)>0">
        <ogc:PropertyIsLessThanOrEqualTo xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
          <ogc:PropertyName>Modified</ogc:PropertyName>
          <ogc:Literal>
            <xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@lessthan | /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@lessthan
          | /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@lessthanorequalto | /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@equalto"/>
          </ogc:Literal>
        </ogc:PropertyIsLessThanOrEqualTo>
      </xsl:if>
    </xsl:if>
  </xsl:template>
  <xsl:template name="dateXpath2">
    <xsl:param name="param1"/>
    <xsl:param name="param2"/>
    <xsl:param name="param3"/>
    <xsl:param name="param4"/>
    <xsl:if test="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param1
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param1
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param2
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param2">
      <xsl:if test="count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@word)>0 or count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@word)>0
          or count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@greaterthanorequalto)>0 or count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@equalto)>0">
        <ogc:PropertyIsGreaterThanOrEqualTo xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
          <ogc:PropertyName>Modified</ogc:PropertyName>
          <ogc:Literal>
            <xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@word | /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@word
          | /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@greaterthanorequalto | /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@equalto"/>
          </ogc:Literal>
        </ogc:PropertyIsGreaterThanOrEqualTo>
      </xsl:if>
    </xsl:if>
    <xsl:if test="
    /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param3
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param3
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param4
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param4">
      <xsl:if test="
    count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@word)>0 or count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@word)>0
          or count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@lessthanorequalto)>0 or count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@equalto)>0">
        <ogc:PropertyIsLessThanOrEqualTo xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
          <ogc:PropertyName>Modified</ogc:PropertyName>
          <ogc:Literal>
            <xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@word | /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@word
          | /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@lessthanorequalto | /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@equalto"/>
          </ogc:Literal>
        </ogc:PropertyIsLessThanOrEqualTo>
      </xsl:if>
    </xsl:if>
  </xsl:template>
  <!--  TODO split single date -->
  <xsl:template name="dateXpath1">
    <xsl:param name="param1"/>
    <xsl:param name="param2"/>
    <xsl:param name="param3"/>
    <xsl:param name="param4"/>
    <xsl:param name="param5"/>
    <xsl:choose>
      <xsl:when test="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param1
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param1
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param2
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param2
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param3
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param3
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param4
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param4
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@tag=$param5
     or /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@tag=$param5">
        <ogc:PropertyIsLike wildCard="*" escape="\" singleChar="%" xmlns:ogc="http://www.opengis.net/ogc">
          <ogc:PropertyName>
            dc:date
          </ogc:PropertyName>
          <ogc:Literal>
            <xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGTEXT/@word | /ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/TAGVALUE/@word"/>
          </ogc:Literal>
        </ogc:PropertyIsLike>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <!--
  <FULLTEXT word="wms"/>
-->
  <xsl:template name="keyword">
    <xsl:if test="count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/FULLTEXT)>0">
      <ogc:PropertyIsLike wildCard="*" escape="\" singleChar="%" xmlns:ogc="http://www.opengis.net/ogc">
        <ogc:PropertyName>AnyText</ogc:PropertyName>
        <ogc:Literal>
          <xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/FULLTEXT/@word"/>
        </ogc:Literal>
      </ogc:PropertyIsLike>
    </xsl:if>
  </xsl:template>
  <xsl:template name="id">
    <xsl:if test="count(/ARCXML/REQUEST/GET_METADATA/GET_METADATA_DOCUMENT)>0">
      <ogc:PropertyIsLike wildCard="*" escape="\" singleChar="%" xmlns:ogc="http://www.opengis.net/ogc">
        <ogc:PropertyName>dc:identifier</ogc:PropertyName>
        <ogc:Literal>
          <xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/GET_METADATA_DOCUMENT/@docid"/>
        </ogc:Literal>
      </ogc:PropertyIsLike>
    </xsl:if>
  </xsl:template>
  <xsl:template name="all">
    <ogc:PropertyIsLessThanOrEqualTo xmlns:ogc="http://www.opengis.net/ogc">
      <ogc:PropertyName>Modified</ogc:PropertyName>
      <ogc:Literal>1500-01-01</ogc:Literal>
    </ogc:PropertyIsLessThanOrEqualTo>
  </xsl:template>
  <!--
  <ENVELOPE maxx="-120.0" maxy="46.0" minx="-130.0" miny="42.0" spatialoperator="overlaps"/>
-->
  <xsl:template name="envelope">
    <!-- generate BBOX query if minx, miny, maxx, maxy are provided -->
    <xsl:if test="count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/ENVELOPE)>0">
      <xsl:choose>
        <xsl:when test="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/ENVELOPE/@spatialoperator='overlaps'">
          <ogc:Overlaps xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
            <xsl:call-template name="bbox"/>
          </ogc:Overlaps>
        </xsl:when>
        <xsl:when test="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/ENVELOPE/@spatialoperator='overlaps2'">
          <ogc:Contains xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
            <xsl:call-template name="bbox"/>
          </ogc:Contains>
        </xsl:when>
        <xsl:when test="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/ENVELOPE/@spatialoperator='within'">
          <ogc:Within xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
            <xsl:call-template name="bbox"/>
          </ogc:Within>
        </xsl:when>
        <xsl:when test="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/ENVELOPE/@spatialoperator='fuzzywithin'">
          <ogc:Contains xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
            <xsl:call-template name="bbox"/>
          </ogc:Contains>
        </xsl:when>
        <xsl:when test="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/ENVELOPE/@spatialoperator='fuzzyequals'">
          <ogc:Equals xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
            <xsl:call-template name="bbox"/>
          </ogc:Equals>
        </xsl:when>
        <xsl:otherwise>
          <ogc:Contains xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
            <xsl:call-template name="bbox"/>
          </ogc:Contains>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>
  <!-- 
  <UPDATED  after="2005-01-14" before="2009-09-09">  
   -->
  <xsl:template name="date">
    <xsl:if test="count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/UPDATED)>0">
      <xsl:if test="count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/UPDATED/@after)>0">
        <ogc:PropertyIsGreaterThanOrEqualTo xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
          <ogc:PropertyName>Modified</ogc:PropertyName>
          <ogc:Literal>
            <xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/UPDATED/@after"/>
          </ogc:Literal>
        </ogc:PropertyIsGreaterThanOrEqualTo>
      </xsl:if>
      <xsl:if test="count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/UPDATED/@before)>0">
        <ogc:PropertyIsLessThanOrEqualTo xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
          <ogc:PropertyName>Modified</ogc:PropertyName>
          <ogc:Literal>
            <xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/SEARCH_METADATA/UPDATED/@before"/>
          </ogc:Literal>
        </ogc:PropertyIsLessThanOrEqualTo>
      </xsl:if>
    </xsl:if>
  </xsl:template>
  <xsl:template name="bbox">
    <ogc:PropertyName xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">Geometry</ogc:PropertyName>
    <gml:Box srsName="EPSG:4326" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
      <gml:coordinates>
        <xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/ENVELOPE/@maxx"/>,<xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/ENVELOPE/@minx"/>,<xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/ENVELOPE/@miny"/>,<xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/ENVELOPE/@maxy"/>
      </gml:coordinates>
    </gml:Box>
  </xsl:template>
  <xsl:attribute-set name="GetRecordsAttributes">
    <xsl:attribute name="version">2.0.2</xsl:attribute>
    <xsl:attribute name="service">CSW</xsl:attribute>
    <xsl:attribute name="resultType">RESULTS</xsl:attribute>
    <xsl:attribute name="startPosition"><xsl:choose><xsl:when test="count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/@startresult)>0"><xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/@startresult"/></xsl:when><xsl:otherwise>0</xsl:otherwise></xsl:choose></xsl:attribute>
    <xsl:attribute name="maxRecords"><xsl:if test="count(/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/@maxresults)>0"><xsl:value-of select="/ARCXML/REQUEST/GET_METADATA/SEARCH_METADATA/@maxresults"/></xsl:if></xsl:attribute>
    <xsl:attribute name="outputFormat">application/xml</xsl:attribute>
  </xsl:attribute-set>
</xsl:stylesheet>
