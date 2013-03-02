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

import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.http.HttpClient401Exception;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.StringHandler;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;

/**
 * Request dispatcher.
 */
class HttpRequestDispatcher {

  private static final Logger LOGGER = Logger.getLogger(HttpRequestDispatcher.class.getCanonicalName());
  private final Object lock;
  private CredentialProvider cp;
  private HttpRequestListenerMap map;

  /**
   * Creates request dispatcher.
   * @param lock object to hold the lock
   * @param map listeners map
   * @param cp credential provider
   */
  public HttpRequestDispatcher(Object lock, HttpRequestListenerMap map, CredentialProvider cp) {
    this.lock = lock;
    this.map = map;
    this.cp = cp;
  }

  /**
   * Dispatches request to all URL's
   */
  public void dispatch() {
    if (map.size() > 0) {
      Senders senders = new Senders();
      final Thread[] tSenders = new Thread[map.size()];
      int i = 0;
      for (Map.Entry<HttpRequestDefinition, HttpResponseListenerArray> e : map.entrySet()) {
        Sender sender = new Sender(senders, e.getKey(), e.getValue());
        senders.add(sender);
        tSenders[i] = new Thread(sender);
        tSenders[i].setDaemon(true);
        i++;
      }
      Thread bootstrap = new Thread(new Runnable() {

        public void run() {
          synchronized (lock) {
            for (Thread t : tSenders) {
              t.start();
            }
          }
        }
      });
      bootstrap.setDaemon(true);
      bootstrap.start();
    } else {
      Thread bootstrap = new Thread(new Runnable() {

        public void run() {
          synchronized (lock) {
            lock.notifyAll();
          }
        }
      });
      bootstrap.setDaemon(true);
      bootstrap.start();
    }
  }

  /**
   * Called when unauthorized access.
   */
  protected void onUnauthorizedException() {
  }

  /**
   * Array of senders.
   */
  private class Senders extends ArrayList<Sender> {

    public boolean remove(Sender o) {
      boolean status = false;
      synchronized (this) {
        status = super.remove(o);
      }
      if (size() == 0) {
        synchronized (lock) {
          lock.notifyAll();
        }
      }
      return status;
    }
  }

  /**
   * Sender.
   */
  private class Sender implements Runnable {

    private Senders senders;
    private HttpRequestDefinition httpReqDef;
    private HttpResponseListenerArray array;

    /**
     * Creates instance of the sender.
     * @param senders array of senders
     * @param httpReqDef HTTP request definition
     * @param array array of listeners
     */
    public Sender(Senders senders, HttpRequestDefinition httpReqDef, HttpResponseListenerArray array) {
      this.senders = senders;
      this.httpReqDef = httpReqDef;
      this.array = array;
    }

    public void run() {
      try {
        StringHandler sh = new StringHandler();
        HttpClientRequest request = new HttpClientRequest();

        request.setUrl(httpReqDef.getUrl());
        request.setContentProvider(httpReqDef.getContentProvider());
        request.setCredentialProvider(cp);
        request.setContentHandler(sh);

        request.execute();

        if (request.getResponseInfo().getResponseCode() == HttpURLConnection.HTTP_OK) {
          String strContent = Val.chkStr(sh.getContent());
          Document docContent = null;
          String contentType = Val.chkStr(request.getResponseInfo().getContentType());

          try {
            if (strContent.length() > 0 && (contentType.toLowerCase().contains("xml") || strContent.startsWith("<?xml"))) {
              docContent = DomUtil.makeDomFromString(Val.removeBOM(strContent), false);
            }
          } catch (Exception ex) {
            LOGGER.log(Level.FINER, "Error converting HTPP response into DOM.", ex);
          }

          array.onResponse(request.getResponseInfo(), strContent, docContent);
        }
      } catch (HttpClient401Exception ex) {
        onUnauthorizedException();
      } catch (IOException ex) {
        LOGGER.log(Level.FINER, "Error submiting HTTP request.", ex);
      } finally {
        senders.remove(this);
      }
    }
  }
}
