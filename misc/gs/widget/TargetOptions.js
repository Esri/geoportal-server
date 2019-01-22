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
  "dojo/_base/lang",
  "dojo/on",
  "dojo/dom-class",
  "dojo/dom-construct",
  "dojo/number",
  "./SearchComponent",
  "dojo/text!./templates/TargetOptions.html",
  "dijit/popup",
  "dijit/TooltipDialog",
  "dijit/form/DropDownButton",
  "dijit/form/CheckBox"],
function(declare, array, lang, on, domClass, domConstruct, number,
  SearchComponent, template, popup) {

  var _def = declare([SearchComponent], {

    i18n: null,
    templateString: template,

    _activeTarget: null,
    _targets: null,

    postCreate: function() {
      this.inherited(arguments);
      this.captionNode.innerHTML = this.i18n.search.targetOptions.caption;

      //var self = this;
      //this.own(on(this.tooltipDialog,"open",function() {
        //var v = self.searchPane.wabWidget.appConfig.theme.name;
        //self.tooltipDialog.domNode.className += " " + v;
      //}));
    },

    startup: function() {
      if (this._started) {
        return;
      }
      this.inherited(arguments);
      this.initOptions();
    },

    /* ................................................................. */

    addTarget: function(target) {
      target.key = this.id+"_target"+this._targets.length;
      this._targets.push(target);
      var self = this;
      var nodeInfo = target.nodeInfo = {};
      nodeInfo.node = domConstruct.create("div",{
        "class": "target"
      },this.targetsNode,"last");
      nodeInfo.enabledCheckbox = domConstruct.create("input",{
        "type": "checkbox",
        "title": self.i18n.search.targetOptions.enabled,
        "onclick": function(e) {
          if (e) e.stopPropagation();
        }
      },nodeInfo.node,"last");
      if (typeof target.enabled === "boolean" && !target.enabled) {
        nodeInfo.enabledCheckbox.checked = false;
      } else {
        nodeInfo.enabledCheckbox.checked = true;
      }
      nodeInfo.nameNode = domConstruct.create("span",{
        "class": "name",
        innerHTML: target.name
      },nodeInfo.node,"last");
      nodeInfo.countNode = domConstruct.create("span",{
        "class": "count",
        innerHTML: "-"
      },nodeInfo.node,"last");
      nodeInfo.node.onclick = function() {
        popup.close(self.tooltipDialog);
        array.forEach(self._targets,function(target2){
          if (target !== target2) {
            domClass.remove(target2.nodeInfo.node,"active");
          }
        });
        domClass.add(nodeInfo.node,"active");
        self._activeTarget = target;
        nodeInfo.enabledCheckbox.checked = true;
        self.search();
      };
      if (!this._activeTarget) {
        domClass.add(nodeInfo.node,"active");
        this._activeTarget = target;
      }
    },

    getConfiguredTargets: function() {
      var targets = null;
      var cfg = this.getWidgetConfig() || {};
      if (cfg && Array.isArray(cfg.targets) && cfg.targets.length > 0) {
        targets = cfg.targets;
      } else {
        targets = [
          {
            "name": "ArcGIS Online",
            "tag": "arcgis",
            "type": "portal",
            "url": "https://www.arcgis.com",
            "enabled": true,
            "requiredFilter": null,
            "profile": null
          }
        ];
      }
      return targets;
    },

    getPrimarySearchResponse: function(result,task) {
      var self = this, searchResponse = null;
      if (lang.isArray(result)) {
        if (result.length > 0) {
          array.forEach(result,function(targetResult){
            var entity, k, k2, v;
            if (targetResult) {
              k = targetResult.target;
              entity = targetResult.entity;
              if (task.primaryTargetKey === k) {
                searchResponse = entity;
              }
              array.forEach(self._targets,function(target){
                if (k === target.key) {
                  v = "-";
                  if (entity && typeof entity.total === "number") {
                    v = number.format(entity.total);
                  }
                  target.nodeInfo.countNode.innerHTML = v;
                }
              });
            }
          });
        }
      } else {
        searchResponse = result;
        v = "-";
        if (task.target === this._activeTarget) {
          if (result && typeof result.total === "number") {
            v = number.format(result.total);
          }
        }
        if (this._activeTarget) {
          this._activeTarget.nodeInfo.countNode.innerHTML = v;
        }
      }
      if (!searchResponse) {
        searchResponse = {error: "No search response."};
      }
      return searchResponse;
    },

    initOptions: function() {
      this._activeTarget = null;
      this._targets = [];
      var self = this;
      var targets = lang.clone(this.getConfiguredTargets());
      array.forEach(targets,function(target) {
        self.addTarget(target);
      });
    },

    /* SearchComponent API ============================================= */

    appendQueryParams: function(params,task) {
      var self = this;
      var targets = array.filter(this._targets,function(target) {
        if (self._activeTarget === target) {
          if (!target.nodeInfo.enabledCheckbox.checked) {
            target.nodeInfo.enabledCheckbox.checked = true;
          }
          return target;
        } else if (target.nodeInfo.enabledCheckbox.checked) {
          return target;
        } else {
          target.nodeInfo.countNode.innerHTML = "-";
        }
      });
      if (this._activeTarget) {
        params.target = targets;
        task.primaryTargetKey = this._activeTarget.key;
        task.target = this._activeTarget;
      }
    }

  });

  return _def;
});
