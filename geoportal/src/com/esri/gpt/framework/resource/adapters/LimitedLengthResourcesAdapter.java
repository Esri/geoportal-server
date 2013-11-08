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
package com.esri.gpt.framework.resource.adapters;

import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Limeited length resource adapter.
 */
public class LimitedLengthResourcesAdapter implements Iterable<Resource> {

/** collection of resources */
private Iterable<? extends Resource> resources;
/** max recs */
private Integer maxRecs;


/**
 * Creates instance of the adapter.
 * @param resource single resource
 * @param maxRecs maximum records
 */
public LimitedLengthResourcesAdapter(Resource resource, Integer maxRecs) {
  this(Arrays.asList(new Resource[]{resource}), maxRecs);
}

/**
 * Creates instance of the adapter.
 * @param resources collection of resources
 * @param maxRecs maximum records
 */
public LimitedLengthResourcesAdapter(Iterable<? extends Resource> resources, Integer maxRecs) {
  this.resources = resources;
  this.maxRecs = maxRecs;
}

public Iterator<Resource> iterator() {
  return new LimitedLengthResourcesIterator();
}

/**
 * Limited length resource iterator.
 */
private class LimitedLengthResourcesIterator extends ReadOnlyIterator<Resource> {
/** underlying iterator */
private Iterator<? extends Resource> iterator = resources.iterator();
/** counter */
private int counter;

public boolean hasNext() {
  return iterator.hasNext() && (maxRecs==null || counter<maxRecs);
}

public Resource next() {
  if (!hasNext()) {
    throw new NoSuchElementException("No more resources.");
  }
  counter++;
  return iterator.next();
}
}
}
