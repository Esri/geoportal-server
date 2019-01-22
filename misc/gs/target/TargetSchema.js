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

  gs.target.TargetSchema = gs.Object.create(gs.Proto,{

    fieldAliases: {writable: true, value: null},

    schemaType: {writable: true, value: null},

    target: {writable: true, value: null},

    typeAliases: {writable: true, value: null},

    itemToAtomEntry: {writable:true,value:function(task,item) {}},

    itemToJson: {writable:true,value:function(task,item) {
      var entry = this.itemToAtomEntry(task,item);
      if (entry) {
        return entry.toJson(task);
      } else {
        return {};
      }
    }},

    translateFieldName: {writable:true,value:function(task,name) {
      var k, lc = null, v, aliases = this.fieldAliases;
      if (aliases && typeof name === "string" && name.length > 0){
        lc = name.toLowerCase();
        for (k in aliases) {
          if (aliases.hasOwnProperty(k)) {
            v = aliases[k];
            if (k === name) {
              return v;
            } else {
              if (lc === null) lc = name.toLowerCase();
              if (k.toLowerCase() === lc) return v;
            }
          }
        }
      }
      return name;
    }},

    translateTypeName: {writable:true,value:function(task,name) {
      var k, lc = null, v, aliases = this.typeAliases;
      if (aliases && typeof name === "string" && name.length > 0){
        lc = name.toLowerCase();
        for (k in aliases) {
          if (aliases.hasOwnProperty(k)) {
            v = aliases[k];
            if (k === name) {
              return v;
            } else {
              if (lc === null) lc = name.toLowerCase();
              if (k.toLowerCase() === lc) return v;
            }
          }
        }
      }
      return name;
    }}

  });

}());
