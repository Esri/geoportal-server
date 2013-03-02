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
  public class RSSParser
  {
    // instance variables ======================================================
    /** The RSS record objects */
    private var _agsGptRssRecords:SearchResultRecordList;
    
    /** The XML RSS Object */
    private var _xmlRss:XML;
    
    /** The xml config object */
    private var _xmlConfig:XML;
    
    protected  var _agsGptModel:RSSModel;
    /**  Endpoint **/
    private var _endPoint:EndPoint;
    // constructors ============================================================
    /**
    * Dont use this constructor!  Use the method create
    * 
    * 
    * @param rss RSS XML.  If null or cannot be identified then exception thrown
    * @param config The configuration xml
    * @throws Error if rss is null or cannot be identified
    * */
    public function RSSParser(rss:XML,agsGptModel:RSSModel) {
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
    public function set endPoint(endPoint:EndPoint):void {
      this._endPoint = endPoint;
    }
    
    /**
    * Gets the end point
    * 
    * */
    public function get endPoint():EndPoint {
      return this._endPoint;
    }
       
    /**
    * Sets the rss record objects
    * 
    * @param records
    * */
    public function set agsGptRssRecords(records:SearchResultRecordList):void {
      this._agsGptRssRecords = records;
    }
    
    /**
    * Gets the rss record objects
    * 
    * @return th rss record objects possibly null
    * */
    public function get agsGptRssRecords():SearchResultRecordList {
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
    protected function parse():SearchResultRecordList {
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
    public static function createRssParser(rss:XML, agsGptModel:RSSModel,
      endPoint:EndPoint):RSSParser {
        
      var rssParser:RSSParser;
      
      if(rss.name() == "rss" && Number(rss.@version) <= 2) {
		    rssParser = new RSSParserGeo(rss, agsGptModel);
		  }
		  else if (rss.namespace().uri.toLowerCase() 
		     == "http://www.w3.org/2005/atom") {
		    rssParser = new RSSParserAtom(rss, agsGptModel);               
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