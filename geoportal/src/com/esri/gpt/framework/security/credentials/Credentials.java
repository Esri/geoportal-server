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
package com.esri.gpt.framework.security.credentials;

/**
 * Super-class for security credentials.
 * <p>
 * This class contains no default properties or methods, and serves as a base
 * for multiple credential types. Typically credentials are supplied as a
 * username and password, but other types are possible. 
 */
public class Credentials {

// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public Credentials() {}

// properties ==================================================================

// methods =====================================================================

/**
 * Determines if these credentials are UsernamePasswordCredentials.
 * @return true if these credentials are UsernamePasswordCredentials
 */
public boolean hasUsernamePasswordCredentials() {
  return (this instanceof UsernamePasswordCredentials);
}

/**
 * Convenience method to cast these credentials to UsernamePasswordCredentials
 * if applicable.
 * @return a cast to UsernamePasswordCredentials is applicable, else a null is returned
 */
public UsernamePasswordCredentials getUsernamePasswordCredentials() {
  if (this instanceof UsernamePasswordCredentials) {
    return (UsernamePasswordCredentials)this;
  } else {
    return null;
  }
}
  
}

