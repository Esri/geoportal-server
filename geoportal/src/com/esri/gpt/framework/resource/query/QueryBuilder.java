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

import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.api.Publishable;
import java.io.IOException;

/**
 * Query builder.
 */
public interface QueryBuilder {

/**
 * Gets query capabilities.
 * @return capabilities
 */
Capabilities getCapabilities();

/**
 * Creates new query.
 * @param crt query criteria.
 * @return query
 */
Query newQuery(Criteria crt);

/**
 * Gets native resource.
 * Native resource is a publishable resource created just for repository definition.
 * Each native resource is {@link Publishable} and each repository has to be able to
 * provide one.
 * @return native resource.
 */
Native getNativeResource();
}
