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
package com.esri.gpt.control.webharvest.client.dcat;

import com.esri.gpt.framework.dcat.DcatIterableAdaptor;
import com.esri.gpt.framework.dcat.dcat.DcatDistribution;
import com.esri.gpt.framework.dcat.dcat.DcatDistributionList;
import com.esri.gpt.framework.dcat.dcat.DcatRecord;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * DCAT adaptor.
 */
class DCATAdaptor implements Iterable<Publishable> {
  private DCATProxy proxy;
  private DcatIterableAdaptor adaptor;

  public DCATAdaptor(DCATProxy proxy, DcatIterableAdaptor adaptor) {
    this.proxy = proxy;
    this.adaptor = adaptor;
  }

  public void close() {
    if (adaptor!=null) {
      adaptor.close();
      adaptor = null;
    }
  }
  
  @Override
  public Iterator<Publishable> iterator() {
    return new DCATIterator();
  }
  
  private class DCATIterator extends ReadOnlyIterator<Publishable> {
    private Iterator<DcatRecord> iterator = adaptor!=null? adaptor.iterator(): null;
    private String rootAccessUrl = null;
    private String accessUrl;

    @Override
    public boolean hasNext() {
      if (iterator==null) {
        return false;
      }
      if (accessUrl!=null) {
        return true;
      }
      if (!iterator.hasNext()) {
        return false;
      }
      while(iterator.hasNext()) {
        DcatRecord record = iterator.next();
        if (rootAccessUrl==null) {
          rootAccessUrl = record.getAccessURL();
        }
        String url = readAccessUrl(record);
        if (!url.isEmpty()) {
          accessUrl = url;
          return true;
        }
      }
      return false;
    }

    @Override
    public Publishable next() {
      if (accessUrl==null) {
        throw new NoSuchElementException();
      }
      DCATRecord record = new DCATRecord(proxy,accessUrl);
      accessUrl = null;
      return record;
    }
    
    private String readAccessUrl(DcatRecord r) {
      DcatDistributionList distList = r.getDistribution();
      for (DcatDistribution d : distList) {
        if (d.getFormat().equalsIgnoreCase("xml")) {
          return d.getAccessURL();
        }
      }
      return "";
    }
    
  }
  
}
