define([
  'dojo/_base/declare',
  'dojo/_base/lang',
  'dojo/topic',
  'esri/request'
],function(declare,lang,
           topic,
           esriRequest){
    return declare(null, {

      query: null,

      execute: function(query){
        this.query = query;
        var requestHandle = esriRequest({
              "url": query.queryUrl,
              "content": query,
              handleAs:'json'
            },{
              useProxy:false
            });
          requestHandle.then(lang.hitch(this,this._onQueryFinish), this._onQueryError);
      },

      _onQueryFinish: function(results, io){
        topic.publish("/widgets/GeoportalSearch/action/search", null, { success: true, results: results, query: this.query, io:io });
      },

      _onQueryError:function(error, io){
        topic.publish("/widgets/GeoportalSearch/action/search", null, { success: false, error: error, io:io  });
      }

   });
});