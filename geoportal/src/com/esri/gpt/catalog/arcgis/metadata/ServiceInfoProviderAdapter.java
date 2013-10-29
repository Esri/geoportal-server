/*
 * Copyright 2013 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.catalog.arcgis.metadata;

import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author Esri
 */
public class ServiceInfoProviderAdapter implements Iterable<IServiceInfoProvider> {

  private Iterable<Resource> resources;

  /**
   * Creates instance of the adapter.
   *
   * @param resources collection of resources
   */
  public ServiceInfoProviderAdapter(Iterable<Resource> resources) {
    this.resources = resources != null ? resources : new ArrayList<Resource>();
  }

  @Override
  public Iterator<IServiceInfoProvider> iterator() {
    return new ServiceInfoProviderIterator();
  }

  /**
   * Publishable iterator.
   */
  private class ServiceInfoProviderIterator extends ReadOnlyIterator<IServiceInfoProvider> {

    /**
     * iterator
     */
    private Iterator<Resource> iterator = resources.iterator();
    /**
     * next publishable
     */
    private IServiceInfoProvider next;

    @Override
    public boolean hasNext() {
      if (next != null) {
        return true;
      }
      while (iterator.hasNext()) {
        Resource resource = iterator.next();
        if (resource instanceof IServiceInfoProvider && ((IServiceInfoProvider) resource).getServiceInfo()!=null) {
          next = (IServiceInfoProvider) resource;
          return true;
        }
      }
      return false;
    }

    @Override
    public IServiceInfoProvider next() {
      if (!hasNext()) {
        throw new NoSuchElementException("No more publishables.");
      }
      IServiceInfoProvider publishable = next;
      next = null;
      return publishable;
    }
  }
}
