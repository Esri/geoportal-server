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
package com.esri.gpt.wms
{
	import mx.messaging.channels.StreamingAMFChannel;
	
	public class WMSUrl
	{   
		private var _validUrl:Boolean = false;
		private var _url:String = "";
		private var _params:Array = null;
		public function WMSUrl(url:String)
		{
			if (url == null || url.length == 0 ||url.substring(0,6).toLowerCase() != "http://" )  _validUrl = false;
			_url = url;
			_validUrl = true;			
			_params = retreiveUrlParams();
		}
		public function get hasParameters():Boolean
		{
			return (_params.length > 0)
		}
		public function getParameterValue(pName:String):String
		{
			for each(var param:Object in _params)
			{
				var name:String = param[0];
				var value:String = param[1];
				if (name.toLocaleLowerCase()== pName.toLocaleLowerCase()) return value
			}
			return null;
		}
		
		
        public function get valid():Boolean
        {
        	return _validUrl
        }
        public function get url():String
        {
        	return _url;
        } 
        public function rootURL():String
        {   
        	if (!valid) return "";
        	var qmIndic:int = url.indexOf("?"); 
        	var slIndic:int = url.lastIndexOf("/");
        	// no '?' and no '/'
        	if ( qmIndic < 0 && slIndic < 0 ){
        	    return url   	
        	} 
        	// there's a '?' but no '/'
        	if ( qmIndic >= 0  && slIndic >= 0)
        	{   
        		// is the '/' at the end
        		if (slIndic == url.length -1){
        			_url = url.substr(0,url.length - 1)
        		}
        	    return url.substr(0,qmIndic)
        	}
        	if ( qmIndic >= 0  && slIndic < 0) {
        		 return url.substr(0,qmIndic)
        	}
        	// no '?' but there's a '/'
        	if ( qmIndic < 0  && slIndic >= 0)
        	   // is the '/' at the end
        	   if (slIndic == url.length -1 ) 
        	     return url.substr(0,url.length - 1)
        	     else 
        	     return url;      
        	return  ""
        }
        
        private function retreiveUrlParams():Array
        {
        	var parameters:Array = new Array();
        	var qmIndic:int = url.indexOf("?"); 
        	if (qmIndic < 0) return parameters;
        	var qvStr:String = url.substr(qmIndic+1);
        	if (qvStr.length  == 0) return parameters;
        	qvStr += "&";
           	do
        	{   
        		var amIndex:int = qvStr.indexOf("&");
        		var s:String = qvStr.substr(0,amIndex);
        		if (amIndex >= 0)
				{
        		    var parameter:Array = s.split("=");
        		    var param:Object = {
        			    name: parameter[0],
        			    value: parameter[1]
        		    };
        		    parameters.push(parameter);
				}
        		qvStr = qvStr.substr(amIndex+1);
        	} while (amIndex >= 0);       	
        	return parameters;
       }
       
   
 
  }
 }