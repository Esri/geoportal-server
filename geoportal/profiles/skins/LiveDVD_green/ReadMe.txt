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

This customization adds a green theme to your geoportal, as see on the Geoportal Server LiveDVD available for Geoportal Server version 1.2 (http://www.esri.com/special/geoportal/index.html).

Steps to apply:
1) Copy the "green" parent folder into your \\geoportal\catalog\skins\themes directory location.

2) The active color theme is referenced in \\geoportal\catalog\skins\lookAndFeel.jsp. By default, the active theme is the red theme. Open lookAndFeel.jsp. Notice there are three <link rel="stylesheet" ...> tags.
 * The first (...\tundra.css) defines the stylesheet reference for the Browse tab and some other functionalities within the geoportal web application. There is no need to alter this \tundra.css location
 * The second <link rel="stylesheet" ...> tag sets the location of the main.css, which defines the color scheme for the geoportal interface.
 * The third <link rel="stylesheet" ...> tag sets the location of the preview.css, defining the color scheme for the Preview functionality.
By default, the main and preview stylesheets are set to reference stylesheets in the red theme folder. When you change these to reference the green folder, the geoportal interface and Preview function will be displayed according to styles in the green theme folder. 

3) Change these references to point to the green folder, as shown below:
      <link rel="stylesheet" type="text/css"
      href="<%=request.getContextPath()%>/catalog/skins/themes/green/main.css"/>

      <link rel="stylesheet" type="text/css"
      href="<%=request.getContextPath()%>/catalog/skins/themes/green/preview.css"/>

4) Save the lookAndFeel.jsp file.

5) Restart the geoportal web application for your changes to take effect.