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
  "./SearchComponent",
  "dojo/text!./templates/SearchBox.html"],
function(declare, lang, on, keys, SearchComponent, template) {

  return declare([SearchComponent], {

    i18n: null,
    templateString: template,

    postCreate: function() {
      this.inherited(arguments);
      var self = this;
      this.own(on(this.searchTextBox,"keyup",function(evt) {
        if (evt.keyCode === keys.ENTER) {
          self.search();
        }
      }));
    },

    searchButtonClicked: function() {
      this.search();
    },

    /* SearchComponent API ============================================= */

    appendQueryParams: function(params,task) {
      var q = this.searchTextBox.value;
      if (q !== null) {
        q = lang.trim(q);
      }
      if (q !== null && q.length > 0) {
        params.canSortByRelevance = true;
        q = "(" + q + ")";
        if (params.q !== null && params.q.length > 0) {
          params.q += " AND " + q;
        } else {
          params.q = q;
        }
      }
    }

  });

});
