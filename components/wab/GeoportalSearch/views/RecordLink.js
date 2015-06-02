define([
  'dojo/_base/declare',
  'dojo/_base/lang',
  'dijit/_WidgetBase',
  'dijit/_TemplatedMixin',
  'dijit/_WidgetsInTemplateMixin',
  'dojo/text!./RecordLink.html',
  'dojo/i18n!widgets/GeoportalSearch/nls/strings',
  'widgets/GeoportalSearch/common/Util',
  'widgets/GeoportalSearch/common/LayerFactory',
],function(declare,lang,_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin, template, nls, 
           util, LayerFactory){
  return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
    baseClass: 'geoportal-search-recordLink',
    templateString: template,
    nls: nls,
    
     postCreate: function() {
       this.inherited(arguments);
     }

  });
});