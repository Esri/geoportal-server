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
        "dojo/dom-construct",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/text!./templates/SimpleFacet.html",
        "dojo/i18n!./nls/resources",
        "dijit/Dialog",
        "./base/QComponent",
        "./base/QClause",
        "./base/QFacet",
        "./FacetLink",
        "./FacetProperties"], 
function(declare, lang, on, domConstruct,
         _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, template, i18n, 
         Dialog, QComponent, QClause, QFacet, FacetLink, FacetProperties) {
  
  var oThisClass = declare("g.solr.SimpleFacet",
      [_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, QComponent],{
    
    i18n: i18n,
    templateString: template,
    qFacet: null,

    postCreate: function() {
      this.inherited(arguments);
      this.activeQClauses = new Array();
      var oFacet = new QFacet(this.facet);
      this.qFacet = oFacet;
      this.ensureSolrClient();
      this.solrClient.registerQComponent(this);
    },
    
    appendQParameters: function(qRequest) {
      this.qFacet.appendQParameters(qRequest);
      qRequest.pushQClauses(this.activeQClauses);
    },
    
    handleQResponse: function(responseObject) {
      var oContentNode = this.contentNode;
      domConstruct.empty(oContentNode);
      this.qFacet.parseQResponse(responseObject,lang.hitch(this,
        function(sValue,nCount,sRangeValue) {
          this._renderFacetValue(oContentNode,sValue,nCount,sRangeValue);
        }
      ));
    },
    
    _renderFacetValue: function(parentNode,sValue,nCount,sRangeValue) {
      var sQuery = this.qFacet.makeFilterQuery(sValue,sRangeValue);
      var qClause = new QClause({
        appliedFilterLabel: sQuery,
        label: sValue+" ("+nCount+")",
        urlParameterName: "fq",
        urlParameterValue: sQuery,
        parentQComponent: this,
        removable: true
      });
      new FacetLink({qClause:qClause},domConstruct.create("div",{},parentNode));
    },
    
    showProperties: function(parentPane) {
      var dialog = new Dialog({
        title: this.i18n.FacetProperties.Title,
        content: domConstruct.create("div")
      });
      var params = {qFacet: this.qFacet};
      var props = new FacetProperties(params,dialog.content);
      
      var sField = this.qFacet.field;
      var hApplied = null, hCancel = null, hHide = null;
      hApplied = on(props,"Applied",lang.hitch(this,function(e) {
        sNewField = this.qFacet.field;
        if (sField !== sNewField) {
          var s = this.i18n.Fields[sNewField];
          if (!s) s = sNewField;
          if (s && parentPane && parentPane.resetTitle) {
            parentPane.resetTitle(s);
          }
        }
        dialog.hide();
        this.solrClient.fetch();
      }));
      hCancel = on(props,"Cancel",lang.hitch(this,function(e) {
        dialog.hide();
      }));
      hHide = on(dialog,"Hide", function() {
        hApplied.remove();
        hCancel.remove();
        hHide.remove();
        setTimeout(function(){dialog.destroyRecursive();},300);
      });
      dialog.show();
    }
    
  });
  
  return oThisClass;
});