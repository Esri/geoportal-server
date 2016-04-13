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
import java.util.List;
import org.json.JSONObject;

/**
 * Package.
 */
public interface CkanPackage {
  JSONObject getJson();
  
  String getId();
  String getTitle();
  Date getCreateDate();
  Date getUpdateDate();
  String getNotes();
  String getAuthor();
  List<CkanResource> getResources();
  
  String getLicenseTitle();
  String getMaintainer();
  List<CkanRelationship> getRelationshipsAsObject();
  List<CkanRelationship> getRelationshipsAsSubject();
  Boolean getPrivate();
  String getMaintainerEmail();
  Long getNumTags();
  String getAuthorEmail();
  String getState();
  String getVersion();
  String getCreatorUserId();
  String getType();
  Long getNumResources();
  List<CkanTag> getTags();
  List<CkanGroup> getGroups();
  String getLicenseId();
  String getOrganization();
  String getName();
  Boolean getIsOpen();
  URL getUrl();
  String getOwnerOrg();
  List<CkanExtra> getExtras();
  String getRevisionId();
  
  CkanTrackingSummary getTrackingSummary();
  URL getLicenseUrl();
  Date getRevisionTimestamp();
}
