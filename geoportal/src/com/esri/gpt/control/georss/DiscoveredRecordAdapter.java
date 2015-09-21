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
import com.esri.gpt.catalog.discovery.PropertyMeanings;
import com.esri.gpt.catalog.discovery.Returnable;
import com.esri.gpt.catalog.schema.Meaning;
import com.esri.gpt.catalog.search.ResourceIdentifier;
import com.esri.gpt.catalog.search.ResourceLinks;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.Val;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.lucene.document.DateTools;

/**
 * Discovered record adapter.
 */
public class DiscoveredRecordAdapter implements IFeedRecord {

  private final Map<String,Map<String,IFeedAttribute>> collection = new HashMap<String, Map<String, IFeedAttribute>>();
  private final HashMap<String,Object[]> values = new HashMap<String, Object[]>();
  private final ResourceLinks resourceLinks = new ResourceLinks();
  private final ResourceIdentifier resourceIdentifier;
  
  public DiscoveredRecordAdapter(ResourceIdentifier resourceIdentifier, DiscoveredRecord record) {
    this.resourceIdentifier = resourceIdentifier;
    for (Returnable ret: record.getFields()) {
      this.values.put(ret.getMeaning().getName(), ret.getValues());
    }
  }

  @Override
  public long getObjectId() {
    Map<String, IFeedAttribute> data = getData(IFeedRecord.STD_COLLECTION_CATALOG);
    if (data==null) return -1;
    IFeedAttribute id = data.get("ID");
    if (id==null) return -1;
    Object value = id.getValue();
    if (!(value instanceof Number)) return -1;
    return ((Number)value).longValue();
  }

  @Override
  public String getUuid() {
    return select(PropertyMeanings.NAME_UUID);
  }

  @Override
  public String getTitle() {
    return select(Meaning.MEANINGTYPE_TITLE);
  }

  @Override
  public Date getModfiedDate() {
    try {
      return new Date(DateTools.stringToTime(select(PropertyMeanings.NAME_DATEMODIFIED)));
    } catch (ParseException ex) {
      return new Date();
    }
  }

  @Override
  public String getAbstract() {
    String abstr = select(Meaning.MEANINGTYPE_ABSTRACT);
    abstr = abstr.replaceAll("\\\n", " ");
    if (abstr.length()>253) {
      abstr = abstr.substring(0, 252) + "...";
    }
    return abstr;
  }

  @Override
  public Envelope getEnvelope() {
    Object[] geoObjs = this.values.get("geometry");
    if (geoObjs!=null && geoObjs.length==1 && geoObjs[0] instanceof Envelope) {
      return (Envelope) geoObjs[0];
    }
    
    String sWest  = select(Meaning.MEANINGTYPE_ENVELOPE_WEST);
    String sEast  = select(Meaning.MEANINGTYPE_ENVELOPE_EAST);
    String sSouth = select(Meaning.MEANINGTYPE_ENVELOPE_SOUTH);
    String sNorth = select(Meaning.MEANINGTYPE_ENVELOPE_NORTH);
    
    return new Envelope(Val.chkDbl(sWest, -180), Val.chkDbl(sSouth, -90), Val.chkDbl(sEast, 180), Val.chkDbl(sNorth, 90));
  }

  @Override
  public ResourceLinks getResourceLinks() {
    return resourceLinks;
  }

  @Override
  public String getContentType() {
    return select(Meaning.MEANINGTYPE_CONTENTTYPE,"\\p{Alpha}+");
  }

  @Override
  public String getResourceUrl() {
    return select(Meaning.MEANINGTYPE_RESOURCE_URL);
  }

  @Override
  public String getViewMetadataUrl() {
    return select(Meaning.MEANINGTYPE_WEBSITE_URL);
  }

  @Override
  public String getFileIdentifier() {
    return select(Meaning.MEANINGTYPE_FILEIDENTIFIER);
  }

  @Override
  public String getThumbnailUrl() {
    return select(Meaning.MEANINGTYPE_THUMBNAIL_URL);
  }
  
  @Override
  public Map<String,IFeedAttribute> getData(String collection) {
    Map<String,IFeedAttribute> data = this.collection.get(collection);
    if (data==null) {
      data = new TreeMap<String, IFeedAttribute>(String.CASE_INSENSITIVE_ORDER);
      this.collection.put(collection, data);
    }
    return data;
  }
  
  @Override
  public String getServiceType() {
    String serviceType = select(Meaning.MEANINGTYPE_RESOURCE_TYPE);
    if (serviceType.isEmpty()) {
      serviceType = resourceIdentifier.guessServiceTypeFromUrl(getResourceUrl());
    }
    return serviceType;
  }

  @Override
  public String getService() {
    return select(Meaning.MEANINGTYPE_RESOURCE_URL);
  }
  
  private String select(String meaning, String pattern) {
    Object [] oArr = values.get(meaning);
    pattern = Val.chkStr(pattern);
    if (oArr!=null) {
      for (Object obj : oArr) {
        String str = obj.toString();
        if (pattern.isEmpty() || str.matches(pattern)) {
          return str;
        }
      }
    }
    return "";
  }
  
  private String select(String meaning) {
    return select(meaning,"");
  }
  
}
