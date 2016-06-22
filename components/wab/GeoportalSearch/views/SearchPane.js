define([
  'dojo/_base/declare',
  'dojo/_base/lang',
  'dijit/_WidgetBase',
  'dijit/_TemplatedMixin',
  'dijit/_WidgetsInTemplateMixin',
  'dojo/text!./SearchPane.html',
  'dojo/i18n!widgets/GeoportalSearch/nls/strings',
  'widgets/GeoportalSearch/common/Util',
  'widgets/GeoportalSearch/common/Query',
  'widgets/GeoportalSearch/common/QueryTask',
  "esri/geometry/webMercatorUtils",
  'esri/layers/GraphicsLayer',

  'esri/arcgis/utils',
  'dojo/json',
  'dojo/_base/lang',
  'dojo/_base/html',
  'dojo/_base/array',
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
    Query, QueryTask,webMercatorUtils,
    GraphicsLayer, arcgisUtils,JSON,
    lang, html, array,
    mouse, on, Memory,ObjectStore,
    topic){
  return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
    baseClass: 'geoportal-search-view',
    templateString: template,
    nls: nls,
    portal: null,

     postCreate: function() {
       this.inherited(arguments);
        this._initUI();
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

     onKeyDown: function(evt) {
       if (evt.keyCode==13 ){ // && !this._searchButton.get('disabled')) {
         this.onSearch();
       }
     },

     onTextChanged: function(evt) {
       //this._searchButton.setDisabled(this.getText().length==0);
     },

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

        var query = new Query();
        query.queryUrl = this.inputQueryCatalog.value;
        query.start=1;
        if(this.inputQueryLiveDataFilter.checked){
           query.contentType = 'liveData';
        }
        if(this.inputQueryName.value.length > 0){
           query.searchText = this.inputQueryName.value;
        }
        var extent = this.map.extent;
        var geom = extent;
        if (this.map.spatialReference.wkid == 102100) {
          geom = webMercatorUtils.webMercatorToGeographic(extent);
        }
        console.log(geom);
        var bbox = geom.xmin + "," + geom.ymin + "," + geom.xmax + "," + geom.ymax;

        if(this.inputExtentIntersecting.checked){
           query.spatialRel ='esriSpatialRelOverlaps';
           query.bbox = bbox;
        }else if(this.inputExtentFullyWithin.checked){
           query.spatialRel ='esriSpatialRelWithin';
           query.bbox = bbox;
        }

        var queryTask = new QueryTask();
        queryTask.execute(query);
    }

  });
});
