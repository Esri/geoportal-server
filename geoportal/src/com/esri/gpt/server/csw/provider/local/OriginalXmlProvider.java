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
package com.esri.gpt.server.csw.provider.local;
import com.esri.gpt.catalog.arcims.ImsMetadataAdminDao;
import com.esri.gpt.catalog.schema.MetadataDocument;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.metadata.MetadataAcl;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.server.csw.provider.components.IOriginalXmlProvider;
import com.esri.gpt.server.csw.provider.components.OperationContext;

/**
 * Provides an XML document from the local catalog in it's original schema.
 */
public class OriginalXmlProvider implements IOriginalXmlProvider {
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public OriginalXmlProvider() {}
  
  /** methods ================================================================= */
  
  /**
   * Provides an XML document in it's original schema.
   * @param context the operation context
   * @param id the document id
   * @return the original XML string
   */
  public String provideOriginalXml(OperationContext context, String id) throws Exception {
    RequestContext reqContext = context.getRequestContext();
    
    // if a file-identifier was passed, determine it's associated uuid
    ImsMetadataAdminDao dao = new ImsMetadataAdminDao(reqContext);
    String docUuid = dao.findUuid(id);
    if (docUuid.length() > 0) {
      id = docUuid;
    } else {
      return "";
    }

    // ensure access to the document
    MetadataAcl acl = new MetadataAcl(reqContext);
    boolean bHasAccess = acl.hasReadAccess(reqContext.getUser(),id);
    if (!bHasAccess) {
      throw new NotAuthorizedException("Access denied.");
    }

    // read and return the xml
    return dao.readXml(id);
  }

}
