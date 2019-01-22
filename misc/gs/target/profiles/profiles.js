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
  /*jshint -W069 */

  gs.target.profiles["CSW2_Geoportal1"] = {
    type: "csw",
    prepareTarget: function(target) {
      target.mixin({
        "schemaMixin": {
          "liveDataPropertyName": "dc:type"
        },
      });
    }
  };

  /*
  gs.target.profiles["sampleProfile1"] = {
    type: "geoportal",
    prepareTarget: function(target) {
      target.mixin({
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
      });
    }
  };
  */

}());
