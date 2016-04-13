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
import org.json.JSONObject;

/**
 * CKAN group implementation.
 */
/*package*/class CkanGroupAdaptor extends CkanObjectImpl implements CkanGroup {

  public CkanGroupAdaptor(JSONObject json) {
    super(json);
  }

  @Override
  public String getDisplayName() {
    return getString("display_name", null);
  }

  @Override
  public String getDescription() {
    return getString("description", null);
  }

  @Override
  public URL getImageDisplayUrl() {
    return getUrl("image_display_url", null);
  }

  @Override
  public String getTitle() {
    return getString("title", null);
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
  public URL getImageUrl() {
    return getUrl("image_url", null);
  }

  @Override
  public String getCapacity() {
    return getString("capacity", null);
  }

  @Override
  public Date getCreateDate() {
    return getDate("created", null);
  }

  @Override
  public Boolean getIsOrganization() {
    return getBoolean("is_organization", null);
  }

  @Override
  public String getRevision() {
    return getString("revision", null);
  }

  @Override
  public String getType() {
    return getString("type", null);
  }

  @Override
  public String getApprovalStatus() {
    return getString("approval_status", null);
  }

  @Override
  public String getRevisionId() {
    return getString("revision_id", null);
  }

  @Override
  public Date getRevisionTimestamp() {
    return getDate("revision_timestamp", null);
  }
  
  @Override
  public String toString() {
    return String.format("GROUP :: display name: %s, title: %s, image url: %s", getDisplayName(), getTitle(), getImageDisplayUrl());
  }
}
