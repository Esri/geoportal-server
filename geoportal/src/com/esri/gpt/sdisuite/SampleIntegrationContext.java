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
package com.esri.gpt.sdisuite;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.principal.User;

/**
 * Sample implementation of an integration context.
 */
public class SampleIntegrationContext extends IntegrationContext {
  
  /** constructors ============================================================ */

  /** Default constructor */
  public SampleIntegrationContext() {
    super();
  }

  /** methods ================================================================= */
  
  @Override
  public IntegrationResponse checkUrl(String url,
                                      User user,
                                      String username,
                                      String password,
                                      String licenseReturnUrl)
    throws NotAuthorizedException, Exception {
    return null;
  }

  @Override
  public void ensureToken(User user) throws Exception {}

  @Override
  public String getBase64EncodedToken(User user) throws Exception {
    return null;
  }

  @Override
  public String getLicenseId(String licenseReference) throws Exception {
    return null;
  }

  @Override
  public String getUsernameFromSAMLToken(String samlToken) throws Exception {
    return null;
  }

  @Override
  public void initializeUser(User user) throws Exception {}
  
  @Override
  public void shutdown() throws Exception {}

}
