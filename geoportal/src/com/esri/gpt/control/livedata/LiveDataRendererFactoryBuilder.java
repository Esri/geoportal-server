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

import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Live Data Renderer factory builder.
 */
public class LiveDataRendererFactoryBuilder {

  /**
   * Creates instance of the new builder.
   * @return instance of builder
   */
  public static LiveDataRendererFactoryBuilder newBuilder() {

    // initialize
    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();
    CatalogConfiguration catCfg = appCfg.getCatalogConfiguration();

    // look for a configured class name for the resource link builder
    String className = Val.chkStr(catCfg.getParameters().getValue("liveDataRendererFactoryBuilder"));

    // instantiate the builder
    if (className.length() == 0) {
      return new LiveDataRendererFactoryBuilder();
    } else {
    try {
      Class<?> cls = Class.forName(className);
      Object obj = cls.newInstance();
      if (obj instanceof LiveDataRendererFactoryBuilder) {
        LiveDataRendererFactoryBuilder builder = (LiveDataRendererFactoryBuilder) obj;
        return builder;
      } else {
        String sMsg = "The configured liveDataRendererFactoryBuilder parameter is invalid: "+className;
        throw new ConfigurationException(sMsg);
      }
    } catch (ConfigurationException t) {
      throw t;
    } catch (Throwable t) {
      String sMsg = "Error instantiating liveDataRendererFactoryBuilder: "+className;
      throw new ConfigurationException(sMsg, t);
    }
    }

  }

  /**
   * Builds collection of factories.
   * @param contextPath context path
   * @param proxyUrl proxy URL
   * @return collection of instances of factories
   * @param kmzBridge KMZ bridge
   */
  public Collection<IRendererFactory> buildFactories(String contextPath, String proxyUrl, String kmzBridge) {
    ArrayList<IRendererFactory> factories = new ArrayList<IRendererFactory>();
    final LiveDataController controller = LiveDataController.getCurrentInstance();

    factories.add(new ArcGISRendererFactory(controller));
    factories.add(new ArcIMSRendererFactory(controller,contextPath, proxyUrl));
    factories.add(new WMSRendererFactory(controller,contextPath, proxyUrl));
    factories.add(new GeorssRendererFactory(controller,contextPath, proxyUrl));
    factories.add(new KmlRendererFactory(controller,contextPath, kmzBridge));
    factories.add(new SosRendererFactory(controller,contextPath, proxyUrl));
    factories.add(new FrameRendererFactory());
    factories.add(new LinkRendererFactory());
    factories.add(new FlashVideoRendererFactory());

    return factories;
  }
}
