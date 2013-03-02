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
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.geometry.Envelope;

/**
 * Maintains information associated with a metadata document
 * to be published to an ArcIMS metadata server.
 */ 
public class PutMetadataInfo {
  
// class variables =============================================================

// instance variables ==========================================================
private String   _contentType = "";
private Envelope _envelope;
private String   _fileIdentifier = "";
private String   _name = "";
private String   _onlink = "";
private String   _parentUuid = "";
private String   _server = "";
private String   _service = "";
private String   _serviceType = "";
private String   _thumbnailBinary = null;
private String   _toEsriIsoXslt = "";
private String   _uuid = "";
private String   _xml = "";

// constructors ================================================================

/** Default constructor. */
public PutMetadataInfo() {}

// properties ==================================================================

/**
 * Gets the content type.
 * @return the content type
 */
public String getContentType() {
  return _contentType;
}
/**
 * Sets the content type.
 * @param contentType the content type
 */
public void setContentType(String contentType) {
  _contentType = Val.chkStr(contentType);
}

/**
 * Gets the bounding envelope for the document.
 * @return the bounding envelope
 */
public Envelope getEnvelope() {
  return _envelope;
}
/**
 * Sets the bounding envelope for the document.
 * @param envelope the bounding envelope
 */
public void setEnvelope(Envelope envelope) {
  _envelope = envelope;
}

/**
 * Gets the file identifier.
 * <br/>The file identifier is typically associated with ISO-19139 documents.
 * @return the file identifier
 */
public String getFileIdentifier() {
  return _fileIdentifier;
}
/**
 * Sets the file identifier.
 * <br/>The file identifier is typically associated with ISO-19139 documents.
 * @param id the file identifier
 */
public void setFileIdentifier(String id) {
  _fileIdentifier = Val.chkStr(id);
}

/**
 * Gets the name.
 * @return the name
 */
public String getName() {
  return _name;
}
/**
 * Sets the name.
 * @param name the name
 */
public void setName(String name) {
  _name = Val.chkStr(name);
}

/**
 * Gets the online linkage url.
 * @return the online linkage url
 */
public String getOnlink() {
  return _onlink;
}
/**
 * Sets the online linkage url.
 * @param url the online linkage url
 */
public void setOnlink(String url) {
  _onlink = Val.chkStr(url);
}

/**
 * Gets the parent UUID of the containing folder for the document.
 * @return the parent UUID
 */
public String getParentUuid() {
  return _parentUuid;
}
/**
 * Sets the parent UUID of the containing folder for the document.
 * @param uuid the parentUUID
 */
public void setParentUuid(String uuid) {
  _parentUuid = UuidUtil.addCurlies(uuid);
}

/**
 * Gets the map server.
 * @return the map server
 */
public String getServer() {
  return _server;
}
/**
 * Sets the map server.
 * @param server the map server
 */
public void setServer(String server) {
  _server = Val.chkStr(server);
}

/**
 * Gets the map service.
 * @return the map service
 */
public String getService() {
  return _service;
}
/**
 * Sets the map service.
 * @param service the map service
 */
public void setService(String service) {
  _service = Val.chkStr(service);
}

/**
 * Gets the map service type.
 * @return the map service type
 */
public String getServiceType() {
  return _serviceType;
}
/**
 * Sets the map service type.
 * @param type the map service type
 */
public void setServiceType(String type) {
  _serviceType = Val.chkStr(type);
}

/**
 * Gets the base64 encoded string for the thumbnail image.
 * @return the base64 encoded string for the thumbnail image
 */
public String getThumbnailBinary() {
  return _thumbnailBinary;
}
/**
 * Sets the base64 encoded string for the thumbnail image.
 * @param base64 the base64 encoded string for the thumbnail image
 */
public void setThumbnailBinary(String base64) {
  _thumbnailBinary = base64;
}

/**
 * Gets the XSLT (file path) for translating to EsriIso format.
 * <p/>
 * When a schema is neither FGDC or EsriIso, the document must
 * be translated to EsriIso and enclosed as a binary node prior
 * to publishing to the ArcIMS metdata server.
 * @return the file path to the XSLT
 */
public String getToEsriIsoXslt() {
  return _toEsriIsoXslt;
}
/**
 * Gets the XSLT (file path) for translating to EsriIso format.
 * <p/>
 * When a schema is neither FGDC or EsriIso, the document must
 * be translated to EsriIso and enclosed as a binary node prior
 * to publishing to the ArcIMS metdata server.
 * @param xslt the file path to the XSLT
 */
public void setToEsriIsoXslt(String xslt) {
  _toEsriIsoXslt = Val.chkStr(xslt);
}

/**
 * Gets the UUID for the document.
 * @return the UUID
 */
public String getUuid() {
  return _uuid;
}
/**
 * Sets the UUID for the document.
 * @param uuid the UUID
 */
public void setUuid(String uuid) {
  _uuid = UuidUtil.addCurlies(uuid);
}

/**
 * Gets the xml string for the document.
 * @return the xml string
 */
public String getXml() {
  return _xml;
}
/**
 * Sets the xml string for the document.
 * @param xml the xml string
 */
public void setXml(String xml) {
  _xml = Val.chkStr(xml);
}

// methods =====================================================================

}

