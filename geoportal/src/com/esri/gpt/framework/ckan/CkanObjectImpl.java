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

import com.esri.gpt.framework.isodate.IsoDateFormat;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Generic ckan object implementation.
 */
/*package*/class CkanObjectImpl implements CkanObject {
  protected static final IsoDateFormat ISO = new IsoDateFormat();
  protected final JSONObject json;

  public CkanObjectImpl(JSONObject json) {
    this.json = json;
  }

  public JSONObject getJson() {
    return json;
  }
  
  @Override
  public String getString(String attr, String defaultValue) {
    return json.optString(attr, defaultValue);
  }
  
  @Override
  public URL getUrl(String attr, URL defaultValue) {
    try {
      return new URL(json.optString(attr, defaultValue!=null? defaultValue.toExternalForm(): null));
    } catch (MalformedURLException ex) {
      return defaultValue;
    }
  }
  
  @Override
  public Date getDate(String attr, Date defaultValue) {
    try {
      return ISO.parseObject(json.optString(attr, defaultValue!=null? ISO.format(defaultValue): null));
    } catch (ParseException ex) {
      return defaultValue;
    }
  }
  
  @Override
  public Boolean getBoolean(String attr, Boolean defaultValue) {
    try {
      return json.getBoolean(attr);
    } catch (JSONException ex) {
      return defaultValue;
    }
  }

  @Override
  public Long getLong(String attr, Long defaultValue) {
    try {
      return json.getLong(attr);
    } catch (JSONException ex) {
      return defaultValue;
    }
  }
}
