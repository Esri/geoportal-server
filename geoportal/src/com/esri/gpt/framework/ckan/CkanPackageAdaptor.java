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
package com.esri.gpt.framework.ckan;

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
/*package*/class CkanPackageAdaptor extends CkanObjectImpl implements CkanPackage, CkanObject {
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
    ArrayList<CkanResource> resources = new ArrayList<CkanResource>();
    if (json.has("resources")) {
      try {
        JSONArray resArray = json.getJSONArray("resources");
        for (int i=0; i<resArray.length(); i++) {
          JSONObject resObj = resArray.getJSONObject(i);
          resources.add(new CkanResourceAdaptor(resObj));
        }
      } catch (JSONException ex) {
        LOG.log(Level.WARNING,"Invalid resource list format", ex);
      }
    }
    return resources;
  }
  
  @Override
  public String toString() {
    return String.format("PACKAGE :: title: %s | notes: %s | created: %s | by: %s", getTitle(), getNotes(), getCreateDate(), getAuthor());
  }
}
