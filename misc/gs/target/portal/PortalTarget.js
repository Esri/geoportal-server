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

  gs.target.types["portal"] = {
    newInstance: function(options,safeOptions) {
      if (options && options.url) options.portalBaseUrl = options.url;
      var target = gs.Object.create(gs.target.portal.PortalTarget);
      target.safeMixin(options).mixin(safeOptions);
      return target;
    }
  };

  gs.target.portal.PortalTarget = gs.Object.create(gs.target.Target, {

    portalBaseUrl: {writable: true, value: "https://www.arcgis.com"},

    /* ............................................................................................ */

    appendIds: {writable:true,value:function(task,urlParams,field,ids) {
      var q = "";
      if (Array.isArray(ids) && ids.length > 0) {
        ids.forEach(function(id){
          if (typeof id === "string" && id.trim().length > 0) {
            if (q.length > 0) q += " OR ";
            q += field+":\""+id.trim()+"\"";
          }
        });
      } else if (typeof ids === "string" && ids.trim().length > 0) {
        q = field+":\""+ids+"\"";
      }
      this.appendQ(urlParams,q);
    }},

    appendPeriod: {writable:true,value:function(task,targetRequest,period) {
      if (!period) return;
      var urlParams = targetRequest.urlParams;
      var wildCards = ["0000000000000000000","9999999999999999999"];

      var makeVal = function(value,isFrom) {
        var v = wildCards[0];
        if (!isFrom) v = wildCards[1];
        if (value !== null && value !== "*") {
          // TODO what about millisecond values in the query?
          if (typeof value === "string" && value.length > 0) {
            v = new Date(value).getTime();
            if (!isNaN(v)) {
              v = ""+v;
              while (v.length < 19) v = "0"+v;

            }
          }
        }
        return v;
      };

      var from = period.from, to = period.to;
      if (from !== null || to !== null) {
        from = makeVal(from,true);
        to = makeVal(to,false);
        this.appendQ(urlParams,"modified:["+from+" TO "+to+"]");
      }
    }},

    appendQ: {writable:true,value:function(urlParams,q) {
      if (typeof q === "string" && q.length > 0) {
        if (typeof urlParams.q === "string" && urlParams.q.length > 0) {
          urlParams.q = "("+urlParams.q+") AND ("+q+")";
        } else {
          urlParams.q = q;
        }
      }
    }},

    /* ............................................................................................ */

    getSchemaClass: {writable:true,value:function() {
      return gs.target.portal.PortalSchema;
    }},

    /* ............................................................................................ */

    prepare: {writable:true,value:function(task) {
      var promise = task.context.newPromise();
      if (!this.schema) this.schema = this.newSchema(task);
      var targetRequest = {
        qAll: "modified:[0000000000000000000 TO 9999999999999999999]",
        urlParams: {"f": "json"}
      };

      this.prepareRequiredFilter(task,targetRequest);
      this.prepareQ(task,targetRequest);
      this.prepareFilter(task,targetRequest);
      this.prepareIds(task,targetRequest);
      this.prepareTypes(task,targetRequest);
      this.prepareModified(task,targetRequest);
      this.prepareTimePeriod(task,targetRequest);
      this.preparePaging(task,targetRequest);
      this.prepareSort(task,targetRequest);
      this.prepareOther(task,targetRequest);
      this.prepareBBox(task,targetRequest); // must be last

      promise.resolve(targetRequest);
      return promise;
    }},

    prepareBBox: {writable:true,value:function(task,targetRequest) {
      var urlParams = targetRequest.urlParams;
      var bbox = task.request.getBBox();
      if (typeof bbox === "string" && bbox.length > 0) {
        urlParams["bbox"] = bbox;
      } else if (typeof urlParams.q !== "string" || urlParams.q.length === 0) {
        urlParams.q = targetRequest.qAll;
      }
    }},

    prepareFilter: {writable:true,value:function(task,targetRequest) {
      this.appendQ(targetRequest.urlParams,task.request.getFilter());
    }},

    prepareIds: {writable:true,value:function(task,targetRequest) {
      var urlParams = targetRequest.urlParams;
      this.appendIds(task,urlParams,"id",task.request.getIds());
      this.appendIds(task,urlParams,"orgid",task.request.getOrgIds());
      this.appendIds(task,urlParams,"group",task.request.getGroupIds());
    }},

    prepareModified: {writable:true,value:function(task,targetRequest) {
      var period = task.request.getModifiedPeriod();
      this.appendPeriod(task,targetRequest,period);
    }},

    prepareOther: {writable:true,value:function(task,targetRequest) {
      // TODO token?
    }},

    preparePaging: {writable:true,value:function(task,targetRequest) {
      var urlParams = targetRequest.urlParams;
      var start = task.request.getStart();
      start = task.val.strToInt(start,null);
      if (typeof start === "number" && task.request.queryIsZeroBased) {
        start = start + 1;
      }
      if (typeof start === "number" && start >= 1) {
        urlParams["start"] = start;
      }
      var num = task.request.getNum();
      num = task.val.strToInt(num,null);
      if (typeof num === "number" && num > 0) {
        urlParams["num"] = num;
      }
    }},

    prepareQ: {writable:true,value:function(task,targetRequest) {
      var q = task.request.getQ();
      if (q === "*" || q === "*:*") q = targetRequest.qAll;
      this.appendQ(targetRequest.urlParams,q);
    }},

    prepareRequiredFilter: {writable:true,value:function(task,targetRequest) {
      this.appendQ(targetRequest.urlParams,this.requiredFilter);
    }},

    prepareSort: {writable:true,value:function(task,targetRequest) {
      var urlParams = targetRequest.urlParams;
      var schema = this.schema;
      var sortField = "", sortOrder = "";
      var sortOptions = task.request.getSortOptions();
      if (Array.isArray(sortOptions)) {
        sortOptions.forEach(function(sortOption){
          var field = schema.translateFieldName(task,sortOption.field);
          if (typeof field === "string" && field.length > 0) {
            if (sortField.length > 0) sortField += ",";
            sortField += field;
            if (sortOrder.length > 0) sortOrder += ",";
            if (sortOption.order === "desc") {
              sortOrder += "desc";
            } else {
              sortOrder += "asc";
            }
          }
        });
      }
      if (typeof sortField === "string" && sortField.length > 0) {
        urlParams["sortField"] = sortField;
        if (typeof sortOrder === "string" && sortOrder.length > 0) {
          //if (sortOrder !== "asc") urlParams["sortOrder"] = sortOrder;
          urlParams["sortOrder"] = sortOrder;
        }
      }
    }},

    prepareTimePeriod: {writable:true,value:function(task,targetRequest) {
      var period = task.request.getTimePeriod();
      this.appendPeriod(task,targetRequest,period);
    }},

    prepareTypes: {writable:true,value:function(task,targetRequest) {
      var urlParams = targetRequest.urlParams;
      var q = "", keys = [];
      var schema = this.schema;
      var types = task.request.getTypes();
      if (!Array.isArray(types) || types.length === 0) return;

      var appendType = function(v) {
        if (typeof v === "string" && v.length > 0 && keys.indexOf(v) === -1) {
          keys.push(v);
          if (q.length > 0) q += " OR ";
          q += "type:\""+v+"\"";
        }
      };

      types.forEach(function(t){
        var t2 = schema.translateTypeName(task,t);
        if (Array.isArray(t2)) {
          t2.forEach(function(t3){
            appendType(t3);
          });
        } else {
          appendType(t2);
        }
      });
      //console.log("prepareTypes",q);
      this.appendQ(urlParams,q);
    }},

    /* ............................................................................................ */

    search: {writable:true,value:function(task) {
      var self = this;
      var promise = task.context.newPromise();

      this.prepare(task).then(function(targetRequest){
        var url = self.portalBaseUrl+"/sharing/rest/search";
        var data = null, dataContentType = "application/x-www-form-urlencoded";
        if (targetRequest && targetRequest.getRecordsXml) {
          data = targetRequest.getRecordsXml;
        } else if (targetRequest && task.val.hasAnyProperty(targetRequest.urlParams)) {
          var qstr = self.urlParamsToQueryString(targetRequest.urlParams);
          if (qstr !== null && qstr.length > 0) {
            if (url.indexOf("?") === -1) url += "?" + qstr;
            else url += "&" + qstr;
          }
        }
        if (task.verbose) console.log("sending url:",url,", postdata:",data);
        return task.context.sendHttpRequest(task,url,data,dataContentType);

      }).then(function(result){
        var searchResult = gs.Object.create(gs.base.SearchResult).init(task);
        var response = JSON.parse(result);
        searchResult.jsonResponse = response;
        if (response) {
          if (task.verbose) {
            console.log("hits",response.total,"start",response.start,
              "num",response.num,"nextStart",response.nextStart);
          }
          searchResult.startIndex = response.start;
          searchResult.totalHits = response.total;
          if (task.request.queryIsZeroBased) {
            searchResult.startIndex = searchResult.startIndex - 1;
          }
          if (searchResult.itemsPerPage > 0) {
            searchResult.itemsPerPage = response.num;
          }
          if (response.results && response.results.push) {
            searchResult.items = response.results;
            if (task.verbose) {
              var i = 1;
              searchResult.items.forEach(function(item){
                console.log(i,item.title,item.id);
                i++;
              });
            }
          }
        }
        promise.resolve(searchResult);

      })["catch"](function(error){
        promise.reject(error);
      });
      return promise;
    }}

  });

}());
