define([
  'dojo/_base/declare',
  'dojo/_base/lang',
  'dojo/topic'
],function(declare,lang,
           topic){
    return declare(null, {    

  postCreate: function(){
    this.suffixes = ["csv", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "gml", "pdf", "zip", "xml", "html", "htm", "aspx", "lyr"];
    this.suffixesKML = [".kml","kmz"];
  },

  findContentTypeUrl: function(links){
                
      var theLinkType = this._processLink(links);     

      var imgURL = "";
      switch(theLinkType) {
        case "www":
            imgURL = "widgets/GeoportalSearch/images/ContentType_clearinghouse.png";
            break;
        case "webmap":
            imgURL = "widgets/GeoportalSearch/images/ContentType_liveData.png";
            break;
        case "mapserver":
            imgURL = "widgets/GeoportalSearch/images/ContentType_liveData.png";
            break;
        case "featureserver":
            imgURL = "widgets/GeoportalSearch/images/ContentType_liveData.png";
            break;
        case "imageserver":
            imgURL = "widgets/GeoportalSearch/images/ContentType_liveData.png";
            break;
        case "wms":
            imgURL = "widgets/GeoportalSearch/images/ContentType_liveData.png";
            break;
        case "kml":
            imgURL = "widgets/GeoportalSearch/images/ContentType_geographicActivities.png";
            break;
        default:
            imgURL = "widgets/GeoportalSearch/images/ContentType_unknown.png";        
      }

      return imgURL;
  },

   _processLink: function(links){

    var contentTypeLinkType = "";

    for (var j=0; j < links.length; j++) {
          
          var theLink = links[j];
           var theLinkType = "";
          theLink.mapServiceType = theLinkType; 
          if ((theLink.type == "open")) {

              var href = theLink.href;
              var hrefLower = href.toLowerCase();
            
              if (hrefLower.indexOf("request=getcapabilities") !== -1) {
                if (hrefLower.indexOf("service=wms") !== -1) {
                  theLinkType = "wms";
                } else {
                  theLinkType = "unsupported"; 
                }
                
              } else if (hrefLower.indexOf("/rest/services/") !== -1) {
                theLinkType = hrefLower.split("/").pop();
                
                if (hrefLower.indexOf("?f=") > 0) {
                  theLinkType = theLinkType.substr(0, theLinkType.indexOf("?f="));
                  href = href.substr(0, href.indexOf("?f="));
                }
                
              } else if (hrefLower.indexOf("/services/") !== -1) {
                if (hrefLower.indexOf("/mapserver/wmsserver") !== -1) {
                  theLinkType = "wms";
                }
                
              } else if (hrefLower.indexOf("/com.esri.wms.esrimap") !== -1) {
                theLinkType = "wms";
                if (hrefLower.indexOf("?") > 0) {
                  href = href.substr(0, href.indexOf("?"));
                }
                
              } else if ((hrefLower.indexOf("viewer.html") !== -1) && (hrefLower.indexOf("url=") !== -1)) {
                href = href.substr(href.indexOf("url=")+4);
                href = decodeURIComponent(href);
                theLinkType = href.split("/").pop().toLowerCase();
                
              } else if ((hrefLower.indexOf("index.jsp") !== -1) && (hrefLower.indexOf("resource=") !== -1)) {
                href = href.substr(href.indexOf("url=")+4);
                href = decodeURIComponent(href);
                theLinkType = href.split("/").pop().toLowerCase();
                
              } else if ((hrefLower.indexOf("/sharing/content/items/") !== -1) && (hrefLower.split("/").pop() == "data")) {
                theLinkType = "webmap";
                if (hrefLower.indexOf("?") > 0) {
                  href = href.substr(0, href.indexOf("?"));
                }
              } 
              theLink.mapServiceType = theLinkType; 

              contentTypeLinkType =  theLinkType;                              
            } 

          }   

          return contentTypeLinkType;     
      },


  _processLink2: function(link,theLinkType){

      var theLink = link;
      
      if ((theLink.type == "open") || (theLink.type == "customLink") || (theLink.type == "agslyr")  || (theLink.type == "addToMap")) {

            // if a link type has already been established other than www
            if (theLinkType.length > 0 && theLinkType != "www") return false;
                  
            var href = theLink.href;
            var hrefLower = href.toLowerCase();
            
            // if the link ends in any of the this.this.suffixes, it's not a map service, but general web link
            // if not assigned value yet, check for typical file types
            if (
              (theLinkType.length == 0) || (theLinkType === "www")){
              for (k=0; k<this.this.suffixes.length; k++) {
                var suffix = this.this.suffixes[this.index];
                if (hrefLower.indexOf(suffix) + suffix.length == hrefLower.length) {
                  theLinkType = "www";
                }
              }
            }

            // if not assigned value yet, check for KML/KMZ
            if ((theLinkType.length == 0) || (theLinkType === "www")) {
              for (k=0; k<this.this.suffixesKML.length; k++) {
                var suffix = this.this.suffixesKML[k];
                if (hrefLower.indexOf(suffix, hrefLower.length - suffix.length) !== -1) {
                  theLinkType = "kml";
                }
              }
            }
            
            // if not assigned value yet, check for services
            if ((theLinkType.length == 0) || (theLinkType === "www")) {
              if (hrefLower.indexOf("request=getcapabilities") !== -1) {
                if (hrefLower.indexOf("service=wms") !== -1) {
                  theLinkType = "wms";
                } else {
                  theLinkType = "unsupported";
                }
                
              } else if (hrefLower.indexOf("/rest/services/") !== -1) {
                theLinkType = hrefLower.split("/").pop();
                
                if (hrefLower.indexOf("?f=") > 0) {
                  theLinkType = theLinkType.substr(0, theLinkType.indexOf("?f="));
                  href = href.substr(0, href.indexOf("?f="));
                }
                
              } else if (hrefLower.indexOf("/services/") !== -1) {
                if (hrefLower.indexOf("/mapserver/wmsserver") !== -1) {
                  theLinkType = "wms";
                }
                
              } else if (hrefLower.indexOf("/com.esri.wms.esrimap") !== -1) {
                theLinkType = "wms";
                if (hrefLower.indexOf("?") > 0) {
                  href = href.substr(0, href.indexOf("?"));
                }
                
              } else if ((hrefLower.indexOf("viewer.html") !== -1) && (hrefLower.indexOf("url=") !== -1)) {
                href = href.substr(href.indexOf("url=")+4);
                href = decodeURIComponent(href);
                theLinkType = href.split("/").pop().toLowerCase();
                
              } else if ((hrefLower.indexOf("/sharing/content/items/") !== -1) && (hrefLower.split("/").pop() == "data")) {
                theLinkType = "webmap";
                if (hrefLower.indexOf("?") > 0) {
                  href = href.substr(0, href.indexOf("?"));
                }
              }
            }

            // if not assigned value yet, check if the layer ends with f=lyr cause then we can make a rest URL of it
            if ((theLinkType.length == 0) || (theLinkType === "www")) {
              suffix = "?f=lyr";
              if (hrefLower.indexOf(suffix) + suffix.length == hrefLower.length) {
                theLinkType = hrefLower.split("/").pop();
                href = href.replace(suffix, "");
              }
            }
            
            // if all else fails, just make it a generic web link
            if (theLinkType.length == 0) {
              
              theLinkType = "www";
            }

            link.mapServicetype = theLinkType;
            link.mapServiceUrl = href;

          }

          return theLinkType;
  }

   });
});