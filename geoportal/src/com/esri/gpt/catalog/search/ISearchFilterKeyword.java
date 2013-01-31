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
 * The Interface ISearchFilterKeyword.  Describes a search text 
 * filter object.
 */
public interface ISearchFilterKeyword extends ISearchFilter {

/**
 * The keys of the search text options. *
 */
public static enum KeySearchTextOptions {

/** The exact. */
exact, // Search for exact text (ordered conjunction)
/** The any. */
any, // Search any word (disjunction)
/** The all. */
all;// Search all words (conjunction)

/**
 * Checks value given as string.
 * @param value value given as string
 * @return value as enum, or {@link KeySearchTextOptions#any} if value 
 * unrecognized
 */
public static KeySearchTextOptions checkValueOf(String value) {
  value = Val.chkStr(value);
  for (KeySearchTextOptions ksto : values()) {
    if (ksto.name().equalsIgnoreCase(value)) {
      return ksto;
    }
  }
  return any;
}
}

/**
 * Gets the search text.
 * 
 * @return the search text
 */
public String getSearchText();

/**
 * Sets the search text.
 * 
 * @param searchText the new search text
 */
public void setSearchText(String searchText);

/**
 * Gets search option as enum.
 * @return search option as enum
 */
public KeySearchTextOptions getSearchOptionAsEnum();

/**
 * Sets search option as enum.
 * @param option search option as enum
 */
public void setSearchOptionAsEnum(KeySearchTextOptions option);
}
