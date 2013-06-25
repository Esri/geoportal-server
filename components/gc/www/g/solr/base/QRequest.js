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
define(["dojo/_base/declare","dojo/_base/lang"], 
function(declare,lang) {
  
  var oThisClass = declare("g.solr.base.QRequest", null, {
    
    urlParams: null,
    
    constructor: function() {
      this.urlParams = {};
    },
    
    pushFDotPropertiesToUrl: function(oFDotProperties,sFacetFieldName) {
      var sKey, sProp = null, oProp;
      if (sFacetFieldName && oFDotProperties) {
        for (sProp in oFDotProperties) {
          sKey = "f."+sFacetFieldName+"."+sProp;
          oProp = oFDotProperties[sProp];
          // TODO: is this needed?
          if ((typeof(oProp) == "string") && (oProp != null)) {
            if (oProp.length == 0) oProp = null;
          }
          if ((typeof(oProp) != "undefined") && (oProp != null)) {
            this.pushUrlParameter(sKey,oFDotProperties[sProp]);
          }
        }
      }
    },
    
    pushPropertiesToUrl: function(oProperties) {
      var sProp = null;
      if (oProperties) {
        for (sProp in oProperties) {
           this.pushUrlParameter(sProp,oProperties[sProp]);
        }
      }
    },
    
    pushQClauses: function(qClauses) {
      if (!qClauses) return;
      var qc, i, n = qClauses.length; 
      for (i=0;i<n;i++) {
        qc = qClauses[i];
        this.pushUrlParameter(qc.urlParameterName,qc.urlParameterValue);
      }
    },
    
    // for multi-valued URL parameters
    pushUrlParameter: function(sKey,oValue) {
      var i, n, p = this.urlParams[sKey];
      if (!lang.isArray(p)) {
        p = new Array();
        this.urlParams[sKey] = p;
      }
      if (lang.isArray(oValue)) {
        n = oValue.length;
        for (i=0;i<n;i++) {
          p.push(oValue[i]);
        }
      } else {
        p.push(oValue);
      }
    },
    
    // for single-valued URL parameters
    putUrlParameter: function(sKey,oValue) {
      this.urlParams[sKey] = oValue;
    }
  
  });
  
  return oThisClass;
});