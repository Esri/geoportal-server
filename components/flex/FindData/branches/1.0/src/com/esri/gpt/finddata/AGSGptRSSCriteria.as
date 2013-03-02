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

import com.esri.ags.geometry.Extent;
import com.esri.gpt.utils.Utils;
  

/**
 * @Author TM
 * 
 * */
 [Bindable]
public class AGSGptRSSCriteria
{
  // class variables ===========================================================
  /** value for any extent **/
  public static const EXTENT_ANY:String = "extentAny";
  
  /** value for intersecting extent */
  public static const EXTENT_INTERSECT:String = "extentIntersect";
  
  /** value for contains extent */
  public static const EXTENT_CONTAINS:String = "extentContains";
  
  // instance variables ========================================================
  /** search text */
  private var _searchText:String;
  
  /** live data **/
  private var _liveData:Boolean;

  /** extent to be used with extentOption **/
  private var _extent:Extent;
  
  /** the extent option */
  private var _extentOption:String;
  
  /** Maximum amount of records to display **/
  private var _numOfResultsPerPage:Number;
  
  /** Starting index **/
  private var _startIndex:Number;
  
  // constructors  =============================================================
  public function AGSGptRSSCriteria(){
  }
  
  // properties ================================================================
  /** Sets the search text 
  * 
  * @param searchText the search text to be used
  * **/
  
  public function set searchText(searchText:String):void {
    this._searchText = searchText
  }
  
  /**
  * Gets the search text
  * 
  * @ return trimmed string, never null
  * */
 
  public function get searchText():String {
    return Utils.chkString(this._searchText);
  }
  
  /**
  * Sets indicator for if only livedata is to be retrieved
  * 
  * @param liveData
  * */
  public function set liveData(liveData:Boolean):void {
    this._liveData = liveData;
  }
  
  /**
  * Get the live data
  * 
  * @return liveData default = false
  * */
  public function get liveData() : Boolean {
    return this._liveData;
  }
  
  /**
  * Sets the extent to be used with the query
  * 
  * @param extent the extent 
  * */
  public function set extent(extent:Extent):void {
    this._extent = extent;
  }
  
  /**
  * Gets the extent to be used with the query
  * 
  * @return extent Possibly null
  * */
  public function get extent() : Extent {
    return this._extent;
  }
  
  /**
  * Sets the maximum number of records
  * 
  * @param num The maximum number of recrods
  * */
  public function set numOfResultsPerPage(num:Number):void {
    this._numOfResultsPerPage = num;
  }
  
  /**
  * Gets the maximum number of records
  * 
  * @return max number of records.  10 by default.
  * */
  public function get numOfResultsPerPage():Number{
    if(this._numOfResultsPerPage < 1) {
      this._numOfResultsPerPage = 10;
    }
    return this._numOfResultsPerPage;
  }
  
  /**
  * gets the extent option
  * 
  * @return always one of the EXTENT_* class variables
  * */
  public function get extentOption() : String {
    var eOption:String = Utils.chkString(this._extentOption);
    if (eOption != EXTENT_ANY && eOption != EXTENT_CONTAINS && 
      eOption != EXTENT_INTERSECT) {
      eOption = EXTENT_ANY;   
    }
    return eOption;
  }
  
  /**
  * Sets the extent option
  * 
  * @param option Uses one of the class variables EXTENT_*
  * */
  
  public function set extentOption(option:String):void{
    this._extentOption = option;
  }
  
  /**
  * Sets the start index
  * 
  * @param startIndex The starting index
  * */
  public function set startIndex(startIndex:int):void {
    _startIndex = startIndex;
  }
  
  
  /**
  * Gets the start index
  * 
  * @return The start index
  * */
  public function get startIndex():int {
    if(_startIndex < 1) {
      _startIndex = 1;
    }
    return _startIndex;
  }
 
}
}