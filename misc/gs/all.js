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

(function(){

  if (typeof gs === "undefined") gs = {};
  if (!gs.atom) gs.atom = {};
  if (!gs.base) gs.base = {};
  if (!gs.config) gs.config = {};
  if (!gs.context) gs.context = {};
  if (!gs.context.browser) gs.context.browser = {};
  if (!gs.context.nashorn) gs.context.nashorn = {};
  if (!gs.context.node) gs.context.node = {};
  if (!gs.provider) gs.provider = {};
  if (!gs.provider.csw) gs.provider.csw = {};
  if (!gs.provider.opensearch) gs.provider.opensearch = {};
  if (!gs.target) gs.target = {};
  if (!gs.target.csw) gs.target.csw = {};
  if (!gs.target.elastic) gs.target.elastic = {};
  if (!gs.target.portal) gs.target.portal = {};
  if (!gs.target.types) gs.target.types = {};
  if (!gs.target.profiles) gs.target.profiles = {};
  if (!gs.writer) gs.writer = {};

  var i, paths;
  var files = {
    common: [
      "base/core",
      "atom/atom-model",
      "base/Request",
      "base/Response",
      "base/SearchResult",
      "base/StringBuilder",
      "base/Task",
      "base/Uris",
      "base/Val",
      "base/XmlBuilder",
      "base/XmlInfo",
      "config/Config",
      "context/Context",
      "context/Processor",
      "provider/Provider",
      "provider/csw/CswProvider",
      "provider/csw/GetRecordsParser",
      "provider/csw/OwsException",
      "provider/opensearch/OpensearchProvider",
      "target/Target",
      "target/TargetSchema",
      "target/csw/CswTarget",
      "target/csw/CswSchema",
      "target/elastic/ElasticTarget",
      "target/elastic/ElasticSchema",
      "target/elastic/GeoportalTarget",
      "target/elastic/GeoportalSchema",
      "target/elastic/CustomElasticTarget",
      "target/elastic/CustomElasticSchema",
      "target/portal/PortalTarget",
      "target/portal/PortalSchema",
      "target/profiles/profiles",
      "writer/Writer",
      "writer/XmlWriter",
      "writer/AtomWriter",
      "writer/CswWriter",
      "writer/JsonWriter",
      "writer/CsvWriter",
      "writer/ErosWriter",
      "writer/KmlWriter",
      "writer/RssWriter"
    ],
    nashorn: [
      "context/nashorn/NashornContext",
      "context/nashorn/NashornProcessor",
      "context/nashorn/StringBuilder"
    ],
    node: [
      "context/node/NodeContext",
      "context/node/NodeProcessor"
    ],
    browser: [
      "context/browser/WebContext",
      "context/browser/WebProcessor"
    ]
  };

  if (typeof gsConfig !== "undefined" && gsConfig.isNashorn) {
    paths = files.common.concat(files.nashorn);
    for (i=0;i<paths.length;i++) {
      load("classpath:gs/"+paths[i]+".js");
    }
  } else if (typeof gsConfig !== "undefined" && gsConfig.isNode) {
    paths = files.common.concat(files.node);
    for (i=0;i<paths.length;i++) {
      require("./"+paths[i]+".js");
    }
  }

  gs.reqAll = function(localRequire, callback) {
    // for the Dojo based widget
    // these need to be loaded synchronously
    paths = files.common.concat(files.browser);
    var req = function() {
      var path = "../"+paths.shift();
      localRequire([path],function(){
        if (paths.length === 0) {
          if (typeof callback === "function") callback();
        } else {
          req();
        }
      });
    };
    req();
  };

}());
