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
import mx.collections.ArrayCollection;

/**
 * Adapted to store pagination information also
 * 
 * */
public class AGSGptRSSRecordList extends ArrayCollection
{

// instance variables ==========================================================
private var _totalResults:int = int.MIN_VALUE;

private var _startIndex:int = int.MIN_VALUE;

private var _itemsPerPage:int = int.MIN_VALUE;

// constructors ================================================================
public function AGSGptRSSRecordList(source:Array=null, 
                                    totalResults:int=int.MIN_VALUE,
                                    startIndex:int=int.MIN_VALUE,
                                    itemsPerPage:int=int.MIN_VALUE)
{
  super(source);
  this._totalResults = totalResults;
  this._startIndex = startIndex;
  this._itemsPerPage = itemsPerPage;
}

// properties ==================================================================  
public function get totalResults():int {
  return _totalResults;
}

public function get startIndex():int {
  return _startIndex;  
}

public function get itemsPerPage():int {
  return _itemsPerPage;  
}

}
}