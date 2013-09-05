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
package com.esri.gpt.control.webharvest.client.dcat;

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.client.waf.DestroyableResource;
import com.esri.gpt.framework.dcat.DcatIterableAdaptor;
import com.esri.gpt.framework.dcat.DcatParser;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * DCAT iterable adaptor.
 */
class DCATRootResource implements DestroyableResource {
  private IterationContext context;
  private DCATInfo info;
  private DCATProxy proxy;
  private DCATAdaptor adaptor;

  /**
   * Creates instance of the resource.
   * @param context iteration context
   * @param info info
   * @param proxy proxy
   */
  public DCATRootResource(IterationContext context, DCATInfo info, DCATProxy proxy) {
    this.context = context;
    this.info = info;
    this.proxy = proxy;
  }

  @Override
  public void destroy() {
    if (adaptor!=null) {
      adaptor.close();
      adaptor=null;
    }
  }

  @Override
  public Iterable<Resource> getNodes() {
    return new Iterable<Resource>() {
      @Override
      public Iterator<Resource> iterator() {
        return new ResourceIterator();
      }
    };
  }
  
  private class ResourceIterator extends ReadOnlyIterator<Resource> {
    private Iterator<Publishable> iterator;
    private Resource resource;
    private boolean noMore;

    @Override
    public boolean hasNext() {
      if (resource!=null) {
        return true;
      }
      if (noMore) {
        return false;
      }
      if (adaptor==null) {
        try {
          URL url = new URL(info.getUrl());
          adaptor = new DCATAdaptor(proxy, new DcatIterableAdaptor(new DcatParser(url.openStream())));
          iterator = adaptor.iterator();
        } catch (IOException ex) {
          context.onIterationException(ex);
          noMore = true;
          return false;
        }
      }
      while (iterator.hasNext()) {
        Publishable next = iterator.next();
        resource = next;
        return true;
      }
      return false;
    }

    @Override
    public Resource next() {
      if (resource==null) {
        throw new NoSuchElementException();
      }
      return resource;
    }
  }
}
