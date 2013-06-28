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
package com.esri.gpt.utils
{
	import com.esri.ags.geometry.Extent;
	import com.esri.ags.geometry.Geometry;
	import com.esri.ags.geometry.MapPoint;
	
	import flash.utils.ByteArray;
	import flash.utils.Dictionary;
	
  /**
  * @Author: TM
  * Utility class
  * */  
  public class Utils
  {
    // constructors ===================================================
    /**
    Privatized since this is a util method (only statics)
    * */
    function Utils(){
    }
    // methods =========================================================
		/**
		 * Checks object.toString() to see if it is a valid number
		 * @param nObj  The object to be checked
		 * @param nDefault The default number 
		 * @return valid integer always
		 **/
		public static function chkNumber(nObj:Object, nDefault:Number):Number {
  	  if(nObj == null) {
  		  return nDefault;
  	  }
  	  try {
  	    return new Number(nObj.toString());
  	  } catch (error:Error) {
  		  trace(error.message);
  	  }
  	  return nDefault;
	  }
	  
	  /**
	  * Checks object.toString() and returns true false according to its value 
	  * @param obj Object to be checked
	  * @param defaultVal Default value
	  * 
	  * 
	  * @return valid boolean always
	  * 
	  * */
	  public static function chkBoolean(obj:Object, defaultVal:Boolean = false):Boolean {
      if(obj == null) {
    	  return defaultVal;
      }
      var sObj:String = obj.toString().toLowerCase();
      if(sObj == "true") {
    	  return true
      }
      if(sObj == "1" ) {
    	  return true;
      }
      if(sObj == "yes" ) {
    	  return true;
      }
      return defaultVal;
    }
    /**
    * Checks a string
    * @return trimmed string, never null
    **/
    public static function chkString(obj:Object):String {
      if(obj == null) {
        return "";
      }
      var strHelper:StringHelper = new StringHelper();
      return strHelper.trim(obj.toString());
   
    }
    
    public static function truncateString(str:String, maxLength:Number):String {
      if(maxLength <= 0 ) {
        return str;
      }
      str = chkString(str);
      if(str.length <= maxLength) {
        return str;
      } 
      return str.substr(0, maxLength -1 ) + "...";
      
    }
    
    /**
    * Sometimes the matches produce empty strings.  Making sure that does
    * not happen. It returns an array with items that do not have an empty 
    * string.
    * 
    * @return array with no empty strings & never null
    * 
    * */
    public static function chkStringMatch(arr:Array):Array {
      var obj:Object;
      var newArr:Array = new Array();
      if(arr == null) {
        return newArr;
      }
      
     for(var i:Number = 0; i < arr.length; i++) {
        if (Utils.chkString(arr[i]) != "") {
            newArr.push(arr[i]);
        }
      }
            
      return newArr;
    }
    
	/**
	* Checks if number is a border number 
	* (MAX_VALUE, MIN_VALUE, INFINITY, NAN)
	* 
	* **/
	public static function isExtremeNumber(param:Number):Boolean {
	   return param == Number.MAX_VALUE || param == Number.MIN_VALUE || 
	     param == Number.NEGATIVE_INFINITY || param == Number.POSITIVE_INFINITY;
	}
	
	/**
	 * Gets the size of a dictionary object
	 * */
	public static function dictionarySize(dict:Dictionary):uint {
		if(dict == null) {
			return 0;
		}
		var i:int = 0;
		for (var key:* in dict) {
			i++;
	    }
	    return i; 
	}
	
	public static function isNumeric(obj:Object):Boolean {
		var i:Number = chkNumber(obj, Number.MAX_VALUE);
		var j:Number = chkNumber(obj, Number.MIN_VALUE);
		
		return i == j;
        
	}
	
	public static function clone(source:Object):*
  {
      var myBA:ByteArray = new ByteArray();
      myBA.writeObject(source);
      myBA.position = 0;
      return(myBA.readObject());
  }
 
  
  /**
  * Some geometries do not have extents (e.g. map point, or extents
  * with same maxx maxy, so will make so 
  * */
  public static function massageExtent(geom:Geometry):Extent {
    if(geom.extent != null) {
      return geom.extent;
    }
    var extent:Extent = null;
    var mapPoint:MapPoint = geom as MapPoint;
    if(mapPoint != null) {
      extent = new Extent(mapPoint.x, mapPoint.y, mapPoint.x - 0.0001, 
        mapPoint.y - 0.0001, geom.spatialReference);
      return extent;
    }
    var tmpExtent:Extent = geom as Extent;
    if(tmpExtent != null) {
      // Has happened when xmin == xmax and ymin == ymax
      extent = new Extent(tmpExtent.xmax, tmpExtent.ymin, 
        tmpExtent.xmin - 0.0001, 
        tmpExtent.ymin - 0.0001, geom.spatialReference);
      return extent;
    }
    
    return extent;
  }
}
}