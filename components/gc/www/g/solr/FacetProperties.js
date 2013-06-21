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
        "dojo/keys",
        "dojo/query",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/text!./templates/FacetProperties.html",
        "dojo/i18n!./nls/resources",
        "dijit/form/Button",
        "dijit/form/TextBox",
        "dijit/form/NumberTextBox",
        "dijit/form/CheckBox",
        "dijit/form/Select"], 
function(declare, lang, on, keys, query,
         _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, template, i18n,
         Button, TextBox, NumberTextBox, CheckBox, Select) {
  
  var oThisClass = declare("g.solr.FacetProperties",
      [_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {
    
    qFacet: null,
    i18n: i18n,
    templateString: template,
    
    postCreate: function() {
      this.inherited(arguments);
      var v, v2;
      query(".fRangeBased",this.containerNode).forEach(function(node){
        node.style.display = "none";
      });
      
      v = false;
      v2 = this.qFacet.missing;
      if ((typeof(v2) != "undefined") && (v2 != null)) {
        if (typeof(v2) == "string") {
          if (v2.toLowerCase() == "true") v = true;
        } else {
          if (v2) v = true;
        }
      }
      this.qFacet.missing = v;
      this.__missing.set("value",""+this.qFacet.missing);
      
      v = "count";
      v2 = this.qFacet.sort;
      if ((typeof(v2) == "string") && (v2 != null)) {
        if (v2.toLowerCase() == "index") v = "index";
      }
      this.qFacet.sort = v;
      this.__sort.set("value",this.qFacet.sort);
    },
    
    apply: function()  {
      
      var checkVal = function(v) {
        if ((typeof(v) == "undefined") || (v == null)) {
          return null;
        }
        if ((typeof(v) == "string") && (v.length == 0)) {
          return null;
        }
        return v;
      };
      
      var v = checkVal(this.__field.get("value"));
      if (v != null) this.qFacet.field = v;
      
      v = checkVal(this.__aliasKey.get("value"));
      this.qFacet.aliasKey = v;
      
      v = checkVal(this.__mincount.get("value"));
      if (v != null) {if (isNaN(v)) v = null;}
      if (v != null) this.qFacet.mincount = v;
      
      v = checkVal(this.__limit.get("value"));
      if (v != null) {if (isNaN(v)) v = null;}
      if (v != null) this.qFacet.limit = v;
      
      v = checkVal(this.__prefix.get("value"));
      this.qFacet.prefix = v;
      
      this.qFacet.missing = false;
      v = checkVal(this.__missing.get("value"));
      if (v == "true")  this.qFacet.missing = true;

      v = checkVal(this.__sort.get("value"));
      this.qFacet.sort = v;
      
      if (this.qFacet.isRange) {
        v = checkVal(this.__rangeStart.get("value"));
        this.qFacet.rangeStart = v;
        v = checkVal(this.__rangeEnd.get("value"));
        this.qFacet.rangeEnd = v;
        v = checkVal(this.__rangeGap.get("value"));
        this.qFacet.rangeGap = v;
      }
      
      this.onApplied();
    },
    
    _okButtonClicked: function(e) {
      this.apply();
    },
   
    onApplied: function() {},
    
    onCancel: function(e) {}
  
  });
  
  return oThisClass;
});