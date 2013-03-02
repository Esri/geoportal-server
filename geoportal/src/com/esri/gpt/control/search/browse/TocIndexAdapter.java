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
package com.esri.gpt.control.search.browse;
import com.esri.gpt.catalog.lucene.LuceneIndexAdapter;
import com.esri.gpt.catalog.lucene.Storeables;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.IndexSearcher;

/**
 * Provides utilities for querying a Lucene index during TOC construction.
 */
public class TocIndexAdapter {
  
  /** instance variables ====================================================== */
  private LuceneIndexAdapter baseAdapter;
  private IndexSearcher      searcher;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public TocIndexAdapter() {}
  
  /** methods ================================================================= */
  
  /**
   * Closes any open resources.
   */
  public void close() {
    if ((this.baseAdapter != null) && (this.searcher != null)) {
      this.baseAdapter.closeSearcher(this.searcher);
    }
  }
  
  /**
   * Gets the index searcher.
   * @param context the operation context
   * @return the index searcher
   * @throws CorruptIndexException if the index is corrupt 
   * @throws IOException if an I/O exception occurs
   */
  public IndexSearcher getSearcher(TocContext context) 
    throws CorruptIndexException, IOException {
    if (this.searcher != null) {
      return this.searcher;
    } else {
      if (this.baseAdapter == null) {
        this.baseAdapter = new LuceneIndexAdapter(context.getRequestContext());
      }
      this.searcher = this.baseAdapter.newSearcher();
      return this.searcher;
    }
  }
  
  /**
   * Returns the field values associated with a document
   * @param context the operation context
   * @param fieldName the field name
   * @param uuid the document uuid
   * @return the field values (null if not found)
   * @throws CorruptIndexException if the index is corrupt
   * @throws IOException if an I/O exception occurs
   */
  public String[] queryFieldByUuid(TocContext context, String fieldName, String uuid) 
    throws CorruptIndexException, IOException {
    TermDocs termDocs = null;
    try {
      uuid = Val.chkStr(uuid);
      if (uuid.length() > 0) {
        IndexSearcher searcher = this.getSearcher(context);
        IndexReader reader = searcher.getIndexReader();
        MapFieldSelector selector = new MapFieldSelector(new String[]{fieldName});
        termDocs = reader.termDocs();
        termDocs.seek(new Term(Storeables.FIELD_UUID,uuid));
        if (termDocs.next()) {
          Document document = reader.document(termDocs.doc(),selector);
          return document.getValues(fieldName);
        }
      }
    } finally {
      try {if (termDocs != null) termDocs.close();} catch (Exception ef) {}
    }
    return null;
  }

}
