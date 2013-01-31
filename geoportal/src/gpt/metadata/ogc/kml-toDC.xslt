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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ows="http://www.opengis.net/ows" 
xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:fo="http://www.w3.org/1999/XSL/Format" 
xmlns:kml20="http://earth.google.com/kml/2.0" 
xmlns:kml21="http://earth.google.com/kml/2.1"
xmlns:kml22="http://www.opengis.net/kml/2.2" >
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
        <xsl:choose>
  				<xsl:when test="//kml20:Document/kml20:name | //kml21:Document/kml21:name | //kml22:Document/kml21:name | //kml22:Document/kml22:name | //kml20:NetworkLink/kml20:name | //kml21:NetworkLink/kml21:name | //kml22:NetworkLink/kml22:name">
  					<dc:title>
  						<xsl:value-of select="//kml20:Document/kml20:name | //kml21:Document/kml21:name | //kml22:Document/kml22:name | //kml20:NetworkLink/kml20:name | //kml21:NetworkLink/kml21:name | //kml22:NetworkLink/kml22:name"/>
  					</dc:title>
          </xsl:when>
          <xsl:otherwise>
            <dc:title>               
              <xsl:call-template name="baseFilename">
                <xsl:with-param name="string" select="$sourceUrl" />
              </xsl:call-template>
            </dc:title>
          </xsl:otherwise>
        </xsl:choose>
				<xsl:if test="//kml20:Document/kml20:description | //kml21:Document/kml21:description | //kml22:Document/kml22:description | //kml22:NetworkLink/kml22:description">
					<dc:description>
						<xsl:value-of select="//kml20:Document/kml20:description | //kml21:Document/kml21:description | //kml22:Document/kml22:description | //kml22:NetworkLink/kml22:description"/>
					</dc:description>
				</xsl:if>		
				<dc:format>
					<xsl:value-of select="'application/vnd.google-earth.kml+xml'"/>
				</dc:format>				        
				<!-- <dc:identifier>
					<xsl:value-of select="$sourceUrl"/>
				</dc:identifier>  -->
				<dc:type>
					<xsl:value-of select="'KML'"/>
				</dc:type>			
				<xsl:for-each select="//kml20:author//kml20:name | //kml21:author//kml21:name | //kml22:author//kml22:name">
					<dc:creator>
						<xsl:value-of select="text()"/>
					</dc:creator>
				</xsl:for-each>
				<dc:subject>KML</dc:subject>
				<xsl:for-each select="//kml20:key | //kml21:key | //kml22:key">
					<dc:subject>
						<xsl:value-of select="text()"/>
					</dc:subject>
				</xsl:for-each>
		   	    <xsl:for-each select="//kml20:name | //kml21:name | //kml22:name">
					<xsl:if test="position() &lt; 5">
						<dc:subject>
							<xsl:value-of select="text()"/>
						</dc:subject>		
					</xsl:if>
				</xsl:for-each>
				
			    <xsl:for-each select="//kml20:description | //kml21:description | //kml22:description">
					<xsl:if test="position() &lt; 5">
						<dc:subject>
							<xsl:value-of select="text()"/>
						</dc:subject>		
					</xsl:if>
				</xsl:for-each>
				
				<dct:references>
					<xsl:value-of select="$sourceUrl"/>
				</dct:references>
	<!--  		<xsl:for-each select="//kml20:Link//kml20:href | //kml21:Link//kml21:href">
					<dct:references>
						<xsl:value-of select="normalize-space(.)"/>
					</dct:references>
				</xsl:for-each>-->  
				<!-- KML bounding box -->
				<xsl:call-template name="makeBBox"/>
			</rdf:Description>
		</rdf:RDF>
	</xsl:template>
  
	<xsl:template name="makeBBox">
		<xsl:variable name="polygon">
			<xsl:for-each select="//kml20:coordinates | //kml21:coordinates | //kml22:coordinates">
				<xsl:value-of select="."/>
				<xsl:value-of select="' '"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="all">
			<xsl:value-of select="normalize-space($polygon)"/>
		</xsl:variable>
        
		<xsl:choose>
      <xsl:when test="string-length($all) &gt; 10000">
      </xsl:when>
	   <xsl:when test="//kml20:Envelope | //kml21:Envelope | //kml22:Envelope">
		</xsl:when>
		<xsl:otherwise>
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
				<ows:WGS84BoundingBox>
        <ows:LowerCorner>
          <xsl:choose>
            <xsl:when test="number(substring-after(substring-before(normalize-space($xys),','),' '))>0">
                <xsl:value-of select="substring-before(normalize-space($xys),',')"/>
            </xsl:when>
              <xsl:otherwise>
                 <xsl:value-of select="'-180 -90'"/>
              </xsl:otherwise>                    
          </xsl:choose> 
          </ows:LowerCorner>
          <ows:UpperCorner>
          <xsl:choose>
             <xsl:when test="number(substring-after(substring-after(normalize-space($xys),','),' '))>0">
                <xsl:value-of select="substring-after(normalize-space($xys),',')"/>
            </xsl:when>
              <xsl:otherwise>
                 <xsl:value-of select="'180 90'"/>
              </xsl:otherwise>
          </xsl:choose>         
          </ows:UpperCorner>
        </ows:WGS84BoundingBox>
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
		<xsl:variable name="currentY" select="number(normalize-space(substring-before(normalize-space(substring-after(normalize-space(substring-before(normalize-space($val),' ')),',')),',')))"/>
		<xsl:variable name="lastCurrentY" select="number(normalize-space(substring-before(normalize-space(substring-after(normalize-space($val),',')),',')))"/>
		<xsl:variable name="currentX" select="number(normalize-space(substring-before(normalize-space(substring-before(normalize-space($val),' ')),',')))"/>
		<xsl:variable name="lastCurrentX" select="number(normalize-space(substring-before(normalize-space($val),',')))"/>
		<xsl:variable name="tempMinY">
			<xsl:choose>
				<xsl:when test="$first = 1">
					<xsl:value-of select="$currentY"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="$isNotLastValue = 'true'">
							<xsl:if test="$miny &lt; $currentY">
								<xsl:value-of select="$miny"/>
							</xsl:if>
							<xsl:if test="$miny &gt; $currentY or 
								$miny = $currentY">
								<xsl:value-of select="$currentY"/>
							</xsl:if>
						</xsl:when>
						<xsl:otherwise>
							<xsl:if test="$miny &lt; $lastCurrentY">
								<xsl:value-of select="$miny"/>
							</xsl:if>
							<xsl:if test="$miny &gt; $lastCurrentY or 
									$miny = $lastCurrentY">
								<xsl:value-of select="$lastCurrentY"/>
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="tempMaxY">
			<xsl:choose>
				<xsl:when test="$first = 1">
					<xsl:value-of select="$currentY"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="$isNotLastValue = 'true'">
							<xsl:if test="$maxy &lt; $currentY">
								<xsl:value-of select="$currentY"/>
							</xsl:if>
							<xsl:if test="$maxy &gt; $currentY or 
							$maxy = $currentY">
								<xsl:value-of select="$maxy"/>
							</xsl:if>
						</xsl:when>
						<xsl:otherwise>
							<xsl:if test="$maxy &lt; $lastCurrentY">
								<xsl:value-of select="$lastCurrentY"/>
							</xsl:if>
							<xsl:if test="$maxy &gt; $lastCurrentY or 
							$maxy = $lastCurrentY">
								<xsl:value-of select="$maxy"/>
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
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
					<xsl:with-param name="val" select="normalize-space(substring-after($val,' '))"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
    
  <xsl:template name="baseFilename">
    <xsl:param name="string" />
    <xsl:choose>
      <xsl:when test="string-length($string) = 0">
        <xsl:value-of select="$string" />
      </xsl:when>
      <xsl:when test="contains($string, '?')">
        <xsl:call-template name="baseFilename">
          <xsl:with-param name="string" select="substring-before($string, '?')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="substring($string, string-length($string), 1) = '/'">
        <xsl:call-template name="baseFilename">
          <xsl:with-param name="string" select="substring-after($string, '/')" />
           <xsl:with-param name="string" select="substring($string, 1, (string-length($string) - 1))" /> 
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($string, '/')">
        <xsl:call-template name="baseFilename">
          <xsl:with-param name="string" select="substring-after($string, '/')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="$string" /></xsl:otherwise>
    </xsl:choose>
  </xsl:template>
      
</xsl:stylesheet>
