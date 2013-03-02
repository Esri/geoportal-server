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

import com.esri.gpt.control.livedata.selector.RendererSelector;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of renderer factories.
 */
public class RendererFactories extends ArrayList<IRendererFactory> {

  /** add to map pattern */
  private static Pattern addToMapPattern = Pattern.compile("^resources=map:\\p{Alnum}+@");
  /** base map only renderer */
  private IRenderer basemapOnlyRenderer;

  /**
   * Creates instance of the collection.
   * @param contextPath web application context path
   * @param proxyUrl general use proxy used to carry AJAX calls
   * @param kmzBridge KMZ bridge
   */
  public RendererFactories(String contextPath, String proxyUrl, String kmzBridge) {
    final LiveDataController controller = LiveDataController.getCurrentInstance();

    basemapOnlyRenderer = new BasemapOnlyRenderer() {

      @Override
      protected int getMapHeightAdjustment() {
        return controller.getMapHeightAdjustment();
      }
    };

    LiveDataRendererFactoryBuilder builder = LiveDataRendererFactoryBuilder.newBuilder();
    this.addAll(builder.buildFactories(contextPath, proxyUrl, kmzBridge));
  }

  /**
   * Selects renderer based on live data service URL.
   * @param url live data service URL
   * @return renderer matching live data service URL or <code>null</code> if no renderer found
   */
  public IRenderer select(String url) {
    return select(url, null);
  }

  /**
   * Selects renderer based on live data service URL.
   * @param url live data service URL
   * @param cp credential provider
   * @return renderer matching live data service URL or <code>null</code> if no renderer found
   */
  public IRenderer select(String url, final CredentialProvider cp) {
    IRenderer renderer = null;

    // assert url
    url = url != null && !url.equals("null") ? url.trim() : "";

    // accept 'Add to map' style of the URL
    Matcher addToMapMatcher = addToMapPattern.matcher(url);
    final String serviceUrl = addToMapMatcher.find() ? url.substring(addToMapMatcher.end()) : url;

    Date start = new Date();

    if (serviceUrl.length() == 0) {
      // provide base map only renderer
      renderer = basemapOnlyRenderer;
    } else {
      ApplicationConfiguration appConfig = ApplicationContext.getInstance().getConfiguration();
      StringAttributeMap parameters = appConfig.getCatalogConfiguration().getParameters();
      boolean advPreviewEnabled = Val.chkBool(parameters.getValue("preview.advanced.enabled"), true);

      if (advPreviewEnabled) {
        RendererSelector selector = new RendererSelector(this);
        renderer = selector.select(url, cp);
      }
    }

    Date now = new Date();

    LogUtil.getLogger().log(Level.INFO, "{0} selected after {1} milliseconds for live data preview.", new Object[]{renderer != null ? renderer.toString() : "No any renderer (" + url + ")", now.getTime() - start.getTime()});

    return renderer;
  }
}
