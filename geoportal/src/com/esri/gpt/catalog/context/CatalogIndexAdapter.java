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
package com.esri.gpt.catalog.context;
import com.esri.gpt.catalog.arcims.GetDocumentRequest;
import com.esri.gpt.catalog.arcims.ImsMetadataAdminDao;
import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.catalog.schema.MetadataDocument;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.catalog.schema.SchemaException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.sql.BaseDao;
import com.esri.gpt.framework.util.Val;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;


/**
 * Super-class for an adapter that maintains and searches an index of 
 * metadata documents.
 */
public abstract class CatalogIndexAdapter extends BaseDao {
  
// class variables =============================================================
  
/** Logger */
private static final Logger LOGGER = Logger.getLogger(CatalogIndexAdapter.class.getName());
  
// instance variables ==========================================================
  
// constructors ================================================================

/**
 * Constructs with an associated request context.
 * @param requestContext the request context
 */
public CatalogIndexAdapter(RequestContext requestContext) {
  super(requestContext);
}

// properties ==================================================================

/**
 * Gets the logger.
 * @return the logger
 */
protected Logger getLogger() {
  return LOGGER;
}

// methods =====================================================================

/**
 * Counts the documents within the index.
 * @throws CatalogIndexException if an exception occurs
 */
public abstract int countDocuments() throws CatalogIndexException;

/**
 * Deletes a  document from the index.
 * @param uuid the document UUID to delete 
 * @throws CatalogIndexException if an exception occurs
 */
public void deleteDocument(String uuid) throws CatalogIndexException {
  String[] uuids = {uuid};
  deleteDocuments(uuids);
}

/**
 * Deletes a collection of documents from the index.
 * @param uuids the collection of document UUIDS to delete 
 * @throws CatalogIndexException if an exception occurs  
 */
public abstract void deleteDocuments(String[] uuids) 
  throws CatalogIndexException;

/**
 * Publishes a document to the index, first reading it's XML from the 
 * metadata server.
 * <br/>The document will not be written if the UUID is null or zero-length.
 * <br/>Existing documents that match the supplied UUID will be replaced.
 * @param uuid the document's UUID
 * @param publisher a publisher who has read access to the document
 * @return the raw XML associated with the document being published
 * @throws CatalogIndexException if an exception occurs 
 */
public String publishDocument(String uuid, Publisher publisher) 
  throws CatalogIndexException {
  uuid = Val.chkStr(uuid);
  if (uuid.length() == 0) {
    throw new IllegalArgumentException("The supplied document UUID was empty.");
  } else {
    try {
      
      /*
       CatalogIndexAdapter: 108
       LuceneIndexAdapter:  294
       ImsMetadataAdminDao: updaterecord, add queryAcl(uuid)
       CSW discovery servlet -> check credentials before returning xml
       Rest -> credentials
       */
      
      // determine the acl
      ImsMetadataAdminDao adminDao = new ImsMetadataAdminDao(getRequestContext());
      String acl = adminDao.queryAclByUUID(uuid);
      
      // determine the xml and update date
      GetDocumentRequest ims = new GetDocumentRequest(getRequestContext(),publisher);
      ims.executeGet(uuid);
      Timestamp updateDate = ims.getUpdateDate();
      String xml = ims.getXml();
      
      // build the schema
      MetadataDocument mdDoc = new MetadataDocument();
      Schema schema = mdDoc.prepareForView(getRequestContext(),xml);
      
      // publish
      publishDocument(uuid,updateDate,schema,acl);
      
      return xml;
    } catch (SQLException e) {
      String sMsg = "Error indexing document:\n "+Val.chkStr(e.getMessage());
      throw new CatalogIndexException(sMsg,e);
    } catch (ImsServiceException e) {
      String sMsg = "Error indexing document:\n "+Val.chkStr(e.getMessage());
      throw new CatalogIndexException(sMsg,e);
    } catch (TransformerException e) {
      String sMsg = "Error indexing document:\n "+Val.chkStr(e.getMessage());
      throw new CatalogIndexException(sMsg,e);
    } catch (SchemaException e) {
      String sMsg = "Error indexing document:\n "+Val.chkStr(e.getMessage());
      throw new CatalogIndexException(sMsg,e);
    }
  }
}

/**
 * Publishes a document to the index based upon a metadata schema.
 * <br/>The document will not be written if the UUID is null or zero-length.
 * <br/>Existing documents that match the supplied UUID will be replaced.
 * @param uuid the document's UUID
 * @param updateDate the document's update date
 * @param schema the evaluated schema associated with the document to be indexed
 * @param acl an XML string represent the access control information associated
 *        the document (if null, no ACL information is indexed)
 * @throws CatalogIndexException if an exception occurs  
 */
public abstract void publishDocument(String uuid, 
                                     Timestamp updateDate,
                                     Schema schema,
                                     String acl) 
  throws CatalogIndexException;

/**
 * Purges the entire catalog index.
 * @throws CatalogIndexException if an exception occurs
 */
public abstract void purgeIndex() throws CatalogIndexException;

/**
 * Queries the ACL values indexed for a document.
 * @param uuid the document UUID
 * @return the ACL values (can be null)
 * @throws CatalogIndexException if an exception occurs
 */
public abstract String[] queryAcls(String uuid) 
  throws CatalogIndexException;

/**
 * Queries the system modified date associated with an indexed document.
 * @param uuid the document UUID
 * @return the update date (null if none was found)
 * @throws CatalogIndexException if an exception occurs
 */
public abstract Timestamp queryModifiedDate(String uuid) 
  throws CatalogIndexException;

}
