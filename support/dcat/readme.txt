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
- Merge gpt-dcat-pacth.xml with your gpt.xml. 
  - The searchResultFormat element goes inside the search element.
  - The paramemeter element (dcat.mappings) goes into catalog, but outside search.
- Append the contents of gpt-dcat-patch.properties to your existing gpt.properties.
- replace your existing WEB-INF/lib/gpt.jar with the gpt-a.b.c-dcat.jar that matches your Geoportal Server version.
- Merge the metadata.zip into the WEB-INF/classes/gpt/config/metadata folder. note especially fgdc-indexables.xml since you should already have one.

With these steps performed your site should be able to provide a DCAT listing in support of Project Open Data (http://project-open-data.github.io/)

If you run into issues or have questions applying this patch, don't hesitate to contact us through our Github site: https://github.com/Esri/geoportal-server/issues

(C) 2013, Esri Inc.