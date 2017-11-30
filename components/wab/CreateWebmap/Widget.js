define(['dojo/_base/declare', 'jimu/BaseWidget',
  'dojo/_base/lang',
  'dojo/_base/html',
  'dojo/promise/all',
  'esri/request',
  'esri/arcgis/OAuthInfo',
  'esri/IdentityManager'],
function(declare, BaseWidget,
  lang,
  html,
  all,
  esriRequest,
  OAuthInfo, 
  esriId
  ) {
  //To create a widget, you need to derive from BaseWidget.
  return declare([BaseWidget], {
    // DemoWidget code goes here

    //please note that this property is be set by the framework when widget is loaded.
    //templateString: template,

    baseClass: 'jimu-widget-create-webmap',

    postCreate: function() {
      this.inherited(arguments);
      console.log('postCreate');
    },

    startup: function() {
      this.inherited(arguments);

      this.signInResult = null;
      
      console.log('startup');
    },

    onOpen: function(){
      console.log('onOpen');
    },

    onClose: function(){
      console.log('onClose');
    },

    onMinimize: function(){
      console.log('onMinimize');
    },

    onMaximize: function(){
      console.log('onMaximize');
    },

    onSignIn: function(credential){
      /* jshint unused:false*/
      console.log('onSignIn');
    },

    onSignOut: function(){
      console.log('onSignOut');
    },

    _onExecuteSignInClick: function() {

      esriId.getCredential(this.appConfig.portalUrl, {
          oAuthPopupConfirmation: false
      }).then(lang.hitch(this, function (signInResult){

        this.signInResult = signInResult;

        this.signInNameNode.innerHTML = this.signInResult.userId;

        html.setStyle(this.signInNode, 'display', 'none');
        html.setStyle(this.signOutNode, 'display', 'block');
        html.setStyle(this.signInNameNode, 'display', 'block');

      }));
    },

    _onExecuteSignOutClick: function() {
      esriId.destroyCredentials();

      html.setStyle(this.signInNode, 'display', 'block');
      html.setStyle(this.signOutNode, 'display', 'none');
      html.setStyle(this.signInNameNode, 'display', 'none');
      this.signInNameNode.innerHTML = "";
      this.signInResult = null;
    },

    _onCreateWebmapClick: function(evt) {

      this.outputWebmapNode.innerHTML = "";

      var text = "";
      var outputMediaType = "";
      var outputExtension = "";

      if (this.signInResult == null) {
        // Error Check 1: Is user is signed in?
        console.log('Not logged in yet error placeholder');
        this.outputWebmapNode.innerHTML = "<div class='errorNotification'>Error: Please log in to your ArcGIS Online account</div>";
      } else if (this.webMapNameNode.value.trim().length == 0) {
        // Error Check 2: Has a valid Hosted Feature name been set? (not empty, no trailing spaces (trim))
        console.log('Needs a proper service name placeholder');
        this.outputWebmapNode.innerHTML = "<div class='errorNotification'>Error: Please enter a name for your Feature Service</div>";
      } else {

        var jsonContext = this.generateJSON();

        //var accountUrl = this.appConfig.portalUrl + 'sharing/rest/content/users/' + this.signInResult.userId + '/addItem?f=json';
        var accountUrl = this.signInResult.server + "/sharing/rest/content/users/" + this.signInResult.userId + "/addItem";
        // var path = "content/users/" + this.signInResult.userId + "/addItem";

        var addWebMapItemRequest = esriRequest({
          url: accountUrl,
          method: 'post',
          content: {
            f: "json",
            title: this.webMapNameNode.value,
            type: 'Web Map',
            tags: "Generated Web Map",
            access: "public",
            token: this.signInResult.token, 
            text: JSON.stringify(jsonContext.text)
          },
          callbackParamName:'callback'
        }, {usePost: true});

        addWebMapItemRequest.then(
          lang.hitch(this, function(response) { 
            // Link to the hosted feature service AGO content page
            this.outputWebmapNode.innerHTML = "<a target='#' href='"+ this.signInResult.server + 
            '/home/item.html?id=' + response.id +"' >Open Webmap</a>";

          }), lang.hitch(this, function(error) {
              console.log("Add Item Error: ", error.message);
              this.outputWebmapNode.innerHTML = "<div class='errorNotification'>" + error.message + "</div>";
          }));

      } // end else

    },

    generateJSON: function() {

      // based off this spec: https://developers.arcgis.com/web-map-specification/objects/webmap/
      var webmapJSON = {
        "title": this.webMapNameNode.value, // should be user defined since new web map being created
        "description": "Test Add Item",
        "tags": "test", // could use user tags or default tags
        "text": {
            "operationalLayers": [], // handles all layer types properly it seems
            "baseMap": {
                "baseMapLayers": [],
                "title": "Basemap"
            },
            "version": 2,
            "spatialReference": {} // documentation shows as lowercase...
        },
        "token": this.signInResult.token, // grab from user AGO login(?)
        "type": "Web Map" // type should always be web map by default 
      }

      // read through map object to create webmap JSON
      var mapObject = this.map;

      // the "text" of "webmapJSON.text" is to contain the webmap JSON for submission to the "Add Item" Operation

      // set the default webmap infos
      webmapJSON.text.operationalLayers = mapObject.itemInfo.itemData.operationalLayers;
      webmapJSON.text.baseMap = mapObject.itemInfo.itemData.baseMap;
      webmapJSON.text.spatialReference = mapObject.itemInfo.itemData.spatialReference;
      webmapJSON.text.version = mapObject.itemInfo.itemData.version;

      // push additional layers into the correct properties

      // check for map services

      // make an array of webmap only layers to check against
      var webmapLayerIDs = [];

      for (var wol in webmapJSON.text.operationalLayers) {
        webmapLayerIDs.push(webmapJSON.text.operationalLayers[wol].id);
      }

      for (var wbl in webmapJSON.text.baseMap.baseMapLayers) {
        webmapLayerIDs.push(webmapJSON.text.baseMap.baseMapLayers[wbl].id);
      }

      // account for all non feature layers
      var mapLayerIdArray = mapObject.layerIds;

      for (var ml in mapLayerIdArray) {
        var currentLayerID = mapLayerIdArray[ml];
        if (webmapLayerIDs.indexOf(currentLayerID) > -1) {
          console.log('not an added layer, skip it');
        } else {

          // add the "added" layer to the JSON and add the id to the layer check
          var currentLayer = this.map.getLayer(currentLayerID);

          if(currentLayer.infoTemplate != null) {

            var opLayerTemplate = {
              "opacity": currentLayer.opacity,
              "mode": currentLayer.mode,
              "layerType": currentLayer.layerType,
              "title": currentLayer.title,
              "url": currentLayer.url,
              "popupInfo": {
                  "showAttachments": currentLayer.infoTemplate.info.showAttachments,
                  "fieldInfos": currentLayer.infoTemplate.info.fieldInfos,
                  "description": currentLayer.infoTemplate.info.description,
                  "title": currentLayer.infoTemplate.info.title
              },
              "visibility": currentLayer.visibility
            }

          } else {

            var opLayerTemplate = {
              "opacity": currentLayer.opacity,
              "mode": currentLayer.mode,
              "layerType": currentLayer.layerType,
              "title": currentLayer.title,
              "url": currentLayer.url,
              "visibility": currentLayer.visibility
            }

          }
          

          // add the "added" layer to the JSON and add the id to the layer check
          webmapJSON.text.operationalLayers.push(opLayerTemplate);
          webmapLayerIDs.push(currentLayerID);
        }
      }

      var featureLayerIdArray = mapObject.graphicsLayerIds;

      // get rid of default graphics layer
      if (featureLayerIdArray.indexOf('graphicsLayer1') > -1) {
        featureLayerIdArray.splice(featureLayerIdArray.indexOf('graphicsLayer1'), 1);
      }

      // check for all feature layers
      for (var fl in featureLayerIdArray) {
        var currentLayerID = featureLayerIdArray[fl];
        if (webmapLayerIDs.indexOf(currentLayerID) > -1) {
          console.log('not an added feature layer, skip it');
        } else {
          // add the "added" layer to the JSON and add the id to the layer check
          var currentLayer = this.map.getLayer(currentLayerID);

          if(currentLayer.infoTemplate != null) {

            var opLayerTemplate = {
              "opacity": currentLayer.opacity,
              "mode": currentLayer.mode,
              "layerType": currentLayer.layerType,
              "title": currentLayer.title,
              "url": currentLayer.url,
              "popupInfo": {
                  "showAttachments": currentLayer.infoTemplate.info.showAttachments,
                  "fieldInfos": currentLayer.infoTemplate.info.fieldInfos,
                  "description": currentLayer.infoTemplate.info.description,
                  "title": currentLayer.infoTemplate.info.title
              },
              "visibility": currentLayer.visibility
            }

          } else {

            var opLayerTemplate = {
              "opacity": currentLayer.opacity,
              "mode": currentLayer.mode,
              "layerType": currentLayer.layerType,
              "title": currentLayer.title,
              "url": currentLayer.url,
              "visibility": currentLayer.visibility
            }

          }

          webmapJSON.text.operationalLayers.push(opLayerTemplate);
          webmapLayerIDs.push(currentLayerID);
        }
      }

      // clean op layers and basemap layers
      for(var t in webmapJSON.text.operationalLayers) {
        webmapJSON.text.operationalLayers[t].layerObject = null;
      }

      for(var r in webmapJSON.text.baseMap.baseMapLayers) {
        webmapJSON.text.baseMap.baseMapLayers[r].layerObject = null;
      }

      return webmapJSON;

    }

  });
});