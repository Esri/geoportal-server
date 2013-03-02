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
import com.esri.gpt.catalog.management.MmdEnums;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.principal.Publisher;

/**
 * Handles the publishing of an uploaded metadata document.
 */
public class UploadRequest extends PublicationRequest {
  
// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

/**
 * Constructs a request to publish an uploaded metadata document.
 * @param requestContext the request context
 * @param publisher the publisher
 * @param sourceFileName the file name associated with the upload 
 * @param sourceXml the XML file content
 */
public UploadRequest(RequestContext requestContext, 
                     Publisher publisher,
                     String sourceFileName,
                     String sourceXml) {
  super(requestContext,publisher,sourceXml);
  String sMethod = MmdEnums.PublicationMethod.upload.toString();
  getPublicationRecord().setPublicationMethod(sMethod);
  getPublicationRecord().setSourceFileName(sourceFileName);
}

// properties ==================================================================

// methods =====================================================================

}
