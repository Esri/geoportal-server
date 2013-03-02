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
 * livedata
 * Copyright (c) 2009, ESRI.
 */

dojo.require("dijit._Widget");
dojo.require("dijit.form.Button");
dojo.require("dijit.Toolbar");
dojo.require("dijit.Tooltip");
dojo.require("dijit.Dialog");
dojo.require("dojox.encoding.base64");

dojo.require("esri.map");
dojo.require("esri.geometry");
dojo.require("esri.tasks.geometry");
dojo.require("esri.toolbars.navigation");
dojo.require("esri.layers.osm");
dojo.require("esri.layers.wms");
if (esri.version>=2.5) {
  dojo.require("esri.layers.wmts");
}

/*
 *==============================================================================
 * livedata-widget
 *==============================================================================
 */
dojo.provide("gpt.LiveData");

dojo.declare("gpt.LiveData", dijit._Widget, {
  _placeholder: null,
  _loginDlg: null,
  _loginAtempts: 0,

  /** constructs a widget **/
  constructor: function() {
    this.postCreate = dojo.hitch(this, this.postCreate);
    this._getAttribute = dojo.hitch(this, this._getAttribute);
    this._getTooltips = dojo.hitch(this, this._getTooltips);
    this._createPlaceholder = dojo.hitch(this, this._createPlaceholder);
    this._initLoginDlg = dojo.hitch(this, this._initLoginDlg);
    this._collectCredentials = dojo.hitch(this, this._collectCredentials);

    this.getUrl = dojo.hitch(this, this.getUrl);
    this.getProxyUrl = dojo.hitch(this, this.getProxyUrl);
    this.getMapId = dojo.hitch(this, this.getMapId);
    this.getMapStyle = dojo.hitch(this, this.getMapStyle);
    this.getMapServiceUrl = dojo.hitch(this, this.getMapServiceUrl);
    this.getMapServiceType = dojo.hitch(this, this.getMapServiceType);
    this.getMapVisibleLayers = dojo.hitch(this, this.getMapVisibleLayers);
    this.getMapInitialExtent = dojo.hitch(this, this.getMapInitialExtent);
    this.getGeometryServiceUrl = dojo.hitch(this, this.getGeometryServiceUrl);
    this.getBasemapLabel = dojo.hitch(this, this.getBasemapLabel);
    this.getTooltips = dojo.hitch(this, this.getTooltips);
    this.getPlaceholder = dojo.hitch(this, this.getPlaceholder);

    this.submitCredentials = dojo.hitch(this, this.submitCredentials);
    this.showLoginDlg = dojo.hitch(this, this.showLoginDlg);

    dojo.subscribe("livedata-error", this, this._handleError);
    dojo.subscribe("livedata-WMSError", this, this._handleWMSError);

    this._initLoginDlg();
  },

  _initLoginDlg: function() {
    this._loginDlg = new dijit.Dialog({
          title: "Enter user name and password",
          style: "width: 300px"
    });
  },

  _collectCredentials: function() {
    var cred = [];
    cred.push(dojo.byId('loginUserName').value);
    cred.push(dojo.byId('loginUserPassword').value);
    return cred;
  },

  submitCredentials: function() {
    this._loginAtempts++;
    var url = this.getUrl();
    var proxyUrl = this.getProxyUrl();
    var cred = this._collectCredentials();
    var i;
    for (i=0; i<cred.length; i++) {
      cred[i] = escape(cred[i]);
    }
    var livedata = cred.join(",");
    livedata += "," + String(this._loginAtempts);
    var livedataBytes = [];
    for (i=0; i<livedata.length; i++) {
      livedataBytes.push(livedata.charCodeAt(i));
    }
    var livedataEncoded = dojox.encoding.base64.encode(livedataBytes);
    this._loginDlg.hide();
    var callUrl = proxyUrl + "?url=" + url;
    dojo.xhrGet({
      url: callUrl,
      headers: {"GPT-livedata": livedataEncoded},
      timeout: 60000,
      load: dojo.hitch(this, this._parseResponse),
      error: gpt.LiveData.handleHttpError
    });
  },

  showLoginDlg: function() {
    // set the content of the dialog:
    this._loginDlg.attr("content",
    "<form><table border='0' width='100%'>" +
    "<tr>" +
      "<td align='right'>User name:</td>" +
      "<td><input type='text' id='loginUserName'/></td>" +
    "</tr>" +
    "<tr>" +
      "<td align='right'>Password:</td>" +
      "<td><input type='password' id='loginUserPassword'/></td>" +
    "</tr>" +
    "<tr>" +
      "<td colspan='2' align='center'><input type='button' value='Submit' onclick='gpt.LiveData.submitCredentials();'/></td>" +
    "</tr>" +
    "</table></form>");
    this._loginDlg.show();
  },

  /** gets service URL **/
  getUrl: function() {
    return this._getAttribute('url');
  },

  /** gets proxy URL **/
  getProxyUrl: function() {
    return this._getAttribute('proxy');
  },

  /** gets map ID **/
  getMapId: function() {
    return this.domNode.id+"-preview";
  },

  /** gets map style **/
  getMapStyle: function() {
    return this._getAttribute('mapStyle');
  },

  /** gets map service URL **/
  getMapServiceUrl: function() {
    return this._getAttribute('mapService');
  },

  /** gets map service type **/
  getMapServiceType: function() {
    return this._getAttribute('mapType');
  },

  /** gets map visible layers **/
  getMapVisibleLayers: function() {
    return this._getAttribute('mapVisibleLayers');
  },

  /** gets map visible layers **/
  getMapInitialExtent: function() {
    return this._getAttribute('mapInitialExtent');
  },

  /** gets geometry service URL **/
  getGeometryServiceUrl: function() {
    return this._getAttribute('geometryService');
  },

  /** gets basemap label **/
  getBasemapLabel: function() {
    return this._getAttribute('basemapLabel');
  },

  /** gets array of tooltips **/
  getTooltips: function() {
    return this._getTooltips();
  },

  /** gets placeholder **/
  getPlaceholder: function() {
    this._createPlaceholder();
    return this._placeholder;
  },

  /** called after widget is created **/
  postCreate: function() {
    // get JSON script from the proxy to execute
    this.domNode.widget = this;
    var url = this._getAttribute('proxy') + "?url=" + escape(this._getAttribute('url'));
    dojo.xhrGet({
      url: url,
      timeout: 60000,
      load: dojo.hitch(this, this._parseResponse),
      error: gpt.LiveData.handleHttpError
    });
  },

  /** handles error */
  _handleError: function(errorDetails) {
    var verbose = new Boolean(this._getAttribute('verbose'));
    if (verbose==true) {
      var placeholder = this._createPlaceholder();
      var errorMessage = this._getAttribute('errorMessage');
      if (errorMessage==null) {
        errorMessage = "Service unavailable";
      }
      if (errorDetails!=null && errorDetails.length>0) {
        errorMessage = errorMessage + "; " + errorDetails;
      }
      placeholder.innerHTML = "<div class=\"errorMessage\">" +errorMessage+ "</div>";
    }
  },
  
  /** handle WMS error */
  _handleWMSError: function(errorDetails) {
    var verbose = new Boolean(this._getAttribute('verbose'));
    if (verbose==true) {
      var placeholder = this._createPlaceholder();
      var errorMessage = this._getAttribute('WMSErrorMessage');
      if (errorMessage==null) {
        errorMessage = "WMS service doesn't provide any layer in a projection of the base map.";
      }
      placeholder.innerHTML = "<div class=\"errorMessage\">" +errorMessage+ "</div>";
    }
  },

  /** gets attribute from the original <div/> node **/
  _getAttribute: function(name) {
    var attr = this.domNode.attributes[name];
    return attr!=null? attr.nodeValue: "";
  },

  /** gets tooltips */
  _getTooltips: function() {
    var org = ["Zoom In", "Zoom Out", "Full Extent", "Previous Extent", "Next Extent","Pan","Show or hide base map"];
    var tooltips = this._getAttribute("tooltips");
    var toolArr = tooltips.split(",");
    var unescaped = "";
    for (var i=0; i<toolArr.length; i++) {
      unescaped = unescape(toolArr[i]);
      if (unescaped.length>0) {
        org[i] = unescaped;
      }
    }
    return org;
  },

  /** creates placeholder */
  _createPlaceholder: function() {
    if (this._placeholder==null) {
      var onCreatePlaceholder = this._getAttribute("onCreatePlaceholder");
      if (onCreatePlaceholder!=null && onCreatePlaceholder.length>0) {
        var fOnCreatePlaceholder = dojo.eval("dojo.hitch(this,"+onCreatePlaceholder+")");
        this._placeholder = fOnCreatePlaceholder(this.domNode);
      } else {
        this._placeholder = this.domNode;
      }
    }

    return this._placeholder;
  },

  /** parses proxy response **/
  _parseResponse: function(response, io) {
    if (response instanceof String) {
      response = dojo.trim(response);
    }
    if (response.length>0) {
      this._createPlaceholder();

      try {
        // parse response
        var responseObj = eval("(" + response + ")");
        // execute proxy response to let write itself onto the body section
        responseObj.init(this);
      } catch (e) {
        // handle exception, display information if verbose mode
        gpt.LiveData.handleInternalException(e);
      }

    }
  }

});

// handles service error
gpt.LiveData.handleServiceError = function(error) {
  if (error!=null) {
    dojo.publish("livedata-error", ["Service error: "+error]);
  } else {
    dojo.publish("livedata-error", ["Service error"]);
  }
};

// handles HTTP error
gpt.LiveData.handleHttpError = function(response, io) {
  if (response!=null && response.status!=null) {
    dojo.publish("livedata-error", ["HTTP status: "+response.status]);
  } else {
    dojo.publish("livedata-error");
  }
};

// handles internal exception
gpt.LiveData.handleInternalException = function(e) {
  if (e!=null && e.message!=null) {
    dojo.publish("livedata-error", ["internal error: " + e.message]);
  } else {
    dojo.publish("livedata-error", ["internal error"]);
  }
};

/** handles click **/
gpt.LiveData.onClickHandler = function(id,imgOpen,imgClosed) {
    var bDisplay = true;

    var elImg = document.getElementById(id+"-img");
    if (elImg != null) {
      var sSrc = imgOpen;
      if (elImg.src.indexOf(imgOpen) != -1) {
        sSrc = imgClosed;
        bDisplay = false;
      }
      elImg.src = sSrc;
      var elChk = document.getElementById(id+"-chk");
      if (elChk != null) elChk.checked = bDisplay;
    }

    var el = document.getElementById(id+"-body");
	  if (el != null) {
	    if (bDisplay)
        el.style.display = "block";
      else
        el.style.display = "none";
	  }
}

/** parses xml */
gpt.LiveData.parseXml = function(strXml) {
  var xmlDoc = null;
  if (dojo.isIE) {
    try { //Internet Explorer
      xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
      xmlDoc.async="false";
      xmlDoc.validateOnParse="false";
      xmlDoc.loadXML(strXml);
    } catch(e) {}
  } else {
    try { //Firefox, Mozilla, Opera, etc.
      xmlDoc=new DOMParser().parseFromString(strXml,"text/xml");
    } catch(e) {}
  }
  return xmlDoc;
}

/** sets extent on the projected map */
gpt.LiveData.setExtent = function(map,extent,gs) {
  if (extent!=null) {
    if (map.spatialReference.wkid!=extent.spatialReference.wkid) {
      if (gs!=null) {
        gs.project([extent], map.spatialReference, function(gfx) {
          map.setExtent(gfx[0]);
        });
      }
    } else {
      map.setExtent(extent);
    }
  }
}

/** gets elements by tag name considering namespace */
gpt.LiveData.getElementsByTagName = function(xml,namespace,tagName) {
  var elements = xml.getElementsByTagName(namespace + ":" + tagName);
  if (elements.length==0) {
    elements = xml.getElementsByTagName(tagName);
  }
  return elements;
}

gpt.LiveData.isGCSWkid = function(wkid) {
  if (wkid == null) return false;
  if (wkid == 84) return true;
  if ((wkid >=   4000) && (wkid <   5000)) return true;
  if ((wkid >=  37000) && (wkid <  38000)) return true;
  if ((wkid >= 104000) && (wkid < 105000)) return true;
  return false;
}

gpt.LiveData.showLoginDlg = function() {
  var domNode = dojo.byId('gpt_LiveData_0');
  if (domNode!=null && domNode.widget!=null) {
    domNode.widget.showLoginDlg();
  }
}

gpt.LiveData.submitCredentials = function() {
  var domNode = dojo.byId('gpt_LiveData_0');
  if (domNode!=null && domNode.widget!=null) {
    domNode.widget.submitCredentials();
  }
}

/*
 *==============================================================================
 * ArcIMS support
 *==============================================================================
 */

dojo.provide("esri.gpt.layers.ArcIMSLayer");

/** ArcIMS Layer **/
dojo.declare("esri.gpt.layers.ArcIMSLayer", esri.layers.DynamicMapServiceLayer, {
  _proxyUrl: "",
  _url : "",
  _imageUrl: "",

  constructor: function(url, proxyUrl, gsUrl) {
    /** hitch methods */
    this.getImageUrl = dojo.hitch(this, this.getImageUrl);
    this._submitGetImageUrlRequest = dojo.hitch(this, this._submitGetImageUrlRequest);
    this._handleGetImageUrlResponse = dojo.hitch(this, this._handleGetImageUrlResponse);

    /** store URL's **/
    this._proxyUrl = proxyUrl;
    this._url = url;

    /** mark layer as loaded **/
    this.loaded = true;
    this.onLoad(this);
  },

  /** gets image URL **/
  getImageUrl: function(extent, width, height, callback) {
    /** declare params */
    try {
      if (extent.xmin!=undefined && extent.ymin!=undefined && extent.xmax!=undefined && extent.ymax!=undefined) {
        this._submitGetImageUrlRequest(extent, width, height);
        callback(this._imageUrl);
      }
    } catch (e) {
      // handle exception, display information if verbose mode
      gpt.LiveData.handleInternalException(e);
    }
  },

  /** submits request to get image URL **/
  _submitGetImageUrlRequest: function(extent, width, height) {
    var strRequest =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<ARCXML version=\"1.1\">"+
      "<REQUEST>"+
      "<GET_IMAGE>"+
      "<PROPERTIES>"+
      (extent.spatialReference!=null && extent.spatialReference.wkid!=null? "<FEATURECOORDSYS id=\"" +extent.spatialReference.wkid+ "\"/>":"") +
      "<BACKGROUND color=\"255,255,255\" transcolor=\"255,255,255\"/>"+
      "<IMAGESIZE width=\"" +width+ "\" height=\"" +height+ "\"/>"+
      "<ENVELOPE minx=\"" +extent.xmin+ "\" miny=\"" +extent.ymin+ "\" maxx=\"" +extent.xmax+ "\" maxy=\"" +extent.ymax+ "\"/>"+
      "</PROPERTIES>"+
      "</GET_IMAGE>"+
      "</REQUEST>"+
      "</ARCXML>";
    dojo.xhrPost({
      url: this._proxyUrl + "?" + this._url,
      postData: strRequest,
      timeout: 30000,
      sync: true,
      load: this._handleGetImageUrlResponse,
      error: gpt.LiveData.handleHttpError
    });
  },

  /** handles get image URL response **/
  _handleGetImageUrlResponse: function(response, io) {
    try {
      var xmlDoc = gpt.LiveData.parseXml(response);
      if (xmlDoc!=null) {
        var nodes = xmlDoc.getElementsByTagName("OUTPUT");
        if (nodes!=null && nodes.length>0) {
          var outputNode = nodes.item(0);
          var imageUrl = outputNode.getAttribute("url");
          this._imageUrl = imageUrl;
        } else {
          var errors = xmlDoc.getElementsByTagName("ERROR");
          if (errors!=null && errors.length>0) {
            var errorNode = errors.item(0);
            var errorMsg = errorNode!=null && errorNode.firstChild!=null? errorNode.firstChild.nodeValue: null;
            gpt.LiveData.handleServiceError(errorMsg);
          }
        }
      }
    } catch (e) {
      // handle exception, display information if verbose mode
      gpt.LiveData.handleInternalException(e);
    }
  }

});

/*
 *==============================================================================
 * WMS support
 *==============================================================================
 */

dojo.provide("esri.gpt.layers.WMSLayer");

/** WMS Layer **/
dojo.declare("esri.gpt.layers.WMSLayer", esri.layers.DynamicMapServiceLayer, {
  _proxyUrl: "",
  _url : "",
  _mapUrl : "",
  _layers : [],
  _version : "1.1.1",
  _layerLimit: 0,

  /** constructs layer **/
  constructor: function(url, proxyUrl, gsUrl) {
    /** hitch methods */
    this.getImageUrl = dojo.hitch(this, this.getImageUrl);
    this._initialize = dojo.hitch(this, this._initialize);
    this._parseCapabilities = dojo.hitch(this, this._parseCapabilities);
    this._getWMSBBOX = dojo.hitch(this, this._getWMSBBOX);
    this._swapExtentAxis = dojo.hitch(this, this._swapExtentAxis);
    this._acceptSrsNodes = dojo.hitch(this, this._acceptSrsNodes);
    this._acceptSrs = dojo.hitch(this, this._acceptSrs);
    this._indexOf = dojo.hitch(this, this._indexOf);
    this._selectLayersFor = dojo.hitch(this, this._selectLayersFor);
    this._getLayersString = dojo.hitch(this, this._getLayersString);
    this._findNumber = dojo.hitch(this, this._findNumber);
    this._getCommonSrs = dojo.hitch(this, this._getCommonSrs);
    this._findGetMapUrl = dojo.hitch(this, this._findGetMapUrl);

    /** store URL's **/
    this._proxyUrl = proxyUrl;
    this._url = url;

    /** initialize layer **/
    this._initialize();
  },

  /** gets image URL **/
  getImageUrl: function(extent, width, height, callback) {
    try {
      /** declare params */
      var params = {  request: "GetMap",
                      service: "WMS",
                      version: this._version,
                      styles: "",
                      bbox: this._getWMSBBOX(extent),
                      width: width,
                      height: height,
                      format:"image/png",
                      transparent: true
                    };
      var callLength = (this._mapUrl + dojo.objectToQuery(params)).length;

      /** find wkid; declare SRS **/
      var wkid = extent.spatialReference!=null? extent.spatialReference.wkid: "4326";
      var srs = "EPSG:" + wkid;

      /** get layers for the wkid **/
      var layers = this._selectLayersFor(wkid,3980-callLength);

      /** find and use common SRS instead of map wkid if no layers for the map wkid **/
      if (layers.length==0 && gpt.LiveData.isGCSWkid(wkid)) {
        var commonSrs = this._getCommonSrs(this._layers);
        for (var csrs=0; csrs<commonSrs.length; csrs++) {
          var nsrs = this._findNumber(commonSrs[csrs]);
          if (gpt.LiveData.isGCSWkid(nsrs)) {
            wkid = nsrs;
            srs = commonSrs[csrs];
            layers = this._selectLayersFor(wkid,3980-callLength);
            break;
          }
        }
      }

      if (layers.length==0) {
        dojo.publish("livedata-WMSError", []);
      }
      
      var layersAsString = this._getLayersString(layers);
      for (var i=0; i<layers.length; i++) {
        if (layers[i].srs!=null && layers[i].srs.length>0) {
          var present = false;
          for (var j = 0; j<layers[i].srs.length; j++) {
            if (layers[i].srs[j]==srs) {
              present = true;
              break;
            }
          }
          if (!present && layers[i].srs.length>0) {
            srs = layers[i].srs[0];
          }
          break;
        }
      }

      /** set CRS/SRC **/
      if (parseInt(this._version.replace(/\./g,"")) <= 111) {
        dojo.mixin(params, { srs: srs });
      } else {
        dojo.mixin(params, { crs: srs });
      }

      /** set layers **/
      dojo.mixin(params, { layers: layersAsString });

      /** do callback **/
      callback(this._mapUrl + dojo.objectToQuery(params));
    } catch (e) {
      // handle exception, display information if verbose mode
      gpt.LiveData.handleInternalException(e);
    }
  },

  /** initializes layer */
  _initialize: function() {
    dojo.xhrGet({
      url: this._proxyUrl + "?" + this._url,
      timeout: 30000,
      sync: true,
      load: this._parseCapabilities,
      error: gpt.LiveData.handleHttpError
    });
  },

  /** parses WMS capabilities */
  _parseCapabilities: function(response, io) {
    try {
      response = dojo.trim(response);
      response = gpt.LiveData.parseXml(response);
      
      /** get root node **/
      var capabilities = response.getElementsByTagName("WMS_Capabilities");
      if (capabilities==null || capabilities.length==0) {
        capabilities = response.getElementsByTagName("WMT_MS_Capabilities");
      }

      var capabilitiesNode = null;

      if (capabilities!=null && capabilities.length>0) {
        capabilitiesNode = capabilities.item(0);
      }

      /** read version **/
      if (capabilitiesNode!=null) {
        var version = capabilitiesNode.getAttribute("version");
        if (version.length>0) {
          this._version = version;
        }
      }

      /** read layer limit */
      if (capabilitiesNode!=null) {
        var layerLimitNodes = capabilitiesNode.getElementsByTagName("LayerLimit");
        if (layerLimitNodes.length>0) {
          this._layerLimit = layerLimitNodes[0].firstChild.nodeValue;
        }
      }

      /** get all layer definitions **/
      var layersNodes = response.getElementsByTagName("Layer");
      var i, j, id, layerNode, layerIds = [], srs = [], nsrs = [];

      /** extracts all queryable layer's ids **/
      for (i=0; i<layersNodes.length; i++) {
        srs = [];
        layerNode = layersNodes.item(i);
        id = "";
        for (j=0; j<layerNode.childNodes.length; j++) {
          if (layerNode.childNodes[j].nodeName=="Name") {
            id = layerNode.childNodes[j].firstChild.nodeValue;
            break;
          }
        }
        if (id.length>0) {
          this._acceptSrsNodes(srs, nsrs, layerNode);
          layerIds.push({id: id, srs: srs, nsrs: nsrs});
        }
      }
      this._layers = layerIds;

      /** obtain GetMap url **/
      var mapUrl = this._findGetMapUrl(response);
      if (mapUrl.length==0 || mapUrl.indexOf("http://localhost")==0) {
        var qmark = this._url.indexOf("?");
        mapUrl = qmark>0? this._url.substring(0,qmark): this._url ;
      }
      if (mapUrl.charAt(mapUrl.length)!='?' && mapUrl.charAt(mapUrl.length)!='&') {
        this._mapUrl = mapUrl.indexOf('?')>=0? mapUrl + '&': mapUrl + '?';
      } else {
        this._mapUrl = mapUrl;
      }

      /** mark layer as loaded **/
      this.loaded = true;
      this.onLoad(this);
    } catch (e) {
      // handle exception, display information if verbose mode
      gpt.LiveData.handleInternalException(e);
    }
  },

  _findGetMapUrl: function(response) {
      var getMapNodes = response.getElementsByTagName("GetMap");
      for (var mni=0; mni<getMapNodes.length; mni++) {
        var getMapNode = getMapNodes[mni];
        var getNodes = getMapNode.getElementsByTagName("Get");
        for (var gi=0; gi<getNodes.length; gi++) {
          var getNode = getNodes[gi];
          var onlineResourceNodes = getNode.getElementsByTagName("OnlineResource");
          for (var ori=0; ori<onlineResourceNodes.length; ori++) {
            var onlineResourceNode = onlineResourceNodes[ori];
            for (var i=0; i<onlineResourceNode.attributes.length; i++) {
              if (onlineResourceNode.attributes[i].nodeName!=null && onlineResourceNode.attributes[i].nodeName.indexOf("href")>=0 && onlineResourceNode.attributes[i].nodeValue.length>0) {
                return onlineResourceNode.attributes[i].nodeValue;
              }
            }
          }
        }
      }
      return "";
  },

  _getCommonSrs: function(layers) {
    var commonSrs = [];
    for (i = 0; i<layers.length; i++) {
      if (i==0) {
        for (j = 0; j<layers[i].srs.length; j++) {
          commonSrs.push(layers[i].srs[j]);
        }
      } else {
        for (k=commonSrs.length-1; k>=0; k--) {
          var present = false;
          for (j = 0; j<layers[i].srs.length; j++) {
            if (layers[i].srs[j]==commonSrs[k]) {
              present = true;
              break;
            }
          }
          if (!present) {
            commonSrs.splice(k,1);
          }
        }
      }
    }
    return commonSrs;
  },

  _selectLayersFor: function(wkid,max) {
    var selLayers = [], i, j, totalLength = 0;
    for (i = 0; i<this._layers.length; i++) {
      var found = false;
      for (j = 0; j<this._layers[i].nsrs.length; j++) {
        if (this._layers[i].nsrs[j]==wkid) {
          totalLength += (escape(this._layers[i].id).length+3);
          if (totalLength>max) {
            return selLayers;
          }
          if (this._layerLimit>0 && selLayers.length>=this._layerLimit) {
            return selLayers;
          }
          selLayers.push(this._layers[i]);
          found = true;
          break;
        }
      }
      if (!found && gpt.LiveData.isGCSWkid(wkid)) {
        for (j = 0; j<this._layers[i].nsrs.length; j++) {
          if (gpt.LiveData.isGCSWkid(this._layers[i].nsrs[j])) {
            totalLength += (escape(this._layers[i].id).length+3);
            if (totalLength>max) {
              return selLayers;
            }
            if (this._layerLimit>0 && selLayers.length>=this._layerLimit) {
              return selLayers;
            }
            selLayers.push(this._layers[i]);
            found = true;
            break;
          }
        }
      }
    }
    return selLayers;
  },

  _getLayersString: function(selLayers) {
    var layerIds = [], i;
    for (i = 0; i<selLayers.length; i++) {
      layerIds.push(selLayers[i].id);
    }
    return layerIds.join(",");
  },

  _findNumber: function(str) {
    var result = str.search(/[0-9]+/);
    if (result>=0) {
      str = str.substr(result);
      result = str.search(/[^0-9]/);
      if (result>=0) {
         str = str.substr(0,result);
      }
    } else {
      str = "0";
    }
    var num = parseInt(str);
    return num;
  },

  /** reads and stores SRS from the list of nodes **/
  _acceptSrsNodes: function(allSrs, allNSrs, parentNode) {
    var accepted = 0;
    var srs = [];
    for (var child=parentNode.firstChild; child!=null; child=child.nextSibling) {
      if (child.nodeName=="CRS" || child.nodeName=="SRS") {
        srs = child.firstChild.nodeValue.split(" ");
        accepted += this._acceptSrs(allSrs, allNSrs, srs);
      }
      if (child.nodeName=="BoundingBox") {
        var srsNode = child.attributes["SRS"];
        srs = srsNode!=null && srsNode.nodeValue!=null? srsNode.nodeValue.split(" "): [];
        accepted += this._acceptSrs(allSrs, allNSrs, srs);
      }
    }
    if (parentNode.parentNode!=null && parentNode.parentNode.nodeName=="Layer") {
      this._acceptSrsNodes(allSrs, allNSrs, parentNode.parentNode);
    }
  },

  _acceptSrs: function(allSrs, allNSrs, srs) {
    var accepted = 0;
    for (var sii=0; sii<srs.length; sii++) {
      if (srs[sii].length>0 && this._indexOf(allSrs,srs[sii])<0) {
        allSrs.push(srs[sii]);
        accepted++;
      }
      var nsrs = this._findNumber(srs[sii]);
      if (this._indexOf(allNSrs,nsrs)<0) {
        allNSrs.push(nsrs);
      }
    }
    return accepted;
  },

  _indexOf: function(array, id) {
    for (var i=0; i<array.length; i++) {
      if (array[i]==id) {
        return i;
      }
    }
    return -1;
  },

  /** gets endpoint URL **/
  _getEndpointUrl: function() {
    var qmark = this._url.indexOf("?");
    return (qmark>0? this._url.substring(0,qmark): this._url);
  },

  /** gets BOX depending on extent **/
  _getWMSBBOX: function(extent) {
    //Get the WKID code
    var code = extent.spatialReference.wkid;
    //Check if the wkid needs the BBOX coords swapped
    if (this._swapExtentAxis(code)) {
      //return the extent with Y axis first
      return extent.ymin + "," + extent.xmin + "," + extent.ymax + "," + extent.xmax;
    } else {
      //Return the extent with X axis first
      return extent.xmin + "," + extent.ymin + "," + extent.xmax + "," + extent.ymax;
    }
  },

  /** check if BOX axis should be replaced */
  _swapExtentAxis: function(code) {
    if (parseInt(this._version.replace(/\./g,"")) <= 111) {
      return false;
    }

    for (var i=0; i<this._swapAxisRanges.length; i++) {
      if (code >= this._swapAxisRanges[i][0] && code <= this._swapAxisRanges[i][1]) {
        return true;
      }
    }
    return false;
  },

  /** array of projections where axes should be replaced **/
  _swapAxisRanges: [
    [4001, 4999],
    [2044, 2045], [2081, 2083], [2085, 2086], [2093, 2093], [2096, 2098], [2105, 2132], [2169, 2170], [2176, 2180],
    [2193, 2193], [2200, 2200], [2206, 2212], [2319, 2319], [2320, 2462], [2523, 2549], [2551, 2735], [2738, 2758],
    [2935, 2941], [2953, 2953], [3006, 3030], [3034, 3035], [3058, 3059], [3068, 3068], [3114, 3118], [3126, 3138],
    [3300, 3301], [3328, 3335], [3346, 3346], [3350, 3352], [3366, 3366], [3416, 3416], [20004, 20032], [20064, 20092],
    [21413, 21423], [21473, 21483], [21896, 21899], [22171, 22177], [22181, 22187], [22191, 22197], [25884, 25884],
    [27205, 27232], [27391, 27398], [27492, 27492], [28402, 28432], [28462, 28492], [30161, 30179], [30800, 30800],
    [31251, 31259], [31275, 31279], [31281, 31290], [31466, 31700] ]

});

/*
 *==============================================================================
 * GEORSS support
 *==============================================================================
 */

dojo.provide("esri.gpt.layers.GeorssLayer");

/** GEORSS layer **/
dojo.declare("esri.gpt.layers.GeorssLayer", esri.layers.GraphicsLayer, {
  _proxyUrl: "",
  _url : "",
  _gs : null,
  _map : null,

  /** constructs layer **/
  constructor: function(url, proxyUrl, gsUrl) {
    /** hitch methods */
    this.init = dojo.hitch(this, this.init);
    this._initialize = dojo.hitch(this, this._initialize);
    this._parseResponse = dojo.hitch(this, this._parseResponse);
    this._drawGeometry = dojo.hitch(this, this._drawGeometry);
    this._drawSinglePoint = dojo.hitch(this, this._drawSinglePoint);
    this._drawSinglePointByCoords = dojo.hitch(this, this._drawSinglePointByCoords);
    this._drawSinglePolygon = dojo.hitch(this, this._drawSinglePolygon);
    this._drawSingleBox = dojo.hitch(this, this._drawSingleBox);
    this._drawSingleEnvelope = dojo.hitch(this, this._drawSingleEnvelope);
    this._getNodeTextValue = dojo.hitch(this, this._getNodeTextValue);
    this._addToMap = dojo.hitch(this, this._addToMap);

    /** store URL's **/
    this._proxyUrl = proxyUrl;
    this._url = url;

    /** create geometry service **/
    if ((gsUrl != null) && (gsUrl.length > 0)) {
     this._gs = new esri.tasks.GeometryService(gsUrl);
    }
  },

  /** initializes layer **/
  init: function(map) {
    this._map = map;
    /** initialize layer **/
    this._initialize();
  },

  _addToMap: function(graphics,markerSymbol,infoTemplate) {
    try {
      if (this._gs!=null && this._map!=null && graphics.spatialReference.wkid!=this._map.spatialReference.wkid) {
        this._gs.project([ graphics ], this._map.spatialReference, dojo.hitch(this, function(gfx) {
          var geometry = new esri.Graphic(gfx[0], markerSymbol);
          geometry.setInfoTemplate(infoTemplate);
          this._map.graphics.add(geometry);
        }));
      } else {
        var geometry = new esri.Graphic(graphics, markerSymbol);
        geometry.setInfoTemplate(infoTemplate);
        this._map.graphics.add(geometry);
      }
    } catch (e) {}
  },

  /** initializes layer */
  _initialize: function() {
    dojo.xhrGet({
      url: this._proxyUrl + "?" + this._url,
      timeout: 30000,
      sync: true,
      load: this._parseResponse,
      error: gpt.LiveData.handleHttpError
    });
  },

  /** parses GEORSS feed */
  _parseResponse: function(response, io) {
    try {
      response = dojo.trim(response);

      if (response.length>0) {
        response = gpt.LiveData.parseXml(response);
        /** create symbols **/
        var markerSymbol = new esri.symbol.SimpleMarkerSymbol();
        var fillSymbol = new esri.symbol.SimpleFillSymbol(
                            esri.symbol.SimpleFillSymbol.STYLE_SOLID,
                            new esri.symbol.SimpleLineSymbol(
                                esri.symbol.SimpleLineSymbol.STYLE_SOLID,
                                new dojo.Color([255,0,0]), 2),
                                new dojo.Color([255,255,0,0.05]));

        /** find entries (Atom), and items (Rss) **/
        var entries = response.getElementsByTagName("entry");
        var items = response.getElementsByTagName("item");

        /** draw geometry of each placemark **/
        var v;
        for (v=0; v<entries.length; v++) {
          this._drawGeometry(entries[v],markerSymbol,fillSymbol);
        }
        for (v=0; v<items.length; v++) {
          this._drawGeometry(items[v],markerSymbol,fillSymbol);
        }
      }

      /** mark layer as loaded **/
      this.loaded = true;
      this.onLoad(this);
    } catch (e) {
      // handle exception, display information if verbose mode
      gpt.LiveData.handleInternalException(e);
    }
  },

  /** draws a single point **/
  _drawSinglePoint: function(node,markerSymbol,infoTemplate) {
      var pair = this._getNodeTextValue(node).split(" ");
      this._drawSinglePointByCoords(pair[1],pair[0],markerSymbol,infoTemplate);
  },

  /** draws a single point by coordinates */
  _drawSinglePointByCoords: function(lon,lat,markerSymbol,infoTemplate) {
      var point = new esri.geometry.Point(lon,lat,new esri.SpatialReference({wkid: 4326}));
      this._addToMap(point, markerSymbol, infoTemplate);
  },

  /** draws single polygon **/
  _drawSinglePolygon: function(node,fillSymbol,infoTemplate) {
      var i, point, rings = [];
      var pair = this._getNodeTextValue(node).split(" ");
      for (i=0; i+1<pair.length; i+=2) {
        point = new esri.geometry.Point(pair[i+1],pair[i],{wkid: 4326});
        rings.push(point);
      }
      var polygon = new esri.geometry.Polygon(new esri.SpatialReference({wkid: 4326}));
      polygon.addRing(rings);
      this._addToMap(polygon, fillSymbol, infoTemplate);
  },

  /** draws single bounding box **/
  _drawSingleBox: function(node,fillSymbol,infoTemplate) {
      var point, rings = [];
      var pair = this._getNodeTextValue(node).split(" ");
      if (pair.length==4) {
        point = new esri.geometry.Point(pair[1],pair[0],new esri.SpatialReference({wkid: 4326}));
        rings.push(point);

        point = new esri.geometry.Point(pair[3],pair[0],new esri.SpatialReference({wkid: 4326}));
        rings.push(point);

        point = new esri.geometry.Point(pair[3],pair[2],new esri.SpatialReference({wkid: 4326}));
        rings.push(point);

        point = new esri.geometry.Point(pair[1],pair[2],new esri.SpatialReference({wkid: 4326}));
        rings.push(point);

        point = new esri.geometry.Point(pair[1],pair[0],new esri.SpatialReference({wkid: 4326}));
        rings.push(point);

        var polygon = new esri.geometry.Polygon(new esri.SpatialReference({wkid: 4326}));
        polygon.addRing(rings);
        this._addToMap(polygon, fillSymbol, infoTemplate);
      }
  },

  /** draws single enveloope **/
  _drawSingleEnvelope: function(lowerCorner,upperCorner,fillSymbol,infoTemplate) {
      var point, rings = [];
      var lowerCornerPair = this._getNodeTextValue(lowerCorner).textContent.split(" ");
      var upperCornerPair = this._getNodeTextValue(upperCorner).textContent.split(" ");
      if (lowerCornerPair.length==2 && upperCornerPair.length==2) {
        point = new esri.geometry.Point(lowerCornerPair[1],lowerCornerPair[0],new esri.SpatialReference({wkid: 4326}));
        rings.push(point);

        point = new esri.geometry.Point(upperCornerPair[1],lowerCornerPair[0],new esri.SpatialReference({wkid: 4326}));
        rings.push(point);

        point = new esri.geometry.Point(upperCornerPair[1],upperCornerPair[0],new esri.SpatialReference({wkid: 4326}));
        rings.push(point);

        point = new esri.geometry.Point(lowerCornerPair[1],upperCornerPair[0],new esri.SpatialReference({wkid: 4326}));
        rings.push(point);

        point = new esri.geometry.Point(lowerCornerPair[1],lowerCornerPair[0],new esri.SpatialReference({wkid: 4326}));
        rings.push(point);

        var polygon = new esri.geometry.Polygon(new esri.SpatialReference({wkid: 4326}));
        polygon.addRing(rings);
        this._addToMap(polygon, fillSymbol, infoTemplate);
      }
  },

  /** draws entire geometry of the entry or item **/
  _drawGeometry: function(node,markerSymbol,fillSymbol) {
      var points = [], polygons = [], boxes = [], lowerCorners = [], upperCorners = [];
      var lat = [], lon = [];

      /** create info **/
      var titles = [], descriptions = [],  summaries = [];
      titles = node.getElementsByTagName("title");
      descriptions = node.getElementsByTagName("description");
      summaries = node.getElementsByTagName("summary");

      var title = titles.length>0 && titles[0].firstChild!=null? titles[0].firstChild.nodeValue : "";
      var description = descriptions.length>0 && descriptions[0].firstChild!=null? descriptions[0].firstChild.nodeValue : "";
      var summary = summaries.length>0 && summaries[0].firstChild!=null? summaries[0].firstChild.nodeValue : "";

      var infoTemplate = new esri.InfoTemplate(title, description.length>0? description: summary);

      /** draw point if any **/
      points = gpt.LiveData.getElementsByTagName(node,"georss", "point");
      if (points.length>0) {
        this._drawSinglePoint(points[0],markerSymbol,infoTemplate);
      }
      points = gpt.LiveData.getElementsByTagName(node,"gml","pos");
      if (points.length>0) {
        this._drawSinglePoint(points[0],markerSymbol,infoTemplate);
      }
      /** points given as geo:lat, geo:long */
      lat = gpt.LiveData.getElementsByTagName(node,"geo","lat");
      lon = gpt.LiveData.getElementsByTagName(node,"geo","long");
      if (lat.length>0 && lon.length>0 && lat[0]!=null && lat[0].firstChild!=null && lon[0]!=null && lon[0].firstChild!=null) {
        this._drawSinglePointByCoords(lon[0].firstChild.nodeValue,lat[0].firstChild.nodeValue,markerSymbol,infoTemplate);
      }

      /** draw polygon if any **/
      polygons = gpt.LiveData.getElementsByTagName(node,"georss","polygon");
      if (polygons.length>0) {
        this._drawSinglePolygon(polygons[0],fillSymbol,infoTemplate);
      }
      polygons = gpt.LiveData.getElementsByTagName(node,"gml","posList");
      if (polygons.length>0) {
        this._drawSinglePolygon(polygons[0],fillSymbol,infoTemplate);
      }

      /** draw box if any **/
      boxes = gpt.LiveData.getElementsByTagName(node,"georss","box");
      if (boxes.length>0) {
        this._drawSingleBox(boxes[0],fillSymbol,infoTemplate);
      }

      /** draw envelope if any **/
      lowerCorners = gpt.LiveData.getElementsByTagName(node,"gml","lowerCorner");
      upperCorners = gpt.LiveData.getElementsByTagName(node,"gml","upperCorner");
      if (lowerCorners.length>0 && upperCorners.length>0) {
        this._drawSingleEnvelope(lowerCorners[0],upperCorners[0],fillSymbol,infoTemplate);
      }
  },

  _getNodeTextValue: function(node) {
    return node!=null && node.firstChild!=null? node.firstChild.nodeValue: "";
  }
});


/*
 *==============================================================================
 * KML support
 *==============================================================================
 */

dojo.provide("esri.gpt.layers.KmlLayer");

/** KML Layer **/
dojo.declare("esri.gpt.layers.KmlLayer", esri.layers.GraphicsLayer, {
  _proxyUrl: "",
  _url : "",
  _gs : null,
  _map : null,

  /** constructs layer **/
  constructor: function(url, proxyUrl, gsUrl) {
    /** hitch methods */
    this.init = dojo.hitch(this, this.init);
    this._initialize = dojo.hitch(this, this._initialize);
    this._parseResponse = dojo.hitch(this, this._parseResponse);
    this._drawGeometry = dojo.hitch(this, this._drawGeometry);
    this._drawSinglePoint = dojo.hitch(this, this._drawSinglePoint);
    this._drawSinglePolygon = dojo.hitch(this, this._drawSinglePolygon);
    this._getNodeTextValue = dojo.hitch(this, this._getNodeTextValue);
    this._addToMap = dojo.hitch(this, this._addToMap);

    /** store URL's **/
    this._proxyUrl = proxyUrl;
    this._url = url;

    /** create geometry service **/
    if ((gsUrl != null) && (gsUrl.length > 0)) {
     this._gs = new esri.tasks.GeometryService(gsUrl);
    }
  },

  _addToMap: function(graphics,markerSymbol,infoTemplate) {
    try {
      if (this._gs!=null && this._map!=null && graphics.spatialReference.wkid!=this._map.spatialReference.wkid) {
        this._gs.project([ graphics ], this._map.spatialReference, dojo.hitch(this, function(gfx) {
          var geometry = new esri.Graphic(gfx[0], markerSymbol);
          geometry.setInfoTemplate(infoTemplate);
          this._map.graphics.add(geometry);
        }));
      } else {
        var geometry = new esri.Graphic(graphics, markerSymbol);
        geometry.setInfoTemplate(infoTemplate);
        this._map.graphics.add(geometry);
      }
    } catch (e) {}
  },

  /** initializes layer **/
  init: function(map) {
    this._map = map;
    /** initialize layer **/
    this._initialize();
  },

  /** initializes layer */
  _initialize: function() {
    dojo.xhrGet({
      url: this._proxyUrl + "?" + this._url,
      timeout: 30000,
      sync: true,
      load: this._parseResponse,
      error: gpt.LiveData.handleHttpError
    });
  },

  /** parses KML capabilities */
  _parseResponse: function(response, io) {
    try {
      response = dojo.trim(response);
      if (response.length>0) {
        response = gpt.LiveData.parseXml(response);
        /** create symbols **/
        var markerSymbol = new esri.symbol.SimpleMarkerSymbol();
        var fillSymbol = new esri.symbol.SimpleFillSymbol(
                            esri.symbol.SimpleFillSymbol.STYLE_SOLID,
                            new esri.symbol.SimpleLineSymbol(
                                esri.symbol.SimpleLineSymbol.STYLE_SOLID,
                                new dojo.Color([255,0,0]), 2),
                                new dojo.Color([255,255,0,0.05]));

        /** find placemarks **/
        var placemarks = response.getElementsByTagName("Placemark");

        /** draw geometry of each placemark **/
        var v;
        for (v=0; v<placemarks.length; v++) {
          this._drawGeometry(placemarks[v],markerSymbol,fillSymbol);
        }
      }

      /** mark layer as loaded **/
      this.loaded = true;
      this.onLoad(this);
    } catch (e) {
      // handle exception, display information if verbose mode
      gpt.LiveData.handleInternalException(e);
    }
  },

  /** draw single point **/
  _drawSinglePoint: function(pointNode,markerSymbol,infoTemplate) {
    var pair, point, graphic;
    var coordinatesNodes = pointNode.getElementsByTagName("coordinates");
    if (coordinatesNodes.length>0) {
      pair = this._getNodeTextValue(coordinatesNodes[0]).split(",");
    } else {
      pair = this._getNodeTextValue(pointNode).split(",");
    }
    if (pair!=null && pair.length>=2) {
      point = new esri.geometry.Point(pair[0],pair[1],new esri.SpatialReference({wkid: 4326}));
      this._addToMap(point,markerSymbol,infoTemplate);
    }
  },

  /** draw single polygon **/
  _drawSinglePolygon: function(polygonNode,fillSymbol,infoTemplate) {
      var i, point, pairs = [], pair = [], rings = [];
      var outerBoundaryIs = polygonNode.getElementsByTagName("outerBoundaryIs");
      if (outerBoundaryIs.length>0) {
        var linearRingNodes = outerBoundaryIs[0].getElementsByTagName("LinearRing");
        if (linearRingNodes.length>0) {
          var coordinatesNodes = linearRingNodes[0].getElementsByTagName("coordinates");
          if (coordinatesNodes.length>0) {
            pairs = this._getNodeTextValue(coordinatesNodes[0]).split(/\s+/);
            for (i=0; i<pairs.length; i++) {
              pair = pairs[i].split(",");
              if (pair.length>=2) {
                point = new esri.geometry.Point(pair[0],pair[1],new esri.SpatialReference({wkid: 4326}));
                rings.push(point);
              }
            }
          }
        }
      }
      var polygon = new esri.geometry.Polygon(new esri.SpatialReference({wkid: 4326}));
      polygon.addRing(rings);
      this._addToMap(polygon,fillSymbol,infoTemplate);
  },

  /** draws entire geometry of the placemark **/
  _drawGeometry: function(placemarkNode,markerSymbol,fillSymbol) {
      var points = [], polygons = [];

      /** create info **/
      var names = [], descriptions = [];
      names = placemarkNode.getElementsByTagName("name");
      descriptions = placemarkNode.getElementsByTagName("description");

      var name = names.length>0 && names[0].firstChild!=null? names[0].firstChild.nodeValue : "";
      var description = descriptions.length>0 && descriptions[0].firstChild!=null? descriptions[0].firstChild.nodeValue : "";

      var infoTemplate = new esri.InfoTemplate(name, description);

      /** draw all points **/
      points = placemarkNode.getElementsByTagName("Point");
      if (points.length>0) {
        this._drawSinglePoint(points[0],markerSymbol,infoTemplate);
      }

      /** draw all polygons */
      polygons = placemarkNode.getElementsByTagName("Polygon");
      if (polygons.length>0) {
        this._drawSinglePolygon(polygons[0],fillSymbol,infoTemplate);
      }
  },

  _getNodeTextValue: function(node) {
    return node!=null && node.firstChild!=null? node.firstChild.nodeValue: "";
  }

});


/*
 *==============================================================================
 * SOS support
 *==============================================================================
 */

gpt.LiveData.sosSelectGraph = function() {
  var p = 0, f = 0, n = 0;
  var observedProperty = document.getElementById("observedProperty");
  var featureOfInterest = document.getElementById("featureOfInterest")
  if (observedProperty!=null && featureOfInterest!=null) {
    for (n=0; n<observedProperty.options.length; n++) {
      if (observedProperty.options[n].selected) {
        p = n;
        break;
      }
    }
    for (n=0; n<featureOfInterest.options.length; n++) {
      if (featureOfInterest.options[n].selected) {
        f = n;
        break;
      }
    }
    for (var i=0; i<observedProperty.options.length; i++) {
      for (var j=0; j<featureOfInterest.options.length; j++) {
        var s = document.getElementById("sensor-"+i+"-"+j);
        if (s!=null) {
          s.style.display = i==p & j==f? "block": "none";
        }
      }
    }
  }
}

/** SOS Layer **/
dojo.provide("esri.gpt.layers.SOSLayer");

dojo.declare("esri.gpt.layers.SOSLayer", esri.layers.GraphicsLayer, {
  _contextUrl: "",
  _proxyUrl: "",
  _url : "",
  _gs : null,
  _map : null,

  /** constructs layer **/
  constructor: function(contextUrl, url, proxyUrl, gsUrl) {
    /** hitch methods */
    this.init = dojo.hitch(this, this.init);
    this._parseCapabilities = dojo.hitch(this, this._parseCapabilities);
    this._addToMap = dojo.hitch(this, this._addToMap);
    this._addPoint = dojo.hitch(this, this._addPoint);
    this._createReadingImage = dojo.hitch(this, this._createReadingImage);
    this._createFullContent = dojo.hitch(this, this._createFullContent);
    this._findUrl = dojo.hitch(this, this._findUrl);
    this._processOffering = dojo.hitch(this, this._processOffering);

    /** store URL's **/
    this._contextUrl = contextUrl;
    this._proxyUrl = proxyUrl;
    this._url = url;

    /** create geometry service **/
    if ((gsUrl != null) && (gsUrl.length > 0)) {
     this._gs = new esri.tasks.GeometryService(gsUrl);
    }
  },


  /** initializes layer **/
  init: function(map) {
    this._map = map;
    /** initialize layer **/
    this._initialize();
  },

  _addToMap: function(graphics,markerSymbol,infoTemplate) {
    try {
      if (this._gs!=null && this._map!=null && graphics.spatialReference.wkid!=this._map.spatialReference.wkid) {
        this._gs.project([ graphics ], this._map.spatialReference, dojo.hitch(this, function(gfx) {
          var geometry = new esri.Graphic(gfx[0], markerSymbol);
          geometry.setInfoTemplate(infoTemplate);
          this._map.graphics.add(geometry);
        }));
      } else {
        var geometry = new esri.Graphic(graphics, markerSymbol);
        geometry.setInfoTemplate(infoTemplate);
        this._map.graphics.add(geometry);
      }
    } catch (e) {}
  },

  /** initializes layer */
  _initialize: function() {
    dojo.xhrGet({
      url: this._proxyUrl + "?" + this._url,
      timeout: 30000,
      sync: true,
      load: this._parseCapabilities,
      error: gpt.LiveData.handleHttpError
    });
  },


  /** parses SOS capabilities */
  _parseCapabilities: function(response, io) {
    try {
      response = dojo.trim(response);
      if (response.length>0) {
        response = gpt.LiveData.parseXml(response);

        var markerSymbol = new esri.symbol.SimpleMarkerSymbol();
        markerSymbol.setStyle(esri.symbol.SimpleMarkerSymbol.STYLE_DIAMOND);

        var postUrl = this._findUrl(response, "GetObservation");
        var foiUrl = this._findUrl(response, "GetFeatureOfInterest");

        var observationOfferingNodes = gpt.LiveData.getElementsByTagName(response, "sos", "ObservationOffering");

        for (var ooi = 0; ooi<observationOfferingNodes.length; ooi++) {
          this._processOffering(postUrl, foiUrl, markerSymbol, observationOfferingNodes[ooi]);
        }
      }

      /** mark layer as loaded **/
      this.loaded = true;
      this.onLoad(this);
    } catch (e) {
      // handle exception, display information if verbose mode
      gpt.LiveData.handleInternalException(e);
    }
  },

  _addPoint: function(point, markerSymbol, caption, content) {
    var infoTemplate = new esri.InfoTemplate(caption, content);
    this._addToMap(point, markerSymbol, infoTemplate);
  },

  _createReadingImage: function(postUrl,name,observedProperty,featureOfInterest,responseFormat,beginPosition,endPosition) {
      var content = "<img alt=\"reading\" src=\""+this._contextUrl+"/SimpleSOSBridge?" +
        "width=210&height=90" +
        "&url=" +encodeURIComponent(postUrl)+
        "&method=POST&" +
        "offeringName=" +encodeURIComponent(name)+
        "&observedProperty=" +encodeURIComponent(observedProperty)+
        "&featureOfInterest=" +encodeURIComponent(featureOfInterest)+
        "&responseFormat=" + encodeURIComponent(responseFormat) +
        "&beginPosition=" +encodeURIComponent(beginPosition)+ "&endPosition=" +encodeURIComponent(endPosition)+ "\"/>";
      return content;
  },


  _createFullContent: function(postUrl,name,observedProperty,featureOfInterest,responseFormat,beginPosition,endPosition) {
    var i, j;
    var content = "";

    content += "<div>";

    //content += "<label for=\"observedProperty\">Prop:</label>";
    content += "<select id=\"observedProperty\" onchange=\"gpt.LiveData.sosSelectGraph();\">";
    for (i=0; i<observedProperty.length; i++) {
      var t = observedProperty[i];
      var lc = Math.max(t.lastIndexOf(":"),t.lastIndexOf("#"));
      if (lc>=0) t = t.substr(lc+1);
      content += "<option>" + t + "</option>";
    }
    content += "</select>";

    content += "<label for=\"ofeatureOfInterest\"> at: </label>";
    content += "<select id=\"featureOfInterest\" onchange=\"gpt.LiveData.sosSelectGraph();\">";
    for (i=0; i<featureOfInterest.length; i++) {
      var p = featureOfInterest[i];
      var pc = Math.max(p.lastIndexOf(":"),p.lastIndexOf("#"));
      if (pc>=0) p = p.substr(pc+1);
      content += "<option>" + p + "</option>";
    }
    content += "</select>";

    content += "</div>";

    for (i=0; i<observedProperty.length; i++) {
      for (j=0; j<featureOfInterest.length; j++) {
        var style= i==0 & j==0? "display: block": "display: none";
        content += "<div id=\"sensor-" +i+ "-" +j+ "\" style=\"" +style+ "\">";
        content += this._createReadingImage(postUrl, name, observedProperty[i], featureOfInterest[j], responseFormat, beginPosition, endPosition);
        content += "</div>";
      }
    }

    return content;
  },

  _findUrl: function(response,operation) {
    var operationNode = null;
    var parameterNodes = gpt.LiveData.getElementsByTagName(response, "ows", "Operation");
    for (var pi=0; pi<parameterNodes.length; pi++) {
      if (parameterNodes[pi].getAttribute("name")==operation) {
        operationNode = parameterNodes[pi];
        break;
      }
    }

    var operationUrl = "";
    if (operationNode!=null) {
      var postUrlNodes = gpt.LiveData.getElementsByTagName(operationNode,"ows","Post");
      if (postUrlNodes.length>0) {
        operationUrl = postUrlNodes[0].getAttribute("xlink:href");
      }
    }

    return operationUrl;
  },

  _processOffering: function(postUrl, foiUrl, markerSymbol, offering) {
    // name
    var nameNodes = gpt.LiveData.getElementsByTagName(offering, "gml", "name");
    var name = nameNodes.length>0 && nameNodes[0].firstChild!=null? nameNodes[0].firstChild.nodeValue: "";

    // description
    var descNodes = gpt.LiveData.getElementsByTagName(offering, "gml", "description");
    var description = descNodes.length>0 && descNodes[0].firstChild!=null? descNodes[0].firstChild.nodeValue: "";

    // format
    var responseFormats = gpt.LiveData.getElementsByTagName(offering, "sos", "responseFormat");
    var responseFormat = responseFormats.length>0 && responseFormats[0].firstChild!=null? responseFormats[0].firstChild.nodeValue: "";

    // start time and endi time
    var beginPositions = gpt.LiveData.getElementsByTagName(offering, "gml", "beginPosition");
    var endPositions = gpt.LiveData.getElementsByTagName(offering, "gml", "endPosition");

    var beginPosition = beginPositions.length>0 && beginPositions[0].firstChild!=null? beginPositions[0].firstChild.nodeValue: "";
    var endPosition = endPositions.length>0 && endPositions[0].firstChild!=null? endPositions[0].firstChild.nodeValue: "";

    // observed properties
    var observedProperty = [];
    var observedPropertyNodes = gpt.LiveData.getElementsByTagName(offering, "sos", "observedProperty");
    for (var opi = 0; opi<observedPropertyNodes.length; opi++) {
      observedProperty.push(observedPropertyNodes[opi].getAttribute("xlink:href"));
    }

    // features of interest
    var featureOfInterest = [];
    var featureOfInterestNodes = gpt.LiveData.getElementsByTagName(offering, "sos", "featureOfInterest");
    for (var foii = 0; foii<featureOfInterestNodes.length; foii++) {
      featureOfInterest.push(featureOfInterestNodes[foii].getAttribute("xlink:href"));
    }

    // center of the offering envelope
    var lowerCornerNodes = gpt.LiveData.getElementsByTagName(offering, "gml", "lowerCorner");
    var upperCornerNodes = gpt.LiveData.getElementsByTagName(offering, "gml", "upperCorner");
    if (lowerCornerNodes.length>0 && upperCornerNodes.length>0) {
      var lowerCorner = lowerCornerNodes[0].firstChild.nodeValue.split(" ");
      var upperCorner = upperCornerNodes[0].firstChild.nodeValue.split(" ");
      var x1 = parseFloat(lowerCorner[0]);
      var y1 = parseFloat(lowerCorner[1]);
      var x2 = parseFloat(upperCorner[0]);
      var y2 = parseFloat(upperCorner[1]);
      var point = new esri.geometry.Point(y1 + (y2-y1)/2.0, x1 + (x2-x1)/2.0, new esri.SpatialReference({wkid: 4326}));

      var content = this._createFullContent(postUrl,name,observedProperty,featureOfInterest,responseFormat,beginPosition,endPosition);
      this._addPoint(point, markerSymbol, description!=null && description.length>0? description: name, content);
    }
  }

});