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
        "dojo/json",
        "dojo/dom-construct",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/text!./templates/SpatialFilterPane.html",
        "dojo/i18n!./nls/resources",
        "./DropPane",
        "./SpatialFilter",
        "dijit/form/CheckBox"], 
function(declare, lang, on, aspect, JSON, domConstruct,
         _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, template, i18n, 
         DropPane, SpatialFilter, CheckBox) {
  
  var oThisClass = declare("g.solr.SpatialFilterPane",
      [_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{

    i18n: i18n,
    templateString: template,
    _resizeTimer: null,
    _searchDisabled: false,
    _useFilterCheckBox: null,
    
    // DropPane
    open: false,
    title: null,
    
    // SpatialFilter 
    basemapUrl: null,
    field: "envelope_geo",
    map: null,
    
    postCreate: function() {
      this.inherited(arguments);
      var spatialFilter =  this.__spatialFilter;
      var dropPane =  this.__dropPane;
      var dropPaneTools = dropPane.dropPaneTools;
      var elTools = dropPaneTools.toolsNode;

      dropPaneTools.disableProperties(true);
      var el = domConstruct.create("span",{});
      elTools.insertBefore(el,dropPaneTools.propertiesNode);
      var chkBox = new CheckBox({
        id: dropPaneTools.id+"_sfcbx",
        title: this.i18n.SpatialFilter.UseFilterTip
      },el);
      this._useFilterCheckBox = chkBox;
      
      this.own(aspect.after(dropPane,"_onShow",lang.hitch(this,function(e) {
        this.repositionMap();
      })));
      this.own(on(spatialFilter,"MapLoad",lang.hitch(this,function(e) {
        this.repositionMap();
      }))); 
      this.own(on(spatialFilter,"QClauseRemoved",lang.hitch(this,function(e) {
        spatialFilter.searchDisabled = true;
        chkBox.set("checked",false);
        spatialFilter.useFilter = false;
        setTimeout(lang.hitch(this,function(){spatialFilter.searchDisabled = false;}),100);
      }))); 
      this.own(on(chkBox,"Change",lang.hitch(this,function(e) {
        this.setUseFilter(chkBox.get("checked"));
      }))); 
    
      spatialFilter.initializeMap();
    },
    
    postMixInProperties: function() {
      this.inherited(arguments);
      if (!this.title) {
        this.title = this.i18n.SpatialFilter.Title;
      }
      if (this.map != null) {
        this.map = JSON.stringify(this.map);
      }
    },
    
    repositionMap: function() {
      var dropPane = this.__dropPane;
      var map = this.__spatialFilter.map;
      if (map) {
        clearTimeout(this._resizeTimer);
        this._resizeTimer = setTimeout(function(){
          if (dropPane.open) {
            map.resize();
            map.reposition();
          }
        },500);
      }
    },
    
    setUseFilter: function(bUseFilter) {
      var dropPane = this.__dropPane;
      var spatialFilter =  this.__spatialFilter;
      var map = spatialFilter.map;
      spatialFilter.useFilter = bUseFilter;
      clearTimeout(this._resizeTimer);
      if (map && dropPane.open) {
        map.resize();
        map.reposition();
      }
      spatialFilter.search();
    }
    
  });
  
  return oThisClass;
});