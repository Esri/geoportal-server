define([
  'dojo/_base/declare',
  'dojo/_base/lang',
  'dijit/_WidgetBase',
  'dijit/_TemplatedMixin',
  'dijit/_WidgetsInTemplateMixin',
  'dojo/text!./RecordInfo.html',
  'dojo/i18n!widgets/GeoportalSearch/nls/strings',
  'widgets/GeoportalSearch/common/Util',
  'widgets/GeoportalSearch/common/LayerFactory',
],function(declare,lang,_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin, template, nls, 
           util, LayerFactory){
  return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
    baseClass: 'geoportal-search-record',
    templateString: template,
    nls: nls,
    
     postCreate: function() {
       this.inherited(arguments);
     },
     
     onAdd: function(evt) {
       LayerFactory.createLayer(this.getUrl()).then(lang.hitch(this,function(layer){
        if (layer!=null) {
         util.publishMessage(nls.messageAddingLayer);
         layer.on("load", lang.hitch(this,this.onLayerLoaded));
         layer.on("error", lang.hitch(this,this.onLayerError));
         this.emit("addlayer-add",{layer: layer});
         console.log("Adding:", this.getUrl());
        } else {
         util.publishMessage(nls.messageAddingError);
        }
       }));
     },
     
     getUrl: function() {
       return this.record.url;
     },
     
     onLayerLoaded: function() {
       util.publishMessage(nls.messageAddingSuccess);
       console.log("Layer loaded");
     },
     
     onLayerError: function(error) {
       util.publishMessage(nls.messageAddingError);
       console.error("Layer error", error);
     }
  });
});