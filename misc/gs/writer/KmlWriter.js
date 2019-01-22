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

  gs.writer.KmlWriter = gs.Object.create(gs.writer.XmlWriter,{

    mediaType: {writable: true, value: gs.base.Response.MediaType_APPLICATION_KML_XML},

    addNamespaces: {writable:true,value:function(task,xmlBuilder) {
      xmlBuilder.writeNamespace("kml",task.uris.URI_KML);
      xmlBuilder.writeNamespace("atom",task.uris.URI_ATOM);
      xmlBuilder.writeNamespace("dc",task.uris.URI_DC);
      xmlBuilder.writeNamespace("opensearch",task.uris.URI_OPENSEARCH);
    }},

    writeEntry: {writable:true,value:function(task,xmlBuilder,item,options) {
      var entry = task.target.itemToAtomEntry(task,item);
      if (!entry) return;

      if (options.entryOnly) {
        xmlBuilder.writeStartElementPfx("kml",task.uris.URI_KML,"Placemark");
        this.addNamespaces(task,xmlBuilder);
      } else {
        xmlBuilder.writeStartElement(task.uris.URI_KML,"Placemark");
      }

      var id = entry.id;
      var title = "Untitled";
      var description = null;
      if (typeof entry.title === "string" && entry.title.length > 0) {
        title = entry.title;
      }
      if (entry.summary && typeof entry.summary.value === "string") {
        description = entry.summary.value;
      }

      xmlBuilder.writeElement(task.uris.URI_ATOM,"id",id);
      xmlBuilder.writeElement(task.uris.URI_KML,"name",title);
      this.addElement(task,xmlBuilder,task.uris.URI_KML,"description",description);
      this.addElement(task,xmlBuilder,task.uris.URI_KML,"styleUrl","#main");
      this.addAtomObject(task,xmlBuilder,gs.atom.Link,entry.link);

      if (gs.atom.BBox.isPrototypeOf(entry.bbox)) {
        var xmin = entry.bbox.xmin;
        var ymin = entry.bbox.ymin;
        var xmax = entry.bbox.xmax;
        var ymax = entry.bbox.ymax;
        //if ((xmin < -180.0) && (xmax >= -180.0)) xmin = -180.0;
        //if ((xmax > 180.0) && (xmin <= 180.0)) xmax = 180.0;
        if ((ymin < -90.0) && (ymax >= -90.0)) ymin = -90.0;
        if ((ymax > 90.0) && (ymin <= 90.0)) ymax = 90.0;
        if (xmin === xmax && ymin === ymax) {
          xmlBuilder.writeStartElement(task.uris.URI_KML,"Point");
          xmlBuilder.writeStartElement(task.uris.URI_KML,"coordinates");
          xmlBuilder.writeCharacters(xmin+","+ymin+",0");
          xmlBuilder.writeEndElement();
          xmlBuilder.writeEndElement();
        } else {
          xmlBuilder.writeStartElement(task.uris.URI_KML,"Polygon");
          xmlBuilder.writeElement(task.uris.URI_KML,"extrude","0");
          xmlBuilder.writeElement(task.uris.URI_KML,"altitudeMode","clampToGround");
          xmlBuilder.writeStartElement(task.uris.URI_KML,"outerBoundaryIs");
          xmlBuilder.writeStartElement(task.uris.URI_KML,"LinearRing");
          xmlBuilder.writeStartElement(task.uris.URI_KML,"coordinates");
          xmlBuilder.writeCharacters("\r\n");
          xmlBuilder.writeCharacters(xmin+","+ymax+",0\r\n");
          xmlBuilder.writeCharacters(xmax+","+ymax+",0\r\n");
          xmlBuilder.writeCharacters(xmax+","+ymin+",0\r\n");
          xmlBuilder.writeCharacters(xmin+","+ymin+",0\r\n");
          xmlBuilder.writeCharacters(xmin+","+ymax+",0\r\n");
          xmlBuilder.writeEndElement();
          xmlBuilder.writeEndElement();
          xmlBuilder.writeEndElement();
          xmlBuilder.writeEndElement();
        }
      }

      this.beforeEndEntry(task,xmlBuilder,item,options,entry);
      xmlBuilder.writeEndElement();
    }},

    writeItems: {writable:true,value:function(task,searchResult) {
      var now = task.val.nowAsString();
      var options = {now: now, entryOnly: false};
      var xmlBuilder = task.context.newXmlBuilder();
      xmlBuilder.writeStartDocument();

      xmlBuilder.writeStartElementPfx("kml",task.uris.URI_KML,"kml");
      this.addNamespaces(task,xmlBuilder);
      xmlBuilder.writeStartElement(task.uris.URI_KML,"Document");
      xmlBuilder.writeElement(task.uris.URI_KML,"name","Results");
      xmlBuilder.writeElement(task.uris.URI_KML,"open","1");
      this.writeStyle(xmlBuilder,"main","7d0000ff");

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
    }},

    writeStyle: {writable:true,value:function(xmlBuilder,name,color) {
      xmlBuilder.writeStartElement(task.uris.URI_KML,"Style");
      xmlBuilder.writeAttribute("id",name);
      xmlBuilder.writeStartElement(task.uris.URI_KML,"LineStyle");
      xmlBuilder.writeElement(task.uris.URI_KML,"width","1.5");
      xmlBuilder.writeEndElement();
      xmlBuilder.writeStartElement(task.uris.URI_KML,"PolyStyle");
      xmlBuilder.writeElement(task.uris.URI_KML,"color",color);
      xmlBuilder.writeEndElement();
      xmlBuilder.writeEndElement();
    }},

    writeResponse: {writable:true,value:function(task,xmlBuilder) {
      task.response.addHeader("Content-Disposition","filename="+Date.now()+".kml");
      this.writeXmlResponse(task,this.mediaType,xmlBuilder.getXml());
    }}
  });

}());
