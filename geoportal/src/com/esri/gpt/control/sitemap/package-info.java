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
 * Provides an end-point associated with the generation of sitemap files 
 * based upon the content of the metadata catalog.
 * <p/>
 * <i>http://host:port/context</i><b>/sitemap</b>
 * 
 * <p>For large repositories, a sitemap index will be generated and will 
 * reference individual sitemap files with the following URL pattern:</p>
 * <i>http://host:port/context</i><b>/sitemap?startRecord=[n]</b>
 * 
 * <p>Response configuration:</p>
 * <i>gpt.xml gptConfig/catalog</i>
 * <br/>parameter configuration elements: <b>&lt;parameter key="" value=""/&gt;</b>
 * <ul>
 * <li>
 *   <b>sitemap.baseUrl</b> - the base URL for sitemap files
 *   <br/>default = auto-generated, e.g. http://host:port/[contextPath]/sitemap
 * </li>
 * <li>
 *   <b>sitemap.documentUrlPattern</b> - the URL pattern referencing documents within a sitemap,
 *     {0} will be replaced with the document's UUID,
 *   <br/>default = /rest/document/{0}?f=html 
 * </li>
 * <li>            
 *   <b>sitemap.urlsPerIndexFile</b> - the maximum number of sitemap files to be referenced within
 *     the sitemap index file (should not exceed 1000),
 *   <br/>default = 1000
 * </li>
 * <li>
 *   <b>sitemap.urlsPerSitemapFile</b> - the maximum number of documents to be referenced within 
 *     an individual sitemap file (should not exceed 50000),
 *   <br/>default = 40000
 * </li>
 * <li>
 *   <b>sitemap.namespaceUri</b> - the sitemap namespace URI,
 *   <br/>default = http://www.sitemaps.org/schemas/sitemap/0.9
 * </li>
 * <li>
 *   <b>sitemap.changefreg</b> - the change frequency to be listed per document reference,
 *     hourly daily weekly monthly yearly ,
 *   <br/>default = weekly
 * </li>
 * <li>
 *   <b>sitemap.priority</b> - the priority to be listed per document reference, 0.0 -> 1.0,
 *   <br/>default = none  
 * </li>
 * </ul> 
 * <p/>      
 * @see <A HREF="http://www.sitemaps.org/" target="_blank">sitemaps.org</A>
 */
package com.esri.gpt.control.sitemap;

