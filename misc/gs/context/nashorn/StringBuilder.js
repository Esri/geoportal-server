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

  gs.context.nashorn.StringBuilder = gs.Object.create(gs.base.StringBuilder,{

    _sb: {writable: true, value: null},

    append: {writable:true,value:function(value) {
      if (value === null) return this;
      if (typeof value !== "string") return this;
      if (this._sb === null) this._sb = new java.lang.StringBuilder();
      this._sb.append(value);
      return this;
    }},

    toString: {writable:true,value:function() {
      if (this._sb === null) return "";
      return this._sb.toString();
    }}

  });

}());
