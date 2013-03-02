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

import flash.events.Event;

public class GptRSSEvent extends Event
{
  
  // class variables ===========================================================
  public static const GPT_RSS_RECORD_CLICK:String = "GptRSSRecordClick";
  
  public static const GPT_RSS_RECORD_MOUSE_OVER:String = "GptRSSRecordMouseOver";
  
  public static const GPT_RSS_RECORD_MOUSE_OUT:String = "GptRSSRecordMouseOut";
  
  public static const GPT_RSS_RECORD_MOUSE_OUT_ALL:String = 
    "GptRSSRecordMouseOutAll";
  
  public static const GPT_RECORD_INDEX:String = "GptRecordIndex";

  public static const GPT_UNHIGHLIGHT_ALL_GRAPHICS:String = 
    "GptUnhighlightGraphics";
  
  public static const GPT_HIDE_ALL_GRAPHICS:String = "GptHideAllGraphics";
  
  public static const GPT_SHOW_ALL_GRAPHICS:String = "GptShowAllGraphics";
  
  public static const GPT_SEARCH_DONE:String = "GptSearchDone";
  
  public static const GPT_UNHIGHLIGHT_ALL:String = "GptUnhighlightAll";
  
  // instance variables ========================================================
  private var _dataObject:Object;
  
	// constructors ==============================================================
  public function GptRSSEvent(type:String, 
        dataObject:Object,
        bubbles:Boolean = false, 
        cancelable:Boolean = false) {
    super(type, bubbles, cancelable);
    _dataObject = dataObject;
    
    
  }
  
 // properties =================================================================
  public function set dataObject(dataObject:Object):void {
    _dataObject = dataObject;
  }
  
  public function get dataObject():Object {
    return _dataObject;
  }
}
}