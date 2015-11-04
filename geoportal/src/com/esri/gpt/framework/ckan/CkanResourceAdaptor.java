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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Resource adaptor.
 */
/*package*/class CkanResourceAdaptor implements CkanResource {
  private static final IsoDateFormat ISO = new IsoDateFormat();
  private final JSONObject resourceJson;

  public CkanResourceAdaptor(JSONObject resourceJson) {
    this.resourceJson = resourceJson;
  }

  @Override
  public String getId() {
    try {
      return resourceJson.getString("id");
    } catch (JSONException ex) {
      return null;
    }
  }

  @Override
  public String getDescription() {
    try {
      return resourceJson.getString("description");
    } catch (JSONException ex) {
      return null;
    }
  }

  @Override
  public Date getUpdateDate() {
    try {
      return ISO.parseObject(resourceJson.getString("webstore_last_updated"));
    } catch (JSONException ex) {
      return null;
    } catch (ParseException ex) {
      return null;
    }
  }

  @Override
  public String getFormat() {
    try {
      return resourceJson.getString("format");
    } catch (JSONException ex) {
      return null;
    }
  }

  @Override
  public URL getUrl() {
    try {
      return new URL(resourceJson.getString("url"));
    } catch (JSONException ex) {
      return null;
    } catch (MalformedURLException ex) {
      return null;
    }
  }
  
  @Override
  public String toString() {
    return String.format("RESOURCE :: description: %s | format: %s | URL: %s", getDescription(), getFormat(), getUrl());
  }
}
