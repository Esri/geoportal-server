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
package com.esri.gpt.server.csw.client;

/**
 * Catalog capabilities.
 */
public class CswCatalogCapabilities {

// instance variables ==========================================================
/** The _get record by id get url. */
private String _getRecordByIDGetURL = null;

/** The _get records post url. */
private String _getRecordsPostURL = null;

/** The abstract text. */
private String abstractText;

/** The title. */
private String title;

// properties ==================================================================

/**
 * Gets the title.
 * 
 * @return the title
 */
public String getTitle() {
  return title;
}

/**
 * Sets the title.
 * 
 * @param title the new title
 */
public void setTitle(String title) {
  this.title = title;
}

/**
 * Gets the abstract text.
 * 
 * @return the abstract text
 */
public String getAbstractText() {
  return abstractText;
}

/**
 * Sets the abstract text.
 * 
 * @param abstractText the new abstract text
 */
public void setAbstractText(String abstractText) {
  this.abstractText = abstractText;
}

/**
 * Gets the _get record by id get url.
 * 
 * @return the _get record by id get url
 */
public String get_getRecordByIDGetURL() {
  return _getRecordByIDGetURL;
}

/**
 * Sets the _get record by id get url.
 * 
 * @param recordByIDGetURL the new _get record by id get url
 */
public void set_getRecordByIDGetURL(String recordByIDGetURL) {
  // Check if recordByIDGetURL has a trailing '?' mark
  // If not, put '?' mark

  if (!recordByIDGetURL.endsWith("?")) {
    recordByIDGetURL += "?";
  }

  _getRecordByIDGetURL = recordByIDGetURL;
}

/**
 * Gets the _get records post url.
 * 
 * @return the _get records post url
 */
public String get_getRecordsPostURL() {
  return _getRecordsPostURL;
}

/**
 * Sets the _get records post url.
 * 
 * @param recordsPostURL the new _get records post url
 */
public void set_getRecordsPostURL(String recordsPostURL) {
  _getRecordsPostURL = recordsPostURL;
}

/**
 * Checks if is ready.
 * 
 * @return true, if is ready
 */
public boolean isReady() {
  boolean ready = false;

  if (_getRecordByIDGetURL != null && _getRecordByIDGetURL.length() > 0
      && _getRecordsPostURL != null && _getRecordsPostURL.length() > 0) {
    ready = true;
  }

  return ready;

}

}