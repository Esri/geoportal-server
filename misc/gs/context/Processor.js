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

  gs.context.Processor = gs.Object.create(gs.Proto,{

    execute: {writable:true,value:function(requestInfo, responseHandler) {
      var task = null;
      try {
        var context = this.newContext();
        var config = this.newConfig();
        var request = this.makeRequest(requestInfo);
        var targets = this.makeTargets(context,config,request);
        var tasks = this.makeTasks(context,config,requestInfo,targets);
        if (tasks.length === 0) {
          this._sendError(responseHandler,null,null,"Search error: no task");
        } else if (tasks.length === 1) {
          task = tasks[0];
          this.executeTask(context,tasks[0],responseHandler);
        } else {
          this.executeTasks(context,tasks,responseHandler);
        }
      } catch(ex) {
        this._sendError(responseHandler,task,ex,null);
      }
    }},

    executeTask: {writable:true,value:function(context, task, responseHandler) {
      var self = this, msg, response;
      task.provider.preprocess(task);
      task.dfd = task.provider.execute(task);
      task.dfd.then(function(){
        if (task.verbose) console.log("Processor.executeTask-complete");
        response = task.response;
        if (typeof responseHandler === "function") {
          responseHandler(response.status,response.mediaType,response.entity,response.headers);
        }
      })["catch"](function(error){
        self._sendError(responseHandler,task,error,null);
      });
    }},

    executeTasks: {writable:true,value:function(context, tasks, responseHandler) {
      var self = this, dfds = [];
      tasks.forEach(function(task){
        self.executeTask(context,task);
        dfds.push(task.dfd);
      });

      var response, result, results = [], o;
      var promise = context.newPromiseAll(dfds);
      promise.then(function(){
        tasks.forEach(function(task){
          if (task.response && task.response.mediaType === task.response.MediaType_APPLICATION_JSON) {
            if (typeof task.response.entity === "string" && task.response.entity.indexOf("{") === 0) {
              try {
                o = JSON.parse(task.response.entity);
                task.response.entity = o;
              } catch(ex) {}
            }
          }
          results.push(task.response);
        });
        result = JSON.stringify(results); // TODO pretty? JSON.stringify(results,null,2);
        response = gs.Object.create(gs.base.Response);
        response.put(response.Status_OK,response.MediaType_APPLICATION_JSON,result);
        if (result.headers) response.headers = result.headers;
        if (typeof responseHandler === "function") {
          responseHandler(response.status,response.mediaType,response.entity,response.headers);
        }
      })["catch"](function(error){
        // TODO collect the errors?
        self._sendError(responseHandler,null,error,null);
      });
    }},

    makeProvider: {writable:true,value:function(task) {
      var provider = gs.Object.create(gs.provider.opensearch.OpensearchProvider);
      var v = task.request.getUrlPath();
      if (task.val.endsWith(v,"/csw") || task.val.endsWith(v,"/csw/")) {
        var isOsDsc = false, vals = task.request.getHeaderValues("Accept");
        if (vals !== null && vals.length > 0) {
          isOsDsc = vals.some(function(s){
            return (s.indexOf("application/opensearchdescription+xml") !== -1);
          });
        }
        if (!isOsDsc) provider = gs.Object.create(gs.provider.csw.CswProvider);
      }
      return provider;
    }},

    makeRequest: {writable:true,value:function(requestInfo) {
      var request = gs.Object.create(gs.base.Request).mixin({
        url: requestInfo.requestUrl,
        body: requestInfo.requestBody,
        headerMap: requestInfo.headerMap,
        parameterMap: requestInfo.parameterMap,
      });
      return request;
    }},

    makeTargets: {writable:true,value:function(context,config,request) {
      var self = this, o, target, targets = [];
      var cfgTargets = config.getTargets() || {};
      var values = request.getTargets();
      //console.log("target values",values);
      if (values !== null) {
        values.forEach(function(v){
          //console.log("target value",v);
          self._checkTarget(v,cfgTargets,targets,config);
        });
      }
      if (targets.length === 0) {
        o = config.defaultTarget;
        if (cfgTargets.hasOwnProperty(o)) {
          target = cfgTargets[o];
          //target.key = o;
          targets.push(target);
        }
      }
      if (targets.length === 0) {
        // TODO exception here??
      }
      return targets;
    }},

    makeTasks: {writable:true,value:function(context,config,requestInfo,targets) {
      var self = this, req, tasks = [];
      var sRequestInfo = JSON.stringify(requestInfo);
      targets.forEach(function(target){
        requestInfo = JSON.parse(sRequestInfo);
        request = gs.Object.create(gs.base.Request).mixin({
          url: requestInfo.requestUrl,
          body: requestInfo.requestBody,
          headerMap: requestInfo.headerMap,
          parameterMap: requestInfo.parameterMap,
        });
        task = self.newTask(context,config,request,{
          baseUrl: requestInfo.baseUrl
        }).mixin(requestInfo.taskOptions);
        task.target = target;
        task.response.target = target.key;
        tasks.push(task);
      });
      return tasks;
    }},

    makeWriters: {writable:true,value:function(task) {
      var index = function(writers,keys,writer) {
        keys.forEach(function(k){
          writers[k.toLowerCase()] = writer;
        });
      };

      var atom = ["atom","application/atom+xml","http://www.w3.org/2005/Atom"];
      var csw = ["csw","","application/xml","http://www.opengis.net/cat/csw/3.0"];
      var json = ["json","pjson","json-source","application/json"];
      var csv = ["csv","text/csv"];
      var eros = ["eros"];
      var kml = ["kml","application/vnd.google-earth.kml+xml","http://www.opengis.net/kml/2.2"];
      var rss = ["rss","georss","application/rss+xml"];

      var writers = {};
      index(writers,atom,gs.Object.create(gs.writer.AtomWriter));
      index(writers,csw,gs.Object.create(gs.writer.CswWriter));
      index(writers,json,gs.Object.create(gs.writer.JsonWriter));
      index(writers,csv,gs.Object.create(gs.writer.CsvWriter));
      index(writers,eros,gs.Object.create(gs.writer.ErosWriter));
      index(writers,kml,gs.Object.create(gs.writer.KmlWriter));
      index(writers,rss,gs.Object.create(gs.writer.RssWriter));

      return writers;
    }},

    newConfig: {writable:true,value:function() {
      return gs.Object.create(gs.config.Config);
    }},

    newContext: {writable:true,value:function() {
      return gs.Object.create(gs.context.Context);
    }},

    newTask: {writable:true,value:function(context,config,request,options) {
      var task = gs.base.Task.newTask().mixin({
        config: config,
        context: context,
        request: request,
      }).mixin(options);
      task.provider = this.makeProvider(task);
      task.writers = this.makeWriters(task);
      return task;
    }},

    /* ............................................................................................ */

    _checkTarget: {writable:true,value:function(v, cfgTargets, targets, config) {
      //console.log("_checkTarget",v);
      var self = this, o = v, target = null, type, lc;
      if (typeof v === "string") {
        v = v.trim();
        if (v.length > 0) {
          try {
            o = JSON.parse(v);
            //console.log("o",o);
          } catch(ex) {
            //console.log(ex);
            o = v;
          }
        }
      }
      //console.log(typeof o,o);
      if (typeof o === "string") {
        if (cfgTargets.hasOwnProperty(o)) {
          target = cfgTargets[o];
          target.key = o;
          targets.push(target);
        } else {
          // TODO error here?
        }
      } else if (o && Array.isArray(o)) {
        o.forEach(function(o2){
          self._checkTarget(o2,cfgTargets,targets,config);
        });
      } else if (o !== null && typeof o === "object" && config.allowDynamicTarget) {
        if (typeof o.url === "string" && o.url.length > 0) {
          if (typeof o.type === "string") {
            lc = o.type.toLowerCase();
            if (lc === "csw2") {
              o.type = "csw";
              o.cswVersion = "2.0.2";
            } else if (lc === "csw3") {
              o.type = "csw";
              o.cswVersion = "3.0.0";
            }
          }
          if ((o.url.indexOf("http://") === 0) || (o.url.indexOf("https://") === 0)) {
            type = gs.target.types[o.type];
            if (type && typeof type.newInstance === "function") {
              target = type.newInstance(o);
            }
            if (target) {
              // TODO pass full parameters to the target (from, size, ...)
              target.requestObject = o;
              if (typeof o.key === "string" && o.key.length > 0) {
                target.key = o.key;
              } else {
                //target.key = o; TODO?
              }
              if (typeof o.filter === "string" && o.filter.length > 0) {
                target.requiredFilter = o.filter;
              }
              if (o.profile && gs.target.profiles[o.profile]) {
                gs.target.profiles[o.profile].prepareTarget(target);
              }
              targets.push(target);
            }
          }
        }
      }
    }},

    _sendError: {writable:true,value:function(responseHandler, task, error, message) {
      var response, asJson = true, msg = "Search error";
      if (task) {
        response = task.response;
      } else {
        response = gs.Object.create(gs.base.Response);
      }
      if (typeof message === "string" && message.length > 0) {
        msg = message;
      } else if (error && typeof error.message === "string" && error.message.length > 0) {
        // TODO return the message?
        msg = "Search error: "+error.message;
      }
      if (!task || !task.hasError) {
        if (task) task.hasError = true;
        // TODO asJson?
        if (asJson) {
          msg = JSON.stringify({"error": msg});
          response.put(response.Status_INTERNAL_SERVER_ERROR,response.MediaType_APPLICATION_JSON,msg);
        } else {
          response.put(response.Status_INTERNAL_SERVER_ERROR,response.MediaType_TEXT_PLAIN,msg);
        }
      }
      if (typeof responseHandler === "function") {
        // TODO include response.headers?
        responseHandler(response.status,response.mediaType,response.entity,response.headers);
      }

      try {
        var printErrors = true; // TODO?
        if (printErrors) {
          if (error) console.log("Search error",error);
          if (error && task && task.context && task.context.isNashorn) {
            error.printStackTrace();
          }
        }
      } catch(ex) {
        console.log(ex);
      }
    }}

  });

}());
