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

  gs.target.csw.CswSchema = gs.Object.create(gs.target.TargetSchema, {

    qClauseName: {writable: true, value: "PropertyIsLike"},

    qPropertyName: {writable: true, value: "AnyText"},
    idPropertyName: {writable: true, value: "Id"},
    liveDataPropertyName: {writable: true, value: null},
    modifiedPropertyName: {writable: true, value: "Modified"},
    spatialPropertyName: {writable: true, value: "Geometry"},
    timePeriodPropertyName: {writable: true, value: "TimePeriod"},

    schemaType: {writable: true, value: "CSW"},

    sortables: {writable: true, value: {
      "title": "title",
      "date": "modified",
      "modified": "modified"
    }},

    handleRecordToAtomEntry: {writable:true,value:function(task,xmlInfo,recordInfo) {
      var ln, ns, hasText, dctype, scheme, text, urlInfo;
      var x, y, xy, xmin, ymin, xmax, ymax;
      var links = [];
      var entry = gs.Object.create(gs.atom.Entry);
      xmlInfo.forEachChild(recordInfo.node,function(childInfo){
        if (childInfo.isElementNode) {
          //console.log(childInfo.localName,childInfo.namespaceURI);
          ln = childInfo.localName;
          ns = childInfo.namespaceURI;
          text = childInfo.nodeText;
          hasText = (typeof text === "string" && text.length > 0);

          if (ns === task.uris.URI_DC) {
            //console.log(childInfo.nodeInfo.localName,childInfo.nodeInfo.namespaceURI);
            if (ln === "identifier") {
              scheme = xmlInfo.getAttributeValue(childInfo.node,"scheme");
              //console.log(ln,text,"scheme =",scheme);
              if (scheme === "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:DocID") {
                entry.id = text;
              } else if (scheme === "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:FileID") {
                if (typeof entry.id !== "string") {
                  entry.id = text;
                }
              } else {
                entry.id = text;
              }
            } else if (ln === "title") {
              entry.title = text;
            } else if (ln === "type" || ln === "subject" || ln === "format") {
              // dc:type scheme can be urn:x-esri:specification:ServiceType:ArcIMS:Metadata:ContentType
              if (hasText) {
                scheme = xmlInfo.getAttributeValue(childInfo.node,"scheme");
                if (typeof scheme !== "string" || scheme.length === 0) {
                  scheme = "dc:"+ln;
                }
                if (!entry.category) entry.category = [];
                else if (!Array.isArray(entry.category)) entry.category = [entry.category];
                entry.category.push(gs.Object.create(gs.atom.Category).init({
                  scheme: scheme,
                  term: text
                }));
              }
            } else if (ln === "creator") {
              if (hasText) {
                if (!entry.author) entry.author = [];
                else if (!Array.isArray(entry.author)) entry.author = [entry.author];
                entry.author.push(gs.Object.create(gs.atom.Person).init({
                  tag: "author",
                  name: text
                }));
              }
            } else if (ln === "contributor") {
              if (hasText) {
                if (!entry.contributor) entry.contributor = [];
                else if (!Array.isArray(entry.contributor)) entry.contributor = [entry.contributor];
                entry.contributor.push(gs.Object.create(gs.atom.Person).init({
                  tag: "contributor",
                  name: text
                }));
              }
            } else if (ln === "rights") {
              if (hasText) {
                if (!entry.rights) entry.rights = [];
                else if (!Array.isArray(entry.rights)) entry.rights = [entry.rights];
                entry.rights.push(gs.Object.create(gs.atom.Text).init({
                  type: "text", // TODO ?
                  value: text
                }));
              }
            } else if (ln === "relation") {
              // TODO ?
            }
          } else if (ns === task.uris.URI_DCT) {
            if (ln === "abstract") {
              if (hasText) {
                entry.summary = gs.Object.create(gs.atom.Text).init({
                  type: "text", // TODO ?
                  value: text
                });
              }
            } else if (ln === "created") {
              if (hasText) {
                entry.published = text;
              }
            } else if (ln === "modified") {
              if (hasText) {
                entry.updated = text;
              }
            } else if (ln === "references") {
              if (hasText) {
                dctype = null;
                scheme = xmlInfo.getAttributeValue(childInfo.node,"scheme");
                //console.log(ln,text,"scheme =",scheme);
                urlInfo = task.val.guessUrlType(text);
                if (urlInfo && urlInfo.type) {
                  dctype = urlInfo.type;
                  //console.log(dctype,text);
                }
                if (scheme === "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server") {
                  links.push(gs.Object.create(gs.atom.Link).init({
                    rel: "related",
                    dctype: dctype,
                    href: text
                  }));
                } else if (scheme === "alternate.html") {
                  links.push(gs.Object.create(gs.atom.Link).init({
                    rel: "alternate",
                    type: "text/html",
                    href: text
                  }));
                } else if (scheme === "alternate.json") {
                  links.push(gs.Object.create(gs.atom.Link).init({
                    rel: "alternate",
                    type: "application/json",
                    href: text
                  }));
                } else if (scheme === "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Document" ||
                           scheme === "alternate.xml") {
                  links.push(gs.Object.create(gs.atom.Link).init({
                    rel: "alternate",
                    type: "application/xml",
                    href: text
                  }));
                } else if (scheme === "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Thumbnail" ||
                           scheme === "icon") {
                  links.push(gs.Object.create(gs.atom.Link).init({
                    rel: "icon",
                    href: text
                  }));
                } else {
                  if (typeof dctype === "string" && dctype.length > 0) {
                    links.push(gs.Object.create(gs.atom.Link).init({
                      rel: "related",
                      dctype: dctype,
                      href: text
                    }));
                  }
                }
              }
            } else if (ln === "alternative") {
              // TODO ?
            } else if (ln === "spatial") {
              // TODO ?
            }
          } else if (ln === "BoundingBox" || "WGS84BoundingBox") {
            // WGS84BoundingBox: LowerCorner x space y , UpperCorner x space y
            xmin = ymin = xmax = ymax = null;
            xmlInfo.forEachChild(childInfo.node,function(childInfo2){
              if (childInfo2.localName === "LowerCorner" || childInfo2.localName === "UpperCorner") {
                if (childInfo2.nodeText) {
                  try {
                    xy = childInfo2.nodeText.split(" ");
                    if (xy.length === 2) {
                      if (childInfo2.localName === "LowerCorner") {
                        xmin = task.val.strToNum(xy[0],null);
                        ymin = task.val.strToNum(xy[1],null);
                      } else {
                        xmax = task.val.strToNum(xy[0],null);
                        ymax = task.val.strToNum(xy[1],null);
                      }
                    }
                  } catch(excoords) {
                  }
                }
              }
            });
            if (typeof xmin === "number" && typeof ymin === "number" &&
                typeof xmax === "number" && typeof ymax === "number") {
              entry.bbox = gs.Object.create(gs.atom.BBox).init({
                xmin: xmin,
                ymin: ymin,
                xmax: xmax,
                ymax: ymax
              });
            }
          }
        }
      });

      if(links.length > 0) entry.link = links;
      return entry;
    }},

    itemToAtomEntry: {writable:true,value:function(task,item) {
      return this.handleRecordToAtomEntry(task,item.xmlInfo,item.recordInfo);
    }}

  });

}());
