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
package com.esri.gpt.catalog.arcims;

/**
 * Stores a reference to an ArcIMS server.
 */
public class ImsCatalog {

// class variables =============================================================

// instance variables ==========================================================
private ImsService _browseService;
private ImsService _publishService;

// constructors ================================================================

/** Default constructor. */
public ImsCatalog() {
  setBrowseService(new ImsService());
  setPublishService(new ImsService());
}

// properties ==================================================================

/**
 * Gets the metadata browse service.
 * @return the service
 */
public ImsService getBrowseService() {
  return _browseService;
}
/**
 * Sets the metadata browse service.
 * @param service the service
 */
private void setBrowseService(ImsService service) {
  _browseService = service;
}

/**
 * Gets the metadata publish service.
 * @return the service
 */
public ImsService getPublishService() {
  return _publishService;
}
/**
 * Sets the metadata publish service.
 * @param service the service
 */
private void setPublishService(ImsService service) {
  _publishService = service;
}

// methods =====================================================================

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append(" publishService:").append(getPublishService()).append("\n");
  sb.append(" browseService:").append(getBrowseService()).append("\n");
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}

}

