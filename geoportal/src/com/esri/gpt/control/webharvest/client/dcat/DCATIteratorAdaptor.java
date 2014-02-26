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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolDCAT;
import com.esri.gpt.framework.dcat.DcatParserAdaptor;
import com.esri.gpt.framework.dcat.dcat.DcatDistribution;
import com.esri.gpt.framework.dcat.dcat.DcatDistributionList;
import com.esri.gpt.framework.dcat.dcat.DcatRecord;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import com.esri.gpt.framework.util.Val;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * DCAT adaptor.
 */
class DCATIteratorAdaptor implements Iterable<Publishable> {
  private DcatParserAdaptor adaptor;
  private Pattern formatPattern;
  

  /**
   * Creates instance of the adaptor.
   * <p>
   * Format property is a value of the 'format' attribute within 'Distribution'
   * array in the DCAT JSON response.
   * </p>
   * @param format format;
   * @param adaptor parser adaptor
   */
  public DCATIteratorAdaptor(String format, DcatParserAdaptor adaptor) {
    this.adaptor = adaptor;
    try {
      formatPattern = Pattern.compile(format, Pattern.CASE_INSENSITIVE);
    } catch (Exception ex) {
      formatPattern = Pattern.compile(HarvestProtocolDCAT.FORMAT_PATTERN_DEFAULT_VALUE, Pattern.CASE_INSENSITIVE);
    }
  }

  /**
   * Closes adaptor.
   * <p>
   * It will also close underlying parser adaptor through the call to {@link DcatParserAdaptor#close}.
   * </p>
   */
  public void close() {
    if (adaptor!=null) {
      adaptor.close();
      adaptor = null;
    }
  }
  
  @Override
  public Iterator<Publishable> iterator() {
    return new DCATIterator(formatPattern);
  }
  
  /**
   * DCAT iterator.
   */
  private class DCATIterator extends ReadOnlyIterator<Publishable> {
    private Pattern formatPattern;
    private Iterator<DcatRecord> iterator = adaptor!=null? adaptor.iterator(): null;
    private DcatRecord record;

    /**
     * Creates instance of the iterator.
     * @param formatPattern format pattern to select access URL
     */
    public DCATIterator(Pattern formatPattern) {
      this.formatPattern = formatPattern;
    }
    
    @Override
    public boolean hasNext() {
      // no iterator? end of story
      if (iterator==null) {
        return false;
      }
      // cached accessUrl non empty? next() hasn't been called yet
      if (record!=null) {
        return true;
      }
      // anything more in the iterator?
      if (!iterator.hasNext()) {
        return false;
      }
      // find something with access URL present
      while(iterator.hasNext()) {
        record = iterator.next();
        if (record!=null) {
          return true;
        }
      }
      return false;
    }

    @Override
    public Publishable next() {
      if (record==null) {
        throw new NoSuchElementException();
      }
      DCATRecord dcatRecord = new DCATRecord(record);
      record = null;
      return dcatRecord;
    }
    
    /**
     * Selects access URL.
     * @param r DCAT record
     * @return access URL or empty string if no access URL present
     */
    private String selectAccessUrl(DcatRecord r) {
      DcatDistributionList distList = r.getDistribution();
      for (DcatDistribution d : distList) {
        if (!d.getAccessURL().isEmpty() && formatPattern.matcher(Val.chkStr(d.getMediaType(), d.getFormat())).matches()) {
          return d.getAccessURL();
        }
      }
      return "";
    }
    
  }
  
}
