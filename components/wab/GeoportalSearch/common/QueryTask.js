define([
  'dojo/_base/declare',
  'dojo/_base/lang',
  'dojo/topic',
  'dojo/io-query',
  'esri/request',
  'esri/geometry/Extent',
  'esri/SpatialReference'
],function(declare,lang,
           topic,ioQuery,
           esriRequest,
           Extent,
           SpatialReference){
    return declare(null, {

      query: null,
      
      execute: function(query){
        //console.warn("execute",query);
        this.query = query;
        var requestHandle = esriRequest({
              "url": query.queryUrl+"?" + this._queryToString(query),
              handleAs:'json'
            },{
              useProxy:false
            });
          requestHandle.then(lang.hitch(this,this._onQueryFinish), this._onQueryError);
      },
      
      _queryToString: function(query) {
        var q = {};
        for (a in query) {
          if (typeof query[a]=="object" || typeof query[a]=="function" || typeof query[a]=="array") {
            continue;
          }
          if (a === "searchText" && query.queryUrl.indexOf("/rest/metadata/search") !== -1) {
            q["q"] = query[a];
          } else {
            q[a] = query[a];
          }
        }
        return ioQuery.objectToQuery(q);
      },

      _onQueryFinish: function(response, io){
        var results = response;
        
        addLink = function(links,href) {
          if (typeof href === "string" && href.length > 0) {
            var link = {
              "href": href,
              "type": "open"
            }
            links.push(link);
          }
        };
        

        // handle Geoportal 2.x structure
        if (response.hits) {
          results = {
            "title": "",
            "description": "",
            "copyright": "",
            "provider": "",
            "updated": "2017-06-28T17:38:42Z",
            "source": this.query.queryUrl,
            "more": "",
            "totalResults": response.hits.total,
            "startIndex": this.query.start,
            "itemsPerPage": response.hits.hits.length,
            "records": []
          };

          var env;
          for (var i=0, items=response.hits.hits.length; i<items;i++) {
            var item = response.hits.hits[i];
            var source = item._source;
            //console.warn("item",item);
            var record = {
              "title": source.title,
              "id": item._id,
              "updated": source.sys_xmlmodified_dt,
              "summary": "",
              "links": []
            };
            if (typeof source.description === "string") {
              record.summary = source.description;
            }
            if (source.envelope_geo && source.envelope_geo.coordinates) {
              env = source.envelope_geo.coordinates;
              record.geometry = new Extent(env[0][0],env[1][1],env[1][0],env[0][1],new SpatialReference(4326));
              record.bbox = [
                env[0][0],env[1][1],env[1][0],env[0][1]
              ];
            }

            // for now, all links are generic 'open' links
            if (typeof source.links_s === "string" && source.links_s.length > 0) {
              addLink(record.links,source.links_s);
            } else if (source.links_s && typeof source.links_s.push === "function") {
              for (var j=0,links=source.links_s.length; j<links;j++) {
                addLink(record.links,source.links_s[j]);
              }
            }

            // generate link to the metadata for this item
            var recordLink = {
              "href": this.query.queryUrl.replace("search","item/").replace("gptVersion=2","").replace("?","") + record.id + "/xml",
              "type": "metadata"
            }
            record.links.push(recordLink);
            //console.warn("record",record);

            // now add this to the result
            results.records.push(record);
          }
        }
        
        topic.publish("/widgets/GeoportalSearch/action/search", null, { success: true, results: results, query: this.query, io:io });
      },

      _onQueryError:function(error, io){
        topic.publish("/widgets/GeoportalSearch/action/search", null, { success: false, error: error, io:io  });
      }

      /*
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
        var results = response;

        // handle Geoportal 2.x structure
        if (response.hits) {
          results = {
            "title": "",
            "description": "",
            "copyright": "",
            "provider": "",
            "updated": "2017-06-28T17:38:42Z",
            "source": this.query.queryUrl,
            "more": "",
            "totalResults": response.total,
            "startIndex": this.query.start,
            "itemsPerPage": response.hits.hits.length,
            "records": []
          };

          for (var i=0, items=response.hits.hits.length; i<items;i++) {
            var item = response.hits.hits[i];
            var record = {
              "title": item._source.title,
              "id": item._id,
              "updated": item._sys_xmlmodified_dt,
              "summary": item._source.description,
              "bbox": [
                item._source.envelope_geo.coordinates[0][0],
                item._source.envelope_geo.coordinates[0][1],
                item._source.envelope_geo.coordinates[1][0],
                item._source.envelope_geo.coordinates[1][1]
              ],
              "geometry": item._source.envelope_geo,
              "links": [
              ]
            };

            // for now, all links are generic 'open' links
            for (var j=0,links=item._source.links_s.length; j<links;j++) {
              var link = item._source.links_s[j];
              var recordLink = {
                "href": link,
                "type": "open"
              }
              record.links.push(recordLink);
            }

            // generate link to the metadata for this item
            var recordLink = {
              "href": this.query.queryUrl.replace("search","item/").replace("gptVersion=2","").replace("?","") + record.id + "/xml",
              "type": "metadata"
            }
            record.links.push(recordLink);

            // now add this to the result
            results.records.push(record);
          }
        }

        topic.publish("/widgets/GeoportalSearch/action/search", null, { success: true, results: results, query: this.query, io:io });
      },

      _onQueryError:function(error, io){
        topic.publish("/widgets/GeoportalSearch/action/search", null, { success: false, error: error, io:io  });
      }
      */

   });
});