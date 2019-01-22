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

  gs.writer.AtomWriter = gs.Object.create(gs.writer.XmlWriter,{

    mediaType: {writable: true, value: gs.base.Response.MediaType_APPLICATION_ATOM_XML},

    addNamespaces: {writable:true,value:function(task,xmlBuilder) {
      xmlBuilder.writeNamespace("atom",task.uris.URI_ATOM);
      xmlBuilder.writeNamespace("dc",task.uris.URI_DC);
      xmlBuilder.writeNamespace("opensearch",task.uris.URI_OPENSEARCH);
      xmlBuilder.writeNamespace("geo",task.uris.URI_GEO);
      xmlBuilder.writeNamespace("georss",task.uris.URI_GEORSS);
      xmlBuilder.writeNamespace("georss10",task.uris.URI_GEORSS10);
      xmlBuilder.writeNamespace("time",task.uris.URI_TIME);
    }},

    writeEntry: {writable:true,value:function(task,xmlBuilder,item,options) {
      var entry = task.target.itemToAtomEntry(task,item);
      if (!entry) return;

      if (options.entryOnly) {
        xmlBuilder.writeStartElementPfx("atom",task.uris.URI_ATOM,"entry");
        this.addNamespaces(task,xmlBuilder);
      } else {
        xmlBuilder.writeStartElement(task.uris.URI_ATOM,"entry");
      }

      // atom:id, atom:title, and atom:updated are required
      var id = entry.id;
      var title = "Untitled";
      var updated = options.now;
      if (typeof entry.title === "string" && entry.title.length > 0) {
        title = entry.title;
      }
      if (typeof entry.updated === "string" && entry.updated.length > 0) {
        updated = entry.updated;
      } else if (typeof entry.published === "string" && entry.published.length > 0) {
        updated = entry.published;
      }

      xmlBuilder.writeElement(task.uris.URI_ATOM,"id",id);
      xmlBuilder.writeElement(task.uris.URI_DC,"identifier",id);
      xmlBuilder.writeElement(task.uris.URI_ATOM,"title",title);
      this.addElement(task,xmlBuilder,task.uris.URI_ATOM,"published",entry.published);
      this.addElement(task,xmlBuilder,task.uris.URI_ATOM,"updated",updated);
      this.addAtomText(task,xmlBuilder,task.uris.URI_ATOM,"summary",entry.summary);
      this.addAtomObject(task,xmlBuilder,gs.atom.Person,entry.author);
      this.addAtomObject(task,xmlBuilder,gs.atom.Person,entry.contributor);
      this.addAtomObject(task,xmlBuilder,gs.atom.Link,entry.link);
      this.addAtomObject(task,xmlBuilder,gs.atom.Category,entry.category);
      this.addAtomText(task,xmlBuilder,task.uris.URI_ATOM,"rights",entry.rights);
      if (gs.atom.Point.isPrototypeOf(entry.point)) {
        entry.point.writeGeoRSSPoint(task,xmlBuilder);
      }
      if (gs.atom.BBox.isPrototypeOf(entry.bbox)) {
        entry.bbox.writeGeoRSSBox(task,xmlBuilder);
        entry.bbox.writeGeoRSSBox10(task,xmlBuilder);
      }
      this.addAtomObject(task,xmlBuilder,task.uris.URI_ATOM,"content",entry.content);

      this.beforeEndEntry(task,xmlBuilder,item,options,entry);
      xmlBuilder.writeEndElement();
    }},

    writeItems: {writable:true,value:function(task,searchResult) {
      var now = task.val.nowAsString();
      var options = {now: now, entryOnly: false};
      var xmlBuilder = task.context.newXmlBuilder();
      xmlBuilder.writeStartDocument();

      xmlBuilder.writeStartElementPfx("atom",task.uris.URI_ATOM,"feed");
      this.addNamespaces(task,xmlBuilder);
      xmlBuilder.writeElement(task.uris.URI_ATOM,"title","Results");
      xmlBuilder.writeElement(task.uris.URI_ATOM,"id",task.baseUrl);
      xmlBuilder.writeElement(task.uris.URI_ATOM,"updated",now); // TODO
      xmlBuilder.writeStartElement(task.uris.URI_ATOM,"author");
      xmlBuilder.writeElement(task.uris.URI_ATOM,"name",task.baseUrl);
      xmlBuilder.writeEndElement();

      this.writeOpensearchInfo(task,searchResult,xmlBuilder);
      this.writeOpensearchQuery(task,xmlBuilder);
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
