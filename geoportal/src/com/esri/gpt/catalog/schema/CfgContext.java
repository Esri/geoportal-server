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

/**
 * Provides a context supporting schema configuration.
 * <p/>
 * This primary intent is to provide access to a SchemaFactory to 
 * aid the instantiation of objects during configuration.
 */
public class CfgContext {

// class variables =============================================================
  
// instance variables ==========================================================
private SchemaFactory _factory;
  
// constructors ================================================================

/** Default constructor. */
public CfgContext() {
  setFactory(new SchemaFactory());
}

/**
 * Constructs with a supplied metadata component factory.
 * @param factory the metadata component factory
 */
public CfgContext(SchemaFactory factory) {
  setFactory(factory);
}

// properties ==================================================================

// methods =====================================================================

/**
 * Gets the metadata component factory.
 * @return the factory
 */
public SchemaFactory getFactory() {
  return _factory;
}
/**
 * Sets the metadata component factory.
 * @param factory the factory
 */
public void setFactory(SchemaFactory factory) {
  _factory = factory;
  if (_factory == null) _factory = new SchemaFactory();
}

}

