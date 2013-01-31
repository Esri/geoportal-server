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

import com.esri.gpt.framework.util.Val;

/**
 * An exception encountered while validating a schema.
 */
public class ValidationException extends SchemaException {

// class variables =============================================================

// instance variables ==========================================================
/** schema key  */
private String _key = "";  
/** validation errors */  
private ValidationErrors _validationErrors;

// constructors ================================================================

/**
 * Construct based upon an error message.
 * @param key schema key
 * @param msg the error message
 * @param errors 
 */
public ValidationException(String key, String msg, ValidationErrors errors) {
  super(msg);
  setKey(key);
  setValidationErrors(errors);
}

// properties ==================================================================
/**
 * Gets schema key.
 * @return schema key
 */
public String getKey() {
  return _key;
}

/**
 * Sets schema key.
 * @param key schema key
 */
public void setKey(String key) {
  _key = Val.chkStr(key);
}
/**
 * Gets the validation errors.
 * @return the validation errors
 */
public ValidationErrors getValidationErrors() {
  return _validationErrors;
}
/**
 * Sets the validation errors.
 * @param errors the validation errors
 */
protected void setValidationErrors(ValidationErrors errors) {
  _validationErrors = errors;
  if (_validationErrors == null) _validationErrors = new ValidationErrors();
}

// methods =====================================================================

}
