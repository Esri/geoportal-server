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
  "dojo/dom-construct",
  "dijit/_WidgetBase",
  "dijit/_TemplatedMixin",
  "dijit/_WidgetsInTemplateMixin",
  "dojo/text!./templates/Settings.html",
  "./TargetSetting"],
function(declare, array, domConstruct, _WidgetBase, _TemplatedMixin,
  _WidgetsInTemplateMixin, template, TargetSetting) {

  var _def = declare([_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin], {

    i18n: null,
    templateString: template,

    config: null,

    postCreate: function() {
      this.inherited(arguments);
    },

    /* ..................................................................... */

    getConfig: function() {
      if (!this.config) this.config = {};
      return this.config;
    },

    setConfig: function(config) {
      this.config = config || {};
      array.forEach(config.targets,function(properties){
        this._addTarget(properties);
      },this);
    },

    validate: function() {
      var targets = [];
      var ok = array.every(this.getChildren(),function(child){
        if (child.isTargetSetting) {
          var childOk = child.validate();
          if (childOk) {
            var props = child.getProperties();
            if (props) targets.push(props);
          }
          return childOk;
        }
        return true;
      });
      if (ok) {
        this.config = {};
        if (targets.length > 0) this.config.targets = targets;
      }
      return ok;
    },

    /* ..................................................................... */

    _addTarget: function(properties) {
      var target;
      var nd = domConstruct.create("div",{},this.targetsNode,"last");
      target = new TargetSetting({
        i18n: this.i18n,
        properties: properties,
        deleteClicked: function() {
          target.destroy();
        }
      },nd);
    },

    _addTargetClicked: function() {
      this._addTarget({});
    },

    _test: function() {
      var ok = this.validate();
      console.log("validate.ok",ok);
      if (ok) console.log("validate.config",this.getConfig());
    }

  });

  return _def;
});
