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
package com.esri.gpt.catalog.search;

import java.net.URI;
import java.net.URISyntaxException;


/**
 * The Interface ISearchURI.  Filter object that will hold
 * the connection end point for the search.  
 * 
 */
public interface ISearchFilterURI extends ISearchFilter {

/**
 * Gets the search uri.
 * 
 * @return the search uri
 * 
 * @throws SearchException the search exception when uri is invalid
 */
public URI getSearchURI() throws URISyntaxException;

}
