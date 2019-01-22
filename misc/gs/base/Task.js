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

  gs.base.Task = gs.Object.create(gs.Proto,{

    async: {writable: true, value: true},
    baseUrl: {writable: true, value: null},
    config: {writable: true, value: null},
    context: {writable: true, value: null},
    dfd: {writable: true, value: null},
    hasError: {writable: true, value: false},
    isCsw2: {writable: true, value: false},
    provider: {writable: true, value: null},
    request: {writable: true, value: null},
    response: {writable: true, value: null},
    target: {writable: true, value: null},
    uris: {writable: true, value: null},
    val: {writable: true, value: null},
    verbose: {writable: true, value: false},
    writer: {writable: true, value: null},
    writers: {writable: true, value: null},

    newTask: {writable:true,value:function(options) {
      var task = gs.Object.create(gs.base.Task).mixin({
        response: gs.Object.create(gs.base.Response),
        uris: gs.Object.create(gs.base.Uris),
        val: gs.Object.create(gs.base.Val),
        writer: gs.Object.create(gs.writer.AtomWriter),
      }).mixin(options);
      return task;
    }}

  });

}());
