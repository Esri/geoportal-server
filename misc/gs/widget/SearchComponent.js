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
  "dijit/_WidgetBase",
  "dijit/_TemplatedMixin",
  "dijit/_WidgetsInTemplateMixin"],
function(declare, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin) {

  var _def = declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {

    isSearchComponent: true,
    searchPane: null,
    template: "<div></div>",

    postCreate: function() {
      this.inherited(arguments);
    },

    /* SearchComponent API ============================================= */

    appendQueryParams: function(params,task) {
      /*jshint unused:vars*/
    },

    processResults: function(searchResponse,task) {
      /*jshint unused:vars*/
    },

    /* utilities ======================================================= */

    getMap: function() {
      if (this.searchPane && this.searchPane.widgetContext) {
        return this.searchPane.widgetContext.getMap();
      }
      return null;
    },

    getWidgetConfig: function() {
      var context = this.getWidgetContext();
      if (context) {
        return context.widgetConfig;
      }
      return null;
    },

    getWidgetContext: function() {
      if (this.searchPane) {
        return this.searchPane.widgetContext;
      }
      return null;
    },

    search: function() {
      if (this.searchPane) {
        this.searchPane.search();
      }
    }

  });

  return _def;
});
