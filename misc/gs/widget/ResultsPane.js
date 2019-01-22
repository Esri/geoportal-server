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
  "dojo/_base/array",
  "./SearchComponent",
  "dojo/text!./templates/ResultsPane.html",
  "./ItemCard",
  "./layers/layerUtil",
  "./util"],
function(declare, array, SearchComponent, template, ItemCard, layerUtil, util) {

  var _def = declare([SearchComponent], {

    i18n: null,
    templateString: template,

    postCreate: function() {
      this.inherited(arguments);
    },

    addItem: function(itemCard) {
      itemCard.placeAt(this.itemsNode);
      itemCard.startup();
      return itemCard;
    },

    destroyItems: function() {
      this.noMatchNode.style.display = "none";
      this.noMatchNode.innerHTML = "";
      this.destroyDescendants(false);
    },

    showNoMatch: function() {
      util.setNodeText(this.noMatchNode,this.i18n.search.resultsPane.noMatch);
      this.noMatchNode.style.display = "block";
    },

    /* SearchComponent API ============================================= */

    processResults: function(searchResponse,task) {
      this.destroyItems();
      this.itemsNode.scrollTop = 0;
      var self = this;
      var results = searchResponse.results;
      if (results && results.length > 0) {
        var supportsRemove = this.getWidgetContext().supportsRemove;
        var idsAdded = [];
        if (supportsRemove) {
          idsAdded = layerUtil.findLayersAdded(this.getMap(),null).referenceIds;
        }
        array.forEach(searchResponse.results, function(result) {
          //console.warn(result.id,idsAdded);
          var itemCard = self.addItem(new ItemCard({
            canRemove: false,
            i18n: self.i18n,
            item: result,
            resultsPane: self,
            searchPane: self.searchPane,
            searchResponse: searchResponse,
            supportsRemove: supportsRemove
          }));
          if (supportsRemove && idsAdded.indexOf(itemCard.referenceId) !== -1) {
            itemCard.setCanRemove(true);
          }
        });
      } else {
        this.showNoMatch();
      }
    }

  });

  return _def;
});
