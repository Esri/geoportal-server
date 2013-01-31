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
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:preserve-space elements="*"/>
	<!-- ===================================================================== -->
	<!-- Input  Parameters
	-->
	<!-- PARAMETER 1
	-->
	<!-- The first parameter holds the current date and will be applied to the ModDate Esri element.
	       See the comment header for the format-date template for an in-depth description of
	       supported date and time formats.

	       Note: a time component can be added to this parameter by specifiying hh:mm:ss after
	       a literal 'T' appended to the date string.
	       e.g. 20050810T13:30:25, 20050812, 20050717T08:40:33pm

	       See the 'format-date' template within this XSLT file for more information
	    -->
	<xsl:param name="p-now" select="''"/>
	<!-- PARAMETER 2
	-->
	<!-- The second parameter holds the publication source code. It must be a numeric code
	       between 0 and 5. The codes and their meaning are:

	       0 = Harvestor, 1 = GN, 2 = GeoCom, 3 = ArcCatalog, 4 = ESRI GOS online form, 5 = ESRI  xml upload
	   -->
	<xsl:param name="p-pub-code" select="5"/>
	<!-- PARAMETER 3
	-->
	<!-- Our default resourceType value is '005' - "Document" or "Other Document". Allowable values
	      for the default are: 003 ("Offline data") or 005 ("Other Document")
	 -->
	<xsl:param name="p-res-type" select="''"/>
	<!-- PARAMETER 4
	-->
	<!-- A GUID can be provided to uniquely identify the document -->
	<!-- This GUID will be used if one is not already present in the Esri PublishedDocID element
	       or the Esri PublishedDocID element is missing
	   -->
	<xsl:param name="p-guid" select="''"/>
	<!-- PARAMETER 5
	   -->
	<!-- sourceuri is an additional identifier or "uri" that can be added as an attribute to the
	       PublishedDocId element. It is used by the Harvestor
	   -->
	<xsl:param name="p-sourceuri" select="''"/>
	<!--                                                                      -->
	<!-- Check the parameters are valid -->
	<!-- Check the 'now' date -->
	<xsl:variable name="mod-date">
		<xsl:call-template name="format-date">
			<xsl:with-param name="date" select="$p-now"/>
		</xsl:call-template>
	</xsl:variable>
	<!-- get any time component -->
	<xsl:variable name="mod-time" select="substring-after($mod-date,'T')"/>
	<!-- check the pub-code -->
	<xsl:variable name="def-pub-code">
		<xsl:choose>
			<xsl:when test="number($p-pub-code) &gt;= 0 and number($p-pub-code) &lt;= 5">
				<!-- the value is valid -->
				<xsl:value-of select="$p-pub-code"/>
			</xsl:when>
			<xsl:otherwise>
				<!-- Not valid - use default code -->
				<xsl:value-of select="'0'"/>
				<!-- "Harvestor" -->
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<!-- check the resource-type is valid -->
	<xsl:variable name="def-res-type">
		<xsl:choose>
			<xsl:when test="number($p-res-type) &gt; 0">
				<!-- the value is valid -->
				<xsl:value-of select="$p-res-type"/>
			</xsl:when>
			<xsl:otherwise>
				<!-- Not valid - use a pre-selected default code -->
				<xsl:value-of select="'005'"/>
				<!-- "Other Documents" -->
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<!-- pre-select all the urls within the metadata -->
	<xsl:variable name="all-links" select="/metadata/idinfo/citation/citeinfo/onlink[. != ''] |
					                           /metadata/dataqual/lineage/srcinfo/srccite/citeinfo/onlink[. != ''] |
					                           /metadata/idinfo/crossref/citeinfo/onlink[. != ''] |
					                           /metadata/metainfo/metextns/onlink[. != ''] |
					                           /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[. != ''] |
					                           /metadata/distInfo/distributor/distorTran/onLineSrc/linkage[. != ''] |
					                           /MD_Metadata/distributionInfo/MD_Distribution/transferOptions/MD_DigitalTransferOptions/onLine/CI_OnlineResource/linkage/URL[. !=''] |
					                           /MD_Metadata/contact/CI_ResponsibleParty/contactInfo/CI_Contact/onlineResource/CI_OnlineResource/linkage/URL[. != ''] | /MD_Metadata/dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/processStep/LI_ProcessStep/processor/CI_ResponsibleParty/contactInfo/CI_Contact/onlineResource/CI_OnlineResource/linkage/URL[.!=''] |
					                           /metadata/Esri/primaryOnlink[.!=''] "/>
<!-- main entry point -->
	<xsl:template match="/">
 		<xsl:for-each select="node() | @*">
			<xsl:copy>
				<!--  filter out "gos" - it's obsolete -->
				<xsl:apply-templates select="node()[not(self::gos)] | @*"/>
				<xsl:if test="count(/*/Esri) = 0">
					<!-- Esri element not found - add in the Esri element -->
					<Esri>
						<!-- add in the guid if one was provided -->
						<xsl:if test="$p-guid != ''">
							<xsl:choose>
								<xsl:when test="$p-sourceuri != ''">
									<!-- add in a source uri attribute -->
									<PublishedDocID sourceuri="{$p-sourceuri}">
										<xsl:call-template name="format-guid">
											<xsl:with-param name="guid" select="$p-guid"/>
										</xsl:call-template>
									</PublishedDocID>
								</xsl:when>
								<xsl:otherwise>
									<PublishedDocID>
										<xsl:call-template name="format-guid">
											<xsl:with-param name="guid" select="$p-guid"/>
										</xsl:call-template>
									</PublishedDocID>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:if>
						<!-- Add in PubSourceCd -->
						<PubSourceCd>
							<xsl:value-of select="$def-pub-code"/>
						</PubSourceCd>
						<!-- add in the Metadata Dates -->
						<ModDate>
							<xsl:call-template name="string-before">
								<xsl:with-param name="string" select="$mod-date"/>
								<xsl:with-param name="find-what" select="'T'"/>
							</xsl:call-template>
						</ModDate>
						<xsl:if test="$mod-time != ''">
							<ModTime>
								<xsl:value-of select="$mod-time"/>
							</ModTime>
						</xsl:if>
						<!-- use the same date for creation -->
						<CreaDate>
							<xsl:call-template name="string-before">
								<xsl:with-param name="string" select="$mod-date"/>
								<xsl:with-param name="find-what" select="'T'"/>
							</xsl:call-template>
						</CreaDate>
						<xsl:if test="$mod-time != ''">
							<CreaTime>
								<xsl:value-of select="$mod-time"/>
							</CreaTime>
						</xsl:if>
						<!-- check "issecured" -->
						<issecured>
							<xsl:value-of select="boolean(/metadata/idinfo/accconst[translate(.,'RESTRICTED','restricted') = 'restricted'] |
					                          /metadata/dataIdInfo/resConst/LegConsts/accessConsts/RestrictCd[@value = '007'] |
					                          /metadata/dataIdInfo/resConst/LegConsts/useConsts/RestrictCd[@value = '007'])"/>
						</issecured>
						<!-- Add resourceType. -->
						<xsl:call-template name="assign-resType"/>
					</Esri>
				</xsl:if>
			</xsl:copy>
		</xsl:for-each>
	</xsl:template>
	<!-- =====================================================
	       This template will be invoked by the XSLT parser if we *have* an existing Esri tag
	   -->
	<xsl:template match="Esri">
		<xsl:copy>
			<!-- Most of the Esri elements, we intend to "remake". The most important of these is resourceType.
 			       Note: 'xsl:copy-of' is a deep copy - all children are copied also...

 			       ModDate and ModTime we will update later.
 			       ContentDevType is obsolete
			   -->
			<xsl:copy-of select="./*[not(self::resourceType) and
				                                   not(self::resourceSubType) and
				                                   not(self::Server) and
				                                   not(self::Service) and
				                                   not(self::ServiceType) and
				                                   not(self::ServiceParam) and
				                                   not(self::primaryOnlink) and
				                                   not(self::ModDate) and
				                                   not(self::ModTime) and
				                                   not(self::PublishedDocID) and
				                                   not(self::PubSourceCd) and
				                                   not(self::ContentDevType) and
				                                   not(self::issecured) and
				                                   not(self::gos)]"/>
			<!-- Note: we do not have ModTime in the copy-of statement above. We will take care of ModTime when
		            we create ModDate -->
			<!-- Note also that resourceType, Server, Service, ServiceType and primaryOnlink are not
			       copied. They will be re-written or created, as necessary, when the resourceType of
			       the document is evaluated.

			<xsl:copy-of select="*[self::CreaDate or self::CreaTime or self::PubSourceCd
			                                   or self::MetaDocID or self::PublishedDocID]"/>
			 -->
			<!-- PublishedDocID needs some special handling...

			       If there is a PublishedDocID we do *NOT* change the guid regardless of whether
			       one was passed in or not.

			       In some cases, a sourceuri attribute is added to the PublishedDocID (in the case of OAI
			       or Z harvested documents). The process is...If there is a valid sourceuri attribute on the
			       PublishedDocID it is unchanged.

			       However, if a sourceuri is provided (in $p-sourceuri) then if there is either an empty sourceuri
			       attribute or no sourceuri attribute (on the PublishedDocID element) then a sourceuri attribute with
			       the value of $p-sourceuri is added to the PublishedDocID element
			  -->
			<xsl:choose>
				<!-- if there is no PublishedDocID and a guid was provided, we add one -->
				<xsl:when test="not(PublishedDocID)">
					<xsl:choose>
						<!-- was a sourceuri string also provided? -->
						<xsl:when test="$p-guid != '' and $p-sourceuri != ''">
							<!-- add in a source uri attribute -->
							<PublishedDocID sourceuri="{$p-sourceuri}">
								<xsl:call-template name="format-guid">
									<xsl:with-param name="guid" select="$p-guid"/>
								</xsl:call-template>
							</PublishedDocID>
						</xsl:when>
						<xsl:when test="$p-guid != ''">
							<PublishedDocID>
								<xsl:call-template name="format-guid">
									<xsl:with-param name="guid" select="$p-guid"/>
								</xsl:call-template>
							</PublishedDocID>
						</xsl:when>
					</xsl:choose>
				</xsl:when>
				<!-- There is a PublishedDocID
				    -->
				<xsl:otherwise>
					<!-- was a sourceuri provided? we can ignore the guid parameter...
			    		   -->
					<xsl:choose>
						<!-- there is not a valid sourceuri on PublishedDocID -->
						<xsl:when test="$p-sourceuri != '' and PublishedDocID[@sourceuri = '' or not(@sourceuri)]">
							<!-- write out PublishedDocID with a valid sourceuri attribute -->
							<PublishedDocID sourceuri="{$p-sourceuri}">
								<xsl:call-template name="format-guid">
									<xsl:with-param name="guid" select="PublishedDocID"/>
								</xsl:call-template>
							</PublishedDocID>
						</xsl:when>
						<!-- there is a sourceuri on PublishedDocID but it's null and none was provided -->
						<xsl:when test="PublishedDocID[@sourceuri = '']">
							<PublishedDocID>
								<!-- drop the sourceuri attribute -->
								<xsl:call-template name="format-guid">
									<xsl:with-param name="guid" select="PublishedDocID"/>
								</xsl:call-template>
							</PublishedDocID>
						</xsl:when>
						<xsl:otherwise>
							<!-- keep the original, sourceuri attribute and all -->
							<PublishedDocID>
								<xsl:if test="PublishedDocID[@sourceuri != '']">
									<xsl:attribute name="sourceuri"><xsl:value-of select="PublishedDocID/@sourceuri"/></xsl:attribute>
								</xsl:if>
								<xsl:call-template name="format-guid">
									<xsl:with-param name="guid" select="PublishedDocID"/>
								</xsl:call-template>
							</PublishedDocID>
							<!--<xsl:copy-of select="PublishedDocID"/>-->
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
			<!-- add in the PubSourceCd -->
			<xsl:choose>
				<xsl:when test="PubSourceCd[.!='']">
					<!-- we have one - keep it -->
					<xsl:copy-of select="PubSourceCd"/>
				</xsl:when>
				<xsl:when test="PubSourceCd[@value != '']">
					<!-- we have one with an attribute containing the code. Convert attribute to value -->
					<PubSourceCd>
						<xsl:value-of select="PubSourceCd/@value"/>
					</PubSourceCd>
				</xsl:when>
				<xsl:otherwise>
					<PubSourceCd>
						<xsl:value-of select="$def-pub-code"/>
					</PubSourceCd>
				</xsl:otherwise>
			</xsl:choose>
			<!-- add in the content dev type-->
			<ContentDevType>
			   <xsl:value-of select="/metadata/Esri/ContentDevType | /MD_Metadata/Esri/ContentDevType"/>
			</ContentDevType>
			<!-- update ModDate -->
			<ModDate>
				<xsl:call-template name="string-before">
					<xsl:with-param name="string" select="$mod-date"/>
					<xsl:with-param name="find-what" select="'T'"/>
				</xsl:call-template>
			</ModDate>
			<!-- If a time was provided, update ModTime -->
			<xsl:choose>
				<xsl:when test="$mod-time != ''">
					<ModTime>
						<xsl:value-of select="$mod-time"/>
					</ModTime>
				</xsl:when>
				<xsl:otherwise>
					<!-- copy over existing ModTime -->
					<xsl:copy-of select="ModTime"/>
				</xsl:otherwise>
			</xsl:choose>
			<!-- check "issecured" -->
			<issecured>
				<xsl:value-of select="boolean(/metadata/idinfo/accconst[translate(.,'RESTRICTED','restricted') = 'restricted'] |
					                          /metadata/dataIdInfo/resConst/LegConsts/accessConsts/RestrictCd[@value = '007'] |
					                          /metadata/dataIdInfo/resConst/LegConsts/useConsts/RestrictCd[@value = '007'] |
					                          issecured[translate(.,'TRUE','true') = 'true'])"/>
			</issecured>
			<!-- Now call the template to determine resourceType-->
			<xsl:call-template name="assign-resType"/>
		</xsl:copy>
	</xsl:template>
	<!-- ======================================================
		 This template cleans up all urls within the document.
	-->
	<xsl:template match="onlink | URL | networkr ">
		<xsl:copy>
			<xsl:call-template name="clean-onlink">
				<xsl:with-param name="onlink" select="."/>
			</xsl:call-template>
		</xsl:copy>
	</xsl:template>
	<!-- ====================================================== -->
	<!-- default copy -->
	<xsl:template match="node() | @*[local-name() != 'lang']">
		<xsl:copy>
			<!--  filter out "gos" - it's obsolete -->
			<xsl:apply-templates select="@* | node()[not(self::gos)]"/>
		</xsl:copy>
	</xsl:template>
	<!-- ======================================================
	       This template selects the resourceType based on the non-null value of, in order:

	       FGDC resdesc, ISO orDesc or ESRI resourceType

	       resdesc is a string literal while the other 2 elements contain numeric codes. If a resourceType cannot be
	       determined, the template is called recursively trying each (non-null) value (in turn).
	   -->
	<xsl:template name="get-resType">
		<xsl:param name="list-res-desc"/>
		<!-- list of values (resdesc, orDesc, resourceType) to be processed -->
		<!-- Convert the first value in the $res-desc nodeset to lower-case and remove all spaces
      	    -->
		<xsl:variable name="res-desc-lc" select="translate(normalize-space($list-res-desc[1]),
		   				               'ABCDEFGHIJKLMNOPQRSTUVWXYZ ','abcdefghijklmnopqrstuvwxyz')"/>
		<xsl:choose>
			<xsl:when test="$res-desc-lc != ''">
				<xsl:choose>
					<!-- A resdesc of Live Data and Maps and other variants thereof or an existing orDesc or
					      resourceType of 001 is translated into a resourceType value of '001'.
					   -->

					<xsl:when test="number($res-desc-lc) = 1 or starts-with($res-desc-lc, 'livedata') or starts-with($res-desc-lc, 'information')">
						<xsl:value-of select="'001'"/>
					</xsl:when>
					<!-- So on and so forth for the other resourceTypes. The string value of resdesc or numeric codes of
					       orDesc and resourceType are checked. When a match is found, the appropriate resourceType
					       value is assigned.
					   -->
					<xsl:when test="number($res-desc-lc) = 2 or starts-with($res-desc-lc, 'downloadabledata') or starts-with($res-desc-lc, 'download')">
						<xsl:value-of select="'002'"/>
					</xsl:when>
					<xsl:when test="number($res-desc-lc) = 3 or starts-with($res-desc-lc, 'offlinedata') or starts-with($res-desc-lc, 'offlineAccess')">
						<xsl:value-of select="'003'"/>
					</xsl:when>
					<xsl:when test="number($res-desc-lc) = 4 or starts-with($res-desc-lc, 'staticmapimage')">
						<xsl:value-of select="'004'"/>
					</xsl:when>
					<xsl:when test="number($res-desc-lc) = 5 or starts-with($res-desc-lc, 'otherdocument') or starts-with($res-desc-lc, 'document')">
						<xsl:value-of select="'005'"/>
					</xsl:when>
					<xsl:when test="number($res-desc-lc) = 6 or starts-with($res-desc-lc, 'application') or starts-with($res-desc-lc, 'search') ">
						<xsl:value-of select="'006'"/>
					</xsl:when>
					<xsl:when test="number($res-desc-lc) = 7 or starts-with($res-desc-lc, 'geographicservice') or starts-with($res-desc-lc, 'order')">
						<xsl:value-of select="'007'"/>
					</xsl:when>
					<xsl:when test="number($res-desc-lc) = 8 or starts-with($res-desc-lc, 'clearinghouse')">
						<xsl:value-of select="'008'"/>
					</xsl:when>
					<xsl:when test="number($res-desc-lc) = 9 or starts-with($res-desc-lc, 'mapfile')">
						<xsl:value-of select="'009'"/>
					</xsl:when>
					<!-- Our rule for resourceType '010' is pretty straightforward. It looks for the string 'geographic activities' (plus any variants)
					       or an existing value of "10" in res-desc-lc.
					 -->
					<xsl:when test="number($res-desc-lc) = 10 or starts-with($res-desc-lc, 'geographicactivit')">
						<xsl:value-of select="'010'"/>
					</xsl:when>
					<!-- Unrecognized value. Recurse and process the next value in list-res-desc (if there is one) -->
					<xsl:otherwise>
						<xsl:call-template name="get-resType">
							<xsl:with-param name="list-res-desc" select="$list-res-desc[position() != 1]"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
        <!-- look for a GoToWebsite URL -->
				<xsl:variable name="goto-web-site">
					<xsl:call-template name="find-first-web-page">
						<xsl:with-param name="list-onlink" select="$all-links"/>
						<xsl:with-param name="server" select="''"/>
						<xsl:with-param name="not-this-url" select="'http://www.esri.com'">
								</xsl:with-param>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="goto-web-site-lc" select="translate($goto-web-site,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/>
				<xsl:if test="$goto-web-site-lc != 'http://'">
					<primaryOnlink>
						<xsl:value-of select="$goto-web-site"/>
					</primaryOnlink>
				</xsl:if>
				<!-- no more values - assign the default resourceType value -->
				<xsl:value-of select="$def-res-type"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ====================================================== -->
	<!-- This template assigns the resourceType (via a call to "get-resType") and runs a series of
	       checks (currently for 001, 002, 004, default resourceType and 010) to determine if the resourceType
	       is valid ~and~ to derive additional element tag values (e.g. Server, Service, resourceSubType, etc.)

	       Ultimately, any document that fails validation checks for its assigned resourceType will be assigned
	       the default resourceType.
	  -->
	<xsl:template name="assign-resType">
		<xsl:variable name="v-res-type">
			<xsl:call-template name="get-resType">
				<!-- select FGDC resdesc, ISO19115 orDesc, Esri resourceType, ISO19139  CI_OnLineFunctionCode -->
				<xsl:with-param name="list-res-desc" select="/metadata/distinfo/resdesc[.!=''] |
                                                                            /metadata/distInfo/distributor/distorTran/onLineSrc/orDesc[.!=''] |
                                                                            /metadata/Esri/resourceType[.!='']|
                                                                            /MD_Metadata/Esri/resourceType[.!='']|
                                                                            /MD_Metadata/distributionInfo/MD_Distribution/transferOptions/MD_DigitalTransferOptions/onLine/CI_OnlineResource/function/CI_OnLineFunctionCode/@codeListValue[.!='']"/>
			</xsl:call-template>

		</xsl:variable>
		<!-- ========================================================= -->
		<!-- resourceType 001 -->
		<!-- Checks for resourceType 001 are the most extensive. Moreover, the template must also
		       determine the values for the Server, Service, ServiceType and primaryOnlink Esri elements.

		       The rules for determining if a document is a valid resourceType 001 are:
		      1) There are pre-existing Server, Service and ServiceType elements    -or-
		      2) There is a valid URL (to an ArcIMS or OGC map or feature service) from which the
		          Server, Service and ServiceType elements can be derived.

		          Valid url formats are documented below in the comments for the 'check-onlink-livedata' template

		      If the resourceType 001 can be verified, an additional check is initiated to determine the content
		      for the 'Go To Website' button on the gos2 Search Results page. This is encapsulated in the
		      'find-first-web-page' template. This code looks for the first ~valid~ url ('valid' covers a pretty broad spectrum!)
		      in the document and copies it to the Esri primaryOnlink element.

		      Finally, if a document cannot be verified as a resourceType 001, it is verified to see if it is a 002. If it fails that check,
		      it is assigned the default resourceType

		      Note: primaryOnlink is an invented element created by this template to store particular 'key' urls. In the
		      case of 001's, it stores the Go To Website url.

		      Note: if a URL was used to derive the Server, Service and ServiceType elements, it is ignored as a candidate for
		      the primaryOnlink element.
		-->
		<!-- resourceType 002 -->
		<!-- Checks attempt to determine if the document contains a url that references a 'downloadable' data set.
		      The definition of a 'downloadable resource' is defined in the comments for the 'check-onlink-download'
		      template.
		      If a valid 'downloadable resource' is found in one of the document urls, it is written to the primaryOnlink element.

		      If a valid 'downloadable resource' cannot be found, the resourceType of the document is changed to
		      the default resourceType
		  -->
		<!-- resourceType 004 -->
		<!-- resourceType 004 documents are "Static Map Images". Somewhere in
	            the document there has to be a url that points to a map image. The xslt cannot verify the ~content~ of the image
	            but it does look for a url that ~references~ an image. The definition of a valid "Map Image" url is defined in the
	            comments for the 'check-onlink-mapimage' template.
	        -->
		<!-- resourceType 010 -->
		<!-- resourceType 010 documents are 'Geographic Activities'. 'Geographic Activities' are divided into 3
		       categories: 'acquisitions', 'requests' or 'none of the above' (e.g. an activity that has been completed or
		       cancelled).
		       The Geographic Activity category is stored in the Esri resourceSubType element, or, in the case of the
		       'none of the above' category, it is represented by the ~absence~ of a resourceSubType element. The rules
		       for determining resourceSubType can be found in the comments for the 'add-res-subType' template.

		       Note: resourceType 010's are not changed to the default resourceType regardless of the resourceSubType
		       determination.
		   -->
		<!-- default resourceType -->
		<!-- Any document that is classified as a 'default' resourceType is evaluated to see if it can be 'upgraded' to
		        a resourceType 001 or a resourceType 002 based on url element content within the document.

		        If a resourceType of 001 or 002 cannot be assigned to the document, it retains its default resourceType
		        categorization. The checks for determining 001 or 002 for a default document are the same checks used
		        to verify a 001 or 002 resourceType assignment ('check-onlink-livedata' and check-onlink-download'
		        templates)

		        Note: a default resourceType can be initially assigned because the document is missing resdesc, orDesc and
		        resourceType or these elements contained invalid values.
		     -->
		<!-- Other resourceTypes -->
		<!-- There are no checks implemented for other resourceTypes. They retain there given resourceType.....unless...
		          it is the default resourceType in which case the above mentioned checks (for 'default resourceType') are run.
		      -->
		<xsl:choose>
			<!-- resourceType 001 -->
			<xsl:when test="number($v-res-type) = 1">
				<!-- check the Esri tags to see if they have been set. If so, we use them for Server
				       and ServiceType -->
				<xsl:variable name="server" select="/metadata/Esri/Server[.!='']|/MD_Metadata/Esri/Server[.!=''] "/>
				<xsl:variable name="server-lc" select="translate($server,'ABCDEFGHIJKLMNOPQRSTUVWXYZ ','abcdefghijklmnopqrstuvwxyz')"/>
				<!-- service name -->
				<xsl:variable name="service" select="/metadata/Esri/Service[.!='']|/MD_Metadata/Esri/Service[.!='']"/>
				<!-- service type -->
				<xsl:variable name="v-service-type" select="/metadata/Esri/ServiceType[.!='']|/MD_Metadata/Esri/ServiceType[.!='']"/>
				<xsl:variable name="service-type" select="translate($v-service-type,'ABCDEFGHIJKLMNOPQRSTUVWXYZ ','abcdefghijklmnopqrstuvwxyz')"/>
				<!-- If server and service-type are already set then we will use them. Otherwise, we
				       will attempt to derive this information from the onlink values
				   -->
				<xsl:choose>
					<!-- ArcIMS service -->
					<xsl:when test="$server != '' and $service != '' and ($service-type = 'image' or $service-type = 'feature' or $service-type = 'arcgis' or $service-type = 'wcs' or $service-type = 'wfs' or $service-type = 'wms')">
						<Server>
							<xsl:value-of select="$server"/>
						</Server>
						<Service>
							<xsl:value-of select="$service"/>
						</Service>
						<ServiceType>
							<xsl:value-of select="$service-type"/>
						</ServiceType>
						<resourceType>001</resourceType>
						<!-- look for a GoToWebsite URL -->
						<primaryOnlink>
							<xsl:call-template name="find-first-web-page">
								<xsl:with-param name="server" select="$server"/>
								<xsl:with-param name="not-this-url" select="concat($server,'/',$service-type,'/',$service)"/>
								<!--xsl:with-param name="not-this-url" select="$server"/-->
							</xsl:call-template>
						</primaryOnlink>
					</xsl:when>
					<!-- OGC-style services -->
					<xsl:when test="$server != '' and ($service-type = 'wms' or $service-type = 'wfs' or $service-type = 'wcs')">
						<Server>
							<xsl:call-template name="string-before">
								<xsl:with-param name="string" select="$server"/>
								<xsl:with-param name="find-what" select="'?'"/>
							</xsl:call-template>
							<!--<xsl:text>?SERVICE=</xsl:text><xsl:value-of select="$service-type"/>-->
						</Server>
						<ServiceType>
							<xsl:value-of select="$service-type"/>
						</ServiceType>
						<!-- Any kvp after the server 'bit'? -->
						<xsl:call-template name="write-service-param">
							<xsl:with-param name="server" select="$server"/>
						</xsl:call-template>
						<resourceType>001</resourceType>
						<!-- look for a GoToWebsite URL -->
						<primaryOnlink>
							<xsl:call-template name="find-first-web-page">
								<xsl:with-param name="server" select="$server"/>
								<xsl:with-param name="not-this-url">
									<xsl:call-template name="string-before">
										<xsl:with-param name="string" select="$server"/>
										<xsl:with-param name="find-what" select="'?'"/>
									</xsl:call-template>
								</xsl:with-param>
							</xsl:call-template>
						</primaryOnlink>
					</xsl:when>
					<!-- In this case, if the server value contains an ArcIMS OGC connector path (e.g. ..../com.esri.wms.Esrimap) -->
					<xsl:when test="contains($server-lc,'/com.esri.') and
					                         (contains($server-lc,'wms') or contains($server-lc,'wfs') or contains($server-lc,'wcs'))">
						<xsl:call-template name="write-esri-ogc-onlink">
							<xsl:with-param name="onlink" select="$server"/>
						</xsl:call-template>
					</xsl:when>
					<!-- In this case, the server path contains a (virtual) directory component that corresponds to an OGC service type
					       (e.g. http://webservices.ionicsoft.com/unData/wfs/UN) or the url contains a kvp of SERVICE=wfs|wms|wcs
					     -->
					<xsl:when test="(contains($server-lc,'/wfs/') or contains($server-lc,'/wms/') or contains($server-lc,'/wcs/'))
				                              or
				                              (contains($server-lc,'service=wfs') or contains($server-lc,'service=wms') or contains($server-lc,'service=wcs'))
				                              or contains($server-lc,'request=getmap')">
						<xsl:variable name="serv-type">
							<xsl:choose>
								<xsl:when test="contains($server-lc,'wfs')">
									<xsl:value-of select="'wfs'"/>
								</xsl:when>
								<xsl:when test="contains($server-lc,'wms') or contains($server-lc,'request=getmap')">
									<xsl:value-of select="'wms'"/>
								</xsl:when>
								<xsl:when test="contains($server-lc,'wcs')">
									<xsl:value-of select="'wcs'"/>
								</xsl:when>
							</xsl:choose>
						</xsl:variable>
						<xsl:variable name="server-final">
							<xsl:call-template name="string-before">
								<xsl:with-param name="string" select="$server"/>
								<xsl:with-param name="find-what" select="'?'"/>
							</xsl:call-template>
						</xsl:variable>
						<Server>
							<xsl:value-of select="$server-final"/>
							<!--<xsl:text>?SERVICE=</xsl:text><xsl:value-of select="$serv-type"/>-->
						</Server>
						<ServiceType>
							<xsl:value-of select="$serv-type"/>
						</ServiceType>
						<!-- Any kvp after the server 'bit'? -->
						<xsl:call-template name="write-service-param">
							<xsl:with-param name="server" select="$server"/>
						</xsl:call-template>
						<resourceType>001</resourceType>
						<!-- look for a GoToWebsite URL -->
						<primaryOnlink>
							<xsl:call-template name="find-first-web-page">
								<xsl:with-param name="server" select="$server-final"/>
								<xsl:with-param name="not-this-url" select="$server-final"/>
							</xsl:call-template>
						</primaryOnlink>
					</xsl:when>
					<xsl:otherwise>
						<!-- use the onlink information -->
						<xsl:call-template name="check-onlink-livedata"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<!-- resourceType 002 -->
			<xsl:when test="number($v-res-type) = 2">
				<xsl:call-template name="check-onlink-download"/>
			</xsl:when>
			<!-- resourceType 004 -->
			<xsl:when test="number($v-res-type) = 4">
				<xsl:call-template name="check-onlink-mapimage"/>
			</xsl:when>
			<!-- resourceType 010 -->
			<xsl:when test="number($v-res-type) = 10">
				<resourceType>
					<xsl:value-of select="$v-res-type"/>
				</resourceType>
				<xsl:call-template name="add-res-subType">
					<!-- To determine resourceSubType, we look at the content of the FGDC progress element or ISO ProgCd
				       or, if all else fails, use an existing Esri resourceSubType element (if there is one)
				-->
					<xsl:with-param name="res-subType" select="/metadata/idinfo/status/progress[.!=''] |
				                                                                         /metadata/dataIdInfo/idStatus/ProgCd[.!=''] |
				                                                                         /metadata/Esri/resourceSubType"/>
				</xsl:call-template>
			</xsl:when>
			<!-- default resourceType-->
			<xsl:when test="number($v-res-type) = number($def-res-type)">
				<!-- for default resourceType, check onlink to see if a resourceType can be determined
				      from that. check-onlink-livedata will, in turn, invoke check-onlink-download to see
				      if the document can be categorized as a 002 if the checks for 001 fail.
				   -->
				<xsl:call-template name="check-onlink-livedata"/>
			</xsl:when>
			<!-- All the others... -->
			<xsl:otherwise>
				<!-- write out the resource type -->
				<resourceType>
					<xsl:value-of select="$v-res-type"/>
				</resourceType>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ================================================================== -->
	<!-- Try and determine resourceType '001' from the document onlinks. The onlink URLs must follow a
	       very specific syntax:

	       For ArcIMS: an onlink must have the format:
	         http://....server-component.../image/...service name... - or -
	         http://....server-component.../feature/...service name..

	      For OGC: an onlink can contain:

	      A valid server URL that contains an ArcIMS OGC connector servlet path.
	      e.g. http://....server-component.../wmsconnector/com.esri.wms.Esrimap

	      - or -

	      The url contains a "request=getMap" kvp (in which case it is assumed to be 'wms')

	      - or -

	      The url contains a virtual path folder that is the name of an OGC service (e.g. .../wcs/, .../wfs/, etc.)

	      - or -

	      The url contains a kvp of "service=" embedded within it and is one of the following values:
	      wfs, wcs, wms.

	      For example: http://wsdali.spotimage.fr/ionicwcs/coverage/SV5_030407J?VERSION=1.0.20&SERVICE=WCS
	      (note that the search for service=wfs|wcs|wms is NOT case-sensitive)

	      Examples:
			BEFORE parse
			http://www.cadcorpdev.co.uk/wfs/SisISAPI.dll
			http://wsdali.spotimage.fr/ionicwcs/coverage/SV5_031122HMX?VERSION=1.0.20&SERVICE=WCS
			http://webservices.ionicsoft.com/unData/wfs/UN

			AFTER parse
			http://www.cadcorpdev.co.uk/wfs/SisISAPI.dll?SERVICE=wfs
			http://wsdali.spotimage.fr/ionicwcs/coverage/SV5_031122HMX?SERVICE=WCS
			http://webservices.ionicsoft.com/unData/wfs/UN?SERVICE=wfs

	      =====================================================================
	      A kvp style of syntax for the entire url is also supported. Key names are *NOT* case-sensitive. Supported
	      keys and their usage are:

	      	server=<server name> - use this key to store the name of the HTTP server and/or the
	      	                                        complete servlet URL for an OGC service

	      	service=<service-name> - store the name of the ArcIMS service. Not required for OGC
	      	servicename=<service-name> - same as 'service'

	      	servicetype=<image|feature> - store the type of ArcIMS service. Not required for OGC

	      The keys can occur in any order, though, typically the 'server=' key is placed first. Also, there must
	      be a semi-colon AFTER each key=value pair UNLESS it is the last key=value pair in the list.

	      Other separators - e.g. &, &amp; or whitespace characters are NOT supported. Such URLS will NOT be correctly parsed.

	      Examples:
	      Server=http://www.mysite.gov; service=abc_wms; ServiceType=image                 -or-
	      server=http://www.mysite.gov; serviceName=abc_wms; servicetype=feature         -or-
	      server=http://www.mysite.gov/some-dir/wmsconnector/com.esri.wms.Esrimap

    	      Note: For OGC servlet paths, the service or servicetype can be derived from the url.

	  If a valid onlink cannot be found, the template calls "check-onlink-download" to see if the resourceType
	  can be set to a '002'. If that fails, the check-onlink-download will set the resourceType to 'default'
	  (typically '005' - Other Document).

	  If a valid onlink (for '001') ~is~ found, the server, servicetype, etc. are derived from it and the first onlink that is a web
	  page (or similar) is tagged as the "Go To Website" url (which is stored in 'primaryOnlink' element).
	-->
	<xsl:template name="check-onlink-livedata">
		<xsl:param name="list-onlink" select="$all-links"/>
		<!-- we're looking for the first URL that matches our criteria
		   -->
		<xsl:choose>
			<xsl:when test="$list-onlink[1] != ''">
				<xsl:variable name="curr-onlink">
					<xsl:call-template name="clean-onlink">
						<xsl:with-param name="onlink" select="$list-onlink[1]"/>
					</xsl:call-template>
				</xsl:variable>
				<!--
				<xsl:variable name="curr-onlink">
					<xsl:call-template name="get-onlink-keyword">
						<xsl:with-param name="onlink" select="$list-onlink[1]"/>
						<xsl:with-param name="keyword" select="'server'"/>
					</xsl:call-template>
				</xsl:variable>
				-->
				<!-- get a lower case flavor to simplify the various string comparisons -->
				<xsl:variable name="curr-onlink-lc" select="translate($curr-onlink,'ABCDEFGHIJKLMNOPQRSTUVWXYZ ','abcdefghijklmnopqrstuvwxyz')"/>
				<xsl:choose>
					<!-- look for /image/ or /feature/ in the url for Arcims style -->
					<xsl:when test="contains($curr-onlink-lc,'/image/') or contains($curr-onlink-lc,'/feature/')">
						<xsl:variable name="serv-type">
							<xsl:choose>
								<xsl:when test="contains($curr-onlink-lc,'/image/')">
									<xsl:value-of select="'image'"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="'feature'"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<ServiceType>
							<xsl:value-of select="$serv-type"/>
						</ServiceType>
						<!-- server will be the portion before the '/image/. service will be the portion immediately after
						       but before any trailing '/'
						   -->
						<xsl:variable name="server">
							<xsl:call-template name="string-before">
								<xsl:with-param name="string">
									<xsl:call-template name="get-onlink-keyword">
										<xsl:with-param name="onlink" select="$curr-onlink"/>
										<xsl:with-param name="keyword" select="'server'"/>
									</xsl:call-template>
								</xsl:with-param>
								<xsl:with-param name="find-what" select="concat('/',$serv-type,'/')"/>
							</xsl:call-template>
						</xsl:variable>
						<Server>
							<xsl:value-of select="$server"/>
						</Server>
						<Service>
							<xsl:call-template name="string-between">
								<xsl:with-param name="buf" select="$curr-onlink"/>
								<xsl:with-param name="begin-param" select="concat('/',$serv-type,'/')"/>
								<xsl:with-param name="end-param" select="'/'"/>
							</xsl:call-template>
						</Service>
						<resourceType>001</resourceType>
						<!-- look for a GoToWebsite URL -->
						<primaryOnlink>
							<xsl:call-template name="find-first-web-page">
								<xsl:with-param name="server" select="$server"/>
								<xsl:with-param name="not-this-url">
									<xsl:call-template name="get-onlink-keyword">
										<xsl:with-param name="onlink" select="$curr-onlink"/>
										<xsl:with-param name="keyword" select="'server'"/>
									</xsl:call-template>
								</xsl:with-param>
							</xsl:call-template>
						</primaryOnlink>
					</xsl:when>
					<!-- OGC style - the link contains an ArcIMS servlet path -->
					<xsl:when test="contains($curr-onlink-lc,'/com.esri.') and
					                         (contains($curr-onlink-lc,'wms') or contains($curr-onlink-lc,'wfs') or contains($curr-onlink-lc,'wcs'))">
						<!-- this is a good one -->
						<xsl:call-template name="write-esri-ogc-onlink">
							<xsl:with-param name="onlink" select="$curr-onlink"/>
						</xsl:call-template>
					</xsl:when>
					<!-- OGC style - the link contains a virtual path component that corresponds to an OGC service type or
					       has an embedded kvp of SERVICE=wfs|wms|wcs -->
					<xsl:when test="(contains($curr-onlink-lc,'/wfs/') or contains($curr-onlink-lc,'/wms/') or contains($curr-onlink-lc,'/wcs/'))
					                          or
					                          (contains($curr-onlink-lc,'service=wfs') or contains($curr-onlink-lc,'service=wms')
					                          or contains($curr-onlink-lc,'service=wcs')) or contains($curr-onlink-lc,'request=getmap')">
						<xsl:variable name="serv-type">
							<xsl:choose>
								<xsl:when test="contains($curr-onlink-lc,'wfs')">
									<xsl:value-of select="'wfs'"/>
								</xsl:when>
								<xsl:when test="contains($curr-onlink-lc,'wms') or contains($curr-onlink-lc,'request=getmap')">
									<xsl:value-of select="'wms'"/>
								</xsl:when>
								<xsl:when test="contains($curr-onlink-lc,'wcs')">
									<xsl:value-of select="'wcs'"/>
								</xsl:when>
							</xsl:choose>
						</xsl:variable>
						<xsl:variable name="server">
							<xsl:call-template name="string-between">
								<xsl:with-param name="buf" select="$curr-onlink"/>
								<xsl:with-param name="begin-param" select="'server='"/>
								<xsl:with-param name="end-param" select="'?'"/>
							</xsl:call-template>
						</xsl:variable>
						<Server>
							<xsl:value-of select="$server"/>
							<!--<xsl:text>?SERVICE=</xsl:text><xsl:value-of select="$serv-type"/>-->
						</Server>
						<ServiceType>
							<xsl:value-of select="$serv-type"/>
						</ServiceType>
						<!-- Any kvp after the server 'bit'? -->
						<xsl:call-template name="write-service-param">
							<xsl:with-param name="server">
								<xsl:call-template name="get-onlink-keyword">
									<xsl:with-param name="onlink" select="$curr-onlink"/>
									<xsl:with-param name="keyword" select="'server2'"/>
								</xsl:call-template>
							</xsl:with-param>
						</xsl:call-template>
						<resourceType>001</resourceType>
						<!-- look for a GoToWebsite URL -->
						<primaryOnlink>
							<xsl:call-template name="find-first-web-page">
								<xsl:with-param name="server" select="$server"/>
								<xsl:with-param name="not-this-url" select="$server"/>
							</xsl:call-template>
						</primaryOnlink>
					</xsl:when>
					<xsl:otherwise>
						<xsl:choose>
							<!-- see if we have the server=,service=,servicetype= style of format -->
							<xsl:when test="contains($curr-onlink-lc,'server=') and
											     (contains($curr-onlink-lc,'service=') or contains($curr-onlink-lc,'servicename=')) and
											     contains($curr-onlink-lc,'servicetype=')">
								<!-- extract the individual components -->
								<Server>
									<xsl:call-template name="get-onlink-keyword">
										<xsl:with-param name="onlink" select="$curr-onlink"/>
										<xsl:with-param name="keyword" select="'server'"/>
										<!-- extract 'server=' portion -->
									</xsl:call-template>
								</Server>
								<Service>
									<xsl:call-template name="get-onlink-keyword">
										<xsl:with-param name="onlink" select="$curr-onlink"/>
										<xsl:with-param name="keyword" select="'service'"/>
									</xsl:call-template>
								</Service>
								<ServiceType>
									<xsl:call-template name="string-between">
										<xsl:with-param name="buf" select="$curr-onlink"/>
										<xsl:with-param name="begin-param" select="'servicetype='"/>
										<xsl:with-param name="end-param" select="';'"/>
									</xsl:call-template>
								</ServiceType>
								<!-- Any kvp after the server 'bit'? -->
								<xsl:call-template name="write-service-param">
									<xsl:with-param name="server">
										<xsl:call-template name="get-onlink-keyword">
											<xsl:with-param name="onlink" select="$curr-onlink"/>
											<xsl:with-param name="keyword" select="'server2'"/>
										</xsl:call-template>
									</xsl:with-param>
								</xsl:call-template>
								<resourceType>001</resourceType>
								<!-- look for a GoToWebsite URL -->
								<primaryOnlink>
									<xsl:call-template name="find-first-web-page">
										<xsl:with-param name="server" select="$curr-onlink"/>
										<xsl:with-param name="not-this-url" select="$curr-onlink"/>
									</xsl:call-template>
								</primaryOnlink>
							</xsl:when>
							<xsl:otherwise>
								<!-- no luck with any of the options, try the next onlink -->
								<xsl:call-template name="check-onlink-livedata">
									<xsl:with-param name="list-onlink" select="$list-onlink[position() != 1]"/>
								</xsl:call-template>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<!-- unable to determing resourceType from the onlink -->
				<!-- see if this document is a '002' - downloadable... -->
				<xsl:call-template name="check-onlink-download"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- =================================================
		Try and determine if the resourceType '002' is valid for this document.

	      This template checks the onlinks for a url that 'points' to an online ~downloadable~ data
	      source. 'Downloadable' is defined as a dataset in one of several, discreet, file formats:
	      zip, tar, rar, ArcINFO export, etc. The specific list of suffixes is in the suffix-set variable below.
	   -->
	<xsl:template name="check-onlink-download">
		<xsl:param name="list-onlink" select="$all-links"/>
		<!-- List of suffixes -->
		<xsl:variable name="suffix-set" select="'.zip,.e00,.gz,.tgz,.dbf,.tar,.shp,.rar,.xls,.txt,.dwg,.dxf,.dgn'"/>
		<xsl:call-template name="check-onlink">
			<xsl:with-param name="list-onlink" select="$list-onlink"/>
			<xsl:with-param name="list-suffix" select="$suffix-set"/>
			<xsl:with-param name="res-type-to-assign" select="'002'"/>
		</xsl:call-template>
	</xsl:template>
	<!-- =================================================
		Try and determine if the resourceType '004' is valid for this document.

	      This template checks the onlinks for a url that 'points' to a map image. However, it cannot
	      actually verify that, if there 'is' an image in the respective url, it points to a static image of a "map".

	      The link must begin with "http:" or "ftp:" and have one of a number of image format (or, in some cases,
	      document format) suffixes.

	      See the suffix-set variable below for the specific suffixes supported.
	   -->
	<xsl:template name="check-onlink-mapimage">
		<xsl:param name="list-onlink" select="$all-links"/>
		<!-- List of suffixes -->
		<xsl:variable name="suffix-set" select="'.gif,.jpg,.jpeg,.bmp,.pdf,.pmf,.tif,.tiff,.cal,.pct,.pict,.eps,.mxd,.av,.mpg,.mpeg,
							                                                    .wmv,.img,.rm'"/>
		<xsl:call-template name="check-onlink">
			<xsl:with-param name="list-onlink" select="$list-onlink"/>
			<xsl:with-param name="list-suffix" select="$suffix-set"/>
			<xsl:with-param name="res-type-to-assign" select="'004'"/>
		</xsl:call-template>
	</xsl:template>
	<!--==================================================================-->
	<!-- Checks a set of URLS against the given set of suffixes. If it finds a match, it assigns the
	      provided resourceType and writes out the valid link to the primaryOnlink element.
	   -->
	<xsl:template name="check-onlink">
		<xsl:param name="list-onlink" select="$all-links"/>
		<xsl:param name="list-suffix"/>
		<xsl:param name="res-type-to-assign"/>
		<!-- Process the first link in the list of (remaining) links - check the protocol specifier and the suffix.
		      We're looking for the first URL that matches...
		   -->
		<xsl:choose>
			<xsl:when test="$list-onlink[1] != ''">
				<xsl:variable name="curr-onlink">
					<xsl:call-template name="get-onlink-keyword">
						<xsl:with-param name="onlink" select="$list-onlink[1]"/>
						<xsl:with-param name="keyword" select="'server2'"/>
					</xsl:call-template>
				</xsl:variable>
				<!-- get a lower case flavor to simplify the various string comparisons -->
				<xsl:variable name="curr-onlink-lc" select="translate($curr-onlink,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/>
				<xsl:variable name="url-is-good">
					<xsl:if test="(starts-with($curr-onlink-lc,'http:') or starts-with($curr-onlink-lc,'ftp:'))
						              and not(contains($curr-onlink-lc,'?'))">
						<!-- now check suffix -->
						<xsl:call-template name="ends-with">
							<xsl:with-param name="string-find" select="$curr-onlink-lc"/>
							<xsl:with-param name="string-set" select="$list-suffix"/>
						</xsl:call-template>
					</xsl:if>
				</xsl:variable>
				<xsl:choose>
					<!-- did we get a valid match? Note: a null or '' string will test false with string(). Within the test,
					       the conversion from string to boolean is implicit. i.e. string(....) is equivalent to boolean(string(....)) -->
					<xsl:when test="string($url-is-good)">
						<!--  -->
						<!-- we have a good url -->
						<resourceType>
							<xsl:value-of select="$res-type-to-assign"/>
						</resourceType>
						<primaryOnlink>
							<xsl:value-of select="$curr-onlink"/>
						</primaryOnlink>
					</xsl:when>
					<xsl:otherwise>
						<!-- recurse -->
						<xsl:call-template name="check-onlink">
							<xsl:with-param name="list-onlink" select="$list-onlink[position() != 1]"/>
							<xsl:with-param name="list-suffix" select="$list-suffix"/>
							<xsl:with-param name="res-type-to-assign" select="$res-type-to-assign"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
         <!-- look for a GoToWebsite URL -->
				<xsl:variable name="goto-web-site">
					<xsl:call-template name="find-first-web-page">
						<xsl:with-param name="list-onlink" select="$all-links"/>
						<xsl:with-param name="server" select="''"/>
						<xsl:with-param name="not-this-url" select="'http://www.esri.com'">
								</xsl:with-param>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="goto-web-site-lc" select="translate($goto-web-site,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/>
				<xsl:if test="$goto-web-site-lc != 'http://'">
					<primaryOnlink>
						<xsl:value-of select="$goto-web-site"/>
					</primaryOnlink>
				</xsl:if>
         <resourceType>
					<xsl:value-of select="$def-res-type"/>
				</resourceType>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ================================================================== -->
	<!-- This template derives resourceSubType when the resourceType is '010'. We are interested in either 'requests' or
	       'acquisitions' for 'Geographic Activities' (i.e. type 010's).

	       Currently, the parameter res-sub-Type will be populated with (in order) the non-null value from an existing
	       FGDC progress element, ISO ProgCd and/or Esri resourceSubType.
	   -->
	<xsl:template name="add-res-subType">
		<xsl:param name="res-subType"/>
		<!-- convert to lower-case-->
		<xsl:variable name="res-subType-lc" select="normalize-space(translate($res-subType,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'))"/>
		<xsl:choose>
			<!-- Note: ProgCd (ISO) denotes "the status of a dataset or progress of a review". A ProgCd of '005' denotes
			       that a "fixed date has been established upon or by which the datra will be created or updated". Hence the value
			       "5" for number($res-subType-lc) is checked below...

			      Other possible codes for ProgCd are 001, 002, 003, 004, 006, and 007. Consult the ISO 19115 specification for
			      more information on ProgCd.
			   -->
			<xsl:when test="$res-subType-lc = 'planned' or $res-subType-lc = 'acquisition' or number($res-subType-lc) = 5">
				<resourceSubType>acquisition</resourceSubType>
			</xsl:when>
			<xsl:when test="$res-subType-lc = 'request'">
				<resourceSubType>request</resourceSubType>
			</xsl:when>
			<xsl:otherwise>
				<!-- there is no resourceSubType -->
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!--==================================================================-->
	<xsl:template name="write-service-param">
		<xsl:param name="server"/>
		<!-- Any kvp after the server 'bit'? -->
		<xsl:choose>
			<!-- server kvp argument takes precedence over the <ServiceParam> tag if
			       one is present in the document
			 -->
			<xsl:when test="contains($server,'?')">
				<ServiceParam>
					<xsl:call-template name="string-after">
						<xsl:with-param name="string" select="$server"/>
						<xsl:with-param name="find-what" select="'?'"/>
					</xsl:call-template>
				</ServiceParam>
			</xsl:when>
			<xsl:when test="/metadata/Esri/ServiceParam">
				<xsl:copy-of select="/metadata/Esri/ServiceParam"/>
			</xsl:when>
			<xsl:otherwise>

			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ================================================================== -->
	<!-- given a url that has an ArcIMS servlet path, and is assumed to be OGC, parse out
	       the server component and write out the various Esri elements (Server, ServiceType,
	       resourceType, etc.)
	    -->
	<xsl:template name="write-esri-ogc-onlink">
		<xsl:param name="onlink"/>
		<!-- Many times, the ogc url will have the servicename appended after the servlet path.
		   	  For example: http://server.com/wmsconnector/com.esri.wms.Esrimap/ServiceName/? or
		   	  similar. This code strips off the server bit and appends back on a valid servlet path (which is
		   	  easier than trying to parse out a servicename (if present)
		    -->
		<!-- 12/05/2005 - changed the code such that the servicename is no longer stripped from the
		           urls - to switch back, the server variable would need to be switched to the commented line
		           below, the servlet-path "re-included" in writing out the Server variable and the server param
		           switched back to the commented-out flavor in the call to 'find-first-web-page'
		       -->
		<!--<xsl:variable name="server" select="substring-before($onlink,'/com.esri.')"/>-->
		<xsl:variable name="server">
			<xsl:call-template name="string-before">
				<xsl:with-param name="string" select="$onlink"/>
				<xsl:with-param name="find-what" select="'?'"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="servlet-path">
			<xsl:value-of select="'/com.esri.'"/>
			<!-- servlet urls either end with a '/' followed by a path or other url component or they end
			       with a '?' -->
			<xsl:call-template name="string-before">
				<xsl:with-param name="string">
					<xsl:call-template name="string-between">
						<xsl:with-param name="buf" select="$onlink"/>
						<xsl:with-param name="begin-param" select="'/com.esri.'"/>
						<xsl:with-param name="end-param" select="'/'"/>
					</xsl:call-template>
				</xsl:with-param>
				<xsl:with-param name="find-what" select="'?'"/>
			</xsl:call-template>
		</xsl:variable>
		<!-- figure out the service type -->
		<xsl:variable name="service-type">
			<xsl:choose>
				<!-- com.esri.ogc.wms.WMSServlet, com.esri.wms.Esrimap, com.esri.wsit.WMSServlet -->
				<xsl:when test="contains($servlet-path,'wms') or contains($servlet-path,'WMS')">
					<xsl:value-of select="'wms'"/>
				</xsl:when>
				<!-- com.esri.ogc.wfs.WFSServlet, com.esri.wfs.Esrimap, com.esri.wsit.WFSServlet -->
				<xsl:when test="contains($servlet-path,'wfs') or contains($servlet-path,'WFS')">
					<xsl:value-of select="'wfs'"/>
				</xsl:when>
				<xsl:when test="contains($servlet-path,'wcs') or contains($servlet-path,'WCS')">
					<xsl:value-of select="'wcs'"/>
				</xsl:when>
				<xsl:otherwise>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- write out the appropriate element(s) -->
		<xsl:choose>
			<xsl:when test="$service-type != ''">
				<ServiceType>
					<xsl:value-of select="$service-type"/>
				</ServiceType>
				<Server>
					<xsl:value-of select="$server"/>
					<!--<xsl:value-of select="$servlet-path"/>-->
				</Server>
				<!-- Any kvp after the server 'bit'? -->
				<xsl:call-template name="write-service-param">
					<xsl:with-param name="server">
						<xsl:call-template name="get-onlink-keyword">
							<xsl:with-param name="onlink" select="$onlink"/>
							<xsl:with-param name="keyword" select="'server2'"/>
						</xsl:call-template>
					</xsl:with-param>
				</xsl:call-template>
				<resourceType>001</resourceType>
				<!-- find a (or "the") Go To Website URL -->
				<primaryOnlink>
					<xsl:call-template name="find-first-web-page">
						<!--<xsl:with-param name="server" select="$server"/>-->
						<xsl:with-param name="server" select="substring-before($server,'/com.esri.')"/>
						<xsl:with-param name="not-this-url" select="$onlink"/>
					</xsl:call-template>
				</primaryOnlink>
			</xsl:when>
			<xsl:otherwise>
				<!-- not a currently recognized type... -->
				<resourceType>
					<xsl:value-of select="$def-res-type"/>
				</resourceType>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ================================================================== -->
	<!-- Resource types 001 have a 'Go To Website' link available. This template will scan the
	       available links to find the first url that references a web site or web page.

	       NOTE: many online linkage values refer to the esri.com metadata profile or the esri.com home page. These
	      url values can be exclued as 'online linkages'...
	   -->
	<xsl:template name="find-first-web-page">
	 <xsl:param name="list-onlink" select="/metadata/idinfo/citation/citeinfo/onlink[. != ''] |
					                           /metadata/dataqual/lineage/srcinfo/srccite/citeinfo/onlink[. != ''] |
					                           /metadata/idinfo/crossref/citeinfo/onlink[. != ''] |
					                           /metadata/metainfo/metextns/onlink[. != ''] |
					                           /metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr[. != ''] |
					                           /metadata/distInfo/distributor/distorTran/onLineSrc/linkage[. != ''] |
					                           /MD_Metadata/distributionInfo/MD_Distribution/transferOptions/MD_DigitalTransferOptions/onLine/CI_OnlineResource/linkage/URL[. !=''] |
					                           /MD_Metadata/contact/CI_ResponsibleParty/contactInfo/CI_Contact/onlineResource/CI_OnlineResource/linkage/URL[. != ''] |
                                /MD_Metadata/dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/processStep/LI_ProcessStep/processor/CI_ResponsibleParty/contactInfo/CI_Contact/onlineResource/CI_OnlineResource/linkage/URL[.!=''] |
					                           /metadata/Esri/primaryOnlink[.!='']"/>
		<xsl:param name="server"/>
		<xsl:param name="not-this-url"/>
		<!-- we're looking for the first URL that matches our criteria
		   -->

  <xsl:for-each select="$list-onlink">
  </xsl:for-each>
  <!--url><xsl:value-of select="$list-onlink[1]"/></url>
  <serverparam><xsl:value-of select="$server"/></serverparam>
  <notthisurl><xsl:value-of select="$not-this-url"/></notthisurl-->
		<xsl:choose>
			<xsl:when test="$list-onlink[1] != ''">
				<xsl:variable name="curr-onlink">
					<xsl:call-template name="get-onlink-keyword">
						<xsl:with-param name="onlink" select="$list-onlink[1]"/>
						<xsl:with-param name="keyword" select="'server2'"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="curr-onlink-lc" select="translate($curr-onlink,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/>
				<xsl:variable name="not-this-url-lc" select="translate($not-this-url,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/>
				<xsl:choose>
					<!-- make sure that the current onlink is not the same as the
					       not-this-url and that the current onlink isn't an ~artificial~ one...(/image/ or /feature/) -->
					<xsl:when test="not(starts-with($curr-onlink-lc, $not-this-url-lc) or starts-with($curr-onlink-lc, 'http://www.esri.com'))
					                          and not(contains($curr-onlink-lc,'/image/') or contains($curr-onlink-lc,'/feature/'))">
						<xsl:variable name="suffix" select="substring($curr-onlink-lc,string-length($curr-onlink-lc)-4)"/>
						<xsl:choose>
							<!-- check the prefix and the url suffix. In this case, it's easier to check for what the url
							      should ~not~ be than to check for all the things it ~could~ be...
							   -->
							<xsl:when test="not(starts-with($curr-onlink-lc,'ftp:') or starts-with($curr-onlink-lc,'file:')
							                                or contains($curr-onlink-lc,'/com.esri.')
							                                or $suffix = '.zip' or $suffix = '.rar' or $suffix = '.tgz' or $suffix = '.e00'
											            or $suffix = '.tar' or $suffix = '.dxf' or substring($suffix,2) = '.gz')">
								<!-- url looks ok... -->
								<xsl:value-of select="$curr-onlink"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:call-template name="find-first-web-page">
									<xsl:with-param name="list-onlink" select="$list-onlink[position() != 1]"/>
									<xsl:with-param name="server" select="$server"/>
									<xsl:with-param name="not-this-url" select="$not-this-url"/>
								</xsl:call-template>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<!-- process the next url -->
						<xsl:call-template name="find-first-web-page">
							<xsl:with-param name="list-onlink" select="$list-onlink[position() != 1]"/>
							<xsl:with-param name="server" select="$server"/>
							<xsl:with-param name="not-this-url" select="$not-this-url"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<!-- we're out of urls... -->
				<!-- use the server address - strip out the bit between the protocol specifier and the first '/' (if
				       there is one...
				    -->
				<xsl:text>http://</xsl:text>
				<xsl:call-template name="string-between">
					<xsl:with-param name="buf" select="$server"/>
					<xsl:with-param name="begin-param" select="'://'"/>
					<xsl:with-param name="end-param" select="'/'"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ================================================================== -->
	<!-- URLs contain many different "oddities" regarding how they are formatted. It is quite common for
	      them to contain the actual URL 'embedded' within some keyword or similar. This template will 'extract'
	      the relevant URL component. The various different forms of URL identified within this template are
	      more a result of empirical observation than following 'documented' rules or guidelines
	   -->
	<xsl:template name="get-onlink-keyword">
		<xsl:param name="onlink"/>
		<xsl:param name="keyword"/>
		<xsl:variable name="onlink2">
			<xsl:call-template name="clean-onlink">
				<xsl:with-param name="onlink" select="$onlink"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$keyword='server' or $keyword='server2'">
				<!-- The url can contain server= followed by a ';', service=, servicename= or all of the above. The server path can
				       also contain a '?'.
				   -->
				<xsl:variable name="srv-tmp1">
					<xsl:choose>
						<xsl:when test="$keyword='server'">
							<!-- strip out the server bit from any url with a "get" keyword=value style url after a '?'-->
							<xsl:call-template name="string-between">
								<xsl:with-param name="buf" select="$onlink2"/>
								<xsl:with-param name="begin-param" select="'server='"/>
								<xsl:with-param name="end-param" select="'?'"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<!-- just get the server bit after a 'server=' keyword and INCLUDE any keyword=value pairs after
							       a '?' if one occurs
							   -->
							<xsl:call-template name="string-after">
								<xsl:with-param name="string" select="$onlink2"/>
								<xsl:with-param name="find-what" select="'server='"/>
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<!-- check that we got just a server path -->
				<!-- look for all possibilities - a trailing semi-colon, or a following service
				       or servicename= keyword
				   -->
				<xsl:variable name="onlink2-lc" select="translate($onlink2,'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
				<xsl:choose>
					<xsl:when test="(contains($onlink2-lc,';service=') or contains($onlink2-lc,';servicename='))
					                          and
					                          not(contains($onlink2-lc,'&amp;service=') or contains($onlink2-lc,'&amp;servicename='))">
						<xsl:call-template name="string-before">
							<!-- servicename is last -->
							<xsl:with-param name="string">
								<xsl:call-template name="string-before">
									<!-- search for service= -->
									<xsl:with-param name="string" select="translate($srv-tmp1,' ','')"/>
									<xsl:with-param name="find-what" select="';service='"/>
								</xsl:call-template>
							</xsl:with-param>
							<xsl:with-param name="find-what" select="';servicename='"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$srv-tmp1"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="$keyword='service'">
				<!-- The url can contain a service=, or servicename= . The service name may also end with a ';'.
				   -->
				<xsl:call-template name="string-between">
					<xsl:with-param name="buf">
						<xsl:call-template name="string-between">
							<xsl:with-param name="buf" select="$onlink2"/>
							<xsl:with-param name="begin-param" select="'service='"/>
							<xsl:with-param name="end-param" select="';'"/>
						</xsl:call-template>
					</xsl:with-param>
					<xsl:with-param name="begin-param" select="'servicename='"/>
					<xsl:with-param name="end-param" select="';'"/>
				</xsl:call-template>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	<!-- ================================================================== -->
	<!-- clean-onlink removes spurious characters at the beginning and end of a url. Commonly, these include < and > symbols as
	      well as their entiry-ref equivalents &lt; and &gt;. The prefix 'url:' (or 'URL:') is also quite common
	   -->
	<xsl:template name="clean-onlink">
		<xsl:param name="onlink"/>
		<!-- rip through the 'clean-up' routines -->
		<!-- First, remove '<' less than, '>' greater than signs and whitespace characters (tab, cr, lf) -->
		<xsl:variable name="buf1" select="translate(normalize-space($onlink),'&#60;&#62;&#09;&#10;&#13; ','')"/>
		<!-- remove any "&lt;","&gt;" entity-ref -->
		<xsl:variable name="buf2">
			<xsl:call-template name="string-between">
				<xsl:with-param name="buf" select="$buf1"/>
				<xsl:with-param name="begin-param" select="'&lt;'"/>
				<xsl:with-param name="end-param" select="'&gt;'"/>
			</xsl:call-template>
		</xsl:variable>
		<!-- remove any prefix -->
		<xsl:call-template name="string-after">
			<xsl:with-param name="string" select="$buf2"/>
			<xsl:with-param name="find-what" select="'url:'"/>
		</xsl:call-template>
	</xsl:template>
	<!-- =================================================================== -->
	<!-- Add checks for the guid format here. Currently, the template checks for wrapping curly brackets
	      only. If they are not found, the template adds them.
	  -->
	<xsl:template name="format-guid">
		<xsl:param name="guid"/>
		<!-- go ahead and process -->
		<xsl:choose>
			<xsl:when test="translate($guid,'1234567890ABCDEFabcdef-','') = '{}'">
				<!-- it has curlies... -->
				<xsl:value-of select="$guid"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="concat('{',$guid,'}')"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ================================================================== -->
	<!-- This template formats date strings in recognized formats to the 8 digit FGDC 'YYYYMMDD'. The 'recognized' formats
	       are based on a limited sub-set of the 8 digit format identified within the ISO 8601 standard. A complete discussion of
	       ISO 8601 can be found at http://www.cl.cam.ac.uk/~mgk25/iso-time.html.

	       Specifically, the 'recognized' formats are:

	       Year:
      		YYYY (eg 1997)
   		  Year and month:
      		YYYY-MM (eg 1997-07) or YYYY/MM or no separator, YYYYMM
            Complete 8 digit date:
      		YYYY-MM-DD (eg 1997-07-16) or YYYY/MM/DD or no separator YYYYMMDD (final, target format)

		These recognized input date formats can be converted to the YYYYMMDD format and are based on a
	      profile of the ISO 8601 identified in a Note to the W3C:

	       	http://www.w3.org/TR/NOTE-datetime

		 Note that all supported input formats have a 4 DIGIT YEAR component. This is in accordance with FGDC Directive 13,
		 (http://www.fgdc.gov/standards/directives/dir13.html) whereby the supported date formats are ASSUMED TO BE
		 Y2K COMPLIANT.

		 Processing rules:
		 1) Excluding separator characters, the first 4 digits are assumed to be the year, digits 5 and 6 the month,
		     and digits 7 and 8 the day (as per the 'recognized' formats).

		     Thus YY/MM/DD or MM/DD/YYYY will be interpreted as YYYYMM and YYYYMMDD respectively. No attempt is made
		     to check for 2 digit rather than 4 digit years or a year at the end of the input date.

		 2) A valid year is considered to be in the range 0 A.D. to 2100 A.D. (upper limit is arbitrary). Non-valid years are converted
		     to the year of script creation: '2005'

		 3) A valid month is assumed to be a value between 1 and 12. If the month is incorrect or missing, a month of '01' is
		    assumed (i.e. January).

		 4) A valid day is assumed to be a value between 1 and 31. Logic to determine the correct range of days based on the month
		     (e.g. 28, 30, 31)  and leap year logic for February are also implemented. If the day is considered incorrect or missing, a
		     value of '01' is assumed.

	     ++++++++++
	     Time:
	      A time designation is also supported. The time component will be translated to HHMMSS00 where HH are in the range of
	      0 to 23 and 00 is a 2 digit right "pad". The recognized formats are:

		  Complete date plus hours and minutes:
               YYYY-MM-DDThh:mm (eg 1997-07-16T19:20)
           Complete date plus hours, minutes and seconds:
      		YYYY-MM-DDThh:mm:ss (eg 1997-07-16T19:20:30)
      	 Complete date plus hours and minutes or hours, minutes and seconds with an am/pm designator
      	     YYYY-MM-DDThh:mmam,YYYY-MM-DDThh:mmpm, YYYY-MM-DDThh:mm:ssam,YYYY-MM-DDThh:mm:sspm
      	     (e.g. 1997-07-16T07:20:30pm which will be converted to 192030 plus 2 additional 00's for the pad - 19203000,
      	              2001-06-15T03:35:20am which will be converted to 03352000.)

      	 Special cases: 12 Noon (12pm) is "12". 12 midnight (12am) is 00. All other hours are represented in the range of 01 to 23.

      	 Note: 'T' is a literal that must appear in the string. If the 'T' literal does not appear in the string or the time format is
      	 invalid, the time component will be ignored. The Time Zone Designator within http://www.w3.org/TR/NOTE-datetime, if
      	 present, will be ignored. All times are assumed to be in the local time of the zone within which this template is being executed.
      	-->
	<xsl:template name="format-date">
		<xsl:param name="date"/>
		<!-- default year, month and day values for missing or invalid date components.
	   	       change as needed.
	   	   -->
		<xsl:variable name="def-year" select="'2005'"/>
		<xsl:variable name="def-month" select="'01'"/>
		<xsl:variable name="def-day" select="'01'"/>
		<!-- remove separators and get date component -->
		<xsl:variable name="date-comp">
			<xsl:call-template name="string-before">
				<xsl:with-param name="string" select="translate(normalize-space($date),'-/\: ','')"/>
				<xsl:with-param name="find-what" select="'T'"/>
			</xsl:call-template>
		</xsl:variable>
		<!-- The year is assumed to be 4 digits (in accordance with FGDC Directive 13). Thus
   		       date formats such as YYMMDD -or- MMDDYYYY will be ~misinterpreted~
   		   -->
		<xsl:variable name="year">
			<xsl:choose>
				<!-- does the date string have a year component? -->
				<xsl:when test="string-length($date-comp) &gt;= 4">
					<xsl:choose>
						<!-- arbitrary upper limit. lower limit is 0 A.D. -->
						<xsl:when test="number(substring($date-comp,1,4)) &gt;= 0 and
						                          number(substring($date-comp,1,4)) &lt;= 9999">
							<xsl:value-of select="substring($date-comp,1,4)"/>
						</xsl:when>
						<xsl:otherwise>
							<!-- bogus year. use default -->
							<xsl:value-of select="$def-year"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<!-- missing year. use default -->
					<xsl:value-of select="$def-year"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="month">
			<xsl:choose>
				<!-- does the date string have a month component? -->
				<xsl:when test="string-length($date-comp) &gt;= 6">
					<xsl:choose>
						<xsl:when test="number(substring($date-comp,5,2)) &gt;= 1 and
						                          number(substring($date-comp,5,2)) &lt;= 12">
							<xsl:value-of select="substring($date-comp,5,2)"/>
						</xsl:when>
						<xsl:otherwise>
							<!-- bogus month. use default -->
							<xsl:value-of select="$def-month"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<!-- missing month. use default -->
					<xsl:value-of select="$def-month"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="day">
			<xsl:choose>
				<!-- does the date string have a day component? -->
				<xsl:when test="string-length($date-comp) &gt;= 8">
					<xsl:choose>
						<!-- 'Day' checks first see if the day is within the range 1 - 31. If so, then the check determines
						       if the day is valid within the range for the specified month (in "$month") and also considers
						       leap years.
						   -->
						<xsl:when test="number(substring($date-comp,7,2)) &gt;= 1 and
						                          number(substring($date-comp,7,2)) &lt;= 31">
							<!-- The leading '1' in the string value of $ML is a placeholder. If '0' is used the number() function
						           will strip it out from the $ML variable BEFORE the substring() function is evaluated! This is
						           probably an XSLT bug for the XML Spy xslt parser - not sure if it exists in msxml, apache XALAN, etc.
						      -->
							<xsl:variable name="ML" select="1312831303130313130313031"/>
							<xsl:variable name="day-test" select="substring($date-comp,7,2)"/>
							<xsl:choose>
								<xsl:when test="$day-test &lt;= number(substring($ML, $month * 2, 2))">
									<!-- the day checks ok -->
									<xsl:value-of select="$day-test"/>
								</xsl:when>
								<!-- leap year -->
								<xsl:when test="$day-test = 29 and $year mod 4 = 0 and ($year mod 100 != 0 or $year mod 400 = 0)">
									<!-- the day checks ok -->
									<xsl:value-of select="$day-test"/>
								</xsl:when>
								<xsl:otherwise>
									<!-- provide default -->
									<xsl:value-of select="$def-day"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:otherwise>
							<!-- bogus day. use default -->
							<xsl:value-of select="$def-day"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<!-- missing day. use default -->
					<xsl:value-of select="$def-day"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- return the date -->
		<xsl:value-of select="concat($year,$month,$day)"/>
		<!-- time component -->
		<xsl:variable name="time-comp" select="substring-after(translate(normalize-space($date),': ',''),'T')"/>
		<!-- Allowable values are: hhmm, hhmmss -or- the same formats with an am/pm designator.
   		      Time is assumed to be 24hr in absence of a am/pm designator
   		   -->
		<xsl:if test="string-length($time-comp) = 4 or string-length($time-comp) = 6 or string-length($time-comp) = 8">
			<!-- determine if we're dealing with am/pm or 24 hr time -->
			<xsl:variable name="hour">
				<xsl:choose>
					<xsl:when test="translate(substring($time-comp,string-length($time-comp)-1,2),'PM','pm') = 'pm' and
	   				                          (number(substring($time-comp,1,2)) &gt;= 1 and
							                 number(substring($time-comp,1,2)) &lt;= 12)">
						<!-- 12 hour format with pm designation -->
						<xsl:choose>
							<!-- special case for midnight -->
							<xsl:when test="number(substring($time-comp,1,2)) = 12">
								<xsl:value-of select="'00'"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="number(substring($time-comp,1,2)) + 12"/>
								<!-- convert to 24 hour -->
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:when test="number(substring($time-comp,1,2)) &gt;= 0 and number(substring($time-comp,1,2)) &lt;= 23">
						<!-- either 12 hour "am" or 24 hour format -->
						<xsl:value-of select="substring($time-comp,1,2)"/>
					</xsl:when>
					<xsl:otherwise>
						<!-- this is not a valid time component for hours -->
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<!-- now figure out minutes if the hours were legitimate -->
			<xsl:variable name="minute">
				<xsl:if test="$hour != '' and (number(substring($time-comp,3,2)) &gt;= 0 and
							                             number(substring($time-comp,3,2)) &lt;= 59)">
					<xsl:value-of select="substring($time-comp,3,2)"/>
				</xsl:if>
			</xsl:variable>
			<!-- finally, seconds... -->
			<xsl:variable name="seconds">
				<xsl:if test="$hour != '' and $minute != '' and (number(substring($time-comp,5,2)) &gt;= 0 and
							                             number(substring($time-comp,5,2)) &lt;= 59)">
					<xsl:value-of select="substring($time-comp,5,2)"/>
				</xsl:if>
			</xsl:variable>
			<!-- return the time -->
			<xsl:if test="$hour != '' and $minute != ''">
				<xsl:value-of select="concat('T',$hour,$minute)"/>
				<!-- check to see if we return a 'seconds' component or if we just
   				       replace with 00's...
   				   -->
				<xsl:choose>
					<xsl:when test="$seconds != ''">
						<xsl:value-of select="concat($seconds,'00')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="'0000'"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	<!-- ==================================================================-->
	<!-- find if one string is "ends-with" one of the strings in the string-set. Does a case-insensitive match.
	       Assumes that individual strings in the set of strings are comma-separated -->
	<xsl:template name="ends-with">
		<xsl:param name="string-find"/>
		<xsl:param name="string-set"/>
		<!-- get the string against which we'll do our comparison -->
		<xsl:variable name="string-match">
			<xsl:choose>
				<xsl:when test="contains($string-set,',')">
					<xsl:value-of select="translate(normalize-space(substring-before($string-set,',')),
					                'ABCDEFGHIJKLMNOPQRSTUVWXYZ&#60;&#62;&#09;&#10;&#13;', 'abcdefghijklmnopqrstuvwxyz')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="translate(normalize-space($string-set),
										'ABCDEFGHIJKLMNOPQRSTUVWXYZ&#60;&#62;&#09;&#10;&#13;', 'abcdefghijklmnopqrstuvwxyz')"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$string-match != ''">
				<!-- extract the suffix from the "find" string. Ensure it has the same number of characters as
				       the suffix against which we are comparing...
				   -->
				<xsl:variable name="string-find-lc" select="translate(
				                                    substring($string-find,string-length($string-find)-string-length($string-match)+1),
										'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
				<xsl:choose>
					<!-- we have a match -->
					<xsl:when test="$string-find-lc = $string-match">
						<xsl:value-of select="true()"/>
					</xsl:when>
					<!-- no match, eliminate the value from the set and try the next ...-->
					<xsl:otherwise>
						<xsl:call-template name="ends-with">
							<xsl:with-param name="string-find" select="$string-find"/>
							<xsl:with-param name="string-set" select="substring-after($string-set,',')"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<!-- there are no more strings -->
				<xsl:value-of select="''"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ==================================================================-->
	<!-- find if one string is "in" the set of other strings. Does a case-insensitive match.
	       Assumes that individual strings in the set of strings are comma-separated -->
	<xsl:template name="string-in">
		<xsl:param name="string-find"/>
		<xsl:param name="string-set"/>
		<!-- lower case both the string and the current string against which we're comparing -->
		<xsl:variable name="string-find-lc" select="translate($string-find,'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
		<xsl:variable name="string-match">
			<xsl:choose>
				<xsl:when test="contains($string-set,',')">
					<xsl:value-of select="translate(normalize-space(substring-before($string-set,',')),
					                'ABCDEFGHIJKLMNOPQRSTUVWXYZ&#60;&#62;&#09;&#10;&#13;', 'abcdefghijklmnopqrstuvwxyz')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="translate(normalize-space($string-set),
										'ABCDEFGHIJKLMNOPQRSTUVWXYZ&#60;&#62;&#09;&#10;&#13;', 'abcdefghijklmnopqrstuvwxyz')"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$string-match != ''">
				<xsl:choose>
					<!-- we have a match -->
					<xsl:when test="$string-find-lc = $string-match">
						<xsl:value-of select="true()"/>
					</xsl:when>
					<!-- no match, eliminate the value from the set and try the next ...-->
					<xsl:otherwise>
						<xsl:call-template name="string-in">
							<xsl:with-param name="string-find" select="$string-find"/>
							<xsl:with-param name="string-set" select="substring-after($string-set,',')"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<!-- there are no more strings -->
				<xsl:value-of select="''"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ================================================================== -->
	<!-- return the portion of the string 'between' the begin and end parameters. Case-insensitive.
	      If the search strings do not occur then the entire string is returned.
	   -->
	<xsl:template name="string-between">
		<xsl:param name="buf"/>
		<xsl:param name="begin-param"/>
		<xsl:param name="end-param"/>
		<xsl:call-template name="string-before">
			<!-- find the portion of the string that occurs 'before' the occurence of the
			      'end search parameter' within the portion of the string that occurs 'after' the
			      occurence of the beginning parameter....If that makes any sense ;-)
			  -->
			<xsl:with-param name="string">
				<xsl:call-template name="string-after">
					<xsl:with-param name="string" select="$buf"/>
					<xsl:with-param name="find-what" select="$begin-param"/>
				</xsl:call-template>
			</xsl:with-param>
			<xsl:with-param name="find-what" select="$end-param"/>
		</xsl:call-template>
	</xsl:template>
	<!-- ================================================================== -->
	<!-- Case-insensitive searches for the occurence of one string within another. These are alternatives
	       to the ~standard~ xslt "substring-before() and substring-after() case-sensitive functions.

	       Note: Contrary to the standard "substring-before() and substring-after()" behaviour, these functions return
	       the input string if the search string is not found (within the input string). substring-before() and substring-after()
	       return an empty (null) string.
	   -->
	<xsl:template name="string-before">
		<xsl:param name="string"/>
		<xsl:param name="find-what"/>
		<!-- lower case the string buffer and the search parameter -->
		<xsl:variable name="string-lc" select="translate($string,'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
		<xsl:variable name="find-what-lc" select="translate($find-what,
														'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
		<!-- search for the occurence of the search-string in the string buffer and return the string that is
		       before it. Use the index of the search-string to "find" it in the original -->
		<xsl:choose>
			<xsl:when test="$find-what-lc != '' and contains($string-lc,$find-what-lc)">
				<xsl:value-of select="substring($string,1,string-length(substring-before($string-lc,$find-what-lc)))"/>
			</xsl:when>
			<!-- search parameter does not occur within the string. Return the original (unlike "null" string for substring-before()) -->
			<xsl:otherwise>
				<xsl:value-of select="$string"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="string-after">
		<xsl:param name="string"/>
		<xsl:param name="find-what"/>
		<!-- lower case the string buffer and the search parameter -->
		<xsl:variable name="string-lc" select="translate($string,'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
		<xsl:variable name="find-what-lc" select="translate($find-what,
														'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
		<!-- search for the occurence of the search-string in the string buffer and return the string that is
		       after it. Use the index of the search-string to "find" it in the original -->
		<xsl:choose>
			<xsl:when test="$find-what-lc != '' and contains($string-lc,$find-what-lc)">
				<xsl:value-of select="substring($string,string-length(substring-before($string-lc,$find-what-lc))+string-length($find-what-lc)+1)"/>
			</xsl:when>
			<!-- search parameter does not occur within the string. Return the original (unlike "null" string for substring-after()) -->
			<xsl:otherwise>
				<xsl:value-of select="$string"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
