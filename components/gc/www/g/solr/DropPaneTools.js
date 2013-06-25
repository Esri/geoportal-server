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
        "dojo/_base/event",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/text!./templates/DropPaneTools.html",
        "dojo/i18n!./nls/resources",
        "dijit/form/CheckBox"], 
function(declare, lang, on, event,
         _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, 
         template, i18n, CheckBox) {
  
  var oThisClass = declare("g.solr.DropPaneTools",
      [_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {
    
    i18n: i18n,
    templateString: template,
    
    postCreate: function() {
      this.inherited(arguments);
      this.own(on(this.toolsNode,"click",lang.hitch(this,function(e) {
        event.stop(e);
      })));
    },
    
    disableProperties: function(bPermanent) {
      if (bPermanent) {
        this.propertiesNode.style.display = "none";
      } else {
        this.propertiesNode.style.visibility = "hidden";
      }
    },
    
    enableProperties: function() {
      this.propertiesNode.style.visibility = "visible";
    },
    
    onPropertiesClick: function(e) {}
    
  });
  
  return oThisClass;
});