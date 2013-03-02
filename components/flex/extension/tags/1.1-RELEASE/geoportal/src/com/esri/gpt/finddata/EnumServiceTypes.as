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
  

import flash.events.IEventDispatcher;


/**
 * Enumerator class for service endpoints.  Flex has no enums so this is
 * a simple workaround.  Do not use the constructor outside of this class
 * (flex has no private constructor)
 * */
public class EnumServiceTypes
{
  
  //class variables ==========================================================
  /** ArcIMS constant */
  public static const AIMS:EnumServiceTypes = 
    new EnumServiceTypes("AIMS");
  
  /** ArcGIS Server rest constant. Do not instantiate! */
  public static const AGS_REST:EnumServiceTypes = 
    new EnumServiceTypes("AGS_REST");
  
  /** ArcGIS Server SOAP constant. Do not instantiate!*/
  public static const AGS_SOAP:EnumServiceTypes = 
    new EnumServiceTypes("AGS_SOAP");
  
  /** WFS Service constant. Do not instantiate! */
  public static const WFS:EnumServiceTypes = 
    new EnumServiceTypes("WFS");
  
  /** WMS Service constant. Do not instantiate! */
  public static const WMS:EnumServiceTypes = 
    new EnumServiceTypes("WMS");
  
  /** WCS Service constant. Do not instantiate! */
  public static const WCS:EnumServiceTypes = 
    new EnumServiceTypes("WCS");
  
  /** RSS Service constant. Do not instantiate! */
  public static const RSS:EnumServiceTypes = 
    new EnumServiceTypes("RSS");
    
  public static const KML:EnumServiceTypes = 
    new EnumServiceTypes("KML");
    
  public static const SHAPE:EnumServiceTypes = 
    new EnumServiceTypes("SHAPE");     
    
  // instance variables ========================================================  
  private var _serviceType:String;
  
  // properties ==============================================================
  /**
  * Do not use this constructor outside of this class!
  * */
  public function EnumServiceTypes(serviceType:String) {
     this._serviceType = serviceType;
  }
  
  public function get serviceType():String {
  	return this._serviceType;
  }
  
  // methods ===================================================================
}
}