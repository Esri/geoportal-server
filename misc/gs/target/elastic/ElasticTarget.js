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

  gs.target.types["elasticsearch"] = {
    newInstance: function(options,safeOptions) {
      // dynamic target specification on the URL, disable for now,
      // will need a whitelist approach before re-enabling
      /*
      if (options && options.url) options.searchUrl = options.url;
      var target = gs.Object.create(gs.target.elastic.ElasticTarget);
      target.safeMixin(options).mixin(safeOptions);
      return target;
      */
      return null;
    }
  };

  gs.target.elastic.ElasticTarget = gs.Object.create(gs.target.Target, {

    accessQuery: {writable: true, value: null},
    
    itemBaseUrl: {writable: true, value: null},

    searchUrl: {writable: true, value: null},

    useSimpleQueryString: {writable: true, value: false}, // TODO?

    /* ............................................................................................ */

    appendPeriod: {writable:true,value:function(task,targetRequest,period,periodInfo) {
      if (!periodInfo) return;

      var isV5Plus = this.schema.isVersion5Plus;
      var fieldsOperator = "must";
      var field = periodInfo.field;
      var toField = periodInfo.toField;
      var nestedPath = periodInfo.nestedPath;

      var from = period.from, to = period.to;
      if (from === "*") from = null;
      if (to === "*") to = null;
      var hasValue = (from !== null || to !== null);

      var hasField = (typeof field === "string" && field.length > 0);
      var hasToField = (typeof toField === "string" && toField.length > 0);
      var isNested = (typeof nestedPath === "string" && nestedPath.length > 0);
      var query = null, condition = null, qFrom, qTo, qNested;

      if (hasValue && hasField) {

        if (hasToField) {
          if (from !== null) {
            if (to !== null) condition = {"gte":from,"lte":to};
            else condition = {"gte":from};
            qFrom = {"range": {}};
            qFrom.range[field] = condition;
            query = qFrom;
          }
          if (to !== null) {
            condition = {"lte":to};
            if (from != null) condition = {"gte":from,"lte":to};
            else condition = {"lte":to};
            qTo = {"range": {}};
            qTo.range[toField] = condition;
            query = qTo;
          }
          if (from !== null && to !== null) {
            if (fieldsOperator === "must") {
              query = {"bool": {"must":[qFrom,qTo]}};
            } else {
              query = {"bool": {"should":[qFrom,qTo]}};
            }
          }
          if (query && isNested) {
            if (isV5Plus) {
              qNested = {"nested":{
                "path": nestedPath,
                "query": query
              }};
            } else {
              qNested = {"query":{"nested":{
                "path": nestedPath,
                "query": query
              }}};
            }
            query = qNested;
          }
        }

        if (!hasToField) {
          if (from !== null && to !== null) {
            condition = {"gte":from,"lte":to};
          } else if (from !== null) {
            condition = {"gte":from};
          } else if (to !== null) {
            condition = {"lte":to};
          }
          if (condition !== null) {
            query = {"range": {}};
            query.range[field] = condition;
            if (isNested) {
              if (isV5Plus) {
                qNested = {"nested":{
                  "path": nestedPath,
                  "query": {"bool": {"must":[query]}}
                }};
              } else {
                qNested = {"query":{"nested":{
                  "path": nestedPath,
                  "query": {"bool": {"must":[query]}}
                }}};
              }
              query = qNested;
            }
          }
        }

      }

      if (query !== null) targetRequest.musts.push(query);
    }},

    /* ............................................................................................ */

    getSchemaClass: {writable:true,value:function() {
      return gs.target.elastic.ElasticSchema;
    }},

    /* ............................................................................................ */

    prepare: {writable:true,value:function(task) {
      var promise = task.context.newPromise();
      if (!this.schema) this.schema = this.newSchema(task);
      var targetRequest = {
        musts: [],
        qAll: "*:*",
        searchCriteria: {},
        useSimpleQueryString: this.useSimpleQueryString
      };
      
      // allow Elasticsearch DSL searches
      if (task.config.allowDslSearches) {
        var body, sBody = task.request.getEsDsl()? task.request.getEsDsl(): task.request.body;
        if (typeof sBody === "string" && sBody.indexOf("{") === 0) {
          sBody = sBody.trim();
          if (sBody.indexOf("{") === 0) {
            body = JSON.parse(sBody);
            if (body.query) {
              targetRequest.musts.push(JSON.parse(JSON.stringify(body.query)));
              delete body.query;
              targetRequest.searchCriteria = body;
            }
          }
        }
      }
      
      this.prepareRequiredFilter(task,targetRequest);
      this.prepareQ(task,targetRequest);
      this.prepareFilter(task,targetRequest);
      this.prepareIds(task,targetRequest);
      this.prepareTypes(task,targetRequest);
      this.prepareModified(task,targetRequest);
      this.prepareTimePeriod(task,targetRequest);
      this.prepareBBox(task,targetRequest);
      this.prepareAccessQuery(task,targetRequest);
      this.preparePaging(task,targetRequest);
      this.prepareSort(task,targetRequest);
      this.prepareOther(task,targetRequest);

      // api 5.1 ["q","from","size","sort","df","analyzer","analyze_wildcard",
      //          "default_operator","lenient", "timeout","terminate_after",
      //          "search_type","_source","stored_fields","track_scores","explain"];

      if (targetRequest.musts.length > 0) {
        targetRequest.searchCriteria["query"] = {"bool":{"must": targetRequest.musts}};
        //console.log("targetRequest.searchCriteria="+(JSON.stringify(targetRequest.searchCriteria)));
      }
      promise.resolve(targetRequest);
      return promise;
    }},
    
    prepareAccessQuery: {writable:true,value:function(task,targetRequest) {
      var query = this.accessQuery;
      if (query !== null && typeof query === "object") {
        //console.log("Setting access query...",JSON.stringify(query));
        targetRequest.musts.push(query);
      }
    }},

    prepareBBox: {writable:true,value:function(task,targetRequest) {
      var spatialInfo = this.schema.spatialInfo;
      if (!spatialInfo) return;

      var field = spatialInfo.field;
      var relation = "intersects";
      var hasField = (typeof field === "string" && field.length > 0);

      var coords = null, query = null, rel;
      var bbox = task.request.getBBox();
      if (typeof bbox === "string" && bbox.length > 0) {
        coords = bbox.split(",");
        if (coords.length > 3) {
          if ((coords[0] < -180.0) && (coords[2] >= -180.0)) coords[0] = -180.0;
          if ((coords[1] < -90.0) && (coords[3] >= -90.0)) coords[1] = -90.0;
          if ((coords[2] > 180.0) && (coords[0] <= 180.0)) coords[2] = 180.0;
          if ((coords[3] > 90.0) && (coords[1] <= 90.0)) coords[3] = 90.0;
        }
      }

      if (hasField && Array.isArray(coords) && coords.length > 3) {
        if (spatialInfo.type === "geo_shape") {
          rel = task.request.getSpatialRel();
          if (typeof rel === "string" && rel.length > 0) {
            rel = rel.toLowerCase();
            if (rel === "intersects" || rel === "within" ||
                rel === "contains" || rel === "disjoint") {
              relation = rel;
            }
          }
          query = {"geo_shape":{}};
          query["geo_shape"][field] = {
            "relation": relation,
            "shape": {
              "type": "envelope",
              "coordinates": [[coords[0],coords[3]], [coords[2],coords[1]]]
            }
          };
        }
        if (spatialInfo.type === "geo_point") {
          query = {"geo_bounding_box": {}};
          query["geo_bounding_box"][field] = {
            "top_left" : {
              "lon" : coords[0],
              "lat" : coords[3]
            },
            "bottom_right" : {
              "lon" : coords[2],
              "lat" : coords[1]
            }
          };
        }
      }

      if (query !== null) targetRequest.musts.push(query);
    }},

    prepareFilter: {writable:true,value:function(task,targetRequest) {
      // TODO array?
      var filter = task.request.getFilter();
      if (typeof filter === "string" && filter.length > 0) {
        targetRequest.musts.push({"query_string": {
          "analyze_wildcard": true,
          "query": filter
        }});
      }
    }},

    prepareIds: {writable:true,value:function(task,targetRequest) {
      var ids = task.request.getIds();
      if (Array.isArray(ids) && ids.length > 0) {
        targetRequest.musts.push({"terms":{"_id":ids}});
      }
    }},

    prepareModified: {writable:true,value:function(task,targetRequest) {
      var period = task.request.getModifiedPeriod();
      var periodInfo = this.schema.modifiedPeriodInfo;
      this.appendPeriod(task,targetRequest,period,periodInfo);
    }},

    prepareOther: {writable:true,value:function(task,targetRequest) {
    }},

    preparePaging: {writable:true,value:function(task,targetRequest) {
      var start = task.request.getStart();
      start = task.val.strToInt(start,null);
      if (typeof start === "number" && !task.request.queryIsZeroBased) {
        start = start - 1;
      }
      if (typeof start === "number" && start >= 0) {
        targetRequest.searchCriteria["from"] = start;
      }
      var num = task.request.getNum();
      num = task.val.strToInt(num,null);
      if (typeof num === "number" && num >= 0) {
        targetRequest.searchCriteria["size"] = num;
      }
    }},

    prepareQ: {writable:true,value:function(task,targetRequest) {
      var q = task.request.getQ();
      if (typeof q === "string" && q.length > 0) {
        var analyze_wildcard = task.request.chkBoolParam("analyze_wildcard",false);
        var lenient = task.request.chkBoolParam("lenient",true);
        if (targetRequest.useSimpleQueryString) {
          targetRequest.musts.push({"simple_query_string": {
            "analyze_wildcard": analyze_wildcard,
            "lenient": lenient,
            "query": q
          }});
        } else {
          targetRequest.musts.push({"query_string": {
            "analyze_wildcard": analyze_wildcard,
            "lenient": lenient,
            "query": q
          }});
        }
      }
    }},

    prepareRequiredFilter: {writable:true,value:function(task,targetRequest) {
      // TODO array?
      var requiredFilter = this.requiredFilter;
      if (typeof requiredFilter === "string" && requiredFilter.length > 0) {
        targetRequest.musts.push({"query_string": {
          "analyze_wildcard": true,
          "query": requiredFilter
        }});
      }
    }},

    prepareSort: {writable:true,value:function(task,targetRequest) {
      var sortables = this.schema.sortables;
      if (!sortables) return;

      var getField = function(v) {
        v = v.toLowerCase();
        for (var k in sortables) {
          if (sortables.hasOwnProperty(k)) {
            if (v === k.toLowerCase()) {
              return sortables[k];
            }
          }
        }
        return null;
      };

      var sort = [], sortOptions = task.request.getSortOptions();
      if (Array.isArray(sortOptions)) {
        sortOptions.forEach(function(sortOption){
          var field = getField(sortOption.field);
          if (typeof field === "string" && field.length > 0) {
            var option = {};
            if (sortOption.order === "asc") {
              option[field] = "asc";
            } else if (sortOption.order === "desc") {
              option[field] = "desc";
            } else {
              option = field;
            }
            sort.push(option);
          }
        });
      }
      if (sort.length > 0) {
        targetRequest.searchCriteria["sort"] = sort;
      }
    }},

    prepareTimePeriod: {writable:true,value:function(task,targetRequest) {
      var period = task.request.getTimePeriod();
      var periodInfo = this.schema.timePeriodInfo;
      this.appendPeriod(task,targetRequest,period,periodInfo);
    }},

    prepareTypes: {writable:true,value:function(task,targetRequest) {
      var shoulds = [], keys = [], query, qNested;
      var schema = this.schema;
      var types = task.request.getTypes();
      var typeInfo = schema.typeInfo;
      if (!typeInfo || !Array.isArray(types) || types.length === 0) return;
      var field = typeInfo.field;
      var nestedPath = typeInfo.nestedPath;
      var hasField = (typeof field === "string" && field.length > 0);
      var isNested = (typeof nestedPath === "string" && nestedPath.length > 0);
      if (!hasField) return;
      var isV5Plus = schema.isVersion5Plus;

      var appendType = function(v) {
        var q;
        if (typeof v === "string" && v.length > 0 && keys.indexOf(v) === -1) {
          keys.push(v);
          q = {"bool": {"must": {"term": {}}}};
          q.bool.must.term[field] = v; // TODO escape?
          shoulds.push(q);
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

      if (shoulds.length > 0) {
        query = {"bool": {"should": shoulds}};
        if (isNested) {
          if (isV5Plus) {
            qNested = {"nested":{
              "path": nestedPath,
              "query": query
            }};
          } else {
            qNested = {"query":{"nested":{
              "path": nestedPath,
              "query": query
            }}};
          }
          query = qNested;
        }
        //console.log("types",JSON.stringify(query));
      }

      if (query) targetRequest.musts.push(query);
    }},

    /* ............................................................................................ */

    search: {writable:true,value:function(task) {
      var self = this;
      var promise = task.context.newPromise();

      this.prepare(task).then(function(targetRequest){
        var url = self.searchUrl, options;
        var data = null, dataContentType = "application/json";
        if (targetRequest && task.val.hasAnyProperty(targetRequest.searchCriteria)) {
          data = JSON.stringify(targetRequest.searchCriteria);
        }
        if (typeof self.username === "string" && self.username.length > 0 &&
            typeof self.password === "string" && self.password.length > 0) {
          options = {
            basicCredentials: {
              username: self.username,
              password: self.password
            }
          };
        }
        if (task.verbose) console.log("sending url:",url,", postdata:",data);
        // console.log("sending url:",url,", postdata:",data);
        return task.context.sendHttpRequest(task,url,data,dataContentType,options);

      }).then(function(result){
        var searchResult = gs.Object.create(gs.base.SearchResult).init(task);
        var response = JSON.parse(result);
        searchResult.jsonResponse = response;
        if (response && response.hits) {
          searchResult.totalHits = response.hits.total;
          if (task.verbose) console.log("totalHits=",searchResult.totalHits);
          var hits = response.hits.hits;
          if (Array.isArray(response.hits.hits)) {
            searchResult.items = response.hits.hits;
            if (task.verbose) {
              var i = 0;
              response.hits.hits.forEach(function(item){
                console.log(i,item._source.title,item._id);
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
