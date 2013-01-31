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
package com.esri.gpt.control.livedata;

import com.esri.gpt.framework.util.Val;
import org.w3c.dom.Document;

/**
 * Host response.
 */
public class HostResponse {

/** content type */
private String contentType = "";
/** document */
private Document document;

/**
 * Creates instance of host respone.
 */
public HostResponse() {}

/**
 * Creates instance of host response.
 * @param contentType content type
 * @param document document
 */
public HostResponse(String contentType, Document document) {
  setContentType(contentType);
  setDocument(document);
}
/**
 * Gets content type.
 * @return the contentType
 */
public String getContentType() {
  return contentType;
}

/**
 * Sets content type.
 * @param contentType the contentType to set
 */
public void setContentType(String contentType) {
  this.contentType = Val.chkStr(contentType);
}

/**
 * @return the document
 */
public Document getDocument() {
  return document;
}

/**
 * @param document the document to set
 */
public void setDocument(Document document) {
  this.document = document;
}
}
