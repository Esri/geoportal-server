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
        "dijit/TitlePane",
        "dojo/i18n!./nls/resources",
        "./DropPaneTools"], 
function(declare, lang, on, event, domConstruct, domClass,
         TitlePane, i18n, DropPaneTools) {

  var oThisClass = declare("g.solr.DropPane",[TitlePane], {
  
    i18n: i18n,
    i18nTitleKey: null,
    dropPaneTools: null,
    
    postCreate: function() {
      this.inherited(arguments);
      domClass.add(this.domNode,"gDropPane");
      var nd = domConstruct.create("span",{});
      this.focusNode.appendChild(nd);
      var params = {};
      this.dropPaneTools = new DropPaneTools(params,nd);
    },
    
    postMixInProperties: function() {
      this.inherited(arguments);
      var s;
      if (!this.title || (this.title.length == 0)) {
        if (this.i18nTitleKey && (this.i18nTitleKey.length > 0)) {
          s = this.i18n[this.i18nTitleKey];
          if (s) this.title = s;
        }
      }
    }
    
  });
  
  return oThisClass;
});