
	////////////////////////////////////////////////////////////////////////////////
	//
	// Version 1.0 - Jan 25, 2011
	//
	// Delevoped by Robert Scheitlin
	//
	//////////////////////////////////////////////////////////////////////////////// 
package widgets.Route
{
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.controls.Image;
	import mx.controls.TextInput;
	import mx.core.mx_internal;
	import mx.events.FlexEvent;
	
	use namespace mx_internal;
	
	[Event(name="searchClick", type="flash.events.Event")]
	
	public class SearchTextInput extends TextInput
	{
		private var _image:Image;
		
		[Bindable]
		private var _searchToolTip:String = "Search";
		
		public function set searchToolTip(value:String):void
		{
			_searchToolTip = value;
		}
		
		public function SearchTextInput()
		{
			super();
		}
		
		override protected function createChildren():void
		{
			super.createChildren();
			
			_image = new Image();
			_image.source = "assets/images/searchicon.png";
			_image.useHandCursor = true;
			_image.buttonMode = true;
			_image.addEventListener(MouseEvent.CLICK, searchClicked);
			_image.toolTip = _searchToolTip;
			this.addChild(DisplayObject(_image));
		}
		
		protected function searchClicked(event:Event):void
		{
			dispatchEvent(new Event("searchClick"));
		}
		
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
		{
			super.updateDisplayList(unscaledWidth, unscaledHeight);
			
			_image.width = 15;
			_image.height = 17;
			
			_image.x = this.width - this._image.width - 5;
			_image.y = (this.height - this._image.height) / 2;
			
			textField.width = this.width - this._image.width - 5;
		}
	}
}