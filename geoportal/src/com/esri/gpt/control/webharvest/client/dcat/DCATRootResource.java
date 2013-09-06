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
import com.esri.gpt.framework.dcat.DcatParserAdaptor;
import com.esri.gpt.framework.dcat.DcatParser;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * DCAT iterable adaptor.
 */
class DCATRootResource implements DestroyableResource {
  private IterationContext context;
  private DCATInfo info;
  private DCATProxy proxy;
  private DCATIteratorAdaptor adaptor;

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
  
  /**
   * Resource iterator.
   */
  private class ResourceIterator extends ReadOnlyIterator<Resource> {
    private boolean paginated = isPaginated();
    private Iterator<Publishable> iterator;
    private Resource resource;
    private boolean noMore;
    private long totalCount;
    private long passCount;
    
    private URL getNextUrl() throws MalformedURLException {
      String sUrl = info.getUrl();
      if (sUrl.contains("{max}") && sUrl.contains("{start}")) {
        sUrl = sUrl.replace("{max}", "10").replace("{start}",Long.toString(1L+totalCount));
      } else if (sUrl.contains("{max}") && sUrl.contains("{start}")) {
        sUrl = sUrl.replace("{max}", "10").replace("{page}",Long.toString(1L+totalCount/10));
      }
      return new URL(sUrl);
    }
    
    private boolean isPaginated() {
      String sUrl = info.getUrl();
      if (sUrl.contains("{max}") && sUrl.contains("{start}")) {
        return true;
      } else if (sUrl.contains("{max}") && sUrl.contains("{start}")) {
        return true;
      }
      return false;
    }

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
          passCount = 0;
          URL url = getNextUrl();
          adaptor = new DCATIteratorAdaptor(proxy, new DcatParserAdaptor(new DcatParser(url.openStream())));
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

      if (paginated && passCount>0) {
        adaptor.close();
        adaptor = null;
        return hasNext();
      } else {
        noMore = true;
      }
      
      return false;
    }

    @Override
    public Resource next() {
      if (resource==null) {
        throw new NoSuchElementException();
      }
      totalCount++;
      passCount++;
      Resource result = resource;
      resource = null;
      return result;
    }
  }
}
