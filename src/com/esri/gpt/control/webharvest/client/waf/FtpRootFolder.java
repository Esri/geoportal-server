/*
 * Copyright 2011 Esri.
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
package com.esri.gpt.control.webharvest.client.waf;

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.query.Criteria;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPFile;

/**
 * Ftp root folder.
 */
class FtpRootFolder implements DestroyableResource {

  protected static final Logger LOGGER = Logger.getLogger(FtpRootFolder.class.getCanonicalName());
  
  protected IterationContext iterationContext;
  protected FtpClientRequest client;
  protected WafInfo info;
  protected Criteria criteria;
  
  protected FTPFile[] files;
  
  private Iterable<Resource> nodes;

  public FtpRootFolder(IterationContext context, WafInfo info, Criteria criteria) {
    this.iterationContext = context;
    this.info = info;
    this.criteria = criteria;
    try {
      this.client = new FtpClientRequest(new URL(info.getUrl()), info.newCredentialProvider());
    } catch (MalformedURLException ex) {
      Logger.getLogger(FtpRootFolder.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public final FTPFile[] getFiles() {
    if (files==null) {
        try {
          files = getFtpClient().listFiles(getFtpClient().getRootFolder());
        } catch (IOException ex) {
          iterationContext.onIterationException(ex);
          files = new FTPFile[]{};
        }
    }
    return files;
  }
  
  public final FtpClientRequest getFtpClient() {
    return client;
  }

  @Override
  public Iterable<Resource> getNodes() {
    if (nodes==null) {
      nodes = new FtpRootFileIterable(iterationContext, getFtpClient(), criteria, getFiles());
    }
    return nodes;
  }
  
  @Override
  public void destroy() {
    if (client!=null) {
      client.disconnect();
      client = null;
    }
  }
}
