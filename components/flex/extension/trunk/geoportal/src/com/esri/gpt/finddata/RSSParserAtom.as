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
import com.esri.ags.SpatialReference;

import mx.collections.ArrayCollection;
import mx.logging.ILogger;
import mx.logging.Log;
import mx.resources.IResourceManager;
import mx.resources.ResourceManager;


[ResourceBundle("gptStrings")]
public class RSSParserAtom extends RSSParser {
  
    // class variables =========================================================
    /** Class Logger **/
    private static const LOG:ILogger = Log.getLogger(
      "com.esri.gpt.flexviewer.widgets.AGSGptRSSParserAtom");
      
    // instance variables ======================================================
    private var elRecordName:String = "entry";
	  private var elAbstract:String = "summary";
		private var elBbox:String = "box";
		private var elLink:String = "link"
		private var elTitle:String = "title";
		private var elId:String = "id";
		private var configXML:XML;
    private var resourceManager:IResourceManager = ResourceManager.getInstance();
		
		private var ptnViewMetadata:RegExp = new RegExp(".*\/rest\/document.*");
		private var ptnViewDetails:RegExp = 
		  new RegExp(".*viewMetadataDetails.page.*");
	  private var ptnPreview:RegExp = new RegExp(".*livedata.*preview.page.*");
	  
	  private var arrAddToMapParsers:Array = new Array();
		
		
		
		
		// constructors ============================================================
    public function RSSParserAtom(rss:XML, agsGptModel:RSSModel) {
      super(rss, agsGptModel);
      arrAddToMapParsers.push(
        { pattern: ".*livedata.*preview.page.*url=", replaceString: "" },
				{ pattern: "&info=.*", replaceString: "" }
			);
      if(configXML) {
        var strTmp:String;
        
			  			  
      }
      this.configXML = agsGptModel.config;
    }
    
    // methods =================================================================
    /**
    * Parses the RSS XML
    * 
    * @return ArrayCollection with AGSGptRSSRecord elements
    * */
    override protected function parse():SearchResultRecordList {
       var ac:SearchResultRecordList = new SearchResultRecordList();
			 var entries:XMLList = this.xmlRss.children();
			 
       for each (var entryXML:XML in entries) {
		      if(entryXML.localName() == elRecordName) {
		        ac.addItem(createRecord(entryXML));
		      }
       }
       return ac;
    }
    
    /**
    * Parses xml record
    * 
    * @param xmlRecord The xml representing a record
    * */
    private function createRecord(xmlRecord:XML):SearchResultRecord {
      var gptRecord:SearchResultRecord = new SearchResultRecord();
      var id:String = "";
			var viewMetadataUrl:String = "";
			var viewDetailsUrl:String = "";
			var mapServiceType:String = "";
			var mapServiceUrl:String ="";
			
			var index:Number = 0;		
			for each (var recordEntry:XML in xmlRecord.children()) {
			  
			  var text:String  = recordEntry.text(); 
			  index = index + 1;
				if(recordEntry.localName() == elTitle) {
					gptRecord.title = text;
				} else if(recordEntry.localName() == elAbstract) {
					gptRecord.abstract = text;
				} else if(recordEntry.localName() == elBbox) {
					gptRecord.geom = this._agsGptModel.readBbox(text, 
					  new SpatialReference(4326));
				} else if(recordEntry.localName() == elId) {
					gptRecord.id = text;
				} else if(recordEntry.localName() == elLink ) {
				  var arrLinks:ArrayCollection= new ArrayCollection();
					var href:String = null;
					var attribs:XMLList = recordEntry.attribute("href");
					if(attribs != null || attribs.length() > 0) {
					     href = attribs[0];
					} 
					if(href != null && href.match(ptnViewMetadata)) {
					  arrLinks.addItem(new EndPoint(
              resourceManager.getString('gptStrings','finddata.lblViewMetadata'), 
              href, this.configXML, endPoint.linkParserId));
					} else if(href != null && href.match(ptnViewDetails)) {
					  arrLinks.addItem(new EndPoint(
              resourceManager.getString('gptStrings','finddata.lblViewDetails'), 
              href, this.configXML, endPoint.linkParserId));
					} else if(href != null && href.match(ptnPreview)) {
					  arrLinks.addItem(new EndPoint(
              resourceManager.getString('gptStrings','finddata.lblPreview'), 
              href, this.configXML, endPoint.linkParserId));
					} else {
					  arrLinks.addItem(new EndPoint(
              resourceManager.getString('gptStrings','finddata.lblOpen') + index, 
              href, this.configXML, endPoint.linkParserId));
					}
					gptRecord.links = arrLinks;
			  }
				
			}
			LOG.debug(gptRecord.toString());
      return gptRecord;  
    }
}
}