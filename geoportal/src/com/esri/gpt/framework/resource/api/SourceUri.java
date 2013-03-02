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

package com.esri.gpt.framework.resource.api;

/**
 * Source URI.
 * Each publishable resource must be identified in a unique way. This also
 * depends on the source of such a resource. <i>SourceUri</i> defines how such
 * a resource can be compared with another. It is usually done through {@link Object#equals}
 * method.
 */
public interface SourceUri {
/**
 * Provides pure string representation of the Source URI.
 * @return pure string representation of the Source URI
 */
String asString();
}
