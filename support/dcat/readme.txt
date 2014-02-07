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

--- 8< ---

This patch provides DCAT support for your Geoportal Server 1.2.4 instance. DCAT support includes providing DCAT information in the Geoportal REST response, and also generates a  cached JSON file listing the entire contents of your catalog.  This file will be made web-accessable through your geoportal (http://your_server/geoportal/dcat.json).  This cached file is generated the first time Tomcat is started after applying the patch, and then it is updated at the interval specified in the thread class you configure in gpt.xml, as described in the instructions below.

With these steps performed your site should be able to provide a DCAT listing in support of Project Open Data (http://project-open-data.github.io/).
To add this DCAT support to your Geoportal Server 1.2.4 instance, take the following steps. If you want to add DCAT support to a geoportal other than version 1.2.4, please contact Esri at portal(at)esri.com

Very First Step: Make a backup copy of your existing geoportal web application before applying any of these changes.

1) Copy the folder structure and new classes from the \\geoportal_124_dcatCache\WEB-INF\classes\com folder into your \\geoportal\WEB-INF\classes folder.

2) Open your \\geoportal\WEB-INF\web.xml file, and add the following section just after the closing </servlet-mapping> section for the <servlet-name>CartServlet</servlet-name>, and just above the <session-config>:


	<servlet>
      <servlet-name>DcatCacheServlet</servlet-name>
      <servlet-class>com.esri.gpt.control.georss.DcatCacheServlet</servlet-class>
    </servlet>
    <servlet-mapping>
      <servlet-name>DcatCacheServlet</servlet-name>
      <url-pattern>/dcat.json</url-pattern>
    </servlet-mapping>

3) Save the web.xml file.

4) Add the following to the \\geoportal\WEB-INF\classes\gpt\config\gpt.xml file, just above the closing </catalog> tag; IMPORTANT - update the 'value' to match the location of your geoportal web application:

 <!-- DCAT cache directory -->
        <parameter key="dcat.cache.path" value="C:/Tomcat/webapps/geoportal"/>

5) Also in the gpt.xml file, add the following just above the closing </scheduler> tag. IMPORTANT - update the 'period' if you want to change how often the cached file is regenerated, and the 'delay' if you want to change the delay between when your web app server is restarted and the cached file is generated:

 <!-- DCAT cache generation -->
        <thread class="com.esri.gpt.control.georss.dcatcache.DcatCacheTask" period='1[DAY]' delay="15[SECOND]"/>

5b) (OPTIONAL) The changes you will be applying with this patch will add new indices to your geoportal's index store, and may change the existing indexed content.  For best results, we highly recommended re-indexing your metadata content in the geoportal. For a complete reindex, first update the lucene        'indexLocation' attribute in this gpt.xml file such that it points to a new empty folder location. When you make this change, you will no longer see search results on your geoportal search page because content is not discoverable in your geoportal until it is indexed.  You will be reindexing your content in a later step.  

6) Save the gpt.xml file

7) Replace your \\geoportal\WEB-INF\classes\gpt\metadata\dcat-mappings.xml file with the one in this patch. 

8) If you have not customized your \\geoportal\WEB-INF\classes\gpt\metadata\property-meanings.xml file, then you can replace your file with the one from this patch. If you have made custom changes to your property-meanings.xml file, then compare your version with this version in the patch and update your file to the following - this update should be near the end of the file:

<property-meaning name="dcat.accessLevelComment" valueType="String" comparisonType="value">
  </property-meaning>
  <property-meaning name="dcat.mbox" valueType="String" comparisonType="value">
  </property-meaning>
  <property-meaning name="dcat.person" valueType="String" comparisonType="value">
  </property-meaning>
  <property-meaning name="dcat.license" valueType="String" comparisonType="value">
  </property-meaning>
  <property-meaning name="dcat.dataDictionary" valueType="String" comparisonType="value">
  </property-meaning>
  <property-meaning name="dcat.modified" valueType="Timestamp" comparisonType="value">
  </property-meaning>
  <property-meaning name="dcat.publisher" valueType="String" comparisonType="value">
  </property-meaning>  
 <property-meaning name="dcat.accessUrl" valueType="String" comparisonType="value">
  </property-meaning>  
   <property-meaning name="dcat.format" valueType="String" comparisonType="value">
  </property-meaning>  


9) Save the property-meanings.xml file.

10) If you have not customized your \\geoportal\WEB-INF\classes\gpt\metadata\fgdc\fgdc-indexables.xml file, then you can replace your file with the one from this patch. If you have made custom changes to your fgdc-indexables.xml file, then compare your version with this version in the patch and update your file to the following - this update should be near the end of the file:

 <!-- dcat-specific -->
  <property meaning="dcat.accessLevelComment" xpath="/metadata/idinfo/accconst | /metadata/idinfo/useconst | /metadata/distinfo/distliab"/>
  <property meaning="dcat.mbox" xpath="/metadata/idinfo/ptcontac/cntinfo/cntemail"/>
  <property meaning="dcat.person" xpath="/metadata/idinfo/ptcontac/cntinfo/cntorgp/cntper" />
  <property meaning="dcat.dataDictionary" xpath="/metadata/idinfo/keywords/theme/themekt | /metadata/eainfo/overview/eadetcit" />
  <property meaning="dcat.modified" xpath="/metadata/idinfo/citation/citeinfo/pubdate"/>
  <property meaning="dcat.publisher" xpath="/metadata/idinfo/citation/citeinfo/pubinfo/publish | /metadata/distinfo/distrib/cntinfo/cntperp/cntper | /metadata/distinfo/distrib/cntinfo/cntorgp/cntorg"/>
  <property meaning="dcat.format" xpath="/metadata/distinfo/storder/digform/digtinfo/fname"/>
  <property meaning="dcat.accessUrl" xpath="/metadata/distinfo/stdorder/digform/digtopt/onlinopt/computer/networka/networkr"/>

11) Save the fgdc-indexables.xml file.

12) If you have not customized your \\geoportal\WEB-INF\classes\gpt\metadata\iso\apiso-indexables.xml file, then you can replace your file with the one from this patch. If you have made custom changes to your apiso-indexables.xml file, then compare your version with this version in the patch and update your file to the following:

a) Update the <property meaning="apiso:Format" to the following xpath- the update here is adding the 'gmd:' namespace to the 'name' element: 
    xpath="/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString"/>

b) Near the end of the file, add the following:

<!-- dcat-specific -->
    <property meaning="dcat.publisher"
 xpath="
/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString[../../gmd:role/gmd:CI_RoleCode/@codeListValue='publisher'] |  /gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString[../../gmd:role/gmd:CI_RoleCode/@codeListValue='publisher'] |
/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString[../../gmd:role/gmd:CI_RoleCode/@codeListValue='publisher'] |  /gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString[../../gmd:role/gmd:CI_RoleCode/@codeListValue='publisher']
| /gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName"/>
    <property meaning="dcat.person" xpath="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString | /gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString |  /gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString | /gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString"/>
    <property meaning="dcat.mbox" xpath="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString | /gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"/> 
    
    <property meaning="dcat.dataDictionary" xpath="/gmd:MD_Metadata/gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage"/>
    
    <property meaning="dcat.accessUrl"
 xpath="
/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL[../../gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='download']"/>

13) Save the apiso-indexables.xml file.

14) If you have not customized your \\geoportal\WEB-INF\classes\gpt\metadata\iso\apiso-2-indexables.xml file, then you can replace your file with the one from this patch. If you have made custom changes to your apiso-2-indexables.xml file, then compare your version with this version in the patch and update your file to the following:

a) Again, update the <property meaning="apiso:Format" to the following xpath- the update here is adding the 'gmd:' namespace to the 'name' element: 
    xpath="/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString"/>

b) Near the end of the file, add the following:

 <!-- dcat-specific -->
  <property meaning="dcat.publisher"
 xpath="
/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString[../../gmd:role/gmd:CI_RoleCode/@codeListValue='publisher'] |  /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString[../../gmd:role/gmd:CI_RoleCode/@codeListValue='publisher']
| /gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName"/>
   <property meaning="dcat.person" xpath="/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString |  /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString"/>
   <property meaning="dcat.mbox" xpath="/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"/> 
   <property meaning="dcat.dataDictionary" xpath="/gmi:MI_Metadata/gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage"/>
   <property meaning="dcat.accessUrl"
 xpath="
/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL[../../gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='download']"/>

15) Save the apiso-2-indexables.xml file.

16) Open the \\geoportal\WEB-INF\classes\gpt\resources\gpt.properties file, and find the "# v1.2.4" section. There, update the properties as follows:

catalog.json.dcat.title = Geoportal Server Dcat Json
catalog.json.dcat.description = Dcat json response of geoportal metadata catalog
catalog.json.dcat.keyword = ["Geoportal Server", "Dcat Json"]
catalog.json.dcat.publisher =
catalog.json.dcat.contactPoint = 
catalog.json.dcat.mbox =
catalog.json.dcat.identifier =
catalog.json.dcat.accessLevel = public 
catalog.json.dcat.accessLevelComment =
catalog.json.dcat.bureauCode = ["010:86"]
catalog.json.dcat.programCode = ["015:001", "015:002"]
catalog.json.dcat.dataDictionary = Places 
catalog.json.dcat.accessURL =
catalog.json.dcat.webService =
catalog.json.dcat.format = Json
catalog.json.dcat.license = Apache 2.0
catalog.json.dcat.spatial = -180 -90 180 90
catalog.json.dcat.temporal = 2013

17) Now, update the strings in this section to suit the needs of your organization. These values will be used as default values for your catalog and metadata records if and when the information is not available from the metadata itself (this patch generally follows the metadata indexing guidance for DCAT as described at http://project-open-data.github.io/metadata-resources). If you are a federal agency, use the guidance at http://project-open-data.github.io/schema to find out more about the codes to update the 'catalog.json.dcat.bureauCode' with your Bureau Code and 'catalog.json.dcat.programCode' with your Program Code.

18) Save the gpt.properties file.

19) Restart your web app server (e.g., Tomcat).

20) Now you will need to reindex metadata documents to see the changes. To do this, login to your geoportal as an administrator and reapprove a few metadata documents. This will reindex those documents for testing. Note that if you did the optional Step 5b above, then only the records you reapprove will be visiable when you do a search in your Geoportal. A complete reindex of your documents will occur later on in the evening when the database and index auto-sync.

21) Next, test. You can see the new dcat indices when you view the REST document statistics at http://your_server/geoportal/rest/index/stats.

22) To see the new indexed metadata in the auto-generated cached DCAT JSON file, restart your web app server (e.g., Tomcat). Wait a couple of minutes so the cached file can be created, and then access the file at http://your_server/geoportal/dcat.json (replacing 'your_server/geoportal' with your geoportal URL).

- Further documentation is available at https://github.com/Esri/geoportal-server/wiki/Customize-DCAT-output.

If you run into issues or have questions applying this patch, don't hesitate to contact us through our GitHub site: https://github.com/Esri/geoportal-server/issues

(C) 2014, Esri Inc.
