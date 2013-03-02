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
    
/**
 * @class An item cart.
 * @name gpt.form.Cart
 */
dojo.provide("gpt.form.Cart");
dojo.declare("gpt.form.Cart",null,{
  
  approximateSize: 0,
  contentNode: null,
  contextPath: null,
  controlBarNode: null,
  countNode: null,
  enabled: true,
  itemsNode: null,
  maxItems: 1,
  parentDialog: null,
  processor: null,
  title: "Cart",
  
  
  // configuration strings
  config: {},

  // i18n strings
  i18n: {
    "catalog.cart.check.tip": "Check to add item to cart",
    "catalog.cart.clear": "Clear Items",
    "catalog.cart.close": "Close",
    "catalog.cart.isfull": "The cart is full; maximum items: {0}",
    "catalog.cart.item.count": "({0} items)",
    "catalog.cart.item.count.one": "(1 item)",
    "catalog.cart.max.tip": "Maximum items: {0}",
    "catalog.cart.process": "Process",
    "catalog.cart.prompt": "",
    "catalog.cart.remove.tip": "Remove Item",
    "catalog.gxe.dialog.invalidResponse": "Invalid response."
  },
    
  // initialize
  initialize: function() {
    GptCore.loadDeclaredConfig(this.config);
  },
  
  /**
   * Builds the user interface.
   * @function 
   * @name buildUI
   * @memberOf gpt.form.Cart#
   */
  buildUI: function() {
    var sPrompt = this.i18n["catalog.cart.prompt"];
    if ((typeof(sPrompt) != "undefined") && (sPrompt != null) && 
        (sPrompt.length > 0)) {
      var elPrompt = document.createElement("span");
      elPrompt.className = "prompt";
      dojo.style(elPrompt,{display: "block"});
      elPrompt.appendChild(document.createTextNode(sPrompt));
      this.contentNode.appendChild(elPrompt);
    }
    
    this.buildUIControlBar();
    
    this.itemsNode = document.createElement("div");
    dojo.style(this.itemsNode,{
      height: "2px",
      border: "1px solid #ccc",
      overflowY: "auto"
    });
    this.contentNode.appendChild(this.itemsNode);
    this.refreshItems();
  },
  
  /**
   * Builds the user interface control bar.
   * @function 
   * @name buildUIControlBar
   * @memberOf gpt.form.Cart#
   */
  buildUIControlBar: function() {
    var elButton;
    
    this.controlBarNode = document.createElement("div");
    var elControlBar = this.controlBarNode;
    elControlBar.className = "gptCartControlBar";
    this.contentNode.appendChild(elControlBar);
    
    elControlBar.appendChild(this.makeDownloadForm());
    
    elButton = document.createElement("button");
    elButton.appendChild(document.createTextNode(this.i18n["catalog.cart.process"]));
    elControlBar.appendChild(elButton);
    dojo.connect(elButton,"onclick",this,dojo.hitch(this,function(e) {
      var sUrl = this.getCartUrl()+"/process";
      var p = this.processor;
      if ((typeof(p) != "undefined") && (p != null)) {
        sUrl += p;
      }
      this.executeDownload(sUrl);
    }));
    
    elButton = document.createElement("button");
    elButton.appendChild(document.createTextNode(this.i18n["catalog.cart.clear"]));
    elControlBar.appendChild(elButton);
    dojo.connect(elButton,"onclick",this,dojo.hitch(this,function(e) {
      this.removeAll();
    }));
    
    elButton = document.createElement("button");
    elButton.appendChild(document.createTextNode(this.i18n["catalog.cart.close"]));
    elControlBar.appendChild(elButton);
    dojo.connect(elButton,"onclick",this,dojo.hitch(this,function(e) {
      this.closeDialog();
    }));
    
    this.countNode = document.createElement("span");
    dojo.style(this.countNode,{
      marginLeft: "20px",
      marginRight: "20px"
    });
    elControlBar.appendChild(this.countNode);
  },
  
  /**
   * Closes the dialog.
   * @function 
   * @name closeDialog
   * @memberOf gpt.form.Cart#
   */
  closeDialog: function() {
    if (this.parentDialog != null) {
      this.parentDialog.hide();
    }
  },
  
  /**
   * Connects the cart to the search results page using check-boxes.
   * @function 
   * @name connectToSearchResults
   * @memberOf gpt.form.Cart#
   */
  connectToSearchResults: function() {
    if (!this.enabled) return;
    
    if (typeof(scIsRemoteCatalog) != 'undefined') {
      if (scIsRemoteCatalog() == true) {
        return;
      }
    }
    
    var connectItems = dojo.hitch(this,function(oKeys){
      if ((typeof(oKeys) == "undefined") || (oKeys == null)) {
        oKeys = new Array();
      }
      for (var i=0;i<jsMetadata.records.length;i++) {
        var oRec = jsMetadata.records[i];
        if ((typeof(oRec) != "undefined") && (oRec != null) &&
            (typeof(oRec.uuid) != "undefined") && (oRec.uuid != null) && (oRec.uuid != "")) {
          var sUuid = oRec.uuid;
          var sId = "frmSearchCriteria:mdRecords:"+i+":smallImgContentType";
          var elImg = dojo.byId(sId);
          if (elImg != null) {
            var elChkCtl = this.makeCheckControl(elImg.parentNode,oKeys,sUuid);
            if (elChkCtl != null) {
              elImg.parentNode.insertBefore(elChkCtl,elImg);
            }
          }
        }
      } 
    });
    
    if ((typeof(jsMetadata) != "undefined") && (jsMetadata != null) &&
        (typeof(jsMetadata.records) != "undefined") && (jsMetadata.records != null)) {
      itemCart.initializeKeys({
        error: dojo.hitch(this,function(oError,ioArgs) {
          console.log(oError);
        }), 
        load: dojo.hitch(this,function(oCart,ioArgs) {
          connectItems(oCart.keys);
        })
      });         
    }    
  },
  
  /**
   * Submits the active items keys to an end point the will process
   * the submitted keys and respond with a downloadable file.
   * 
   * The function uses the Form created by makeDownloadForm(),
   * the response is returned in a new tab. The response does not 
   * need to be an attached file, it could be an HTML page.
   * 
   * @function 
   * @name executeDownload
   * @memberOf gpt.form.Cart#
   * @param {String} sUrl the item processing url
   */
  executeDownload: function(sUrl) {
    var oKeys = this.getLocalKeys();
    if (oKeys.length > 0) {
      var elForm = dojo.byId("gptCartDownloadForm");
      var elKeys = dojo.byId("gptCartDownloadKeys");
      elKeys.value = oKeys.join(",");
      elForm.action = sUrl;
      elForm.submit();
    }
  },
  
  /**
   * Gets the base URL to the server side cart.
   * @function 
   * @name getCartUrl
   * @memberOf gpt.form.Cart#
   * @returns {String} the base URL (e.g. /geoportal/cart)
   */
  getCartUrl: function() {
    return this.contextPath+"/cart";
  },
  
  /**
   * Gets the array of item keys local to the dialog.
   * @function 
   * @name getLocalKeys
   * @memberOf gpt.form.Cart#
   * @returns {Array} the item keys
   */
  getLocalKeys: function() {
    var oKeys = new Array();
    dojo.query(".gptCartItem",this.itemsNode).forEach(function(item) {
      oKeys[oKeys.length] = item.gptCartItemId;
    });
    return oKeys;
  },
  
  /**
   * Handles an error message.
   * @function 
   * @name handleError
   * @memberOf gpt.form.Cart#
   * @param {oWorking} oWorking the working object
   * @param {String} sMsg the error message
   */
  handleError: function(oWorking,sMsg) {
    if ((typeof(sMsg) == "undefined") || (sMsg == null)) {
      sMsg = this.i18n["catalog.gxe.dialog.invalidResponse"];
    }
    if ((typeof(oWorking) != "undefined") && (oWorking != null)) {
      oWorking.hide();
    }
    alert(sMsg);
  },
  
  /**
   * Reads the keys within the cart.
   * @function 
   * @name initializeKeys
   * @memberOf gpt.form.Cart#
   * @param {Object} params (for load and error callback functions)
   */
  initializeKeys: function(params) {
    
    var hndlError = dojo.hitch(this,function(oErrorx,ioArgs){
      if ((typeof(oError) == "undefined") || (oError == null)) {
        oError = new Error(this.i18n["catalog.gxe.dialog.invalidResponse"]);
      } 
      if ((typeof(params) != "undefined") && (params != null) &&
          (typeof(params.error) == "function")) {
        params.error(oError,ioArgs);
      } else {
        console.log(oError);
      }
    });
    
    var hndlLoad = dojo.hitch(this,function(oCart,ioArgs){
      if ((typeof(params) != "undefined") && (params != null) &&
          (typeof(params.load) == "function")) {
        params.load(oCart,ioArgs);
      } 
    });
    
    GptCore.loadDeclaredI18N(this.i18n,dojo.hitch(this,function(){
      var sUrl = this.getCartUrl()+"/keys";
      dojo.xhrGet({
        handleAs: "json",
        url: sUrl,
        preventCache: true,
        error: dojo.hitch(this,function(responseObject,ioArgs) {
          hndlError(responseObject,ioArgs);
        }),
        load: dojo.hitch(this,function(responseObject,ioArgs) {
          if ((typeof(responseObject) == "undefined") || (responseObject == null)) {
            hndlError(null,ioArgs);
            return;
          } 
          var oError = responseObject.error;
          if ((typeof(oError) != "undefined") && (oError != null)) {
            hndlError(oError,ioArgs);
            return;
          }
          var oCart = responseObject.cart;
          if ((typeof(oCart) == "undefined") || (oCart == null)) {
            hndlError(null,ioArgs);
            return;
          }
          this.maxItems = oCart.maxItems;
          this._setCount(responseObject);
          hndlLoad(oCart);
        })
      });
    }));
  },
  
  /**
   * Makes a check control for use on the search or browse pages.
   * @function 
   * @name makeCheckControl
   * @memberOf gpt.form.Cart#
   * @param {Node} ndParent the parent DOM node
   * @param {Array} oKeys array of keys in the cart
   * @param {String} sKey the associated document key (uuid)
   * @returns {Element} the UI element (can be null)
   */
  makeCheckControl: function(ndParent,oKeys,sKey) {
    
    var bWasAdded = false;
    dojo.query(".gptCartCheckBox",ndParent).forEach(function(item) {
      bWasAdded = true;
    });
    if (bWasAdded) return null;
    
    var sTip = this.i18n["catalog.cart.check.tip"];
    var bFull = (this.approximateSize >= this.maxItems);
    var bChecked = false;
    if ((typeof(oKeys) == "undefined") || (oKeys == null)) oKeys = new Array();
    for (var i=0;i<oKeys.length;i++) {
      if (sKey == oKeys[i]) {
        bChecked = true;
        break;
      }
    }
    
    var executeCheck = dojo.hitch(this,function(elChk){
      var sUrl = this.getCartUrl();
      if (elChk.checked) sUrl += "/add";
      else sUrl += "/remove";
      sUrl += "?key="+encodeURIComponent(sKey);
      dojo.xhrGet({
        handleAs: "json",
        url: sUrl,
        preventCache: true,
        error: dojo.hitch(this,function(responseObject,ioArgs) {
          console.log(responseObject);
        }),
        load: dojo.hitch(this,function(responseObject,ioArgs) {
          this._setCount(responseObject);
        })
      });
    });  
    
    var connectCheck = dojo.hitch(this,function(elChk){
      dojo.connect(elChk,"onclick",this,dojo.hitch(this,function(e) {
        executeCheck(elChk);
      }));
      dojo.connect(this,"onRemove",this,dojo.hitch(this,function(sRemKey) {
        if (sKey == sRemKey) {
          if (elChk.checked) elChk.checked = false;
        }
      }));
      dojo.connect(this,"onRemoveAll",this,dojo.hitch(this,function() {
        if (elChk.checked) elChk.checked = false;
      }));
    });
    
    var connectImg = dojo.hitch(this,function(elImg,elChk){
      dojo.connect(elImg,"onclick",this,dojo.hitch(this,function(e) {
        this.showDialog();
      }));
    });
    
    var elChk = document.createElement("input");
    elChk.className = "gptCartCheckBox";
    if (dojo.isIE <= 8) {
      if (bChecked) {
        elChk = document.createElement(
          "<input type=\"checkbox\" checked=\"checked\"/>");
      } else {
        elChk.type = "checkbox";
      }
    } else {
      elChk.type = "checkbox";
      if (bChecked) elChk.checked = true;
    }
    connectCheck(elChk);
    if (!bChecked && bFull) {
      elChk.style.visibility = "hidden";
      sTip = this.i18n["catalog.cart.isfull"];
      sTip = sTip.replace("{0}",""+this.maxItems);
    } else {
      elChk.style.visibility = "visible";
    }
    
    var elSpan = document.createElement("span");
    elSpan.className = "gptCartCheckControl";
    elSpan.title = sTip;
    dojo.style(elSpan,{
      paddingTop: "5px",
      paddingRight: "2px",
      marginRight: "10px",
      border: "1px solid #808080"
     });
    elSpan.appendChild(elChk);
    
    var elImg = document.createElement("img");
    elImg.src = this.contextPath+"/catalog/images/cart.png";
    //connectImg(elImg,elChk);
    elSpan.appendChild(elImg);
    return elSpan;
  },
  
  /**
   * Makes an HTML form suitable for use by a file download operation.
   * @function 
   * @name makeDownloadForm
   * @memberOf gpt.form.Cart#
   * @returns {Form} the form
   */
  makeDownloadForm: function() {
    var elForm = document.createElement("form");
    elForm.id = "gptCartDownloadForm";
    dojo.style(elForm,{display:"inline"});
    elForm.setAttribute("method","post");
    elForm.setAttribute("enctype","application/x-www-form-urlencoded");
    elForm.setAttribute("target","_blank");
    var elHidden = document.createElement("input");
    elHidden.id = "gptCartDownloadKeys";
    elHidden.setAttribute("type","hidden");
    elHidden.name = "keys";
    elForm.appendChild(elHidden); 
    return elForm;
  },
  
  /**
   * Triggered when the user removes an item from the cart.
   * @event 
   * @name onRemove
   * @memberOf gpt.form.Cart#
   * @param {String} sKey the key for the item
   */
  onRemove: function(sKey) {},
  
  /**
   * Triggered when the user clears all items from the cart.
   * @event 
   * @name onRemoveAll
   * @memberOf gpt.form.Cart#
   */
  onRemoveAll: function() {},
  
  /**
   * Refreshes the item count.
   * @function 
   * @name refreshCount
   * @memberOf gpt.form.Cart#
   */
  refreshCount: function() {
    var el = this.countNode;
    if ((typeof(el) != "undefined") && (el != null)) {
      var nItems = 0;
      dojo.query(".gptCartItem",this.itemsNode).forEach(function(item) {
        nItems++;
      });
      while (el.childNodes.length >= 1) el.removeChild(el.firstChild);
      var s = this.i18n["catalog.cart.item.count"];
      if (nItems == 1) {
        s = this.i18n["catalog.cart.item.count.one"];
      } else {
        s = s.replace("{0}",nItems);
      }
      var sTip = this.i18n["catalog.cart.max.tip"];
      sTip = sTip.replace("{0}",this.maxItems);
      el.title = sTip;
      el.appendChild(document.createTextNode(s));
    }
  },
  
  /**
   * Refreshes the item content.
   * @function 
   * @name refreshItems
   * @memberOf gpt.form.Cart#
   */
  refreshItems: function() {
    
    var connectDel = dojo.hitch(this,function(elDel,elItem,sKey){
      dojo.connect(elDel,"onclick",this,dojo.hitch(this,function(e) {
        var oWorking = this.showWorking();
        var sUrl = this.getCartUrl()+"/remove";
        sUrl += "?key="+encodeURIComponent(sKey);
        dojo.xhrGet({
          handleAs: "json",
          url: sUrl,
          preventCache: true,
          error: dojo.hitch(this,function(responseObject,ioArgs) {
            this.handleError(oWorking,responseObject.message);
          }),
          load: dojo.hitch(this,function(responseObject,ioArgs) {
            if ((typeof(responseObject) == "undefined") || (responseObject == null)) {
              this.handleError(oWorking,null);
            } else {
              var oError = responseObject.error;
              if ((typeof(oError) != "undefined") && (oError != null)) {
                this.handleError(oWorking,oError.message);
              } else {
                this._setCount(responseObject);
                this.itemsNode.removeChild(elItem);
                this.refreshCount();
                oWorking.hide();
                this.onRemove(sKey);
              }
            }
          })
        });
      }));
    });
    
    var emptyItems = dojo.hitch(this,function(bRefreshCount){
      var el = this.itemsNode;
      while (el.childNodes.length >= 1) el.removeChild(el.firstChild);
      if (bRefreshCount) this.refreshCount();
    });
    
    var populateItems = dojo.hitch(this,function(oItems){
      emptyItems(true);
      var el = this.itemsNode;
      var sDelTip = this.i18n["catalog.cart.remove.tip"];
      for (var i=0;i<oItems.length;i++) {
        var elItem = document.createElement("div");
        dojo.addClass(elItem,"gptCartItem");
        dojo.style(elItem,{margin:"10px",fontSize:"small"});
        var elDel = document.createElement("img");
        elDel.title = sDelTip;
        elDel.src = this.contextPath+"/catalog/images/asn-delete.png";
        dojo.style(elDel,{
          marginLeft: "2px",
          marginRight: "2px",
          width: "16px",
          height: "16px",
          verticalAlign: "middle",
          cursor: "pointer"
        });
        elItem.appendChild(elDel);
        elItem.appendChild(document.createTextNode(oItems[i].title));
        elItem.gptCartItemId = oItems[i].id;
        el.appendChild(elItem);
        connectDel(elDel,elItem,oItems[i].id);
      }
      var mb1 = dojo.marginBox(this.parentDialog.domNode);
      var mb2 = dojo.marginBox(this.itemsNode);
      var nH = mb1.h - mb2.t - 14;
      if (nH <= 0) nH = 1;
      var sH = nH+"px";
      dojo.style(this.itemsNode,"height",sH);
      this.refreshCount();
    });
    
    var queryItems = dojo.hitch(this,function(oWorking,oKeys){
      var s, q = "";
      for (var i=0;i<oKeys.length;i++) {
        if (i > 0) q += " OR ";
        s = oKeys[i];
        s = s.replace("{","\\{");
        s = s.replace("}","\\}");
        s = s.replace("-","\\-");
        q += s;
      }
      q = "uuid:("+q+")";
      
      var sUrl = this.contextPath+"/rest/find/document";
      dojo.xhrPost({
        handleAs: "json",
        url: sUrl,
        preventCache: true,
        content: {searchText:q, max:this.maxItems, f:"json"},
        error: dojo.hitch(this,function(responseObject,ioArgs) {
          this.handleError(oWorking,responseObject.message);
        }),
        load: dojo.hitch(this,function(responseObject,ioArgs) {
          if ((typeof(responseObject) == "undefined") || (responseObject == null)) {
            this.handleError(oWorking,null);
          } else {
            var oError = responseObject.error;
            if ((typeof(oError) != "undefined") && (oError != null)) {
              this.handleError(oWorking,oError.message);
            } else {
              var oRecs = responseObject.records;
              if ((typeof(oRecs) == "undefined") || (oRecs == null)) {
                this.handleError(oWorking,null);
              } else {
                if (oRecs.length > 1) {
                  oRecs.sort(function(a,b){
                    var s1 = a.title.toLowerCase(), s2 = b.title.toLowerCase();
                    if (s1 < s2) return -1;
                    else if (s1 > s2)return 1;
                    return 0;
                  });
                }
                populateItems(oRecs);
                oWorking.hide();
              }
            }
          }
        })
      });
    });
    
    var oWorking = this.showWorking();
    var sUrl = this.getCartUrl()+"/keys";
    dojo.xhrGet({
      handleAs: "json",
      url: sUrl,
      preventCache: true,
      error: dojo.hitch(this,function(responseObject,ioArgs) {
        this.handleError(oWorking,responseObject.message);
      }),
      load: dojo.hitch(this,function(responseObject,ioArgs) {
        if ((typeof(responseObject) == "undefined") || (responseObject == null)) {
          this.handleError(oWorking,null);
        } else {
          var oError = responseObject.error;
          if ((typeof(oError) != "undefined") && (oError != null)) {
            this.handleError(oWorking,oError.message);
          } else {
            var oCart = responseObject.cart;
            if ((typeof(oCart) == "undefined") || (oCart == null)) {
              this.handleError(oWorking,null);
            } else {
              this.maxItems = oCart.maxItems;
              this._setCount(responseObject);
              var oKeys = oCart.keys;
              if (oKeys.length == 0) {
                emptyItems(true);
                oWorking.hide();
              } else {
                queryItems(oWorking,oKeys);
              }
            }
          }
        }
      })
    });
  },
  
  /**
   * Removes all items.
   * @function 
   * @name removeAll
   * @memberOf gpt.form.Cart#
   */
  removeAll: function() {
    var oWorking = this.showWorking();
    var sUrl = this.getCartUrl()+"/clear";
    dojo.xhrGet({
      handleAs: "json",
      url: sUrl,
      preventCache: true,
      error: dojo.hitch(this,function(responseObject,ioArgs) {
        this.handleError(oWorking,responseObject.message);
      }),
      load: dojo.hitch(this,function(responseObject,ioArgs) {
        if ((typeof(responseObject) == "undefined") || (responseObject == null)) {
          this.handleError(oWorking,null);
        } else {
          var oError = responseObject.error;
          if ((typeof(oError) != "undefined") && (oError != null)) {
            this.handleError(oWorking,oError.message);
          } else {
            this._setCount(responseObject);
            this.onRemoveAll();
            oWorking.hide();
            this.closeDialog();
          }
        }
      })
    });
  },
  
  /**
   * Shows the dialog.
   * @function 
   * @name showDialog
   * @memberOf gpt.form.Cart#
   */
  showDialog: function() { 
    if (!this.enabled) return;
    
    var sTitle = this.title;
    this.contentNode = document.createElement("div");
    dojo.addClass(this.contentNode,"gptCart");
    
    var vp = dijit.getViewport();
    var nW = vp.w-100;
    var nH = vp.h-50;
    if (vp.w > 600) nW = 600;
    if (vp.h > 500) nH = 500;
    var sStyle = "width:"+nW+"px;height:"+nH+"px;";
    sStyle += "max-width:98%;max-height:98%;";
    sStyle += "border:1px solid black;background-color:white;";
    
    this.parentDialog = new dijit.Dialog({
      title: sTitle,
      content: this.contentNode,
      style: sStyle,
      autofocus: false
    });
    dojo.addClass(this.parentDialog.domNode,"tundra");

    dojo.connect(this.parentDialog,"onHide",dojo.hitch(this,function() {
      setTimeout(dojo.hitch(this,function(){
        this.parentDialog.destroyRecursive();
      }),300);
    }));
        
    var bFirst = true;
    GptCore.loadDeclaredI18N(this.i18n,dojo.hitch(this,function(){
      if (bFirst) {
        this.parentDialog.show();
        this.buildUI();
      }
    }));
  },
  
  /**
   * Shows the working underlay and image.
   * @function 
   * @name showWorking
   * @memberOf gpt.form.Cart#
   * @returns {oWorking} the working object
   */
  showWorking: function() {   
    var sStyle = "opacity: 0;";
    var oUnderlay = new dijit.DialogUnderlay({style:sStyle});
    oUnderlay.show();
    
    var mb = dojo.marginBox(this.parentDialog.domNode);
    var nL = mb.l +(mb.w / 2);
    var nT = mb.t +(mb.h / 2);
  
    var oWorking = document.createElement("span");
    oWorking.startTime = (new Date()).getTime();
    dojo.style(oWorking,{
      position: "absolute",
      marginLeft: "5px",
      marginRight: "5px",
      left: nL+"px",
      top: nT+"px",
      opacity: "1",
      zIndex: "9998",
      cursor: "pointer"
    });
    
    var elLoading = document.createElement("img");
    elLoading.src = this.contextPath+"/catalog/images/loading.gif";
    dojo.style(elLoading,{
      width: "24px",
      height: "24px",
      verticalAlign: "bottom"
    });
    oWorking.appendChild(elLoading);
    dojo.body().appendChild(oWorking); 
    
    dojo.connect(oWorking,"onclick",dojo.hitch(this,function(e) {
      try {dojo.body().removeChild(oWorking);} catch (error) {}
      try {oUnderlay.hide();} catch (error) {}
    }));

    oWorking.hide = function() {
      tMinMillis = 1;
      var tStart = oWorking.startTime;
      var tEnd = (new Date()).getTime();
      var tElapsed = (tEnd - tStart);
      if (tElapsed < tMinMillis) {
        var tDelay = Math.min(tElapsed,tMinMillis);
        setTimeout(dojo.hitch(this,function(){
          try {dojo.body().removeChild(oWorking);} catch (error) {}
          try {oUnderlay.hide();} catch (error) {}
        }),tDelay);
      } else {
        try {dojo.body().removeChild(oWorking);} catch (error) {}
        try {oUnderlay.hide();} catch (error) {}
      }
    };
    
    return oWorking;
  },
  
  /**
   * Shows the working underlay and image.
   * @function 
   * @name _setCount
   * @memberOf gpt.form.Cart#
   * @param {Object} responseObject the XHR responseObject
   */
  _setCount: function(responseObject) {
    if ((typeof(responseObject) != "undefined") && (responseObject != null)) {
      var oCart = responseObject.cart;
      if ((typeof(oCart) != "undefined") && (oCart != null)) {
        this.approximateSize = oCart.size;
        var bFull = (this.approximateSize >= this.maxItems);
        var sTip = this.i18n["catalog.cart.check.tip"];
        var sFullTip = this.i18n["catalog.cart.isfull"];
        sFullTip = sFullTip.replace("{0}",""+this.maxItems);
        dojo.query(".gptCartCheckControl").forEach(function(item) {
          dojo.query(".gptCartCheckBox",item).forEach(function(item2) {
            if (bFull) {
              if (item2.checked) {
                item.title = sTip;
                item2.style.visibility = "visible";
              } else {
                item.title = sFullTip;
                item2.style.visibility = "hidden";
              }
            } else {
              item.title = sTip;
              item2.style.visibility = "visible";
            }
          });
          
        });
      }
    }
  }
  
});







