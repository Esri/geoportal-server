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

dojo.require("dojox.form.Uploader");
dojo.require("dojox.embed.Flash");
dojo.require("dojox.form.uploader.plugins.HTML5");
    
dojo.provide("gpt.gxe.MetadataPanel");
dojo.declare("gpt.gxe.MetadataPanel", null, {

    /*
    * Some issues to consider:
    * 
    * gpt.properties, .css
    * IE style problems
    * handle an expired token,
    * "do you want to save on page unload?", or auto save draft
    * dojo io preventCache?
    * implement delete
    * button reset dialog size?
    * download filename (metadata.xml?)
    * Gemet proxy.jsp
    * localized error message on GxeServlet
    * gxe-proxy.jsp restrictions
    * javascript doc
    * 
    */

    canEdit: false,
    contentNode: null,
    documentClient: null,
    gxeContext: null,
    parentDialog: null,
    proxyUrlAdapter: null,
    originalXml: null,
    title: null,

    // constructor
    /*
    constructor: function(args){
    // this fails in IE8
    dojo.safeMixin(this,args);
    },
    */

    // configuration strings
    config: {
        "ItemMetadata.enabled": false,
        "ItemMetadata.allowDelete": false,
        "ItemMetadata.allowSaveDraft": false,
        "ItemMetadata.documentTypeKeys": null,
        "ItemMetadata.gxeProxyUrl": null,
        "ItemMetadata.useGxeProxyUrl": false,
        "ItemMetadata.gemetThemesProxy": null
    },

    // i18n strings
    i18n: {
        "catalog.gxe.dialog.ok": "OK",
        "catalog.gxe.dialog.cancel": "Cancel",
        "catalog.gxe.dialog.close": "Close",
        "catalog.gxe.dialog.download": "Download",
        "catalog.gxe.dialog.genericError": "An error has occurred.",
        "catalog.gxe.dialog.import": "Import",
        "catalog.gxe.dialog.importFile": "Import a file from your computer:",
        "catalog.gxe.dialog.importingFilename": "Importing {0}",
        "catalog.gxe.dialog.invalidResponse": "Invalid response.",
        "catalog.gxe.dialog.load": "Load",
        "catalog.gxe.dialog.loadTitle": "Load a Document",
        "catalog.gxe.dialog.loadingDocument": "Loading document...",
        "catalog.gxe.dialog.loadingDocumentType": "Loading {0}...",
        "catalog.gxe.dialog.loadingEditor": "Loading editor...",
        "catalog.gxe.dialog.mode.edit": "Editor",
        "catalog.gxe.dialog.mode.view": "Details",
        "catalog.gxe.dialog.mode.xml": "XML",
        "catalog.gxe.dialog.noDefinedTypes": "There are no defined metadata types.",
        "catalog.gxe.dialog.noMetadata": "This item has no metadata.",
        "catalog.gxe.dialog.save": "Save",
        "catalog.gxe.dialog.savingDocument": "Saving document...",
        "catalog.gxe.dialog.saveDraft": "Save Draft",
        "catalog.gxe.dialog.selectXmlFile": "Select an XML file to import...",
        "catalog.gxe.dialog.startNewDocument": "Start a new document:",
        "catalog.gxe.dialog.title": "Metadata"
    },

    // initialize
    initialize: function () {
        GptCore.loadDeclaredConfig(this.config);
        if (this.config["ItemMetadata.useGxeProxyUrl"]) {
            var s = this.config["ItemMetadata.gxeProxyUrl"];
            if ((typeof (s) != "undefined") && (s != null)) {
                this.proxyUrlAdapter = new gpt.gxe.ProxyUrlAdapter();
                this.proxyUrlAdapter.serverProxyUrl = s;
            }
        }
        this.gxeContext = new gxe.Context();
        this.gxeContext.contextPath = GptCore.getServerContextPath();
    },

    // close the dialog
    closeDialog: function () {
        if (this.parentDialog != null) {
            this.parentDialog.hide();
        }
    },

    // fired when a metadata document has been successfully saved
    onMetadataSaved: function () {
    },

    showDialog: function () {
        var bResetTitle = true;
        var sTitle = this.i18n["catalog.gxe.dialog.title"];
        if ((typeof (this.title) != "undefined") && (this.title != null)) {
            bResetTitle = false;
            sTitle = this.title;
        }

        this.contentNode = document.createElement("div");
        dojo.addClass(this.contentNode, "gxeMetadataDialog");

        var vp = dijit.getViewport();
        var nW = vp.w - 100;
        var nH = vp.h - 50;
        var sStyle = "width:" + nW + "px;height:" + nH + "px;";
        sStyle += "max-width:98%;max-height:98%;";

        this.parentDialog = new dijit.Dialog({
            title: sTitle,
            content: this.contentNode,
            style: sStyle,
            autofocus: false
        });

        dojo.connect(this.parentDialog, "onHide", dojo.hitch(this, function () {
            setTimeout(dojo.hitch(this, function () {
                this.parentDialog.destroyRecursive();
            }), 300);
        }));

        GptCore.loadDeclaredI18N(this.i18n, dojo.hitch(this, function () {
            if (bResetTitle) {
                // TODO: may need to reset the title
            }
            this._build();
            this.parentDialog.show();
            this._execLoadXml();
        }));

    },

    _adaptProxyUrl: function (sUrl) {
        if (this.proxyUrlAdapter != null) {
            sUrl = this.proxyUrlAdapter.adaptUrl(sUrl);
        }
        return sUrl;
    },

    // build the UI
    _build: function () {

        this._buildButtonBar();

        var elGxeDetailsSection = document.createElement("div");
        elGxeDetailsSection.id = "gxeDetailsSection";
        elGxeDetailsSection.style.display = "none";
        dojo.addClass(elGxeDetailsSection, "gxeMode");
        dojo.addClass(elGxeDetailsSection, "gxeViewDetailsMode");
        this.contentNode.appendChild(elGxeDetailsSection);

        var elXmlView = null;
        if (dojo.isIE <= 8) {
            elXmlView = document.createElement("textarea");
            //TODO: there is a backspace problem here
            //elXmlView.readOnly = true;
        } else {
            elXmlView = document.createElement("textarea");
            elXmlView.setAttribute("readonly", true);
        }
        elXmlView.id = "gxeXmlView";
        elXmlView.style.display = "none";
        dojo.addClass(elXmlView, "gxeMode");
        dojo.addClass(elXmlView, "gxeViewXmlMode");
        this.contentNode.appendChild(elXmlView);

        var elGxeMessageArea = document.createElement("div");
        elGxeMessageArea.id = "gxeMessageArea";
        elGxeMessageArea.className = "gxeMessageArea";
        elGxeMessageArea.style.display = "none";
        this.contentNode.appendChild(elGxeMessageArea);

        var elGxeDocument = document.createElement("div");
        elGxeDocument.id = "gxeDocument";
        elGxeDocument.className = "gxeDocument";
        elGxeDocument.style.display = "none";
        dojo.addClass(elGxeDocument, "gxeMode");
        this.contentNode.appendChild(elGxeDocument);
    },

    _buildButtonBar: function () {
        var oThis = this;

        var addMode = function (elParent, sMode, sLabel, bChecked) {
            var sMetadataMode = "gxeMetadataMode";
            sMetadataModeId = sMetadataMode + "_" + sMode;
            var elOption = document.createElement("span");
            var elLabel, elRadio;
            dojo.addClass(elOption, "gxeButton gxeButton-enabled");
            elParent.appendChild(elOption);
            if (dojo.isIE <= 8) {
                if (bChecked) {
                    elRadio = document.createElement(
            "<input type=\"radio\" name=\"" + sMetadataMode + "\" checked=\"checked\"/>");
                } else {
                    elRadio = document.createElement(
            "<input type=\"radio\" name=\"" + sMetadataMode + "\"/>");
                }
                elRadio.id = sMetadataModeId;
                elLabel = document.createElement("<label for=\"" + sMetadataModeId + "\"/>");
            } else {
                elRadio = document.createElement("input");
                elRadio.type = "radio";
                elRadio.id = sMetadataModeId;
                elRadio.setAttribute("name", sMetadataMode);
                elRadio.setAttribute("type", "radio");
                if (bChecked) elRadio.setAttribute("checked", "checked");
                elLabel = document.createElement("label");
                elLabel.setAttribute("for", sMetadataModeId);
            }
            elLabel.appendChild(document.createTextNode(sLabel));
            elOption.appendChild(elRadio);
            elOption.appendChild(elLabel);
            dojo.connect(elRadio, "onclick", function (e) {
                oThis._changeMode(sMode);
            });
        };

        var elButton, elSep;

        var elControlBar = document.createElement("div");
        elControlBar.className = "gxeMainControlBar";
        this.contentNode.appendChild(elControlBar);

        var elViewTools = document.createElement("div");
        elViewTools.id = "gxeViewModeTools";
        elViewTools.style.display = "inline";
        elControlBar.appendChild(elViewTools);
        addMode(elViewTools, "view", this.i18n["catalog.gxe.dialog.mode.view"], true);
        addMode(elViewTools, "xml", this.i18n["catalog.gxe.dialog.mode.xml"], false);

        if (this.canEdit) {

            elSep = document.createElement("span");
            elSep.className = "gxeButtonSeparator";
            elViewTools.appendChild(elSep);

            elButton = document.createElement("button");
            elButton.className = "gxeButton gxeButton-enabled";
            elButton.appendChild(document.createTextNode(this.i18n["catalog.gxe.dialog.mode.edit"]));
            elViewTools.appendChild(elButton);
            dojo.connect(elButton, "onclick", this, dojo.hitch(this, function (e) {
                this._changeMode("edit");
            }));

            var elEditTools = document.createElement("div");
            elEditTools.id = "gxeEditModeTools";
            dojo.style(elEditTools, { display: "none" });
            elControlBar.appendChild(elEditTools);

            elButton = document.createElement("button");
            elButton.className = "gxeButton gxeButton-enabled";
            elButton.appendChild(document.createTextNode(this.i18n["catalog.gxe.dialog.load"]));
            elEditTools.appendChild(elButton);
            dojo.connect(elButton, "onclick", this, dojo.hitch(this, function (e) {
                this._showLoadDialog();
            }));

            elButton = document.createElement("button");
            elButton.className = "gxeButton";
            elButton.disabled = true;
            elButton.appendChild(document.createTextNode(this.i18n["catalog.gxe.dialog.download"]));
            dojo.addClass(elButton, "gxeToolRequiresEditor");
            elEditTools.appendChild(elButton);
            this._makeDownloadForm(elEditTools, elButton);

            elSep = document.createElement("span");
            elSep.className = "gxeButtonSeparator";
            elEditTools.appendChild(elSep);

            elButton = document.createElement("button");
            elButton.className = "gxeButton";
            elButton.disabled = true;
            elButton.appendChild(document.createTextNode(this.i18n["catalog.gxe.dialog.save"]));
            dojo.addClass(elButton, "gxeToolRequiresEditor");
            elEditTools.appendChild(elButton);
            dojo.connect(elButton, "onclick", this, dojo.hitch(this, function (e) {
                this._execSaveXml(false);
            }));

            if (this.config["ItemMetadata.allowSaveDraft"]) {
                elButton = document.createElement("button");
                elButton.className = "gxeButton";
                elButton.disabled = true;
                elButton.appendChild(document.createTextNode(this.i18n["catalog.gxe.dialog.saveDraft"]));
                dojo.addClass(elButton, "gxeToolRequiresEditor");
                elEditTools.appendChild(elButton);
                dojo.connect(elButton, "onclick", this, dojo.hitch(this, function (e) {
                    this._execSaveXml(true);
                }));
            }
        }

        elSep = document.createElement("span");
        elSep.className = "gxeButtonSeparator";
        elControlBar.appendChild(elSep);

        // TODO: what about cancel using dialog X button?
        // TODO: warn if edits have been made but not saved?
        if (this.parentDialog != null) {
            elButton = document.createElement("button");
            elButton.className = "gxeButton gxeButton-enabled";
            elButton.appendChild(document.createTextNode(this.i18n["catalog.gxe.dialog.close"]));
            elControlBar.appendChild(elButton);
            dojo.connect(elButton, "onclick", this, dojo.hitch(this, function (e) {
                this.closeDialog();
            }));
        }

        var elPlaceholder = document.createElement("button");
        elPlaceholder.className = "gxeButton";
        elPlaceholder.id = "gxeMainWorkingMessageArea.anchor";
        elPlaceholder.style.visibility = "hidden";
        elControlBar.appendChild(elPlaceholder);
    },

    _checkBrowserVersion:function ()
	{
	  var msg = "You're not using Internet Explorer.";
	  var ver = this._getInternetExplorerVersion();

	  if ( ver > -1 )
	  {		
		if ( ver >= 8.0 ) 
		  msg = "You're using a recent copy of Internet Explorer."
		else
		  msg = "You should upgrade your copy of Internet Explorer.";
		return true;
	  }
	  return false;
	},
    
    // change the dialog mode
    _changeMode: function (sMode) {
        var b162 = false;
        if ((dojo.version.major <= 1) && (dojo.version.minor <= 6)) {
          b162 = true;
        }

        var elEdit = dojo.byId("gxeDocument");
        var elView = dojo.byId("gxeDetailsSection");
        var elXml = dojo.byId("gxeXmlView");
        var elViewTools = dojo.byId("gxeViewModeTools");
        var elEditTools = dojo.byId("gxeEditModeTools");
        if (sMode == "edit") {
            if (this.canEdit) {
                if (elEdit != null) elEdit.style.display = "block";
                if (elView != null) elView.style.display = "none";
                if (elXml != null) elXml.style.display = "none";
                if (elEditTools != null) elEditTools.style.display = "inline";
                if (elViewTools != null) elViewTools.style.display = "none";
                var sXml = this.originalXml;
                if ((typeof (sXml) != "undefined") && (sXml != null)) {
                    this._execInterrogateDefinition(sXml);
                } else {
                    this._showLoadDialog();
                }
            }
        } else if (sMode == "view") {
            if (elEdit != null) elEdit.style.display = "none";
            if (elView != null) elView.style.display = "block";
            if (elXml != null) elXml.style.display = "none";
            if (elEditTools != null) elEditTools.style.display = "none";

            if (this.parentDialog != null) {
                if (!b162) {
                  //this.parentDialog._position();
                  //this.parentDialog._layout();
                  //this.parentDialog._layoutChildren();
                } else {
                  this.parentDialog.layout();
                }
                var mb = dojo.marginBox(this.parentDialog.domNode);
                var nH = mb.h - 100;
                var sH = nH + "px";
                if (!b162) {
                    var sH2 = mb.h + "px";
                    dojo.style(this.parentDialog.containerNode, "height", sH2);
                }
                if (elView != null) {
                    dojo.style(elView, "height", sH);
                }
                if (elXml != null) {
                    dojo.style(elXml, "height", sH);
                }
                if (elEdit != null) {
                    dojo.style(elEdit, "height", sH);
                }
            }

        } else if (sMode == "xml") {
            if (elEdit != null) elEdit.style.display = "none";
            if (elView != null) elView.style.display = "none";
            if (elXml != null) elXml.style.display = "block";
            if (elEditTools != null) elEditTools.style.display = "none";
        }
    },

    // interrogate the definition for an XML document 
    _execInterrogateDefinition: function (sXml) {
        dojo.query(".gxeToolRequiresEditor").forEach(function (item) {
            item.disabled = true;
            if (dojo.hasClass(item, "gxeButton-enabled")) {
                dojo.removeClass(item, "gxeButton-enabled");
            }
        });

        var sUrl = this.gxeContext.contextPath + "/gxe/interrogate";
        sUrl = this._adaptProxyUrl(sUrl);

        var oThis = this;
        var oWorking = this._showWorking(this.i18n["catalog.gxe.dialog.loadingEditor"]);
        var handleError = function (sError) {
            oWorking.hide();
            oThis._showErrorDialog(oThis.i18n["catalog.gxe.dialog.mode.edit"], sError);
        };

        dojo.xhrPost({
            handleAs: "json",
            headers: { "Content-Type": "text/plain; charset=UTF-8" },
            postData: sXml,
            url: sUrl,
            error: dojo.hitch(this, function (responseObject, ioArgs) {
                handleError(responseObject.message);
            }),
            load: dojo.hitch(this, function (responseObject, ioArgs) {
                if ((typeof (responseObject) == "undefined") || (responseObject == null)) {
                    handleError(this.i18n["catalog.gxe.dialog.invalidResponse"]);
                } else {
                    var oError = responseObject.error;
                    if ((typeof (oError) != "undefined") && (oError != null)) {
                        handleError(oError.message);
                    } else {
                        //handleError(this.i18n["catalog.gxe.dialog.invalidResponse"]);
                        //return;
                        var cfgDefinition = responseObject.cfgDefinition;
                        var sResponseXml = responseObject.xml;
                        if ((typeof (cfgDefinition) == "undefined") || (cfgDefinition == null)) {
                            handleError(this.i18n["catalog.gxe.dialog.invalidResponse"]);
                        } else if ((typeof (sResponseXml) == "undefined") || (sResponseXml == null)) {
                            handleError(this.i18n["catalog.gxe.dialog.invalidResponse"]);
                        } else {
                            var dom = this._makeXmlDom(sResponseXml);
                            this._prepForEditorLoad();
                            gxe.cfg.initialize(cfgDefinition);
                            var elHtmlParent = dojo.byId("gxeDocument");
                            elHtmlParent.style.display = "none";
                            this.gxeContext.buildUI(cfgDefinition, elHtmlParent, dom);
                            elHtmlParent.style.display = "block";
                            this._onEditorLoaded();
                            oWorking.hide();
                        }
                    }
                }
            })
        });
    },

    // interrogate the details of an XML document 
    _execInterrogateDetails: function (oWorking, sXml) {

        var setDetails = function (sMessage) {
            oWorking.hide();
            var elMessage = document.createElement("div");
            elMessage.appendChild(document.createTextNode(sMessage));
            dojo.style(elMessage, { margin: "10px" });
            dojo.byId("gxeDetailsSection").appendChild(elMessage);
        };

        var setDetailsHtml = function (sInnerHtml) {
            oWorking.hide();
            dojo.byId("gxeDetailsSection").innerHTML = sInnerHtml;
        };

        var sUrl = this.gxeContext.contextPath + "/gxe/interrogate/details";
        sUrl = this._adaptProxyUrl(sUrl);
        dojo.xhrPost({
            handleAs: "json",
            headers: { "Content-Type": "text/plain; charset=UTF-8" },
            postData: sXml,
            url: sUrl,
            error: dojo.hitch(this, function (responseObject, ioArgs) {
                var sError = responseObject.message;
                setDetails(sError);
                this._changeMode("view");
            }),
            load: dojo.hitch(this, function (responseObject, ioArgs) {
                if ((typeof (responseObject) == "undefined") || (responseObject == null)) {
                    setDetails(this.i18n["catalog.gxe.dialog.invalidResponse"]);
                } else {
                    var oError = responseObject.error;
                    if ((typeof (oError) != "undefined") && (oError != null)) {
                        setDetails(oError.message);
                    } else {
                        var sInnerHtml = responseObject.details;
                        if ((typeof (sInnerHtml) == "undefined") || (sInnerHtml == null)) {
                            setDetails(this.i18n["catalog.gxe.dialog.invalidResponse"]);
                        } else {
                            setDetailsHtml(sInnerHtml);
                        }
                    }
                }
                this._changeMode("view");
            })
        });
    },


    // load an XML document from the document store
    _execLoadXml: function () {

        var oWorking = this._showWorking(this.i18n["catalog.gxe.dialog.loadingDocument"]);

        var setDetails = function (oItemMetadata, sMessage) {
            oWorking.hide();
            var elMessage = document.createElement("div");
            elMessage.appendChild(document.createTextNode(sMessage));
            dojo.style(elMessage, { margin: "10px" });
            dojo.byId("gxeDetailsSection").appendChild(elMessage);
        };

        if ((this.originalXml != null) && (this.originalXml.length > 0)) {
            dojo.byId("gxeXmlView").value = this.originalXml;
            this._execInterrogateDetails(oWorking, this.originalXml);

        } else if (this.documentClient != null) {
            this.documentClient.loadXml({
                error: dojo.hitch(this, function (responseObject, ioArgs) {
                    setDetails(this, this.i18n["catalog.gxe.dialog.noMetadata"]);
                    this._changeMode("view");
                }),
                load: dojo.hitch(this, function (responseObject, ioArgs) {
                    this.originalXml = null;
                    if (typeof (responseObject) == "undefined") {
                        setDetails(this, this.i18n["catalog.gxe.dialog.invalidResponse"]);
                        this._changeMode("view");
                    } else if (typeof (responseObject) == "string") {
                        this.originalXml = responseObject;
                    }
                    if ((this.originalXml != null) && (this.originalXml.length > 0)) {
                        dojo.byId("gxeXmlView").value = this.originalXml;
                        this._execInterrogateDetails(oWorking, this.originalXml);
                    } else {
                        setDetails(this, this.i18n["catalog.gxe.dialog.noMetadata"]);
                        this._changeMode("view");
                    }
                })
            });
        }
    },

    // save the XML document to the document store
    _execSaveXml: function (bAsDraft) {
        var xmlGenerator = new gxe.xml.Generator();
        var sXml = xmlGenerator.generate(this.gxeContext, bAsDraft);
        if (!xmlGenerator.hadValidationErrors) {
            if ((sXml != null) && (sXml.length > 0)) {
                if (this.documentClient != null) {
                    var oWorking = this._showWorking(this.i18n["catalog.gxe.dialog.savingDocument"]);
                    this.documentClient.saveXml({
                        xml: sXml,
                        error: dojo.hitch(this, function (responseObject, ioArgs) {
                            var s1 = this.i18n["catalog.gxe.dialog.save"];
                            var s2 = this.i18n["catalog.gxe.dialog.genericError"];
                            var s3 = responseObject.message;
                            oWorking.hide();
                            this._showErrorDialog(s1, s2, s3);
                        }),
                        load: dojo.hitch(this, function (responseObject, ioArgs) {
                            this.onMetadataSaved();
                            oWorking.hide();
                        })
                    });
                }
            }
        }
    },

    _getInternetExplorerVersion:function()
	// Returns the version of Internet Explorer or a -1
	// (indicating the use of another browser).
	{
	  var rv = -1; // Return value assumes failure.
	  if (navigator.appName == 'Microsoft Internet Explorer')
	  {
		var ua = navigator.userAgent;
		var re  = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
		if (re.exec(ua) != null)
		  rv = parseFloat( RegExp.$1 );
	  }
	  return rv;
	},
		
	_isIE:function(){
		return this._checkBrowserVersion();
	},
    
    // make and connect a form for document download
    _makeDownloadForm: function (elButtonBar, elDownloadButton) {
        var sDownloadUrl = this.gxeContext.contextPath + "/gxe/echo/attachment";
        sDownloadUrl = this._adaptProxyUrl(sDownloadUrl);

        var elForm = document.createElement("form");
        elForm.id = "gxeDownloadForm";
        dojo.style(elForm, { display: "inline" });
        elForm.setAttribute("method", "post");
        elForm.setAttribute("enctype", "application/x-www-form-urlencoded");
        elForm.setAttribute("target", "_blank");
        elForm.action = sDownloadUrl;
        elButtonBar.appendChild(elForm);
        var elHidden = document.createElement("input");
        elHidden.id = "gxeDownloadXml";
        elHidden.setAttribute("type", "hidden");
        elHidden.name = "xml";
        elForm.appendChild(elHidden);

        dojo.connect(elDownloadButton, "onclick", this, dojo.hitch(this, function (e) {
            var bAsDraft = true;
            var xmlGenerator = new gxe.xml.Generator();
            var sXml = xmlGenerator.generate(this.gxeContext, bAsDraft);
            if (!xmlGenerator.hadValidationErrors) {
                if ((sXml != null) && (sXml.length > 0)) {
                    dojo.byId("gxeDownloadXml").value = sXml;
                    dojo.byId("gxeDownloadForm").submit();
                }
            }
        }));
    },

    // make the import button (file upload)
    _makeImportButton: function (elOffScreen, sLabel, oParentDialog) {
        var b162 = false;
        if ((dojo.version.major <= 1) && (dojo.version.minor <= 6)) {
          b162 = true;
        }
        
        var elButton = document.createElement("button");
        elButton.className = "gxeButton";
        if ((typeof (sLabel) != "undefined") && (sLabel != null)) {
            elButton.appendChild(document.createTextNode(sLabel));
        } else {
            elButton.appendChild(document.createTextNode(this.i18n["catalog.gxe.dialog.import"]));
        }

        var sImportUrl = this.gxeContext.contextPath + "/gxe/interrogate/multipart";
        // HTML5 based dojox.form.Uploader 1.6 requires a wrapped response.
        // i.e. responseObject={}
        if (b162) sImportUrl += "?wrap=true";
        sImportUrl = this._adaptProxyUrl(sImportUrl);
        var djUploader = new dojox.form.Uploader({
            label: "offscreenUploader",
            multiple: false,
            uploadOnSelect: false,
            url: sImportUrl
        });
        elOffScreen.appendChild(djUploader.domNode);

        dojo.connect(elButton, "onclick", this, dojo.hitch(this, function (e) {
            djUploader.reset();
            if (b162) {
              dojo.query(".dojoxInputNode", djUploader.domNode).forEach(function (item) {
                item.click();
              });
            } else {
              dojo.query("input", djUploader.domNode).forEach(function (item) {
                item.click();
              });
            }
        }));

        var oThis = this;
        var oWorking = null;
        var sImportMsg = null;
        dojo.connect(djUploader, "onChange", dojo.hitch(this, function (dataArray) {
            var sImportFileName = "";
            dojo.forEach(dataArray, function (d) {
                sImportFileName = d.name;
            });
            sImportMsg = this.i18n["catalog.gxe.dialog.importingFilename"];
            sImportMsg = sImportMsg.replace("{0}", sImportFileName);
            oWorking = this._showWorking(sImportMsg);
            if ((typeof (oParentDialog) != "undefined") && (oParentDialog != null)) {
                oParentDialog.hide();
            }
            djUploader.upload();
        }));

        // errors don't seem to be fired by this dijit (onError/onAbort don't work)
        dojo.connect(djUploader, "onComplete", this, dojo.hitch(this, function (responseObject) {
            var handleError = function (sError) {
                oWorking.hide();
                oThis._showErrorDialog(oThis.i18n["catalog.gxe.dialog.import"],
            sImportMsg, sError);
            };

            if ((typeof (responseObject) == "undefined") || (responseObject == null)) {
                handleError(this.i18n["catalog.gxe.dialog.invalidResponse"]);
            } else {
                var oError = responseObject.error;
                if ((typeof (oError) != "undefined") && (oError != null)) {
                    handleError(oError.message);
                } else {
                    var cfgDefinition = responseObject.cfgDefinition;
                    var sXml = responseObject.xml;
                    if ((typeof (cfgDefinition) == "undefined") || (cfgDefinition == null)) {
                        handleError(this.i18n["catalog.gxe.dialog.invalidResponse"]);
                    } else if ((typeof (sXml) == "undefined") || (sXml == null)) {
                        handleError(this.i18n["catalog.gxe.dialog.invalidResponse"]);
                    } else {
                        //handleError(this.i18n["catalog.gxe.dialog.invalidResponse"]);
                        //return;
                        var dom = this._makeXmlDom(sXml);
                        this._prepForEditorLoad();
                        gxe.cfg.initialize(cfgDefinition);
                        var elHtmlParent = dojo.byId("gxeDocument");
                        elHtmlParent.style.display = "none";
                        this.gxeContext.buildUI(cfgDefinition, elHtmlParent, dom);
                        elHtmlParent.style.display = "block";
                        this._onEditorLoaded();
                        oWorking.hide();
                    }
                }
            }
        }));

        return elButton;
    },
    
    // make the import button for IE (file upload)
    _makeImportButtonForIE: function (eIOptions,elOffScreen, sLabel, oParentDialog) {
        var b162 = false;
        if ((dojo.version.major <= 1) && (dojo.version.minor <= 6)) {
          b162 = true;
		}
		var elfrm = document.createElement("iframe");
		elfrm.setAttribute("name", "uploadFrame");
		elfrm.setAttribute("id", "uploadFrame");
		elfrm.setAttribute("frameborder", "0");
		elfrm.setAttribute("scrolling", "no");	
		elfrm.style.width = 1+"px";
		elfrm.style.height = 1+"px";		
	    
        var elForm = document.createElement("form");
		elForm.setAttribute("method", "POST");
		elForm.id = "loadXmlForm";
		elForm.setAttribute('target', 'uploadFrame');
		elForm.setAttribute("enctype", "multipart/form-data");
        var elButton = document.createElement("button");
		var oThis = this;
        var oWorking = null;
        var sImportMsg = null;
		dojo.connect(elButton, "onclick", this, dojo.hitch(this, function (e) {
			dojo.stopEvent(e);
			if(dijit.byId("uploader").domNode.children.uploadedfile.value.length == 0){
				oThis._showErrorDialog(oThis.i18n["catalog.gxe.dialog.loadTitle"],
							oThis.i18n["catalog.gxe.dialog.genericError"], oThis.i18n["catalog.gxe.dialog.selectXmlFile"]);				
				return false;
			}
			var sImportFileName = "";
            sImportMsg = this.i18n["catalog.gxe.dialog.importingFilename"];
            sImportMsg = sImportMsg.replace("{0}", sImportFileName);
            oWorking = this._showWorking(sImportMsg);
            if ((typeof (oParentDialog) != "undefined") && (oParentDialog != null)) {
                oParentDialog.hide();
            }
			elForm.submit();
		 }));
		elButton.className = "gxeButton gxeButton-enabled";
	    elButton.id = "importBtn";
        if ((typeof (sLabel) != "undefined") && (sLabel != null)) {
		    elButton.appendChild(document.createTextNode(this.i18n["catalog.gxe.dialog.load"]));
        } else {
			sLabel = this.i18n["catalog.gxe.dialog.import"];
			elButton.appendChild(document.createTextNode(this.i18n["catalog.gxe.dialog.load"]));
        }

        var sImportUrl =  this.gxeContext.contextPath + "/gxe/interrogate/multipart?isIE=true";
        // HTML5 based dojox.form.Uploader 1.6 requires a wrapped response.
        // i.e. responseObject={}
        if (b162) sImportUrl += "?wrap=true";
        sImportUrl = this._adaptProxyUrl(sImportUrl);
						
        var djUploader = new dojox.form.Uploader({
			xclass: "gxeButton gxeButton-enabled",
			id: "uploader",
            label: sLabel,
            multiple: false,
            uploadOnSelect: false 
        });
		elForm.action = sImportUrl;
		elForm.appendChild(djUploader.domNode);
		elForm.appendChild(elButton);
        eIOptions.appendChild(elForm);
		eIOptions.appendChild(elfrm);
		
		dojo.connect(elfrm, "onload", this, dojo.hitch(this, function (e) {
			var responseObject = elfrm.contentWindow["gxeImportFileResponse"];
			if(!responseObject){
				return;
			}
			var handleError = function (sError) {
			if(oWorking)
                oWorking.hide();
                oThis._showErrorDialog(oThis.i18n["catalog.gxe.dialog.import"],
            sImportMsg, sError);
            };

            if ((typeof (responseObject) == "undefined") || (responseObject == null)) {
                handleError(this.i18n["catalog.gxe.dialog.invalidResponse"]);
            } else {
                var oError = responseObject.error;
                if ((typeof (oError) != "undefined") && (oError != null)) {
                    handleError(oError.message);
                } else {
                    var cfgDefinition = responseObject.cfgDefinition;
                    var sXml = responseObject.xml;
                    if ((typeof (cfgDefinition) == "undefined") || (cfgDefinition == null)) {
                        handleError(this.i18n["catalog.gxe.dialog.invalidResponse"]);
                    } else if ((typeof (sXml) == "undefined") || (sXml == null)) {
                        handleError(this.i18n["catalog.gxe.dialog.invalidResponse"]);
                    } else {
                        var dom = this._makeXmlDom(sXml);
                        this._prepForEditorLoad();
                        gxe.cfg.initialize(cfgDefinition);
                        var elHtmlParent = dojo.byId("gxeDocument");
                        elHtmlParent.style.display = "none";
                        this.gxeContext.buildUI(cfgDefinition, elHtmlParent, dom);
                        elHtmlParent.style.display = "block";
                        this._onEditorLoaded();
						if(oWorking)
							oWorking.hide();
                    }
                }
            }
		}));
    },

    // make an XML document from a string
    _makeXmlDom: function (sXml) {
        var dom = null;
        try {
            if (window.DOMParser) {
                var parser = new DOMParser();
                dom = parser.parseFromString(sXml, "text/xml");
            } else if (window.ActiveXObject) {
                dom = new ActiveXObject("MSXML2.DOMDocument");
                dom.async = "false";
                dom.loadXML(sXml);
            }
        } catch (xmlParseErr) {
            // TODO: alert or fail here?
            dom = null;
            console.error("Unable to dynamically parse existing metadata XML.");
            console.error(xmlParseErr);
        }
        return dom;
    },

    // fired when the editor has been loaded with a document
    _onEditorLoaded: function () {
        dojo.query(".gxeToolRequiresEditor").forEach(function (item) {
            item.disabled = false;
            if (!dojo.hasClass(item, "gxeButton-enabled")) {
                dojo.addClass(item, "gxeButton-enabled");
            }
        });
    },

    // prepare for the loading of a document into the editor
    _prepForEditorLoad: function () {
        dojo.query(".gxeToolRequiresEditor").forEach(function (item) {
            item.disabled = true;
            if (dojo.hasClass(item, "gxeButton-enabled")) {
                dojo.removeClass(item, "gxeButton-enabled");
            }
        });
        var ctlMessageArea = this.gxeContext.messageArea;
        if ((typeof (ctlMessageArea) != "undefined") && (ctlMessageArea != null)) {
            ctlMessageArea.clearAll();
        }
        var elHtmlParent = dojo.byId("gxeDocument");
        elHtmlParent.style.display = "none";
        while (elHtmlParent.childNodes.length >= 1) elHtmlParent.removeChild(elHtmlParent.firstChild);
        var gxeContext2 = new gxe.Context();
        gxeContext2.contextPath = this.gxeContext.contextPath;
        gxeContext2.gptMapConfig = this.gxeContext.gptMapConfig;
        gxeContext2.newDocumentId = this.gxeContext.newDocumentId;
        this.gxeContext = gxeContext2;
    },


    // show an error message dialog
    _showErrorDialog: function (sTitle, sMsg, sDetails) {
        var elDialogContent = document.createElement("div");
        var oDialog = new dijit.Dialog({
            title: sTitle,
            content: elDialogContent
        });
        dojo.connect(oDialog, "onHide", dojo.hitch(this, function () {
            setTimeout(dojo.hitch(this, function () {
                oDialog.destroyRecursive();
            }), 300);
        }));

        var elMsgArea = document.createElement("div");
        dojo.addClass(elMsgArea, "gxeMessageArea");
        elMsgArea.style.display = "block";
        var elMsgList = document.createElement("div");
        dojo.addClass(elMsgList, "gxeMessageList");
        elMsgArea.appendChild(elMsgList);
        var elErrMsg = document.createElement("div");
        elErrMsg.appendChild(document.createTextNode(sMsg));
        dojo.addClass(elErrMsg, "error");
        dojo.style(elErrMsg, {
            marginTop: "10px",
            marginBottom: "10px"
        });
        elMsgList.appendChild(elErrMsg);

        if ((typeof (sDetails) != "undefined") && (sDetails != null)) {
            var elDetMsg = document.createElement("div");
            elDetMsg.appendChild(document.createTextNode(sDetails));
            elErrMsg.appendChild(elDetMsg);
        }
        var sCancel = this.i18n["catalog.gxe.dialog.cancel"];
        var elCancel = document.createElement("button");
        elCancel.appendChild(document.createTextNode(sCancel));
        dojo.style(elCancel, { marginLeft: "50px" });
        elMsgList.appendChild(elCancel);
        elDialogContent.appendChild(elMsgArea);
        dojo.connect(elCancel, "onclick", dojo.hitch(this, function (e) {
            oDialog.hide();
        }));

        oDialog.show();
        return oDialog;
    },

    // show the dialog for loading an XML type or a local file into the editor
    _showLoadDialog: function () {
        var bEnabled = true;

        var oThis = this;
        var oWorking = null;
        var sWorkingMsg = null;
        var elDialogContent = document.createElement("div");
        var oDialog = new dijit.Dialog({
            title: this.i18n["catalog.gxe.dialog.loadTitle"],
            content: elDialogContent
        });
        dojo.connect(oDialog, "onHide", dojo.hitch(this, function () {
            setTimeout(dojo.hitch(this, function () {
                oDialog.destroyRecursive();
            }), 300);
        }));


        var elOffScreen = document.createElement("span");
        dojo.addClass(elOffScreen, "dijitOffScreen");
        elDialogContent.appendChild(elOffScreen);

        var elCriteria = document.createElement("div");
        elCriteria.className = "gxePopupPanel";
        elDialogContent.appendChild(elCriteria);

        var elCaption = document.createElement("p");
        elCaption.className = "gxeHeading";
        elCaption.appendChild(document.createTextNode(this.i18n["catalog.gxe.dialog.startNewDocument"]));
        elCriteria.appendChild(elCaption);
        var elTypes = document.createElement("ul");
        elTypes.className = "gxeLinks";
        dojo.style(elTypes, { marginLeft: "10px" });
        elCriteria.appendChild(elTypes);

        if (!(dojo.isIE <= 8)) {
            var elICriteria = document.createElement("div");
            elICriteria.className = "gxePopupPanel";
            dojo.style(elICriteria, { marginTop: "10px" });
            elDialogContent.appendChild(elICriteria);
            var eIPanel = document.createElement("div");
            elICriteria.appendChild(eIPanel);
            var elICaption = document.createElement("p");
            eIPanel.appendChild(elICaption);
            elICaption.appendChild(document.createTextNode(this.i18n["catalog.gxe.dialog.importFile"]));
            elICaption.className = "gxeHeading";
            var eIOptions = document.createElement("div");
            eIOptions.className = "gxeLinks";
            eIPanel.appendChild(eIOptions);
            dojo.style(eIOptions, { marginLeft: "10px" });
            var sILabel = this.i18n["catalog.gxe.dialog.selectXmlFile"];
            var elIButton;
			/*
			 * IE10 supports HTML5 plugin. IE9 not supported with post 2.1 release of Portal 
            if(this._isIE()){
				this._makeImportButtonForIE(eIOptions,elOffScreen, sILabel, oDialog);
			}else{*/
				elIButton = this._makeImportButton(elOffScreen, sILabel, oDialog);
				elIButton.className = "gxeButton gxeButton-enabled";
                eIOptions.appendChild(elIButton);
			// }
        }

        var handleError = function (sError) {
            oWorking.hide();
            oThis._showErrorDialog(this.i18n["catalog.gxe.dialog.loadTitle"], sWorkingMsg, sError);
        };

        var aDocTypes = null;
        var oDocTypes = this.config["ItemMetadata.documentTypeKeys"];
        if ((typeof (oDocTypes) != "undefined") && (oDocTypes != null)) {
            try {
                if (oDocTypes != "*") {
                    aDocTypes = oDocTypes.split(",");
                }
            } catch (err) {
                aDocTypes = null;
            }
        }
        var addTypes = function (oTypes) {
            for (var iType = 0; iType < oTypes.length; iType++) {
                var bAddType = true;
                var oType = oTypes[iType];
                if (aDocTypes != null) {
                    bAddType = false;
                    for (var iDocType = 0; iDocType < aDocTypes.length; iDocType++) {
                        var sDocType = aDocTypes[iDocType];
                        console.log(sDocType + " " + oType.key);
                        if ((sDocType == "*") || (sDocType == oType.key)) {
                            bAddType = true;
                            break;
                        }
                    }
                }
                if (bAddType) {
                    var elItem = document.createElement("li");
                    elItem.className = "gxeButton gxeButton-enabled";
                    elItem.appendChild(document.createTextNode(oType.label));
                    elTypes.appendChild(elItem);
                    connectType(elItem, oType);
                }
            }
        };

        var connectType = function (elItem, oType) {
            dojo.connect(elItem, "onclick", this, dojo.hitch(this, function (e) {
                sWorkingMsg = oThis.i18n["catalog.gxe.dialog.loadingDocumentType"];
                sWorkingMsg = sWorkingMsg.replace("{0}", oType.label);
                oWorking = oThis._showWorking(sWorkingMsg);
                oDialog.hide();

                var sDefUrl = oThis.gxeContext.contextPath + "/gxe/definition/type";
                sDefUrl += "?key=" + encodeURIComponent(oType.key);
                sDefUrl = oThis._adaptProxyUrl(sDefUrl);
                dojo.xhrGet({
                    handleAs: "json",
                    url: sDefUrl,
                    error: dojo.hitch(this, function (responseObject, ioArgs) {
                        handleError(responseObject.message);
                    }),
                    load: dojo.hitch(this, function (responseObject, ioArgs) {
                        if ((typeof (responseObject) == "undefined") || (responseObject == null)) {
                            handleError(oThis.i18n["catalog.gxe.dialog.invalidResponse"]);
                        } else {
                            var oError = responseObject.error;
                            if ((typeof (oError) != "undefined") && (oError != null)) {
                                handleError(oError.message);
                            } else {
                                var cfgDefinition = responseObject.cfgDefinition;
                                if ((typeof (cfgDefinition) == "undefined") || (cfgDefinition == null)) {
                                    handleError(oThis.i18n["catalog.gxe.dialog.invalidResponse"]);
                                } else {
                                    oThis._prepForEditorLoad();
                                    //handleError(oThis.i18n["catalog.gxe.dialog.invalidResponse"]);
                                    //return;
                                    gxe.cfg.initialize(cfgDefinition);
                                    var elHtmlParent = dojo.byId("gxeDocument");
                                    elHtmlParent.style.display = "none";
                                    oThis.gxeContext.buildUI(cfgDefinition, elHtmlParent, null);
                                    elHtmlParent.style.display = "block";
                                    oThis._onEditorLoaded();
                                    oWorking.hide();
                                }
                            }
                        }
                    })
                });
            }));
        };

        var sTypesUrl = this.gxeContext.contextPath + "/gxe/definition/types";
        sTypesUrl = this._adaptProxyUrl(sTypesUrl);
        dojo.xhrGet({
            handleAs: "json",
            url: sTypesUrl,
            error: dojo.hitch(this, function (responseObject, ioArgs) {
                addError(responseObject.message);
                oDialog.show();
            }),
            load: dojo.hitch(this, function (responseObject, ioArgs) {
                if ((typeof (responseObject) == "undefined") || (responseObject == null)) {
                    addError(this.i18n["catalog.gxe.dialog.invalidResponse"]);
                } else {
                    var oError = responseObject.error;
                    if ((typeof (oError) != "undefined") && (oError != null)) {
                        addError(oError.message);
                    } else {
                        var oTypes = responseObject.types;
                        if ((typeof (oTypes) == "undefined") || (oTypes == null)) {
                            addError(oThis.i18n["catalog.gxe.dialog.invalidResponse"]);
                        } else if (oTypes.length == 0) {
                            var elNone = document.createElement("span");
                            elNone.appendChild(document.createTextNode(this.i18n["catalog.gxe.dialog.noDefinedTypes"]));
                            elWorking.appendChild(elNone);
                        } else {
                            addTypes(oTypes);
                        }
                    }
                }
                oDialog.show();
            })
        });
    },
    
    
    _showWorking: function (sMsg) {

        var elPos = dojo.byId("gxeMainWorkingMessageArea.anchor");
        var oPos = dojo.position(elPos, true);

        var oUnderlay = new dijit.DialogUnderlay();
        oUnderlay.show();

        var oWorking = document.createElement("span");
        oWorking.startTime = (new Date()).getTime();
        oWorking.id = "gxeMainWorkingMessageArea";
        dojo.style(oWorking, {
            position: "absolute",
            marginLeft: "5px",
            marginRight: "5px",
            left: oPos.x + "px",
            top: oPos.y + 2 + "px",
            opacity: "1",
            zIndex: "9998",
            cursor: "pointer"
        });

        var elLoading = document.createElement("img");
        elLoading.id = "gxeMainWorkingMessageArea.loadingImage";
        elLoading.src = this.gxeContext.contextPath + "/catalog/images/loading.gif";
        dojo.style(elLoading, {
            marginLeft: "5px",
            marginRight: "5px",
            width: "16px",
            height: "16px",
            verticalAlign: "bottom"
        });
        oWorking.appendChild(elLoading);

        var elText = document.createElement("span");
        elText.id = "gxeMainWorkingMessageArea.text";
        elText.appendChild(document.createTextNode(sMsg));
        oWorking.appendChild(elText);

        dojo.connect(oWorking, "onclick", dojo.hitch(this, function (e) {
            try { dojo.body().removeChild(oWorking); } catch (error) { }
            try { oUnderlay.hide(); } catch (error) { }
        }));

        dojo.body().appendChild(oWorking);

        oWorking.hide = function () {
            var tStart = oWorking.startTime;
            var tEnd = (new Date()).getTime();
            var tElapsed = (tEnd - tStart);
            if (tElapsed < 750) {
                var tDelay = Math.min(tElapsed, 750);
                setTimeout(dojo.hitch(this, function () {
                    try { dojo.body().removeChild(oWorking); } catch (error) { }
                    try { oUnderlay.hide(); } catch (error) { }
                }), tDelay);
            } else {
                try { dojo.body().removeChild(oWorking); } catch (error) { }
                try { oUnderlay.hide(); } catch (error) { }
            }
        };

        return oWorking;
    }

});

dojo.provide("gpt.gxe.DocumentClient");
dojo.declare("gpt.gxe.DocumentClient",null,{
  
  /* this fails on IE 8
  constructor: function(args){
    dojo.safeMixin(this,args);
  },
  */

  handleError: function(params,responseObject,ioArgs) {
    if ((typeof(params) != "undefined") && (params != null) &&
        (typeof(params.error) == "function")) {
      params.error(responseObject,ioArgs);
    } else {
      // TODO need a generic error handler
    }
  },
  
  handleLoad: function(params,responseObject,ioArgs) {
    if ((typeof(params) != "undefined") && (params != null) &&
        (typeof(params.load) == "function")) {
      params.load(responseObject,ioArgs);
    } 
  },
  
  loadXml: function(params) {
  },
  
  saveXml: function(params) {
  }
  
});

dojo.provide("gpt.gxe.ProxyUrlAdapter");
dojo.declare("gpt.gxe.ProxyUrlAdapter",null,{
  serverProxyUrl: null,
  
  /* this fails on IE 8
  constructor: function(args){
    dojo.safeMixin(this,args);
  },
  */
  
  adaptUrl: function(sUrl) {
    if (this.serverProxyUrl != null) { 
      sUrl = this.serverProxyUrl+"?url="+encodeURIComponent(sUrl);
    }
    return sUrl;
  }

});
