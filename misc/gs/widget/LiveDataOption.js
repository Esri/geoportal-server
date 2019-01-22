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
  "./SearchComponent",
  "dojo/text!./templates/LiveDataOption.html",
  "dijit/form/CheckBox"],
function(declare, SearchComponent, template) {

  var _def = declare([SearchComponent], {

    i18n: null,
    templateString: template,

    postCreate: function() {
      this.inherited(arguments);
      this.liveDataLabel.innerHTML = this.i18n.search.liveDataOption.liveData;
    },

    liveDataClicked: function() {
      this.search();
    },

    /* SearchComponent API ============================================= */

    appendQueryParams: function(params,task) {
      if (this.liveDataToggle.get("checked")) {
        params.canSortByRelevance = true;
        params.type = "liveData";
      }
    }

  });

  return _def;
});
