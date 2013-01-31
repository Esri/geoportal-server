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
package com.esri.gpt.catalog.publication;
import com.esri.gpt.catalog.arcims.DeleteMetadataRequest;
import com.esri.gpt.catalog.arcims.ImsMetadataAdminDao;
import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.sql.BaseDao;
import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Database access object associated with the metadata catalog.
 */
class CatalogDao extends BaseDao {
  
  /** class variables ========================================================= */
  
  /** Logger */
  private static Logger LOGGER = Logger.getLogger(CatalogDao.class.getName());
  
  /** constructors  =========================================================== */
  
  /**
   * Constructs with an associated request context.
   * @param requestContext the request context
   */
  public CatalogDao(RequestContext requestContext) {
    super(requestContext);
  }
  
  /** properties  ============================================================= */
  
  /**
   * Gets the primary catalog table name.
   * @return the primary catalog table name
   */
  private String getResourceTableName() {
    return getRequestContext().getCatalogConfiguration().getResourceTableName();
  }
  
  /** methods ================================================================= */
  
  /**
   * Deletes catalog documents that are no longer referenced by a parent resource.
   * @param context the processing context
   * @param sourceURIs the collection of source URIs to delete (key is SOURCEURI value is DOCUUID)
   * @throws SQLException if an exception occurs while communicating with the database
   * @throws ImsServiceException if an exception occurs during delete
   * @throws CatalogIndexException if an exception occurs during delete
   * @throws IOException if accessing index fails
   */
  protected void deleteSourceURIs(final ProcessingContext context, Map<String,String> sourceURIs)
    throws SQLException, ImsServiceException, CatalogIndexException, IOException {
    this.deleteSourceURIs(context.getPublisher(), sourceURIs.entrySet(), new CatalogRecordListener() {
      @Override
      public void onRecord(String sourceUri, String uuid) {
        context.incrementNumberDeleted();
        ProcessedRecord record = new ProcessedRecord();
        record.setSourceUri(sourceUri);
        record.setStatusType(ProcessedRecord.StatusType.DELETED);
        context.getProcessedRecords().add(record);
      }
    });
  }

  /**
   * Deletes catalog documents that are no longer referenced by a parent resource.
   * @param publisher publisher
   * @param sourceURIs the collection of source URIs to delete (key is SOURCEURI value is DOCUUID)
   * @param listener listener called upon deleting a single document
   * @throws SQLException if an exception occurs while communicating with the database
   * @throws ImsServiceException if an exception occurs during delete
   * @throws CatalogIndexException if an exception occurs during delete
   * @throws IOException if accessing index fails
   */
  protected void deleteSourceURIs(Publisher publisher, Iterable<Map.Entry<String,String>> sourceURIs, CatalogRecordListener listener)
      throws ImsServiceException, SQLException, CatalogIndexException, IOException {
    ImsMetadataAdminDao adminDao = new ImsMetadataAdminDao(getRequestContext());
    DeleteMetadataRequest delRequest = new DeleteMetadataRequest(
        this.getRequestContext(),publisher);
    for (Map.Entry<String,String> entry: sourceURIs) {
      if (Thread.currentThread().isInterrupted()) break;
      String uri = entry.getKey();
      String uuid = entry.getValue();
      LOGGER.finest("Deleting uuid="+uuid+", sourceuri="+uri);
      boolean bOk = delRequest.executeDelete(uuid);
      if (bOk) {
        listener.onRecord(uri, uuid);
      }
    }
  }

  /**
   * Queries document source URIs associated with a parent resource (SQL LIKE).
   * @param pattern the source URI pattern of the parent resource
   * @param pattern2 optional secondary source URI pattern of the parent resource
   * @return the collection of associated source URIs (key is SOURCEURI value is DOCUUID)
   * @throws SQLException if an exception occurs while communicating with the database
   */
  protected Map<String,String> querySourceURIs(String pattern, String pattern2) 
    throws SQLException {
    PreparedStatement st = null;
    Map<String,String> uris = new HashMap<String,String>();
    try {
      String table = this.getResourceTableName();
      String sql = "SELECT SOURCEURI,DOCUUID FROM "+table+" WHERE SOURCEURI LIKE ?";
      if ((pattern2 != null) && (pattern2.length() > 0)) {
        sql += " OR SOURCEURI LIKE ?";
      }
      this.logExpression(sql);
      Connection con = this.returnConnection().getJdbcConnection();
      st = con.prepareStatement(sql);
      st.setString(1,pattern+"%");
      if ((pattern2 != null) && (pattern2.length() > 0)) {
        st.setString(2,pattern2+"%");
      }
      ResultSet rs = st.executeQuery();
      int numFound = 0;
      while (rs.next()) {
        if (Thread.currentThread().isInterrupted()) return null;
        numFound++;
        String uri = rs.getString(1);
        String uuid = rs.getString(2);
        uris.put(uri,uuid);
      }
    } finally {
      CatalogDao.closeStatement(st);
    }
    return uris;
  }


  /**
   * Queries document source URIs associated with the harvesting site.
   * @param siteUuid site UUID
   * @param listener source URI listener
   * @throws SQLException if an exception occurs while communicating with the database
   * @throws IOException if accessing index fails
   */
  protected void querySourceURIs(String siteUuid, CatalogRecordListener listener)
    throws SQLException, IOException {
    PreparedStatement st = null;
    try {
      String table = this.getResourceTableName();
      String sql = "SELECT SOURCEURI,DOCUUID FROM "+table+" WHERE SITEUUID = ?";
      this.logExpression(sql);
      Connection con = this.returnConnection().getJdbcConnection();
      st = con.prepareStatement(sql);
      st.setString(1,siteUuid);
      ResultSet rs = st.executeQuery();
      int numFound = 0;
      while (rs.next()) {
        if (Thread.currentThread().isInterrupted()) return;
        numFound++;
        String uri = rs.getString(1);
        String uuid = rs.getString(2);
        listener.onRecord(uri, uuid);
      }
    } finally {
      CatalogDao.closeStatement(st);
    }
  }

  protected static interface CatalogRecordListener {
    void onRecord(String sourceUri, String uuid) throws IOException;
  }
}
