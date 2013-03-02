<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:georss="http://www.georss.org/georss" xml:base="http://geogratis.gc.ca/api/en/">
	<xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no"/>
	<xsl:template match="/">
		<xsl:choose>
			<xsl:when test="/gptJsonXml/error">
				<exception>
					<exceptionText>
						<xsl:for-each select="/gptJsonXml/error">
							<xsl:text> [ </xsl:text>
							<xsl:value-of select="./message"/>
							<xsl:text> : </xsl:text>
							<xsl:value-of select="./details"/>
							<xsl:text> : </xsl:text>
							<xsl:value-of select="./code"/>
							<xsl:text> ] </xsl:text>
						</xsl:for-each>
					</exceptionText>
				</exception>
			</xsl:when>
			<xsl:otherwise>
				<Records>
					<xsl:attribute name="maxRecords"><xsl:value-of select="count(//rss/channel/item)"/></xsl:attribute>
					<xsl:for-each select="//rss/channel/item">
						<Record>
							<ID><xsl:value-of select="guid"/></ID>
							<Title><xsl:value-of select="title"/></Title>
							<Abstract><xsl:value-of select="description"/></Abstract>
							<MinX>
								<xsl:value-of select="substring-before(substring-after(georss:polygon, ' '), ' ')"/>
							</MinX>
							<MinY>
								<xsl:value-of select="substring-before(georss:polygon, ' ')"/>
							</MinY>
							<MaxX>
								<xsl:value-of select="substring-before(substring-after(substring-after(substring-after(substring-after(substring-after(georss:polygon, ' '), ' '), ' '), ' '), ' '), ' ')"/>
							</MaxX>
							<MaxY>
								<xsl:value-of select="substring-before(substring-after(substring-after(georss:polygon, ' '), ' '), ' ')"/>
							</MaxY>
							<Type>downloadableData</Type>
							<ModifiedDate>
								<xsl:value-of select="pubDate"/>
							</ModifiedDate>
							<Links>
								<Link label="catalog.search.searchResult.viewDetails"><xsl:value-of select="link"/></Link>
								<Link gptLinkTag="metadata" show="false"/>
								<Link gptLinkTag="addToMap" show="false"/>
								<Link gptLinkTag="zoomTo" show="true"/>
								<Link gptLinkTag="open" show="false"/>
								<Link gptLinkTag="previewInfo" show="false"/>
							</Links>
						</Record>
					</xsl:for-each>
				</Records>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
