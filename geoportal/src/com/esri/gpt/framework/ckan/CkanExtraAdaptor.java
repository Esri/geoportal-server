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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * CKAN extra implementation.
 */
/*package*/class CkanExtraAdaptor extends CkanObjectImpl implements CkanExtra {

  public CkanExtraAdaptor(JSONObject json) {
    super(json);
  }

  @Override
  public String getKey() {
    return getString("key", null);
  }

  @Override
  public String getValue() {
    return getString("value", null);
  }

  @Override
  public String setValue(String value) {
    String oldValue = getValue();
    try {
      json.put("value", value);
    } catch (JSONException ex) {
    } 
    return oldValue;
  }

  public Object getAttribute(String name) {
    return json.opt(name);
  }
  
  public List<String> getAttributeNames() {
    ArrayList<String> names = new ArrayList<String>();
    JSONArray na = json.names();
    for (int i=0; i<na.length(); i++) {
      try {
        names.add(na.getString(i));
      } catch (JSONException ex) {
      }
    }
    return names;
  }
  
  @Override
  public Boolean getDeleted() {
    return getBoolean("deleted", null);
  }
  
  @Override
  public String toString() {
    return String.format("EXTRA :: key: %s, value: %s, deleted: %s", getKey(), getValue(), getDeleted());
  }
}
