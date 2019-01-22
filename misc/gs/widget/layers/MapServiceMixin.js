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
  "dojo/_base/array",
  "dojo/Deferred",
  "dojo/promise/all",
  "./layerUtil",
  "../util",
  "esri/layers/ArcGISDynamicMapServiceLayer",
  "esri/layers/ArcGISTiledMapServiceLayer",
  "esri/layers/DynamicLayerInfo",
  "esri/layers/ImageParameters",
  "esri/layers/LayerDrawingOptions"],
function(declare, lang, array, Deferred, all, layerUtil, util,
  ArcGISDynamicMapServiceLayer, ArcGISTiledMapServiceLayer, DynamicLayerInfo,
  ImageParameters, LayerDrawingOptions) {

  var _def = declare(null, {

    addMapService: function(serviceUrl,item,itemData) {
      var self = this, dfd = new Deferred();
      var isSingleFeatureLayer = false;
      util.readRestInfo(serviceUrl).then(function(response) {
        if (response && typeof response.type === "string" &&
           (response.type === "Feature Layer" || response.type === "Table")) {
          isSingleFeatureLayer = true;
          return self.addFeatureService(serviceUrl,item,itemData);
        } else {
          var lyr;
          var options = {id: util.generateId()};
          if (response.tileInfo) {
            lyr = new ArcGISTiledMapServiceLayer(serviceUrl, options);
          } else {
            if (response && response.supportedImageFormatTypes &&
                response.supportedImageFormatTypes.indexOf("PNG32") !== -1) {
              options.imageParameters = new ImageParameters();
              options.imageParameters.format = "png32";
            }
            lyr = new ArcGISDynamicMapServiceLayer(serviceUrl, options);
            self._processDynamicLayer(response,lyr,itemData);
          }
          self._waitThenAddDynamicLayer(dfd,lyr,item,itemData);
        }
      }).then(function(result) {
        dfd.resolve(result);
      }).otherwise(function(error) {
        dfd.reject(error);
      });
      return dfd;
    },

    _processDynamicLayer: function(restResponse,layer,itemData) {
      if (!itemData || !itemData.layers || itemData.layers.length === 0) {
        return;
      }
      var expressions = [];
      var dynamicLayerInfo;
      var dynamicLayerInfos = [];
      var drawingOptions;
      var drawingOptionsArray = [];
      var source;
      array.forEach(itemData.layers, function(layerInfo){
        if (layerInfo.layerDefinition && layerInfo.layerDefinition.definitionExpression) {
          expressions[layerInfo.id] = layerInfo.layerDefinition.definitionExpression;
        }
        if (layerInfo.layerDefinition && layerInfo.layerDefinition.source) {
          dynamicLayerInfo = null;
          source = layerInfo.layerDefinition.source;
          if (source.type === "mapLayer") {
            var metaLayerInfos = array.filter(restResponse.layers, function(rlyr) {
              return rlyr.id === source.mapLayerId;
            });
            if (metaLayerInfos.length) {
              dynamicLayerInfo = lang.mixin(metaLayerInfos[0], layerInfo);
            }
          } else {
            dynamicLayerInfo = lang.mixin({}, layerInfo);
          }
          if (dynamicLayerInfo) {
            dynamicLayerInfo.source = source;
            delete dynamicLayerInfo.popupInfo;
            dynamicLayerInfo = new DynamicLayerInfo(dynamicLayerInfo);
            if (itemData.visibleLayers) {
              var vis = ((typeof itemData.visibleLayers) === "string") ?
                itemData.visibleLayers.split(",") : itemData.visibleLayers;
              if (array.indexOf(vis, layerInfo.id) > -1) {
                dynamicLayerInfo.defaultVisibility = true;
              } else {
                dynamicLayerInfo.defaultVisibility = false;
              }
            }
            dynamicLayerInfos.push(dynamicLayerInfo);
          }
        }
        if (layerInfo.layerDefinition && layerInfo.layerDefinition.source &&
            layerInfo.layerDefinition.drawingInfo) {
          drawingOptions = new LayerDrawingOptions(layerInfo.layerDefinition.drawingInfo);
          drawingOptionsArray[layerInfo.id] = drawingOptions;
        }
      });

      if (expressions.length > 0) {
        layer.setLayerDefinitions(expressions);
      }
      if (dynamicLayerInfos.length > 0) {
        layer.setDynamicLayerInfos(dynamicLayerInfos, true);
        if (drawingOptionsArray.length > 0) {
          layer.setLayerDrawingOptions(drawingOptionsArray, true);
        }
      } else {
        //var checkVisibleLayers = true;
      }
    },

    _setDynamicLayerInfoTemplates: function(layer) {
      var self = this, templates = null, dfds = [];

      var readLayer = function(lInfo) {
        var dfd = util.readRestInfo(layer.url + "/" + lInfo.id);
        dfd.then(function(result){
          try {
            var popupInfo = layerUtil.newPopupInfo(result);
            if (popupInfo) {
              templates[lInfo.id] = {
                infoTemplate: layerUtil.newInfoTemplate(popupInfo)
              };
            }
          } catch(exp) {
            console.warn("Error setting popup.");
            console.error(exp);
          }
        });
        return dfd;
      };

      if (layer.infoTemplates === null) {
        array.forEach(layer.layerInfos, function(lInfo) {
          if (templates === null) {
            templates = {};
          }
          if (!lInfo.subLayerIds) {
            dfds.push(readLayer(lInfo));
          }
        });
      }
      if (dfds.length > 0) {
        all(dfds).then(function(){
          if (templates) {
            layer.infoTemplates = templates;
          }
        }).otherwise(function(ex){
          console.warn("Error reading sublayers.");
          console.error(ex);
        });
      }
    },

    _waitThenAddDynamicLayer: function(dfd,lyr,item,itemData) {
      var self = this;
      layerUtil.waitForLayer(this.i18n,lyr).then(function(layer) {
        var templates = null;
        array.forEach(layer.layerInfos, function(layerInfo) {
          var cfgLyr = null;
          if (itemData) {
            array.some(itemData.layers, function(l) {
              if (layerInfo.id === l.id) {
                cfgLyr = l;
                return true;
              }
            });
          }
          var popupInfo = null;
          if (cfgLyr && cfgLyr.popupInfo) {
            popupInfo = cfgLyr.popupInfo;
          }
          if (popupInfo) {
            if (templates === null) {
              templates = {};
            }
            templates[layerInfo.id] = {
              infoTemplate: layerUtil.newInfoTemplate(popupInfo,layerInfo.name)
            };
          }
        });
        if (layer.infoTemplates === null) {
          if (templates) {
            layer.infoTemplates = templates;
          } else {
            self._setDynamicLayerInfoTemplates(layer);
          }
        }
        layerUtil.addMapLayer(self.map,layer,item,self.referenceId);
        dfd.resolve(layer);
      }).otherwise(function(error){
        dfd.reject(error);
      });
    }

  });

  return _def;
});
