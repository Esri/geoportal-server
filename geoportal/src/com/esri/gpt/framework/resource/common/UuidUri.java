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
package com.esri.gpt.framework.resource.common;

import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.util.UuidUtil;

/**
 * UUID-based source URI.
 */
public class UuidUri implements SourceUri {

/** uuid */
private String uuid;

/**
 * Creates instance of the source URI.
 * @param uuid base uuid
 */
public UuidUri(String uuid) {
  this.uuid = UuidUtil.isUuid(uuid) ? UuidUtil.addCurlies(uuid) : "";
}

@Override
public boolean equals(Object sourceUri) {
  if (UuidUtil.isUuid(uuid) && sourceUri instanceof UuidUri) {
    return uuid.equalsIgnoreCase(((UuidUri)sourceUri).uuid);
  }
  return false;
}

@Override
public int hashCode() {
  return uuid.hashCode();
}

@Override
public String asString() {
  return uuid;
}

@Override
public String toString() {
  return uuid;
}
}
