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
package com.esri.gpt.framework.search;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import java.util.Map;

import com.esri.gpt.catalog.search.ResourceLink;

/**
 * The Class SearchXslRecordLinks.
 * 
 * @author TM
 */
public class SearchXslRecordLinks {

// instance variables ==========================================================
/** The custom links. */
private Map<String, List<String>> customLinks =
  new HashMap<String, List<String>>();

/** The default link options. */
private Map<String, Boolean> defaultLinkOptions = 
  new TreeMap<String, Boolean>(String.CASE_INSENSITIVE_ORDER);


// properties ==================================================================
/**
 * Gets the custom links.
 * 
 * @return the custom links
 */
public Map<String, List<String>> getCustomLinks() {
  if(customLinks == null) {
    customLinks = new HashMap<String, List<String>>();
  }
  return customLinks;
}

// methods =====================================================================
/**
 * Adds the custom link.
 * 
 * @param label the label
 * @param url the url
 */
public void addCustomLink(String label, String url) {
  Map<String, List<String>> customLinks = getCustomLinks();
  if(!customLinks.containsKey(label)) {
    customLinks.put(label, new LinkedList<String>());
  }
  customLinks.get(label).add(url);
}

/**
 * Adds the default link options.
 * 
 * @param tag the tag
 * @param show the show
 */
public void addDefaultLinkOptions(String tag, boolean show) {
  if(this.defaultLinkOptions == null) {
    this.defaultLinkOptions = new HashMap<String, Boolean>();
  }
  defaultLinkOptions.put(tag, show );
}

/**
 * Read link option.
 * 
 * @param tag the tag
 * @return true,false according to setting (default is true if tag is not found)
 */
public boolean readShowLink(String tag) {
  Boolean bTag = defaultLinkOptions.get(tag);
  if(bTag == null) {
    return true;
  }
  return bTag;
}

/**
 * Read show link.
 * 
 * @param tag the tag
 * @param defaultShowLink the default show link
 * @return true, if successful
 */
public boolean readShowLink(String tag, boolean defaultShowLink) {
  Boolean bTag = defaultLinkOptions.get(tag);
  if(bTag == null) {
    return defaultShowLink;
  }
  return bTag;
}

/**
 * Returns whether zoomto should be viewable.  Convinience method for JSF.
 * 
 * @return true or false
 */
public boolean getShowZoomTo() {
  return this.readShowLink(ResourceLink.TAG_ZOOMTO);
}

}
