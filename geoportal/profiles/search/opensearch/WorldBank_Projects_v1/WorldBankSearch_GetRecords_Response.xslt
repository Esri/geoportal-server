<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:atom='http://www.w3.org/2005/Atom' 
xmlns:wb="http://search.worldbank.org/ns/1.0" exclude-result-prefixes="">
	<xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no"/>
	<xsl:template match="/">
		<Records>
			<xsl:for-each select="/atom:feed/atom:entry">
				<Record>
					<ID>
						<xsl:if test="string-length(./atom:id)>0">
							<xsl:value-of select="normalize-space(./atom:id)"/>
							<xsl:variable name="resourceId" select="normalize-space(./atom:id)"/>
						</xsl:if>					
					</ID>
					<Title>					
						<xsl:value-of select="normalize-space(./atom:title/text())"/> (<xsl:value-of select="normalize-space(wb:projects.lendinginstr)"/> to <xsl:value-of select="normalize-space(wb:projects.countryname)"/>)
					</Title>
					<Abstract>
					<xsl:choose>
						<xsl:when test="count(wb:projects.project_abstract)>0">
							<xsl:value-of select="normalize-space(wb:projects.project_abstract)"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="normalize-space(wb:projects.lendinginstr)"/> to <xsl:value-of select="normalize-space(wb:projects.countryname)"/>. Status of this project is <xsl:value-of select="normalize-space(wb:projects.status)"/>. The board approval date for this project is <xsl:value-of select="normalize-space(wb:projects.boardapprovaldate)"/>
						</xsl:otherwise>
					</xsl:choose>
						
					</Abstract>
					<Type>World Bank</Type>
					<MinX>-180</MinX>
					<MinY>-90</MinY>
					<MaxX>180</MaxX>
					<MaxY>90</MaxY>
					<ModifiedDate>
						<xsl:value-of select="./wb:projects.closingdate"/>
					</ModifiedDate>
					<References>
						<xsl:if test="count(./wb:projects.url)>0">
							<xsl:value-of select="./wb:projects.url"/>
							<xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server<xsl:text>&#x2715;</xsl:text>
						</xsl:if>
						
						<xsl:if test="count(/atom:feed/atom:logo)>0">
							<xsl:value-of select="/atom:feed/atom:logo"/>
							<xsl:text>&#x2714;</xsl:text>urn:x-esri:specification:ServiceType:ArcIMS:Metadata:ContentType<xsl:text>&#x2715;</xsl:text>
						</xsl:if>
						<xsl:value-of select="normalize-space(./link)"/>
					<xsl:for-each select="./wb:projects.projectdocs/wb:projects.projectdoc">	
						<xsl:value-of select="./wb:projects.EntityID/text()"/>
					</xsl:for-each>
					</References>
					<!--          <xsl:value-of select="normalize-space(./atom:link/@href)"/>              
              <xsl:value-of select="normalize-space(./media:group/media:thumbnail/@url)"/>-->
					<Types>
						<xsl:value-of select="normalize-space(./wb:projects.keywords/text())"/>
						<xsl:value-of select="normalize-space(./wb:projects.regionname/text())"/>
						<xsl:value-of select="normalize-space(./wb:projects.countryname/text())"/>
						<xsl:value-of select="normalize-space(./wb:projects.boardapprovaldate/text())"/>
						<xsl:value-of select="normalize-space(./wb:projects.impagency/text())"/>
						<xsl:value-of select="normalize-space(./wb:projects.prodlinetext/text())"/>
						<xsl:value-of select="normalize-space(./wb:projects.mjtheme1name/text())"/>
						<xsl:value-of select="normalize-space(./wb:projects.mjtheme2name/text())"/>
					</Types>
					<Links>					
						<Link gptLinkTag="open" show="true"/>
						<Link gptLinkTag="preview" show="false"/>
						<Link gptLinkTag="metadata" show="false"/>
						<Link gptLinkTag="addToMap" show="false"/>
						<Link gptLinkTag="zoomTo" show="false"/>
					</Links>
				</Record>
			</xsl:for-each>
		</Records>
	</xsl:template>
</xsl:stylesheet>
