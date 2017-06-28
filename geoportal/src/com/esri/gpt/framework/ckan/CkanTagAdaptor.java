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

import java.util.Date;
import org.json.JSONObject;

/**
 * CKAN tag implementation.
 */
/*package*/class CkanTagAdaptor extends CkanObjectImpl implements CkanTag {

  public CkanTagAdaptor(JSONObject json) {
    super(json);
  }

  @Override
  public String getVocabularyId() {
    return getString("vocabulary_id", null);
  }

  @Override
  public String getState() {
    return getString("state", null);
  }

  @Override
  public String getDisplayName() {
    return getString("display_name", null);
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
  public Date getRevisionTimestamp() {
    return getDate("revision_timestamp", null);
  }

  @Override
  public String toString() {
    return String.format("TAG :: display name: %s", getDisplayName());
  }
  
}
