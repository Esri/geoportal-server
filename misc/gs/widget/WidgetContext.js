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
define(["dojo/_base/declare",
  "dojo/_base/lang",
  "./layers/LayerLoader"],
function(declare, lang, LayerLoader) {

  var _def = declare([], {

    i18n: null,
    map: null,
    proxyUrl: null,
    supportsRemove: true,
    widgetConfig: null,
    widgetFolder: "gs/widget",

    constructor: function(args) {
      lang.mixin(this, args);
    },

    addItem: function(serviceType,serviceUrl,item,itemUrl,referenceId) {
      var layerLoader = new LayerLoader({
        i18n: this.i18n,
        map: this.getMap(),
        referenceId: referenceId
      });
      return layerLoader.addItem(serviceType,serviceUrl,item,itemUrl);
    },

    addLayer: function(serviceType,serviceUrl,referenceId) {
      var layerLoader = new LayerLoader({
        i18n: this.i18n,
        map: this.getMap(),
        referenceId: referenceId
      });
      return layerLoader.addLayer(serviceType,serviceUrl);
    },

    getGeographicExtent: function() {
      if (this.map) {
        return this.map.geographicExtent;
      }
    },

    getMap: function() {
      return this.map;
    },

    showError: function(title,error) {
      console.warn("wro/Context.showError",title,error);
    },

    showMessage: function(title,message) {
      console.warn("wro/Context.showMessage",title,message);
    },

    showMessages: function(title,subTitle,messages) {
      console.warn("wro/Context.showMessages",title,subTitle,messages);
    }

  });

  return _def;
});
