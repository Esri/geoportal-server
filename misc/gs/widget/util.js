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
define(["dojo/_base/array",
  "dojo/_base/url",
  "dojo/dom-geometry",
  "dojo/dom-style",
  "dojo/window",
  "esri/config",
  "esri/request"],
function(array, DjUrl, domGeometry, domStyle, win, esriConfig, esriRequest) {

  var _def = {

    addCors: function(uri) {
      var corsUri = null;
      if (typeof uri === "string" && uri.length > 0) {
        if ((uri.indexOf("http://") === 0) || (uri.indexOf("https://") === 0)) {
          corsUri = (new DjUrl(uri)).host; // host or authority?
        } else {
          corsUri = uri;
        }
      }
      if (typeof corsUri === "string" && corsUri.length > 0) {
        if (esriConfig.defaults.io.corsEnabledServers.indexOf(corsUri) === -1) {
          esriConfig.defaults.io.corsEnabledServers.push(corsUri);
        }
      }
    },

    checkMixedContent: function(uri) {
      if ((typeof window.location.href === "string") &&
        (window.location.href.indexOf("https://") === 0)) {
        if ((typeof uri === "string") && (uri.indexOf("http://") === 0)) {
          uri = "https:" + uri.substring("5");
        }
      }
      return uri;
    },

    endsWith: function(sv, sfx) {
      return (sv.indexOf(sfx, (sv.length - sfx.length)) !== -1);
    },

    escapeForLucene: function(value) {
      var a = ['+', '-', '&', '!', '(', ')', '{', '}', '[', ']',
      '^', '"', '~', '*', '?', ':', '\\'];
      var r = new RegExp("(\\" + a.join("|\\") + ")", "g");
      return value.replace(r, "\\$1");
    },

    generateId: function() {
      var t = null;
      if (typeof Date.now === "function") {
        t = Date.now();
      } else {
        t = (new Date()).getTime();
      }
      var r = ("" + Math.random()).replace("0.", "r");
      return (t + "" + r).replace(/-/g, "");
    },

    mitigateDropdownClip: function(dd,ddul) {
      // Dropdown menus clipped by scrollable container
      var reposition = function() {
        var cs = domStyle.getComputedStyle(dd);
        var winBox = win.getBox();
        var ddGeom = domGeometry.position(dd);
        var ddulGeom = domGeometry.position(ddul);

        var t = ddGeom.y + ddGeom.h;
        var l = ddGeom.x;
        domStyle.set(ddul,"top",t+"px");
        domStyle.set(ddul,"left",l+"px");

        var position = t;
        var buttonHeight = ddGeom.h;
        var menuHeight = ddulGeom.h;
        var winHeight = winBox.h;
        if (position > menuHeight && winHeight - position < buttonHeight + menuHeight) {
          t = t - menuHeight - buttonHeight - 4;
          domStyle.set(ddul,"top",t+"px");
        }
      };
      reposition();
    },

    readItemJsonData: function(itemUrl) {
      var n = itemUrl.indexOf("?");
      if (n !== -1) itemUrl = itemUrl.substring(0,n);
      var url = this.checkMixedContent(itemUrl);
      url = itemUrl + "/data";
      this.addCors(url);
      return esriRequest({
        url: url,
        content: {f: "json"},
        handleAs: "json"
      },{});
    },

    readRestInfo: function(url) {
      url = this.checkMixedContent(url);
      this.addCors(url);
      return esriRequest({
        url: url,
        content: {f: "json"},
        handleAs: "json",
        callbackParamName: "callback"
      },{});
    },

    setNodeText: function(nd, text) {
      nd.innerHTML = "";
      if (text) {
        nd.appendChild(document.createTextNode(text));
      }
    },

    setNodeTitle: function(nd, text) {
      nd.title = "";
      if (text) {
        nd.setAttribute("title", text);
      }
    },

    setNodeHTML: function(nd, html) {
      nd.innerHTML = "";
      if (html) {
        nd.innerHTML = html;
      }
    }

  };

  return _def;
});
