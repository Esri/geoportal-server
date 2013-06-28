/*See the NOTICE file distributed with
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
*/ 
package com.esri.gpt.finddata
{
  
import com.esri.gpt.utils.Utils;

import mx.collections.ArrayCollection;  


/**
 * @author TM
 * 
 * Class representing search end point
 * 
 * 
 * */
[Bindable]
public class AGSGptRSSEndPoint {
  // class variables ===========================================================
  /** default parser id to be used if one is not supplied **/
  public static const LINK_PARSERID_DEFAULT:String = "default";
  
  // instance variables ========================================================
  /** site name **/
  private var _siteName:String;
  
  /** site url **/
  private var _siteUrl:String;
  
  /** link parser id **/
  private var _linkParserId:String;
  
  /** configuration xml **/
  private var _configXml:XML;
  
  /** The label Prefix **/
  private var _useParentNameAsPrefix:Boolean;
  
  /** The parent name **/
  private var _parentName:String;
  
  // constructors ==============================================================
  public function AGSGptRSSEndPoint(siteName:String, siteUrl:String, 
    configXml:XML, linkParserId:String, useParentNameAsPrefix:Boolean = false,
    parentName:String = ""):void {
    this._configXml = configXml;
  	this.siteName = siteName;
  	this.siteUrl = siteUrl;
  	this.linkParserId = linkParserId;
  	this.useParentNameAsPrefix = useParentNameAsPrefix;
  	this.parentName = parentName;
    	
  }
  
  // properties ================================================================
  /**
  * Set sitename
  * 
  * @param siteName the site name
  * 
  * */
  public function set siteName(siteName:String):void {
    this._siteName = siteName;
  }
  /**
  * Get sitename
  * 
  * @param siteName the site name (trimmed, never null)
  * 
  * */
  public function get siteName():String {
    return Utils.chkString(this._siteName); 
  }
  
  /**
  * Sets the site url
  * 
  * @param siteUrl The site url
  * */
  public function set siteUrl(siteUrl:String):void {
    
    this._siteUrl = Utils.chkString(siteUrl);
   
  }
  
  /**
  * Gets the site url
  * 
  * @return site url (trimmed, never null)
  * 
  * */
  public function get siteUrl():String {
    return Utils.chkString(this._siteUrl); 
  }
  
  /**
  * specifies whether parent name should be used as prefix
  * 
  * @return true/false (default is  false)
  * */
  public function get useParentNameAsPrefix():Boolean {
    return Utils.chkBoolean(this._useParentNameAsPrefix);
  }
  
  /** 
  * set the parent name as the prefix
  * 
  * @param useParentNameAsPrefix Use the parent name as prefix
  * */
  public function set useParentNameAsPrefix(useParentNameAsPrefix:Boolean):void {
    this._useParentNameAsPrefix = useParentNameAsPrefix;
  }
  
  /**
  * Set the parent name
  * 
  * @param parentName The parent name
  * 
  * */
  public function set parentName(parentName:String):void {
    this._parentName = parentName;
  }
  
  /**
  * Gets the parent name
  * 
  * @return the parent name (never null, trimmed)
  * */
  public function get parentName():String {
    return Utils.chkString(this._parentName);
  }
  
  /**
  * Link Parser id
  * 
  * @return link parser id (trimmed, never null)
  * */
  public function get linkParserId():String {
    var id:String = Utils.chkString(this._linkParserId);
    if(id == "") {
      this._linkParserId = LINK_PARSERID_DEFAULT;
    }
    return Utils.chkString(this._linkParserId);
  }
  
  /**
  * Link Parser id
  * 
  * @param linkParserId id to be used 
  * 
  * */
  public function set linkParserId(linkParserId:String):void {
    this._linkParserId = linkParserId;
  }
  
  
  /**
  * Returns regular expression for detecting map links of this end point.
  * 
  * */
  public function get regDetectMapLinkProvider():ArrayCollection {
    
    return orgarnizePatterns( 
		    _configXml.linkParser.(@id == linkParserId).regDetectMapLinkProvider.text(), 
		    _configXml.linkParser.(@id == linkParserId).regDetectMapLinkProvider.@delimeter);
    
  }
  
  /**
  * Returns regular expressions for extracting map service from the 
  * map link detected in url gotten from regDetectMapLinkProvider
  * 
  * @return regular expression array (never null)
  * */
  public function get regExtractMapService():ArrayCollection {
    return orgarnizePatterns( 
		    _configXml.linkParser.(@id == linkParserId).regExtractMapservice.text(), 
		    _configXml.linkParser.(@id == linkParserId).regExtractMapservice.@delimeter,
		    _configXml.linkParser.(@id == linkParserId).regExtractMapservice.@replaceDelimeter); 
  }
  
    /**
  * Returns regular expressions for extracting map link  type detected in url
  * gotten from regDetectMapLinkProvider
  * 
  * @return regular expression array (never null)
  * */
  public function get regExtractMapLinkType():ArrayCollection {
    return orgarnizePatterns( 
		    _configXml.linkParser.(@id == linkParserId).regExtractMapLinkType.text(), 
		    _configXml.linkParser.(@id == linkParserId).regExtractMapLinkType.@delimeter,
		    _configXml.linkParser.(@id == linkParserId).regExtractMapLinkType.@replaceDelimeter); 
  }
  
   
  /**
  * Returns regular expressions to remove links not to be shown in map
  * 
  * @return reg expression (trimmed, never null)
  * 
  * **/
  public function get regExcludeLinks():String {
   return Utils.chkString(this._configXml.linkParser.(@id == linkParserId).regExcludeLinks.text());
  }
  
  /**
  * The label of the end Point
  * 
  * @return the label for this end point
  * */
  public function get label():String {
    var label:String = this.siteName;
    if(this.useParentNameAsPrefix == true) {
      label = this.parentName + label;
    }
    return label;
  }
  
  	
  
  // methods ===================================================================
  /**
  * String representation
  * */
  public function toString():String {
    return "Site name = " + this.siteName + "," ;
  }
  
  /**
		 * Makes an arraycollection of patterns from a string
		 * elements will include object.sRegEx, object.sReplace
		 * 
		 * */
  private function orgarnizePatterns(sPatterns:String, sDelimeter:String="",
    sReplaceDelimeter:String = "")
		  :ArrayCollection {
		  var arr:ArrayCollection = new ArrayCollection();
		  var sPtn:String = Utils.chkString(sPatterns);
		  var sDel:String = Utils.chkString(sDelimeter);
		  var sRepDel:String = Utils.chkString(sReplaceDelimeter);
		  var objKv:Object;
		  var arrKv:Array;
		  var key:String = "";
		  var value:String = "";
		  var delDummy:String = "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@";
		  if(sRepDel == "") {
		    sRepDel = delDummy;
		  }
		  if(sDel == "") {
		    sDel = delDummy;
		  }
		  
		  if(sPtn == "") {
		    return arr;
		  }
		  
		  if(sDel == delDummy) {
		    arrKv = sPtn.split(sRepDel);
		    key = (arrKv != null && arrKv.length > 0 && arrKv[0] != null)? arrKv[0].toString(): "";
		    value = (arrKv != null && arrKv.length > 1 && arrKv[1] != null)? arrKv[1].toString(): "";
		    arr.addItem({sRegEx:key, sReplace:value});
		  } else {
		    for each (var strToken:String in sPtn.split(sDel)) {
		      if(strToken == null) {
		        continue;
		      }
		      arrKv = strToken.split(sRepDel);
		      key = (arrKv != null && arrKv.length > 0 && arrKv[0] != null)? arrKv[0].toString(): "";
		      value = (arrKv != null && arrKv.length > 1 && arrKv[1] != null)? arrKv[1].toString(): "";
		      arr.addItem({sRegEx:key, sReplace:value});
		    }
		  }
		  return arr;
  }
 
 
 
  /**
  * Returns an array collection of all the endpoints defined
  * 
  * @param configXML the configuration XML
  * 
  * @return end points (never null, possibly empty)
  * 
  * */
  public static function readEndpoints(configXML:XML):ArrayCollection {
    var endPoints:ArrayCollection = new ArrayCollection();
    var endPointList:XMLList = configXML.gptEndpoint;
    var sRegEndpointList:String = Utils.chkString(configXML.regEndpointList);
    var url:String;
	  for each (var endPoint:XML in endPointList) {
	    url = Utils.chkString(endPoint.@url);
	    if(sRegEndpointList != "" && 
	      Utils.chkStringMatch(url.match(new RegExp(sRegEndpointList)))) {
	      
	    }
	    
		  endPoints.addItem(new AGSGptRSSEndPoint(endPoint.@name, 
		    url, configXML, endPoint.@linkParserId));
	  }
	  return endPoints;
  }
 
  /**
  * Extracts AIMS host from a url
  * 
  * @param url URL to extract from service name (trimmed, never null)
  * 
  * */
  public function extractAimsHost(url:String):String {
    var regExtractAimsHost:ArrayCollection = orgarnizePatterns( 
	    _configXml.linkParser.(@id == linkParserId).regLinkTypes.arcIms.regExtractHost, 
	    _configXml.linkParser.(@id == linkParserId).regLinkTypes.arcIms.regExtractHost.@delimeter,
	    _configXml.linkParser.(@id == linkParserId).regLinkTypes.arcIms.regExtractHost.@replaceDelimeter); 
	  return readString(regExtractAimsHost,url);
  }
  
  /**
		 * Returns a service type string indicating the type of service
		 * the url points to
		 * @param url Url to the service endpoint, or string service type
		 * */
	public function extractServiceType(url:String):AGSGptEnumServiceTypes {
    var serviceType:AGSGptEnumServiceTypes  = null;
    if(url == null || url == "") {
  	  return null;
    }
    //url = url.toLowerCase();
    var imsReg:String = 
      _configXml.linkParser.(@id == linkParserId).regLinkTypes.arcIms.regDetect;
    var agsReg:String = 
      _configXml.linkParser.(@id == linkParserId).regLinkTypes.agsRest.regDetect;
    var wmsReg:String = 
       _configXml.linkParser.(@id == linkParserId).regLinkTypes.wms.regDetect;
      
    
    url = Utils.chkString(url);
    var arrWms:Array = url.match(new RegExp(wmsReg));
    var arrIms:Array = url.match(new RegExp(imsReg));
    var arrAgs:Array = url.match(new RegExp(agsReg));
   
    if (Utils.chkStringMatch(arrWms).length >= 1 ) {
      serviceType = AGSGptEnumServiceTypes.WMS;
    } else if (Utils.chkStringMatch(arrIms).length >= 1) {
      serviceType = AGSGptEnumServiceTypes.AIMS;
    } else if (Utils.chkStringMatch(arrAgs).length >= 1) {
      serviceType = AGSGptEnumServiceTypes.AGS_REST;
    }
               
    return serviceType;
  }	
  
  /**
  * Extract AIMS service name from a url
  * 
  * @param url URL to extract from service name (trimmed, never null)
  * */
  public function extractAimsServiceName(url:String):String {
     var regExtractServiceName:ArrayCollection = orgarnizePatterns( 
		   _configXml.linkParser.(@id == linkParserId).regLinkTypes.arcIms.regExtractServiceName, 
		   _configXml.linkParser.(@id == linkParserId).regLinkTypes.arcIms.regExtractServiceName.@delimeter,
		   _configXml.linkParser.(@id == linkParserId).regLinkTypes.arcIms.regExtractServiceName.@replaceDelimeter);  
		 return readString(regExtractServiceName, url);
  }
  
  /*** 
  * Cloning endpoint
  * 
  * @return cloned agsGptRSSEndpoint
  */
  public function clone():AGSGptRSSEndPoint {
    return new AGSGptRSSEndPoint(this.siteName,this.siteUrl, 
      this._configXml, this.linkParserId);
      
  }
  
  /**
  * Checks whether this url is a repository or if it points to an xml
  * repository list
  * 
  * @return true if it is, false if not
  * */
  public function readIsRepisotoryListUrl():Boolean {
    var reg:String = 
      _configXml.linkParser.(@id == linkParserId).regDetectEndpointListUrl;
    reg = Utils.chkString(reg);
    if(reg == "") {
      return false;
    }
    var arrMatch:Array = 
      Utils.chkStringMatch(this.siteUrl.match(new RegExp(reg)));
    
    return arrMatch.length >= 1;
    
  }
  
  /**
  * Reads the repository url
  * 
  * @param id The id to construct the url out of
  * */
  public function readRepositoryUrl(id:String):String {
    var regsRepositoryPrfx:ArrayCollection = orgarnizePatterns( 
		    _configXml.linkParser.(@id == linkParserId).regExtractExtSearchPrfx.text(), 
		    _configXml.linkParser.(@id == linkParserId).regExtractExtSearchPrfx.@delimeter,
		    _configXml.linkParser.(@id == linkParserId).regExtractExtSearchPrfx.@replaceDelimeter); 
		
		var str:String = readString(regsRepositoryPrfx, this.siteUrl);
	  return str.replace(new RegExp("{id}"), id);    
	
  }
  
  /**
  * Takes the orgarnized patterns and the string and process it
  * 
  * @regOrgPatterns Output from function orgarnizepatterns
  * @str The string to be processed
  * 
  * @return processed string (trimmed, never null)
  * */
  public static function readString(regOrgPatterns:ArrayCollection, str:String ):String {
    if(regOrgPatterns == null || str == null || str == "") {
      return "";
    }
  
    for each (var objPtn:Object in regOrgPatterns) {
      str = str.replace(new RegExp(objPtn.sRegEx), objPtn.sReplace);
    } 
    return Utils.chkString(str);
    
  }
   
}
}