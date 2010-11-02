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
package com.esri.gpt.control.webharvest.engine;

import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.control.webharvest.protocol.ProtocolInvoker;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.framework.security.principal.Publisher;

/**
 * Execution unit.
 */
class ExecutionUnit {

/** repository */
private HrRecord repository;
/** criteria */
private Criteria criteria;
/** query  builder */
private QueryBuilder queryBuilder;
/** cleanup flag */
private boolean cleanup;
/** report builder */
private ReportBuilder rp;
/** collection of source URI's */
private SourceUriArray sourceUris;
/** publisher */
private Publisher publisher;

/**
 * Creates instance of the execution unit.
 * @param repository repository
 * @param criteria criteria
 * @param queryBuilder query builder
 */
public ExecutionUnit(HrRecord repository, Criteria criteria, QueryBuilder queryBuilder) {
  if (repository == null)
    throw new IllegalArgumentException("No repository provided.");
  if (criteria == null)
    throw new IllegalArgumentException("No criteria provided.");
  if (queryBuilder == null)
    throw new IllegalArgumentException("No query builder provided.");
  this.repository = repository;
  this.criteria = criteria;
  this.queryBuilder = queryBuilder;
  this.cleanup = ProtocolInvoker.getUpdateContent(repository.getProtocol());
}

/**
 * Gets query.
 * @return query
 */
public Query getQuery() {
  return queryBuilder.newQuery(criteria);
}

/**
 * Gets native resource.
 * @return native resource
 */
public Publishable getNative() {
  return queryBuilder.getNativeResource();
}

/**
 * Gets criteria.
 * @return criteria
 */
public Criteria getCriteria() {
  return criteria;
}

/**
 * Gets repository.
 * @return repository
 */
public HrRecord getRepository() {
  return repository;
}

/**
 * Gets cleanup flag.
 * @return <code>true</code> to perform cleanup
 */
public boolean getCleanupFlag() {
  return cleanup;
}

/**
 * Sets cleanup flag.
 * @param cleanup cleanup flag
 */
public void setCleanupFlag(boolean cleanup) {
  this.cleanup = cleanup;
}

/**
 * Sets report builder.
 * @param rp report builder
 */
public void setReportBuilder(ReportBuilder rp) {
  this.rp = rp;
}

/**
 * Gets report builder.
 * @return report builder
 */
public ReportBuilder getReportBuilder() {
  return this.rp;
}

/**
 * Gets collection of source URI's.
 * @return collection of source URI's
 */
public SourceUriArray getSourceUris() {
  return sourceUris;
}

/**
 * Sets collection of source URI's.
 * @param sourceUris collection of source URI's
 */
public void setSourceUris(SourceUriArray sourceUris) {
  this.sourceUris = sourceUris;
}

/**
 * Gets publisher.
 * @return publisher
 */
public Publisher getPublisher() {
  return publisher;
}

/**
 * Sets publisher.
 * @param publisher publisher
 */
public void setPublisher(Publisher publisher) {
  this.publisher = publisher;
}

@Override
public String toString() {
  return "{ uuid: " +repository.getUuid()+ ", protocol: " + repository.getProtocol().getKind()+", criteria: "+criteria+"}";
}
}
