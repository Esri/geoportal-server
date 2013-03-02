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
package com.esri.gpt.catalog.management;
import com.esri.gpt.catalog.context.CatalogIndexAdapter;
import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.catalog.lucene.RemoteIndexer;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.sql.BaseDao;
import com.esri.gpt.framework.util.Val;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Database access object associated with item collections.
 */
public class CollectionDao extends BaseDao {
  
  /** class variables ========================================================= */
  
  /** Logger */
  private static Logger LOGGER = Logger.getLogger(CollectionDao.class.getName());
  
  /** constructors  =========================================================== */
  
  /**
   * Constructs with an associated request context.
   * @param requestContext the request context
   */
  public CollectionDao(RequestContext requestContext) {
    super(requestContext);
  }
  
  /** properties  ============================================================= */
  
  /**
   * Gets the catalog index adapter.
   * @return the the catalog index adapter (null if none)
   */
  private CatalogIndexAdapter getCatalogIndexAdapter() {
    return getRequestContext().getCatalogConfiguration().makeCatalogIndexAdapter(getRequestContext());
  }
  
  /**
   * Gets the collection table name.
   * @return the collection table name
   */
  public String getCollectionTableName() {
    return this.getTablePrefix()+"COLLECTION";
  }
  
  /**
   * Gets the collection member table name.
   * @return the collection member table name
   */
  public String getCollectionMemberTableName() {
    return this.getTablePrefix()+"COLLECTION_MEMBER";
    //return "TMP_COLLECTION_MEMBER";
  }
  
  /**
   * Gets resource table name.
   * @return resource table name
   */
  private String getResourceTableName() {
    return this.getRequestContext().getCatalogConfiguration().getResourceTableName();
  }
  
  /**
   * Gets the table prefix.
   * @return the table prefix
   */
  public String getTablePrefix() {
    return this.getRequestContext().getCatalogConfiguration().getTablePrefix();
  }
  
  /**
   * Determine if collections are in use.
   * @return <code>true</code> if collections are in use
   */
  public boolean getUseCollections() {
    RequestContext context = this.getRequestContext();
    StringAttributeMap params = context.getCatalogConfiguration().getParameters();
    String s = Val.chkStr(params.getValue("catalog.useCollections"));
    return s.equalsIgnoreCase("true");
  }
  
  /** methods ================================================================= */

  /**
   * Adds a set of document UUIDS to a collection.
   * @param docUuids the set of document UUIDS
   * @param colUuid the collection UUID
   * @return the number of records inserted
   * @throws SQLException if an exception occurs while communicating with the database
   * @throws CatalogIndexException is an exception occurs while writing to the index
   */
  public int addMembers(Publisher publisher, StringSet docUuids, String colUuid) 
    throws SQLException, CatalogIndexException {
    PreparedStatement st1 = null;
    PreparedStatement st2 = null;
    PreparedStatement stR = null;
    int nBatch = 0;
    StringSet indexableUuids = new StringSet();
    try {
      String table = this.getCollectionMemberTableName();
      String tableR = this.getResourceTableName();
      String sql1 = "SELECT DOCUUID FROM "+table+" WHERE DOCUUID=? AND COLUUID=?";
      String sql2 = "INSERT INTO "+table+" (DOCUUID,COLUUID) VALUES (?,?)";
      String sqlR = "SELECT APPROVALSTATUS FROM "+tableR+" WHERE DOCUUID=?";
      
      Connection con = this.returnConnection().getJdbcConnection();
      st1 = con.prepareStatement(sql1);
      st2 = con.prepareStatement(sql2);
      stR = con.prepareStatement(sqlR);
      for (String docUuid: docUuids) {
        boolean bInsert = true;
        st1.clearParameters();
        st1.setString(1,docUuid);
        st1.setString(2,colUuid);
        ResultSet rs1 = st1.executeQuery();
        if (rs1.next()) bInsert = false;
        rs1.close();
        
        if (bInsert) {
          stR.clearParameters();
          stR.setString(1,docUuid);
          ResultSet rsR = stR.executeQuery();
          if (rsR.next()) {
            String status = Val.chkStr(rsR.getString(1));
            if (MmdEnums.ApprovalStatus.isPubliclyVisible(status)) {
              indexableUuids.add(docUuid);
            }
          }
          rsR.close();
          
          st2.setString(1,docUuid);
          st2.setString(2,colUuid);
          st2.addBatch();
          nBatch++;
        }
      }
      st1.close();
      st1 = null;
      
      if (nBatch > 0) {
        st2.executeBatch();
      }
    } finally {
      CollectionDao.closeStatement(st1);
      CollectionDao.closeStatement(st2);
      CollectionDao.closeStatement(stR);
    }
    
    if (indexableUuids.size() > 0) {
      this.reindex(publisher,indexableUuids);
    }
    
    return nBatch;
  }
  
  /**
   * Queries named collections.
   * @return the list {colUuid,shortName}
   * @throws SQLException if an exception occurs while communicating with the database
   */
  public List<String[]> queryCollections() throws SQLException {
    PreparedStatement st = null;
    ArrayList<String[]> al = new ArrayList<String[]>();
    try {
      String table = this.getCollectionTableName();
      String sql = null;
      if (getIsDbCaseSensitive(this.getRequestContext())) {
        sql = "SELECT COLUUID,SHORTNAME FROM "+table+
              " ORDER BY UPPER(SHORTNAME) ASC";
      } else {
        sql = "SELECT COLUUID,SHORTNAME FROM "+table+
              " ORDER BY UPPER(SHORTNAME) ASC";
      }
      this.logExpression(sql);
      Connection con = this.returnConnection().getJdbcConnection();
      st = con.prepareStatement(sql);
      ResultSet rs = st.executeQuery();
      while (rs.next()) {
        if (Thread.currentThread().isInterrupted()) return null;
        String uuid = Val.chkStr(rs.getString(1));
        String name = Val.chkStr(rs.getString(2));
        al.add(new String[]{uuid,name});
      }
    } finally {
      CollectionDao.closeStatement(st);
    }
    return al;
  }
  
  /**
   * Re-indexes a set of documents.
   * @param publisher the publisher
   * @param uuids the set of document UUIDS
   * @throws SQLException if an exception occurs while communicating with the database
   * @throws CatalogIndexException is an exception occurs while writing to the index
   */
  private void reindex (Publisher publisher, StringSet uuids) 
    throws SQLException, CatalogIndexException {
    if (uuids.size() == 0) return;
    StringAttributeMap params = this.getRequestContext().getCatalogConfiguration().getParameters();
    String param = Val.chkStr(params.getValue("lucene.useRemoteWriter"));
    boolean bUseRemoteWriter = param.equalsIgnoreCase("true");
    param = Val.chkStr(params.getValue("lucene.useLocalWriter"));
    boolean bUseLocalWriter = !param.equalsIgnoreCase("false");
    if (bUseRemoteWriter) {
      RemoteIndexer remoteIndexer = new RemoteIndexer();
      remoteIndexer.send(this.getRequestContext(),"publish",
          uuids.toArray(new String[0]));
    } 
    if (bUseLocalWriter) {
      CatalogIndexAdapter indexAdapter = getCatalogIndexAdapter();
      for (String uuid: uuids) {
        indexAdapter.publishDocument(uuid,publisher);
      }
    }
    
  }
  
  /**
   * Removes a set of document UUIDS from a collection.
   * @param docUuids the set of document UUIDS
   * @param colUuid the collection UUID
   * @return the number of records removed
   * @throws SQLException if an exception occurs while communicating with the database
   * @throws CatalogIndexException is an exception occurs while writing to the index
   */
  public int removeMembers(Publisher publisher, StringSet docUuids, String colUuid) 
    throws SQLException, CatalogIndexException {
    PreparedStatement st1 = null;
    PreparedStatement st2 = null;
    PreparedStatement stR = null;
    int nBatch = 0;
    StringSet indexableUuids = new StringSet();
    try {
      String table = this.getCollectionMemberTableName();
      String tableR = this.getResourceTableName();
      String sql1 = "SELECT DOCUUID FROM "+table+" WHERE DOCUUID=? AND COLUUID=?";
      String sql2 = "DELETE FROM "+table+" WHERE DOCUUID=? AND COLUUID=?";
      String sqlR = "SELECT APPROVALSTATUS FROM "+tableR+" WHERE DOCUUID=?";
      
      Connection con = this.returnConnection().getJdbcConnection();
      st1 = con.prepareStatement(sql1);
      st2 = con.prepareStatement(sql2);
      stR = con.prepareStatement(sqlR);
      for (String docUuid: docUuids) {
        boolean bRemove = false;
        st1.clearParameters();
        st1.setString(1,docUuid);
        st1.setString(2,colUuid);
        ResultSet rs1 = st1.executeQuery();
        if (rs1.next()) bRemove = true;
        rs1.close();
        
        if (bRemove) {
          stR.clearParameters();
          stR.setString(1,docUuid);
          ResultSet rsR = stR.executeQuery();
          if (rsR.next()) {
            String status = Val.chkStr(rsR.getString(1));
            if (MmdEnums.ApprovalStatus.isPubliclyVisible(status)) {
              indexableUuids.add(docUuid);
            }
          }
          rsR.close();
          
          st2.setString(1,docUuid);
          st2.setString(2,colUuid);
          st2.addBatch();
          nBatch++;
        }
      }
      st1.close();
      st1 = null;
      
      if (nBatch > 0) {
        st2.executeBatch();
      }
    } finally {
      CollectionDao.closeStatement(st1);
      CollectionDao.closeStatement(st2);
      CollectionDao.closeStatement(stR);
    }
    
    if (indexableUuids.size() > 0) {
      this.reindex(publisher,indexableUuids);
    }
    return nBatch;
  }

}
