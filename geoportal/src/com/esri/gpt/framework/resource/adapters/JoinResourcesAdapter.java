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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Join resources adapter.
 */
public class JoinResourcesAdapter implements Iterable<Resource> {
  /** first resource collection */
  private Iterable<Resource>  res1;
  /** second resource collection */
  private Iterable<Resource>  res2;

  /**
   * Creates instance of the adapter.
   * @param res1 first resource collection
   * @param res2 second resource collection
   */
  public JoinResourcesAdapter(Iterable<Resource> res1, Iterable<Resource> res2) {
    this.res1 = res1;
    this.res2 = res2;
  }

  public Iterator<Resource> iterator() {
    return new JoinResourcesIterator();
  }

  /**
   * Join resources iterator.
   */
  private class JoinResourcesIterator extends ReadOnlyIterator<Resource> {
    private LinkedList<Iterable<Resource>> list = new LinkedList<Iterable<Resource>>();
    {
      list.add(res1);
      list.add(res2);
    }
    private Iterator<Resource> iterator = null;

    public boolean hasNext() {
      if (iterator!=null && iterator.hasNext()) return true;
      if (!list.isEmpty()) {
        Iterable<Resource> col = list.poll();
        iterator = col.iterator();
        return hasNext();
      }
      return false;
    }

    public Resource next() {
      if (!hasNext()) {
        throw new NoSuchElementException("No more publishables.");
      }
      return iterator.next();
    }

  }
}
