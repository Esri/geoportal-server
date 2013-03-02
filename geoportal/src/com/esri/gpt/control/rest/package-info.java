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
 * REST interface.
 * <h4>Reference</h4>
 * REST interface supports the following commands:
 * </p>
 * <ul>
 * <li><b>/rest/document/<i>&lt;uuid&gt;</i></b> - gets the metadata of the 
 * document identified by <i>&lt;uuid&gt;</i>,
 * <h4>Parameters:</h4>
 * <ul>
 * <li><b>f</b> - format (<i>xml</i>, <i>html</i>, <i>htmlfragment</i>). 
 * Default: <i>xml</i>. If <i>htmlfragment</i> is chosen, an html snippet will 
 * be generated.<br/>
 * Example: <code>.../rest/document/{550e8400-e29b-41d4-a716-446655440000}?f=html</code>
 * </li>
 * <br/>
 * 
 * <li>
 * <b>style</b> - style URL. Array of URL's of the Cascading Style Sheet (*.css) 
 * files used when <code>html</code> format is choosen. URL's are separated by 
 * coma (,).<br/>
 * Example: <code>.../rest/document?f=html&style=http://&lt;host&gt;:&lt;port&gt;/&lt;context&gt;/main.css</code>
 * </li>
 * <br/>
 * <br/>
 * </ul>
 * </li>
 * </ul>
 */
package com.esri.gpt.control.rest;

