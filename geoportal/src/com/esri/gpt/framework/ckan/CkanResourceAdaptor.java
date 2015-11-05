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

import java.net.URL;
import java.util.Date;
import org.json.JSONObject;

/**
 * Resource adaptor.
 */
/*package*/class CkanResourceAdaptor extends CkanObjectImpl implements CkanResource {

  public CkanResourceAdaptor(JSONObject resourceJson) {
    super(resourceJson);
  }

  @Override
  public String getId() {
    return getString("id", null);
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
  public String toString() {
    return String.format("RESOURCE :: description: %s | format: %s | URL: %s", getDescription(), getFormat(), getUrl());
  }
}
