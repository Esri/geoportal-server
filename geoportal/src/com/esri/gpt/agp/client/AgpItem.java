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
package com.esri.gpt.agp.client;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An ArcGIS portal item.
 */
public class AgpItem {
  
  /** instance variables ====================================================== */
  private AgpProperties properties = new AgpProperties();
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpItem() {}

  /** properties ============================================================== */
  
  /**
   * Gets the item properties.
   * @return the item properties
   */
  public AgpProperties getProperties() {
    return this.properties;
  }
  /**
   * Sets the item properties.
   * @param properties the item properties
   */
  public void setProperties(AgpProperties properties) {
    this.properties = properties;
  }
  
  /** methods ================================================================= */
  
  /**
   * Parse the JSON response.
   * @param jsoItem the JSON item
   * @throws Exception if an exception occurs
   */
  public void parseItem(JSONObject jsoItem) throws JSONException {
    AgpProperties props = this.getProperties();
    
    /* Just at the time of initial rest api publication.
    id                  The unique id for this item.
    owner               The username of the user who owns this item.
    created             The date the item was created. Shown in UNIX time in milliseconds.
    modified            The date the item was last modified. Shown in UNIX time in milliseconds.
    name                The file name of the item for file types. Read-only.
    title               The title for the item. This is the name that is displayed to users and by which they refer to the item. Every item must have a title.
    url                 The url for the resource represented by the item. Applies only to items that represent web accessible resources such as map services.
    type                The gis content type of this item. Example types include : "Web Map", "Map Service",  "Shapefile" and "Web Mapping Application".
    typeKeywords        A set of keywords that further describes the type of this item. Each item is tagged with a set of type keywords that are derived based on its primary type.
    description         Item description.
    tags                User defined tags that describe the item.
    snippet             A short summary description of the item.
    thumbnail           The url to the thumbnail used for the item.
    extent              The bounding rectangle of the item. Should always be in WGS84.
    spatialReference    The coordinate system of the item.
    accessInformation   Information on the source of the item.
    licenseInformation  Any license information or restrictions.
    culture             The item locale information (language and country).
    access              Indicates the level of access to this item: private, shared, org, public.
    size                The size of the item.
    numComments         Number of comments on the item.
    numRatings          Number of ratings on the item.
    avgRating           Average rating. Uses a weighted average called "Bayesian average".
    numViews            Number of views on the item.
   
    licenseInfo -> licenseInformation
    
    String[] aPropsTransfer = {
        "itemType",
        "type",
        "url",
        "text",
        "title",
        "description",
        "snippet",
        "accessInformation",
        "licenseInfo",
        "culture",
        "tags",
        "extent"
      };
      
    */
    
    String[] aProps = {
      "id",
      "owner",
      "item",
      "itemType",
      "type",
      "url",
      "text",
      "uploaded",
      "modified",
      "title",
      "description",
      "snippet",
      "thumbnail",
      "thumbnailurl",
      "metadata",
      "accessInformation",
      "licenseInfo",
      "culture",
      "access",
      "size",
      "numComments",
      "numRatings",
      "avgRating",
      "numViews",
      
      "documentation",
      "guid",
      "name",
      "lastModified",
    };
    for (String sProp: aProps) {
      if (jsoItem.has(sProp) && (!jsoItem.isNull(sProp))) {
        props.add(new AgpProperty(sProp,jsoItem.getString(sProp)));
      }
    }
    
    String[] aDelimitedProps = {
      "typeKeywords",
      "tags"
    };
    for (String sProp: aDelimitedProps) {
      if (jsoItem.has(sProp) && (!jsoItem.isNull(sProp))) {
        StringBuilder sb = new StringBuilder();
        JSONArray jsoValues = jsoItem.getJSONArray(sProp);
        int n = jsoValues.length();
        for (int i=0;i<n;i++) {
          if (sb.length() > 0) sb.append(",");
          sb.append(jsoValues.getString(i));
        }
        if (sb.length() > 0) {
          props.add(new AgpProperty(sProp,sb.toString()));
        }                
      }
    }
    
    if (jsoItem.has("extent") && (!jsoItem.isNull("extent"))) {
      JSONArray jsoExtent = jsoItem.getJSONArray("extent");
      if (jsoExtent.length() == 2) {
        JSONArray jsoLL = jsoExtent.getJSONArray(0);
        JSONArray jsoUR = jsoExtent.getJSONArray(1);
        String xmin = jsoLL.getString(0);
        String ymin = jsoLL.getString(1);
        String xmax = jsoUR.getString(0);
        String ymax = jsoUR.getString(1);
        String sExtent = xmin+","+ymin+","+xmax+","+ymax;
        props.add(new AgpProperty("extent",sExtent));
        
        String sProp = "spatialReference";
        if (jsoItem.has(sProp) && (!jsoItem.isNull(sProp))) {
          props.add(new AgpProperty(sProp,jsoItem.getString(sProp)));
        }
      }
    }
  }
  
}