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

  /* ============================================================================================ */

  gs.atom.Feed = gs.Object.create(gs.Proto,{

    id: {writable: true, value: null},             // required    (String)
    title: {writable: true, value: null},          // required    (String or gs.atom.Text)
    updated: {writable: true, value: null},        // required    (String - ISO 8601)

    author: {writable: true, value: null},         // recommended (gs.atom.Person)
    link: {writable: true, value: null},           // recommended (Array of gs.atom.Link)

    generator: {writable: true, value: null},      // optional   (gs.atom.Generator)
    icon: {writable: true, value: null},           // optional
    logo: {writable: true, value: null},           // optional
    rights: {writable: true, value: null},         // optional   (String or gs.atom.Text)
    subtitle: {writable: true, value: null}        // optional   (String or gs.atom.Text)

  });

  /* ============================================================================================ */

  gs.atom.Entry = gs.Object.create(gs.Proto,{

    id: {writable: true, value: null},             // required    (String)
    title: {writable: true, value: null},          // required    (String or gs.atom.Text)
    updated: {writable: true, value: null},        // required    (String - ISO 8601)

    author: {writable: true, value: null},         // recommended (gs.atom.Person - can be Array))
    content: {writable: true, value: null},        // recommended (String or gs.atom.Content or gs.atom.Text)
    link: {writable: true, value: null},           // recommended (gs.atom.Link - can be Array)
    summary: {writable: true, value: null},        // recommended (String or gs.atom.Text)

    category: {writable: true, value: null},       // optional   (gs.atom.Category - can be Array)
    contributor: {writable: true, value: null},    // optional   (gs.atom.Person - can be Array)
    published: {writable: true, value: null},      // optional   (String - ISO 8601)
    rights: {writable: true, value: null},         // optional   (String or gs.atom.Text - can be Array)
    source: {writable: true, value: null},         // optional   (gs.atom.Entry)

    bbox: {writable: true, value: null},           // optional   (gs.atom.BBox, custom element)
    point: {writable: true, value: null},          // optional   (gs.atom.Point, custom element)

    toJson: {writable:true,value:function(task) {
      var self = this, json = {}, v;

      var add = function(parent,name,value) {
        if (Array.isArray(value)) {
          value.forEach(function(v2){
            add(parent,name,v2);
          });
        }
        if (gs.atom.Text.isPrototypeOf(value)) {
          value = value.value;
        }
        if (typeof value === "string" && value.length > 0) {
          if (typeof parent[name] === "string") {
            parent[name] = [parent[name]];
          }
          if (Array.isArray(parent[name])) {
            parent[name].push(value);
          } else {
            parent[name] = value;
          }
        }
      };

      var addCategories = function(parent,name,value) {
        if (Array.isArray(value)) {
          value.forEach(function(v2){
            addCategories(parent,name,v2);
          });
        }
        if (gs.atom.Category.isPrototypeOf(value)) {
          if (!parent[name]) parent[name] = [];
          v = {};
          add(v,"label",value.label);
          add(v,"scheme",value.scheme);
          add(v,"term",value.term);
          parent[name].push(v);
        }
      };

      var addLinks = function(parent,name,value) {
        if (Array.isArray(value)) {
          value.forEach(function(v2){
            addLinks(parent,name,v2);
          });
        }
        if (gs.atom.Link.isPrototypeOf(value)) {
          if (!parent[name]) parent[name] = [];
          v = {};
          add(v,"rel",value.rel);
          add(v,"dctype",value.dctype);
          add(v,"type",value.type);
          add(v,"href",value.href);
          add(v,"hreflang",value.hreflang);
          add(v,"_length",value._length);
          add(v,"title",value.title);
          parent[name].push(v);
        }
      };

      var addPerson = function(parent,name,value) {
        if (Array.isArray(value)) {
          value.forEach(function(v2){
            addPerson(parent,name,v2);
          });
        }
        if (gs.atom.Person.isPrototypeOf(value)) {
          v = {};
          add(v,"email",value.email);
          add(v,"name",value.name);
          add(v,"uri",value.uri);
          if (Array.isArray(parent[name])) {
            parent[name].push(v);
          } else if (parent[name]) {
            parent[name] = [parent[name]];
            parent[name].push(v);
          } else {
            parent[name] = v;
          }
        }
      };

      var addText = function(parent,name,value) {
        if (Array.isArray(value)) {
          value.forEach(function(v2){
            addText(parent,name,v2);
          });
        }
        if (gs.atom.Text.isPrototypeOf(value)) {
          v = {};
          add(v,"type",value.type);
          add(v,"value",value.value);
          add(v,"uri",value.uri);
          if (Array.isArray(parent[name])) {
            parent[name].push(v);
          } else if (parent[name]) {
            parent[name] = [parent[name]];
            parent[name].push(v);
          } else {
            parent[name] = v;
          }
        }
      };

      add(json,"id",this.id);
      add(json,"title",this.title);
      add(json,"description",this.summary); // addText?
      add(json,"published",this.published);
      add(json,"updated",this.updated);
      addPerson(json,"author",this.author);
      addPerson(json,"contributor",this.contributor);
      addLinks(json,"links",this.link);
      addCategories(json,"categories",this.category);
      add(json,"rights",this.rights); // addText?

      if (gs.atom.Point.isPrototypeOf(this.point)) {
        json["point"] = {
          x: this.point.x,
          y: this.point.y
        };
      }

      if (gs.atom.BBox.isPrototypeOf(this.bbox)) {
        json["bbox"] = {
          xmin: this.bbox.xmin,
          ymin: this.bbox.ymin,
          xmax: this.bbox.xmax,
          ymax: this.bbox.ymax
        };
      }

      if (gs.atom.Point.isPrototypeOf(this.point)) {
        json["point"] = {
          x: this.point.x,
          y: this.point.y
        };
      }

      if (gs.atom.Text.isPrototypeOf(this.content)) {
        addText(json,"content",this.content);
      } else if (gs.atom.Content.isPrototypeOf(this.content)) {
        json["content"] = v = {};
        add(v,"src",this.content.src);
        add(v,"type",this.content.type);
        add(v,"value",this.content.value);
      }

      // TODO source?

      return json;
    }},

  });

  /* ============================================================================================ */

  gs.atom.BBox = gs.Object.create(gs.Proto,{

    xmin: {writable: true, value: null},
    ymin: {writable: true, value: null},
    xmax: {writable: true, value: null},
    ymax: {writable: true, value: null},

    init: {writable:true,value:function(options) {
      if (options !== null && typeof options === "object") {
        this.xmin = options["xmin"];
        this.ymin = options["ymin"];
        this.xmax = options["xmax"];
        this.ymax = options["ymax"];
      }
      return this;
    }},

    writeGeoRSSBox: {writable:true,value:function(task,xmlBuilder) {
      var rssBox = this.ymin+" "+this.xmin+" "+this.ymax+" "+this.xmax;
      xmlBuilder.writeElement(task.uris.URI_GEORSS,"box",rssBox);
    }},

    writeGeoRSSBox10: {writable:true,value:function(task,xmlBuilder) {
      var rssBox = this.ymin+" "+this.xmin+" "+this.ymax+" "+this.xmax;
      xmlBuilder.writeElement(task.uris.URI_GEORSS10,"box",rssBox);
    }},

    writeOwsBoundingBox: {writable:true,value:function(task,xmlBuilder) {
      var uri = task.uris.URI_OWS2;
      if (task.isCsw2) uri = task.uris.URI_OWS;
      xmlBuilder.writeStartElement(uri,"BoundingBox");
      xmlBuilder.writeElement(uri,"LowerCorner",this.xmin+" "+this.ymin);
      xmlBuilder.writeElement(uri,"UpperCorner",this.xmax+" "+this.ymax);
      xmlBuilder.writeEndElement();
    }}

  });

  /* ============================================================================================ */

  gs.atom.Category = gs.Object.create(gs.Proto,{

    label: {writable: true, value: null},
    scheme: {writable: true, value: null},
    term: {writable: true, value: null}, // required

    init: {writable:true,value:function(options) {
      if (options !== null && typeof options === "object") {
        if (options["label"]) this.label = options["label"];
        if (options["scheme"]) this.scheme = options["scheme"];
        if (options["term"]) this.term = options["term"];
      }
      return this;
    }},

    write: {writable:true,value:function(task,xmlBuilder) {
      if ((this.term === null || this.term.length === 0)) return;
      xmlBuilder.writeStartElement(task.uris.URI_ATOM,"category");
      xmlBuilder.writeAttribute("scheme",this.scheme);
      xmlBuilder.writeAttribute("term",this.term);
      xmlBuilder.writeAttribute("label",this.label);
      xmlBuilder.writeEndElement();
    }}

  });

  /* ============================================================================================ */

  gs.atom.Content = gs.Object.create(gs.Proto,{

    src: {writable: true, value: null},
    type: {writable: true, value: null}, // usually text|html|xhtml
    value: {writable: true, value: null},

    init: {writable:true,value:function(options) {
      if (options !== null && typeof options === "object") {
        if (options["src"]) this.src = options["src"];
        if (options["type"]) this.type = options["type"];
        if (options["value"]) this.value = options["value"];
      }
      return this;
    }},

    write: {writable:true,value:function(task,xmlBuilder) {
      xmlBuilder.writeStartElement(task.uris.URI_ATOM,"content");
      xmlBuilder.writeAttribute("src",this.src);
      xmlBuilder.writeAttribute("type",this.type);
      xmlBuilder.writeEndElement();
      if (typeof this.value === "string") {
        xmlBuilder.writeCharacters(this.value);
      }
    }}

  });

  /* ============================================================================================ */

  gs.atom.Generator = gs.Object.create(gs.Proto,{

    uri: {writable: true, value: null},
    version: {writable: true, value: null},
    value: {writable: true, value: null}

  });

  /* ============================================================================================ */

  gs.atom.Link = gs.Object.create(gs.Proto,{

    href: {writable: true, value: null}, // required
    hreflang: {writable: true, value: null},
    _length: {writable: true, value: null}, // length: length of the resource in bytes
    rel: {writable: true, value: null}, // alternate|enclosure|related|self|via
    title: {writable: true, value: null},
    type: {writable: true, value: null}, // media type

    dctype: {writable: true, value: null}, // custom

    init: {writable:true,value:function(options) {
      if (options !== null && typeof options === "object") {
        if (options["href"]) this.href = options["href"];
        if (options["hreflang"]) this.hreflang = options["hreflang"];
        if (options["_length"]) this._length = options["_length"];
        if (options["rel"]) this.rel = options["rel"];
        if (options["title"]) this.title = options["title"];
        if (options["type"]) this.type = options["type"];
        if (options["dctype"]) this.dctype = options["dctype"];
      }
      return this;
    }},

    write: {writable:true,value:function(task,xmlBuilder) {
      if ((this.href === null || this.href.length === 0)) return;
      xmlBuilder.writeStartElement(task.uris.URI_ATOM,"link");
      xmlBuilder.writeAttribute("rel",this.rel);
      xmlBuilder.writeAttribute("dc:type",this.dctype);
      xmlBuilder.writeAttribute("type",this.type);
      xmlBuilder.writeAttribute("href",this.href);
      xmlBuilder.writeAttribute("hreflang",this.hreflang);
      xmlBuilder.writeAttribute("length",this._length);
      xmlBuilder.writeAttribute("title",this.title);
      xmlBuilder.writeEndElement();
    }}

  });

  /* ============================================================================================ */

  gs.atom.Person = gs.Object.create(gs.Proto,{

    email: {writable: true, value: null},
    name: {writable: true, value: null}, // required
    uri: {writable: true, value: null},

    tag: {writable: true, value: "author"}, // author|contributor

    init: {writable:true,value:function(options) {
      if (options !== null && typeof options === "object") {
        if (options["tag"]) this.tag = options["tag"];
        if (options["email"]) this.email = options["email"];
        if (options["name"]) this.name = options["name"];
        if (options["uri"]) this.uri = options["uri"];
      }
      return this;
    }},

    write: {writable:true,value:function(task,xmlBuilder,tag) {
      if ((this.name === null || this.name.length === 0)) return;
      if ((this.tag === null || this.tag.length === 0)) this.tag = "author";
      xmlBuilder.writeStartElement(task.uris.URI_ATOM,this.tag);
      xmlBuilder.writeElement(task.uris.URI_ATOM,"name",this.name);
      xmlBuilder.writeElement(task.uris.URI_ATOM,"email",this.email);
      xmlBuilder.writeElement(task.uris.URI_ATOM,"uri",this.uri);
      xmlBuilder.writeEndElement();
    }}

  });

  /* ============================================================================================ */

  gs.atom.Point = gs.Object.create(gs.Proto,{

    x: {writable: true, value: null},
    y: {writable: true, value: null},

    init: {writable:true,value:function(options) {
      if (options !== null && typeof options === "object") {
        this.x = options["x"];
        this.y = options["y"];
      }
      return this;
    }},

    writeGeoRSSPoint: {writable:true,value:function(task,xmlBuilder) {
      var rssPoint = this.y+" "+this.x;
      xmlBuilder.writeElement(task.uris.URI_GEORSS,"point",rssPoint);
    }}

  });

  /* ============================================================================================ */

  gs.atom.Text = gs.Object.create(gs.Proto,{

    type: {writable: true, value: null}, // usually text|html|xhtml
    value: {writable: true, value: null},

    init: {writable:true,value:function(options) {
      if (options !== null && typeof options === "object") {
        if (options["type"]) this.type = options["type"];
        if (options["value"]) this.value = options["value"];
      }
      return this;
    }},

    write: {writable:true,value:function(task,xmlBuilder,namespaceURI,localName) {
      xmlBuilder.writeStartElement(namespaceURI,localName);
      xmlBuilder.writeAttribute("type",this.type);
      if (typeof this.value === "string") {
        xmlBuilder.writeCharacters(this.value);
      }
      xmlBuilder.writeEndElement();
    }}

  });

  /* ============================================================================================ */

}());
