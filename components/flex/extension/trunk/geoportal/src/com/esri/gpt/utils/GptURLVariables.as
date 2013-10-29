package com.esri.gpt.utils
{
	import flash.net.URLVariables;
	
	/**
	 * Adobe URLVariables was throwing exceptions.  Reimplementing
	 * 
	 * */
	
	public dynamic class GptURLVariables extends URLVariables
	{
		public function GptURLVariables(source:String=null)
		{
			super(source); 
		}
		
		override public function decode(params:String):void {
			params = Utils.chkString(params);
			var keyValueParams:Array = params.split("&");	
			for(var i:int = 0; i < keyValueParams.length; i++) {
				var keyValueParam:String = keyValueParams[i];
				var arrKeyValueParam:Array = keyValueParam.split("=");
				if(arrKeyValueParam.length > 1) {
					this[Utils.chkString(arrKeyValueParam[0])] = unescape(Utils.chkString(arrKeyValueParam[1]));
				}
			}
 
		}
	}
}