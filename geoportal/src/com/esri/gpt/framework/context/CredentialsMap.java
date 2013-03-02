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
package com.esri.gpt.framework.context;

import com.esri.gpt.framework.http.CredentialProvider;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Credentials map.
 * Associates credentials with the URL
 */
public class CredentialsMap extends TreeMap<String,CredentialProvider> {

  /**
   * Creates instance of the map.
   */
  public CredentialsMap() {
    super(new UriComparator());
  }

  /**
   * Extracts object from the session.
   * @param request HTTP request
   * @return credentials map
   */
  public static CredentialsMap extract(HttpServletRequest request) {
    HttpSession session = request.getSession(true);
    Object obj = session.getAttribute(CredentialsMap.class.getCanonicalName());
    if (!(obj instanceof CredentialsMap)) {
      obj = new CredentialsMap();
      session.setAttribute(CredentialsMap.class.getCanonicalName(), obj);
    }
    return (CredentialsMap) obj;
  }

  /**
   * Comparator which only compares scheme and authority.
   */
  private static class UriComparator implements Comparator<String>, Serializable {

    public int compare(String o1, String o2) {
      try {
        o1 = getSchemeAndAuthority(o1);
        o2 = getSchemeAndAuthority(o2);
        return o1.compareToIgnoreCase(o2);
      } catch (URISyntaxException ex) {
        return o1.compareToIgnoreCase(o2);
      }
    }

    /**
     * Gets scheme and authority from the URI.
     * @param s URI
     * @return scheme and authority
     * @throws URISyntaxException if string is not a valid URI
     */
    private static String getSchemeAndAuthority(String s) throws URISyntaxException {
      URI u = new URI(s);
      return u.getScheme()+ "://" + u.getAuthority();
    }
  }
}
