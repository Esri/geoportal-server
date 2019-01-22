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

  gs.writer.RssWriter = gs.Object.create(gs.writer.XmlWriter,{

    mediaType: {writable: true, value: gs.base.Response.MediaType_APPLICATION_RSS_XML},

    addNamespaces: {writable:true,value:function(task,xmlBuilder) {
      xmlBuilder.writeNamespace("atom",task.uris.URI_ATOM);
      xmlBuilder.writeNamespace("dc",task.uris.URI_DC);
      xmlBuilder.writeNamespace("opensearch",task.uris.URI_OPENSEARCH);
      xmlBuilder.writeNamespace("geo",task.uris.URI_GEO);
      xmlBuilder.writeNamespace("georss",task.uris.URI_GEORSS);
      xmlBuilder.writeNamespace("georss10",task.uris.URI_GEORSS10);
      xmlBuilder.writeNamespace("time",task.uris.URI_TIME);
    }},

    formatDate: {writable:true,value:function(task,iso) {
      // TODO need a Node implementation
      if (task.context.isNashorn) {
        try {
          var dt = Date.parse(iso);
          if (!isNaN(dt)) {
            if (!this.DF) {
              this.DF = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZ");
            }
            var v = this.DF.format(dt);
            //console.log(iso,v);
            if (v) return v;
          }
        } catch(ex) {
          //console.log("RssWriter: Unable to format date:",ex);
        }
      }
      return iso;
    }},

    writeEntry: {writable:true,value:function(task,xmlBuilder,item,options) {
      var entry = task.target.itemToAtomEntry(task,item);
      if (!entry) return;
      xmlBuilder.writeStartElement(null,"item");

      var id = entry.id;
      var title = "Untitled";
      var description = "";
      var updated = options.now;
      if (typeof entry.title === "string" && entry.title.length > 0) {
        title = entry.title;
      }
      if (entry.summary && typeof entry.summary.value === "string") {
        description = entry.summary.value;
      }
      if (typeof entry.updated === "string" && entry.updated.length > 0) {
        updated = entry.updated;
      } else if (typeof entry.published === "string" && entry.published.length > 0) {
        updated = entry.published;
      }

      xmlBuilder.writeElement(null,"guid",id);
      xmlBuilder.writeElement(task.uris.URI_ATOM,"id",id);
      xmlBuilder.writeElement(null,"title",title);
      if (updated !== null && updated.length > 0) {
        xmlBuilder.writeElement(null,"pubDate",this.formatDate(task,updated));
      }

      xmlBuilder.writeElement(null,"description",description);
      /*
      var snippet = G.getSnippet(xmlBuilder,title,description,links);
      xmlBuilder.writer.writeStartElement("description");
      xmlBuilder.writer.writeCData(snippet);
      xmlBuilder.writer.writeEndElement();
      */

      this.addAtomObject(task,xmlBuilder,gs.atom.Link,entry.link);

      if (gs.atom.Point.isPrototypeOf(entry.point)) {
        entry.point.writeGeoRSSPoint(task,xmlBuilder);
      }
      if (gs.atom.BBox.isPrototypeOf(entry.bbox)) {
        entry.bbox.writeGeoRSSBox(task,xmlBuilder);
        entry.bbox.writeGeoRSSBox10(task,xmlBuilder);
      }

      this.beforeEndEntry(task,xmlBuilder,item,options,entry);
      xmlBuilder.writeEndElement();
    }},

    writeItems: {writable:true,value:function(task,searchResult) {
      var now = task.val.nowAsString();
      var options = {now: now, entryOnly: false};
      var xmlBuilder = task.context.newXmlBuilder();
      xmlBuilder.writeStartDocument();

      xmlBuilder.writeStartElement(null,"rss");
      xmlBuilder.writeAttribute("version","2.0");
      this.addNamespaces(task,xmlBuilder);
      xmlBuilder.writeStartElement(null,"channel");
      xmlBuilder.writeElement(null,"title","Results");
      xmlBuilder.writeElement(null,"description","RSS Results");
      xmlBuilder.writeElement(null,"link",task.baseUrl);
      xmlBuilder.writeElement(null,"docs","http://www.rssboard.org/rss-specification");
      xmlBuilder.writeElement(null,"category","GeoRss");

      this.writeOpensearchInfo(task,searchResult,xmlBuilder);
      var items = searchResult.items ? searchResult.items : [];
      if (searchResult.itemsPerPage > 0) {
        for (var i=0;i<items.length;i++) {
          this.writeEntry(task,xmlBuilder,items[i],options);
        }
      }
      xmlBuilder.writeEndElement();
      xmlBuilder.writeEndDocument();
      this.writeResponse(task,xmlBuilder);
    }}

  });

}());
