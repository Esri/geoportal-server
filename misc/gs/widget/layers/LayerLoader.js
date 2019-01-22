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
  "dojo/Deferred",
  "./FeatureServiceMixin",
  "./ImageServiceMixin",
  "./KMLMixin",
  "./MapServiceMixin",
  "./VectorTileServiceMixin",
  "./WMSMixin",
  "./layerUtil",
  "../util"],
function(declare, lang, Deferred, FeatureServiceMixin, ImageServiceMixin,
  KMLMixin, MapServiceMixin, VectorTileServiceMixin, WMSMixin, layerUtil, util) {

  var _def = declare([FeatureServiceMixin,ImageServiceMixin,KMLMixin,
                      MapServiceMixin,VectorTileServiceMixin,WMSMixin], {

    i18n: null,
    itemUrl: null,
    map: null,

    constructor: function(args) {
      lang.mixin(this, args);
    },

    addItem: function(serviceType,serviceUrl,item,itemUrl) {
      var self = this, dfd = new Deferred();
      itemUrl = this.itemUrl = util.checkMixedContent(itemUrl);
      this._readItemJsonData(serviceType,itemUrl).then(function(itemData) {
        return self.addLayer(serviceType,serviceUrl,item,itemData);
      }).then(function(result) {
        dfd.resolve(result);
      }).otherwise(function(error) {
        console.error(error);
        dfd.reject(error);
      });
      return dfd;
    },

    addLayer: function(serviceType,serviceUrl,item,itemData) {
      // "Vector Tile Service" KML
      serviceUrl = util.checkMixedContent(serviceUrl);
      if (serviceType === "Feature Service") {
        return this.addFeatureService(serviceUrl,item,itemData);
      } else if (serviceType === "Image Service") {
        return this.addImageService(serviceUrl,item,itemData);
      } else if (serviceType === "Map Service") {
        return this.addMapService(serviceUrl,item,itemData);
      } else if (serviceType === "Vector Tile Service") {
        return this.addVectorTileService(serviceUrl,item,itemData);
      } else if (serviceType === "WMS") {
        return this.addWMS(serviceUrl,item,itemData);
      } else if (serviceType === "KML") {
        return this.addKML(serviceUrl,item,itemData);
      } else {
        // TODO not supported message?
        var dfd = new Deferred();
        dfd.resolve(null);
        return dfd;
      }
    },

    _readItemJsonData: function(serviceType,itemUrl) {
      var types = ["Feature Service","Image Service","Map Service","WMS"];
      if (types.indexOf(serviceType) !== -1) {
        return util.readItemJsonData(itemUrl);
      } else {
        var dfd = new Deferred();
        dfd.resolve(null);
        return dfd;
      }
    },

  });

  return _def;
});
