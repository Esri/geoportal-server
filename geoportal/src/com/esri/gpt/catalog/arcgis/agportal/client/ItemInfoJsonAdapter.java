/*
 * See the NOTICE file distributed with
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
package com.esri.gpt.catalog.arcgis.agportal.client;

import com.esri.gpt.catalog.arcgis.agportal.itemInfo.ESRI_ItemInformation;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Item Info JSON adapter.
 * NOTE! This is EXPERIMENTAL feature. It might be removed at any time in the future.
 */
public class ItemInfoJsonAdapter {
  
  /**
   * Transforms JSON object into item information.
   * @param jsonObject JSON object containing item information
   * @return item information
   * @throws JSONException if accessing JSON object fails
   */
  public ESRI_ItemInformation toItemInfo(JSONObject jsonObject) throws JSONException {
    ESRI_ItemInformation itemInfo = new ESRI_ItemInformation();
    itemInfo.setId(jsonObject.getString("id"));
    itemInfo.setTitle(jsonObject.getString("title"));
    itemInfo.setDescription(jsonObject.getString("description"));
    itemInfo.setSnippet(jsonObject.getString("snippet"));
    itemInfo.setUrl(jsonObject.getString("url"));
    itemInfo.setOwner(jsonObject.getString("owner"));
    itemInfo.setAccess(jsonObject.getString("access"));
    itemInfo.setCulture(jsonObject.getString("culture"));
    itemInfo.setType(jsonObject.getString("type"));
    itemInfo.setTypeKeywords(makeListFromJsonArray(jsonObject.getJSONArray("typeKeywords")));
    itemInfo.setTags(makeListFromJsonArray(jsonObject.getJSONArray("tags")));

    JSONArray extentArray = jsonObject.getJSONArray("extent");
    if (extentArray!=null && extentArray.length()==2) {
      String [] lowerCorner = Val.chkStr(extentArray.getJSONArray(0).toString()).replaceAll("^\\[|\\]$", "").split(",");
      String [] upperCorner = Val.chkStr(extentArray.getJSONArray(1).toString()).replaceAll("^\\[|\\]$", "").split(",");
      double minx = -180, miny = -90, maxx = 180, maxy = 90;
      if (lowerCorner!=null && lowerCorner.length==2) {
        minx = Val.chkDbl(lowerCorner[0], minx);
        miny = Val.chkDbl(lowerCorner[1], miny);
      }
      if (upperCorner!=null && upperCorner.length==2) {
        maxx = Val.chkDbl(upperCorner[0], maxx);
        maxy = Val.chkDbl(upperCorner[1], maxy);
      }
      itemInfo.setExtent(new Envelope(minx, miny, maxx, maxy));
    }
    
    String sModifiedDate = Val.chkStr(jsonObject.getString("modified"));
    itemInfo.setModifiedDate(formatDate(sModifiedDate));
    
    return itemInfo;
  }

  /**
   * Formats date.
   * @param dateAsStringHavingLong string having long numerical
   * @return date
   */
  private Date formatDate(String dateAsStringHavingLong) {
    try {
      Long lDate = Long.parseLong(dateAsStringHavingLong);
      return new Date(lDate);
    } catch (NumberFormatException ex) {
      return null;
    }
  }
  
  /**
   * Makes list of strings from JSON array.
   * @param array JSON array
   * @return list of strings
   * @throws JSONException if accessing JSON object fails
   */
  private List<String> makeListFromJsonArray(JSONArray array) throws JSONException {
    ArrayList<String> list = new ArrayList<String>();
    for (int i = 0; i < array.length(); i++) {
      String str = Val.chkStr(array.getString(i));
      if (str.length()>0) {
        list.add(str);
      }
    }
    return list;
  }
}
