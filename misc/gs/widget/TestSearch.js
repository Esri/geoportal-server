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
  "require",
  "dijit/_WidgetBase",
  "dijit/_TemplatedMixin",
  "dijit/_WidgetsInTemplateMixin",
  "dojo/text!./templates/TestSearch.html",
  "../all",],
function(declare, localRequire, _WidgetBase, _TemplatedMixin,
  _WidgetsInTemplateMixin, template) {

  /*

  {"f":"json","target":"arcgis"}

  {"f":"json","target":"cswB"}
  {"f":"json","target":"cswA"}
  {"f":"json","target":["arcgis","gptdb1"]}
  {"f":"json","target":["gptdb1","cswA"]}

  {"f":"json","target":["arcgis","gptdb1"]}

  {
    "f":"json",
    "target":[
      {
        "key":"abc",
        "type":"csw",
        "url":"http://gptdb1.esri.com:8080/geoportal/csw?service=CSW&request=GetRecords"
      }
    ]
  }

  {
    "f":"json",
    "target":[
      {
        "key":"abc",
        "type":"csw",
        "cswVersion": "2.0.2",
        "url":"https://gptogc.esri.com/geoportal/csw?service=CSW&request=GetRecords"
      }
    ]
  }

  {
    "f":"json",
    "target":[
      {
        "key":"abc",
        "type":"geoportal",
        "profile": "sampleProfile1",
        "url":"http://gptdb2.esri.com:8080/geoportal/elastic/img/item/_search"
      }
    ]
  }

  */

  var _def = declare([_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin], {

    templateString: template,

    _wasLoaded: false,

    postCreate: function() {
      this.inherited(arguments);
      var self = this;
      gs.reqAll(localRequire,function(){
        self._wasLoaded = true;
        //console.log("Search._wasLoaded",self._wasLoaded);
      });
    },

    informExternal: function(text) {
      try {
        if (window && window.external && window.external.gsHasListener) {
          window.external.gsListener(text);
        }
      } catch(ex) {
        console.error(ex);
        //alert(ex);
      }
    },

    search: function() {
      if (!this._wasLoaded) return; // TODO need a ui error message
      var self = this;
      try {
        this.resultTextarea.value = "Searching...";

        var parameterMap = {};
        var sParams = this.requestTextarea.value.trim();
        if (sParams.length > 0) {
          try {
            var params = JSON.parse(sParams);
            parameterMap = params;
            //console.log("params",params);
          } catch(ex) {
            this.resultTextarea.value = ""+ex;
            return;
          }
        }

        var requestInfo = {
          "requestUrl": "/request",
          "baseUrl": "/base",
          "headerMap": {},
          "parameterMap": parameterMap
        };
        //requestInfo.requestUrl = "http://www.geoportal.com/geoportal/opensearch/description";

        var processor = gs.Object.create(gs.context.browser.WebProcessor).mixin({
          newConfig: function() {
            var config = gs.Object.create(gs.config.Config);
            config.proxyUrl = "http://gptdb1.esri.com:8080/geoportal/proxy.jsp";
            return config;
          }
        });
        processor.execute(requestInfo,function(status,mediaType,entity,headers){
          //console.log(status,mediaType,"\r\n",entity);
          self.resultTextarea.value = entity;
          self.informExternal(entity);
        });

      } catch(error) {
        var msg = "Error processing request.";
        console.log(msg);
        if (error) {
          console.error(error);
          msg = ""+error;
        }
        this.resultTextarea.value = msg;
      }

    }

  });

  return _def;
});
