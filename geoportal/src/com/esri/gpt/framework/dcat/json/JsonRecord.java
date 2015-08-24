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
package com.esri.gpt.framework.dcat.json;

import java.util.ArrayList;
import java.util.List;

/**
 * Raw DCAT record.
 */
public class JsonRecord extends JsonAttributes {
  private final JsonArray<JsonAttributes> distribution = new JsonArray<JsonAttributes>();
  private final List<JsonAttribute> keywords = new ArrayList<JsonAttribute>();
  private final List<JsonAttribute> themes = new ArrayList<JsonAttribute>();
  private final List<JsonAttribute> bureauCodes = new ArrayList<JsonAttribute>();
  private final List<JsonAttribute> programCodes = new ArrayList<JsonAttribute>();
  private final List<JsonAttribute> references = new ArrayList<JsonAttribute>();
  private final List<JsonAttribute> systemRecords = new ArrayList<JsonAttribute>();
  private final List<JsonAttribute> languages = new ArrayList<JsonAttribute>();

  public JsonArray<JsonAttributes> getDistribution() {
    return distribution;
  }
  
  public List<JsonAttribute> getKeywords() {
    return keywords;
  }

  public List<JsonAttribute> getBureauCodes() {
    return bureauCodes;
  }

  public List<JsonAttribute> getProgramCodes() {
    return programCodes;
  }

  public List<JsonAttribute> getReferences() {
    return references;
  }

  public List<JsonAttribute> getSystemRecords() {
    return systemRecords;
  }

  public List<JsonAttribute> getLanguages() {
    return languages;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(super.toString());
    if (!distribution.isEmpty()) {
      if (sb.length()>0) {
        sb.append(",");
      }
      sb.append("\"distribution\":").append(distribution.toString());
    }
    return "{"+sb.toString()+"}";
  }

	public List<JsonAttribute> getThemes() {
	    return themes;
	}
}
