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

In order to add DCAT support to your Geoportal Server instance take the following steps:

- Make coffee and have some!
- Backup your existing Geoportal Server
- Merge gpt-dcat-patch.xml with your gpt.xml. 
  - The searchResultFormat element goes inside the search element.
  - The parameter element (dcat.mappings) goes into catalog, but outside the search element.
  - The thread class "com.esri.gpt.control.georss.dcatcache.DcatCacheTask" goes inside the scheduler element.
- Append the contents of gpt-dcat-patch.properties to your existing gpt.properties.
- Replace your existing WEB-INF/lib/gpt.jar with the gpt-a.b.c-dcat.jar that matches your Geoportal Server version. IMPORTANT: if you have customizations that required a different 
gpt.jar than the default distributed with your geoportal, those customizations will be overwritten when you apply this patch.
- Carefully merge the metadata.zip into the WEB-INF/classes/gpt/config/metadata folder. Note especially fgdc-indexables.xml, apiso-indexables.xml, apiso-2-indexables.xml, and property-meanings.xml you will add the property meanings 
that begin with 'dcat.'. Do not change the other property meanings, only add the ones for dcat. Also, remember to copy the dcat-mappings.xml to the 
\\geoportal\WEB-INF\classes\gpt\metadata directory. This dcat-mappings.xml will map existing indices in lucene to the DCAT indices.
- If you applied the gpt-1.2.2-dcat.jar file (i.e., you are patching Geoportal Server version 1.2.2), there is an additional step to support enhanced functionality. The enhanced functionality
is that a schedulable cached JSON file listing the entire contents of your catalog will be made available at a URL http://your_server/geoportal/dcat.json.  This cached file is generated the first time Tomcat is started after applying the patch, and then 
it is updated at the interval specified in the thread class you configured in the earlier step for gpt.xml.
- Additional step for gpt-1.2.2.jar: open your geoportal/WEB-INF/web.xml file and copy the snippet from the web_snippet.xml file into the servlet section.
- If you have geoportal version 1.2.4, then you will want to apply the metadata configuration file changes described earlier (the metadata.zip step). Also, you will not need to update your gpt.jar file in the WEB-INF/lib folder; instead, follow instructions included in the ReadMe_124.txt file that is in the 'geoportal_1.2.4_dcatCache.zip' file.
- Save any files you changed, and restart your web app server (e.g., Tomcat).
- Re-index your FGDC and ISO metadata so they will be available through the REST API for DCAT. To force an immediate reindex for testing purposes, login as an administrator and reapprove a few metadata documents. 
- Next, test. You can see the new dcat indices when you view the REST document statistics at http://your_server/geoportal/rest/index/stats.
- Further documentation is available at https://github.com/Esri/geoportal-server/wiki/Customize-DCAT-output.

Note - default values for DCAT parameters can also be set in the gpt.properties file - if you look for the properties that begin wtih "catalog.json.dcat", you'll see where to set them for the DCAT JSON response.

With these steps performed your site should be able to provide a DCAT listing in support of Project Open Data (http://project-open-data.github.io/)

If you run into issues or have questions applying this patch, don't hesitate to contact us through our Github site: https://github.com/Esri/geoportal-server/issues

(C) 2013, Esri Inc.
