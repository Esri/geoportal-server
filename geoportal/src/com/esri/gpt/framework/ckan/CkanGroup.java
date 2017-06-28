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

/**
 * CKAN group.
 */
public interface CkanGroup {
  String getDisplayName();
  String getDescription();
  URL getImageDisplayUrl();
  URL getImageUrl();
  String getTitle();
  String getId();
  String getName();
  String getCapacity();
  Date getCreateDate();
  Boolean getIsOrganization();
  String getRevision();
  String getType();
  String getApprovalStatus();
  String getRevisionId();
  Date getRevisionTimestamp();
}
