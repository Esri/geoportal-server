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
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import org.apache.commons.net.ftp.FTPFile;

/**
 * FTP folder.
 */
class FtpFolder implements Resource {

  protected IterationContext iterationContext;
  protected FtpClientRequest ftpClient;
  protected Criteria criteria;
  private String folder;
  
  protected FTPFile[] files;
  private Iterable<Resource> nodes;

  public FtpFolder(IterationContext iterationContext, FtpClientRequest ftpClient, Criteria criteria, String folder) {
    this.iterationContext = iterationContext;
    this.ftpClient = ftpClient;
    this.criteria = criteria;
    this.folder = Val.chkStr(folder);
  }

  @Override
  public Iterable<Resource> getNodes() {
    if (nodes==null) {
      nodes =  new FtpFileIterable(iterationContext, getFtpClient(), criteria, getFiles(), folder);
    }
    return nodes;
  }
  
  public final FTPFile[] getFiles() {
    if (files==null) {
        try {
          files = getFtpClient().listFiles(folder);
        } catch (IOException ex) {
          iterationContext.onIterationException(ex);
          files = new FTPFile[]{};
        }
    }
    return files;
  }
  
  /**
   * Gets request.
   * @return request
   */
  public final FtpClientRequest getFtpClient() {
    return ftpClient;
  }
}
