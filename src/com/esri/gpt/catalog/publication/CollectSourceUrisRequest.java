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

import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.framework.context.RequestContext;
import java.sql.SQLException;

/**
 * Collects source URI's.
 */
public class CollectSourceUrisRequest {
/** request context */
private RequestContext requestContext;
/** repository */
private HrRecord repository;

/**
 * Creates instance of the request.
 * @param requestContext request context
 * @param repository harvest repository
 */
public CollectSourceUrisRequest(RequestContext requestContext, HrRecord repository) {
  if (requestContext==null) throw new IllegalArgumentException("No request context provided.");
  if (repository==null) throw new IllegalArgumentException("No repository provided.");
  this.requestContext = requestContext;
  this.repository = repository;
}

/**
 * Executes request.
 * @throws SQLException if accessing database fails
 */
public void execute() throws SQLException {
  CatalogDao dao = new CatalogDao(requestContext);
  dao.querySourceURIs(repository.getUuid(), new CatalogDao.CatalogRecordListener() {
      public void onRecord(String sourceUri, String uuid) {
        CollectSourceUrisRequest.this.onSourceUri(sourceUri, uuid);
      }
  });
}

/**
 * Called every time a source URI is found. Override to provide data handling.
 * @param sourceUri source URI
 * @param uuid record UUID
 */
protected void onSourceUri(String sourceUri, String uuid) {
}
}
