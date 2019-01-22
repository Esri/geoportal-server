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

  gs.target.types["csw"] = {
    newInstance: function(options,safeOptions) {
      if (options && options.url) options.getRecordsUrl = options.url;
      var target = gs.Object.create(gs.target.csw.CswTarget);
      target.safeMixin(options).mixin(safeOptions);
      return target;
    }
  };

  gs.target.csw.CswTarget = gs.Object.create(gs.target.Target, {

    // http://docs.opengeospatial.org/is/12-176r7/12-176r7.html

    cswVersion: {writable: true, value: "3.0.0"},

    elementSetName: {writable: true, value: "summary"},

    getCapabilitiesUrl: {writable: true, value: null},

    getRecordByIdUrl: {writable: true, value: null},

    getRecordsUrl: {writable: true, value: null},

    resultType: {writable: true, value: "RESULTS"},

    /* ............................................................................................ */

    appendPropertyClause: {writable:true,value:function(task,targetRequest,
        clauseName,propertyName,literal,literal2) {
      // TODO use fieldAliases
      if (typeof propertyName === "string" && propertyName.length > 0 &&
          typeof literal === "string" && literal.length > 0) {
        var uris = targetRequest.uris;
        var xmlBuilder = targetRequest.xmlBuilder;
        var propertyTag = "ValueReference";
        if (this.cswVersion === "2.0.2") {
          propertyTag = "PropertyName";
        }
        xmlBuilder.writeStartElement(uris.fes,clauseName);
        if (clauseName === "PropertyIsLike") {
          xmlBuilder.writeAttribute("wildcard","*");
          xmlBuilder.writeAttribute("singleChar","?");
          xmlBuilder.writeAttribute("escapeChar","\\");
        }
        xmlBuilder.writeElement(uris.fes,propertyTag,propertyName);
        if (clauseName === "PropertyIsBetween") {
          xmlBuilder.writeElement(uris.fes,"LowerBoundary",literal);
          xmlBuilder.writeElement(uris.fes,"UpperBoundary",literal2);
        } else {
          xmlBuilder.writeElement(uris.fes,"Literal",literal);
        }
        xmlBuilder.writeEndElement();
      }
    }},

    appendQ: {writable:true,value:function(urlParams,q) {
      if (typeof q === "string" && q.length > 0) {
        if (typeof urlParams.q === "string" && urlParams.q.length > 0) {
          urlParams.q = "("+urlParams.q+") AND ("+q+")";
        } else {
          urlParams.q = q;
        }
      }
    }},

    appendSpatialClause: {writable:true,value:function(task,targetRequest,clauseName,propertyName,bbox) {
      if (typeof propertyName === "string" && propertyName.length > 0 &&
        typeof bbox === "string" && bbox.length > 0) {
        var uris = targetRequest.uris;
        var xmlBuilder = targetRequest.xmlBuilder;
        var propertyTag = "ValueReference";
        if (this.cswVersion === "2.0.2") {
          propertyTag = "PropertyName";
        }
        xmlBuilder.writeStartElement(uris.fes,clauseName);
        xmlBuilder.writeElement(uris.fes,propertyTag,propertyName);
        xmlBuilder.writeStartElement(uris.gml,"Box");
        xmlBuilder.writeAttribute("srsName","http://www.opengis.net/gml/srs/epsg.xml#4326");
        xmlBuilder.writeElement(uris.gml,"coordinates",bbox);
        xmlBuilder.writeEndElement();
        xmlBuilder.writeEndElement();
      }
    }},

    appendType: {writable:true,value:function(task,targetRequest,v) {
      if (typeof v === "string" && v.length > 0) {
        var isLiveData = (typeof v === "string"  && v.toLowerCase() === "livedata");
        var urlParams = targetRequest.urlParams;
        var xmlBuilder = targetRequest.xmlBuilder;
        if (urlParams) {
          if (typeof urlParams.type === "string") {
            urlParams.type = [urlParams.type];
          }
          if (Array.isArray(urlParams.type)) {
            urlParams.type.push(v);
          } else {
            urlParams.type = v;
          }
        }
        if (xmlBuilder && isLiveData) {
          var prop = this.schema.liveDataPropertyName;
          if (prop) {
            this.appendPropertyClause(task,targetRequest,"PropertyIsEqualTo",prop,v);
          }
        }
      }
    }},

    buildGetRecordsUrl: {writable:true,value:function(task,targetRequest) {
      // CSW3
      var urlParams = targetRequest.urlParams = {};
      urlParams.elementSetName = this.elementSetName;
      //urlParams["resultType"] = this.resultType;
      this.preparePaging(task,targetRequest);
      this.prepareRequiredFilter(task,targetRequest);
      this.prepareQ(task,targetRequest);
      this.prepareFilter(task,targetRequest);
      this.prepareIds(task,targetRequest);
      this.prepareTypes(task,targetRequest);
      this.prepareModified(task,targetRequest);
      this.prepareTimePeriod(task,targetRequest);
      this.prepareBBox(task,targetRequest);
      this.prepareOther(task,targetRequest);
      this.prepareSort(task,targetRequest);
    }},

    buildGetRecordsXml: {writable:true,value:function(task,targetRequest) {
      var uris = targetRequest.uris = {};
      if (this.cswVersion === "2.0.2") {
        uris.csw = task.uris.URI_CSW2;
        uris.ows = task.uris.URI_OWS;
        uris.fes = task.uris.URI_OGC;
        uris.gml = task.uris.URI_GML;
      } else {
        uris.csw = task.uris.URI_CSW3;
        uris.ows = task.uris.URI_OWS2;
        uris.fes = task.uris.URI_FES2;
        uris.gml = task.uris.URI_GML32;
      }
      uris.dc = task.uris.URI_DC;
      uris.dct = task.uris.URI_DCT;

      var xmlBuilder = targetRequest.xmlBuilder = this.newXmlBuilder(task);
      xmlBuilder.writeStartDocument();
      xmlBuilder.writeStartElementPfx("csw",uris.csw,"GetRecords");
      xmlBuilder.writeNamespace("csw",uris.csw);
      xmlBuilder.writeNamespace("fes",uris.fes);
      xmlBuilder.writeNamespace("gml",uris.gml);
      xmlBuilder.writeAttribute("service","CSW");
      xmlBuilder.writeAttribute("version",this.cswVersion); // TODO do we need this?
      xmlBuilder.writeAttribute("resultType",this.resultType);
      if (this.cswVersion === "2.0.2") {
        xmlBuilder.writeAttribute("outputFormat","application/xml");
        xmlBuilder.writeAttribute("outputSchema",uris.csw);
      } else {
        // TODO Switch to Atom for CSW3?
        xmlBuilder.writeAttribute("outputFormat","application/xml");
        xmlBuilder.writeAttribute("outputSchema",uris.csw);
      }
      this.preparePaging(task,targetRequest);
      xmlBuilder.writeStartElement(uris.csw,"Query");
      xmlBuilder.writeElement(uris.csw,"ElementSetName",this.elementSetName);
      xmlBuilder.writeStartElement(uris.csw,"Constraint");
      xmlBuilder.writeAttribute("version","1.1.0");
      xmlBuilder.writeStartElement(uris.fes,"Filter");
      xmlBuilder.writeStartElement(uris.fes,"And");

      this.prepareRequiredFilter(task,targetRequest);
      this.prepareQ(task,targetRequest);
      this.prepareFilter(task,targetRequest);
      this.prepareIds(task,targetRequest);
      this.prepareTypes(task,targetRequest);
      this.prepareModified(task,targetRequest);
      this.prepareTimePeriod(task,targetRequest);
      this.prepareBBox(task,targetRequest);
      this.prepareOther(task,targetRequest);

      xmlBuilder.writeEndElement(); // And
      xmlBuilder.writeEndElement(); // Filter
      xmlBuilder.writeEndElement(); // Constraint
      this.prepareSort(task,targetRequest);
      xmlBuilder.writeEndElement(); // Query
      xmlBuilder.writeEndElement(); // GetRecords
      xmlBuilder.writeEndDocument();
      targetRequest.getRecordsXml = xmlBuilder.getXml();
    }},

    handleGetRecordsResponse: {writable:true,value:function(task,response,searchResult) {
      var msg, xmlInfo;
      try {
        xmlInfo = this.newXmlInfo(task,response);
      } catch(ex) {
        msg = "CswTarget: GetRecords returned an invalid XML";
        console.log(msg,"\r\n",response);
        throw ex;
      }
      if (!xmlInfo || !xmlInfo.root) return;
      var rootInfo = xmlInfo.getNodeInfo(xmlInfo.root);
      if (rootInfo.localName === "ExceptionReport") {
        msg = "CswTarget: GetRecords returned an ExceptionReport";
        console.log(msg,"\r\n",response);
        throw new Error(msg);
      }
      searchResult.items = [];
      xmlInfo.forEachChild(xmlInfo.root,function(result){
        if (result.localName === "SearchResults") {
          xmlInfo.forEachAttribute(result.node,function(attr){
            if (attr.localName === "numberOfRecordsMatched") {
              searchResult.totalHits = task.val.strToInt(attr.nodeText,0);
            }
          });
          xmlInfo.forEachChild(result.node,function(recordInfo){
            // TODO what about a non Dublin Core record, e.g. an ISO document
            if (recordInfo.localName === "BriefRecord" ||
                recordInfo.localName === "SummaryRecord" ||
                recordInfo.localName === "Record") {
              searchResult.items.push({
                xmlInfo: xmlInfo,
                recordInfo: recordInfo
              });
            }
          });
        }
      });
    }},

    getSchemaClass: {writable:true,value:function() {
      return gs.target.csw.CswSchema;
    }},

    newXmlBuilder: {writable:true,value:function(task) {
      return task.context.newXmlBuilder(task);
    }},

    newXmlInfo: {writable:true,value:function(task,xmlString) {
      return task.context.newXmlInfo(task,xmlString);
    }},

    /* ............................................................................................ */

    prepare: {writable:true,value:function(task) {
      var promise = task.context.newPromise("prepare");
      if (!this.schema) this.schema = this.newSchema(task);
      var targetRequest = {
        getRecordsXml: null,
        uris: null,
        urlParams: null,
        xmlBuilder: null
      };
      if (this.cswVersion === "2.0.2") {
        this.buildGetRecordsXml(task,targetRequest);
      } else {
        this.buildGetRecordsUrl(task,targetRequest);
      }
      promise.resolve(targetRequest);
      return promise;
    }},

    prepareBBox: {writable:true,value:function(task,targetRequest) {
      var urlParams = targetRequest.urlParams;
      var xmlBuilder = targetRequest.xmlBuilder;
      var bbox = task.request.getBBox();
      if (typeof bbox === "string" && bbox.length > 0) {
        var rel = task.request.getSpatialRel();
        if (typeof rel === "string" && rel.length > 0) {
          rel = rel.toLowerCase();
        }
        if (urlParams) {
          urlParams.bbox = bbox;
          if (rel === "intersects" || rel === "within") {
            urlParams.spatialRel = rel; // this isn't part of the 3.0.0 spec
          }
        }
        if (xmlBuilder) {
          var cn = "BBOX";
          var pn = this.schema.spatialPropertyName;
          if (rel === "within") cn = "Within";
          this.appendSpatialClause(task,targetRequest,cn,pn,bbox);
        }
      }
    }},

    prepareFilter: {writable:true,value:function(task,targetRequest) {
      var urlParams = targetRequest.urlParams;
      var xmlBuilder = targetRequest.xmlBuilder;
      var v = task.request.getFilter();
      if (urlParams) this.appendQ(urlParams,v);
      if (xmlBuilder) {
        var cn = this.schema.qClauseName;
        var pn = this.schema.qPropertyName;
        this.appendPropertyClause(task,targetRequest,cn,pn,v);
      }
    }},

    prepareIds: {writable:true,value:function(task,targetRequest) {
      var urlParams = targetRequest.urlParams;
      var xmlBuilder = targetRequest.xmlBuilder;
      var uris = targetRequest.uris;
      var self = this, pn = this.schema.idPropertyName;
      if (typeof pn !== "string" || pn.length === 0) xmlBuilder = null;
      var ids = task.request.getIds();
      if (ids && ids.length === 1) {
        if (urlParams) urlParams.id = ids[0];
        if (xmlBuilder) {
          self.appendPropertyClause(task,targetRequest,"PropertyIsEqualTo",pn,ids[0]);
        }
      } else if (ids && ids.length > 1) {
        if (xmlBuilder) {
          xmlBuilder.writeStartElement(uris.fes,"Or");
        }
        ids.forEach(function(id){
          if (urlParams) {
            if (Array.isArray(urlParams.id)) {
              urlParams.id.push(id);
            } else if (typeof urlParams.id === "undefined") {
              urlParams.id = id;
            } else {
              urlParams.id = [urlParams.id];
              urlParams.id.push(id);
            }
          }
          if (xmlBuilder) {
            self.appendPropertyClause(task,targetRequest,"PropertyIsEqualTo",pn,id);
          }
        });
        if (xmlBuilder) {
          xmlBuilder.writeEndElement();
        }
      }
    }},

    prepareModified: {writable:true,value:function(task,targetRequest) {
      var urlParams = targetRequest.urlParams;
      var xmlBuilder = targetRequest.xmlBuilder;
      var period = task.request.getModifiedPeriod();
      var param = "";
      var pn = this.schema.modifiedPropertyName;
      if (period.from !== null) {
        param = period.from;
        if (xmlBuilder) {
          this.appendPropertyClause(task,targetRequest,
            "PropertyIsGreaterThanOrEqualTo",pn,period.from);
        }
      }
      if (period.to !== null) {
        param += "/"+period.to;
        if (xmlBuilder) {
          this.appendPropertyClause(task,targetRequest,
            "PropertyIsLessThanOrEqualTo",pn,period.to);
        }
      }
      if (urlParams && param.length > 0) {
        urlParams.modified = param; // not in CSW 3.0.0 spec
      }
    }},

    prepareOther: {writable:true,value:function(task,targetRequest) {
    }},

    preparePaging: {writable:true,value:function(task,targetRequest) {
      var urlParams = targetRequest.urlParams;
      var xmlBuilder = targetRequest.xmlBuilder;
      var start = task.request.getStart();
      start = task.val.strToInt(start,null);
      if (typeof start === "number" && task.request.queryIsZeroBased) start = start + 1;
      if (typeof start === "number" && start >= 1) {
        if (urlParams) urlParams.startPosition = start;
        if (xmlBuilder) xmlBuilder.writeAttribute("startPosition",""+start);
      }
      var num = task.request.getNum();
      num = task.val.strToInt(num,null);
      if (typeof num === "number" && num > 0) {
        if (urlParams) urlParams.maxRecords = num;
        if (xmlBuilder) xmlBuilder.writeAttribute("maxRecords",""+num);
      }
    }},

    prepareQ: {writable:true,value:function(task,targetRequest) {
      var urlParams = targetRequest.urlParams;
      var xmlBuilder = targetRequest.xmlBuilder;
      var v = task.request.getQ();
      if (urlParams) this.appendQ(urlParams,v);
      if (xmlBuilder) {
        var cn = this.schema.qClauseName;
        var pn = this.schema.qPropertyName;
        this.appendPropertyClause(task,targetRequest,cn,pn,v);
      }
    }},

    prepareRequiredFilter: {writable:true,value:function(task,targetRequest) {
    }},

    prepareSort: {writable:true,value:function(task,targetRequest) {
      var urlParams = targetRequest.urlParams;
      var xmlBuilder = targetRequest.xmlBuilder;
      var uris = targetRequest.uris;
      var sortables = this.schema.sortables;
      if (!sortables) return;

      var getField = function(v) {
        v = v.toLowerCase();
        for (var k in sortables) {
          if (sortables.hasOwnProperty(k)) {
            if (v === k.toLowerCase()) {
              return sortables[k];
            }
          }
        }
        return null;
      };

      var sort, sortOptions = task.request.getSortOptions();
      if (Array.isArray(sortOptions)) {
        sortOptions.forEach(function(sortOption){
          var field = getField(sortOption.field);
          if (typeof field === "string" && field.length > 0) {
            if (!sort) sort = [];
            sort.push({"field": field, "order": sortOption.order});
          }
        });
      }

      if (sort && sort.length > 0 && urlParams) {
        var param = "";
        sort.forEach(function(sortOption){
          if (param.length > 0) param += ",";
          param += sortOption.field;
          if (sortOption.order === "desc") param += ":D";
          //else param += ":A";
        });
        urlParams.sortBy = param;
      }

      if (sort && sort.length > 0 && xmlBuilder) {
        xmlBuilder.writeStartElement(uris.fes,"SortBy");
        sort.forEach(function(sortOption){
          xmlBuilder.writeStartElement(uris.fes,"SortProperty");
          xmlBuilder.writeElement(uris.fes,"PropertyName",sortOption.field);
          if (typeof sortOption.order === "string" && sortOption.order.length > 0) {
            xmlBuilder.writeElement(uris.fes,"SortOrder",sortOption.order.toUpperCase());
          }
          xmlBuilder.writeEndElement();
        });
        xmlBuilder.writeEndElement();
      }
    }},

    prepareTimePeriod: {writable:true,value:function(task,targetRequest) {
      // temporal extent of the data
      var urlParams = targetRequest.urlParams;
      var xmlBuilder = targetRequest.xmlBuilder;
      var period = task.request.getTimePeriod();
      var param = "";
      var pn = this.schema.timePeriodPropertyName;
      if (period.from !== null) {
        param = period.from;
        if (xmlBuilder) {
          this.appendPropertyClause(task,targetRequest,
            "PropertyIsGreaterThanOrEqualTo",pn,period.from);
        }
      }
      if (period.to !== null) {
        param += "/"+period.to;
        if (xmlBuilder) {
          this.appendPropertyClause(task,targetRequest,
            "PropertyIsLessThanOrEqualTo",pn,period.to);
        }
      }
      if (urlParams && param.length > 0) {
        urlParams.time = param;
      }
    }},

    prepareTypes: {writable:true,value:function(task,targetRequest) {
      // TODO liveData?
      // TODO ISO topic categories?
      // TODO IMS content types?
      var self = this;
      var schema = this.schema;
      var types = task.request.getTypes();
      if (!Array.isArray(types) || types.length === 0) return;
      types.forEach(function(t){
        var t2 = schema.translateTypeName(task,t);
        if (Array.isArray(t2)) {
          t2.forEach(function(t3){
            self.appendType(task,targetRequest,t3);
          });
        } else {
          self.appendType(task,targetRequest,t2);
        }
      });
    }},

    /* ............................................................................................ */

    search: {writable:true,value:function(task) {
      var self = this;
      var promise = task.context.newPromise();

      this.prepare(task).then(function(targetRequest){
        var url = self.getRecordsUrl;
        var data = null, dataContentType = "application/xml";
        if (targetRequest && targetRequest.getRecordsXml) {
          data = targetRequest.getRecordsXml;
        } else if (targetRequest && task.val.hasAnyProperty(targetRequest.urlParams)) {
          var qstr = self.urlParamsToQueryString(targetRequest.urlParams);
          if (qstr !== null && qstr.length > 0) {
            if (url.indexOf("?") === -1) url += "?" + qstr;
            else url += "&" + qstr;
          }
        }
        if (task.verbose) console.log("sending url:",url,", postdata:",data);
        return task.context.sendHttpRequest(task,url,data,dataContentType);

      }).then(function(response){
        //if (task.verbose || true) console.log("GetRecordsResponse:\r\n",response);
        try {
          var searchResult = gs.Object.create(gs.base.SearchResult).init(task);
          self.handleGetRecordsResponse(task,response,searchResult);
          if (task.verbose) console.log("totalHits=",searchResult.totalHits);
          promise.resolve(searchResult);
        } catch(ex) {
          promise.reject(ex);
        }

      })["catch"](function(error){
        promise.reject(error);
      });
      return promise;
    }}

  });

}());
