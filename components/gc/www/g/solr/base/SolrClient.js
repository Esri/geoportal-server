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
define(["dojo/_base/declare",
        "dojo/_base/lang",
        "dojo/dom",
        "dojo/_base/xhr",
        "dojo/io/script",
        "dojo/io-query",
        "dijit/_WidgetBase",
        "./QComponent",
        "./QRequest",
        "./QClause"], 
function(declare, lang, dom, dxhr, scriptIO, ioQuery, _WidgetBase,
         QComponent, QRequest, QClause) {
  
  var oThisClass = declare("g.solr.base.SolrClient", [_WidgetBase,QComponent], {
    activeQClauses: null,
    autoFetch: true,
    collectionUrl: null,
    lastQueryUrl: null,
    registeredQComponents: null,
    
    postMixInProperties: function() {
      this.inherited(arguments);
      this.activeQClauses = new Array();
      this.registeredQComponents = new Array();
      this.registeredQComponents.push(this);
      
      var i, n, aClauses = this.filters;
      if (aClauses) {
        n = aClauses.length;
        for (i=0;i<n;i++) {
          var qClause = new QClause(aClauses[i]);
          this.activeQClauses.push(qClause);
        }
      }
    },
    
    startup: function() {
      if (this._started) {return;}
      this.inherited(arguments);
      if (this.autoFetch) {
        this.fetch({});
      }
    },
    
    appendQParameters: function(qRequest) {
      qRequest.putUrlParameter("start",0);
      qRequest.putUrlParameter("rows",0);
      qRequest.putUrlParameter("wt","json");
      qRequest.putUrlParameter("facet",true);
      qRequest.putUrlParameter("q","*:*");
      qRequest.pushQClauses(this.activeQClauses);
    },

    fetch: function() {
      var qRequest = new QRequest();
      var sUrl = this.collectionUrl+"/select";
      var i, n = this.registeredQComponents.length;
      for (i=0;i<n;i++) {
        this.registeredQComponents[i].appendQParameters(qRequest);
      }
      //this.appendQParameters(qRequest);
      var urlParams = qRequest.urlParams;
      
      //console.log("** fetch",urlParams);
      
      var elWorking = dom.byId("gWorking");
      var showError = function(error) {
        if (elWorking) elWorking.style.color = "red";
        if (elWorking) elWorking.innerHTML = "...";
      };
      var showWorking = function() {
        if (elWorking) elWorking.style.color = "black";
        if (elWorking) elWorking.innerHTML = "...";
      };
      var hideWorking = function() {
        if (elWorking) elWorking.innerHTML = "";
      };
      
      showWorking();
      scriptIO.get({
        content: urlParams,
        callbackParamName: "json.wrf",
        timeout: 15000,
        url: sUrl,
        error: lang.hitch(this,function(err,ioArgs) {
          // TODO: handle error
          //errorHandler(err,request);
          showError(err);
        }),
        load: lang.hitch(this,function(response,ioArgs) {
          hideWorking();
          urlParams.indent = "on";
          var lastQStr = ioQuery.objectToQuery(urlParams);
          var lastUri = sUrl+"?"+lastQStr;
          this.lastQueryUrl = lastUri;
      
          var i, n = this.registeredQComponents.length;
          for (i=0;i<n;i++) {
            this.registeredQComponents[i].handleQResponse(response);
          }
        })
      });
    },
    
    forEachQComponent: function(fCallback) {
      var i, n = this.registeredQComponents.length;
      for (i=0;i<n;i++) {
        fCallback(this.registeredQComponents[i]);
      }
    },
    
    registerQComponent: function(oQComponent) {
      this.registeredQComponents.push(oQComponent);
    }
    
  });
  
  return oThisClass;
});
