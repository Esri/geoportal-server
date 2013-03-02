<%--
 See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 Esri Inc. licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<% // searchjs.jsp - Provides the Javascript for the catalog search widget %>
<%@page language="java" contentType="text/javascript; charset=UTF-8" session="false"%>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%
  com.esri.gpt.framework.jsf.MessageBroker msg = 
    (new com.esri.gpt.framework.jsf.FacesContextBroker(request,response)).extractMessageBroker();

  String namespace = "gpt";
  String cssNamespace = "gpt";
  String caption = msg.retrieveMessage("catalog.widget.search.caption");
  String title = caption;
  String label = msg.retrieveMessage("catalog.widget.search.label");
  String tipSearch = msg.retrieveMessage("catalog.widget.search.tip.search");
  String tipKML = msg.retrieveMessage("catalog.widget.search.tip.kml");
  String tipRSS = msg.retrieveMessage("catalog.widget.search.tip.rss");
  String tipAbout = msg.retrieveMessage("catalog.widget.search.tip.about");
  String tipClose = msg.retrieveMessage("catalog.widget.search.tip.close");
  String msgNoMatch = msg.retrieveMessage("catalog.widget.search.msg.noMatch");
  String about = msg.retrieveMessage("catalog.widget.search.about");
  
  String frameworkUrl = "http://serverapi.arcgisonline.com/jsapi/arcgis/?v=2.5";
  String baseUrl = com.esri.gpt.framework.context.RequestContext.resolveBaseContextPath(request);
  String restUrl = baseUrl+"/rest/find/document";
  String cssUrl = baseUrl+"/widgets/widgets.css";
  String imagesUrl = baseUrl+"/widgets/images";
  String searchProxyUrl = baseUrl+"/widgets/searchProxy.jsp";
  String requestPath = baseUrl+"/widgets/searchjs.jsp";
    
  String anchor = chkStr(request.getParameter("anchor")).toLowerCase();
  boolean hasFramework = chkStr(request.getParameter("hasFramework")).equalsIgnoreCase("true");
  int max = chkInt(request.getParameter("max"),30);
  
  String propContentType = chkStr(request.getParameter("contentType"));
  String propDataCategory = chkStr(request.getParameter("dataCategory"));
  String propBBox = chkStr(request.getParameter("bbox"));
  String propSpatialRel = chkStr(request.getParameter("spatialRel"));
  String propAfter = chkStr(request.getParameter("after"));
  String propBefore = chkStr(request.getParameter("before"));
    String propRid = chkStr(request.getParameter("rid"));
    String propValidAfter = chkStr(request.getParameter("validAfter"));
    String propValidBefore = chkStr(request.getParameter("validBefore")); 
    String propIsPartOf = chkStr(request.getParameter("isPartOf"));
    String propPublisher = chkStr(request.getParameter("publisher"));
    String propSource = chkStr(request.getParameter("source"));
  String propOrderBy = chkStr(request.getParameter("orderBy"));
  
  String fLoadFramework = "_gLoadSearchFramework"+System.currentTimeMillis();
  String fLoadStyle = "_gLoadSearchStyle"+System.currentTimeMillis();
  String fStartup = "_gSearchStartup"+System.currentTimeMillis();
%>

<%!

	/**
	 * Converts a string to an int value.
	 * @param s the string to convert
	 * @param defaultVal the default value to return if the string is invalid
	 * @return the converted value
	 */
	private int chkInt(String s, int defaultVal) {
	  int n = defaultVal;
	  try {
	    n = Integer.parseInt(s);
	  } catch (Exception e) {
	    n = defaultVal;
	  }
	  return n;
	}

  /**
   * Check a string value.
   * @param s the string to check
   * @return the checked string (trimmed, zero length if the supplied String was null)
   */
  private String chkStr(String s) {
    if (s == null) return "";
    else return s.trim();
  }

%>

// load the Dojo framework
function <%=fLoadFramework%>() {
  var frameworkUrl = "<%=frameworkUrl%>";
  var hasFramework = <%=hasFramework%> || (typeof dojo != "undefined");
	if (!hasFramework && (frameworkUrl != null) && (frameworkUrl.length > 0)) {
	
	  // this works in Firefix 3, in IE7 it seems to mess up Dojo's knowledge of
	  // when the page is loaded and the dojo.addOnLoad function below never fires
    var elScript = document.createElement("script");
    elScript.src = frameworkUrl;
    elScript.type = "text/javascript";
    var elHead = document.getElementsByTagName("head").item(0);
    elHead.appendChild(elScript);
    
    // wait for the load
    var count = 0;
    var checkLoad = function() {
      count++;
      var hasDojo = (typeof dojo != "undefined");
      if (!hasDojo && (count < 600)) {
        setTimeout(checkLoad,100); 
      } else if (!hasDojo) {
        alert("<%=caption%>\nThe following script failed to load:\n"+frameworkUrl);
      } else {
        <%=fStartup%>();
      }
    }
    setTimeout(checkLoad,100);  
    
	} else {
	  <%=fStartup%>();
	}
};

// load the CSS style file
function <%=fLoadStyle%>() {
  var cssUrl = "<%=cssUrl%>";
  if ((cssUrl != null) && (cssUrl.length > 0)) {
    var elLink = document.createElement("link");
    elLink.rel= "stylesheet";
    elLink.ttpe = "text/css";
    elLink.href = cssUrl;
    var elHead = document.getElementsByTagName("head").item(0);
    elHead.appendChild(elLink);
  }
};
<%=fLoadStyle%>();
<%=fLoadFramework%>();

function <%=fStartup%>() {
	dojo.require("dijit._Widget");
	dojo.require('dojox.layout.FloatingPane');
	dojo.require("dojo.io.script");
	
	dojo.addOnLoad(function(){

	  // default configuration
	  var _gSearchNamespace = "<%=namespace%>";
	  var _gSearchRequestPath = "<%=requestPath%>";  
	  
		var _gSearchParams = {
		  dockable: false,
		  closable: true, 
		  maxable: false,
		  resizable: true,
		  
		  anchorButtonId: _gSearchNamespace+"-search-button",
		  anchorTextId: _gSearchNamespace+"-search-text",
		  anchor: "<%=anchor%>",
		  caption: "<%=caption%>",
		  configVariable: _gSearchNamespace+"SearchCfg",
		  cssNamespace: "<%=cssNamespace%>",
		  imagesUrl: "<%=imagesUrl%>",
		  about: "<%=about%>",
		  label: "<%=label%>",
		  max: <%=max%>,
		  msgNoMatch: "<%=msgNoMatch%>",
		  preferredTop: 50,
		  
      propContentType: "<%=propContentType%>",
      propBBox: "<%=propBBox%>",
      propSpatialRel: "<%=propSpatialRel%>",
      propDataCategory: "<%=propDataCategory%>",
      propAfter: "<%=propAfter%>",
      propBefore: "<%=propBefore%>",
      propRid: "<%=propRid%>",
      propValidAfter: "<%=propValidAfter%>",
      propValidBefore: "<%=propValidBefore%>",
      propIsPartOf: "<%=propIsPartOf%>",
      propPublisher: "<%=propPublisher%>",
      propSource: "<%=propSource%>",
      propOrderBy: "<%=propOrderBy%>",
      
		  restUrl: "<%=restUrl%>",
		  searchProxyUrl: "<%=searchProxyUrl%>",
		  tipClose: "<%=tipClose%>",
		  tipAbout: "<%=tipAbout%>",
		  tipKML: "<%=tipKML%>",
		  tipRSS: "<%=tipRSS%>",
		  tipSearch: "<%=tipSearch%>",
		  title: "<%=title%>"
		  
		};
		
		// function to build the search pane widget
	  var _gBuildSearchPane = function(params) {
	    var nd = _gInitializeSearchPaneNode(params);
      var anchor = new <%=namespace%>.SearchAnchor(params,null);
      anchor.startup();
	    var pane = new <%=namespace%>.SearchPane(params,nd);
	    pane.startup();
	  }
	  
    // declare the anchor
    dojo.provide("<%=namespace%>.SearchAnchor");
    dojo.declare("<%=namespace%>.SearchAnchor",dijit._Widget,{
    
      // build the inner controls associated with the widget
      build: function() {
      
        // find the script element
        var insertionNode = null;
        dojo.query("script").forEach(function(item) {
          if (item.src.indexOf(_gSearchRequestPath) == 0) {
            if (insertionNode == null) insertionNode = item;
          }
        });
        
        if (insertionNode == null) {
          alert(this.caption+"\nUnable to find widget insertion point.");
        }
            
        if (this.anchor == "link") {
	        var elLink = document.createElement("a");
	        elLink.id = this.anchorButtonId;
	        elLink.setAttribute("href","javascript:void(0);");
	        elLink.appendChild(document.createTextNode(this.caption));
	        insertionNode.parentNode.insertBefore(elLink,insertionNode);
	        
	      } else if (this.anchor == "button") {
	        var elBtn = document.createElement("button");
	        elBtn.className = this.cssNamespace+"-search-anchor-button";
	        elBtn.id = this.anchorButtonId;
	        elBtn.appendChild(document.createTextNode(this.caption));
	        insertionNode.parentNode.insertBefore(elBtn,insertionNode);
	        
	      } else if (this.anchor == "fieldset") {
	       
	        var elSet = document.createElement("fieldset");
          elSet.className = this.cssNamespace+"-search-anchor";
          insertionNode.parentNode.insertBefore(elSet,insertionNode);
          
            var elLbl = document.createElement("label");
            elLbl.setAttribute("for",this.anchorTextId);
            elLbl.appendChild(document.createTextNode(this.caption));
            elSet.appendChild(elLbl);
       
            var elGrp = document.createElement("div");
            elSet.appendChild(elGrp);
            
            var elTxt = document.createElement("input");
            elTxt.id = this.anchorTextId;
            elTxt.type = "text"
            elTxt.size = 30;
            elTxt.maxlength = 1024;
            elGrp.appendChild(elTxt);
          
            var elImg = document.createElement("img");
            elImg.id = this.anchorButtonId;
            elImg.src = this.imagesUrl+"/search.png";
            elImg.title = this.tipSearch;
            elImg.alt = this.tipSearch;
            elGrp.appendChild(elImg);
	      }
        
      },
    
      // override, startup
      startup: function() {
        if (this._started) return;
        this.inherited("startup",arguments);
        if (this.anchor != "none") {
	        if ((this.anchor != "link") && (this.anchor != "button") && 
	            (this.anchor != "fieldset")) {
	          this.anchor = "fieldset";
	        }
	        this.build();
	      }
      },
    
      // override, post create
      postCreate: function() {  
        this.inherited("postCreate",arguments);   
      }
    });
	     
	  // declare the widget as a FloatingPane extension
		dojo.provide("<%=namespace%>.SearchPane");
		dojo.declare("<%=namespace%>.SearchPane",dojox.layout.FloatingPane,{
		
		  about: "",
		  anchorButtonId: "",
	    anchorTextId: "",
	    caption: "",
	    configVariable: "",
	    cssNamespace: "",
		  imagesUrl: "",
		  msgNoMatch: "",
		  preferredTop: 50,
		  restUrl: "",
		  searchProxyUrl: "",
		  tipAbout: "",
	    tipClose: "",
	    tipKML: "",
	    tipRSS: "",
	    tipSearch: "",
		  
		  _anchoredTextNode: null,
		  _wasRepositioned: false,
		   
		  // return a KML response associated with the query
	    asKML: function() {
	      window.open(this.makeRestUrl("kml"),"_blank");
	    },
	    
	    // return an RSS response associated with the query
		  asRSS: function() {
		    window.open(this.makeRestUrl("georss"));
		  }, 
		
		  // build the inner controls associated with the widget
		  build: function() {
		    
		    var makeIcon = function(src,title) {
		      var elImg = document.createElement("img");
		      elImg.src = src;
		      elImg.title = title;
		      elImg.alt = title;
		      return elImg;
		    };
		    
		    var thisId = this.id;
		    var idPfx = this.id;
		    var cssPfx = this.cssNamespace+"-search-pane";    
	      var elBody = document.createElement("div");
	      elBody.id = idPfx+"-body";
	      elBody.className = cssPfx+"-body";
	      this.setContent(elBody);
	      
	        var elControl = document.createElement("div");
	        elControl.id = idPfx+"-control";
	        elControl.className = cssPfx+"-control";
	        elBody.appendChild(elControl);
	        
	          var elLabel = document.createElement("label");
	          elLabel.id = idPfx+"-label";
	          elLabel.setAttribute("for",idPfx+"-text");
	          elLabel.appendChild(document.createTextNode(this.label));
	          elControl.appendChild(elLabel);
	         
	          var elText = document.createElement("input");
	          elText.id = idPfx+"-text";
	          elText.type = "text"
	          elText.size = 40;
	          elText.maxlength = 1024;
	          elControl.appendChild(elText);
	          dojo.connect(elText,"onkeypress",this,"onKeyPressText");
	         
	          var elBtn = elBtn = makeIcon(this.imagesUrl+"/search.png",this.tipSearch);
	          elControl.appendChild(elBtn);
	          dojo.connect(elBtn,"onclick",this,"search");
	          
	          var elSep = document.createElement("span");
            elSep.innerHTML = "&nbsp;&nbsp;&nbsp;";
            elControl.appendChild(elSep);
	                  
	          elBtn = makeIcon(this.imagesUrl+"/rss.png",this.tipRSS);
	          elControl.appendChild(elBtn);
	          dojo.connect(elBtn,"onclick",this,"asRSS");
	          
	          elBtn = makeIcon("http://www.google.com/earth/images/google_earth_link.gif",this.tipKML);
	          elControl.appendChild(elBtn);
	          dojo.connect(elBtn,"onclick",this,"asKML");
	
	          elBtn = makeIcon(this.imagesUrl+"/about.png",this.tipAbout);
	          elControl.appendChild(elBtn);
	          dojo.connect(elBtn,"onclick",this,"showAbout");
	          
            elBtn = makeIcon(this.imagesUrl+"/loading.gif","");
            elBtn.id = idPfx+"-loading";
            elBtn.style.visibility = "hidden";
            elControl.appendChild(elBtn);
            //dojo.connect(elBtn,"onclick",this,"showAbout");
	          
	          dojo.query("span.dojoxFloatingCloseIcon",this.domNode).forEach(function(item) {
	            item.title = dijit.byId(thisId).tipClose;
	          });
	              
	        var elResults = document.createElement("div");
	        elResults.id = idPfx+"-results";
	        elResults.className = cssPfx+"-results";
	        elBody.appendChild(elResults);
		    	    
		    // connect all anchor text boxes to the onKeyPressText function
	      // i.e. all elements where: id=this.anchorTextId or name=this.anchorTextId 
	      dojo.query("[id='"+this.anchorTextId+"'], [name='"+this.anchorTextId+"']").forEach(function(item) {
	        dojo.connect(item,"onkeypress",dijit.byId(thisId),"onKeyPressText");
	      });
	      	    
		    // connect all anchor buttons to the onAnchorButtonClicked function
		    // i.e. all elements where: id=this.anchorButtonId or name=this.anchorButtonId 
		    dojo.query("[id='"+this.anchorButtonId+"'], [name='"+this.anchorButtonId+"']").forEach(function(item) {
		      dojo.connect(item,"onclick",dijit.byId(thisId),"onAnchorButtonClicked");
		    });
		    	    
		  },
		  
		  // override, cllose the widget
		  close: function() {
		    this.hide(null);
		  },
		  
		  // override, comment out this.domNode.style.display = "none";
			hide: function(/* Function? */ callback){
				dojo.fadeOut({
				  node:this.domNode,
				  duration:this.duration,
				  onEnd: dojo.hitch(this,function() {
				    //this.domNode.style.display = "none";
				    this.domNode.style.visibility = "hidden";
				    if(this.dockTo && this.dockable){
				      this.dockTo._positionDock(null);
				    }
				    if (typeof callback == "function") {callback();}
				  })
				}).play();
	    },
	    
	    // make the rest url
      makeRestUrl: function(fmt) {

        var appendParam = function(url,name,value) {
          if ((value != null) && (value.length > 0)) {
            url += "&"+name+"="+escape(dojo.trim(value));
          }
          return url;
        }
                
        var sUrl = this.restUrl;
        if (sUrl.indexOf("?") != -1) {
          sUrl = sUrl.substring(0,sUrl.indexOf("?"));
        }
        sUrl += "?f="+escape(fmt);
        
        var nMax = 30;
        if ((typeof this.max == "number") && (this.max > 0) && (this.max < 100)) {
          nMax = this.max;
        }
        sUrl += "&max="+escape(nMax);
        
        var sText = this.validateText();
        if (sText.length > 0) {
          sUrl += "&searchText="+escape(sText);  
        }
        
        sUrl = appendParam(sUrl,"contentType",this.propContentType);
        sUrl = appendParam(sUrl,"dataCategory",this.propDataCategory);
        sUrl = appendParam(sUrl,"bbox",this.propBBox);
        sUrl = appendParam(sUrl,"spatialRel",this.propSpatialRel);
        sUrl = appendParam(sUrl,"after",this.propAfter);
        sUrl = appendParam(sUrl,"before",this.propBefore);
        sUrl = appendParam(sUrl,"rid",this.propRid);
        sUrl = appendParam(sUrl,"validAfter",this.propValidAfter);
        sUrl = appendParam(sUrl,"validBefore",this.propValidBefore);  
        sUrl = appendParam(sUrl,"isPartOf",this.propIsPartOf);
        sUrl = appendParam(sUrl,"publisher",this.propPublisher);
        sUrl = appendParam(sUrl,"source",this.propSource);
        sUrl = appendParam(sUrl,"orderBy",this.propOrderBy);
        
        return sUrl; 
      },
		  	  	  	     
		  // handle a click on an achor button
	    onAnchorButtonClicked: function(e) {
	      this.open();
	      var elAnchorText = null;
	      
	      var queryAnchor = function(anchorTextId,qScope) {
	        var el = null;
	        var qStr = "[id='"+anchorTextId+"'], [name='"+anchorTextId+"']";
	        dojo.query(qStr,qScope).forEach(function(item) { 
	          if (el == null) {
	            if ((typeof item.value != "undefined") && (item.value != null)) el = item;
	          }
	        });
	        return el;
	      };
	      
	      // look for a close search text anchor
	      if (!e) e = window.event;
	      if (e) {
	        var target = (window.event) ? e.srcElement : e.target;
	        elAnchorText = queryAnchor(this.anchorTextId,target.parentNode);
	        if (elAnchorText == null) {
	          elAnchorText = queryAnchor(this.anchorTextId,target.parentNode.parentNode);
	        }
		    }
	      if (elAnchorText == null) {
	        elAnchorText = queryAnchor(this.anchorTextId,null);
	      }
	      
	      if (elAnchorText != null) {
	        this._anchoredTextNode = elAnchorText;
	        dojo.byId(this.id+"-text").value = dojo.trim(elAnchorText.value);;
	        this.search();
	      }
	    },
	    
		  // handle a key press within a search text box
		  onKeyPressText: function(e) {
		    if (!e) e = window.event;
		    if (e) {
		      var nKey = (e.keyCode) ? e.keyCode : e.which;
		      if (nKey == 13) {
		        var target = (window.event) ? e.srcElement : e.target;
		        var bAnchored = (typeof target.id == "undefined") || 
		                        (target.id == null) || (target.id != this.id+"-text");
		        if (bAnchored) {
		          if ((typeof target.value != "undefined") && (target.value != null)) {
		            this._anchoredTextNode = target;
		            dojo.byId(this.id+"-text").value = dojo.trim(target.value);;
		            this.open();
		            this.search();
		          }
		        } else {
		          this.search();
		        }
		      }
		    } 
		  },
		  
		  // expand/contract a resultant record on a title click
		  onTitleClicked: function(e) {
		    if (!e) e = window.event;
		    var target = (window.event) ? e.srcElement : e.target; 
		    if (target != null) {
		      var qScope = target.parentNode;
		      if (target.tagName.toLowerCase() == "img") qScope = qScope.parentNode;
		      dojo.query("div.abstract",qScope).forEach(function(item) {
		        if (item.style.display == "block") item.style.display = "none";
		        else item.style.display = "block";
		      });
		      dojo.query("div.links",qScope).forEach(function(item) {
		        if (item.style.display == "block") item.style.display = "none";
		        else item.style.display = "block";
		      });
		    }
		  },
		   
		  // open the widget
	    open: function() {
	      if (this.domNode.style.visibility == "hidden") {
	        this.reposition();
	        //this.show(dojo.hitch(this,this.reposition));
	        this.show(null);
	      } else {
	        this.reposition();
	      }
	    },
		  
		  // override, post create
		  postCreate: function() {  
		    this.inherited("postCreate",arguments);
		    this.domNode.style.visibility = "hidden"; 	    
		  },
		  
		  renderResponse: function(response,wasSearched) {
		    var thisId = this.id;
		    var resultsId = this.id+"-results";
		    var count = 0;
		    var nDuration = 400;
		    var elResults = dojo.byId(resultsId);
	
		    elResults.innerHTML = response;
		    
		    dojo.query("div.title",resultsId).forEach(function(item) {
		      count++;
		      dojo.connect(item,"onclick",dijit.byId(thisId),"onTitleClicked");
		    });
		    if (wasSearched && (count == 0) && (response.length == 0)) {
		      elResults.innerHTML = this.msgNoMatch;
		    }
		    
		    dojo.byId(this.id+"-loading").style.visibility = "hidden";     
		  },
		  
		  // reposition to ensure visibility
		  reposition: function() {
		    var nd = this.domNode;
	      var vp = dijit.getViewport();
	      var mb = dojo.marginBox(nd);
	      var ab = dojo.coords(nd,true);
	      if ( !this._wasRepositioned ||
	           (vp.t > (ab.t+10)) || ((ab.t+ab.h) > (vp.t+vp.h+100)) ||
	           (vp.l > (ab.l+100)) || ((ab.l+ab.w) > (vp.l+vp.w+100)) ) {
	        this._wasRepositioned = true;
		      var nL = Math.floor(vp.l+((vp.w-mb.w)/2));
	        var nT = Math.floor(vp.t+((vp.h-mb.h)/2));
	        var nPrefTop = this.preferredTop;
	        if ((typeof nPrefTop == "number") && (nPrefTop > 0) && (nPrefTop < (vp.h+10))) {
	          var nMaxTop = vp.t + nPrefTop;
	          if (nT > nMaxTop) nT = nMaxTop;
	        }
	        if (nT < vp.t) nT = (vp.t+10);
	        //if (nL < vp.l) nL = (vp.l+10);
	        dojo.style(nd,{left:nL+"px",top:nT+"px"});
	      }
		  },
		  
		  // execute the search
		  search: function() {
		    var thisId = this.id;
		    var sUrl = this.makeRestUrl("htmlfragment");
		    var useProxy = ((this.searchProxyUrl != null) && (this.searchProxyUrl.length > 0));    
	 
	      dojo.byId(this.id+"-loading").style.visibility = "visible";
		    if (!useProxy) {
		      dojo.xhrGet({
		        handleAs: "text",
		        load: function(responseObject,ioArgs){
		              dijit.byId(thisId).renderResponse(responseObject,true);},
		        preventCache: true,
		        url: sUrl
		      });
		      
		    } else {
		      sUrl = this.searchProxyUrl+"?url="+escape(sUrl);	      
		      dojo.io.script.get({
		        url: sUrl,
	          load: function(responseObject,ioArgs){
	                  dijit.byId(thisId).renderResponse(responseObject.innerHTML,true);
	                },
	          preventCache: true,
	          callbackParamName: "callback"
	        });
		      
		    }
		  },
		  
		  // override, show the widget
		  show: function(/* Function? */callback){
			  var anim = dojo.fadeIn({
			    node: this.domNode,
			    duration: this.duration,
			    beforeBegin: dojo.hitch(this,function(){
			      this.domNode.style.display = "";
			      this.domNode.style.visibility = "visible";
			      if (this.dockTo && this.dockable) {this.dockTo._positionDock(null);}
			      
			      // This line of original Dojo code was causing problems, moved to onEnd:
			      // if (typeof callback == "function") {callback();}
			      this._isDocked = false;
			      if (this._dockNode) {
			        this._dockNode.destroy();
			        this._dockNode = null;
			       }
		      }),
		      onEnd: dojo.hitch(this,function(){
	          if (typeof callback == "function") {callback();}
	        })
		    }).play();
		    
		    // This line of original Dojo code was causing problems, element grows in size
		    // this.resize(dojo.coords(this.domNode));
	    },
		  
		  // show about about the widget
		  showAbout: function() {
		    this.renderResponse(this.about,false);
	    },
		  
		  // override, startup
		  startup: function() {
	      if (this._started) return;
	      this.inherited("startup",arguments);
	      this.build();
	      this.reposition();
	    },
	    
	    // validate and synchronize text input
	    validateText: function() {
	      var elText = dojo.byId(this.id+"-text");
	      var sText = dojo.trim(elText.value);
	      if (sText != elText.value) elText.value = sText;
	      if ((typeof this._anchoredTextNode != "undefined") && (this._anchoredTextNode != null)) {
	        this._anchoredTextNode.value = sText;
	      }
	      return sText;
	    }
		  
		});
		  
	  // function to collect customized configuration and to make the serach pane node
	  var _gInitializeSearchPaneNode = function(params) {
	  
	    // look for customized configuration
	    var param;
	    var scriptParams = null;
	    try {
	      dojo.query("script["+params.configVariable+"]").forEach(function(item) {
	        if (item.getAttribute(params.configVariable) != null) {
	          scriptParams = eval("("+item.getAttribute(params.configVariable)+")");
	        }
	      });
	      if (scriptParams != null) {
	        for(param in scriptParams) params[param] = scriptParams[param];
	      }
	    } catch (err) {}
	    try {
	      var inlineParams = eval(params.configVariable);
	      if (inlineParams != null) {
	        for(param in inlineParams) params[param] = inlineParams[param];
	      }
	    } catch (err) {}  
	      
	    // create a base container node
	    var idPfx = _gSearchNamespace+"-search-pane";
	    var cssPfx = params.cssNamespace+"-search-pane"
	    var elContainer = document.createElement("div");
	    elContainer.id = idPfx+"-container";
	    elContainer.className = cssPfx+"-container";
	    dojo.body().appendChild(elContainer);
	    
	    // create a the DOM node for the widget
	    var elPane = document.createElement("div");
	    elPane.id = idPfx;
	    elPane.className = cssPfx;
	    elPane.style.position = "absolute";
	    elPane.style.display = "none";
	    elContainer.appendChild(elPane);
	    
	    return elPane;
	  }
		
	  // trigger the widget build
	  _gBuildSearchPane(_gSearchParams);
	    
	});

}


