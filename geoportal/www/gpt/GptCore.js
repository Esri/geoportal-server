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
  
  window.GptCore = {

    config: {
      "GptCore.clientContextPath": "/geoportal",
      "GptCore.serverContextPath": "/geoportal",
      
      "GptCore.allowDynamicClients": true,
      "GptCore.clientVersion": "1.2.1",
      "GptCore.preventClientCache": false,
      "GptCore.timeoutForJsonp": 5000,
      "GptCore.timeoutForScript": 5000
    },
        
    configure: function(oConfig) {
      var sProp;
      if (typeof(oConfig) != "undefined") {
        for (sProp in oConfig) {
          this.config[sProp] = oConfig[sProp];
        }
      }
    },
    
    echoConfig: function(oConfig,bSimpleTypesOnly) {  
      console.log("GptCore.config ------------------");
      for (sProp in this.config) {
        console.log(sProp+"="+this.config[sProp]);
      }
    },
      
    /**
     * Load a CSS file.
     */
    loadCss: function(uri) {
      try {
        if (!this.config["GptCore.allowDynamicClients"]) {
        } else {
          var aUris = new Array();
          if (typeof(uri) == "string") {
            aUris[0] = uri;
          } else if (typeof(uri) == 'object') {
            if (typeof(uri.length) != "undefined") {
              aUris = uri;
            }
          }
          var elHead = document.getElementsByTagName("head").item(0);
          var i, n = aUris.length, elCss;
          for (i=0; i<n; i++) {
            //console.debug("Loading css: "+aUris[i]);
            elCss = document.createElement("link");
            elCss.rel = "stylesheet";
            elCss.type = "text/css";
            elCss.href = aUris[i];
            elHead.appendChild(elCss);
          }
        }
      } catch (error) {
        console.log(error);
      }
    },
    
    getClientContextPath: function() {
      return this.config["GptCore.clientContextPath"];
    },
    
    getServerContextPath: function() {
      return this.config["GptCore.serverContextPath"];
    },
        
    loadDeclaredConfig: function(oDeclaredConfig,fCallback) {
      var bOk = true, sProp, oProp;
      if ((typeof(oDeclaredConfig) != "undefined") && (oDeclaredConfig != null)) {
        for (sProp in oDeclaredConfig) {
          oProp = this.config[sProp];
          if (typeof(oProp) != "undefined") {
            oDeclaredConfig[sProp] = oProp;
          }
        }
      }
      if ((typeof(fCallback) == "function")) fCallback(bOk);
    },
    
    /**
     * Load a set of declared localization keys.
     */
    loadDeclaredI18N: function(oI18N,fCallback) {
      var sKey, sKeys = "";
      for (sKey in oI18N) {
        if (sKeys.length > 0) sKeys += ",";
        sKeys += sKey;
      }
      if ((sKeys == null) || (sKeys.length == 0)) {
        fCallback();
      } else {
        var sUrl = this.getServerContextPath()+"/gxe/i18n";
        sUrl += "?keys="+encodeURIComponent(sKeys);
        dojo.io.script.get({
          callbackParamName: "callback",
          timeout: this.config["GptCore.timeoutForJsonp"],
          url: sUrl,
          error: dojo.hitch(this,function(responseObject,ioArgs) {
            // TODO: can this be recursive if there is an error
            fCallback();
          }),
          load: dojo.hitch(this,function(responseObject,ioArgs) {
            if ((typeof(responseObject) == "undefined") || (responseObject == null)) {
            } else {
              var oError = responseObject.error;
              if ((typeof(oError) != "undefined") && (oError != null)) {
              } else {  
                var oRespI18N = responseObject.i18n;
                if ((typeof(oRespI18N) == "undefined") || (oRespI18N == null)) {
                } else if (oRespI18N.length == 0) {
                } else {
                  var sRespKey, oRespValue, bSet;
                  for (sRespKey in oI18N) {
                    bSet = false;
                    oRespValue = oRespI18N[sRespKey];
                    if (oRespValue != null) {
                      bSet = true;
                      if (typeof(oRespValue == "string")) {
                        if (oRespValue.indexOf("??") == 0) bSet = false;
                      }
                    }
                    if (bSet) oI18N[sRespKey] = oRespValue;
                  }
                }
              }
            }
            // TODO can this be recursive if the is an error
            fCallback();
          })
        });
      }
    },
    
    /**
     * Load a script.
     */
    loadScript: function(sUri,fCallback) {
      if (!this.config["GptCore.allowDynamicClients"]) {
        fCallback(true);
      } else {
        //console.debug("Loading script: "+sUri);
        var bOk = false;
        dojo.io.script.get({
          url: sUri,
          timeout: this.config["GptCore.timeoutForScript"],
          error: dojo.hitch(this,function(responseObject,ioArgs) {
            if ((typeof(fCallback) == "function")) fCallback(bOk);
            console.log(responseObject);
          }),
          load: dojo.hitch(this,function(responseObject,ioArgs) {
            bOk = true;
            if ((typeof(fCallback) == "function")) fCallback(bOk);
            })
          });
        }
      },
 
    /**
     * Load an array of scripts.
     */
    loadScripts: function(aUris,fCallback) {
      if (typeof(aUris) == "string") {
        this.loadScript(aUris,fCallback);
      } else {
        var nOk = 0, nTotal = aUris.length;
        this._loadScripts(aUris,nOk,nTotal,dojo.hitch(this,function(bOk){
          if ((typeof(fCallback) == "function")) fCallback(bOk);
        }));
      }
    },
    
    /**
     * Load an array of scripts (recursive execution).
     */
    _loadScripts: function(aUris,nOk,nTotal,fCallback) {
      var sUri = aUris.shift();
      this.loadScript(sUri,dojo.hitch(this,function(bScriptOk){
        if (bScriptOk) nOk++;
        if (aUris.length > 0) {
          this._loadScripts(aUris,nOk,nTotal,fCallback);
        } else {
          var bOk = (nOk == nTotal);
          if ((typeof(fCallback) == "function")) fCallback(bOk);
        }
      }));
    },
    
    makeClientUri: function(sRelativeUri) {
      var s = sRelativeUri;
      if (s.indexOf("/") != 0) s = "/"+s;
      s = this.getClientContextPath()+s;
      var v = this.config["GptCore.clientVersion"];
      if (v != null) s += "?v="+encodeURIComponent(v);
      if (this.config["GptCore.preventClientCache"]) {
        if (v != null) s += "&";
        else s += "?";
        s += "t="+encodeURIComponent(""+(new Date()).getTime());
      } 
      return s;
    },
    
    makeServerUri: function(sRelativeUri) {
      var s = sRelativeUri;
      if (s.indexOf("/") != 0) s = "/"+s;
      s = this.getServerContextPath()+s;
      return s;
    },
    
    mixinConfig: function(oConfig,bSimpleTypesOnly) {
      var bSet,sProp,oProp,sType;
      if (typeof(oConfig) != "undefined") {
        for (sProp in oConfig) {
          bSet = false;
          oProp = oConfig[sProp];
          sType = typeof(oProp);
          if (sType != "undefined") {
            if (oProp == null) {
              bSet = true;
            } else if (!bSimpleTypesOnly) {
              bSet = true;
            } else {
              if ((sType == "string") || (sType == "number") || (sType == "boolean")) {
                bSet = true;
              }
            }
          }
          if (bSet) this.config[sProp] = oConfig[sProp];
        }
      }
    }
  
  };
  
  /** Auto Configure. */
  (function(){
    if (typeof(GptCore) != "undefined") {
      if (typeof(gptConfig) != "undefined") {
        GptCore.configure(gptConfig);
      }
      if (typeof(gptMixinConfig) != "undefined") {
        GptCore.configure(gptMixinConfig);
      }
    }
  }());
  
}());


