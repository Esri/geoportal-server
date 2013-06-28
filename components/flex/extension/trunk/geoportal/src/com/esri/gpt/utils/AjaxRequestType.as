package com.esri.gpt.utils
{
	public class AjaxRequestType
	{
		private var item:String;
		public static var GET:AjaxRequestType = new AjaxRequestType("GET");
		public static var POST:AjaxRequestType = new AjaxRequestType("POST");
		
		public function AjaxRequestType(item:String=null)     
		{
			this.item = item;
		}
		public function toString():String
		{
			return this.item;
		}
	}
}