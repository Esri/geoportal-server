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

  gs.provider.opensearch.OpensearchProvider = gs.Object.create(gs.provider.Provider,{

    isSingleIdRequest: {writable: true, value: false},

    chkBBoxParam: {writable:true,value:function(task) {
      if (task.hasError) return;
      var bbox = task.request.getBBox();
      if (bbox === null || bbox.length === 0) return;
      var a = bbox.split(",");
      if (a.length > 3) {
        var n = task.val.strToNum(a[0].trim(),1);
        if (n > 10000) {
          var msg = "{\"error\": \"invalidBoundingBoxCoords\"}";
          var response = task.response;
          response.put(response.Status_BAD_REQUEST,response.MediaType_APPLICATION_JSON,msg);
          task.hasError = true;
        }
      }
    }},

    description: {writable:true,value:function(task) {
      var promise = task.context.newPromise();
      var opensearchUrl = task.baseUrl+"/opensearch"; // TODO doc or config?

      var qstr = "", url = task.request.url;
      var n = url.indexOf("?");
      if (n !== -1) qstr = url.substring(n + 1).trim();
      if (qstr.length > 0) qstr = "&"+qstr;

      var xml = task.context.readResourceFile(task.config.opensearchDescriptionFile,"UTF-8");
      xml = xml.trim();
      xml = xml.replace(/{opensearch.url}/g,task.val.escXml(opensearchUrl));
      xml = xml.replace(/{base.url}/g,task.val.escXml(task.baseUrl));
      xml = xml.replace(/&amp;{args}/g,task.val.escXml(qstr));
      var response = task.response;
      response.put(response.Status_OK,response.MediaType_APPLICATION_XML,xml);
      promise.resolve();
      return promise;
    }},

    execute: {writable:true,value:function(task) {
      var v = task.request.getUrlPath();
      var isDescription = task.val.endsWith(v,"/opensearch/description") || // TODO doc or config?
                          task.val.endsWith(v,"/Eros/description"); 
      if (!isDescription) {
        var vals = task.request.getHeaderValues("Accept");
        if (vals !== null && vals.length > 0) {
          isDescription = vals.some(function(s){
            return (s.indexOf("application/opensearchdescription+xml") !== -1);
          });
        }
      }
      if (isDescription) {
        return this.description(task);
      } else {
        this.chkBBoxParam(task);
        if (task.hasError) {
          var promise = task.context.newPromise();
          promise.reject();
          return promise;
        } else {
          var ids = task.request.getIds();
          if (Array.isArray(ids) && ids.length === 1) {
            this.isSingleIdRequest = true;
            //task.request.isItemByIdRequest = true;
          }
          task.request.f = "atom";
          return this.search(task);
        }
      }
    }},

    search: {writable:true,value:function(task) {
      var promise = task.context.newPromise();
      task.request.parseF(task);
      this.setWriter(task);
      var isSingleIdRequest = this.isSingleIdRequest;
      task.target.search(task).then(function(searchResult){
        if (isSingleIdRequest && (!searchResult.items || searchResult.items.length === 0)) {
          // TODO is this error only for the CSW ets-cat30 test?
          var msg = "{\"error\": \"Id not found.\"}";
          task.response.put(task.response.Status_NOT_FOUND,task.response.MediaType_APPLICATION_JSON,msg);
          promise.resolve();
        } else {
          task.writer.write(task,searchResult);
          promise.resolve();
        }
      })["catch"](function(error){
        promise.reject(error);
      });
      return promise;
    }}

  });

}());
