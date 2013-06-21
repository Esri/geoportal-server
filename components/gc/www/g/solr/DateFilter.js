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
        "dojo/date",
        "dojo/date/stamp",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/text!./templates/DateFilter.html",
        "dojo/i18n!./nls/resources",
        "./base/QComponent",
        "./base/QClause",
        "dijit/form/DateTextBox"], 
function(declare, lang, date, stamp,
         _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, template, i18n, 
         QComponent, QClause, DateTextBox) {
  
  var oThisClass = declare("g.solr.DateFilter",
        [_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, QComponent],{
    
    i18n: i18n,
    templateString: template,
    
    field: null,
    toField: null,
    _searchDisabled: false,

    postCreate: function() {
      this.inherited(arguments);
      this.activeQClauses = new Array();
      this.ensureSolrClient();
      this.solrClient.registerQComponent(this);
    },
    
    appendQParameters: function(qRequest) {
      this.activeQClauses = new Array();
      var sFrom = null, sTo = null, sQuery = null;
      
      // fields
      var sField = this.field;
      var sToField = this.toField;
      if (!sField && !sToField) return;
      if (sField && sToField) {
        if (sField === sToField) sToField = null;
      } else if (sToField) {
        sField = sToField;
        sToField = null;
      }
      
      // dates
      var options = {zulu: true};
      var dtFrom = this.__fromDate.get("value");
      var dtTo = this.__toDate.get("value");
      if (dtFrom != null) {
        sFrom = stamp.toISOString(dtFrom,options);
      }
      if (dtTo != null) {
        var dt = date.add(dtTo,"day",1);
        dt = date.add(dt,"millisecond",-1);
        sTo = stamp.toISOString(dt,options);
      }
      
      // query
      if ((sFrom != null) && (sTo != null)) {
        if (sField && sToField) {
          sQuery = sField+":["+sFrom+" TO *] AND "+sToField+":[* TO "+sTo+"]";
        } else {
          sQuery = sField+":["+sFrom+" TO "+sTo+"]";
        }
      } else if (sFrom != null) {
        sQuery = sField+":["+sFrom+" TO *]";
      } else if (sTo != null) {
        if (sToField) {
          sQuery = sToField+":[* TO "+sTo+"]";
        } else {
          sQuery = sField+":[* TO "+sTo+"]";
        }
      }
      
      // add the clause
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
    
    _dateChanged: function(e) {
      if (this.solrClient && !this._searchDisabled) {
        this.solrClient.fetch();
      }
    },
    
    handleQResponse: function(responseObject) {},
    
    onQClauseRemoved: function(qClause) {
      this._searchDisabled = true;
      this.__fromDate.set("value",null);
      this.__toDate.set("value",null);
      setTimeout(lang.hitch(this,function(){this._searchDisabled = false;}),100);
    }
    
  });
  
  return oThisClass;
});