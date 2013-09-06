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
package com.esri.gpt.framework.dcat;

import com.esri.gpt.framework.dcat.DcatParser.ListenerInternal;
import com.esri.gpt.framework.dcat.dcat.DcatRecord;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * DCAT parser adaptor.
 * <p>
 * It allows to wrap {@link DcatParser} parser so instead having 'event paradigm'
 * based mechanism, an 'iterator paradigm' could be used.
 * </p>
 */
public class DcatParserAdaptor implements Iterable<DcatRecord> {
  private DcatParser parser;
  
  /**
   * Creates instance of the adaptor.
   * @param parser parser
   */
  public DcatParserAdaptor(DcatParser parser) {
    this.parser = parser;
  }

  /**
   * Closes adaptor.
   * <p>
   * It also closes underlying parser. This method is called either by the user
   * of the adaptor or it will be called automatically if iteration reaches the
   * end. Subsequential calls to the method will not case any adverse effects.
   * </p>
   */
  public void close() {
    if (parser!=null) {
      parser.close();
      parser = null;
    }
  }
  
  /**
   * Called upon parsing exception thrown during iteration.
   * @param ex 
   */
  protected void onException(DcatParseException ex) {
    // TODO: override if needed
  }
  
  @Override
  public Iterator<DcatRecord> iterator() {
    return new DcatIterator();
  }
  
  /**
   * DCAT iterator.
   */
  private class DcatIterator extends ReadOnlyIterator<DcatRecord> {
    private DcatRecord nextRecord;
    private boolean firstTime = true;

    @Override
    public boolean hasNext() {
      if (parser==null) {
        return false;
      }
      if (nextRecord!=null) {
        return true;
      }
      ListenerInternal listener = new ListenerInternal() {
        @Override
        public boolean onRecord(DcatRecord record) {
          nextRecord = record;
          return false;
        }
      };
      try {
        if (firstTime) {
          firstTime = false;
          parser.parse(listener);
        } else {
          parser.parseRecords(listener);
        }
        boolean hasNext = nextRecord!=null;
        if (!hasNext) {
          close();
        }
        return hasNext;
      } catch (DcatParseException ex) {
        close();
        onException(ex);
        return false;
      }
    }

    @Override
    public DcatRecord next() {
      if (nextRecord==null) {
        throw new NoSuchElementException();
      }
      DcatRecord next = nextRecord;
      nextRecord = null;
      return next;
    }
    
  }
}
