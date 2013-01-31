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
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Search client.
 * NOTE! This is EXPERIMENTAL feature. It might be removed at any time in the future.
 */
public class SearchClient {
  private String url;
  
  /**
   * Creates instance of the search client.
   * @param url search URL.
   */
  public SearchClient(String url) {
    this.url = Val.chkStr(url);
  }
  
  /**
   * List all content.
   * @return search result
   * @throws IOException if accessing server fails
   */
  public SearchResult listAll() throws IOException {
    return search(new SearchParams("*:*", 100, 1));
  }
  
  /**
   * Invokes search.
   * @param params search parameters
   * @return search result
   * @throws IOException if accessing server fails
   */
  public SearchResult search(SearchParams params) throws IOException {
    if (params.getNum()<1) return null;
    if (params.getStart()<1) return null;
    
    ItemInfoJsonAdapter itemInfoAdapter = new ItemInfoJsonAdapter();
    HttpClientRequest cr = new HttpClientRequest();
    cr.setUrl(url + "/search?q=" +params.getQuery()+ "&f=json&num=" +params.getNum()+ "&start=" + params.getStart());
    cr.execute();
    
    try {
      List<ESRI_ItemInformation> infos = new ArrayList<ESRI_ItemInformation>();
      String json = cr.readResponseAsCharacters();
      JSONObject jso = new JSONObject(json);
      String nextStart = jso.getString("nextStart");
      JSONArray records = jso.getJSONArray("results");
      for (int idx = 0; idx < records.length(); idx++) {
          JSONObject record = records.optJSONObject(idx);
          ESRI_ItemInformation ii = itemInfoAdapter.toItemInfo(record);
          infos.add(ii);
      }
      return new SearchResult(new SearchParams(params.getQuery(), params.getNum(), Val.chkInt(nextStart, -1)), infos);
    } catch (JSONException ex) {
      throw new IOException("Error reading response.", ex);
    }
  }
  
  /**
   * Search result.
   */
  public final class SearchResult {
    private SearchParams nextParams;
    private List<ESRI_ItemInformation> items;
    
    /**
     * Creates instance of the search result.
     * @param nextParams search parameters for the next chunk of the search result
     * @param items current items
     */
    public SearchResult(SearchParams nextParams, List<ESRI_ItemInformation> items) {
      this.nextParams = nextParams;
      this.items = items;
    }
    
    /**
     * Gets items.
     * @return list of items
     */
    public List<ESRI_ItemInformation> getItems() {
      return items;
    }
    
    /**
     * Continues search beginning from the end of last search.
     * @return search result
     * @throws IOException 
     */
    public SearchResult next() throws IOException {
      return search(nextParams);
    }
  }

  /**
   * Search parameters.
   */
  public final class SearchParams {
    private String q;
    private int    num;
    private int    start;
    
    /**
     * Creates instance of search parameters.
     * @param q query string
     * @param num number of records to fetch
     * @param start number of the first record to fetch
     */
    public SearchParams(String q, int num, int start) {
      this.q = Val.chkStr(q);
      this.num = num;
      this.start = start;
    }
    
    /**
     * Gets query string.
     * @return query string
     */
    public String getQuery() {
      return q;
    }
    
    /**
     * Gets number of records to fetch.
     * @return number of records to fetch
     */
    public int getNum() {
      return num;
    }
    
    /**
     * Gets number of the first record to fetch.
     * @return number of the first record to fetch
     */
    public int getStart() {
      return start;
    }
  }
}
