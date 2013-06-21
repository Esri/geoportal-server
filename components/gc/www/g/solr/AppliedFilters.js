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
        "dojo/dom-construct",
        "dojo/dom-class",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/text!./templates/AppliedFilters.html",
        "dojo/i18n!./nls/resources",
        "./base/QComponent",
        "./AppliedFilter"], 
function(declare, lang, on, event, domConstruct, domClass,
         _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, template, i18n,
         QComponent, AppliedFilter) {
    
  var oThisClass = declare("g.solr.AppliedFilters",
        [_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, QComponent],{
    
    hasRemovable: false,
    i18n: i18n,
    templateString: template,
    
    postCreate: function() {
      this.inherited(arguments);
      this.ensureSolrClient();
      this.solrClient.registerQComponent(this);
    },
    
    clearAll: function() {
      this.solrClient.forEachQComponent(lang.hitch(this,function(qComponent) {
        var i, n, qClauses = qComponent.activeQClauses, aClauses = new Array();
        if (qClauses) {
          n = qClauses.length;
          for (i=0;i<n;i++) {
            if (!qClauses[i].removable) {
              aClauses.push(qClauses[i]);
            } else {
              qComponent.onQClauseRemoved(qClauses[i]);
            }
          }
          qComponent.activeQClauses = aClauses;
        }
      }));
      this.solrClient.fetch();
    },
    
    handleQResponse: function(responseObject) {
      var oContentNode = this.contentNode;
      domConstruct.empty(oContentNode);
      
      var bHasRemovable = false;
      this.solrClient.forEachQComponent(lang.hitch(this,function(qComponent) {
        var i, n, qClauses = qComponent.activeQClauses;
        if (qClauses) {
          n = qClauses.length;
          for (i=0;i<n;i++) {
            if (qClauses[i].viewable) {
              if (qClauses[i].removable) bHasRemovable = true;
              new AppliedFilter({qClause: qClauses[i]},
                  domConstruct.create("div",{},oContentNode));
            }
          }
        }
      }));
      this.hasRemovable = bHasRemovable;
    }
    
  });
  
  return oThisClass;
});