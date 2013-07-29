/*
 * Copyright 2013 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.agp.client;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Fetches user folders.
 */
public class AgpFetchFoldersRequest {
  public List<AgpFolder> execute(AgpConnection connection, String userId) throws Exception {
    ArrayList<AgpFolder> folders = new ArrayList<AgpFolder>();
    String sType = "application/x-www-form-urlencoded";
    String sUrl = connection.makeSharingUrl()+"/content/users/"+userId;
    StringBuilder params = new StringBuilder("f=json");
    connection.appendToken(params);
    AgpProperties hdr = connection.makeRequestHeaderProperties();
    AgpClient client = connection.ensureClient();
    JSONObject jso = client.executeJsonRequest(sUrl,hdr,params,sType);
    
    if (jso.has("folders")) {
      JSONArray jfolders = jso.getJSONArray("folders");
      for (int i=0; i<jfolders.length(); i++) {
        JSONObject jfolder = jfolders.getJSONObject(i);
        if (jfolder.has("id")) {
          AgpFolder folder = new AgpFolder();
          folder.setId(jfolder.getString("id"));
          folder.setTitle(jfolder.optString("title"));
          folders.add(folder);
        }
      }
    }
    
    return folders;
  }
  
  /**
   * Portal for ArcGIS user.
   */
  public static class AgpFolder {
    private String id = "";
    private String title = "";

    /**
     * Gets folder id.
     * @return folder id
     */
    public String getId() {
      return id;
    }

    /**
     * Sets folder id.
     * @param id folder id
     */
    public void setId(String id) {
      this.id = id;
    }

    /**
     * Gets folder title.
     * @return folder title
     */
    public String getTitle() {
      return title;
    }

    /**
     * Sets folder title.
     * @param title folder title
     */
    public void setTitle(String title) {
      this.title = title;
    }
    
  }
}
