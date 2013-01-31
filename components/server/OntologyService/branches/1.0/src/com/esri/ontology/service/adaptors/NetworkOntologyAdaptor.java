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
package com.esri.ontology.service.adaptors;

import com.esri.ontology.service.control.Parametralizable;
import com.esri.ontology.service.util.Val;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;

/**
 * Network ontology adaptor.
 */
public class NetworkOntologyAdaptor extends BaseOntologyAdaptor {

  /** base URl */
  private String baseUrl = "";

  /**
   * Initializes adaptor.
   * <p/>
   * Initialization parameters:
   * <ul>
   * <li>baseUrl - ontology servlet URL [mandatory]</li>
   * </ul>
   * @param properties properties
   * @see BaseOntologyAdaptor#init
   */
  @Override
  public void init(Properties properties) {
    super.init(properties);
    baseUrl = properties.getProperty("baseUrl", "");
  }

  public String parse(String term) throws OntologyAdaptorException {
    try {

      String sUrl = getUrl(term);
      if (sUrl.length() > 0) {
        URL url = new URL(sUrl);
        InputStream in = url.openStream();

        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        String line = null;

        while ((line = r.readLine()) != null) {
          if (sb.length() > 0) {
            sb.append("\r\n");
          }
          sb.append(line);
        }

        return sb.toString();
      } else {
        return "";
      }

    } catch (Exception ex) {
      throw new OntologyAdaptorException("Convertion to lucene failed.", ex);
    }
  }

  /**
   * Gets connection URL.
   * @param term term to search
   * @return connection URL
   */
  private String getUrl(String term) {
    term = Val.chkStr(term);
    if (baseUrl.length() > 0 && term.length() > 0) {
      StringBuilder sb = new StringBuilder();
      if (baseUrl.endsWith("/")) {
        sb.append(baseUrl.substring(0,baseUrl.length()-1));
      } else {
        sb.append(baseUrl);
      }
      sb.append("?term=" + encode(term));
      String params = getParams();
      if (params.length() > 0) {
        sb.append("&" + params);
      }
      return sb.toString();
    }
    return "";
  }

  /**
   * Gets param sequence for the URL.
   * @return param sequence
   */
  private String getParams() {
    StringBuilder sb = new StringBuilder();

    appendParametralizable(sb, queryCriteria);
    appendParametralizable(sb, selection);
    appendParametralizable(sb, format);

    return sb.toString();
  }

  /**
   * Appends any parametralizable to the string builder.
   * @param sb string builder
   * @param p parametralizable
   */
  private void appendParametralizable(StringBuilder sb, Parametralizable p) {
    String parameter = p.toParameter();
    if (parameter.length() > 0) {
      if (sb.length() > 0) {
        sb.append("&");
      }
      sb.append(parameter);
    }
  }

  /**
   * Encodes string into URL-friendly string.
   * @param s string to encode
   * @return encoded string
   */
  private String encode(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      return s;
    }
  }
}
