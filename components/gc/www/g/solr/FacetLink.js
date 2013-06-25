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
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/FacetLink.html"], 
function(declare, lang, on,
         _WidgetBase, _TemplatedMixin, template) {
  
  var oThisClass = declare("g.solr.FacetLink",[_WidgetBase,_TemplatedMixin], {
    
    label: null,
    qClause: null,
    templateString: template,
    
    postMixInProperties: function() {
      this.inherited(arguments);
      if (!this.label || (this.label.length == 0)) {
        if (this.qClause && this.qClause.label) {
          this.label = this.qClause.label;
        }
      }
    },
    
    onLinkClicked: function(e) {
      if (!this.qClause) return;
      this.qClause.parentQComponent.pushQClause(this.qClause,true);
    }
  
  });
  
  return oThisClass;
});