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

  var fs = require("fs");

  gs.context.node.NodeContext = gs.Object.create(gs.context.Context,{

    readResourceFile: {writable:true,value:function(path,charset) {
      path = "../"+path; // TODO this is relative to where node was launched
      if (charset === null || charset.length == 0) charset = "UTF-8";
      return fs.readFileSync(path,{encoding:charset});
    }},

    sendHttpRequest: {writable:true,value:function(task,url,data,dataContentType) {
      var promise = this.newPromise();
      var options = {url: url};
      if (typeof data !== "undefined" && data !== null) {
        options = {
          url: url,
          method: "POST",
          headers: [{name: "content-type", value: dataContentType}],
          body: data
        };
      }
      // TODO gs._request path for require
      gs._request(options, function (error, response, body) {
        var msg = "Network error";
        if (!error && response && response.statusCode === 200) {
          promise.resolve(body);
        } else {
          if (response) console.log("response.statusText",response.statusText);
          if (error) {
            promise.reject(new Error(msg)); // TODO
          } else {
            promise.reject(new Error(msg)); // TODO
          }
        }
        if (error) {
          promise.reject(new Error(msg)); // TODO
        } else if (response && response.statusCode === 200) {
          promise.resolve(body);
        } else {
          promise.reject(new Error(msg)); // TODO
        }
      });
      return promise;
    }}

  });

}());
