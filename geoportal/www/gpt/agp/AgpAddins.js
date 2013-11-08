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
/*

An example insert for an ArcGIS Portal home/index.html page:

    Place gxe-proxy.jsp at: home/js/custom/gpt/gxe-proxy.jsp
    Modify gxe-proxy.jsp if proxy access needs to be restricted.

    <!-- Customization for extended metadata (view/edit) -->
    <script type="text/javascript">
      gptConfig = {
        "GptCore.serverContextPath": "http://host:port/geoportal",
        "ItemMetadata.enabled": true,
        "ItemMetadata.allowSaveDraft": true,
        "ItemMetadata.gxeProxyUrl": "js/custom/gpt/gxe-proxy.jsp",
        "ItemMetadata.useGxeProxyUrl": true
      }
    </script>
    <script type="text/javascript" 
      src="http://host:port/geoportal/gpt/agp/AgpAddins.js">
    </script>
    <!-- ----------------------------------------------- -->

 */
(function(){
    
  var baseConfig = {
      
    "GptCore.clientContextPath": null,
    "GptCore.serverContextPath": null,
    "GptCore.allowDynamicClients": true,
    "GptCore.preventClientCache": false,
      
    "ItemMetadata.enabled": true,
    "ItemMetadata.allowSaveDraft": false,
    "ItemMetadata.documentTypeKeys": null,
    "ItemMetadata.gxeProxyUrl": "js/custom/gpt/gxe-proxy.jsp",
    "ItemMetadata.useGxeProxyUrl": true,
    
    /* used to fetch the gemet themes endpoint url
     * */
    "ItemMetadata.gemetThemesProxy":""
    
  };
  
  var addinHelper = {
    wasConfigEnsured: false,
    wasConfigValid: false,
    
    configureAddins: function() {
      if (typeof(esriGeowConfig) != "undefined") {
        dojo.addOnLoad(dojo.hitch(this,function(){
          
          if ((typeof(itemWidget) != "undefined")) {
            dojo.connect(itemWidget,"onItemLoad",dojo.hitch(this,function(item){
              if (item) this.configureItemAddins();
            }));
          }
          
        }));
      }
    },
        
    configureItemAddins: function() {
      var aCss, aScripts;
      try {
        if (this.ensureConfig()) {
          
          if (baseConfig["ItemMetadata.enabled"]) {
            aCss = ["/gpt/gxe/gxe.css"];
            aScripts = ["/gpt/gxe/gxe.js",
                        "/gpt/gxe/gemet.js",
                        "/gpt/gxe/MetadataPanel.js",
                        "/gpt/agp/ItemMetadataAddin.js"];
            this.loadClientResources(aCss,aScripts,true,function(bOk){
              if (bOk) {
                var oCtl = new gpt.agp.ItemMetadataButton();
                oCtl.initialize(itemWidget,esriGeowConfig.restBaseUrl);
              }
            });
          }
          
        }
      } catch (error) {
        console.log(error);
      }
    },
    
    ensureConfig: function() {     
      var sProp, oProp;
      try {
        if (this.wasConfigEnsured) return this.wasConfigValid;
        this.wasConfigEnsured = true;
        this.wasConfigValid = false;
        if (typeof(gptConfig) != "undefined") {
          this.mixinConfig(gptConfig,true);
        }
        if (typeof(gptMixinConfig) != "undefined") {
          this.mixinConfig(gptMixinConfig,true);
        }
        var oProp1 = baseConfig["GptCore.serverContextPath"];
        var oProp2 = baseConfig["GptCore.clientContextPath"];
        if ((typeof(oProp2) == "undefined") || (oProp2 == null)) {
          baseConfig["GptCore.clientContextPath"] = oProp1;
        }
        if ((typeof(oProp1) == "undefined") || (oProp1 == null)) {
          console.log("Missing config, \"GptCore.serverContextPath\"");
        } else {
          this.wasConfigValid = true;
        }
        return this.wasConfigValid;
      } catch (error) {
        this.wasConfigValid = false;
        console.log(error);
        return this.wasConfigValid;
      }
    },
    
    ensureCore: function(fCallback) { 
      if (typeof(GptCore) != "undefined") {
        GptCore.configure(baseConfig);
        fCallback(true);
      } else {
        if (!baseConfig["GptCore.allowDynamicClients"]) {
          fCallback(true);
        } else {
          var sUrl = baseConfig["GptCore.clientContextPath"]+"/gpt/GptCore.js";
          dojo.io.script.get({
            url: sUrl,
            timeout: 5000,
            error: function(responseObject,ioArgs) {fCallback(false);},
            load: function(responseObject,ioArgs) {
              if (typeof(GptCore) == "undefined") {
                fCallback(false);
              } else {
                GptCore.configure(baseConfig);
                fCallback(true);
              }
            }
          });
        }
      }
    },
    
    loadClientResources: function(aCss,aScripts,bRelative,fCallback) { 
      this.ensureCore(dojo.hitch(this,function(bOk){
        if (!bOk) {
          fCallback(false);
        } else {
          if (typeof(GptCore) == "undefined") {
            fCallback(false);
          } else { 
            var i,n;
            if ((aCss != null) && (aCss.length > 0)) {
              if (bRelative) {
                n = aCss.length;
                for (i=0; i<n; i++) {
                  aCss[i] = GptCore.makeClientUri(aCss[i]);
                }
              }
              GptCore.loadCss(aCss);
            }
            if ((aScripts != null) && (aScripts.length > 0)) {
              if (bRelative) {
                n = aScripts.length;
                for (i=0; i<n; i++) {
                  aScripts[i] = GptCore.makeClientUri(aScripts[i]);
                }
              }
              GptCore.loadScripts(aScripts,dojo.hitch(this,function(bOk){
                fCallback(bOk);
              }));
            }
          }
        } 
      }));
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
          if (bSet) baseConfig[sProp] = oConfig[sProp];
        }
      }
    }
    
  };
  
  /** Auto Configure. */
  (function(){
    addinHelper.configureAddins();
  }());
  
}());



