/* See the NOTICE file distributed with

* this work for additional information regarding copyright ownership.

* Esri Inc. licenses this file to You under the Apache License, Version 2.0

* (the "License"); you may not use this file except in compliance with

* the License.  You may obtain a copy of the License at

*

*     http://www.apache.org/licenses/LICENSE-2.0

*

* Unless required by applicable law or agreed to in writing, software

* distributed under the License is distributed on an "AS IS" BASIS,

* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

* See the License for the specific language governing permissions and

* limitations under the License.

*/
package com.esri.gpt.finddata
{
  import com.esri.ags.SpatialReference;
  import com.esri.ags.geometry.Geometry;
  import com.esri.ags.geometry.MapPoint;
  import com.esri.gpt.utils.Utils;
  
  import mx.collections.ArrayCollection;
  
  /**
  * @Author TM
  * 
  * Bean representing Georss record
  * */
  [Bindable]
  public class AGSGptRSSRecord
  {
    
    // instance variables ======================================================
    /** The title */
    private var _title:String;
    /** The abstract */
    private var _abstract:String;
    /** The id */
    private var _id:String;
    /** The map service url */
    private var _mapServiceUrl:String;
    /** The geometry */
    private var _geom:Geometry;
    /** The links */
    private var _links:ArrayCollection;
    /** Type of serviceType */    
    private var _serviceType:AGSGptEnumServiceTypes;
	  /** Index **/
	  private var _index:int;
    /** **/
    private var _hasDefaultGeometry:Boolean = true;
    
    // constructors ============================================================
    public function AGSGptRSSRecord(){
    }
    
    // properties ==============================================================
   
    
    /**
    * if geometry is generated from the code and not the rss record
    * then this will show true. False otherwise.
    * 
    * @return a default geometry
    * */
    public function get hasDefaultGeometry():Boolean {
      return _hasDefaultGeometry;
    }
    /**
    * Sets the title
    * @param title (can be null or empty)
    * */
    public function set title(title:String):void {
      this._title = title;
    }
    /**
    * Gets the title
    * @return title (trimmed, never null)
    * */
    public function get title():String {
      return Utils.chkString(this._title);
    }
    
    /**
    * Sets the Abstract
    * @param abstract (can be null or empty)
    * */
    public function set abstract(abstract:String):void {
      this._abstract = abstract;
    }
    /**
    * Gets the Abstract
    * @return abstract (trimmed, never null)
    * */
    public function get abstract():String {
      return Utils.chkString(this._abstract);
    }
    
    /**
    * Sets the Geometry
    * @param geom (can be null or empty)
    * */
    public function set geom(geom:Geometry):void {
      this._geom = geom;
    }
    /**
    * Gets the Geometry
    * @return geometry (never null, if null recieved, then MapPoint(0,0,4326) is returned
    * */
    public function get geom():Geometry {
      if(this._geom == null) {
        _hasDefaultGeometry = true;
        var mapPoint:MapPoint = new MapPoint(0,0);
        mapPoint.spatialReference = new SpatialReference(4326);
        return mapPoint;
      }
      _hasDefaultGeometry = false;
      return this._geom;
    }
    
     /**
    * Sets the id
    * @param id (can be null or empty)
    * */
    public function set id(id:String):void {
      this._id = id;
    }
    /**
    * Gets the id
    * @return id (trimmed, never null)
    * */
    public function get id():String {
      if(Utils.chkString(this._id) == "") {
        var zeroDate:Date = new Date();
        this._id = zeroDate.getHours().toString() 
          + zeroDate.getMinutes().toString() 
          + zeroDate.getMilliseconds().toString();
      }
      return Utils.chkString(this._id);
    }
    
     /**
    * Sets the Map Service URL
    * @param mapServiceUrl (can be null or empty)
    * */
    public function set mapServiceUrl(mapServiceUrl:String):void {
      this._mapServiceUrl = mapServiceUrl;
    }
    /**
    * Gets the Map Service Url
    * @return mapServiceUrl (trimmed, never null)
    * */
    public function get mapServiceUrl():String {
      return Utils.chkString(this._mapServiceUrl);
    }
    
     /**
    * Sets the Links
    * @param links (can be null or empty)
    * */
    public function set links(links:ArrayCollection):void {
      this._links = links;
      if(this._links == null) {
        return;
      }
    }
    
    /**
    * Set the servicetype
    * 
    * @param The servicetype
    * */
    public function set serviceType(serviceType:AGSGptEnumServiceTypes):void {
      this._serviceType = serviceType;
    }
    
    /**
    * Get the servicetype
    * 
    * @return service type (Could be null)
    * */
    public function get serviceType():AGSGptEnumServiceTypes {
      return this._serviceType;
    }
    
    /**
    * Gets the Links key=label, value=url
    * 
    * @return links (never null)
    * */
    public function get links():ArrayCollection {
      if(this._links == null) {
        this._links = new ArrayCollection();
      }
      return this._links;
    }
    
    public function toString():String {
      var tmp:String = "Abstract: " + this.abstract + ",\n";
      tmp += "ID=" + this.id + ",\n";
      tmp += "TITLE=" + this.title + ",\n";
      tmp += "Map Service Url= " + this.mapServiceUrl + ",\n";
      tmp += "Geometry = " + this.geom + ",\n";
      var links:ArrayCollection = this.links;
      for (var key:Object in links) {
        tmp += "Link = { Key = " + key + "," + "Value = " + links[key] + "}\n";
      }
      return tmp;
    }

  }
}