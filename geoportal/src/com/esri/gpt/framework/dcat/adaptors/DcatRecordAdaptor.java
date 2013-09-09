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
package com.esri.gpt.framework.dcat.adaptors;

import com.esri.gpt.framework.dcat.dcat.DcatDistributionList;
import com.esri.gpt.framework.dcat.dcat.DcatRecord;
import com.esri.gpt.framework.dcat.raw.RawDcatRecord;

/**
 * DCAT record adaptor.
 */
public class DcatRecordAdaptor extends DcatAdaptor implements DcatRecord {
  private RawDcatRecord record;

  /**
   * Creates instance of the adaptor.
   * @param record raw record
   */
  public DcatRecordAdaptor(RawDcatRecord record) {
    super(record);
    this.record = record;
  }

  @Override
  public String getTitle() {
    return getString("title");
  }

  @Override
  public String getAbstract() {
    return getString("abstract");
  }

  @Override
  public String getKeyword() {
    return getString("keyword");
  }

  @Override
  public String getIdentifier() {
    return getString("identifier");
  }

  @Override
  public String getAccessLevel() {
    return getString("accessLevel");
  }

  @Override
  public String getDataDictionary() {
    return getString("dataDictionary");
  }

  @Override
  public String getWebService() {
    return getString("webService");
  }

  @Override
  public String getAccessURL() {
    return getString("accessURL");
  }

  @Override
  public String getFormat() {
    return getString("format");
  }

  @Override
  public String getSpatial() {
    return getString("spatial");
  }

  @Override
  public String getTemporal() {
    return getString("temporal");
  }

  @Override
  public String getModified() {
    return getString("modified");
  }

  @Override
  public String getPublisher() {
    return getString("publisher");
  }

  @Override
  public String getPerson() {
    return getString("person");
  }

  @Override
  public String getMbox() {
    return getString("mbox");
  }

  @Override
  public String getLicense() {
    return getString("license");
  }

  @Override
  public DcatDistributionList getDistribution() {
    return new DcatDistributionListAdaptor(record.getDistribution());
  }
  
  @Override
  public String toString() {
    return record.toString();
  }
}
