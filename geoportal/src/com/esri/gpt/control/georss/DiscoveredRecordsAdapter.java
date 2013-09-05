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

import com.esri.gpt.catalog.discovery.DiscoveredRecord;
import com.esri.gpt.catalog.discovery.DiscoveredRecords;
import com.esri.gpt.catalog.search.OpenSearchProperties;
import com.esri.gpt.catalog.search.ResourceIdentifier;
import java.sql.SQLException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Discovered records adapter.
 */
public class DiscoveredRecordsAdapter extends AbstractList<IFeedRecord> implements IFeedRecords {
  private OpenSearchProperties osProps;
  private List<FieldMeta> metadata;
  private ArrayList<IFeedRecord> feedRecords = new ArrayList<IFeedRecord>();
  private ResourceIdentifier resourceIdentifier;
  
  public DiscoveredRecordsAdapter(ResourceIdentifier resourceIdentifier, OpenSearchProperties osProps, List<FieldMeta> metadata, DiscoveredRecords records, Map<DiscoveredRecord,Map<String,List<String>>> mapping) throws SQLException {
    this.resourceIdentifier = resourceIdentifier;
    this.osProps = osProps;
    this.metadata = metadata;
    
    for (DiscoveredRecord dr: records) {
      DiscoveredRecordAdapter record = new DiscoveredRecordAdapter(resourceIdentifier,dr);
      Map<String, IFeedAttribute> attrs = new HashMap<String, IFeedAttribute>();
      Map<String,List<String>> data = mapping.get(dr);
      if (data!=null) {
        for (Map.Entry<String,List<String>> e: data.entrySet()) {
          List<IFeedAttribute> l = new ArrayList<IFeedAttribute>();
          for (String s: e.getValue()) {
            l.add(IFeedAttribute.Factory.create(s,256));
          }
          attrs.put(e.getKey(), IFeedAttribute.Factory.create(l));
        }
        record.getData(IFeedRecord.STD_COLLECTION_INDEX).putAll(attrs);
      }
      feedRecords.add(record);
    }
  }

  @Override
  public IFeedRecord get(int index) {
    return feedRecords.get(index);
  }

  @Override
  public int size() {
    return feedRecords.size();
  }

  @Override
  public OpenSearchProperties getOpenSearchProperties() {
    return osProps;
  }

  @Override
  public List<FieldMeta> getMetaData() {
    return metadata;
  }
}
