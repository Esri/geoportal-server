<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no"/>
	<xsl:template match="/">
		<xsl:element name="csw:GetRecords" use-attribute-sets="GetRecordsAttributes" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:gml="http://www.opengis.net/gml" xmlns:gmd="http://www.isotc211.org/2005/gmd">
			<csw:Query typeNames="gmd:MD_Metadata">
				<csw:ElementSetName>full</csw:ElementSetName>
				<csw:Constraint version="1.1.0">
					<ogc:Filter xmlns="http://www.opengis.net/ogc">
						<ogc:And>
							<!-- Key Word search -->
							<xsl:apply-templates select="/GetRecords/KeyWord"/>
							<!-- LiveDataOrMaps search -->
							<xsl:apply-templates select="/GetRecords/LiveDataMap"/>
							<!-- Envelope search, e.g. ogc:BBOX -->
							<xsl:apply-templates select="/GetRecords/Envelope"/>
							<!-- Date Range Search -->
							<xsl:call-template name="tmpltDate"/>
						</ogc:And>
					</ogc:Filter>
				</csw:Constraint>
			</csw:Query>
		</xsl:element>
	</xsl:template>
	
	
	<!-- key word search -->
	<xsl:template match="/GetRecords/KeyWord" xmlns:ogc="http://www.opengis.net/ogc">
		<xsl:if test="normalize-space(.)!=''">
			<ogc:PropertyIsLike escapeChar="!" singleChar="#" wildCard="*">
				<ogc:PropertyName>apiso:AnyText</ogc:PropertyName>
				<ogc:Literal>
					<xsl:call-template name="output-keywords">
						<xsl:with-param name="list"><xsl:value-of select="." /></xsl:with-param>
					</xsl:call-template>
				</ogc:Literal>
			</ogc:PropertyIsLike>
		</xsl:if>
		<xsl:if test="normalize-space(.)=''">
            <ogc:PropertyIsLike escapeChar="!" singleChar="#" wildCard="*">
                <ogc:PropertyName>apiso:AnyText</ogc:PropertyName>
                <ogc:Literal>
                    <xsl:call-template name="output-keywords">
                        <xsl:with-param name="list">*</xsl:with-param>
                    </xsl:call-template>
                </ogc:Literal>
            </ogc:PropertyIsLike>
        </xsl:if>
	</xsl:template>
	
	
	<!-- LiveDataOrMaps search -->
	<xsl:template match="/GetRecords/LiveDataMap" xmlns:ogc="http://www.opengis.net/ogc">
		<xsl:if test="translate(normalize-space(./text()),'true', 'TRUE') ='TRUE'">
			<ogc:PropertyIsEqualTo>
				<ogc:PropertyName>dc:type</ogc:PropertyName>
				<ogc:Literal>liveData</ogc:Literal>
			</ogc:PropertyIsEqualTo>
		</xsl:if>
	</xsl:template>
	<!-- envelope search -->
	<xsl:template match="/GetRecords/Envelope" xmlns:ogc="http://www.opengis.net/ogc">
		<!-- generate BBOX query if minx, miny, maxx, maxy are provided -->
		<xsl:if test="./MinX and ./MinY and ./MaxX and ./MaxY">
		     <xsl:choose>
        <xsl:when test="/GetRecords/RecordsFullyWithinEnvelope/text() = 'true'">
       
             <ogc:Within xmlns:gml="http://www.opengis.net/gml">
                <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>
                <gml:Envelope>
                    <gml:lowerCorner>
                        <xsl:value-of select="MinX"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="MinY"/>
                    </gml:lowerCorner>
                    <gml:upperCorner>
                        <xsl:value-of select="MaxX"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="MaxY"/>
                    </gml:upperCorner>
                </gml:Envelope>
            </ogc:Within>
         
        </xsl:when>
        <xsl:otherwise>
            <ogc:Intersects xmlns:gml="http://www.opengis.net/gml">
                <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>
                <gml:Envelope>
                    <gml:lowerCorner>
                        <xsl:value-of select="MinX"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="MinY"/>
                    </gml:lowerCorner>
                    <gml:upperCorner>
                        <xsl:value-of select="MaxX"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="MaxY"/>
                    </gml:upperCorner>
                </gml:Envelope>
            </ogc:Intersects>
        </xsl:otherwise>
      
      </xsl:choose>
			
		</xsl:if>
	</xsl:template>
	<!--    
	<ogc:PropertyIsLessThanOrEqualTo>
		<ogc:PropertyName>apiso:TempExtent_begin</ogc:PropertyName>
		<ogc:Literal>2011-01-30T23:00:00</ogc:Literal>
	</ogc:PropertyIsLessThanOrEqualTo>
	<ogc:PropertyIsGreaterThan>
		<ogc:PropertyName>apiso:TempExtent_end</ogc:PropertyName>
		<ogc:Literal>2008-01-10T23:00:00</ogc:Literal>
	</ogc:PropertyIsGreaterThan>
-->
	<xsl:template name="tmpltDate" xmlns:ogc="http://www.opengis.net/ogc">
		<!-- xsl:if test="string-length(normalize-space(/GetRecords/FromDate/text())) &gt; 0" -->
		<xsl:if test="contains(/GetRecords/KeyWord, 'from:') &gt; 0">
			<ogc:PropertyIsGreaterThanOrEqualTo>
				<ogc:PropertyName>apiso:TempExtent_begin</ogc:PropertyName>
				<ogc:Literal>
					<xsl:choose>
						<xsl:when test="contains(substring-after(/GetRecords/KeyWord, 'from:'),' ')">
							<xsl:value-of select="substring-before(substring-after(/GetRecords/KeyWord, 'from:'),' ')"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="substring-after(/GetRecords/KeyWord, 'from:')"/>
						</xsl:otherwise>
					</xsl:choose>
				</ogc:Literal>
			</ogc:PropertyIsGreaterThanOrEqualTo>
		</xsl:if>
		<xsl:if test="contains(/GetRecords/KeyWord, 'to:') &gt; 0">
			<ogc:PropertyIsLessThanOrEqualTo>
				<ogc:PropertyName>apiso:TempExtent_end</ogc:PropertyName>
				<ogc:Literal>
					<xsl:choose>
						<xsl:when test="contains(substring-after(/GetRecords/KeyWord, 'to:'),' ')">
							<xsl:value-of select="substring-before(substring-after(/GetRecords/KeyWord, 'to:'),' ')"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="substring-after(/GetRecords/KeyWord, 'to:')"/>
						</xsl:otherwise>
					</xsl:choose>
				</ogc:Literal>
			</ogc:PropertyIsLessThanOrEqualTo>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="string-replace-all">
		<xsl:param name="text"/>
		<xsl:param name="replace"/>
		<xsl:param name="by"/>
		<xsl:choose>
			<xsl:when test="contains($text, $replace)">
				<xsl:value-of select="substring-before($text,$replace)"/>
				<xsl:value-of select="$by"/>
				<xsl:call-template name="string-replace-all">
					<xsl:with-param name="text" select="substring-after($text,$replace)"/>
					<xsl:with-param name="replace" select="$replace"/>
					<xsl:with-param name="by" select="$by"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<xsl:template name="output-keywords">
		<xsl:param name="list" />
		<xsl:variable name="newlist" select="concat(normalize-space($list), ' ')" />
		<xsl:variable name="first" select="substring-before($newlist, ' ')" />
		<xsl:variable name="remaining" select="substring-after($newlist, ' ')" />
		<xsl:if test="not(contains($first,'from:') or contains($first,'to:'))">
		  <xsl:value-of select="$first" />
		  <xsl:text>+</xsl:text>
		</xsl:if>
		<xsl:if test="$remaining">
			<xsl:call-template name="output-keywords">
				<xsl:with-param name="list" select="$remaining" />
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	
	<xsl:attribute-set name="GetRecordsAttributes">
		<xsl:attribute name="version">2.0.2</xsl:attribute>
		<xsl:attribute name="service">CSW</xsl:attribute>
		<xsl:attribute name="outputSchema">http://www.isotc211.org/2005/gmd</xsl:attribute>
		<xsl:attribute name="resultType">results</xsl:attribute>
		<xsl:attribute name="startPosition"><xsl:value-of select="/GetRecords/StartPosition"/></xsl:attribute>
		<xsl:attribute name="maxRecords"><xsl:value-of select="/GetRecords/MaxRecords"/></xsl:attribute>
	</xsl:attribute-set>
</xsl:stylesheet>
