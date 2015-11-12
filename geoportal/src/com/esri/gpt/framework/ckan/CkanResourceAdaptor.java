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
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Resource adaptor.
 */
/*package*/class CkanResourceAdaptor extends CkanObjectImpl implements CkanResource {
  private final CkanPackage pkg;

  public CkanResourceAdaptor(CkanPackage pkg, JSONObject json) {
    super(json);
    this.pkg = pkg;
  }

  @Override
  public CkanPackage getPackage() {
    return pkg;
  }

  @Override
  public String getId() {
    return getString("id", null);
  }

  @Override
  public String getName() {
    return getString("name", null);
  }
  
  @Override
  public String getDescription() {
    return getString("description", null);
  }

  @Override
  public Date getUpdateDate() {
    return getDate("webstore_last_updated", null);
  }

  @Override
  public String getFormat() {
    return getString("format", null);
  }

  @Override
  public URL getUrl() {
    return getUrl("url", null);
  }

  @Override
  public Date getCreateDate() {
    return getDate("created", null);
  }

  @Override
  public Date getCacheUpdateDate() {
    return getDate("cache_last_updated", null);
  }

  @Override
  public Date getWebStoreUpdateDate() {
    return getDate("webstore_last_updated", null);
  }

  @Override
  public Boolean getDataStoreActive() {
    return getBoolean("datastore_active", null);
  }

  @Override
  public Long getSize() {
    return getLong("size", null);
  }

  @Override
  public String getState() {
    return getString("state", null);
  }

  @Override
  public String getHash() {
    return getString("hash", null);
  }

  @Override
  public String getMimeTypeInner() {
    return getString("mimetype_inner", null);
  }

  @Override
  public String getUrlType() {
    return getString("url_type", null);
  }

  @Override
  public String getMimeType() {
    return getString("mimetype", null);
  }

  @Override
  public URL getCacheUrl() {
    return getUrl("cache_url", null);
  }

  @Override
  public String getWebStoreUrl() {
    return getString("webstore_url", null);
  }

  @Override
  public Long getPosition() {
    return getLong("position", null);
  }

  @Override
  public String getRevisionId() {
    return getString("revision_id", null);
  }

  @Override
  public String getResourceType() {
    return getString("resource_type", null);
  }

  @Override
  public String getResourceGroupId() {
    return getString("resource_group_id", null);
  }

  @Override
  public String getPackageId() {
    return pkg.getId();
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
  public String toString() {
    return String.format("RESOURCE :: description: %s | format: %s | URL: %s", getDescription(), getFormat(), getUrl());
  }
}
