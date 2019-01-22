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
  "dojo/Deferred",
  "./layerUtil",
  "../util",
  "esri/request",
  "esri/layers/VectorTileLayer"],
function(declare, Deferred, layerUtil, util, esriRequest, VectorTileLayer) {

  var _def = declare(null, {

    addVectorTileService: function(serviceUrl,item,itemData) {
      var self = this, dfd = new Deferred(), opLayer = {};
      if (typeof serviceUrl !== "string" || serviceUrl.length === 0) {
        dfd.resolve(null);
        return dfd;
      }
      this._checkVectorTileUrl(serviceUrl,opLayer).then(function(url) {
        if ((typeof url === "string") && (url.length > 0)) {
          url = util.checkMixedContent(url);
          var props = {
            id: util.generateId(),
            opacity: 1,
            visible: true
          };
          var lyr = new VectorTileLayer(url,props);
          return layerUtil.waitForLayer(self.i18n,lyr);
        }
      }).then(function(layer) {
        if (layer) {
          layerUtil.addMapLayer(self.map,layer,item,self.referenceId);
        }
        dfd.resolve(layer);
      }).otherwise(function(error) {
        dfd.reject(error);
      });
      return dfd;
    },

    _checkVectorTileUrl: function(url,operationalLayer) {
      var dfd = new Deferred();
      if (util.endsWith(url,".json")) {
        operationalLayer.styleUrl = url;
        dfd.resolve(url);
        return dfd;
      }
      var params = {
        url: null,
        content: {},
        handleAs: "json",
        callbackParamName: "callback"
      };
      if (this.itemUrl) {
        params.url = this.itemUrl + "/resources/styles/root.json";
        esriRequest(params, {}).then(function() {
          operationalLayer.styleUrl = params.url;
          dfd.resolve(params.url);
        }).otherwise(function() {
          params.url = url + "/resources/styles/root.json";
          esriRequest(params, {}).then(function() {
            operationalLayer.styleUrl = params.url;
            dfd.resolve(params.url);
          }).otherwise(function() {
            operationalLayer.url = url;
            dfd.resolve(url);
          });
        });
      } else {
        params.url = url + "/resources/styles/root.json";
        esriRequest(params, {}).then(function() {
          operationalLayer.styleUrl = params.url;
          dfd.resolve(params.url);
        }).otherwise(function() {
          operationalLayer.url = url;
          dfd.resolve(url);
        });
      }
      return dfd;
    }

  });

  return _def;
});
