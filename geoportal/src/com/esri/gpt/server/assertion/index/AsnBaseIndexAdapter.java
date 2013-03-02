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
package com.esri.gpt.server.assertion.index;
import com.esri.gpt.catalog.lucene.LuceneIndexAdapter;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.server.assertion.components.AsnContext;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.NativeFSLockFactory;

/**
 * Base class for an adapter that handles read/write operations against a 
 * Lucene index of assertions.
 */
public class AsnBaseIndexAdapter {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(AsnBaseIndexAdapter.class.getName());
  
  /** Hold NativeFSLockFactory objects statically within the JVM */
  private static final Map<String,NativeFSLockFactory> NATIVEFSLOCKFACTORIES = 
    new HashMap<String,NativeFSLockFactory>();
  
  /** instance variables ====================================================== */
  private AsnIndexReference indexReference;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnBaseIndexAdapter() {}
    
  /** properties ============================================================== */
  
  /**
   * Gets the configuration reference for the index.
   * @return the index configuration reference
   */
  public AsnIndexReference getIndexReference() {
    return this.indexReference;
  }
  /**
   * Sets the configuration reference for the index.
   * @param indexReference index configuration reference
   */
  public void setIndexReference(AsnIndexReference indexReference) {
    this.indexReference = indexReference;
  }
  
  /** methods ================================================================= */
  
  /**
   * Closes an index reader.
   * @param reader the reader to close
   */
  public void closeReader(IndexReader reader) {
    if (reader != null) {
      try {
        reader.close();
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE,"IndexReader failed to close.",e);
      }
    }
  }
  
  /**
   * Closes an index searcher.
   * @param searcher the searcher to close
   */
  public void closeSearcher(IndexSearcher searcher) {
    if (searcher != null) {
      try {
        searcher.close();
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE,"IndexSearcher failed to close.",e);
      }
    }
  }
  
  /**
   * Closes an index writer.
   * @param writer the writer to close
   */
  public void closeWriter(IndexWriter writer) {
    if (writer != null) {
      try {
        writer.close();
      } catch (CorruptIndexException e) {
        LOGGER.log(Level.SEVERE,"IndexWriter failed to close.",e);
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE,"IndexWriter failed to close.",e);
      }
    }
  }
  
  /**
   * Configures the adapter from an index configuration reference.
   * @param indexReference index configuration reference
   */
  public void configure(AsnIndexReference indexReference) {
    this.setIndexReference(indexReference);
  }
  
  /**
   * Gets the Lucene Directory assoctated with the index.
   * @return the directory
   * @throws IOException if an I/O exception occurs
   */
  protected Directory getDirectory() throws IOException {
    File fDir = new File(this.getIndexReference().getIndexLocation());
    NativeFSLockFactory nativeLockFactory = this.getNativeLockFactory();
    if (nativeLockFactory != null) {
      return FSDirectory.open(fDir,nativeLockFactory);
    } else {
      return FSDirectory.open(fDir);
    }
  }
  
  /**
   * Gets the native lock factory if it has been configured for use.
   * @return the native lock factory (null if not configured for use)
   * @throws IOException if an I/O exception occurs
   */
  protected synchronized NativeFSLockFactory getNativeLockFactory() throws IOException {
    NativeFSLockFactory factory = null;
    if (this.getIndexReference().getUseNativeFSLockFactory()) {
      File dir = new File(this.getIndexReference().getIndexLocation());
      String path = dir.getCanonicalPath();
      synchronized (NATIVEFSLOCKFACTORIES) {
        factory = NATIVEFSLOCKFACTORIES.get(path);
        if (factory == null) {
          factory = new NativeFSLockFactory(dir);
          NATIVEFSLOCKFACTORIES.put(path,factory);
        } 
      }
    } 
    return factory;
  }
  
  /**
   * Makes an index reader.
   * <br/>The reader must be closed following use.
   * @return the reader
   * @throws CorruptIndexException if the index is corrupt
   * @throws IOException if an I/O exception occurs
   */
  public IndexReader makeIndexReader() throws CorruptIndexException, IOException  {
    IndexReader reader = IndexReader.open(this.getDirectory(),true);
    return reader;
  }
  
  /**
   * Makes an index writer.
   * <br/>The writer must be closed following use.
   * @param analyzer the analyzer
   * @return the writer
   * @throws CorruptIndexException if the index is corrupt
   * @throws LockObtainFailedException if a write lock cannot be obtained
   * @throws IOException if an I/O exception occurs
   */
  public IndexWriter makeIndexWriter(Analyzer analyzer) 
    throws CorruptIndexException, LockObtainFailedException, IOException {
    IndexWriter.MaxFieldLength mfl = IndexWriter.MaxFieldLength.UNLIMITED;
    return new IndexWriter(this.getDirectory(),analyzer,mfl);
  }
  
  /**
   * Instantiates a new analyzer.
   * @param context the assertion operation context
   * @return the analyzer
   */
  public Analyzer newAnalyzer(AsnContext context) {
    RequestContext rc = null;
    try {
      LuceneIndexAdapter adapter = null;
      if ((context != null) && (context.getRequestContext() != null)) {
        adapter = new LuceneIndexAdapter(context.getRequestContext());
      } else {
        rc = RequestContext.extract(null);
        adapter = new LuceneIndexAdapter(rc);
      }
      return adapter.newAnalyzer();
    } finally {
      if (rc != null) rc.getConnectionBroker().closeAll();
    }
  }
  
  /**
   * Optimizes the index.
   * @throws CorruptIndexException if the index is corrupt
   * @throws LockObtainFailedException if a write lock cannot be obtained
   * @throws IOException if an I/O exception occurs
   */
  public void optimize() 
    throws CorruptIndexException, LockObtainFailedException, IOException {
    IndexWriter writer = null;
    try {
      writer = this.makeIndexWriter(this.newAnalyzer(null));
      writer.optimize();
    } finally {
      this.closeWriter(writer);
    }
  }
  
  /**
   * Opens and closes a writer.
   * <br/>This ensures that a directory folder structure exists.
   * @throws CorruptIndexException if the index is corrupt
   * @throws LockObtainFailedException if a write lock cannot be obtained
   * @throws IOException if an I/O exception occurs
   */
  public void touch() throws CorruptIndexException, LockObtainFailedException, IOException {
    IndexWriter writer = null;
    try {
      writer = this.makeIndexWriter(this.newAnalyzer(null));
    } finally {
      closeWriter(writer);
    }
  }
  
}
