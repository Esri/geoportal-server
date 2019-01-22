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

  gs.base.Val = gs.Object.create(gs.Proto,{

    NL: {writable: true, value: "\r\n"},
    XML_HEADER: {writable: true, value: "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"},

    chkStr: {writable:true,value:function(v) {
      if (typeof v === "undefined" || v === null) return null;
      return ""+v;
    }},

    chkStrArray: {writable:true,value:function(v) {
      if (typeof v === "undefined" || v === null) return null;
      else if (typeof v === "string") return [v];
      else if (typeof v.push === "function") return v;
      return null;
    }},

    chkObjArray: {writable:true,value:function(v) {
      if (typeof v === "undefined" || v === null) return null;
      else if (typeof v === "object" && typeof v.push === "undefined") return [v];
      else if (typeof v === "object" && typeof v.push === "function") return v;
      return null;
    }},

    endsWith: {writable:true,value:function(v,sfx) {
      if (typeof v !== "string") return null;
      return (v.indexOf(sfx,(v.length - sfx.length)) !== -1);
    }},

    escXml: {writable:true,value:function(s) {
      if (s === null) return null;
      if (s.length === 0) return s;
      var i, c, sb = "";
      for (i = 0; i < s.length; i++) {
        c = s.charAt(i);
        if (c === "&") {
          sb += "&amp;";
        } else if (c === "<") {
          sb += "&lt;";
        } else if (c === ">") {
          sb += "&gt;";
        } else if (c === "\'") {
          sb += "&apos;";
        } else if (c === "\"") {
          sb += "&quot;";
        } else {
          sb += c;
        }
      }
      return sb;
    }},

    guessUrlType: {writable:true,value:function(url) {
      var endsWith = function(v,sfx) {return (v.indexOf(sfx,(v.length-sfx.length)) !== -1);};

      var arcgisTypes = ["FeatureServer","GeocodeServer","GeoDataServer","GeometryServer",
                         "GlobeServer","GPServer","ImageServer","MapServer","MobileServer",
                         "NAServer","SceneServer","SchematicsServer","StreamServer",
                         "VectorTileServer"];
      var ogcTypes = ["WMS","WFS","WCS","WMTS","WPS","SOS","CSW"];
      var dataTypes = ["dbf","csv","gpx","pdf","shp","txt","xls","xlsx","zip"];
      var kml = "KML";
      var ims = "IMS";

      var i, v, v2, lc, lc2, n, n2, type = null;
      var isHttp = (typeof url === "string" &&
        (url.indexOf("http://") === 0 || url.indexOf("https://") === 0));
      var isFtp = (typeof url === "string" &&
        (url.indexOf("ftp://") === 0 || url.indexOf("ftps://") === 0));
      if (isHttp) {
        lc = url.toLowerCase();
        if (lc.indexOf("service=") > 0) {
          for (i=0;i<ogcTypes.length;i++) {
            v = "service="+ogcTypes[i].toLowerCase();
            if (lc.indexOf("?"+v) > 0 || lc.indexOf("&"+v) > 0) {
              type = ogcTypes[i];
              break;
            }
          }
        } else if (lc.indexOf("/rest/services/") > 0 ||
                   lc.indexOf("/arcgis/services/") > 0) {
          for (i=0;i<arcgisTypes.length;i++) {
            lc2 = arcgisTypes[i].toLowerCase();
            if (endsWith(lc,"/"+lc2)) {
              type = arcgisTypes[i];
              break;
            } else if (lc2 === "mapserver" || lc2 === "featureserver"){
              v = "/"+lc2+"/";
              n = lc.indexOf(v);
              if (n > 0) {
                v2 = lc.substring(n+v.length).trim();
                try {
                  if (v2.length > 0) {
                    n2 = Number(v2);
                    if (!isNaN(n2) && typeof n2 === "number" && isFinite(n2)) {
                      type = arcgisTypes[i];
                      break;
                    }
                  }
                } catch(exn) {}
              }
            }
          }
        }
        if (type === null) {
          if (endsWith(lc,".kml") || endsWith(lc,".kmz") ||
              lc.indexOf("?f=kml") > 0 || lc.indexOf("&f=kml") > 0 ||
              lc.indexOf("?f=kmz") > 0 || lc.indexOf("&f=kmz") > 0) {
            type = kml;
          }
        }
        if (type === null) {
          if (lc.indexOf("com.esri.esrimap.esrimap") >= 0) {
            type = ims;
          } else if (lc.indexOf("com.esri.wms.esrimap") >= 0) {
            type = "WMS";
          }
        }
        if (type === null) {
          for (i=0;i<dataTypes.length;i++) {
            if (endsWith(lc,"."+dataTypes[i])) {
              type = dataTypes[i];
              break;
            }
          }
        }
      }
      if (type !== null && (isHttp || isFtp)) {
        return {type: type, url: url};
      }
    }},

    hasAnyProperty: {writable:true,value:function(obj) {
      if (typeof obj === "object" && obj !== null) {
        for (var k in obj) {
          if (obj.hasOwnProperty(k)) {
            return true;
          }
        }
      }
      return false;
    }},

    millisToIso8601: {writable:true,value:function(millis) {
      if (typeof millis === "undefined") return null;
      if (millis === null) return null;
      var pad = function(number) {
        if (number < 10) {
          return '0' + number;
        }
        return number;
      };
      var date = new Date(millis);
      var v = date.getUTCFullYear() +
        '-' + pad(date.getUTCMonth() + 1) +
        '-' + pad(date.getUTCDate()) +
        'T' + pad(date.getUTCHours()) +
        ':' + pad(date.getUTCMinutes()) +
        ':' + pad(date.getUTCSeconds()) +
        '.' + (date.getUTCMilliseconds() / 1000).toFixed(3).slice(2, 5) +
        'Z';
      return v;
    }},

    nowAsString: {writable:true,value:function() {
      return this.millisToIso8601(Date.now());
    }},

    strToInt: {writable:true,value:function(v,defaultValue) {
      try {
        if (typeof v !== "string") v = this.chkStr(v);
        if (typeof v === "string" && v.trim().length > 0) {
          v = parseInt(v.trim(),10);
          if (typeof v === "number" && !isNaN(v) && isFinite(v)) {
            return v;
          }
        }
      } catch(ex) {}
      return defaultValue;
    }},

    strToNum: {writable:true,value:function(v,defaultValue) {
      try {
        if (typeof v !== "string") v = this.chkStr(v);
        if (typeof v === "string" && v.trim().length > 0) {
          v = Number(v.trim());
          if (typeof v === "number" && !isNaN(v) && isFinite(v)) {
            return v;
          }
        }
      } catch(ex) {}
      return defaultValue;
    }},

    trim: {writable:true,value:function(v) {
      if (typeof v === "string") return v.trim();
      return v;
    }}

  });

}());
