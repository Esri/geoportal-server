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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolDCAT;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.common.CommonCapabilities;
import com.esri.gpt.framework.dcat.DcatParser;
import com.esri.gpt.framework.dcat.DcatParserAdaptor;
import com.esri.gpt.framework.dcat.DcatVersion;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.CommonPublishable;
import com.esri.gpt.framework.resource.common.UrlUri;
import com.esri.gpt.framework.resource.query.Capabilities;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import org.apache.commons.io.IOUtils;

/**
 * DCAT query builder.
 */
public class DCATQueryBuilder implements QueryBuilder {

  /** logger */
  private static final Logger LOGGER = Logger.getLogger(DCATQuery.class.getCanonicalName());
  /** capabilities */
  private static final Capabilities capabilities = new DCATCommonCapabilities();
  /** iteration context */
  private IterationContext context;
  /** service info */
  private DCATInfo info;

  /**
   * Creates instance of the builder.
   * @param context iteration context
   * @param protocol protocol
   * @param url URL
   */
  public DCATQueryBuilder(IterationContext context, HarvestProtocolDCAT protocol, String url) {
    if (context == null) {
      throw new IllegalArgumentException("No context provided.");
    }
    this.context = context;
    this.info = new DCATInfo(url,protocol.getFormat());
  }

  @Override
  public Capabilities getCapabilities() {
    return capabilities;
  }

  @Override
  public Query newQuery(Criteria crt) {
    Query q = new DCATQuery(context, info, crt);
    LOGGER.log(Level.FINER, "Query created: {0}", q);
    return q;
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
  public Native getNativeResource() {
    DCATIteratorAdaptor adaptor = null;
    try {
      URL url = new URL(info.getUrl());
      adaptor = new DCATIteratorAdaptor(info.getFormat(), new DcatParserAdaptor(new DcatParser(openStream(url))));
      Iterator<Publishable> iterator = adaptor.iterator();
      if (iterator.hasNext() && adaptor.getDcatVersion().compareTo(DcatVersion.DV10)==0) {
        return new NativeImpl(iterator.next().getContent());
      }
    } catch (Exception ex) {
      LOGGER.log(Level.WARNING, "Error reading native resource.", ex);
    } finally {
      if (adaptor!=null) {
        adaptor.close();
      }
    }
    return null;
  }


/**
 * Native implementation.
 */
  private class NativeImpl extends CommonPublishable implements Native {
      private String content;

      public NativeImpl(String content) {
        this.content = content;
      }

      @Override
      public SourceUri getSourceUri() {
        return new UrlUri(info.getUrl());
      }

      @Override
      public String getContent() throws IOException {
        return content;
      }
  }
  
  /**
   * DCAT capabilities.
   */
  private static class DCATCommonCapabilities extends CommonCapabilities {

    @Override
    public boolean canQueryFromDate() {
      return true;
    }

    @Override
    public boolean canQueryToDate() {
      return true;
    }
  }
  
}
