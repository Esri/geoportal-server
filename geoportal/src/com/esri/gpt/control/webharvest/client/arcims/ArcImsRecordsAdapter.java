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

import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.CommonPublishable;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * ArcIMS records adapter.
 */
class ArcImsRecordsAdapter implements Iterable<Resource> {
/** service proxy */
private ArcImsProxy proxy;
/** collection of record source URI's */
private Collection<SourceUri> records;

/**
 * Creates instance of the adapter.
 * @param proxy service proxy.
 * @param records collection of records source URI's
 */
public ArcImsRecordsAdapter(ArcImsProxy proxy, Collection<SourceUri> records) {
  if (proxy == null) throw new IllegalArgumentException("No proxy provided.");
  this.proxy = proxy;
  this.records = records;
}

public Iterator<Resource> iterator() {
  return new ArcImsRecordsIterator();
}

/**
 * ArcIMS records iterator.
 */
private class ArcImsRecordsIterator extends ReadOnlyIterator<Resource> {
/** iterator */
private Iterator<SourceUri> iterator = records.iterator();

public boolean hasNext() {
  return iterator.hasNext();
}

public Resource next() {
  return new CommonPublishable() {
    private SourceUri uri = iterator.next();

    public SourceUri getSourceUri() {
      return uri;
    }

    public String getContent() throws IOException {
      return proxy.read(uri.asString());
    }
  };
}
}
}
