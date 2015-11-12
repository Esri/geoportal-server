/*
 * Copyright 2015 Esri.
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
package com.esri.gpt.framework.ckan;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Package adaptor.
 */
/*package*/final class CkanPackageAdaptor extends CkanObjectImpl implements CkanPackage {
  private static final Logger LOG = Logger.getLogger(CkanPackageAdaptor.class.getName());

  public CkanPackageAdaptor(JSONObject packageJson) {
    super(packageJson);
  }

  @Override
  public String getId() {
    return getString("id", null);
  }

  @Override
  public String getTitle() {
    return getString("title", null);
  }

  @Override
  public Date getCreateDate() {
    return getDate("metadata_created", null);
  }

  @Override
  public Date getUpdateDate() {
    return getDate("metadata_modified", null);
  }

  @Override
  public String getNotes() {
    return getString("notes", null);
  }

  @Override
  public String getAuthor() {
    return getString("author", null);
  }

  @Override
  public List<CkanResource> getResources() {
    String packageId = getId();
    ArrayList<CkanResource> resources = new ArrayList<CkanResource>();
    if (json.has("resources")) {
      try {
        JSONArray resArray = json.getJSONArray("resources");
        for (int i=0; i<resArray.length(); i++) {
          JSONObject resObj = resArray.getJSONObject(i);
          resources.add(new CkanResourceAdaptor(this,resObj));
        }
      } catch (JSONException ex) {
        LOG.log(Level.WARNING,"Invalid resource list format", ex);
      }
    }
    return resources;
  }
  
  @Override
  public String getLicenseTitle() {
    return getString("license_title", null);
  }

  @Override
  public String getMaintainer() {
    return getString("maintainer", null);
  }

  @Override
  public List<CkanRelationship> getRelationshipsAsObject() {
    ArrayList<CkanRelationship> relationships = new ArrayList<CkanRelationship>();
    if (json.has("relationships_as_object")) {
      try {
        JSONArray arr = json.getJSONArray("relationships_as_object");
        for (int i=0; i<arr.length(); i++) {
          JSONObject obj = arr.getJSONObject(i);
          relationships.add(new CkanRelationshipAdaptor(obj));
        }
      } catch (JSONException ex) {
        LOG.log(Level.WARNING,"Invalid tags list format", ex);
      }
    }
    return relationships;
  }

  @Override
  public List<CkanRelationship> getRelationshipsAsSubject() {
    ArrayList<CkanRelationship> relationships = new ArrayList<CkanRelationship>();
    if (json.has("relationships_as_subject")) {
      try {
        JSONArray arr = json.getJSONArray("relationships_as_subject");
        for (int i=0; i<arr.length(); i++) {
          JSONObject obj = arr.getJSONObject(i);
          relationships.add(new CkanRelationshipAdaptor(obj));
        }
      } catch (JSONException ex) {
        LOG.log(Level.WARNING,"Invalid tags list format", ex);
      }
    }
    return relationships;
  }

  @Override
  public Boolean getPrivate() {
    return getBoolean("private", null);
  }

  @Override
  public String getMaintainerEmail() {
    return getString("maintainer_email", null);
  }

  @Override
  public Long getNumTags() {
    return (long)getTags().size();
  }

  @Override
  public String getAuthorEmail() {
    return getString("author_email", null);
  }

  @Override
  public String getState() {
    return getString("state", null);
  }

  @Override
  public String getVersion() {
    return getString("version", null);
  }

  @Override
  public String getCreatorUserId() {
    return getString("creator_user_id", null);
  }

  @Override
  public String getType() {
    return getString("type", null);
  }

  @Override
  public Long getNumResources() {
    return (long)getResources().size();
  }

  @Override
  public List<CkanTag> getTags() {
    ArrayList<CkanTag> tags = new ArrayList<CkanTag>();
    if (json.has("tags")) {
      try {
        JSONArray arr = json.getJSONArray("tags");
        for (int i=0; i<arr.length(); i++) {
          JSONObject obj = arr.getJSONObject(i);
          tags.add(new CkanTagAdaptor(obj));
        }
      } catch (JSONException ex) {
        LOG.log(Level.WARNING,"Invalid tags list format", ex);
      }
    }
    return tags;
  }

  @Override
  public List<CkanGroup> getGroups() {
    ArrayList<CkanGroup> groups = new ArrayList<CkanGroup>();
    if (json.has("groups")) {
      try {
        JSONArray arr = json.getJSONArray("groups");
        for (int i=0; i<arr.length(); i++) {
          JSONObject obj = arr.getJSONObject(i);
          groups.add(new CkanGroupAdaptor(obj));
        }
      } catch (JSONException ex) {
        LOG.log(Level.WARNING,"Invalid groups list format", ex);
      }
    }
    return groups;
  }

  @Override
  public String getLicenseId() {
    return getString("license_id", null);
  }

  @Override
  public String getOrganization() {
    return getString("organization", null);
  }

  @Override
  public String getName() {
    return getString("name", null);
  }

  @Override
  public Boolean getIsOpen() {
    return getBoolean("isopen", null);
  }

  @Override
  public URL getUrl() {
    return getUrl("url", null);
  }

  @Override
  public String getOwnerOrg() {
    return getString("owner_org", null);
  }

  @Override
  public List<CkanExtra> getExtras() {
    ArrayList<CkanExtra> extras = new ArrayList<CkanExtra>();
    if (json.has("extras")) {
      try {
        JSONArray arr = json.getJSONArray("extras");
        for (int i=0; i<arr.length(); i++) {
          JSONObject obj = arr.getJSONObject(i);
          extras.add(new CkanExtraAdaptor(obj));
        }
      } catch (JSONException ex) {
        LOG.log(Level.WARNING,"Invalid extras list format", ex);
      }
    }
    return extras;
  }

  @Override
  public String getRevisionId() {
    return getString("revision_id", null);
  }

  @Override
  public CkanTrackingSummary getTrackingSummary() {
    try {
      return json.has("tracking_summary") && json.get("tracking_summary") instanceof JSONObject?
              new CkanTrackingSummaryAdaptor(json.getJSONObject("tracking_summary")):
              null;
    } catch (JSONException ex) {
      return null;
    }
  }

  @Override
  public URL getLicenseUrl() {
    return getUrl("license_url", null);
  }

  @Override
  public Date getRevisionTimestamp() {
    return getDate("revision_timestamp", null);
  }
  
  @Override
  public String toString() {
    return String.format("PACKAGE :: title: %s | created on: %s | by: %s | notes: %s", getTitle(), getNotes(), getCreateDate(), getAuthor());
  }
}
