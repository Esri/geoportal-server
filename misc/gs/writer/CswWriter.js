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

  gs.writer.CswWriter = gs.Object.create(gs.writer.XmlWriter,{

    mediaType: {writable: true, value: gs.base.Response.MediaType_APPLICATION_XML},

    uris: {writable: true, value: null},

    /* .......................................................................................... */

    addAtomCategory: {writable:true,value:function(task,xmlBuilder,namespaceURI,localName,value) {
      if (value === null) return;
      if (!Array.isArray(value)) value = [value];
      var self = this;
      value.forEach(function(v){
        if (gs.atom.Category.isPrototypeOf(v)) {
          if (typeof v.term === "string" && v.term.length > 0) {
            xmlBuilder.writeStartElement(namespaceURI,localName);
            if (typeof v.scheme === "string" && v.scheme.length > 0) {
              xmlBuilder.writeAttribute("scheme",v.scheme);
            }
            xmlBuilder.writeCharacters(v.term);
            xmlBuilder.writeEndElement();
          }
        }
      });
    }},

    addAtomLink: {writable:true,value:function(task,xmlBuilder,namespaceURI,localName,value) {
      if (value === null) return;
      if (!Array.isArray(value)) value = [value];
      var scheme;
      value.forEach(function(v){
        if (gs.atom.Link.isPrototypeOf(v)) {
          if (typeof v.href === "string" && v.href.length > 0) {
            xmlBuilder.writeStartElement(namespaceURI,localName);
            if (typeof v.dctype === "string" && v.dctype.length > 0) {
              scheme = v.dctype;
              if (task.isCsw2) {
                // TODO use the schemes from Geoportal1?
              }
              xmlBuilder.writeAttribute("scheme",scheme);
            } else if (v.rel === "alternate") {
              scheme = v.rel;
              if (v.type === "application/xml") {
                scheme = "alternate.xml";
                // TODO use the schemes from Geoportal1?
              } else if (v.type === "text/html") {
                scheme = "alternate.html";
                // TODO use the schemes from Geoportal1?
              } else if (v.type === "application/json") {
                scheme = "alternate.json";
                // TODO use the schemes from Geoportal1?
              }
              xmlBuilder.writeAttribute("scheme",scheme);
            } else if (typeof v.rel === "string" && v.rel.length > 0) {
              xmlBuilder.writeAttribute("scheme",v.rel);
            }
            xmlBuilder.writeCharacters(v.href);
            xmlBuilder.writeEndElement();
          }
        }
      });
    }},

    addAtomPerson: {writable:true,value:function(task,xmlBuilder,namespaceURI,localName,value) {
      if (value === null) return;
      if (!Array.isArray(value)) value = [value];
      var self = this;
      value.forEach(function(v){
        if (gs.atom.Person.isPrototypeOf(v)) {
          if (typeof v.name === "string" && v.name.length > 0) {
            xmlBuilder.writeElement(namespaceURI,localName,v.name);
          }
        }
      });
    }},

    addAtomText: {writable:true,value:function(task,xmlBuilder,namespaceURI,localName,value) {
      var self = this;
      if (Array.isArray(value)) {
        value.forEach(function(v){
          self.addAtomText(task,xmlBuilder,namespaceURI,localName,v);
        });
      } else if (gs.atom.Text.isPrototypeOf(value)) {
        xmlBuilder.writeStartElement(namespaceURI,localName);
        if (typeof value.value === "string") {
          xmlBuilder.writeCharacters(value.value);
        }
        xmlBuilder.writeEndElement();
      } else if (typeof value === "string" && value.length > 0) {
        xmlBuilder.writeElement(namespaceURI,localName,value);
      }
    }},

    addNamespaces: {writable:true,value:function(task,xmlBuilder) {
      if (task.isCsw2) {
        xmlBuilder.writeNamespace("csw",task.uris.URI_CSW2);
        xmlBuilder.writeNamespace("ows",task.uris.URI_OWS);
      } else {
        xmlBuilder.writeNamespace("csw",task.uris.URI_CSW3);
        xmlBuilder.writeNamespace("ows",task.uris.URI_OWS2);
      }
      xmlBuilder.writeNamespace("dc",task.uris.URI_DC);
      xmlBuilder.writeNamespace("dct",task.uris.URI_DCT);
    }},

    ensureUris: {writable:true,value:function(task) {
      if (this.uris) return this.uris;
      var uris = this.uris = {};
      if (task.isCsw2) {
        uris.csw = task.uris.URI_CSW2;
        uris.ows = task.uris.URI_OWS;
      } else {
        uris.csw = task.uris.URI_CSW3;
        uris.ows = task.uris.URI_OWS2;
      }
      return uris;
    }},

    marshallOptions: {writable:true,value:function(task,options) {
      options.recordTypeName = "Record";
      //options.elementSetName = "summary";
      var p = task.provider;
      if (typeof p.recordTypeName === "string" && p.recordTypeName.length > 0) {
        options.recordTypeName = p.recordTypeName;
      }
      if (typeof p.elementSetName === "string" && p.elementSetName.length > 0) {
        options.elementSetName = p.elementSetName;
      }
    }},

    writeEntry: {writable:true,value:function(task,xmlBuilder,item,options) {
      var uris = this.ensureUris(task);
      var recordTypeName = options.recordTypeName;
      if (options.entryOnly) {
        this.marshallOptions(task,options);
        recordTypeName = options.recordTypeName;
        xmlBuilder.writeStartElementPfx("csw",uris.csw,recordTypeName);
        this.addNamespaces(task,xmlBuilder);
      } else {
        xmlBuilder.writeStartElement(uris.csw,recordTypeName);
      }
      var entry = task.target.itemToAtomEntry(task,item);

      var id = entry.id;
      var title = "Untitled";
      if (typeof entry.title === "string" && entry.title.length > 0) {
        title = entry.title;
      }
      //if (task.verbose) console.log("title",title);

      xmlBuilder.writeElement(task.uris.URI_DC,"identifier",id);
      xmlBuilder.writeElement(task.uris.URI_DC,"title",title);
      //xmlBuilder.writeElement(task.uris.URI_DC,"type",itemType);   // TODO

      if (recordTypeName !== "BriefRecord") {
        this.addAtomCategory(task,xmlBuilder,task.uris.URI_DC,"subject",entry.category);
        // dc:format (summary) TODO
        // dc:relation (summary)
        this.addAtomText(task,xmlBuilder,task.uris.URI_DCT,"modified",entry.updated);
        this.addAtomText(task,xmlBuilder,task.uris.URI_DCT,"abstract",entry.summary);
        // dct:spatial (summary)
        // csw:TemporalExtent (summary)?
        if (recordTypeName === "SummaryRecord") {
          this.addAtomLink(task,xmlBuilder,task.uris.URI_DCT,"references",entry.link);
        } else {
          this.addAtomText(task,xmlBuilder,task.uris.URI_DCT,"created",entry.published);
          this.addAtomPerson(task,xmlBuilder,task.uris.URI_DC,"creator",entry.author);
          this.addAtomPerson(task,xmlBuilder,task.uris.URI_DC,"contributor",entry.contributor);
          this.addAtomText(task,xmlBuilder,task.uris.URI_DC,"rights",entry.rights);
          this.addAtomLink(task,xmlBuilder,task.uris.URI_DCT,"references",entry.link);
        }
      }

      if (gs.atom.BBox.isPrototypeOf(entry.bbox)) {
        entry.bbox.writeOwsBoundingBox(task,xmlBuilder);
      }

      this.beforeEndEntry(task,xmlBuilder,item,options,entry);
      xmlBuilder.writeEndElement();
    }},

    writeItems: {writable:true,value:function(task,searchResult) {
      var now = task.val.nowAsString();
      var options = {now: now, entryOnly: false};
      var xmlBuilder = task.context.newXmlBuilder();
      xmlBuilder.writeStartDocument();
      this.marshallOptions(task,options);

      var items = searchResult.items ? searchResult.items : [];
      var uris = this.ensureUris(task);
      xmlBuilder.writeStartElementPfx("csw",uris.csw,"GetRecordsResponse");
      this.addNamespaces(task,xmlBuilder);
      xmlBuilder.writeStartElement(uris.csw,"SearchStatus");
      xmlBuilder.writeAttribute("timestamp",now);
      xmlBuilder.writeEndElement();

      var numReturned = items.length;
      if (searchResult.itemsPerPage === 0) numReturned = 0;

      xmlBuilder.writeStartElement(uris.csw,"SearchResults");
      xmlBuilder.writeAttribute("numberOfRecordsMatched",""+searchResult.totalHits);
      xmlBuilder.writeAttribute("numberOfRecordsReturned",""+numReturned);
      xmlBuilder.writeAttribute("nextRecord",""+searchResult.calcNextRecord(task));
      xmlBuilder.writeAttribute("recordSchema",uris.csw);
      if (options.elementSetName != null && options.elementSetName.length > 0) {
        xmlBuilder.writeAttribute("elementSetName",options.elementSetName);
      }
      if (searchResult.itemsPerPage > 0) {
        for (var i=0;i<items.length;i++) {
          this.writeEntry(task,xmlBuilder,items[i],options);
        }
      }
      xmlBuilder.writeEndElement();

      xmlBuilder.writeEndElement();
      xmlBuilder.writeEndDocument();
      this.writeResponse(task,xmlBuilder);
    }}

  });

}());
