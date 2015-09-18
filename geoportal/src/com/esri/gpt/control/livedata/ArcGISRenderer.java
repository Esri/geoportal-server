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

import static com.esri.gpt.framework.util.Val.stripHttpProtocol;

/**
 * ARCGIS Renderer.
 */
/*packge*/ abstract class ArcGISRenderer extends MapBasedRenderer {

  protected abstract String getUrl();

  protected abstract boolean isImageService();
  
  protected abstract String getLayerId();

  @Override
  protected String newLayerDeclaration() {
    if (isImageService()) {
      return "new esri.layers.ArcGISImageServiceLayer(\"" +stripHttpProtocol(getUrl())+ "\")";
    } else {
      return "new esri.layers.ArcGISDynamicMapServiceLayer(\"" +stripHttpProtocol(getUrl())+ "\")";
    }
  }


  @Override
  protected String finalizeNewLayer() {
    if (getLayerId()!=null) {
      return "node.liveDataLayer.setVisibleLayers([" +getLayerId()+ "]);";
    } else {
      return "";
    }
  };
  
  @Override
  protected boolean generateBaseMap() {
    return false;
  }

  @Override
  public String toString() {
    return ArcGISRenderer.class.getSimpleName() + "("+getUrl()+")";
  }
}
