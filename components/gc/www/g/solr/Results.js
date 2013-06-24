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
define(["dojo/_base/declare",
        "dojo/_base/lang",
        "dojo/dom-construct",
        "dojo/dom-class",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/text!./templates/Results.html",
        "dojo/i18n!./nls/resources",
        "./base/QComponent"], 
function(declare, lang, domConstruct, domClass,
         _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, template, i18n,
         QComponent) {
  
  var oThisClass = declare("g.solr.Results",
        [_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, QComponent],{
    
    i18n: i18n,
    templateString: template,
    start: 0,
    rows: 10,
    _preserveStart: false,
    
    postCreate: function() {
      this.inherited(arguments);
      this.ensureSolrClient();
      this.solrClient.registerQComponent(this);
    },
    
    appendQParameters: function(qRequest) {
      if (!this._preserveStart) this.start = 0;
      qRequest.putUrlParameter("start",this.start);
      qRequest.putUrlParameter("rows",this.rows);
    },
    
    handleQResponse: function(responseObject) {
      this._preserveStart = false;
      var i, o, doc, docs, nDocs=0, nHits=0, nPage=0, nPages=0, bMore=false, s;
      
      var oSummaryNode = this.summaryCountNode;
      domConstruct.empty(oSummaryNode);
      if (responseObject.response) {
        o = responseObject.response.numFound;
        if (typeof(o) != "undefined") nHits = o;
        o = responseObject.response.start;
        if (typeof(o) != "undefined") this.start = o;
        if (responseObject.response.docs) {
          nDocs = responseObject.response.docs.length;
        }
        
        bLess = (this.start > 0);
        bMore = ((this.start+this.rows) < nHits);
        if (bLess) {
          this.__pagerFirst.domNode.style.visibility = "visible";
          this.__pagerPrevious.domNode.style.visibility = "visible";

        } else {
          this.__pagerFirst.domNode.style.visibility = "hidden";
          this.__pagerPrevious.domNode.style.visibility = "hidden";
        }
        if (bMore) {
          this.__pagerNext.domNode.style.visibility = "visible";
        } else {
          this.__pagerNext.domNode.style.visibility = "hidden";
        }
        
        // TODO: need i18n strings for this (and facet count as well)
        if (nDocs == 0) {
          s = "0 results";
        } else if (nDocs == 1) {
          s = "1 result";
        } else if (bMore || bLess) {
          nPage =  Math.ceil((this.start+nDocs) / this.rows);
          nPages = Math.ceil(nHits / this.rows);
          //s = (this.start+1)+" to "+(this.start+nDocs)+" of "+nHits;
          s = nHits+" results. Page "+nPage+" of "+nPages;
        } else  {
          s = nDocs+" results";
        }
        oSummaryNode.appendChild(document.createTextNode(s));
      }
      
      var oItemsNode = this.itemsNode;
      domConstruct.empty(oItemsNode);
      if (responseObject.response && responseObject.response.docs) {
        
        docs = responseObject.response.docs;
        for (i=0;i<docs.length;i++) {
          doc = docs[i];

          var el = domConstruct.create("div",{
            innerHTML: doc.title,
            style: {margin:"5px", padding: "2px", border:"1px solid #cccccc"}
          });
          
          var elLinks = domConstruct.create("span",{className:"links"},el);
          domConstruct.create("a",{
            target: "_blank",
            href: doc["url.metadata_s"],
            innerHTML: "xml",
            style: {marginLeft: "10px"}
          },elLinks);
          
          var sHref = this.solrClient.collectionUrl+"/select";
          sHref += "?q=id:"+doc.id;
          domConstruct.create("a",{
            target: "_blank",
            href: sHref+"&indent=on&wt=xml",
            innerHTML: "solrxml",
            style: {marginLeft: "10px"}
          },elLinks);
          domConstruct.create("a",{
            target: "_blank",
            href: sHref+"&indent=on&wt=json",
            innerHTML: "solrjson",
            style: {marginLeft: "10px"}
          },elLinks);
          
          oItemsNode.appendChild(el);
        }
      }
    },
    
    _onPagerFirstClicked: function(e) {
      this._preserveStart = false;
      this.start = 0;
      this.search();
    },
    _onPagerPreviousClicked: function(e) {
      this._preserveStart = true;
      this.start -= this.rows;
      this.search();
    },
    _onPagerNextClicked: function(e) {
      this._preserveStart = true;
      this.start += this.rows;
      this.search();
    },
    
    search: function() {
      if (this.solrClient) this.solrClient.fetch();
    }
    
  });
  
  
  return oThisClass;
});