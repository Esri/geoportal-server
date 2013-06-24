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
        "dojo/json",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/text!./templates/ResultsPane.html",
        "dojo/i18n!./nls/resources",
        "./DropPane",
        "./Results"], 
function(declare, lang, JSON,
         _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, template, i18n, 
         DropPane, Results) {
  
  var oThisClass = declare("g.solr.ResultsPane",
      [_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{

    i18n: i18n,
    templateString: template,
    
    // DropPane
    open: false,
    title: null,
    
    // Results constructor properties
    rows: 10,
    
    postMixInProperties: function() {
      this.inherited(arguments);
      if (!this.title) {
        this.title = this.i18n.Results.Title;
      }
      if (this.results != null) {
        this.results = JSON.stringify(this.results);
      }
    }
    
  });
  
  return oThisClass;
});