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

import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.principal.Publisher;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Deletes collection of the source URI's.
 */
public class DeleteSourceUrisRequest {
/** request context */
private RequestContext requestContext;
/** publisher */
private Publisher publisher;
/** iterable */
private Iterable<Map.Entry<String,String>> sourceUriData;
/** deleted count */
private volatile long deletedCount;

public DeleteSourceUrisRequest(RequestContext requestContext, Publisher publisher, Iterable<Map.Entry<String,String>> sourceUriData) {
  this.requestContext = requestContext;
  this.publisher = publisher;
  this.sourceUriData = sourceUriData;
}

public void execute() throws ImsServiceException, SQLException, CatalogIndexException, IOException {
  deletedCount = 0;
  CatalogDao dao = new CatalogDao(requestContext);
  CatalogDao.CatalogRecordListener listener = new CatalogDao.CatalogRecordListener() {
    @Override
    public void onRecord(String sourceUri, String uuid) {
      deletedCount++;
    }
  };
  dao.deleteSourceURIs(publisher, sourceUriData, listener);
}

public long getDeletedCount() {
  return deletedCount;
}
}
