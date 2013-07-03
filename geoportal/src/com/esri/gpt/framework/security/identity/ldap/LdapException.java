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
package com.esri.gpt.framework.security.identity.ldap;
import com.esri.gpt.framework.context.ApplicationRuntimeException;

/**
 * A ldap related runtime exception.
 */
public class LdapException extends ApplicationRuntimeException {

// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

/**
 * Construct based upon an error message.
 * @param msg the error message
 */
public LdapException(String msg) {
  super(msg);
}

/**
 * Construct based upon an error message and a cause.
 * @param msg the error message
 * @param cause the cause
 */
public LdapException(String msg, Throwable cause) {
  super(msg,cause);
}

// properties ==================================================================

// methods =====================================================================

}
