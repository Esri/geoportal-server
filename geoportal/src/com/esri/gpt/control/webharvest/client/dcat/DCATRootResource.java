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
import com.esri.gpt.framework.dcat.DcatVersion;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.commons.io.IOUtils;

/**
 * DCAT iterable adaptor.
 */
class DCATRootResource implements DestroyableResource {
  private final IterationContext context;
  private final DCATInfo info;
  private DCATIteratorAdaptor adaptor;

  /**
   * Creates instance of the resource.
   * @param context iteration context
   * @param info info
   */
  public DCATRootResource(IterationContext context, DCATInfo info) {
    this.context = context;
    this.info = info;
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
    private final boolean paginated = isPaginated();
    private Iterator<Publishable> iterator;
    private Resource resource;
    private boolean noMore;
    private long totalCount;
    private long passCount;
    
    /**
     * Gets URL to the next chunk of data.
     * @return URL to the next chunk of data
     * @throws MalformedURLException if creating URL fails
     */
    private URL getNextUrl() throws MalformedURLException {
      String sUrl = info.getUrl();
      if (sUrl.contains("{max}") && sUrl.contains("{start}")) {
        sUrl = sUrl.replace("{max}", "10").replace("{start}",Long.toString(1L+totalCount));
      } else if (sUrl.contains("{max}") && sUrl.contains("{start}")) {
        sUrl = sUrl.replace("{max}", "10").replace("{page}",Long.toString(1L+totalCount/10));
      }
      return new URL(sUrl);
    }
    
    /**
     * Checks if this is harvesting of the paginated DCAT.
     * @return <code>true</code> if DCAT seems to be paginated
     */
    private boolean isPaginated() {
      String sUrl = info.getUrl();
      if (sUrl.contains("{max}") && sUrl.contains("{start}")) {
        return true;
      } else if (sUrl.contains("{max}") && sUrl.contains("{start}")) {
        return true;
      }
      return false;
    }
    
    /**
     * Open stream.
     * @return stream
     * @throws IOException if opening stream fails
     */
    private InputStream openStream(URL url) throws IOException {
      
      // create temporary file
      final File tempFile = File.createTempFile("dcat_", Long.toString(System.nanoTime()));
      
      InputStream in = null;
      OutputStream out = null;
      
      try {
        
        // copy URL stream into temporary file
        in = url.openStream();
        out = new FileOutputStream(tempFile);
        IOUtils.copy(in, out);
        
      } catch (IOException ex) {
        
        // close temporary file stream and delete file
        IOUtils.closeQuietly(out);
        out = null;
        tempFile.delete();
        
        // rethrow exception
        throw ex;
        
      } catch (RuntimeException ex) {
        
        // close temporary file stream and delete file
        IOUtils.closeQuietly(out);
        out = null;
        tempFile.delete();
        
        // rethrow exception
        throw ex;
        
      } finally {
        
        // close all quietly
        IOUtils.closeQuietly(out);
        IOUtils.closeQuietly(in);
        
      }
      
      return new FileInputStream(tempFile) {
        @Override
        public void close() throws IOException {
          super.close();
          // delete temporary file
          tempFile.delete();
        }
      };
    }
    
    @Override
    public boolean hasNext() {
      // absolutly no more data
      if (noMore) {
        return false;
      }
      // still cached? there is next
      if (resource!=null) {
        return true;
      }
      // no adaptor? create one
      if (adaptor==null) {
        try {
          passCount = 0;
          adaptor = new DCATIteratorAdaptor(info.getFormat(), new DcatParserAdaptor(new DcatParser(openStream(getNextUrl()))));
          iterator = adaptor.iterator();
        } catch (IOException ex) {
          context.onIterationException(ex);
          noMore = true;
          return false;
        }
      }
      
      // check for the next available data
      while (iterator.hasNext()) {
        Publishable next = iterator.next();
        if (totalCount==0 && adaptor.getDcatVersion().compareTo(DcatVersion.DV10)==0) {
          totalCount++;
          continue;
        }
        resource = next;
        return true;
      }

      // nothing more is available? so how much records have been found in the
      // last chunk of data? If more than one close current adaptor and try again.
      // It will force to create next chunk of data.
      if (paginated && passCount>0) {
        adaptor.close();
        adaptor = null;
        return hasNext();
      } else {
        // last pass yeld zero records; than means no more records available at all
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
      // clear cached record (resource)
      resource = null;
      return result;
    }
  }
}
