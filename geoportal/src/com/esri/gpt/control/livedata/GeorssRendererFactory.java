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

import com.esri.gpt.control.livedata.selector.HttpRequestDefinition;
import com.esri.gpt.control.livedata.selector.IHttpResponseListener;
import com.esri.gpt.control.livedata.selector.IRegistry;
import com.esri.gpt.control.livedata.selector.ISetter;
import com.esri.gpt.framework.http.ResponseInfo;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Georss renderer factory.
 */
/*packge*/ class GeorssRendererFactory extends MapBasedRendererFactory {

  /** context path */
  private String contextPath = "";
  /** proxy URL */
  private String proxyUrl = "";

  @Override
  public boolean isDefinitive() {
    return true;
  }

  @Override
  public void register(IRegistry reg, final ISetter setter, final String url) {
    reg.register(new HttpRequestDefinition(url), new IHttpResponseListener() {

      public void onResponse(ResponseInfo info, String strContent, Document docContent) {
        if (docContent != null) {
          try {
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            Node ndFeed = (Node) xPath.evaluate("/feed", docContent, XPathConstants.NODE);
            Node ndRss = (Node) xPath.evaluate("/rss", docContent, XPathConstants.NODE);

            if (ndFeed != null || ndRss != null) {
              setter.set(new GeorssRenderer() {

                @Override
                protected String getUrl() {
                  return url;
                }

                @Override
                protected String getProxyUrl() {
                  return contextPath + proxyUrl;
                }

                @Override
                protected int getMapHeightAdjustment() {
                  return getProperties().getMapHeightAdjustment();
                }
              });
            }
          } catch (Exception ex) {
          }
        }
      }
    });
  }

  /**
   * Creates instance of the factory.
   * @param properties properties
   * @param contextPath context path
   * @param proxyUrl proxy URL
   */
  public GeorssRendererFactory(ILiveDataProperties properties, String contextPath, String proxyUrl) {
    super(properties);
    this.contextPath = contextPath != null ? contextPath.trim() : "";
    this.proxyUrl = proxyUrl != null ? proxyUrl.trim() : "";
  }
}
