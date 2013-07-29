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
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Fetch AGP users
 */
public class AgpFetchUsersRequest {
  
  public List<AgpUser> execute(AgpConnection connection, String q) throws Exception {
    if (!q.isEmpty()) {
      return execute(connection, q, 1);
    } else {
      return new ArrayList<AgpUser>();
    }
  }
  
  private List<AgpUser> execute(AgpConnection connection, String q, long start) throws Exception {
    ArrayList<AgpUser> users = new ArrayList<AgpUser>();
    
    String sType = "application/x-www-form-urlencoded";
    String sUrl = connection.makeSharingUrl()+"/community/users";
    StringBuilder params = new StringBuilder("q="+q+"&f=json&start="+start);
    connection.appendToken(params);
    AgpProperties hdr = connection.makeRequestHeaderProperties();
    AgpClient client = connection.ensureClient();
    JSONObject jso = client.executeJsonRequest(sUrl,hdr,params,sType);
    
    if (jso.has("results")) {
      JSONArray results = jso.getJSONArray("results");
      for (int i=0; i<results.length(); i++) {
        JSONObject juser = results.getJSONObject(i);
        if (juser.has("username")) {
          AgpUser user = makeUser(juser);
          users.add(user);
        }
      }
    }
    
    if (jso.has("nextStart")) {
      long nextStart = -1;
      try {
        nextStart = jso.getLong("nextStart");
      } catch (JSONException ex) {}
      if (nextStart>0) {
        users.addAll(execute(connection, q, nextStart));
      }
    }
    
    return users;
  }
  
  public AgpUser getSelfUser(AgpConnection connection) throws Exception {
    
    String sType = "application/x-www-form-urlencoded";
    String sUrl = connection.makeSharingUrl()+"/community/self";
    StringBuilder params = new StringBuilder("f=json");
    connection.appendToken(params);
    AgpProperties hdr = connection.makeRequestHeaderProperties();
    AgpClient client = connection.ensureClient();
    JSONObject juser = client.executeJsonRequest(sUrl,hdr,params,sType);
    return makeUser(juser);
  }
  
  private AgpUser makeUser(JSONObject juser) throws JSONException {
    AgpUser user = new AgpUser();
    
    user.setUsername(juser.has("username")? juser.getString("username"): "");
    user.setFullName(juser.has("fullName")? juser.getString("fullName"): "");
    user.setAccess(juser.has("access")? juser.getString("access"): "");
    user.setRole(juser.has("role")? juser.getString("role"): "");
    user.setOrgId(juser.has("orgId")? juser.getString("orgId"): "");
    
    return user;
  }
  
  /**
   * Portal for ArcGIS user.
   */
  public static class AgpUser {
    private String username = "";
    private String fullName = "";
    private String access = "";
    private String role = "";
    private String orgId = "";

    /**
     * Gets user name.
     * @return user name
     */
    public String getUsername() {
      return username;
    }

    /**
     * Sets user name.
     * @param username user name 
     */
    public void setUsername(String username) {
      this.username = username;
    }

    /**
     * Gets user full name.
     * @return user full name
     */
    public String getFullName() {
      return fullName;
    }

    /**
     * Sets user full name.
     * @param fullName user full name
     */
    public void setFullName(String fullName) {
      this.fullName = fullName;
    }

    /**
     * Gets user access.
     * @return user access
     */
    public String getAccess() {
      return access;
    }

    /**
     * Sets user access.
     * @param access user access
     */
    public void setAccess(String access) {
      this.access = access;
    }

    /**
     * Gets user role.
     * @return user role
     */
    public String getRole() {
      return role;
    }

    /**
     * Sets user role.
     * @param role user role
     */
    public void setRole(String role) {
      this.role = role;
    }

    /**
     * Gets user OrgID
     * @return user OrgID
     */
    public String getOrgId() {
      return orgId;
    }

    /**
     * Sets user OrgID.
     * @param orgId user OrgID
     */
    public void setOrgId(String orgId) {
      this.orgId = orgId;
    }
  }
}
