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

  gs.provider.csw.GetRecordsParser = gs.Object.create(gs.Proto,{

    cswProvider: {writable: true, value: null},
    ids: {writable: true, value: null},
    modifiedFrom: {writable: true, value: null},
    modifiedTo: {writable: true, value: null},
    q: {writable: true, value: null},
    task: {writable: true, value: null},
    xmlInfo: {writable: true, value: null},

    parseBody: {writable:true,value:function(cswProvider,task) {
      this.ids = [];
      this.cswProvider = cswProvider;
      this.task = task;
      this.xmlInfo = null;
      var body = null, xmlInfo = null, ows, msg;
      var filterNode, sortByNode;

      if (task.request && task.request.body && typeof task.request.body === "string") {
        body = task.request.body.trim();
        if (body.length > 0) {
          try {
            xmlInfo = this.xmlInfo = task.context.newXmlInfo(task,body);
          } catch(ex) {
            msg = "Error parsing GetRecords xml:";
            if (ex && ex.message) msg += ex.message;
            ows = gs.Object.create(gs.provider.csw.OwsException);
            ows.put(task,ows.OWSCODE_NoApplicableCode,"",msg);
            //console.log("Error parsing GetRecords xml.");
            //console.error(ex);
          }
        }
      }
      if (!xmlInfo || !xmlInfo.root) return;

      var rootInfo = xmlInfo.getNodeInfo(xmlInfo.root);
      if (rootInfo.localName === "GetRecords") {
        if (rootInfo.namespaceURI === task.uris.URI_CSW2) {
          task.isCsw2 = true;
        }
      } else {
        return; // TODO throw an error?
      }

      xmlInfo.forEachAttribute(xmlInfo.root,function(attr){
        if (attr.localName === "startPosition") {
          var start = task.val.strToInt(attr.nodeText,-1);
          // TODO should this be >= 1
          if (start >= 1) {
            // TODO start = start - 1; is this correct indexOffset??
            cswProvider.addOverrideParameter(task,"start",""+start);
          }
        } else if (attr.localName === "maxRecords") {
          var maxRecords = attr.nodeText;
          if (maxRecords.toLowerCase !== "unlimited") {
            var num = task.val.strToInt(maxRecords,-1);
            if (num >= 0) {
              cswProvider.addOverrideParameter(task,"num",""+num);
            }
          }
        }
      });

      xmlInfo.forEachChild(xmlInfo.root,function(level2){
        if (level2.localName === "Query") {
          xmlInfo.forEachChild(level2.node,function(level3){
            if (level3.localName === "ElementSetName") {
              var elementSet = level3.nodeText;
              if (typeof elementSet === "string" && elementSet.trim().length > 0) {
                cswProvider.addOverrideParameter(task,"ElementSetName",elementSet.trim());
              }
            } else if (level3.localName === "Constraint") {
              xmlInfo.forEachChild(level3.node,function(level4){
                if (level4.localName === "Filter") {
                  filterNode = level4.node;
                }
              });
            } else if (level3.localName === "SortBy") {
              sortByNode = level3.node;
            }
          });
        }
      });

      try{
        if (filterNode) this._parseFilter(filterNode);
        if (sortByNode) this._parseSortBy(sortByNode);
      } catch(ex) {
        if (ex && ex.message && ex.message === "OWSException") {
          // ignore this, the exception has already been set
        } else {
          msg = "Error evaluating GetRecords request:";
          if (ex && ex.message) msg += ex.message;
          ows = gs.Object.create(gs.provider.csw.OwsException);
          ows.put(task,ows.OWSCODE_NoApplicableCode,"",msg);
          // TODO temporary ?
          //console.log("Error parsing OGC filter.");
          //console.error(ex);
        }
      }

    }},

    /* .............................................................................................. */

    _appendQ: {writable:true,value:function(qToAppend) {
      if (typeof qToAppend !== "string" || qToAppend.length === 0) return;
      if (typeof this.q !== "string") this.q = "";
      if (this.q.length > 0) this.q += " AND ";
      this.q += "("+qToAppend+")";
    }},

    _getPropertyLiteral: {writable:true,value:function(nodeInfo,ignoreValidation) {
      var v;
      this.xmlInfo.forEachChild(nodeInfo.node,function(childInfo){
        if (childInfo.localName === "Literal") {
          v = childInfo.nodeText;
          return "break";
        }
      });
      if (typeof v !== "string") v = "";
      v = v.trim(); // TODO ?
      if (v.length === 0 && !ignoreValidation) {
        var locator = "Literal";
        var msg = locator+" is required for "+nodeInfo.localName;
        ows = gs.Object.create(gs.provider.csw.OwsException);
        ows.put(this.task,ows.OWSCODE_InvalidParameterValue,locator,msg);
        throw new Error("OWSException");
      }
      //console.log("_getPropertyLiteral",nodeInfo.localName,v);
      return v;
    }},

    _getPropertyName: {writable:true,value:function(nodeInfo,ignoreValidation) {
      // dc:type - liveData, Format - content type, Subject - theme
      var queryables = ["anytext","id","title"];
      var anytextAliases = ["","anytext","format","subject"];
      if (!ignoreValidation) anytextAliases.push("dc:type");

      var name = null, lc = "", locator, ows, msg;

      this.xmlInfo.forEachChild(nodeInfo.node,function(childInfo){
        if (childInfo.localName === "PropertyName") {
          locator = "PropertyName";
          name = childInfo.nodeText;
          return "break";
        } else if (childInfo.localName === "ValueReference") {
          locator = "ValueReference";
          name = childInfo.nodeText;
          return "break";
        }
      });
      if (typeof name !== "string") name = "";
      name = name.trim();
      lc = name.toLowerCase();
      msg = locator+" is required for "+nodeInfo.localName;
      if (anytextAliases.indexOf(lc) !== -1) {
        return "";
      } else if (queryables.indexOf(lc) === -1) {
        if (lc.length > 0) {
          msg = name+" is not a supported queryable.";
        }
        if (!ignoreValidation) {
          ows = gs.Object.create(gs.provider.csw.OwsException);
          ows.put(this.task,ows.OWSCODE_InvalidParameterValue,locator,msg);
          throw new Error("OWSException");
        }
      }
      return name;
    }},

    _getSpatialFilter: {writable:true,value:function(nodeInfo) {
      var xmlInfo = this.xmlInfo;
      var a, box, coords, envelope, lowerCorner, upperCorner, msg, ows;

      var spatialRel = null;
      var lc = nodeInfo.localName.toLowerCase();
      if (lc === "intersects" || lc === "within" ||
          lc === "contains" || lc === "disjoint") {
        spatialRel = lc;
      }

      xmlInfo.forEachChild(nodeInfo.node,function(childInfo){
        if (childInfo.localName === "Box") {
          box = childInfo.node;
          xmlInfo.forEachChild(childInfo.node,function(childInfo2){
            if (childInfo2.localName === "coordinates") {
              coords = childInfo2.nodeText;
            }
          });
        } else if (childInfo.localName === "Envelope") {
          envelope = childInfo.node;
          xmlInfo.forEachChild(childInfo.node,function(childInfo2){
            if (childInfo2.localName === "lowerCorner") {
              lowerCorner = childInfo2.nodeText;
            } else if (childInfo2.localName === "upperCorner") {
              upperCorner = childInfo2.nodeText;
            }
          });
        }
      });
      if (box) {
        if (typeof coords === "string" && coords.length > 0) {
          this.cswProvider.addOverrideParameter(this.task,"bbox",coords);
          if (typeof spatialrel === "string" && spatialrel.length > 0) {
            this.cswProvider.addOverrideParameter(this.task,"spatialRel",spatialrel);
          }
        } else {
          msg = "gml:coordinates are required for gml:Box";
          ows = gs.Object.create(gs.provider.csw.OwsException);
          ows.put(this.task,ows.OWSCODE_InvalidParameterValue,"gml:Box",msg);
        }
      } else if (envelope) {
        if (typeof lowerCorner === "string" && lowerCorner.length > 0 &&
            typeof upperCorner === "string" && upperCorner.length > 0) {
          a = lowerCorner.split(" ");
          if (a.length === 2) {
            coords = a[0]+","+a[1];
            a = upperCorner.split(" ");
            if (a.length === 2) {
              coords += ","+a[0]+","+a[1];
              this.cswProvider.addOverrideParameter(this.task,"bbox",coords);
              if (typeof spatialrel === "string" && spatialrel.length > 0) {
                this.cswProvider.addOverrideParameter(this.task,"spatialRel",spatialrel);
              }
            }
          }
        } else {
          msg = "gml:lowerCorner and gml:upperCorner are required for gml:Envelope";
          ows = gs.Object.create(gs.provider.csw.OwsException);
          ows.put(this.task,ows.OWSCODE_InvalidParameterValue,"gml:Envelope",msg);
        }
      } else {
        msg = "gml:Box or gml:Envelope is required for "+nodeInfo.localName;
        ows = gs.Object.create(gs.provider.csw.OwsException);
        ows.put(this.task,ows.OWSCODE_InvalidParameterValue,nodeInfo.localName,msg);
        throw new Error("OWSException");
      }
    }},

    _parseFilter: {writable:true,value:function(filterNode) {
      if (!filterNode) return;
      this._parseFilterClause(filterNode);
      if (typeof this.q === "string" && this.q.length > 0) {
        this.cswProvider.addOverrideParameter(this.task,"q",this.q);
      }
      if (typeof this.ids && this.ids.length > 0) {
        this.cswProvider.addOverrideParameter(this.task,"id",this.ids);
      }
      if (typeof this.modifiedFrom === "string" && typeof this.modifiedTo === "string") {
        this.cswProvider.addOverrideParameter(this.task,"modified",this.modifiedFrom+"/"+this.modifiedTo);
      } else if (typeof this.modifiedFrom === "string") {
        this.cswProvider.addOverrideParameter(this.task,"modified",this.modifiedFrom+"/");
      } else if (typeof this.modifiedTo === "string") {
        this.cswProvider.addOverrideParameter(this.task,"modified","/"+this.modifiedTo);
      }
    }},

    _parseFilterClause: {writable:true,value:function(node) {
      var self = this, propName, literal, nodeInfo;
      this.xmlInfo.forEachChild(node,function(childInfo){
        if (childInfo.isElementNode) {
          var v, v2, ln = childInfo.localName;
          var peek = self._getPropertyName(childInfo,true);
          //console.log("ln",ln,"peek",peek);

          // modified
          if ((peek === "modified" || peek === "dct:modified") &&
              (ln === "PropertyIsGreaterThan" || ln === "PropertyIsGreaterThanOrEqualTo" ||
               ln === "PropertyIsLessThan" || ln === "PropertyIsLessThanOrEqualTo" ||
               ln === "PropertyIsBetween")) {
            v = v2 = null;
            if (ln === "PropertyIsGreaterThan" || ln === "PropertyIsGreaterThanOrEqualTo") {
              v = self._getPropertyLiteral(childInfo);
            } else if (ln === "PropertyIsLessThan" || ln === "PropertyIsLessThanOrEqualTo") {
              v2 = self._getPropertyLiteral(childInfo);
            } else if (ln === "PropertyIsBetween") {
              self.xmlInfo.forEachChild(childInfo.node,function(childInfo2){
                if (childInfo2.localName === "LowerBoundary") {
                  v = childInfo2.nodeText;
                  return "break";
                } else if (childInfo2.localName === "UpperBoundary") {
                  v2 = childInfo2.nodeText;
                  return "break";
                }
              });
            }
            if (typeof v === "string" && v.trim().length > 0) {
              self.modifiedFrom = v.trim();
            }
            if (typeof v2 === "string" && v2.trim().length > 0) {
              self.modifiedTo = v2.trim();
            }
            ln = "ignore";
          }

          // liveData
          if ((peek === "dc:type") && (ln === "PropertyIsEqualTo")) {
            v = self._getPropertyLiteral(childInfo,true);
            if (typeof v === "string" && v.trim().toLowerCase() === "livedata") {
              self.cswProvider.addOverrideParameter(self.task,"type","liveData");
              ln = "ignore";
            }
          }

          if (ln === "ignore") {

          // logical clauses
          } else if (ln === "And") {
            self._parseFilterClause(childInfo.node);
          } else if (ln === "Or") {
            self._throwUnsupportedOperator(childInfo);
          } else if (ln === "Not") {
            self._throwUnsupportedOperator(childInfo);

          // property clauses
          } else if (ln === "PropertyIsBetween") {
            self._throwUnsupportedOperator(childInfo);
          } else if (ln === "PropertyIsEqualTo") {
            propName = self._getPropertyName(childInfo);
            v = self._getPropertyLiteral(childInfo);
            if (propName.toLowerCase() === "id") {
              self.ids.push(v);
            } else {
              if (propName.length > 0 && propName.toLowerCase() !== "anytext") {
                v = propName+":("+v+")";
              }
              self._appendQ(v);
            }
          } else if (ln === "PropertyIsGreaterThan") {
            self._throwUnsupportedOperator(childInfo);
          } else if (ln === "PropertyIsGreaterThanOrEqualTo") {
            self._throwUnsupportedOperator(childInfo);
          } else if (ln === "PropertyIsLessThan") {
            self._throwUnsupportedOperator(childInfo);
          } else if (ln === "PropertyIsLessThanOrEqualTo") {
            self._throwUnsupportedOperator(childInfo);
          } else if (ln === "PropertyIsLike") {
            propName = self._getPropertyName(childInfo);
            v = self._getPropertyLiteral(childInfo);
            if (propName.length > 0 && propName.toLowerCase() !== "anytext") {
              v = propName+":("+v+")";
            }
            self._appendQ(v);
          } else if (ln === "PropertyIsNotEqualTo") {
            self._throwUnsupportedOperator(childInfo);
          } else if (ln === "PropertyIsNull") {
            self._throwUnsupportedOperator(childInfo);


          // spatial clauses
          } else if (ln === "BBOX") {
            self._getSpatialFilter(childInfo);
          } else if (ln === "Beyond") {
            self._throwUnsupportedOperator(childInfo);
          } else if (ln === "Contains") {
            self._throwUnsupportedOperator(childInfo);
          } else if (ln === "Crosses") {
            self._throwUnsupportedOperator(childInfo);
          } else if (ln === "Disjoint") {
            self._throwUnsupportedOperator(childInfo);
          } else if (ln === "DWithin") {
            self._throwUnsupportedOperator(childInfo);
          } else if (ln === "Equals") {
            self._throwUnsupportedOperator(childInfo);
          } else if (ln === "Intersects") {
            self._getSpatialFilter(childInfo);
          } else if (ln === "Overlaps") {
            self._throwUnsupportedOperator(childInfo);
          } else if (ln === "Touches") {
            self._throwUnsupportedOperator(childInfo);
          } else if (ln === "Within") {
            self._getSpatialFilter(childInfo);

          } else {
            self._throwUnsupportedOperator(childInfo);
          }
        }
      });
    }},

    _parseSortBy: {writable:true,value:function(sortByNode) {
      var a = [], sortField, sortOrder, xmlInfo = this.xmlInfo;
      xmlInfo.forEachChild(sortByNode,function(childInfo){
        if (childInfo.localName === "SortProperty") {
          sortField = sortOrder = null;
          xmlInfo.forEachChild(childInfo.node,function(childInfo2){
            if (childInfo2.localName === "PropertyName") {
              sortField = childInfo2.nodeText;
            } else if (childInfo2.localName === "SortOrder") {
              sortOrder = childInfo2.nodeText;
            }
          });
          if (typeof sortField == "string" && sortField.trim().length > 0) {
            sortField = sortField.trim();
            if (typeof sortOrder === "string" &&
               (sortOrder.trim().toLowerCase() === "asc" ||
                sortOrder.trim().toLowerCase() === "desc")) {
              sortField += ":"+sortOrder.trim().toLowerCase();
            }
            a.push(sortField);
          }
        }
      });
      if (a.length > 0) {
        this.cswProvider.addOverrideParameter(this.task,"sort",a);
      }
    }},

    _throwUnsupportedOperator: {writable:true,value:function(nodeInfo) {
      var locator = nodeInfo.localName;
      var msg = "Operator "+nodeInfo.nodeName+" is not supported.";
      var ows = gs.Object.create(gs.provider.csw.OwsException);
      ows.put(this.task,ows.OWSCODE_InvalidParameterValue,locator,msg);
      throw new Error("OWSException");
    }}

  });

}());
