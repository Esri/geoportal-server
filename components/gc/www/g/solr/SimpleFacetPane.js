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
        "dojo/json",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/text!./templates/SimpleFacetPane.html",
        "dojo/i18n!./nls/resources",
        "./DropPane",
        "./SimpleFacet"], 
function(declare, lang, on, JSON,
         _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, template, i18n, 
         DropPane, SimpleFacet) {
  
  var oThisClass = declare("g.solr.SimpleFacetPane",
      [_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{

    i18n: i18n,
    templateString: template,
    
    // TitleBarPane
    open: false,
    title: null,
    
    // Facet constructor parameters
    facet: null,
    
    postCreate: function() {
      this.inherited(arguments);
      var tools = this.__dropPane.dropPaneTools;
      tools.enableProperties();
      this.own(on(tools,"PropertiesClick",lang.hitch(this,function(e) {
        this.__facet.showProperties(this);
      })));
    },
    
    postMixInProperties: function() {
      this.inherited(arguments);
      if (this.facet != null) {
        if (!this.title) {
          var s = this.i18n.Fields[this.facet.field];
          if (!s) s = this.facet.field;
          if (s) this.title = s;
        }
        this.facet = JSON.stringify(this.facet);
      }
    },
    
    resetTitle: function(sTitle) {
      this.__dropPane.set("title",sTitle);
    }
    
  });
  
  return oThisClass;
});