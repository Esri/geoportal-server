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
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.protocol.Protocol;
import com.esri.gpt.control.webharvest.protocol.ProtocolInvoker;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.Val;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Execution unit.
 */
public abstract class ExecutionUnit {

/** task */
private Task task;  
/** query  builder */
private QueryBuilder queryBuilder;
/** cleanup flag */
private boolean cleanup;
/** publisher */
private Publisher publisher;
/** attributes */
private Map<String,Object> attributes = new HashMap<String,Object>();
/** restrictions */
private Set<String> restrictions = new HashSet<String>();

/**
 * Creates instance of the execution unit.
 * @param task task
 */
public ExecutionUnit(Task task) {
  if (task == null)
    throw new IllegalArgumentException("No task provided.");
  this.task = task;
  this.cleanup = ProtocolInvoker.getUpdateContent(task.getResource().getProtocol()) && task.getCriteria().getFromDate()==null;
  this.queryBuilder = task.getResource().newQueryBuilder(new IterationContext() {
      @Override
      public void onIterationException(Exception ex) {
        ExecutionUnit.this.onIteratonException(ex);
      }
  });
  if (this.queryBuilder == null) {
    throw new IllegalArgumentException("No query builder can be created.");
  }
  Protocol protocol = task.getResource().getProtocol();
  if (protocol!=null) {
    String r = Val.chkStr(protocol.getAttributeMap().getValue("restrictions"));
    String [] ar = r.split(",");
    restrictions.addAll(Arrays.asList(ar));
  }
}

protected abstract void onIteratonException(Exception ex);
/**
 * Gets query.
 * @return query
 */
public Query getQuery() {
  return queryBuilder.newQuery(task.getCriteria());
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
  return task.getCriteria();
}

/**
 * Gets repository.
 * @return repository
 */
public HrRecord getRepository() {
  return task.getResource();
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

/**
 * Sets attribute.
 * @param name name
 * @param value value
 */
public void setAttribute(String name, Object value) {
  attributes.put(name, value);
}

/**
 * Gets attribute.
 * @param name name
 * @return value
 */
public Object getAttribute(String name) {
  return attributes.get(name);
}

/**
 * Gets restrictions.
 * @return restrictions
 */
public Set<String> getRestrictions() {
  return restrictions;
}

@Override
public String toString() {
  return "{ uuid: " +getRepository().getUuid()+ ", protocol: " + getRepository().getProtocol().getKind()+", criteria: "+getCriteria()+"}";
}
}
