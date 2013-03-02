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
 
  /**
  * Event representing discovered endpoint
  * 
  * */
  public class EndPointEvent extends Event
  {
    
    // class variables =========================================================
    public static const EVENT_SEARCH_ENDPOINT:String = "eventSearchPoint";
      
    // instance variables ======================================================
    private var _agsGptRssEndpoint:EndPoint;   
    
    
    // constructor =============================================================
    public function EndPointEvent(
      type:String, agsRSSEndPoint:EndPoint, 
      bubbles:Boolean=false, cancelable:Boolean=false)
    {
      super(type, bubbles, cancelable);
      this._agsGptRssEndpoint = agsRSSEndPoint;
    }
    
    // properties ==============================================================
    /**
    * Get GPT end point
    * 
    * @return ags gpt rss end point (could be null)
    * */
    public function get agsGptRssEndpoint():EndPoint {
      return this._agsGptRssEndpoint;
    }
    
    /**
    * Sets the GPT Endpoint
    * 
    * @param agsGptRssEndpoint Gpt rss endpoint
    * */
    public function set agsGptRssEndpoint(
      agsGptRssEndpoint:EndPoint):void {
        
      this._agsGptRssEndpoint = agsGptRssEndpoint;
    }
    
    // methods =================================================================
    /**
    * Clone of this class
    * 
    * @returns cloned instance (never null)
    * */
    override public function clone():Event {
       var endPoint:EndPoint = agsGptRssEndpoint.clone();
       return new EndPointEvent(this.type, this.agsGptRssEndpoint,
         this.bubbles, this.cancelable);
    }
    
   
  }
}