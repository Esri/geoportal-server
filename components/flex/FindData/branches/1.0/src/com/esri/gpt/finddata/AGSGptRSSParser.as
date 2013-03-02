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
  import mx.collections.ArrayCollection;
  
  
  /**
  * @Author TM
  * 
  * Parser for RSS
  * */
  public class AGSGptRSSParser
  {
    // instance variables ======================================================
    /** The RSS record objects */
    private var _agsGptRssRecords:AGSGptRSSRecordList;
    
    /** The XML RSS Object */
    private var _xmlRss:XML;
    
    /** The xml config object */
    private var _xmlConfig:XML;
    
    protected  var _agsGptModel:AGSGptRSSModel;
    /**  Endpoint **/
    private var _endPoint:AGSGptRSSEndPoint;
    // constructors ============================================================
    /**
    * Dont use this constructor!  Use the method create
    * 
    * 
    * @param rss RSS XML.  If null or cannot be identified then exception thrown
    * @param config The configuration xml
    * @throws Error if rss is null or cannot be identified
    * */
    public function AGSGptRSSParser(rss:XML,agsGptModel:AGSGptRSSModel) {
      if(rss == null) {
        throw new Error("Unable to determine RSS feed type. null xml recived");
      }
      _agsGptModel = agsGptModel;
    }
    // properties ==============================================================
    /**
    * Set end point
    * 
    * @param endPoint The endpoint
    *  */
    public function set endPoint(endPoint:AGSGptRSSEndPoint):void {
      this._endPoint = endPoint;
    }
    
    /**
    * Gets the end point
    * 
    * */
    public function get endPoint():AGSGptRSSEndPoint {
      return this._endPoint;
    }
       
    /**
    * Sets the rss record objects
    * 
    * @param records
    * */
    public function set agsGptRssRecords(records:AGSGptRSSRecordList):void {
      this._agsGptRssRecords = records;
    }
    
    /**
    * Gets the rss record objects
    * 
    * @return th rss record objects possibly null
    * */
    public function get agsGptRssRecords():AGSGptRSSRecordList {
      return this._agsGptRssRecords;  
    }
    
    /**
    * Sets the xml RSS
    * 
    * @param xmlRss the xml object
    * */
    protected function set xmlRss(xmlRss:XML):void {
      this._xmlRss = xmlRss;
    }
    
    /**
    * Gets the xml rss
    * 
    * @return xml object (could be null)
    */
    protected function get xmlRss():XML {
      return this._xmlRss;
    }
    
    /**
    * Sets the xml config
    * 
    * @param xmlConfig object for xml config
    * */
    protected function set xmlConfig(xmlConfig:XML):void {
      this._xmlConfig = xmlConfig;
    }
    
    /**
    * Gets the xml config object
    * 
    * @return the xml config object (could be null)
    * */
    protected function get xmlConfig():XML {
      return this._xmlConfig;
    }
    
    // methods =================================================================
    /**
    * Override this method
    * 
    * @return ArrayCollection with AGSGptRSSRecord object elements (could be null)
    * */
    protected function parse():AGSGptRSSRecordList {
      return null;
    }
    
    /**
    * Creates the rssparser object
    * 
    * 
    * @param rss RSS XML.  If null or cannot be identified then exception thrown
    * @param config The configuration xml
    * @param endPoint end point associated with this parser
    * @return rssParser
    * 
    * */
    public static function createRssParser(rss:XML, agsGptModel:AGSGptRSSModel,
      endPoint:AGSGptRSSEndPoint):AGSGptRSSParser {
        
      var rssParser:AGSGptRSSParser;
      
      if(rss.name() == "rss" && Number(rss.@version) <= 2) {
		    rssParser = new AGSGptRSSParserGeo(rss, agsGptModel);
		  }
		  else if (rss.namespace().uri.toLowerCase() 
		     == "http://www.w3.org/2005/atom") {
		    rssParser = new AGSGptRSSParserAtom(rss, agsGptModel);               
		  }
		  if(rssParser == null) {
		    throw new Error("Unable to determine RSS feed type.");                
		  }  
		  rssParser.xmlRss = rss;
          rssParser.xmlConfig = agsGptModel.config;
          rssParser.endPoint = endPoint;
		  try {
		    rssParser.agsGptRssRecords = rssParser.parse();
		  } catch (e:Error) {
		    throw new Error("Could not parse RSS XML :" + e.message);
		  }
		  return rssParser;
    }
        
  }
}