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

  gs.config.Config = gs.Object.create(gs.Proto,{

    defaultTarget: {writable: true, value: "arcgis"},
    
    // allow POSTing of Elasticsearch DSL (Domain Specific Language) queries and aggregations
    allowDslSearches: {writable: true, value: true},

    allowDynamicTarget: {writable: true, value: true},

    cswCapabilitiesFile: {writable: true, value: "gs/config/csw-capabilities.xml"},

    csw2CapabilitiesFile: {writable: true, value: "gs/config/csw2-capabilities.xml"},

    opensearchDescriptionFile: {writable: true, value: "gs/config/opensearch-description.xml"},

    proxyUrl: {writable: true, value: null},

    _targets: {writable: true, value: null},

    getTargets: {writable:true,value:function() {
      if (!this._targets) this._targets = this.makeTargets();
      return this._targets;
    }},

    makeTargets: {writable:true,value:function() {

      var targets = {

        "arcgis": gs.Object.create(gs.target.portal.PortalTarget).mixin({
          "portalBaseUrl": "https://www.arcgis.com"
        })

        /*

        Some Examples

        "arcgisSDI": gs.Object.create(gs.target.portal.PortalTarget).mixin({
          "portalBaseUrl": "https://www.arcgis.com",
          "requiredFilter": "orgid:RhGiohBHzSBKt1MS"
        }),

        "portal1": gs.Object.create(gs.target.portal.PortalTarget).mixin({
          "portalBaseUrl": "http://urbanvm.esri.com/arcgis"
        }),

        "gptdb1": gs.Object.create(gs.target.elastic.GeoportalTarget).mixin({
          "searchUrl": "http://gptdb1.esri.com:8080/geoportal/elastic/metadata/item/_search"
        }),

        "gptdb2": gs.Object.create(gs.target.elastic.GeoportalTarget).mixin({
          "searchUrl": "http://gptdb2.esri.com:8080/geoportal/elastic/img/item/_search",
          "schemaMixin": {
            "bboxField": null,
            "pointField": "envelope_cen_pt",
            "modifiedPeriodInfo": {
              "field": "sys_modified_dt",
            },
            "sortables": {
              "title": "title.sort",
              "date": "sys_modified_dt",
              "modified": "sys_modified_dt"
            },
            "spatialInfo": {
              "field": "envelope_cen_pt",
              "type": "geo_point"
            },
            "timePeriodInfo": null,
            "typeAliases": null,
            "typeInfo": null
          }
        }),

        "customElastic": gs.Object.create(gs.target.elastic.CustomElasticTarget).mixin({
          "searchUrl": "http://gptdb1.esri.com:9200/metadata/item/_search",
          "itemBaseUrl": null
        }),

        "cswA": gs.Object.create(gs.target.csw.CswTarget).mixin({
          "cswVersion": "2.0.2",
          "getRecordsUrl": "https://gptogc.esri.com/geoportal/csw?service=CSW&request=GetRecords"
        }),

        "cswB": gs.Object.create(gs.target.csw.CswTarget).mixin({
          "cswVersion": "3.0.0",
          "getRecordsUrl": "http://gptdb1.esri.com:8080/geoportal/csw?service=CSW&request=GetRecords"
        }),

        "cswC": gs.Object.create(gs.target.csw.CswTarget).mixin({
          "cswVersion": "3.0.0",
          "getRecordsUrl": "http://urban.esri.com:8080/geoportal/csw?service=CSW&request=GetRecords"
        })
        */

      };

      return targets;
    }}

  });

}());
