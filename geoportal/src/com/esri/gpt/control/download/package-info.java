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

/**
 * Provides implementation for data download controller.
 * <p/>
 * Data download allows to select region and layers, and request to prepare 
 * available data for it in the specified format and projection. These data will
 * be send to the requested user by email.
 * <p/>
 * Enty point to download data is a page available as 
 * <code>/&lt;context&gt;/catalog/download/download.page</code>. This page can 
 * be accessed either from the Portal main menu or from web browser URL. It
 * recognizes the following parameters:
 * <ul>
 * <li><b>extractDataTaskUrl</b> - geoprocessing task URL<br/>
 * Example: <code>.../catalog/download/download.page?extractDataTaskUrl=http://serverapi.arcgisonline.com/arcgis/rest/services/ExtractData/GPServer/ExtractPortlandDataByString</code>
 * </li>
 * <li><b>mapServiceUrl</b> - map service URL used by geoprocessing task<br/>
 * Example: <code>.../catalog/download/download.page?mapServiceUrl=http://serverapi.arcgisonline.com/arcgis/rest/services/ExtractableData/MapServer</code><br/>
 * Important! <code>mapServiceUrl</code> has to refer exactly the same maps ervice as 
 * geoprocessing task is using.
 * </li>
 * <li><b>format</b> - data format (<code>Geography Markup Language - GML - (.gml)</code>, 
 * <code>Geography Markup Language Simple Features - GMLSF - (.gml)</code>, 
 * <code>MapInfo - MIF - (.mif)</code>, 
 * <code>AutoCAD - ACAD - (.dwg)</code>, 
 * <code>MicroStation Design - IGDS - (.dgn)</code>)<br/>
 * Example: <code>.../catalog/download/download.page?format=MapInfo%20-%20MIF%20-%20(.mif)</code>
 * </li>
 * <li><b>projection</b> - map projection (<code>Mercator</code>,
 * <code>UTM zone 10 north</code>, <code>Web Mercator</code>, 
 * <code>WGS 1984</code>)<br/>
 * Example: <code>.../catalog/download/download.page?projection=Mercator</code>
 * </li>
 * <li><b>layers</b> - indexes of layers selected to include within the output
 * separated by comas (,)<br/>
 * Example: <code>.../catalog/download/download.page?layers=1,3,7</code>
 * </li>
 * </ul>
 */
package com.esri.gpt.control.download;

