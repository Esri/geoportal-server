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
package com.esri.gpt.control.webharvest.client.arcims;

import com.esri.gpt.catalog.arcims.ExtendedQueryRequest;
import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.UuidUri;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

/**
 * ArcIMS records.
 */
class ArcImsRecords implements Iterable<Resource> {
/** logger */
private static final Logger LOGGER = Logger.getLogger(ArcImsRecords.class.getCanonicalName());
/** iteration context */
private IterationContext context;
/** service proxy */
private ArcImsProxy proxy;
/** query criteria */
private Criteria criteria;

/**
 * Creates instance of the folders.
 * @param context iteration context
 * @param proxy service proxy
 * @param criteria query criteria
 */
public ArcImsRecords(IterationContext context, ArcImsProxy proxy, Criteria criteria) {
  if (context==null) throw new IllegalArgumentException("No context provided.");
  if (proxy==null) throw new IllegalArgumentException("No proxy provided.");
  this.context = context;
  this.proxy = proxy;
  this.criteria = criteria;
}

public Iterator<Resource> iterator() {
  return new ArcImsRecordsIterator();
}

/**
 * ArcIMS records iterator.
 */
private class ArcImsRecordsIterator extends ReadOnlyIterator<Resource> {
  private Iterator<Resource> iterator;
  /** no more records*/
  private boolean noMore;

  public boolean hasNext() {
    if (noMore) return false;
    if (iterator==null) {
      try {
        advanceToNextRecords();
      } catch (ImsServiceException ex) {
        noMore = true;
        context.onIterationException(ex);
      }
    }
    return iterator!=null? iterator.hasNext(): false;
  }

  public Resource next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    return iterator.next();
  }

  private void advanceToNextRecords() throws ImsServiceException {
    ExtendedQueryRequest queryRequest = proxy.getQueryRequest(criteria);
    queryRequest.execute();
    iterator = new ArcImsRecordsAdapter(proxy,convertUuidsToSourceUris(queryRequest.getUuids())).iterator();
  }
}

/**
 * Converts uuids to source uris.
 * @param uuids uuids
 * @return source uris
 */
private Set<SourceUri> convertUuidsToSourceUris(Set<String> uuids) {
  HashSet<SourceUri> sourceUris = new HashSet<SourceUri>();
  if (uuids!=null) {
    for (String uuid : uuids) {
      sourceUris.add(new UuidUri(uuid));
    }
  }
  return sourceUris;
}

}
