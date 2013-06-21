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
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/text!./templates/Expression.html",
        "dojo/i18n!./nls/resources",
        "./base/QComponent",
        "./base/QClause",
        "dijit/form/TextBox"], 
function(declare, lang, on, keys,
         _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, 
         template, i18n, QComponent, QClause, TextBox) {
  
  var oThisClass = declare("g.solr.Expression",
      [_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, QComponent], {
    
    i18n: i18n,
    templateString: template,
    
    postCreate: function() {
      this.inherited(arguments);
      this.activeQClauses = new Array();
      this.ensureSolrClient();
      this.solrClient.registerQComponent(this);
      this.own(on(this.__textBox,"keyup",lang.hitch(this,function(evt) {
        if (evt.keyCode == keys.ENTER) this.search();
      })));
    },
    
    appendQParameters: function(qRequest) {
      this.activeQClauses = new Array();
      var s = lang.trim(this.__textBox.get("value"));
      if (s.length > 0) {
        qRequest.putUrlParameter("q",s);
        var qClause = new QClause({
          appliedFilterLabel: s,
          label: s,
          urlParameterName: null,
          urlParameterValue: null,
          parentQComponent: this,
          removable: true
        });
        this.activeQClauses.push(qClause);
      }
    },
    
    onQClauseRemoved: function(qClause) {
      this.__textBox.set("value",null);
    },
    
    search: function() {
      this.solrClient.fetch();
    }
  
  });
  
  return oThisClass;
});