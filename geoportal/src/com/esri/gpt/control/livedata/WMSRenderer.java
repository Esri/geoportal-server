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

/**
 * WMS renderer.
 */
/*packge*/ abstract class WMSRenderer extends MapBasedRenderer {

  protected abstract String getProxyUrl();

  protected abstract String getUrl();

  @Override
  protected String newLayerDeclaration() {
    return "new esri.gpt.layers.WMSLayer(\"" +getUrl()+ "\",\"" +getProxyUrl()+ "\",geometryService)";
  }

  @Override
  protected String initializeNewLayer() {
    return "node.liveDataMap.addLayer(node.liveDataLayer);";
  }

  @Override
  protected String finalizeNewLayer() {
    return "node.liveDataMap.addLayer(node.liveDataLayer);";
  };

  @Override
  public String toString() {
    return WMSRenderer.class.getSimpleName() + "("+getUrl()+")";
  }

}
