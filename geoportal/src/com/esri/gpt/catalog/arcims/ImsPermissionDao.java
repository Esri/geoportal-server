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
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.identity.local.LocalDao;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.sql.BaseDao;
import com.esri.gpt.framework.util.UuidUtil;

import java.sql.SQLException;

/**
 * Database access object associated with ArcIMS service permissions.
 */
public class ImsPermissionDao extends BaseDao {
  
// class variables =============================================================
  
/** Administrator role - "metadata_administrator" */
public static final String ROLE_ADMINISTRATOR = "metadata_administrator";

/** Browser role - "metadata_browser" */
public static final String ROLE_BROWSER = "metadata_browser";

/** Browse all role - "metadata_browser_all" */
public static final String ROLE_BROWSER_ALL = "metadata_browser_all";

/** Publisher role - "metadata_publisher" */
public static final String ROLE_PUBLISHER = "metadata_publisher";
  
// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
protected ImsPermissionDao() {
  super();
}

/**
 * Constructs with an associated request context.
 * @param requestContext the request context
 */
public ImsPermissionDao(RequestContext requestContext) {
  super(requestContext);
}

// properties ==================================================================
// methods =====================================================================

/**
 * Prepares a publisher prior to submitting a request to an
 * ArcIMS metadata service request.
 * <br/>Authentication/authorization does not occur in this step.
 * <br/>It is assumed that any authentication/authorization has occurred prior
 * the execution of this method.
 * @param publisher the subject publisher
 * @param ensureDefaultFolder if true also ensure that a folder exists 
 *        for the publisher 
 * @throws IdentityException if an integrity violation occurs
 * @throws SQLException if a database exception occurs
 */
public void preparePublisher(Publisher publisher, 
                             boolean ensureDefaultFolder) 
  throws IdentityException, SQLException {
  
  // ensure the the publisher is referenced within the local user table
  LocalDao localDao = new LocalDao(getRequestContext());
  if (publisher.getIsRemote()) {
    localDao.ensureReferenceToRemoteUser(publisher);
  }
  
  // ensure that the publisher has a folder
  if (ensureDefaultFolder) {
    publisher.setFolderUuid(UuidUtil.makeUuid(true));
  }
}

}
