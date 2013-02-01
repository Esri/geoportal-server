/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

This customization supports the ability to add GI-CAT CSW 2.0.2 repositories to your geoportal for federated search and/or harvesting per documentation at http://github.com/eggwhites/geoportal-server/wiki/How-to-Publish-Resources.

To Apply:

1) Copy the CSW_2.0.2_OGCISOAP_GICAT_GetRecords_Request.xslt, CSW_2.0.2_OGCISOAP_GICAT_GetRecords_Response.xslt, and CSW_2.0.2_OGCISOAP_GICAT_GetRecordById_Response.xslt files to the \\geoportal\WEB-INF\classes\gpt\search\profiles location in your geoportal web application

2) Open the \\geoportal\WEB-INF\classes\gpt\search\profiles\CSWProfiles.xml file and scroll to the bottom. Paste the snippet from the Gi-Cat-CSWProfiles-snippet.xml file into this file, just after the fial </Profile> tag and before the closing </CSWProfiles> tag.

3) Save the CSWProfiles.xml file.

4) Restart your geoportal web application for this change to take affect. Now, the GI-Cat CSW profile should show up when you choose the CSW radio on the geoportal's Register Resource on the Network page.