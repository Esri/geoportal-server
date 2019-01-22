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
  "./WidgetContext"],
function(declare, Deferred, WidgetContext) {

  var _def = declare([WidgetContext], {

    supportsRemove: false,

    addItem: function(serviceType,serviceUrl,item,itemUrl,referenceId) {
      //return this.addLayer(serviceType,serviceUrl,referenceId);
      var dfd = new Deferred();
      try {
        //alert(serviceType+"\r\n"+serviceUrl+"\r\n"+referenceId);
        if (window && window.external && window.external.gsHasListener) {
          window.external.gsAddItem(serviceType,serviceUrl,item.id,referenceId,
            function(result){
              dfd.resolve(result);
            }
          );
        } else {
          dfd.resolve();
        }
      } catch(ex) {
        console.error(ex);
        alert(ex);
        dfd.reject(ex);
      }
      return dfd;
    },

    addLayer: function(serviceType,serviceUrl,referenceId) {
      var dfd = new Deferred();
      try {
        //alert(serviceType+"\r\n"+serviceUrl+"\r\n"+referenceId);
        if (window && window.external && window.external.gsHasListener) {
          window.external.gsAddLayer(serviceType,serviceUrl,referenceId,
            function(result){
              dfd.resolve(result);
            }
          );
        } else {
          dfd.resolve();
        }
      } catch(ex) {
        console.error(ex);
        alert(ex);
        dfd.reject(ex);
      }
      return dfd;
    },

    getGeographicExtent: function() {
      try {
        if (window && window.external && window.external.gsHasListener) {
          var bbox = window.external.gsGetGeographicExtent();
          if (typeof bbox === "string" && bbox.indexOf("{") === 0) {
            bbox = JSON.parse(bbox);
            return bbox;
          }
        }
      } catch(ex) {
        console.error(ex);
      }
      return null;
    },

    getMap: function() {
      return null;
    }

  });

  return _def;
});
