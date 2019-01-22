/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

(function(){
  /*jshint -W069 */

  gs.target.portal.PortalSchema = gs.Object.create(gs.target.TargetSchema, {

    fieldAliases: {writable: true, value: {
      "date": "modified",
      "author": "owner"
    }},

    schemaType: {writable: true, value: "ArcGIS"},

    target: {writable: true, value: null},

    typeAliases: {writable: true, value: {
      "FeatureServer": "Feature Service",
      "MapServer": "Map Service",
      "ImageServer": "Image Service",
      "GeocodeServer": "Geocoding Service",
      "GeoDataServer": "Geodata Service",
      "GeometryServer": "Geometry Service",
      "GlobeServer": "Globe Service",
      "GPServer": "Geoprocessing  Service",
      "NAServer": "Network Analysis Service",
      "SceneServer": "Scene Service",
      "VectorTileServer": "Vector Tile Service",

      "shp": "Shapefife",

      "liveData": ["Feature Service",
                   "Map Service",
                   "Image Service",
                   "Scene Service",
                   "Vector Tile Service",
                   "KML","WMS","WFS","WCS","WMTS"]
    }},


    buildAtomCategories: {writable:true,value:function(task,item) {
      var categories = [];

      var itemType = task.val.chkStr(item["type"]);
      if (typeof itemType === "string" && itemType.length > 0) {
        categories.push(gs.Object.create(gs.atom.Category).init({
          scheme: "ArcGIS",
          term: itemType
        }));
      }

      if (Array.isArray(item["tags"])) {
        item["tags"].forEach(function(v){
          v = task.val.chkStr(v);
          if (typeof v === "string" && v.length > 0) {
            categories.push(gs.Object.create(gs.atom.Category).init({
              scheme: "tags",
              term: v
            }));
          }
        });
      }

      return categories;
    }},

    buildAtomLinks: {writable:true,value:function(task,item) {
      var links = [], ok, url;
      var itemDetailsUrl = null, itemUrl = null, metadataUrl = null, restUrl = null;

      var portalBaseUrl = this.target.portalBaseUrl;
      if (typeof portalBaseUrl === "string" && portalBaseUrl.length > 0) {
        restUrl = portalBaseUrl+"/sharing/rest";
        itemUrl = restUrl+"/content/items/"+encodeURIComponent(item["id"]);
        metadataUrl = itemUrl+"/info/metadata/metadata.xml";
        //metadataUrl = itemUrl+"/info/metadata/metadata.xml?format=iso19139"; // TODO which metadata format?
        itemDetailsUrl = portalBaseUrl+"/home/item.html?id="+encodeURIComponent(item["id"]);
      }

      var hasMetadata = false;
      if (Array.isArray(item["typeKeywords"])) {
        item["typeKeywords"].some(function(v){
          if (typeof v === "string" && v.toLowerCase() === "metadata") {
            hasMetadata = true;
            return true;
          }
        });
      }

      url = item["url"];
      if (typeof url === "string" && url.length > 0) {
        links.push(gs.Object.create(gs.atom.Link).init({
          rel: "related", // TODO?
          dctype: item["type"],
          href: url
        }));
      }

      url = item["thumbnail"];
      if (typeof url === "string" && url.length > 0) {
        if (url.indexOf("thumbnail/") === 0 && itemUrl !== null) {
          url = itemUrl+"/info/"+url; // TODO?
        }
        ok = url.indexOf("http://") === 0 || url.indexOf("https://") === 0;
        if (ok) {
          links.push(gs.Object.create(gs.atom.Link).init({
            rel: "icon",
            href: url
          }));
        }
      }

      if (itemUrl !== null) {
        links.push(gs.Object.create(gs.atom.Link).init({
          rel: "alternate",
          type: "application/json",
          href: itemUrl+"?f=json"
        }));
      }

      if (itemDetailsUrl !== null) {
        links.push(gs.Object.create(gs.atom.Link).init({
          rel: "alternate",
          type: "text/html",
          href: itemDetailsUrl
        }));
      }

      if (hasMetadata && metadataUrl !== null) {
        links.push(gs.Object.create(gs.atom.Link).init({
          rel: "alternate", // TODO via???
          type: "application/xml",
          href: metadataUrl
        }));
      }

      return links;
    }},

    itemToAtomEntry: {writable:true,value:function(task,item) {
      var entry = gs.Object.create(gs.atom.Entry);
      entry.id = item["id"];
      entry.title =  task.val.chkStr(item["title"]);
      entry.published = task.val.millisToIso8601(item["created"]);
      entry.updated = task.val.millisToIso8601(item["modified"]);
      entry.category = this.buildAtomCategories(task,item);
      entry.link = this.buildAtomLinks(task,item);

      var summary = task.val.chkStr(item["description"]);
      if (summary !== null && summary.length > 0) {
        entry.summary = gs.Object.create(gs.atom.Text).init({
          type: "html",
          value: summary
        });
      }

      var author = task.val.chkStrArray(item["owner"]);
      if (Array.isArray(author)) {
        author.forEach(function(v){
          v = task.val.chkStr(v);
          if (v !== null && v.length > 0) {
            if (!entry.author) entry.author = [];
            entry.author.push(gs.Object.create(gs.atom.Person).init({
              tag: "author",
              name: v
            }));
          }
        });
      }

      var credits = task.val.chkStrArray(item["accessInformation"]);
      if (Array.isArray(credits)) {
        credits.forEach(function(v){
          v = task.val.chkStr(v);
          if (v !== null && v.length > 0) {
            if (!entry.contributor) entry.contributor = [];
            entry.contributor.push(gs.Object.create(gs.atom.Person).init({
              tag: "contributor",
              name: v
            }));
          }
        });
      }

      var rights = task.val.chkStrArray(item["licenseInfo"]);
      if (Array.isArray(rights)) {
        rights.forEach(function(v){
          v = task.val.chkStr(v);
          if (v !== null && v.length > 0) {
            if (!entry.rights) entry.rights = [];
            entry.rights.push(gs.Object.create(gs.atom.Text).init({
              type: "html",
              value: v
            }));
          }
        });
      }

      var extent = task.val.chkStrArray(item["extent"]);
      if (extent && extent.length === 2) {
        entry.bbox = gs.Object.create(gs.atom.BBox).init({
          xmin: extent[0][0],
          ymin: extent[0][1],
          xmax: extent[1][0],
          ymax: extent[1][1]
        });

        // TODO center point?
      }

      return entry;
    }},

    itemToJson: {writable:true,value:function(task,item) {
      var json = gs.target.TargetSchema.itemToJson.call(this,task,item); // call super
      if (item) {
        json._source = item;
        /*
        var enc, portalBaseUrl, itemDetailsUrl;
        portalBaseUrl = this.target.portalBaseUrl;
        if (typeof portalBaseUrl === "string" && portalBaseUrl.length > 0) {
          enc = encodeURIComponent(item.id);
          itemUrl = portalBaseUrl+"/sharing/rest/content/items/"+enc;
        }
        */
      }
      return json;
    }}

  });

}());
