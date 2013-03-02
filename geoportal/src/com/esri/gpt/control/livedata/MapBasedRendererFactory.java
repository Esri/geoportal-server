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
package com.esri.gpt.control.livedata;

import com.esri.gpt.control.livedata.selector.IRegistry;
import com.esri.gpt.control.livedata.selector.ISetter;
import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.http.HttpClient401Exception;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.XmlHandler;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;

/**
 * Map-based renderer factory.
 */
/*packge*/ abstract class MapBasedRendererFactory implements IRendererFactory {
  protected ILiveDataProperties properties;

  /**
   * Creates instance of the factory.
   * @param properties properties
   */
  public MapBasedRendererFactory(ILiveDataProperties properties) {
    this.properties = properties;
  }

  public boolean isDefinitive() {
    return false;
  }

  public void register(IRegistry reg, ISetter setter, String url) {
  }

  /**
   * Gets properties.
   * @return properties
   */
  protected ILiveDataProperties getProperties() {
    return properties;
  }

  /**
   * Access initial information from the host
   * @param url URL
   * @return capabilities
   * @throws javax.xml.parsers.ParserConfigurationException
   * @throws HttpClient401Exception if user not authenticated
   */
  protected Document accessHost(String url)
    throws ParserConfigurationException, HttpClient401Exception {
    return accessHost(url, null);
  }

  /**
   * Access initial information from the host
   * @param url URL
   * @param cp credential provider
   * @return capabilities
   * @throws javax.xml.parsers.ParserConfigurationException
   */
  protected Document accessHost(String url, CredentialProvider cp)
    throws ParserConfigurationException, HttpClient401Exception {

    // read the response from the targeted server
    InputStream responseStream = null;
    InputStream wrappedStream = null;
    try {

      HttpClientRequest cr = new HttpClientRequest();
      cr.setUrl(url);
      XmlHandler sh = new XmlHandler(false);
      cr.setContentHandler(sh);
      cr.setCredentialProvider(cp != null ? cp : CredentialProvider.getThreadLocalInstance());
      cr.execute();

      return sh.getDocument();
    } catch (IllegalArgumentException ex) {
      LogUtil.getLogger().log(Level.FINER, "Error accessing host: " + url, ex);
      return null;
    } catch (IOException ex) {
      LogUtil.getLogger().log(Level.FINER, "Error accessing host: " + url, ex);
      return null;
    } finally {
      try {
        if (wrappedStream != null) {
          wrappedStream.close();
        }
      } catch (Exception ef) {
      }
      try {
        if (responseStream != null) {
          responseStream.close();
        }
      } catch (Exception ef) {
      }
    }
  }

  /**
   * Query string reprezentation.
   */
  protected static class Query extends TreeMap<String, String> {

    /**
     * Creates instance of the query.
     */
    public Query() {
      super(String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * Creates instance of the query.
     * @param query template query
     */
    public Query(Query query) {
      super(String.CASE_INSENSITIVE_ORDER);
      if (query != null) {
        this.putAll(query);
      }
    }

    /**
     * Creates instance of the query.
     * @param queryString query string
     */
    public Query(String queryString) {
      super(String.CASE_INSENSITIVE_ORDER);
      for (String param : Val.chkStr(queryString).split("&")) {
        add(param);
      }
    }

    /**
     * Adds a single parameter.
     * Parameter is a string in the format of: <key>=<value>.
     * @param param parameter to add.
     */
    public void add(String param) {
      param = Val.chkStr(param);
      int eqSignIdx = param.indexOf('=');
      if (eqSignIdx > 0) {
        String key = param.substring(0, eqSignIdx);
        String value = param.substring(eqSignIdx + 1);
        put(key, value);
      } else if (eqSignIdx == 0) {
        // do nothing; param with no name
      } else if (param.length() > 0) {
        put(param, "");
      }
    }

    /**
     * Mixes two queries.
     * @param query query to mix with this query
     * @return mixed query
     */
    public Query mixin(Query query) {
      Query mixedQuery = new Query(this);
      if (query != null) {
        for (Map.Entry<String, String> entry : query.entrySet()) {
          mixedQuery.put(entry.getKey(), entry.getValue());
        }
      }
      return mixedQuery;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<String, String> entry : this.entrySet()) {
        if (sb.length() > 0) {
          sb.append("&");
        }
        sb.append(entry.getKey() + "=" + entry.getValue());
      }
      return sb.toString();
    }
  }
}
