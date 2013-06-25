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
        "dojo/dom-construct",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/text!./templates/AppliedFiltersPane.html",
        "dojo/i18n!./nls/resources",
        "./DropPane",
        "./AppliedFilters"], 
function(declare, lang, on, aspect, domConstruct,
         _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, template, i18n, 
         DropPane, AppliedFilters) {
  
  var oThisClass = declare("g.solr.AppliedFilters",
      [_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{

    clearAllNode: null,
    i18n: i18n,
    templateString: template,
    
    // DropPane
    open: false,
    title: null,
    
    postCreate: function() {
      this.inherited(arguments);
      var filters =  this.__appliedFilters;
      var dropPane =  this.__dropPane;
      var elTools = dropPane.dropPaneTools.toolsNode;
      this.clearAllNode = domConstruct.create("span",{
        className: "dijitReset dijitInline dijitIcon dijitIconDelete gIconTool",
        style: {visibility: "hidden"},
        title: this.i18n.AppliedFilters.ClearAll},elTools);
      this.own(on(this.clearAllNode,"click",lang.hitch(this,function(e) {
        filters.clearAll();
      }))); 
      this.own(aspect.after(filters,"handleQResponse",lang.hitch(this,function(e) {
        if (filters.hasRemovable) {
          this.clearAllNode.style.visibility = "visible";
        } else {
          this.clearAllNode.style.visibility = "hidden";
          if (dropPane.open) {
            dropPane.toggle();
          }
        }
      }))); 
    },
    
    postMixInProperties: function() {
      this.inherited(arguments);
      if (!this.title) {
        this.title = this.i18n.AppliedFilters.Title;
      }
    }
    
  });
  
  return oThisClass;
});