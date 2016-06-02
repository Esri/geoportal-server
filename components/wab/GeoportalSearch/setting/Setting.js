define([
    'dojo/_base/declare',
    "dojo/_base/lang",
    "dojo/_base/array",
    'dojo/_base/html',
    "dojo/on",
    "dojo/dom-style",
    "dojo/dom-attr",
    "dojo/dom-construct",
    'dijit/_WidgetsInTemplateMixin',
    'jimu/BaseWidgetSetting',
    'dijit/form/ValidationTextBox', 
    'jimu/dijit/SimpleTable',
		'jimu/dijit/Message',
    'dojo/Deferred',
    'dojo/promise/all',
    'dojo/request/xhr',
    'dojo/request/script',
    'jimu/utils',
    'jimu/tokenUtils',
    'jimu/portalUtils',
    'jimu/portalUrlUtils',
    'esri/IdentityManager',
    'esri/request',
  ],
  function(declare,
    lang,
    array,
    html,
    on,
    domStyle,
    domAttr,
    domConstruct,
    _WidgetsInTemplateMixin,BaseWidgetSetting, 
    ValidationTextBox, SimpleTable,Message,
    Deferred, all, xhr, script, jimuUtils, tokenUtils,
    portalUtils, portalUrlUtils, IdentityManager,esriRequest) {

    return declare([BaseWidgetSetting, _WidgetsInTemplateMixin], {
      baseClass: 'geoportal-search-widget-setting',
      portalUrl: null,
      sharingUrl: null,
      defSignIn: null,
      mapPortalUrl: null,
      postCreate: function(){
        console.log("startup");
        this.inherited(arguments);
        this.defaultsPopulated = false;

        this.mapPortalUrl = this.appConfig.map.portalUrl;
        this.portalUrl = portalUrlUtils.getPortalUrlFromLocation();
        this.sharingUrl = portalUrlUtils.getSharingUrl(this.portalUrl);
       // this.defSignIn = IdentityManager.checkSignInStatus(this.sharingUrl);

        this.own(on(this.btnAdd,'click',lang.hitch(this,this._addFieldRow)));
        this.own(on(this.catalogsTable,'row-delete',lang.hitch(this,function(tr){
          if(tr.select){
            tr.select.destroy();
            delete tr.select;
          }
        })));  
        this.setConfig(this.config);                                   
      },

      setConfig: function(config) {
          console.log("SetConfig");
          this.config = config;  
          this._setCatalogsTable();          
      },      

    _setCatalogsTable:function(){
        console.log("inside _setCatalogsTable");
        var catalogs = this.config.catalogs;
        this.catalogsTable.clear();
        this.recordsPerPage.set("value", this.config.recordsPerPage);
        array.forEach(catalogs,lang.hitch(this,function(catalog){                   
            this._populateCatalogTableRow(catalog);            
        }));
      },

      _populateCatalogTableRow:function(catalog){
        console.log("inside _populateCatalogTableRow");
        var result = this.catalogsTable.addRow({});
        if(result.success && result.tr){
          var tr = result.tr;
          this._addUrl(tr,true);
          this._addTitle(tr);

          tr.urlText.set("value",catalog.url);
          tr.titleText.set("value",catalog.name);
        }        
      },
      
      _addFieldRow:function(){
        console.log("inside _addFieldRow");
        var result = this.catalogsTable.addRow({});
        if(result.success && result.tr){
          var tr = result.tr;
          this._addUrl(tr,false);        
          this._addTitle(tr);
        }
      },

      _addUrl: function(tr,isConfig){       
          var instance = this;   
          var td = dojo.query('.simple-table-cell',tr)[1];
          html.setStyle(td,"verticalAlign","middle");
          var urlTextBox = new ValidationTextBox({style:{
                      width:"100%",
                      height:"30px"
                    }});
          urlTextBox.placeAt(td);
          urlTextBox.startup();
          tr.urlText = urlTextBox;  
         
          /*this.own(on(tr.urlText, 'change', lang.hitch(this, function(evt){            
            var itemDataUrl = portalUrlUtils.getItemUrl(this.mapPortalUrl, evt) + '?f=json'; // this.portalUrl portalUrlUtils.getItemDataUrl
            this._fetchItemIData(tr,itemDataUrl,isConfig);            
          })));*/        
      },

      _addTitle: function(tr){          
          var td = dojo.query('.simple-table-cell',tr)[0];
          html.setStyle(td,"verticalAlign","middle");
          var titleTextBox = new ValidationTextBox({style:{
            width:"100%",
            height:"30px"
          }});
          titleTextBox.placeAt(td);
          titleTextBox.startup();
          tr.titleText = titleTextBox;
      },

      _fetchItemIData: function(tr,itemDataUrl,isConfig){
        var nls = this.nls;
        esriRequest({url: itemDataUrl}).then(function(response){
          if(!isConfig){
           tr.titleText.set("value",response.title);               
          }
        }, function(err){
            console.error(err);
            new Message({
                message: nls.invalidUrl
            });         
        });
      },

      _requestWithSignIn: function(tr,itemDataUrl,isConfig){
        var nls = this.nls;
        var def = new Deferred();
        defSignIn.promise.always(function(){
              esriRequest({url: itemDataUrl}).then(function(response){
                if(!isConfig){
                 tr.titleText.set("value",response.title);               
                }
                def.resolve(response);                
              }, function(err){
                  console.error(err);
                  new Message({
                      message: nls.invalidUrl
                  });
                 def.reject();
              });
            });
      },

      _populateRepositories: function(response){
        var catalogUrl = this.catalogUrl.value;
        array.forEach(response.rows, lang.hitch(this, function(row, index){
          var catalog = { "name": row.name, "url": catalogUrl + "/rest/find/document?rid=" + row.id}
          this._populateCatalogTableRow(catalog); 
        }));
      },

      _fetchCatalogs: function(){
        var getCatalogsUrl = this.catalogUrl.value + "/rest/repositories?protocol=all";
        esriRequest({
          url: getCatalogsUrl,
          handleAs: "text"
        }).then(lang.hitch(this,function(data){
           if(typeof(data) == 'undefined' || data == null) {
            data = "";
           }
           var catalogs = dojo.eval("[{" + data + "}]");
           if(typeof(catalogs.length) != 'undefined'
             && catalogs.length == 1) {
             catalogs = catalogs[0];
             
           }
           if(typeof(catalogs.rows) == 'undefined') {
             catalogs.rows = new Array();
           }
          this._populateRepositories(catalogs); 
        }), lang.hitch(this,function(err){
            console.error(err);
            new Message({
                message: this.nls.invalidUrl
            });         
        }));
          
      },
     
      getConfig: function() {
      	console.log("GetConfig");
        
        this.config.recordsPerPage = this.recordsPerPage.get("value");
        
        this.config.catalogs = new Array();
        var trs = this.catalogsTable.getRows();
        var flds = new Array();
        array.forEach(trs,lang.hitch(this,function(tr){
          var urlText = tr.urlText;
          var titleText = tr.titleText;
          var field = {
            url:  urlText.value,
            name: titleText.value
           
          };
          this.config.catalogs.push(field);
        }));

        return this.config;
      }
     
    });
  });