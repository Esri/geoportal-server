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

import org.json.JSONObject;

/**
 * CKAN relationship implementation.
 */
/*package*/class CkanRelationshipAdaptor extends CkanObjectImpl implements CkanRelationship {

  public CkanRelationshipAdaptor(JSONObject json) {
    super(json);
  }

  @Override
  public String getSubject() {
    return getString("subject", null);
  }

  @Override
  public String getObject() {
    return getString("object", null);
  }

  @Override
  public String getType() {
    return getString("type", null);
  }

  @Override
  public String getComment() {
    return getString("comment", null);
  }

  @Override
  public String toString() {
    return String.format("RELATIONSHIP :: subject: %s, object: %s, type: %s", getSubject(), getObject(), getType());
  }
  
}
