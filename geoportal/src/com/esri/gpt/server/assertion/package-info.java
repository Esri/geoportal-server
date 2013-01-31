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
 * 
 * Provides operations associated with RDF like assertions that are serialized
 * as documents within one or more Lucene indexes.
 * 
 * <p>e.g. resource ratings, comments, ...</p> 
 *
 * <p><b>Service Request Patterns</b></p>
 * <ul>
 *   <li>[contextPath]/assertion/operations</li>
 *   <li>[contextPath]/assertion?s=[subject]&amp;p=[predicate]&amp;v=[value]&amp;f=[format]&amp;start=[start]&amp;max=[max]</li>
 * </ul>
 * 
 * <p><b>Service Request Parameters</b></p>
 * <ul>
 *   <li>s=[subject] - always required</li>
 *   <li>p=[predicate]- always required</li>
 *   <li>v=[value] - only required for operations that have a defined valueType,
 *      a value can be posted in the HTTP request body</li>
 *   <li>f=[format] - response format (optional, xml|json|pjson)</li>
 *   <li>start=[start] - starting record (optional, for queries that return multiple records)</li>
 *   <li>max=[max] - max records to return (optional, for queries that return multiple records)</li>
 * </ul>
 * 
 */
package com.esri.gpt.server.assertion;
