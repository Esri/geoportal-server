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

  /* ============================================================================================ */

  gs.base.XmlBuilder = gs.Object.create(gs.Proto,{

    prefixes: {writable: true, value: null},
    prefixByUri: {writable: true, value: null},
    uriByPrefix: {writable: true, value: null},

    activeElement: {writable: true, value: null},
    root: {writable: true, value: null},
    stack: {writable: true, value: null},

    sbXml: {writable: true, value: null},

    getXml: {writable:true,value:function() {
      // <?xml version="1.0" encoding="UTF-8" standalone="no"?>
      var v = this.sbXml.toString();
      if (typeof v === "string") {
        v = v.trim();
        if (v.length > 0) {
          v = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + v;
        }
      }
      return v;
    }},

    init: {writable:true,value:function(stringBuilder) {
      this.sbXml = stringBuilder;
      return this;
    }},

    writeAttribute: {writable:true,value:function(localName,value) {
      if (value === null) return;
      this.writeAttributeNS(null,localName,value);
    }},

    writeAttributeNS: {writable:true,value:function(namespaceURI,localName,value) {
      if (value === null) return;
      var prefix = null;
      if (namespaceURI !== null) prefix = this.prefixByUri[namespaceURI];
      var a = gs.Object.create(gs.base.XmlAttribute).init(prefix,localName,namespaceURI);
      a.value = value;
      this.activeElement.attributes.push(a);
    }},

    writeCharacters: {writable:true,value:function(value) {
      if (value === null) return;
      if (typeof this.activeElement.value !== "string") this.activeElement.value = "";
      this.activeElement.value += value;
    }},

    writeElement: {writable:true,value:function(namespaceURI,localName,value) {
      if (value === null) return;
      this.writeStartElement(namespaceURI,localName);
      this.writeCharacters(value);
      this.writeEndElement();
    }},

    writeEndDocument: {writable:true,value:function() {
      //console.log("writeEndDocument",this.stack.length);
      var self = this;
      var doc = {
        sbXml: this.sbXml,
        val: gs.Object.create(gs.base.Val)
      };
      if (this.root) {
        doc.xml = doc.val.XML_HEADER;
        if (this.prefixes && this.prefixes.length > 0) {
          this.prefixes.forEach(function(pfx){
            var a = gs.Object.create(gs.base.XmlAttribute).init("xmlns",pfx,null);
            a.value = self.uriByPrefix[pfx];
            self.root.attributes.push(a);
          });
        }
        this.root.write(doc);
      }
    }},

    writeEndElement: {writable:true,value:function() {
      //console.log("end /",this.activeElement.prefix+":"+this.activeElement.localName);
      var el = this.stack.shift();
      if (this.stack.length > 0) {
        this.activeElement = this.stack[0];
      } else {
        this.activeElement = null;
      }
    }},

    writeNamespace: {writable:true,value:function(prefix,namespaceURI) {
      this.prefixes.push(prefix);
      this.prefixByUri[namespaceURI] = prefix;
      this.uriByPrefix[prefix] = namespaceURI;
    }},

    writeStartDocument: {writable:true,value:function() {
      this.stack = [];
      this.prefixes = [];
      this.prefixByUri = {};
      this.uriByPrefix = {};
    }},

    writeStartElement: {writable:true,value:function(namespaceURI,localName) {
      var prefix = this.prefixByUri[namespaceURI];
      this.writeStartElementPfx(prefix,namespaceURI,localName);
    }},

    writeStartElementPfx: {writable:true,value:function(prefix,namespaceURI,localName) {
      var el = gs.Object.create(gs.base.XmlElement).init(prefix,localName,namespaceURI);
      if (this.activeElement) {
        this.activeElement.elements.push(el);
      } else {
        this.root = el;
      }
      this.stack.unshift(el);
      this.activeElement = el;
      //console.log("start",prefix+"+"+localName,namespaceURI);
    }}

  });

  /* ============================================================================================ */

  gs.base.XmlNode = gs.Object.create(gs.Proto,{

    localName: {writable: true, value: null},
    namespaceURI: {writable: true, value: null},
    prefix: {writable: true, value: null},
    value: {writable: true, value: null},

    init: {writable:true,value:function(prefix,localName,namespaceURI) {
      this.prefix = prefix;
      this.localName = localName;
      this.namespaceURI = namespaceURI;
      return this;
    }}
  });

  /* ============================================================================================ */

  gs.base.XmlAttribute = gs.Object.create(gs.base.XmlNode,{
  });

  /* ============================================================================================ */

  gs.base.XmlElement = gs.Object.create(gs.base.XmlNode,{

    attributes: {writable: true, value: null},
    elements: {writable: true, value: null},

    init: {writable:true,value:function(prefix,localName,namespaceURI) {
      gs.base.XmlNode.init.call(this,prefix,localName,namespaceURI); // call super
      this.attributes = [];
      this.elements = [];
      return this;
    }},

    write: {writable:true,value:function(doc) {
      var name = this.localName, sa = "", v = null;
      var hasElements = (this.elements && this.elements.length > 0);
      if (typeof this.prefix === "string" && this.prefix.length > 0) {
        name = this.prefix+":"+this.localName;
      }
      if (typeof this.value === "string" && this.value.length > 0) {
        v = doc.val.escXml(this.value);
      }
      if (this.attributes && this.attributes.length > 0) {
        this.attributes.forEach(function(a){
          if (typeof a.prefix === "string" && a.prefix.length > 0) {
            sa += " "+a.prefix+":"+a.localName;
          } else {
            sa += " "+a.localName;
          }
          sa += "=\""+doc.val.escXml(a.value)+"\"";
        });
      }
      //if (sa.length === 0) sa = " ";
      if (v === null && !hasElements) {
        doc.sbXml.append("<"+name+sa+"/>");
      } else {
        doc.sbXml.append("<"+name+sa+">");
        if (v !== null) {
          doc.sbXml.append(v);
        }
        if (hasElements) {
          this.elements.forEach(function(el){
            el.write(doc);
          });
        }
        doc.sbXml.append("</"+name+">");
      }
    }}

  });

  /* ============================================================================================ */

}());
