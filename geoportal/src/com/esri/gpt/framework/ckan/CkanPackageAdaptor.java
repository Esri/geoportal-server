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

import com.esri.gpt.framework.isodate.IsoDateFormat;
import java.text.ParseException;
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
/*package*/class CkanPackageAdaptor implements CkanPackage {
  private static final Logger LOG = Logger.getLogger(CkanPackageAdaptor.class.getName());
  private static final IsoDateFormat ISO = new IsoDateFormat();
  private final JSONObject packageJson;

  public CkanPackageAdaptor(JSONObject packageJson) {
    this.packageJson = packageJson;
  }

  @Override
  public String getId() {
    try {
      return packageJson.getString("id");
    } catch (JSONException ex) {
      return null;
    }
  }

  @Override
  public String getTitle() {
    try {
      return packageJson.getString("title");
    } catch (JSONException ex) {
      return null;
    }
  }

  @Override
  public Date getCreateDate() {
    try {
      return ISO.parseObject(packageJson.getString("metadata_created"));
    } catch (JSONException ex) {
      return null;
    } catch (ParseException ex) {
      return null;
    }
  }

  @Override
  public Date getUpdateDate() {
    try {
      return ISO.parseObject(packageJson.getString("metadata_modified"));
    } catch (JSONException ex) {
      return null;
    } catch (ParseException ex) {
      return null;
    }
  }

  @Override
  public String getNotes() {
    try {
      return packageJson.getString("notes");
    } catch (JSONException ex) {
      return null;
    }
  }

  @Override
  public String getAuthor() {
    try {
      return packageJson.getString("author");
    } catch (JSONException ex) {
      return null;
    }
  }

  @Override
  public List<CkanResource> getResources() {
    ArrayList<CkanResource> resources = new ArrayList<CkanResource>();
    if (packageJson.has("resources")) {
      try {
        JSONArray resArray = packageJson.getJSONArray("resources");
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
