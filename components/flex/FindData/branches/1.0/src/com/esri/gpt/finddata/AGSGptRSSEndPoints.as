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
  /**
  * @Author TM
  * 
  * Class aggregates the points available to the app
  * 
  * */
  import com.esri.gpt.utils.Utils;
  
  import flash.events.Event;
  import flash.events.EventDispatcher;
  
  import mx.logging.ILogger;
  import mx.logging.Log;
  import mx.rpc.AsyncResponder;
  import mx.rpc.AsyncToken;
  import mx.rpc.events.FaultEvent;
  import mx.rpc.events.ResultEvent;
  import mx.rpc.http.HTTPService;
  
  [Event(name="eventSearchPoint", 
     type="com.esri.gpt.flexviewer.widgets.AGSGptRSSEndPointEvent")]
  public class AGSGptRSSEndPoints extends EventDispatcher
  {
    // class variables =========================================================
    private static const LOG:ILogger = 
      Log.getLogger("com.esri.gpt.flexviewer.widgets.AGSGptRSSEndPoints");
      
    // instance variables ======================================================
    //private var _xmlConfig:XML;
    private var _agsGptModel:AGSGptRSSModel;
    // constructor =============================================================
    public function AGSGptRSSEndPoints(agsGptModel:AGSGptRSSModel)
    {
      //this._xmlConfig = agsGptRSSEndPoint.config; 
      this._agsGptModel = agsGptModel;
    }
    
    // methods =================================================================
    /**
    * Initiatest read end points and the dispatcher
    * 
    * **/
    public function readEndpoints():void
    {
      var endPointList:XMLList = this._agsGptModel.config.gptEndpoint;
      var sRegEndpointList:String = Utils.chkString(this._agsGptModel.config.regEndpointList);
      var url:String;
      var endPoint:AGSGptRSSEndPoint;
      var event:Event;
      var httpService:HTTPService;
     
	   
	    for each (var xmlEndPoint:XML in endPointList) 
	    {
	      try {
	        url = Utils.chkString(xmlEndPoint.@url);
	    	    
		      endPoint = new AGSGptRSSEndPoint(xmlEndPoint.@name, 
		        url, this._agsGptModel.config, xmlEndPoint.@linkParserId, 
		        Utils.chkBoolean(xmlEndPoint.@useNameAsPrefix, false));
		      var tmp:String = xmlEndPoint.@labelPrefix;
		   
		      if(!endPoint.readIsRepisotoryListUrl()) {
		        event = new AGSGptRSSEndPointEvent(
		          AGSGptRSSEndPointEvent.EVENT_SEARCH_ENDPOINT, endPoint);
		        this.dispatchEvent(event);
		      } else {
		        httpService = this.readHttpService();
		        httpService.resultFormat = "e4x";
		      //httpService.requestTimeout = this.requestTimeout;
	          var token:AsyncToken = this._agsGptModel.connect(url, httpService); 
	          token.endPoint = endPoint;
	          token.addResponder( new AsyncResponder( readRepositoryList, 
	            repositoryListError, token));
		      
		      }
	      }catch(err:Error) {
	        LOG.error("Could parse endpoint " + err.toString() + "\n\n"
	        + xmlEndPoint);
	      }
	    }
    }
    
    /**
    * Reads the external search list urls
    * 
    * @param event The resulting event from the http call
    * */
    private function readRepositoryList(event:ResultEvent, 
      token:AsyncToken = null):void 
    {
      var xml:XML = event.result as XML;
      var id:String;
      var name:String;
      var prefix:String;
      var parentEndpoint:AGSGptRSSEndPoint = token.endPoint;
      var parentUrl:String = 
        (parentEndpoint != null)? parentEndpoint.siteUrl: "";
      var parentLinkParserId:String = 
        (parentEndpoint != null)? parentEndpoint.linkParserId: "";
      var evt:AGSGptRSSEndPointEvent = null;
      var endPoint:AGSGptRSSEndPoint = null;
      
      if(xml == null) {
        return;
      }
      
     for each (var xmlRow:XML in xml.children()) {
        try {
          id = xmlRow.uuid;
          name = xmlRow.name;
          endPoint = new AGSGptRSSEndPoint(name, parentEndpoint.readRepositoryUrl(id), 
            this._agsGptModel.config, parentEndpoint.linkParserId, 
            parentEndpoint.useParentNameAsPrefix, parentEndpoint.siteName);
          evt = new AGSGptRSSEndPointEvent(
		        AGSGptRSSEndPointEvent.EVENT_SEARCH_ENDPOINT, endPoint);
		      this.dispatchEvent(evt);
        } catch (err:Error) {
          LOG.warn("Could not extract id, name from endpoint parent url= "
             + parentUrl + "\n\n" + err.toString());
        }
      }
      
    }
    
    /**
    * If there is an error reading the repository list then this will catch
    * the error
    * 
    * @param event The fault event
    * */
    private function repositoryListError(event:FaultEvent, 
      token:AsyncToken):void {
        LOG.error("Could not connection to endpoint" + event.message
          +  event.toString());
      
    }
    
   
    
    	/**
		 * Read the httpservice object
		 * 
		 * @return the object
		 * */
		private function readHttpService():HTTPService {
		  var httpService:HTTPService = new HTTPService();
		  return httpService;
		}

  }
}