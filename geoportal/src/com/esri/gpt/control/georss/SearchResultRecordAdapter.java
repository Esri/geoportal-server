/*
 * Copyright 2012 Esri.
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
package com.esri.gpt.control.georss;

import com.esri.gpt.catalog.search.ResourceLinks;
import com.esri.gpt.catalog.search.SearchResultRecord;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.UuidUtil;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SearchResultRecord adapter.
 */
public class SearchResultRecordAdapter implements IFeedRecord {
  private Map<String,Map<String,IFeedAttribute>> collection = new HashMap<String, Map<String, IFeedAttribute>>();
  private SearchResultRecord record;
  
  public SearchResultRecordAdapter(SearchResultRecord record) {
    this.record = record;
  }

  @Override
  public long getObjectId() {
    return -1;
  }

  @Override
  public String getUuid() {
    return record.getUuid();
  }

  @Override
  public String getTitle() {
    return record.getTitle();
  }

  @Override
  public Date getModfiedDate() {
    return record.getModfiedDate();
  }

  @Override
  public String getAbstract() {
    return record.getAbstract();
  }

  @Override
  public Envelope getEnvelope() {
    return record.getEnvelope();
  }

  @Override
  public ResourceLinks getResourceLinks() {
    return record.getResourceLinks();
  }

  @Override
  public String getContentType() {
    return record.getContentType();
  }

  @Override
  public String getResourceUrl() {
    return record.getResourceUrl();
  }

  @Override
  public String getViewMetadataUrl() {
    return record.getViewMetadataUrl();
  }

  @Override
  public String getServiceType() {
    return record.getService();
  }

  @Override
  public String getService() {
    return record.getService();
  }

  @Override
  public String getThumbnailUrl() {
    return record.getThumbnailUrl();
  }

  @Override
  public String getFileIdentifier() {
    return record.getFileIdentifier();
  }

  @Override
  public Map<String, IFeedAttribute> getData(String collection) {
    Map<String,IFeedAttribute> data = this.collection.get(collection);
    if (data==null) {
      data = new HashMap<String, IFeedAttribute>();
      this.collection.put(collection, data);
    }
    return data;
  }
  
  
  @Override
  public String toString() {
    return record.toString();
  }
}
