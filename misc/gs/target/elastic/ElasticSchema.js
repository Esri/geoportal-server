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

  gs.target.elastic.ElasticSchema = gs.Object.create(gs.target.TargetSchema, {

    isVersion5Plus: {writable: true, value: true},

    bboxField: {writable: true, value: null},
    pointField: {writable: true, value: null},

    fieldAliases: {writable: true, value: null},

    schemaType: {writable: true, value: "Elasticsearch"},

    sortables: {writable: true, value: {
    }},

    spatialInfo: {writable: true, value: {
    }},

    modifiedPeriodInfo: {writable: true, value: {
    }},

    timePeriodInfo: {writable: true, value: {
    }},

    typeInfo: {writable: true, value: {
    }},

    itemToAtomEntry: {writable:true,value:function(task,item) {
      //console.log("ElasticSchema::itemToAtomEntry");
      var source = item._source;
      var entry = gs.Object.create(gs.atom.Entry);
      entry.id = item._id;
      entry.title = task.val.chkStr(source.title);
      return entry;
    }},

    itemToJson: {writable:true,value:function(task,item) {
      var json = gs.target.TargetSchema.itemToJson.call(this,task,item); // call super
      if (item._source) {
        json._source = item._source;
      }
      return json;
    }}

  });

}());
