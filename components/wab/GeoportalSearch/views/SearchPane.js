define([
  'dojo/_base/declare',
  'dojo/_base/lang',
  'dijit/_WidgetBase',
  'dijit/_TemplatedMixin',
  'dijit/_WidgetsInTemplateMixin',
  'dojo/text!./SearchPane.html',
  'dojo/i18n!widgets/GeoportalSearch/nls/strings',
  'widgets/GeoportalSearch/common/Util',
  'esri/layers/GraphicsLayer',
  'esri/request',
  'esri/arcgis/utils',
  'dojo/json',
    'dojo/_base/lang',
    'dojo/_base/html',
    'dojo/_base/array',
    'dojo/query',
    'dojo/mouse',
    'dojo/on',
  'dojo/store/Memory',
  'dojo/data/ObjectStore',
  'dojo/topic',
    'dojo/aspect',
    'dojo/dom-construct',
  'dojo/dom',
  'dijit/form/TextBox',
  'dijit/form/Button',
  'dijit/ProgressBar',
  'dijit/form/TextBox',
  'dijit/form/RadioButton',
  'dijit/form/CheckBox',
  'dijit/form/Select'
],function(declare, lang, _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin, template, nls, util,
  GraphicsLayer,esriRequest, arcgisUtils,JSON,
    lang, html, array,
    query, mouse, on, Memory,ObjectStore,
    topic){
  return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
    baseClass: 'geoportal-search-view',
    templateString: template,
    nls: nls,
    portal: null,
    
     postCreate: function() {
       this.inherited(arguments);
      // this.disableAll(true);
      /* require(['esri/arcgis/Portal'],lang.hitch(this,function(arcgisPortal){
         var config = this.appConfig.getCleanConfig();
         var portalUrl = config.portalUrl;
         this.portal = new arcgisPortal.Portal(portalUrl);
         this.portal.signIn().then(lang.hitch(this,function(result){
           this._searchText.setDisabled(false);
           this._searchButton.setDisabled(this.getText().length==0);
         }));
       }));*/

        this._initUI(); // moved here
     },

     _initUI: function(){
      console.log('_initUI');
      this.resultLayer = new GraphicsLayer();
      this.map.addLayer(this.resultLayer);
      var items = array.map(this.config.catalogs, function (itm, i) {
              return {
                  id: itm.url,
                  label: itm.name
              };
          });

      var catalogsStore = new ObjectStore({
          objectStore: new Memory({
              data: items
          })
      });
      this.inputQueryCatalog.setStore(catalogsStore);
    },
     
     onTextKeyDown: function(evt) {
       if (evt.keyCode==13 && !this._searchButton.get('disabled')) {
         this.handleSearch(evt);
       }
     },
     
     onTextChanged: function(evt) {
       this._searchButton.setDisabled(this.getText().length==0);
     },
     
    /* onSearch: function(evt) {
       util.publishRecords();
       console.log("Searching:", this._searchText.get('value'));
       util.publishMessage(nls.messageSearching);
       this.portal.queryItems({
         q: this.getText() + ' AND (type:"Map Service" OR type:"Image Service" OR type:"Feature Service")', 
         start: 1,
         num: 100
       }).then(
          lang.hitch(this,this.handleResult),
          lang.hitch(this,this.handleError)
       );
     },
     
     handleResult: function(result) {
       util.publishMessage();
       console.log("Result", result);
       util.publishRecords(result.results);
     },
     
     handleError: function(error) {
       util.publishMessage(nls.messageSearchingError);
       console.error("Error", error);
     },
     */

     disableAll: function(disabled) {
       this._searchText.setDisabled(disabled);
       this._searchButton.setDisabled(disabled);
     },
     
     getText: function() {
       return this._searchText.get('value');
     },


     onSearch: function(){
      console.log('onSearch');
      this._search();
    },

    _search: function() {
  
    topic.publish("/widgets/GeoportalSearch/action/switchTab", null, { tab: this.nls.results });
    
    var queryUrl = this.inputQueryCatalog.value;
    var params = { 
                    start:1,
                    max:10,
                    f: 'json'
                  };
    if(this.inputQueryLiveDataFilter.checked){
       lang.mixin(params,{contentType:'liveData'});
    }
    if(this.inputQueryName.value.length > 0){
       lang.mixin(params,{searchText:this.inputQueryName.value});
    }
    var extent = this.map.extent;
   // var webgm = webMercatorUtils.geographicToWebMercator(extent);
    var geom = extent;
    if (this.map.spatialReference.wkid == 10200) {
      geom = webMercatorUtils.webMercatorToGeographic(extent);
    }
    console.log(geom);
    //console.log(webgm);
    var bbox = geom.xmin + "," + geom.ymin + "," + geom.xmax + "," + geom.ymax;
    if(this.inputExtentIntersecting.checked){
       lang.mixin(params,{spatialRel:'esriSpatialRelOverlaps',bbox : bbox});
    }else if(this.inputExtentFullyWithin.checked){      
       lang.mixin(params,{spatialRel:'esriSpatialRelWithin',bbox : bbox});
    }

    var requestHandle = esriRequest({
        "url": queryUrl,
        "content": params,
        handleAs:'json'
      },{
        useProxy:false
      });
    requestHandle.then(this._onQueryFinish, this._onQueryError);

  },

  _onQueryFinish: function(results, io){
    topic.publish("/widgets/GeoportalSearch/action/search", null, { success: true, results: results, io:io });
  },

  _onQueryError:function(error, io){
    topic.publish("/widgets/GeoportalSearch/action/search", null, { success: false, error: error, io:io  });
  }


  });
});