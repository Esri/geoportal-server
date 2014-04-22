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

This customization supports the ability to create and publish/harvest NATO NGMP metadata to the Esri Geoportal Server. 
See http://github.com/Esri/geoportal-server/wiki/How-to-Publish-Resources for documentation for how to create, publish and harvest metadata to a geoportal, and http://github.com/Esri/geoportal-server/wiki/Add-a-Custom-Profile for details on Adding a Custom profile to the geoportal.

To Apply:

1) In the geoportal web application files, copy the ngmp-definition.xml and ngmp-template.xml files to the \\geoportal\WEB-INF\classes\gpt\metadata\iso\ngmp directory

2) Copy the ngmp folder and all its contents to the \\geoportal\WEB-INF\classes\gpt\gxe\iso directory

3) Add the contents of ngmp.properties to \\geoportal\WEB-INF\classes\gpt\resources\gpt.properties

4) Add the following to the \\geoportal\WEB-INF\classes\gpt\metadata\schemas.xml file, anywhere before the final </schemas> tag:

  <schema fileName="gpt/metadata/iso/ngmp/ngmp-definition.xml">

5) Save gpt.properties. Restart the geoportal web application for your changes to take effect. Now when you login as a publisher or administrator, and choose the option to create metadata using the geoportal's online form, you should see the NATO NGMP profile entry in your list of supported geoportal schemas. Also, you should be able to upload or harvest NATO NGMP metadata.