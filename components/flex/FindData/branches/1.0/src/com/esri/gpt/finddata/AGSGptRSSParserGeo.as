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
  import com.esri.gpt.utils.Namespaces;
  import com.esri.gpt.utils.Utils;
  
  import mx.collections.ArrayCollection;
  import mx.logging.ILogger;
  import mx.logging.Log;
  import mx.utils.object_proxy;
  
  /**
  * Parses the GeoRSS feed
  * */
  public class AGSGptRSSParserGeo extends AGSGptRSSParser {
    // class variables =========================================================
    /** The Georss namespace**/
    private static const GEORSS : Namespace = Namespaces.GEORSS_NS;
    
    private static const OPENSEARCH : Namespace = Namespaces.OPENSEARCH_11;
       
    /** Class Logger **/
    private static const LOG:ILogger = Log.getLogger(
      "com.esri.gpt.flexviewer.widgets.AGSGptRSSParserGeo");
    
    /** Configuration XML **/  
    private var configXML:XML;  
    
    // constructors ============================================================
    /**
    * 
    * @param rss The RSS XML
    * @param config The configuration XML
    * */
    public function AGSGptRSSParserGeo(rss:XML, agsGptModel:AGSGptRSSModel) {
      super(rss, agsGptModel);
      this.configXML = agsGptModel.config;
    }
    
    // methods =================================================================
      /**
    * Parses the RSS XML
    * 
    * @return ArrayCollection with AGSGptRSSRecord elements
    * */
    override protected function parse():AGSGptRSSRecordList {
       
			 var x:XML = this.xmlRss;
			 var sTmp:String = x.channel.totalResults;
       if(sTmp == null || sTmp == "") {
         sTmp = x.*::channel.*::totalResults;
       }
       var totalResults:int = Utils.chkNumber(sTmp, int.MIN_VALUE);
       
       sTmp = x.channel.startIndex;
       if(sTmp == null || sTmp == "") {
         sTmp = x.*::channel.*::startIndex;
       }
       var startIndex:int = Utils.chkNumber(sTmp, int.MIN_VALUE);
       
       sTmp = x.channel.itemsPerPage;
       if(sTmp == null || sTmp == "") {
         sTmp = x.*::channel.*::itemsPerPage;
       }
       var itemsPerPage:int = Utils.chkNumber(sTmp, int.MIN_VALUE);
       
       var ac:AGSGptRSSRecordList = new AGSGptRSSRecordList(null,
         totalResults, startIndex, itemsPerPage);
       
       for each (var xmlRecord:XML in x.channel.item) {
         ac.addItem(createRecord(xmlRecord));
		   }
       return ac;
    }
    
    /**
    * Creates record
    * 
    * @param xmlRecord The xml record
    * */
    private function createRecord(xmlRecord:XML):AGSGptRSSRecord {
      if (xmlRecord == null) {
        return null;
      }
      var record:AGSGptRSSRecord = new AGSGptRSSRecord();
      var tmp:String = xmlRecord.title;
      record.title = tmp; 
      var descr:String = xmlRecord.description;
      record.geom = GeoRSSUtil.toGeometry(xmlRecord);
      if(record.geom.spatialReference == null) {
        record.geom.spatialReference = new SpatialReference(4326);
      }
      if(descr == null) {
        return record;
      }
      record.id = xmlRecord.guid.text();
      try {
        xmlRecord = new XML(descr);
        tmp = xmlRecord.div.(@["class"] == "abstract").text();
        record.abstract = tmp;
        var xmlList:XMLList = null;
        var xml:XML = null;
        try {
          xmlList = xmlRecord.div.(@["class"] == "links").A;
        } catch(err1:Error) {
          
        }
        try {
          if(xmlList == null) {
            xml = xmlRecord.div.(@["class"] == "links").A;
          }
        }catch(err2:Error) {
            
        }
       
        var arrLinks:ArrayCollection = new ArrayCollection();
        if(xmlList != null) {
          var item:XML;
          for each(item in xmlList) {
            arrLinks.addItem(new AGSGptRSSEndPoint(item.text(), item.@HREF,
              this.configXML, endPoint.linkParserId));
          }
          
        } else if (xml != null) {
          arrLinks.addItem(new AGSGptRSSEndPoint(xml.text(), xml.@HREF,
            this.configXML, endPoint.linkParserId));
        }
        record.links = arrLinks;
 
      } catch(err:Error) {
        LOG.error("Could not create AGS Gpt RSS Record. " + descr + ":" +
         err.message + " : \n" + err.getStackTrace());
      }
      LOG.debug(record.toString());
      
      return record;
    }
    
  }
}