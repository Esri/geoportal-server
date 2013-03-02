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
package com.esri.gpt.catalog.search;

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolNone;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;

import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.control.webharvest.protocol.Protocol;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.sql.BaseDao;
import com.esri.gpt.framework.sql.ConnectionBroker;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;

/**
 * The Class GptRepository. Object that communicates with the Gpt repository for
 * the search classes .
 */
public class GptRepository extends BaseDao implements ISearchSaveRepository {

// class variables =============================================================
/** Table where all the saves are inserted into **/
private static String SAVE_TABLE = "GPT_SEARCH";

/** Class logger **/
private static final Logger LOG = Logger.getLogger(GptRepository.class
    .getCanonicalName());

// methods =====================================================================
/**
 * Delete a search
 * 
 * @param id
 *          associated with the saved search
 * @param user
 *          associated with the saved search
 * @throws SearchException
 */
public void delete(Object id, User user) throws SearchException {
  PreparedStatement pStmt = null;
  Connection connection = null;
  try {
    String sql = " DELETE FROM " + SAVE_TABLE + " WHERE UUID=? AND USERID=? ";
    connection = this.getConnection();
    pStmt = connection.prepareStatement(sql);
    int n = 1;
    pStmt.setString(n++, id.toString());
    pStmt.setInt(n++, user.getLocalID());
    pStmt.executeUpdate();
  } catch (Exception e) {
    throw new SearchException(e);
  } finally {
    closeStatement(pStmt);
  }

}

/**
 * 
 * @param user
 *          associated with the saved search
 * @return List of criterias
 * @throws SearchException
 */
public SavedSearchCriterias getSavedList(User user) throws SearchException {

  Connection connection = null;
  SavedSearchCriterias criteria = new SavedSearchCriterias();
  PreparedStatement ps = null;
  ResultSet rs = null;

  try {
    String sql = "SELECT UUID, NAME, CRITERIA FROM " + SAVE_TABLE + " WHERE USERID = ?";
    connection = this.getConnection();
    ps = connection.prepareStatement(sql);
    ps.setInt(1, user.getLocalID());
    rs = ps.executeQuery();
    String name = null;
    String id;
    String sCriteria;
    while (rs.next()) {
      id = rs.getString(1);
      name = rs.getString(2);
      sCriteria = rs.getString(3);
      SavedSearchCriteria sCrit = new SavedSearchCriteria(id, name, this);
      sCrit.setCriteria(sCriteria);
      criteria.add(sCrit);
    }
  } catch (Exception e) {
    throw new SearchException(e);
  } finally {
    closeResultSet(rs);
    closeStatement(ps);
  }

  return criteria;
}

/**
 * Gets search criteria object
 * 
 * @param id
 *          of the search criteria
 * @param user
 *          associated with the saved search
 * @return SearchCriteria queried
 * @throws SearchException
 */
public SearchCriteria getSearchCriteria(Object id, User user)
    throws SearchException {
  Connection connection = null;
  PreparedStatement ps = null;
  ResultSet rs = null;
  SearchCriteria searchCriteria = null;
  try {
    String sql = "SELECT CRITERIA FROM " + SAVE_TABLE + " WHERE USERID = ? "
        + "AND UUID = ?";
    connection = this.getConnection();
    ps = connection.prepareStatement(sql);
    ps.setInt(1, user.getLocalID());
    ps.setString(2, id.toString());
    rs = ps.executeQuery();
    String criteriaXml = null;

    if (rs.next()) {
      criteriaXml = Val.chkStr(rs.getString(1));
      if ("".equals(criteriaXml)) {
        throw new SearchException(
            "Empty search criteria retrived from repository.");
      }
      searchCriteria = new SearchCriteria(DomUtil.makeDomFromString(
          criteriaXml, false));
    }
  } catch (Exception e) {
    throw new SearchException(e);
  } finally {
    closeResultSet(rs);
    closeStatement(ps);
  }

  return searchCriteria;
}

/**
 * Saves the criteria
 * 
 * @param savedCriteria
 *          Object with the search to be saved
 * @throws SearchException
 */
public void save(SavedSearchCriteria savedCriteria) throws SearchException {

  PreparedStatement pStmt = null;
  Connection connection = null;
  try {
    SearchCriteria criteria = savedCriteria.getSearchCriteria();
    String name = criteria.getSavedSearchName();

    int userId = savedCriteria.getUser().getLocalID();
    String uuid = UUID.randomUUID().toString();
    String sql = " INSERT INTO " + SAVE_TABLE
        + " (UUID, NAME, USERID, CRITERIA) " + " VALUES(?,?,?,?)";

    connection = this.getConnection();
    pStmt = connection.prepareStatement(sql);
    int n = 1;
    pStmt.setString(n++, uuid);
    pStmt.setString(n++, name);
    pStmt.setInt(n++, userId);
    pStmt.setString(n++, criteria.toDom2());
    pStmt.executeUpdate();
  } catch (Exception e) {
    throw new SearchException(e);
  } finally {
    closeStatement(pStmt);
  }

}

/**
 * Saves the criteria
 * 
 * @param name name
 * @param restCriteria
 *          Object with the search to be saved
 * @param user user
 * @throws SearchException
 */
public void save(String name, String restCriteria, User user) 
  throws SearchException {

  PreparedStatement pStmt = null;
  Connection connection = null;
  try {
       
    String uuid = UUID.randomUUID().toString();
    String sql = " INSERT INTO " + SAVE_TABLE
        + " (UUID, NAME, USERID, CRITERIA) " + " VALUES(?,?,?,?)";

    connection = this.getConnection();
    pStmt = connection.prepareStatement(sql);
    int n = 1;
    pStmt.setString(n++, uuid);
    pStmt.setString(n++, name);
    pStmt.setInt(n++, user.getLocalID());
    pStmt.setString(n++, restCriteria);
    pStmt.executeUpdate();
  } catch (Exception e) {
    throw new SearchException(e);
  } finally {
    closeStatement(pStmt);
  }

}

/**
 * Gets the connection.
 * 
 * @return the connection (never null)
 * 
 * @throws SearchException
 *           the search exception
 * @throws SQLException
 *           the SQL exception
 */
protected Connection getConnection() throws SearchException, SQLException {

  SearchConfig.getConfiguredInstance();
  RequestContext requestContext = this.getRequestContext();
  if (requestContext == null) {
    throw new SearchException("Could not get a request context so as "
        + " to make a" + " connection to the repository.");
  }

  ConnectionBroker connectionBroker = requestContext.getConnectionBroker();
  if (connectionBroker == null) {
    throw new SearchException("Could not get a Connection Broker so as"
        + " to make a" + " connection to the repository.");
  }

  
  ManagedConnection managedConnection = connectionBroker.returnConnection("");
  Connection connection = managedConnection.getJdbcConnection();
  if (connection == null) {
    throw new SearchException("Got null connection to repository "
        + "for save search");
  }

  return connection;

}

/**
 * Gets the request context
 * 
 * @return RequestContext for this Request
 */
@Override
public RequestContext getRequestContext() {
  if(super.getRequestContext() != null) {
	  return super.getRequestContext();
  }
  FacesContextBroker broker = new FacesContextBroker();
  RequestContext requestContext = broker.extractRequestContext();
  return requestContext;
}

/**
 * Read harvest record. Will update uuid, url, profile, protocol and name
 * 
 * @param rid
 *          repository id
 * @param context
 *          the request context
 * 
 * @return the hr record
 * 
 * @throws SearchException
 *           the search exception
 */
public HrRecord readHarvestRecord(String rid, RequestContext context)
    throws SearchException {
  
  StringSet ridSet = new StringSet();
  ridSet.add(rid);
  Map<String, HrRecord> mapHrRecord = this.readHarvestRecords(ridSet, context);
  if(mapHrRecord.size() < 1 || mapHrRecord.get(rid) == null) {
    throw new SearchException("Could not get record from db with rid = " + rid);
  }
  return mapHrRecord.get(rid);

}

/**
 * Read harvest records.
 * 
 * @param rids the rids
 * @param context the context
 * @return the map between rid and hrRecord (never null)
 * @throws SearchException the search exception
 * 
 * 
 */
public Map<String, HrRecord> readHarvestRecords(StringSet rids,
    RequestContext context) throws SearchException {
  // TODO: Sort out exception issues
  HrRecord record = null;
  PreparedStatement st = null;
  ManagedConnection mcon = null;
  ResultSet rs = null;
  Map<String, HrRecord> mpRidRecords = new 
    TreeMap<String, HrRecord>(String.CASE_INSENSITIVE_ORDER);  
  Map<String, String> idFields = new 
    TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

  for (String rid : rids) {
    rid = Val.chkStr(rid);
    if (rid.length() < 1) {
      continue;
    }

    String field = "DOCUUID";
    try {
      Integer.parseInt(rid);
      field = "ID";
    } catch (NumberFormatException nfe) {
      if (!UuidUtil.isUuid(rid)) {
        rid = rid.toUpperCase();
        field = "UPPER(TITLE)";
      }
    }
    idFields.put(rid, field);
  }

  // Create sql to query db
  String table = context.getCatalogConfiguration().getResourceTableName();
  String sql = "SELECT PROTOCOL_TYPE, HOST_URL, PROTOCOL, TITLE, " +
  		"DOCUUID, ID FROM "
      + table + " WHERE ";
  
  int i = 0;
  int maxEntries = idFields.size();
  
  
  for (Map.Entry<String, String> entry : idFields.entrySet()) {
    sql += entry.getValue() + "= ?";
    if (i >= 0 && i < maxEntries - 1) {
      sql += " or ";
    }
    i++; 

  }
  
  sql += " AND ((APPROVALSTATUS = 'approved') OR (APPROVALSTATUS = 'reviewed'))";
  sql += " AND SEARCHABLE = 'true'";

  LOG.info("DB Query " + sql);
  ArrayList<HrRecord> arrHrRecords = new ArrayList<HrRecord>();
  try {
    mcon = context.getConnectionBroker().returnConnection("");
    st = mcon.getJdbcConnection().prepareStatement(sql);
    int parNum = 1;
    for (Map.Entry<String, String> entry : idFields.entrySet()) {
      String field = entry.getValue();
      String rid = entry.getKey();
      if (field.equalsIgnoreCase("ID")) {
        st.setInt(parNum, Val.chkInt(rid, Integer.MIN_VALUE));
      } else {
        st.setString(parNum, rid);
      }
      parNum++;
    }
    rs = st.executeQuery();
   
    while (rs.next()) {
      
      // Assembling records for each id
      record = new HrRecord();
      record.setHostUrl(rs.getString(2));

      String protocolDef = rs.getString(3);
      Protocol protocol = getRequestContext().getApplicationConfiguration()
          .getProtocolFactories().parseProtocol(protocolDef);
      try {
        record.setProtocol(protocol);
      } catch (IllegalArgumentException ex) {
        record.setProtocol(new HarvestProtocolNone());
      }
      record.setName(rs.getString(4));
      record.setUuid(rs.getString(5));
      LOG
          .info("Name = " + record.getName() + "protocol "
              + protocol.toString());
      
      // Attempting to marshall db with ids that have been given
      String id = Val.chkStr(rs.getString("ID"));
      String idValue = Val.chkStr(idFields.get(id));
      String docuuid = Val.chkStr(rs.getString("DOCUUID"));
      String docuuidValue = Val.chkStr(idFields.get(docuuid));
      String title = Val.chkStr(rs.getString("TITLE"));
      String titleValue = Val.chkStr(idFields.get(title));
      if(!"".equals(docuuidValue)) {
        mpRidRecords.put(docuuid, record);
      } else if (!"".equals(titleValue)) {
        mpRidRecords.put(title, record);
      } else if (!"".equals(idValue)) {
        mpRidRecords.put(id, record);
      }
    }
    
  } catch (Exception e) {
    throw new SearchException(e);
    // throw new SearchException("Could not get Harvest Record with UUID/ID = "
    // + rid + ":" + e.getMessage(), e);
  } finally {
    BaseDao.closeStatement(st);
    BaseDao.closeResultSet(rs);
  }

  return mpRidRecords;

}

}
