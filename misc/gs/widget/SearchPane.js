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
  "require",
  "dojo/_base/lang",
  "dojo/_base/array",
  "dojo/_base/window",
  "dojo/dom-class",
  "dojo/on",
  "dijit/_WidgetBase",
  "dijit/_TemplatedMixin",
  "dijit/_WidgetsInTemplateMixin",
  "dojo/text!./templates/SearchPane.html",
  "dijit/Dialog",
  "./BBoxOption",
  "./LiveDataOption",
  "./Paging",
  "./ResultsPane",
  "./SearchBox",
  "./SortOptions",
  "./TargetOptions",
  "../all"],
function(declare, localRequire, lang, array, win, domClass, on,
  _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, template, Dialog,
  BBoxOption, LiveDataOption, Paging, ResultsPane, SearchBox, SortOptions,
  TargetOptions) {

  var _def = declare([_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin], {

    i18n: null,
    templateString: template,

    components: null,
    defaultFilter : null,
    requiredFilter: null,
    searchOnStart: true,
    widgetContext: null,

    _checkSearchOnStart: true,
    _wasLoaded: false,

    postCreate: function() {
      this.inherited(arguments);
      var self = this;
      this.toggleLoading(false);

      gs.reqAll(localRequire,function(){
        self.init();
        self._wasLoaded = true;
        if (self._started && self._checkSearchOnStart && self.searchOnStart) {
          self.search();
        }
      });
    },

    startup: function() {
      if (this._started) {
        return;
      }
      this.inherited(arguments);
      if (this._wasLoaded && this._checkSearchOnStart && this.searchOnStart) {
        this.search();
      }
    },

    /* ...................................................................... */

    buildQueryParams: function(task) {
      var qRequired = null;
      if (typeof this.requiredFilter === "string" &&
          this.requiredFilter.length > 0) {
        qRequired = this.requiredFilter;
      }
      var params = {
        q: qRequired,
        canSortByRelevance: false
      };
      array.forEach(this.components,function(component) {
        component.appendQueryParams(params,task);
      });
      delete params.canSortByRelevance;
      if (params.q === null && typeof this.defaultFilter === "string" &&
          this.defaultFilter.length > 0) {
        params.q = this.defaultFilter;
      }
      return params;
    },

    informExternal: function(text) {
      try {
        if (window && window.external && window.external.gsHasListener) {
          window.external.gsListener(text);
        }
      } catch(ex) {
        console.error(ex);
        //alert(ex);
      }
    },

    init: function() {
      this.components = [];
      var mixins = {
        i18n: this.i18n,
        searchPane: this,
      };
      this.targetOptions = new TargetOptions(mixins,this.targetOptionsNode);
      this.components.push(this.targetOptions);
      this.targetOptions.startup(); // TODO?
      this.searchBox = new SearchBox(mixins,this.searchBoxNode);
      this.components.push(this.searchBox);
      this.bboxOption = new BBoxOption(mixins,this.bboxOptionNode);
      this.components.push(this.bboxOption);
      this.bboxOption = new LiveDataOption(mixins,this.liveDataOptionNode);
      this.components.push(this.bboxOption);
      this.resultsPane = new ResultsPane(mixins,this.resultsPaneNode);
      this.components.push(this.resultsPane);
      this.paging = new Paging(mixins,this.pagingNode);
      this.components.push(this.paging);
      this.sortOptions = new SortOptions(mixins,this.sortOptionsNode); // leave this last
      this.components.push(this.sortOptions);
    },

    processResults: function(searchResponse,task) {
      array.forEach(this.components,function(component) {
        component.processResults(searchResponse,task);
      });
    },

    search: function() {
      if (!this._wasLoaded) return; // TODO need a ui error message
      var self = this, task = {};
      this.toggleLoading(true);
      var parameterMap = this.buildQueryParams(task);
      parameterMap.f = "json";
      //parameterMap.target = "gptdb1";
      //parameterMap.target = "cswA";

      var requestInfo = {
        requestUrl: "/request",
        baseUrl: "/base",
        headerMap: {},
        parameterMap: parameterMap
      };
      var result, searchResponse;
      var processor = this._proc = gs.Object.create(gs.context.browser.WebProcessor).mixin({
        newConfig: function() {
          var config = gs.Object.create(gs.config.Config);
          config.proxyUrl = self.widgetContext.proxyUrl;
          return config;
        }
      });
      processor.execute(requestInfo,function(status,mediaType,entity,headers){
        if (processor === self._proc) {
          //console.log(status,mediaType,"\r\n",entity);
          self.informExternal(entity);
          try {
            result = JSON.parse(entity);
            searchResponse = self.targetOptions.getPrimarySearchResponse(result,task);
            self.processResults(searchResponse,task);
          } catch(ex) {
            // TODO handle errors
            console.error(ex);
          }
          self.toggleLoading(false);
        }
      });
    },

    showSettings: function() {
      var dialog = new Dialog({
        title: "My Dialog",
        content: "Test content.",
        style: "width: 100%; height: 100%;"
      });
      dialog.show();
    },

    toggleLoading: function(visible) {
      if (visible) {
        domClass.add(this.loadingNode,"loading");
      } else {
        domClass.remove(this.loadingNode,"loading");
      }
    }

  });

  return _def;
});
