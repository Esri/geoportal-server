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

import com.esri.gpt.control.livedata.IRenderer;
import com.esri.gpt.control.livedata.IRendererFactory;
import com.esri.gpt.control.livedata.LoginDlgRenderer;
import com.esri.gpt.control.livedata.selector.HttpRequestListenerMap.ISelector;
import com.esri.gpt.framework.http.CredentialProvider;
import java.util.Collection;

/**
 * Renderer selector.
 */
public class RendererSelector {

  private Collection<IRendererFactory> factories;

  /**
   * Creates instance of the selector.
   * @param factories renderer factories
   */
  public RendererSelector(Collection<IRendererFactory> factories) {
    this.factories = factories;
  }

  /**
   * Selects factory for the given URL.
   * @param url URL
   * @param cp credential provider or <code>null</code>
   * @return renderer or <code>null</code> if no renderer found
   */
  public IRenderer select(String url, CredentialProvider cp) {
    IRenderer renderer = null;
    final Setters setters = new Setters();
    HttpRequestListenerMap map = new HttpRequestListenerMap();
    HttpRequestListenerMap exactMap = new HttpRequestListenerMap();

    for (IRendererFactory rf : factories) {
      rf.register(map, rf.isDefinitive() ? setters.getDefinitiveSetter() : setters.getNonDefinitiveSetter(), url);
      if (rf.isDefinitive()) {
        rf.register(exactMap, setters.getDefinitiveSetter(), url);
      }
    }

    final String exactUrl = url;
    exactMap = exactMap.select(new ISelector() {
      public boolean eligible(HttpRequestDefinition httpReqDef) {
        return httpReqDef.getUrl().equalsIgnoreCase(exactUrl);
      }
    });

    HttpRequestDispatcher exactDisp = new HttpRequestDispatcher(setters, exactMap, cp) {
      @Override
      protected void onUnauthorizedException() {
        setters.getDefinitiveSetter().set(new LoginDlgRenderer());
      }
    };
    synchronized (setters) {
      exactDisp.dispatch();
      try {
        setters.wait(30000);
      } catch (InterruptedException ex) {
      }
      renderer = setters.getRenderer();
    }
    if (renderer == null) {
      HttpRequestDispatcher disp = new HttpRequestDispatcher(setters, map, cp) {
        @Override
        protected void onUnauthorizedException() {
          setters.getDefinitiveSetter().set(new LoginDlgRenderer());
        }
      };
      synchronized (setters) {
        disp.dispatch();
        try {
          setters.wait(30000);
        } catch (InterruptedException ex) {
        }
        renderer = setters.getRenderer();
      }
    }
    return renderer;
  }
}
