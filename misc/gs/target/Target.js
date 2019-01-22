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

  gs.target.Target = gs.Object.create(gs.Proto,{

    key: {writable: true, value: null},

    requiredFilter: {writable: true, value: null},

    schema: {writable: true, value: null},

    schemaMixin: {writable: true, value: null},

    useProxy: {writable: true, value: false},

    /* ............................................................................................ */

    getSchemaClass: {writable:true,value:function() {
      return gs.target.TargetSchema;
    }},

    itemToAtomEntry: {writable:true,value:function(task,item) {
      return this.schema.itemToAtomEntry(task,item);
    }},

    itemToJson: {writable:true,value:function(task,item) {
      return this.schema.itemToJson(task,item);
    }},

    newSchema: {writable:true,value:function(task) {
      var schemaMixin = this.schemaMixin || {};
      schemaMixin.target = this;
      return gs.Object.create(this.getSchemaClass()).mixin(schemaMixin);
    }},

    prepare: {writable:true,value:function(task) {}},

    search: {writable:true,value:function(task) {}},

    urlParamsToQueryString: {writable:true,value:function(urlParams) {
      var k, v, qstr = null;
      if (urlParams) {
        for (k in urlParams) {
          if (urlParams.hasOwnProperty(k)) {
            v = urlParams[k];
            if (typeof v !== "undefined" && v !== null) {
              if (!Array.isArray(v)) v = [v];
              v.forEach(function(v2){
                if (qstr === null) qstr = "";
                if (qstr.length > 0) qstr += "&";
                qstr += encodeURIComponent(k)+"="+encodeURIComponent(v2);
              });
            }
          }
        }
      }
      return qstr;
    }}

  });

}());
