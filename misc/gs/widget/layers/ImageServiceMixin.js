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
  "esri/lang",
  "esri/layers/ArcGISImageServiceLayer",
  "esri/layers/ImageServiceParameters",
  "esri/layers/MosaicRule",
  "esri/layers/RasterFunction"],
function(declare, Deferred, layerUtil, util, esriLang, ArcGISImageServiceLayer,
  ImageServiceParameters, MosaicRule, RasterFunction) {

  var _def = declare(null, {

    addImageService: function(serviceUrl,item,itemData) {
      var dfd = new Deferred();
      var mapLayerId = util.generateId();
      var layerUrl = serviceUrl;
      var layerObject = {
        mapLayerId: mapLayerId,
        bandIds: null,
        format: null,
        compressionQuality: null,
        opacity: 1.0,
        visibility: true
      };
      itemData = itemData || {};

      if (esriLang.isDefined(itemData.visibility) && itemData.visibility === false) {
        layerObject.visibility = false; // TODO?
      }
      if (esriLang.isDefined(itemData.opacity)) {
        layerObject.opacity = itemData.opacity;
      }
      if (esriLang.isDefined(itemData.minScale) &&
         !esriLang.isDefined(layerObject.minScale)) {
        layerObject.minScale = itemData.minScale;
      }
      if (esriLang.isDefined(itemData.maxScale) &&
         !esriLang.isDefined(layerObject.maxScale)) {
        layerObject.maxScale = itemData.maxScale;
      }
      if (esriLang.isDefined(itemData.refreshInterval) &&
         !esriLang.isDefined(layerObject.refreshInterval)) {
        layerObject.refreshInterval = itemData.refreshInterval;
      }
      if (itemData.popupInfo && !layerObject.popupInfo && !layerObject.disablePopup) {
        layerObject.popupInfo = itemData.popupInfo;
      }
      if (itemData.renderingRule && !layerObject.renderingRule) {
        layerObject.renderingRule = itemData.renderingRule;
        if (itemData.renderingRule.functionName) {
          layerObject.renderingRule.rasterFunction = itemData.renderingRule.functionName;
        }
      }
      if (itemData.bandIds && !layerObject.bandIds) {
        layerObject.bandIds = itemData.bandIds;
      }
      if (itemData.mosaicRule && !layerObject.mosaicRule) {
        layerObject.mosaicRule = itemData.mosaicRule;
      }
      if (itemData.format && !layerObject.format) {
        layerObject.format = itemData.format;
      }
      if (esriLang.isDefined(itemData.compressionQuality) &&
         !esriLang.isDefined(layerObject.compressionQuality)) {
        layerObject.compressionQuality = itemData.compressionQuality;
      }
      if (itemData.layerDefinition && itemData.layerDefinition.definitionExpression &&
         (!esriLang.isDefined(layerObject.layerDefinition) ||
          !esriLang.isDefined(layerObject.layerDefinition.definitionExpression))) {
        layerObject.layerDefinition = layerObject.layerDefinition || {};
        layerObject.layerDefinition.definitionExpression =
          itemData.layerDefinition.definitionExpression;
      }

      var imageServiceParameters = new ImageServiceParameters();
      //imageServiceParameters.bandIds = layerObject.bandIds;
      if (layerObject.bandIds !== null) {
        imageServiceParameters.bandIds = layerObject.bandIds;
      }
      if (layerObject.format !== null) {
        imageServiceParameters.format = layerObject.format;
        if (layerObject.compressionQuality !== null) {
          imageServiceParameters.compressionQuality = layerObject.compressionQuality;
        }
      }
      if (layerObject.renderingRule && layerObject.renderingRule.rasterFunction) {
        var rasterFunction = new RasterFunction(layerObject.renderingRule);
        imageServiceParameters.renderingRule = rasterFunction;
      }
      if (layerObject.mosaicRule) {
        var mosaicRule = new MosaicRule(layerObject.mosaicRule);
        imageServiceParameters.mosaicRule = mosaicRule;
      }
      if (esriLang.isDefined(layerObject.noData)) {
        imageServiceParameters.noData = layerObject.noData;
      }
      if (esriLang.isDefined(layerObject.noDataInterpretation)) {
        imageServiceParameters.noDataInterpretation = layerObject.noDataInterpretation;
      }
      if (esriLang.isDefined(layerObject.interpolation)) {
        imageServiceParameters.interpolation = layerObject.interpolation;
      }

      var props = {
        imageServiceParameters: imageServiceParameters,
        opacity: layerObject.opacity,
        visible: layerObject.visibility
      };
      if (esriLang.isDefined(layerObject.mapLayerId)) {
        props.id = layerObject.mapLayerId;
      }
      if (esriLang.isDefined(layerObject.minScale)) {
        props.minScale = layerObject.minScale;
      }
      if (esriLang.isDefined(layerObject.maxScale)) {
        props.maxScale = layerObject.maxScale;
      }
      if (esriLang.isDefined(layerObject.refreshInterval)) {
        props.refreshInterval = layerObject.refreshInterval;
      }
      if (esriLang.isDefined(layerObject.resourceInfo)) {
        props.resourceInfo = layerObject.resourceInfo;
      }

      var lyr = new ArcGISImageServiceLayer(layerUtil.checkUrl(layerUrl),props);
      layerUtil.waitForLayer(this.i18n,lyr).then(function(layer){
        if (layerObject.layerDefinition && layerObject.layerDefinition.definitionExpression) {
          layer.setDefinitionExpression(layerObject.layerDefinition.definitionExpression,true);
        }
        // TODO setInfoTemplate
        //if (!options.ignorePopups && layerObject.popupInfo) {
        //  layer.setInfoTemplate(new clazz(layerObject.popupInfo));
        //}
        /*
        rasterUtil.populateLayerWROInfo(layer,true).then(
          function(){dfd.resolve(layer);},
          function(error2){dfd.reject(error2);}
        );
        */
        layerUtil.addMapLayer(self.map,layer,item,self.referenceId);
        dfd.resolve(layer);
      }).otherwise(function(error){
        dfd.reject(error);
      });
      return dfd;
    }

  });

  return _def;
});
