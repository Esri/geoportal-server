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
  "dojo/dom-class",
  "dojo/number",
  "dojo/text!./templates/Paging.html",
  "./SearchComponent",
  "./util"],
function(declare, domClass, number, template, SearchComponent, util) {

  var _def = declare([SearchComponent], {

    i18n: null,
    templateString: template,

    hasLess: false,
    hasMore: false,
    nextStart: -1,
    numPerPage: null,
    previousStart: -1,
    start: 1,

    postCreate: function() {
      this.inherited(arguments);
      this.domNode.style.display = "none";
      if (this.numPerPage === null) {
        this.numPerPage = 30;
      }
      this.enableOrDisable();
    },

    startup: function() {
      if (this._started) {
        return;
      }
      this.inherited(arguments);

      try {
        var numPer = this.getConfig().numPerPage;
        var v = Number(numPer);
        if (typeof v === "number" && !isNaN(v)) {
          v = Math.floor(v);
          if (v >= 1 && v <= 100) {
            this.numPerPage = v;
          }
        }
      } catch (ex) {
        console.warn("Error setting numPerPage:");
        console.warn(ex);
      }
      if (this.numPerPage === null) {
        this.numPerPage = 30;
      }
    },

    enableOrDisable: function() {
      if (this.hasLess) {
        domClass.remove(this.firstButton.parentNode, "disabled");
        domClass.remove(this.previousButton.parentNode, "disabled");
      } else {
        domClass.add(this.firstButton.parentNode, "disabled");
        domClass.add(this.previousButton.parentNode, "disabled");
      }
      if (this.hasMore) {
        domClass.remove(this.nextButton.parentNode, "disabled");
      } else {
        domClass.add(this.nextButton.parentNode, "disabled");
      }
    },

    /* events ========================================================== */

    firstButtonClicked: function() {
      if (this.hasLess) {
        this.start = 1;
        this.search();
      }
    },
    previousButtonClicked: function() {
      if (this.hasLess) {
        this.start = this.previousStart;
        this.search();
      }
    },
    nextButtonClicked: function() {
      if (this.hasMore) {
        this.start = this.nextStart;
        this.search();
      }
    },

    /* SearchComponent API ============================================= */

    appendQueryParams: function(params,task) {
      params.start = this.start;
      params.num = this.numPerPage;
    },

    processResults: function(searchResponse,task) {
      if (typeof searchResponse.total !== "number") {
        this.domNode.style.display = "none";
        return;
      }
      this.start = 1;
      var nPer = this.numPerPage;
      var nHits = searchResponse.total;
      var nStart = searchResponse.start;
      //var nStart = searchResponse.queryParams.start;
      if (nStart < 1) {
        nStart = 1;
      }

      this.hasLess = false;
      this.previousStart = -1;
      if (nStart > 1) {
        this.hasLess = true;
        this.previousStart = nStart - searchResponse.num;
        //this.previousStart = nStart - searchResponse.queryParams.num;
        if (this.previousStart < 1) {
          this.previousStart = 1;
        }
      }

      this.hasMore = false;
      this.nextStart = -1;
      if (searchResponse.nextStart > 1) {
        this.hasMore = true;
        this.nextStart = searchResponse.nextStart;
      }

      var sPage = "";
      if (nHits === 0) {
        this.pageNode.innerHTML = "&nbsp;";
        this.domNode.style.display = "none";
      } else {
        this.domNode.style.display = "";
        if (nHits > nPer) {
          var nPage = 1;
          if (nStart > 1) {
            nPage = Math.floor(nStart / nPer) + 1;
          }
          sPage = this.i18n.search.paging.pagePattern;
          sPage = sPage.replace("{page}", "" + nPage);
        } else {
          sPage = this.i18n.search.paging.pagePattern;
          sPage = sPage.replace("{page}", "" + 1);
        }
        util.setNodeText(this.pageNode,sPage);
      }
      this.updateCount(searchResponse);
      this.enableOrDisable();
    },

    updateCount: function(searchResponse) {
      var total = searchResponse.total;
      var type = this.i18n.search.resultCount.itemPlural;
      if (total === 1) {
        type = this.i18n.search.resultCount.itemSingular;
      }
      var v = this.i18n.search.resultCount.countPattern;
      v = v.replace("{count}",number.format(total));
      v = v.replace("{type}",type);
      util.setNodeText(this.countNode,v);
    }

  });

  return _def;
});
