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

import com.esri.gpt.catalog.schema.MetadataDocument;
import com.esri.gpt.catalog.schema.SchemaException;
import com.esri.gpt.framework.context.RequestContext;

/**
 * Handles the validation of a metadata document.
 */
public class ValidationRequest extends PublicationRequest {

/**
 * Constructs a request to publish a metadata document.
 * @param requestContext the request context
 * @param sourceFileName the file name associated with the verify
 * @param sourceXml the XML content
 */
public ValidationRequest(RequestContext requestContext, String sourceFileName, String sourceXml) {
  super(requestContext,null,sourceXml);
  getPublicationRecord().setSourceFileName(sourceFileName);
}

/**
 * Verifies the document.
 * @throws SchemaException if a schems related exception occurs
 */
public void verify() throws SchemaException {

  // prepare the schema for publication, send the request
  MetadataDocument document = new MetadataDocument();
  document.prepareForPublication(this);
}

}
