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
import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.catalog.context.CatalogIndexAdapter;
import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.catalog.discovery.IStoreable;
import com.esri.gpt.catalog.discovery.PropertyMeanings;
import com.esri.gpt.catalog.schema.Meaning;
import com.esri.gpt.catalog.schema.MetadataDocument;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.catalog.schema.indexable.Indexables;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.metadata.MetadataAcl;
import com.esri.gpt.framework.util.Val;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.NativeFSLockFactory;
import org.apache.lucene.store.SimpleFSLockFactory;

/**
 * Maintains a Lucene index of approved metadata documents.
 */
public class LuceneIndexAdapter extends CatalogIndexAdapter {
  
  /** class variables ========================================================= */
  
  private static final String BACKGROUND_LOCKNAME = "geoportal-background.lock";
  
  public static final String BATCH_INDEXWRITER_KEY = "LuceneIndexAdapter.BatchIndexWriter";
  
  /** Logger */
  private static final Logger LOGGER = Logger.getLogger(LuceneIndexAdapter.class.getName());
  
  /** Hold NativeFSLockFactory objects statically within the JVM */
  private static final Map<String,NativeFSLockFactory> NATIVEFSLOCKFACTORIES = 
    new HashMap<String,NativeFSLockFactory>();

  /** Hold IndxeSearcher objects statically within the JVM */
  private static final Map<String,IndexSearcher> SEARCHERS = new HashMap<String,IndexSearcher>();
    
  /** instance variables ====================================================== */
  private LuceneConfig luceneConfig;
  private boolean      useSingleSearcher = false;
    
  /** constructors ============================================================ */
  
  /**
   * Constructs with an associated request context.
   * @param requestContext the request context
   */
  public LuceneIndexAdapter(RequestContext requestContext) {
    super(requestContext);
    if (requestContext == null) {
      throw new IllegalArgumentException("The requestContext can't be null.");
    }
    this.luceneConfig = requestContext.getCatalogConfiguration().getLuceneConfig();
    if (this.luceneConfig == null) {
      throw new IllegalArgumentException("The requestContext contains no LuceneConfig.");
    } else if (this.luceneConfig.getIndexLocation().length() == 0) {
      throw new IllegalArgumentException("The LuceneConfig indexLocation is empty.");
    }
    
    StringAttributeMap params = requestContext.getCatalogConfiguration().getParameters();
    String param = Val.chkStr(params.getValue("lucene.useSingleSearcher"));
    this.useSingleSearcher = param.equalsIgnoreCase("true");
  }
  
  /** properties ============================================================== */
    
  /**
   * Gets the logger.
   * @return the logger
   */
  @Override
  protected Logger getLogger() {
    return LOGGER;
  }
  
  /**
   * Gets the Lucene configuration.
   * @return the Lucene configuration
   */
  protected LuceneConfig getLuceneConfig() {
    return this.luceneConfig;
  }
  
  /** methods ================================================================= */
  
  /**
   * Closes an index searcher.
   * @param searcher the searcher to close.
   */
  public void closeSearcher(IndexSearcher searcher) {
    try {
      if ((searcher != null) && !this.useSingleSearcher) {
        getLogger().finer("Closing Lucene IndexSearcher...");
        searcher.getIndexReader().close();
        searcher.close();
      }
    } catch (Throwable t) {
      getLogger().log(Level.WARNING,"IndexSearcher failed to close.",t);
    }
  }
  
  /**
   * Closes an index writer.
   * @param writer the writer to close.
   */
  protected void closeWriter(IndexWriter writer) {
    try {
      if (writer != null) {
        getLogger().finer("Closing Lucene IndexWriter...");
        writer.close();
      }
    } catch (Throwable t) {
      getLogger().log(Level.WARNING,"IndexWriter failed to close.",t);
          
      // There are times when closing the IndexWriter fails (typically FileNotFound while flushing).
      // This is a bit of a disaster, if it happens we're unsure if the index is corrupted, it
      // also leaves the write lock unreleased, which basically is terminal when a 
      // NativeFSLockFactory is active (you need to stop/start the web server)
      // This is an attempt to forcibly release the lock.
      if ((writer != null) && this.luceneConfig.getUseNativeFSLockFactory()) {
        java.lang.reflect.Field wlFld = null;
        try {
          wlFld = writer.getClass().getDeclaredField("writeLock");
        } catch (Throwable t2) {}   
        if (wlFld != null) {
          boolean wasReleased = false;
          try {
            wlFld.setAccessible(true);
            Object wlObj = wlFld.get(writer);
            if ((wlObj != null) && (wlObj instanceof Lock)) {
              Lock wlLock = (Lock)wlObj;
              wasReleased = !wlLock.isLocked();
              if (!wasReleased) {
                wlLock.release();
                wasReleased = !wlLock.isLocked();
              } 
            }
          } catch (Throwable t2) {
            getLogger().log(Level.WARNING,"Unable to forcibly release an abandoned write lock.",t2);
          } finally {
            String sMsg = "The IndexWriter failed to close, write-lock released: "+wasReleased;
            getLogger().warning(sMsg);
          }
        }
      } 
      
    }
  }
  
  /**
   * Counts the documents within the index.
   * @throws CatalogIndexException if an exception occurs
   */
  @Override
  public int countDocuments() throws CatalogIndexException { 
    IndexWriter writer = null;
    try {
      writer = newWriter();
      return writer.maxDoc();
    } catch (Exception e) {
      String sMsg = "Error accessing index:\n "+Val.chkStr(e.getMessage());
      throw new CatalogIndexException(sMsg,e);
    } finally {
      closeWriter(writer);
    }
  }
  
  /**
   * Deletes a collection of documents from the index.
   * @param uuids the collection of document UUIDS to delete 
   * @throws CatalogIndexException if an exception occurs  
   */
  @Override
  public void deleteDocuments(String[] uuids) 
    throws CatalogIndexException {
    IndexWriter writer = null;
    try {
      if ((uuids != null) && (uuids.length > 0)) {
        StringBuilder sbMsg = new StringBuilder("Removing UUIDs from the catalog index:");
        ArrayList<Term> alTerms = new ArrayList<Term>();
        for (String uuid: uuids) {
          String sUuid = Val.chkStr(uuid);
          if (sUuid.length() > 0) {
            alTerms.add(new Term(Storeables.FIELD_UUID,sUuid));
            if (getLogger().isLoggable(Level.FINER)) {
              sbMsg.append("\n ").append(sUuid);
            }
          }
        }
        if (alTerms.size() > 0) {
          if (getLogger().isLoggable(Level.FINER)) {
            getLogger().finer(sbMsg.toString());
          }
          Term[] aTerms = alTerms.toArray(new Term[0]);
          writer = newWriter();
          writer.deleteDocuments(aTerms);
          
          // writer.optimize(); // optimization was moved to a scheduled thread - LuceneIndexOptimizer
        }
      }
    } catch (Exception e) {
      String sMsg = "Error deleting document(s):\n "+Val.chkStr(e.getMessage());
      throw new CatalogIndexException(sMsg,e);
    } finally {
      closeWriter(writer);
    }
  }
  
  /**
   * Gets the native lock factory if it has been configured for use.
   * @return the native lock factory (null if not configured for use)
   * @throws IOException if an exception occurs
   */
  private synchronized NativeFSLockFactory getNativeLockFactory() throws IOException {
    NativeFSLockFactory factory = null;
    if (this.luceneConfig.getUseNativeFSLockFactory()) {
      File dir = new File(this.luceneConfig.getIndexLocation());
      String path = dir.getCanonicalPath();
      synchronized (NATIVEFSLOCKFACTORIES) {
        factory = NATIVEFSLOCKFACTORIES.get(path);
        if (factory == null) {
          factory = new NativeFSLockFactory(dir);
          NATIVEFSLOCKFACTORIES.put(path,factory);
        } 
      }
      //if (NATIVEFSLOCKFACTORY == null) {
      //  File lDir = new File(this.luceneConfig.getIndexLocation());
      //  NATIVEFSLOCKFACTORY = new NativeFSLockFactory(lDir);
      //}
      //return NATIVEFSLOCKFACTORY;
    } 
    return factory;
  }
  
  /**
   * Makes an analyzer for catalog documents.
   * <br/>Currently returns a StandardAnalyzer.
   * @return the analyzer
   */
  public Analyzer newAnalyzer() {
    String className = Val.chkStr(this.luceneConfig.getAnalyzerClassName());
    try {
      if ((className.length() == 0) || className.equals("org.apache.lucene.analysis.standard.StandardAnalyzer")) {
        return new StandardAnalyzer(org.apache.lucene.util.Version.LUCENE_30);
      } else {
        Class<?> cls = Class.forName(className);
        Object obj = cls.newInstance();
        if (obj instanceof Analyzer) {
          return (Analyzer)obj;
        } else {
          String sMsg = "The configured Lucene analyzer class name is invalid: "+className;
          getLogger().severe(sMsg);
        }
      }
    } catch (Throwable t) {
      String sMsg = "Error instantiating Lucene analyzer: "+className;
      getLogger().log(Level.SEVERE,sMsg,t);
    }
    return new StandardAnalyzer(org.apache.lucene.util.Version.LUCENE_30);
  }
  
  /**
   * Gets the Lucene Directory the has been configured to store the index.
   * @return the directory
   * @throws IOException if an exception occurs
   */
  private Directory newDirectory() throws IOException {
    File fDir = new File(this.luceneConfig.getIndexLocation());
    NativeFSLockFactory nativeLockFactory = this.getNativeLockFactory();
    if (nativeLockFactory != null) {
      return FSDirectory.open(fDir,nativeLockFactory);
    } else {
      return FSDirectory.open(fDir);
    }
  }
  
  /**
   * Makes a searcher for catalog documents.
   * <p/>The searcher is created from the value returned by 
   * getCatalogIndexPath().
   * @return the writer
   * @throws CorruptIndexException if the index is corrupt 
   * @throws IOException if the directory cannot be read/written to, 
   *         or if it does not exist and create is false or if there is any 
   *         other low-level IO error
   */
  public synchronized IndexSearcher newSearcher() 
    throws CorruptIndexException, IOException {

    IndexSearcher searcher = null;
    if (!this.useSingleSearcher) {
      getLogger().finer("Opening Lucene IndexSearcher...");
      IndexReader reader = IndexReader.open(this.newDirectory(),true);
      searcher = new IndexSearcher(reader);
    } else {
    
      File fDir = new File(this.luceneConfig.getIndexLocation());
      String path = fDir.getCanonicalPath();
      synchronized (SEARCHERS) {
        searcher = SEARCHERS.get(path);
        if (searcher != null) {
          try {
            if (!searcher.getIndexReader().isCurrent()) {
              SEARCHERS.remove(path);
              searcher.getIndexReader().close();
              searcher.close();
              searcher = null;
            }
          } catch (AlreadyClosedException e) {
            SEARCHERS.remove(path);
            searcher = null;
          }
        }
        if (searcher == null) {
          IndexReader reader = IndexReader.open(this.newDirectory(),true);
          searcher = new IndexSearcher(reader);
          SEARCHERS.put(path,searcher);
        } 
      }
    }
    return searcher;
  }
    
  /**
   * Makes a writer for catalog documents.
   * <p/>The writer is created from the values returned by 
   * getCatalogIndexPath() and newCatalogAnalyzer(). 
   * <br/>The index will be creating it if it does not already exist.
   * <br/>
   * The writer must be closed after use.
   * @return the writer
   * @throws CorruptIndexException if the index is corrupt 
   * @throws LockObtainFailedException if another writer has this index 
   *         open (write.lock could not be obtained) 
   * @throws IOException if the directory cannot be read/written to, 
   *         or if it does not exist and create is false or if there is any 
   *         other low-level IO error
   */
  protected IndexWriter newWriter() 
    throws CorruptIndexException, LockObtainFailedException, IOException {
    return newWriter(false);
  }
  
  /**
   * Makes a writer for catalog documents.
   * <p/>The writer is created from the values returned by 
   * getCatalogIndexPath() and newCatalogAnalyzer(). 
   * <br/>The index will be creating it if it does not already exist.
   * <br/>
   * The writer must be closed after use.
   * @param forCompleteRebuild true if index will be completely rebuilt
   * @return the writer
   * @throws CorruptIndexException if the index is corrupt 
   * @throws LockObtainFailedException if another writer has this index 
   *         open (write.lock could not be obtained) 
   * @throws IOException if the directory cannot be read/written to, 
   *         or if it does not exist and create is false or if there is any 
   *         other low-level IO error
   */
  private IndexWriter newWriter(boolean forCompleteRebuild) 
    throws CorruptIndexException, LockObtainFailedException, IOException {
    File f = new File(this.luceneConfig.getIndexLocation());
    getLogger().log(Level.FINER, "Creating Lucene IndexWriter for: {0}", f.getAbsolutePath());
    IndexWriter.MaxFieldLength mfl = IndexWriter.MaxFieldLength.UNLIMITED;
    if (!forCompleteRebuild) {
      return new IndexWriter(this.newDirectory(),this.newAnalyzer(),mfl);
    } else {
      return new IndexWriter(this.newDirectory(),this.newAnalyzer(),true,mfl);
    }
  }
  
  /**
   * Obtains a write lock intended for background synchronization and 
   * optimization processes.
   * <br/>There will be no wait time for this type of lock, if not obtained 
   * immediately an exception will be thrown.
   * <br/>It is recommended to use LuceneIndexAdapter.touck() prior to
   * obtaining the background lock to ensure a proper directory structure.
   * <br/>The lock must be closed when the background process is complete.
   * @return the obtained lock
   * @throws LockObtainFailedException if the lock wwas not obtained
   */
  public synchronized Lock obtainBackgroundLock() throws LockObtainFailedException {
    Lock lock = null;
    String pfx = "Unable to obtain background lock.";
    if (this.luceneConfig.getUseNativeFSLockFactory()) {
      try {
        NativeFSLockFactory nativeLockFactory = this.getNativeLockFactory();
        lock = nativeLockFactory.makeLock(BACKGROUND_LOCKNAME);
      } catch (IOException e) {
        String msg = pfx+" "+e.getMessage();
        getLogger().log(Level.WARNING,pfx,e);
        throw new LockObtainFailedException(msg);
      }
    } else {
      try {
        File fDir = new File(this.luceneConfig.getIndexLocation());
        SimpleFSLockFactory factory = new SimpleFSLockFactory(fDir);
        factory.setLockPrefix("lucene-simple");
        lock = factory.makeLock(BACKGROUND_LOCKNAME);
      } catch (IOException e) {
        String msg = pfx+" "+e.getMessage();
        getLogger().log(Level.WARNING,pfx,e);
        throw new LockObtainFailedException(msg);
      }
    }
    try {
      boolean wasObtained = lock.obtain();
      if (!wasObtained) {
        String msg = "Unable to obtain background lock for: "+lock.toString();
        throw new LockObtainFailedException(msg);
      }
      return lock;
    } catch (LockObtainFailedException e) {
      throw e;
    } catch (IOException e) {
      String msg = pfx+" "+e.getMessage();
      getLogger().log(Level.WARNING,pfx,e);
      throw new LockObtainFailedException(msg);
    }
  }

    
  @Override
  public void publishDocument(String uuid, 
                              Timestamp updateDate, 
                              Schema schema,
                              String acl)
    throws CatalogIndexException {
    uuid = Val.chkStr(uuid);
    if (uuid.length() == 0) {
      throw new IllegalArgumentException("The supplied document UUID was empty.");
    } 
    
    IndexWriter writer = null;
    PreparedStatement st = null;
    try {
      
      // determine the storeables
      Document document = new Document();    
      Storeables storeables = null;
      PropertyMeanings meanings = null;
      Indexables indexables = schema.getIndexables();
      if ((indexables != null) && (indexables.getIndexableContext() != null)) {
        meanings = indexables.getIndexableContext().getPropertyMeanings();
        storeables = (Storeables)indexables.getIndexableContext().getStoreables();
      }
      if (storeables == null) {
        meanings= schema.getMeaning().getPropertyMeanings();
        storeables = (Storeables)schema.getMeaning().getStoreables();
      }
      
      // resolve the thumbnail URL          
      if (Val.chkStr(schema.getMeaning().getThumbnailUrl()).length() == 0) {
        String thumbBinary = Val.chkStr(schema.getMeaning().getThumbnailBinary());
        if ((thumbBinary != null) && (thumbBinary.length() > 0)) {
          String thumbUrl = "/thumbnail?uuid="+URLEncoder.encode(uuid,"UTF-8");
          //IStoreable storeable = schema.getMeaning().getStoreables().get(Meaning.MEANINGTYPE_THUMBNAIL_URL);
          IStoreable storeable = storeables.get(Meaning.MEANINGTYPE_THUMBNAIL_URL);
          if (storeable != null) {
            storeable.setValue(thumbUrl);
          } else {
            storeables.ensure(meanings,Meaning.MEANINGTYPE_THUMBNAIL_URL).setValue(thumbUrl);
          }
        }
      }
        
      // build the ACL property for the document
      acl = Val.chkStr(acl);
      MetadataAcl oAcl = new MetadataAcl(this.getRequestContext());
      String[] aclValues = oAcl.makeDocumentAcl(acl);
      AclProperty aclProp = new AclProperty(Storeables.FIELD_ACL);
      aclProp.setValues(aclValues);
      
      // build the document to store 
      storeables.ensure(meanings,Storeables.FIELD_UUID).setValue(uuid);
      storeables.ensure(meanings,Storeables.FIELD_DATEMODIFIED).setValue(updateDate);
      storeables.add(aclProp);
      
      // document XML
      String xml = Val.chkStr(schema.getActiveDocumentXml());
      String fldName = Storeables.FIELD_XML;
      LOGGER.log(Level.FINER, "Appending field: {0}", fldName);
      Field fld = new Field(fldName,xml,Field.Store.YES,Field.Index.NO,Field.TermVector.NO);
      document.add(fld);
      
      // add additional indexable fields based upon the SQL database record
      boolean bReadDB = true;
      if (bReadDB) {
        CatalogConfiguration cfg = this.getRequestContext().getCatalogConfiguration();
        this.getRequestContext().getCatalogConfiguration().getResourceTableName();
        String sql = "SELECT SITEUUID, TITLE FROM "+cfg.getResourceTableName()+" WHERE DOCUUID=?";
        Connection con = this.returnConnection().getJdbcConnection();
        this.logExpression(sql);
        st = con.prepareStatement(sql);
        st.setString(1,uuid);
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
          
          String dbVal = Val.chkStr(rs.getString("SITEUUID"));
          if (dbVal.length() > 0) {
            //storeables.ensure(meanings,Storeables.FIELD_SITEUUID).setValue(dbVal);
            fldName = Storeables.FIELD_SITEUUID;
            LOGGER.log(Level.FINER, "Appending field: {0} ={1}", new Object[]{fldName, dbVal});
            fld = new Field(fldName,dbVal,Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
            document.add(fld);
          }

          dbVal = Val.chkStr(rs.getString("TITLE"));
          if (dbVal.length() > 0) {
            // if the title is found and is different than that in the database
            // it means that title from the database is typed by the user. In
            // that case make 'title.org' element based on the current title.
            IStoreable iTitle = storeables.get(Meaning.MEANINGTYPE_TITLE);
            if (iTitle!=null) {
              Object [] values = iTitle.getValues();
              if (values.length>0 && values[0] instanceof String) {
                String val = (String)values[0];
                if (!val.equals(dbVal)) {
                  storeables.ensure(meanings,Meaning.MEANINGTYPE_TITLE_ORG).setValue(val);
                }
              }
            }
            // ensure the title from the database
            storeables.ensure(meanings,Meaning.MEANINGTYPE_TITLE).setValue(dbVal);
          }
          
        } 
        st.close();
        st = null;
      }

      for (IStoreable ist: storeables.collection()) {
        Storeable storeable = (Storeable)ist;
        storeable.appendForWrite(document);
      }
      
      // cswOutputSchema, cswBriefXml, cswSummaryXml
      String cswOutputSchema = Val.chkStr(schema.getCswOutputSchema());
      if (cswOutputSchema.length() > 0) {
        fldName = Storeables.FIELD_SCHEMA;
        LOGGER.log(Level.FINER, "Appending field: {0} ={1}", new Object[]{fldName, cswOutputSchema});
        fld = new Field(fldName,cswOutputSchema,Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
        document.add(fld);
      }
      String briefXslt = Val.chkStr(schema.getCswBriefXslt());
      if (briefXslt.length() > 0) {
        MetadataDocument mdDoc = new MetadataDocument();
        String briefXml = mdDoc.transform(xml,briefXslt);
        fldName = Storeables.FIELD_XML_BRIEF;
        LOGGER.log(Level.FINER, "Appending field: {0}", fldName);
        fld = new Field(fldName,briefXml,Field.Store.YES,Field.Index.NO,Field.TermVector.NO);
        document.add(fld);
      }
      String summaryXslt = Val.chkStr(schema.getCswSummaryXslt());
      if (summaryXslt.length() > 0) {
        MetadataDocument mdDoc = new MetadataDocument();
        String summaryXml = mdDoc.transform(xml,summaryXslt);
        fldName = Storeables.FIELD_XML_SUMMARY;
        LOGGER.log(Level.FINER, "Appending field: {0}", fldName);
        fld = new Field(fldName,summaryXml,Field.Store.YES,Field.Index.NO,Field.TermVector.NO);
        document.add(fld);
      }
      
      // check for to see if a batch IndexWriter has been placed within the RequestContext objectMap,
      // this is useful for batch processes where open/close/optimize on the index is costly
      IndexWriter batchWriter = null;
      Object o = this.getRequestContext().getObjectMap().get(LuceneIndexAdapter.BATCH_INDEXWRITER_KEY);
      if (o != null && (o instanceof IndexWriter)) {
        batchWriter = (IndexWriter)o;
      }
      
      // write the document (use update to replace an existing document),
      Term term = new Term(Storeables.FIELD_UUID,uuid);
      if (batchWriter != null) {
        batchWriter.updateDocument(term,document);
      } else {
        writer = newWriter();
        writer.updateDocument(term,document);
      }
    } catch (Exception e) {
      String sMsg = "Error indexing document:\n "+Val.chkStr(e.getMessage());
      throw new CatalogIndexException(sMsg,e);
    } finally {
      try {if (st != null) st.close();} catch (Exception ef) {}
      closeWriter(writer);
    }
    
  }
  
  /**
   * Purges the entire catalog index.
   * @throws CatalogIndexException if an exception occurs
   */
  @Override
  public void purgeIndex() throws CatalogIndexException {
    IndexWriter writer = null;
    try {
      getLogger().info("Emptying Lucene index...");
      writer = newWriter(true);
    } catch (Exception e) {
      String sMsg = "Error purging index:\n "+Val.chkStr(e.getMessage());
      throw new CatalogIndexException(sMsg,e);
    } finally {
      closeWriter(writer);
    }
  }
   
  /**
   * Queries the ACL values indexed for a document.
   * @param uuid the document UUID
   * @return the ACL values (can be null)
   * @throws CatalogIndexException if an exception occurs
   */
  @Override
  public String[] queryAcls(String uuid)  throws CatalogIndexException {
    ArrayList<String> values = new ArrayList<String>();
    IndexSearcher searcher = null;
    TermDocs termDocs = null;
    try {
      uuid = Val.chkStr(uuid);
      if (uuid.length() > 0) {
        searcher = newSearcher();
        String[] aFields = new String[]{Storeables.FIELD_ACL};
        MapFieldSelector selector = new MapFieldSelector(aFields);
        searcher = newSearcher();
        IndexReader reader = searcher.getIndexReader();
        termDocs = reader.termDocs();
        termDocs.seek(new Term(Storeables.FIELD_UUID,uuid));
        if (termDocs.next()) {
          Document document = reader.document(termDocs.doc(),selector);
          Field[] fields = document.getFields(Storeables.FIELD_ACL);
          if ((fields != null) && (fields.length > 0)) {
            for (Field field: fields) {
              values.add(field.stringValue());
            }
          }
        } 
      }
    } catch (IOException e) {
      String sMsg = "Error accessing index:\n "+Val.chkStr(e.getMessage());
      throw new CatalogIndexException(sMsg,e);
    } finally {
      try {if (termDocs != null) termDocs.close();} catch (Exception ef) {}
      closeSearcher(searcher);
    }
    return values.toArray(new String[0]);
  }
  
  /**
   * Queries the system modified date associated with an indexed document.
   * @param uuid the document UUID
   * @return the update date (null if none was found)
   * @throws CatalogIndexException if an exception occurs
   */
  @Override
  public Timestamp queryModifiedDate(String uuid) throws CatalogIndexException {
    Timestamp tsUpdate = null;
    IndexSearcher searcher = null;
    TermDocs termDocs = null;
    try {
      uuid = Val.chkStr(uuid);
      if (uuid.length() > 0) {
        String[] aFields = new String[]{Storeables.FIELD_DATEMODIFIED};
        MapFieldSelector selector = new MapFieldSelector(aFields);
        searcher = newSearcher();
        IndexReader reader = searcher.getIndexReader();
        termDocs = reader.termDocs();
        termDocs.seek(new Term(Storeables.FIELD_UUID,uuid));
        if (termDocs.next()) {
          Document document = reader.document(termDocs.doc(),selector);
          String sUpdate = document.get(Storeables.FIELD_DATEMODIFIED);
          tsUpdate = new Timestamp(Long.valueOf(sUpdate));
        }
      }
    } catch (IOException e) {
      String sMsg = "Error accessing index:\n "+Val.chkStr(e.getMessage());
      throw new CatalogIndexException(sMsg,e);
    } finally {
      try {if (termDocs != null) termDocs.close();} catch (Exception ef) {}
      closeSearcher(searcher);
    }
    return tsUpdate;
  }
  
  /**
   * Reads the document UUIDs within the index.
   * @param maxUuids the maximum number to read
   * @param startIndex the index to begin reading
   * @return the set of UUIDs
   * @throws CatalogIndexException if an exception occurs
   */
  private StringSet readUuids(int startIndex, int maxUuids) throws CatalogIndexException {
    StringSet ssUuids = new StringSet();
    IndexSearcher searcher = null; 
    TermEnum terms = null;
    try {
      String sField = Storeables.FIELD_UUID;
      searcher = newSearcher();
      terms = searcher.getIndexReader().terms(new Term(sField,""));
      int nCount = 0;
      while (sField.equals(terms.term().field())) {        
        if(nCount >= startIndex){
         ssUuids.add(terms.term().text());
        }
        nCount++;
        if (nCount >= (startIndex + maxUuids)) break;
        if (!terms.next()) break;
      }
      
    } catch (Exception e) {
      String sMsg = "Error accessing index:\n "+Val.chkStr(e.getMessage());
      throw new CatalogIndexException(sMsg,e);
    } finally {
      try {if (terms != null) terms.close();} catch (Exception ef) {}
      closeSearcher(searcher);
    }
    return ssUuids;
  }
  
  /**
   * Opens and closes a writer.
   * <br/>This ensures that a directory folder structure exists.
   * @throws CatalogIndexException if an exception occurs
   */
  public void touch() throws CatalogIndexException { 
    IndexWriter writer = null;
    try {
      writer = newWriter();
    } catch (Exception e) {
      String sMsg = "Error accessing index:\n "+Val.chkStr(e.getMessage());
      throw new CatalogIndexException(sMsg,e);
    } finally {
      closeWriter(writer);
    }
  }

}
