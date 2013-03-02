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
package com.esri.gpt.control.webharvest.client.csw;

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.common.CommonResult;
import com.esri.gpt.framework.resource.adapters.JoinResourcesAdapter;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.Result;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

/**
 * CSW query.
 */
class CswQuery implements Query {

/** logger */
private static final Logger LOGGER = Logger.getLogger(CswQuery.class.getCanonicalName());
/** iteration context */
private IterationContext context;
/** CSW proxy */
private CswProxy proxy;
/** query criteria */
private Criteria criteria;

/**
 * Creates instance of the query.
 * @param context iteration context
 * @param proxy service proxy
 * @param criteria query criteria
 */
public CswQuery(IterationContext context, CswProxy proxy, Criteria criteria) {
  if (context == null)
    throw new IllegalArgumentException("No context provided.");
  if (proxy == null)
    throw new IllegalArgumentException("No proxy provided.");
  this.context = context;
  this.proxy = proxy;
  this.criteria = criteria;
}

  @Override
public Result execute() {
  LOGGER.finer("Executing query: " + this);
  Result r = new CommonResult(new JoinResourcesAdapter(new NativeIterable(), new CswFolders(context, proxy, criteria))) {
    @Override
    public void destroy() {
      proxy.destroy();
    }
  };
  LOGGER.finer("Completed query execution: " + this);
  return r;
}

@Override
public String toString() {
  return "{protocol: " + proxy + ", criteria:" + criteria + "}";
}

/**
 * Iterable over native metadata.
 */
private class NativeIterable implements Iterable<Resource> {

    @Override
  public Iterator<Resource> iterator() {
    return new NativeIterator();
  }

  /**
   * Native iterator.
   */
  private class NativeIterator extends ReadOnlyIterator<Resource> {
    boolean hasNext = true;

    public boolean hasNext() {
      return hasNext;
    }

    public Resource next() {
      if (!hasNext()) {
        throw new NoSuchElementException("No more publishables.");
      }
      hasNext = false;
      return proxy.getNativeResource();
    }
  }
}

}
