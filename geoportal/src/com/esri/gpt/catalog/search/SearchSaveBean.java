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

import com.esri.gpt.framework.util.Val;

/**
 * The Class SearchSaveBean.  Bean representing a saved search.
 */
public class SearchSaveBean {

// instance variables ==========================================================
/** The id. */
String id;

/** The name. */
String name;

// constructor =================================================================
/**
 * Instantiates a new search save bean.
 * 
 * @param id the id
 * @param name the name
 */
public SearchSaveBean(String id, String name){
  this.id = id;
  this.name = name;
}

// properties ==================================================================
/**
 * Gets the id.
 * 
 * @return the id (never null)
 */
public String getId() {
  return Val.chkStr(id);
}

/**
 * Sets the id.
 * 
 * @param id the new id
 */
public void setId(String id) {
  this.id = id;
}

/**
 * Gets the name.
 * 
 * @return the name (never null)
 */
public String getName() {
  return Val.chkStr(name);
}

/**
 * Sets the name.
 * 
 * @param name the new name
 */
public void setName(String name) {
  this.name = name;
}

}

