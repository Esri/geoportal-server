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
package com.esri.gpt.catalog.schema;
import com.esri.gpt.framework.jsf.FacesContextBroker;

/**
 * Provides a context supporting the active UI request.
 * <p/>
 * This class extends FacesContextBroker allowing ease of access to the
 * FacesContext instance, MessageBroker and other utilities.
 */
public class UiContext extends FacesContextBroker {

// class variables =============================================================
  
// instance variables ==========================================================
private boolean _isCreateDocument = false;
  
// constructors ================================================================

/** Default constructor. */
public UiContext() {
  super();
}

// properties ==================================================================

/**
 * Gets the status indicating if a new document is being created.
 * @return true if a new document is being created
 */
public boolean getIsCreateDocument() {
  return _isCreateDocument;
}
/**
 * Sets the status indicating if a new document is being created
 * @param isCreate true if a new document is being created
 */
public void setIsCreateDocument(boolean isCreate) {
  _isCreateDocument = isCreate;
}

// methods =====================================================================

}

