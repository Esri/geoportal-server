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

  gs.provider.csw.CswProvider = gs.Object.create(gs.provider.Provider,{

    elementSetName: {writable: true, value: null},
    isCsw2: {writable: true, value: false},
    isCswProvider: {writable: true, value: true},
    kvpNsPrefixByUri: {writable: true, value: null},
    kvpNsUriByPrefix: {writable: true, value: null},
    recordTypeName: {writable: true, value: "Record"},
    responseFields: {writable: true, value: null},
    supportsCsw2: {writable: true, value: true},

    chkBBoxParam: {writable:true,value:function(task) {
      if (task.hasError) return;
      var msg, ows;
      var bbox = task.request.getBBox();
      if (bbox === null || bbox.length === 0) return;
      var a = bbox.split(",");
      if (a.length === 5) {
        var s = a[4].trim();
        if (s !== "4326" && s.toLowerCase() !=="urn:ogc:def:crs:EPSG::4326") {
          msg = "CSW: The bbox CRS is not supported, use urn:ogc:def:crs:EPSG::4326.";
          ows = gs.Object.create(gs.provider.csw.OwsException);
          ows.put(task,ows.OWSCODE_InvalidParameterValue,"bbox",msg);
          return;
        }
      }
      if ((a.length === 4) || (a.length === 5)) {
        var n = task.val.strToNum(a[0].trim(),1);
        if (n > 10000) {
          msg = "CSW: The bbox coordinates should be WGS84.";
          ows = gs.Object.create(gs.provider.csw.OwsException);
          ows.put(task,ows.OWSCODE_InvalidParameterValue,"bbox",msg);
          return;
        }
      }
    }},

    chkParam: {writable:true,value:function(task,key) {
      return task.request.chkParam(key);
    }},

    execute: {writable:true,value:function(task) {
      if (!task.request.hasQueryParameters()) {
        return this.getCapabilities(task);
      }

      var msg, ows, promise;
      var service = this.chkParam(task,"service");
      var request = this.chkParam(task,"request");
      var version = this.chkParam(task,"version");

      if (!task.hasError && version !== null && version.length > 0 && version !== "3.0.0") {
        if (version === "2.0.2" && this.supportsCsw2) {
          this.isCsw2 = true;
          task.isCsw2 = true;
        } else {
          msg = "CSW: version must be 3.0.0";
          if (this.supportsCsw2) msg += " or 2.0.2";
          ows = gs.Object.create(gs.provider.csw.OwsException);
          ows.put(task,ows.OWSCODE_InvalidParameterValue,"version",msg);
        }
      }

      if (!task.hasError && service === null) {
        msg = "CSW: The service parameter is missing.";
        ows = gs.Object.create(gs.provider.csw.OwsException);
        ows.put(task,ows.OWSCODE_MissingParameterValue,"service",msg);
      } else if (ows === null && service.toLowerCase() != "csw") {
        msg = "CSW: The service parameter must be CSW.";
        ows = gs.Object.create(gs.provider.csw.OwsException);
        ows.put(task,ows.OWSCODE_InvalidParameterValue,"service",msg);
      }
      if (!task.hasError && request === null) {
        msg = "CSW: The request parameter is missing.";
        ows = gs.Object.create(gs.provider.csw.OwsException);
        ows.put(task,ows.OWSCODE_MissingParameterValue,"request",msg);
      }
      if (!task.hasError && request.toLowerCase() === "getcapabilities") {
        if (request !== "GetCapabilities" && !this.isCsw2) {
          msg = "CSW: Case sensitive issue, use request=GetCapabilities";
          ows = gs.Object.create(gs.provider.csw.OwsException);
          ows.put(task,ows.OWSCODE_InvalidParameterValue,"request",msg);
        }
      }
      if (!task.hasError) {
        if (this.chkParam(task,"outputFormat") === null) {
          var accept = task.request.getHeader("accept");
          if (accept === "application/atom+xml") {
            this.addOverrideParameter(task,"outputFormat","application/atom+xml");
          }
        }
      }

      if (!task.hasError) {
        var lcRequest = request.toLowerCase();
        if (lcRequest === "getcapabilities") {
          return this.getCapabilities(task);
        } else if (lcRequest === "getrecordbyid") {
          return this.getRecordById(task);
        } else if (lcRequest === "getrecords") {
          return this.getRecords(task);
        } else {
          msg = "CSW: The request parameter is invalid.";
          ows = gs.Object.create(gs.provider.csw.OwsException);
          ows.put(task,ows.OWSCODE_InvalidParameterValue,"request",msg);
          promise = task.context.newPromise();
          promise.reject();
          return promise;
        }
      } else {
        promise = task.context.newPromise();
        promise.reject();
        return promise;
      }
    }},

    getCapabilities: {writable:true,value:function(task) {
      var msg, ows, xml, promise = task.context.newPromise();
      var cswUrl = task.baseUrl+"/csw"; // TODO
      var opensearchDscUrl = task.baseUrl+"/opensearch/description";
      cswUrl = this.makeCapabilitiesHref(task,cswUrl);
      opensearchDscUrl = this.makeCapabilitiesHref(task,opensearchDscUrl);
      var capabilitiesFile = task.config.cswCapabilitiesFile;
      if (this.isCsw2) capabilitiesFile = task.config.csw2CapabilitiesFile;

      var mime = null;
      var hasTextXml = false;
      var hasAppXml = false;
      var hasOther = false;
      var acceptFormats = task.request.getParameterValues("acceptFormats");
      if (acceptFormats !== null && acceptFormats.length === 1) {
        acceptFormats = acceptFormats[0].split(",");
      }
      if (acceptFormats != null) {
        acceptFormats.some(function(s){
          s = s.toLowerCase();
          if (s === "application/xml") {
            hasAppXml = true;
          } else if (s === "text/xml") {
            hasTextXml = true;
          } else if (s.length > 0) {
            hasOther = true;
          }
        });
      }

      var accept = task.request.getHeader("accept");
      if (accept === "text/xml") {
        hasTextXml = true;
      }
      if (!hasAppXml && hasTextXml) {
        mime = "text/xml";
      } else if (!hasAppXml && !hasTextXml && hasOther) {
        msg = "CSW: The acceptFormats parameter is invalid.";
        ows = gs.Object.create(gs.provider.csw.OwsException);
        ows.put(task,ows.OWSCODE_InvalidParameterValue,"acceptFormats",msg);
      }

      if (!task.hasError) {
        var acceptVersions = task.request.getParameterValues("acceptVersions");
        if (acceptVersions !== null && acceptVersions.length === 1) {
          acceptVersions = acceptVersions[0].split(",");
        }
        if (acceptVersions !== null && acceptVersions.length > 0) {
          var has30 = acceptVersions.some(function(s){
            return (s === "3.0.0");
          });
          if (!has30) {
            msg = "CSW: The acceptVersions parameter is invalid, 3.0.0 is required";
            ows = gs.Object.create(gs.provider.csw.OwsException);
            ows.put(task,ows.OWSCODE_VersionNegotiationFailed,"acceptVersions",msg);
          }
        }
      }

      if (!task.hasError) {
        xml = task.context.readResourceFile(capabilitiesFile,"UTF-8");
        xml = xml.trim();
        xml = xml.replace(/{csw.url}/g,task.val.escXml(cswUrl));
        xml = xml.replace(/{opensearch.description.url}/g,task.val.escXml(opensearchDscUrl));
      }

      if (!task.hasError) {
        var sections = this.chkParam(task,"sections");
        if (sections !== null && sections.length > 0) {
          if (sections.toLowerCase() === "all") {
          } else if (sections.toLowerCase() === "filter_capabilities") {
            xml = task.context.removeAllButFilter(xml);
          } else {
            msg = "CSW: The sections parameter must be All or Filter_Capabilities.";
            ows = gs.Object.create(gs.provider.csw.OwsException);
            ows.put(task,ows.OWSCODE_InvalidParameterValue,"sections",msg);
          }
        }
      }

      if (!task.hasError) {
        var response = task.response;
        response.put(response.Status_OK,response.MediaType_APPLICATION_XML,xml);
        if (mime === "text/xml") {
          response.mediaType = response.MediaType_TEXT_XML;
        }
        promise.resolve();
      } else {
        promise.reject();
      }
      return promise;
    }},

    getRecordById: {writable:true,value:function(task) {
      var msg, ows;
      task.request.isItemByIdRequest = true;
      var id = task.request.getParameter("id");
      if (id === null) {
        msg = "CSW: The Id parameter is missing.";
        ows = gs.Object.create(gs.provider.csw.OwsException);
        ows.put(task,ows.OWSCODE_MissingParameterValue,"Id",msg);
      } else if (id.trim().length === 0) {
        msg = "CSW: The Id parameter is empty.";
        ows = gs.Object.create(gs.provider.csw.OwsException);
        ows.put(task,ows.OWSCODE_InvalidParameterValue,"Id",msg);
      }
      if (!task.hasError) this.parseKvp(task);
      if (task.hasError) {
        var promise = task.context.newPromise();
        promise.reject();
        return promise;
      } else {
        return this.search(task);
      }
    }},

    getRecords: {writable:true,value:function(task) {
      var msg, ows;
      this.inputIndexOffset = 1; // TODO?
      var parser = gs.Object.create(gs.provider.csw.GetRecordsParser);
      parser.parseBody(this,task);
      var startPosition = this.chkParam(task,"startPosition");
      if (startPosition !== null && startPosition.length > 0) {
        var start = task.val.strToInt(startPosition,-1);
        // TODO should this be >= 1
        if (start >= 1) {
          // TODO start = start - 1; is this correct indexOffset??
          this.addOverrideParameter(task,"start",""+start);
        }
      }
      var maxRecords = this.chkParam(task,"maxRecords");
      if (maxRecords !== null && maxRecords.length > 0) {
        if (maxRecords.toLowerCase !== "unlimited") {
          var num = task.val.strToInt(maxRecords,-1);
          if (num >= 0) {
            this.addOverrideParameter(task,"num",""+num);
          }
        }
      }
      if (!task.hasError) this.parseKvp(task);
      if (task.hasError) {
        var promise = task.context.newPromise();
        promise.reject();
        return promise;
      } else {
        return this.search(task);
      }
    }},

    makeCapabilitiesHref: {writable:true,value:function(task,cswUrl) {
      // TODO encoding decoding ?
      var str = "", url = task.request.url;
      var n = url.indexOf("?");
      if (n !== -1) str = url.substring(n + 1);
      //console.log("str",str);

      var i, l, qp = str.split("&"), map = {}, name, val, item;
      for (i = 0, l = qp.length; i < l; ++i){
        item = qp[i];
        if (item.length){
          var s = item.indexOf("=");
          if (s < 0){
            name = decodeURIComponent(item);
            val = "";
          } else {
            name = decodeURIComponent(item.slice(0, s));
            val = decodeURIComponent(item.slice(s + 1));
          }
          var lc = name.toLowerCase();
          if (lc !== "service" && lc !== "request" && lc !== "version") {
            if (typeof map[name] == "string"){
              map[name] = [map[name]];
            }
            if (Array.isArray(map[name])){
              map[name].push(val);
            } else {
              map[name] = val;
            }
          }
        }
      }

      var pairs = [], assign;
      for (name in map){
        if (map.hasOwnProperty(name)) {
          val = map[name];
          assign = encodeURIComponent(name) + "=";
          if (Array.isArray(val)){
            for (i = 0, l = val.length; i < l; ++i){
              pairs.push(assign + encodeURIComponent(val[i]));
            }
          } else {
            pairs.push(assign + encodeURIComponent(val));
          }
        }
      }

      var qstr = pairs.join("&");
      if (qstr.length > 0) cswUrl = cswUrl + "?" + qstr;
      //console.log("qstr",qstr);
      //console.log("cswUrl",cswUrl);

      return cswUrl;
    }},

    parseKvp: {writable:true,value:function(task) {
      if (task.hasError) return;
      task.request.f = "csw";
      this.kvpNsPrefixByUri = {};
      this.kvpNsUriByPrefix = {};
      this.chkBBoxParam(task);
      this.parseKvpOutput(task);
      this.parseKvpNamespace(task);
      this.parseKvpTypeNames(task);
      this.parseKvpElementNames(task); // TODO field names
      //this.parseKvpSortBy(task);     // TODO sortBy differences, field names
      //this.parseKvpTime(task);       // TODO validation OwsExceptions??
      task.request.parseF(task);
    }},

    parseKvpElementNames: {writable:true,value:function(task) {
      if (task.hasError) return;
      var msg, ows, self = this;
      var defaultSetName = this.elementSetName;
      var recordTypeName = this.recordTypeName;
      var responseFields = null;

      var elementSetName = this.chkParam(task,"ElementSetName");
      var elementNames = task.request.getParameterValues("ElementName");
      if (elementNames !== null && elementNames.length === 1) {
        elementNames = elementNames[0].split(",");
      }
      if (elementNames !== null && elementNames.length > 0) {
        if (elementSetName !== null && elementSetName.length > 0) {
          msg = "CSW: Either an ElementSetName parameter OR one or more ElementName parameters shall";
          msg += " be specified in a query.";
          ows = gs.Object.create(gs.provider.csw.OwsException);
          ows.put(task,ows.OWSCODE_NoApplicableCode,"ElementSetName",msg);
          return;
        }
      }

      if (elementSetName !== null && elementSetName.length > 0) {
        elementSetName = elementSetName.toLowerCase();
        if (elementSetName === "brief") {
          recordTypeName = "BriefRecord";
        } else if (elementSetName === "summary") {
          recordTypeName = "SummaryRecord";
        } else if (elementSetName === "full") {
          recordTypeName = "Record";
        } else {
          msg = "CSW: The ElementSetName parameter must be brief, summary or full.";
          ows = gs.Object.create(gs.provider.csw.OwsException);
          ows.put(task,ows.OWSCODE_InvalidParameterValue,"ElementSetName",msg);
          return;
        }
      } else if (elementNames != null && elementNames.length > 0) {
        elementSetName = "summary";
        recordTypeName = "SummaryRecord";
        var all = gs.Object.create(gs.provider.csw.QFields);
        all.makeAll(task);
        var matched = gs.Object.create(gs.provider.csw.QFields);
        elementNames.forEach(function(name){
          if (task.hasError) return;
          name = name.trim();
          if (name.length > 0) {
            var field = gs.Object.create(gs.provider.csw.QField);
            field.init(name,null,self.kvpNsUriByPrefix);
            var match = all.match(field);
            if (match) {
              matched.add(match);
            } else {
              msg = "CSW: An ElementName is invalid.";
              ows = gs.Object.create(gs.provider.csw.OwsException);
              ows.put(task,ows.OWSCODE_InvalidParameterValue,"ElementName",msg);
              return;
            }
          }
        });
        if (task.hasError) return;
        if (matched.size() > 0) {
          responseFields = matched;
          elementSetName = null;
          recordTypeName = "Record";
        }
      } else {
        if (defaultSetName === "brief") {
          elementSetName = "brief";
          recordTypeName = "BriefRecord";
        } else if (defaultSetName === "full") {
          elementSetName = "full";
          recordTypeName = "Record";
        } else {
          elementSetName = "summary";
          recordTypeName = "SummaryRecord";
        }
      }

      this.recordTypeName = recordTypeName;
      this.elementSetName = elementSetName;
      this.elementNames = elementNames;
      this.responseFields = responseFields;
    }},

    parseKvpNamespace: {writable:true,value:function(task) {
      if (task.hasError) return;
      var msg, ows, self = this;

      // pattern: namespace=xmlns(ogc=http://www.opengis.net/ogc),xmlns(gml=http://www.opengis.net/gml)...
      var namespaces = task.request.getParameterValues("namespace");
      if (namespaces !== null && namespaces.length === 1) {
        namespaces = namespaces[0].split(",");
      }
      if (namespaces !== null) {
        namespaces.forEach(function(ns){
          if (task.hasError) return;
          var nsPfx = null, sUri = null, pair;
          if (ns.toLowerCase().indexOf("xmlns(") === 0) {
            ns = ns.substring(6);
            if (task.val.endsWith(ns,")")) {
              ns = ns.substring(0,ns.length - 1);
            }
            if (ns.length > 0) {
              pair = ns.split("=");
              if (pair.length == 1) {
                nsUri = pair[0];
              } else if (pair.length == 2) {
                nsPfx = pair[0];
                nsUri = pair[1];
              }
            }
          }
          if (nsPfx === null || nsPfx.length === 0 || nsUri === null || nsUri.length === 0) {
            msg = "The namespace must follow the following pattern:";
            msg += " xmlns(pfx1=uri1),xmlns(pfx2=uri2),...";
            ows = gs.Object.create(gs.provider.csw.OwsException);
            ows.put(task,ows.OWSCODE_InvalidParameterValue,"namespace",msg);
            return;
          } else {
            if (nsPfx !== null && nsPfx.length > 0) {
              self.kvpNsUriByPrefix[nsPfx] = nsUri;
              self.kvpNsPrefixByUri[nsUri] = nsPfx;
            }
          }
        });
        if (task.hasError) return;
      }

      //for (var k in this.kvpNsUriByPrefix) console.log(k,":",this.kvpNsUriByPrefix[k]);
      //for (var k in this.kvpNsPrefixByUri) console.log(k,":",this.kvpNsPrefixByUri[k]);
    }},

    parseKvpOutput: {writable:true,value:function(task) {
      if (task.hasError) return;
      var lc, msg, ows;
      var outputSchema = this.chkParam(task,"outputSchema");
      var outputFormat =  this.chkParam(task,"outputFormat");
      var f =  this.chkParam(task,"f");
      if (outputSchema !== null && outputSchema.length > 0) {
        lc = outputSchema.toLowerCase();
        if (lc === task.uris.URI_CSW.toLowerCase()) {
          outputSchema = task.uris.URI_CSW;
        } else if (lc === task.uris.URI_ATOM.toLowerCase()) {
          outputSchema = task.uris.URI_ATOM;
        } else {
          msg = "CSW: The outputSchema parameter must be "+task.uris.URI_CSW+" or "+task.uris.URI_ATOM;
          ows = gs.Object.create(gs.provider.csw.OwsException);
          ows.put(task,ows.OWSCODE_InvalidParameterValue,"outputSchema",msg);
          return;
        }
      } else if (outputFormat !== null && outputFormat.length > 0) {
        lc = outputFormat.toLowerCase();
        if (lc === "application/xml") {
          outputSchema = task.uris.URI_CSW;
        } else if (lc === "application/atom+xml") {
          outputSchema = task.uris.URI_ATOM;
        } else {
          msg = "CSW: The outputFormat parameter must be application/xml or application/atom+xml";
          ows = gs.Object.create(gs.provider.csw.OwsException);
          ows.put(task,ows.OWSCODE_InvalidParameterValue,"outputFormat",msg);
          return;
        }
      } else if (f === null || f.length === 0) {
        outputSchema = task.uris.URI_CSW;
      }
      if (outputSchema !== null && outputSchema.length > 0) {
        this.addOverrideParameter(task,"outputSchema",outputSchema);
      }

    }},

    parseKvpTypeNames: {writable:true,value:function(task) {
      if (task.hasError) return;
      var msg, ows;
      var typeNames = this.chkParam(task,"typeNames");
      if (typeNames !== null) {
        var ok = false, lc = typeNames.toLowerCase();
        if (lc === "record" || lc === "csw:record") {
          ok = true;
        } else {
          var pfx = this.kvpNsPrefixByUri[task.uris.URI_CSW];
          if (typeof pfx === "string") {
            if (lc === pfx.toLowerCase()+":record") {
              ok = true;
            }
          }
        }
        if (!ok) {
          msg = "CSW: The typeNames parameter must be csw:Record";
          ows = gs.Object.create(gs.provider.csw.OwsException);
          ows.put(task,ows.OWSCODE_InvalidParameterValue,"typeNames",msg);
          return;
        }
      }
    }},

    search: {writable:true,value:function(task) {
      var promise = task.context.newPromise();
      task.request.parseF(task);
      this.setWriter(task);
      task.target.search(task).then(function(searchResult){
        if (task.request.isItemByIdRequest && (!searchResult.items || searchResult.items.length === 0)) {
          task.response.status = task.response.Status_NOT_FOUND;
          ows = gs.Object.create(gs.provider.csw.OwsException);
          ows.put(task,ows.OWSCODE_InvalidParameterValue,"id","Id not found.");
          promise.resolve();
        } else {
          task.writer.write(task,searchResult);
          promise.resolve();
        }
      })["catch"](function(error){
        var msg = "Search error";
        if (typeof error.message === "string" && error.message.length > 0) {
          msg = error.message;
        }
        task.response.status = task.response.Status_INTERNAL_SERVER_ERROR;
        ows = gs.Object.create(gs.provider.csw.OwsException);
        ows.put(task,ows.OWSCODE_NoApplicableCode,null,msg);
        promise.resolve();
      });
      return promise;
    }}

  });

  /* ============================================================================================ */

  gs.provider.csw.QField = gs.Object.create(gs.Proto,{

    name: {writable: true, value: null},
    namespacePrefix: {writable: true, value: null},
    namespaceUri: {writable: true, value: null},
    qname: {writable: true, value: null},
    sortable: {writable: true, value: false},

    init: {writable:true,value:function(name, namespaceUri, nsUriByPrefix) {
      this.name = name;
      this.qname = name;
      var chkByPrefix = true;
      if (typeof namespaceUri === "string" && namespaceUri.length > 0) {
        this.namespaceUri = namespaceUri;
        chkByPrefix = false;
      }
      var idx = name.indexOf(":");
      if (idx != -1) {
        this.namespacePrefix = name.substring(0,idx);
        this.name = name.substring(idx + 1);
        if (typeof nsUriByPrefix !== "undefined" && nsUriByPrefix !== null) {
          if (chkByPrefix && typeof nsUriByPrefix !== "string") {
            this.namespaceUri = nsUriByPrefix[this.namespacePrefix];
          }
        }
      }
    }}

  });

  /* ============================================================================================ */

  gs.provider.csw.QFields = gs.Object.create(gs.Proto,{

    list: {writable: true, value: null},

    add: {writable:true,value:function(field) {
      if (!this.list) this.list = [];
      this.list.push(field);
      return field;
    }},

    _add: {writable:true,value:function(name,namespaceUri) {
      var field = gs.Object.create(gs.provider.csw.QField);
      field.init(name,namespaceUri,null);
      return this.add(field);
    }},

    makeAll: {writable:true,value:function(task) {
      this.list = [];
      this._add("dc:identifier",task.uris.URI_DC);                 // brief
      this._add("dc:title",task.uris.URI_DC).sortable = true;      // brief
      this._add("dc:type",task.uris.URI_DC).sortable = true;       // brief
      this._add("ows:BoundingBox",task.uris.URI_OWS2);             // brief
      this._add("dc:subject",task.uris.URI_DC);                    // summary
      //this._add("dc:format",task.uris.URI_DC);                   // summary
      //this._add("dc:relation",task.uris.URI_DC);                 // summary
      this._add("dct:modified",task.uris.URI_DCT).sortable = true; // summary
      this._add("dct:abstract",task.uris.URI_DCT);                 // summary
      //this._add("dct:spatial",task.uris.URI_DCT);                // summary
      //this._add("csw:TemporalExtent",task.uris.URI_CSW);         // summary
      //this._add("csw:AnyText",task.uris.URI_CSW);                // full
      this._add("dct:alternative",task.uris.URI_DCT);              // full - Snippet
      this._add("dc:creator",task.uris.URI_DC);                    // full
      this._add("dct:created",task.uris.URI_DCT);                  // full
      this._add("dc:contributor",task.uris.URI_DC);                // full - Credits
      this._add("dc:rights",task.uris.URI_DC);                     // full - Access and Use Constraints
      this._add("dct:references",task.uris.URI_DCT);               // summary? should this be full? - Links
    }},

    match: {writable:true,value:function(field) {
      var found = null, lc = field.name.toLowerCase();
      if (this.list) {
        this.list.some(function(f){
          if (lc === f.name.toLowerCase()) {
            if (field.namespaceUri === f.namespaceUri)  {
              found = f;
              return true;
            }
          }
        });
      }
      return found;
    }},

    size: {writable:true,value:function() {
      if (this.list) return this.list.length;
      return 0;
    }}

  });

  /* ============================================================================================ */

}());
