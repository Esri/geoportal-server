define([
  'dojo/_base/declare',
  'dojo/_base/lang',
  'dijit/_WidgetBase',
  'dijit/_TemplatedMixin',
  'dijit/_WidgetsInTemplateMixin',
  'dojo/text!./Record.html',
  'dojo/i18n!widgets/GeoportalSearch/nls/strings',
  'dojo/dom-construct',
  'dojo/dom',
  'widgets/GeoportalSearch/common/Util',
  'widgets/GeoportalSearch/common/LinksProcessor',
  'widgets/GeoportalSearch/views/RecordLink',
  'widgets/GeoportalSearch/views/RecordServiceLink',
  'widgets/GeoportalSearch/views/RecordContentType'
],function(declare,lang,_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin, template, nls, 
    domConstruct,dom,
    util, LinksProcessor,RecordLink,RecordServiceLink,RecordContentType){
  return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
    
    baseClass: 'geoportal-search-record',
    templateString: template,
    nls: nls,
    suffixes: null,
    suffixesKML: null,

    postCreate: function() {
       this.inherited(arguments);              
       this.processRecord();
    },
   
  processRecord: function(){  

    var theLinkType = "";
    var firstLinkType = "";
    var links = this.record.links;
    
    var linksProcessor = new LinksProcessor();
    var contentTypeUrl = linksProcessor.findContentTypeUrl(links);
    this.record.contentTypeUrl = contentTypeUrl;

    for (var j=0; j < links.length; j++) {
      var link = links[j];       
      linkInfo = new RecordLink({record:this.record, link:link});
      linkInfo.startup();
      domConstruct.place(linkInfo.domNode,this.links);        
    } 

    
    var contentTypeIcon = new RecordContentType({record:this.record});
    contentTypeIcon.startup();
    domConstruct.place(contentTypeIcon.domNode,this.contentType);      
  },
     
     getUrl: function() {
       return this.record.url;
     }

  });
});