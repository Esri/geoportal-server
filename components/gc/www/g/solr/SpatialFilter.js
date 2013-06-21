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
        "dojo/on",
        "dojo/aspect",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/text!./templates/SpatialFilter.html",
        "dojo/i18n!./nls/resources",
        "esri/map",
        "esri/layers/ArcGISTiledMapServiceLayer",
        "./base/QComponent",
        "./base/QClause",
        "dijit/layout/ContentPane"], 
function(declare, lang, on, aspect, 
         _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, template, i18n, 
         AgsMap, ArcGISTiledMapServiceLayer, QComponent, QClause, ContentPane) {
  
  var oThisClass = declare("g.solr.SpatialFilter",
        [_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, QComponent],{
    
    i18n: i18n,
    templateString: template,
    
    basemapUrl: null,
    field: "envelope_geo",
    map: null,
    searchDisabled: false,
    useFilter: false,
    
    postCreate: function() {
      this.inherited(arguments);
      this.activeQClauses = new Array();
      this.ensureSolrClient();
      this.solrClient.registerQComponent(this);
    },
    
    appendQParameters: function(qRequest) {
      this.activeQClauses = new Array();
      var sField = this.field;
      if (!sField) return;
      if (!this.map) return;
      
      var sQuery = null;
      if (this.useFilter) {
        var env = this.map.geographicExtent;
        if (env) {
          var xmin = env.xmin, ymin = env.ymin;
          var xmax = env.xmax, ymax = env.ymax;
          if (xmin < -180) xmin = -180;
          if (xmax > 180) xmax = 180;
          if (ymin < -90) ymin = -90;
          if (ymax > 90) ymax = 90;
          var sEnv = xmin+" "+ymin+" "+xmax+" "+ymax;
          sQuery = sField+":\"Intersects("+sEnv+")\"";
        }
      }
      if (sQuery != null) {
        var qClause = new QClause({
          appliedFilterLabel: sQuery,
          label: sQuery,
          urlParameterName: "fq",
          urlParameterValue: sQuery,
          parentQComponent: this,
          removable: true
        });
        this.activeQClauses.push(qClause);
        qRequest.pushQClauses(this.activeQClauses);
      }
    },
    
    handleQResponse: function(responseObject) {},
    
    initializeMap: function(parentDropPane) {
      var oMapProperties = this.map;
      this.map = null;
      var oMap = new esri.Map(this.mapPane.domNode,oMapProperties);
      var s = this.basemapUrl;
      if (!s) {
        // TODO: lookup from config default?
      }
      if (s) {
        var basemap = new esri.layers.ArcGISTiledMapServiceLayer(s);
        oMap.addLayer(basemap);
        this.own(on(oMap,"Load",lang.hitch(this,function(){
          this.map = oMap;
          this.own(on(oMap,"ExtentChange",lang.hitch(this,function(){
            if (this.useFilter) this.search();
          })));
          this.onMapLoad();
        })));
      }
    },
    
    onMapLoad: function() {},
    
    onQClauseRemoved: function(qClause) {
      this.useFilter = false;
    },
    
    search: function() {
      if (this.solrClient && !this.searchDisabled) {
        this.solrClient.fetch();
      }
    }
    
  });
  
  return oThisClass;
});