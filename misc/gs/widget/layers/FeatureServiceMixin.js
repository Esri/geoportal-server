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
  "dojo/_base/array",
  "dojo/promise/all",
  "dojo/Deferred",
  "./layerUtil",
  "../util",
  "esri/lang",
  "esri/dijit/PopupTemplate",
  "esri/layers/FeatureLayer",
  "esri/renderers/jsonUtils"],
function(declare, array, all, Deferred, layerUtil, util, esriLang, PopupTemplate,
  FeatureLayer, jsonRendererUtils) {

  var _def = declare(null, {

    addFeatureService: function(serviceUrl,item,itemData) {
      var self = this, dfd = new Deferred();
      var layerIds = null, layerDfds = [], featureLayers = [];

      util.readRestInfo(serviceUrl).then(function(response) {
        //console.warn("addFeatureService.serviceInfo",response);
        if (response && typeof response.type === "string" &&
           (response.type === "Feature Layer" || response.type === "Table")) {
          // a single layer registered from a service /FeatureServer/1 or /MapServer/2
          var layer = new FeatureLayer(serviceUrl, {
            id: util.generateId(),
            outFields: ["*"]
          });
          layerDfds.push(layerUtil.waitForLayer(self.i18n,layer));
        } else {
          var list = [];
          if (response && response.layers && response.layers.length > 0) {
            array.forEach(response.layers,function(lyr){
              list.push(lyr);
            });
          }
          if (response && response.tables && response.tables.length > 0) {
            array.forEach(response.tables,function(tbl){
              list.push(tbl);
            });
          }
          if (list.length > 0) {
            array.forEach(list, function(lyr) {
              var bAdd = true;
              if (layerIds !== null && layerIds.length > 0) {
                bAdd = array.some(layerIds, function(lid) {
                  return (lid === lyr.id);
                });
              }
              if (bAdd) {
                var layer = new FeatureLayer(serviceUrl + "/" + lyr.id, {
                  id: util.generateId(),
                  outFields: ["*"]
                });
                layerDfds.push(layerUtil.waitForLayer(self.i18n,layer));
              }
            });
          } else {
            // TODO popup a message here?
            console.warn("No layers or tables...");
          }
        }
        return all(layerDfds);

      }).then(function(results) {
        //console.warn("addFeatureService.layerDfds",results);
        array.forEach(results, function(result) {
          featureLayers.push(result);
        });
        featureLayers.reverse();
        return featureLayers;

      }).then(function() {
        array.forEach(featureLayers, function(layer) {
          var opLayer = self._processFeatureLayer(layer,item,itemData);
          if (esriLang.isDefined(opLayer.title)) {
            layer.arcgisProps = {
              title: opLayer.title
            };
            layer._titleForLegend = opLayer.title;
            if (!esriLang.isDefined(layer.title)) {
              layer.title = opLayer.title;
            }
          }
          layerUtil.addMapLayer(self.map,layer,item,self.referenceId);
        });
      }).then(function() {
        dfd.resolve(featureLayers);
      }).otherwise(function(error) {
        console.error(error);
        dfd.reject(error);
      });
      return dfd;
    },

    _makeFeatureLayerTitle: function(pattern,serviceName,layerName) {
      var n, v, regexp;
      try {
        if (serviceName && layerName && (serviceName === layerName)) {
          return serviceName;
        } else if (serviceName && layerName) {
          // try to remove a timestamp suffix
          n = layerName.indexOf(serviceName);
          if (n === 0) {
            v = layerName.substring(n + serviceName.length + 1);
            if (v.length >= 13) {
              regexp = /^\d+$/;
              if (regexp.test(v)) {
                return serviceName;
              }
            }
          }
        }
      } catch (ex) {}
      v = pattern.replace("{serviceName}",serviceName).replace("{layerName}",layerName);
      return v;
    },

    _processFeatureLayer: function(featureLayer,item,itemData) {
      if (!item) return featureLayer;
      var self = this;
      var dlPattern = this.i18n.search.featureLayerTitlePattern;
      var opLayer = null;
      if (itemData && itemData.layers && (itemData.layers.length > 0)) {
        array.some(itemData.layers, function(info) {
          var layerDefinition, jsonRenderer, renderer, isCustomTemplate = false;
          var popInfo, jsonPopInfo, infoTemplate;
          if (info.id === featureLayer.layerId) {
            //console.warn("layerInfo",info);
            if (info.popupInfo) {
              popInfo = info.popupInfo;
              jsonPopInfo = JSON.parse(JSON.stringify(popInfo));
              infoTemplate = new PopupTemplate(jsonPopInfo);
              featureLayer.setInfoTemplate(infoTemplate);
              isCustomTemplate = true;
            }
            if (esriLang.isDefined(info.showLabels)) {
              featureLayer.setShowLabels(info.showLabels);
            }
            if (esriLang.isDefined(info.refreshInterval)) {
              featureLayer.setRefreshInterval(info.refreshInterval);
            }
            if (esriLang.isDefined(info.showLegend)) {
              // TODO?
              console.log("");
            }
            if (esriLang.isDefined(info.timeAnimation)) {
              if (info.timeAnimation === false) {
                // TODO?
                console.log("");
              }
            }
            layerDefinition = info.layerDefinition;
            if (layerDefinition) {
              if (layerDefinition.definitionExpression) {
                featureLayer.setDefinitionExpression(layerDefinition.definitionExpression);
              }
              if (layerDefinition.displayField) {
                featureLayer.displayField(layerDefinition.displayField);
              }
              if (layerDefinition.drawingInfo) {
                if (layerDefinition.drawingInfo.renderer) {
                  jsonRenderer = JSON.parse(
                    JSON.stringify(layerDefinition.drawingInfo.renderer)
                  );
                  renderer = jsonRendererUtils.fromJson(jsonRenderer);
                  if (jsonRenderer.type && (jsonRenderer.type === "classBreaks")) {
                    renderer.isMaxInclusive = true;
                  }
                  featureLayer.setRenderer(renderer);
                }
                if (esriLang.isDefined(layerDefinition.drawingInfo.transparency)) {
                  // TODO validate before setting?
                  featureLayer.setOpacity(1 - (layerDefinition.drawingInfo.transparency / 100));
                }
              }
              if (esriLang.isDefined(layerDefinition.minScale)) {
                featureLayer.setMinScale(layerDefinition.minScale);
              }
              if (esriLang.isDefined(layerDefinition.maxScale)) {
                featureLayer.setMaxScale(layerDefinition.maxScale);
              }
              if (esriLang.isDefined(layerDefinition.defaultVisibility)) {
                if (layerDefinition.defaultVisibility === false) {
                  featureLayer.setVisibility(false); // TODO?
                }
              }
            }
            if (!isCustomTemplate) {
              self._setFeatureLayerInfoTemplate(featureLayer,info.popupInfo);
            }
            opLayer = {
              url: featureLayer.url,
              id: featureLayer.id,
              itemId: item.id,
              title: self._makeFeatureLayerTitle(dlPattern,item.title,featureLayer.name)
            };
            return true;
          }
        });
        return opLayer;

      } else {
        opLayer = {
          url: featureLayer.url,
          id: featureLayer.id,
          itemId: item.id,
          title: self._makeFeatureLayerTitle(dlPattern,item.title,featureLayer.name)
        };
        self._setFeatureLayerInfoTemplate(featureLayer,null,opLayer.title);
        return opLayer;
      }
    },

    _setFeatureLayerInfoTemplate: function(featureLayer,popupInfo,title) {
      if (!popupInfo) {
        popupInfo = layerUtil.newPopupInfo(featureLayer,title);
      }
      var template = layerUtil.newInfoTemplate(popupInfo,title);
      featureLayer.setInfoTemplate(template);
    }

  });

  return _def;

});
