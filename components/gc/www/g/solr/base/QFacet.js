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
  
  var oThisClass = declare("g.solr.base.QFacet", null, {
    
    aliasKey: null,
    field: null,
    isRange: false,
    uniqueId: null,
    useStaticExclusion: false,
    
    // fdot properties
    limit: null,
    missing: null,
    mincount: null,
    prefix: null,
    sort: null,
    rangeStart: null,
    rangeEnd: null,
    rangeGap: null,

    constructor: function(args) {
      lang.mixin(this,args);
      this.uniqueId = oThisClass._staticCounter++;
    },
    
    appendQParameters: function(qRequest) {
      
      var appendFDot = function(sName,sProp,oVal) {
        var sKey = "f."+sName+"."+sProp;
        qRequest.pushUrlParameter(sKey,oVal);
      };
      
      var checkVal = function(v) {
        if ((typeof(v) == "undefined") || (v == null)) {
          return null;
        }
        if ((typeof(v) == "string") && (v.length == 0)) {
          return null;
        }
        return v;
      };
      
      var v, sName;
      var sField = this.field, sAlias = this.aliasKey, bRange = this.isRange;
      if (!sField || ((sField.length == 0))) return
      
      if (this.useStaticExclusion) {
        sName = sField;
        if (sAlias && (sAlias.length > 0)) sName = sAlias;
        sName = "{!ex=e"+this.uniqueId+"}"+sName;
        qRequest.pushUrlParameter("facet.field",sName);
      } else {
        sName = sField;
        if (sAlias && (sAlias.length > 0)) sName = "{!key="+sAlias+"}"+sName;
        if (bRange) {
          qRequest.pushUrlParameter("facet.range",sName);
        } else {
          qRequest.pushUrlParameter("facet.field",sName);
        }
      }
      sName = sField;
      if (sAlias && (sAlias.length > 0)) sName = sAlias;
      
      v = checkVal(this.mincount);
      if (v != null) appendFDot(sName,"facet.mincount",v);
      v = checkVal(this.limit);
      if (v != null) appendFDot(sName,"facet.limit",v);
      v = checkVal(this.prefix);
      if (v != null) appendFDot(sName,"facet.prefix",v);
      v = checkVal(this.missing);
      if (v != null) appendFDot(sName,"facet.missing",v);
      v = checkVal(this.sort);
      if (v != null) appendFDot(sName,"facet.sort",v);
      if (bRange) {
        v = checkVal(this.rangeStart);
        if (v != null) appendFDot(sName,"facet.range.start",v);
        v = checkVal(this.rangeEnd);
        if (v != null) appendFDot(sName,"facet.range.end",v);  
        v = checkVal(this.rangeGap);
        if (v != null) appendFDot(sName,"facet.range.gap",v);    
      }
    },
    
    makeFilterQuery: function(sValue,sRangeValue) {
      // TODO needs to be Solr escaped
      var sField = this.field, sAlias = this.aliasKey;
      if (sAlias && (sAlias.length > 0)) sField = sAlias;
      var sQuery = sField+":\""+sValue+"\"";
      if (sValue == null) {
        sQuery = "-"+sField+":[* TO *]";
      } else if (this.useStaticExclusion) {
        sQuery = "{!tag=e"+this.uniqueId+"}"+sQuery;
      } else if (sRangeValue != null) {
        sQuery = sField+":"+sRangeValue;
      }
      return sQuery;
    },
    
    parseQResponse: function(responseObject,callback) {
      var sField = this.field, sAlias = this.aliasKey;
      if (!sField) return
      if (sAlias && (sAlias.length > 0)) sField = sAlias;
      
      var i, n, oField;
      var bRange = this.isRange, oRange, oCounts, sRangeValue = null;
      if (!bRange && responseObject && responseObject.facet_counts && 
          responseObject.facet_counts.facet_fields) {
        oField = responseObject.facet_counts.facet_fields[sField];
        if (oField) {
          n = oField.length;
          for (i=0;i<n;i+=2) {
            callback(oField[i],oField[i+1],sRangeValue);
          };
        }
      }
      
      if (bRange && responseObject && responseObject.facet_counts && 
          responseObject.facet_counts.facet_ranges) {
        oRange = responseObject.facet_counts.facet_ranges[sField];
        if (oRange && oRange.counts) {
          oCounts = oRange.counts;
          n = oCounts.length;
          for (i=0;i<n;i+=2) {
            if (oCounts[i+2]) {
              sRangeValue = "["+oCounts[i]+" TO "+oCounts[i+2]+"]";
            } else {
              sRangeValue = "["+oCounts[i]+" TO *]";
            }
            callback(oCounts[i],oCounts[i+1],sRangeValue);
          }
        }
      }
      
    },
  
  });
  
  oThisClass._staticCounter = 0;
  
  return oThisClass;
});