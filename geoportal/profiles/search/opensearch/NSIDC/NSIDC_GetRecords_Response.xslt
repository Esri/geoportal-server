<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:georss="http://www.georss.org/georss"
 xmlns:dct="http://purl.org/dc/terms/" xmlns:dc="http://purl.org/dc/elements/1.1/" 
 xmlns:atom="http://www.w3.org/2005/Atom" 
 xmlns:media="http://search.yahoo.com/mrss/" 
 xmlns:openSearch="http://a9.com/-/spec/opensearch/1.1/" 
 xmlns:os="http://a9.com/-/spec/opensearch/1.1/" 
 xmlns:gd="http://schemas.google.com/g/2005">
	<xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no"/>
	<xsl:template match="/">
		<Records>
			<xsl:attribute name="maxRecords"><xsl:value-of select="//os:totalResults"/></xsl:attribute>
			<xsl:for-each select="/atom:feed/atom:entry">
				<Record>
					<ID>
						<xsl:value-of select="substring-after(atom:id,'http://nsidc.org/api/opensearch/1.1/dataset/')"/>
					</ID>
					<Title>
						<xsl:choose>
							<xsl:when test=" ./atom:title/text() != 'null' ">
								<xsl:value-of select="normalize-space(./atom:title)"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="normalize-space(./media:group/media:title)"/>
							</xsl:otherwise>
						</xsl:choose>
					</Title>
					<Abstract>
						<xsl:value-of select="normalize-space(atom:summary)"/>
					</Abstract>
					<Type/>
					<MinX>
						<xsl:value-of select="substring-before(substring-after(georss:box,' '), ' ')"/>
					</MinX>
					<MinY>
						<xsl:value-of select="substring-before(georss:box,' ')"/>
					</MinY>
					<MaxX>
						<xsl:value-of select="substring-after(substring-after(substring-after(georss:box,' '),' '),' ')"/>
					</MaxX>
					<MaxY>
						<xsl:value-of select="substring-before(substring-after(substring-after(georss:box,' '),' '), ' ')"/>
					</MaxY>
					<ModifiedDate>
						<xsl:value-of select="./atom:updated"/>
					</ModifiedDate>
					<References>
						<xsl:value-of select="atom:link[@type='application/vnd.google-earth.kml+xml']/@href"/>
						<xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server<xsl:text>&#x2715;</xsl:text>
					</References>
					<Types>
						<xsl:value-of select="normalize-space(./media:group/media:keywords/text())"/>
						<!-- convert remote type to GPT type again -->
						<xsl:text>&#x2714;</xsl:text>Video<xsl:text>&#x2715;</xsl:text>
					</Types>
					<Links>
						<Link gptLinkTag="customLink" show="true"/>
						<Link gptLinkTag="previewInfo" show="false"/>
						<Link label="catalog.search.searchSite.nsidc.html">
								<xsl:value-of select="atom:link[@type='text/html']/@href"/>
						</Link>
						<Link label="catalog.search.searchSite.nsidc.granule">
								<xsl:value-of select="atom:link[@type='application/opensearchdescription+xml']/@href"/>
						</Link>						
					</Links>
				</Record>
			</xsl:for-each>
		</Records>
	</xsl:template>
</xsl:stylesheet>
