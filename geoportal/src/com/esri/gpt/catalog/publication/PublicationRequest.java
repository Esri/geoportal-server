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
import com.esri.gpt.catalog.arcims.ImsMetadataAdminDao;
import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.catalog.arcims.PutMetadataInfo;
import com.esri.gpt.catalog.arcims.PutMetadataRequest;
import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.catalog.management.MmdEnums;
import com.esri.gpt.catalog.management.MmdEnums.ApprovalStatus;
import com.esri.gpt.catalog.schema.MetadataDocument;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.catalog.schema.SchemaException;
import com.esri.gpt.catalog.schema.Schemas;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.sql.IClobMutator;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Super-class for a metadata document publication request.
 */
public class PublicationRequest {

// class variables =============================================================

// instance variables ==========================================================
private boolean           _hasMetadataServer = false;
private Publisher         _publisher;
private PublicationRecord _record = new PublicationRecord();
private RequestContext    _requestContext;
private boolean           _updateIndex = true;

// constructors ================================================================
  
/** Default constructor. */
public PublicationRequest() {}

/**
 * Constructs a request to publish a metadata document.
 * @param requestContext the request context
 * @param publisher the publisher
 * @param sourceXml the XML content
 */
public PublicationRequest(RequestContext requestContext, 
                          Publisher publisher,
                          String sourceXml) {
  setRequestContext(requestContext);
  setPublisher(publisher);
  getPublicationRecord().setSourceXml(sourceXml);
  
  // check for auto-approval (only applies to new records)
  if (requestContext != null) {
    StringAttributeMap params = requestContext.getCatalogConfiguration().getParameters();
    String sAuto = Val.chkStr(params.getValue("publicationRequest.autoApprove"));
    if (sAuto.toLowerCase().equals("true")) {
      this.getPublicationRecord().setAutoApprove(true);
    }
  }
}

public boolean getUpdateIndex() {
  return _updateIndex;
}

public void setUpdateIndex(boolean _updateIndex) {
  this._updateIndex = _updateIndex;
}

// properties ==================================================================

/**
 * Gets the configured schemas.
 * @return the configured schemas
 */
protected Schemas getConfiguredSchemas() {
  return getRequestContext().getCatalogConfiguration().getConfiguredSchemas();
}

/**
 * Gets the publisher associated with the request.
 * @return the publisher
 */
public Publisher getPublisher() {
  return _publisher;
}
/**
 * Sets the publisher associated with the request.
 * @param publisher the publisher
 */
private void setPublisher(Publisher publisher) {
  _publisher = publisher;
}

/**
 * Gets the associated publication record.
 * @return the publication record
 */
public PublicationRecord getPublicationRecord() {
  return _record;
}

/**
 * Gets the associated request context.
 * @return the request context
 */
public RequestContext getRequestContext() {
  return _requestContext;
}
/**
 * Sets the associated request context.
 * @param requestContext the request context
 */
private void setRequestContext(RequestContext requestContext) {
  _requestContext = requestContext;
}

// methods =====================================================================

/**
 * Determines and sets the source URI for the document.
 * @param schema the evaluated schema for the document being published
 */
protected void determineSourceUri(Schema schema) {
  PublicationRecord rec = getPublicationRecord();
  if (rec.getSourceUri().length() == 0) {
    String sUri = "";
    if (rec.getSourceFileName().length() > 0) {
      boolean bUsePrefix = true;
      String sTmp = rec.getSourceFileName().toLowerCase();
      if (sTmp.startsWith("http://") || sTmp.startsWith("https://") ||
          sTmp.startsWith("ftp://") || sTmp.startsWith("ftps://") ||
          sTmp.startsWith("\\\\")) {
        bUsePrefix = false;
      }
      if (bUsePrefix) {
        sUri = "userid:"+getPublisher().getLocalID()+"/"+rec.getSourceFileName();
      } else {
        sUri = rec.getSourceFileName();
      }
    }
    rec.setSourceUri(sUri);
  }
}

/**
 * Determines and sets the UUID for the document.
 * <br/>If the UUID has not been set, this step will attempt
 * to avoid duplication by querying the fileIdentifier and sourceUri.
 * If an existing record is not located, a new UUID is generated.
 * @param schema the evaluated schema for the document being published
 * @throws SQLException if a database exception occurs
 */
protected void determineUuid(Schema schema) throws SQLException {
  ImsMetadataAdminDao imsDao = null;
  PublicationRecord rec = getPublicationRecord();
  rec.setFileIdentifier(schema.getMeaning().getFileIdentifier());
  
  if (rec.getUuid().length() == 0) {
    String sEsriDocID = schema.getMeaning().getEsriDocID();
    if (UuidUtil.isUuid(sEsriDocID)) {
      rec.setUuid(sEsriDocID);
    }
  }
  
  if (rec.getUuid().length() == 0) {
    if (imsDao == null) imsDao = new ImsMetadataAdminDao(getRequestContext());
    String sUuid = imsDao.findExistingUuid(rec.getFileIdentifier(),null);
    if (sUuid.length() > 0) {
      rec.setUuid(sUuid);
    }
  }
  
  if (rec.getUuid().length() == 0) {
    String sFile = rec.getSourceFileName();
    if (sFile.endsWith(".xml")) {
      sFile = sFile.substring(0,sFile.length()-4);
      if (UuidUtil.isUuid(sFile)) {
        rec.setUuid(sFile);
      }
    }
  }
  
  if (rec.getUuid().length() == 0) {
    if (imsDao == null) imsDao = new ImsMetadataAdminDao(getRequestContext());
    String sUuid = imsDao.findExistingUuid(null,rec.getSourceUri());
    if (sUuid.length()>0) {
      rec.setUuid(sUuid);
    } else if (UuidUtil.isUuid(rec.getSourceUri())) {
      rec.setUuid(rec.getSourceUri());
    } else {
      sUuid = UuidUtil.makeUuid(true);
      rec.setUuid(sUuid);
    }
  }
}

/**
 * Reads the XML associated with a document uuid.
 * @param uuid the UUID for the record to read
 * @return the associated XML string (null if no match was found)
 * @throws SQLException if a database exception occurs
 */
private String readCurrentXml(String uuid) throws SQLException {
  PreparedStatement st = null;
  ResultSet rs = null;
  try {
    String adminTable = this.getRequestContext().getCatalogConfiguration().getResourceTableName();
    String metaTable = this.getRequestContext().getCatalogConfiguration().getResourceDataTableName();
    ManagedConnection mc = this.getRequestContext().getConnectionBroker().returnConnection("");
    Connection con = mc.getJdbcConnection();

    String sql = "SELECT DOCUUID FROM "+adminTable+" WHERE DOCUUID=?";
    st = con.prepareStatement(sql);
    st.setString(1,uuid);
    rs = st.executeQuery();
    if (rs.next()) {
      try {if (rs != null) rs.close();} catch (Exception ef) {}
      try {if (st != null) st.close();} catch (Exception ef) {}
      IClobMutator cm = mc.getClobMutator();
      sql = "SELECT XML FROM "+metaTable+" WHERE DOCUUID=?";
      st = con.prepareStatement(sql);
      st.setString(1,uuid);
      rs = st.executeQuery();
      if (rs.next()) {
        return cm.get(rs,1);
      }
    }

  } finally {
    try {if (rs != null) rs.close();} catch (Exception ef) {}
    try {if (st != null) st.close();} catch (Exception ef) {}
  }
  return null;
}

/**
 * Queries the approval status associated with a document.
 * @param uuid the document UUID
 * @return the approval status (empty string if not found)
 * @throws SQLException if a database exception occurs
 */
private String readStatus(String uuid) throws SQLException {
  PreparedStatement st = null;
  try {
    String adminTable = this.getRequestContext().getCatalogConfiguration().getResourceTableName();
    ManagedConnection mc = this.getRequestContext().getConnectionBroker().returnConnection("");
    Connection con = mc.getJdbcConnection();
    String sql = "SELECT APPROVALSTATUS FROM "+adminTable+" WHERE DOCUUID=?";
    st = con.prepareStatement(sql);
    st.setString(1,uuid);
    ResultSet rs = st.executeQuery();
    if (rs.next()) {
      return rs.getString(1);
    }
  } finally {
    try {if (st != null) st.close();} catch (Exception ef) {}
  }
  return null;
}

/**
 * Prepares publication record for publication.
 * @return schema
 * @throws SchemaException if record can not have associated schema
 * @throws SQLException if accessing database fails
 */
public Schema prepareForPublication()
  throws SchemaException, SQLException {

  // prepare the schema for publication, send the request
  MetadataDocument document = new MetadataDocument();
  Schema schema = document.prepareForPublication(this);
  determineSourceUri(schema);
  determineUuid(schema);

  return schema;
}

/**
 * Publishes the document.
 * @param schema document schema
 * @throws ImsServiceException in an exception occurs while communication
 *         with the ArcIMS metadata publishing service
 * @throws SQLException if a database exception occurs
 * @throws CatalogIndexException if a document indexing exception occurs
 */
public void publish(Schema schema)
  throws SQLException, ImsServiceException, CatalogIndexException {

  // don't update if the xml hasn't changed
  boolean bSend = true;
  String meth = Val.chkStr(this.getPublicationRecord().getPublicationMethod());
  boolean isEditor = meth.equalsIgnoreCase("editor") || meth.equalsIgnoreCase("seditor");
  if (!_hasMetadataServer && !isEditor && this.getPublicationRecord().getUpdateOnlyIfXmlHasChanged()) {
    String status = Val.chkStr(this.readStatus(getPublicationRecord().getUuid()));
    if (!status.equalsIgnoreCase("draft")) {
      String currentXml = Val.chkStr(this.readCurrentXml(getPublicationRecord().getUuid()));
      if (currentXml.length() > 0) {
        if (currentXml.equals(this.getPublicationRecord().getSourceXml())) {
          bSend = false;
          getPublicationRecord().setWasDocumentReplaced(true);
          getPublicationRecord().setWasDocumentUnchanged(true);
        }
      }
    }
  }
  if (bSend) {
    sendPublicationRequest(schema);
  }
}

/**
 * Publishes the document.
 * @throws SchemaException if a schems related exception occurs
 * @throws ImsServiceException in an exception occurs while communication
 *         with the ArcIMS metadata publishing service
 * @throws SQLException if a database exception occurs
 * @throws CatalogIndexException if a document indexing exception occurs
 */
public void publish() 
  throws SchemaException, ImsServiceException, SQLException, CatalogIndexException {

  Schema schema = prepareForPublication();
  publish(schema);
}

/**
 * Sends the publication request to ArcIMS, updates the administration table.
 * @throws ImsServiceException in an exception occurs while communication
 *         with the ArcIMS metadata publishing service
 * @throws SQLException if a database exception occurs
 * @throws CatalogIndexException if a document indexing exception occurs
 */
private void sendPublicationRequest(Schema schema) 
  throws ImsServiceException, SQLException, CatalogIndexException   {
  
  // prepare the ArcIMS request  
  PutMetadataRequest imsRequest;
  imsRequest = new PutMetadataRequest(getRequestContext(),getPublisher());
  imsRequest.setLockTitle(getPublicationRecord().getLockTitle());
  
  PutMetadataInfo putInfo = new PutMetadataInfo();
  putInfo.setUuid(getPublicationRecord().getUuid());
  putInfo.setXml(getPublicationRecord().getSourceXml());
  
  putInfo.setFileIdentifier(schema.getMeaning().getFileIdentifier());
  putInfo.setName(getPublicationRecord().getAlternativeTitle().length()>0? getPublicationRecord().getAlternativeTitle(): schema.getMeaning().getTitle());
  putInfo.setThumbnailBinary(schema.getMeaning().getThumbnailBinary());
  
  //putInfo.setParentUuid(getPublisher().getFolderUuid());
  //putInfo.setEnvelope(schema.getMeaning().getEnvelope());
  //putInfo.setToEsriIsoXslt(schema.getToEsriXslt());
  //putInfo.setContentType(schema.getMeaning().getArcIMSContentType());
  //putInfo.setOnlink(schema.getMeaning().getWebsiteUrl());
  //putInfo.setServer(schema.getMeaning().getResourceUrl());
  //putInfo.setService(schema.getMeaning().getServiceName());
  //putInfo.setServiceType(schema.getMeaning().getResourceType());
  
  // send the request to ArcIMS, determine if the document was replaced
  if(this._record != null ) {
    this.getRequestContext().getObjectMap().put(MmdEnums.INCOMING_STATUS, 
        this._record.getApprovalStatus());
  }
  imsRequest.executePut(putInfo);
  String sReplaced = PutMetadataRequest.ACTION_STATUS_REPLACED;
  boolean bReplaced = imsRequest.getActionStatus().equals(sReplaced);
  getPublicationRecord().setWasDocumentReplaced(bReplaced);
  if (!bReplaced && getPublicationRecord().getAutoApprove()) {
    String status = Val.chkStr(getPublicationRecord().getApprovalStatus());
    if (status.length() == 0) {
      getPublicationRecord().setApprovalStatus("approved");
    }
  }
  
  // update the administrative table
  ImsMetadataAdminDao imsDao = new ImsMetadataAdminDao(getRequestContext());
  imsDao.setUpdateIndex(getUpdateIndex());
  imsDao.updateRecord(schema,getPublicationRecord());
}

}