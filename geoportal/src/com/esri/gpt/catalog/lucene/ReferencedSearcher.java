/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.catalog.lucene;
import java.io.IOException;
import java.util.logging.Level;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

import com.esri.gpt.framework.util.LogUtil;

/**
 * This is an experimental class to attempt to deal with the 
 * issues of opening many IndexReader(s) against the same 
 * Lucene index (issues; performance, memory, file handles).
 *
 */
class ReferencedSearcher {

  /** instance variables ====================================================== */  
  private IndexSearcher currentSearcher;
  private boolean       reopening;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied directory.
   * @param dir the directory
   */
  public ReferencedSearcher(Directory dir) throws IOException {
    this.currentSearcher = new IndexSearcher(IndexReader.open(dir,true)); 
    this.warm(this.currentSearcher);
  }
    
  /** methods ================================================================= */
  
  protected void close() throws IOException {
    this.swap(null);
  }
  
  protected synchronized IndexSearcher get() {
    this.currentSearcher.getIndexReader().incRef();
    return this.currentSearcher;
  }  
  
  private synchronized void onReopenBegin() throws InterruptedException {
    while (this.reopening) {wait();}
    this.reopening = true;
  }
  
  private synchronized void onReopenEnd() {
    this.reopening = false;
    notifyAll();
  }
  
  protected synchronized void release(IndexSearcher searcher) throws IOException {
    searcher.getIndexReader().decRef();
  }
  
  protected void checkForReopen() throws InterruptedException,IOException {
    this.onReopenBegin();
    try {
      final IndexSearcher searcher = get();
      try {
        IndexReader newReader = this.currentSearcher.getIndexReader().reopen();
        if (newReader != this.currentSearcher.getIndexReader()) {
          IndexSearcher newSearcher = new IndexSearcher(newReader);
          this.warm(newSearcher);
          this.swap(newSearcher);
        }
      } catch (IOException e) {
        LogUtil.getLogger().log(Level.SEVERE,"Error during index re-open.",e);
      } finally {
        this.release(searcher);
      }
    } finally {
      onReopenEnd();
    }
  }
  
  private synchronized void swap(IndexSearcher newSearcher)
    throws IOException {
    this.release(this.currentSearcher);
    this.currentSearcher = newSearcher;
  }
  
  private void warm(IndexSearcher searcher) throws IOException {}
  
}
