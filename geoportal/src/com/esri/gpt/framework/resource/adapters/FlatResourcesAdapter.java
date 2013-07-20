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
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Flattes collection of resources.
 */
public class FlatResourcesAdapter implements Iterable<Resource> {
/** collection of resources */
private Iterable<? extends Resource> resources;

/**
 * Creates instance of the adapter.
 * @param resources collection of resources
 */
public FlatResourcesAdapter(Iterable<? extends Resource> resources) {
  this.resources = resources;
}

/**
 * Creates instance of the adapter.
 * @param resource collection of resources
 */
public FlatResourcesAdapter(Resource resource) {
  this(Arrays.asList(new Resource[]{resource}));
}

@Override
public Iterator<Resource> iterator() {
  return new FlatResourcesIterator();
}

/**
 * Flat resources iterator.
 */
private class FlatResourcesIterator extends ReadOnlyIterator<Resource> {

/** stack of iterators */
private LinkedList<Iterator<? extends Resource>> stack = new LinkedList<Iterator<? extends Resource>>();
{
  Iterator<? extends Resource> iter = resources.iterator();
  if (iter.hasNext()) {
    stack.addLast(iter);
  }
}

@Override
public boolean hasNext() {
  if (stack.size()==0) return false;
  if (stack.getLast().hasNext()) return true;
  stack.removeLast();
  return hasNext();
}

@Override
public Resource next() {
  if (!hasNext()) {
    throw new NoSuchElementException("No more resources.");
  }
  Resource resource = stack.getLast().next();
  while (stack.size()>0 && !stack.getLast().hasNext()) {
    stack.removeLast();
  }
  Iterator<Resource> iter = resource.getNodes().iterator();
  if (iter.hasNext()) {
    stack.addLast(iter);
  }
  return resource;
}
}
}
