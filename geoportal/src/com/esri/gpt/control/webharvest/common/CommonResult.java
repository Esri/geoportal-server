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
package com.esri.gpt.control.webharvest.common;

import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.query.Result;
import java.util.Arrays;

/**
 * Common result implementation.
 */
public class CommonResult implements Result {
/** resources */
private Iterable<Resource> resources;

/**
 * Creates instance of the result.
 * @param resources collection of resources
 */
public CommonResult(Iterable<Resource> resources) {
  this.resources = resources;
}

/**
 * Creates instance of the result.
 * @param resource a single resource
 */
public CommonResult(Resource resource) {
  this(Arrays.asList(new Resource[] {resource}));
}

@Override
public Iterable<Resource> getResources() {
  return resources;
}

@Override
public void destroy() {
}

}
