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

  gs.writer.XmlWriter = gs.Object.create(gs.writer.Writer,{

    mediaType: {writable: true, value: gs.base.Response.MediaType_APPLICATION_XML},

    write: {writable:true,value:function(task,searchResult) {
      if (task.provider.isCswProvider && task.request.isItemByIdRequest) {
        this.writeItem(task,searchResult);
      } else {
        this.writeItems(task,searchResult);
      }
    }},

    /* .......................................................................................... */

    addAtomObject: {writable:true,value:function(task,xmlBuilder,atomType,atomObject) {
      if (Array.isArray(atomObject)) {
        atomObject.forEach(function(v){
          if (typeof v.write === "function") {
            v.write(task,xmlBuilder);
          }
        });
      } else if (atomType.isPrototypeOf(atomObject)) {
        if (typeof atomObject.write === "function") {
          atomObject.write(task,xmlBuilder);
        }
      }
    }},

    addAtomText: {writable:true,value:function(task,xmlBuilder,namespaceURI,localName,value) {
      var self = this;
      if (Array.isArray(value)) {
        value.forEach(function(v){
          self.addAtomText(task,xmlBuilder,namespaceURI,localName,v);
        });
      } else if (gs.atom.Text.isPrototypeOf(value)) {
        if (typeof value.write === "function") {
          value.write(task,xmlBuilder,namespaceURI,localName);
        }
      } else if (typeof value === "string" && value.length > 0) {
        xmlBuilder.writeElement(namespaceURI,localName,value);
      }
    }},

    addElement: {writable:true,value:function(task,xmlBuilder,namespaceURI,localName,value) {
      if (typeof value === "string" && value.length > 0) {
        xmlBuilder.writeElement(namespaceURI,localName,value);
      }
    }},

    addNamespaces: {writable:true,value:function(task,xmlBuilder) {
    }},

    beforeEndEntry: {writable:true,value:function(task,xmlBuilder,item,options,entry) {
      // TODO call schema before ending element
    }},

    writeEntry: {writable:true,value:function(task,xmlBuilder,item,options) {
    }},

    writeItem: {writable:true,value:function(task,searchResult) {
      var now = task.val.nowAsString();
      var options = {now: now, entryOnly: true};
      var xmlBuilder = task.context.newXmlBuilder();
      xmlBuilder.writeStartDocument();
      this.writeEntry(task,xmlBuilder,searchResult.items[0],options);
      xmlBuilder.writeEndDocument();
      this.writeResponse(task,xmlBuilder);
    }},

    writeItems: {writable:true,value:function(task,searchResult) {
    }},

    writeOpensearchInfo: {writable:true,value:function(task,searchResult,xmlBuilder) {
      var dscUrl = task.baseUrl+"/opensearch/description"; // TODO
      var totalHits = searchResult.totalHits;
      var startIndex = searchResult.startIndex;
      var itemsPerPage = searchResult.itemsPerPage;
      var link = gs.Object.create(gs.atom.Link).init({
        rel: "search",
        type: "application/opensearchdescription+xml",
        href: dscUrl
      });
      link.write(task,xmlBuilder);
      xmlBuilder.writeElement(task.uris.URI_OPENSEARCH,"totalResults",""+totalHits);
      xmlBuilder.writeElement(task.uris.URI_OPENSEARCH,"startIndex",""+startIndex);
      xmlBuilder.writeElement(task.uris.URI_OPENSEARCH,"itemsPerPage",""+itemsPerPage);
    }},

    writeOpensearchQuery: {writable:true,value:function(task,xmlBuilder) {
      var uris = task.uris, request = task.request;
      var osqQ = request.getParameter("q");
      var osqId = request.getParameter("id");
      var osqBBox = request.getParameter("bbox");
      var osqTime = request.getParameter("time");
      var osqStartIndex = request.getParameter("start");
      var osqCount = request.getParameter("num");
      xmlBuilder.writeStartElement(uris.URI_OPENSEARCH,"Query");
      xmlBuilder.writeAttribute("role","request");
      if (osqQ !== null && osqQ.length > 0) {
        xmlBuilder.writeAttribute("searchTerms",osqQ);
      }
      if (osqId !== null && osqId.length > 0) {
        xmlBuilder.writeAttributeNS(uris.URI_GEO,"uid",osqId);
      }
      if (osqBBox !== null && osqBBox.length > 0) {
        xmlBuilder.writeAttributeNS(uris.URI_GEO,"box",osqBBox);
      }
      if (osqTime !== null && osqTime.length > 0) {
        xmlBuilder.writeAttributeNS(uris.URI_TIME,"time",osqTime);
      }
      if (osqStartIndex !== null && osqStartIndex.length > 0) {
        xmlBuilder.writeAttribute("startIndex",osqStartIndex);
      }
      if (osqCount !== null && osqCount.length > 0) {
        xmlBuilder.writeAttribute("count",osqCount);
      }
      xmlBuilder.writeEndElement();
    }},

    writeResponse: {writable:true,value:function(task,xmlBuilder) {
      this.writeXmlResponse(task,this.mediaType,xmlBuilder.getXml());
    }},

    writeXmlResponse: {writable:true,value:function(task,mediaType,xml) {
      if (task.request.pretty) xml = task.context.indentXml(task,xml);
      var response = task.response;
      response.put(response.Status_OK,mediaType,xml);
    }}

  });

}());
