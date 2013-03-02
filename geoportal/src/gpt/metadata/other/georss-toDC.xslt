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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#" xmlns:eq="http://earthquake.usgs.gov/rss/1.0/" xmlns:ows="http://www.opengis.net/ows" xmlns:georss="http://www.georss.org/georss" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:gml="http://www.opengis.net/gml">
	<xsl:param name="sourceUrl"/>
	<xsl:param name="serviceType"/>
	<xsl:output omit-xml-declaration="no" method="xml" indent="yes" encoding="UTF-8"/>
	<xsl:template match="/">
		<xsl:call-template name="main"/>
	</xsl:template>
	<xsl:template name="main">
		<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/">
      <rdf:Description>
        <xsl:attribute namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#" name="about">
          <xsl:value-of select="$sourceUrl"/>
        </xsl:attribute>
				<xsl:if test="/rss/channel/title | //atom:feed//atom:title">
					<dc:title>
						<xsl:value-of select="/rss/channel/title | //atom:feed//atom:title"/>
					</dc:title>
				</xsl:if>
				<xsl:if test="/rss/channel/description | //atom:feed//atom:subtitle">
					<dc:description>
						<xsl:value-of select="/rss/channel/description | //atom:feed//atom:subtitle"/>
					</dc:description>
				</xsl:if>
				<xsl:if test="/rss/channel/pubDate | //atom:feed//atom:updated">
					<xsl:if test="//atom:feed//atom:updated">
						<dc:date xml:lang="en">
							<xsl:call-template name="AtomFormatDate">
								<xsl:with-param name="DateTime" select="//atom:feed//atom:updated"/>
							</xsl:call-template>
						</dc:date>
					</xsl:if>
					<xsl:if test="/rss/channel/pubDate ">
						<dc:date xml:lang="en">
							<xsl:call-template name="RSSFormatDate">
								<xsl:with-param name="DateTime" select="/rss/channel/pubDate"/>
							</xsl:call-template>
						</dc:date>
					</xsl:if>
				</xsl:if>
				<dc:format>
					<xsl:value-of select="application/rss+xml"/>
				</dc:format>
				<xsl:for-each select="/Language">
					<dc:language>
						<xsl:value-of select="text()"/>
					</dc:language>
				</xsl:for-each>
				<dc:identifier>
					<xsl:value-of select="$sourceUrl"/>
				</dc:identifier>
				<xsl:choose>
					<xsl:when test="string-length(normalize-space($serviceType))>0">
						<dc:type>
							<xsl:value-of select="$serviceType"/>
						</dc:type>
					</xsl:when>
				</xsl:choose>
				<dc:type>georss</dc:type>
				<xsl:for-each select="/rss/channel/dc:publisher | //atom:feed//atom:author//atom:name | /rss/channel/generator">
					<dc:creator>
						<xsl:value-of select="text()"/>
					</dc:creator>
				</xsl:for-each>
				<xsl:for-each select="/rss/channel/managingEditor">
					<dc:contributor>
						<xsl:value-of select="text()"/>
					</dc:contributor>
				</xsl:for-each>
				<dc:subject>georss</dc:subject>
				<dct:references>
					<xsl:value-of select="$sourceUrl"/>
				</dct:references>
				<!-- GeoRSS bounding box -->
				<xsl:choose>
					<xsl:when test="//item//geo:lat">
						<xsl:call-template name="getBBox"/>
					<!--		<xsl:with-param name="item" select="//item"/>
						</xsl:call-template>-->
					</xsl:when>
					<xsl:when test="//georss:point | //georss:line | //georss:polygon | //georss:box | //gml:Point  | //gml:posList | //gml:LinearRing">
						<xsl:call-template name="makeBBox">
					</xsl:call-template>
					</xsl:when>
				</xsl:choose>
				<!--	<xsl:if test="//atom:entry//geo:lat">
					<xsl:call-template name="getAtomBBox">
						<xsl:with-param name="item" select="//atom:entry"/>
					</xsl:call-template>
				</xsl:if>-->
			</rdf:Description>
		</rdf:RDF>
	</xsl:template>
	<xsl:template name="makeBBox">
		<xsl:variable name="polygon">
			<xsl:for-each select="//georss:polygon">
				<xsl:value-of select="."/>
				<xsl:value-of select="' '"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="point">
			<xsl:for-each select="//georss:point">
				<xsl:value-of select="."/>
				<xsl:value-of select="' '"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="line">
			<xsl:for-each select="//georss:line">
				<xsl:value-of select="."/>
				<xsl:value-of select="' '"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="all">
			<xsl:value-of select="normalize-space($point)"/>
			<xsl:value-of select="' '"/>
			<xsl:value-of select="normalize-space($line)"/>
			<xsl:value-of select="' '"/>
			<xsl:value-of select="normalize-space($polygon)"/>
		</xsl:variable>
		<xsl:variable name="xys">
			<xsl:call-template name="allXY">
				<xsl:with-param name="minx" select="' '"/>
				<xsl:with-param name="miny" select="' '"/>
				<xsl:with-param name="maxx" select="' '"/>
				<xsl:with-param name="maxy" select="' '"/>
				<xsl:with-param name="first" select="1"/>
				<xsl:with-param name="val" select="normalize-space($all)"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="//georss:box">
				<xsl:call-template name="computeBboxFromBox">
					<xsl:with-param name="tempBox" select="$xys"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="//gml:Envelope">
				<xsl:call-template name="computeBboxFromEnvelope">
					<xsl:with-param name="tempBox" select="$xys"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<ows:WGS84BoundingBox>
					<ows:LowerCorner>
						<xsl:value-of select="substring-before(normalize-space($xys),',')"/>
					</ows:LowerCorner>
					<ows:UpperCorner>
						<xsl:value-of select="substring-after(normalize-space($xys),',')"/>
					</ows:UpperCorner>
				</ows:WGS84BoundingBox>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="computeBboxFromBox">
		<xsl:param name="tempBox"/>
		<xsl:variable name="minx">
			<xsl:value-of select="number(normalize-space(substring-before(normalize-space(substring-before(normalize-space($tempBox),',')),' ')))"/>
		</xsl:variable>
		<xsl:variable name="miny">
			<xsl:value-of select="number(normalize-space(substring-after(normalize-space(substring-before(normalize-space($tempBox),',')),' ')))"/>
		</xsl:variable>
		<xsl:variable name="maxx">
			<xsl:value-of select="number(normalize-space(substring-before(normalize-space(substring-after(normalize-space($tempBox),',')),' ')))"/>
		</xsl:variable>
		<xsl:variable name="maxy">
			<xsl:value-of select="number(normalize-space(substring-after(normalize-space(substring-after(normalize-space($tempBox),',')),' ')))"/>
		</xsl:variable>
		<xsl:variable name="eminx">
			<xsl:for-each select="//georss:box">
				<xsl:sort select="substring-before(normalize-space(substring-after(normalize-space(//georss:box),' ')),' ')" data-type="number" order="ascending"/>
				<xsl:if test="position() = 1">
					<xsl:value-of select="substring-before(normalize-space(substring-after(normalize-space(//georss:box),' ')),' ')"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="eminy">
			<xsl:for-each select="//georss:box">
				<xsl:sort select="substring-before(normalize-space(//georss:box),' ')" data-type="number" order="ascending"/>
				<xsl:if test="position() = 1">
					<xsl:value-of select="substring-before(normalize-space(//georss:box),' ')"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="emaxx">
			<xsl:for-each select="//georss:box">
				<xsl:sort select="substring-after(normalize-space(substring-after(normalize-space(substring-after(normalize-space(//georss:box),' ')),' ')),' ')" data-type="number" order="descending"/>
				<xsl:if test="position() = 1">
					<xsl:value-of select="substring-after(normalize-space(substring-after(normalize-space(substring-after(normalize-space(//georss:box),' ')),' ')),' ')"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="emaxy">
			<xsl:for-each select="//georss:box">
				<xsl:sort select="substring-before(normalize-space(substring-after(normalize-space(substring-after(normalize-space(//georss:box),' ')),' ')),' ')" data-type="number" order="descending"/>
				<xsl:if test="position() = 1">
					<xsl:value-of select="substring-before(normalize-space(substring-after(normalize-space(substring-after(normalize-space(//georss:box),' ')),' ')),' ')"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:call-template name="computeBbox">
			<xsl:with-param name="minx" select="$minx"/>
			<xsl:with-param name="miny" select="$miny"/>
			<xsl:with-param name="maxx" select="$maxx"/>
			<xsl:with-param name="maxy" select="$maxy"/>
			<xsl:with-param name="eminx" select="$eminx"/>
			<xsl:with-param name="eminy" select="$eminy"/>
			<xsl:with-param name="emaxx" select="$emaxx"/>
			<xsl:with-param name="emaxy" select="$emaxy"/>
		</xsl:call-template>
	</xsl:template>
	<xsl:template name="computeBboxFromEnvelope">
		<xsl:param name="tempBox"/>
		<xsl:variable name="minx">
			<xsl:value-of select="number(normalize-space(substring-before(normalize-space(substring-before(normalize-space($tempBox),',')),' ')))"/>
		</xsl:variable>
		<xsl:variable name="miny">
			<xsl:value-of select="number(normalize-space(substring-after(normalize-space(substring-before(normalize-space($tempBox),',')),' ')))"/>
		</xsl:variable>
		<xsl:variable name="maxx">
			<xsl:value-of select="number(normalize-space(substring-before(normalize-space(substring-after(normalize-space($tempBox),',')),' ')))"/>
		</xsl:variable>
		<xsl:variable name="maxy">
			<xsl:value-of select="number(normalize-space(substring-after(normalize-space(substring-after(normalize-space($tempBox),',')),' ')))"/>
		</xsl:variable>
		<xsl:variable name="eminx">
			<xsl:for-each select="//gml:Envelope">
				<xsl:sort select="substring-before(.//gml:LowerCorner,' ')" data-type="number" order="ascending"/>
				<xsl:if test="position() = 1">
					<xsl:value-of select="substring-before(.//gml:LowerCorner,' ')"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="eminy">
			<xsl:for-each select="//gml:Envelope">
				<xsl:sort select="substring-after(.//gml:LowerCorner,' ')" data-type="number" order="ascending"/>
				<xsl:if test="position() = 1">
					<xsl:value-of select="substring-after(.//gml:LowerCorner,' ')"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="emaxx">
			<xsl:for-each select="//gml:Envelope">
				<xsl:sort select="substring-before(.//gml:UpperCorner,' ')" data-type="number" order="descending"/>
				<xsl:if test="position() = 1">
					<xsl:value-of select="substring-before(.//gml:UpperCorner,' ')"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="emaxy">
			<xsl:for-each select="//gml:Envelope">
				<xsl:sort select="substring-after(.//gml:UpperCorner,' ')" data-type="number" order="descending"/>
				<xsl:if test="position() = 1">
					<xsl:value-of select="substring-after(.//gml:UpperCorner,' ')"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:call-template name="computeBbox">
			<xsl:with-param name="minx" select="$minx"/>
			<xsl:with-param name="miny" select="$miny"/>
			<xsl:with-param name="maxx" select="$maxx"/>
			<xsl:with-param name="maxy" select="$maxy"/>
			<xsl:with-param name="eminx" select="$eminx"/>
			<xsl:with-param name="eminy" select="$eminy"/>
			<xsl:with-param name="emaxx" select="$emaxx"/>
			<xsl:with-param name="emaxy" select="$emaxy"/>
		</xsl:call-template>
	</xsl:template>
	<xsl:template name="computeBbox">
		<xsl:param name="minx"/>
		<xsl:param name="miny"/>
		<xsl:param name="maxx"/>
		<xsl:param name="maxy"/>
		<xsl:param name="eminx"/>
		<xsl:param name="eminy"/>
		<xsl:param name="emaxx"/>
		<xsl:param name="emaxy"/>
		<ows:WGS84BoundingBox>
			<ows:LowerCorner>
				<xsl:call-template name="findMin">
					<xsl:with-param name="value1" select="$minx"/>
					<xsl:with-param name="value2" select="$eminx"/>
				</xsl:call-template>
				<xsl:value-of select="' '"/>
				<xsl:call-template name="findMin">
					<xsl:with-param name="value1" select="$miny"/>
					<xsl:with-param name="value2" select="$eminy"/>
				</xsl:call-template>
			</ows:LowerCorner>
			<ows:UpperCorner>
				<xsl:call-template name="findMax">
					<xsl:with-param name="value1" select="$maxx"/>
					<xsl:with-param name="value2" select="$emaxx"/>
				</xsl:call-template>
				<xsl:value-of select="' '"/>
				<xsl:call-template name="findMax">
					<xsl:with-param name="value1" select="$maxy"/>
					<xsl:with-param name="value2" select="$emaxy"/>
				</xsl:call-template>
			</ows:UpperCorner>
		</ows:WGS84BoundingBox>
	</xsl:template>
	<xsl:template name="findMax">
		<xsl:param name="value1"/>
		<xsl:param name="value2"/>
		<xsl:choose>
			<xsl:when test="normalize-space($value1)=''">
				<xsl:value-of select="$value2"/>
			</xsl:when>
			<xsl:when test="(number($value1) &gt; number($value2)) or (number($value1) = number($value2))">
				<xsl:value-of select="$value1"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$value2"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="findMin">
		<xsl:param name="value1"/>
		<xsl:param name="value2"/>
		<xsl:choose>
			<xsl:when test="normalize-space($value1)=''">
				<xsl:value-of select="$value2"/>
			</xsl:when>
			<xsl:when test="(number($value1) &lt; number($value2)) or (number($value1) = number($value2))">
				<xsl:value-of select="$value1"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$value2"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="allXY">
		<xsl:param name="minx"/>
		<xsl:param name="miny"/>
		<xsl:param name="maxx"/>
		<xsl:param name="maxy"/>
		<xsl:param name="first"/>
		<xsl:param name="val"/>
		<xsl:variable name="isNotLastValue" select="contains(normalize-space(substring-after(normalize-space($val),' ')),' ')"/>
		<xsl:variable name="currentY" select="number(normalize-space(substring-before(normalize-space($val),' ')))"/>
		<!--	<xsl:variable name="lastCurrentY" select="number(normalize-space(substring-before(normalize-space(substring-after(normalize-space($val),',')),',')))"/> -->
		<xsl:variable name="currentX" select="number(normalize-space(substring-before(normalize-space(substring-after(normalize-space($val),' ')),' ')))"/>
		<xsl:variable name="lastCurrentX" select="number(normalize-space(substring-after(normalize-space($val),' ')))"/>
		<xsl:variable name="tempMinY">
			<xsl:choose>
				<xsl:when test="$first = 1">
					<xsl:value-of select="$currentY"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="$miny &lt; $currentY">
						<xsl:value-of select="$miny"/>
					</xsl:if>
					<xsl:if test="$miny &gt; $currentY or $miny = $currentY">
						<xsl:value-of select="$currentY"/>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="tempMaxY">
			<xsl:choose>
				<xsl:when test="$first = 1">
					<xsl:value-of select="$currentY"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="$maxy &lt; $currentY">
						<xsl:value-of select="$currentY"/>
					</xsl:if>
					<xsl:if test="$maxy &gt; $currentY or $maxy = $currentY">
						<xsl:value-of select="$maxy"/>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="tempMinX">
			<xsl:choose>
				<xsl:when test="$first = 1">
					<xsl:value-of select="$currentX"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="$isNotLastValue = 'true'">
							<xsl:if test="$minx &lt; $currentX">
								<xsl:value-of select="$minx"/>
							</xsl:if>
							<xsl:if test="$minx &gt; $currentX or $minx = $currentX">
								<xsl:value-of select="$currentX"/>
							</xsl:if>
						</xsl:when>
						<xsl:otherwise>
							<xsl:if test="$minx &lt; $lastCurrentX">
								<xsl:value-of select="$minx"/>
							</xsl:if>
							<xsl:if test="$minx &gt; $lastCurrentX or $minx = $lastCurrentX">
								<xsl:value-of select="$lastCurrentX"/>
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="tempMaxX">
			<xsl:choose>
				<xsl:when test="$first = 1">
					<xsl:value-of select="$currentX"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="$isNotLastValue = 'true'">
							<xsl:if test="$maxx &lt; $currentX">
								<xsl:value-of select="$currentX"/>
							</xsl:if>
							<xsl:if test="$maxx &gt; $currentX or $maxx = $currentX">
								<xsl:value-of select="$maxx"/>
							</xsl:if>
						</xsl:when>
						<xsl:otherwise>
							<xsl:if test="$maxx &lt; $lastCurrentX">
								<xsl:value-of select="$lastCurrentX"/>
							</xsl:if>
							<xsl:if test="$maxx &gt; $lastCurrentX or $maxx = $lastCurrentX">
								<xsl:value-of select="$maxx"/>
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="string-length(normalize-space($val)) = 0">
				<xsl:value-of select="$minx"/>
				<xsl:value-of select="' '"/>
				<xsl:value-of select="$miny"/>
				<xsl:value-of select="','"/>
				<xsl:value-of select="$maxx"/>
				<xsl:value-of select="' '"/>
				<xsl:value-of select="$maxy"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="allXY">
					<xsl:with-param name="minx" select="$tempMinX"/>
					<xsl:with-param name="miny" select="$tempMinY"/>
					<xsl:with-param name="maxy" select="$tempMaxY"/>
					<xsl:with-param name="maxx" select="$tempMaxX"/>
					<xsl:with-param name="first" select="2"/>
					<xsl:with-param name="val" select="normalize-space(substring-after(normalize-space(substring-after($val,' ')),' '))"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="getXandY">
		<xsl:param name="coords"/>
		<xsl:param name="counter"/>
		<xVal>
			<xsl:value-of select="substring-before($coords,' ')"/>
		</xVal>
		<yVal>
			<xsl:value-of select="substring-before($coords,' ')"/>
		</yVal>
		<xsl:call-template name="getXandY">
			<xsl:with-param name="coords" select="substring-after($coords,' ')"/>
			<xsl:with-param name="counter" select="0"/>
		</xsl:call-template>
	</xsl:template>
	<xsl:template name="getAtomBBox">
		<xsl:param name="item"/>
		<georss:box>
			<xsl:call-template name="getMinY">
				<xsl:with-param name="item" select="$item"/>
			</xsl:call-template>
			<xsl:value-of select="' '"/>
			<xsl:call-template name="getMinX">
				<xsl:with-param name="item" select="$item"/>
			</xsl:call-template>
			<xsl:value-of select="' '"/>
			<xsl:call-template name="getMaxY">
				<xsl:with-param name="item" select="$item"/>
			</xsl:call-template>
			<xsl:value-of select="' '"/>
			<xsl:call-template name="getMaxX">
				<xsl:with-param name="item" select="$item"/>
			</xsl:call-template>
		</georss:box>
	</xsl:template>
	<xsl:template name="getMaxX">
		<xsl:param name="item"/>
		<xsl:for-each select="$item">
			<xsl:sort select="substring-before(./georss:point,' ')" data-type="number" order="descending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="substring-before(./georss:point,' ')"/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="getMinX">
		<xsl:param name="item"/>
		<xsl:for-each select="$item">
			<xsl:sort select="substring-before(./georss:point,' ')" data-type="number" order="ascending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="substring-before(./georss:point,' ')"/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="getMaxY">
		<xsl:param name="item"/>
		<xsl:for-each select="$item">
			<xsl:sort select="substring-after(.//georss:point,' ')" data-type="number" order="descending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="substring-after(.//georss:point,' ')"/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="getMinY">
		<xsl:param name="item"/>
		<xsl:for-each select="$item">
			<xsl:sort select="substring-after(.//georss:point,' ')" data-type="number" order="ascending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="substring-after(.//georss:point,' ')"/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="getBBox">
		<xsl:param name="item"/>
		<ows:WGS84BoundingBox>
			<ows:LowerCorner>
				<xsl:call-template name="getMinLong"/>
				<xsl:value-of select="' '"/>
				<xsl:call-template name="getMinLat"/>
			</ows:LowerCorner>
			<ows:UpperCorner>
				<xsl:call-template name="getMaxLong"/>
				<xsl:value-of select="' '"/>
				<xsl:call-template name="getMaxLat"/>
			</ows:UpperCorner>
		</ows:WGS84BoundingBox>
	</xsl:template>
	<xsl:template name="getMaxLat">
		<xsl:for-each select="//geo:lat">
			<xsl:sort select="." data-type="number" order="descending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="."/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="getMinLat">
		<xsl:for-each select="//geo:lat">
			<xsl:sort select="." data-type="number" order="ascending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="."/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="getMaxLong">
		<xsl:for-each select="//geo:long">
			<xsl:sort select="." data-type="number" order="descending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="."/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="getMinLong">
		<xsl:for-each select="//geo:long">
			<xsl:sort select="." data-type="number" order="ascending"/>
			<xsl:if test="position() = 1">
				<xsl:value-of select="."/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="RSSFormatDate">
		<xsl:param name="DateTime"/>
		<!-- new date format 2006-01-14 -->
		<xsl:variable name="date">
			<xsl:value-of select="substring(normalize-space(substring-after($DateTime,',')),1,11)"/>
		</xsl:variable>
		<xsl:variable name="strLength">
			<xsl:value-of select="string-length($date)"/>
		</xsl:variable>
		<xsl:variable name="year">
			<xsl:value-of select="substring($date,7,4)"/>
		</xsl:variable>
		<xsl:variable name="day">
			<xsl:value-of select="substring($date,1,2)"/>
		</xsl:variable>
		<xsl:variable name="mo">
			<xsl:value-of select="substring($date,4,3)"/>
		</xsl:variable>
		<xsl:value-of select="$year"/>
		<xsl:value-of select="'-'"/>
		<xsl:choose>
			<xsl:when test="$mo = 'Jan'">01</xsl:when>
			<xsl:when test="$mo = 'Feb'">02</xsl:when>
			<xsl:when test="$mo = 'Mar'">03</xsl:when>
			<xsl:when test="$mo = 'Apr'">04</xsl:when>
			<xsl:when test="$mo = 'May'">05</xsl:when>
			<xsl:when test="$mo = 'Jun'">06</xsl:when>
			<xsl:when test="$mo = 'Jul'">07</xsl:when>
			<xsl:when test="$mo = 'Aug'">08</xsl:when>
			<xsl:when test="$mo = 'Sep'">09</xsl:when>
			<xsl:when test="$mo = 'Oct'">10</xsl:when>
			<xsl:when test="$mo = 'Nov'">11</xsl:when>
			<xsl:when test="$mo = 'Dec'">12</xsl:when>
		</xsl:choose>
		<xsl:value-of select="'-'"/>
		<!--	<xsl:if test="(string-length($day) &lt; 2)">
			<xsl:value-of select="0"/>
		</xsl:if>-->
		<xsl:value-of select="$day"/>
	</xsl:template>
	<xsl:template name="AtomFormatDate">
		<xsl:param name="DateTime"/>
		<!-- new date format 2006-01-14 -->
		<xsl:variable name="date">
			<xsl:value-of select="substring($DateTime,1,10)"/>
		</xsl:variable>
		<xsl:value-of select="$date"/>
	</xsl:template>
</xsl:stylesheet>
