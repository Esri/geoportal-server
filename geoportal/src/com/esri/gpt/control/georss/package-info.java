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
 * Provides GEORSS feed.
 * GEORSS feed is generated according to RSS 2.0 specification and GEORSS with
 * simple encoding.
 * <p/>
 * GEORSS feed is accessible through 
 * &#47;<i>&lt;Application Context&gt;</i><b>&#47;rest&#47;find&#47;document</b> 
 * context.
 * <h4>Parameters:</h4>
 * <ul>
 * 
 * <li>
 * <b>bbox</b> - bounding box; defines bounding box of the query - each record 
 * found has to have it's envelope completly enclosed within defined bounding 
 * box. Bounding box is defined by two pairs of coordinates representing 
 * west-south, and east-north corner of the envelope separated by coma (,). 
 * Each corner is defined by pair of values as longitude-lattitude separated by 
 * coma (,). Default: -180,-90,180,90 (entire World).<br/>
 * Example: <code>.../rest/find/document?bbox=0,0,20,30</code>
 * </li>
 * <br/>
 * 
 * <li>
 * <b>spatialRel</b> - spatial relationship. Possible values: 
 * <code>esriSpatialRelWithin</code> (metadata envelope has to completly fit
 * within request bounding box), <code>esriSpatialRelOverlaps</code> (metadata
 * envelope has at least overlap request bounding box).
 * Default: <code>esriSpatialRelWithin</code>. Used in conjunction with 
 * <b>bbox</b> parameter.<br/>
 * Example: <code>.../rest/find/document?spatialRel=esriSpatialRelOverlaps</code>
 * </li>
 * <br/>
 * 
 * <li>
 * <b>searchText</b> - text to be searched within metadata.<br/>
 * Example: <code>.../rest/find/document?searchText=Congo</code>
 * </li>
 * <br/>
 * 
 * <li>
 * <b>contains</b> - <code>true</code> to search for any word specified in 
 * <i>searchText</i> attribute, <code>false</code> to perform exact search.
 * Used in conjunction with <b>searchText</b> parameter. Also accepts any of
 * the values defined in 
 * {@link com.esri.gpt.catalog.search.ISearchFilterKeyword.KeySearchTextOptions}.
 * Default: <code>true</code><br/>
 * Example: <code>.../rest/find/document?contains=false</code>
 * </li>
 * <br/>
 * 
 * <li>
 * <b>contentType</b> - search metadata for the specific content type. Accepts
 * names defined in 
 * {@link com.esri.gpt.catalog.search.SearchEngineCSW.AimsContentTypes}.
 * Default: <code>none</code><br/>
 * Example: <code>.../rest/find/document?contentType=downloadableData</code>
 * </li>
 * <br/>
 * 
 * <li>
 * <b>dataCategory</b> - search metadata for the specific data category. Accepts
 * any set fo the following keywords eparated by come (,): farming, biota,
 * boundaries, climatologyMeteorologyAtmosphere, economy, elevation, 
 * environment, geoscientificInformation, health, imageryBaseMapsEarthCover,
 * intelligenceMilitary, inlandWaters, location, oceans, planningCadastre,
 * society, structure, transportation, utilitiesCommunication.
 * Default: <code>empty set</code><br/>
 * Example: <code>.../rest/find/document?dataCategory=economy,elevation</code>
 * </li>
 * <br/>
 * 
 * <li>
 * <b>after</b> - metadata updated afer certain date given as 'yyyy-mm-dd'
 * <br/>
 * Example: <code>.../rest/find/document?after=2006-01-01</code>
 * </li>
 * <br/>
 * 
 * <li>
 * <b>before</b> - metadata updated before certain date given as 'yyyy-mm-dd'
 * <br/>
 * Example: <code>.../rest/find/document?before=2006-01-01</code>
 * </li>
 * <br/>
 * 
 * <li>
 * <b>orderBy</b> - sort parameter. Accepts any of the values defined in
 * {@link com.esri.gpt.catalog.search.SearchFilterSort.OptionsSort}. Default: 
 * {@link com.esri.gpt.catalog.search.SearchFilterSort.OptionsSort#dateDescending}.
 * <br/>
 * Example: <code>.../rest/find/document?orderBy=dateAscending</code>
 * </li>
 * <br/>
 * 
 * <li>
 * <b>start</b> - the starting record. Default: 1.<br/>
 * Example: <code>.../rest/find/document?start=2</code>
 * </li>
 * <br/>
 * 
 * <li>
 * <b>max</b> - maximum number of records in the feed. Default: 10.<br/>
 * Example: <code>.../rest/find/document?max=5</code>
 * </li>
 * <br/>
 * 
 * <li>
 * <b>geometryType</b> - defines how spatial data will be represented in the 
 * feed. Possible values are: <code>esriGeometryPoint</code>, 
 * <code>esriGeometryPolygon</code>, <code>esriGeometryBox</code>. Default: 
 * <code>esriGeometryPolygon</code>.<br/>
 * Example: <code>.../rest/find/document?geometryType=esriGeometryPoint</code>
 * </li>
 * <br/>
 * 
 * <li>
 * <b>f</b> - output format. Possible values are: <code>georss</code>, 
 * <code>kml</code>, <code>html</code>, <code>htmlfragment</code>. 
 * Default: <code>georss</code>. If <code>htmlfragment</code> selected, 
 * body'less HTML snippet will be generated.<br/>
 * Example: <code>.../rest/find/document?f=kml</code>
 * </li>
 * <br/>
 * 
 * <li>
 * <b>style</b> - style URL. Array of URL's of the Cascading Style Sheet (*.css) 
 * files used when <code>html</code> format is choosen. URL's are separated by 
 * coma (,).<br/>
 * Example: <code>.../rest/find/document?f=html&style=http://&lt;host&gt;:&lt;port&gt;/&lt;context&gt;/main.css</code>
 * </li>
 * <br/>
 * 
 * <li>
 * <b>target</b> - links target. Possible values are: <code>blank</code>, 
 * <code>parent</code>, <code>self</code>, <code>top</code> (just like HTML <A>
 * "target" attribute except no leading underscore '_'). Default: 
 * <code>blank</code>. It affects every link generated in GEORSS feed.
 * Example: <code>.../rest/find/document?target=self</code>
 * </li>
 * 
 * </ul>
 * @see <A HREF="http://www.rssboard.org/rss-specification" target="_blank">RSS 2.0 specification</A>
 * @see <A HREF="http://georss.org/" target="_blank">GEORSS specification</A>
 * @see <A HREF="http://www.opengeospatial.org/standards/kml/" target="_blank">Keyhole Markup Language</A>
 */
package com.esri.gpt.control.georss;

