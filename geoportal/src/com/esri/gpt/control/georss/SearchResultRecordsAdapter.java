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

import com.esri.gpt.catalog.search.OpenSearchProperties;
import com.esri.gpt.catalog.search.SearchResultRecords;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * SearchResultRecords adapter
 */
public class SearchResultRecordsAdapter extends AbstractList<IFeedRecord> implements IFeedRecords {
  private SearchResultRecords records;
  
  public SearchResultRecordsAdapter(SearchResultRecords records) {
    this.records = records;
  }

  @Override
  public IFeedRecord get(int index) {
    return new SearchResultRecordAdapter(records.get(index));
  }

  @Override
  public int size() {
    return records.size();
  }
  
  @Override
  public String toString() {
    return records.toString();
  }

  @Override
  public OpenSearchProperties getOpenSearchProperties() {
    return records.getOpenSearchProperties();
  }

  @Override
  public List<FieldMeta> getMetaData() {
    return new ArrayList<IFeedRecords.FieldMeta>();
  }
}
