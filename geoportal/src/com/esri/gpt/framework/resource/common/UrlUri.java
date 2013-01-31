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
package com.esri.gpt.framework.resource.common;

import com.esri.gpt.framework.resource.api.SourceUri;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * URL-based source URI.
 */
public class UrlUri implements SourceUri {
/** base URL */
private URL url;

/**
 * Creates instance of the source URI.
 * @param url base URL
 */
public UrlUri(URL url) {
  this.url = url;
}

/**
 * Creates instance of the source URI.
 * @param url base URL
 */
public UrlUri(String url) {
  try {
    this.url = new URL(url);
  } catch (MalformedURLException ex) {
  }
}

/**
 * Gets URL.
 * @return URL or <code>null</code> if no URL
 */
public URL getUrl() {
  return url;
}

@Override
public boolean equals(Object sourceUri) {
  if (url!=null && sourceUri instanceof UrlUri) {
    return url.equals(((UrlUri)sourceUri).url);
  }
  return false;
}

@Override
public int hashCode() {
  return url!=null? url.hashCode(): 0;
}

@Override
public String asString() {
  return url!=null? url.toExternalForm(): "";
}

@Override
public String toString() {
  return url!=null? url.toString(): "";
}
}
