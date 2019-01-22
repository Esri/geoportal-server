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

gsConfig = {
  isNashorn: true
};
load("classpath:gs/all.js");

/* entry-point from the JVM */
function execute(nhRequest,sRequestInfo,sSelfInfo) {
  try {
    var requestInfo = JSON.parse(sRequestInfo);
    //requestInfo.taskOptions.verbose = true;

    // to override the base URL if you have a reverse proxy
    //requestInfo.baseUrl = "https://www.geoportal.com/geoportal";

    var selfInfo, accessQuery = null;
    if (typeof sSelfInfo === "string" && sSelfInfo.length > 0) {
      //print("sSelfInfo",sSelfInfo);
      selfInfo = JSON.parse(sSelfInfo);
      accessQuery = _makeAccessQuery(selfInfo);
    }
    
    try {
      // some backward compatibility end points
      if (requestInfo && requestInfo.parameterMap && 
          typeof requestInfo.requestUrl === "string") {
        var hasF = !!requestInfo.parameterMap.f;
        var v = requestInfo.requestUrl;
        if (v.indexOf("?") !== -1) v = v.substring(0,v.indexOf("?"));
        if (v.indexOf("/Eros") != -1) {
          if (!hasF) requestInfo.parameterMap.f = "eros";
        } else if (v.endsWith("/search") || v.endsWith("/search/")) {
          if (!hasF && !requestInfo.parameterMap.target && !requestInfo.parameterMap.targets) {
            requestInfo.parameterMap.f = "json-source";
          }
        }
      }
    } catch(ex2) {
      print(ex2);
    }

    var processor = gs.Object.create(gs.context.nashorn.NashornProcessor).mixin({
      newConfig: function() {
        var config = gs.Object.create(gs.config.Config);
        if (selfInfo && selfInfo.elastic && selfInfo.elastic.searchUrl) {
          // self target for a Geoportal instance
          var targets = config.getTargets();
          targets.self = gs.Object.create(gs.target.elastic.GeoportalTarget).mixin({
            "searchUrl": selfInfo.elastic.searchUrl,
            "itemBaseUrl": requestInfo.baseUrl+"/rest/metadata/item"
          });
          if (accessQuery) {
            targets.self.accessQuery = accessQuery;
          }
          if (typeof selfInfo.elastic.username === "string") {
            // for x-pack
            targets.self.username = selfInfo.elastic.username;
            targets.self.password = selfInfo.elastic.password;
          }
          config.defaultTarget = "self";
        }
        return config;
      }
    });

    processor.execute(requestInfo,function(status,mediaType,entity,headers){
      var hm = null;
      if (Array.isArray(headers) && headers.length > 0) {
        hm = new java.util.HashMap();
        headers.forEach(function(header){
          hm.put(header.name,header.value);
        });
      }
      if (requestInfo.taskOptions.verbose) print(entity);
      nhRequest.putResponse(status,mediaType,entity,hm);
    });

  } catch(error) {
    // TODO include the error message in the json response?
    print("Error processing request.");
    print(error);
    if (error) error.printStackTrace();
    var msg = "{\"error\": \"Error processing request.\"}";
    nhRequest.putResponse(500,"application/json",msg,null);
  }

}

/* entry-point from the JVM */
function mergeAccessQuery(sSelfInfo,sUrl,sBody) {
  var wasModified = false, accessQuery, body, q, musts = [];
  if (typeof sSelfInfo === "string" && sSelfInfo.length > 0) {
    //print("sSelfInfo",sSelfInfo);
    selfInfo = JSON.parse(sSelfInfo);
    accessQuery = _makeAccessQuery(selfInfo);
    if (accessQuery) {
      if (typeof sBody === "string" && sBody.indexOf("{") === 0) {
        body = JSON.parse(sBody);
        if (body.query) {
          if (body.query.bool && body.query.bool.must) {
            body.query.bool.must.push(accessQuery);
            sBody = JSON.stringify(body);
            wasModified = true;
          } else {
            musts.push(JSON.parse(JSON.stringify(body.query))); // TODO ??
            musts.push(accessQuery);
            body.query = {"bool": {"must": musts}};
            sBody = JSON.stringify(body);
            wasModified = true;
          }
        } else {
          body.query = accessQuery;
          sBody = JSON.stringify(body);
          wasModified = true;
        }
      } else {
        body = {"query": accessQuery};
        sBody = JSON.stringify(body);
        wasModified = true;
      }      
    }
  }
  var result = {sBody: sBody, wasModified: wasModified};
  return JSON.stringify(result);
}

function _makeAccessQuery(selfInfo) {
  var q, query = null, musts = [];
  var isAdmin = false;
  var username = null;
  var groups = null;
  var supportsApprovalStatus = false;
  var supportsGroupBasedAccess = false;
  if (selfInfo && selfInfo.elastic && selfInfo.elastic.access) {
    isAdmin = selfInfo.elastic.access.isAdmin;
    username = selfInfo.elastic.access.username;
    groups = selfInfo.elastic.access.groups;
    supportsApprovalStatus = selfInfo.elastic.access.supportsApprovalStatus;
    supportsGroupBasedAccess = selfInfo.elastic.access.supportsGroupBasedAccess;
  }

  var approvalQuery = null;
  var approvalShoulds = [];
  if (!isAdmin && supportsApprovalStatus) {
    // isAdmin || sys_owner_s === username || 
    // sys_approval_status_s is missing ||
    // sys_approval_status_s === "approved" || sys_approval_status_s === "reviewed"
    if (typeof username === "string" && username.length > 0) {
      approvalShoulds.push({"term": {"sys_owner_s": username}});
    }
    approvalShoulds.push({"terms": {"sys_approval_status_s": ["approved","reviewed"]}});
    approvalShoulds.push({"bool": {"must_not": {"exists": { "field": "sys_approval_status_s"}}}});
    if (approvalShoulds.length > 0) {
      approvalQuery = {"bool": {"should": approvalShoulds}};
      musts.push(approvalQuery);
      //console.log("approvalQuery",JSON.stringify({"query": approvalQuery}));
    }
  }
  
  var accessQuery = null;
  var accessShoulds = [];
  if (!isAdmin && supportsGroupBasedAccess) {
    // isAdmin || sys_owner_s === username ||
    // sys_access_s is missing || sys_access_s === "public" ||
    // one of the current user's groups is in sys_access_groups_s
    if (typeof username === "string" && username.length > 0) {
      accessShoulds.push({"term": {"sys_owner_s": username}});
    }
    accessShoulds.push({"term": {"sys_access_s": "public"}});
    accessShoulds.push({"bool": {"must_not": {"exists": { "field": "sys_access_s"}}}});
    if (typeof groups === "string" && groups.length > 0) groups= [groups];
    if (Array.isArray(groups)) {
      accessShoulds.push({"terms": {"sys_access_groups_s": groups}});
    }
    if (accessShoulds.length > 0) {
      accessQuery = {"bool": {"should": accessShoulds}};
      musts.push(accessQuery);
      //console.log("accessQuery",JSON.stringify({"query": accessQuery}));
    }
  }
  
  if (musts.length === 1) {
    query = musts[0];
  } else if (musts.length > 1) {
    query = {"bool": {"must": musts}};
  }
  //if (query) print("query",JSON.stringify({"query": query}));
  return query;
}
