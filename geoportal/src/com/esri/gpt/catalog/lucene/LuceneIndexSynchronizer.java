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
import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.catalog.management.CollectionDao;
import com.esri.gpt.catalog.schema.MetadataDocument;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.catalog.schema.SchemaException;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.metadata.MetadataAcl;
import com.esri.gpt.framework.sql.IClobMutator;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * A process to ensure that the Lucene index in synchronized with the database catalog.
 */
public class LuceneIndexSynchronizer {
    
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(LuceneIndexSynchronizer.class.getName());
  
  /** Running status. */
  public static volatile boolean RUNNING = false;
  
  /** instance variables ====================================================== */
  private LuceneIndexAdapter     adapter;
  private Connection             con;
  private RequestContext         context;
  private int                    feedbackMillis = (120 * 1000);
  private int                    maxDeleteTokens = 1000;
  private int                    maxSqlTokens = 1000;
  private int                    maxUuidCache = 100000;
  private IClobMutator           mutator;
  private IndexReader            reader;
  private String                 resourceDataTable;
  private String                 resourceTable;
  private IndexSearcher          searcher;
  private long                   startMillis = 0;
  private HashMap<String,String> synchedUuidCache = new HashMap<String,String>();
  private StringAttributeMap     taskParams;
  private boolean                wasInterrupted = false;
  private IndexWriter            writer;
  
  /** constructors ============================================================ */

  /**
   * Constructs with the configuration parameters associated with a scheduled task.
   */
  public LuceneIndexSynchronizer(StringAttributeMap scheduledTaskParameters) {
    this.taskParams = scheduledTaskParameters;
    if (this.taskParams != null) {
      int nParam;
      nParam = Val.chkInt(this.taskParams.getValue("feedbackSeconds"),-1);
      if (nParam > 0) this.feedbackMillis = (nParam * 1000);
      nParam = Val.chkInt(this.taskParams.getValue("maxDeleteTokens"),-1);
      if (nParam > 0) this.maxDeleteTokens = nParam;
      nParam = Val.chkInt(this.taskParams.getValue("maxSqlTokens"),-1);
      if (nParam > 0) this.maxSqlTokens = nParam;
      nParam = Val.chkInt(this.taskParams.getValue("maxUuidCache"),-1);
      if (nParam > 0) this.maxUuidCache = nParam;
    }
  }
  
  /** methods ================================================================= */
  
  /**
   * Checks to see if the thread was interrupted.
   * @return true if the thread was interrupted
   */
  private boolean checkInterrupted() {
    if (!this.wasInterrupted) {
      if (Thread.interrupted()) {
        this.wasInterrupted = true;
      }
    }
    return this.wasInterrupted;
  }
    
  /**
   * Closes open connections to the database and index.
   */
  private void closeAll() {
    try {      
      try {
        if ((this.adapter != null) && (this.searcher != null)) {
          this.adapter.closeSearcher(this.searcher);
        }
      } catch (Exception ef) {
        LOGGER.log(Level.WARNING,"IndexSearcher failed to close.",ef);
      }
      try {
        if ((this.adapter != null) && (this.writer != null)) {
          this.adapter.closeWriter(this.writer);
        }
      } catch (Exception ef) {
        LOGGER.log(Level.WARNING,"IndexWriter failed to close.",ef);
      } finally {
        this.writer = null;
      }
      try {
        if (this.context != null) {
          this.context.getConnectionBroker().closeAll();
        }
      } catch (Exception ef) {
        LOGGER.log(Level.WARNING,"JDBC connection failed to close.",ef);
      }
    } finally {
      this.reader = null;
      this.searcher = null;
      this.con = null;
    }
  }
   
  /**
   * Counts the documents within the database.
   * @param info synchronization step information
   * @throws SQLException if an exception occurs while communicating with the database
   */
  private void countDatabaseDocs(CountInfo info) throws SQLException {
    long tStartMillis = System.currentTimeMillis();
    PreparedStatement st = null;
    try {
      String sql = "SELECT COUNT(*) FROM "+this.resourceTable;
      LOGGER.finest(sql);
      st = this.con.prepareStatement(sql);
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        info.numDatabaseDocs = rs.getInt(1);
      }
      LOGGER.fine(info.getStepMessage("countDatabaseDocs",info.numDatabaseDocs,tStartMillis));
    } finally {
      try {if (st != null) st.close();} catch (Exception ef) {}
    }
  }
  
  /**
   * Counts the indexable (approved/reviewed) documents within the database.
   * @param info synchronization step information
   * @throws SQLException if an exception occurs while communicating with the database
   */
  private void countIndexableDocs(CountInfo info) throws SQLException {
    long tStartMillis = System.currentTimeMillis();
    PreparedStatement st = null;
    try {
      String sql = "SELECT COUNT(*) FROM "+this.resourceTable;
      sql += " WHERE (APPROVALSTATUS = ?) OR (APPROVALSTATUS = ?)";
      LOGGER.finest(sql);
      st = this.con.prepareStatement(sql);
      st.setString(1,"approved");
      st.setString(2,"reviewed");
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        info.numIndexableDocs = rs.getInt(1);
      }
      LOGGER.fine(info.getStepMessage("countIndexableDocs",info.numIndexableDocs,tStartMillis));
    } finally {
      try {if (st != null) st.close();} catch (Exception ef) {}
    }
  }
  
  /**
   * Deletes a set of documents from the index.
   * @param uuids the set of document UUIDS to delete
   * @throws CatalogIndexException if an exception occurs during deletion
   */
  private void deleteDocuments(StringSet uuids) throws CatalogIndexException {
    if (uuids.size() > 0) {
      this.adapter.deleteDocuments(uuids.toArray(new String[0]));
    }
  }
  
  /**
   * Ensure that a set of documents found within the index are still indexable.
   * <br/>Deletions are performed as required.
   * @param uuids the set of document UUIDS to check
   * @throws SQLException if an exception occurs while communicating with the database
   * @throws CatalogIndexException if an exception occurs during deletion
   */
  private void ensureIndexable(WalkIndexInfo info, StringSet uuids, StringSet delUuids) 
    throws SQLException, CatalogIndexException {
    long tStartMillis = System.currentTimeMillis();
    PreparedStatement st = null;
    try {
      if (uuids.size() == 0) return;
      
      // build the database query
      StringBuffer tokens = new StringBuffer();
      for (String uuid: uuids) {
        if (tokens.length() > 0) tokens.append(",");
        tokens.append("'").append(uuid).append("'");
      }
      StringBuffer sb = new StringBuffer();
      sb.append("SELECT DOCUUID FROM ").append(this.resourceTable);
      sb.append(" WHERE ((APPROVALSTATUS = ?) OR (APPROVALSTATUS = ?))");
      sb.append(" AND DOCUUID IN (").append(tokens).append(")");
      String sql = sb.toString();
      LOGGER.finest(sql);
      
      // execute the query, remove indexable uuids from the set
      st = this.con.prepareStatement(sql);
      st.setString(1,"approved");
      st.setString(2,"reviewed");
      ResultSet rs = st.executeQuery();
      if (checkInterrupted()) return;
      while (rs.next()) {
        info.numFoundInDb++;
        String uuid = rs.getString(1);
        uuids.remove(uuid);
      }
      if (checkInterrupted()) return;
      info.dbMillis += (System.currentTimeMillis() - tStartMillis);
      
      // add non-indexable uuids to the deletion set
      for (String uuid: uuids) {
        info.numRequiringDelete++;
        delUuids.add(uuid);
        if (delUuids.size() >= this.maxDeleteTokens) {
          if (this.checkInterrupted()) return;
          this.deleteDocuments(delUuids);
          info.numDocsDeleted += delUuids.size();
          delUuids.clear();
          if (this.checkInterrupted()) return;
        }
      }
      
    } finally {
      try {if (st != null) st.close();} catch (Exception ef) {}
    }
  }
    
  /**
   * Closes and open resources.
   * @throws Throwable if an exception occurs
   */
  @Override
  public void finalize() throws Throwable {
    try {
      this.closeAll();
    } finally {
      super.finalize();
    }
  }
  
  /**
   * Parses a database acl XML string into string srray of principals.
   * <br/>Note: this method is used in place of MetadataAcl.makeDocumentAcl for
   * permormance. The xml is not parsed into an XML document, it's parsed
   * purely with String.indexOf and String.substring.
   * @param aclXml the acl store dwithin the database
   * @return the string array of principals
   */
  private String[] parseAcl(String aclXml) {
    ArrayList<String> list = new ArrayList<String>();
    aclXml = Val.chkStr(aclXml);
    if (aclXml.length() > 0) {
      String sTmp = "<principal type=\"groupDn\">";
      String sTmp2 = "</principal>";
      String group;
      int nIdx = aclXml.indexOf(sTmp);
      //if (nIdx != -1) {
      while (nIdx != -1) {
        aclXml = aclXml.substring(nIdx+(sTmp.length()));
        int nIdx2 = aclXml.indexOf(sTmp2);
        group = Val.chkStr(aclXml.substring(0,nIdx2));
        if (group.length() > 0) {
          group = "g."+group;
          list.add(group);
        }
        aclXml = aclXml.substring(nIdx2+(sTmp2.length()));
        nIdx = aclXml.indexOf(sTmp);
      }
    }
    //for (String s: list) System.err.println(s);
    return list.toArray(new String[0]);
  }
  
  /**
   * Reads the context of the metadata XML column.
   * <br/>This is not applicable when the ArcIMS metadata server is active.
   * @param uuid the UUID for the record to read
   * @throws SQLException if a database exception occurs
   */
  private String readXml(String uuid) throws SQLException {
    PreparedStatement st = null;
    try {
      String sql = "SELECT XML FROM "+this.resourceDataTable+" WHERE DOCUUID=?";
      st = con.prepareStatement(sql.toString());
      st.setString(1,uuid);
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        return this.mutator.get(rs,1);
      }
    } finally {
      try {if (st != null) st.close();} catch (Exception ef) {}
    }
    return null;
  }
    
  /**
   * Executes the synchronization process.
   */
  public void syncronize() {
    LOGGER.info("Synchronization run started..."); 
    if (RUNNING) {
      LOGGER.log(Level.INFO,"Synchronization run aborted, reason: already running.");
      return;
    }
    RUNNING = true;
    Lock backgroundLock = null;
    try {
      
      // initialize
      int numDeleted = 0;
      int numUpdated = 0;
      this.context = RequestContext.extract(null);
      this.startMillis = System.currentTimeMillis();
      StringAttributeMap catParams = this.context.getCatalogConfiguration().getParameters();
      
      // initialize index
      this.context.getObjectMap().put("lucene.useRemoteWriter",false);
      this.adapter = new LuceneIndexAdapter(context);
      if (this.adapter == null) {
        LOGGER.severe("A valid CatalogIndexAdapter cound not be initialized.");
      }
      this.adapter.touch(); // ensures that a proper directory structure exists
      if (this.checkInterrupted()) return;
      if (!this.adapter.getUsingSingleWriter()) {
        backgroundLock = this.adapter.obtainBackgroundLock();
      }
      this.searcher = this.adapter.newSearcher();
      this.reader = this.searcher.getIndexReader();
      if (this.checkInterrupted()) return;
      
      if (this.adapter.getUsingSingleWriter()) {
        this.writer = this.adapter.newWriter();
        this.adapter.setAutoCommitSingleWriter(false);
      }
      
      // initialize database
      ManagedConnection mc = this.context.getConnectionBroker().returnConnection("");
      this.con = mc.getJdbcConnection();
      this.mutator = mc.getClobMutator();
      this.resourceTable =  this.context.getCatalogConfiguration().getResourceTableName();
      this.resourceDataTable =  this.context.getCatalogConfiguration().getResourceDataTableName(); 
      
      // count current documents within the index and database
      CountInfo countInfo = new CountInfo();
      countInfo.numIndexedDocs = this.reader.numDocs();
      this.countDatabaseDocs(countInfo);
      if (this.checkInterrupted()) return;
      this.countIndexableDocs(countInfo);
      if (this.checkInterrupted()) return;
      
      // purge the index if required
      if (countInfo.numIndexableDocs == 0) {
        if (countInfo.numIndexedDocs > 0) {
          String p = Val.chkStr(catParams.getValue(
              "lucene.synchronizer.allowFullIndexPurge"));
          boolean bAllowFullIndexPurge = p.equalsIgnoreCase("true");
          if (bAllowFullIndexPurge) {
            numDeleted = countInfo.numIndexedDocs;
            this.closeAll();
            this.adapter.purgeIndex();
          } else {
            this.closeAll();
            LOGGER.severe("The database contains no indexable documents," +
          		" the Lucene index contains "+countInfo.numIndexedDocs+
          		" indexed documents. A manual purge of the index is recommended.");
          }
        }
      } else {
        
        // walk the database
        WalkDatabaseInfo walkDbInfo = new WalkDatabaseInfo(countInfo);
        this.walkDatabase(walkDbInfo);
        numDeleted = walkDbInfo.numDocsDeleted;
        numUpdated = walkDbInfo.numDocsUpdated;
        if (this.checkInterrupted()) return;
        
        // determine if the index should be walked for deletions
        boolean bWalkIndex = true;
        if (walkDbInfo.numOriginallyIndexed == 0) {
          bWalkIndex = false;
        } else {
          boolean bConsistent = (walkDbInfo.numIndexable == walkDbInfo.numIndexableFound) &&
                                (walkDbInfo.numIndexable == walkDbInfo.numOriginallyIndexed);
          if (bConsistent) {
            bWalkIndex = false;
          } else {
            int nFound = (walkDbInfo.numIndexableFound + walkDbInfo.numNonIndexableFound);
            boolean bFoundAll = (nFound == walkDbInfo.numOriginallyIndexed);
            if (bFoundAll) {
              bWalkIndex = false;
            }
          }
        }
        
        // walk the index if required
        if (bWalkIndex) {
          WalkIndexInfo walkIndexInfo = new WalkIndexInfo(countInfo);
          this.walkIndex(walkIndexInfo);
          numDeleted += walkIndexInfo.numDocsDeleted;
        }
      }
      
      // close connections
      this.closeAll();
      
      // log the summary message
      double dSec = (System.currentTimeMillis() - this.startMillis) / 1000.0;
      StringBuffer msg = new StringBuffer();
      msg.append("Synchronization run completed."); 
      msg.append("\n processed=").append(countInfo.numDatabaseDocs);
      msg.append(" docsUpdated=").append(numUpdated);
      msg.append(" docsDeleted=").append(numDeleted);
      msg.append(", runtime: ");
      msg.append(Math.round(dSec / 60.0 * 100.0) / 100.0).append(" minutes");
      if (dSec <= 600) {
        msg.append(", ").append(Math.round(dSec * 100.0) / 100.0).append(" seconds");
      }
      LOGGER.info(msg.toString());

    } catch (LockObtainFailedException e) {
      LOGGER.log(Level.INFO,"Synchronization run aborted, reason: "+e.getMessage());
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE,"Error synchronizing index.",t);
    } finally {
      this.closeAll();
      if (backgroundLock != null) {
        try {
          backgroundLock.release();
        } catch (Throwable t) {
          LOGGER.log(Level.WARNING,"Error releasing lock.",t);
        }
      }
      if (this.wasInterrupted) {
        LOGGER.info("Synchronization run was interrupted."); 
      }
      RUNNING = false;
    }
  }
    
  /**
   * Walks the documents within the database modifying the index as required.
   * @param info synchronization step information
   * @throws IOException if an exception occurs while communicating with the index
   * @throws SQLException if an exception occurs while communicating with the database
   * @throws CatalogIndexException if an exception occurs while modifying the index
   */
  private void walkDatabase(WalkDatabaseInfo info) 
    throws IOException, SQLException, CatalogIndexException {
    LOGGER.fine("Checking database records...");
    PreparedStatement st = null;
    PreparedStatement stCol = null;
    TermDocs termDocs = null;
    try {
     
      // determine the metadata acl policy
      MetadataAcl acl = new MetadataAcl(this.context);
      boolean bCheckAcl = !acl.isPolicyUnrestricted();
      
      // determine if collections are being used
      List<String[]> collections = null;
      CollectionDao colDao = new CollectionDao(this.context);
      boolean hasCollections = false;
      boolean useCollections = colDao.getUseCollections();
      String sColMemberTable = colDao.getCollectionMemberTableName();
      String sqlCol = "SELECT COLUUID FROM "+sColMemberTable+" WHERE DOCUUID=?";
      if (useCollections) {
        collections = colDao.queryCollections();
        hasCollections = (collections.size() > 0);
      }
      
      // initialize index related variables
      boolean bCheckIndex = (info.numOriginallyIndexed > 0);
      String fldUuid = Storeables.FIELD_UUID;
      String fldModified = Storeables.FIELD_DATEMODIFIED;
      String fldAcl = Storeables.FIELD_ACL;
      
      ArrayList<String> alFields = new ArrayList<String>();
      alFields.add(fldModified);
      if (bCheckAcl) alFields.add(fldAcl);
      if (useCollections) alFields.add("isPartOf");
      FieldSelector selector = new MapFieldSelector(alFields.toArray(new String[0]));
      
      Term termUuid = new Term(fldUuid);
      if (bCheckIndex) {
        termDocs = this.reader.termDocs();
      }
      StringSet delUuids = new StringSet();
      
      // build the database query
      StringBuffer sb = new StringBuffer("SELECT");
      sb.append(" ").append(this.resourceTable).append(".DOCUUID");
      sb.append(",").append(this.resourceTable).append(".APPROVALSTATUS");
      sb.append(",").append(this.resourceTable).append(".PROTOCOL_TYPE");
      sb.append(",").append(this.resourceTable).append(".FINDABLE");
      sb.append(",").append(this.resourceTable).append(".UPDATEDATE");
      sb.append(",").append(this.resourceTable).append(".ACL");
      sb.append(" FROM ").append(this.resourceTable);
      String sql = sb.toString();
      LOGGER.finest(sql);
      
      // execute the query, walk through the database records
      Connection con = this.context.getConnectionBroker().returnConnection("").getJdbcConnection();
      st = con.prepareStatement(sql);
      ResultSet rs = st.executeQuery();
      if (this.checkInterrupted()) return;
      if (useCollections && hasCollections) {
        stCol = con.prepareStatement(sqlCol);
      }
      
      while (rs.next()) {
        
        info.numProcessed++;
        info.loopCount++;
        long nDbTimeModified = 0;
        Timestamp tsDbModified = null;
        String sDbAcl = null;
        boolean bIndexable = false;
        
        // read the database uuid and approval status
        String uuid = rs.getString(1);
        String status = rs.getString(2);
        String protocolType = Val.chkStr(rs.getString(3));
        boolean findable = Val.chkBool(rs.getString(4),false);

        bIndexable = (status != null) && 
                     (status.equalsIgnoreCase("approved") || status.equalsIgnoreCase("reviewed"));
        if (bIndexable && protocolType.length()>0 && !findable) {
          bIndexable = false;
        }
        
        // read the database modification date
        if (bIndexable) { 
          tsDbModified = rs.getTimestamp(5);
          if (tsDbModified != null) {
            nDbTimeModified = tsDbModified.getTime();
          }
          bIndexable = (nDbTimeModified > 0);
        }
        
        // for non-indexable documents, delete
        if (!bIndexable) {
          info.numNonIndexable++;
          if (bCheckIndex) {
            termDocs.seek(termUuid.createTerm(uuid));
            if (termDocs.next()) {
              info.numNonIndexableFound++;
              info.numRequiringDelete++;
              delUuids.add(uuid);
              if (delUuids.size() >= this.maxDeleteTokens) {
                if (this.checkInterrupted()) return;
                this.deleteDocuments(delUuids);
                info.numDocsDeleted += delUuids.size();
                delUuids.clear();
                if (this.checkInterrupted()) return;
              }
            }   
          }
        }
        
        // for indexable documents, check to ensure that they are in sync
        if (bIndexable) {
          info.numIndexable++;
          boolean bRequiresUpdate = true;
          
          // find the document within the index
          if (bCheckIndex) {
            termDocs.seek(termUuid.createTerm(uuid));
            if (termDocs.next()) {
              info.numIndexableFound++;
              Document doc = this.reader.document(termDocs.doc(),selector);
              if (doc != null) {
                bRequiresUpdate = false;
                
                // check the modification date
                long nIdxTimeModified = 0;
                String sModified = doc.get(fldModified);
                if (sModified != null) {
                  try {
                    nIdxTimeModified = Long.valueOf(sModified);
                  } catch (NumberFormatException e) {
                    nIdxTimeModified = 0;
                  }
                }
                bRequiresUpdate = (nIdxTimeModified == 0) || (nDbTimeModified > nIdxTimeModified);
                if (bRequiresUpdate) info.numWithInconsistentDates++;
                  
                // check the acl
                if (!bRequiresUpdate && bCheckAcl) {
                  long tAclStartMillis = System.currentTimeMillis();
                  bRequiresUpdate = true;
                  String[] aclsDb = null;
                  sDbAcl = rs.getString(6);
                  try {
                    // use an internal method for quick parsing
                    //aclsDb = acl.makeDocumentAcl(sDbAcl);
                    aclsDb = this.parseAcl(sDbAcl);
                  } catch (Exception eacl) {
                    String sMsg = "Error parsing acl";
                    sMsg += ", uuid="+uuid+"\n"+Val.chkStr(eacl.getMessage());
                    LOGGER.log(Level.WARNING,sMsg,eacl);
                  }                
                                    
                  if (aclsDb == null) aclsDb = new String[0];
                  ArrayList<String> aclsIdx = new ArrayList<String>();
                  Field[] aclFields = doc.getFields(fldAcl);
                  if ((aclFields != null) && (aclFields.length > 0)) {
                    for (Field aclField: aclFields) {
                      aclsIdx.add(aclField.stringValue());
                    }
                  }
                  if (aclsDb.length == aclsIdx.size()) {
                    int nMatched = 0;
                    if (aclsDb.length > 0) {
                      for (String s1: aclsDb) {
                        for (String s2: aclsIdx) {
                          if (s1.equalsIgnoreCase(s2)) {
                            nMatched++;
                            break;
                          }
                        }
                      }
                    }
                    bRequiresUpdate = (nMatched != aclsDb.length);
                  }
                  if (bRequiresUpdate) info.numWithInconsistentAcls++;
                  info.aclMillis += (System.currentTimeMillis() - tAclStartMillis);
                }
                
                // check collection membership
                if (!bRequiresUpdate && useCollections) {
                  long tColStartMillis = System.currentTimeMillis();
                  bRequiresUpdate = true;
                  
                  ArrayList<String> colDb = new ArrayList<String>();
                  if (useCollections && hasCollections) {
                    stCol.clearParameters();
                    stCol.setString(1,uuid);
                    ResultSet rsCol = stCol.executeQuery();
                    while (rsCol.next()) {
                      String sCUuid = rsCol.getString(1);
                      for (String[] col: collections) {
                        if (sCUuid.equals(col[0])) {  
                          colDb.add(col[1]);
                          break;
                        }
                      }
                    }
                    rsCol.close();
                  }
                  
                  ArrayList<String> colIdx = new ArrayList<String>();
                  Field[] colFields = doc.getFields("isPartOf");
                  if ((colFields != null) && (colFields.length > 0)) {
                    for (Field colField: colFields) {
                      colIdx.add(colField.stringValue());
                    }
                  }
                  if (colDb.size() == colIdx.size()) {
                    int nMatched = 0;
                    if (colDb.size() > 0) {
                      for (String s1: colDb) {
                        for (String s2: colIdx) {
                          if (s1.equalsIgnoreCase(s2)) {
                            nMatched++;
                            break;
                          }
                        }
                      }
                    }
                    bRequiresUpdate = (nMatched != colDb.size());
                  }
                  if (bRequiresUpdate) info.numWithInconsistentColMembership++;
                  info.colMillis += (System.currentTimeMillis() - tColStartMillis);
                 }
                
              }
            }
          }
          
          // execute the update if required
          if (bRequiresUpdate) {
            if (this.checkInterrupted()) return;        
            try {
              if (bCheckAcl) {
                if (sDbAcl == null) sDbAcl = rs.getString(6);
              }
              String sXml = Val.chkStr(this.readXml(uuid));
              if (sXml.length() > 0) {
                info.numRequiringUpdate++;
                MetadataDocument mdDoc = new MetadataDocument();
                Schema schema = mdDoc.prepareForView(this.context,sXml);
                this.adapter.publishDocument(uuid,tsDbModified,schema,sDbAcl);
                info.numDocsUpdated++;
              }
            } catch (SchemaException se) {
              
              // dont' allow the entire process to fail over one bad xml
              String sMsg = "Error indexing document during synchronization";
              sMsg += ", uuid="+uuid+"\n"+Val.chkStr(se.getMessage());
              LOGGER.log(Level.WARNING,sMsg,se);
            }
            if (this.checkInterrupted()) return;
          }
          
        }
 
        // cache the synchronized uuids
        if (this.synchedUuidCache != null) {
          this.synchedUuidCache.put(uuid,"");
          if (this.synchedUuidCache.size() > this.maxUuidCache) {
            this.synchedUuidCache = null;
          }
        }
        
        // log a status message if the feedback threshold was reached
        if (this.checkInterrupted()) return;
        if ((System.currentTimeMillis() - info.loopStartMillis) >= this.feedbackMillis) {
          LOGGER.info(info.getLoopMessage());
        }
       
      }
      
      // delete any documents left over in the buffer
      if (delUuids.size() >= 0) {
        if (this.checkInterrupted()) return;
        this.deleteDocuments(delUuids);
        info.numDocsDeleted += delUuids.size();
      }
      
      LOGGER.info(info.getStepMessage());
    } finally {
      try {if (st != null) st.close();} catch (Exception ef) {}
      try {if (stCol != null) stCol.close();} catch (Exception ef) {}
      try {if (termDocs != null) termDocs.close();} catch (Exception ef) {}
    }
  }
  
  /**
   * Walks the documents within the index removing documents that are no longer indexable.
   * @param info synchronization step information
   * @throws IOException if an exception occurs while communicating with the index
   * @throws SQLException if an exception occurs while communicating with the database
   * @throws CatalogIndexException if an exception occurs while modifying the index
   */
  private void walkIndex(WalkIndexInfo info) 
    throws IOException, SQLException, CatalogIndexException {
    LOGGER.fine("Checking indexed documents...");
    TermEnum termEnum = null;
    try {
      StringSet chkUuids = new StringSet();
      StringSet delUuids = new StringSet();
      String fldUuid = Storeables.FIELD_UUID;
      termEnum = this.reader.terms(new Term(fldUuid));
      do {
        Term term = termEnum.term();
        if ((term == null) || !term.field().equals(fldUuid)) {
          break;
        } 
        info.numProcessed++;
        info.loopCount++;
         
        // check the cache to see if the uuid was already synchronized,
        // otherwise add it to the set of uuids to check
        String uuid = term.text();
        if (this.synchedUuidCache != null) {
          if (this.synchedUuidCache.containsKey(uuid)) {
            info.numFoundInCache++;
          } else {
            chkUuids.add(uuid);
          }
        } else {
          chkUuids.add(uuid);
        }
        
        // check to ensure that these documents are indexable
        if (chkUuids.size() >= this.maxSqlTokens) {
          if (this.checkInterrupted()) return;
          this.ensureIndexable(info,chkUuids,delUuids);
          chkUuids.clear();
          if (this.checkInterrupted()) return;
          if ((System.currentTimeMillis() - info.loopStartMillis) >= this.feedbackMillis) {
            LOGGER.info(info.getLoopMessage());
          }
        }
        
        // log a status message if the loop threshold was reached
        if (info.loopCount >= info.loopThreshold) {
          if (this.checkInterrupted()) return;
          if ((System.currentTimeMillis() - info.loopStartMillis) >= this.feedbackMillis) {
            LOGGER.info(info.getLoopMessage());
          }
        }
        
     } while (termEnum.next());
      
      // check any documents left over in the buffers
      if (chkUuids.size() > 0) {
        if (this.checkInterrupted()) return;
        this.ensureIndexable(info,chkUuids,delUuids);
        if (this.checkInterrupted()) return;
      }
      if (delUuids.size() >= 0) {
        if (this.checkInterrupted()) return;
        this.deleteDocuments(delUuids);
        info.numDocsDeleted += delUuids.size();
        if (this.checkInterrupted()) return;
      }
      
      LOGGER.info(info.getStepMessage());
    } finally {
      try {if (termEnum != null) termEnum.close();} catch (Exception ef) {}
    }
  }
    
  /** inner classes =========================================================== */
  
  /** Stores information original counts within the dtatabase and index. */
  class CountInfo {
    protected int numDatabaseDocs = -1;
    protected int numIndexableDocs = -1;
    protected int numIndexedDocs = -1;
    
    /** Constructor. */
    protected CountInfo() {}
    
    /** Makes a log message for a step. */
    protected String getStepMessage(String step, int count, long startMillis) {
      double dSec = (System.currentTimeMillis() - startMillis) / 1000.0;
      StringBuffer msg = new StringBuffer();
      msg.append("Step ").append(step).append(": ").append(count); 
      msg.append(", stepRuntime: ");
      msg.append(Math.round(dSec / 60.0 * 100.0) / 100.0).append(" minutes");
      if (dSec <= 600) {
        msg.append(", ").append(Math.round(dSec * 100.0) / 100.0).append(" seconds");
      }
      return msg.toString();
    }
  }
  
  /** Stores information collected while walking the database. */
  class WalkDatabaseInfo {
    protected long aclMillis = 0;
    protected long colMillis = 0;
    protected int  loopThreshold = 1000;
    protected int  loopCount = 0;
    protected long loopStartMillis = 0;
    protected int  numDatabaseDocs = 0;
    protected int  numDocsDeleted = 0;
    protected int  numDocsUpdated = 0;    
    protected int  numIndexable = 0;
    protected int  numIndexableFound = 0;
    protected int  numNonIndexable = 0;
    protected int  numNonIndexableFound = 0;
    protected int  numOriginallyIndexed = 0;
    protected int  numProcessed = 0;
    protected int  numRequiringDelete = 0;
    protected int  numRequiringUpdate = 0;
    protected int  numWithInconsistentAcls = 0;
    protected int  numWithInconsistentColMembership = 0;
    protected int  numWithInconsistentDates = 0;
    protected long startMillis = 0;
        
    /** Constructor. */
    protected WalkDatabaseInfo(CountInfo countInfo) {
      this.startMillis = System.currentTimeMillis();
      this.loopStartMillis = this.startMillis;
      this.numDatabaseDocs = countInfo.numDatabaseDocs;
      this.numOriginallyIndexed = countInfo.numIndexedDocs;
    }
    
    /** Makes a log message for a loop. */
    protected String getLoopMessage() {      
      long tNow = System.currentTimeMillis();
      double dSec = (tNow - this.loopStartMillis) / 1000.0;
      double dElSec = (tNow - this.startMillis) / 1000.0;
      StringBuffer msg = new StringBuffer();
      msg.append("Processing database documents: ");            
      msg.append(this.numProcessed+" of ").append(this.numDatabaseDocs);
      msg.append("\n last loop: ").append(this.loopCount).append(" docs");
      msg.append(", ").append(Math.round(dSec / 60.0 * 100.0) / 100.0).append(" minutes");
      if (dSec <= 600) {
        msg.append(", ").append(Math.round(dSec * 100.0) / 100.0).append(" seconds");
      }
      msg.append(", elapsed step time: ");
      msg.append(Math.round(dElSec / 60.0 * 100.0) / 100.0).append(" minutes");
      if (dElSec <= 600) {
        msg.append(", ").append(Math.round(dElSec * 100.0) / 100.0).append(" seconds");
      }
      this.loopCount = 0;
      this.loopStartMillis = System.currentTimeMillis();
      return msg.toString();
    }
    
    /** Makes a log message for the step. */
    protected String getStepMessage() {
      double dSec = (System.currentTimeMillis() - this.startMillis) / 1000.0;
      double dAclSec = this.aclMillis / 1000.0;
      double dColSec = this.colMillis / 1000.0;
      StringBuffer msg = new StringBuffer();
      msg.append("Step walkDatabase: "); 
      msg.append("\n databaseCount=").append(this.numDatabaseDocs);
      msg.append(" processed=").append(this.numProcessed);
      msg.append(" originallyIndexed=").append(this.numOriginallyIndexed);
      msg.append("\n indexable=").append(this.numIndexable);
      msg.append(" indexableFound=").append(this.numIndexableFound);
      msg.append(" nonIndexable=").append(this.numNonIndexable);
      msg.append(" nonIndexableFound=").append(this.numNonIndexableFound); 
      msg.append("\n inconsistentDates=").append(this.numWithInconsistentDates);
      msg.append(" inconsistentAcls=").append(this.numWithInconsistentAcls);
      msg.append(" inconsistentColMembership=").append(this.numWithInconsistentColMembership);
      msg.append("\n requiringUpdate=").append(this.numRequiringUpdate);
      msg.append(" requiringDelete=").append(this.numRequiringDelete);
      msg.append(" docsUpdated=").append(this.numDocsUpdated);
      msg.append(" docsDeleted=").append(this.numDocsDeleted);
      
      msg.append("\n stepRuntime: ");
      msg.append(Math.round(dSec / 60.0 * 100.0) / 100.0).append(" minutes");
      if (dSec <= 600) {
        msg.append(", ").append(Math.round(dSec * 100.0) / 100.0).append(" seconds");
      }
      
      msg.append(" (aclSubTime: ");
      msg.append(Math.round(dAclSec / 60.0 * 100.0) / 100.0).append(" minutes");
      if (dAclSec <= 600) {
        msg.append(", ").append(Math.round(dAclSec * 100.0) / 100.0).append(" seconds");
      }
      msg.append(")");
      
      msg.append(" (colMembershipSubTime: ");
      msg.append(Math.round(dColSec / 60.0 * 100.0) / 100.0).append(" minutes");
      if (dColSec <= 600) {
        msg.append(", ").append(Math.round(dColSec * 100.0) / 100.0).append(" seconds");
      }
      msg.append(")");
      
      return msg.toString();
    }
  }
  
  /** Stores information collected while walking the index. */
  static class WalkIndexInfo {
    protected long dbMillis = 0;
    protected int  loopThreshold = 10000;
    protected int  loopCount = 0;
    protected long loopStartMillis = 0;
    protected int  numDocsDeleted = 0;
    protected int  numFoundInCache = 0;
    protected int  numFoundInDb = 0;
    protected int  numOriginallyIndexed = 0;
    protected int  numProcessed = 0;
    protected int  numRequiringDelete = 0;
    protected long startMillis = 0;
    
    /** Constructor. */
    protected WalkIndexInfo(CountInfo countInfo) {
      this.startMillis = System.currentTimeMillis();
      this.loopStartMillis = this.startMillis;
      this.numOriginallyIndexed = countInfo.numIndexedDocs;
    }
    
    /** Makes a log message for a loop. */
    protected String getLoopMessage() {      
      long tNow = System.currentTimeMillis();
      double dSec = (tNow - this.loopStartMillis) / 1000.0;
      double dElSec = (tNow - this.startMillis) / 1000.0;
      StringBuffer msg = new StringBuffer();
      msg.append("Processing indexed documents: ");            
      msg.append(this.numProcessed+" of ").append(this.numOriginallyIndexed);
      msg.append("\n last loop: ").append(this.loopCount).append(" docs");
      msg.append(", ").append(Math.round(dSec / 60.0 * 100.0) / 100.0).append(" minutes");
      if (dSec <= 600) {
        msg.append(", ").append(Math.round(dSec * 100.0) / 100.0).append(" seconds");
      }
      msg.append(", elapsed step time: ");
      msg.append(Math.round(dElSec / 60.0 * 100.0) / 100.0).append(" minutes");
      if (dElSec <= 600) {
        msg.append(", ").append(Math.round(dElSec * 100.0) / 100.0).append(" seconds");
      }
      this.loopCount = 0;
      this.loopStartMillis = System.currentTimeMillis();
      return msg.toString();
    }
    
    /** Makes a log message for the step. */
    protected String getStepMessage() {
      double dSec = (System.currentTimeMillis() - this.startMillis) / 1000.0;
      double dbSec = this.dbMillis / 1000.0;
      StringBuffer msg = new StringBuffer();
      msg.append("Step walkIndex: "); 
      msg.append("\n processed=").append(this.numProcessed);
      msg.append(" numFoundInCache=").append(this.numFoundInCache);
      msg.append(" numFoundInDb=").append(this.numFoundInDb);
      msg.append("\n requiringDelete=").append(this.numRequiringDelete);
      msg.append(" docsDeleted=").append(this.numDocsDeleted);
      msg.append("\n stepRuntime: ");
      msg.append(Math.round(dSec / 60.0 * 100.0) / 100.0).append(" minutes");
      if (dSec <= 600) {
        msg.append(", ").append(Math.round(dSec * 100.0) / 100.0).append(" seconds");
      }
      msg.append(" (spentInDb: ");
      msg.append(Math.round(dbSec / 60.0 * 100.0) / 100.0).append(" minutes");
      if (dbSec <= 600) {
        msg.append(", ").append(Math.round(dbSec * 100.0) / 100.0).append(" seconds");
      }
      msg.append(")");
      return msg.toString();
    }
  }

}
