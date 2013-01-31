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

import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Provides collection of {@link Publishable} only found in a given collection of
 * resources.
 */
public class PublishablesAdapter implements Iterable<Publishable> {

private Iterable<Resource> resources;

/**
 * Creates instance of the adapter.
 * @param resources collection of resources
 */
public PublishablesAdapter(Iterable<Resource> resources) {
  this.resources = resources!=null? resources: new ArrayList<Resource>();
}

public Iterator<Publishable> iterator() {
  return new PublishableIterator();
}

/**
 * Publishable iterator.
 */
private class PublishableIterator extends ReadOnlyIterator<Publishable> {

/** iterator */
private Iterator<Resource> iterator = resources.iterator();
/** next publishable */
private Publishable next;

public boolean hasNext() {
  if (next!=null) return true;
  while (iterator.hasNext()) {
    Resource resource = iterator.next();
    if (resource instanceof Publishable) {
      next = (Publishable)resource;
      return true;
    }
  }
  return false;
}

public Publishable next() {
  if (!hasNext()) {
    throw new NoSuchElementException("No more publishables.");
  }
  Publishable publishable = next;
  next = null;
  return publishable;
}
}
}
