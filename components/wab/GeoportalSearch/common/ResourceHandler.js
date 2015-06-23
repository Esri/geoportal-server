define([
  'dojo/_base/declare',
  'dojo/_base/lang',
  'dojo/on',
  'dojo/topic',
  'jimu/dijit/Message',
  'widgets/GeoportalSearch/common/LinksProcessor',
  'widgets/GeoportalSearch/common/LayerFactory',
  'widgets/GeoportalSearch/common/WebmapProcessor'
],function(declare,lang,
           topic,
           on,
           Message,
           LinksProcessor,
           LayerFactory,
           WebmapProcessor){
    return declare(null, {    

		postCreate: function(){
      this.inherited(arguments);
    //  this._attachTopics();
    },

    _attachTopics: function(){
      topic.subscribe("app/appConfigLoaded", lang.hitch(this, this.addResource));
      topic.subscribe("app/appConfigChanged", lang.hitch(this, this.addResource));
    },

    _parseParameters: function(){
      var query = window.location.search;
      if (query.indexOf('?') > -1) {
        query = query.substr(1);
      }
      var pairs = query.split('&');
      var queryObject = {};
      for(var i = 0; i < pairs.length; i++){
        var splits = decodeURIComponent(pairs[i]).split('=');
        var parameterValue = "";
        // loop parameter values in array elements since value may contain '='
        for(j=1; j<splits.length; j++){
          if(parameterValue.length > 0){
            parameterValue += "=";
           }
          parameterValue += splits[j]; 
        }
        queryObject[splits[0]] = parameterValue;
      }
      return queryObject;
    },

		addResource: function(params){
      // debugger;
      var queryObject = this._parseParameters();  // window.queryObject; env.js // <-- did not work well, so using above function
      if(queryObject.resource){
         var resource = queryObject.resource; 
         console.group("Add to map parameters => " + resource);        
         var title = "";
         if(queryObject.title){
          title = decodeURIComponent(queryObject.title);
         }
         var parts = resource.split(":");
         if(!parts && parts.length<2){
           return;
         }

         var linkType = parts[0];
         var href = "";
         // loop parameter values in array elements since value may contain ':'
         for(i=1; i<parts.length; i++){
           if(href.length > 0){
            href += ":";
           }
           href += parts[i]; 
         }
         console.log(href);
         if(href.length ==0){
          return;
         }  
         console.log(linkType);
         if (linkType == "mapserver" || linkType == "featureserver" || linkType == "imageserver" 
          || linkType == "kml" || linkType == "wms") {
          
          LayerFactory.createLayer(href,linkType).then(lang.hitch(this,function(layer){
              layer.on("error",lang.hitch(this,function(error){
                  new Message({
                    message: params.nls._onLayerLoadError
                  });
                  console.debug(error);
              }));
              /*if(title.length > 0){
                layer.attr("id",title);
                layer.attr("name",title);
                layer.attr("title",title);
              }*/
              params.map.addLayer(layer);
              console.log("layer added to map.");
          }));
                 
        } else if (linkType == "agsrest" || linkType == "ags") { 

          var linksProcessor = new LinksProcessor();
          linkType = linksProcessor.getServiceType(href);

          LayerFactory.createLayer(href,linkType).then(lang.hitch(this,function(layer){              
              params.map.addLayer(layer);
              console.log("layer added to map.");
          }));

        }else if (linkType == "webmap") { 
           console.log("webmap processing...");
           var wmProcessor = new WebMapProcessor();
           wmProcessor.process(href,params.map);
           console.log("webmap operational layers added to map.");
        } 
        console.groupEnd();
      }
		}

   });
});