package com.esri.gpt.utils
{
	import flash.external.ExternalInterface;
	public class Ajax
	{
	
		private var _url:String = "";
		private var _async:Boolean=false;
		private var _responseText:String;
		private var _requestType:AjaxRequestType;
		
		public function Ajax(url:String)
		{
			this._url = url;
		}
		public function get async():Boolean
		{
			return this._async;
		}
		public function set async(value:Boolean):void
		{
			this._async = value; 
		}
		public function get responseText():String
		{
			return this._responseText;
		}
		public function get requestType():AjaxRequestType
		{
			return this._requestType;
		}
		public function set requestType(value:AjaxRequestType):void
		{
			this._requestType = value;   
		}  
		
		public function send(data:String=null):String
		{
			var sendingData:String = sendingData(data);
			this._responseText = ExternalInterface.call(sendingData);
			return this._responseText;
		}
		
		private function sendingData(data:String=null):String
		{
			if(ExternalInterface.available)
			{
				var data:String = "function()" + 
					"{" + 
					"var xmlHttp;"+
					"try" + 
					"{"+             
					" xmlHttp = new ActiveXObject('Msxml2.XMLHTTP');" +                                     
					"}"+
					"catch(e)" + 
					"{" + 
					"try" + 
					"{" + 
					"xmlHttp=new ActiveXObject('Microsoft.XMLHTTP');" + 
					"}" + 
					"catch(oc)" + 
					"{" + 
					"xmlHttp=null;" + 
					"}" +             
					"}" + 
					"if(!xmlHttp && typeof XMLHttpRequest != 'undefined')" + 
					"{" + 
					"xmlHttp=new XMLHttpRequest();" + 
					"}"+
					"try" + 
					"{" + 
					"xmlHttp.open('"+ requestType.toString() + "','" + _url + "'," + async + ");"+
					"xmlHttp.send("+ data + ");"+
					"return xmlHttp.responseText;" +            
					"}"+
					"catch(x){alert(x)}" +         
					"}";
				
				return data; 
			}
			else
			{
				throw new Error("This browser is not supported.");
			}      
		}    
	}
}