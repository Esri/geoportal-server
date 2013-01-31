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
 * gxe.js (v1.2)
 * Geoportal XML editor.
 */

dojo.require("dijit.Dialog");

/**
 * @fileOverview Geoportal XML Editor (GXE).
 * @version 1.2
 */

/**
 * @class GXE configuration object structure (JSON).
 * <br/>Represents the logical structure for the response of a request
 * for a Geoportal XML editor definition.
 * @name cfgObject
 * @property {String} name the qualified configuration name
 * @property {String} value the value (optional)
 * @property {cfgAttribute[]} attributes the configuration attributes
 * @property {cfgObject[]} children the children (optional)
 */

/**
 * @class GXE configuration attribute structure (JSON).
 * @name cfgAttribute
 * @property {String} name the qualified configuration name
 * @property {String} value the value
 */

/**
 * @class Primary namespace for the Geoportal XML Editor (GXE).
 * @static
 * @name gxe
 */
var gxe = {

  /**
   * @class Static utilities for processing JSON based configuration objects associated
   * with an editor definition.
   * @static
   * @name gxe.cfg
   * @property {String} pfxGxe the GXE namespace prefix 
   *   <br/>(="g")
   * @property {String} pfxHtml the GXE HTML namespace prefix 
   *   <br/>(="h")
   * @property {String} uriGxe the GXE namespace URI 
   *   <br/>(="http://www.esri.com/geoportal/gxe")
   * @property {String} uriGxeHtml the GXE HTML namespace URI 
   *   <br/>(="http://www.esri.com/geoportal/gxe/html")
   */
  cfg: {
    pfxGxe: "g",
    pfxHtml: "h",
    uriGxe: "http://www.esri.com/geoportal/gxe",
    uriGxeHtml: "http://www.esri.com/geoportal/gxe/html",
  
    /**
     * Finds an immediate child of a configuration object.
     * @example Example:
     * gxe.cfg.findChild(cfgObject,"http://www.esri.com/geoportal/gxe","options");
     * @function 
     * @name findChild
     * @memberOf gxe.cfg
     * @param {cfgObject} cfgObject the configuration object to process
     * @param {String} namespace the configuration namespace of the child to find
     * @param {String} name the configuration name of the child to find
     * @return {cfgObject} the located child (null if not found)
     */
    findChild: function(cfgObject,namespace,name) {
      var children = cfgObject.children;
      var nChildren = 0;
      if (children != null) nChildren = children.length;
      for (var i=0; i<nChildren; i++) {
        var child = children[i];
        if ((child.namespace == namespace) && (child.name == name)) {
          return child;
        }
      }
      return null;
    },
    
    /**
     * Finds an immediate child of a configuration object within the GXE namespace.
     * <br/>(i.e. namespace "http://www.esri.com/geoportal/gxe")
     * @example Example:
     * gxe.cfg.findGxeChild(cfgObject,"options");
     * @function 
     * @name findGxeChild
     * @memberOf gxe.cfg
     * @param {cfgObject} cfgObject the configuration object to process
     * @param {String} name the configuration name of the child to find
     * @return {cfgObject} the located child (null if not found)
     */
    findGxeChild: function(cfgObject,name) {
      return this.findChild(cfgObject,this.uriGxe,name);
    },

    /**
     * Executes a function for each immediate and matching child of a configuration object.
     * <br/>
     * <br/>The callback function will have the following signature: function(cfgChild) {}
     * <br/>The callback function can return the String "break" to terminate the loop.
     * @function 
     * @name forEachChild
     * @memberOf gxe.cfg
     * @param {cfgObject} cfgObject the configuration object to process
     * @param {String} namespace the configuration namesapce to match (* to match any)
     * @param {String} name the configuration name to match (* to match any)
     * @param {function} callback the callback function 
     */
    forEachChild: function(cfgObject,namespace,name,callback) {
      var children = cfgObject.children;
      var nChildren = 0;
      if (children != null) nChildren = children.length;
      for (var i=0; i<nChildren; i++) {
        var child = children[i];
        var bMatch = ((namespace == "*") || (child.namespace == namespace));
        if (bMatch) bMatch = ((name == "*") || (child.name == name));
        if (bMatch) {
          var _ret = callback(child);
          if ((typeof(_ret) == "string") && (_ret == "break")) break;
        }
      }
    },

    /**
     * Executes a function for each HTML attribute associated with a configuration object.
     * <br/>(i.e. each attribute within namespace "http://www.esri.com/geoportal/gxe/html")
     * <br/>
     * <br/>The callback function will have the following signature: function(cfgAttribute) {}
     * <br/>The callback function can return the String "break" to terminate the loop.
     * @function 
     * @name forEachHtmlAttribute
     * @memberOf gxe.cfg
     * @param {cfgObject} cfgObject the configuration object to process
     * @param {function} callback the callback function 
     */
    forEachHtmlAttribute: function(cfgObject,callback) {
      var attributes = cfgObject.attributes;
      var nAttributes = 0;
      if (attributes != null) nAttributes = attributes.length;
      for (var i=0; i<nAttributes; i++) {
        var attribute = attributes[i];
        if (attribute.namespace == this.uriGxeHtml) {
          var _ret = callback(attribute);
          if ((typeof(_ret) == "string") && (_ret == "break")) break;
        }
      }
    },
  
    /**
     * Gets an attribute value.
     * @example Example:
     * gxe.cfg.getAttributeValue(cfgObject,"http://www.esri.com/geoportal/gxe","minOccurs");
     * @function 
     * @name getAttributeValue
     * @memberOf gxe.cfg
     * @param {cfgObject} cfgObject the configuration object to process
     * @param {String} namespace the configuration namespace of the attribute to find
     * @param {String} name the configuration name of the attribute to find
     * @return {Object} the attribute value (null if not found)
     */
    getAttributeValue: function(cfgObject,namespace,name) {
      var attributes = cfgObject.attributes;
      var nAttributes = 0;
      if (attributes != null) nAttributes = attributes.length;
      for (var i=0; i<nAttributes; i++) {
        var attribute = attributes[i];
        if ((attribute.namespace == namespace) && (attribute.name == name)) {
          return attribute.value;
        }
      }
      return null;
    },
  
    /**
     * Gets an attribute value within the GXE namespace.
     * <br/>(i.e. namespace "http://www.esri.com/geoportal/gxe")
     * @example Example:
     * gxe.cfg.getGxeAttributeValue(cfgObject,"minOccurs");
     * @function 
     * @name getGxeAttributeValue
     * @memberOf gxe.cfg
     * @param {cfgObject} cfgObject the configuration object to process
     * @param {String} name the configuration name of the attribute to find
     * @return {Object} the attribute value (null if not found)
     */
    getGxeAttributeValue: function(cfgObject,name) {
      return this.getAttributeValue(cfgObject,this.uriGxe,name);
    },

    /**
     * Gets an attribute value within the GXE HTML namespace.
     * <br/>(i.e. namespace "http://www.esri.com/geoportal/gxe/html")
     * @example Example:
     * gxe.cfg.getGxeHtmlAttributeValue(cfgObject,"maxlength");
     * @function 
     * @name getGxeHtmlAttributeValue
     * @memberOf gxe.cfg
     * @param {cfgObject} cfgObject the configuration object to process
     * @param {String} name the configuration name of the attribute to find
     * @return {Object} the attribute value (null if not found)
     */
    getGxeHtmlAttributeValue: function(cfgObject,name) {
      return this.getAttributeValue(cfgObject,this.uriGxeHtml,name);
    },
    
    /**
     * Gets the g:label attribute value associated with a configuration object.
     * <br/>If null, the associated XML target name will be returned.
     * @function 
     * @name getLabelText
     * @memberOf gxe.cfg
     * @param {cfgObject} cfgObject the configuration object to process
     * @return {String} the attribute value (null if not found) 
     */
    getLabelText: function(cfgObject) {
      var sLabel = this.getGxeAttributeValue(cfgObject,"label");
      if ((sLabel != null) && (sLabel.length > 0)) {
        return sLabel;
      } else {
        return this.getGxeAttributeValue(cfgObject,"targetName");
      }
    },
    
    /**
     * Gets the g:maxOccurs attribute value associated with a configuration object.
     * @function 
     * @name getMaxOccurs
     * @memberOf gxe.cfg
     * @param {cfgObject} cfgObject the configuration object to process
     * @return {String} the attribute value (null if not found) 
     */
    getMaxOccurs: function(cfgObject) {
      return this.getGxeAttributeValue(cfgObject,"maxOccurs");
    },

    /**
     * Gets the g:minOccurs attribute value associated with a configuration object.
     * @function 
     * @name getMinOccurs
     * @memberOf gxe.cfg
     * @param {cfgObject} cfgObject the configuration object to process
     * @return {String} the attribute value (null if not found) 
     */
    getMinOccurs: function(cfgObject) {
      return this.getGxeAttributeValue(cfgObject,"minOccurs");
    },

    /**
     * Gets the name for the XML target associated with a configuration object.
     * @function 
     * @name getTargetName
     * @memberOf gxe.cfg
     * @param {cfgObject} cfgObject the configuration object to process
     * @return {String} the target name
     */
    getTargetName: function(cfgObject) {
      if (cfgObject.targetNameOverride != null) return cfgObject.targetNameOverride;
      else return this.getGxeAttributeValue(cfgObject,"targetName");
    },

    /**
     * Gets the namespace for the XML target associated with a configuration object.
     * <br/>If the supplied configuration object was not directly configured
     * with a g:targetNS attribute, then the value will be inherited from the first 
     * applicable ancestor.
     * @function 
     * @name getTargetNS
     * @memberOf gxe.cfg
     * @param {cfgObject} cfgObject the configuration object to process
     * @return {String} the target namespace
     */
    getTargetNS: function(cfgObject) {
      if (cfgObject.targetNSOverride != null) return cfgObject.targetNSOverride;
      var cfgCheck = cfgObject;
      while ((cfgCheck != undefined) && (cfgCheck != null)) {
        var ns = this.getGxeAttributeValue(cfgCheck,"targetNS");
        if (ns != null) return ns;
        cfgCheck = cfgCheck.parent;
      }
    },
    
    /**
     * Initializes the configured definition for an editor. 
     * @function 
     * @name initialize
     * @memberOf gxe.cfg
     * @param {cfgObject} cfgDefinition the configured editor definition (JSON)
     */
    initialize: function(cfgDefinition) {
      this._initializeObject(cfgDefinition,null,null,false);
      var namespaces = new gxe.xml.XmlNamespaces();
      var cfgRoot = gxe.cfg.findChild(cfgDefinition,this.uriGxe,"rootElement");
      if (cfgRoot != null) {
        this._initializeObject(cfgRoot,null,null,false);
        var cfgNamespaces = this.findChild(cfgRoot,this.uriGxe,"namespaces");
        if (cfgNamespaces != null) {
          this._initializeObject(cfgNamespaces,cfgRoot,null,false);
          gxe.cfg.forEachChild(cfgNamespaces,this.uriGxe,"namespace",dojo.hitch(this,function(cfgNS) {
            this._initializeObject(cfgNS,cfgNamespaces,null,false);
            var pfx = this.getGxeAttributeValue(cfgNS,"prefix");
            var uri = this.getGxeAttributeValue(cfgNS,"uri");
            namespaces.add(new gxe.xml.XmlNamespace(pfx,uri));
          }));
        }
      }
      this._initializeObject(cfgDefinition,null,namespaces,true);
    },

    // initializes a configuration object
    _initializeObject: function(cfgObject,cfgParent,gxeNamespaces,bRecurse) {
      cfgObject.parent = cfgParent;
      var attributes = cfgObject.attributes;
      var nAttributes = 0;
      if (attributes != null) nAttributes = attributes.length;
      for (var i=0; i<nAttributes; i++) {
        var attribute = attributes[i];
        attribute.parent = cfgObject;
        this._initializeObjectNS(attribute);
      }
      var children = cfgObject.children;
      var nChildren = 0;
      if (children != null) nChildren = children.length;
      for (var i=0; i<nChildren; i++) {
        var child = children[i];
        this._initializeObjectNS(child);
        if (bRecurse) this._initializeObject(child,cfgObject,gxeNamespaces,true);
      }
      this._initializeTargetNS(cfgObject,gxeNamespaces);     
    },

    // initializes a configuration object name and namespace
    _initializeObjectNS: function(cfgObject) {
      var namespace = cfgObject.namespace;
      var name = cfgObject.name;
      var prefix = null;
      var localName = name;
      var nIdx = name.indexOf(":");
      if (nIdx != -1) {
        prefix = name.substring(0,nIdx);
        localName = name.substring(nIdx+1);
      }
      if (namespace == null) {
        if (prefix == this.pfxGxe) {
          cfgObject.namespace = this.uriGxe;
          cfgObject.name = localName;
        } else if (prefix == this.pfxHtml) {
          cfgObject.namespace = this.uriGxeHtml;
          cfgObject.name = localName;
        }
      }      
    },

    // initializes the XML target name  and namespace associated with a configuration object
    _initializeTargetNS: function(cfgObject,gxeNamespaces) {
      if (gxeNamespaces != null) {
        var namespace = this.getGxeAttributeValue(cfgObject,"targetNS");
        var name = this.getGxeAttributeValue(cfgObject,"targetName");
        if ((name != null) && (name.length > 0)) {
          var nIdx = name.indexOf(":");
          if (nIdx != -1) {
            var prefix = name.substring(0,nIdx);
            var localName = name.substring(nIdx+1);
            var uri = gxeNamespaces.getUri(prefix);
            if (uri == null) {
            } else if ((namespace != null) && (namespace != uri)) {
            } else {
              cfgObject.targetNSOverride = uri;
              cfgObject.targetNameOverride = localName;
            }
          }
        }
      }
    }

  }
  
};

/**
 * @class Provides client functionality for executing AJAX calls to the server. 
 * @name gxe.Client
 */
dojo.provide("gxe.Client");
dojo.declare("gxe.Client",null,{
  
  /**
   * Loads a JSON based editor definition through XML interrogation.
   * <br/><br/>This method is geared towards the Geoportal Server end-point
   * for loading an editor definition:<br/>[context path]/gxe/definition<br/>
   * @function 
   * @name interrogateDefinition
   * @memberOf gxe.Client#
   * @param {gxe.Context} context the editor context  
   * @param {String} sXml the XML to interrogate
   * @param {function} callback function to call once the definition has been 
   *   successfully retrieved 
   *   <br/>signature: function(responseObject,ioArgs)
   *   <br/>--- where responseObject is the JSON definition for the editor
   */
  interrogateDefinition: function(context,sXml,callback) {
    var u = context.contextPath+"/gxe/definition";
    dojo.xhrPost({
      handleAs: "json",
      headers: {"Content-Type": "text/plain; charset=UTF-8"},
      postData: sXml,
      preventCache: true,
      url: u,
      error: dojo.hitch(this,"onError"),
      load: dojo.hitch(this,function(responseObject,ioArgs) {
        callback(responseObject,ioArgs);
      })
    });
  },
  
  /**
   * Handles an error condition.
   * @function 
   * @name onError
   * @memberOf gxe.Client#
   * @param {Error} error the error 
   * @param {Object} ioArgs the Dojo i/o arguments 
   */
  onError: function(error,ioArgs) {  
    var msg = null;
    if (ioArgs == null) {
      msg = error.message;
    } else {
      if ((ioArgs.xhr.status >= 200) && (ioArgs.xhr.status < 300)) {
        msg = error.message;
      } else {
        msg = " HTTP: " +ioArgs.xhr.status+", "+ioArgs.args.url;
      }
    }
    if (msg != null) alert(msg);
  },
  
  /**
   * Loads a JSON based editor definition.
   * <br/><br/>This method is geared towards the Geoportal Server end-point
   * for loading an editor definition:<br/>[context path]/gxe/definition<br/>
   * @function 
   * @name queryDefinition
   * @memberOf gxe.Client#
   * @param {gxe.Context} context the editor context  
   * @param {String} sParam a URL parameter name (key|loc)
   * @param {String} sParamValue a URL parameter value 
   *   <br/>when sParam="key", use the key for the standard (e.g. "fgdc")
   *   <br/>when sParam="loc", use the location (e.g. "gpt/gxe/fgdc/fgdc-editor.xml")
   * @param {function} callback function to call once the definition has been 
   *   successfully retrieved 
   *   <br/>signature: function(responseObject,ioArgs)
   *   <br/>--- where responseObject is the JSON definition for the editor
   */
  queryDefinition: function(context,sParam,sParamValue,callback) {
    var u = context.contextPath+"/gxe/definition";
    u += "?"+encodeURIComponent(sParam)+"="+encodeURIComponent(sParamValue)+"&f=json";
    dojo.xhrGet({
      handleAs: "json",
      preventCache: true,
      url: u,
      error: dojo.hitch(this,"onError"),
      load: dojo.hitch(this,function(responseObject,ioArgs) {
        callback(responseObject,ioArgs);
      })
    });
  },
  
  /**
   * Loads an XML document.
   * <br/><br/>This method is geared towards the Geoportal Server rest end-point
   * for document management:<br/>[context path]/rest/manage/document<br/>
   * @function 
   * @name queryDocument
   * @memberOf gxe.Client#
   * @param {gxe.Context} context the editor context  
   * @param {String} id the document identifier 
   * @param {function} callback function to call once the document has been 
   *   successfully retrieved 
   *   <br/>signature: function(responseObject,ioArgs) 
   *   <br/>--- where responseObject is the XML DOM
   */
  queryDocument: function(context,id,callback) {
    var u = context.contextPath+"/rest/manage/document?id="+encodeURIComponent(id);
    dojo.xhrGet({
      handleAs: "xml",
      preventCache: true,
      url: u,
      error: dojo.hitch(this,"onError"),
      load: dojo.hitch(this,function(responseObject,ioArgs) {
        context.openDocumentId = id;
        callback(responseObject,ioArgs);
      })
    });
  },
  
  /**
   * Saves an XML document.
   * <br/><br/>This method is geared towards the Geoportal Server rest end-point
   * for document management:<br/>[context path]/rest/manage/document<br/>
   * @function 
   * @name saveDocument
   * @memberOf gxe.Client#
   * @param {gxe.Context} context the editor context  
   * @param {String} id the document identifier 
   *   (can be null for documents that are internally identified)
   * @param {String} sXml the XML to save 
   * @param {boolean} asDraft true if document is being saved as a draft
   * @param {function} callback optional function to call once the save has 
   *   successfully executed 
   *   <br/>signature: function(responseObject,ioArgs)
   */
  saveDocument: function(context,id,sXml,asDraft,callback) {
    var u = context.contextPath+"/rest/manage/document?publicationMethod=editor&errorsAsJson=jErr";
    if (id == null) id = context.openDocumentId;
    if (id == null) id = context.newDocumentId;
    if ((id != null) && (id.length > 0)) u += "&id="+encodeURIComponent(id);
    if (asDraft) u += "&asDraft=true";
   
    var dialog = new dijit.Dialog({
      title: context.getI18NString("client.saving.title"),
      style: "width: 300px; display: none;"
    });
    dojo.addClass(dialog.domNode,"tundra");
    dialog.show();
    
    dojo.xhrPut({
      handleAs: "text",
      preventCache: true,
      url: u,
      putData: sXml,
      error: dojo.hitch(this,function(errorObject,ioArgs) {
        dialog.hide();
        dialog.destroy();
        this.onError(errorObject,ioArgs);
      }),
      load: dojo.hitch(this,function(responseObject,ioArgs) {
        dialog.hide();
        dialog.destroy();
        try {
          if (responseObject!=null) {
            jErr = null;
            eval(responseObject);
            if (jErr!=null) {
              if (jErr.errors!=null && jErr.errors.length>0) {
                for (var m=0; m<jErr.errors.length; m++) {
                  context.messageArea.addError(jErr.errors[m]);
                }
              } else {
                context.messageArea.addError(jErr.message);
              }
              jErr = null;
            }
          }
        } catch (err) {
          // handle eval error
        }
        if (typeof(callback) == "function") callback(responseObject,ioArgs);
      })
    });
  }
  
});

/**
 * @class Provides a context for the editor. 
 * @name gxe.Context
 * @property {String} contextPath the wep-app context path
 * @property {cfgObject} cfgContext the g:context portion configured editor definition (JSON)
 * @property {cfgObject} cfgDefinition the configured editor definition (JSON)
 * @property {GptMapConfig} gptMapConfig the interactive map configuration
 * @property {Element} htmlParentElement the parent HTML element
 * @property {String} newDocumentId the ID to use for a newly created document
 * @property {String} openDocumentId the ID for the document that was opened
 * @property {gxe.xml.XmlDocument} xmlDocument the target XML document for the editor
 * @property {String} idPrefix the prefix to use when generating HTML element IDs
 * @property {gxe.control.MessageArea} messageArea the message area
 */
dojo.provide("gxe.Context");
dojo.declare("gxe.Context",null,{
  contextPath: null,
  cfgContext: null,
  cfgDefinition: null,
  gptMapConfig: null,
  htmlParentElement: null,
  newDocumentId: null,
  openDocumentId: null,
  xmlDocument: null,
  idPrefix: "gxeId",
  messageArea: null,
  _uniqueId: 0,

  /**
   * Builds the editor user interface.
   * @function 
   * @name buildUI
   * @memberOf gxe.Context#
   * @param {cfgObject} cfgDefinition the configured editor definition (JSON)
   * @param {Element} htmlParentElement the parent HTML element (the editor
   *   will be appended to this parent)
   * @param {DOM} domDocument the XML target document
   *   (can be null, used when opening an existing document)
   */
  buildUI: function(cfgDefinition,htmlParentElement,domDocument) {
    this.cfgDefinition = cfgDefinition;
    this.cfgContext = gxe.cfg.findGxeChild(cfgDefinition,"context");
    this.htmlParentElement = htmlParentElement;

    var elMessageArea = dojo.byId("gxeMessageArea");
    this.messageArea = new gxe.control.MessageArea();
    this.messageArea.context = this;
    this.messageArea.build(dojo.byId(elMessageArea),null,null);    
    
    this.xmlDocument = new gxe.xml.XmlDocument();
    var xmlRoot = this.xmlDocument.initializeRoot(this,cfgDefinition);
    var domProcessor = null;
    var domRoot = null;
    if (domDocument != null) {
      var ndRoot = domDocument.documentElement;
      var processor = new gxe.xml.DomProcessor();
      if (processor.isMatching(ndRoot,xmlRoot.cfgObject)) {
        domProcessor = processor;
        domRoot = ndRoot;
      } else {
        throw new Error("The XML root element does not match the editor definition.");
      }
    }

    var ctl = this.makeXhtmlControl(xmlRoot.cfgObject,null,true);
    ctl.xmlParentElement = null;
    ctl.xmlNode = xmlRoot;
    ctl.build(htmlParentElement,domProcessor,domRoot);
  },
  
  /**
   * Generates a unique ID.
   * <br/>(String, prefixed with this.idPrefix).
   * @function 
   * @name generateUniqueId
   * @memberOf gxe.Context#
   * @return {String} the ID
   */
  generateUniqueId: function() {
    this._uniqueId++;
    return this.idPrefix+this._uniqueId;
  },
  
  /**
   * Gets a localized message string associated with the editor context.
   * @function 
   * @name getI18NString
   * @memberOf gxe.Context#
   * @param {String} sKey the key for the message string
   * @return {String} the message string
   */
  getI18NString: function(sKey) {
    var sValue = sKey;
    if (this.cfgContext != null) {
      gxe.cfg.forEachChild(this.cfgContext,gxe.cfg.uriGxe,"i18n",dojo.hitch(this,function(cfgChild) {
        var sk = gxe.cfg.getGxeAttributeValue(cfgChild,"key");
        if (sk == sKey) {
          sValue = cfgChild.value;
          // return "break"; // quicker but disable to allow profile override
        }
      }));
    }
    return sValue;
  },

  /**
   * Makes a GXE HTML based user interface control.
   * <br/>By default, a new gxe.control.Control object will be instantiated. If the supplied
   * configuration object has a configured g:jsClass attribute, the attribute value will be used 
   * to instantiatethe control object (it is assumed that any supplied g:jsClass will extend from
   * gxe.control.control).
   * @function 
   * @name makeXhtmlControl
   * @memberOf gxe.Context#
   * @param {cfgObject} cfgObject the associated editor configuration object
   * @param {gxe.control.Control} ctlParent the parent control
   * @param {boolean} bInitialize if true then run new control's initialize function
   * @return {String} the new control
   */
  makeXhtmlControl: function(cfgObject,ctlParent,bInitialize) {
    var ctl = null;
    var sJsClass = gxe.cfg.getGxeAttributeValue(cfgObject,"jsClass");
    if ((sJsClass != null) && (sJsClass.length > 0)) {
      ctl = eval("new "+sJsClass+"()");
    } else {
      ctl = new gxe.control.Control();
    }
    ctl.parentControl = ctlParent;
    if (bInitialize) {
      ctl.initialize(this,cfgObject);
    }
    return ctl;
  }
  
});


/* Utility classes =================================================== */

/**
 * @class Simulates some methods associated with an ArrayList data structure.
 * @name gxe.util.ArrayList
 * @property {Array} _array The underlying JavaScript array.
 */
dojo.provide("gxe.util.ArrayList");
dojo.declare("gxe.util.ArrayList",null,{
  _array: null,

  /** @constructor */
  constructor: function() {
    this._array = new Array();
  },

  /**
   * Appends an object to the collection (same as push()).
   * @function 
   * @name add
   * @memberOf gxe.util.ArrayList#
   * @param {Object} obj the object to add
   */
  add: function(obj) {
    this._array.push(obj);
  },

  /**
   * Gets the item at the specified index.
   * @function 
   * @name getItem
   * @memberOf gxe.util.ArrayList#
   * @param {Integer} nIndex the index
   * @returns {Object} the corresponding object
   */
  getItem: function(nIndex) {
    return this._array[nIndex];
  },

  /**
   * Gets the length of the array.
   * @function 
   * @name getLength
   * @memberOf gxe.util.ArrayList#
   * @returns {Integer} the length
   */
  getLength: function() {
    return this._array.length;
  },

  /**
   * Inserts an object at a specified index.
   * @function 
   * @name insertAt
   * @memberOf gxe.util.ArrayList#
   * @param {Integer} nIndex the index (same as JavaScript Array.splice)
   * @param {Object} obj the object to insert
   */
  insertAt: function(nIndex,obj) {
    this._array.splice(nIndex,0,obj);
  },

  /**
   * Appends an object to the collection.
   * @function 
   * @name push
   * @memberOf gxe.util.ArrayList#
   * @param {Object} obj the object to add
   */
  push: function(obj) {
    this._array.push(obj);
  },

  /**
   * Removes the object at the specified index from the collection.
   * @function 
   * @name removeIndex
   * @memberOf gxe.util.ArrayList#
   * @param {Integer} nIndex the index of the object to remove
   */
  removeIndex: function(nIndex) {
    this._array.splice(nIndex,1);
  },

  /**
   * Swaps the positions of two objects within the collection.
   * @function 
   * @name swapPosition
   * @memberOf gxe.util.ArrayList#
   * @param {Integer} nFromIndex the from index
   * @param {Integer} nToIndex the to index
   */
  swapPosition: function(nFromIndex,nToIndex) {
    var a = this._array[nFromIndex];
    var b = this._array[nToIndex];
    this._array[nFromIndex] = b;
    this._array[nToIndex] = a;
  }

});

/**
 * @class Simulates some methods associated with an StringBuffer data structure.
 * @name gxe.util.StringBuffer
 * @property {String} _text The underlying JavaScript String.
 */
dojo.provide("gxe.util.StringBuffer");
dojo.declare("gxe.util.StringBuffer",null,{
  _text: "",

  /**
   * Constructor.
   * @function
   * @name constructor
   * @constructor
   * @memberOf gxe.util.StringBuffer#
   * @param {String} text the initial text
   * @returns {gxe.util.StringBuffer} the new instance
   */
  constructor: function(text) {
    if (text != null) this._text = text;
  },

  /**
   * Appends a string.
   * @function 
   * @name append
   * @memberOf gxe.util.StringBuffer#
   * @param {String} s the string to append
   * @returns {gxe.util.StringBuffer} this instance
   */
  append: function(s) {
    this._text += s;
    return this;
  },

  /**
   * Returns the associated string.
   * @function 
   * @name toString
   * @memberOf gxe.util.StringBuffer#
   * @returns {String} this string
   */
  toString: function() {
    return this._text;
  }
});

/**
 * @class Represents an HTML based attribute.
 * @name gxe.html.HtmlAttribute
 * @property {String} name The attribute name.
 * @property {Object} value The attribute value.
 */
dojo.provide("gxe.html.HtmlAttribute");
dojo.declare("gxe.html.HtmlAttribute",null,{
  name: null,
  value: null
});

/**
 * @class A collection of HTML based attributes.
 * @name gxe.html.HtmlAttributes
 * @extends gxe.util.ArrayList
 */
dojo.provide("gxe.html.HtmlAttributes");
dojo.declare("gxe.html.HtmlAttributes",gxe.util.ArrayList,{

  /**
   * Applies the attribute collection to an HTML DOM Element.
   * (i.e. sets all attribute values)
   * @function 
   * @name apply
   * @memberOf gxe.html.HtmlAttributes#
   * @param {Element} elHtml the corresponding HTML DOM Element
   */
  apply: function(elHtml) {
    var n = this.getLength();
    for (var i=0; i<n; i++) {
      var attr = this.getItem(i);
      if (attr != null) {
        if ((attr.name != null) && (attr.value != null)) {
          var value = attr.value;
          if (typeof(value) =="string") {
            if (value.indexOf("$fire.") == 0) value = null;
          }
          if (value != null) {
            var s = attr.name.toLowerCase();
            if (s != "tag") {
              elHtml.setAttribute(attr.name,value);
              if (dojo.isIE <= 8) {
                if (attr.name == "class") {
                  elHtml.className = value; 
                  //
                } else if (attr.name = "readonly")  {
                  //elHtml.readOnly = true;
                }
              }
            }
          }  
        } 
      }
    }
  },

  /**
   * Finds an attribute with given name.
   * @function 
   * @name find
   * @memberOf gxe.html.HtmlAttributes#
   * @param {String} name the name of the attribute to find
   * @returns {gxe.html.HtmlAttribute} the corresponding attribute (null if not found)
   */
  find: function(name) {
    if (name != null) {
      name = dojo.trim(name);
      if (name.length > 0) {
        var lc = name.toLowerCase();
        var n = this.getLength();
        for (var i=0; i<n; i++) {
          var attr = this.getItem(i);
          if (attr != null) {
            if ((attr.name != null) && (attr.name.toLowerCase() == lc)) {
              return attr;
            } 
          }
        }
      }
    }
    return null;
  }, 

  /**
   * Adds an HTML attribute to the collection.
   * If an attribute with the supplied name previously exists, its value will be updated.
   * @function 
   * @name set
   * @memberOf gxe.html.HtmlAttributes#
   * @param {String} name the name of the attribute
   * @param {Object} value the value of the attribute
   */
  set: function(name,value) {
    if (name != null) {
      name = dojo.trim(name);
      if (name.length > 0) {
        var attr = this.find(name);
        if (attr != null) {
          attr.value = value;
        } else {
          attr = new gxe.html.HtmlAttribute();
          attr.name = name;
          attr.value = value;
          this.add(attr);
        }
      }
    }
  } 
});


/* XML related classes =============================================== */

/**
 * @class Provides utility functions for processing an XML document.
 * <br/>(used when opening an existing document)
 * @name gxe.xml.DomProcessor
 */
dojo.provide("gxe.xml.DomProcessor");
dojo.declare("gxe.xml.DomProcessor",null,{
  
  // some new methods have been added that can be fully leveraged at a later date
  // forEachElementNode, forEachMatchingElementNode, splitQualifiedName

  // DOM node types
  nodeTypes: {
    ELEMENT_NODE: 1,
    ATTRIBUTE_NODE: 2,
    TEXT_NODE: 3,
    CDATA_SECTION_NODE: 4,
    ENTITY_REFERENCE_NODE: 5,
    ENTITY_NODE: 6,
    PROCESSING_INSTRUCTION_NODE: 7,
    COMMENT_NODE: 8,
    DOCUMENT_NODE: 9,
    DOCUMENT_TYPE_NODE: 10,
    DOCUMENT_FRAGMENT_NODE: 11,
    NOTATION_NODE: 12
  },
  
  // this function is not in use, development only
  buildUI: function(context,cfgDefinition,htmlParentElement,sXml) {
    if (window.DOMParser) {
      var parser = new DOMParser();
      var dom = parser.parseFromString(sXml,"text/xml");
      context.buildUI(cfgDefinition,htmlParentElement,dom);
    } else if (window.ActiveXObject) {
      var dom = new ActiveXObject("MSXML2.DOMDocument");
      dom.async = "false";
      dom.loadXML(sXml);
      context.buildUI(cfgDefinition,htmlParentElement,dom);
    }
  },
  
  /**
   * Attempts to find the attribute of a DOM Node that matches the XML target 
   * associated with an editor configuration object.
   * @see gxe.xml.DomProcessor#isMatching
   * @function 
   * @name findMatchingChildAttribute
   * @memberOf gxe.xml.DomProcessor#
   * @param {Node} domParentNode the DOM node whose attributes will be searched
   * @param {Object} cfgChild the editor configuration object that 
   *   will be used to determine a match
   * @returns {Node} a matching DOM attribute (null if no match)
   */
  findMatchingChildAttribute: function(domParentNode,cfgChild) {
    var attributes = domParentNode.attributes;
    if ((attributes != null) && (attributes.length > 0)) {
      var n = attributes.length;
      for (var i=0; i<n; i++) {
        var attribute = attributes.item(i);
        if (this.isMatching(attribute,cfgChild)) return attribute;
      }
    }
    return null;
  },

  /**
   * Attempts to find an immediate child element of a DOM Node that matches 
   * the XML target associated with an editor configuration object.
   * @see gxe.xml.DomProcessor#isMatching
   * @function 
   * @name findMatchingChildElement
   * @memberOf gxe.xml.DomProcessor#
   * @param {Node} domParentNode the DOM node whose children will be searched
   * @param {Object} cfgChild the editor configuration object that 
   *   will be used to determine a match
   * @returns {Node} a matching DOM element (null if no match)
   */
  findMatchingChildElement: function(domParentNode,cfgChild) {
    var children = domParentNode.childNodes;
    if ((children != null) && (children.length > 0)) {
      var n = children.length;
      for (var i=0; i<n; i++) {
        var child = children[i];
        if (child.nodeType == this.nodeTypes.ELEMENT_NODE) {
          if (this.isMatching(child,cfgChild)) return child;
        }
      }
    }
    return null;
  },

  /**
   * Attempts to find the immediate child elements of a DOM Node that match 
   * the XML target associated with an editor configuration object.
   * @see gxe.xml.DomProcessor#isMatching
   * @function 
   * @name findMatchingChildElements
   * @memberOf gxe.xml.DomProcessor#
   * @param {Node} domParentNode the DOM node whose children will be searched
   * @param {Object} cfgChild the editor configuration object that 
   *   will be used to determine a match
   * @returns {Node[]} the matching DOM elements (null if no match)
   */
  findMatchingChildElements: function(domParentNode,cfgChild) {
    var matches = null;
    var children = domParentNode.childNodes;
    if ((children != null) && (children.length > 0)) {
      var n = children.length;
      for (var i=0; i<n; i++) {
        var child = children[i];
        if (child.nodeType == this.nodeTypes.ELEMENT_NODE) {
          if (this.isMatching(child,cfgChild)) {
            if (matches == null) matches = new Array();
            matches.push(child);
          }
        }
      }
    }
    return matches;
  },
  
  /**
   * Executes a function for each immediate child element of a DOM Node.
   * <br/>Only child elements of nodeType=1 (ELEMENT_NODE) will be considered.
   * <br/>
   * <br/>The callback function will have the following signature: function(domChildNode) {}
   * <br/>The callback function can return the String "break" to terminate the loop.
   * @function 
   * @name forEachElementNode
   * @memberOf gxe.xml.DomProcessor#
   * @param {Node} domParentNode the DOM node whose children will be searched
   * @param {function} callback the callback function 
   */
  forEachElementNode: function(domParentNode,callback) {
    var children = domParentNode.childNodes;
    if ((children != null) && (children.length > 0)) {
      var n = children.length;
      for (var i=0; i<n; i++) {
        var child = children[i];
        if (child.nodeType == this.nodeTypes.ELEMENT_NODE) {
          var _ret = callback(child);
          if ((typeof(_ret) == "string") && (_ret == "break")) break;
        }
      }
    }
  },
  
  
  /**
   * Executes a function for each immediate child element of a DOM Node that matches
   * a supplied namespace and loca name.
   * <br/>Only child elements of nodeType=1 (ELEMENT_NODE) will be considered.
   * <br/>
   * <br/>The callback function will have the following signature: function(domChildNode) {}
   * <br/>The callback function can return the String "break" to terminate the loop.
   * @function 
   * @name forEachMatchingElementNode
   * @memberOf gxe.xml.DomProcessor#
   * @param {Node} domParentNode the DOM node whose children will be searched
   * @param {String} sNamespaceUri the namespace URI to match (can be null)
   * @param {String} sLocalName the local node name to match (i.e unqualified name)
   * @param {function} callback the callback function 
   */
  forEachMatchingElementNode: function(domParentNode,sNamespaceUri,sLocalName,callback) {  
    var targetNS = sNamespaceUri;
    if ((targetNS != null) && (targetNS.length == 0)) targetNS = null;
    this.forEachElementNode(domParentNode,dojo.hitch(this,function(domChildNode) {
      if ((domChildNode.namespaceURI == targetNS) || (targetNS == "*")) {
        var pfxPlusLocal = this.splitQualifiedName(domChildNode.nodeName);
        if ((pfxPlusLocal.localName == sLocalName) || (sLocalName == "*")) {
          var _ret = callback(domChildNode);
          if ((typeof(_ret) == "string") && (_ret == "break")) return "break";
        }
      }
    }));
  },

  /**
   * Gets the text content of a DOM Node (element or attribute).
   * @function 
   * @name getNodeText
   * @memberOf gxe.xml.DomProcessor#
   * @param {Node} domNode the DOM node that is actively being processed 
   * @returns {String} the text content (can be null)
   */
  getNodeText: function(domNode) {
    var s;
    if (domNode.nodeType == this.nodeTypes.ELEMENT_NODE) {
      var children = domNode.childNodes;
      if ((children != null) && (children.length > 0)) {
        var n = children.length;
        for (var i=0; i<n; i++) {
          var child = children[i];
          if (child.nodeType == this.nodeTypes.TEXT_NODE) {
            s = child.nodeValue;
            if (s != null) s = dojo.trim(s);
            return s;
          }
        }
      }
      return "";
    } else {
      s = domNode.nodeValue;
      if (s != null) s = dojo.trim(s);
      return s;
    }  
    return null;
  },
  
  /**
   * Determines if a DOM Node has either: attributes, child elements or element text.
   * @function 
   * @name hasChildrenOrAttributes
   * @memberOf gxe.xml.DomProcessor#
   * @param {Node} domNode the DOM node that is actively being processed 
   * @returns {boolean} true if there is a match
   */
  hasChildrenOrAttributes: function(domNode) {
    if (domNode != null) {
      var attributes = domNode.attributes;
      if ((attributes != null) && (attributes.length > 0)) return true;
      var children = domNode.childNodes;
      if ((children != null) && (children.length > 0)) {
        var n = children.length;
        for (var i=0; i<n; i++) {
          if (children[i].nodeType == this.nodeTypes.ELEMENT_NODE) {
            return true;
          } else if (children[i].nodeType == this.nodeTypes.TEXT_NODE) {
            var s = children[i].nodeValue;
            if ((s != null) && (dojo.trim(s).length > 0)) return true;
          }
        }
      }
    }
    return false;
  },
  
  /**
   * Determines if the qualified name associated with a DOM Node matches the XML target 
   * associated with an editor configuration object.
   * <br/>Editor configuration example for referencing a target XML element:<br/>
   * &lt;g:element g:targetName="pfx:name" ..
   * @function 
   * @name isMatching
   * @memberOf gxe.xml.DomProcessor#
   * @param {Node} domNode the DOM node that is actively being processed 
   * @param {Object} cfgObject the associated editor configuration object
   * @returns {boolean} true if there is a match
   */
  isMatching: function(domNode,cfgObject) {
    var targetNS = gxe.cfg.getTargetNS(cfgObject);
    if ((targetNS != null) && (targetNS.length == 0)) targetNS = null;
    
    if (dojo.isIE <= 8) {
      if (targetNS == null) targetNS = "";
    } 
    
    if (domNode.namespaceURI == targetNS) {
      var targetName = gxe.cfg.getTargetName(cfgObject);
      var nodeName = domNode.nodeName;
      var prefix = null;
      var localName = nodeName;
      var nIdx = nodeName.indexOf(":");
      if (nIdx != -1) {
        prefix = nodeName.substring(0,nIdx);
        localName = nodeName.substring(nIdx+1);
      }
      if (localName == targetName) return true;
    }
    return false;
  },
  
  /**
   * Determines if there is a match between a supplied DOM Node and a descendant condition.
   * <br/><br/>Note: matchTopElement does not support full XPath expressions
   * @function 
   * @name matchTopElement
   * @memberOf gxe.xml.DomProcessor#
   * @param {gxe.xml.XmlNamespaces} xmlNamespaces a configured list of target namespaces
   * @param {Node} domNode the DOM node that is actively being processed 
   * @param {String} sMatchPath the relative path for the element to match
   *   (a simple path relative to the supplied parent DOM node)
   * @param {String} sMatchTextNodeValue the text node value to match 
   *   (null indicates no test should be made) 
   * @param {boolean} bMust true indicates that there must be a match 
   *   (false indicates must not)
   * @returns {boolean} true if there is a match
   */
  
  matchTopElement: function(xmlNamespaces,domNode,sMatchPath,sMatchTextNodeValue,bMust) {
    var tokens = sMatchPath.split("/");
    var nTokens = tokens.length;
    var domFinalMatches = new Array();
    var domCurrentNodes = new Array();
    domCurrentNodes.push(domNode);
    
    for (var i=0; i<nTokens; i++) {
      var bIsLast = (i == (nTokens - 1));
      var uri = null;
      var pfxPlusLocal = this.splitQualifiedName(tokens[i]);
      var localName = pfxPlusLocal.localName;
      if (pfxPlusLocal.prefix != null) {
        uri = xmlNamespaces.getUri(pfxPlusLocal.prefix);
      }

      var domCurrentMatches = new Array();
      for (var j=0; j<domCurrentNodes.length; j++) {
        this.forEachMatchingElementNode(domCurrentNodes[j],uri,localName,
          dojo.hitch(this,function(domChildNode) {
            if (bIsLast) {
              if (sMatchTextNodeValue == null) {
                domCurrentMatches.push(domChildNode);
              } else {
                var s = this.getNodeText(domChildNode);
                if (s == sMatchTextNodeValue) {
                  domCurrentMatches.push(domChildNode);
                }
              }
            } else {
              domCurrentMatches.push(domChildNode);
            }
          }
        ));
      }
      domCurrentNodes = domCurrentMatches;
      if (domCurrentNodes.length == 0) break;
      if (bIsLast) domFinalMatches = domCurrentNodes;
    }
    
    if (bMust) return (domFinalMatches.length > 0);
    else return (domFinalMatches.length == 0);
  },
  
  /**
   * Splits a qualified name into a prefix plus localName pair. 
   * @function 
   * @name splitQualifiedName
   * @memberOf gxe.xml.DomProcessor#
   * @param {String} sQualifiedName the qualified name
   * @returns {"prefix":{String}, "localName":{String}} the prefix plus localName pair
   */
  splitQualifiedName: function(sQualifiedName) {
    var prefixPlusLocalName = {"prefix": null, "localName": sQualifiedName};
    var tokens = sQualifiedName.split(":");
    if (tokens.length == 2) {
      prefixPlusLocalName.prefix = tokens[0];
      prefixPlusLocalName.localName = tokens[1];
    }
    return prefixPlusLocalName;
  }
  
});

/**
 * @class Serializes an XML document.
 * @name gxe.xml.Generator
 * @property {gxe.Context} context the editor context 
 * @property {String} documentTitle the document title
 * @property {boolean} hadValidationErrors true if validation errors were encountered
 * @property {boolean} isValidating true if all content should be validated
 * @property {boolean} isValidatingTitleOnly true if only the title should be validated
 * @property {boolean} isSaveAsDraft true if serialization is for a draft document
 */
dojo.provide("gxe.xml.Generator");
dojo.declare("gxe.xml.Generator",null,{
  context: null,
  documentTitle: null,
  hadValidationErrors: false,
  isValidating: false,
  isValidatingTitleOnly: false,
  isSaveAsDraft: false,
  
  /**
   * Executes the text content of an XML attribute. 
   * @function 
   * @name escAttribute
   * @memberOf gxe.xml.Generator#
   * @param {String} s the string to escape
   * @returns {String} the escaped string
   */
  escAttribute: function(s) {
    return this._execEscape(s,true);
  },
  
  /**
   * Executes the text content of an XML element. 
   * @function 
   * @name escElement
   * @memberOf gxe.xml.Generator#
   * @param {String} s the string to escape
   * @returns {String} the escaped string
   */
  escElement: function(s) {
    return this._execEscape(s,true);
  },
  
  /**
   * Executes and XML escape against a string. 
   * @function 
   * @name _execEscape
   * @memberOf gxe.xml.Generator#
   * @param {String} s the string to escape
   * @param {boolean} bEscapeApostrophe true if apostrophies should be escaped 
   * @returns {String} the escaped string
   */
  _execEscape: function(s,bEscapeApostrophe) {
    if (s == null) {
      return null;
    } else if (s.length == 0) {
      return s;
    } else {
      var sApos = "&apos;";
      if (!bEscapeApostrophe) sApos = "'";
      var sb = "";
      for (var i=0; i<s.length; i++) {
        var c = s.charAt(i);
        if      (c == "&")  sb += "&amp;";
        else if (c == "<")  sb += "&lt;";
        else if (c == '>')  sb += "&gt;";
        else if (c == '\'') sb += sApos;
        else if (c == '"')  sb += "&quot;";
        else                sb += c;
      }
      return sb;
    }
  },

  /**
   * Serializes an XML document. 
   * @function 
   * @name generate
   * @memberOf gxe.xml.Generator#
   * @param {gxe.Context} context the editor context
   * @param {boolean} asDraft true if the serialization is for a draft document 
   *   (minimal validation)
   * @returns {String} the serialized string
   */
  generate: function(context,asDraft) {
    this.context = context;
    this.documentTitle = null;
    this.isValidating = true;
    this.isValidatingTitleOnly = false;
    this.isSaveAsDraft = asDraft;
    this.context.messageArea.clearAll();
    var stringBuffer = new gxe.util.StringBuffer();
    context.xmlDocument.rootElement.echo(this,stringBuffer,0);
      
    var sXml = stringBuffer.toString();
    sXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+sXml; 
    return sXml;
  },

  /**
   * Handles a validation error. 
   * @function 
   * @name handleValidationError
   * @memberOf gxe.xml.Generator#
   * @param {sMessage} sMessage the message
   * @param {gxe.xml.XmlNode} xmlNode the target XML node that threw the error
   * @returns {gxe.control.InputBase} inputControl the input control for the target node
   */
  handleValidationError: function(sMessage,xmlNode,inputControl) {
    var bHadErrors = false;
    if (!this.isSaveAsDraft) {
      this.hadValidationErrors = true;
      this.context.messageArea.addValidationError(sMessage,xmlNode,inputControl);
    } else {
      var bIsTitle = xmlNode.nodeInfo.isDocumentTitle;
      if (bIsTitle) {
        this.hadValidationErrors = true;
        this.context.messageArea.addValidationError(sMessage,xmlNode,inputControl);
      }
    }
    if (!bHadErrors && this.hadValidationErrors) {
      dojo.addClass(this.context.htmlParentElement,"gxeRepairMode");
    }
  }

});

/**
 * @class A target XML document.
 * @name gxe.xml.XmlDocument
 * @property {gxe.Context} context the editor context 
 * @property {gxe.xml.XmlNamespaces} namespaces the namespaces associated with the document
 * @property {gxe.xml.XmlElement} rootElement the root element for the document
 */
dojo.provide("gxe.xml.XmlDocument");
dojo.declare("gxe.xml.XmlDocument",null,{
  context: null,
  namespaces: null,
  rootElement: null,

  /** constructor */
  constructor: function() {
    this.namespaces = new gxe.xml.XmlNamespaces();
  },

  /**
   * Initializes the root element and XML namespaces. 
   * @function 
   * @name initializeRoot
   * @memberOf gxe.xml.XmlDocument#
   * @param {gxe.Context} context the editor context 
   * @param {Object} cfgDefinition the configured editor definition (JSON)
   * @returns {gxe.xml.XmlElement} the root element for the document
   */
  initializeRoot: function(context,cfgDefinition) {
    this.context = context;
    this.namespaces = new gxe.xml.XmlNamespaces();
    
    var cfgRoot = gxe.cfg.findGxeChild(cfgDefinition,"rootElement");
    if (cfgRoot == null) {
      throw new Error("The editor definition contains no g:rootElement.");
    }
    
    var cfgItems = gxe.cfg.findChild(cfgRoot,gxe.cfg.uriGxe,"namespaces");
    if (cfgItems != null) {
      gxe.cfg.forEachChild(cfgItems,gxe.cfg.uriGxe,"namespace",dojo.hitch(this,function(cfgNS) {
        var pfx = gxe.cfg.getGxeAttributeValue(cfgNS,"prefix");
        var uri = gxe.cfg.getGxeAttributeValue(cfgNS,"uri");
        this.namespaces.add(new gxe.xml.XmlNamespace(pfx,uri));
      }));
    }

    this.rootElement = new gxe.xml.XmlElement(this,null,cfgRoot);

    // assume all namespace prefixes are specified on the root node
    var n = this.namespaces.getLength();
    for (var i=0; i<n; i++) {
      var ns = this.namespaces.getItem(i);
      if ((ns != null) && (ns.uri != null) && (ns.uri.length > 0)) {
        var ni = new gxe.xml.XmlNodeInfo();
        if ((ns.prefix != null) && (ns.prefix.length > 0)) {
          ni.namespacePrefix ="xmlns";
          ni.localName = ns.prefix;
          ni.nodeValue = ns.uri;
        } else {
          ni.localName ="xmlns";
          ni.nodeValue = ns.uri;
        }
        var attr = new gxe.xml.XmlAttribute(this,this.rootElement,null);
        attr.nodeInfo = ni;
        this.rootElement.attributes.add(attr);
      }                           
    }
    
    return this.rootElement;
  }
  
});

/**
 * @class A target XML node (element or attribute).
 * @name gxe.xml.XmlNode
 * @property {Object} cfgObject the associated editor configuration object
 * @property {boolean} isOptionalPlaceHolder true if this node is an optional place holder only 
 *   (no serialization)
 * @property {boolean} isPlaceHolder true if this node is a place holder only 
 *   (no serialization)
 * @property {gxe.xml.XmlNodeInfo} nodeInfo the node information
 * @property {gxe.xml.XmlDocument} parentDocument the parent XML document
 * @property {Element} parentElement the parent HTML element
 */
dojo.provide("gxe.xml.XmlNode");
dojo.declare("gxe.xml.XmlNode",null,{
  _dataExclusiveRadioButton: null,
  _dataInputControl: null,
  cfgObject: null,
  isOptionalPlaceHolder: false,
  isPlaceHolder: false,
  nodeInfo: null,
  parentDocument: null,
  parentElement: null,
  
  /**
   * Constructor. 
   * @function 
   * @name constructor
   * @memberOf gxe.xml.XmlNode#
   * @param {gxe.xml.XmlDocument} parentDocument the parent XML document
   * @param {Element} parentElement the parent HTML element
   * @param {Object} cfgObject the associated editor configuration object
   */
  constructor: function(parentDocument,parentElement,cfgObject) {
    this.parentDocument = parentDocument;
    this.parentElement = parentElement;
    this.cfgObject = cfgObject;
    this.intitalizeNodeInfo();
  },

  /**
   * Serializes XML content. 
   * @function 
   * @name echo
   * @memberOf gxe.xml.XmlNode#
   * @param {gxe.xml.Generator} xmlGenerator the XML generator
   * @param {gxe.util.StringBuffer} stringBuffer the buffer to which content will be written
   * @param {Integer} nDepth the indentation depth
   */
  echo: function(xmlGenerator,stringBuffer,nDepth) {},
  
  /**
   * Formats a validation message.
   * @function 
   * @name formatValidationMessage
   * @memberOf gxe.xml.XmlNode#
   * @param {gxe.control.InputBase} inputControl the associated input control
   * @param {String} i18nContextKey the context key associated with the localized message string
   * @returns {String} the message
   */
  formatValidationMessage: function(inputControl,i18nContextKey) {
    var l = inputControl.findParentLabelText(this);
    var f = this.parentDocument.context.getI18NString("validate.format");
    var m = this.parentDocument.context.getI18NString(i18nContextKey);
    var s = l+" "+m;
    if (f != null) {
      if ((f.indexOf("{label}") != -1) && (f.indexOf("{message}") != -1)) {
        s = f.replace("{label}",l).replace("{message}",m);
      }
    }
    
    /*
    if ((i18nContextKey != "validate.ok") && (inputControl.htmlElement != null)) {
      var sTip = inputControl.htmlElement.title;
      if ((typeof(sTip) != "undefined") && (sTip != null)) {
        sTip = dojo.trim(sTip);
        if (sTip.length > 0) s += " "+sTip;
      }
    }
    */
    
    return s;
  },

  /**
   * Indicates if this node should be serialized even if its content is empty.
   * <br/>Based upon cfg attribute g:serializeIfEmpty.
   * @function 
   * @name getSerializeIfEmpty
   * @memberOf gxe.xml.XmlNode#
   * @returns {boolean} true if this node should be serialized when empty
   */
  getSerializeIfEmpty: function() {
    if (this.cfgObject == null) return false;
    var s = gxe.cfg.getGxeAttributeValue(this.cfgObject,"serializeIfEmpty");
    return (s == "true");
  },


  /** this pair no longer in use */
  getExclusiveRadioButton: function() {return this._dataExclusiveRadioButton;},
  setExclusiveRadioButton: function(ctl) {this._dataExclusiveRadioButton = ctl;},

  /**
   * Gets the input control associated with this node.
   * @function 
   * @name getInputControl
   * @memberOf gxe.xml.XmlNode#
   * @returns {gxe.control.InputBase} the input control (can be null)
   */
  getInputControl: function() {return this._dataInputControl;},
  
  /**
   * Sets the input control associated with this node.
   * @function 
   * @name setInputControl
   * @memberOf gxe.xml.XmlNode#
   * @param {gxe.control.InputBase} ctl the input control
   */
  setInputControl: function(ctl) {this._dataInputControl = ctl;},

  /**
   * Gets the default label text for the node.
   * @function 
   * @name getLabelText
   * @memberOf gxe.xml.XmlNode#
   * @returns {String} the label text
   */
  getLabelText: function() {
    return gxe.cfg.getLabelText(this.cfgObject);
  },

  /**
   * Gets the qualified URI for the node.
   * <br/>Format: namespaceURI#localName
   * @function 
   * @name getQualifiedUri
   * @memberOf gxe.xml.XmlNode#
   * @returns {String} the qualified URI
   */
  getQualifiedUri: function() {
    return this.nodeInfo.namespaceURI+"#"+this.nodeInfo.localName;
  },

  /**
   * Initializes the node info.
   * @function 
   * @name intitalizeNodeInfo
   * @memberOf gxe.xml.XmlNode#
   */
  intitalizeNodeInfo: function() {
    this.nodeInfo = new gxe.xml.XmlNodeInfo();
    if (this.cfgObject == null) return;
    if (this.parentDocument == null) return;
    
    var ni = this.nodeInfo;
    ni.namespaceURI = gxe.cfg.getTargetNS(this.cfgObject);
    ni.namespacePrefix = this.parentDocument.namespaces.getPrefix(ni.namespaceURI);
    ni.localName = gxe.cfg.getTargetName(this.cfgObject);

    var sIsTitle = gxe.cfg.getGxeAttributeValue(this.cfgObject,"isDocumentTitle");
    if (sIsTitle == "true") ni.isDocumentTitle = true;
    var isIsoCLV = gxe.cfg.getGxeAttributeValue(this.cfgObject,"isIsoCLV");
    if (isIsoCLV == "true") ni.isIsoCodeListValue = true;
    var isIsoWMVL = gxe.cfg.getGxeAttributeValue(this.cfgObject,"isIsoWMVL");
    if (isIsoWMVL == "true") ni.isIsoWrappedMultiValueList = true;
  },

  /**
   * Determines if the node represents an XML attribute.
   * @function 
   * @name isAttribute
   * @memberOf gxe.xml.XmlNode#
   * @returns {boolean} true if the node is an attribute
   */
  isAttribute: function() {return false;},
  
  /**
   * Determines if the node is repeatable.
   * @function 
   * @name isRepeatable
   * @memberOf gxe.xml.XmlNode#
   * @returns {boolean} true if the node is repeatable
   */
  isRepeatable: function() {return false;},
  
  /**
   * Resolves the minimum number of occurrences for the node.
   * @function 
   * @name resolveMinOccurs
   * @memberOf gxe.xml.XmlNode#
   * @returns {Integer} the minimum number of occurrences
   */
  resolveMinOccurs: function() {
    var nMinOccurs = null;
    var sMinOccurs = gxe.cfg.getGxeAttributeValue(this.cfgObject,"minOccurs");
    if (sMinOccurs != null) {
      var pe = this.parentElement;
      if ((sMinOccurs == "$parent") && (pe != null)) {
        sMinOccurs = gxe.cfg.getGxeAttributeValue(pe.cfgObject,"minOccurs");
        if (sMinOccurs == "$parent") {
          pe = pe.parentElement;
          if (pe != null) {
            sMinOccurs = gxe.cfg.getGxeAttributeValue(pe.cfgObject,"minOccurs");
          }
        }
      }
      var n = parseInt(sMinOccurs);
      if (!isNaN(n)) {
        nMinOccurs = n;
        if (nMinOccurs < 0) nMinOccurs = 0;
        if ((nMinOccurs > 1) && this.isAttribute()) nMinOccurs = 1;
      }
    }
    if (nMinOccurs == null) {
      var sUse = gxe.cfg.getGxeAttributeValue(this.cfgObject,"use");
      if (sUse == "optional") nMinOccurs = 0;
      else if (sUse == "required") nMinOccurs = 1;
    }
    if (nMinOccurs == null) {
      if (this.isAttribute()) nMinOccurs = 0;
      else nMinOccurs = 1;
    }
    return nMinOccurs;
  },

  /**
   * Validates the content of an input control.
   * @function 
   * @name validateInput
   * @memberOf gxe.xml.XmlNode#
   * @param {gxe.control.InputBase} inputControl the input control
   * @param {boolean} bInFeedbackMode true if the request is part of validation feedback
   * @returns {"isValid":{boolean}, "message":{String}} the validation status
   */
  validateInput: function(inputControl,bInFeedbackMode) {
    if (inputControl.getSupportsMultipleValues()) {
      return this.validateInputValues(inputControl,inputControl.getInputValues(bInFeedbackMode));
    } else {
      return this.validateInputValue(inputControl,inputControl.getInputValue(bInFeedbackMode));
    }
  },

  /**
   * Validates an input value associated with a control.
   * @function 
   * @name validateInputValue
   * @memberOf gxe.xml.XmlNode#
   * @param {gxe.control.InputBase} inputControl the input control that generated the value
   * @param {String} value the input value
   * @returns {"isValid":{boolean}, "message":{String}} the validation status
   */
  validateInputValue: function(inputControl,value) {
    var regexp;
    var status = {"isValid": true, "message": "?ok"};
    
    var sLabel = inputControl.findParentLabelText(this);
    status.message = this.formatValidationMessage(inputControl,"validate.ok");

    // check for empty input
    if ((value == null) || (dojo.trim(value).length == 0)) {
      var nMinOccurs = this.resolveMinOccurs();   
      if ((nMinOccurs >= 1) && !this.getSerializeIfEmpty()) {
        
        var xln = this.nodeInfo.localName;
        if ((xln == "beginPosition") || (xln == "endPosition")) {
          if ((typeof(this.attributes) != "undefined") && (this.attributes != null)) {
            var nAttr = this.attributes.getLength();
            for (var iAttr=0; iAttr<nAttr; iAttr++) {
              var xAttr = this.attributes.getItem(iAttr);
              if (xAttr.nodeInfo.localName == "indeterminatePosition") {
                if (xAttr.isPlaceHolder || xAttr.isOptionalPlaceHolder) break;
                if (xAttr.getInputControl() != null) {
                  var xAttrVal = xAttr.getInputControl().getInputValue();
                  if ((typeof(xAttrVal) != "undefined") && (xAttrVal != null)) {
                    if ((xAttrVal == "unknown") || (xAttrVal == "now")) {
                      return status;
                    }
                  }
                }
                break;
              }
            }
          }
        }
        
        status.isValid = false;
        status.message = this.formatValidationMessage(inputControl,"validate.empty");
        return status;
      } else {
        return status;
      }
    }
    
    // check for acceptable alternate values
    if (!status.isValid) return status;
    var sAlternates = gxe.cfg.getGxeAttributeValue(this.cfgObject,"alternateValues");
    if (sAlternates != null) {
      var aAlternates = sAlternates.split(",");
      for (var i=0;i<aAlternates.length;i++) {
        var sAlternate = dojo.trim(aAlternates[i]);
        if ((sAlternate.length > 0) && (sAlternate == value)) return status;
      }
    }
    
    // check types, TODO not all xs: types are implemented
    if (!status.isValid) return status;
    var sType = gxe.cfg.getGxeAttributeValue(this.cfgObject,"valueType");
    if (sType != null) sType = dojo.trim(sType);
   
    if ((sType == "integer") || (sType == "xs:integer") || (sType == "xsd:integer")) {
      // the expression is not definitive
      //regexp = /^[-]?[0-9]+$/;
      regexp  = /(^-?\d\d*$)/;
      if (!regexp.test(value)) {
        status.isValid = false;
        status.message = this.
        formatValidationMessage(inputControl,"validate.integer");
      }
      
    } else if ((sType == "decimal") || (sType == "xs:decimal") || (sType == "xsd:decimal") ||
               (sType == "double") || (sType == "xs:double") || (sType == "xsd:double") ||
               (sType == "float") || (sType == "xs:float") || (sType == "xsd:float") ||
               (sType == "number")) {
      // same expression for any non-integer type, should be more explicit
      // the expression is not definitive
      regexp = /(^-?\d\d*\.\d*$)|(^-?\d\d*$)|(^-?\.\d\d*$)/;
      if (!regexp.test(value)) {
        status.isValid = false;
        status.message = this.formatValidationMessage(inputControl,"validate.number");
      }
      
    } else if ((sType == "date") || (sType == "xs:date") || (sType == "xsd:date")) {

      // allows yyyy-mm-ddZ or yyyy-mm-dd or yyyy-mm or yyyy
      var bOk = false;
      var regexp1 = /^(\d{4})$/;
      var regexp2 = /^(\d{2})$/;
      var parts = value.split("-");
            
      if (regexp1.test(parts[0])) {
        if (parts.length > 1) {
          if (regexp2.test(parts[1])) {
            if (parts.length > 2) {
              if (parts.length == 3) {
                if (parts[2].charAt(parts[2].length-1) == 'Z') {
                  parts[2] = parts[2].substring(0,parts[2].length-1);
                }
                if (regexp2.test(parts[2])) bOk = true;
              }
            } else bOk = true;
          }
        } else bOk = true;
      }

      if (!bOk) {
        status.isValid = false;
        status.message = this.formatValidationMessage(inputControl,"validate.date");
      }
      
    } else if ((sType == "dateTime") || (sType == "xs:dateTime") || (sType == "xsd:dateTime")) {
      // TODO not handled
      
    } else if (sType == "fgdc:date") {

      // allows yyyymmdd or yyyymm or yyyy
      var bOk = false;
      var regexp1 = /^(\d{4})$/;
      var regexp2 = /^(\d{2})$/;
      var parts = new Array();
      if (value.length == 8) {
        parts[0] = value.substring(0,4);
        parts[1] = value.substring(4,6);
        parts[2] = value.substring(6,8);
      } else if (value.length == 6) {
        parts[0] = value.substring(0,4);
        parts[1] = value.substring(4,6);
      } else if (value.length == 4) {
        parts[0] = value.substring(0,4); 
      }
      if (parts.length > 0) {
        if (regexp1.test(parts[0])) {
          if (parts.length > 1) {
            if (regexp2.test(parts[1])) {
              if (parts.length > 2) {
                if (parts.length == 3) {
                  if (parts[2].charAt(parts[2].length-1) == 'Z') {
                    parts[2] = parts[2].substring(0,parts[2].length-1);
                  }
                  if (regexp2.test(parts[2])) bOk = true;
                }
              } else bOk = true;
            }
          } else bOk = true;
        }
      }
      if (!bOk) {
        status.isValid = false;
        status.message = this.formatValidationMessage(inputControl,"validate.date");
      }

    } else if (sType == "fgdc:time") {
      
      // (hours minutes seconds) examples: hh hhmm hhmmss 
      // (offset from GMT) examples: hh+hhmm hhmmss-hhmm
      // (suffixed with Z for Zulu time) examples: hhZ hhmmZ hhmmssZ
      // (decimal seconds are ssssssss)
      var regexp1 = /^\d{2}(\d{2}(\d{2,})?)?$/;
      var regexp2 = /^\d{2}(\d{2}(\d{2,})?)?[+\-]\d{4}$/;
      var regexp3 = /^\d{2}(\d{2}(\d{2,})?)?Z$/;
      if (!regexp1.test(value) && !regexp2.test(value) && !regexp3.test(value)) {
        status.isValid = false;
        status.message = this.formatValidationMessage(inputControl,"validate.other");
      }
      
    }

    // check restrictions, TODO not all xs: restrictions are implemented
    if (!status.isValid) return status;
    gxe.cfg.forEachChild(this.cfgObject,gxe.cfg.uriGxe,"restriction",dojo.hitch(this,function(cfgRestriction) {
      gxe.cfg.forEachChild(cfgRestriction,gxe.cfg.uriGxe,"*",dojo.hitch(this,function(cfgChild) {        

        if (cfgChild.name == "pattern") {
          var pattern = gxe.cfg.getGxeAttributeValue(cfgChild,"value");
          if (pattern != null) {
            try {
              // TODO what about ["g"|"i"|"gi"]
              // var regExp = new RegExp("PATTERN", ["g"|"i"|"gi"]);
              regexp = new RegExp(pattern);
              var obj = regexp.exec(value);
              if (!regexp.test(value)) {
                status.isValid = false;
                //status.message = "?invalid "+sLabel+" "+pattern;
                status.message = this.formatValidationMessage(inputControl,"validate.other");
                return "break";
              }  
            } catch (err) {
              console.log(err+", "+gxe.cfg.getGxeAttributeValue(this.cfgObject,"targetName")+
                  ", the g:"+cfgChild.name+" is incorrectly defined, "+pattern);
            }
          }
        }
        
        if ((cfgChild.name == "minExclusive") || (cfgChild.name == "minInclusive") || 
            (cfgChild.name == "maxExclusive") || (cfgChild.name == "maxInclusive")) {
          var nValue = new Number(value);
          var sBound = gxe.cfg.getGxeAttributeValue(cfgChild,"value");
          if (!isNaN(nValue)) {
            var nBound = new Number(sBound);
            if ((sBound != null) && !isNaN(nBound)) {
              if (cfgChild.name == "minExclusive") {
                if (nValue <= nBound) status.isValid = false;
              } else if (cfgChild.name == "minInclusive") {
                if (nValue < nBound) status.isValid = false;
              } else if (cfgChild.name == "maxExclusive") {
                if (nValue >= nBound) status.isValid = false;
              } else if (cfgChild.name == "maxInclusive") {
                if (nValue > nBound) status.isValid = false;
              }
              if (!status.isValid) {
               status.message = this.formatValidationMessage(inputControl,"validate.other");
              }
            } else {
              console.log(gxe.cfg.getGxeAttributeValue(this.cfgObject,"targetName")+
                  ", the g:"+cfgChild.name+" bound is incorrectly defined, "+sBound);
            }
          } else {
            status.isValid = false;
            status.message = this.formatValidationMessage(inputControl,"validate.other");
          }
        }
        
        if ((cfgChild.name == "length") ||  
            (cfgChild.name == "minLength") || (cfgChild.name == "maxLength")) {
          var nLength = value.length;
          var sBound = gxe.cfg.getGxeAttributeValue(cfgChild,"value");
          var nBound = new Number(sBound);
          if ((sBound != null) && !isNaN(nBound)) {
            if (cfgChild.name == "length") {
              if (nLength != nBound) status.isValid = false;
            } else if (cfgChild.name == "minLength") {
              if (nLength < nBound) status.isValid = false;
            } else if (cfgChild.name == "maxLength") {
              if (nLength > nBound) status.isValid = false;
            }
            if (!status.isValid) {
             status.message = this.formatValidationMessage(inputControl,"validate.other");
            }
          } else {
            console.log(gxe.cfg.getGxeAttributeValue(this.cfgObject,"targetName")+
                ", the g:"+cfgChild.name+" bound is incorrectly defined, "+sBound);
          }
        }
        
      })); 
      if (!status.isValid) return "break";
    }));

    return status;
  },

  /**
   * Validates multiple input values associated with a control.
   * @function 
   * @name validateInputValues
   * @memberOf gxe.xml.XmlNode#
   * @param {gxe.control.InputBase} inputControl the input control that generated the values
   * @param {Array} values the value array
   * @returns {"isValid":{boolean}, "message":{String}} the validation status
   */
  validateInputValues: function(inputControl,values) {
    var status = {"isValid": false, "message": null};
    if ((values == null) || (values.length == 0)) {
      var nMinOccurs = this.resolveMinOccurs(); 
      if ((nMinOccurs >= 1) && !this.getSerializeIfEmpty()) {
        status.isValid = false;
        status.message = this.formatValidationMessage(inputControl,"validate.empty");
      } else {
        status.isValid = true;
      }
    } else {
      status.isValid = true;
    }
    return status;
  },
  
  /**
   * Determines if this element wraps an ISO19139 multi-value list.
   * <br/>(e.g. MD_TopicCategoryCode - InputSelectMany)
   * @function 
   * @name wrapsIsoMultiValueList
   * @memberOf gxe.xml.XmlNode#
   * @returns {boolean}
   */
  wrapsIsoMultiValueList: function() {
    return false;
  }
  
});

/**
 * @class A target XML attribute.
 * @name gxe.xml.XmlAttribute
 * @extends gxe.xml.XmlNode
 */
dojo.provide("gxe.xml.XmlAttribute");
dojo.declare("gxe.xml.XmlAttribute",gxe.xml.XmlNode,{

  /** Override gxe.xml.XmlNode.echo() */
  echo: function(xmlGenerator,stringBuffer,nDepth) {
    if (this.isPlaceHolder || this.isOptionalPlaceHolder) return;
    
    var bSerialize = true;
    var bValidating = xmlGenerator.isValidating;
    var bIsTitle = this.nodeInfo.isDocumentTitle;
    if (bIsTitle && !bValidating) {
      if (xmlGenerator.isValidatingTitleOnly) bValidating = true;
    }
    var bSerializeIfEmpty = (this.getSerializeIfEmpty() || !bValidating);

    var sNodeValue = this.nodeInfo.nodeValue;
    var inputControl = this.getInputControl();
    if (inputControl != null) {
      this.nodeInfo.nodeValue = inputControl.getInputValue();
      sNodeValue = this.nodeInfo.nodeValue;
    }
    if (sNodeValue == null) sNodeValue = "";
    else sNodeValue = dojo.trim(sNodeValue);
    var bIsEmpty = (sNodeValue.length == 0);

    if ((inputControl != null) && bValidating) {
      var status = this.validateInputValue(inputControl,sNodeValue);
      if (!status.isValid) {
        bSerialize = false;
        xmlGenerator.handleValidationError(status.message,this,inputControl);
      }
    }
    if (bIsTitle) xmlGenerator.documentTitle = sNodeValue;
    
    if (bSerialize && (!bIsEmpty || bSerializeIfEmpty)) {
      var sNodeName = this.nodeInfo.localName;
      if (this.nodeInfo.namespacePrefix != null) {
        sNodeName = this.nodeInfo.namespacePrefix+":"+sNodeName;
      }
      sNodeValue = xmlGenerator.escAttribute(sNodeValue);
      stringBuffer.append(" "+sNodeName+"=\""+sNodeValue+"\"");
    }
  },

  /** Override gxe.xml.XmlNode.isAttribute() */
  isAttribute: function() {return true;},
  
  /** Override gxe.xml.XmlNode.isRepeatable() */
  isRepeatable: function() {return false;}
});

/**
 * @class A collection of XML attributes (of type gxe.xml.XmlAttribute).
 * @name gxe.xml.XmlAttributes
 * @extends gxe.util.ArrayList
 */
dojo.provide("gxe.xml.XmlAttributes");
dojo.declare("gxe.xml.XmlAttributes",gxe.util.ArrayList,{

  /**
   * Serializes XML content. 
   * @function 
   * @name echo
   * @memberOf gxe.xml.XmlAttributes#
   * @param {gxe.xml.Generator} xmlGenerator the XML generator
   * @param {gxe.util.StringBuffer} stringBuffer the buffer to which content will be written
   * @param {Integer} nDepth the indentation depth
   */
  echo: function(xmlGenerator,stringBuffer,nDepth) {
    var n = this.getLength();
    for (var i=0; i<n; i++) this.getItem(i).echo(xmlGenerator,stringBuffer,nDepth);
  }
});

/**
 * @class A target XML element.
 * @name gxe.xml.XmlElement
 * @extends gxe.xml.XmlNode
 */
dojo.provide("gxe.xml.XmlElement");
dojo.declare("gxe.xml.XmlElement",gxe.xml.XmlNode,{
  attributes: null,
  children: null,
  exclusiveChoiceControl: null,
  repeatablesContainer: null,

  /** constructor */
  constructor: function() {
    this.attributes = new gxe.xml.XmlAttributes();
    this.children = new gxe.xml.XmlElements();
  },

  /** Override gxe.xml.XmlNode.echo() */
  echo: function(xmlGenerator,stringBuffer,nDepth) {
    if (this.isPlaceHolder || this.isOptionalPlaceHolder) return;
    
    var pfx = "\r\n";
    for (var i=0; i<nDepth; i++) pfx += "\t";
    if (this.exclusiveChoiceControl != null) {
      if (!this.exclusiveChoiceControl.isElementSelected(this)) return;
    }

    var bValidating = xmlGenerator.isValidating;
    var bSerializeIfEmpty = (this.getSerializeIfEmpty() || !bValidating);
    var bIsTitle = this.nodeInfo.isDocumentTitle;
    var inputValues = null;
    var inputControl = this.getInputControl();
    if (inputControl != null) {
      this.nodeInfo.nodeValue = null;
      if (inputControl.getSupportsMultipleValues()) {

        inputValues = inputControl.getInputValues();
        if ((inputValues != null) && (inputValues.length > 0)) {
          this.nodeInfo.nodeValue = inputValues[0];
        }

        if (bValidating || (bIsTitle && xmlGenerator.isValidatingTitleOnly)) {
          var status = this.validateInputValues(inputControl,inputValues);
          if (!status.isValid) {
            bSerializeIfEmpty = false;
            this.nodeInfo.nodeValue = null;
            inputValues = null;
            xmlGenerator.handleValidationError(status.message,this,inputControl);
          }
        }
        
      } else {
        var inputValue = inputControl.getInputValue();
        if (inputValue != null) {
          this.nodeInfo.nodeValue = inputValue;
        }
        if (bValidating || (bIsTitle && xmlGenerator.isValidatingTitleOnly)) {
          var status = this.validateInputValue(inputControl,this.nodeInfo.nodeValue);
          if (!status.isValid) {
            bSerializeIfEmpty = false;
            this.nodeInfo.nodeValue = null;
            xmlGenerator.handleValidationError(status.message,this,inputControl);
          }
        }
      }
    }
    if (bIsTitle) xmlGenerator.documentTitle = this.nodeInfo.nodeValue;

    var nodeName = this.nodeInfo.localName;
    if (this.nodeInfo.namespacePrefix != null) {
      nodeName = this.nodeInfo.namespacePrefix+":"+nodeName;
    }
    var nodeValue = this.nodeInfo.nodeValue;

    var sbAttributePortion = new gxe.util.StringBuffer();
    this.attributes.echo(xmlGenerator,sbAttributePortion,nDepth);
    var sAttributePortion = sbAttributePortion.toString();
    var bHasAttributePortion = (sAttributePortion.length > 0);
    
    var bIsIsoCodeListValue = this.nodeInfo.isIsoCodeListValue;
    if (bIsIsoCodeListValue) {
      var n = this.attributes.getLength();
      for (var i=0; i<n; i++) {
        var attr = this.attributes.getItem(i);
        if (attr.nodeInfo.localName == "codeListValue") {
          var ic = attr.getInputControl();
          if (ic != null) {
            if (!ic.getSupportsMultipleValues()) {
              var sCode = ic.getInputValue(true);
              if ((sCode == null) || (sCode.length == 0)) return;
              else nodeValue = sCode;
            }
          }
        }
      }
    }
    
    // isIsoWrappedMultiValueList, e.g. MD_TopicCategoryCode - InputSelectMany
    if (this.wrapsIsoMultiValueList()) {
      this.children.getItem(0).echo(xmlGenerator,stringBuffer,nDepth);
      return;
    } else if (this.nodeInfo.isIsoWrappedMultiValueList) {
      if ((inputValues != null) && (inputValues.length > 0)) {
        var pNodeInfo = this.parentElement.nodeInfo;
        var pNodeName = pNodeInfo.localName;
        if (pNodeInfo.namespacePrefix != null) {
          pNodeName = pNodeInfo.namespacePrefix+":"+pNodeName;
        }
        var n = inputValues.length;
        for (var i=0; i<n; i++) {
          stringBuffer.append(pfx+"<"+pNodeName+">");
          stringBuffer.append(pfx+"\t<"+nodeName+">");
          stringBuffer.append(xmlGenerator.escElement(inputValues[i]));
          stringBuffer.append("</"+nodeName+">");
          stringBuffer.append(pfx+"</"+pNodeName+">");
        }
      }
      return;    
    }
   
    if (nodeValue != null) nodeValue = xmlGenerator.escElement(nodeValue);
    var bHasValue = ((nodeValue != null) && (nodeValue.length > 0));

    var sbChildPortion = new gxe.util.StringBuffer();
    this.children.echo(xmlGenerator,sbChildPortion,nDepth);
    var sChildPortion = sbChildPortion.toString();
    var bHasChildPortion = (sChildPortion.length > 0);

    if (bHasValue || bHasAttributePortion || bHasChildPortion) {
      var sbElement = new gxe.util.StringBuffer();
      sbElement.append(pfx+"<"+nodeName);
      if (bHasAttributePortion) sbElement.append(sAttributePortion);
      if (bHasValue || bHasChildPortion) {
        sbElement.append(">");
        if (bHasValue) sbElement.append(nodeValue);
        if (bHasChildPortion) {
          sbElement.append(sChildPortion);
          sbElement.append(pfx);
        } 
        sbElement.append("</"+nodeName+">");
      } else {
        sbElement.append("/>");
      }
      stringBuffer.append(sbElement.toString());
    } else {
      if (bSerializeIfEmpty) stringBuffer.append(pfx+"<"+nodeName+"/>");
    }

    if ((inputValues != null) && (inputValues.length > 1)) {
      var n = inputValues.length;
      for (var i=1; i<n; i++) {
        var xmlSibling = new gxe.xml.XmlElement(this.parentDocument,this.parentElement,this.cfgObject);
        xmlSibling.nodeInfo.nodeValue = inputValues[i];
        xmlSibling.echo(xmlGenerator,stringBuffer,nDepth);
      }
    }
  },

  /** Override gxe.xml.XmlNode.isRepeatable() */
  isRepeatable: function() {
    var sMaxOccurs = gxe.cfg.getGxeAttributeValue(this.cfgObject,"maxOccurs");
    if (sMaxOccurs == "unbounded") {
      return true;
    } else {
      var nMaxOccurs = parseInt(sMaxOccurs);
      if (isNaN(nMaxOccurs)) nMaxOccurs = 1;
      return (nMaxOccurs > 1);
    }
    return false;
  },
  
  /** Override gxe.xml.XmlNode.wrapsIsoMultiValueList() */
  wrapsIsoMultiValueList: function() {
    if (this.children.getLength() == 1) {
      return this.children.getItem(0).nodeInfo.isIsoWrappedMultiValueList;
    }
    return false;
  }
  
});

/**
 * @class A collection of XML elements (of type gxe.xml.XmlElement).
 * @name gxe.xml.XmlElements
 * @extends gxe.util.ArrayList
 */
dojo.provide("gxe.xml.XmlElements");
dojo.declare("gxe.xml.XmlElements",gxe.util.ArrayList,{

  /**
   * Serializes XML content. 
   * @function 
   * @name echo
   * @memberOf gxe.xml.XmlElements#
   * @param {gxe.xml.Generator} xmlGenerator the XML generator
   * @param {gxe.util.StringBuffer} stringBuffer the buffer to which content will be written
   * @param {Integer} nDepth the indentation depth
   */
  echo: function(xmlGenerator,stringBuffer,nDepth) {
    var n = this.getLength();
    for (var i=0; i<n; i++) this.getItem(i).echo(xmlGenerator,stringBuffer,nDepth+1);
  },

  /**
   * Finds the array index associated with an element.
   * @function 
   * @name findIndex
   * @memberOf gxe.xml.XmlElements#
   * @param {gxe.xml.XmlElement} xmlElement the subject element
   * @returns {Integer} the associated index (-1 if not located)
   */
  findIndex: function(xmlElement) {
    var n = this.getLength();
    for (var i=0; i<n; i++) {
      if (this.getItem(i) == xmlElement) return i;
    }
    return -1;
  }
  
});

/**
 * @class An XML namespace.
 * @name gxe.xml.XmlNamespace
 * @extends gxe.util.ArrayList
 * @property {String} prefix the prefix
 * @property {String} uri the URI
 */
dojo.provide("gxe.xml.XmlNamespace");
dojo.declare("gxe.xml.XmlNamespace",null,{
  prefix: null,
  uri: null,
  
  /**
   * Constructor.
   * @function 
   * @name constructor
   * @memberOf gxe.xml.XmlNamespaces#
   * @param {String} prefix the prefix
   * @param {String} uri the URI
   */
  constructor: function(prefix,uri) {
    this.prefix = prefix;
    this.uri = uri;
  }
});

/**
 * @class A collection of XML namespaces.
 * @name gxe.xml.XmlNamespaces
 * @extends gxe.util.ArrayList
 */
dojo.provide("gxe.xml.XmlNamespaces");
dojo.declare("gxe.xml.XmlNamespaces",gxe.util.ArrayList,{
  
  /**
   * Gets the namespace prefix associated with a URI.
   * @function 
   * @name getPrefix
   * @memberOf gxe.xml.XmlNamespaces#
   * @param {String} uri the URI
   * @returns {String} the namespace prefix for the URI (null if not located)
   */
  getPrefix: function(uri) {
    var n = this.getLength();
    for (var i=0; i<n; i++) {
      var ns = this.getItem(i);
      if ((ns != null) && (ns.uri == uri)) {
        return ns.prefix;
      }                           
    }
    return null;
  },

  /**
   * Gets the namespace URI associated with a prefix.
   * @function 
   * @name getUri
   * @memberOf gxe.xml.XmlNamespaces#
   * @param {String} prefix the prefix
   * @returns {String} the namespace URI for the prefix (null if not located)
   */
  getUri: function(prefix) {
    var n = this.getLength();
    for (var i=0; i<n; i++) {
      var ns = this.getItem(i);
      if ((ns != null) && (ns.prefix == prefix)) {
        return ns.uri;
      }                           
    }
    return null;
  }
});

/**
 * @class Provides information about a target XML node.
 * @name gxe.xml.XmlNodeInfo
 * @property {boolean} isDocumentTitle true if this node represents the document title
 * @property {boolean} isIsoCodeListValue true if this node represents an
 *   ISO19139 code list value
 * @property {boolean} isIsoWrappedMultiValueList true if this node represents an
 *   ISO19139 wrapped multi-value list (e.g. MD_TopicCategoryCode - InputSelectMany)
 * @property {String} localName the local node name
 * @property {String} namespacePrefix the namespace prefix
 * @property {String} namespaceURI the namespace URI
 * @property {String} nodeValue the node value
 */
dojo.provide("gxe.xml.XmlNodeInfo");
dojo.declare("gxe.xml.XmlNodeInfo",null,{
  localName: null,
  isDocumentTitle: false,
  isIsoCodeListValue: false,
  isIsoWrappedMultiValueList: false,
  namespacePrefix: null,
  namespaceURI: null,
  nodeValue: null
});


/* UI controls ======================================================= */


/**
 * @class Base class for all renderable controls.
 * <br/><br/>
 * Controls are created and rendered based upon the configuration objects associated
 * with an editor (i.e. elements that define the editor).
 * <br/><br/>
 * Controls will only be rendered if an associated HTML tag name can be determined.
 * <ul>
 * <li>If a configuration object explicitly defines an h:tag attribute, it will be used to
 * create a corresponding HTML DOM element on the page.</li>
 * <li>If a configuration object is defined within the GXE HTML namespace, then the local name
 * of the configuration object will be used to create a corresponding HTML DOM element 
 * on the page.</li>
 * </ul>
 * 
 * <br/><br/>
 * The processing flow is as follows:
 * <ul>
 * <li>instantiate</li>
 * <li>...initialize</li>
 * <li>...build</li>
 * <li>...execBuild</li>
 * <li>......importCfgProperties</li>
 * <li>.........importHtmlAttributes</li>
 * <li>......createHtmlElement</li>
 * <li>......onHtmlElementCreated</li>
 * <li>......processChildren</li>
 * <li>......... (recursive on this process)</li>
 * <li>......onHtmlChildrenCreated</li>
 * <li>......parentControl.onChildControlCreated</li>
 * </ul>
 *
 * @name gxe.control.Control
 * @property {cfgObject} cfgObject the associated editor configuration object 
 *   (i.e. an editor definition element)
 * @property {gxe.Context} context the editor context
 * @property {gxe.html.HtmlAttributes} htmlAttributes the configured HTML attributes for the control
 * @property {Element} htmlElement the HTML element to which the control is attached
 * @property {String} htmlTag the configured tag name for the HTML element
 * @property {String} htmlTextContent the configured text node content for the HTML element
 * @property {String} gxeId a unique auto-generated ID for the control
 * @property {gxe.control.Control} parentControl the parent of this control
 * @property {gxe.xml.XmlNode} xmlNode the targeted XML node.
 *   This is section based, many component controls target the same node. 
 *   (e.g. a header div, a label, an input text box can all target the same XML node)
 * @property {gxe.xml.XmlElement} xmlParentElement the parent element of the targeted XML node
 */
dojo.provide("gxe.control.Control");
dojo.declare("gxe.control.Control",null,{
  
  cfgObject: null,
  context: null,
  htmlAttributes: null,
  htmlClass: null,
  htmlElement: null,
  htmlTag: null,
  htmlTextContent: null,
  gxeId: null,
  parentControl: null,
  xmlNode: null,
  xmlParentElement: null,

  /**
   * Initializes the instance.
   * @function 
   * @name initialize
   * @memberOf gxe.control.Control#
   * @param {gxe.Context} context the editor context
   * @param {cfgObject} cfgObject the associated editor configuration object 
   *   (i.e. an editor definition element)
   */
  initialize: function(context,cfgObject) {
    this.context = context;
    this.cfgObject = cfgObject;
    this.gxeId = this.context.generateUniqueId();
    this.htmlAttributes = new gxe.html.HtmlAttributes();
    this.htmlAttributes.set("id",this.gxeId);
    if (this.cfgObject != null) {
      var targetName = gxe.cfg.getGxeAttributeValue(this.cfgObject,"targetName");
      if (targetName != null) {
        this.htmlAttributes.set("gxeTargetName",targetName);
      } 
    }
  },

  /**
   * Builds the user interface control.
   * @function 
   * @name build
   * @memberOf gxe.control.Control#
   * @param {Element} htmlParentElement the parent HTML element (a new 
   *   control will be appended to this parent)
   * @param {gxe.xml.DomProcessor} domProcessor an XML processor 
   *   (when opening an existing document)
   * @param {Node} domNode the DOM node that is actively being processed 
   *   (when opening an existing document)
   */
  build: function(htmlParentElement,domProcessor,domNode) {
    this.execBuild(htmlParentElement,domProcessor,domNode);
  },

  /**
   * Executes the build of the the user interface control.
   * @function 
   * @name execBuild
   * @memberOf gxe.control.Control#
   * @param {Element} htmlParentElement the parent HTML element (a new 
   *   control will be appended to this parent)
   * @param {gxe.xml.DomProcessor} domProcessor an XML processor 
   *   (when opening an existing document)
   * @param {Node} domNode the DOM node that is actively being processed 
   *   (when opening an existing document)
   */
  execBuild: function(htmlParentElement,domProcessor,domNode) {
    this.importCfgProperties(this.cfgObject);

    var bRendered = true;
    var oRendered = this.evaluateValue(gxe.cfg.getGxeAttributeValue(this.cfgObject,"rendered"));
    if (oRendered != null) {
      if (typeof(oRendered) == "boolean") {
        bRendered = (oRendered == true);
      } else if (typeof(oRendered) == "string") {
        if (oRendered == "$editor.isExpertMode") {
          bRendered = false;
        } else {
          bRendered = (oRendered == "true");
        }
      }
    }
    if (!bRendered) return;
    
    this.createHtmlElement();
    if (this.htmlElement != null) {
      this.htmlElement.gxeControl = this;
      if (htmlParentElement != null) htmlParentElement.appendChild(this.htmlElement);


      gxe.cfg.forEachHtmlAttribute(this.cfgObject,dojo.hitch(this,function(cfgAttribute) {
        var name = cfgAttribute.name;
        var value = cfgAttribute.value;
        if ((name != null) && (value != null) && (typeof(value) == "string")) {
          if (value.indexOf("$fire.") == 0) {
            var htmlEventName = name;
            var gxeEventName = dojo.trim(value.substring(6));
            this.htmlElement.setAttribute("gxeEventName",gxeEventName);
            dojo.connect(this.htmlElement,htmlEventName,this,dojo.hitch(this,function(e) {
              this.onEvent(e,this,htmlEventName,gxeEventName);               
            }));
          }
        }
      }));

      this.onHtmlElementCreated(domProcessor,domNode);
      this.processChildren(this.cfgObject,this.htmlElement,this.xmlNode,domProcessor,domNode);
      this.onHtmlChildrenCreated(domProcessor,domNode);
      if (this.parentControl != null) {
        this.parentControl.onChildControlCreated(this,domProcessor,domNode);
      }
    }
    
  },

  /**
   * Creates the HTML element associated with the control.
   * @function 
   * @name createHtmlElement
   * @memberOf gxe.control.Control#
   * @returns {Element} the HTML element (can be null)
   */
  createHtmlElement: function() {
    var el = null;
    if (this.htmlTag != null) {
      var el = document.createElement(this.htmlTag);
      if (this.htmlClass != null) el.className = this.htmlClass;
      if (this.htmlAttributes != null) this.htmlAttributes.apply(el);
      if (this.htmlTextContent != null) {
        var text = dojo.trim(this.htmlTextContent);
        if (text.length > 0) {
          el.appendChild(document.createTextNode(text));
        }
      }
    }
    this.htmlElement = el;
  },

  /**
   * Ensures the visibility of the HTML element associated with the control.
   * <br/>By default, no action is taken and the ensureVisibility method is triggered for the 
   * parent control.
   * @function 
   * @name ensureVisibility
   * @memberOf gxe.control.Control#
   * @param {gxe.xml.XmlNode} subjectXmlNode the subject XML node (can be null)
   */
  ensureVisibility: function(subjectXmlNode) {
    if (this.parentControl != null) {
      this.parentControl.ensureVisibility(subjectXmlNode);
    }
  },

  /**
   * Evaluates a configuration value.
   * @function 
   * @name evaluateValue
   * @memberOf gxe.control.Control#
   * @param {Object} value the value
   * @returns {Object} the evaluated value
   */
  evaluateValue: function(value) {
    if ((value != null) && (typeof(value) == "string")) {
      if (value.indexOf("$eval.") == 0) {
        var sEval = value.substring(6);
        value = eval(sEval);
      }
    }
    return value;
  },

  /**
   * Finds the first child control that matches a Dojo query expression.
   * @function 
   * @name findFirstChildControl
   * @memberOf gxe.control.Control#
   * @param {String} sDojoSelector the Dojo query expression
   * @param {gxe.control.Control} the first matching child control (null if none)
   */
  findFirstChildControl: function(sDojoSelector) {
    var ctlFirst = null;
    if ((this.htmlElement != null) && (typeof(sDojoSelector) != "undefined") && (sDojoSelector != null)) {
      dojo.query(sDojoSelector,this.htmlElement).forEach(dojo.hitch(this,function(item) {
        if (ctlFirst == null) {
          var ctl = item.gxeControl;
          if ((typeof(ctl) != "undefined") && (ctl != null)) {
            ctlFirst = ctl;
          }
        }
      }));
    }
    return ctlFirst;
  },
  
  /**
   * Explicitly fires the onEvent method.
   * @event 
   * @name fireOnEvent
   * @memberOf gxe.control.Control#
   * @param {Event} e the underlying browser event
   * @deprecated 
   */
  fireOnEvent: function(e) {
    this.onEvent(e,this,null,null);
  },

  /**
   * Ensures the visibility of and focuses the HTML element associated with the control.
   * @function 
   * @name focus
   * @memberOf gxe.control.Control#
   * @param {gxe.xml.XmlNode} subjectXmlNode the subject XML node (can be null)
   */
  focus: function(subjectXmlNode) {
    if (this.htmlElement != null) {
      this.ensureVisibility(subjectXmlNode);
      this.htmlElement.focus();
    }
  },

  /**
   * Gets the label text associated with a control.
   * @function 
   * @name getLabelText
   * @memberOf gxe.control.Control#
   * @return the label text (can be null)
   */
  getLabelText: function() {
    var sLabel = gxe.cfg.getLabelText(this.cfgObject);
    if (sLabel == null) sLabel = this.xmlNode.getLabelText();
    return sLabel;
  },
  
  /**
   * Imports configuration properties into the current control.
   * @function 
   * @name importCfgProperties
   * @memberOf gxe.control.Control#
   * @param {cfgObject} cfgObject the associated configuration object
   */
  importCfgProperties: function(cfgObject) {
    if ((this.htmlAttributes != null) && (cfgObject != null)) {
      var value = this.evaluateValue(cfgObject.value);
      var sHtmlTag = gxe.cfg.getGxeHtmlAttributeValue(cfgObject,"tag");
      if (sHtmlTag != null) {
        if (sHtmlTag.length > 0) this.htmlTag = sHtmlTag;
      } else {
        if (cfgObject.namespace == gxe.cfg.uriGxeHtml) {
          this.htmlTag = cfgObject.name;
        } 
      }
      this.htmlTextContent = value;
      this.importHtmlAttributes(cfgObject);
      gxe.cfg.forEachChild(cfgObject,gxe.cfg.uriGxe,"code",dojo.hitch(this,function(cfgCode) {
        if (cfgCode.value != null) eval(cfgCode.value);
      }));
    }
  },

  /**
   * Imports configuration attributes within the GXE HTML namespace into the current control.
   * <br/>(i.e. namespace "http://www.esri.com/geoportal/gxe/html")
   * @function 
   * @name importHtmlAttributes
   * @memberOf gxe.control.Control#
   * @param {cfgObject} cfgObject the associated configuration object
   */
  importHtmlAttributes: function(cfgObject) {
    if (cfgObject != null) {
      this.htmlAttributes.set("gxename",cfgObject.name);
      gxe.cfg.forEachHtmlAttribute(cfgObject,dojo.hitch(this,function(cfgAttribute) {
        var sName = cfgAttribute.name.toLowerCase();
        var value = this.evaluateValue(cfgAttribute.value);
        if (sName!= "id") this.htmlAttributes.set(sName,value);
      }));
    }
  },

  /**
   * Fired on a parent control when a child has been created.
   * <br/>This event fires after the child has been fully processed.
   * @event 
   * @name onChildControlCreated
   * @memberOf gxe.control.Control#
   * @param {gxe.control.Control} control the child control
   * @param {gxe.xml.DomProcessor} domProcessor an XML processor 
   *   (when opening an existing document)
   * @param {Node} domNode the DOM node that is actively being processed 
   *   (when opening an existing document)
   */
  onChildControlCreated: function(control,domProcessor,domNode) {},
  
  /**
   * Fired for certain events triggered by the control.
   * @event 
   * @name onEvent
   * @memberOf gxe.control.Control#
   * @param {Event} e the underlying browser event
   * @param {gxe.control.Control} gxeControl the control that fired the event
   * @param {String} htmlEventName the HTML event name (e.g. "onclick")
   * @param {String} gxeEventName a GXE name for the event (e.g. "onLabelClicked")
   */
  onEvent: function(e,gxeControl,htmlEventName,gxeEventName) {},

  /**
   * Fired when all of the children for a control have been created.
   * <br/>This event fires after all child controls have been added to the HTML DOM.
   * @event 
   * @name onHtmlChildrenCreated
   * @memberOf gxe.control.Control#
   * @param {gxe.xml.DomProcessor} domProcessor an XML processor 
   *   (when opening an existing document)
   * @param {Node} domNode the DOM node that is actively being processed 
   *   (when opening an existing document)
   */
  onHtmlChildrenCreated: function(domProcessor,domNode) {},

  /**
   * Fired when the HTML element for a control has been created.
   * <br/>This event fires prior to the creation of child controls.
   * @event 
   * @name onHtmlElementCreated
   * @memberOf gxe.control.Control#
   * @param {gxe.xml.DomProcessor} domProcessor an XML processor 
   *   (when opening an existing document)
   * @param {Node} domNode the DOM node that is actively being processed 
   *   (when opening an existing document)
   */
  onHtmlElementCreated: function(domProcessor,domNode) {},

  /**
   * Processes a configuration object associated with a targeted XML attribute (g:attribute).
   * @function 
   * @name processCfgAttribute
   * @memberOf gxe.control.Control#
   * @param {cfgObject} cfgAttribute the configuration object to process
   * @param {Element} htmlParentElement the parent HTML element
   *   (new controls will be appended to this parent)
   * @param {gxe.xml.XmlElement} xmlParentElement the targeted XML parent element
   * @param {gxe.xml.DomProcessor} domProcessor an XML processor 
   *   (when opening an existing document)
   * @param {Node} domNode the DOM node that is actively being processed 
   *   (when opening an existing document)
   */
  processCfgAttribute: function(cfgAttribute,htmlParentElement,xmlParentElement,domProcessor,domNode) {
    var sTargetName = gxe.cfg.getGxeAttributeValue(cfgAttribute,"targetName");
    var xmlDocument = xmlParentElement.parentDocument;
    var xmlAttribute = new gxe.xml.XmlAttribute(xmlDocument,xmlParentElement,cfgAttribute);
    xmlParentElement.attributes.add(xmlAttribute);
    var domMatch = null;
    var sDefault = gxe.cfg.getGxeAttributeValue(this.cfgObject,"fixedValue");
    if (sDefault == null) sDefault = gxe.cfg.getGxeAttributeValue(cfgAttribute,"value");
    if ((domProcessor != null) && (domNode != null)) {
      domMatch = domProcessor.findMatchingChildAttribute(domNode,cfgAttribute);
    } else {
      if (sDefault != null) xmlAttribute.nodeInfo.nodeValue = sDefault;
    }

    var ctl = this.context.makeXhtmlControl(cfgAttribute,this,true);
    ctl.xmlNode = xmlAttribute;
    ctl.xmlParentElement = xmlParentElement;
    ctl.build(htmlParentElement,domProcessor,domMatch);
    
    if ((domMatch != null) && (xmlAttribute.getInputControl() == null)) {
      // can this put the document in an undesirable state?
      if (sDefault != null) {
        xmlAttribute.nodeInfo.nodeValue = sDefault;
      } else {
        var sNodeValue = domMatch.nodeValue;
        xmlAttribute.nodeInfo.nodeValue = sNodeValue;
      }
    }
  },

  /**
   * Processes a configuration object associated with a targeted XML element (g:element).
   * @function 
   * @name processCfgElement
   * @memberOf gxe.control.Control#
   * @param {cfgObject} cfgElement the configuration object to process
   * @param {Element} htmlParentElement the parent HTML element
   *   (new controls will be appended to this parent)
   * @param {gxe.xml.XmlElement} xmlParentElement the targeted XML parent element
   * @param {gxe.xml.DomProcessor} domProcessor an XML processor 
   *   (when opening an existing document)
   * @param {Node} domNode the DOM node that is actively being processed 
   *   (when opening an existing document)
   */
  processCfgElement: function(cfgElement,htmlParentElement,xmlParentElement,domProcessor,domNode) {
    var sTargetName = gxe.cfg.getGxeAttributeValue(cfgElement,"targetName");
    var xmlDocument = xmlParentElement.parentDocument;
    var xmlElement = new gxe.xml.XmlElement(xmlDocument,xmlParentElement,cfgElement);
    xmlParentElement.children.add(xmlElement);
    var bRepeatable = xmlElement.isRepeatable();
    
    var domMatch = null;
    var domMatches = null;
    if ((domProcessor != null) && (domNode != null)) {
      
      var cfgMatchTop = gxe.cfg.findGxeChild(cfgElement,"matchTopElements");
      
      if (cfgMatchTop == null) {
        if (!bRepeatable) {
          domMatch = domProcessor.findMatchingChildElement(domNode,cfgElement);
          if (domMatch != null) {
            domMatches = new Array();
            domMatches.push(domMatch);
          } 
        } else {
          domMatches = domProcessor.findMatchingChildElements(domNode,cfgElement);
          if ((domMatches != null) && (domMatches.length > 0)) domMatch = domMatches[0];
        }
        
      } else {
        domMatches = domProcessor.findMatchingChildElements(domNode,cfgElement);
        if ((domMatches != null) && (domMatches.length > 0)) {
          var ns = xmlDocument.namespaces;
          var topMatches = new Array();
          for (var i=0; i<domMatches.length; i++) {
            var nd = domMatches[i];
            var bMatched = true;
            var nConditions = 0;
            var nConditionsMatched = 0;
            gxe.cfg.forEachChild(cfgMatchTop,gxe.cfg.uriGxe,"match",
              dojo.hitch(this,function(cfgChild) {
                nConditions++;
                var qPath = gxe.cfg.getGxeAttributeValue(cfgChild,"qPath");
                var qValue = gxe.cfg.getGxeAttributeValue(cfgChild,"qValue");
                var qMode = gxe.cfg.getGxeAttributeValue(cfgChild,"qMode");
                var bMust = (qMode != "mustNot");
                if (qPath != null) {
                  var b = domProcessor.matchTopElement(ns,nd,qPath,qValue,bMust);
                  if (b) nConditionsMatched++;
                }
              }
            ));
            if (nConditions == nConditionsMatched) topMatches.push(nd);
          }
          domMatches = topMatches;
          if (topMatches.length > 0) domMatch = topMatches[0];
        }
      }
    }  

    var ctl = this.context.makeXhtmlControl(cfgElement,this,true);
    ctl.xmlNode = xmlElement;
    ctl.xmlParentElement = xmlParentElement;
    ctl.build(htmlParentElement,domProcessor,domMatch);
    
    if (bRepeatable && (domMatches != null) && (domMatches.length > 1)) {
      var ctlRepeatables = xmlElement.repeatablesContainer;
      if (ctlRepeatables != null) {
        var bRepeat = true;
        var ctlInput = xmlElement.getInputControl();
        if ((ctlInput != null) && ctlInput.getSupportsMultipleValues()) bRepeat = false;
        else if (xmlElement.wrapsIsoMultiValueList()) bRepeat = false;
        if (bRepeat) {
          for (var i=1; i<domMatches.length; i++ ) {
            ctlRepeatables.repeatSection(domProcessor,domMatches[i]);
          }
        }
      }
    }
    
  },

  /**
   * Processes the children of a configuration object.
   * @function 
   * @name processChildren
   * @memberOf gxe.control.Control#
   * @param {cfgObject} cfgObject the configuration object to process
   * @param {Element} htmlParentElement the parent HTML element
   *   (new controls will be appended to this parent)
   * @param {gxe.xml.XmlNode} xmlNode the target XML node
   * @param {gxe.xml.DomProcessor} domProcessor an XML processor 
   *   (when opening an existing document)
   * @param {Node} domNode the DOM node that is actively being processed 
   *   (when opening an existing document)
   */
  processChildren: function(cfgObject,htmlParentElement,xmlNode,domProcessor,domNode) {
    gxe.cfg.forEachChild(cfgObject,"*","*",dojo.hitch(this,function(cfgChild) {
      if (cfgChild.namespace == gxe.cfg.uriGxe) {
        
        if (cfgChild.name == "attribute") {
          this.processCfgAttribute(cfgChild,htmlParentElement,xmlNode,domProcessor,domNode);          

        } else if ((cfgChild.name == "body") || 
                   (cfgChild.name == "elementText") || 
                   (cfgChild.name == "header")) {
          var ctl = this.context.makeXhtmlControl(cfgChild,this,true);
          ctl.xmlNode = xmlNode;
          ctl.xmlParentElement = xmlNode.parentElement;
          ctl.build(htmlParentElement,domProcessor,domNode);
          
        } else if (cfgChild.name == "element") {
          this.processCfgElement(cfgChild,htmlParentElement,xmlNode,domProcessor,domNode);
        } else {

          var sHtmlTag = gxe.cfg.getGxeHtmlAttributeValue(cfgChild,"tag");
          if (sHtmlTag != null) {
            var ctl = this.context.makeXhtmlControl(cfgChild,this,true);
            ctl.xmlNode = xmlNode;
            ctl.xmlParentElement = xmlNode.parentElement;
            ctl.build(htmlParentElement,domProcessor,domNode);
          }
        }

      } else if (cfgChild.namespace == gxe.cfg.uriGxeHtml) {
        var ctl = this.context.makeXhtmlControl(cfgChild,this,true);
        ctl.xmlNode = xmlNode;
        ctl.xmlParentElement = xmlNode.parentElement;
        ctl.build(htmlParentElement,domProcessor,domNode);
      }
    }));
  }

});

/**
 * @class An array backed collection of GXE controls.
 * @name gxe.control.ControlArray
 * @extends gxe.control.Control
 * @property {boolean} displayInline indicates if the control should be displayed inline 
 *   (default=false, i.e. block display)
 * @property {boolean} isExclusive indicates if the control is using exclusive display 
 *   (default=false)
 */
dojo.provide("gxe.control.ControlArray");
dojo.declare("gxe.control.ControlArray",gxe.control.Control,{
  _activeIndex: -1,
  _array: null,
  displayInline: false,
  isExclusive: false,

  /** Override gxe.control.Control.initialize() */
  initialize: function(context,cfgObject) {
    this._array = new Array();
    this.inherited(arguments);
    var s = gxe.cfg.getGxeAttributeValue(this.cfgObject,"displayInline");
    this.displayInline = (s == "true");
  },

  /**
   * Activates the control at a given index.
   * @function
   * @name activateIndex
   * @memberOf gxe.control.ControlArray#
   * @param {Integer} nIndex the index of the control to activate
   */
  activateIndex: function(nIndex) {
    this._activeIndex = nIndex;
    this.syncDisplay(true);
  },

  /**
   * Finds the index associated with a control.
   * @function
   * @name findIndex
   * @memberOf gxe.control.ControlArray#
   * @param {gxe.control.Control} control the subject control
   * @returns {Integer} the associated index (-1 if not located)
   */
  findIndex: function(control) {
    var n = this._array.length;;
    for (var i=0; i<n; i++) {
      if (this.getItem(i) == control) return i;
    }
    return -1;
  },

  /**
   * Finds the index associated with a target XML node.
   * @function
   * @name findIndexByXmlNode
   * @memberOf gxe.control.ControlArray#
   * @param {gxe.xml.XmlNode} subjectXmlNode the subject XML node (element or attribute)
   * @returns {Integer} the associated index (-1 if not located)
   */
  findIndexByXmlNode: function(subjectXmlNode) {
    if (subjectXmlNode != null) {
      var inputControl = subjectXmlNode.getInputControl();
      var n = this._array.length;;
      for (var i=0; i<n; i++) {
        var ctl = this.getItem(i);
        if (ctl.xmlNode == subjectXmlNode) return i;
        if ((inputControl != null) && (ctl.htmlElement != null)) {
          var s = "[id='"+inputControl.gxeId+"']";
          var bLocated = false;
          dojo.query(s,ctl.htmlElement).forEach(dojo.hitch(this,function(item) {
            var ctl2 = item.gxeControl;
            if ((typeof(ctl2) != "undefined") && (ctl2 != null)) {
              if (ctl2 == inputControl) bLocated = true;
            }
          }));
          if (bLocated) return i;
        }
      }
      return -1;
    }
  },

  /**
   * Fires the onArrayModified event.
   * @function
   * @name fireOnArrayModified
   * @memberOf gxe.control.ControlArray#
   */
  fireOnArrayModified: function() {
    this.onArrayModified(this.getLength(),this._activeIndex);
  },

  /**
   * Gets the active index.
   * @function
   * @name getActiveIndex
   * @memberOf gxe.control.ControlArray#
   * @returns {Integer}the active index
   */
  getActiveIndex: function() {
    return this._activeIndex;
  },
  
  /**
   * Gets the item at the specified index.
   * @function 
   * @name getItem
   * @memberOf gxe.control.ControlArray#
   * @param {Integer} nIndex the index
   * @returns {gxe.control.Control} the corresponding control
   */
  getItem: function(nIndex) {
    return this._array[nIndex];
  },

  /**
   * Gets the length of the array.
   * @function 
   * @name getLength
   * @memberOf gxe.control.ControlArray#
   * @returns {Integer} the length
   */
  getLength: function() {
    return this._array.length;
  },

  /**
   * Inserts a control at a specified index.
   * @function 
   * @name insertAt
   * @memberOf gxe.control.ControlArray#
   * @param {Integer} nIndex the index (same as JavaScript Array.splice)
   * @param {gxe.control.Control} control the control to insert
   * @param {boolean} bActivate (not currently used)
   */
  insertAt: function(nIndex,control,bActivate) {
    var elRef = null;
    var nIdx = -1;
    var childNodes = this.htmlElement.childNodes;
    for (var i=0; i<childNodes.length; i++) {
      if (childNodes[i].nodeType == 1) {
        nIdx++;
        if (nIdx == nIndex) {
          elRef = childNodes[i];
          break;
        }
      }
    }
    this._array.splice(nIndex,0,control);
    this.htmlElement.insertBefore(control.htmlElement,elRef);
    this.fireOnArrayModified();
  },

  /**
   * Fired when the array structure has been modified.
   * @event
   * @name onArrayModified
   * @memberOf gxe.control.ControlArray#
   * @param {Integer} count the number of controls in the array
   * @param {Integer} activeIndex the index of the active control
   */
  onArrayModified: function(count,activeIndex) {},

  /**
   * Appends a control to the array.
   * @function 
   * @name push
   * @memberOf gxe.control.ControlArray#
   * @param {gxe.control.Control} control the control to add
   * @param {boolean} bActivate if true then activate the newly added control
   */
  push: function(control,bActivate) {
    this._array.push(control);
    if (!this.isExclusive) {
      this.htmlElement.appendChild(control.htmlElement);
    }
    if (bActivate) {
      this._activeIndex = this.getLength() - 1;
      this.syncDisplay(true);
    }
    this.fireOnArrayModified();
  },

  /**
   * Removes the control at the specified index from the array.
   * @function 
   * @name removeIndex
   * @memberOf gxe.control.ControlArray#
   * @param {Integer} nIndex the index of the control to remove
   * @param {boolean} bActivate if true then activate an appropriate index following removal
   */
  removeIndex: function(nIndex,bActivate) {
    var el = this.getItem(nIndex).htmlElement;
    el.parentNode.removeChild(el);
    this._array.splice(nIndex,1);

    // which one is active following delete?
    if (bActivate) {
      if (this.getLength() == 1) {
        this._activeIndex = 0;
        this.syncDisplay(true);
      } else if (this.getLength() >= nIndex) {
        this._activeIndex = nIndex;
        if (this.getLength() == nIndex) this._activeIndex--;
        this.syncDisplay(true);
      } else {
        this._activeIndex = -1;
        this.syncDisplay(false);
      }
    }
    this.fireOnArrayModified();
  },
  
  /**
   * Explicitly sets the display style for the control array.
   * @function 
   * @name setDisplayStyle
   * @memberOf gxe.control.ControlArray#
   * @param {boolean} bShow if true then show the control
   */
  setDisplayStyle: function(bShow) {
    var el = this.htmlElement;
    if (bShow) {
      if (this.displayInline) el.style.display = "inline";
      else el.style.display = "block";
    } else {
      el.style.display = "none";
    }
  },

  /**
   * Swaps the positions of two controls within the array.
   * @function 
   * @name swapPosition
   * @memberOf gxe.control.ControlArray#
   * @param {Integer} nFromIndex the from index
   * @param {Integer} nToIndex the to index
   * @param {boolean} bActivate if true then activate the "to" index following the swap
   */
  swapPosition: function(nFromIndex,nToIndex,bActivate) {

    if (nFromIndex == nToIndex) return;
    var a = this._array[nFromIndex];
    var b = this._array[nToIndex];

    if (this.isExclusive) {
      this._array[nFromIndex] = b;
      this._array[nToIndex] = a;
      if (bActivate) {
        this._activeIndex = nToIndex;
        this.syncDisplay(true);
      }
      
    } else {
      var bUp = (nFromIndex > nToIndex);
      var elA = a.htmlElement;
      var elB = b.htmlElement;
      var nRefIndex = nFromIndex;
      if (bUp) {
        elA = b.htmlElement;
        elB = a.htmlElement;
        nRefIndex = nToIndex;
      }
      this._array[nFromIndex] = b;
      this._array[nToIndex] = a;
  
      var elARem = elA.parentNode.removeChild(elA);
      elB.parentNode.insertBefore(elARem,elB);
      var elRef = null;
      var nIdx = -1;
      var childNodes = elB.parentNode.childNodes;
      for (var i=0; i<childNodes.length; i++) {
        if (childNodes[i].nodeType == 1) {
          nIdx++;
          if (nIdx == nRefIndex) {
            elRef = childNodes[i];
            break;
          }
        }
      }
      var elBRem = elB.parentNode.removeChild(elB);
      elARem.parentNode.insertBefore(elBRem,elRef);
    }
    
    this.fireOnArrayModified();
  },

  /**
   * Ensures that the active control for an exclusively displayed array is properly displayed.
   * @function 
   * @name syncDisplay
   * @memberOf gxe.control.ControlArray#
   * @param {boolean} bForce if true then ensure that the control array itself is properly displayed
   */
  syncDisplay: function(bForce) {
    if (this.isExclusive) {
      var n = this._array.length;
      for (var i=0; i<n; i++) {
        var el = this._array[i].htmlElement;
        if (i == this._activeIndex) {
          if (this.htmlElement.childNodes.length == 0) {
            this.htmlElement.appendChild(el);
          } else {
            this.htmlElement.replaceChild(el,this.htmlElement.childNodes[0]);
          }
          break;
        }
      }
      if (this._activeIndex == -1) {
        if (this.htmlElement.childNodes.length == 1) {
          this.htmlElement.removeChild(this.htmlElement.childNodes[0]);
        }
      }
      if (bForce) {
        if (this.displayInline) this.htmlElement.style.display = "inline";
        else this.htmlElement.style.display = "block";
      }
    }
  },

  /**
   * Toggles the display style of the control array.
   * @function 
   * @name toggleDisplay
   * @memberOf gxe.control.ControlArray#
   */
  toggleDisplay: function() {
    var el = this.htmlElement;
    if (el.style.display == "none") {
      if (this.displayInline) el.style.display = "inline";
      else el.style.display = "block";
    } else el.style.display = "none";
  }

});

/**
 * @class A control array using exclusive display.
 * @name gxe.control.ExclusiveControlArray
 * @extends gxe.control.ControlArray
 * @property {boolean} isExclusive true
 */
dojo.provide("gxe.control.ExclusiveControlArray");
dojo.declare("gxe.control.ExclusiveControlArray",gxe.control.ControlArray,{
  isExclusive: true,

  /** Override gxe.control.Control.onChildControlCreated() */
  onChildControlCreated: function(control,domProcessor,domNode) {
    this.push(control,false);
    this.syncDisplay(true);
  }
});

/**
 * @class A control array using non-exclusive display.
 * @name gxe.control.NonExclusiveControlArray
 * @extends gxe.control.ControlArray
 * @property {boolean} isExclusive false
 */
dojo.provide("gxe.control.NonExclusiveControlArray");
dojo.declare("gxe.control.NonExclusiveControlArray",gxe.control.ControlArray,{
  isExclusive: false,

  /** Override gxe.control.Control.onChildControlCreated() */
  onChildControlCreated: function(control,domProcessor,domNode) {
    this.push(control,false);
    this.syncDisplay(true);
  }
});

/**
 * @class Provides a container for repeatable element controls (multiplicity > 1).
 * @name gxe.control.RepeatablesContainer
 * @extends gxe.control.ControlArray
 */
dojo.provide("gxe.control.RepeatablesContainer");
dojo.declare("gxe.control.RepeatablesContainer",gxe.control.ControlArray,{
  cfgSectionBody: null,
  isExclusive: false,
  multiplicityTools: null,
  xmlElementCfgObject: null,
  xmlParentElement: null,
  xmlPlaceHolder: null,

  /**
   * Determines if elements can be removed.
   * @function 
   * @name canRemove
   * @memberOf gxe.control.RepeatablesContainer#
   * @returns {boolean} true if elements can be removed
   */
  canRemove: function() {
    var bCanRemove = false;
    if (this.isConfigured()) {
      var nSections = 0;
      var nSimiliar = this.countSimilarSections();
      if (nSimiliar > 0) {
        var sMinOccurs = gxe.cfg.getMinOccurs(this.xmlElementCfgObject);
        var nMinOccurs = parseInt(sMinOccurs);
        if (isNaN(nMinOccurs)) nMinOccurs = 1;
        if (nMinOccurs < nSimiliar) {
          if (nSimiliar == 1) bCanRemove = false;
          else bCanRemove = true;          
        }
      }
    }
    return bCanRemove;
  },

  /**
   * Determines if elements can be repeated.
   * @function 
   * @name canRepeat
   * @memberOf gxe.control.RepeatablesContainer#
   * @returns {boolean} true if elements can be repeated
   */
  canRepeat: function() {
    var bCanRepeat = false;
    if (this.isConfigured()) {
      var sMaxOccurs = gxe.cfg.getMaxOccurs(this.xmlElementCfgObject);
      if (sMaxOccurs == "unbounded") {  
        bCanRepeat = true;
      } else {
        var nSimiliar = this.countSimilarSections();
        var nMaxOccurs = parseInt(sMaxOccurs);
        if (isNaN(nMaxOccurs)) nMaxOccurs = 1;
        if ((nMaxOccurs > 0) && (nSimiliar < nMaxOccurs)) {
          bCanRepeat = true;
        }
      }
    }
    return bCanRepeat;
  },
  
  /**
   * Counts the number of similar sections.
   * @function 
   * @name countSimilarSections
   * @memberOf gxe.control.RepeatablesContainer#
   * @returns {Integer} the number of similiar sections
   */
  countSimilarSections: function() {
    return this.getLength();
  },

  /** Override gxe.control.Control.ensureVisibility() */
  ensureVisibility: function(subjectXmlNode) {
    if (this.isExclusive) {
      var nIndex = this.findIndexByXmlNode(subjectXmlNode);
      if ((nIndex != -1) && (nIndex != this.getActiveIndex())) {
        this.activateIndex(nIndex);
        this.fireOnArrayModified();
      }
    }
    this.inherited(arguments);
  },

  /**
   * Handles multiplicity related events .
   * <br/>(repeatSection removeSection moveSectionUp moveSectionDown)
   * @event 
   * @name handleEvent
   * @memberOf gxe.control.RepeatablesContainer#
   * @param {Event} e the underlying browser event
   * @param {gxe.control.Section} sectionControl the section from which the event was fired
   * @param {String} htmlEventName the HTML event name (e.g. "onclick")
   * @param {String} gxeEventName a GXE name for the event (e.g. "repeatSection")
   */
  handleEvent: function(e,sectionControl,htmlEventName,gxeEventName) {
    if (gxeEventName == "repeatSection") {
      if (!this.isExclusive) {
        this.activateIndex(this.findIndex(sectionControl));
        var nTargetIndex = this.getActiveIndex();
        var nTargetLength = this.getLength();
        var bOk = (nTargetIndex == (nTargetLength - 1));
        if (bOk) this.repeatSection(null,null);
      } else {
        this.repeatSection(null,null);
      }
    } else if (gxeEventName == "removeSection") {
      if (!this.isExclusive) this.activateIndex(this.findIndex(sectionControl));
      this.removeSection();
    } else if (gxeEventName == "moveSectionUp") {
      if (!this.isExclusive) this.activateIndex(this.findIndex(sectionControl));
      this.moveSection(true);
    } else if (gxeEventName == "moveSectionDown") {
      if (!this.isExclusive) this.activateIndex(this.findIndex(sectionControl));
      this.moveSection(false);
    } 
  },

  /**
   * Determines if the control has been properly configured.
   * @function 
   * @name isConfigured
   * @memberOf gxe.control.RepeatablesContainer#
   * @returns {boolean} true if properly configured
   */
  isConfigured: function() {
    return (this.xmlElementCfgObject != null) && (this.xmlParentElement != null);
  },

  /**
   * Moves a section.
   * @function 
   * @name moveSection
   * @memberOf gxe.control.RepeatablesContainer#
   * @param {boolean} bUp true if the section (i.e. element should be move up within the target XML document)
   */
  moveSection: function(bUp) {
    if (!this.isConfigured()) return;
    
    // move up/down refers to up/down within the XML document
    
    // determine the target and xml indices
    var nTargetIndex = this.getActiveIndex();
    var nTargetLength = this.getLength();
    if ((nTargetIndex < 0) || (nTargetLength <= 0)) return;
    if (bUp && (nTargetIndex == 0)) return;
    if (!bUp && (nTargetIndex == (nTargetLength - 1))) return;
    var xmlElement = this.getItem(nTargetIndex).xmlNode;
    var nXmlIndex = xmlElement.parentElement.children.findIndex(xmlElement);
    if (nXmlIndex == -1) return;
  
    // determine the reposition indices
    var nNewTargetIndex = nTargetIndex + 1;
    var nNewXmlIndex = nXmlIndex + 1;
    if (bUp) {
      nNewTargetIndex = nTargetIndex - 1;
      nNewXmlIndex = nXmlIndex - 1;
    }
    if (nTargetIndex == nNewTargetIndex) return;
    
    // reposition
    xmlElement.parentElement.children.swapPosition(nXmlIndex,nNewXmlIndex);
    this.swapPosition(nTargetIndex,nNewTargetIndex,true);
  },
  
  /** Override gxe.control.ControlArray.onArrayModified() */
  onArrayModified: function(control,domProcessor,domNode) {
    this.syncTools();
  },

  /** Override gxe.control.Control.onChildControlCreated() */
  onChildControlCreated: function(control,domProcessor,domNode) {
    var bActivate = false;
    if (this.isExclusive) {
      bActivate = (domProcessor == null) || (this.getLength() == 0);
    }
    this.push(control,bActivate);
    this.syncDisplay(true);
  },

  /**
   * Removes a section.
   * @function 
   * @name removeSection
   * @memberOf gxe.control.RepeatablesContainer#
   */
  removeSection: function() {
    if (!this.canRemove()) return;
    
    var nSections = this.getLength();
    var nTargetIndex = this.getActiveIndex();
    if (nTargetIndex < 0) return;
    var xmlElement = this.getItem(nTargetIndex).xmlNode;
    var nXmlIndex = xmlElement.parentElement.children.findIndex(xmlElement);
    if (nXmlIndex == -1) return;

    if (nSections > 1) {
      xmlElement.parentElement.children.removeIndex(nXmlIndex);
      this.removeIndex(nTargetIndex,true);
    } else if (nSections == 1) {
      if (this.isExclusive) {
        xmlElement.isPlaceHolder = true;
        this.xmlPlaceHolder = xmlElement;
        this.removeIndex(nTargetIndex,true);
      } else {
        var ctlLast = this.getItem(0);
        var ctlBodyContainer = ctlLast.sectionBodyContainer;
        if ((typeof(ctlBodyContainer) != "undefined") && (ctlBodyContainer != null)) {
          this.cfgSectionBody = ctlBodyContainer.cfgSectionBody;
          xmlElement.isPlaceHolder = true;
          this.xmlPlaceHolder = xmlElement;
          ctlBodyContainer.removeIndex(0,true);
        } 
      }
    }
    
  },
  
  /**
   * Repeats a section.
   * @function 
   * @name repeatSection
   * @memberOf gxe.control.RepeatablesContainer#
   * @param {gxe.xml.DomProcessor} domProcessor an XML processor 
   *   (when opening an existing document)
   * @param {Node} domNode the DOM node that is actively being processed 
   *   (when opening an existing document)
   */
  repeatSection: function(domProcessor,domNode) {

    // initialize
    if (!this.canRepeat()) return;
    var nSections = this.getLength();      
    var xmlParent = this.xmlParentElement;
    var xmlDocument = xmlParent.parentDocument;
    var newXmlElement = new gxe.xml.XmlElement(xmlDocument,xmlParent,this.xmlElementCfgObject);
    var bUseAppend = true;

    // determine the insertion index
    var nTargetIndex = -1;
    var nXmlIndex = -1;
    if (this.xmlPlaceHolder != null) {
      nXmlIndex = xmlParent.children.findIndex(this.xmlPlaceHolder);
      if (nXmlIndex != -1) nXmlIndex++; // insert after
    } else {
      if (bUseAppend) {
        var xmlLast = this.getItem(nSections - 1).xmlNode;
        nXmlIndex = xmlParent.children.findIndex(xmlLast);
        if (nXmlIndex != -1) nXmlIndex++; // insert after
      } else {
        nTargetIndex = this.getActiveIndex();
        var xmlLast = this.getItem(this.getActiveIndex()).xmlNode;
        nXmlIndex = xmlParent.children.findIndex(xmlLast);
        if (nXmlIndex != -1) nXmlIndex++; // insert after
      }
    }
    if (nXmlIndex == -1) return; // exception message here??
          
    // repeat
    var elTmp = document.createElement("div"); 
    
    if (this.xmlPlaceHolder == null) {
      if (this.isExclusive) {
        xmlParent.children.insertAt(nXmlIndex,newXmlElement);
        this.processChildren(this.cfgObject,elTmp,newXmlElement,domProcessor,domNode);
      } else {
        xmlParent.children.insertAt(nXmlIndex,newXmlElement);
        var cfgElement = this.xmlElementCfgObject;
        var ctl = this.context.makeXhtmlControl(cfgElement,this,true);
        ctl.xmlNode = newXmlElement;
        ctl.xmlParentElement = newXmlElement.parentElement;
        ctl.build(elTmp,domProcessor,domNode);        
      }

    } else {
      var nPlaceHolderIndex = xmlParent.children.findIndex(this.xmlPlaceHolder);
      if (nPlaceHolderIndex == -1) return; // exception message here??
      
      if (this.isExclusive) {
        xmlParent.children.insertAt(nPlaceHolderIndex,newXmlElement);
        this.processChildren(this.cfgObject,elTmp,newXmlElement,domProcessor,domNode);
        xmlParent.children.removeIndex(nPlaceHolderIndex+1);
        this.xmlPlaceHolder = null;
        
      } else {
        var ctlLast = this.getItem(0);
        ctlLast.xmlNode = newXmlElement;
        ctlLast.xmlParentElement = this.xmlNode.parentElement;
        var ctlBodyContainer = ctlLast.sectionBodyContainer;
        if ((typeof(ctlBodyContainer) != "undefined") && (ctlBodyContainer != null)) {
          xmlParent.children.insertAt(nPlaceHolderIndex,newXmlElement);
          var cfgElement = this.xmlElementCfgObject;
          var ctl = this.context.makeXhtmlControl(this.cfgSectionBody,ctlBodyContainer,true);
          ctl.xmlNode = newXmlElement;
          ctl.xmlParentElement = newXmlElement.parentElement;
          ctl.build(elTmp,domProcessor,domNode);
          xmlParent.children.removeIndex(nPlaceHolderIndex+1);
          this.xmlPlaceHolder = null;          
          
        } 
      }
    } 
  },
    
  /**
   * Synchronizes the multiplicity tools for a section.
   * @function 
   * @name syncTools
   * @memberOf gxe.control.RepeatablesContainer#
   */
  syncTools: function() {
    if (!this.isConfigured()) return;
    var aToolbars = new Array();
    if (this.isExclusive) {
      aToolbars.push(this.multiplicityTools)
    } else {
      for (var i=0; i<this.getLength(); i++) aToolbars.push(this.getItem(i).multiplicityTools);
    }
    if (aToolbars.length > 0) {
      var _toggle = function(el,bEnabled) {
        if (bEnabled) {
          dojo.removeClass(el,"gxeDisabled");
          el.disabled = false;
        } else {
          dojo.addClass(el,"gxeDisabled");
          el.disabled = true;
        }
      };
    
      var nTargetIndex = this.getActiveIndex();
      var nTargetLength = this.getLength();
      for (var i=0; i<aToolbars.length; i++) {
        var ctlTools = aToolbars[i];
        if ((typeof(ctlTools) != "undefined") &&  (ctlTools != null)) {
          if (!this.isExclusive) nTargetIndex = i;
          dojo.query("[gxeToolName='repeatSection']",ctlTools.htmlElement).forEach(dojo.hitch(this,function(item) {
            var b = this.canRepeat();
            if (b && !this.isExclusive) b = (nTargetIndex == (nTargetLength - 1));
            _toggle(item,b);
          }));
          dojo.query("[gxeToolName='removeSection']",ctlTools.htmlElement).forEach(dojo.hitch(this,function(item) {
            _toggle(item,this.canRemove());
          }));
          dojo.query("[gxeToolName='moveSectionUp']",ctlTools.htmlElement).forEach(dojo.hitch(this,function(item) {
            _toggle(item,(nTargetIndex > 0));
          }));
          dojo.query("[gxeToolName='moveSectionDown']",ctlTools.htmlElement).forEach(dojo.hitch(this,function(item) {
            _toggle(item,((nTargetLength > 1) && (nTargetIndex < (nTargetLength - 1))));
          }));
        }
      }
    }
  }
  
});

/**
 * @class Container supporting the display of an array of tabs.
 * <br/><br/>Typically associated with:
 * <br/>gpt/gxe/core/ui/Tabs.xml
 * <br/>gpt/gxe/core/xml/ElementChoice.xml
 * <br/><br/>Also extended by:
 * <br/>gxe.control.IndexedTabArray
 * <br/>
 * @name gxe.control.TabArray
 * @extends gxe.control.Control
 */
dojo.provide("gxe.control.TabArray");
dojo.declare("gxe.control.TabArray",gxe.control.Control,{
  _activeIndex: -1,
  _array: null,

  /** Override gxe.control.Control.initialize() */
  initialize: function(context,cfgObject) {
    this._array = new Array();
    this.inherited(arguments);
  },

  /**
   * Activates the tab at a given index.
   * Once activated, the onTabClicked event is fired.
   * @function
   * @name activateIndex
   * @memberOf gxe.control.TabArray#
   * @param {Integer} nIndex the index of the tab to activate
   */
  activateIndex: function(nIndex) {
    this._activeIndex = nIndex;
    var nTabs = this._array.length;
    for (var i=0; i<nTabs; i++) this._setTabStyle(i);
    this.onTabClicked(this._activeIndex);
  },
  
  /**
   * Appends a radio button tab to the array.
   * @function
   * @name appendRadio
   * @memberOf gxe.control.TabArray#
   * @param {Integer} nIndex the index of the tab
   * @param {String} sLabel the label for the tab
   * @param {boolean} bIsActive true if the new tab should be active (i.e. selected)
   */
  appendRadio: function(nIndex,sLabel,bIsActive) {
    var elItem = document.createElement("li");
    this.htmlElement.appendChild(elItem);
    
    var elLink = document.createElement("a");
    elLink.tabindex = (nIndex + 1);
    elLink._gxeTabIndex = nIndex;
    elLink.setAttribute("href","javascript:void(0);");
    if (bIsActive) elLink.className = "current";
    elItem.appendChild(elLink);
    
    var sChoices = this.gxeId+"Option";
    var sOptionId = this.context.generateUniqueId();
    var elOption = document.createElement("input");
    if(dojo.isIE <= 8) {
      if (bIsActive) {
        elOption = document.createElement(
            "<input type=\"radio\" name=\""+sChoices+"\" checked=\"checked\"/>");
      } else {
        elOption = document.createElement("<input type=\"radio\" name=\""+sChoices+"\"/>");
      }
    } else  {
      elOption.setAttribute("name",sChoices);
      elOption.setAttribute("type","radio");
      if (bIsActive) elOption.setAttribute("checked","checked");
    }
    
    elOption.setAttribute("id",sOptionId);
    elOption.setAttribute("value",nIndex);    
    elLink.appendChild(elOption);
    
    var elLabel = document.createElement("label");
    if(dojo.isIE <= 8) {
      elLabel = document.createElement("<label for=\""+sOptionId+"\"/>");
    } else {
      elLabel.setAttribute("for",sOptionId);
    }
    
    elLabel.appendChild(document.createTextNode(sLabel));
    elLink.appendChild(elLabel);
    
    dojo.connect(elOption,"onclick",this,dojo.hitch(this,function(e) {
      this.activateIndex(elLink._gxeTabIndex);           
    }));
    this._array.push(elLink);
    if (bIsActive) this.activateIndex(nIndex);
    return elLink;
  },
  
  /**
   * Appends a tab to the array.
   * @function
   * @name appendTab
   * @memberOf gxe.control.TabArray#
   * @param {Integer} nIndex the index of the tab
   * @param {String} sLabel the label for the tab
   * @param {boolean} bIsActive true if the new tab should be active (i.e. selected)
   */
  appendTab: function(nIndex,sLabel,bIsActive) {
    var elItem = document.createElement("li");
    var elLink = document.createElement("a");
    elLink.tabindex = (nIndex + 1);
    elLink._gxeTabIndex = nIndex;
    elLink.setAttribute("href","javascript:void(0);");
    if (bIsActive) elLink.className = "current";
    elLink.appendChild(document.createTextNode(sLabel));
    elItem.appendChild(elLink);
    dojo.connect(elLink,"onclick",this,dojo.hitch(this,function(e) {
      this.activateIndex(elLink._gxeTabIndex);
    }));
    this.htmlElement.appendChild(elItem);
    this._array.push(elLink);
    if (bIsActive) this.activateIndex(nIndex);
    return elLink;
  },
  
  /**
   * Fired when a tab is clicked.
   * @event 
   * @name onTabClicked
   * @memberOf gxe.control.TabArray#
   * @param {Integer} nIndex the index of the clicked tab
   */
  onTabClicked: function(nIndex) {},
  
  /**
   * Sets the CSS class name for a tab.
   * @function
   * @name _setTabStyle
   * @memberOf gxe.control.TabArray#
   * @param {Integer} nIndex the index of the tab
   */
  _setTabStyle: function(nIndex) {
    var elLink = this._array[nIndex];
    var nTabIndex = elLink._gxeTabIndex;
    var bIsActive = (nTabIndex == this._activeIndex);
    if (bIsActive && elLink.className != "current") {
      elLink.className = "current";
    } else if (!bIsActive && elLink.className == "current") {
      elLink.className = "";
    }
  }

});

/**
 * @class Provides an array of tabs indexed by the multiplicity of a targeted XML element.
 * <br/>An indexed tab array is used when the body of the target is display exclusively.
 * @name gxe.control.IndexedTabArray
 * @extends gxe.control.TabArray
 */
dojo.provide("gxe.control.IndexedTabArray");
dojo.declare("gxe.control.IndexedTabArray",gxe.control.TabArray,{
  
  /**
   * Auto configures the control.
   * @function 
   * @name autoConfigure
   * @memberOf gxe.control.IndexedTabArray#
   * @param {gxe.Context} context the editor context 
   * @param {gxe.control.Control} parentControl the parent control
   */
  autoConfigure: function(context,parentControl) {
    var cfgAttr;
    var cfgObj = new Object();
    cfgObj.namespace = gxe.cfg.uriGxeHtml;
    cfgObj.name = "ul";
    cfgObj.parent = parentControl.cfgObject;
    cfgObj.attributes = new Array();
    this.cfgObject = cfgObj;
  
    cfgAttr = new Object();
    cfgAttr.namespace = gxe.cfg.uriGxeHtml;
    cfgAttr.name = "class";
    cfgAttr.value = "gxeTabArray";
    cfgAttr.parent = cfgObj;
    cfgObj.attributes.push(cfgAttr);
    this.initialize(context,cfgObj);
  },
  
  /**
   * Synchronizes the array of indexed tabs.
   * @function 
   * @name synchronize
   * @memberOf gxe.control.IndexedTabArray#
   * @param {Integer} count the number of repeated XML elements
   * @param {Integer} activeIndex the index of the active element
   */
  synchronize: function(count,activeIndex) {
    var nTabs = this._array.length;
    if (nTabs < count) {
      for (var i=nTabs; i<count; i++) {
        var nTabIndex = this._array.length;
        var bIsActive = (nTabIndex == activeIndex);
        this.appendTab(nTabIndex,""+(nTabIndex+1),bIsActive);
      }
    } else if (nTabs > count) {
      var n = (nTabs - count);
      for (var i=(nTabs - 1); i>=count; i--) {
        var elTab = this._array[i];
        elTab.parentNode.removeChild(elTab);
        this._array.pop();
      }
    }
    this._activeIndex = activeIndex;
    nTabs = this._array.length;
    for (var i=0; i<nTabs; i++) {
      var elLink = this._array[i];
      elLink.tabindex = (i + 1);
      elLink._gxeTabIndex = i;
      this._setTabStyle(i);
    }
    if (nTabs < 2) this.htmlElement.style.display = "none";
    else this.htmlElement.style.display = "inline";
  }

});

/**
 * @class Supports the message area section of the page. 
 * @name gxe.control.MessageArea
 * @extends gxe.control.Control
 */
dojo.provide("gxe.control.MessageArea");
dojo.declare("gxe.control.MessageArea",gxe.control.Control,{
  ul: null,

  /**
   * Adds an error message.
   * @function 
   * @name addError
   * @memberOf gxe.control.MessageArea#
   * @param {String} sMessage the message
   */
  addError: function(sMessage) {
    this.addMessage(sMessage,"error");
  },

  /**
   * Adds a message.
   * @function 
   * @name addMessage
   * @memberOf gxe.control.MessageArea#
   * @param {String} sMessage the message
   * @param {String} sClass the CSS class name (success|warning|error)
   */
  addMessage: function(sMessage,sClass) {
    var elItem = document.createElement("li");
    if (sClass != null) elItem.className = sClass;
    elItem.appendChild(document.createTextNode(sMessage));
    this.ul.appendChild(elItem);
    this.ensureVisibility();
  },
  
  /**
   * Adds a success message.
   * @function 
   * @name addSuccess
   * @memberOf gxe.control.MessageArea#
   * @param {String} sMessage the message
   */
  addSuccess: function(sMessage) {
    this.addMessage(sMessage,"success");
  },

  /**
   * Adds a validation error to the control.
   * <br/>Validation errors are added as warning messages.
   * @function 
   * @name addValidationError
   * @memberOf gxe.control.MessageArea#
   * @param {String} sMessage the message
   * @param {gxe.xml.XmlNode} xmlNode the targeted XML node (element or attribute)
   * @param {gxe.control.InputBase} the input control associated with the target
   */
  addValidationError: function(sMessage,xmlNode,inputControl) {
    if (sMessage == null) sMessage = "";

    if (inputControl == null) {
      this.addMessage(sMessage,"error");
    } else {
      
      var elItem = document.createElement("li");
      elItem.className = "warning";
      this.ul.appendChild(elItem);
      var elLink = document.createElement("a");
      elLink.setAttribute("href","javascript:void(0);");
      elLink.appendChild(document.createTextNode(sMessage));
      if (inputControl.htmlElement != null) {
        var sTip = inputControl.htmlElement.title;
        if ((typeof(sTip) != "undefined") && (sTip != null)) {
          sTip = dojo.trim(sTip);
          if (sTip.length > 0) elLink.title = sTip;
        }
      }
      
      elItem.appendChild(elLink);
      var handle = dojo.connect(elLink,"onclick",this,dojo.hitch(this,function(e) {
        inputControl.focus(xmlNode); 
        this.scrollOnClick(elItem);
      }));
      
      dojo.connect(inputControl,"onInputChanged",this,dojo.hitch(this,function(inputControl2,value) {
        var status =  xmlNode.validateInput(inputControl2,true);
        if (status.isValid) {
          elItem.className = "success"; 
        } else {
          elItem.className = "warning"; 
        }  
        if (status.message != null) {
          while (elLink.childNodes.length >= 1) elLink.removeChild(elLink.firstChild);
          elLink.appendChild(document.createTextNode(status.message));
        }              
      }));
      
      this.ensureVisibility();
    }    
  },

  /**
   * Adds a warning message.
   * @function 
   * @name addWarning
   * @memberOf gxe.control.MessageArea#
   * @param {String} sMessage the message
   */
  addWarning: function(sMessage) {
    this.addMessage(sMessage,"warning");
  },

  /** Override gxe.control.Control.build() */
  build: function(htmlParentElement,domProcessor,domNode) {
    this.htmlElement = htmlParentElement;
    this.htmlElement.style.display = "none";
    
    var el = this.htmlElement;
    while (el.childNodes.length >= 1) el.removeChild(el.firstChild);
    
    var elClear = document.createElement("a");
    elClear.setAttribute("href","javascript:void(0);");
    elClear.className = "gxeClearAll";
    elClear.appendChild(document.createTextNode(this.context.getI18NString("button.clearMessages")));
    dojo.connect(elClear,"onclick",this,"clearAll");
    this.htmlElement.appendChild(elClear);

    /*
    var elClear = document.createElement("button");
    elClear.setAttribute("type","button");
    elClear.appendChild(document.createTextNode(this.context.getI18NString("button.clearMessages")));
    dojo.connect(elClear,"onclick",this,"clearAll");
    this.htmlElement.appendChild(elClear);
    */
    
    var elWrapper = document.createElement("div");
    elWrapper.className = "gxeMessageListWrapper";
    this.htmlElement.appendChild(elWrapper);
    
    this.ul = document.createElement("ul");
    this.ul.className = "gxeMessageList";
    elWrapper.appendChild(this.ul);
  },

  /**
   * Clears add messages and hides the message area.
   * @function 
   * @name clearAll
   * @memberOf gxe.control.MessageArea#
   */
  clearAll: function() {
    var aRemove = new Array();
    var childNodes = this.ul.childNodes;
    for (var i=0; i<childNodes.length; i++) {
      if (childNodes[i].nodeType == 1) {
        aRemove.push(childNodes[i]);
      }
    }
    for (var i=0; i<aRemove.length; i++) {
      this.ul.removeChild(aRemove[i]);
    }
    this.htmlElement.style.display = "none";
    if (dojo.hasClass(this.context.htmlParentElement,"gxeRepairMode")) {
      dojo.removeClass(this.context.htmlParentElement,"gxeRepairMode");
    }
  },

  /** Override gxe.control.Control.ensureVisibility() */
  ensureVisibility: function() {
    if (this.htmlElement.style.display == "none") this.htmlElement.style.display = "block";
  },

  /**
   * Handles an exception by adding an error message to the control.
   * @function 
   * @name handleException
   * @memberOf gxe.control.MessageArea#
   * @param {String} exception the exception
   */
  handleException: function(exception) {
    this.addMessage(exception,"error");
  },
  
  /**
   * Scrolls the clicked message item toward the center of the container.
   * @function 
   * @name scrollOnClick
   * @memberOf gxe.control.MessageArea#
   * @param {DOMNode} elItem the clicked item
   */
  scrollOnClick: function(elItem) {
    var elWrapper = this.ul.parentNode;
    var nItemOffset = elItem.offsetTop - this.ul.offsetTop;
    var nItemHeight = elItem.offsetHeight;
    var nClientHeight = elWrapper.clientHeight;
    elWrapper.scrollTop = nItemOffset - (nClientHeight / 2) + (nItemHeight / 2);
  }

});

/**
 * @class Supports a UI section. 
 * <br/>A section typically consists of a header and a body.
 * <br/><br/>Typically associated with:
 * <br/>gpt/gxe/core/ui/Section.xml
 * <br/><br/>Also referenced by:
 * <br/>gpt/gxe/core/xml/Attribute.xml
 * <br/>gpt/gxe/core/xml/Element.xml
 * <br/>gpt/gxe/core/xml/ElementTextOnly.xml
 * <br/>
 * @name gxe.control.Section
 * @extends gxe.control.Control
 */
dojo.provide("gxe.control.Section");
dojo.declare("gxe.control.Section",gxe.control.Control,{
  repeatablesContainer: null,
  sectionContainer: null,
  sectionHeader: null,
  sectionBodyContainer: null,
  sectionBody: null,
  uiLabelText: null,
  useExclusiveDisplay: true,

  cfgSectionBody: null,
  indexedTabArray: null,
  tabArray: null,

  /** Override gxe.control.Control.initialize() */
  initialize: function(context,cfgObject) {
    var s = gxe.cfg.getGxeAttributeValue(cfgObject,"useExclusiveDisplay");
    if (this.useExclusiveDisplay) this.useExclusiveDisplay = (s != "false");
    else this.useExclusiveDisplay = (s == "true");
    this.inherited(arguments);
  },

  /** Override gxe.control.Control.build() */
  build: function(htmlParentElement,domProcessor,domNode) {
    var bRepeatable = this.xmlNode.isRepeatable();
    if ((this.parentControl == null) || this.useExclusiveDisplay || !bRepeatable) {
      this.inherited(arguments);
      return;
    } 

    var ctlContainer = this.parentControl.sectionContainer;
    if ((typeof(ctlContainer) == "undefined") || (ctlContainer == null)) {

      
      var cfgParent = this.parentControl.cfgObject;
      var cfgChild = this.cfgObject;

      var cfgContainer = new Object();
      cfgContainer.namespace = gxe.cfg.uriGxeHtml;
      cfgContainer.name = "div";
      cfgContainer.parent = cfgParent;

      var cfgAttribute;
      cfgContainer.attributes = new Array();

      cfgAttribute = new Object();
      cfgAttribute.namespace = gxe.cfg.uriGxeHtml;
      cfgAttribute.name = "class";
      cfgAttribute.value = "gxeSectionContainer";
      cfgAttribute.parent = cfgContainer;
      cfgContainer.attributes.push(cfgAttribute);
      
      cfgAttribute = new Object();
      cfgAttribute.namespace = gxe.cfg.uriGxe;
      cfgAttribute.name = "jsClass";
      cfgAttribute.value = "gxe.control.RepeatablesContainer";
      cfgAttribute.parent = cfgContainer;
      cfgContainer.attributes.push(cfgAttribute);

      ctlContainer = this.context.makeXhtmlControl(cfgContainer,this.parentControl,true);
      ctlContainer.isExclusive = false;
      
      ctlContainer.sectionContainer = ctlContainer;
      this.sectionContainer = ctlContainer;      
      ctlContainer.xmlNode = this.xmlNode;
      ctlContainer.xmlParentElement = this.xmlNode.parentElement;
      
      ctlContainer.build(htmlParentElement,domProcessor,domNode);
      var elHtmlParent = ctlContainer.htmlElement;
      cfgContainer.children = new Array();
      cfgContainer.children.push(this.cfgObject);

      this.parentControl = ctlContainer;
      this.execBuild(elHtmlParent,domProcessor,domNode);
    } else {
      this.sectionContainer = ctlContainer;
      this.inherited(arguments);
    }
    
  },
  
  /**
   * Initializes events associated with the section header label.
   * <br/>Adds a checkbox to the label when a targeted XML node is optional.
   * @function 
   * @name initializeLabelEvents
   * @memberOf gxe.control.Section#
   * @param {gxe.xml.XmlNode} xmlNode the targeted XML node (element or attribute)
   * @param {gxe.control.SectionMenu} associated ctlMenu the section menu 
   *   (may be null)
   * @param {gxe.control.IndexedtabArray} ctlIndexedIabArray associated indexed tab array 
   *   (may be null)
   * @param {gxe.xml.DomProcessor} domProcessor an XML processor 
   *   (when opening an existing document)
   * @param {Node} domNode the DOM node that is actively being processed 
   *   (when opening an existing document)
   */
  initializeLabelEvents: function(xmlNode,ctlMenu,ctlIndexedIabArray,domProcessor,domNode) {
    
    var ctlHeader = this.findFirstChildControl(">[gxename='header']");
    if (ctlHeader != null) {
      
      var ctlLabel = ctlHeader.findFirstChildControl(">[gxename='label']");
      if (ctlLabel != null) {
        
        var cn = ctlLabel.htmlElement.childNodes;
        for (var i=0; i<cn.length; i++) {
          if (cn[i].nodeType == 3) {
            this.uiLabelText = cn[i].nodeValue; // (nodeType == 3) is a TEXT_NODE
            break;
          }
        }
        
        if (ctlMenu == null) {
          var sInfo = gxe.cfg.getGxeAttributeValue(this.cfgObject,"info");
          if (sInfo != null) this.makeControlMenu(ctlHeader,true);
        }
        
        var nMinOccurs = xmlNode.resolveMinOccurs();   
        if (nMinOccurs >= 1) {
          ctlLabel.htmlElement.className = "required";
        } else {
          
          var bChecked = true;
          if (domProcessor == null) {
            bChecked = false;
            if (gxe.cfg.getGxeAttributeValue(this.cfgObject,"preferOpen") == "true") bChecked = true;
          } else {
            bChecked = domProcessor.hasChildrenOrAttributes(domNode);
          }
          
          var sId = this.context.generateUniqueId();
          var el = document.createElement("input");
          el.setAttribute("type","checkbox");
          el.setAttribute("id",sId);
          var elLabel = ctlLabel.htmlElement;
          elLabel.parentNode.insertBefore(el,elLabel);
          if (bChecked) el.setAttribute("checked","checked");
          elLabel.setAttribute("for",sId);
          
          var _toggleVisibility = function(ctl,bVisible) {
            if (ctl != null) {
              if (bVisible) ctl.htmlElement.style.visibility = "visible";
              else ctl.htmlElement.style.visibility = "hidden";
            }
          };
          
          var _onCheckBoxClicked = function(sbc,bWasChecked) {
            xmlNode.isOptionalPlaceHolder = !bWasChecked;
            _toggleVisibility(ctlMenu,bWasChecked);
            _toggleVisibility(ctlIndexedIabArray,bWasChecked);
            if (sbc != null) {
              sbc.setDisplayStyle(bWasChecked);
              var n = sbc.getLength();
              for ( var i=0; i<sbc.getLength(); i++) {
                sbc.getItem(i).xmlNode.isOptionalPlaceHolder = !bWasChecked;
              }
            }
          };
          
          dojo.connect(el,"onclick",this,dojo.hitch(this,function(e) { 
            _onCheckBoxClicked(this.sectionBodyContainer,el.checked);
          }));
          
          if (!bChecked) _onCheckBoxClicked(this.sectionBodyContainer,bChecked);
          
        }
        
      }
      
      var nSubHeaders = 0;
      dojo.query("[gxename='header']",this.htmlElement).forEach(dojo.hitch(this,function(item) {
        nSubHeaders++;
      }));
      if (nSubHeaders == 1) this.htmlElement.style.border = "none";
    }
  },

  /**
   * Fired for certain events triggered from the section header.
   * @event 
   * @name onHeaderEvent
   * @memberOf gxe.control.Section#
   * @param {Event} e the underlying browser event
   * @param {gxe.control.Control} gxeControl the control that fired the event
   * @param {String} htmlEventName the HTML event name (e.g. "onclick")
   * @param {String} gxeEventName a GXE name for the event (e.g. "onLabelClicked")
   *   within the configuration object for the section control (for popup help)
   */
  onHeaderEvent: function(e,gxeControl,htmlEventName,gxeEventName) {
    if (gxeEventName == "onLabelClicked") {
      if (this.sectionBodyContainer != null) {
        this.sectionBodyContainer.toggleDisplay();
      }
    } else {
      if (this.repeatablesContainer != null) {
        this.repeatablesContainer.handleEvent(e,this,htmlEventName,gxeEventName);
      }
    }
  },

  /** Override gxe.control.Control.onHtmlChildrenCreated() */
  onHtmlChildrenCreated: function(domProcessor,domNode) {
    var ctlContainer = this.sectionContainer;
    var ctlBodyContainer = this.sectionBodyContainer;
    var ctlHeader = this.findFirstChildControl("[gxename='header']");
    var ctlBody = this.findFirstChildControl("[gxename='body']");
    var ctlTabArray = null;
    var ctlIndexedTabs = null;

    if (ctlHeader != null) {
      ctlTabArray = ctlHeader.findFirstChildControl("[gxename='tabArray']");
      ctlIndexedTabs = ctlHeader.findFirstChildControl("[gxename='indexedTabArray']");
      
      /*
      dojo.query("[gxeEventName]",ctlHeader.htmlElement).forEach(dojo.hitch(this,function(item) {
        var ctl = item.gxeControl;
        if ((typeof(ctl) != "undefined") && (ctl != null)) {
          dojo.connect(ctl,"onEvent",this,"onHeaderEvent");
        }
      }));
      */
    }

    if (!this.useExclusiveDisplay) {
      if (ctlContainer != null) this.repeatablesContainer = ctlContainer;
    } else {
      if (ctlBodyContainer != null) {
        this.repeatablesContainer = ctlBodyContainer;
        if (ctlIndexedTabs != null) {
          dojo.connect(ctlBodyContainer,"onArrayModified",ctlIndexedTabs,"synchronize");
          dojo.connect(ctlIndexedTabs,"onTabClicked",ctlBodyContainer,"activateIndex");
        }
      }
    }
    if (this.repeatablesContainer != null) {
      this.repeatablesContainer.xmlElementCfgObject = this.xmlNode.cfgObject;
      this.repeatablesContainer.xmlParentElement = this.xmlNode.parentElement;
      this.xmlNode.repeatablesContainer = this.repeatablesContainer;
    }
  },
  
  /**
   * Makes a menu bar for the section within the section header.
   * @function 
   * @name makeControlMenu
   * @memberOf gxe.control.Section#
   * @param {gxe.control.SectionHeader} ctlHeader the section header
   * @param {boolean} bCheckForInfo if true then check for "g:info" attribute
   *   within the configuration object for the section control (for popup help)
   * @returns {gxe.control.SectionMenu} the section menu
   */
  makeControlMenu: function(ctlHeader,bCheckForInfo) {
    var ctlMenu = new gxe.control.SectionMenu();
    ctlMenu.autoConfigure(this.context,ctlHeader);
    ctlMenu.build(ctlHeader.htmlElement,null,null);
    if (bCheckForInfo) {
      var sInfo = gxe.cfg.getGxeAttributeValue(this.cfgObject,"info");
      if (sInfo != null) sInfo = dojo.trim(sInfo);
      if ((sInfo != null) && (sInfo.length > 0)) {
        var sImages = this.context.contextPath+"/catalog/images/";
        ctlMenu.appendImageButton("info",sImages+"gxe-info.png",sInfo);
        // TODO: implement a popup bubble,
        // use XHR to call the server to return the content of an i18n key
      }
    }
    return ctlMenu;
  }

});

/**
 * @class Supports the header portion of a UI section.
 * <br/><br/>Typically associated with:<br/>gpt/gxe/core/ui/Section.xml<br/>
 * @name gxe.control.SectionHeader
 * @extends gxe.control.Control
 */
dojo.provide("gxe.control.SectionHeader");
dojo.declare("gxe.control.SectionHeader",gxe.control.Control,{
});

/**
 * @class Supports a menu bar associated with the header portion of a UI section.
 * <br/><br/>This class is not currently associated with any editor definition files.<br/><br/>
 * @name gxe.control.SectionMenu
 * @extends gxe.control.Control
 */
dojo.provide("gxe.control.SectionMenu");
dojo.declare("gxe.control.SectionMenu",gxe.control.Control,{
  
  /**
   * Appends an image button to the menu bar.
   * @function 
   * @name appendImageButton
   * @memberOf gxe.control.SectionMenu#
   * @param {String} gxeEventName to associate with the click event of the new img element 
   * @param {String} sSrc the src for the new img element
   * @param {String} sTip the tool tip for the new img element
   */
  appendImageButton: function(gxeEventName,sSrc,sTip) {
    var el = document.createElement("img");
    el.alt = sTip;
    el.title = sTip;
    el.src = sSrc;
    el.setAttribute("gxeToolName",gxeEventName); 
    this.htmlElement.appendChild(el);
    dojo.connect(el,"onclick",this,dojo.hitch(this,function(e) { 
      this.onEvent(e,this,"onclick",gxeEventName);
    }));
    return el;
  },
  
  /**
   * Auto configures the control.
   * @function 
   * @name autoConfigure
   * @memberOf gxe.control.SectionMenu#
   * @param {gxe.Context} context the editor context 
   * @param {gxe.control.Control} parentControl the parent control
   */
  autoConfigure: function(context,parentControl) {
    var cfgAttr;
    var cfgObj = new Object();
    cfgObj.namespace = gxe.cfg.uriGxeHtml;
    cfgObj.name = "span";
    cfgObj.parent = parentControl.cfgObject;
    cfgObj.attributes = new Array();
    this.cfgObject = cfgObj;

    cfgAttr = new Object();
    cfgAttr.namespace = gxe.cfg.uriGxeHtml;
    cfgAttr.name = "class";
    cfgAttr.value = "gxeSectionMenu";
    cfgAttr.parent = cfgObj;
    cfgObj.attributes.push(cfgAttr);
    this.initialize(context,cfgObj);
  }

});

/**
 * @class Supports the body portion of a UI section.
 * <br/><br/>Typically associated with:<br/>gpt/gxe/core/ui/Section.xml<br/>
 * @name gxe.control.SectionBody
 * @extends gxe.control.Control
 */
dojo.provide("gxe.control.SectionBody");
dojo.declare("gxe.control.SectionBody",gxe.control.Control,{

  /** Override gxe.control.Control.build() */
  build: function(htmlParentElement,domProcessor,domNode) {
  
    var ctlContainer = this.parentControl.sectionBodyContainer;
    if ((typeof(ctlContainer) == "undefined") || (ctlContainer == null)) {
      var cfgParent = this.parentControl.cfgObject;
      var cfgChild = this.cfgObject;

      var cfgContainer = new Object();
      cfgContainer.namespace = gxe.cfg.uriGxeHtml;
      cfgContainer.name = "div";
      cfgContainer.parent = cfgParent;

      var cfgAttribute;
      cfgContainer.attributes = new Array();

      cfgAttribute = new Object();
      cfgAttribute.namespace = gxe.cfg.uriGxeHtml;
      cfgAttribute.name = "class";
      cfgAttribute.value = "gxeSectionBodyContainer";
      cfgAttribute.parent = cfgContainer;
      cfgContainer.attributes.push(cfgAttribute);
      
      cfgAttribute = new Object();
      cfgAttribute.namespace = gxe.cfg.uriGxe;
      cfgAttribute.name = "jsClass";
      cfgAttribute.value = "gxe.control.RepeatablesContainer";
      cfgAttribute.parent = cfgContainer;
      cfgContainer.attributes.push(cfgAttribute);
      
      ctlContainer = this.context.makeXhtmlControl(cfgContainer,this.parentControl,true);
      ctlContainer.isExclusive = true;
      ctlContainer.sectionBodyContainer = ctlContainer;
      ctlContainer.cfgSectionBody = this.cfgObject;
      this.parentControl.sectionBodyContainer = ctlContainer;
      ctlContainer.xmlNode = this.xmlNode;
      ctlContainer.xmlParentElement = this.xmlNode.parentElement;
      
      ctlContainer.build(htmlParentElement,domProcessor,domNode);
      var elHtmlParent = ctlContainer.htmlElement;
      cfgContainer.children = new Array();
      cfgContainer.children.push(this.cfgObject);

      this.parentControl = ctlContainer;
      this.execBuild(elHtmlParent,domProcessor,domNode);
    } else {
      this.inherited(arguments);
    }
    
  },

  /** Override gxe.control.Control.ensureVisibility() */
  ensureVisibility: function(subjectXmlNode) {
    if (this.parentControl.sectionBodyContainer != null) {
      var el = this.parentControl.sectionBodyContainer.htmlElement;
      if (el.style.display == "none") {
        this.parentControl.sectionBodyContainer.toggleDisplay();
      }
    }
    this.inherited(arguments);
  }

});

/**
 * @class Supports a UI section.
 * @name gxe.control.Parameter
 * @extends gxe.control.Section
 * @deprecated use gxe.control.Section instead
 */
dojo.provide("gxe.control.Parameter");
dojo.declare("gxe.control.Parameter",gxe.control.Section,{
});

/**
 * @class Supports a UI section associated with a targeted XML element.
 * <br/><br/>Typically associated with:
 * <br/>gpt/gxe/core/xml/Element.xml
 * <br/>gpt/gxe/core/xml/ElementTextOnly.xml
 * <br/>
 * @name gxe.control.Element
 * @extends gxe.control.Section
 */
dojo.provide("gxe.control.Element");
dojo.declare("gxe.control.Element",gxe.control.Section,{
  multiplicityTools: null,
  
  /** Override gxe.control.Control.onHtmlChildrenCreated() */
  onHtmlChildrenCreated: function(domProcessor,domNode) {
    this.inherited(arguments);
    
    var ctlMenu = null;
    var ctlIndexedIabArray = null;
    var sImages = this.context.contextPath+"/catalog/images/";
    var ctlHeader = this.findFirstChildControl(">[gxename='header']");
    if (ctlHeader != null) {
      
      var bBuildRepeatables = false;
      if ((this.xmlNode != null) && this.xmlNode.isRepeatable()) {
        bBuildRepeatables = true;
        var ctlInput = this.xmlNode.getInputControl();
        if ((ctlInput != null) && ctlInput.getSupportsMultipleValues()) {
          bBuildRepeatables = false;
        } else if (this.xmlNode.wrapsIsoMultiValueList()) {
          bBuildRepeatables = false;
        }
      }
          
      if (bBuildRepeatables) {
        ctlMenu = this.makeControlMenu(ctlHeader,true);
        
        ctlMenu.appendImageButton("repeatSection",sImages+"gxe-repeat.png",
            this.context.getI18NString("button.repeatSection"));
        ctlMenu.appendImageButton("removeSection",sImages+"gxe-remove.png",
            this.context.getI18NString("button.removeSection"));
        if (this.useExclusiveDisplay) {
          ctlMenu.appendImageButton("moveSectionUp",sImages+"gxe-move-left.png",
              this.context.getI18NString("button.moveSectionLeft"));
          ctlMenu.appendImageButton("moveSectionDown",sImages+"gxe-move-right.png",
              this.context.getI18NString("button.moveSectionRight"));
        } else {
          ctlMenu.appendImageButton("moveSectionUp",sImages+"gxe-move-up.png",
              this.context.getI18NString("button.moveSectionUp"));
          ctlMenu.appendImageButton("moveSectionDown",sImages+"gxe-move-down.png",
              this.context.getI18NString("button.moveSectionDown"));
        }
        dojo.connect(ctlMenu,"onEvent",this,"onHeaderEvent");
        this.multiplicityTools = ctlMenu;
        
        var ctlRepeatables = this.repeatablesContainer;
        if (ctlRepeatables != null) ctlRepeatables.multiplicityTools = ctlMenu;
        if (this.useExclusiveDisplay && (ctlRepeatables != null)) {
          ctlIndexedIabArray = new gxe.control.IndexedTabArray();
          ctlIndexedIabArray.autoConfigure(this.context,ctlHeader);
          ctlIndexedIabArray.build(ctlHeader.htmlElement,domProcessor,domNode);
          dojo.connect(ctlRepeatables,"onArrayModified",ctlIndexedIabArray,"synchronize");
          dojo.connect(ctlIndexedIabArray,"onTabClicked",ctlRepeatables,"activateIndex");
          dojo.connect(ctlIndexedIabArray,"onTabClicked",ctlRepeatables,"syncTools");
        }
        if (ctlRepeatables != null) ctlRepeatables.syncTools();
        
      }
    }
    
    this.initializeLabelEvents(this.xmlNode,ctlMenu,ctlIndexedIabArray,domProcessor,domNode);
  }

});

/**
 * @class Supports a UI section associated with a targeted XML attribute.
 * <br/><br/>Typically associated with:<br/>gpt/gxe/core/xml/Attribute.xml<br/>
 * @name gxe.control.Attribute
 * @extends gxe.control.Parameter
 */
dojo.provide("gxe.control.Attribute");
dojo.declare("gxe.control.Attribute",gxe.control.Parameter,{
  
  /** Override gxe.control.Control.onHtmlChildrenCreated() */
  onHtmlChildrenCreated: function(domProcessor,domNode) {
    this.inherited(arguments);
    this.initializeLabelEvents(this.xmlNode,null,null,domProcessor,domNode);
  }
});

/**
 * @class Supports the display of a label associated with a targeted XML node (element or attribute).
 * <br/><br/>Typically associated with:
 * <br/>gpt/gxe/core/ui/Section.xml
 * <br/>gpt/gxe/core/ui/TargetLabel.xml
 * <br/>
 * @name gxe.control.TargetLabel
 * @extends gxe.control.Control
 */
dojo.provide("gxe.control.TargetLabel");
dojo.declare("gxe.control.TargetLabel",gxe.control.Control,{

  /** Override gxe.control.Control.createHtmlElement() */
  createHtmlElement: function() {
    var s = this.htmlTextContent;
    if ((typeof(s) == "undefined") || (s == null) || (s.length == 0)) {
      this.htmlTextContent = this.getLabelText();
    }
    this.inherited(arguments);
  }

});

/**
 * @class Supports the display of an exclusive element choice.
 * <br/><br/>Typically associated with:<br/>gpt/gxe/core/xml/ElementChoice.xml<br/>
 * @name gxe.control.ElementChoice
 * @extends gxe.control.Control
 */
dojo.provide("gxe.control.ElementChoice");
dojo.declare("gxe.control.ElementChoice",gxe.control.Control,{
  choiceBody: null,

  /**
   * Determines if an XML element is associated with the selected choice.
   * <br/>The anchor is enabled when text has been entered into the associated input text box.
   * @function 
   * @name isElementSelected
   * @memberOf gxe.control.ElementChoice#
   * @param {gxe.xml.XmlElement} xmlElement the XML element to test
   * @return {boolean} true if the XML element is selected
   */
  isElementSelected: function(xmlElement) {
    if (this.choiceBody != null) {
      var nActiveIndex = this.choiceBody.getActiveIndex();
      if (nActiveIndex >= 0) {
        var item = this.choiceBody.getItem(nActiveIndex);
        return (item.xmlNode == xmlElement);
      }
    }
    return false;
  },

  /** Override gxe.control.Control.onHtmlChildrenCreated() */
  onHtmlChildrenCreated: function(domProcessor,domNode) {

    var bAllowNone = (gxe.cfg.getMinOccurs(this.cfgObject) == "0");
    var sLabel = gxe.cfg.getGxeAttributeValue(this.cfgObject,"label");
    var sLabelNone = gxe.cfg.getGxeAttributeValue(this.cfgObject,"labelNone");
    if (sLabelNone == null) sLabelNone = "??none??";

    // find the component controls, build the tabs based upon the content of the body
    var ctlHeader = this.findFirstChildControl("[gxename='header']");
    if (ctlHeader != null) {
      var ctlTabArray = ctlHeader.findFirstChildControl("[gxename='tabArray']");
      if (ctlTabArray != null) {
        var ctlBody = this.findFirstChildControl("[gxename='body']");
        if (ctlBody != null) {
          this.choiceBody = ctlBody;
          dojo.connect(ctlTabArray,"onTabClicked",ctlBody,"activateIndex");

          var nSelectedIndex = ctlBody.defaultSelectedIndex;
          if (typeof(nSelectedIndex) != "number") nSelectedIndex = -1;
          if (bAllowNone) {
            //ctlTabArray.appendTab(-1,sLabelNone,(nSelectedIndex == -1));
            ctlTabArray.appendRadio(-1,sLabelNone,(nSelectedIndex == -1));
          } 
          
          var n = ctlBody.getLength();
          for (var i=0; i<n; i++) {
            var bIsSelected = false;
            if (nSelectedIndex != -1) bIsSelected = (i == nSelectedIndex);
            else if (!bAllowNone && (i == 0)) bIsSelected = true;
            var ctl = ctlBody.getItem(i);
            var sLabel = ctl.getLabelText();
            //ctlTabArray.appendTab(i,sLabel,bIsSelected);
            ctlTabArray.appendRadio(i,sLabel,bIsSelected);
            ctl.xmlNode.exclusiveChoiceControl = this;
          }
        }
      }
    }
  }

});

/**
 * @class Supports the body section associated with an exclusive element choice.
 * <br/><br/>Typically associated with:<br/>gpt/gxe/core/xml/ElementChoice.xml<br/>
 * @name gxe.control.ElementChoiceBody
 * @extends gxe.control.ExclusiveControlArray
 */
dojo.provide("gxe.control.ElementChoiceBody");
dojo.declare("gxe.control.ElementChoiceBody",gxe.control.ExclusiveControlArray,{
  defaultSelectedIndex: -1,

  /** Override gxe.control.Control.processChildren() */
  processChildren: function(cfgObject,htmlParentElement,xmlNode,domProcessor,domNode) {
  
    // Don't process all the children we only want the child "element"s
    var nIndex = -1;
    var nSelectedIndex = -1;
    gxe.cfg.forEachChild(this.cfgObject,"*","*",dojo.hitch(this,function(cfgChild) {
      if (cfgChild.namespace == gxe.cfg.uriGxe) {
        if (cfgChild.name == "element") {
          nIndex++;
          if (nSelectedIndex == -1) {
            if ((domProcessor != null) && (domNode != null)) {
              var domMatch = domProcessor.findMatchingChildElement(domNode,cfgChild);
              if (domMatch != null) nSelectedIndex = nIndex;
            } else {
              var sSelected = gxe.cfg.getGxeAttributeValue(cfgChild,"selected");
              if (sSelected == "true") nSelectedIndex = nIndex;
            }
          }
        }
      }
    }));
    this.defaultSelectedIndex = nSelectedIndex;

    gxe.cfg.forEachChild(this.cfgObject,"*","*",dojo.hitch(this,function(cfgChild) {
      if (cfgChild.namespace == gxe.cfg.uriGxe) {
        if (cfgChild.name == "element") {
          var elTmp = document.createElement("div");
          this.processCfgElement(cfgChild,elTmp,this.xmlNode,domProcessor,domNode);
        } 
      }
    }));
  }

});

/**
 * @class Supports the display of a set of tabs.
 * <br/><br/>Typically associated with:<br/>gpt/gxe/core/ui/Tabs.xml<br/>
 * @name gxe.control.Tabs
 * @extends gxe.control.Control
 */
dojo.provide("gxe.control.Tabs");
dojo.declare("gxe.control.Tabs",gxe.control.Control,{
  tabArray: null,
  tabsBody: null,

  /** Override gxe.control.Control.ensureVisibility() */
  ensureVisibility: function(subjectXmlNode) {
    if ((this.tabArray != null) && (this.tabsBody != null)) {
      var nIndex = this.tabsBody.findIndexByXmlNode(subjectXmlNode);
      if ((nIndex != -1) && (nIndex != this.tabsBody.getActiveIndex())) {
        this.tabArray.activateIndex(nIndex);
      }
    }
    this.inherited(arguments);
  },

  /** Override gxe.control.Control.onHtmlChildrenCreated() */
  onHtmlChildrenCreated: function(domProcessor,domNode) {

    // find the component controls, build the tabs based upon the content of the body
    var ctlHeader = this.findFirstChildControl("[gxename='header']");
    if (ctlHeader != null) {
      var ctlTabArray = ctlHeader.findFirstChildControl("[gxename='tabArray']");
      if (ctlTabArray != null) {
        this.tabArray = ctlTabArray;
        var ctlBody = this.findFirstChildControl("[gxename='body']");
        if (ctlBody != null) {
          this.tabsBody = ctlBody;
          dojo.connect(ctlTabArray,"onTabClicked",ctlBody,"activateIndex");
          var nSelectedIndex = -1;
          var n = ctlBody.getLength();
          for (var i=0; i<n; i++) {
            var ctl = ctlBody.getItem(i);
            var sLabel = ctl.getLabelText();
            var sSelected = gxe.cfg.getGxeAttributeValue(ctl.cfgObject,"selected");
            if (sSelected == "true") nSelectedIndex = i;
            ctlTabArray.appendTab(i,sLabel,(nSelectedIndex == i));
          }
          if ((n > 0) && (nSelectedIndex == -1)) ctlTabArray.activateIndex(0);
        }
      }
    }
  }

});

/**
 * @class Supports the body section associated with a set of tabs.
 * <br/><br/>Typically associated with:<br/>gpt/gxe/core/ui/Tabs.xml<br/>
 * @name gxe.control.TabsBody
 * @extends gxe.control.ExclusiveControlArray
 */
dojo.provide("gxe.control.TabsBody");
dojo.declare("gxe.control.TabsBody",gxe.control.ExclusiveControlArray,{
});


/* Input controls ==================================================== */

/**
 * @class Base class for all input controls.
 * @name gxe.control.InputBase
 * @extends gxe.control.Control
 * @property {boolean} supportsMultipleValues indicates whether or not the control supports
 *   the input of multiple values
 */
dojo.provide("gxe.control.InputBase");
dojo.declare("gxe.control.InputBase",gxe.control.Control,{
  supportsMultipleValues: false,

  /**
   * Attempts to find label text associated with the input control.
   * The label text is used for validation feedback within the gxe.control.MessageArea 
   * portion of the page.
   * @function 
   * @name findParentLabelText
   * @memberOf gxe.control.InputBase#
   * @param {gxe.xml.XmlNode} xmlNode the targeted XML node for input control
   * @return {String} the label text
   */
  findParentLabelText: function(xmlNode) {
    var ctl = this.parentControl;
    while ((typeof(ctl) != "undefined") && (ctl != null)) {
      var s = ctl.uiLabelText;
      if ((typeof(s) != "undefined") && (s != null)) return s;
      ctl = ctl.parentControl;
    }
    return "?"+xmlNode.nodeInfo.localName;
  },
  
  /**
   * Fires the onInputChanged() event.
   * @function 
   * @name fireInputChanged
   * @memberOf gxe.control.InputBase#
   * @param {Event} e the underlying browser event
   */  
  fireInputChanged: function(e) {
    this.onInputChanged(this);
  },

  /**
   * Fires the onInputChanged() event based upon a browser onkeyup() event.
   * The onInputChanged() event will only be fired if the user key is 
   * not 13 (carriage return) and not 9 (tab).
   * @function 
   * @name fireInputChangedOnKeyUp
   * @memberOf gxe.control.InputBase#
   * @param {Event} e the underlying browser event
   */  
  fireInputChangedOnKeyUp: function(e) {
    if (!e) e = window.event;
    if (e) {
      var nKey = (e.keyCode) ? e.keyCode : e.which;
      // ignore carriage return and tab
      if ((nKey != 13) && (nKey != 9)) this.fireInputChanged(e);
    }
  },
  
  /**
   * Gets the value associated with the input control.
   * This method should be overridden for all sub-classes that support single valued
   * input (i.e. where this.supportsMultipleValues == false).
   * @function 
   * @name getInputValue
   * @memberOf gxe.control.InputBase#
   * @param {boolean} bInFeedbackMode true if the value is being requested as validation feedback
   * @return {Object} the input value
   */  
  getInputValue: function(bInFeedbackMode) {return null;},
  
  /**
   * Gets the values associated with the input control.
   * This method should be overridden for all sub-classes that support multi-valued
   * input (i.e. where this.supportsMultipleValues == true).
   * @function 
   * @name getInputValues
   * @memberOf gxe.control.InputBase#
   * @param {boolean} bInFeedbackMode true if the value is being requested as validation feedback
   * @return {Object[]} the input values
   */  
  getInputValues: function(bInFeedbackMode) {return null;},

  /**
   * Indicates whether or not the control supports the input of multiple values.
   * (simple wrapper for this.supportsMultipleValues)
   * @function 
   * @name getSupportsMultipleValues
   * @memberOf gxe.control.InputBase#
   * @return {boolean} true if multi-valued input is supported
   */ 
  getSupportsMultipleValues: function() {return this.supportsMultipleValues;},

  /**
   * Makes an HTML "input" element of type "text" supporting entry of "other" code values.
   * This function is useful when the user requires the ability to enter a value outside
   * of a coded domain.
   * @function 
   * @name makeOtherInputText
   * @memberOf gxe.control.InputBase#
   * @param {Object} cfgOption the JSON configuration object associated with the input control
   * @return {Element} the HTML "input" element
   */ 
  makeOtherInputText: function(cfgOption) {
    var elOther = document.createElement("input");
    elOther.setAttribute("type","text");
    gxe.cfg.forEachHtmlAttribute(cfgOption,dojo.hitch(this,function(cfgAttribute) {
      var sName = cfgAttribute.name.toLowerCase();
      var sValue = cfgAttribute.name.toLowerCase();
      if ((sName == "maxlength") || (sName != "size")) {
        elOther.setAttribute(sName,cfgAttribute.value);
      }
    }));
    return elOther;
  },

  /** Override gxe.control.onHtmlChildrenCreated() */
  onHtmlChildrenCreated: function(domProcessor,domNode) {
    this.inherited(arguments);
    var sTip = gxe.cfg.getGxeAttributeValue(this.cfgObject,"tip");
    if (sTip == null) sTip = gxe.cfg.getGxeAttributeValue(this.xmlNode.cfgObject,"tip");
    if (sTip == null) {
      var sMinOccurs = gxe.cfg.getGxeAttributeValue(this.xmlNode.cfgObject,"minOccurs");
      if (sMinOccurs == "$parent") {
        var pe = this.xmlNode.parentElement;
        if (pe != null) sTip = gxe.cfg.getGxeAttributeValue(pe.cfgObject,"tip");
      }
    }
    /*
    if (sTip == null) {
      var sType = gxe.cfg.getGxeAttributeValue(this.xmlNode.cfgObject,"valueType");
      if (sType != null) sType = dojo.trim(sType);
      if ((sType == "date") || (sType == "xs:date") || (sType == "xsd:date")) {
      }
    }
    */
    if (sTip != null) this.htmlElement.title = sTip;
  },


  /**
   * An event fired when input has changed.
   * @event 
   * @name onInputChanged
   * @memberOf gxe.control.InputBase#
   * @param {Object} inputControl the input control that initiated the change
   */ 
  onInputChanged: function(inputControl) {}

});

/**
 * @class Supports the input of multiple values through a delimited "textarea" control.
 * <br/><br/>Typically associated with:<br/>gpt/gxe/core/ui/InputDelimitedTextArea.xml<br/>
 * @name gxe.control.InputDelimitedTextArea
 * @extends gxe.control.InputBase
 * @property {boolean} supportsMultipleValues true
 * @property {String} delimiter the delimiter (default="," configuration attribute g:delimiter)
 */
dojo.provide("gxe.control.InputDelimitedTextArea");
dojo.declare("gxe.control.InputDelimitedTextArea",gxe.control.InputBase,{
  delimiter: ",",
  supportsMultipleValues: true,

  /** Override gxe.control.Control.initialize() */
  initialize: function(context,cfgObject) {
    this.inherited(arguments);
    var s = gxe.cfg.getGxeAttributeValue(cfgObject,"delimiter");
    if ((s != null) && (s.length > 0)) this.delimiter = s;
  },

  /** Override gxe.control.InputBase.getInputValues() */
  getInputValues: function(bInFeedbackMode) {
    if ((this.delimiter == null) || (this.delimiter.length == 0)) this.delimiter = ",";
    var values = new Array();
    if (this.htmlElement != null) this._mergeTokens(values,this.htmlElement.value);
    return values;
  },

  /**
   * Tokenizes a supplied value and merges the result into a supplied array.
   * <br/>The supplied value will be split using this.delimiter plus characters: \r and \n 
   * @function 
   * @name _mergeTokens
   * @memberOf gxe.control.InputDelimitedTextArea#
   * @param {Array} values the values into which the tokens will be merged
   * @param {String} sValue the value that will be tokenized (using this.delimiter)
   */
  _mergeTokens: function(values,sValue) {
    if (sValue != null) {
      sValue = sValue.replace(/(\r\n|\r|\n|\n\r)/g,this.delimiter);
      var tokens = sValue.split(this.delimiter);
      if (tokens != null) {
        for (var i=0; i<tokens.length; i++) {
          var sToken = dojo.trim(tokens[i]);
          if (sToken.length > 0) values.push(sToken);
        }
      }
    }
  },

  /** Override gxe.control.Control.onHtmlElementCreated() */
  onHtmlElementCreated: function(domProcessor,domNode) {
    this.xmlNode.setInputControl(this);

    // TODO set a default value?

    if ((this.delimiter == null) && (this.delimiter.length == 0)) this.delimiter = ",";
    var domValues = new Array();
    if ((domProcessor != null) && (domNode != null)) {
      if (this.xmlNode.nodeInfo.isIsoWrappedMultiValueList) {
        var domParentMatches = domProcessor.findMatchingChildElements(
            domNode.parentNode.parentNode,this.xmlNode.parentElement.cfgObject);
        if (domParentMatches != null) {
          for (var i=0; i<domParentMatches.length; i++) {
            var domMatches = domProcessor.findMatchingChildElements(
                domParentMatches[i],this.xmlNode.cfgObject);
            if (domMatches != null) {
              for (var j=0; j<domMatches.length; j++) {
                var domMatch = domMatches[j];
                var sValue = domProcessor.getNodeText(domMatch);
                if (sValue != null) domValues.push(sValue);
              }
            }
          }
        }
      } else {
        var domMatches = domProcessor.findMatchingChildElements(
            domNode.parentNode,this.xmlNode.cfgObject);
        if (domMatches != null) {
          for (var i=0; i<domMatches.length; i++) {
            var domMatch = domMatches[i];
            var sValue = domProcessor.getNodeText(domMatch);
            if (sValue != null) domValues.push(sValue);
          }
        }
      }
    }   
    
    if (domValues.length > 0) {
      var values = new Array();
      for (var i=0; i<domValues.length; i++) {
        this._mergeTokens(values,domValues[i]);
      }
      if (values.length > 0) {
        var sValues = "";
        for (var i=0; i<values.length; i++) {
          if (sValues.length > 0) sValues += this.delimiter;
          sValues += values[i];
        }
        this.htmlElement.value = sValues;
      }
    }    
    
    dojo.connect(this.htmlElement,"onchange",this,"fireInputChanged");
    dojo.connect(this.htmlElement,"onkeyup",this,"fireInputChangedOnKeyUp");
  }
});

/**
 * @class Supports the input of multiple values through a collection of check boxes.
 * <br/><br/>Typically associated with:<br/>gpt/gxe/core/ui/InputSelectMany.xml<br/>
 * @name gxe.control.InputSelectMany
 * @extends gxe.control.InputBase
 * @property {boolean} supportsMultipleValues true
 */
dojo.provide("gxe.control.InputSelectMany");
dojo.declare("gxe.control.InputSelectMany",gxe.control.InputBase,{
  _checkBoxes: null,
  supportsMultipleValues: true,

  /**
   * Appends a checkbox option to the parent HTML DOM element.
   * @function 
   * @name _appendCheckBox
   * @memberOf gxe.control.InputSelectMany#
   * @param {String} sLabel the label
   * @param {String} sValue a value associated with the checkbox
   * @param {boolean} bSelected true if the box should be checked
   */
  _appendCheckBox: function(sLabel,sValue,bSelected) {
    var elListItem = document.createElement("li");
    this.htmlElement.appendChild(elListItem);
    
    var sCollectionName = this.gxeId+"Options";
    var sOptionId = this.context.generateUniqueId();
    var elOption = document.createElement("input");
    if (this._dataName != null) {
      if(dojo.isIE <= 8) {
        elOption = document.createElement("<input name=\""+sCollectionName+"\"/>");
      } else {
        elOption.setAttribute("name",sCollectionName);
      }
    }
    elOption.setAttribute("type","checkbox");
    elOption.setAttribute("id",sOptionId);
    elOption.setAttribute("value",sValue); 
    elListItem.appendChild(elOption);
    if (bSelected) elOption.setAttribute("checked","checked");
    this._checkBoxes.push(elOption);
    
    var elLabel = document.createElement("label");
    elLabel.setAttribute("for",sOptionId);
    elLabel.appendChild(document.createTextNode(sLabel));
    elListItem.appendChild(elLabel);
    return elListItem;
  },

  /** Override gxe.control.Control.focus() */
  focus: function(subjectXmlNode) {
    this.inherited(arguments);
    if ((this._checkBoxes != null) && (this._checkBoxes.length > 0)) {
      this._checkBoxes[0].focus();
    }
  },

  /** Override gxe.control.InputBase.getInputValues() */
  getInputValues: function(bInFeedbackMode) {
    var values = new Array();
    if (this._checkBoxes != null) {
      var n = this._checkBoxes.length;
      for (var i=0; i<n; i++) {
        var elCheckBox = this._checkBoxes[i];
        if (elCheckBox.checked) {
          var oValue = elCheckBox.value;
          var elOther = elCheckBox.gxeOtherInputText;
          if (elOther != null) {
            var sValue = dojo.trim(elOther.value);
            if (sValue != elOther.value) elOther.value = sValue;
            if (sValue.length > 0) oValue = sValue;
            else oValue = null;
          }
          if (oValue != null) values.push(oValue);
        }
      }
    }
    return values;
  },

  /** Override gxe.control.Control.onHtmlElementCreated() */
  onHtmlElementCreated: function(domProcessor,domNode) {
    this.xmlNode.setInputControl(this);
    this._checkBoxes = new Array();
    var cfgInput = this.cfgObject;

    var otherElements = new Array();
    var otherCheckBoxes = new Array();
    var knownValues = new Array();
    var bUseDomValuesForSelected = (domProcessor != null);
    var domValues = new Array();
    if ((domProcessor != null) && (domNode != null)) {
      if (this.xmlNode.nodeInfo.isIsoWrappedMultiValueList) {
        var domParentMatches = domProcessor.findMatchingChildElements(
            domNode.parentNode.parentNode,this.xmlNode.parentElement.cfgObject);
        if (domParentMatches != null) {
          for (var i=0; i<domParentMatches.length; i++) {
            var domMatches = domProcessor.findMatchingChildElements(
                domParentMatches[i],this.xmlNode.cfgObject);
            if (domMatches != null) {
              for (var j=0; j<domMatches.length; j++) {
                var domMatch = domMatches[j];
                var sValue = domProcessor.getNodeText(domMatch);
                if (sValue != null) domValues.push(sValue);
              }
            }
          }
        }
      } else {
        var domMatches = domProcessor.findMatchingChildElements(
            domNode.parentNode,this.xmlNode.cfgObject);
        if (domMatches != null) {
          for (var i=0; i<domMatches.length; i++) {
            var domMatch = domMatches[i];
            var sValue = domProcessor.getNodeText(domMatch);
            if (sValue != null) domValues.push(sValue);
          }
        }
      }
    }

    var cfgOptions = gxe.cfg.findChild(this.cfgObject,gxe.cfg.uriGxe,"options");
    if (cfgOptions != null) {
      gxe.cfg.forEachChild(cfgOptions,gxe.cfg.uriGxe,"option",dojo.hitch(this,function(cfgOption) {
        var sLabel = gxe.cfg.getGxeAttributeValue(cfgOption,"label");
        var sValue =  gxe.cfg.getGxeAttributeValue(cfgOption,"value");
        var sAlias =  gxe.cfg.getGxeAttributeValue(cfgOption,"alias");
        var sSelected = gxe.cfg.getGxeAttributeValue(cfgOption,"selected");
        var bSelected = false;
        if (!bUseDomValuesForSelected) {
          bSelected = (sSelected == "true");
        } else {
          knownValues.push(sValue);
          for (var i=0; i<domValues.length; i++) {
            if (sValue == domValues[i]) {
              bSelected = true;
              break;
            } else if (sAlias != null) {
              if (sAlias == domValues[i]) {
                bSelected = true;
                break;
              }
            }
          }
        }
        var elListItem = this._appendCheckBox(sLabel,sValue,bSelected);
        var elCheckBox = this._checkBoxes[this._checkBoxes.length - 1];
        var sOther = gxe.cfg.getGxeAttributeValue(cfgOption,"isOther");
        if ((sOther != null) && (sOther == "true")) {
          var elOther = this.makeOtherInputText(cfgOption);
          elCheckBox.gxeOtherInputText = elOther;
          elListItem.appendChild(elOther);
          if (bUseDomValuesForSelected) {
            otherElements.push(elOther);
            otherCheckBoxes.push(elCheckBox);
          }
          dojo.connect(elOther,"onchange",this,"fireInputChanged");
          dojo.connect(elOther,"onkeyup",this,"fireInputChangedOnKeyUp");
        }
        dojo.connect(elCheckBox,"onchange",this,"fireInputChanged");
      }));
    }

    if (bUseDomValuesForSelected && (domValues.length > 0) && (otherElements.length > 0)) {
      var unknownValues = new Array();
      for (var i=0; i<domValues.length; i++) {
        var domValue = domValues[i];
        var bKnown = false;
        for (var j=0; j<knownValues.length; j++) {
          if (domValue == knownValues[j]) {bKnown = true;break;}
        }
        if (!bKnown) unknownValues.push(domValue);
      }
      for (var i=0; i<unknownValues.length; i++) {
        if (otherElements.length > i) {
          otherElements[i].value = unknownValues[i];
          otherCheckBoxes[i].checked = true;
        }
      }
    }
  }

});

/**
 * @class Supports the input of a single value through a drop down list ("select").
 * <br/><br/>Typically associated with:<br/>gpt/gxe/core/ui/InputSelectOne.xml<br/>
 * @name gxe.control.InputSelectOne
 * @extends gxe.control.InputBase
 * @property {boolean} supportsMultipleValues false
 * @property {Element} _htmlOther reference to the "other" code input text box
 */
dojo.provide("gxe.control.InputSelectOne");
dojo.declare("gxe.control.InputSelectOne",gxe.control.InputBase,{
  _htmlOther: null,
  supportsMultipleValues: false,

  /** Override gxe.control.InputBase.getInputValue() */
  getInputValue: function(bInFeedbackMode) {
    if ((this.htmlElement != null) && (this.htmlElement.selectedIndex != null) &&
        (this.htmlElement.selectedIndex >= 0)) {
      var elOptions = this.htmlElement.options;
      var elOption = elOptions[this.htmlElement.selectedIndex];
      if ((this._htmlOther != null) && (elOption.value == this._htmlOther.gxeOptionValue)) {
        var sValue = dojo.trim(this._htmlOther.value);
        if (!bInFeedbackMode) {
          if (sValue != this._htmlOther.value) this._htmlOther.value = sValue;
        }
        if (sValue.length > 0) return sValue;
      } else {
        return elOption.value;
      }
    }
    return null;
  },

  /**
   * Catches "onchange" events for the HTML "select" element.
   * <br/>This method triggers the firing of the inputChanged() event.
   * <br/>If applicable, this method toggles the display of the 
   * "other" code input text box.
   * @function 
   * @name _onChange
   * @memberOf gxe.control.InputSelectOne#
   * @param {Event} e the underlying browser event
   */
  _onChange: function(e) {
    if (this._htmlOther != null) {
      var elOption = this.htmlElement.options[this.htmlElement.selectedIndex];
      if (elOption.value == this._htmlOther.gxeOptionValue) {
        this._htmlOther.style.display = "inline";
      } else {
        this._htmlOther.style.display = "none";
      }
    }
    this.fireInputChanged(e);
  },

  /** Override gxe.control.Control.onHtmlElementCreated() */
  onHtmlElementCreated: function(domProcessor,domNode) {
    this.xmlNode.setInputControl(this);

    var sDomValue = null;
    var bUseDomValueForSelected = (domProcessor != null);
    if ((domProcessor != null) && (domNode != null)) {
      sDomValue = domProcessor.getNodeText(domNode);
    }
    var elOptions = this.htmlElement.options;
    var bFoundSelected = false;
    var nIndex = -1;
    var cfgOptions = gxe.cfg.findChild(this.cfgObject,gxe.cfg.uriGxe,"options");
    
    if (cfgOptions != null) {
      gxe.cfg.forEachChild(cfgOptions,gxe.cfg.uriGxe,"option",dojo.hitch(this,function(cfgOption) {
        nIndex++;
        var sLabel = gxe.cfg.getGxeAttributeValue(cfgOption,"label");
        var sValue = gxe.cfg.getGxeAttributeValue(cfgOption,"value");
        var sAlias =  gxe.cfg.getGxeAttributeValue(cfgOption,"alias");
        var bSelected = false;
        
        if (!bFoundSelected) {
          if (!bUseDomValueForSelected) {
            var sSelected = gxe.cfg.getGxeAttributeValue(cfgOption,"selected");
            bSelected = (sSelected == "true");
          } else {
            bSelected = (sValue == sDomValue);
            if (!bSelected && (sAlias != null)) {
              bSelected = (sAlias == sDomValue);
            }
          }
          if (bSelected) bFoundSelected = true;
        }
        
        var elOption = new Option(sLabel,sValue,bSelected,bSelected);
        elOptions[elOptions.length] = elOption;
  
        if (this._htmlOther == null) {
          var sOther = gxe.cfg.getGxeAttributeValue(cfgOption,"isOther");
          if ((sOther != null) && (sOther == "true")) {
            this._htmlOther = this.makeOtherInputText(cfgOption);
            this._htmlOther.gxeOptionValue = sValue;
            this._htmlOther.gxeOptionIndex = nIndex;
            if (!bSelected) this._htmlOther.style.display = "none";
            this.htmlElement.parentNode.appendChild(this._htmlOther);
          }
        }
      }));
    }
    
    if (!bFoundSelected && (elOptions.length > 0)) {
      var nSelectedIndex = 0;
      if (bUseDomValueForSelected && (sDomValue != null) && (this._htmlOther != null)) {
        nSelectedIndex = this._htmlOther.gxeOptionIndex;
        this._htmlOther.value = sDomValue;
      }
      this.htmlElement.selectedIndex = nSelectedIndex;
      this._onChange();
    }
    dojo.connect(this.htmlElement,"onchange",this,"_onChange");
    if (this._htmlOther != null) {
      dojo.connect(this._htmlOther,"onchange",this,"fireInputChanged");
      dojo.connect(this._htmlOther,"onkeyup",this,"fireInputChangedOnKeyUp");
    }
  }
  
});

/**
 * @class Supports the input of a single value through a text box ("input" type="text").
 * <br/><br/>Typically associated with:<br/>gpt/gxe/core/ui/InputText.xml<br/>
 * @name gxe.control.InputText
 * @extends gxe.control.InputBase
 * @property {boolean} supportsMultipleValues false
 */
dojo.provide("gxe.control.InputText");
dojo.declare("gxe.control.InputText",gxe.control.InputBase,{
  supportsMultipleValues: false,

  /** Override gxe.control.InputBase.getInputValue() */
  getInputValue: function(bInFeedbackMode) {
    if ((this.htmlElement != null) && (this.htmlElement.value != null)) {
      var sValue = dojo.trim(this.htmlElement.value);
      if (!bInFeedbackMode) {
        if (sValue != this.htmlElement.value) this.htmlElement.value = sValue;
      }
      if (sValue.length > 0) return sValue;
    }
    return null;
  },

  /** Override gxe.control.Control.importHtmlAttributes() */
  importHtmlAttributes: function(cfgObject) {
    this.inherited(arguments);
    this.htmlTag = "input";
    this.htmlAttributes.set("type","text");
  },

  /** Override gxe.control.Control.onHtmlElementCreated() */
  onHtmlElementCreated: function(domProcessor,domNode) {
    this.xmlNode.setInputControl(this);
    var sValue = gxe.cfg.getGxeAttributeValue(this.cfgObject,"fixedValue");
    if (sValue == null) {
      sValue = gxe.cfg.getGxeAttributeValue(this.xmlNode.cfgObject,"fixedValue");
    }
    if (sValue == null) {
      if ((domProcessor != null) && (domNode != null)) {
        sValue = domProcessor.getNodeText(domNode);
      } else if (domProcessor == null) {
        sValue = gxe.cfg.getGxeAttributeValue(this.cfgObject,"value");
        if (sValue == null) sValue = gxe.cfg.getGxeAttributeValue(this.xmlNode.cfgObject,"value");
      }
    }
    if (sValue != null) {
      var sType = gxe.cfg.getGxeAttributeValue(this.xmlNode.cfgObject,"valueType");
      if (sType != null) sType = dojo.trim(sType);
      if (sType == "fgdc:date") sValue = sValue.replace(/-/g,"");
      this.htmlElement.value = sValue;
    }
    dojo.connect(this.htmlElement,"onchange",this,"fireInputChanged");
    dojo.connect(this.htmlElement,"onkeyup",this,"fireInputChangedOnKeyUp");
  }
  
});

/**
 * @class Supports the input of a single value through a "textarea" control.
 * <br/><br/>Typically associated with:<br/>gpt/gxe/core/ui/InputTextArea.xml<br/>
 * @name gxe.control.InputTextArea
 * @extends gxe.control.InputBase
 * @property {boolean} supportsMultipleValues false
 */
dojo.provide("gxe.control.InputTextArea");
dojo.declare("gxe.control.InputTextArea",gxe.control.InputBase,{
  supportsMultipleValues: false,

  /** Override gxe.control.InputBase.getInputValue() */
  getInputValue: function(bInFeedbackMode) {
    if ((this.htmlElement != null) && (this.htmlElement.value != null)) {
      var sValue = dojo.trim(this.htmlElement.value);
      if (!bInFeedbackMode) {
        if (sValue != this.htmlElement.value) this.htmlElement.value = sValue;
      }
      if (sValue.length > 0) return sValue;
    }
    return null;
  },

  /** Override gxe.control.Control.onHtmlElementCreated() */
  onHtmlElementCreated: function(domProcessor,domNode) {
    this.xmlNode.setInputControl(this);
    var sValue = gxe.cfg.getGxeAttributeValue(this.cfgObject,"fixedValue");
    if (sValue == null) {
      sValue = gxe.cfg.getGxeAttributeValue(this.xmlNode.cfgObject,"fixedValue");
    }
    if (sValue == null) {
      if ((domProcessor != null) && (domNode != null)) {
        sValue = domProcessor.getNodeText(domNode);
      } else if (domProcessor == null) {
        sValue = gxe.cfg.getGxeAttributeValue(this.cfgObject,"value");
        if (sValue == null) {
          sValue = gxe.cfg.getGxeAttributeValue(this.xmlNode.cfgObject,"value");
        }
      }
    }
    if (sValue != null) this.htmlElement.value = sValue;
    dojo.connect(this.htmlElement,"onchange",this,"fireInputChanged");
    dojo.connect(this.htmlElement,"onkeyup",this,"fireInputChangedOnKeyUp");
  }
  
});

/**
 * @class Provides a popup dialog for GEMET keyword selection.
 * <br/><br/>Requires class Gemet from library [wepabb]/catalog/js/v[latest]/gemet.js.
 * @name gxe.control.InputGemetKeyword
 * @extends gxe.control.InputText
 * @property {boolean} supportsMultipleValues false
 * @property {Element} _gemetTool reference to the anchor element taht launches the GEMET dialog
 */
dojo.provide("gxe.control.InputGemetKeyword");
dojo.declare("gxe.control.InputGemetKeyword",gxe.control.InputText,{
  supportsMultipleValues: false,
  _gemetTool: null,
  
  /**
   * Utility to enable/disable the anchor that launches the GEMET dialog.
   * <br/>The anchor is enabled when text has been entered into the associated input text box.
   * @function 
   * @name _enableDisable
   * @memberOf gxe.control.InputGemetKeyword#
   */
  _enableDisable: function() {
    var bOk = false;
    if (this._gemetTool != null) bOk = (dojo.trim(this.htmlElement.value).length > 0);
    if (bOk) this._gemetTool.style.display = "inline";
    else this._gemetTool.style.display = "none";
  },
  
  /** Override gxe.control.InputBase.onInputChanged() */
  onInputChanged: function() {this._enableDisable();},

  /** Override gxe.control.Control.onHtmlChildrenCreated() */
  onHtmlChildrenCreated: function(domProcessor,domNode) {
    this.inherited(arguments);
    var elLink = document.createElement("a");
    elLink.setAttribute("href","javascript:void(0);");
    elLink.className = "gxeInputTool";
    elLink.appendChild(document.createTextNode(this.context.getI18NString("gemet.find")));
    this.htmlElement.parentNode.appendChild(elLink);
    
    var gemet = new Gemet();
    gemet.lblHelp = this.context.getI18NString("gemet.dialogHelp");
    gemet.lblDialogTitle = this.context.getI18NString("gemet.dialogTitle");
    gemet.lblWordNotFound = this.context.getI18NString("gemet.wordNotFound");
    gemet.lblCancel = this.context.getI18NString("gemet.cancel");
    gemet.lblOk = this.context.getI18NString("gemet.ok");
    gemet.lblErrorKeywordEmpty = this.context.getI18NString("gemet.lblErrorKeywordEmpty");
    gemet.lblLoadingMessage = this.context.getI18NString("gemet.connectingMessage");
    gemet.proxyUrl = this.context.contextPath+"/catalog/download/proxy.jsp?{0}";
    gemet.imgLoading = this.context.contextPath+"/catalog/images/loading.gif";
    
    dojo.connect(elLink,"onclick",this,dojo.hitch(this,function(e) {
      var sValue = dojo.trim(this.htmlElement.value);
      if (sValue.length > 0) {
        gemet.findConcepts(sValue,null,dojo.hitch(this,function(sGemetText) {
          if ((typeof(sGemetText) != "undefined") && (sGemetText != null)) {
            sGemetText = dojo.trim(sGemetText);
            if (sGemetText.length > 0) {
              this.htmlElement.value = sGemetText;
              //this.htmlElement.disabled = true;
            }
          }
        })); 
      }
    }));
    
    this._gemetTool = elLink;
    this._enableDisable();
  }
  
});

/**
 * @class Provides an interactive map control for the definition of a bounding spatial envelope.
 * <br/><br/>Requires library [wepabb]/catalog/js/v[latest]/gpt.js.
 * @name gxe.control.Map
 * @extends gxe.control.Control
 */
dojo.provide("gxe.control.Map");
dojo.declare("gxe.control.Map",gxe.control.Control,{
  gptMap: null,
  gptMapToolbar: null,
  gptLocator: null,
  gptInpEnv: null,
  _wasMapInitialized: false,
  
  /**
   * Appends an image button to a map toolbar.
   * @function 
   * @name _appendImageButton
   * @memberOf gxe.control.Map#
   * @param {Element} elToolBar the parent HTML element for the toolbar
   * @param {String} sId the id for the new img element
   * @param {String} sSrc the src for the new img element
   * @param {String} sTip the tool tip for the new img element
   */
  _appendImageButton: function(elToolBar,sId,sSrc,sTip) {
    var el = document.createElement("img");
    el.id = sId;
    el.alt = sTip;
    el.title = sTip;
    el.src = sSrc;
    elToolBar.appendChild(el);
    return el;
  },
  
  /** Override gxe.control.Control.build() */
  build: function(htmlParentElement,domProcessor,domNode) {
    this.inherited(arguments);
    this.htmlElement.style.display = "none";
    
    var idPfx = this.gxeId+"_";
    var sImages = this.context.contextPath+"/catalog/images/";
    var elImg;
    
    var elUseMap = document.createElement("a");
    elUseMap.setAttribute("href","javascript:void(0);");
    elUseMap.className = "gxeUseMapButton";
    elUseMap.appendChild(document.createTextNode(
        this.context.getI18NString("map.useMap")));
    this.htmlElement.parentNode.insertBefore(elUseMap,this.htmlElement);
    
    var elToolBar = document.createElement("div");
    elToolBar.id = idPfx+"mapToolbar";
    elToolBar.className = "mapToolbar";
    this.htmlElement.appendChild(elToolBar);
    
    elImg = this._appendImageButton(elToolBar,idPfx+"mapButton-zoomToWorld",
        sImages+"btn-zoomToWorld-off.gif",
        this.context.getI18NString("map.zoomToWorld"));
    
    elImg = this._appendImageButton(elToolBar,idPfx+"mapButton-zoomToInputEnvelope",
        sImages+"btn-zoomToInputEnvelope-off.gif",
        this.context.getI18NString("map.zoomToInputEnvelope"));
    
    elImg = this._appendImageButton(elToolBar,idPfx+"mapTool-drawInputEnvelope",
        sImages+"btn-drawInputEnvelope-off.gif",
        this.context.getI18NString("map.drawInputEnvelope"));
    elImg.className = "firstTool";
    
    elImg = this._appendImageButton(elToolBar,idPfx+"mapTool-deactivate",
        sImages+"btn-deactivate-off.gif",
        this.context.getI18NString("map.deactivate"));
    
    var elLocatorInput = document.createElement("input");
    elLocatorInput.setAttribute("type","text");
    elLocatorInput.setAttribute("maxLength","1024");
    elLocatorInput.id = idPfx+"mapInput-locate";
    elLocatorInput.className = "locatorInput";
    dojo.connect(elLocatorInput,"onkeypress",this,"onLocatorKeyPress");
    elToolBar.appendChild(elLocatorInput);
    
    elImg = this._appendImageButton(elToolBar,idPfx+"mapButton-locate",
        sImages+"btn-locate-off.gif",
        this.context.getI18NString("map.locate"));
        
    var elCandidates = document.createElement("div");
    elCandidates.id = idPfx+"locatorCandidates";
    elCandidates.className = "locatorCandidates";
    this.htmlElement.appendChild(elCandidates);
    
    var elMapContainer = document.createElement("div");
    elMapContainer.className = "gxeMapContainer";
    this.htmlElement.appendChild(elMapContainer);
    
    var elMapCanvas = document.createElement("div");
    elMapCanvas.id = idPfx+"interactiveMap";
    elMapCanvas.className = "gxeMapCanvas";
    elMapContainer.appendChild(elMapCanvas);
    
    var config = this.context.gptMapConfig;
    config.mapElementId = idPfx+"interactiveMap";
    config.mapToolName = "drawInputEnvelope";
    config.mapToolbarId = idPfx+"mapToolbar";
    config.locatorInputId =  idPfx+"mapInput-locate";
    config.locatorCandidatesId = idPfx+"locatorCandidates";
    
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
          config.inputEnvelopeXMinId = nlW[0].id;
          config.inputEnvelopeYMinId = nlS[0].id;
          config.inputEnvelopeXMaxId = nlE[0].id;
          config.inputEnvelopeYMaxId = nlN[0].id;
        }
        break;
      }
      el = el.parentNode;
    }
    
    dojo.connect(elUseMap,"onclick",this,"_useMap");
  },
  
  /**
   * Responds to a key press from the input text box associated with the locator (i.e. gazateer).
   * @function 
   * @name onLocatorKeyPress
   * @memberOf gxe.control.Map#
   * @param {Event} e the underlying browser event
   */
  onLocatorKeyPress: function(e) {
    if (!e) e = window.event;
    if (e) {
      var nKey = (e.keyCode) ? e.keyCode : e.which;
      if (nKey == 13) {
        if (this.gptLocator != null) this.gptLocator.locate();
        return false;
      }
    } 
    return true;
  },
  
  /**
   * Responds to a click of a map related button.
   * @function 
   * @name onMapButtonClicked
   * @memberOf gxe.control.Map#
   * @param {String} sButtonName the map button name
   */
  onMapButtonClicked: function(sButtonName) {
    if (sButtonName == "zoomToWorld") {
      if (this.gptMap != null) this.gptMap.zoomToWorld();
    } else if (sButtonName == "zoomToInputEnvelope") {
      if (this.gptInpEnv != null) this.gptInpEnv.zoomToInputEnvelope();
    } else if (sButtonName == "locate") {
      if (this.gptLocator != null) this.gptLocator.locate();
    }
  },
  
  /**
   * Responds following the load of the underlying map control.
   * @function 
   * @name onMapLoaded
   * @memberOf gxe.control.Map#
   */
  onMapLoaded: function() {
    if (this.gptInpEnv != null) this.gptInpEnv.highlightInputEnvelope();
  },
  
  /**
   * Repositions the underlying map control.
   * @function 
   * @name repositionMap
   * @memberOf gxe.control.Map#
   */
  repositionMap: function() {
    if (this.gptMap != null) {
      this.gptMap.reposition();
      if (this._wasMapInitialized && (this.gptInpEnv != null)) {
        if (dojo.isIE <= 8) {
          this.gptInpEnv.highlightInputEnvelope();
        }
      }
    }
  },
  
  /**
   * Handles a "Use Map" click.
   * @function 
   * @name _useMap
   * @memberOf gxe.control.Map#
   * @param {Event} e the underlying browser event
   */
  _useMap: function(e) {
    if (this._wasMapInitialized) {
      this.repositionMap();
      return;
    }
    
    this.htmlElement.style.display = "block";
    var config = this.context.gptMapConfig;
    
    this.gptMap = new GptMap();
    dojo.connect(this.gptMap,"onMapLoaded",this,"onMapLoaded");
  
    this.gptInpEnv = new GptInputEnvelope();
    this.gptInpEnv.initialize(config,this.gptMap);
    
    this.gptMap.initialize(config);
    this._wasMapInitialized = true;
    
    this.gptMapToolbar = new GptMapToolbar();
    dojo.connect(this.gptMapToolbar,"onMapButtonClicked",this,"onMapButtonClicked");
    dojo.connect(this.gptMapToolbar,"onDrawInputEnvelope",this.gptInpEnv,"onDrawInputEnvelope");
    this.gptMapToolbar.initialize(config,this.gptMap);
    
    this.gptLocator = new GptLocator();
    this.gptLocator.initialize(config,this.gptMap);
    
    dojo.connect(window,"onresize",this,"repositionMap");
    dojo.connect(window,"onscroll",this,"repositionMap");
  }
  
});

/**
 * @class Provides a popup dialog for the selection of keywords.
 * @name fgdc.control.KeywordSelector
 * @extends gxe.control.Control
 */
dojo.provide("fgdc.control.KeywordSelector");
dojo.declare("fgdc.control.KeywordSelector",gxe.control.Control,{
  
  /**
   * Appends a checkbox option to the popup dialog.
   * @function 
   * @name _appendCheckBox
   * @memberOf fgdc.control.KeywordSelector#
   * @param {Element} el the parent HTML element
   * @param {String} sLabel the label
   * @param {String} sValue a value associated with the checkbox
   * @param {boolean} bSelected true if the box should be checked
   */
  _appendCheckBox: function(el,sLabel,sValue,bSelected) {
    var sOptionId = this.context.generateUniqueId();
    var elListItem = document.createElement("li");
    el.appendChild(elListItem);
    var elOption = document.createElement("input");
    elOption.setAttribute("type","checkbox");
    elOption.setAttribute("id",sOptionId);
    elOption.setAttribute("value",sValue); 
    elListItem.appendChild(elOption);
    if (bSelected) elOption.setAttribute("checked","checked");
    
    var elLabel = document.createElement("label");
    elLabel.setAttribute("for",sOptionId);
    elLabel.appendChild(document.createTextNode(sLabel));
    elListItem.appendChild(elLabel);
    return elListItem;
  },
  
  /** Override gxe.control.Control.build() */
  build: function(htmlParentElement,domProcessor,domNode) {
    this.inherited(arguments);
    
    if (this.htmlElement != null) {
      dojo.connect(this.htmlElement,"onclick",this,dojo.hitch(this,function(e) {
        
        var delimitedTextArea = null;
        var aCurrentValues = null;
        var siblings = this.xmlNode.parentElement.children;
        for (var i=0;i<siblings.getLength();i++) {
          var sibling = siblings.getItem(i);
          if (sibling.getInputControl() != null) {
            if (sibling.getInputControl().getSupportsMultipleValues()) {
              var s = sibling.nodeInfo.localName;
              if (s.length > 3) {
                s = s.substring(s.length-3);
                if (s == "key") {
                  delimitedTextArea = sibling.getInputControl();
                  aCurrentValues = delimitedTextArea.getInputValues(true);
                  break;
                }
              }
            }
          }
        }

        if (delimitedTextArea != null) {
          var elListDiv = document.createElement("div");
          dojo.style(elListDiv,{margin:"5px",padding:"5px",border:"1px solid #CCC"});
          var elList = document.createElement("ul");
          elList.className = "gxeSelectMany";
          elListDiv.appendChild(elList);
          var cfgOptions = gxe.cfg.findChild(this.cfgObject,gxe.cfg.uriGxe,"options");
          if (cfgOptions != null) {
            gxe.cfg.forEachChild(cfgOptions,gxe.cfg.uriGxe,"option",dojo.hitch(this,function(cfgOption) {
              var sLabel = gxe.cfg.getGxeAttributeValue(cfgOption,"label");
              var sValue =  gxe.cfg.getGxeAttributeValue(cfgOption,"value");
              var sAlias =  gxe.cfg.getGxeAttributeValue(cfgOption,"alias");
              var sSelected = gxe.cfg.getGxeAttributeValue(cfgOption,"selected");
              var bSelected = false;
              if (aCurrentValues != null) {
                for (var iCur=0; iCur<aCurrentValues.length; iCur++) {
                  if (aCurrentValues[iCur] == sValue) {
                    bSelected = true;
                    break;
                  }
                }
              }
              this._appendCheckBox(elList,sLabel,sValue,bSelected);
            }));
          }
                    
          var sTitle = this.htmlTextContent;
          if (sTitle == null) sTitle = "?Select";
          var dialog = new dijit.Dialog({
            title: sTitle,
            style: "display: none; border: 1px solid #000000; background: #FFFFFF;",
            autofocus: false
          });
          dojo.addClass(dialog.domNode,"tundra");
          dialog.domNode.appendChild(elListDiv);
          
          var elButtonDiv = document.createElement("div");
          dojo.style(elButtonDiv,{marginLeft:"auto",marginRight:"auto",width:"50%",padding:"5px"});
          dialog.domNode.appendChild(elButtonDiv);
          var elOk = document.createElement("button");
          elOk.appendChild(document.createTextNode(this.context.getI18NString("dialog.ok")));
          elButtonDiv.appendChild(elOk);
          var elCancel = document.createElement("button");
          elCancel.appendChild(document.createTextNode(this.context.getI18NString("dialog.cancel")));
          elButtonDiv.appendChild(elCancel);
          
          dojo.connect(elOk,"onclick",this,dojo.hitch(this,function(e) {
            var sCheckedValues = "";
            dojo.query("[type='checkbox']",dialog.domNode).forEach(dojo.hitch(this,function(item) {
              if (item.checked) {
                if (sCheckedValues.length > 0) sCheckedValues += delimitedTextArea.delimiter;
                sCheckedValues += item.value;
              }
            }));
            var sThesaurus = gxe.cfg.getGxeAttributeValue(this.cfgObject,"thesaurus");
            if (sThesaurus == null) sThesaurus = "";
            this.xmlNode.getInputControl().htmlElement.value = sThesaurus;
            this.xmlNode.getInputControl().fireInputChanged();
            delimitedTextArea.htmlElement.value = sCheckedValues;
            delimitedTextArea.fireInputChanged();
            dialog.hide();
            dialog.destroy();
          }));
          
          dojo.connect(elCancel,"onclick",this,dojo.hitch(this,function(e) {
            dialog.hide();
            dialog.destroy();
          }));
          dialog.show(); 
        }
      }));
    }
  }
  
});

