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
package com.esri.gpt.framework.resource.query;

/**
 * Query interface capabilities.
 * @see QueryBuilder#getCapabilities()
 */
public interface Capabilities {

/**
 * Checks if query is able to limit maximum number records.
 * @return <code>true</code> if feature is supported
 */
boolean canQueryMaxRecords();

/**
 * Checks if query can search by text.
 * @return <code>true</code> if feature is supported
 */
boolean canQuerySearchText();

/**
 * Checks if query can search by <i>from</i> date.
 * @return <code>true</code> if feature is supported
 */
boolean canQueryFromDate();

/**
 * Checks if query can search by <i>to</i> date.
 * @return <code>true</code> if feature is supported
 */
boolean canQueryToDate();

/**
 * Checks if query can search by bounding box.
 * @return <code>true</code> if feature is supported
 */
boolean canQueryBBox();

/**
 * Checks if query can search by content type.
 * @return <code>true</code> if feature is supported
 */
boolean canQueryContentType();

/**
 * Checks if query can search by data category.
 * @return <code>true</code> if feature is supported
 */
boolean canQueryDataCategory();

/**
 * Checks if query can search by bounding box option.
 * @return <code>true</code> if feature is supported
 */
boolean canQueryBBoxOption();

/**
 * Checks if query can sort.
 * @return <code>true</code> if feature is supported
 */
boolean canQuerySortOption();
}
