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

import java.sql.SQLException;

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.Val;

/**
 * Publishes a record by executing a request against an ArcIMS
 * metadata publish service.
 */
public class PutMetadataRequest extends PublishServiceRequest {

// class variables =============================================================

// instance variables ==========================================================
private boolean lockTitle;
// constructors ================================================================

/** Default constructor. */
public PutMetadataRequest() {}

/**
 * Constructs with an associated request context and publisher.
 * @param requestContext the request context
 * @param publisher the publisher
 */
public PutMetadataRequest(RequestContext requestContext,
                          Publisher publisher) {
  super(requestContext,publisher);
}

// properties ==================================================================

/**
 * Checks if lock title.
 * If a flag is set, it means a title is locked and synchronizer is not allowed
 * to update it, although all the rest of information is allowed to be updated.
 * @return <code>true</code> to lock title
 */
public boolean getLockTitle() {
  return lockTitle;
}

/**
 * Sets if lock title
 * If a flag is set, it means a title is locked and synchronizer is not allowed
 * to update it, although all the rest of information is allowed to be updated.
 * @param lockTitle <code>true</code> to lock title
 */
public void setLockTitle(boolean lockTitle) {
  this.lockTitle = lockTitle;
}

// methods =====================================================================

/**
 * Escapes special xml characters within a string.
 * @param s the string to escape
 * @return escaped string
 */
private String esc(String s) {
  return Val.escapeXml(s);
}

/**
 * Executes a PUT_METADATA request against an ArcIMS metadata publish service.
 * @param info the information for the document to be published
 * @return true if the action status was ok
 * @throws PublishServiceException  if an exception occurs
 */
public boolean executePut(PutMetadataInfo info)
  throws ImsServiceException {
  
  // check for the metadata server data access proxy, use if active
  ImsMetadataProxyDao proxy = new ImsMetadataProxyDao(this.getRequestContext(),this.getPublisher());
  try {
    proxy.insertRecord(this,info);
    return wasActionOK();
  } catch (SQLException e) {
    throw new ImsServiceException(e.toString(),e);
  }
}

/**
 * Checks to see if a string is a URL.
 * @param s the string to check
 * @return <b>true</b> if the string is a url
 */
private boolean isUrl(String s) {
  s = s.toLowerCase();
  return s.startsWith("http:") || s.startsWith("https:") || s.startsWith("ftp:");
}

/**
 * Prepares the xml for publication.
 * @param xml the xml string to prepare
 * @return the modified xml string
 */
private String prepareXml(String xml) {
  xml = Val.chkStr(xml);

  // remove the processing instruction
  if (xml.startsWith("<?xml")) {
    int nIdx = xml.indexOf("?>") ;
    if (nIdx > 0) {
      xml = xml.substring(nIdx+2,xml.length());
    }
  }

  // get rid of anything following the last closing tag
  if (!xml.endsWith(">")) {
    int nIdx = xml.lastIndexOf(">");
    if (nIdx != -1) {
      if (nIdx < (xml.length()-1)) {
        xml = xml.substring(0,nIdx+1) ;
      }
    }
  }

  // encode double hyphens
  xml = xml.replaceAll("--","&#45;&#45;");

 // there was somthing in the original code (ArcIMSUtil)
 // about encoding ampersands here ??

  // add the processing instruction
  xml = "<?xml version=\"1.0\"?>"+xml;
  return xml;
}

}

