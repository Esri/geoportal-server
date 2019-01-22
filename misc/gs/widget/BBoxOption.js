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
  "dojo/text!./templates/BBoxOption.html",
  "dijit/form/CheckBox"],
function(declare, SearchComponent, template) {

  var _def = declare([SearchComponent], {

    i18n: null,
    templateString: template,

    postCreate: function() {
      this.inherited(arguments);
      this.bboxLabel.innerHTML = this.i18n.search.bboxOption.bbox;
    },

    bboxClicked: function() {
      this.search();
    },

    getBBox: function() {
      var ext = this.searchPane.widgetContext.getGeographicExtent();
      if (ext) {
        return ext.xmin + "," + ext.ymin + "," + ext.xmax + "," + ext.ymax;
      }
      return null;
    },

    /* SearchComponent API ============================================= */

    appendQueryParams: function(params,task) {
      if (this.bboxToggle.get("checked")) {
        var bbox = this.getBBox();
        if (bbox) {
          // TODO is bbox just a filter, are results spatially ranked?
          params.canSortByRelevance = true;
          params.bbox = bbox;
        }
      }
    }

  });

  return _def;
});
