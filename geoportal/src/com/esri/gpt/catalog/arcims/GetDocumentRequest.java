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
package com.esri.gpt.catalog.arcims;

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;

import java.sql.SQLException;
import java.sql.Timestamp;
import javax.xml.transform.TransformerException;
import org.xml.sax.Attributes;

/**
 * Retrieves a document executing a request against an ArcIMS metadata publish service.
 */
public class GetDocumentRequest extends PublishServiceRequest {

// class variables =============================================================

// instance variables ==========================================================
private String    _thumbnailUrl = "";
private Timestamp _updateDate = null;
private String    _uuid = "";
private String    _xml = "";
private String    _xmlUrl = "";

// constructors ================================================================

/** Default constructor. */
public GetDocumentRequest() {}

/**
 * Constructs with an associated request context and publisher.
 * @param requestContext the request context
 * @param publisher the publisher
 */
public GetDocumentRequest(RequestContext requestContext,
                           Publisher publisher) {
  super(requestContext, publisher);
}

// properties ==================================================================

/**
 * Gets the url to the thumbnail image.
 * @return the thumbnail url
 */
private String getThumbnailUrl() {
  return _thumbnailUrl;
}

/**
 * Sets the url to the thumbnail image.
 * @param url the thumbnail url
 */
private void setThumbnailUrl(String url) {
  _thumbnailUrl = Val.chkStr(url);
}

/**
 * Gets the update date.
 * @return the update date
 */
public Timestamp getUpdateDate() {
  return _updateDate;
}

/**
 * Sets the update date.
 * @param updateDate the update date
 */
private void setUpdateDate(String updateDate) {
  updateDate = Val.chkStr(updateDate);
  if (updateDate.length() == 0) {
    _updateDate = null;
  } else {
    try {
      _updateDate = Timestamp.valueOf(updateDate);
    } catch (Exception e) {
      _updateDate = null;
      System.err.println("Error setting update date: " + updateDate);
      e.printStackTrace(System.err);
    }
  }
}

/**
 * Sets the update date.
 * @param updateDate the update date
 */
protected void setUpdateTimestamp(Timestamp updateDate) {
  _updateDate = updateDate;
}

/**
 * Gets the document uuid.
 * @return the document uuid
 */
public String getUuid() {
  return _uuid;
}

/**
 * Sets the document uuid
 * @param uuid the document uuid
 */
private void setUuid(String uuid) {
  _uuid = UuidUtil.addCurlies(uuid);
}

/**
 * Gets the url to the xml document.
 * @return the url to the xml document
 */
private String getXmlUrl() {
  return _xmlUrl;
}

/**
 * Sets the url to the xml document.
 * @param url the xml document url
 */
private void setXmlUrl(String url) {
  _xmlUrl = Val.chkStr(url);
}

/**
 * Gets the document xml.
 * @return the document xml
 */
public String getXml() {
  return _xml;
}

/**
 * Sets the document xml.
 * @param xml the document xml
 */
protected void setXml(String xml) {
  _xml = Val.chkStr(xml);
}

// methods =====================================================================
/**
 * Executes a GET_METADATA_DOCUMENT request against an ArcIMS metadata publish service.
 * @param docUuid the metatata document uuid of the record to load
 * @throws PublishServiceException if an exception occurs
 */
public void executeGet(String docUuid)
  throws ImsServiceException, TransformerException {
  reset();
  setUuid(docUuid);
  
  // check for the metadata server data access proxy, use if active
  ImsMetadataProxyDao proxy = new ImsMetadataProxyDao(this.getRequestContext(),this.getPublisher());
  try {
    proxy.readRecord(this,this.getUuid());
    return;
  } catch (SQLException e) {
    throw new ImsServiceException(e.toString(),e);
  }
}

/**
 * Triggered when a SAX element is started during the parsing of an axl response.
 * @param lowerCaseTagName the lower-case tag name of the element
 * @param attributes the element attributes
 */
@Override
protected void onStartSaxElement(String lowerCaseTagName, Attributes attributes) {
  if (lowerCaseTagName.equals("metadata_dataset")) {
    setXmlUrl(attributes.getValue("url"));
    setThumbnailUrl(attributes.getValue("thumbnail"));
    setUpdateDate(attributes.getValue("updated"));
  }
}

/**
 * Resets the request.
 */
public void reset() {
  setUuid("");
  setXml("");
  setXmlUrl("");
  setThumbnailUrl("");
  setUpdateDate(null);
}
}

