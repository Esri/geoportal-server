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
package com.esri.gpt.control.download;

import com.esri.gpt.framework.util.Val;
import java.io.Serializable;

/**
 * Item (projection or output format) info.
 */
public class ItemInfo implements Serializable {

/** key */
private String _key = "";
/** alias */
private String _alias = "";
/** resource key */
private String _resourceKey = "";

/**
 * Creates instance of the item info.
 */
public ItemInfo() {
  
}

/**
 * Creates instance of the item info.
 * @param key key
 * @param alias alias
 * @param resourceKey resource key
 */
public ItemInfo(String key, String alias, String resourceKey) {
  setKey(key);
  setAlias(alias);
  setResourceKey(resourceKey);
}

/**
 * Gets key.
 * @return key
 */
public String getKey() {
  return _key;
}

/**
 * Sets key.
 * @param key key
 */
public void setKey(String key) {
  _key = Val.chkStr(key);
}

/**
 * Gets alias.
 * @return alias
 */
public String getAlias() {
  return _alias;
}

/**
 * Sets alias.
 * @param alias alias
 */
public void setAlias(String alias) {
  _alias = Val.chkStr(alias);
}

/**
 * Gets resource key.
 * @return resource key
 */
public String getResourceKey() {
  return _resourceKey;
}

/**
 * Sets resource key.
 * @param resourceKey resource key
 */
public void setResourceKey(String resourceKey) {
  _resourceKey = Val.chkStr(resourceKey);
}

/**
 * Creates string representation of the item info.
 * @return string representation of the item info
 */
@Override
public String toString() {
  StringBuilder sb = new StringBuilder();
  sb.append("key=");
  sb.append(getKey());
  sb.append(", ");
  sb.append("alias=");
  sb.append(getAlias());
  sb.append(", ");
  sb.append("resKey=");
  sb.append(getResourceKey());
  return sb.toString();
}
}
