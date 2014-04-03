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
dojo.provide("gpt.agp.ItemMetadataButton");
dojo.declare("gpt.agp.ItemMetadataButton",null,{
  
  agpSharingUrl: null,
  canEdit: false,
  documentClient: null,
  itemTitle: null,
  originalXml: null,
  
  i18n: {
    "catalog.gxe.dialog.anchor.metadata": "Metadata"
  },
  
  initialize: function(agpItemWidget,sAgpSharingUrl) {
    if (dijit.byId("idItemMetadataButton") != null) return;
    if (!agpItemWidget) return;
    if (!agpItemWidget.item) return;
    this.agpSharingUrl = sAgpSharingUrl; 
    this.canEdit = false;
    this.itemTitle = agpItemWidget.item.title;;
    this.originalXml = null;
    this.documentClient = new gpt.gxe.AgpDocumentClient();
    this.documentClient.initialize(agpItemWidget,sAgpSharingUrl);
   
    var oUser = agpItemWidget.util.getUser();
    if (oUser) {
      this.canEdit = (agpItemWidget.item["owner"] == oUser.email);
    }
    
    this.documentClient.loadXml({
      error: dojo.hitch(this,function(responseObject,ioArgs) {
        console.log(responseObject);
      }),
      load: dojo.hitch(this,function(responseObject,ioArgs) {
        var sXml = responseObject;
        var bHasMetadata = (sXml != null) && (sXml.length > 0);
        if (bHasMetadata) this.originalXml = sXml;
        if (bHasMetadata || this.canEdit) {
          GptCore.loadDeclaredI18N(this.i18n,dojo.hitch(this,function(){
            this.addMetadataButton();
          }));
        }
      })
    });
    
  },
  
  addMetadataButton: function() {
    var sLabel = this.i18n["catalog.gxe.dialog.anchor.metadata"];
    var elButtonNode = document.createElement("span");
    var elOwnerTools = dojo.byId("item-owner-toolbar");
    //elOwnerTools.parentNode.insertBefore(elButtonNode,elOwnerTools);
    elOwnerTools.parentNode.appendChild(elButtonNode);
    var djButton = new dijit.form.Button({
      id: "idItemMetadataButton",
      label: sLabel
    },elButtonNode);
    dojo.addClass(djButton.domNode,"readOnly");
    dojo.addClass(djButton.domNode,"calcite narrow transparent");
    dojo.connect(djButton,"onClick",this,dojo.hitch(this,function() {
      var oMetadata = new gpt.gxe.MetadataPanel();
      oMetadata.initialize();
      oMetadata.documentClient = this.documentClient;
      oMetadata.canEdit = this.canEdit;
      oMetadata.title = this.itemTitle;
      //oMetadata.originalXml = this.originalXml; // Force a re-read
      oMetadata.showDialog();
    }));
  }
});

dojo.provide("gpt.gxe.AgpDocumentClient");
dojo.declare("gpt.gxe.AgpDocumentClient",gpt.gxe.DocumentClient,{
  agpItemWidget: null,
  agpSharingUrl: null,
  token: null,
  
  // initialize
  initialize: function(agpItemWidget,sAgpSharingUrl) {
    this.agpItemWidget = agpItemWidget; 
    this.agpSharingUrl = sAgpSharingUrl; 
    var sToken = agpItemWidget.token;
    if ((typeof(sToken) != "undefined") && (sToken != null)) {
      if (sToken.indexOf("?") == 0) sToken = sToken.substring(1);
      if (sToken.indexOf("token=") == 0) sToken = sToken.substring(6);
      this.token = sToken;
    }
  },
  
  loadXml: function(params) {
    var sItemId = this.agpItemWidget.item.id;
    var sUrl = this.agpSharingUrl+"content/items/";
    sUrl += encodeURIComponent(sItemId);
    sUrl += "/info/metadata/metadata.xml";
    if (this.token != null) {
      sUrl += "?token="+this.token;
    }
            
    dojo.xhrGet({
      handleAs: "text",
      preventCache: true,
      url: sUrl,
      error: dojo.hitch(this,function(responseObject,ioArgs) {
        // TODO this indicates that the item has no metadata?
        this.handleLoad(params,null,ioArgs);
      }),
      load: dojo.hitch(this,function(responseObject,ioArgs) {
        if ((typeof(responseObject) == "undefined") || (responseObject == null)) {
          this.handleLoad(params,null,ioArgs);
        } else {
          var oError = responseObject.error;
          if ((typeof(oError) != "undefined") && (oError != null)) {
            this.handleError(params,oError,ioArgs);
          } else {
            this.handleLoad(params,responseObject,ioArgs);
          }
        }
      })
    });
  },
  
  saveXml: function(params) {
    var sXml = params.xml;
    var sOwner = this.agpItemWidget.item.owner;
    var sItemId = this.agpItemWidget.item.id;
    var sFolder = this.agpItemWidget.item.folderId;
    
    var u = this.agpSharingUrl;
    u += "content/users/"+encodeURIComponent(sOwner);
    if ((typeof(sFolder) != "undefined") && (sFolder != null) &&
        (sFolder.length > 0)) {
      u += "/"+encodeURIComponent(sFolder);
    }
    u += "/items/"+encodeURIComponent(sItemId)+"/update";
    u += "/info/metadata/metadata.xml";
    u += "?token="+this.token;
    u += "&f=json&overwrite=true";    

    var sBoundary = "387F8C2A-CFAB-443C-863B-B180E79B05F4";
    var aData = [];
    aData.push(
      "--"+sBoundary,
      "Content-Disposition: form-data; name=\"metadata\"; filename=\"metadata.xml\"",
      "Content-Type: text/xml","",sXml);
    aData.push("--"+sBoundary+"--","");
    var sData = aData.join("\r\n");
    
    dojo.xhrPost({
      handleAs: "json",
      preventCache: true,
      url: u,
      headers: {"Content-Type": "multipart/form-data; boundary="+sBoundary},
      postData: sData, 
      error: dojo.hitch(this,function(responseObject,ioArgs) {
        this.handleError(params,responseObject,ioArgs);
      }),
      load: dojo.hitch(this,function(responseObject,ioArgs) {
        if ((typeof(responseObject) == "undefined") || (responseObject == null)) {
          this.handleError(params,new Error("Empty response."),ioArgs);
        } else {
          var oError = responseObject.error;
          if ((typeof(oError) != "undefined") && (oError != null)) {
            this.handleError(params,oError,ioArgs);
          } else {  
            this.handleLoad(params,responseObject,ioArgs);
          }
        }
      })
    });
    
  }
});

// popup map control for the metadata editor
dojo.provide("gxe.control.Map");
dojo.declare("gxe.control.Map",gxe.control.Control,{
  _inputEnvelopeXMinId: null,
  _inputEnvelopeYMinId: null,
  _inputEnvelopeXMaxId: null,
  _inputEnvelopeYMaxId: null,

  build: function(htmlParentElement,domProcessor,domNode) {
    this.inherited(arguments);
    this.htmlElement.style.display = "none";
    
    var elUseMap = document.createElement("button");
    elUseMap.className = "gxeUseMapButton";
    elUseMap.appendChild(document.createTextNode(
        this.context.getI18NString("map.useMap")));
    this.htmlElement.parentNode.insertBefore(elUseMap,this.htmlElement);

    var el = this.htmlElement.parentNode;
    while (el != null) {
      var nl = dojo.query("[gxeMapPart='envelope_container']",el);
      if (nl.length == 1) {
        var elC = nl[0];
        var nlN = dojo.query("[gxeMapPart='envelope_north']",elC);
        var nlS = dojo.query("[gxeMapPart='envelope_south']",elC);
        var nlE = dojo.query("[gxeMapPart='envelope_east']",elC);
        var nlW = dojo.query("[gxeMapPart='envelope_west']",elC);
        if ((nlN.length == 1) && (nlS.length == 1) &&
            (nlE.length == 1) && (nlW.length == 1)) {
          this._inputEnvelopeXMinId = nlW[0].id;
          this._inputEnvelopeYMinId = nlS[0].id;
          this._inputEnvelopeXMaxId = nlE[0].id;
          this._inputEnvelopeYMaxId = nlN[0].id;
        }
        break;
      }
      el = el.parentNode;
    }
    
    dojo.connect(elUseMap,"onclick",this,"_useMap");
  },

  _useMap: function(e) {

    var curExt = null;
    var xmin = parseFloat(dojo.byId(this._inputEnvelopeXMinId).value);
    var ymin = parseFloat(dojo.byId(this._inputEnvelopeYMinId).value);
    var xmax = parseFloat(dojo.byId(this._inputEnvelopeXMaxId).value);
    var ymax = parseFloat(dojo.byId(this._inputEnvelopeYMaxId).value);  
    if (!isNaN(xmin) && !isNaN(ymin) && !isNaN(xmax) && !isNaN(ymax)) {
      curExt = [[xmin,ymin],[xmax,ymax]];
    }

    var alerter = dojo.subscribe("onExtentUpdate",this,dojo.hitch(this,function(extent) {
      var el = dojo.byId(this._inputEnvelopeXMinId);
      if (el != null) {
        dojo.byId(this._inputEnvelopeXMinId).value = (Math.round(extent[0][0] * 100.0) / 100);
        dojo.byId(this._inputEnvelopeYMinId).value = (Math.round(extent[0][1] * 100.0) / 100);
        dojo.byId(this._inputEnvelopeXMaxId).value = (Math.round(extent[1][0] * 100.0) / 100);
        dojo.byId(this._inputEnvelopeYMaxId).value = (Math.round(extent[1][1] * 100.0) / 100);   
      }
    }));

    var extDlg = dijit.byId("extent-dialog");
    if (extDlg != null) {
      dojo.connect(extDlg,"onHide",this,dojo.hitch(this,function(e) {
        dojo.unsubscribe(alerter);
      }));
    }

    var dlg = esri.arcgisonline.sharing.dijit.dialog.ExtentDlg.prototype.statics.getInstance();
    if (curExt == null) dlg.show();
    else dlg.show(curExt );
  }
  
});
