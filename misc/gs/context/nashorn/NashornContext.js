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

  if (typeof console === "undefined") {
    console = {};
    console.debug = print;
    console.log = print;
    console.warn = print;
    console.error = print;
  }

  gs._jvmTypes = {
    CharArray: Java.type("char[]")
  };

  /* ============================================================================================== */

  gs.context.nashorn.NashornContext = gs.Object.create(gs.context.Context,{

    isNashorn: {writable: true, value: true},

    indentXml: {writable:true,value:function(task,xml) {
      return gs.context.nashornUtil.indentXml(xml);
    }},

    newCounter: {writable:true,value:function() {
      return new Packages.java.util.concurrent.atomic.AtomicInteger();
    }},

    newStringBuilder: {writable:true,value:function() {
      return gs.Object.create(gs.context.nashorn.StringBuilder).init();
    }},

    newXmlInfo: {writable:true,value:function(task,xmlString) {
      if (typeof xmlString === "string") xmlString = xmlString.trim();
      var source = new org.xml.sax.InputSource(new java.io.StringReader(xmlString));
      var factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setExpandEntityReferences(false);
      factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing",true);
      //factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
      var builder = factory.newDocumentBuilder();
      var dom = builder.parse(source);
      var root = dom.getDocumentElement();
      var xmlInfo = gs.Object.create(gs.context.nashorn.XmlInfo).mixin({
        dom: dom,
        root: root
      });
      return xmlInfo;
    }},

    readResourceFile: {writable:true,value:function(path,charset) {
      return gs.context.nashornUtil.readResourceFile(path,charset);
    }},

    removeAllButFilter: {writable:true,value:function(xml) {
      return gs.context.nashornUtil.removeAllButFilter(xml);
    }},

    sendHttpRequest: {writable:true,value:function(task,url,data,dataContentType,options) {
      var result, promise = this.newPromise("sendHttpRequest");
      try {
        if (task.async) {
          new java.lang.Thread(function() {
            try {
              if (task.verbose) console.log("NashornContext sendHttpRequest.async");
              result = gs.context.nashornUtil.sendHttpRequest(url,data,dataContentType,options);
              if (task.verbose) console.log("NashornContext sendHttpRequest.async resolved",url);
              //print(typeof result, result.length);
              promise.resolve(result);
            } catch(err2) {
              promise.reject(err2);
            }
          }).start();
        } else {
          result = gs.context.nashornUtil.sendHttpRequest(url,data,dataContentType,options);
          promise.resolve(result);
        }
      } catch (err) {
        promise.reject(err);
      }
      return promise;
    }}

  });

  /* ============================================================================================== */

  gs.context.nashornUtil = {

    indentXml: function(xml) {
      // TODO removeBOM ??
      var header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
      if (xml !== null) xml = xml.trim();
      if (xml === null || xml.length === 0) throw new Error("Empty XML.");
      var source = new javax.xml.transform.stream.StreamSource(new java.io.StringReader(xml));
      var streamResult = new javax.xml.transform.stream.StreamResult(new java.io.StringWriter());
      this.transform(source,streamResult,true);
      var v = streamResult.getWriter().toString();
      if (v !== null) {
        v = v.trim();
        if (v.startsWith(header+"<")) v = v.replace(header,header+"\r\n");
        if (v.length === 0) v = null;
      }
      return v;
    },

    newXPathEvaluator: function(nsmap) {
      var key;
      var createFunc = function(value) {
        if (typeof value === "function") return value;
        return function() {return value;};
      };
      var iface = function(map) {
        var ifaceImpl = {};
        for (key in map) {
          ifaceImpl[key] = createFunc(map[key]);
        }
        return ifaceImpl;
      };

      //var nsContext = new gs._jvmTypes.NamespaceContext(iface({
      var nsContext = new javax.xml.namespace.NamespaceContext(iface({
        getNamespaceURI: function(prefix) {
          //console.log("getNamespaceURI",prefix);
          if (nsmap) return nsmap[prefix];
        },
        getPrefix: function(uri) {
          //console.log("getPrefix",uri);
          if (nsmap) {
            for (key in nsmap) {
              if (nsmap[key] === uri) return key;
            }
          }
          return null;
        },
        getPrefixes: function(uri) {
          //console.log("getPrefixes",uri);
          return null;
        }
      }));

      var xpath = javax.xml.xpath.XPathFactory.newInstance().newXPath();
      xpath.setNamespaceContext(nsContext);
      var XPATH_NODE = javax.xml.xpath.XPathConstants.NODE;
      var XPATH_NODESET = javax.xml.xpath.XPathConstants.NODESET;
      var XPATH_STRING = javax.xml.xpath.XPathConstants.STRING;
      var evaluator = {
        getNode: function(contextNode,xpathExpression) {
          return xpath.evaluate(xpathExpression,contextNode,XPATH_NODE);
        },
        getNodes: function(contextNode,xpathExpression) {
          var nl = xpath.evaluate(xpathExpression,contextNode,XPATH_NODESET);
          return this._nodeListToArray(nl);
        },
        getString: function(contextNode,xpathExpression) {
          return xpath.evaluate(xpathExpression,contextNode,XPATH_STRING);
        }
      };
      return evaluator;
    },

    readResourceFile: function(path,charset) {
      if (charset === null || charset.length == 0) charset = "UTF-8";
      var url = java.lang.Thread.currentThread().getContextClassLoader().getResource(path);
      var content = new java.lang.String(java.nio.file.Files.readAllBytes(
        java.nio.file.Paths.get(url.toURI())),charset);
      return content;
    },

    removeAllButFilter: function(xml) {
      // TODO removeBOM ??
      try {
        var inputSource = new org.xml.sax.InputSource(new java.io.StringReader(xml));
        var factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setExpandEntityReferences(false);
        factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing",true);
        //factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        var builder = factory.newDocumentBuilder();
        var dom = builder.parse(inputSource);

        var root = dom.getDocumentElement();
        var nl = root.getChildNodes();
        for (var i=0;i<nl.getLength();i++) {
          var nd = nl.item(i);
          if (nd.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
            if (nd.getLocalName() !== "Filter_Capabilities") {
              root.removeChild(nd);
            }
          } else if (nd.getNodeType() == org.w3c.dom.Node.COMMENT_NODE) {
            root.removeChild(nd);
          } else if (nd.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
          }
        }

        var source = new javax.xml.transform.dom.DOMSource(dom);
        var streamResult = new javax.xml.transform.stream.StreamResult(new java.io.StringWriter());
        this.transform(source,streamResult,true);
        var result = streamResult.getWriter().toString();
        return result;
      } catch(e) {
        return xml;
      }
    },

    sendHttpRequest: function(url, data, dataContentType, options) {
      var result = null;
      var br = null, br2 = null, wr = null;
      var sw = new java.io.StringWriter();
      var sw2 = new java.io.StringWriter();
      var con = null, buffer, nRead;
      try {
        var u = new java.net.URL(url);
        //print(u);
        java.net.HttpURLConnection.setFollowRedirects(true);
        con = u.openConnection();
        con.setInstanceFollowRedirects(true);

        if (options && options.basicCredentials &&
            typeof options.basicCredentials.username === "string" &&
            options.basicCredentials.username.length > 0 &&
            typeof options.basicCredentials.password === "string" &&
            options.basicCredentials.password.length > 0) {
          var cred = options.basicCredentials.username+":"+options.basicCredentials.password;
          cred = new java.lang.String(java.util.Base64.getEncoder().encode(cred.getBytes("UTF-8")),"UTF-8");
          con.setRequestProperty( "Authorization","Basic "+cred);
        }

        if (typeof data === "string" && data.length > 0) {
          con.setDoOutput(true);
          con.setRequestMethod("POST");
          var postData = data.getBytes("UTF-8");
          if (typeof dataContentType === "string" && dataContentType.length > 0) {
            con.setRequestProperty( "Content-Type",dataContentType);
          }
          con.setRequestProperty("charset","UTF-8");
          con.setRequestProperty("Content-Length",""+postData.length);
          wr = new java.io.DataOutputStream(con.getOutputStream());
          wr.write(postData);
        }
        var charset = "UTF-8";
        var contentType = con.getContentType();
        if (contentType !== null) {
          var a = contentType.split(";");
          a.some(function(v){
            v = v.trim();
            if (v.toLowerCase().startsWith("charset=")) {
              var cs = v.substring("charset=".length).trim();
              if (cs.length() > 0) {
                charset = cs;
                return true;
              }
            }
          });
        }
        //print("contentType="+contentType+" ... charset="+charset);
        br = new java.io.BufferedReader(new java.io.InputStreamReader(con.getInputStream(),charset));
        nRead = 0;
        buffer = new gs._jvmTypes.CharArray(4096);
        while ((nRead = br.read(buffer,0,4096)) >= 0) {
          sw.write(buffer,0,nRead); // TODO comment out this line and Invalid JSON: <json>:1:0 Expected json literal but found eof
        }
        result = sw.toString();
        //console.log("result",result);
      } catch(e) {
        var msg;
        try {
          if (con) {
            var estrm = con.getErrorStream();
            if (estrm) {
              br2 = new java.io.BufferedReader(new java.io.InputStreamReader(con.getErrorStream()));
              nRead = 0;
              buffer = new gs._jvmTypes.CharArray(4096);
              while ((nRead = br2.read(buffer,0,4096)) >= 0) {
                sw2.write(buffer,0,nRead);
              }
              msg = sw2.toString();              
            }
          }
        } catch(ex2) {
          console.log("Geoportal-Search: problem getting HTTP response error:");
          console.error(ex);
        }
        //if (msg) print("Error msg:",msg);
        if (msg) {
          throw new Error(msg);
        } else {
          print(e); // TODO printStackTrace
          throw new Error(e.toString());
        }
      } finally{
        try {if (wr !== null) wr.close();} catch(ef) {print(ef);}
        try {if (br !== null) br.close();} catch(ef) {print(ef);}
        try {if (br2 !== null) br2.close();} catch(ef) {print(ef);}
      }
      return result;
    },

    transform: function(source,result,indent) {
      var factory = javax.xml.transform.TransformerFactory.newInstance();
      factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD,"");
      factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing",true);
      //factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
      var transformer = factory.newTransformer();
      transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING,"UTF-8");
      transformer.setOutputProperty(javax.xml.transform.OutputKeys.METHOD,"xml");
      if (indent) {
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT,"yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
      }
      transformer.transform(source,result);
    }

  };

  /* ============================================================================================== */

  gs.context.nashorn.XmlInfo = gs.Object.create(gs.base.XmlInfo,{

    NODETYPE_ATTRIBUTE: {writable: true, value: org.w3c.dom.Node.ATTRIBUTE_NODE},
    NODETYPE_ELEMENT: {writable: true, value: org.w3c.dom.Node.ELEMENT_NODE},
    NODETYPE_TEXT: {writable: true, value: org.w3c.dom.Node.TEXT_NODE},

    dom: {writable: true, value: null},
    root: {writable: true, value: null},

    forEachAttribute: {writable:true,value:function(node,callback) {
      var r, self = this;
      this.getAttributes(node).forEach(function(child){
        if (callback) {
          r = callback(self.getNodeInfo(child,true));
          if (r === "break") return;
        }
      });
    }},

    forEachChild: {writable:true,value:function(node,callback) {
      var r, self = this;
      this.getChildren(node).forEach(function(child){
        if (callback) {
          r = callback(self.getNodeInfo(child,true));
          if (r === "break") return;
        }
      });
    }},

    getAttributes: {writable:true,value:function(node) {
      if (node) {
        return this._nodeListToArray(node.getAttributes());
      }
      return [];
    }},

    getAttributeValue: {writable:true,value:function(node,localName,namespaceURI) {
      var value = null;
      var ns = (typeof namespaceURI === "string" && namespaceURI.length > 0);
      this.forEachAttribute(node,function(info){
        if (localName === info.localName) {
          if (ns) {
            if (namespaceURI === info.namespaceURI) {
              value = info.nodeText;
              return "break";
            }
          } else {
            value = info.nodeText;
            return "break";
            /*
            if (typeof info.namespaceURI !== "string" ||
                info.namespaceURI.length === 0) {
              value = info.nodeText;
              return "break";
            }
            */
          }
        }
      });
      return value;
    }},

    getChildren: {writable:true,value:function(node) {
      if (node) {
        return this._nodeListToArray(node.getChildNodes());
      }
      return [];
    }},

    /*
     * nodeInfo:
     * {
     *   node: ,
     *   nodeText: , (if requested)
     *   nodeName: ,
     *   localName: ,
     *   namespaceURI: ,
     *   isAttributeNode: ,
     *   isElementNode: ,
     *   isTextNode:
     * }
     */
    getNodeInfo: {writable:true,value:function(node,withText) {
      if (!node) return null;
      var info = {
        node: node,
        nodeText: this.getNodeText(node),
        nodeName: node.getNodeName(),
        localName: node.getLocalName(),
        namespaceURI: node.getNamespaceURI(),
        isAttributeNode: node.getNodeType() === this.NODETYPE_ATTRIBUTE,
        isElementNode: node.getNodeType() === this.NODETYPE_ELEMENT,
        isTextNode: node.getNodeType() === this.NODETYPE_TEXT
      };
      if (withText) info.nodeText = this.getNodeText(node);
      return info;
    }},

    getNodeText: {writable:true,value:function(node) {
      var v;
      if (node) {
        if (node.getNodeType() === 1) {
          v = node.getTextContent();
          if (typeof v === "string") v = v.trim();
        } else {
          v = node.getNodeValue();
        }
        if (typeof v === "string") {
          return v;
        }
      }
      return null;
    }},

    _nodeListToArray: {writable:true,value:function(nl) {
      var i, a = [];
      if (nl) {
        for (i = 0; i < nl.getLength(); i++) {
          a.push(nl.item(i));
        }
      }
      return a;
    }}

  });

  /* ============================================================================================== */

}());
