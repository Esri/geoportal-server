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
package com.esri.gpt.control.livedata.selector;

import com.esri.gpt.framework.http.ContentProvider;
import com.esri.gpt.framework.http.StringProvider;
import com.esri.gpt.framework.util.Val;
import java.util.Comparator;

/**
 * Request definition.
 */
public class HttpRequestDefinition {
  /** comparator */
  public static final Comparator<HttpRequestDefinition> CASE_INSENCITIVE_ORDER =
    new CaseInsensitiveComparator();

  /** URL */
  private String url = "";
  /** POST body content (optional) */
  private String content = "";

  /**
   * Creates GET request definition.
   * @param url URL
   */
  public HttpRequestDefinition(String url) {
    this.url = Val.chkStr(url);
  }

  /**
   * Creates POST request definition.
   * @param url URL
   * @param content POST body
   */
  public HttpRequestDefinition(String url, String content) {
    this.url = Val.chkStr(url);
    this.content = Val.chkStr(content);
  }

  /**
   * Gets URL.
   * @return URL
   */
  public String getUrl() {
    return url;
  }

  /**
   * Gets content provider.
   * @return content provider for POST request, <code>null</code> for GET request
   */
  public ContentProvider getContentProvider() {
    return content.length() > 0 ? new StringProvider(content, "UTF-8") : null;
  }

  @Override
  public String toString() {
    return url;
  }

  /**
   * Case insensitive comparator.
   */
  private static class CaseInsensitiveComparator implements Comparator<HttpRequestDefinition> {

    public int compare(HttpRequestDefinition o1, HttpRequestDefinition o2) {
      int urlComp = o1.url.compareToIgnoreCase(o2.url);
      if (urlComp != 0) {
        return urlComp;
      } else {
        return o1.content.compareTo(o2.content);
      }
    }
  }
}
